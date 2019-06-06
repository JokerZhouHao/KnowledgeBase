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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.zip.GZIPOutputStream;

import entity.OptMethod;
import entity.sp.DateNidNode;
import entity.sp.DatesWIds;
import entity.sp.GraphByArray;
import entity.sp.QueryParams;
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
import precomputation.sample.TestInputDataBuilder;
import precomputation.sp.IndexNidKeywordsListService;
import precomputation.sp.IndexWordPNService;
import spatialindex.spatialindex.IVisitor;
import spatialindex.spatialindex.Point;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IBuffer;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;
import spatialindex.storagemanager.TreeLRUBuffer;
import utility.FileMakeOrLoader;
import utility.Global;
import utility.MComparator;
import utility.MLog;
import utility.RandomNumGenerator;
import utility.TimeUtility;

/**
 * 使用索引来获得可达性的SP实现
 * @author Monica
 *
 */
public class SPBest implements SPInterface{
	
	// 静态变量
	private static Set<Integer>[] rtreeNode2Pid = null;
	private static CReach cReach = null;
	private static MinMaxDateService minMaxDateSer = null;
	private static int[] pid2RtreeLeafNode = null;
	private static Set<Integer> widHasDate = null;
	
	// 非静态变量
	private IndexNidKeywordsListService nIdWIdDateSer = null;
	private IndexWordPNService wIdPnSer = null;
	private IndexWordPNService wIdPnInfSer = null;
	private IndexWordPNService wIdPnNodateSer = null;
	private LRUBuffer buffer = null;
	private RTreeWithGI rgi = null;
	private Wid2DateNidPairIndex wid2DateNidPairIndex = null;
	private W2PReachService w2pReachSer = null;
	private Map<Integer, Map<Integer, String>> cacheSeachedWid = new HashMap<>();	// 缓存关键词的查询结果
	public static RandomNumGenerator dateSpanGen = new RandomNumGenerator(0, 7);
	private DatesWIds searchedDatesWids[] = new DatesWIds[Global.numNodes];
	
	private QueryParams qp = null;
	private ArrayBlockingQueue<QueryParams> qpQueue = null;
	
