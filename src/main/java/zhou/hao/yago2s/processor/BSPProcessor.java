package zhou.hao.yago2s.processor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import sil.spatialindex.IEntry;
import sil.spatialindex.Point;
import zhou.hao.helper.MComparator;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.RandomNumGenerator;
import zhou.hao.tools.TimeStr;
import zhou.hao.yago2s.entity.MinHeap;
import zhou.hao.yago2s.entity.PNode;
import zhou.hao.yago2s.entity.PTree;
import zhou.hao.yago2s.entity.MinHeap.DisPTree;
import zhou.hao.yago2s.entity.MinHeap.MLinkedNode;
import zhou.hao.yago2s.service.BuildMapService;
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
	
	// 记录距离+1乘以日期差+1的最小的点
	private class MinDisMulDateSpanRec{
		Integer lValue = Integer.MAX_VALUE;
		PNode node =  null;
		
		public MinDisMulDateSpanRec() {}

	}
	
	// bsp算法实现
	public MinHeap bsp(int k, double[] pCoords, ArrayList<Integer> wordIdList, Date curDate){
		
		Point point = new Point(pCoords);
		
		// 初始化所需数据
		if(!hasInit)  this.init();
		
		// 记录结果
		MinHeap minHeap = new MinHeap();;
		
		// 排序wordIdList
		ArrayList<Integer> sortedWordList = new ArrayList<>(wordIdList);
		sortedWordList.sort(new MComparator<Integer>());
		////////////////////////////打印测试
		System.out.println("sortedWordList : ");
		for(Integer in : sortedWordList) {
			System.out.print(in + " ");
		}
		System.out.println("\n");
		
		// 获得Mq
		indexNidKeywordsListService.openIndexReader();
		HashMap<Integer, KeywordIdDateList> searchedNodeListMap = indexNidKeywordsListService.searchKeywordIdListReNodeIdMap(wordIdList);
		indexNidKeywordsListService.closeIndexReader();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		/////////////////////////// 打印测试
		System.out.println("searchedNodeListMap : ");
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
		
		// 计算与当前时间时差最小的点组成的map
		HashMap<Integer, Integer> wordDateSpanMap = new HashMap<>();
		HashMap<Integer, Integer> nodeDateSpanMap = new HashMap<>();
		for(Integer in : wordIdList)	wordDateSpanMap.put(in, Integer.MAX_VALUE);
		int tempI1 = Integer.MAX_VALUE, tempI2 = 0, tempI3, tempI4;
		for(Entry<Integer, KeywordIdDateList> en : searchedNodeListMap.entrySet()) {
			tempI1 = Integer.MAX_VALUE;
			// 求节点的最小时间差
			for(Date da : en.getValue().getDateList()) {
				if((tempI2 = TimeStr.calGapBetweenDate(curDate, da)) < tempI1)	tempI1 = tempI2;
			}
			nodeDateSpanMap.put(en.getKey(), tempI1);
			for(Integer in : en.getValue().getKeywordIdList()) {
				if(wordDateSpanMap.get(in) > tempI1) {
					wordDateSpanMap.put(in, tempI1);
				}
			}
		}
		
		///////////////////////////////// 打印测试
		System.out.println("wordDateSpanMap : ");
		for(Entry<Integer, Integer> en : wordDateSpanMap.entrySet()) {
			System.out.print(en.getKey() + " - ");
			System.out.print(en.getValue() + " - ");
			System.out.println();
		}
		System.out.println();
		
		IEntry iEntry = null;
		indexCooorSer.initGetNext(point);
		double curMinF = 0;
		int minPTreeDateSum = 0;
		double curMinDis = 0;
		// 阈值
		double threshold = Double.POSITIVE_INFINITY;
		// 词数
		int wordNum = wordIdList.size();
		for(Entry<Integer, Integer> en : wordDateSpanMap.entrySet())	minPTreeDateSum += en.getValue();
		int i, j;
		// 循环计算
		while(null != (iEntry = indexCooorSer.getNext())) {
			// 判断是否会超过阈值
			curMinDis = point.getMinimumDistance(iEntry.getShape()) + 1;
			curMinF = minPTreeDateSum * curMinDis;
			if(curMinF >= threshold)	break;
			
			// 记录当前已被添进bfs队列的点
			HashMap<Integer, Boolean> hasAccessNodeMap = new HashMap<>();
			
			// 实现 GETSEMANTICPLACE方法 starting
			// 初始化给待用数据
			PNode curPNode = new PNode(iEntry.getIdentifier(), false);
			hasAccessNodeMap.put(iEntry.getIdentifier(), Boolean.TRUE);
			PTree pTree = new PTree(curPNode);
			pTree.addNode(null, new PNode(Integer.MIN_VALUE, Boolean.FALSE));// Integer.MIN_VALUE表示bfs树的新的一层
			ArrayList<Integer> copyWordIdList = new ArrayList<>(sortedWordList);
			
			int curPTreeLevel = 1; // 将根节点所在层看作第一层
			KeywordIdDateList curKIDL = null;
			
			ArrayList<Integer> tempList = null;
			
			int curCandWordNum = 0;
			
			// 记录当前找到的最短路径的点
			HashMap<Integer, MinDisMulDateSpanRec>	curMinWordMap = new HashMap<>();
			for(Integer in : wordIdList) curMinWordMap.put(in, new MinDisMulDateSpanRec());
			MinDisMulDateSpanRec tempMinMul = null;
			
			boolean noFindPTree = Boolean.FALSE;	// 标识是否找到PTree
			boolean noWordSearch = Boolean.FALSE; 	// 标识已找到包含word的最短点都已找到
			
			// bfs
			while(true) {
				// 如果将要到新的一层
				if(Integer.MIN_VALUE == curPNode.getId()) {
					curPTreeLevel++;
					// 判断当前层下，计算出的f是否会超过阈值
					if(curCandWordNum != wordNum) {
						curMinF = 0;
						for(Entry<Integer, MinDisMulDateSpanRec> en : curMinWordMap.entrySet()) {
							tempMinMul = en.getValue();
							if(Integer.MAX_VALUE == tempMinMul.lValue)	curMinF += curPTreeLevel * wordDateSpanMap.get(en.getKey());
							else{
								curMinF += tempMinMul.lValue;
								// 判断当前节点是否已是最小节点
								if(Boolean.FALSE == tempMinMul.node.isLeaf() && tempMinMul.lValue <= curPTreeLevel * wordDateSpanMap.get(en.getKey())) {
									tempMinMul.node.setLeaf(Boolean.TRUE);
									copyWordIdList.remove((Object)en.getKey());
								}
							}
						}
						if(curMinF * curMinDis >= threshold) {
							noFindPTree = true;
							break;
						}
					} else {
						// 判断当前节点是否已是最小节点
						for(Entry<Integer, MinDisMulDateSpanRec> en : curMinWordMap.entrySet()) {
							tempMinMul = en.getValue();
							// 判断当前节点key是否已是最小节点
							if(Boolean.FALSE == tempMinMul.node.isLeaf() && tempMinMul.lValue <= curPTreeLevel * wordDateSpanMap.get(en.getKey())) {
								tempMinMul.node.setLeaf(Boolean.TRUE);
								copyWordIdList.remove((Object)en.getKey());
								if(0 == copyWordIdList.size()) {
									noWordSearch = Boolean.TRUE;
									break;
								}
							}
						}
						if(noWordSearch)	break;
					}
					
					// 取下一节点
					curPNode = curPNode.getNext();
					if(null == curPNode) {
						// 已遍历完
						noWordSearch = Boolean.TRUE;
						break;
					} else {
						pTree.addNode(null, new PNode(Integer.MIN_VALUE, Boolean.FALSE));
					}
				}
				
				// 处理当前点
				if(null != (curKIDL = searchedNodeListMap.get(curPNode.getId()))){
					tempList = curKIDL.getKeywordIdList();
					// 计算相交的word情况
					tempI1 = tempList.size();
					tempI2 = copyWordIdList.size();
					// 注：tempList和copyWordIdList均是有序的
					j = 0;
					for(i=0; i<tempI1; i++) {
						for(; j<tempI2; j++) {
							tempI4 = copyWordIdList.get(j);
							if(tempI4 == tempList.get(i)) {
								tempMinMul = curMinWordMap.get(tempI4);
								tempI3 = nodeDateSpanMap.get(curPNode.getId()) * curPTreeLevel;
								// 判断该节点是否可成为叶子节点
								if(tempI3 < tempMinMul.lValue) {
									// 增加了个候选者       
									if(Integer.MAX_VALUE == tempMinMul.lValue) {
										curCandWordNum++;
									}
									
									// 判断该节点是否是包含当前word且时差最小的点
									if(nodeDateSpanMap.get(curPNode.getId()) == wordDateSpanMap.get(tempI4)) {
										curPNode.setLeaf(Boolean.TRUE);
										// 移除词
										copyWordIdList.remove(j);
										j--;
										tempI2--;
										if(copyWordIdList.size() == 0) {
											noWordSearch = Boolean.TRUE;
										}
									}
									tempMinMul.lValue = tempI3;
									tempMinMul.node = curPNode;
									if(noWordSearch)	break;
								}
							} else if(tempI4 > tempList.get(i))	break;
						}
						if(j==tempI2 || noWordSearch)	break;
					}
				}
				if(noWordSearch)	break;
				
				// 当前点已访问，添加到访问map中
//				hasAccessNodeMap.put(curPNode.getId(), Boolean.TRUE);
				
				// 添加bfs节点
				if(null != (tempList = yago2sArrMap[curPNode.getId()])) {
					for(int in : tempList) {
						if(!hasAccessNodeMap.containsKey(in)) {
							pTree.addNode(curPNode, new PNode(in, Boolean.FALSE));
							// 记录已被放进bfs队列的点
							hasAccessNodeMap.put(in, Boolean.TRUE);
						}
					}
				}
				
				// 设置当前点
				curPNode = curPNode.getNext();
			}
			//  GETSEMANTICPLACE方法 ending
			
			// 该节点不能成为树
			if(noFindPTree)	continue;
			
			// 如果生成一颗树了
			if(curCandWordNum == wordNum){
				if(0 != copyWordIdList.size()) {
					for(Entry<Integer, MinDisMulDateSpanRec> en : curMinWordMap.entrySet()) {
						en.getValue().node.setLeaf(Boolean.TRUE);
					}
				}
				pTree.deleteUnnecessaryNode();
				curMinF = 0;
				for(Entry<Integer, MinDisMulDateSpanRec>en : curMinWordMap.entrySet()) {
					curMinF += en.getValue().lValue;
				}
				curMinF *= curMinDis;
				
				if(minHeap.size() != k) {
					minHeap.addPTree(curMinF, pTree);
					if(minHeap.size() == k)	threshold = minHeap.getLast().getNodeInfo().getDistance();
				} else {
					minHeap.updatePTree(curMinF, pTree);
					threshold = minHeap.getLast().getNodeInfo().getDistance();
				}
				
				// 测试打印
				System.out.println(curMinF);
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
		wordIdList.add(17);
		wordIdList.add(18);
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
