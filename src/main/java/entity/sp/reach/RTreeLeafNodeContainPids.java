package entity.sp.reach;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import spatialindex.rtree.Node;
import spatialindex.rtree.RTree;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IBuffer;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;
import spatialindex.storagemanager.TreeLRUBuffer;
import utility.Global;
import utility.IOUtility;
import utility.TimeUtility;

public class RTreeLeafNodeContainPids extends RTree{
	private DataOutputStream dos = null;
	private int count = 0;
	
	public static int maxId = Integer.MIN_VALUE;
	public static int minId = Integer.MAX_VALUE;
	private static Integer maxRtreeNodeId = null;
	
	public RTreeLeafNodeContainPids(PropertySet psRTree, IStorageManager sm) throws Exception {
		super(psRTree, sm);
	}
	
	private void writeRTreeNode(int nid) throws Exception{
		Node node = readNode(nid);
		if(node.isLeaf()) {
			dos.writeInt(-node.m_identifier -1);
			dos.writeInt(node.m_children);
			for(int child = 0; child < node.m_children; child++) {
				dos.writeInt(node.m_pIdentifier[child]);
			}
			if((++count)%1000 == 0) {
				System.out.println("> 已处理" + count + "个rtree leaf node，用时：" + TimeUtility.getSpendTimeStr(Global.globalStartTime, System.currentTimeMillis()));
			}
		} else {
			dos.writeInt(-node.m_identifier -1);
			dos.writeInt(node.m_children);
			for(int child = 0; child < node.m_children; child++) {
				dos.writeInt(-node.m_pIdentifier[child] - 1);
			}
			for(int child = 0; child < node.m_children; child++) {
				writeRTreeNode(node.m_pIdentifier[child]);
			}
		}
	}
	
	public void writeRTreeNode2Nids(String filePath) throws Exception{
		System.out.println("> 开始输出recRtreeLeafNodeContainPids.bin . . . " + TimeUtility.getTime());
		this.dos = IOUtility.getDos(filePath);
		Node node = readNode(m_rootID);
		for(int child = 0; child < node.m_children; child++) {
			writeRTreeNode(node.m_pIdentifier[child]);
		}
		dos.close();
		System.out.println("> Over输出recRtreeLeafNodeContainPids.bin, 共处理" + count + "个rTree叶子节点 ！！！ " + TimeUtility.getTailTime());
	}
	
	public static RTreeLeafNodeContainPids getInstance(String treePath) throws Exception{
		PropertySet psRTree = new PropertySet();
		psRTree.setProperty("FileName", treePath);
		psRTree.setProperty("PageSize", Global.rtreePageSize);
		psRTree.setProperty("BufferSize", Global.rtreeBufferSize);
		psRTree.setProperty("fanout", Global.rtreeFanout);
		
		IStorageManager diskfile = new DiskStorageManager(psRTree);
		IBuffer file = new TreeLRUBuffer(diskfile, Global.rtreeBufferSize, false);
		
		Integer i = new Integer(1); 
		psRTree.setProperty("IndexIdentifier", i);
		return new RTreeLeafNodeContainPids(psRTree, file);
	}
	
	public static int[] loadPid2RTreeLeafNode(String filePath) throws Exception{
		int[] ins = new int[Global.numPid + getMaxRtreeNodeId() + 1];
		int i=0;
		for(i=0; i<ins.length; i++) {
			ins[i] = 1;
		}
		
		DataInputStream dis = null;
		try {
			dis = IOUtility.getDis(filePath);
			int rtreeNid = 0;
			int num = 0;
			int id = 0;
			while(true) {
				rtreeNid = dis.readInt();
				num = dis.readInt();
				for(i=0; i<num; i++) {
					id = dis.readInt();
					if(id<0)	id = (-id) - 1 + Global.numPid;
					ins[id] = rtreeNid;
				}
			}
		} catch(EOFException e) {
			System.out.println("> 成功加载recRtreeLeafNodeContainPids.bin");
			try {
				dis.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		} catch (Exception e) {
			System.out.println("> 失败加载recRtreeLeafNodeContainPids.bin");
			e.printStackTrace();
			System.exit(0);
		}
		return ins;
	}
	
	public void displayAllNid(int nid) throws Exception{
//		if(nid==8462) {
//			Node node = readNode(nid);
//			for(int child = 0; child < node.m_children; child++) {
//				System.out.print(node.m_pIdentifier[child] + "  ");
//			}
//			throw new Exception("nid=8462 " + readNode(8462).isLeaf());
//		}
		if(nid<0) {
//			System.out.println(m_rootID);
			if(minId>m_rootID) minId = m_rootID;
			if(maxId<m_rootID)	maxId = m_rootID;
			displayAllNid(m_rootID);
		} else {
			Node node = readNode(nid);
			if(!node.isLeaf()) {
				for(int child = 0; child < node.m_children; child++) {
//					System.out.println(node.m_pIdentifier[child]);
					if(minId>node.m_pIdentifier[child]) minId = node.m_pIdentifier[child];
					if(maxId<node.m_pIdentifier[child])	maxId = node.m_pIdentifier[child];
					displayAllNid(node.m_pIdentifier[child]);
				}
			}
		}
	}
	
	public static int getMaxRtreeNodeId() throws Exception{
		if(maxId!=Integer.MIN_VALUE) {
			return maxId;
		}
		RTreeLeafNodeContainPids rtree = RTreeLeafNodeContainPids.getInstance(Global.indexRTree);
		rtree.displayAllNid(-1);
		return maxId;
	}
	
	public static void main(String[] args) throws Exception{
		String filePath = Global.recRTreeLeafNodeContainPidsPath;
		RTreeLeafNodeContainPids.getInstance(Global.indexRTree).writeRTreeNode2Nids(filePath);
		
//		System.out.println(RTreeLeafNodeContainPids.getMaxRtreeNodeId());
		
//		int[] ins = RTreeLeafNodeContainPids.loadPid2RTreeLeafNode(filePath);
//		for(int in : ins) {
//			System.out.println(in);
//		}
//		System.out.println(ins[158823]);
		
//		RTreeLeafNodeContainPids rtree = RTreeLeafNodeContainPids.getInstance(Global.indexRTree);
//		System.out.println(rtree.readNode(615).isLeaf());
//		rtree.displayAllNid(-1);
//		System.out.println();
//		System.out.println(minId + "  " + maxId);
		
//		System.out.println(RTreeLeafNodeContainPids.getMaxRtreeNodeId());
		
//		int i = -1;
//		i = i+1;
	}
	
}