	/**
	 * 初始化
	 * @throws Exception
	 */
	public SPBest(ArrayBlockingQueue<QueryParams> queue) throws Exception{
		this.qpQueue = queue;
		
		// 初始化静态数据
		if(null == rtreeNode2Pid)	rtreeNode2Pid = P2WRTreeReach.loadRTreeNode2Pids(Global.recRTreeNode2NidReachPath);
		if(null == cReach)	cReach = new CReach(Global.outputDirectoryPath + Global.sccFile, Global.outputDirectoryPath + Global.indexTFLabel, Global.numSCCs);
		if(null == pid2RtreeLeafNode)	pid2RtreeLeafNode = RTreeLeafNodeContainPids.loadPid2RTreeLeafNode(Global.recRTreeLeafNodeContainPidsPath);
		if(null == minMaxDateSer)	minMaxDateSer = new MinMaxDateService(Global.outputDirectoryPath + Global.minMaxDatesFile);
		if(null == widHasDate)	widHasDate = FileMakeOrLoader.loadWidHasDate();
		if(null == Global.wordFrequency) Global.wordFrequency = IndexNidKeywordsListService.loadWordFrequency(Global.outputDirectoryPath + Global.wordFrequencyFile);
		
		// 由于rgi在计算时，会上锁，故每个SPBest实例都会新创建个rgi
		//buffer for alpha WN inverted index 
		buffer = new LRUBuffer(Global.alphaIindexRTNodeBufferSize, Global.rtreePageSize);
		
		//the data index structure of RDF data with R-tree, RDF Graph, and Inverted index of keywords
		try {
			if(rgi == null) {
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
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		// 添加点对可达时间记录
		if(Global.isDebug)	Global.recReachBW = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(Global.fileReachGZip)))));
	}
	
	/**
	 * 打开各索引文件
	 * @param qp
	 */
	public void openIndex(QueryParams qp) {
		// 各索引路径
		String nIdWIdDateIndex = Global.outputDirectoryPath + Global.indexNIdWordDate;
		String wIdPNIndex = Global.outputDirectoryPath + Global.indexWidPN + "_" + String.valueOf(qp.radius) + "_" + String.valueOf(qp.MAX_PN_LENGTH) + File.separator;
		String wIdPNInfIndex = Global.outputDirectoryPath + Global.indexWidPN + "_" + String.valueOf(qp.radius) + "_" + Global.INFINITE_PN_LENGTH_STR + File.separator;
		String wIdPNNodateIndex = Global.outputDirectoryPath + Global.indexWidPNNodate + "_" + String.valueOf(qp.radius) + "_" + Global.INFINITE_PN_LENGTH_STR + File.separator;
		
		nIdWIdDateSer = new IndexNidKeywordsListService(nIdWIdDateIndex);
		nIdWIdDateSer.openIndexReader();
		
		if(qp.MAX_PN_LENGTH> 0) {
			wIdPnSer = new IndexWordPNService(wIdPNIndex);
			wIdPnSer.openIndexReader();
		}
		wIdPnInfSer = new IndexWordPNService(wIdPNInfIndex);
		wIdPnInfSer.openIndexReader();
		wIdPnNodateSer = new IndexWordPNService(wIdPNNodateIndex);
		wIdPnNodateSer.openIndexReader();
		
		wid2DateNidPairIndex = new Wid2DateNidPairIndex(Global.indexWid2DateNid);
		wid2DateNidPairIndex.openIndexReader();
		
		String indexWid2PidBase = Global.outputDirectoryPath + "wid_2_pid_reachable_pidDis_fre=" + String.valueOf(qp.MAX_WORD_FREQUENCY) + File.separator + "wids_block_";
		w2pReachSer = new W2PReachService(indexWid2PidBase);
		w2pReachSer.openIndexs();
	}
	
	/**
	 * 释放资源
	 * @throws Exception
	 */
	public void free() throws Exception{
		nIdWIdDateSer.closeIndexReader();
		if(qp.MAX_PN_LENGTH> 0)	wIdPnSer.closeIndexReader();
		wIdPnInfSer.closeIndexReader();
		wIdPnNodateSer.closeIndexReader();
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
			qp.rr.setFrontTime();
		}
		
		Point qpoint = new Point(pCoords);
		int searchIntDate = TimeUtility.getIntDate(searchDate);
		int eIntDate = Integer.MIN_VALUE;
		if(null != eDate) {
			searchIntDate -= qp.DATE_RANGE;
			eIntDate = TimeUtility.getIntDate(eDate) + qp.DATE_RANGE + 1;
		}
		
		int i = 0;
		
		// 获得有序的查询词
		ArrayList<Integer> sortedQwordsList = new ArrayList<>(qwords);
		sortedQwordsList.sort(new MComparator<Integer>());
		int sortQwords[] = new int[sortedQwordsList.size()];
		for(i=0; i<sortQwords.length; i++) {
			sortQwords[i] = sortedQwordsList.get(i);
		}
		
		// 获得wid2DateNid和nIdDateWidMap
		if(Global.isTest) {
			qp.rr.setFrontTime();
		}
		boolean[] signInDate = new boolean[sortQwords.length];
		for(i=0; i<searchedDatesWids.length; i++)	searchedDatesWids[i] = null;
		DatesWIds dws = null;
		
		SortedDateWidIndex[] wid2DateNidPair = new SortedDateWidIndex[sortedQwordsList.size()];
		int[] maxDateSpans = new int[sortedQwordsList.size()];
		ArrayList<DateNidNode> dnList = null;
		int tDateSpan = 0;
		int sDate = TimeUtility.getIntDate(searchDate);
		
		// 纪录哪些点在时间范围内
		List<Integer>[] nidsInDate = new ArrayList[sortQwords.length];
		
		for(i=0; i<sortQwords.length; i++) {
			// 检索
			wid2DateNidPair[i] = wid2DateNidPairIndex.getDateNids(sortQwords[i], TimeUtility.getIntDate(searchDate));
			// 不存在该wid
			if(null==wid2DateNidPair[i]) {
				return null;
			}
			
			// init
			dnList = wid2DateNidPair[i].dateWidList;
			wid2DateNidPair[i] = new SortedDateWidIndex();
			int maxDate = sDate;	// 如果不存在包含查询词w并且带有时间的节点，则那些没有时间的节点默认使用查询时间
			
			// 遍历
			for(DateNidNode dnn : dnList) {
				tDateSpan = Math.abs(dnn.getDate() - sDate);
				if(tDateSpan  >= qp.maxDateSpan)
					dnn.isMax = Boolean.TRUE;
				
				// 获得wid2DateNid
				if(eDate == null && dnn.getDate() != Global.TIME_INAVAILABLE) {
					if(tDateSpan > maxDateSpans[i]) {
						maxDateSpans[i] = tDateSpan;
						maxDate = dnn.getDate();
					}
					wid2DateNidPair[i].addDateWid(dnn);
				}
				
				// 获得nIdDateWidMap
				if(null == (dws = searchedDatesWids[dnn.getNid()])) {
					if(dnn.getDate() == Global.TIME_INAVAILABLE)	{
						dws = new DatesWIds(Global.TIME_INAVAILABLE, sortQwords.length);     /****************  注意：此处对于没有时间的节点给的是Global.TIME_INAVAILABLE而不是maxDate   **********/
					}
					else	dws = new DatesWIds(dnn.getDate(), sortQwords.length);
					searchedDatesWids[dnn.getNid()] = dws;
				}
				dws.addWid(i, sortQwords[i]);
				
				// 获得在时间范围内的nid，设置signInDate
				if(null != eDate && dnn.getDate() >= sDate && dnn.getDate() <= eIntDate) {
					if(null == nidsInDate[i])	nidsInDate[i] = new ArrayList<>();
					nidsInDate[i].add(dnn.getNid());
				}
				if(dnn.getDate() != Global.TIME_INAVAILABLE) {
					signInDate[i] = Boolean.TRUE;
				}
			}
			
			qp.rr.numBspWid2DateWid += wid2DateNidPair[i].size();
		}
		if(Global.isTest) {
			qp.rr.timeBspBuidingWid2DateNid += qp.rr.getTimeSpan();
			qp.rr.setFrontTime();
		}
		
		// 判断是否至少有一个词在时间范围内
		List<Integer> matchNids = null;
		if(eDate != null) {
//			for(i=0; i<signInRange.length; i++)
//				if(signInRange[i])	break;
//			if(i==signInRange.length) {
//				Global.curRecIndex++;
//				return null;
//			}
			
			// 获得符合查询条件的点
//			matchNids = new ArrayList<>();
//			for(i=0; i<searchedDatesWids.length; i++) {
//				if(null != searchedDatesWids[i]) {
//					matchNids.add(i);
//				}
//			}
		}
		
		// 获得W2PReachable
		if(Global.isTest)	qp.rr.setFrontTime();
		Map<Integer, Short>[] w2pReachable = new Map[sortQwords.length];
		if(qp.optMethod == OptMethod.O5 || qp.optMethod == OptMethod.O4 || qp.optMethod == OptMethod.O2) {
			for(i=0; i<sortQwords.length; i++) {
				if(Global.wordFrequency.get(sortQwords[i]) >= qp.MAX_WORD_FREQUENCY) {
					w2pReachable[i] = w2pReachSer.getPids(sortQwords[i]);
					// 所有pid都不能到达该wid
					if(null == w2pReachable[i]) {
						MLog.log(sortQwords[i] + "'s w2pReachable is null");
						return null;
					}
				}
			}
		}
		if(Global.isTest) {
			qp.rr.timeBspGetW2PReach = qp.rr.getTimeSpan();
			qp.rr.setFrontTime();
			if(qp.optMethod == OptMethod.O4) {
				qp.rr.timeBspStart += qp.rr.timeBspGetW2PReach;
			}
		}
		
		// 获得word 的  place neighborhood
		HashMap<Integer, WordRadiusNeighborhood> wordPNMap = new HashMap<>();
		if(qp.MAX_PN_LENGTH> 0) {
			for(i=0; i<sortQwords.length; i++) {
				byte[] bs = null;
				if(widHasDate.contains(sortQwords[i])) {
					if(qp.optMethod == OptMethod.O5 || qp.optMethod == OptMethod.O3) {
						bs =  wIdPnSer.getPlaceNeighborhoodBin(sortQwords[i]);
					} else {
						bs = wIdPnInfSer.getPlaceNeighborhoodBin(sortQwords[i]);
					}
					if(null != bs)	wordPNMap.put(sortQwords[i], new WordRadiusNeighborhood(qp.radius, bs));
				} else {
					bs = wIdPnNodateSer.getPlaceNeighborhoodBin(sortQwords[i]);
					if(null != bs)	wordPNMap.put(sortQwords[i], new WordRadiusNeighborhood(qp.radius, bs, Boolean.FALSE));
					else	return null;	// 因为wIdPnNodateSer记录了所有不带有时间的关键词
				}
			}
		}
		
		if(Global.isDebug) {
			System.out.println("> 完成计算wordPNMap，用时" + TimeUtility.getSpendTimeStr(Global.frontTime, System.currentTimeMillis()));
			Global.frontTime = System.currentTimeMillis();
		}
		
		if(Global.isTest) {
			qp.rr.timeBspGetPN = qp.rr.getTimeSpan();
			qp.rr.timeEnterkSPComputation = System.nanoTime();
		}
		
		IVisitor v = new KSPCandidateVisitor(k);
		
		KSPIndex kSPExecutor = new KSPIndex(rgi, rtreeNode2Pid, pid2RtreeLeafNode, cReach, searchedDatesWids, wid2DateNidPair, minMaxDateSer, 
						w2pReachable, wordPNMap, maxDateSpans, signInDate, qp, nidsInDate);
		if(eDate == null)	kSPExecutor.kSPComputation(k, qp.radius, qpoint, sortQwords, searchIntDate, v);
		else kSPExecutor.kSPComputation(k, qp.radius, matchNids, qpoint, sortQwords, searchIntDate, eIntDate, v);
		
		if(Global.isTest) {
			qp.rr.setTimeKSPComputation();
			qp.rr.setFrontTime();
			qp.rr.resultSize = ((KSPCandidateVisitor) v).size();
		}
		
		// ATTENTION: MUST reset graph after each query
		rgi.getGraph().reset();
		
		if(Global.isTest) {
			qp.rr.timeBspClearJob = qp.rr.getTimeSpan();
		}
		
		return (KSPCandidateVisitor)v;
	}
	
	/**
	 * 主方法
	 * @param args
	 * @throws Exception
	 */
	public void test(QueryParams qp) throws Exception{
		qp.startTime = System.currentTimeMillis();
		this.qp = qp;
		
		// 输入的检索参数，依次是检索类型(0为单个时间，1为时间范围), 样本数, radius, k, 检索词数
		String algName = "SPBest";
		String sampleResultFile = qp.resultPath(algName);
		
		if(Global.isTest) {
			MLog.log(qp.startInfo(algName));
		}
		
		// 打开索引
		openIndex(qp);
		
		double[] pcoords = new double[2];
		pcoords[0] = 35.68275862680435;
		pcoords[1] = -85.23272932806015;
		ArrayList<Integer> qwords = new ArrayList<>();
		qwords.add(11691841);
		qwords.add(11381939);
		Date date = TimeUtility.getDate("1954-01-09");
		Date eDate = TimeUtility.getDate("1954-01-09");
		int binIntDate = 0;
		
		BufferedWriter bw = null;
		
		int samNum = qp.testSampleNum;
		
		if(Global.isTest) {
			// 输出结果
			bw = new BufferedWriter(new FileWriter(sampleResultFile));
			
			// 写csv header
			if(qp.curRecIndex == 1) {
				bw.write(qp.rr.getHeader());
			}
			
			BufferedReader br = new BufferedReader(new FileReader(Global.inputDirectoryPath + Global.testSampleFile + "." + String.valueOf(Global.testOrgSampleNum) + ".wn=" + qp.numWid));
			String lineStr = null;
			while(samNum > 0) {
				lineStr = br.readLine().trim();
				if(lineStr.isEmpty() || lineStr.startsWith("#"))	continue;
				
				String[] strArr = lineStr.split(Global.delimiterLevel1)[1].split(Global.delimiterSpace);
				pcoords[0] = Double.parseDouble(strArr[0]);
				pcoords[1] = Double.parseDouble(strArr[1]);
				
				qwords.clear();
				for(int i=2; i<2 + qp.numWid; i++) {
//					if(qp.numWid==1) {
//						qwords.add(Integer.parseInt(strArr[i+5]));	// 避免第一个关键词为一些频繁单无意义的词，例如I,the等
//						break;
//					}
					qwords.add(Integer.parseInt(strArr[i]));
				}
				
				date = TimeUtility.getDate(strArr[strArr.length-1]);
				eDate = date;
				binIntDate = (TimeUtility.getIntDate(date) + TimeUtility.getIntDate(eDate))/2;
				
				// 设置起始时间
				if(Global.isTest) {
					qp.rr.timeBspStart = System.nanoTime();
				}
				
				if(qp.searchType==0) {
					date = TimeUtility.getDate(TimeUtility.getDateByIntDate(binIntDate));
					bsp(qp.testK, pcoords, qwords, date, null);
				} else {
					bsp(qp.testK, pcoords, qwords, date, date);
				}
				
				// 设置结束时间
				if(Global.isTest) {
					qp.rr.setTimeBsp();
				}
				
				bw.write(qp.rr.getBspInfo(qp.curRecIndex, 1000000));
				bw.flush();
				qp.rr = new RunRecord();
				
				qp.curRecIndex++;
				samNum--;
			}
			br.close();
		}
		
		// 释放资源
		free();
		
		if(Global.isTest) {
			bw.close();
			MLog.log(qp.endInfo(algName));
		}
	}
	
	@Override
	public void run() {
		try {
			QueryParams params = null;
			while(true) {
				params = qpQueue.take();
				if(params == AlgTest.SIGN_OVER_TEST) {
					break;
				}
				test(params);
				AlgTest.decreaseTask();
			}
		} catch (Exception e) {
			e.printStackTrace();
			MLog.log("SPBest线程异常退出  !");
			System.exit(0);
		}
	}

	public static void main(String[] args) throws Exception {
		String filePath = Global.inputDirectoryPath + File.separator + "sample_result" + File.separator + args[0];
		List<QueryParams> qps = TestInputDataBuilder.loadTestQuery(filePath);
		SPBest sb = new SPBest(null);
		for(QueryParams q : qps) {
			sb.test(q);
		}
	}
}
