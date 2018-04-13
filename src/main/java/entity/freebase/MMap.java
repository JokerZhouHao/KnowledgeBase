package entity.freebase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import entity.freebase.AList.Path;

import java.util.Set;

/**
 * 
 * @author Monica
 * @since 2017/12/6
 * 功能：记录RDF K Shortest Pah中的M变量
 */
public class MMap {
	
	// 路径list
	public class PathList{
		ArrayList<Path> pathList = null;
		int pathNum = 0;
		Boolean isDeal = Boolean.FALSE;
		
		public PathList() {}
		
		public PathList(int initPathListSize) {
			pathList = new ArrayList<>();
			for(int i=0; i<initPathListSize; i++)	pathList.add(null);
		}
		
		public boolean addPath(int targetPathIndex, Path path) {
			if(null == pathList.get(targetPathIndex)) {
				pathList.set(targetPathIndex, path);
				pathNum++;
				return Boolean.TRUE;
			} else return Boolean.FALSE;
		}
		
		public void set(int targetPathIndex, Path path) {
			pathList.set(targetPathIndex, path);
			pathNum++;
		}
		
		public Boolean isFull() {
			return pathNum==pathListSize;
		}
		
		public Path get(int index) {
			return pathList.get(index);
		}
		
		public CanPathList convertToCanPathList() {
			return new CanPathList(this);
		}
		
		public void display() {
			for(Path pa : pathList) {
				if(null==pa)	System.out.print(" <null> ");
				else {
					pa.display();
					System.out.print(" ");
				}
			}
		}
		
	}
	
	// 候选最短list
	public class CanPathList extends PathList{
		int totalDis = 0;
		
		public CanPathList(PathList pl) {
			this.isDeal = pl.isDeal;
			this.pathList = pl.pathList;
			this.pathNum = pl.pathNum;
			for(Path p : pl.pathList)
				totalDis += p.distance;
		}
		
		public void display() {
			System.out.print("> " + pathList.get(0).currentNodeId + " totalDis = " + totalDis + " : ");
			for(Path pa : pathList) {
				if(null==pa)	System.out.print(" <null> ");
				else {
					pa.display();
					System.out.print(" ");
				}
			}
		}

		public int getTotalDis() {
			return totalDis;
		}
		
		
	}
	
	public class KeyList{
		ArrayList<Integer> keyList = null;
		int currentIndex = 0;
		int size = 0;
		PathList currentPathList = null;
		
		public KeyList(Set<Integer> keySet) {
			keyList = new ArrayList<>(keySet);
			size = keyList.size();
			getNext();
		}
		
		public PathList get() {
			return currentPathList;
		}
		
		public PathList getNext() {
			PathList pList = null;
			currentIndex++;
			while(currentIndex < size) {
				if(!(pList = mMap.get(keyList.get(currentIndex))).isDeal)	break;
				pList.isDeal = Boolean.TRUE;
				currentIndex++;
			}
			if(currentIndex<size) {
				currentPathList = mMap.get(keyList.get(currentIndex));
				return currentPathList;
			} else {
				currentPathList = null;
				return null;	// 已经遍历完
			}
		}
		
		public Boolean isOver() {
			if(currentIndex<size)	return Boolean.FALSE;
			else return Boolean.TRUE;
		}
		
		// 将pathIndex对应的元素为null的PathList的isDeal置为true
		public void setHasDeal(int pathIndex) {
			for(int i = currentIndex; i<size; i++) {
				PathList pl = mMap.get(keyList.get(i));
				if(null == pl.get(pathIndex))	pl.isDeal = true;
			}
		}
	}
	
	private HashMap<Integer, PathList> mMap = new HashMap<Integer, PathList>();
	private Integer pathListSize = 0;
	private KeyList keyList = null;
	private Integer k = 0;
	
	public void reset() {
		mMap.clear();
		pathListSize = 0;
		keyList = null;
		k = 0;
		System.gc();
	}
	
	public HashMap<Integer, PathList> getmMap() {
		return mMap;
	}

	// 放当前的最短路径
	private LinkedList<CanPathList> canShortestPathList = new LinkedList<>();
	
	// AList
	private AList aList = null;
	
	public MMap(Integer k, Integer pathListSize, AList al) {	
		this.k = k;
		this.pathListSize = pathListSize;
		aList = al;
	}
	
	public int size() {
		return mMap.size();
	}
	
	// 将pathIndex对应的元素为null的PathList的isDeal置为true
	public void setHasDeal(int pathIndex) {
		keyList.setHasDeal(pathIndex);
	} 
	
