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
	//		if (args.length != 1) {
	//		throw new Exception("Usage: runnable configFile");
	//	}
	//	Utility.loadInitialConfig(args[0]);
	//	String edgeFile = Global.inputDirectoryPath + Global.edgeFile + Global.dataVersion;
//			String edgeFile = LocalFileInfo.getDataSetPath() + "test/edgeYagoVB.txt";
		System.out.println("> 开始构造" + edgeFile + "的SCC文件 . . .");
		long start = System.currentTimeMillis();
		DirectedGraph<Integer, DefaultEdge> graph = GraphUtility.buildSimpleDirectedGraph(edgeFile);
		StrongConnectivityInspector<Integer, DefaultEdge> scc = 
				new StrongConnectivityInspector<Integer, DefaultEdge>(graph);
		List<Set<Integer>> sccs = scc.stronglyConnectedSets();
		
		Utility<Integer, Integer> uti = new Utility<Integer, Integer>();
	//	String outputFile = Global.outputDirectoryPath + Global.edgeFile + ".SCC." + Global.dataVersion;
//			String outputFile = LocalFileInfo.getDataSetPath() + "test/edgeYagoVB.SCC";
		uti.outputListOfSets(sccFile, sccs);
		
		long end = System.currentTimeMillis();
		System.out.println("> 结束构造" + edgeFile + "的SCC文件, Revision Minutes: " + ((end - start) / 1000.0f) / 60.0f);
	}
	
	public static void tfLabelDateFormat(String DAGedgeFile, String sccFile, String edgeFile, String nidDocFile) throws Exception{
//		if (args.length != 1) {
//			throw new Exception("Usage: runnable configFile");
//		}
//		Utility.loadInitialConfig(args[0]);
		// output
//		String DAGedgeFile = Global.outputDirectoryPath + Global.dagFile + Global.sccFlag
//				+ Global.keywordFlag + Global.edgeFile + Global.dataVersion;
//		String DAGedgeFile = LocalFileInfo.getDataSetPath() + "test/" + Global.dagFile + Global.sccFlag
//				+ Global.keywordFlag + Global.edgeFile;

		// input scc file
//		String sccFile = Global.outputDirectoryPath + Global.edgeFile + Global.sccFlag + Global.dataVersion;
//		String sccFile = LocalFileInfo.getDataSetPath() + "test/edgeYagoVB.SCC";
		Map<Integer, Integer> vertexSCCMap = TFlabelUtility.loadVertexSCCMap(sccFile);

		// input edge file and converted it to DAG with scc as vertex
//		String edgeFile = Global.inputDirectoryPath + Global.edgeFile + Global.dataVersion;
//		String edgeFile = LocalFileInfo.getDataSetPath() + "test/edgeYagoVB.txt";
		Map<Integer, Set<Integer>> DAGedges = TFlabelUtility.convertToDAG(edgeFile, vertexSCCMap);

		// input: documents of vertices. Augment each keyword as a vertex into the DAG graph
		System.out.println("> 开始构造" + DAGedgeFile + "文件 . . .");
		long start = System.currentTimeMillis();
//		String nidDocFile = Global.inputDirectoryPath + Global.nidKeywordsListMapFile + Global.dataVersion;
//		String nidDocFile = LocalFileInfo.getDataSetPath() + "test/" + "nidKeywordsListMapYagoVB.txt";
		TFlabelUtility.augmentKeywordsToDAG(nidDocFile, vertexSCCMap, DAGedges);
		
		Utility<Integer, Integer> uti = new Utility<Integer, Integer>();
		uti.outputMapOfSetsTFLabelFormat(DAGedgeFile, DAGedges, (Global.numNodes + Global.numKeywords));
		
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
