/**
 * 
 */
package utility;

import rdfindex.memory.RTreeWithGI;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.MemoryStorageManager;
import spatialindex.storagemanager.PropertySet;

/**
 * @author jieming
 *
 */
public class RGIUtility {

	/**
	 * Build the whole index, namely RTreeWithGI, where G is for RDF Graph and I is for inverted index.
	 * Inverted index is pre-built and stored on disk, while R-tree and graph are build on the fly in memory.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static RTreeWithGI buildRGI() throws Exception {
		
		PropertySet psRTree = new PropertySet();
		
		Double f = new Double(0.7);
		psRTree.setProperty("FillFactor", f);
		psRTree.setProperty("IndexCapacity", Global.rtreeFanout);
		psRTree.setProperty("LeafCapacity", Global.rtreeFanout);
		psRTree.setProperty("Dimension", new Integer(2));
		psRTree.setProperty("fanout", Global.rtreeFanout);
		IStorageManager rtreeMem = new MemoryStorageManager();	
		//the configuration of inverted index is inside this RTreeWithGI constructor.
		RTreeWithGI rgi = new RTreeWithGI(psRTree, rtreeMem);
		// build the RDF graph
		rgi.buildSimpleGraphInMemory();
		// build the Rtree
		rgi.buildRtreeInMemory();
		return rgi;

	}
}
