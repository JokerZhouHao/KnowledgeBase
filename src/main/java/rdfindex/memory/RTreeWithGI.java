/**
 * 
 */
package rdfindex.memory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import entity.sp.GraphByArray;
import neustore.base.LRUBuffer;
import spatialindex.rtree.RTree;
import spatialindex.spatialindex.Region;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;
import utility.Global;
import utility.Utility;

/**
 * We have two versions of RTreeWithGI:
 * 
 * this class is the version that has both R-tree and graph in memory, 
 * and is used when processing kSP queries
 * 
 * rdfindex.mygraph.RTreeWithGI is the version that has only graph in memory,
 * and is used to pre-build the alpha word neighborhoods of all the places and rtree nodes.
 * 
 * @author jmshi
 *
 */
public class RTreeWithGI extends RTree {
	// this RTree: in memory
	// graph member: in memory
	protected GraphByArray graph;

	public RTreeWithGI(PropertySet psRTree, IStorageManager sm) throws Exception {
		super(psRTree, sm);
		// Construct the inverted index file name, create the buffer for inverted index,
		// and create the inverted index instance
		String iindexname = Global.outputDirectoryPath + Global.invertedIndexFile + Global.diskFlag
				+ Global.iindexPageSize + Global.dataVersion;
		LRUBuffer buffer = new LRUBuffer(Global.iindexBufferSize, Global.iindexPageSize);
	}

	/**
	 * build in memory graph
	 * @throws Exception
	 */
	public void buildSimpleGraphInMemory() throws Exception {
		// Construct the input graph file
		String edgefile = Global.inputDirectoryPath + Global.edgeFile + Global.dataVersion;
		this.graph = new GraphByArray(Global.numNodes);
		this.graph.loadGraph(edgefile);
	}
	
	/**
	 * build in memory rtree
	 * 
	 * @throws Exception
	 */
	public void buildRtreeInMemory() throws Exception {

		String placefile = Global.inputDirectoryPath + Global.pidCoordFile + Global.dataVersion;
		
		BufferedReader reader = Utility.getBufferedReader(placefile);
		String line = reader.readLine();
		int cntLines = 0;
		double[] f1 = new double[2];
		double[] f2 = new double[2];
		String[] pidCoord;

		long start = System.currentTimeMillis();
		while ((line = reader.readLine()) != null) {
			cntLines++;
			if (line.contains(Global.delimiterPound)) {
				continue;
			}
			pidCoord = line.split(Global.delimiterLevel1);
			int id = Integer.parseInt(pidCoord[0]);
			String[] coord = pidCoord[1].split(Global.delimiterSpace);
			float x = Float.parseFloat(coord[0]);
			float y = Float.parseFloat(coord[1]);
			if (x < -90 || x > 90 || y < -180 || y > 180) {
				continue;
			}
			f1[0] = f2[0] = x;
			f1[1] = f2[1] = y;
			Region r = new Region(f1, f2);
			this.insertData(null, r, id);

			if (cntLines % 10000 == 0)
				System.out.println(cntLines + " places inserted");
		}

		long end = System.currentTimeMillis();

		System.err.println(this);
		System.err.println("Minutes: " + ((end - start) / 1000.0f) / 60.0f);

		boolean ret = this.isIndexValid();
		if (ret == false)
			System.err.println("Structure is INVALID!");

		if (cntLines != Global.numPlaces) {
			throw new Exception("actual num places in file is " + cntLines + " but config is "
					+ Global.numPlaces);
		}
		reader.close();
	}

	public GraphByArray getGraph() {
		return this.graph;
	}
}