	// 初始时，添加路径
	public int initAddPath(int targetNodeIndex, Path path) {
		PathList pathList = mMap.get(path.currentNodeId);
		if(null == pathList) {
			pathList = new PathList(pathListSize);
			mMap.put(path.currentNodeId, pathList);
		} else {
			if(pathList.isDeal)	return canShortestPathList.size();
		}
		if(pathList.addPath(targetNodeIndex, path) && pathList.isFull()) {
			pathList.isDeal = Boolean.TRUE;
			CanPathList canList= pathList.convertToCanPathList();
			canShortestPathList.add(canList);
			mMap.put(path.currentNodeId, canList);
		}
		return canShortestPathList.size();
	}
	
	
	// 找到候选的前k条候选最短路径
	public int findKCanShortestPaths(int targetNodeIndex, Path path) {
		PathList pathList = mMap.get(path.currentNodeId);
		int i = 0;
		if(null==pathList) {
			pathList = new PathList(pathListSize);
			mMap.put(path.currentNodeId, pathList);
		} else {
			if(pathList.isDeal)	return canShortestPathList.size();
		}
		
		// 当前的行充满
		if(pathList.addPath(targetNodeIndex, path) && pathList.isFull()) {
			pathList.isDeal = Boolean.TRUE;
			Long totalDis = 0L;
			for(Path p : pathList.pathList)	totalDis += p.distance;
			if(canShortestPathList.isEmpty()) {
				canShortestPathList.add(pathList.convertToCanPathList());
			} else {
				int size = canShortestPathList.size();
				for(i=size-1; i>=0; i--) {
					if(canShortestPathList.get(i).totalDis <= totalDis) break;
				}
				
//				if(i==-1 || i==size-1)	canShortestPathList.add(pathList.convertToCanPathList());
//				else	canShortestPathList.add(i+1, pathList.convertToCanPathList());
				if(i==size-1)	canShortestPathList.add(pathList.convertToCanPathList());
				else	canShortestPathList.add(i+1, pathList.convertToCanPathList());
			}
		}
		
		return canShortestPathList.size();
	}  
	
	// 初始化置keySet
	public void initKeyList() {
		keyList = new KeyList(mMap.keySet());
	}
	
	// 判断keyList是否已遍历完
	public Boolean keyListIsOver() {
		if(null == keyList.getNext())	return Boolean.TRUE;
		else return Boolean.FALSE;
	}
	
	// 添加路径，如果找到所有最短路径，就返回true，否则返回false
	public Boolean addPath(int targetNodeIndex, Path path) {
		int tempTotalDis = 0, i = 0;
		// aList队列头的dis改变了
		if(aList.getHeadsHasChange()) {
			PathList pl = keyList.get();
			Integer[] queueHeadsDis = aList.getQueueHeadsDis();
			Path p0 = null;
			while(null!=pl) {
				if(!pl.isDeal) {
					tempTotalDis = 0;
					for(i=0; i<pathListSize; i++) {
						p0 = pl.get(i);
						if(null != p0)	tempTotalDis += p0.distance;
						else	tempTotalDis += queueHeadsDis[i];
					}
					if(canShortestPathList.getLast().totalDis > tempTotalDis) {
						aList.setHeadsHasChange(Boolean.FALSE);
						break;
					}
				}
				pl = keyList.getNext();
			}
			if(null==pl)	return Boolean.TRUE;
		}
		
		if(path.currentNodeId<keyList.currentIndex)	return false;
		
		PathList pathList = mMap.get(path.currentNodeId);
		int size = 0;
		
		if(null != pathList && !pathList.isDeal) {
			if(null == pathList.get(targetNodeIndex)) {
				pathList.set(targetNodeIndex, path);
				
				// 已充满
				if(pathList.isFull()) {
					pathList.isDeal = Boolean.TRUE;
					tempTotalDis = 0;
					for(Path p : pathList.pathList) 	tempTotalDis += p.distance;
					size = canShortestPathList.size();
					for(i=0; i<size; i++)
						if(canShortestPathList.get(i).totalDis > tempTotalDis)	break;
					if(i!=size) {
						canShortestPathList.add(i, pathList.convertToCanPathList());
						canShortestPathList.pollLast();
					}
//					if(i<size-1) {
//						if(canShortestPathList.get(i).totalDis >= tempTotalDis)
//						canShortestPathList.set(i, pathList.convertToCanPathList());
//						canShortestPathList.pollLast();
//					} else {
//						if(canShortestPathList.get(size-1).totalDis > tempTotalDis) {
//							canShortestPathList.pollLast();
//							canShortestPathList.add(pathList.convertToCanPathList());
//						}
//					}
					if(pathList == keyList.get()) {
						if(null==keyList.getNext())	return Boolean.TRUE;
//						else return Boolean.FALSE;
					}
				}
			}
			
//			// aList队列头的dis改变了
//			if(aList.getHeadsHasChange()) {
//				PathList pl = keyList.get();
//				Integer[] queueHeadsDis = aList.getQueueHeadsDis();
//				Path p0 = null;
//				while(null!=pl) {
//					if(!pl.isDeal) {
//						tempTotalDis = 0;
//						for(i=0; i<pathListSize; i++) {
//							p0 = pl.get(i);
//							if(null != p0)	tempTotalDis += p0.distance;
//							else	tempTotalDis += queueHeadsDis[i];
//						}
//						if(canShortestPathList.getLast().totalDis > tempTotalDis) {
//							aList.setHeadsHasChange(Boolean.FALSE);
//							break;
//						}
//					}
//					pl = keyList.getNext();
//				}
//				if(null==pl)	return Boolean.TRUE;
//			}
		}
		
		return Boolean.FALSE;
	}
	
