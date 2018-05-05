/**
 * 
 */
package main.sp;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import entity.sp.DateWId;
import entity.sp.MinHeap;
import entity.sp.NidToDateWidIndex;
import entity.sp.WordRadiusNeighborhood;
import entity.sp.NidToDateWidIndex.DateWid;
import entity.sp.RTreeWithGI;
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
	private IndexNidKeywordsListService wIdDateSer = null;
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
		// 各索引路径
		String nIdWIdDateIndex = Global.outputDirectoryPath + Global.indexNIdWordDate;
		String wIdDateIndex = Global.outputDirectoryPath + Global.indexWIdDate;
		String sccPath = Global.outputDirectoryPath + Global.sccFile;
		String tfLabelIndex = Global.outputDirectoryPath + Global.indexTFLabel;
		String wIdPNIndex = Global.outputDirectoryPath + Global.indexWidPN;
		
		nIdWIdDateSer = new IndexNidKeywordsListService(nIdWIdDateIndex);
		nIdWIdDateSer.openIndexReader();
		wIdDateSer = new IndexNidKeywordsListService(wIdDateIndex);
		wIdDateSer.openIndexReader();
		reachableQuerySer = new ReachableQueryService(sccPath, tfLabelIndex);
		wIdPnSer = new IndexWordPNService(wIdPNIndex);
		wIdPnSer.openIndexReader();
		
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
	}
	
	public void free() {
		nIdWIdDateSer.closeIndexReader();
		wIdDateSer.closeIndexReader();
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
		
		List<KSPCandidate> semanticTreeResult = new ArrayList<>();
		
		long start = System.currentTimeMillis();
		
		Point qpoint = new Point(pCoords);
		
		if(Global.isDebug) {
			System.out.println("> 开始计算nIdDateWidMap . . . ");
		}
		// 获得Mq
		Map<Integer, DateWId> nIdDateWidMap = new HashMap<>();
		Map<Integer, String> tempMap = nIdWIdDateSer.searchNIDKeyListDateIndex(qwords);
		for(Entry<Integer, String> en : tempMap.entrySet()) {
			nIdDateWidMap.put(en.getKey(), new DateWId(en.getValue()));
		}
		tempMap.clear();
		if(Global.isDebug) {
			System.out.println("> 完成计算nIdDateWidMap，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()));
			Global.frontTime = System.currentTimeMillis();
		}
		
		/////////////////////////// 打印测试
//		System.out.println("searchedNodeListMap : ");
//		for(Entry<Integer, DateWId> en : nIdDateWidMap.entrySet()) {
//			System.out.println(en.getKey() + " : " + en.getValue().getStr());
//		}
//		System.out.println();
		
		if(Global.isDebug) {
			System.out.println("> 开始计算wordMinDateSpanMap . . . ");
		}
		// 计算与当前时间时差最小的word组成的map
		String[] dateArr = null;
		HashMap<Integer, Integer> wordMinDateSpanMap = new HashMap<>();
		int intSearchDate = TimeUtility.getIntDate(searchDate);
		for(int in : qwords) {
			ArrayList<Integer> dateList = new ArrayList<>();
			dateArr = wIdDateSer.searchWIDDateIndex(in).split(Global.delimiterDate);
			for(String st : dateArr) {
				dateList.add(Integer.parseInt(st));
			}
			wordMinDateSpanMap.put(in, TimeUtility.getMinDateSpan(intSearchDate, dateList));
		}
		if(Global.isDebug) {
			System.out.println("> 完成计算wordMinDateSpanMap，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()));
			Global.frontTime = System.currentTimeMillis();
		}
		///////////////////////////////// 打印测试
//		System.out.println("wordMinDateSpanMap : ");
//		for(Entry<Integer, Integer> en : wordMinDateSpanMap.entrySet()) {
//			System.out.println(en.getKey() + " - " + en.getValue());
//		}
//		System.out.println();
		
		if(Global.isDebug) {
			System.out.println("> 开始计算wordPNMap . . . ");
		}
		// 获得word 的  place neighborhood
		HashMap<Integer, WordRadiusNeighborhood> wordPNMap = new HashMap<>();
		for(Integer in : qwords) {
			String st1 =  wIdPnSer.getPlaceNeighborhoodStr(in);
			if(st1.startsWith(Global.delimiterPound)) {
				// pidDate串太长，被切分了
				StringBuffer sBuf = new StringBuffer();
				int starti = Integer.parseInt(st1.split(Global.delimiterPound)[1]);
				int endi = Integer.parseInt(st1.split(Global.delimiterPound)[2]);
				for(int i = starti; i< endi; i++) {
					sBuf.append(wIdPnSer.getPlaceNeighborhoodStr(i));
				}
				wordPNMap.put(in, new WordRadiusNeighborhood(Global.radius, sBuf.toString()));
			} else {
				wordPNMap.put(in, new WordRadiusNeighborhood(Global.radius, wIdPnSer.getPlaceNeighborhoodStr(in)));
			}
		}
		if(Global.isDebug) {
			System.out.println("> 完成计算wordPNMap，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()));
			Global.frontTime = System.currentTimeMillis();
		}
		
		IVisitor v = new KSPCandidateVisitor(k);
		
//		Global.startTime = start;
		
		kSP kSPExecutor = new kSP(semanticTreeResult, rgi, nIdDateWidMap, wordMinDateSpanMap, wordPNMap, reachableQuerySer);
		kSPExecutor.kSPComputation(k, Global.radius, qpoint, qwords, intSearchDate, v);
		long end = System.currentTimeMillis();
		Global.runtime[0] += (end - start);

		// ATTENTION: MUST reset graph after each query
		rgi.getGraph().reset();
		
		// 清空释放内存
		for(Entry<Integer, DateWId> en : nIdDateWidMap.entrySet()) {
			if(null != en.getValue()) {
				en.getValue().clear();
			}
		}
		nIdDateWidMap.clear();
		wordMinDateSpanMap.clear();
		
		for(Entry<Integer, WordRadiusNeighborhood> en : wordPNMap.entrySet()) {
			if(null != en.getValue())	en.getValue().clear();
		}
		wordPNMap.clear();
				
		System.out.print("> 查找词");
		for(int in : qwords) {
			System.out.print(in + " ");
		}
		System.out.println("，共找到" + ((KSPCandidateVisitor)v).size() + "个结果，用时：" + TimeUtility.getSpendTimeStr(Global.bspStartTime, System.currentTimeMillis()));
		
		return (KSPCandidateVisitor)v;
	}
	
	public static void main(String[] args) throws Exception{
		SPCompleteDisk spc = new SPCompleteDisk();
		
		int k = 10;
		double[] pcoords = new double[2];
		pcoords[0] = 49.014446;
		pcoords[1] = 48.799393;
		ArrayList<Integer> qwords = new ArrayList<>();
		qwords.add(11381939);
		qwords.add(8511774);
		Date date = TimeUtility.getDate("1936-01-08");
		if(args.length >= 6) {
			System.out.println("> 初始化输入参数\n");
			k = Integer.parseInt(args[0]);
			pcoords[0] = Double.parseDouble(args[1]);
			pcoords[1] = Double.parseDouble(args[2]);
			qwords = new ArrayList<>();
			qwords.add(Integer.parseInt(args[3]));
			qwords.add(Integer.parseInt(args[4]));
			date  = TimeUtility.getDate(args[5]);
		}
		Utility.showSemanticTreeResult(spc.bsp(k, pcoords, qwords, date).getResultQ());
		spc.free();
	}
}
