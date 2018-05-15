/**
 * 
 */
package main.sp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import entity.sp.DateNidNode;
import entity.sp.DatesWIds;
import entity.sp.MinHeap;
import entity.sp.NidToDateWidIndex;
import entity.sp.WordRadiusNeighborhood;
import entity.sp.NidToDateWidIndex.DateWid;
import entity.sp.RTreeWithGI;
import entity.sp.SortedDateWid;
import kSP.kSP;
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
public class SPCompleteDisk {
	
	private IndexNidKeywordsListService nIdWIdDateSer = null;
	private ReachableQueryService reachableQuerySer = null;
	private IndexWordPNService wIdPnSer = null;
	private LRUBuffer buffer = null;
	private RTreeWithGI rgi = null;
	
	public SPCompleteDisk() {
		if(Global.isDebug) {
			Global.startTime = System.currentTimeMillis();
			System.out.println("> 开始构造SPCompleteDisk . . . \n");
		}
		
		if(Global.isDebug) {
			System.out.println("> 开始打开各个lucen索引 . . . ");
		}
		
		if(Global.isTest) {
			Global.tempTime = System.currentTimeMillis();
			Global.frontTime = System.currentTimeMillis();
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
			Global.timeOpenLuceneIndex = TimeUtility.getSpanSecondStr(Global.frontTime, System.currentTimeMillis());
			Global.frontTime = System.currentTimeMillis();
		}
		
		reachableQuerySer = new ReachableQueryService(sccPath, tfLabelIndex);
		
		if(Global.isTest) {
			Global.timeLoadTFLable = TimeUtility.getSpanSecondStr(Global.frontTime, System.currentTimeMillis());
			Global.frontTime = System.currentTimeMillis();
		}
		
		if(Global.isDebug) {
			Global.frontTime = System.currentTimeMillis();
			System.out.println("> 完全打开各个lucen索引，用时" + TimeUtility.getSpendTimeStr(Global.startTime, Global.frontTime));
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
			Global.timeBuildRGI = TimeUtility.getSpanSecondStr(Global.frontTime, System.currentTimeMillis());
			Global.frontTime = System.currentTimeMillis();
		}
		
		if(Global.isTest) {
			Global.timeBuildSPCompleteDisk = TimeUtility.getSpanSecondStr(Global.tempTime, System.currentTimeMillis());
			Global.frontTime = System.currentTimeMillis();
		}
	}
	
	public void free() {
		nIdWIdDateSer.closeIndexReader();
		wIdPnSer.closeIndexReader();
		reachableQuerySer.freeQuery();
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
			Global.bspStartTime = System.currentTimeMillis();
			Global.frontTime = Global.bspStartTime;
		}
		
		long start = System.currentTimeMillis();
		
		Point qpoint = new Point(pCoords);
		
