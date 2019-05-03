package main.sp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import entity.sp.DateNidNode;
import entity.sp.DatesWIds;
import entity.sp.GraphByArray;
import entity.sp.WordRadiusNeighborhood;
import entity.sp.date.MinMaxDateService;
import entity.sp.date.Wid2DateNidPairIndex;
import entity.sp.reach.CReach;
import entity.sp.reach.P2WRTreeReach;
import entity.sp.reach.RTreeLeafNodeContainPids;
import entity.sp.reach.W2PReachService;
import entity.sp.RTreeWithGI;
import entity.sp.RunRecord;
import entity.sp.SortedDateWidIndex;
import kSP.KSPIndex;
import kSP.candidate.KSPCandidateVisitor;
import neustore.base.LRUBuffer;
import precomputation.sp.IndexNidKeywordsListService;
import precomputation.sp.IndexWordPNService;
import spatialindex.spatialindex.IVisitor;
import spatialindex.spatialindex.Point;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IBuffer;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;
import spatialindex.storagemanager.TreeLRUBuffer;
import utility.Global;
import utility.MComparator;
import utility.RandomNumGenerator;
import utility.TimeUtility;

/**
 * 使用索引来获得可达性的SP实现
 * @author Monica
 *
 */
public class SPBest {
	
	private IndexNidKeywordsListService nIdWIdDateSer = null;
	private IndexWordPNService wIdPnSer = null;
	private LRUBuffer buffer = null;
	private RTreeWithGI rgi = null;
	private Wid2DateNidPairIndex wid2DateNidPairIndex = null;
	private Set<Integer>[] rtreeNode2Pid = null;
	private W2PReachService w2pReachSer = null;
	private CReach cReach = null;
	private MinMaxDateService minMaxDateSer = null;
	
	private Map<Integer, Map<Integer, String>> cacheSeachedWid = new HashMap<>();	// 缓存关键词的查询结果
	
	private int[] pid2RtreeLeafNode = null;
	
	public static RandomNumGenerator dateSpanGen = new RandomNumGenerator(0, 7);
	
	private DatesWIds searchedDatesWids[] = new DatesWIds[Global.numNodes];
	
