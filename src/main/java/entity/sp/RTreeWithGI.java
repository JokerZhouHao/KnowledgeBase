/**
 * 
 */
package entity.sp;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import entity.sp.GraphByArray;
import entity.sp.SortedList.SortedListNode;
import spatialindex.rtree.Node;
import spatialindex.rtree.RTree;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;
import utility.Global;

/**
 * 
 * We have two versions of RTreeWithGI:
 * 
 * this class is the version that has only graph in memory,
 * and is used to pre-build the alpha word neighborhoods of all the places and rtree nodes.
 * 
 * rdfindex.mygraph.memory.RTreeWithGI  is the version that has both R-tree and graph in memory, 
 * and is used when processing kSP queries
 * 
 * @author jmshi
 *
 */
public class RTreeWithGI extends RTree {
	// this RTree: disk
	// graph: in memory
	protected GraphByArray graph;

	public RTreeWithGI(PropertySet psRTree, IStorageManager sm) throws Exception {
		super(psRTree, sm);
	}

	/**
	 * build in memory graph
	 * 
	 * @throws Exception
	 */
	public void buildSimpleGraphInMemory() throws Exception {
		String edgefile = Global.inputDirectoryPath + Global.edgeFile + Global.dataVersion;
		this.graph = new GraphByArray(Global.numNodes);
		this.graph.loadGraph(edgefile);
	}

	public GraphByArray getGraph() {
		return this.graph;
	}

	public void precomputeAlphaWN(NidToDateWidIndex nidToDateWidIndex, int radius) throws Exception {
		/*
		 * add all the leaf nodes of Rtree into graph for each keyword, read its
		 * postinglist add the keyword into graph run Bellman-Ford algorithm to
		 * get all SPDistance from keyword to all the leaf nodes? store the
		 * result remove the keyword from graph remove all the leaf nodes of
		 * Rtree from graph
		 */
		PrintWriter writer = new PrintWriter(Global.outputDirectoryPath + Global.alphaWN + Global.rtreeFlag
				+ Global.rtreeFanout + "." + radius + Global.dataVersion);
		PrintWriter writerrt = new PrintWriter(Global.outputDirectoryPath + "alphaDocCompTime"
				+ Global.rtreeFlag + Global.rtreeFanout + "." + radius + Global.rtreeFanout
				+ Global.dataVersion);

		int[] count = new int[3];// numNodes and sumDocLength

		long starttime = System.currentTimeMillis();
		precomputeAlphaWN(this.m_rootID, nidToDateWidIndex, radius, writer, count);
		long endtime = System.currentTimeMillis();

		writer.close();

		writerrt.println((endtime - starttime) + Global.delimiterPound + count[0] + Global.delimiterPound
				+ count[1] + Global.delimiterPound + count[2] + Global.delimiterPound);
		writerrt.close();
	}

	/**
	 * @param vidDocMap
	 * @param writer
	 * @param sumDocLength
	 * @param numNodes
	 * @throws Exception
	 */
	private RadiusNeighborhood precomputeAlphaWN(int nodeid, NidToDateWidIndex nidToDateWidIndex,
			int radius, PrintWriter writer, int[] count) throws Exception {
		Node n = readNode(nodeid);
		System.out.println("processing " + count[0] + "th node with id " + n.m_identifier);
		if (n.isLeaf()) {

			RadiusNeighborhood leafRadiusWN = new RadiusNeighborhood(radius);
			for (int child = 0; child < n.m_children; child++) {
				// get and output the alpha document of places in the leaf node
				int pid = n.m_pIdentifier[child];
				count[2]++;
				RadiusNeighborhood radiusWN = graph.alphaRadiusOfVertex(pid, radius, nidToDateWidIndex);
				this.outputAlphaWN(writer, radius, pid, radiusWN, count);

				// merge the alpha document of places to get the alpha document of the leaf node
				leafRadiusWN.merge(radiusWN);
			}
			this.outputAlphaWN(writer, radius, (-n.getIdentifier() - 1), leafRadiusWN, count);
			count[0]++;
			return leafRadiusWN;

		} else {
			RadiusNeighborhood nodeAlphaWN = new RadiusNeighborhood(radius);
			int child;
			for (child = 0; child < n.m_children; child++) {
				RadiusNeighborhood childAlphaWN = precomputeAlphaWN(n.m_pIdentifier[child],
						nidToDateWidIndex, radius, writer, count);
				if(this.m_rootID != n.getIdentifier())	nodeAlphaWN.merge(childAlphaWN);
			}
			if(this.m_rootID != n.getIdentifier()) {
				this.outputAlphaWN(writer, radius, (-n.getIdentifier() - 1), nodeAlphaWN, count);
			}
			count[0]++;
			return nodeAlphaWN;
		}
	}

	/**
	 * @param writer
	 * @param n
	 * @param alphaDocOfN
	 */
	private void outputAlphaWN(PrintWriter writer, int radius, int vid, RadiusNeighborhood radiusWN, int[] count) {
		writer.print(vid + Global.delimiterLevel1);
		SortedListNode p = null;
		for(HashMap<Integer, SortedList> widToDateMap : radiusWN.getEachLayerWN()) {
			if(null == widToDateMap || widToDateMap.isEmpty()) {
				writer.print(Global.signEmptyLayer + Global.delimiterLayer);
			}
			for(Entry<Integer, SortedList> en : widToDateMap.entrySet()) {
				writer.print(en.getKey() + Global.delimiterLevel2);
				p = en.getValue().getHead();
				while(null != p) {
					writer.print(p.getValue() + Global.delimiterDate);
					p = p.getNext();
				}
				writer.print(' ');
			}
			writer.print(Global.delimiterLayer);
		}
		writer.println();
		writer.flush();
	}
}
