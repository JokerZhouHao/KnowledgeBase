/**
 * 
 */
package main.sp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import entity.sp.DateNidNode;
import entity.sp.DatesWIds;
import entity.sp.MinHeap;
import entity.sp.NidToDateWidIndex;
import entity.sp.WordRadiusNeighborhood;
import entity.sp.NidToDateWidIndex.DateWid;
import entity.sp.RTreeWithGI;
import entity.sp.RunRecord;
import entity.sp.SortedDateWidCReach;
import kSP.kSPCReach;
import kSP.candidate.KSPCandidate;
import kSP.candidate.KSPCandidateVisitor;
import neustore.base.LRUBuffer;
import precomputation.rechable.ReachableQueryService;
import precomputation.sp.IndexNidKeywordsListService;
import precomputation.sp.IndexWordPNService;
import queryindex.VertexQwordsMap;
import spatialindex.spatialindex.IVisitor;
import spatialindex.spatialindex.Point;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IBuffer;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;
import spatialindex.storagemanager.TreeLRUBuffer;
import utility.Global;
import utility.LocalFileInfo;
import utility.MComparator;
import utility.RGIUtility;
import utility.TFlabelUtility;
import utility.TimeUtility;
import utility.Utility;

/**
 * Main class invoking SP algorithm
 * @author Monica
 *
 */
public class SPCompleteDiskCReach {
	
	private IndexNidKeywordsListService nIdWIdDateSer = null;
	private ReachableQueryService reachableQuerySer = null;
	private IndexWordPNService wIdPnSer = null;
	private LRUBuffer buffer = null;
	private RTreeWithGI rgi = null;
	
