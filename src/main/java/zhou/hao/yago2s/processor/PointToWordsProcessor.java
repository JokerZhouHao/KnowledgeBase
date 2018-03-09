package zhou.hao.yago2s.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.infomatiq.jsi.Point;

import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.RandomNumGenerator;
import zhou.hao.tools.TimeStr;
import zhou.hao.yago2s.service.BuildMapService;
import zhou.hao.yago2s.service.BuildNidKeywordListMapService;
import zhou.hao.yago2s.service.IndexCoordService;
import zhou.hao.yago2s.service.IndexCoordService.PointAndId;
import zhou.hao.yago2s.service.IndexNidKeywordsListService;
import zhou.hao.yago2s.service.Yago2sInfoService;

/**
 * 
 * @author Monica
 * @since 2018/3/8
 * 功能 ：输入 一个point坐标，keywordIdList，time，三个参数，从输入点开始BFS，返回最先包含完整keywordIdList的点
 * 例如：输入的list中有keywordId 2, 3 ,4 ,point对应的节点id为4，然后进行BFS，依次碰到点8, 9 , 10 , 23
 * 		其中8中包含2,3,10中包含4，此情况下，不会去BFS23，而是在10处停，返回8,10
 */
public class PointToWordsProcessor {
	
	private IndexCoordService indexCooorSer = null;
	private ArrayList<Integer>[] yago2sArrMap = null;
	private IndexNidKeywordsListService indexNidKeywordsListServicenew = null;
	private Boolean hasInit = Boolean.FALSE;
	
	public IndexCoordService getIndexCooorSer() {
		return indexCooorSer;
	}

	// 初始化成员变量
	public void init() {
		indexCooorSer = new IndexCoordService(LocalFileInfo.getDataSetPath() + "yagoVB.zip", "pidCoordYagoVB.txt");
		indexCooorSer.buildRTree();
		yago2sArrMap = new BuildMapService(LocalFileInfo.getDataSetPath() + "yagoVB.zip", "edgeYagoVB.txt").buildMap();
		indexNidKeywordsListServicenew = new IndexNidKeywordsListService(LocalFileInfo.getYagoZipIndexBasePath() + "NidKeywordsListMapDBpediaVBTxt");
//		indexNidKeywordsListServicenew = new IndexNidKeywordsListService(LocalFileInfo.getDataSetPath() + "testIndex");
		hasInit = Boolean.TRUE;
	}
	
