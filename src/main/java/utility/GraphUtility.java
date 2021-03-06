/**
 * 
 */
package utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import entity.sp.PlaceRadiusNeighborhood;
import entity.sp.SortedList;
import entity.sp.SortedList.SortedListNode;

/**
 * @author jieming
 *
 */
public class GraphUtility {

	public static DirectedGraph<Integer, DefaultEdge> buildSimpleDirectedGraph(String edgeFile)
			throws Exception {
		
		BufferedReader reader = Utility.getBufferedReader(edgeFile);
		String line = reader.readLine();//first line is statistics of graph
//		String[] stat = line.split(Global.delimiterPound);
//		int numNodes = Integer.parseInt(stat[0]);
//		Global.numNodes = numNodes;
//		int numEdges = Integer.parseInt(stat[1]);
//		Global.numEdges = numEdges;
		int numNode = Global.numNodes;
		
		DirectedGraph<Integer, DefaultEdge> sdgraph = new SimpleDirectedGraph<Integer, DefaultEdge>(
				DefaultEdge.class);

		// we know the ids of all nodes, just simply add them...
		for (int nid = 0; nid < Global.numNodes; nid++) {
			sdgraph.addVertex(nid);
		}

		
		int edgeCount = 0;
		int cntLines = 0;
		while ((line = reader.readLine()) != null) {
			cntLines++;
			String[] nidOutNids = line.split(Global.delimiterLevel1);
			if (nidOutNids.length != 2) {
				throw new Exception("nid->nids direct edge splits wrong length, should be 2 but is "
						+ nidOutNids.length);
			}

			int startV = Integer.parseInt(nidOutNids[0]);

			String[] endVsStr = nidOutNids[1].split(Global.delimiterLevel2);

			int endVi = -1;
			try {
				for (int i = 0; i < endVsStr.length; i++) {
					endVi = Integer.parseInt(endVsStr[i]);
					sdgraph.addEdge(startV, endVi);
					edgeCount++;
					if (edgeCount % 100000 == 0) {
						System.out.println("line " + cntLines + ": " + edgeCount + " edges added");
					}
				}
			} catch (Exception e) {
				System.out.println(startV + " " + endVi + " edge insert error" + line);
				throw e;
			}
		}
		reader.close();

		return sdgraph;
	}
	
}
