package entity.sp.reach;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import entity.sp.AllDateWidNodes;
import entity.sp.AllPidWid;
import entity.sp.GraphByArray;
import entity.sp.RTreeWithGI;
import entity.sp.AllDateWidNodes.DWid;
import spatialindex.rtree.Node;
import spatialindex.rtree.RTree;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IBuffer;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;
import spatialindex.storagemanager.TreeLRUBuffer;
import utility.Global;
import utility.IOUtility;
import utility.LoopQueue;
import utility.TimeUtility;

/**
 * 提供RTree各节点可达情况方法
 * @author Monica
 *
 */
public class P2NRTreeReach extends RTree {
	
	private DataOutputStream dos = null;
	private int count = 0;
	private final static int numRTreeNode = 17482;
	public static Set<Integer>[] pid2Nids = null;
	private ArrayBlockingQueue<TempClass> queue;
	private static GraphByArray graph = null;
	private static Map<Integer, DWid> allDW = null;
	private static List<Integer> allPid = null;
	private static boolean hasInit = false;
	
	public P2NRTreeReach(PropertySet psRTree, IStorageManager sm, ArrayBlockingQueue<TempClass> queue) throws Exception {
		super(psRTree, sm);
		this.queue = queue;
		init();
	}
	
	
	public void init() {
		if(hasInit)	return;
		hasInit = true;
		try {
			System.out.println("> 开始初始化P2NRTreeReach . . . ");
			graph = new GraphByArray(Global.numNodes);
			graph.loadGraph(Global.inputDirectoryPath + Global.edgeFile);
			allDW = AllDateWidNodes.loadFromFile(Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile);
			allPid = AllPidWid.getAllPid();
			System.out.println("> 成功初始化P2NRTreeReach 。 ");
		} catch (Exception e) {
			System.err.println("> 初始化P2NRTreeReach失败！");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static P2NRTreeReach getInstance(String treePath, ArrayBlockingQueue<TempClass> queue) throws Exception{
		PropertySet psRTree = new PropertySet();
		psRTree.setProperty("FileName", treePath);
		psRTree.setProperty("PageSize", Global.rtreePageSize);
		psRTree.setProperty("BufferSize", Global.rtreeBufferSize);
		psRTree.setProperty("fanout", Global.rtreeFanout);
		
		IStorageManager diskfile = new DiskStorageManager(psRTree);
		IBuffer file = new TreeLRUBuffer(diskfile, Global.rtreeBufferSize, false);
		
		Integer i = new Integer(1); 
		psRTree.setProperty("IndexIdentifier", i);
		return new P2NRTreeReach(psRTree, file, queue);
	}
	
	private void write(int nid, Set<Integer> wids) throws Exception{
		dos.writeInt(nid);
		dos.writeInt(wids.size());
		for(int in : wids) {
			dos.writeInt(in);
		}
	}
	
	private Set<Integer> getRtreeNodeReachNids(Set<Integer> pids) throws Exception{
		Set<Integer> allSuitableNids = new HashSet<>();
		Set<Integer> allAccessedNids = new HashSet<>();
		
		int[] edges = null;
		DWid tDWid = null;
		LoopQueue<Integer> queue = new LoopQueue<>(1000000);
		
		for(int pid : pids) {
			if(allAccessedNids.contains(pid))	continue;
			
			// BFS
			queue.reset();
			Integer nid = -1;
			
			queue.push(pid);
			while(null != (nid = queue.poll())) {
				if(null != (edges =  graph.getEdge(nid))) {
					for(int e : edges) {
						if(!allAccessedNids.contains(e)) {
							if(!queue.push(e)) {
								throw new Exception("> 队列" + queue.size() + "太短");
							}
							allAccessedNids.add(e);
						}
					}
				}
				
				// 添加date
				if(null != (tDWid = allDW.get(nid)) && !allSuitableNids.contains(nid)) {
					allSuitableNids.add(nid);
				}
			}
		}
		
		allAccessedNids.clear();
		return allSuitableNids;
	}
	
	private Set<Integer> writeRTreeNode(int nid) throws Exception{
//		if(maxNid < nid) maxNid = nid;
		Node node = readNode(nid);
		Set<Integer> pids = new HashSet<>();
		Set<Integer> tSet = null;
		if(node.isLeaf()) {
			Set<Integer> rtreeNodeContainPids = new HashSet<>();
			for(int child = 0; child < node.m_children; child++) {
				rtreeNodeContainPids.add(node.m_pIdentifier[child]);
			}
			pids = getRtreeNodeReachNids(rtreeNodeContainPids);
		} else {
			for(int child = 0; child < node.m_children; child++) {
				tSet = writeRTreeNode(node.m_pIdentifier[child]);
				if(!tSet.isEmpty()) {
					pids.addAll(tSet);
				}
			}
		}
		if(!pids.isEmpty()) {
			queue.put(new TempClass(nid, pids));
		}
		if((++count)%1000 == 0) {
			System.out.println("> 已处理" + count + "个rtree node，用时：" + TimeUtility.getSpendTimeStr(Global.globalStartTime, System.currentTimeMillis()));
		}
		return pids;
	}
	
	public void writeRTreeNode2Nids(String filePath) throws Exception{
		System.out.println("> 开始输出recRTreeNode2NidReach.bin . . . " + TimeUtility.getTime());
//		this.dos = IOUtility.getDos(filePath);
		Node node = readNode(m_rootID);
		for(int child = 0; child < node.m_children; child++) {
			writeRTreeNode(node.m_pIdentifier[child]);
		}
//		dos.close();
		queue.put(new TempClass(Integer.MIN_VALUE, null));
		System.out.println("> Over输出recRTreeNode2NidReach.bin, 共处理" + count + "个rTree节点（除掉了root节点） ！！！ " + TimeUtility.getTailTime());
	}
	
	public static Set<Integer>[] loadRTreeNode2Pids(String filePath){
		DataInputStream dis = null;
		Set<Integer>[] rtreeNode2Pids = new Set[numRTreeNode + 1];
		try {
			dis = IOUtility.getDis(filePath);
			int nid, size;
			Set<Integer> pids = null;
			int i = 0;
			while(true) {
				nid = dis.readInt();
				size = dis.readInt();
				pids = new HashSet<>();
				for(i=0; i<size; i++) {
					pids.add(dis.readInt());
				}
				rtreeNode2Pids[nid] = pids;
			}
		} catch (EOFException e) {
			try {
				dis.close();
			} catch (Exception e2) {
			}
		} catch (Exception e) {
			System.err.println("> 加载loadRTreeNode2Pids出错而退出！！");
			e.printStackTrace();
			System.exit(0);
		}
		return rtreeNode2Pids;
	}
	
	public static void building() throws Exception{
		System.out.println("> 开始生成文件" + Global.recRTreeNode2NidReachPath + " . . . .");
		
//		System.out.println("> 开始读取pid . . . " + TimeUtility.getTime());
//		ArrayBlockingQueue<Integer> endSignQueue = new ArrayBlockingQueue<>(P2NReach.zipNum);
//		P2NReach.buidingP2NReachFile(endSignQueue);
//		int i = 0;
//		for(i=0; i<P2NReach.zipNum; i++) {
//			endSignQueue.take();
//		}
//		System.out.println("\n\n> 已读取完pid，开始处理RTree节点 . . . " + TimeUtility.getTailTime() + "\n");
		
//		P2NRTreeReach.pid2Nids = P2NReach.pid2Nids;
		ArrayBlockingQueue<TempClass> queue = new ArrayBlockingQueue(4);
		new TempClassWriteThread(queue, Global.recRTreeNode2NidReachPath).start();
		P2NRTreeReach rtree = P2NRTreeReach.getInstance(Global.indexRTree, queue);
		rtree.writeRTreeNode2Nids(Global.recRTreeNode2NidReachPath);
		
//		System.out.println("> 删除所有" + Global.recP2NReachPath + "文件 . . . ");
//		P2NReach.deleteAllFiles();
		
		System.out.println("> 已处理完所有RTree节点，用时：" + TimeUtility.getTailTime());
		
		System.out.println("> Over生成文件" + Global.recRTreeNode2NidReachPath + ", 用时: " + TimeUtility.getSpendTimeStr(Global.globalStartTime, System.currentTimeMillis()));
	}
	
	public static void test() throws Exception{
		P2NRTreeReach rtree = P2NRTreeReach.getInstance(Global.indexRTree, null);
//		System.out.println(rtree.getTreeHeight());
		System.out.println(rtree.getStatistics().getNumberOfNodes());
		System.out.println(rtree.m_rootID);
	}
	
	public static void main(String[] args) throws Exception{
		P2NRTreeReach.building();
//		Set<Integer>[] arr = P2NRTreeReach.loadRTreeNode2Pids(Global.recRTreeNode2NidReachPath);
//		int i = 0;
//		i = i+10;
	}
}
