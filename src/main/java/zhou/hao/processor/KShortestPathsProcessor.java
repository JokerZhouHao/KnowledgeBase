package zhou.hao.processor;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;

import zhou.hao.entry.AList;
import zhou.hao.entry.AList.Path;
import zhou.hao.entry.AList.PolledPath;
import zhou.hao.entry.FreeBaseMap;
import zhou.hao.entry.MMap.CanPathList;
import zhou.hao.entry.MMap.PathList;
import zhou.hao.entry.MMap;
import zhou.hao.entry.PathDistanceList;
import zhou.hao.service.IndexNodeMapService;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.MemoryInfo;
import zhou.hao.tools.TimeStr;

/**
 * 
 * @author Monica
 * @since 2017/12/07
 * 功能：求k shortest paths
 */
public class KShortestPathsProcessor {
	private ArrayList<ArrayList<Integer>> wordsToNodesList = new ArrayList<ArrayList<Integer>>();
	private int wordsNum = 0;
	private int k = 0;
	private AList aList = null;
	private MMap mmap = null;
	private ArrayList<String> searchedWords = null;
//	private IndexNodeMapService searcher = new IndexNodeMapService(LocalFileInfo.getTestIndexPath());
	private IndexNodeMapService searcher = null;
	private FreeBaseMap freeBaseMap = null;
	private int nodeNum = 0;
	private int degreeNum = 0;
	
	// 记录所有分的的词
	private int termNum = 114837967;
	private String[] termArr = null;
	private int gFreeBaseNodeNum = LocalFileInfo.gFreeBaseNodeNum;
//	private FreeBaseMap freeBaseMap = new FreeBaseMap(LocalFileInfo.getBasePath() + "orginal_code\\data\\testedges.zip");
	
	public KShortestPathsProcessor(String indexPath, int degreeNum, int nodeNum) {
		this.degreeNum = degreeNum;
		this.nodeNum = nodeNum;
		searcher = new IndexNodeMapService(indexPath);
	}
	
	// searcher测试
	public void testSearcher() {
		searcher.openIndexWriter();
		searcher.addDoc(0, "v0 v2", "3 ");
		searcher.addDoc(1, "v1", "3 ");
		searcher.addDoc(2, "v2", "3 ");
		searcher.addDoc(3, "v3", "0 1 2 4");
		searcher.addDoc(4, "v4", "3 5 ");
		searcher.addDoc(5, "v5", "4 6 ");
		searcher.addDoc(6, "v6", "5 ");
		searcher.closeIndexWriter();
	}
	
