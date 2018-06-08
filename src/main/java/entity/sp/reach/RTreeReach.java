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

import entity.sp.RTreeWithGI;
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

/**
 * 提供RTree各节点可达情况方法
 * @author Monica
 *
 */
public class RTreeReach extends RTree{
	
	private DataOutputStream dos = null;
	private int count = 0;
	private final static int numRTreeNode = 17482;
	
	public RTreeReach(PropertySet psRTree, IStorageManager sm) throws Exception {
		super(psRTree, sm);
	}
	
	public static RTreeReach getInstance(String treePath) throws Exception{
		PropertySet psRTree = new PropertySet();
		psRTree.setProperty("FileName", treePath);
		psRTree.setProperty("PageSize", Global.rtreePageSize);
		psRTree.setProperty("BufferSize", Global.rtreeBufferSize);
		psRTree.setProperty("fanout", Global.rtreeFanout);
		
		IStorageManager diskfile = new DiskStorageManager(psRTree);
		IBuffer file = new TreeLRUBuffer(diskfile, Global.rtreeBufferSize, false);
		
		Integer i = new Integer(1); 
		psRTree.setProperty("IndexIdentifier", i);
		return new RTreeReach(psRTree, file);
	}
	
	private void write(int nid, List<Integer> nids) throws Exception{
		dos.writeInt(nid);
		dos.writeInt(nids.size());
		for(int in : nids) {
			dos.writeInt(in);
		}
	}
	
	private List<Integer> writeRTreeNode(int nid) throws Exception{
//		if(maxNid < nid) maxNid = nid;
		Node node = readNode(nid);
		List<Integer> nids = new ArrayList<>();
		if(node.isLeaf()) {
			for(int child = 0; child < node.m_children; child++) {
				nids.add(node.m_pIdentifier[child]);
			}
		} else {
			List<Integer> tList = null;
			for(int child = 0; child < node.m_children; child++) {
				tList = writeRTreeNode(node.m_pIdentifier[child]);
				if(!tList.isEmpty()) {
					nids.addAll(tList);
				}
				tList.clear();
			}
		}
		if(!nids.isEmpty())	write(nid, nids);
		if((++count)%1000 == 0) {
			System.out.println("> 已处理" + count + "个rtree node，用时：" + TimeUtility.getSpendTimeStr(Global.globalStartTime, System.currentTimeMillis()));
		}
		return nids;
	}
	
	public void writeRTreeNode2Nids(String filePath) throws Exception{
		System.out.println("> 开始输出writeRTreeNode2Nids . . . " + TimeUtility.getTime());
		this.dos = IOUtility.getDos(filePath);
		Node node = readNode(m_rootID);
		for(int child = 0; child < node.m_children; child++) {
			writeRTreeNode(node.m_pIdentifier[child]);
		}
		dos.close();
		System.out.println("> Over输出writeRTreeNode2Nids, 共处理" + count + "个rTree节点（除掉了root节点） ！！！ " + TimeUtility.getTailTime());
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
	
	public static void test() throws Exception{
		RTreeReach rtree = RTreeReach.getInstance(Global.indexRTree);
//		System.out.println(rtree.getTreeHeight());
		System.out.println(rtree.getStatistics().getNumberOfNodes());
		System.out.println(rtree.m_rootID);
	}
	
	public static void main(String[] args) throws Exception{
//		test();
		RTreeReach rtree = RTreeReach.getInstance(Global.indexRTree);
		rtree.writeRTreeNode2Nids(Global.outputDirectoryPath + Global.rtreeNode2PidsFile);
//		System.out.println(rtree.maxNid);
//		Set<Integer>[] map = RTreeReach.loadRTreeNode2Pids(Global.outputDirectoryPath + Global.rtreeNode2PidsFile);
//		int i = 0;
//		i++;
	}
}
