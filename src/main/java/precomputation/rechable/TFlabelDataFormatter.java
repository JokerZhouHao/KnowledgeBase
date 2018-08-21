/**
 * 
 */
package precomputation.rechable;

import java.io.File;
import java.nio.file.DirectoryNotEmptyException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;

import utility.Global;
import utility.GraphUtility;
import utility.TFlabelUtility;
import utility.TimeUtility;
import utility.Utility;
import utility.LocalFileInfo;

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
		System.out.println("> 结束构造" + edgeFile + "的SCC文件, 用时：" + TimeUtility.getSpendTimeStr(start, end));
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
		System.out.println("> 结束构造" + DAGedgeFile + "文件, 用时：" + TimeUtility.getSpendTimeStr(start, end));
	}
	
	public static void build() throws Exception{
		if(!new File(Global.inputDirectoryPath).exists()) {
			throw new DirectoryNotEmptyException("目录inputDirectoryPath ： " + Global.inputDirectoryPath + "不存在");
		}
		if(!new File(Global.outputDirectoryPath).exists()) {
			throw new DirectoryNotEmptyException("目录outputDirectoryPath ： " + Global.outputDirectoryPath + "不存在");
		}
		if(!new File(Global.outputDirectoryPath + Global.indexTFLabel).exists()) {
//			throw new DirectoryNotEmptyException("存放TF-label的目录 ： " + Global.outputDirectoryPath + Global.indexTFLabel + "不存在");
			new File(Global.outputDirectoryPath + Global.indexTFLabel).mkdir();
		}
		
		long startTime = System.currentTimeMillis();
		
		String DAGedgeFile = Global.outputDirectoryPath + Global.DAGFile;
		String sccFile = Global.outputDirectoryPath + Global.sccFile;
		String edgeFile = Global.inputDirectoryPath + Global.edgeFile;
//		String nidDocFile = Global.inputDirectoryPath + Global.nodeIdKeywordListFile;
		String nidDocFile = Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile;
		
		System.out.println("> 开始构造创建TF-label索引所需的" + sccFile + "和" + nidDocFile + " . . . ");
		TFlabelDataFormatter.buildSCC(edgeFile, sccFile);
		TFlabelDataFormatter.tfLabelDateFormat(DAGedgeFile, sccFile, edgeFile, nidDocFile);
		System.out.println("> 完成构造创建TF-label索引所需的" + sccFile + "和" + nidDocFile + "，用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()));
		
		// test
//		System.out.println(Global.outputDirectoryPath + Global.indexTFLabel);
//		ReachableQueryService ser = new ReachableQueryService(sccFile, Global.outputDirectoryPath + Global.indexTFLabel);
//		ser.display();
//		ser.freeQuery();
	}
	
	public static void main(String[] args) throws Exception {
		TFlabelDataFormatter.build();
	}
}
