package utility;

import neustore.base.LRUBuffer;
import spatialindex.rtree.Node;
import spatialindex.rtree.RTree;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IBuffer;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;
import spatialindex.storagemanager.TreeLRUBuffer;

public class RTreeUtility {
	
	public static RTree instance() throws Exception{
		LRUBuffer buffer = new LRUBuffer(Global.alphaIindexRTNodeBufferSize, Global.rtreePageSize);
		PropertySet psRTree = new PropertySet();
		String indexRTree = Global.indexRTree;
		psRTree.setProperty("FileName", indexRTree);
		psRTree.setProperty("PageSize", Global.rtreePageSize);
		psRTree.setProperty("BufferSize", Global.rtreeBufferSize);
		psRTree.setProperty("fanout", Global.rtreeFanout);
		
		IStorageManager diskfile = new DiskStorageManager(psRTree);
		IBuffer file = new TreeLRUBuffer(diskfile, Global.rtreeBufferSize, false);
		
		Integer i = new Integer(1); 
		psRTree.setProperty("IndexIdentifier", i);
		return new RTree(psRTree, diskfile);
	}
	
	public static long numRtreeNode() throws Exception{
		int num = 0;
		RTree rtree = instance();
		return rtree.m_stats.getNumberOfNodes();
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println(RTreeUtility.numRtreeNode());
	}
}
