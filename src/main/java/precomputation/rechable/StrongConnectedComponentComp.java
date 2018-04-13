/**
 * 
 */
package precomputation.rechable;


import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;

import utility.Global;
import utility.GraphUtility;
import utility.Utility;
import utility.LocalFileInfo;

/**
 * Compute the Strong Connected Components in RDF graphs by utilizing jgrapht library.
 * @author jmshi
 *
 */
public class StrongConnectedComponentComp {
	
	public static void buildSCC(String edgeFile, String outputFile) throws Exception{
	//		if (args.length != 1) {
	//		throw new Exception("Usage: runnable configFile");
	//	}
	//	Utility.loadInitialConfig(args[0]);
	//	String edgeFile = Global.inputDirectoryPath + Global.edgeFile + Global.dataVersion;
//		String edgeFile = LocalFileInfo.getDataSetPath() + "test/edgeYagoVB.txt";
		System.out.println("> 开始构造" + edgeFile + "的SCC文件 . . .");
		long start = System.currentTimeMillis();
		DirectedGraph<Integer, DefaultEdge> graph = GraphUtility.buildSimpleDirectedGraph(edgeFile);
		StrongConnectivityInspector<Integer, DefaultEdge> scc = 
				new StrongConnectivityInspector<Integer, DefaultEdge>(graph);
		List<Set<Integer>> sccs = scc.stronglyConnectedSets();
		long end = System.currentTimeMillis();
		System.out.println("> 结束构造" + edgeFile + "的SCC文件, Revision Minutes: " + ((end - start) / 1000.0f) / 60.0f);
		
		Utility<Integer, Integer> uti = new Utility<Integer, Integer>();
	//	String outputFile = Global.outputDirectoryPath + Global.edgeFile + ".SCC." + Global.dataVersion;
//		String outputFile = LocalFileInfo.getDataSetPath() + "test/edgeYagoVB.SCC";
		uti.outputListOfSets(outputFile, sccs);
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		StrongConnectedComponentComp.buildSCC( LocalFileInfo.getDataSetPath() + "test/edgeYagoVB.txt", 
				LocalFileInfo.getDataSetPath() + "test/edgeYagoVB.SCC");
	}

}