	// 计算k shortest path
	public LinkedList<CanPathList> findKShortestPaths(int k, ArrayList<String> searchedWords){
		 this.k = k;
		 this.wordsNum = searchedWords.size();
		 this.searchedWords = searchedWords;
		 AList aList = new AList(wordsNum);
		 MMap mmap = new MMap(k, wordsNum, aList);
		 int i=0;
		 Path path = null;
		 int pIndex = 0;
		 int longestDis = 0;
		 
		 System.out.println("  > 开始查找word . . . " + TimeStr.getTime());
		 searcher.openIndexReader();
		 // 计算wordsToNodesList
		 for(i=0; i<wordsNum; i++) {
			// 检查图中是否存在所查找的关键字
			ArrayList<Integer> nodeIdList = searcher.searchWordReNodeIds(searchedWords.get(i), nodeNum);
			if(nodeIdList.isEmpty()) {
				System.out.println("  > 查找的【" + searchedWords.get(i) + "】不在当前node范围内！！！\n");
				return null;
			}
			wordsToNodesList.add(nodeIdList);
		 }
		 searcher.closeIndexReader();
		 System.out.println("  > 所查词均在map中！" + TimeStr.getTime() + "\n");
		 
		 searchedWords.clear();
		 
		 Long startTime = System.currentTimeMillis();
		 /******************************
		  * 
		  *  初始化aList
		  *
		  ******************************/
		System.out.println("  > 初始化aList . . . " + TimeStr.getTime());
		int size = wordsToNodesList.size();
//		ArrayList<HashMap<Integer, Boolean>> recordMapList = new ArrayList<HashMap<Integer, Boolean>>();
//		HashMap<Integer, Boolean> recordMap = null;
		ArrayList<boolean[]> recordMapList = new ArrayList<boolean[]>();
		boolean[] recordMap = null;
		for(i=0; i< size; i++) {
//			recordMap = new HashMap<Integer, Boolean>();
			recordMap = new boolean[gFreeBaseNodeNum];
			for(Integer in : wordsToNodesList.get(i)) {
				aList.addPath(i, new Path(in, i));
//				recordMap.put(in, Boolean.TRUE);
				recordMap[in] = true;
			}
			recordMapList.add(recordMap);
			wordsToNodesList.get(i).clear();
		}
		wordsToNodesList.clear();
		System.out.println("  > 完成初始化aList ！！！ " + TimeStr.getTime() + "\n");
		
		
		/***********************************************
		 * 
		 *  初始化mmap，如果有k条总距离为0的路径，就返回
		 *  
		 ***********************************************/
		System.out.println("  > 初始化mmap . . . " + TimeStr.getTime());
		PolledPath polledPath = null;
		while((polledPath = aList.poll())!=null && polledPath.getDis()==0) {
			path = polledPath.getPath();
			pIndex = polledPath.getPathListIndex();
			recordMap = recordMapList.get(pIndex);
			
			ArrayList<Integer> sourceNodeIdList = freeBaseMap.getReverseEdges(path.getCurrentNodeId());
			if(null != sourceNodeIdList) {	// 不为空
				for(Integer in : sourceNodeIdList) {
//					if(recordMap.get(in)!=null)	continue;
					if(recordMap[in])	continue;
					aList.addPath(polledPath.getPathListIndex(), path.add(in, pIndex));
//					recordMap.put(in, Boolean.TRUE);
					recordMap[in] = true;
				}
			}
			
			if(k == mmap.initAddPath(polledPath.getPathListIndex(), path)) {
				System.out.println("  > 初始化mmap成功！！！" + TimeStr.getTime() + "\n");
				System.out.println("  > 已获得" + k + "条长度为0的最短路径！！！");
				System.out.println("  > 用时(不包含查词时间) : " + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()) + "\n");
				return mmap.getCanShortestPathList();
			}
		}
		System.out.println("  > 初始化mmap成功！！！" + TimeStr.getTime() + "\n");
		if(null==polledPath) {
			System.out.println("  > 队列已为空，获得" + mmap.getCanShortestPathList().size() + "条长度为0的最短路径！！！");
			System.out.println("  > 用时(不包含查词时间) : " + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()) + "\n");
			return mmap.getCanShortestPathList();
		}
		
		aList.addFirst(polledPath.getPathListIndex(), polledPath.getPath());
		
		
		
		aList.initQueueHeadsDis(mmap);
		/*************************************
		 * 
		 *  计算k条 最短 【候选】  路径
		 *  
		 *************************************/
		int frontDis = -1;
		System.out.println("  > 开始计算" + k + "条候选路径.....  " + TimeStr.getTime());
		boolean isNull = true;
		while((polledPath = aList.poll())!=null) {
			path = polledPath.getPath();
			pIndex = polledPath.getPathListIndex();
//			targetNode = path.getTargetNodeId();
			recordMap = recordMapList.get(pIndex);
			
			ArrayList<Integer> sourceNodeIdList = freeBaseMap.getReverseEdges(path.getCurrentNodeId());
			if(null != sourceNodeIdList) {	// 不为空
				
				 for(Integer in : sourceNodeIdList) {
//					if(recordMap.get(in)!=null) {
					if(recordMap[in]) {
//						System.out.println("          > 命中");
						continue;
					}
					
					aList.addPath(polledPath.getPathListIndex(), path.add(in, pIndex));
//					recordMap.put(in, Boolean.TRUE);
					recordMap[in] = true;
				}
			}
			
			aList.verifyHeadsHasChange(polledPath.getPathListIndex());
			if(aList.getHeadsHasChange()) {
				aList.setHeadsHasChange(false);
				longestDis = 0;
				for(Integer in : aList.getAllHeadDistance())	longestDis += in;
				if(longestDis!=frontDis) {
					frontDis = longestDis;
					System.out.println("    > 当前aList首元素长度和为  : " + longestDis + " " + MemoryInfo.getTotalFreeUsedAvailable() + " " + TimeStr.getTime());
					System.out.print("        > ");
					for(Integer in : aList.getAllQueueSize()) {
						System.out.print(in + " ");
					}
					System.out.println();
				}
			}
			
			
			if(k==mmap.findKCanShortestPaths(polledPath.getPathListIndex(), path)) {
				isNull = false;
				break;
			}
		}
		System.out.println("  > 已获得" + mmap.getCanShortestPathList().size() + "条候选路径！！！  " + TimeStr.getTime());
		if(mmap.getCanShortestPathList().size()!=0) {
			i = 0;
			for(CanPathList li : mmap.getCanShortestPathList()) {
				System.out.print("    " + (++i) + ". ");
				li.display();
				System.out.println();
			}
		}
		System.out.println();
		if(isNull) {
			System.out.println("  > 队列为已为空，候选路径都是最短路径，获得" + mmap.getCanShortestPathList().size() + "条最短路径！！！" + TimeStr.getTime());
			System.out.println("  > 用时(不包含查词时间) : " + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()) + "\n");
			return mmap.getCanShortestPathList();
		}
		
		mmap.initKeyList();
		aList.initQueueHeadsDis(mmap);
		
		/**************************************
		 * 
		 *  计算k条最短路径
		 *  
		 **************************************/
		System.out.println("  > 开始从候选路径中计算" + k + "条最短路径.....  " + TimeStr.getTime());
		while((polledPath = aList.poll())!=null) {
			path = polledPath.getPath();
			pIndex = polledPath.getPathListIndex();
//			targetNode = path.getTargetNodeId();
			recordMap = recordMapList.get(pIndex);
			
			ArrayList<Integer> sourceNodeIdList = freeBaseMap.getReverseEdges(path.getCurrentNodeId());
			if(null != sourceNodeIdList) {	// 不为空
				
				for(Integer in : sourceNodeIdList) {
//					if(recordMap.get(in)!=null) {
					if(recordMap[in]) {
//						System.out.println("          > 命中");
						continue;
					}
					
					aList.addPath(polledPath.getPathListIndex(), path.add(in, pIndex));
//					recordMap.put(in, Boolean.TRUE);
					recordMap[in] = true;
				}
			}
			
			int pathIndex = aList.verifyHeadsHasChange(polledPath.getPathListIndex());
			
			if(aList.getHeadsHasChange()) {
				longestDis = 0;
				for(Integer in : aList.getAllHeadDistance())	longestDis += in;
				if(longestDis!=frontDis) {
					frontDis = longestDis;
					System.out.println("    > 当前aList首元素长度和为  : " + longestDis + " " + MemoryInfo.getTotalFreeUsedAvailable() + " " + TimeStr.getTime());
					System.out.print("        > ");
					for(Integer in : aList.getAllQueueSize()) {
						System.out.print(in + " ");
					}
					System.out.println();
				}
			}
			
			if(pathIndex!=-1) {	// 有行为空
				mmap.setHasDeal(pathIndex);
			}
			
			if(mmap.addPath(polledPath.getPathListIndex(), path)) {
				System.out.println("  > 已获得" + k + "条最短路径！！！" + TimeStr.getTime());
				System.out.println("  > 用时(不包含查词时间) : " + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()) + "\n");
				return mmap.getCanShortestPathList();
			}
		}
		
		System.out.println("  > 已获得" + mmap.getCanShortestPathList().size() + "条最短路径！！！" + TimeStr.getTime());
		System.out.println("  > 用时(不包含查词时间) : " + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()) + "\n");
		return mmap.getCanShortestPathList();
	}
	
	public void initTermArr() {
		System.out.println("  > 开始初始化termArr . . . " + TimeStr.getTime());
		searcher.openIndexReader();
		termArr = this.searcher.getTermArr(termArr);
		searcher.closeIndexReader();
		System.out.println("  > 完成初始化termArr ！！！" + TimeStr.getTime());
	}
	
	// 获得分词流
	public String getTerm(String str) {
		Analyzer analyzer = new StandardAnalyzer();
		 try {
	            //将一个字符串创建成Token流
	            TokenStream stream  = analyzer.tokenStream("", str);
	            //保存相应词汇
	            CharTermAttribute cta = stream.addAttribute(CharTermAttribute.class);
	            stream.reset();
	            while(stream.incrementToken()){
	                return cta.toString();
	            }
	            return null;
       } catch (IOException e) {
           e.printStackTrace();
       }
	   return null;
	}
	
	public ArrayList<String> getSearchedWords(ArrayList<String> wordList, int nodeId, int totalTime, int times){
		if(times > totalTime)	return wordList;
		ArrayList<Integer> nodeList = null;
		if(times%2==1) nodeList = freeBaseMap.getPositiveEdges(nodeId);
		else nodeList = freeBaseMap.getReverseEdges(nodeId);
		if(null==nodeList)	return null;
		for(int in : nodeList) {
			String s = this.getTerm(searcher.searchNodeIdReAtt(in));
			if(null != s) {
				if(s.equals("wikipedia"))	continue;
				boolean sign = false;
				for(String st : wordList) {
					if(st.equals(s)) {
						sign = true;
						break;
					}
				}
				if(sign)	continue;
				wordList.add(s);
				int curIndex = wordList.size()-1;
				if(null!=this.getSearchedWords(wordList, in, totalTime, times+1))
					return wordList;
				wordList.remove(curIndex);
			}
		}
		return null;
	}
	
	// 从各点的的words中随机选n个word作为查询词
	public ArrayList<String> getSearchedWords(int n){
		ArrayList<String> wordList = null;
		searcher.openIndexReader();
		while(wordList==null) {
			wordList = new ArrayList<>();
			int randNum = (int)(Math.random()*(nodeNum-1));
			wordList = this.getSearchedWords(wordList, randNum, n, 1);
		}
		searcher.closeIndexReader();
		return wordList;
	}
	
	// 初始化
	public void init() {
		freeBaseMap = new FreeBaseMap(degreeNum, nodeNum);
//		this.initTermArr();
	}
	
	public int getNodeNum() {
		return nodeNum;
	}

	public void setNodeNum(int nodeNum) {
		this.nodeNum = nodeNum;
	}
	
	public AList getaList() {
		return aList;
	}

	public void setaList(AList aList) {
		this.aList = aList;
	}

	public MMap getMmap() {
		return mmap;
	}

	public void setMmap(MMap mmap) {
		this.mmap = mmap;
	}

	public static void main(String[] args) {
		KShortestPathsProcessor processor = new KShortestPathsProcessor(LocalFileInfo.getIndexPath(1000000), 0, 1000000);
		processor.init();
		ArrayList<String> wordList = new ArrayList<>();
		Scanner scanner = new Scanner(System.in);
		String str = "";
		while(!str.equals("0")) {
			System.out.print("> 输入参数：");
			str = scanner.nextLine();
			wordList = processor.getSearchedWords(Integer.parseInt(str));
			for(String s : wordList)
				System.out.print(s + "   ");
			System.out.println("\n");
		}
		
		
		
//		Scanner scanner = new Scanner(System.in);
//		String paramsStr = "";
//		System.out.print("> 请输入预设的nodeNum, 0表示为所有节点, 例如 ：20000 : ");
//		int nodeNum = scanner.nextInt();
//		
//		System.out.println("> 开始初始化工作 . . . " + TimeStr.getTime() + " " + MemoryInfo.getTotalFreeUsedAvailable());
//		if(0==nodeNum)	nodeNum = LocalFileInfo.gFreeBaseNodeNum;
//		KShortestPathsProcessor processor = new KShortestPathsProcessor("", 0, 0);
//		processor.init();
//		System.out.println("> 完成初始化工作！！！" + TimeStr.getTime() + " " + MemoryInfo.getTotalFreeUsedAvailable() + "\n");
//		
//		paramsStr = scanner.nextLine();
//		while(true) {
//			System.out.print("\n> 请输入运行参数(k=0退出），例如 ：k=10, searchWordNum=12 : ");
//			HashMap<String, String> paramsMap = new HashMap<String, String>();
//			paramsStr = scanner.nextLine();
//			args = paramsStr.split(",");
//			int argsLen = args.length;
//			for(int i=0; i<argsLen; i++) {
////				System.out.println(args[i]);
//				String[] strArr = args[i].split("=");
//				paramsMap.put(strArr[0].trim(), strArr[1].trim());
//			}
//			if(paramsMap.get("k")==null) {
//				System.out.println("> 请输入参数k !!! ");
//				continue;
//			}
//			if(Integer.parseInt(paramsMap.get("k"))<1) {
//				System.out.println("> 退出成功！！！");
//				System.exit(0);
//			}
//			if(paramsMap.get("searchWordNum")==null) {
//				System.out.println("> 请输入参数searchWordNum !!! ");
//				continue;
//			}
//			
////			for(Entry<String, String> en : paramsMap.entrySet()) {
////				System.out.println("key = " + en.getKey() + " value = " + en.getValue());
////			}
////			continue;
//			
//			Long startTime = System.currentTimeMillis();
//			
//			// 随机获得searchWordNum个term
//			System.out.println("> 正在获得searchedWords . . . " + TimeStr.getTime());
//			ArrayList<String> searchedWords = processor.getSearchedWords(Integer.parseInt(paramsMap.get("searchWordNum")));
//			
//			///////////////////////////////////////
//			int nu = Integer.parseInt(paramsMap.get("searchWordNum"));
//			if(1==nu) {
////				【n50016173】    【monorails_and_satellites】    【coq_zxj_ze0c】    【003ab8ada708】    【9780671600334】
//				searchedWords.clear();
//				searchedWords.add("n50016173");
//				searchedWords.add("monorails_and_satellites");
//				searchedWords.add("coq_zxj_ze0c");
//				searchedWords.add("003ab8ada708");
//				searchedWords.add("9780671600334");
//			} else if(2==nu) {
////				【ed4823a9】    【003arecording_recording_gid_2d886f03】    【003arecording_recording_gid_045eb4f8】    【27697389】    【humarathon_half_marathon】
//				searchedWords.clear();
//				searchedWords.add("ed4823a9");
//				searchedWords.add("003arecording_recording_gid_2d886f03");
//				searchedWords.add("003arecording_recording_gid_045eb4f8");
//				searchedWords.add("27697389");
//				searchedWords.add("humarathon_half_marathon");
//			}
////			searchedWords.clear();
////			searchedWords.add("nnekada");
////			searchedWords.add("kitssee");
////			searchedWords.add("sky");
//			///////////////////////////////////////
//			
//			System.out.println("> 所获得searchedWords, " + TimeStr.getTime() + " : ");
//			for(String s : searchedWords) {
//				System.out.print("   【" + s + "】 ");
//			}
//			System.out.println();
//			
//			// 计算K条最短路径的点
//			System.out.println("> 开始计算shortest-K个点 . . . " + TimeStr.getTime());
//			LinkedList<CanPathList> list = processor.findKShortestPaths(Integer.parseInt(paramsMap.get("k")), searchedWords);
//			if(null==list) {
//				System.out.println(" > 有searchedWord不在当前的node中！！！");
//				continue;
//			}
//			System.out.println("> 共获得" + list.size() + "个shortest点," + TimeStr.getTime() + " : ");
//			if(list.size()!=0) {
//				int i = 0;
//				for(CanPathList li : list) {
////					System.out.println("> totalDis = " + li.getTotalDis());
//					System.out.print((++i) + ". ");
//					li.display();
//					System.out.println();
////					System.out.print("sourceNodeId : " + li.getSourceNodeId() + " dis = " + li.getTempSum());
////					System.out.println();
//				}
//			}
//			// 释放内存
//			System.out.println("> 重置aList和mMap前内存占用情况 :  " + MemoryInfo.getTotalFreeUsedAvailable());
////			processor.getaList().reset();
////			processor.getMmap().reset();
//			System.out.println("> 重置aList和mMap后内存占用情况 :  " + MemoryInfo.getTotalFreeUsedAvailable());
//			System.out.println("> 结束时间：" + TimeStr.getTime() + " 花时：" + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()));
//		}
	}
}