	/**
	 * 初始化
	 * @throws Exception
	 */
	public SPBest() throws Exception{
		// 各索引路径
		String nIdWIdDateIndex = Global.outputDirectoryPath + Global.indexNIdWordDate;
		String wIdPNIndex = Global.outputDirectoryPath + Global.indexWidPN + "_" + String.valueOf(Global.radius) + "_" + String.valueOf(Global.MAX_PN_LENGTH) + File.separator;
		
		nIdWIdDateSer = new IndexNidKeywordsListService(nIdWIdDateIndex);
		nIdWIdDateSer.openIndexReader();
		
		if(Global.MAX_PN_LENGTH> 0) {
			wIdPnSer = new IndexWordPNService(wIdPNIndex);
			wIdPnSer.openIndexReader();
		}
		
		wid2DateNidPairIndex = new Wid2DateNidPairIndex(Global.indexWid2DateNid);
		wid2DateNidPairIndex.openIndexReader();
		
		rtreeNode2Pid = P2WRTreeReach.loadRTreeNode2Pids(Global.recRTreeNode2NidReachPath);
		
		Global.indexWid2PidBase = Global.outputDirectoryPath + "wid_2_pid_reachable_pidDis_fre=" + String.valueOf(Global.MAX_WORD_FREQUENCY) + File.separator + "wids_block_";
		w2pReachSer = new W2PReachService(Global.indexWid2PidBase);
		w2pReachSer.openIndexs();
		
		cReach = new CReach(Global.outputDirectoryPath + Global.sccFile, Global.outputDirectoryPath + Global.indexTFLabel, Global.numSCCs);
		
		pid2RtreeLeafNode = RTreeLeafNodeContainPids.loadPid2RTreeLeafNode(Global.recRTreeLeafNodeContainPidsPath);
		
		minMaxDateSer = new MinMaxDateService(Global.outputDirectoryPath + Global.minMaxDatesFile);
		
		Global.wordFrequency = IndexNidKeywordsListService.loadWordFrequency(Global.outputDirectoryPath + Global.wordFrequencyFile);
		
		if(Global.isDebug) {
			System.out.println("> 初始化RTreeWithGI . . . . ");
		}
		
		if(Global.isTest) {
			Global.rr.setFrontTime();
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
		
		if(Global.isTest) {
			Global.rr.timeBuildRGI = Global.rr.getTimeSpan();
			Global.rr.setFrontTime();
		}
		
		// 添加点对可达时间记录
		if(Global.isDebug)	Global.recReachBW = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(Global.fileReachGZip)))));
		
		if(Global.isTest) {
			Global.rr.timeBuildSPCompleteDisk = System.nanoTime() - Global.rr.startTime;
			Global.rr.setFrontTime();
		}
	}
	
	/**
	 * 释放空间
	 * @throws Exception
	 */
	public void free() throws Exception{
		nIdWIdDateSer.closeIndexReader();
		if(Global.MAX_PN_LENGTH> 0)	wIdPnSer.closeIndexReader();
		wid2DateNidPairIndex.closeIndexReader();
		w2pReachSer.closeIndexs();
		if(Global.isDebug)	Global.recReachBW.close();
	}
	
	/**
	 * bsp算法实现
	 * @param args
	 * @throws Exception
	 */
	public KSPCandidateVisitor bsp(int k, double[] pCoords, ArrayList<Integer> qwords, Date searchDate, Date eDate) throws Exception {
		
		if(Global.isTest) {
			Global.rr.timeBspStart = System.nanoTime();
			Global.rr.setFrontTime();
		}
		
		Point qpoint = new Point(pCoords);
		int searchIntDate = TimeUtility.getIntDate(searchDate);
		int eIntDate = Integer.MIN_VALUE;
		if(null != eDate) {
			searchIntDate -= Global.DATE_RANGE;
			eIntDate = TimeUtility.getIntDate(eDate) + Global.DATE_RANGE + 1;
		}
		
		int i = 0;
		
		// 获得有序的查询词
		ArrayList<Integer> sortedQwordsList = new ArrayList<>(qwords);
		sortedQwordsList.sort(new MComparator<Integer>());
		int sortQwords[] = new int[sortedQwordsList.size()];
		for(i=0; i<sortQwords.length; i++) {
			sortQwords[i] = sortedQwordsList.get(i);
		}
		
		// 计算那些点在时间范围内
		Set<Integer> matchSetNids = null;
		List<Integer> matchNids = null;
		if(eIntDate != Integer.MIN_VALUE) {
			matchSetNids = minMaxDateSer.search(searchIntDate, eIntDate);
			matchNids = new ArrayList<>();
		}
		
		// 获得nIdDateWidMap
//		for(i=0; i<searchedDatesWids.length; i++)	searchedDatesWids[i] = null;
//		Set<Integer> tSet = new HashSet<>();
//		for(i=0; i<sortQwords.length; i++) {
//			Map<Integer, String> tempMap = null;
//			
//			if(null == (tempMap = cacheSeachedWid.get(sortQwords[i]))) {
//				tempMap = nIdWIdDateSer.searchNIDKeyListDate(sortQwords[i]);
//				// 不存在该wid
//				if(null==tempMap) {
//					Global.curRecIndex++;
//					return null;
//				}
//				
//				if(tempMap.size() > 100000) {	// 缓存命中量超过100000的节点的查询结果
//					cacheSeachedWid.put(sortQwords[i], tempMap);
//				}
//			}
//			
//			DatesWIds dws = null;
//			for(Entry<Integer, String> en : tempMap.entrySet()) {
//				if(eDate != null) {
//					// 过滤掉不在时间范围内的词
//					if(!matchSetNids.contains(en.getKey())) {
//						continue;
//					} else {
//						tSet.add(en.getKey());
//					}
//				}
//				if(null == (dws = searchedDatesWids[en.getKey()])) {
//					dws = new DatesWIds(en.getValue(), sortQwords.length);
//					dws.addWid(i, sortQwords[i]);
//					searchedDatesWids[en.getKey()] = dws;
//				} else {
//					dws.addWid(i, sortQwords[i]);
//				}
//			}
//		}
		
		// 没有符合查询条件的点
//		if(eDate != null) {
//			if(tSet.size()==0) {
//				Global.curRecIndex++;
//				return null;
//			}
//			else {
//				for(int ii : tSet) {
//					matchNids.add(ii);
//				}
//			}
//			if(Global.isOutputTestInfo)	System.out.println(searchIntDate + " " + " " + eIntDate + " " + matchSetNids.size() + " " + matchNids.size() + " " + searchedDatesWids.length);
//			matchSetNids.clear();
//		}
		
		if(Global.isTest) {
			Global.rr.timeBspSearchWid2DateNid += Global.rr.getTimeSpan();
			Global.rr.setFrontTime();
		}
		
		// 获得wid2DateNid
		if(Global.isTest) {
			Global.rr.setFrontTime();
		}
		SortedDateWidIndex[] wid2DateNidPair = null;
		int[] maxDateSpans = new int[sortedQwordsList.size()];
//		if(eIntDate == Integer.MIN_VALUE) {
			wid2DateNidPair = new SortedDateWidIndex[sortedQwordsList.size()];
			for(i=0; i<sortQwords.length; i++) {
				wid2DateNidPair[i] = wid2DateNidPairIndex.getDateNids(sortQwords[i], TimeUtility.getIntDate(searchDate));
				// 不存在该wid
				if(null==wid2DateNidPair[i]) {
					Global.curRecIndex++;
					return null;
				}
				// 记录最大时间差
				maxDateSpans[i] = Math.max(Math.abs(wid2DateNidPair[i].dateWidList.get(0).getDate() - TimeUtility.getIntDate(searchDate)), 
						Math.abs(wid2DateNidPair[i].dateWidList.get(wid2DateNidPair[i].dateWidList.size() - 1).getDate() - TimeUtility.getIntDate(searchDate))) + 1;
				
				Global.rr.numBspWid2DateWid += wid2DateNidPair[i].size();
			}
			if(Global.isTest) {
				Global.rr.timeBspBuidingWid2DateNid += Global.rr.getTimeSpan();
				Global.rr.setFrontTime();
			}
//		}
		
		// 获得nIdDateWidMap
		for(i=0; i<searchedDatesWids.length; i++)	searchedDatesWids[i] = null;
		DatesWIds dws = null;
		for(i=0; i < wid2DateNidPair.length; i++) {
			for(DateNidNode dnn : wid2DateNidPair[i].dateWidList) {
				if(null == (dws = searchedDatesWids[dnn.getNid()])) {
					dws = new DatesWIds(dnn.getDate(), sortQwords.length);
					searchedDatesWids[dnn.getNid()] = dws;
				}
				dws.addWid(i, sortQwords[i]);
			}
		}
		
		// 没有符合查询条件的点
//		Set<Integer> tSet = new HashSet<>();
		if(eDate != null) {
			for(i=0; i<searchedDatesWids.length; i++) {
				if(null != searchedDatesWids[i])
					matchNids.add(i);
			}
			if(Global.isOutputTestInfo)	System.out.println(searchIntDate + " " + " " + eIntDate + " " + matchSetNids.size() + " " + matchNids.size() + " " + searchedDatesWids.length);
			matchSetNids.clear();
		}
		
		// 获得W2PReachable
		Map<Integer, Short>[] w2pReachable = new Map[sortQwords.length];
		for(i=0; i<sortQwords.length; i++) {
			if(Global.wordFrequency.get(sortQwords[i]) >= Global.MAX_WORD_FREQUENCY) {
				w2pReachable[i] = w2pReachSer.getPids(sortQwords[i]);
				// 所有pid都不能到达该wid
				if(null == w2pReachable[i]) {
					Global.curRecIndex++;
					System.out.println(sortQwords[i] + "'s w2pReachable is null");
					return null;
				}
			}
		}
		if(Global.isTest) {
			Global.rr.timeBspGetW2PReach = Global.rr.getTimeSpan();
			Global.rr.setFrontTime();
		}
		
		// 获得word 的  place neighborhood
		HashMap<Integer, WordRadiusNeighborhood> wordPNMap = new HashMap<>();
		if(Global.MAX_PN_LENGTH> 0) {
			for(Integer in : qwords) {
				byte[] bs =  wIdPnSer.getPlaceNeighborhoodBin(in);
				if(null != bs) {
					wordPNMap.put(in, new WordRadiusNeighborhood(Global.radius, bs));
				}
			}
		}
		
		if(Global.isDebug) {
			System.out.println("> 完成计算wordPNMap，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()));
			Global.frontTime = System.currentTimeMillis();
		}
		
		if(Global.isTest) {
			Global.rr.timeBspGetPN = Global.rr.getTimeSpan();
			Global.rr.timeEnterkSPComputation = System.nanoTime();
		}
		
		IVisitor v = new KSPCandidateVisitor(k);
		
		KSPIndex kSPExecutor = new KSPIndex(rgi, rtreeNode2Pid, pid2RtreeLeafNode, cReach, searchedDatesWids, wid2DateNidPair, minMaxDateSer, w2pReachable, wordPNMap, maxDateSpans);
		if(eDate == null)	kSPExecutor.kSPComputation(k, Global.radius, qpoint, sortQwords, searchIntDate, v);
		else kSPExecutor.kSPComputation(k, Global.radius, matchNids, qpoint, sortQwords, searchIntDate, eIntDate, v);
		
		if(Global.isTest) {
			Global.rr.setTimeKSPComputation();
			Global.rr.setFrontTime();
			Global.rr.resultSize = ((KSPCandidateVisitor) v).size();
		}
		
		// ATTENTION: MUST reset graph after each query
		rgi.getGraph().reset();
		
		if(Global.isTest) {
			Global.curRecIndex++;
			Global.rr.timeBspClearJob = Global.rr.getTimeSpan();
			Global.rr.setTimeBsp();
			if(Global.isOutputTestInfo)	System.out.println("> 已处理" + (Global.curRecIndex) + "个sample");
		}
		
		return (KSPCandidateVisitor)v;
	}
	
	/**
	 * 主方法
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		// 输入的检索参数，依次是检索类型(0为单个时间，1为时间范围), 样本数, radius, k, 检索词数
		Global.TYPE_TEST = "SPBest";
		int searchType = 0;
		int numSample = Global.testSampleNum;
		int radius = 3;
		int k = 5;
		int numWid = 5;
		
		String sampleResultFile = null;
		
		if(args.length>0) {
			searchType = Integer.parseInt(args[0]);
			numSample = Integer.parseInt(args[1]);
			radius = Integer.parseInt(args[2]);
			k = Integer.parseInt(args[3]);
			numWid = Integer.parseInt(args[4]);
			if(args.length>5) {
				Global.MAX_PN_LENGTH= Integer.parseInt(args[5]);
			}
			if(args.length>6) {
				Global.maxDateSpan = Integer.parseInt(args[6]);
			}
			
			sampleResultFile = Global.inputDirectoryPath + Global.testSampleResultFile + "." + Global.TYPE_TEST + "." + 
					"nwlen=" + String.valueOf(Global.MAX_PN_LENGTH) + "."+ 
					"mds=" + String.valueOf(Global.maxDateSpan) + "." + 
					"t=" + String.valueOf(searchType) + "." +
					"ns=" + String.valueOf(numSample) + "." +
					"r=" + String.valueOf(radius) + "." +
					"k=" + String.valueOf(k) + "." +
					"nw=" + String.valueOf(numWid);
			
			if(args.length>7) {
				Global.MAX_WORD_FREQUENCY = Integer.parseInt(args[7]);
			}
			sampleResultFile = sampleResultFile + ".wf=" + String.valueOf(Global.MAX_WORD_FREQUENCY);
			
			if(args.length>8) {
				Global.DATE_RANGE = Integer.parseInt(args[8]);
			}
			sampleResultFile = sampleResultFile + ".dr=" + String.valueOf(Global.DATE_RANGE);
		}
		
		if(Global.isTest) {
			Global.startTime = System.currentTimeMillis();
			System.out.println("> SP_BEST 开始测试样本 nwlen=" + String.valueOf(Global.MAX_PN_LENGTH) + " " +   
								"mds=" + String.valueOf(Global.maxDateSpan) + " " + 			
								"t=" + String.valueOf(searchType) + " " +
								"ns=" + String.valueOf(numSample) + " " +
								"r=" + String.valueOf(radius) + " " +
								"k=" + String.valueOf(k) + " " +
								"nw=" + String.valueOf(numWid) + " " + 
								"wf=" + String.valueOf(Global.MAX_WORD_FREQUENCY) + " " + 
								"dr=" + String.valueOf(Global.DATE_RANGE) + " " + 
								" . . . ");
		}
		
		Global.testSampleNum = numSample;
		Global.radius = radius;
		Global.testK = k;
		
		System.out.println("> 开始初始化SPComplementDiskIndex . . . ");
		SPBest spc = new SPBest();
		System.out.println("> 成功初始化SPComplementDiskIndex ！ ！ ！ ");
		double[] pcoords = new double[2];
		pcoords[0] = 35.68275862680435;
		pcoords[1] = -85.23272932806015;
		ArrayList<Integer> qwords = new ArrayList<>();
		qwords.add(11691841);
		qwords.add(11381939);
		Date date = TimeUtility.getDate("1954-01-09");
		Date eDate = TimeUtility.getDate("1954-01-09");
		int binIntDate = 0;
		
		if(Global.isTestRangeDate) {
			Date sDate = TimeUtility.getDate("1954-01-09");
			eDate = TimeUtility.getDate("1988-01-09");
			spc.bsp(k, pcoords, qwords, sDate, eDate);
			return;
		}
		
		int samNum = numSample;
		BufferedWriter bw = null;
		if(Global.isTest) {
			// 输出结果
			bw = new BufferedWriter(new FileWriter(sampleResultFile + ".csv"));
			
			BufferedReader br = new BufferedReader(new FileReader(Global.inputDirectoryPath + Global.testSampleFile + "." + String.valueOf(Global.testOrgSampleNum) + ".t=" + String.valueOf(searchType) + ".wn=10"));
			String lineStr = null;
			while(samNum > 0) {
				lineStr = br.readLine();
				String[] strArr = lineStr.split(Global.delimiterLevel1)[1].split(Global.delimiterSpace);
				pcoords[0] = Double.parseDouble(strArr[0]);
				pcoords[1] = Double.parseDouble(strArr[1]);
				
				qwords.clear();
				for(int i=2; i<2 + numWid; i++) {
					if(numWid==1) {
						qwords.add(Integer.parseInt(strArr[i+5]));	// 避免第一个关键词为一些频繁单无意义的词，例如I,the等
						break;
					}
					qwords.add(Integer.parseInt(strArr[i]));
				}
				
				date = TimeUtility.getDate(strArr[strArr.length-2]);
				eDate = TimeUtility.getDate(strArr[strArr.length-1]);
				binIntDate = (TimeUtility.getIntDate(date) + TimeUtility.getIntDate(eDate))/2;
				
				if(searchType==0) {
					date = TimeUtility.getDate(TimeUtility.getDateByIntDate(binIntDate));
					spc.bsp(k, pcoords, qwords, date, null);
				} else {
					spc.bsp(k, pcoords, qwords, date, eDate);
				}
				
				samNum--;
				
				// 写数据
				if(Global.curRecIndex == 1) {
					System.out.println(Global.rr.getInitInfo());
					bw.write(Global.rr.getHeader());
				}
				
				bw.write(Global.rr.getBspInfo(Global.curRecIndex, 1000000));
				Global.rr = new RunRecord();
				bw.flush();
			}
			br.close();
		}
		spc.free();
		if(Global.isTest) {
			bw.close();
			System.out.println("> Over测试 SP_BEST 样本 nwlen=" + String.valueOf(Global.MAX_PN_LENGTH) + " " +   
					"mds=" + String.valueOf(Global.maxDateSpan) + " " + 
					"t=" + String.valueOf(searchType) + " " +
					"ns=" + String.valueOf(numSample) + " " +
					"r=" + String.valueOf(radius) + " " +
					"k=" + String.valueOf(k) + " " +
					"nw=" + String.valueOf(numWid) + " " + 
					"wf=" + String.valueOf(Global.MAX_WORD_FREQUENCY) + " " + 
					"dr=" + String.valueOf(Global.DATE_RANGE) + " " + 
					"，用时：" + TimeUtility.getTailTime());
			System.out.println();
		}
	}
}
