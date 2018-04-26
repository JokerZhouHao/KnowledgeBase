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
		//buffer for alpha WN inverted index 
		buffer = new LRUBuffer(Global.alphaIindexRTNodeBufferSize, Global.rtreePageSize);
		
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
	public List<KSPCandidate> bsp(int k, double[] pCoords, ArrayList<Integer> qwords, Date searchDate) throws Exception {
		
		List<KSPCandidate> semanticTreeResult = new ArrayList<>();
		
		long start = System.currentTimeMillis();
		System.out.println("> 开始执行bsp . . . ");
		
		Point qpoint = new Point(pCoords);
		
		// 获得Mq
		Map<Integer, DateWId> nIdDateWidMap = new HashMap<>();
		Map<Integer, String> tempMap = nIdWIdDateSer.searchNIDKeyListDateIndex(qwords);
		for(Entry<Integer, String> en : tempMap.entrySet()) {
			nIdDateWidMap.put(en.getKey(), new DateWId(en.getValue()));
		}
		tempMap.clear();
		
		/////////////////////////// 打印测试
		System.out.println("searchedNodeListMap : ");
		for(Entry<Integer, DateWId> en : nIdDateWidMap.entrySet()) {
			System.out.println(en.getKey() + " : " + en.getValue().getStr());
		}
		System.out.println();
		
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
		
		///////////////////////////////// 打印测试
		System.out.println("wordMinDateSpanMap : ");
		for(Entry<Integer, Integer> en : wordMinDateSpanMap.entrySet()) {
			System.out.println(en.getKey() + " - " + en.getValue());
		}
		System.out.println();
		
		// 获得word 的  place neighborhood
		HashMap<Integer, WordRadiusNeighborhood> wordPNMap = new HashMap<>();
		for(Integer in : qwords) {
			wordPNMap.put(in, new WordRadiusNeighborhood(Global.radius, wIdPnSer.getPlaceNeighborhoodStr(in)));
		}
		
		IVisitor v = new KSPCandidateVisitor(k);
		
		Global.startTime = start;
		
		kSP kSPExecutor = new kSP(semanticTreeResult, rgi, nIdDateWidMap, wordMinDateSpanMap, wordPNMap, reachableQuerySer);
		kSPExecutor.kSPComputation(k, Global.radius, qpoint, qwords, intSearchDate, v);
		long end = System.currentTimeMillis();
		Global.runtime[0] += (end - start);

		// ATTENTION: MUST reset graph after each query
		rgi.getGraph().reset();
		System.out.print("> 查找词");
		for(int in : qwords) {
			System.out.print(in + " ");
		}
		System.out.println("，共找到" + semanticTreeResult.size() + "个结果，用时：" + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()));
		return semanticTreeResult;
	}
	
	public static void main(String[] args) throws Exception{
		SPCompleteDisk spc = new SPCompleteDisk();
		double[] pcoords = new double[2];
		pcoords[0] = 1;
		pcoords[1] = 3;
		ArrayList<Integer> qwords = new ArrayList<>();
		qwords.add(25);
		qwords.add(26);
		Utility.showSemanticTreeResult(spc.bsp(3, pcoords, qwords, new Date()));
		spc.free();
	}
}
