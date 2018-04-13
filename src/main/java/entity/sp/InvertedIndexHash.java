package entity.sp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import jdbm.RecordManagerFactory;
import jdbm.recman.CacheRecordManager;
import neustore.base.ByteArray;
import neustore.base.DBBuffer;
import neustore.base.DBBufferReturnElement;
import neustore.base.DBIndex;
import neustore.base.Data;
import neustore.base.FloatData;
import neustore.base.IntKey;
import neustore.base.Key;
import neustore.base.KeyData;
import neustore.base.LRUBuffer;
import neustore.heapfile.HeapFilePage;
import utility.Global;
import utility.Utility;

/**
 * Inverted index whose fast lookup is supported by a in-memory hash map, named wordPageMap.
 * @author jieming
 *
 */
public class InvertedIndexHash extends DBIndex {

	protected CacheRecordManager cacheRecordManager;

	protected long recid;
	// The map key is wordID, and the value is the pageID where the list of the word is stored
	protected HashMap<Integer, Long> wordPageMap;
	// equals to number of entries per node in the btree
	protected int pageSize; 

	private int numRecs;

	protected Key sampleKey;

	protected Data sampleData;

	protected Hashtable invertedlists;

	protected int count;

	public InvertedIndexHash(DBBuffer _buffer, String filename, boolean isCreate, int pagesize, int buffersize)
			throws Exception {
		super(_buffer, filename, isCreate);
		sampleKey = new IntKey(0);
		sampleData = new FloatData(0);

		int cachesize = buffersize;
		cacheRecordManager = new CacheRecordManager(RecordManagerFactory.createRecordManager(filename),
				cachesize, true);
		pageSize = pagesize;

		small_num = 0.1;

		if (!isCreate) {
			loadWordPageMap(filename.substring(0, filename.indexOf(".iindex")) + "." + "wordPageMap.txt");
		} else {
			wordPageMap = new HashMap<Integer, Long>();
		}
	}

	protected void readIndexHead(byte[] indexHead) {
		ByteArray ba = new ByteArray(indexHead, true);
		try {
			numRecs = ba.readInt();
		} catch (IOException e) {
		}
	}

	protected void writeIndexHead(byte[] indexHead) {
		ByteArray ba = new ByteArray(indexHead, false);
		try {
			ba.writeInt(numRecs);

		} catch (IOException e) {
		}
	}

	protected void initIndexHead() {
		numRecs = 0;
	}

	public int numRecs() {
		return numRecs;
	}

	protected HeapFilePage readPostingListPage(long pageID, boolean isWeighted) throws IOException {
		DBBufferReturnElement ret = buffer.readPage(file, pageID);
		HeapFilePage thePage = null;
		if (ret.parsed) {
			thePage = (HeapFilePage) ret.object;
		} else {
			thePage = new HeapFilePage(pageSize, sampleKey, sampleData);
			thePage.read((byte[]) ret.object, isWeighted);
		}
		return thePage;
	}

	/**
	 * Suppose a large inverted index in TXT file format is given, It is
	 * impossible to load such large inverted index into memory all together.
	 * 
	 * We want to convert this TXT inverted index into the DISK-BASED indexing
	 * structure defined in this class Rather than loading all posting lists and
	 * then writing all of them into disk
	 * 
	 * We need to read line by line, i.e., posting list by posting list And
	 * write into disk posting list by posting list.
	 * 
	 * @throws Exception
	 * 
	 * */

