package zhou.hao.entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Set;

import zhou.hao.service.GoogleFreebaseLineProcessService;

public class NodeIdHashMapTestMap {
	
	private HashMap<String, Integer> nodeNameMap = new HashMap<String, Integer>();
	private String lastestNodeName[] = new String[2];
	private Integer lastestNodeId[] = new Integer[2];
	private HashMap<Integer, HashMap<String, Boolean>> keywordHashMap = new HashMap<>();
	private HashMap<Integer, HashMap<Integer, Boolean>> pointToNodeHashMap = new HashMap<>(); 
	
	private LinkedBlockingQueue<Pair<Integer, String>> attrBlockQuue = new LinkedBlockingQueue<Pair<Integer, String>>(1000);
	private LinkedBlockingQueue<Pair<Integer, Integer>> pointBlockQuue = new LinkedBlockingQueue<Pair<Integer, Integer>>(1000);
	private ArrayBlockingQueue<Boolean> signBlockQueue = new ArrayBlockingQueue<Boolean>(2);
	
	public NodeIdHashMapTestMap() {
		this.lastestNodeName[0] = this.lastestNodeName[1] = "[^\\]";
		this.lastestNodeId[0] = this.lastestNodeId[1] = -1;
		ExecutorService exc = Executors.newCachedThreadPool();
		exc.execute(new AddAttrThread());
		exc.execute(new AddPointToNodeThread());
		exc.shutdown();
	}
	
	public HashMap<String, Integer> getNodeNameMap() {
		return nodeNameMap;
	}

	public void setNodeNameMap(HashMap<String, Integer> nodeNameMap) {
		this.nodeNameMap = nodeNameMap;
	}

	public  HashMap<Integer, HashMap<String, Boolean>> getKeywordHashMap() {
		return keywordHashMap;
	}

	public void setKeywordHashMap( HashMap<Integer, HashMap<String, Boolean>> keywordHashMap) {
		this.keywordHashMap = keywordHashMap;
	}

	public HashMap<Integer, HashMap<Integer, Boolean>> getPointToNodeHashMap() {
		return pointToNodeHashMap;
	}

	public void setPointToNodeHashMap(HashMap<Integer, HashMap<Integer, Boolean>> pointToNodeHashMap) {
		this.pointToNodeHashMap = pointToNodeHashMap;
	}

	// 查找节点
	public Integer findNode(String nodeName) {
		for(int i=0; i<2; i++) {
			if(nodeName.length()==this.lastestNodeName[i].length() && nodeName.equals(this.lastestNodeName[i])) return this.lastestNodeId[i];
		}
		
		return this.nodeNameMap.get(nodeName);
	}
	
	// 添加属性
	public void addAttr(Integer nodeId, String key) {
		HashMap<String, Boolean> attrMap  = this.keywordHashMap.get(nodeId);
		if(null==attrMap) {
			attrMap = new HashMap<String, Boolean>(1);
			attrMap.put(key, Boolean.TRUE);
			this.keywordHashMap.put(nodeId, attrMap);
		} else {
			if(null == attrMap.get(key)) {
				attrMap.put(key, Boolean.TRUE);
			}
		}
	}
	
