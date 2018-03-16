package zhou.hao.yago2s.processor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map.Entry;

import sil.spatialindex.IEntry;
import sil.spatialindex.Point;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.RandomNumGenerator;
import zhou.hao.tools.TimeStr;
import zhou.hao.yago2s.entity.MinHeap;
import zhou.hao.yago2s.entity.PNode;
import zhou.hao.yago2s.entity.PTree;
import zhou.hao.yago2s.entity.MinHeap.DisPTree;
import zhou.hao.yago2s.entity.MinHeap.MLinkedList;
import zhou.hao.yago2s.entity.MinHeap.MLinkedNode;
import zhou.hao.yago2s.service.BuildMapService;
import zhou.hao.yago2s.service.BuildNidKeywordListMapService;
import zhou.hao.yago2s.service.IndexCoordService;
import zhou.hao.yago2s.service.IndexNidKeywordsListService;
import zhou.hao.yago2s.service.IndexNidKeywordsListService.KeywordIdDateList;
import zhou.hao.yago2s.service.Yago2sInfoService;

/**
 * 
 * @author Monica
 * @since 2018/3/8
 * 功能 ：实现论文Top-k Relevant Semantic Place Retrieval on Spatial RDF Data 中的BSP算法
 */
public class BSPProcessor {
	
	private IndexCoordService indexCooorSer = null;
	private ArrayList<Integer>[] yago2sArrMap = null;
	private IndexNidKeywordsListService indexNidKeywordsListService = null;
	private Boolean hasInit = Boolean.FALSE;
	
	public IndexCoordService getIndexCooorSer() {
		return indexCooorSer;
	}

	// 初始化成员变量
	public void init() {
//		indexCooorSer = new IndexCoordService(LocalFileInfo.getDataSetPath() + "yagoVB.zip", "pidCoordYagoVB.txt");
//		indexCooorSer.buildRTree();
//		yago2sArrMap = new BuildMapService(LocalFileInfo.getDataSetPath() + "yagoVB.zip", "edgeYagoVB.txt").buildMap();
//		indexNidKeywordsListService = new IndexNidKeywordsListService(LocalFileInfo.getYagoZipIndexBasePath() + "NidKeywordsListMapDBpediaVBTxt");
		
		indexCooorSer = new IndexCoordService(LocalFileInfo.getDataSetPath() + "test.zip", "pidCoordYagoVB.txt");
		indexCooorSer.buildRTree();
		yago2sArrMap = new BuildMapService(LocalFileInfo.getDataSetPath() + "test.zip", "edgeYagoVB.txt").buildMap();
		indexNidKeywordsListService = new IndexNidKeywordsListService(LocalFileInfo.getDataSetPath() + "testIndex");
		
		hasInit = Boolean.TRUE;
	}
	
