/**
 * 
 */
package precomputation.alpha;

import entity.sp.InvertedIndex;
import entity.sp.RTreeWithGI;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IBuffer;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;
import spatialindex.storagemanager.TreeLRUBuffer;
import utility.Global;
import utility.Utility;

/**
 * Compute the alpha Word Neighborhoods of all the places and all the Rtree nodes together
 * @author jmshi
 */
public class AlphaWNPrecomputation {
	public static void main(String[] args) throws Exception {

		if (args.length != 3) {
			System.out.println("usage: runnable configFile alpha_radius nidKeywordsListFile");
			System.exit(-1);
		}
		
		Utility.loadInitialConfig(args[0]);
		double alpha_radius = Double.parseDouble(args[1]);
		
		PropertySet psRTree = new PropertySet();
		String treefile = Global.outputDirectoryPath + Global.pidCoordFile + Global.rtreeFlag + Global.rtreeFanout + Global.dataVersion;
		psRTree.setProperty("FileName", treefile);
		psRTree.setProperty("PageSize", Global.rtreePageSize);
		psRTree.setProperty("BufferSize", Global.rtreeBufferSize);
		psRTree.setProperty("fanout", Global.rtreeFanout);
		
		IStorageManager diskfile = new DiskStorageManager(psRTree);
		IBuffer file = new TreeLRUBuffer(diskfile, Global.rtreeBufferSize, false);
		
		Integer i = new Integer(1); 
		psRTree.setProperty("IndexIdentifier", i);
		
		RTreeWithGI rgi = new RTreeWithGI(psRTree, file);
		rgi.buildSimpleGraphInMemory();
		
		String vidDocFile = args[2];
		InvertedIndex nidKeywordsMap = new InvertedIndex(vidDocFile);
		long start = System.currentTimeMillis();
		rgi.precomputeAlphaWN(nidKeywordsMap, alpha_radius);
		long end = System.currentTimeMillis();
		System.out.println("Revision Minutes: " + ((end - start) / 1000.0f) / 60.0f);
		
	}
}
