/**
 * 
 */
package entity.sp;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import invertedindex.InvertedIndexHash;
import invertedindexmemory.InvertedIndex;
import entity.sp.GraphByArray;;
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
	// inverted index: disk based
	InvertedIndexHash iindex;

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

	public void precomputeAlphaWN(InvertedIndex vidDocMap, double radius) throws Exception {
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
		precomputeAlphaWN(this.m_rootID, vidDocMap, radius, writer, count);
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
	private Map<Integer, Double> precomputeAlphaWN(int nodeid, InvertedIndex vidDocMap,
			double radius, PrintWriter writer, int[] count) throws Exception {
		Node n = readNode(nodeid);
		System.out.println("processing " + count[0] + "th node with id " + n.m_identifier);
		if (n.isLeaf()) {

			Map<Integer, Double> leafAlphaWN = new HashMap<Integer, Double>();
			for (int child = 0; child < n.m_children; child++) {
				// get and output the alpha document of places in the leaf node
				int pid = n.m_pIdentifier[child];
				count[2]++;
				Map<Integer, Double> pidAlphaWN = graph.alphaRadiusOfVertex(pid, radius, vidDocMap);
				this.outputAlphaWN(writer, pid, pidAlphaWN, count);

				// merge the alpha document of places to get the alpha document of the leaf node
				mergeAlphaWN(leafAlphaWN, pidAlphaWN);
			}
			this.outputAlphaWN(writer, (-n.getIdentifier() - 1), leafAlphaWN, count);
			count[0]++;
			return leafAlphaWN;

		} else {
			Map<Integer, Double> nodeAlphaWN = new HashMap<Integer, Double>();
			int child;
			for (child = 0; child < n.m_children; child++) {
				Map<Integer, Double> childAlphaWN = precomputeAlphaWN(n.m_pIdentifier[child],
						vidDocMap, radius, writer, count);
				mergeAlphaWN(nodeAlphaWN, childAlphaWN);
			}
			this.outputAlphaWN(writer, (-n.getIdentifier() - 1), nodeAlphaWN, count);
			count[0]++;
			return nodeAlphaWN;
		}
	}

	/**
	 * @param resultAlphaWN
	 * @param aAlphaWN
	 */
	public void mergeAlphaWN(Map<Integer, Double> resultAlphaWN, Map<Integer, Double> aAlphaWN) {
		for (Entry<Integer, Double> entry : aAlphaWN.entrySet()) {
			int kword = entry.getKey();
			double gdist = entry.getValue();
			if (resultAlphaWN.containsKey(kword)) {
				if (resultAlphaWN.get(kword) > gdist) {
					resultAlphaWN.put(kword, gdist);
				}
			} else {
				resultAlphaWN.put(kword, gdist);
			}
		}
	}

	/**
	 * @param writer
	 * @param n
	 * @param alphaDocOfN
	 */
	private void outputAlphaWN(PrintWriter writer, int vid, Map<Integer, Double> alphaWN, int[] count) {
		writer.print(vid + Global.delimiterSpace);
		if (alphaWN != null && alphaWN.size() > 0) {
			for (Entry<Integer, Double> entry : alphaWN.entrySet()) {
				int kword = entry.getKey();
				double gdist = entry.getValue();
				writer.print(kword + Global.delimiterSpace + gdist + Global.delimiterSpace);
				count[1]++;
			}
		}
		writer.println();
		writer.flush();
	}
}