	/**
	 * @param point
	 * @param wordIdList
	 * @param time
	 * @return
	 * 功能 ：输入 一个point坐标，keywordIdList，time，三个参数，从输入点开始BFS，返回最先包含完整keywordIdList的点
	 * 例如：输入的list中有keywordId 2, 3 ,4 ,point对应的节点id为4，然后进行BFS，依次碰到点8, 9 , 10 , 23
	 * 		其中8中包含2,3,10中包含4，此情况下，不会去BFS23，而是在10处停，返回8,10
	 */
	public ArrayList<Integer> getPointToWordsIdList(Point point, ArrayList<Integer> wordIdList, Long time){
		if(!hasInit) {
			this.init();
		}
		Long startTime = System.currentTimeMillis();
		System.out.println("> 查找点" + point + ", word" + wordIdList + ", time:" + time + ". . .  " + TimeStr.getTime());
		int pointSNodeId = indexCooorSer.nearestN(point, 1).get(0).getId();
		
		indexNidKeywordsListServicenew.openIndexReader();
		HashMap<Integer, ArrayList<Integer>> resultNodeListMap = indexNidKeywordsListServicenew.searchKeywordIdListReNodeIdMap(wordIdList);
		indexNidKeywordsListServicenew.closeIndexReader();
		boolean signNodeArr[] = new boolean[Yago2sInfoService.nodeNum];
		HashMap<Integer, Boolean> signNoFindWordMap = new HashMap<>();
		for(int in : wordIdList)	signNoFindWordMap.put(in, Boolean.TRUE);
		ArrayList<Integer> tempEdge = null;
		LinkedList<Integer> tempLink = new LinkedList<>();
		ArrayList<Integer> tempResList = null;
		ArrayList<Integer> resultList = new ArrayList<>();
		
		// 开始计算
		tempLink.add(pointSNodeId);
		signNodeArr[pointSNodeId] = true;
		Integer curNodeId = null;
		while(null != (curNodeId = tempLink.poll())) {
			// 检查节点是否包含查找的word
			if(null != (tempResList = resultNodeListMap.get(curNodeId))) {
				boolean isFirst = true;
				for(int in : tempResList) {
					if(signNoFindWordMap.containsKey(in)) {
						if(isFirst) {
							resultList.add(curNodeId);
							isFirst = false;
						}
						signNoFindWordMap.remove(in);
						if(signNoFindWordMap.isEmpty()) {
							System.out.println("> b结束查找点" + point + ", word" + wordIdList + ", time:" + time + ", 花时" + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()) + ". " + TimeStr.getTime());
							tempLink.clear();
							return resultList;
						}
					}
				}
			}
			
			// 添加BFS的点
			if(null != (tempEdge = yago2sArrMap[curNodeId])){
//				System.out.print("> curId[" + curNodeId + "] ");
				for(int in : tempEdge) {
//					System.out.print(in + " ");
					if(!signNodeArr[in]) {
						tempLink.add(in);
						signNodeArr[in] = true;
					}
				}
			}
//			System.out.print("queue : ");
//			for(int in : tempLink)	System.out.print(in + " ");
//			System.out.println();
		}
		System.out.println("> 只找到部分word, 结束查找点" + point + ", word" + wordIdList + ", time:" + time + ", 花时" + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()) + ". " + TimeStr.getTime());
		System.out.println();
		return resultList.isEmpty()?null:resultList;
	}
	
	// 主函数
	public static void main(String[] args) {
		PointToWordsProcessor pro = new PointToWordsProcessor();
		RandomNumGenerator randomGe = new RandomNumGenerator(Yago2sInfoService.keywordStartId, Yago2sInfoService.keywordStartId + Yago2sInfoService.keywordNum -1);
		RandomNumGenerator pointIdGe = new RandomNumGenerator(0, Yago2sInfoService.coordNum-1);
		ArrayList<Integer>	wordIdList = new ArrayList<>();
		ArrayList<Integer>	resList = null;
		pro.init();
		ArrayList<Integer>[] nidKeywordArr = new BuildNidKeywordListMapService().buildNidKeywordListMapArr();
		IndexCoordService cooSe = pro.getIndexCooorSer();
		ArrayList<PointAndId> pIdList = null;
		ArrayList<Integer>	tempList = null;
		while(true) {
			wordIdList.clear();
			pIdList = cooSe.nearestN(cooSe.getPoint(pointIdGe.getRandomNum()), 4);
			boolean sign = false;
			for(int i=0; i<3; i++) {
				if(null != (tempList = nidKeywordArr[pIdList.get(i+1).getId()])) {
					wordIdList.add(tempList.get(0));
				} else {
					sign = true;
					break;
				}
			}
			if(sign)	continue;
			resList = pro.getPointToWordsIdList(pIdList.get(0).getPoint(), wordIdList, System.currentTimeMillis());
			if(null != resList) {
				System.out.print("> 结果 : ");
				for(int in : resList) {
					System.out.print(in + " ");
				}
				break;
			} else continue;
		}
//		Point point = new Point(1, 2);
//		wordIdList.add(16);
//		wordIdList.add(19);
//		wordIdList.add(12);
//		resList = pro.getPointToWordsIdList(point, wordIdList, System.currentTimeMillis());
//		System.out.print("> 结果 : ");
//		for(int in : resList)	System.out.print(in + " ");
//		System.out.println();
	}
	
}
