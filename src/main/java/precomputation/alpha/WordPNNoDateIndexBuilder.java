/**
 * 
 */
package precomputation.alpha;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class WordPNNoDateIndexBuilder {
	
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
				if(sb.length() >= Global.MAX_PN_LENGTH) {
					return null;
				}
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
		
		public byte[] toBytes() {
			int len = 0;
			
			// 计算要转化为bytes的长度
			for(HashMap<Integer, String> pIdToDateMap : eachLayerWN) {
				len++;
				if(null == pIdToDateMap || pIdToDateMap.isEmpty()) {
					continue;
				}
				len += pIdToDateMap.size();
				
				if(len*4 >= Global.MAX_PN_LENGTH) {
					long pidNum = 0;
					for(HashMap<Integer, String> pIdToDateMap1 : eachLayerWN) {
						pidNum += pIdToDateMap1.size();
					}
					System.out.println("> W_PN_NODATE too long > pidNum = " + pidNum + " MAX_PN_LENGTH = " + Global.MAX_PN_LENGTH);
					return null;
				}
//				for(Entry<Integer, String> en : pIdToDateMap.entrySet()) {
//					len++;
//					len++;
//					len += en.getValue().split(Global.delimiterDate).length;
//					if(len*4 >= Global.MAX_PN_LENGTH) {
//						long pidNum = 0;
//						for(HashMap<Integer, String> pIdToDateMap1 : eachLayerWN) {
//							pidNum += pIdToDateMap1.size();
//						}
//						System.out.println("> W_PN too long > pidNum = " + pidNum + " MAX_PN_LENGTH = " + Global.MAX_PN_LENGTH);
//						return null;
//					}
//				}
			}
			
			// 转化为bytes
			String strArr[] = null;
			ByteBuffer bb = ByteBuffer.allocate(len * 4);
			bb.rewind();
			for(HashMap<Integer, String> pIdToDateMap : eachLayerWN) {
				if(null == pIdToDateMap || pIdToDateMap.isEmpty()) {
					bb.putInt(0);
					continue;
				}
				bb.putInt(pIdToDateMap.size());
				for(Entry<Integer, String> en : pIdToDateMap.entrySet()) {
					bb.putInt(en.getKey());
//					strArr = en.getValue().split(Global.delimiterDate);
//					bb.putInt(strArr.length);
//					for(String st : strArr) {
//						bb.putInt(Integer.parseInt(st));
//					}
				}
			}
			return bb.array();
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
		if(!new File(Global.outputDirectoryPath + Global.indexWidPNNodate).exists()) {
			throw new DirectoryNotEmptyException("存放wPNNodate索引的目录 ： " + Global.outputDirectoryPath + Global.indexWidPNNodate + "不存在");
		}
		
		long start = System.currentTimeMillis();
		System.out.println("> 开始创建widPN索引 . . .");
		
		String inputDocFile = Global.placeWNNodateFile;
		String outputIindexFile = Global.wordPNNodateFile;
		int startKeyword = Global.numNodes;
		int endKeyword = Global.numNodes + Global.numKeywords;
		int interval = (endKeyword - startKeyword - 1)/5;
		
		// 在建索引过程中输出wPN文件
		boolean isOutput = false;
		PrintWriter writer = null;
		
		if(isOutput)	writer = new PrintWriter(outputIindexFile);
		int iindexSize = 0;
		int iindexTotalLength = 0;
		
		WordPNNoDateIndexBuilder alphaPNBuilder = new WordPNNoDateIndexBuilder();
		IndexWordPNService alphaIndexSer = new IndexWordPNService(Global.outputDirectoryPath + Global.indexWidPNNodate);
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
				
				// 解决pIdDates太长，Lucene无法处理，以字符串建索引
//				String st = radiusPN.toString();
//				if(null != st && st.length() < Global.MAX_STORED_STRING_LENGTH) {
//					alphaIndexSer.addDoc(kid, st);
//				}
				
				// 以二进制建索引
				byte[] bs = radiusPN.toBytes();
				if(null != bs) {
					alphaIndexSer.addBinDoc(kid, bs);
				}
				
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
	 * 批创建WN
	 * @throws Exception
	 */
	public static void batchBuildingWN(List<Integer> radiusList) throws Exception{
		System.out.print("> 开始创建radius=");
		for(int ii : radiusList) {
			System.out.print(String.valueOf(ii) + " ");
		}
		System.out.println("的WordPN . . . ");
		long startTime = System.currentTimeMillis();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(Global.outputDirectoryPath + "buildRadiusTime.txt", Boolean.TRUE));
		for(int radius : radiusList) {
			long start = System.currentTimeMillis();
			Global.radius = radius;
			System.out.println("> 开始处理radius=" + radius + "的wordPN . . . ");
			Global.indexWidPN = "wid_pn_" + String.valueOf(Global.radius) + "_" + String.valueOf(Global.MAX_PN_LENGTH) + File.separator;
			if(!(new File(Global.outputDirectoryPath + Global.indexWidPN).exists())) {
				new File(Global.outputDirectoryPath + Global.indexWidPN).mkdir();
			}
			Global.placeWNFile = Global.outputDirectoryPath + "placeWN" + Global.rtreeFlag + Global.rtreeFanout + "." + Global.radius + Global.dataVersion;
			if(!new File(Global.placeWNFile).exists())	PlaceWNPrecomputation.BuildingPlaceWN(Global.placeWNFile, Boolean.FALSE);
			WordPNNoDateIndexBuilder.buildingWordPN();
//			new File(Global.placeWNFile).delete();
			bw.write(String.valueOf(radius) + " : " + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()) + '\n');
			bw.flush();
			System.out.println("> 已处理完radius=" + radius + "的wordPN，用时：" + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()));
			System.out.println();
		}
		bw.close();
		System.out.print("> Over创建radius=");
		for(int ii : radiusList) {
			System.out.print(String.valueOf(ii) + " ");
		}
		System.out.println("的WordPN，共用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()));
	}
	
	/*
	 * 创建单个WN
	 */
	public static void buildingWN(int radius, int len) throws Exception{
//		Global.MAX_STORED_STRING_LENGTH = Global.MAX_STORED_STRING_LENGTH/len;
		Global.MAX_PN_LENGTH = len;
		
		Global.radius = radius;
		System.out.println("> 开始创建radius=" + String.valueOf(radius) + " len=" + String.valueOf(len) + "的WordPNNodate . . . ");
		
		Global.placeWNNodateFile = Global.outputDirectoryPath + "placeWNNodate" + Global.rtreeFlag + Global.rtreeFanout + "." + Global.radius + Global.dataVersion;
		if(!new File(Global.placeWNNodateFile).exists())	PlaceWNPrecomputation.BuildingPlaceWN(Global.placeWNNodateFile, Boolean.FALSE);
		
		Global.indexWidPNNodate = "wid_pn_nodate_" + String.valueOf(Global.radius) + "_" + String.valueOf(len) + File.separator;
		if(!(new File(Global.outputDirectoryPath + Global.indexWidPN).exists())) {
			new File(Global.outputDirectoryPath + Global.indexWidPNNodate).mkdir();
		}
		
		Global.wordPNNodateFile = Global.outputDirectoryPath + "wordPNNodate"+ Global.rtreeFlag
				+ Global.rtreeFanout + "." + Global.radius + Global.dataVersion;
		
		WordPNNoDateIndexBuilder.buildingWordPN();
		System.out.println("> Over创建radius=" + String.valueOf(radius) + " len=" + String.valueOf(len) + "的WordPNNodate, 用时：" + TimeUtility.getTailTime());
	}
	
	/**
	 * 主函数
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Global.printInputOutputPath();
		WordPNNoDateIndexBuilder.buildingWN(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		
//		List<Integer> lens = new ArrayList<>();
//		lens.add(100000);
//		lens.add(1000000);
//		lens.add(10000000);
//		lens.add(100000000);
//		lens.add(1000000000);
//		for(int len : lens) {
//			buildingWN(3, len);
//		}
		
//		List<Integer> list = new ArrayList<>();
//		list.add(1);
//		list.add(2);
//		list.add(3);
//		list.add(5);
//		WordPNIndexBuilder.batchBuildingWN(list);
	}
}
