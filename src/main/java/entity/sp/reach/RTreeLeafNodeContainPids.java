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
	
	public static int[] loadPid2RTreeLeafNode(String filePath){
		int[] ins = new int[Global.numPid];
		int i=0;
		for(i=0; i<ins.length; i++) {
			ins[i] = 1;
		}
		
		DataInputStream dis = null;
		try {
			dis = IOUtility.getDis(filePath);
			int rtreeNid = 0;
			int num = 0;
			while(true) {
				rtreeNid = dis.readInt();
				num = dis.readInt();
				for(i=0; i<num; i++) {
					ins[dis.readInt()] = rtreeNid;
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
	
	public static void main(String[] args) throws Exception{
		String filePath = Global.recRTreeLeafNodeContainPidsPath;
		RTreeLeafNodeContainPids.getInstance(Global.indexRTree).writeRTreeNode2Nids(filePath);
//		int[] ins = RTreeLeafNodeContainPids.loadPid2RTreeLeafNode(filePath);
//		System.out.println(ins[158823]);
		
//		RTreeLeafNodeContainPids rtree = RTreeLeafNodeContainPids.getInstance(Global.indexRTree);
//		System.out.println(rtree.readNode(615).isLeaf());
		
//		int i = -1;
//		i = i+1;
	}
	
}
