/**
 * 
 */
package precomputation.graph;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;

import utility.Global;
import utility.GraphUtility;
import utility.TFlabelUtility;
import utility.Utility;
import zhou.hao.tools.LocalFileInfo;

/**
 * @author jmshi 
 * 
 */
public class TFlabelDataFormatter {
	
	public static void buildSCC(String edgeFile, String sccFile) throws Exception{
		System.out.println("> 开始构造" + edgeFile + "的SCC文件 . . .");
		long start = System.currentTimeMillis();
		DirectedGraph<Integer, DefaultEdge> graph = GraphUtility.buildSimpleDirectedGraph(edgeFile);
		StrongConnectivityInspector<Integer, DefaultEdge> scc = 
				new StrongConnectivityInspector<Integer, DefaultEdge>(graph);
		List<Set<Integer>> sccs = scc.stronglyConnectedSets();
		
		Utility<Integer, Integer> uti = new Utility<Integer, Integer>();
		uti.outputListOfSets(sccFile, sccs);
		
		long end = System.currentTimeMillis();
		System.out.println("> 结束构造" + edgeFile + "的SCC文件, Revision Minutes: " + ((end - start) / 1000.0f) / 60.0f);
	}
	
	public static void tfLabelDateFormat(String DAGedgeFile, String sccFile, String edgeFile, String nidDocFile) throws Exception{
		System.out.println("> 开始构造" + DAGedgeFile + "文件 . . .");
		long start = System.currentTimeMillis();
		
		Map<Integer, Integer> vertexSCCMap = TFlabelUtility.loadVertexSCCMap(sccFile);

		// input edge file and converted it to DAG with scc as vertex
		Map<Integer, Set<Integer>> DAGedges = TFlabelUtility.convertToDAG(edgeFile, vertexSCCMap);

		// input: documents of vertices. Augment each keyword as a vertex into the DAG graph
		TFlabelUtility.augmentKeywordsToDAG(nidDocFile, vertexSCCMap, DAGedges);
		Global.numSCCs = Global.numNodes + Global.numKeywords;
		
		Utility<Integer, Integer> uti = new Utility<Integer, Integer>();
		uti.outputMapOfSetsTFLabelFormat(DAGedgeFile, DAGedges, Global.numSCCs);
		
		long end = System.currentTimeMillis();
		System.out.println("> 结束构造" + DAGedgeFile + "文件。Revision Minutes: " + ((end - start) / 1000.0f) / 60.0f);
	}
	
	public static void main(String[] args) throws Exception {
		String DAGedgeFile = LocalFileInfo.getDataSetPath() + "test/" + Global.dagFile + Global.sccFlag
				+ Global.keywordFlag + Global.edgeFile;
		String sccFile = LocalFileInfo.getDataSetPath() + "test/edgeYagoVB.SCC";
		String edgeFile = LocalFileInfo.getDataSetPath() + "test/edgeYagoVB.txt";
		String nidDocFile = LocalFileInfo.getDataSetPath() + "test/" + "nidKeywordsListMapYagoVB.txt";
		TFlabelDataFormatter.buildSCC(edgeFile, sccFile);
		TFlabelDataFormatter.tfLabelDateFormat(DAGedgeFile, sccFile, edgeFile, nidDocFile);
	}
}