	public MinHeap bsp(int k, double[] pCoords, ArrayList<Integer> wordIdList, Date curDate){
		
		Point point = new Point(pCoords);
		
		// 初始化所需数据
		if(!hasInit)  this.init();
		
		MinHeap minHeap = new MinHeap();;
		
		// 获得Mq
		indexNidKeywordsListService.openIndexReader();
		HashMap<Integer, KeywordIdDateList> searchedNodeListMap = indexNidKeywordsListService.searchKeywordIdListReNodeIdMap(wordIdList);
		indexNidKeywordsListService.closeIndexReader();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for(Entry<Integer, KeywordIdDateList> en : searchedNodeListMap.entrySet()) {
			System.out.print(en.getKey() + " : ");
			for(Date da : en.getValue().getDateList())
				System.out.print(sdf.format(da) + " ");
			System.out.print(" : ");
			for(Integer in : en.getValue().getKeywordIdList())
				System.out.print(in + " ");
			System.out.println();
		}
		System.out.println();
		
		// 阈值
		double threshold = Double.POSITIVE_INFINITY;
		
		// 循环计算
		IEntry iEntry = null;
		indexCooorSer.initGetNext(point);
		double curMinDis = 0;
		while(null != (iEntry = indexCooorSer.getNext())) {
			if((curMinDis = point.getMinimumDistance(iEntry.getShape())) >= threshold)	break;
			
			// 实现 GETSEMANTICPLACE方法 starting
			PNode curPNode = new PNode(iEntry.getIdentifier(), false);
			PTree pTree = new PTree(curPNode);
			pTree.addNode(null, new PNode(Integer.MIN_VALUE, Boolean.FALSE));// Integer.MIN_VALUE表示bfs树的新的一层
			LinkedList<Integer> copyWordIdList = new LinkedList<>(wordIdList);
			
			HashMap<Integer, Boolean> hasAccessNodeMap = new HashMap<>();
			
			int curPTreeLevel = 0;
			double pTreeLen = 1;
			KeywordIdDateList curKIDL = null;
			
			ArrayList<Integer> tempList = null;
			ArrayList<Date> tempDates = null; 
			
			long tempLong = 0;
			int i = 0, tempNum = 0;
			
			// bfs
			while(null != curPNode) {
				
				if(Integer.MIN_VALUE == curPNode.getId()) {
					curPNode = curPNode.getNext();
					if(null == curPNode)	break;
					else {
						pTree.addNode(null, new PNode(Integer.MIN_VALUE, Boolean.FALSE));
						curPTreeLevel++;
					}
				}
				
				// 处理当前点
				Boolean isLeaf = Boolean.FALSE;
				if(!hasAccessNodeMap.containsKey(curPNode.getId()) && null != (curKIDL = searchedNodeListMap.get(curPNode.getId()))){
					tempNum = 0;
					tempList = curKIDL.getKeywordIdList();
					// 标记是否已提前找到所有点
					Boolean isBreak = Boolean.FALSE;
					// 计算相交的词数
					for(int in1 : tempList) {
						i = Integer.MIN_VALUE;
						for(int in2 : copyWordIdList) {
							if(in1 == in2) {
								i = in2;
								break;
							}
						}
						if(i != Integer.MIN_VALUE) {
							copyWordIdList.remove((Object)i);
							tempNum++;
							if(copyWordIdList.isEmpty()) {
								isBreak = Boolean.TRUE;
								break;
							}
						}
					}
					
					if(0 != tempNum) {
						tempDates = curKIDL.getDateList();
						// 计算离当前最短的天数
						long minGapDay = Long.MAX_VALUE;
						for(Date da : tempDates) {
							if(minGapDay > (tempLong = TimeStr.calGapBetweenDate(da, curDate)))
								minGapDay = tempLong;
						}
						
						pTreeLen += tempNum * curPTreeLevel * minGapDay;
						curPNode.setLeaf(Boolean.TRUE);
					}
					
					if(isBreak)	break;
				}
				
				// 当前点已访问，添加到访问map中
				hasAccessNodeMap.put(curPNode.getId(), Boolean.TRUE);
				
				// 添加当前点连接的边
				if(null != (tempList = yago2sArrMap[curPNode.getId()])) {
					for(int in : tempList) {
						if(!hasAccessNodeMap.containsKey(in)) {
							pTree.addNode(curPNode, new PNode(in, Boolean.FALSE));
						}
					}
				}
				
				// 设置当前点
				curPNode = curPNode.getNext();
			}
			//  GETSEMANTICPLACE方法 ending
			
			if(copyWordIdList.isEmpty()){
				pTree.deleteUnnecessaryNode();
				if(k != minHeap.size()) minHeap.addPTree(pTreeLen * curMinDis, pTree);
				else minHeap.updatePTree(pTreeLen * curMinDis, pTree);
				threshold = minHeap.getLast().getNodeInfo().getDistance();
				
				// 测试打印
				System.out.println(pTreeLen * curMinDis);
				pTree.displayPath();
				System.out.println();
			}
		}
		return minHeap;
	}
	
	
	// 主函数
	public static void main(String[] args) {
		BSPProcessor pro = new BSPProcessor();
		RandomNumGenerator randomGe = new RandomNumGenerator(Yago2sInfoService.keywordStartId, Yago2sInfoService.keywordStartId + Yago2sInfoService.keywordNum -1);
		RandomNumGenerator pointIdGe = new RandomNumGenerator(0, Yago2sInfoService.coordNum-1);
		ArrayList<Integer>	wordIdList = new ArrayList<>();
		pro.init();
		
		
		double[] pCoords = {2, 3};
		wordIdList.add(21);
		wordIdList.add(23);
		MinHeap minHeap = pro.bsp(2, pCoords, wordIdList, TimeStr.getNowDate());
		System.out.println("\n> 结果 : ");
		MLinkedNode<DisPTree> dpt = minHeap.getDisPTreeList().getHead().getNext();
		while(null != dpt) {
			System.out.println(dpt.getNodeInfo().getDistance());
			dpt.getNodeInfo().getpTree().displayPath();
			System.out.println();
			dpt = dpt.getNext();
		}
		System.out.println();
		
		
		
//		ArrayList<Integer>[] nidKeywordArr = new BuildNidKeywordListMapService().buildNidKeywordListMapArr();
//		IndexCoordService cooSe = pro.getIndexCooorSer();
//		ArrayList<PointAndId> pIdList = null;
//		ArrayList<Integer>	tempList = null;
//		while(true) {
//			wordIdList.clear();
//			pIdList = cooSe.nearestN(cooSe.getPoint(pointIdGe.getRandomNum()), 4);
//			boolean sign = false;
//			for(int i=0; i<3; i++) {
//				if(null != (tempList = nidKeywordArr[pIdList.get(i+1).getId()])) {
//					wordIdList.add(tempList.get(0));
//				} else {
//					sign = true;
//					break;
//				}
//			}
//			if(sign)	continue;
//			resList = pro.getPointToWordsIdList(pIdList.get(0).getPoint(), wordIdList, System.currentTimeMillis());
//			if(null != resList) {
//				System.out.print("> 结果 : ");
//				for(int in : resList) {
//					System.out.print(in + " ");
//				}
//				break;
//			} else continue;
//		}
	}
	
}
