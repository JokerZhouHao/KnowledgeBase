/**
 * 
 */
package precomputation.alpha;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.index.IndexWriter;

import entity.sp.PlaceRadiusNeighborhood;
import entity.sp.SortedList;
import entity.sp.SortedList.SortedListNode;
import precomputation.sp.IndexWordPNService;
import utility.Global;
import utility.GraphUtility;
import utility.TimeUtility;
import utility.Utility;

/**
 * Build the alpha wn inverted index part by part.
 * @author jmshi
 *
 */
public class WordPNIndexBuilder {
	
	// 临时存放alph place neighborhood
	class TempAlphaPN{
		private HashMap<Integer, String>[] eachLayerWN = null;
		
		public TempAlphaPN(int radius) {
			eachLayerWN = new HashMap[radius + 1];
		}
		
		public void clear() {
			for(HashMap<Integer, String> hm : eachLayerWN) {
				if(null != hm)	hm.clear();
			}
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for(HashMap<Integer, String> pIdToDateMap : eachLayerWN) {
				if(null == pIdToDateMap || pIdToDateMap.isEmpty()) {
					sb.append(Global.signEmptyLayer + Global.delimiterLayer);
					continue;
				}
				for(Entry<Integer, String> en : pIdToDateMap.entrySet()) {
					sb.append(en.getKey() + Global.delimiterLevel2 + en.getValue() + Global.delimiterSpace);
				}
				sb.append(Global.delimiterLayer);
			}
			return sb.toString();
		}
	}
	
	/**
	 * 输出alphaPN
	 * @param writer
	 * @param radius
	 * @param vid
	 * @param radiusWN
	 */
	private void outputAlphaPN(PrintWriter writer, int radius, int vid, TempAlphaPN alphaPN) {
		writer.print(vid + Global.delimiterLevel1);
		writer.print(alphaPN.toString());
		writer.println();
		writer.flush();
	}
	
