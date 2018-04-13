/**
 * 
 */
package entity.sp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import utility.Global;
import utility.Utility;

/**
 * @author jmshi 
 * 		   In memory limited Integer version for our usage only Array of
 *         Arrays structure, since we already know the keyword ids are integer
 *         and start from certain value continuously rather than a hash/Btree of
 *         arrays to manage the inverted index
 * 
 *         This version is much faster than the disk version inverted index
 *         implementation since the disk version needs to create many many
 *         KeyData in postinglist. GC overhead is too heavy
 */

public class InvertedIndex {
	private Map<Integer, List<Integer>> invertedIndex = null;

	private Map<Integer, Integer> lengthCountMap = null;

	public InvertedIndex(String invertedIndexFile) throws Exception {
		this.loadInvertedIndex(invertedIndexFile);
	}
	
	/**
	 * only load postinglists of given keywords
	 * */
	public InvertedIndex(String invertedIndexFile, Integer[] qwords) throws Exception {
		this.loadInvertedIndex(invertedIndexFile, qwords);
	}

	private void loadInvertedIndex(String invertedIndexFile) throws Exception {
		invertedIndex = new HashMap<Integer, List<Integer>>();

		BufferedReader reader = Utility.getBufferedReader(invertedIndexFile);
		String line = reader.readLine();
		// first line is metadata in format: numlines#totalCount#

		int cntlines = 0;
		while ((line = reader.readLine()) != null) {
			cntlines++;
			if (line.contains(Global.delimiterPound)) {
				continue;
			}
			String[] splits = line.split(Global.delimiterLevel1);
			if (splits.length != 2) {
				throw new Exception("Inverted index " + line + "splits should be 2, but is " + splits.length
						+ " at line " + cntlines);
			}

			int keyword = Integer.parseInt(splits[0]);

			String[] idsStr = splits[1].split(Global.delimiterLevel2);

			ArrayList<Integer> ids = new ArrayList<Integer>(idsStr.length);
			for (int i = 0; i < idsStr.length; i++) {
				ids.add(i, Integer.parseInt(idsStr[i]));
			}
			invertedIndex.put(keyword, ids);

			if (cntlines % 100000 == 0) {
				System.out.print(cntlines + " postinglist loaded,");
			}
		}
	}
	
	private void loadInvertedIndex(String invertedIndexFile, Integer[] qwords) throws Exception {
		invertedIndex = new HashMap<Integer, List<Integer>>();

		Set<Integer> qwordSet = new HashSet<Integer>();
		for (int i = 0; i < qwords.length; i++) {
			qwordSet.add(qwords[i]);
		}
		
		BufferedReader reader = Utility.getBufferedReader(invertedIndexFile);
		String line = reader.readLine();
		// first line is metadata in format: numlines#totalCount#
		// keyword: id1,id2,...
		String[] stat = line.split(Global.delimiterPound);
		int numlines = Integer.parseInt(stat[0]);
		if (numlines != Global.numKeywords) {
			throw new Exception("configuration numKeywords is " + Global.numKeywords
					+ " but stat in read-file is " + numlines);
		}

		int cntlines = 0;
		while ((line = reader.readLine()) != null) {
			cntlines++;
			String[] splits = line.split(Global.delimiterLevel1);
			if (splits.length != 2) {
				throw new Exception("Inverted index " + line + "splits should be 2, but is " + splits.length
						+ " at line " + cntlines);
			}

			int keyword = Integer.parseInt(splits[0]);
			if (keyword >= Global.numNodes + Global.numKeywords || keyword < Global.numNodes) {
				throw new Exception("inverted index " + line + " kid out of range");
			}
			
			//only load postinglists of provided keywords
			if (!qwordSet.contains(keyword)) {
				continue;
			}

			String[] idsStr = splits[1].split(Global.delimiterLevel2);

			ArrayList<Integer> ids = new ArrayList<Integer>(idsStr.length);
			for (int i = 0; i < idsStr.length; i++) {
				ids.add(i, Integer.parseInt(idsStr[i]));
			}
			invertedIndex.put(keyword, ids);
			
			qwordSet.remove(keyword);
			System.out.print(keyword + "'s postinglist loaded,");

			if (qwordSet.size() == 0) {
				break;//postinglists of all keywords are loaded
			}		
		}
	}
	
	public Map<Integer, List<Integer>> getInvertedIndex(){
		return this.invertedIndex;
	}

	public List<Integer> readPostinglist(int index) {
		return invertedIndex.get(index);
	}

	public int size() {
		return invertedIndex.size();
	}
}