	// 打印MMap
	public void display() {
		Set<Integer> nodeIdSet = mMap.keySet();
		System.out.print("MMap display --------------------------->\n");
		for(Integer in : nodeIdSet) {
			System.out.print(in + " isDeal = " + mMap.get(in).isDeal + " > ");
			mMap.get(in).display();
			System.out.println();
		}
		System.out.println();
	}

	public LinkedList<CanPathList> getCanShortestPathList() {
		return canShortestPathList;
	}
	
//	// 获得源点的nodelist
//	public MMapNodeList getNodeList(int sourceNode) {
//		return mMap.get(sourceNode);
//	}
//	
//	
//	// 计算最短路径，并返回当前已经获得的最短路径
//	public void calShortestPath(int caledPathSourNodeId){
//		MMapNodeList caledNodeList = mMap.get(caledPathSourNodeId);
//		MMapCandidateNodeList caledCanNodeList = null;
//		
//		// 判断是否为候选路径
//		if(!caledNodeList.isMMapNewNodeList()) {	// 普通path
//			caledCanNodeList = caledNodeList.convertToCandidate(caledPathSourNodeId);
//		} else {	// 新类型path
//			// 释放内存
//			MMapNewNodeList newNodeList = ((MMapNewNodeList)caledNodeList);
//			for(MMapCandidateNodeList can : newNodeList.candidateMap.keySet()) {
//				can.removeMayLessPath(newNodeList);
//			}
//			newNodeList.candidateMap.clear();
//			newNodeList.candidateMap = null;
//			System.gc();
//			caledCanNodeList = newNodeList.convertToCan(caledPathSourNodeId);
//		}
//		mMap.put(caledPathSourNodeId, caledCanNodeList);
//		
//		int size = 0;
//		int i = 0;
//		
//		// 判断该路径能否放在shortest中
//		Iterator<MMapCandidateNodeList> iter = shortestPathList.iterator();
//		i = 0;
//		size = shortestPathList.size();
//		while(iter.hasNext()) {
//			if(-1==caledCanNodeList.compareTo(iter.next()))	break;
//			i++;
//		}
//		if(i!=size) {
//			shortestPathList.add(i, caledCanNodeList);
//			return;
//		} else {
//			if(!shortestPathList.isEmpty() && 0==caledCanNodeList.compareTo(shortestPathList.getLast())) {
//				shortestPathList.add(i, caledCanNodeList);
//				return;
//			}
//		}
//		
//		// 将该路径放入shortestCandidates中
//		iter = shortestCandidates.iterator();
//		i = 0;
//		size = shortestCandidates.size();
//		while(iter.hasNext()) {
//			if(-1==caledCanNodeList.compareTo(iter.next()))	break;
//			i++;
//		}
//		if(i!=size)	{
//			shortestCandidates.add(i, caledCanNodeList);
//			if(shortestPathList.size() + shortestCandidates.size() > k)	shortestCandidates.pollLast();
//		} else {
//			if(shortestPathList.size() + shortestCandidates.size() < k)	shortestCandidates.add(caledCanNodeList);
//		}
//		
//		// 根据算法进行
//		MMapNodeList tempList = null;
//		MMapNewNodeList tempNewList = null;
//		MMapCandidateNodeList tempCandiList = null;
//		i = 0;
//		int calTime = 0;
//		size = mMap.size();
//		
//		while(true) {
//			if(shortestCandidates.isEmpty()) break;
//			tempCandiList = shortestCandidates.getFirst();
//			calTime++;
//			Boolean noAdd = Boolean.TRUE;
//			
//			Set<Entry<Integer, MMapNodeList>> set = mMap.entrySet();
//			// 遍历所有
//			for(Entry<Integer, MMapNodeList> entry : set) {
//				tempList = entry.getValue();
//				if(1 == tempList.sign) { // sign为1表示未被冲满
//					Long tempSum = 0L;
//					if(calTime==1)	tempSum = tempList.setTempSum(aList.getAllHeadDistance());
//					else tempSum = tempList.tempSum;
//					if(tempCandiList.tempSum > tempSum) {
//						noAdd = Boolean.FALSE;
//						if(!tempList.isMMapNewNodeList()) {
//							tempNewList = tempList.convertToNew();
//							tempNewList.addCandidatePath(tempCandiList);
//							mMap.put(entry.getKey(), tempNewList);
//						} else {
//							tempNewList = ((MMapNewNodeList)tempList);
//							tempNewList.addCandidatePath(tempCandiList);
//						}
//						tempCandiList.addNewPath(tempNewList);
//						break;
//					}
//				}
//			}
//			
//			// 判断是否全部都不小于该候选路径
//			if(noAdd==Boolean.TRUE) {
//				shortestPathList.add(tempCandiList);
//				if(shortestPathList.size()==k)	return;
//				shortestCandidates.poll();
//			} else 	break;
//		}
//	}
	
}