	public static void buildingWordPN() throws Exception{
		if(!new File(Global.outputDirectoryPath).exists()) {
			throw new DirectoryNotEmptyException("目录outputDirectoryPath ： " + Global.outputDirectoryPath + "不存在");
		}
		if(!new File(Global.outputDirectoryPath + Global.indexWidPN ).exists()) {
			throw new DirectoryNotEmptyException("存放wPN索引的目录 ： " + Global.outputDirectoryPath + Global.indexWidPN + "不存在");
		}
		
		long start = System.currentTimeMillis();
		System.out.println("> 开始创建widPN索引 . . .");
		
		String inputDocFile = Global.placeWNFile;
		String outputIindexFile = Global.wordPNFile;
		int startKeyword = Global.numNodes;
		int endKeyword = Global.numNodes + Global.numKeywords;
		int interval = (endKeyword - startKeyword - 1)/5;
		
		// 在建索引过程中输出wPN文件
		boolean isOutput = false;
		PrintWriter writer = null;
		
		if(isOutput)	writer = new PrintWriter(outputIindexFile);
		int iindexSize = 0;
		int iindexTotalLength = 0;
		
		WordPNIndexBuilder alphaPNBuilder = new WordPNIndexBuilder();
		IndexWordPNService alphaIndexSer = new IndexWordPNService(Global.outputDirectoryPath + Global.indexWidPN);
		alphaIndexSer.openIndexWriter();
		
		TempAlphaPN radiusPN = null;
		
		///////////////////////////
//		startKeyword = 10358261;
		int offsetStart = Global.numNodes + Global.numKeywords;
		
		while (startKeyword < endKeyword) {
			HashMap<Integer, TempAlphaPN> alphaPNMap = new HashMap<>();
			System.out.println("processing keywords [" + startKeyword + "," + (startKeyword + interval) + "]");
			// build partial inverted index
			alphaPNBuilder.buildAlphaPN(startKeyword,
					startKeyword + interval, inputDocFile, alphaPNMap);
			// output partial inverted index
			for (int kid = startKeyword; kid <= startKeyword + interval; kid++) {
				if(null == (radiusPN = alphaPNMap.get(kid)))	continue;
				//a new keyword with nonempty posting list.
				iindexSize++;
				iindexTotalLength += 1;
				if(isOutput)	alphaPNBuilder.outputAlphaPN(writer, Global.radius, kid, radiusPN);
				
				// 解决pIdDates太长，Lucene无法处理
				String st = radiusPN.toString();
				if(st.length() < Global.MAX_STORED_STRING_LENGTH) {
					alphaIndexSer.addDoc(kid, st);
				}
//				if(st.length() > IndexWriter.MAX_STORED_STRING_LENGTH) {
//					int stSplitNum = st.length()%IndexWriter.MAX_STORED_STRING_LENGTH;
//					if(stSplitNum == 0 ) {
//						stSplitNum = st.length()/IndexWriter.MAX_STORED_STRING_LENGTH;
//					} else {
//						stSplitNum = st.length()/IndexWriter.MAX_STORED_STRING_LENGTH + 1;
//					}
//					alphaIndexSer.addDoc(kid, Global.delimiterPound + String.valueOf(offsetStart) + Global.delimiterPound + String.valueOf(offsetStart + stSplitNum));
//					int strStart = 0;
//					int strEnd = 0;
//					for(int i=0; i<stSplitNum; i++) {
//						strStart = strEnd;
//						strEnd = strStart + IndexWriter.MAX_STORED_STRING_LENGTH;
//						if(strEnd <= st.length()) {
//							alphaIndexSer.addDoc(offsetStart + i, st.substring(strStart, strEnd));
//						} else {
//							alphaIndexSer.addDoc(offsetStart + i, st.substring(strStart, st.length()));
//						}
//					}
//					offsetStart += stSplitNum;
//				} else {
//					alphaIndexSer.addDoc(kid, st);
//				}
				radiusPN.clear();
			}
			// clear and go to next batch
			alphaPNMap.clear();
			System.gc();
			startKeyword += interval + 1;
			
			/////////////////////////
//			break;
		}
		
		if(isOutput) {
			writer.flush();
			writer.close();
		}
		
		alphaIndexSer.closeIndexWriter();
		
		System.out.println("> 结束构造widPN索引，用时：" + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()));
	}
	
	private void buildAlphaPN(int startKeyword,
			int endKeyword, String inputDocFile, HashMap<Integer, TempAlphaPN> alphaPNMap) throws Exception {
		
		// Map<Integer, Set<String>> invertedIndex = new HashMap<Integer,
		// Set<String>>();
		// read nidKeywordListMap line by line to build inverted index
		BufferedReader reader = Utility.getBufferedReader(inputDocFile);
		String line;

//		int cntlines = 0;
		String[] layers = null;
		String[] widDates = null;
		String dates = null;
		int i, j, pid, wid;
		TempAlphaPN alphaPN = null;
		HashMap<Integer, String> tempMap = null;
		while ((line = reader.readLine()) != null) {
//			cntlines++;
//			if (line.contains(Global.delimiterPound)) {
//				continue;
//			}

//			if (cntlines % 10000 == 0) {
//				System.out.println("> 已处理" + cntlines + "个placeWN");
//			}
			i = line.indexOf(Global.delimiterLevel1);
			pid = Integer.parseInt(line.substring(0, i));
			layers = line.substring(i + Global.delimiterLevel1.length()).split(Global.delimiterLayer);
			if(layers.length != Global.radius+1)	continue;
			
			for(i = 0; i<layers.length; i++) {
				if(!layers[i].equals(Global.signEmptyLayer)) {
					widDates = layers[i].split(Global.delimiterSpace);
					for(String st : widDates) {
						j = st.indexOf(Global.delimiterLevel2);
						wid = Integer.parseInt(st.substring(0, j));
						if(wid < startKeyword || wid > endKeyword) {
							continue;
						}
						
						dates = st.substring(j + 1);
						if(null == (alphaPN = alphaPNMap.get(wid))) {
							alphaPN = new TempAlphaPN(Global.radius);
							alphaPNMap.put(wid, alphaPN);
						}
						if(null == (tempMap = alphaPN.eachLayerWN[i])) {
							tempMap = alphaPN.eachLayerWN[i] = new HashMap<>();
						}
						tempMap.put(pid, dates);
					}
				}
			}
		}
		reader.close();
	}
	
	/**
	 * 主函数
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ArrayList<Integer> radiusList = new ArrayList<>();
//		radiusList.add(1);
//		radiusList.add(2);
//		radiusList.add(3);
		radiusList.add(5);
//		HashMap<Integer, String> rec = new HashMap<>();
		BufferedWriter bw = new BufferedWriter(new FileWriter(Global.outputDirectoryPath + "buildRadiusTime.txt", Boolean.TRUE));
		for(int radius : radiusList) {
			Long start = System.currentTimeMillis();
			Global.radius = radius;
			Global.indexWidPN = "wid_pn_" + String.valueOf(Global.radius) + File.separator;
			if(!(new File(Global.outputDirectoryPath + Global.indexWidPN).exists())) {
				new File(Global.outputDirectoryPath + Global.indexWidPN).mkdir();
			}
			Global.placeWNFile = Global.outputDirectoryPath + "placeWN" + Global.rtreeFlag + Global.rtreeFanout + "." + Global.radius + Global.dataVersion;
//			PlaceWNPrecomputation.BuildingPlaceWN();
			WordPNIndexBuilder.buildingWordPN();
			new File(Global.placeWNFile).delete();
			System.out.println();
//			rec.put(radius, TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()));
			bw.write(String.valueOf(radius) + " : " + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()) + '\n');
			bw.flush();
		}
		bw.close();
	}
}
