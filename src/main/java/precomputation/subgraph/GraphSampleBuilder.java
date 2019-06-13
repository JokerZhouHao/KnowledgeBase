package precomputation.subgraph;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import entity.sp.GraphByArray;
import utility.Global;
import utility.IOUtility;
import utility.RandomNumGenerator;

/**
 * 生成不同规模的子图
 * @author Monica
 *
 */
public class GraphSampleBuilder extends GraphSample{
	private GraphByArray graph = null;
	
	private RandomNumGenerator numGen = new RandomNumGenerator(1, 100);
	
	private double jumpProbability = 0.15;
	
	public GraphSampleBuilder(Boolean init) throws Exception{
		if(init)	init();
	}
	
	private void init()throws Exception{
		String edgeFile = Global.inputDirectoryPath + Global.edgeFile + Global.dataVersion;
		this.graph = new GraphByArray(Global.numNodes);
		this.graph.loadGraph(edgeFile);
	}
	
	/**
	 * 按照概率跳出
	 * @return
	 */
	public Boolean jump() {
		if(RandomNumGenerator.getRandomDouble() <= jumpProbability)	return Boolean.TRUE;
		else return Boolean.FALSE;
	}
	
	/**
	 * 输出样本图
	 * @param sampleGraph
	 * @throws Exception
	 */
	public void outputGraph(Set[] sampleGraph) throws Exception{
		Set<Integer> nids = new TreeSet();
		int numEdge = 0;
		for(int i=0; i<sampleGraph.length; i++) {
			if(null != sampleGraph[i]) {
				nids.add(i);
				nids.addAll(sampleGraph[i]);
				numEdge += sampleGraph[i].size();
			}
		}
		
		BufferedWriter bw = IOUtility.getBW(Global.inputDirectoryPath + String.valueOf(nids.size()) + "_RNN_" + Global.edgeFile);
		bw.write(String.valueOf(nids.size()) + "#" + String.valueOf(numEdge) + "#\n");
		for(int nid : nids) {
			bw.write(String.valueOf(nid) + ",");
		}
		bw.write("\n");
		for(int i=0; i<sampleGraph.length; i++) {
			if(null != sampleGraph[i] && !sampleGraph[i].isEmpty()) {
				bw.write(String.valueOf(i) + Global.delimiterLevel1);
				for(Object nid : sampleGraph[i]) {
					bw.write(String.valueOf(nid) + Global.delimiterLevel2);
				}
				bw.write('\n');
			}
		}
		bw.close();
	}
	
	/**
	 * 创建样本图
	 * @param sampleNums
	 * @throws Exception
	 */
	public void buildGraphSample(LinkedList<Integer> sampleNums) throws Exception{
		System.out.println("> 开始生成不同规模的子图  . . . ");
		boolean accessedNode[] = new boolean[Global.numNodes];
		for(int i=0; i<accessedNode.length; i++) {
			accessedNode[i] = false;
		}
		
		Set[] sampleGraph = new Set[accessedNode.length];
		int[] edges = null;
		
		int numCurNode = 0;
		
		
		int nidIndex = -1;
		int curNid = -1;
		
		List<Integer> tList = new ArrayList<>();
		Stack<Integer> stack = new Stack<>();
		
		int i, j, k;
		for(int numThreshold : sampleNums) {
			while(true) {
				
				j = RandomNumGenerator.getRInt(accessedNode.length);
				
				curNid = j;
				if(!accessedNode[curNid]) {
					accessedNode[curNid] = true;
					numCurNode++;
					sampleGraph[curNid] = new HashSet<>();
					if(numCurNode == numThreshold)	break;
				}
				
				edges = graph.getEdge(curNid);
				
				if((null==edges) || (sampleGraph[curNid].size()==edges.length)) continue;
				
				for(int nid : edges) {
					sampleGraph[curNid].add(nid);
					
					if(!accessedNode[nid]) {
						accessedNode[nid] = true;
						numCurNode++;
						sampleGraph[nid] = new HashSet<>();
						if(numCurNode==numThreshold)	break;
					}
				}
				if(numCurNode==numThreshold)	break;
				
				if(numCurNode % 1000 == 0) {
					System.out.println("> 已添加" + String.valueOf(numCurNode) + "个节点");
				}
			}
			if(numCurNode == numThreshold) {
				outputGraph(sampleGraph);
				System.out.println("> 成功生成" + String.valueOf(numThreshold) + "个节点的子图");
			}
		}
		System.out.println("> 成功生成所有子图。");
	}
	
	public static void main(String[] args) throws Exception{
		LinkedList<Integer> sampleNums = new LinkedList<>();
		sampleNums.add(2000000);
		sampleNums.add(4000000);
		sampleNums.add(6000000);
		GraphSampleBuilder gsnc =  new GraphSampleBuilder(Boolean.TRUE);
		gsnc.buildGraphSample(sampleNums);
		gsnc.adjustSampleGraph(sampleNums);
	}
}