	// 添加属性线程
	class AddAttrThread implements Runnable{
		@Override
		public void run() {
			try {
				while(true) {
					Pair<Integer, String> attrEntry = attrBlockQuue.take();
					if(attrEntry.getKey()>=0) {
						addAttr(attrEntry.getKey(), attrEntry.getValue());
					} else if(attrEntry.getKey()==-1) {
						signBlockQueue.put(Boolean.TRUE);	// 要写入块
					} else if(attrEntry.getKey()==-2) {
						signBlockQueue.put(Boolean.TRUE);
						break;	// 读完了
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("> AddAttrThread异常退出！！！\n");
				System.exit(0);
			}
		}
		
	}
	
	// 添加指向点
	public void addPointToNode(Integer nodeId, Integer pointToNodeId) {
		HashMap<Integer, Boolean> pointToNodeMap = this.pointToNodeHashMap.get(nodeId);
		if(null==pointToNodeMap) {
			pointToNodeMap = new HashMap<Integer, Boolean>(1);
			pointToNodeMap.put(pointToNodeId, Boolean.TRUE);
			this.pointToNodeHashMap.put(nodeId, pointToNodeMap);
		}
		else {
			if(null==pointToNodeMap.get(pointToNodeId)) {
				pointToNodeMap.put(pointToNodeId, Boolean.TRUE);
			}
		}
	}
	
	// 添加指向点线程
	class AddPointToNodeThread implements Runnable{
		@Override
		public void run() {
			try {
				while(true) {
					Pair<Integer, Integer> pointEntry = pointBlockQuue.take();
					if(0<=pointEntry.getKey()) {
						addPointToNode(pointEntry.getKey(), pointEntry.getValue());
					} else if(-1==pointEntry.getKey()) {
						signBlockQueue.put(Boolean.TRUE);	// 要写入块了
					} else if(-2==pointEntry.getKey()) {
						signBlockQueue.put(Boolean.TRUE);
						break;	// 读取完毕
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("> AddPointToNodeThread异常退出\n");
				System.exit(0);
			}
		}
	}
	
	// 通知要写块，并等待AddAttThread和AddPointThread执行完毕
	public void notifyWriteBlock() {
		try {
			System.out.println("notifyWriteBlock!!!");
			this.attrBlockQuue.put(new Pair<Integer, String>(-1, ""));
			this.pointBlockQuue.put(new Pair<Integer, Integer>(-1, -1));
			this.signBlockQueue.take();
			this.signBlockQueue.take();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("> notifyWriteBlock()异常而退出！！！");
			System.exit(0);
		}
	}
	
	// 通知已经读完了
	public void notifyReadOver() {
		try {
			this.attrBlockQuue.put(new Pair<Integer, String>(-2, ""));
			this.pointBlockQuue.put(new Pair<Integer, Integer>(-2, -1));
			this.signBlockQueue.take();
			this.signBlockQueue.take();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("> notifyReadOver()异常而退出！！！");
			System.exit(0);
		}
	}
	
	// 添加节点
	public void addNode(GoogleFreebaseLineProcessService.Node freeBaseLineNode) {
		freeBaseLineNode = freeBaseLineNode.getNext();
		Integer index = 0;
		while(null != freeBaseLineNode) {
			Integer newNodeId = this.findNode(freeBaseLineNode.getNodeStr());
			if(null==newNodeId) {	// 没有该节点
				newNodeId = this.nodeNameMap.size();
				this.nodeNameMap.put(freeBaseLineNode.getNodeStr(), newNodeId);
			}
			if(null!=freeBaseLineNode.getNodeAttr()) {	// 添加属性
//				this.addAttr(newNodeId, freeBaseLineNode.getNodeAttr());
				try {
					this.attrBlockQuue.put(new Pair<Integer, String>(newNodeId, freeBaseLineNode.getNodeAttr()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			this.lastestNodeName[index] = freeBaseLineNode.getNodeStr();
			this.lastestNodeId[index] = newNodeId;
			index++;
			if(2==index) {	// 该行有两个节点
//				this.addPointToNode(this.lastestNodeId[0], this.lastestNodeId[1]);
				try {
					this.pointBlockQuue.put(new Pair<Integer, Integer>(this.lastestNodeId[0], this.lastestNodeId[1]));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
//			try {
////				Thread.sleep(3000);
//			} catch (Exception e) {
//				// TODO: handle exception
//			}
			
			
			
			freeBaseLineNode = freeBaseLineNode.getNext();
		}
	}
	
	// 显示所有节点
	public void displayAllNodes() {
		Set<Entry<String, Integer>> nodeIdSet = this.nodeNameMap.entrySet();
		ArrayList<Entry<String, Integer>> nodeIdList = new ArrayList<>();
		nodeIdList.addAll(nodeIdSet);
		this.nodeNameMap.clear();
		System.gc();
		Collections.sort(nodeIdList, new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o1.getValue() - o2.getValue();
			}
		});
		for(Entry<String, Integer> node : nodeIdList) {
			Integer nodeId = node.getValue();
			System.out.print(nodeId + " | "); // 输出节点id
			System.out.print(node.getKey() + " | "); // 输出节点名
			// 输出指向点
			HashMap<Integer, Boolean> pointToNodeMap = this.pointToNodeHashMap.get(nodeId);
			if(null!=pointToNodeMap) {
				for(Entry<Integer, Boolean> entry : pointToNodeMap.entrySet()) {
					System.out.print(entry.getKey() + " ");
				}
			}
			System.out.print(" | ");
			// 输出属性
			HashMap<String, Boolean> attrMap = this.keywordHashMap.get(nodeId);
			if(null!=attrMap) {
				for(Entry<String, Boolean> entry : attrMap.entrySet()) {
					System.out.print(entry.getKey() + " ");
				}
			}
			System.out.println(" |");
		}
	}
	
	// 获得节点数
	public int getNodeNum() {
		return this.nodeNameMap.size();
	}
}