		if(Global.isDebug) {
			System.out.println("> 开始计算nIdDateWidMap和widDatesMap . . . ");
		}
		// 获得nIdDateWidMap, 和wordMinDateSpanMap
		ArrayList<Integer> sortedQwordsList = new ArrayList<>(qwords);
		sortedQwordsList.sort(new MComparator<Integer>());
		Map<Integer, DatesWIds> nIdDateWidMap = new HashMap<>();
		HashMap<Integer, SortedDateWid> widDatesMap = new HashMap<>();
		for(int in : sortedQwordsList) {
			if(Global.isTest) {
				Global.frontTime = System.currentTimeMillis();
			}
			Map<Integer, String> tempMap = nIdWIdDateSer.searchNIDKeyListDate(in);
			if(Global.isTest) {
				Global.timeBsp[0] += System.currentTimeMillis() - Global.frontTime;
				Global.frontTime = System.currentTimeMillis();
			}
			DatesWIds dws = null;
			SortedDateWid sdw = null;
			int tt = 0;
			for(Entry<Integer, String> en : tempMap.entrySet()) {
				if((++tt)%1000 == 0 && Global.isTest) {
					if(System.currentTimeMillis() - Global.frontTime > Global.limitTime0) {
						return null;
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
					sdw = new SortedDateWid();
					widDatesMap.put(in, sdw);
				}
				for(int din : dws.getDateList()) {
					sdw.addDateWid(new DateNidNode(din, en.getKey()));
				}
			}
			if(Global.isTest) {
				Global.timeBsp[1] += System.currentTimeMillis() - Global.frontTime;
			}
		}
		if(Global.isTest) {
			Global.frontTime = System.currentTimeMillis();
		}
		for(SortedDateWid sdw : widDatesMap.values()) {
			sdw.formatDateWidList();
		}
		if(Global.isDebug) {
			System.out.println("> 完成计算nIdDateWidMap和widDatesMap，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()));
			Global.frontTime = System.currentTimeMillis();
		}
		
		if(Global.isTest) {
			Global.timeBsp[1] += System.currentTimeMillis() - Global.frontTime;
			Global.timeBsp[2] = Global.timeBsp[0] + Global.timeBsp[1];
			
			Global.timeBsp[0] /= 1000;
			Global.timeBsp[1] /= 1000;
			Global.timeBsp[2] /= 1000;
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
			Global.timeBsp[3] = (System.currentTimeMillis() - Global.frontTime) / 1000;
			Global.frontTime = System.currentTimeMillis();
		}
		
		IVisitor v = new KSPCandidateVisitor(k);
		
//		Global.startTime = start;
		
		kSP kSPExecutor = new kSP(rgi, nIdDateWidMap, widDatesMap, wordPNMap, reachableQuerySer);
		kSPExecutor.kSPComputation(k, Global.radius, qpoint, qwords, TimeUtility.getIntDate(searchDate), v);
		
		if(Global.isTest) {
			Global.timeBsp[4] = (System.currentTimeMillis() - Global.frontTime) / 1000;
			Global.timeBsp[6] = ((KSPCandidateVisitor)v).size();
			Global.frontTime = System.currentTimeMillis();
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
		for(Entry<Integer, SortedDateWid> en : widDatesMap.entrySet()) {
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
			Global.timeBsp[5] = (System.currentTimeMillis() - Global.bspStartTime) / 1000;
			System.out.println("> 已处理" + (Global.curRecIndex) + "个sample");
		}
		
		
		return (KSPCandidateVisitor)v;
	}
	
	public static void main(String[] args) throws Exception{
		if(Global.isTest) {
			Global.startTime = System.currentTimeMillis();
			System.out.println("> 开始测试样本 . . . ");
		}
		System.out.println("> 开始初始化SPCompleteDisk . . . ");
		SPCompleteDisk spc = new SPCompleteDisk();
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
			bw = new BufferedWriter(new FileWriter(Global.inputDirectoryPath + String.valueOf(Global.testK) + "." + String.valueOf(Global.testSampleNum) + Global.testSampleResultFile));
			
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
					bw.write(String.valueOf(samNumCopy) + "#\n");
					bw.write("timeOpenLuceneIndex : " + Global.timeOpenLuceneIndex + "\n");
					bw.write("timeLoadTFLable : " + Global.timeLoadTFLable + "\n");
					bw.write("timeBuildRGI : " + Global.timeBuildRGI + "\n");
					bw.write("timeBuildSPCompleteDisk : " + Global.timeBuildSPCompleteDisk + "\n\n");
					bw.write("num FindPNTime ReadPNTime IsJoin nIdDateWidMap_widDatesMapLuceneTime convertTime totTime wordPNMap treeTime bspTime resultNum first.m_minDist kthScore\n");
				}
				
				bw.write(String.valueOf(Global.curRecIndex) + " ");
				for(int j=0; j<3; j++) {
					bw.write(String.valueOf(Global.timePn[j] + " "));
					Global.timePn[j] = 0;
				}
				for(int j=0; j<7; j++) {
					bw.write(String.valueOf(Global.timeBsp[j]) + " ");
					Global.timeBsp[j] = 0;
				}
				for(int j=0; j<2; j++) {
					bw.write(Global.bspRes[j] + " ");
				}
				bw.write('\n');
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
			Global.timeTotal = TimeUtility.getSpanSecondStr(Global.startTime, System.currentTimeMillis());
			
			bw.write("\ntimeReadLuceneMax : " + Global.timeReadLuceneMax);
			
			bw.write("\n" + "timeTotal : " + Global.timeTotal);
			bw.flush();
			bw.close();
			System.out.println("> 完成测试样本，用时" + TimeUtility.getSpanSecondStr(Global.startTime, System.currentTimeMillis()));
		}
	}
}
