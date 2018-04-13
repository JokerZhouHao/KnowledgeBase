package entity.freebase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import entity.freebase.MMap.PathList;

import java.util.Set;

/**
 * 
 * @author Monica
 * @since 2017/12/6
 * 功能：记录RDF K Shortest Pah中的a变量
 */

public class AList {
	
	public static class Path {
		int currentNodeId = -1;
		int targetNodeId = -1;
//		HashMap<Integer, Boolean> pathNodeMap = new HashMap<Integer, Boolean>();
		public int distance = 0;
		int aListIndex = -1;
		Path next = null;
		
		public Path() {
			
		}
		
		public Path(int tar, int aListIndex) {
			targetNodeId = currentNodeId = tar;
			this.aListIndex = aListIndex;
//			pathNodeMap.put(tar, Boolean.TRUE);
		}
		
		// 若路径中已经包含该节点就返回null
		public Path add(int cur, int aListIndex) {
//			if(pathNodeMap.containsKey(cur))	return null;
//			else {
				return this.copy(cur, aListIndex);
//			}
		}
		
		// 创建新的path
//		public Path createNewPath(int targetNodeId) {
//			Path path = new Path();
//			path.targetNodeId = targetNodeId;
//			path.distance = 
//		}
		
		public Path copy(int cur, int aListIndex) {
			Path path = new Path();
			path.currentNodeId = cur;
			path.distance = distance + 1;
//			path.pathNodeMap = (HashMap<Integer, Boolean>)pathNodeMap.clone();
//			path.pathNodeMap.put(cur, Boolean.TRUE);
			path.targetNodeId = targetNodeId;
			path.aListIndex = aListIndex;
			return path;
		}
		
		// 打印Path
		public void display() {
			System.out.print("<" + currentNodeId + "," + targetNodeId + "," + distance + ">");
//			Set<Integer> set = pathNodeMap.keySet();
//			for(Integer in : set) {
//				System.out.print(" " + in);
//			}
//			System.out.println();
		}

		public int getCurrentNodeId() {
			return currentNodeId;
		}

		public int getTargetNodeId() {
			return targetNodeId;
		}

		public int getDistance() {
			return distance;
		}

		public int getaListIndex() {
			return aListIndex;
		}
	}
	
	public class PolledPath{
		private int pathListIndex = -1;
		private Path path = null;
		
		public PolledPath(int li, Path pa) {
			this.pathListIndex = li;
			this.path = pa;
		}
		
		public int getPathListIndex() {
			return pathListIndex;
		}
		public Path getPath() {
			return path;
		}
		
		public int getDis() {
			return path.distance;
		}
	}
	
	public class PathQueue{
		Path head = new Path();
		Path tail = null;
		int size = 0;
		
		public void addPath(Path p) {
			if(tail!=null) {
				tail.next = p;
				tail = p;
				p.next = null;
			} else {
				head.next = tail = p;
				tail.next = null;
			}
			size++;
		}
		
		// 弹出
		public Path pollFirst() {
			if(head.next!=null)	{
				head = head.next;
				size--;
				return head;
			} else 	return null;
		}
		
		// 添加头结点
		public void addFirst(Path pa) {
			pa.next = head.next;
			head = new Path();
			head.next = pa;
			size++;
		}
	}
	
	private ArrayList<PathQueue> aList = new ArrayList<PathQueue>();
	private Integer[] queueHeadsDis = null;
	private Boolean headsHasChange = Boolean.FALSE;
	private int size = 0;
	private int lastPollPathIndex = 0;
	private int lastPollPathDis = -1;
	
	public AList(int searchedKeywordNum) {
		size = searchedKeywordNum;
		for(int i=0; i<searchedKeywordNum; i++)	aList.add(new PathQueue()); 
		queueHeadsDis = new Integer[searchedKeywordNum];
		int len = queueHeadsDis.length;
		for(int i=0; i<len; i++)	queueHeadsDis[i] = -1;
	}
	
	// 重置
	public void reset() {
		aList.clear();
		queueHeadsDis = null;
		headsHasChange = Boolean.FALSE;
		size = 0;
		lastPollPathIndex = 0;
		lastPollPathDis = -1;
//		System.gc();
	}
	
	// 获得队列长度
	public ArrayList<Integer> getAllQueueSize(){
		ArrayList<Integer> li = new ArrayList<>();
		for(PathQueue pq : this.aList)
			li.add(pq.size);
		return li;
	}
	
	// 初始化queueHeadsDis
	public void initQueueHeadsDis(MMap mmap) {
		headsHasChange = Boolean.TRUE;
		for(int i=0; i<size; i++) {
			PathQueue pq = aList.get(i);
			if(pq.size>0)	queueHeadsDis[i] = pq.head.next.distance;
			else {
				for(PathList pl : mmap.getmMap().values())
					if(pl.get(i)==null)	pl.isDeal = Boolean.TRUE;
			}
		}
	}
	
	// 添加路径
	public void addPath(int pathListIndex, Path pa) {
		aList.get(pathListIndex).addPath(pa);
	}
	
	// 弹出列首的最短路径
	public PolledPath poll() {
		PathQueue pqu = aList.get(lastPollPathIndex);
		if(pqu.size >0 && pqu.head.next.distance == lastPollPathDis) {
			return new PolledPath(lastPollPathIndex, pqu.pollFirst());
		}
		
		int index = -1;
		for(int i=0; i<size; i++) {
			if(0 != aList.get(i).size) {
				if(index==-1)	index = i;
				else
					if(aList.get(index).head.next.distance>aList.get(i).head.next.distance)	index = i;
			}
		}
		if(-1==index)	return null;
		else {
//			PathQueue pq = aList.get(index);
//			Path tempP = pq.pollFirst();
//			if(pq.size>0 && queueHeadsDis[index]!=pq.head.next.distance) {
//				queueHeadsDis[index] = pq.head.next.distance;
//				headsHasChange = Boolean.TRUE;
//			}
			lastPollPathIndex = index;
			lastPollPathDis = aList.get(index).head.next.distance;
			return new PolledPath(index, aList.get(index).pollFirst());
		}
	}
	
	// 修改headsHasChange标志, 返回值若不小于0，表明返回数字对应的aList元素为空
	public int verifyHeadsHasChange(int pathIndex) {
		if(aList.get(pathIndex).size>0) {
			if(queueHeadsDis[pathIndex]!=aList.get(pathIndex).head.next.distance) {
				queueHeadsDis[pathIndex] = aList.get(pathIndex).head.next.distance;
				headsHasChange = Boolean.TRUE;
				return -1;
			} else return -1;
		} else return pathIndex;
	}
	
	// 获得所有队首的距离
	public Integer[] getAllHeadDistance(){
		return queueHeadsDis;
	}
	
	// 打印AList
	public void display() {
		System.out.println("AList display --------------------------->");
		int size = aList.size();
		for(int i=0; i<size; i++) {
			System.out.println("word > " + i + " : 队列路径----->");
			Path tempP = aList.get(i).head;
			while(null!=(tempP=tempP.next))	tempP.display();
			System.out.println();
		}
		System.out.println();
	}

	public Boolean getHeadsHasChange() {
		return headsHasChange;
	}

	public void setHeadsHasChange(Boolean headsHasChange) {
		this.headsHasChange = headsHasChange;
	}

	public Integer[] getQueueHeadsDis() {
		return queueHeadsDis;
	}

	public void setQueueHeadsDis(Integer[] queueHeadsDis) {
		this.queueHeadsDis = queueHeadsDis;
	}
	
	public void addFirst(int listIndex, Path path) {
		aList.get(listIndex).addFirst(path);
		
	}
}