	protected void convertingInvertedIndexUnweightedLineByLine(String indexname, String inputFile)
			throws Exception {
		BufferedReader reader = Utility.getBufferedReader(inputFile);
		String line;
		String[] postinglist;
		String[] docIDsStr;
		ArrayList docIDs = new ArrayList();

		// for each line
		int cntlines = 0;
		while ((line = reader.readLine()) != null) {
			cntlines++;
			if (line.contains(Global.delimiterPound)) {
				continue;
			}
			// load posting list
			postinglist = line.split(Global.delimiterLevel1);
			if (postinglist.length != 2) {
				throw new Exception("posting list splits error: " + line);
			}
			int word = Integer.parseInt(postinglist[0]);
			docIDsStr = postinglist[1].split(Global.delimiterLevel2);
			docIDs.clear();
			for (int i = 0; i < docIDsStr.length; i++) {
				docIDs.add(new KeyData(new IntKey(Integer.parseInt(docIDsStr[i])), null));
			}
			// store posting list
			long newPageID = allocate();
			wordPageMap.put(word, newPageID);
			HeapFilePage newPage = new HeapFilePage(pageSize, sampleKey, sampleData);
			storelist(docIDs, newPage, newPageID);

			cacheRecordManager.commit();
			docIDs.clear();
//			System.out.println(cntlines);
			if (cntlines % 200000 == 0) {
//				System.out.println("suggesting to do garbage collection");
				System.gc();
			}
		}

		cacheRecordManager.commit();
		propagateWordPageMap(indexname);
	}

	
	protected void convertingInvertedIndexWeightedLineByLine(String indexname, String inputFile)
			throws Exception {
		BufferedReader reader = Utility.getBufferedReader(inputFile);
		String line;
		String[] postinglist;
		ArrayList docIDs = new ArrayList();
		HeapFilePage newPage = new HeapFilePage(pageSize, sampleKey, sampleData);
		// for each line
		int cntlines = 0;
		int sumLength = 0;

		while ((line = reader.readLine()) != null) {
			if (line.contains(Global.delimiterPound)) {
				continue;
			}
			cntlines++;
			// load posting list
			postinglist = line.split(Global.delimiterSpace);
			if (postinglist.length < 2) {
				throw new Exception("posting list splits error: " + line);
			}
			int word = Integer.parseInt(postinglist[0]);
			docIDs.clear();
			for (int i = 1; i < postinglist.length - 1; i += 2) {
				int docID = Integer.parseInt(postinglist[i]);
				float weight = Float.parseFloat(postinglist[i + 1]);
				docIDs.add(new KeyData(new IntKey(docID), new FloatData(weight)));
			}
			sumLength += (postinglist.length - 1) / 2;
			// store posting list
			long newPageID = allocate();
			wordPageMap.put(word, newPageID);
			newPage.reset();
			storelistNoNewPage(docIDs, newPage, newPageID);

			cacheRecordManager.commit();
			docIDs.clear();
		}
		cacheRecordManager.commit();
		propagateWordPageMap(indexname);
	}
	
	private void storelistNoNewPage(ArrayList list, HeapFilePage newPage, long newPageID) throws IOException {
		for (int j = 0; j < list.size(); j++) {
			KeyData rec = (KeyData) list.get(j);
			IntKey key = (IntKey) rec.key;
			FloatData data = (FloatData) rec.data;
			
			int availableByte = newPage.getAvailableBytes();
			int neededByte = data == null ? key.size() : key.size() + data.size();
			if (availableByte < neededByte) {

				long nextPageID = allocate();
				newPage.setNextPageID(nextPageID);
				buffer.writePage(file, newPageID, newPage);
				cacheRecordManager.commit();
				newPageID = nextPageID;
				newPage.reset();
			}
			newPage.insert(key, data);
		}
		buffer.writePage(file, newPageID, newPage);
		cacheRecordManager.commit();
		newPage.reset();
	}

	private float storelist(ArrayList list, HeapFilePage newPage, long newPageID) throws IOException {
		float weight = Float.NEGATIVE_INFINITY;
		for (int j = 0; j < list.size(); j++) {
			KeyData rec = (KeyData) list.get(j);
			IntKey key = (IntKey) rec.key;
			FloatData data = (FloatData) rec.data;
			if (data != null) {
				weight = Math.max(weight, data.data);
			}
			int availableByte = newPage.getAvailableBytes();
			int neededByte = data == null ? key.size() : key.size() + data.size();
			if (availableByte < neededByte) {

				long nextPageID = allocate();
				newPage.setNextPageID(nextPageID);
				buffer.writePage(file, newPageID, newPage);
				cacheRecordManager.commit();
				newPage.clear();
				newPageID = nextPageID;
				newPage = new HeapFilePage(pageSize, sampleKey, sampleData);
			}
			newPage.insert(key, data);
		}
		buffer.writePage(file, newPageID, newPage);
		cacheRecordManager.commit();
		newPage.clear();
		return weight;// get the maximum weight of the list
	}

