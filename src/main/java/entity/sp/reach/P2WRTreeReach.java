package entity.sp.reach;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

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
public class P2WRTreeReach extends RTree {
	
	private DataOutputStream dos = null;
	private int count = 0;
	public static Map<Integer, Short>[] pid2Wids = null;
	
	public P2WRTreeReach(PropertySet psRTree, IStorageManager sm) throws Exception {
		super(psRTree, sm);
	}
	
	public static P2WRTreeReach getInstance(String treePath) throws Exception{
		PropertySet psRTree = new PropertySet();
		psRTree.setProperty("FileName", treePath);
		psRTree.setProperty("PageSize", Global.rtreePageSize);
		psRTree.setProperty("BufferSize", Global.rtreeBufferSize);
		psRTree.setProperty("fanout", Global.rtreeFanout);
		
		IStorageManager diskfile = new DiskStorageManager(psRTree);
		IBuffer file = new TreeLRUBuffer(diskfile, Global.rtreeBufferSize, false);
		
		Integer i = new Integer(1); 
		psRTree.setProperty("IndexIdentifier", i);
		return new P2WRTreeReach(psRTree, file);
	}
	
	private void write(int nid, Map<Integer, Short> widDis) throws Exception{
		dos.writeInt(nid);
		dos.writeInt(widDis.size());
		for(Entry<Integer, Short> en : widDis.entrySet()) {
			dos.writeInt(en.getKey());
			dos.writeShort(en.getValue());
		}
	}
	
	private Map<Integer, Short> writeRTreeNode(int nid) throws Exception{
//		if(maxNid < nid) maxNid = nid;
		Node node = readNode(nid);
		Map<Integer, Short> widDis = new HashMap<>();
		Map<Integer, Short> tSet = null;
		if(node.isLeaf()) {
			for(int child = 0; child < node.m_children; child++) {
				tSet = pid2Wids[node.m_pIdentifier[child]];
				if(null != tSet) {
					for(Entry<Integer, Short> en : tSet.entrySet()) {
						if(widDis.containsKey(en.getKey())) {
							if(widDis.get(en.getKey()) > en.getValue()) {
								widDis.put(en.getKey(), en.getValue());
							}
						} else {
							widDis.put(en.getKey(), en.getValue());
						}
					}
					tSet.clear();
					pid2Wids[node.m_pIdentifier[child]] = null;
				}
			}
		} else {
			for(int child = 0; child < node.m_children; child++) {
				tSet = writeRTreeNode(node.m_pIdentifier[child]);
				if(!tSet.isEmpty()) {
					for(Entry<Integer, Short> en : tSet.entrySet()) {
						if(widDis.containsKey(en.getKey())) {
							if(widDis.get(en.getKey()) > en.getValue()) {
								widDis.put(en.getKey(), en.getValue());
							}
						} else {
							widDis.put(en.getKey(), en.getValue());
						}
					}
					tSet.clear();
				}
			}
		}
		if(!widDis.isEmpty())	write(-nid-1, widDis);	// -nid-1是为了与pid区分开
		if((++count)%1000 == 0) {
			System.out.println("> 已处理" + count + "个rtree node，用时：" + TimeUtility.getSpendTimeStr(Global.globalStartTime, System.currentTimeMillis()));
		}
		return widDis;
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
	
	public static Set<Integer>[] loadRTreeNode2Pids(String filePath) throws Exception{
		DataInputStream dis = null;
		Set<Integer>[] rtreeNode2Pids = new Set[RTreeLeafNodeContainPids.getMaxRtreeNodeId() + 1];
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
		System.out.println("> 开始处理pid . . . " + TimeUtility.getTime());
		ArrayBlockingQueue<Integer> endSignQueue = new ArrayBlockingQueue<>(P2WReach.zipNum);
		P2WReach.buildingPidToWidReachFile(endSignQueue);
		int i = 0;
		for(i=0; i<P2WReach.zipNum; i++) {
			endSignQueue.take();
		}
		System.out.println("\n\n> 已处理完pid，开始处理RTree节点 . . . " + TimeUtility.getTailTime() + "\n");
		
		P2WRTreeReach.pid2Wids = P2WReach.pid2Wids;
		P2WRTreeReach rtree = P2WRTreeReach.getInstance(Global.indexRTree);
		rtree.writeRTreeNode2Nids(Global.recPidWidReachPath + ".rtree");
		System.out.println("> 已处理完所有RTree节点，用时：" + TimeUtility.getTailTime());
	}
	
	// 删除文件
	public static void deleteAllFiles() throws Exception{
		P2WReach.deleteAllFiles();
		String fp = Global.recPidWidReachPath + ".rtree";
		new File(fp).delete();
	}
	
	public static void test() throws Exception{
		P2WRTreeReach rtree = P2WRTreeReach.getInstance(Global.indexRTree);
//		System.out.println(rtree.getTreeHeight());
		System.out.println(rtree.getStatistics().getNumberOfNodes());
		System.out.println(rtree.m_rootID);
	}
	
	public static void main(String[] args) throws Exception{
		P2WRTreeReach.building();
	}
}
