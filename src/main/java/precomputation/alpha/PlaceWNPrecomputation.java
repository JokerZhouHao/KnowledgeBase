/**
 * 
 */
package precomputation.alpha;

import java.io.File;
import java.nio.file.DirectoryNotEmptyException;

import entity.sp.NidToDateWidIndex;
import entity.sp.RTreeWithGI;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IBuffer;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;
import spatialindex.storagemanager.TreeLRUBuffer;
import utility.Global;
import utility.TimeUtility;
import utility.Utility;

/**
 * Compute the alpha Word Neighborhoods of all the places and all the Rtree nodes together
 * @author jmshi
 */
public class PlaceWNPrecomputation {
	public static void main(String[] args) throws Exception {

		if(!new File(Global.inputDirectoryPath).exists()) {
			throw new DirectoryNotEmptyException("目录inputDirectoryPath ： " + Global.inputDirectoryPath + "不存在");
		}
		if(!new File(Global.outputDirectoryPath).exists()) {
			throw new DirectoryNotEmptyException("目录outputDirectoryPath ： " + Global.outputDirectoryPath + "不存在");
		}
		if(!new File(Global.outputDirectoryPath + Global.rTreePath).exists()) {
			throw new DirectoryNotEmptyException("存放RTree的目录 ： " + Global.outputDirectoryPath + Global.rTreePath + "不存在");
		}
		
		long start = System.currentTimeMillis();
		String pidWNFile = Global.placeWNFile;
		System.out.println("> 开始构造" + pidWNFile + "文件 . . .");
		
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
		
		RTreeWithGI rgi = new RTreeWithGI(psRTree, file);
		rgi.buildSimpleGraphInMemory();
		
		String nidToDateWidFile = Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile;
		NidToDateWidIndex nidToDateWidIndex = new NidToDateWidIndex(nidToDateWidFile);
		rgi.precomputeAlphaWN(nidToDateWidIndex, Global.radius);
		System.out.println("> 结束构造" + pidWNFile + "，用时：" + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()));
	}
}
