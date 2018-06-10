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
import kSP.kSPCReach;
import kSP.candidate.KSPCandidate;
import kSP.candidate.KSPCandidateVisitor;
import neustore.base.LRUBuffer;
import precomputation.rechable.ReachableQueryService;
import precomputation.sp.IndexNidKeywordsListService;
import precomputation.sp.IndexWordPNService;
import queryindex.VertexQwordsMap;
import rdfindex.memory.RTreeWithGI;
import spatialindex.spatialindex.IVisitor;
import spatialindex.spatialindex.Point;
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
public class SPComplete {
	
	private IndexNidKeywordsListService nIdWIdDateSer = null;
	private IndexNidKeywordsListService wIdDateSer = null;
	private ReachableQueryService reachableQuerySer = null;
	private IndexWordPNService wIdPnSer = null;
	private LRUBuffer buffer = null;
	private RTreeWithGI rgi = null;
	
	public SPComplete() {
		//buffer for alpha WN inverted index 
		buffer = new LRUBuffer(Global.alphaIindexRTNodeBufferSize, Global.rtreePageSize);
		
		// 各索引路径
		String nIdWIdDateIndex = Global.outputDirectoryPath + Global.indexNIdWordDate;
		String wIdDateIndex = Global.outputDirectoryPath + Global.indexWIdDate;
		String sccPath = Global.outputDirectoryPath + Global.sccFile;
		String tfLabelIndex = Global.outputDirectoryPath + Global.indexTFLabel;
		String wIdPNIndex = Global.outputDirectoryPath + Global.indexWidPN;
		
		nIdWIdDateSer = new IndexNidKeywordsListService(nIdWIdDateIndex);
		wIdDateSer = new IndexNidKeywordsListService(wIdDateIndex);
		reachableQuerySer = new ReachableQueryService(sccPath, tfLabelIndex);
		wIdPnSer = new IndexWordPNService(wIdPNIndex);
		
		//the data index structure of RDF data with R-tree, RDF Graph, and Inverted index of keywords
		try {
			rgi = RGIUtility.buildRGI();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
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
		nIdWIdDateSer.openIndexReader();
		Map<Integer, String> tempMap = nIdWIdDateSer.searchNIDKeyListDateIndex(qwords);
		for(Entry<Integer, String> en : tempMap.entrySet()) {
			nIdDateWidMap.put(en.getKey(), new DateWId(en.getValue()));
		}
		tempMap.clear();
		nIdWIdDateSer.closeIndexReader();
		
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
		wIdDateSer.openIndexReader();
		for(int in : qwords) {
			ArrayList<Integer> dateList = new ArrayList<>();
			dateArr = wIdDateSer.searchWIDDateIndex(in).split(Global.delimiterDate);
			for(String st : dateArr) {
				dateList.add(Integer.parseInt(st));
			}
			wordMinDateSpanMap.put(in, TimeUtility.getMinDateSpan(intSearchDate, dateList));
		}
		wIdDateSer.closeIndexReader();
		
		///////////////////////////////// 打印测试
		System.out.println("wordMinDateSpanMap : ");
		for(Entry<Integer, Integer> en : wordMinDateSpanMap.entrySet()) {
			System.out.println(en.getKey() + " - " + en.getValue());
		}
		System.out.println();
		
		// 获得word 的  place neighborhood
		HashMap<Integer, WordRadiusNeighborhood> wordPNMap = new HashMap<>();
		wIdPnSer.openIndexReader();
		for(Integer in : qwords) {
			wordPNMap.put(in, new WordRadiusNeighborhood(Global.radius, wIdPnSer.getPlaceNeighborhoodStr(in)));
		}
		wIdPnSer.closeIndexReader();
		
		IVisitor v = new KSPCandidateVisitor(k);
		
		Global.startTime = start;
		
		kSPCReach kSPExecutor = new kSPCReach(semanticTreeResult, rgi, nIdDateWidMap, wordMinDateSpanMap, wordPNMap, reachableQuerySer);
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
		SPComplete spc = new SPComplete();
		double[] pcoords = new double[2];
		pcoords[0] = 1;
		pcoords[1] = 3;
		ArrayList<Integer> qwords = new ArrayList<>();
		qwords.add(25);
		qwords.add(26);
		Utility.showSemanticTreeResult(spc.bsp(3, pcoords, qwords, new Date()));
	}
}
