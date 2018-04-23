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
import kSP.kSP;
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
	private Map<Integer, List<List<Integer>>> semanticTreeResult = null;
	
	public SPComplete() {
		//buffer for alpha WN inverted index 
		buffer = new LRUBuffer(Global.alphaIindexRTNodeBufferSize, Global.rtreePageSize);
		
		// 各索引路径
		String nIdWIdDateIndex = Global.outputDirectoryPath + Global.indexNIdWordDate;
		String wIdDateIndex = Global.outputDirectoryPath + Global.indexWIdDate;
		String sccPath = Global.outputDirectoryPath + Global.numNodes + "." + Global.numSCCs + Global.sccFlag;
		String tfLabelIndex = Global.outputDirectoryPath + Global.indexTFLabel;
		String wIdPNIndex = Global.outputDirectoryPath + Global.indexWidPN;
		
		nIdWIdDateSer = new IndexNidKeywordsListService(nIdWIdDateIndex);
		wIdDateSer = new IndexNidKeywordsListService(wIdDateIndex);
		reachableQuerySer = new ReachableQueryService(sccPath, tfLabelIndex);
		wIdPnSer = new IndexWordPNService(wIdPNIndex);
		semanticTreeResult = new HashMap<>();
		
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
	public void bsp(int k, double[] pCoords, ArrayList<Integer> qwords, Date searchDate) throws Exception {
		
		long start = System.currentTimeMillis();
		
		Point qpoint = new Point(pCoords);
		
//		// 排序wordIdList
//		ArrayList<Integer> sortedWordList = new ArrayList<>(wordIdList);
//		sortedWordList.sort(new MComparator<Integer>());
//		////////////////////////////打印测试
//		System.out.println("sortedWordList : ");
//		for(Integer in : sortedWordList) {
//			System.out.print(in + " ");
//		}
//		System.out.println("\n");
		
		// 获得Mq
		Map<Integer, DateWId> dateWIdMap = new HashMap<>();
		nIdWIdDateSer.openIndexReader();
		Map<Integer, String> tempMap = nIdWIdDateSer.searchNIDKeyListDateIndex(qwords);
		for(Entry<Integer, String> en : tempMap.entrySet()) {
			dateWIdMap.put(en.getKey(), new DateWId(en.getValue()));
		}
		tempMap.clear();
		nIdWIdDateSer.closeIndexReader();
		
		/////////////////////////// 打印测试
//		System.out.println("searchedNodeListMap : ");
//		for(Entry<Integer, String> en : nIdDateWidMap.entrySet()) {
//		System.out.println(en.getKey() + " : " + en.getValue());
//		}
//		System.out.println();
		
		// 计算与当前时间时差最小的word组成的map
		String[] dateArr = null;
		ArrayList<Integer> dateList = new ArrayList<>();
		HashMap<Integer, Integer> wordMinDateSpanMap = new HashMap<>();
		int intSearchDate = TimeUtility.getIntDate(searchDate);
		wIdDateSer.openIndexReader();
		for(int in : qwords) {
			dateArr = wIdDateSer.searchWIDDateIndex(in).split(Global.delimiterDate);
			dateList.clear();
			for(String st : dateArr) {
				dateList.add(Integer.parseInt(st));
			}
			wordMinDateSpanMap.put(in, TimeUtility.getMinDateSpan(intSearchDate, dateList));
		}
		wIdDateSer.closeIndexReader();
		
		///////////////////////////////// 打印测试
		System.out.println("wordDateSpanMap : ");
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
		kSP kSPExecutor = new kSP(semanticTreeResult, rgi, dateWIdMap, wordMinDateSpanMap, wordPNMap, reachableQuerySer);
		
		kSPExecutor.kSPComputation(k, Global.radius, qpoint, qwords, intSearchDate, v);
		long end = System.currentTimeMillis();
		Global.runtime[0] += (end - start);

		// ATTENTION: MUST reset graph after each query
		rgi.getGraph().reset();
	}
}