	public SPCompleteDiskCReach() throws Exception{
		if(Global.isDebug) {
			Global.startTime = System.currentTimeMillis();
			System.out.println("> 开始构造SPCompleteDisk . . . \n");
		}
		
		if(Global.isDebug) {
			System.out.println("> 开始打开各个lucen索引 . . . ");
		}
		
		// 各索引路径
		String nIdWIdDateIndex = Global.outputDirectoryPath + Global.indexNIdWordDate;
		String sccPath = Global.outputDirectoryPath + Global.sccFile;
		String tfLabelIndex = Global.outputDirectoryPath + Global.indexTFLabel;
		String wIdPNIndex = Global.outputDirectoryPath + Global.indexWidPN;
		
		nIdWIdDateSer = new IndexNidKeywordsListService(nIdWIdDateIndex);
		nIdWIdDateSer.openIndexReader();
		wIdPnSer = new IndexWordPNService(wIdPNIndex);
		wIdPnSer.openIndexReader();
		
		if(Global.isTest) {
			Global.rr.frontTime = System.currentTimeMillis();
		}
		
		reachableQuerySer = new ReachableQueryService(sccPath, tfLabelIndex);
		
		if(Global.isTest) {
			Global.rr.timeLoadTFLable = Global.rr.getTimeSpan();
			Global.rr.setFrontTime();
		}
		
		if(Global.isDebug) {
			System.out.println("> 初始化RTreeWithGI . . . . ");
		}
		
		//buffer for alpha WN inverted index 
		buffer = new LRUBuffer(Global.alphaIindexRTNodeBufferSize, Global.rtreePageSize);
		
		//the data index structure of RDF data with R-tree, RDF Graph, and Inverted index of keywords
		try {
			PropertySet psRTree = new PropertySet();
			String indexRTree = Global.indexRTree;
			psRTree.setProperty("FileName", indexRTree);
			psRTree.setProperty("PageSize", Global.rtreePageSize);
			psRTree.setProperty("BufferSize", Global.rtreeBufferSize);
			psRTree.setProperty("fanout", Global.rtreeFanout);
			
			IStorageManager diskfile = new DiskStorageManager(psRTree);
			IBuffer file = new TreeLRUBuffer(diskfile, Global.rtreeBufferSize, false);
			
			Integer i = new Integer(1); 
			psRTree.setProperty("IndexIdentifier", i);
			
			rgi = new RTreeWithGI(psRTree, file);
			rgi.buildSimpleGraphInMemory();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		if(Global.isDebug) {
			Global.tempTime = System.currentTimeMillis();
			System.out.println("> 完成初始化RTreeWithGI，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, Global.tempTime));
			Global.frontTime = Global.tempTime;
		}
		
		if(Global.isDebug) {
			System.out.println("\n> 完成构造SPCompleteDisk，用时" + TimeUtility.getSpendTimeStr(Global.startTime, Global.frontTime));
		}
		
		if(Global.isTest) {
			Global.rr.timeBuildRGI = Global.rr.getTimeSpan();
			Global.rr.setFrontTime();
		}
		
		// 添加点对可达时间记录
		Global.recReachBW = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(Global.fileReachGZip)))));
		
		if(Global.isTest) {
			Global.rr.timeBuildSPCompleteDisk = System.currentTimeMillis() - Global.rr.startTime;
			Global.rr.setFrontTime();
		}
	}
	
	public void free() throws Exception{
		nIdWIdDateSer.closeIndexReader();
		wIdPnSer.closeIndexReader();
		reachableQuerySer.freeQuery();
		Global.recReachBW.close();
	}
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public KSPCandidateVisitor bsp(int k, double[] pCoords, ArrayList<Integer> qwords, Date searchDate) throws Exception {
		
		if(Global.isDebug) {
			System.out.println("> 开始执行bsp . . . ");
			Global.bspStartTime = System.currentTimeMillis();
			Global.frontTime = Global.bspStartTime;
		}
		
		if(Global.isTest) {
			Global.rr.timeBspStart = System.currentTimeMillis();
			Global.rr.setFrontTime();
		}
		
		Boolean isOver = Boolean.FALSE;
		
		Point qpoint = new Point(pCoords);
		
		if(Global.isDebug) {
			System.out.println("> 开始计算nIdDateWidMap和widDatesMap . . . ");
		}
		// 获得nIdDateWidMap, 和wordMinDateSpanMap
		ArrayList<Integer> sortedQwordsList = new ArrayList<>(qwords);
		sortedQwordsList.sort(new MComparator<Integer>());
		Map<Integer, DatesWIds> nIdDateWidMap = new HashMap<>();
		HashMap<Integer, SortedDateWidCReach> widDatesMap = new HashMap<>();
		for(int in : sortedQwordsList) {
			if(Global.isTest) {
				Global.rr.setFrontTime();
			}
			Map<Integer, String> tempMap = nIdWIdDateSer.searchNIDKeyListDate(in);
			if(Global.isTest) {
				Global.rr.timeBspSearchWid2DateNid += Global.rr.getTimeSpan();
				Global.rr.setFrontTime();
			}
			DatesWIds dws = null;
			SortedDateWidCReach sdw = null;
			int tt = 0;
			for(Entry<Integer, String> en : tempMap.entrySet()) {
				if((++tt)%1000 == 0 && Global.isTest) {
					if(Global.rr.getTimeSpan()> Global.rr.limitBuidingWid2DateNid) {
						isOver = Boolean.TRUE;
						break;
					}
				}
				if(null == (dws = nIdDateWidMap.get(en.getKey()))) {
					dws = new DatesWIds(en.getValue());
					dws.addWid(in);
					nIdDateWidMap.put(en.getKey(), dws);
				} else {
					dws.addWid(in);
				}
				if(null == (sdw = widDatesMap.get(in))) {
					sdw = new SortedDateWidCReach();
					widDatesMap.put(in, sdw);
				}
				int t0 = -1;
				for(int din : dws.getDateList()) {
					Global.rr.numBspWid2DateWid++;
					if(-1 == t0)	t0 = sdw.addDateWid(new DateNidNode(din, en.getKey()));
					else t0 = sdw.addDateWid(new DateNidNode(din, en.getKey()), t0);
				}
			}
			if(Global.isTest) {
				Global.rr.timeBspBuidingWid2DateNid += Global.rr.getTimeSpan();
				if(isOver)	break;
			}
		}
		
		if(Global.isTest) {
			Global.rr.setFrontTime();
		}
		if(Global.isDebug) {
			System.out.println("> 完成计算nIdDateWidMap和widDatesMap，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()));
			Global.frontTime = System.currentTimeMillis();
		}
		
		/////////////////////////// 打印测试
//		System.out.println("searchedNodeListMap : ");
//		for(Entry<Integer, DateWId> en : nIdDateWidMap.entrySet()) {
//			System.out.println(en.getKey() + " : " + en.getValue().getStr());
//		}
//		System.out.println();
		
		if(Global.isDebug) {
			System.out.println("> 开始计算wordPNMap . . . ");
		}
		// 获得word 的  place neighborhood
		HashMap<Integer, WordRadiusNeighborhood> wordPNMap = new HashMap<>();
		for(Integer in : qwords) {
			if(Global.isTest) {
				Global.tempTime = System.currentTimeMillis();
			}
			String st1 =  wIdPnSer.getPlaceNeighborhoodStr(in);
			if(null != st1) {
				wordPNMap.put(in, new WordRadiusNeighborhood(Global.radius, st1));
			}
//			if(st1.startsWith(Global.delimiterPound)) {
//				if(Global.isTest) {
//					Global.timePn[2] = 1;
//					Global.frontTime = System.currentTimeMillis();
//				}
//				// pidDate串太长，被切分了
//				StringBuffer sBuf = new StringBuffer();
//				int starti = Integer.parseInt(st1.split(Global.delimiterPound)[1]);
//				int endi = Integer.parseInt(st1.split(Global.delimiterPound)[2]);
//				for(int i = starti; i< endi; i++) {
//					if(Global.isTest && i==starti) {
//						Global.tempTime = System.currentTimeMillis();
//						Global.isFirstReadPn = true;
//					}
//					sBuf.append(wIdPnSer.getPlaceNeighborhoodStr(i));
//				}
//				wordPNMap.put(in, new WordRadiusNeighborhood(Global.radius, sBuf.toString()));
//			} else {
//				wordPNMap.put(in, new WordRadiusNeighborhood(Global.radius, st1));
//			}
		}
		
		///////测试//////////////////////
//		if(Global.isTest) {
//			return null;
//		}
		//////测试/////////////////////////
		
		if(Global.isDebug) {
			System.out.println("> 完成计算wordPNMap，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()));
			Global.frontTime = System.currentTimeMillis();
		}
		
		if(Global.isTest) {
			Global.rr.timeBspGetPN = Global.rr.getTimeSpan();
			Global.rr.timeEnterkSPComputation = System.currentTimeMillis();
		}
		
		IVisitor v = new KSPCandidateVisitor(k);
		
//		Global.startTime = start;
		
		kSPCReach kSPExecutor = new kSPCReach(rgi, nIdDateWidMap, widDatesMap, wordPNMap, reachableQuerySer);
		if(!isOver)	kSPExecutor.kSPComputation(k, Global.radius, qpoint, qwords, TimeUtility.getIntDate(searchDate), v);
		
		if(Global.isTest) {
			Global.rr.setTimeKSPComputation();
			Global.rr.setFrontTime();
			Global.rr.resultSize = ((KSPCandidateVisitor) v).size();
		}
		
		// ATTENTION: MUST reset graph after each query
		rgi.getGraph().reset();
		
		// 清空释放内存
		for(Entry<Integer, DatesWIds> en : nIdDateWidMap.entrySet()) {
			if(null != en.getValue()) {
				en.getValue().clear();
			}
		}
		nIdDateWidMap.clear();
		for(Entry<Integer, SortedDateWidCReach> en : widDatesMap.entrySet()) {
			en.getValue().clear();
		}
		widDatesMap.clear();
		
		for(Entry<Integer, WordRadiusNeighborhood> en : wordPNMap.entrySet()) {
			if(null != en.getValue())	en.getValue().clear();
		}
		wordPNMap.clear();
		
		if(Global.isDebug) {
			System.out.print("> 查找词");
			for(int in : qwords) {
				System.out.print(in + " ");
			}
			System.out.println("，共找到" + ((KSPCandidateVisitor)v).size() + "个结果，用时：" + TimeUtility.getSpendTimeStr(Global.bspStartTime, System.currentTimeMillis()));
		}
		
		if(Global.isTest) {
			Global.curRecIndex++;
			Global.rr.timeBspClearJob = Global.rr.getTimeSpan();
			Global.rr.setTimeBsp();
			System.out.println("> 已处理" + (Global.curRecIndex) + "个sample");
		}
		return (KSPCandidateVisitor)v;
	}
	
	public static void main(String[] args) throws Exception{
		if(Global.isTest) {
			Global.startTime = System.currentTimeMillis();
			System.out.println("> 开始测试样本 . . . ");
		}
		
		// 添加测试样本
		String sampleFileSign = "";
		if(args.length > 0) {
			sampleFileSign = args[0];
		}
		
		System.out.println("> 开始初始化SPCompleteDisk . . . ");
		SPCompleteDiskCReach spc = new SPCompleteDiskCReach();
		System.out.println("> 成功初始化SPCompleteDisk ！ ！ ！ ");
//		SPCompleteDisk spc = null;
//		10 35.68275862680435 -85.23272932806015 11691841 11381939 1954-01-09
		int k = 10;
		double[] pcoords = new double[2];
		pcoords[0] = 35.68275862680435;
		pcoords[1] = -85.23272932806015;
		ArrayList<Integer> qwords = new ArrayList<>();
		qwords.add(11691841);
		qwords.add(11381939);
		Date date = TimeUtility.getDate("1954-01-09");
		int samNum = Global.testSampleNum;
		int samNumCopy = 0;
		BufferedWriter bw = null;
		if(Global.isTest) {
			if(args.length > 0) {
				for(int i=0; i<args.length; i++) {
					if(args[i].contains("sn")){
						samNum = Integer.parseInt(args[i].split("=")[1].trim());
						if(samNum > Global.testOrgSampleNum) {
							samNum = Global.testOrgSampleNum;
						}
						Global.testSampleNum = samNum;
					} else if (args[i].contains("k")) {
						Global.testK = Integer.parseInt(args[i].split("=")[1].trim());
					}
				}
			}
			samNumCopy = samNum;
			
			// 输出结果
			bw = new BufferedWriter(new FileWriter(Global.inputDirectoryPath + String.valueOf(Global.testK) + "." + String.valueOf(Global.testSampleNum) + Global.testSampleResultFile + sampleFileSign));
			
			BufferedReader br = new BufferedReader(new FileReader(Global.inputDirectoryPath + Global.testSampleFile));
			br.readLine();
			String lineStr = null;
			while(samNum > 0) {
				br.readLine();
				lineStr = br.readLine();
				String[] strArr = lineStr.split(Global.delimiterSpace);
				k = Integer.parseInt(strArr[0]);
				k = Global.testK;
				pcoords[0] = Double.parseDouble(strArr[1]);
				pcoords[1] = Double.parseDouble(strArr[2]);
				qwords = new ArrayList<>();
				qwords.add(Integer.parseInt(strArr[3]));
				qwords.add(Integer.parseInt(strArr[4]));
				date = TimeUtility.getDate(strArr[5]);
				spc.bsp(k, pcoords, qwords, date);
				samNum--;
				
				// 写数据
				if(Global.curRecIndex == 1) {
					System.out.println(Global.rr.getInitInfo());
					bw.write(Global.rr.getHeader());
				}
				
				bw.write(Global.rr.getBspInfo(Global.curRecIndex, 1000));
				Global.rr = new RunRecord();
				
//				bw.write(String.valueOf(Global.curRecIndex) + " ");
////				for(int j=0; j<3; j++) {
////					bw.write(String.valueOf(Global.timePn[j] + " "));
////					Global.timePn[j] = 0;
////				}
//				bw.write("| ");
//				bw.write(String.valueOf(Global.leftMaxSpan) + " ");
//				bw.write(String.valueOf(Global.rightMaxSpan) + " ");
//				bw.write(String.valueOf(Global.timeGetMinDateSpan) + " ");
//				Global.leftMaxSpan = 0;
//				Global.rightMaxSpan = 0;
//				Global.timeGetMinDateSpan = 0;
//				bw.write("| ");
//				
//				for(int j=0; j<3; j++) {
//					bw.write(String.valueOf(Global.recCount[j] + " "));
//					Global.recCount[j] = 0;
//				}
//				bw.write("| ");
//				
//				for(int j=0; j<5; j++) {
//					bw.write(String.valueOf(Global.timePTree[j]/1000) + " ");
//					Global.timePTree[j] = 0;
//				}
//				bw.write("| ");
//				
//				bw.write(String.valueOf(Global.queueSize) + " ");
//				bw.write("| ");
//				
//				for(int j=0; j<7; j++) {
//					if(j==1) {
//						Global.timeBsp[5] -= Global.timeBsp[1];
//						Global.timeBsp[5] -= (Global.timeRecReachable / 1000);
//						Global.timeRecReachable = 0;
//					}
//					bw.write(String.valueOf(Global.timeBsp[j]) + " ");
//					Global.timeBsp[j] = 0;
//				}
//				bw.write("| ");
//				
//				for(int j=0; j<2; j++) {
//					bw.write(Global.bspRes[j] + " ");
//					Global.bspRes[j] = null;
//				}
//				bw.write("| ");
//				bw.write('\n');
				bw.flush();
			}
			br.close();
		}
		
		if(Global.isDebug) {
			if(args.length >= 6) {
				System.out.println("> 初始化输入参数\n");
				k = Integer.parseInt(args[0]);
				pcoords[0] = Double.parseDouble(args[1]);
				pcoords[1] = Double.parseDouble(args[2]);
				qwords = new ArrayList<>();
				qwords.add(Integer.parseInt(args[3]));
				qwords.add(Integer.parseInt(args[4]));
				date  = TimeUtility.getDate("2018-05-15");
			}
			Utility.showSemanticTreeResult(spc.bsp(k, pcoords, qwords, date).getResultQ());
		}
		spc.free();
		if(Global.isTest) {
			bw.close();
			System.out.println("> 完成测试样本，用时" + TimeUtility.getTailTime());
		}
	}
}