	public double small_num;

	public ArrayList readPostingList(int wordID, boolean isWeighted) throws IOException {
		ArrayList list = new ArrayList();
		Long pageID = wordPageMap.get(wordID);
		if (pageID == null) {
			return null;
		} else {
			long firstPageID = pageID;

			while (firstPageID != -1) {
				HeapFilePage thePage = readPostingListPage(firstPageID, isWeighted);
				for (int i = 0; i < thePage.numRecs(); i++) {
					KeyData rec = thePage.get(i);
					list.add(rec);
				}
				firstPageID = thePage.getNextPageID();
			}

		}
		return list;
	}

	public Map<Integer, Float> readPostingListMap(int wordID, boolean isWeighted) throws IOException {
		Map<Integer, Float> postinglist = new HashMap<Integer, Float>();
		Long pageID = wordPageMap.get(wordID);
		if (pageID == null) {
			// System.out.println("Posting List not found " + wordID);
			// System.exit(-1);
			return null;
		} else {
			long firstPageID = pageID;

			while (firstPageID != -1) {
				// System.out.println(" page " + firstPageID);
				HeapFilePage thePage = readPostingListPage(firstPageID, isWeighted);
				for (int i = 0; i < thePage.numRecs(); i++) {
					KeyData rec = thePage.get(i);
					int id = ((IntKey) rec.key).key;
					float weight = ((FloatData) rec.data).data;
					postinglist.put(id, weight);
				}
				// System.out.println("size " + thePage.numRecs());
				firstPageID = thePage.getNextPageID();
			}
		}
		return postinglist;
	}

	private void propagateWordPageMap(String indexname) throws FileNotFoundException,
			UnsupportedEncodingException {
		Utility<Integer, Long> uti = new Utility<Integer, Long>();
		uti.outputMap(indexname + "." + "wordPageMap.txt", wordPageMap);
	}

	public void loadWordPageMap(String inputFile) throws Exception {
		wordPageMap = new HashMap<Integer, Long>();
		BufferedReader reader = Utility.getBufferedReader(inputFile);
		String line;

		while ((line = reader.readLine()) != null) {
			String[] wordPage = line.split(": ");
			if (wordPage.length != 2) {
				throw new Exception("word page map splits error, should be length 2 but not: " + line);
			}
			wordPageMap.put(Integer.parseInt(wordPage[0]), Long.parseLong(wordPage[1]));
		}
	}

	/**
	 * Disk storer of inverted index in plain text format.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 6) {
			throw new Exception(
					"Usage: runnable configFile buffersize pagesize iidxNameFlag isWeighted(y/n) inputfile");
		}

		boolean isCreate = true;
		Utility.loadInitialConfig(args[0]);
		int buffersize = Integer.parseInt(args[1]);
		int pagesize = Integer.parseInt(args[2]);
		String iidxNameFlag = args[3];
		String isWeightStr = args[4];
		String inputFile = args[5];

		String indexName = Global.outputDirectoryPath + iidxNameFlag + Global.diskFlag + pagesize
				+ Global.dataVersion;
		LRUBuffer buffer = new LRUBuffer(buffersize, pagesize);
		InvertedIndexHash iindex = new InvertedIndexHash(buffer, indexName + ".iindex", isCreate, pagesize,
				buffersize);// ATTENTION: pagesize

		if (isWeightStr.contains("y")) {
			iindex.convertingInvertedIndexWeightedLineByLine(indexName, inputFile);
		} else {
			iindex.convertingInvertedIndexUnweightedLineByLine(indexName, inputFile);
		}
		iindex.close();
	}
}