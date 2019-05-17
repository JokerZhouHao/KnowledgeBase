package entity.sp;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import entity.sp.AllDateWidNodes.DWid;
import utility.Global;
import utility.LoopQueue;

/**
 * 带有wid的graph
 * @author ZhouHao
 * @since 2019年5月13日
 */
public class GraphWithWids {
	private static GraphByArray graph = null;
	private static Map<Integer, DWid> allDW = null;
	
	protected short[] distance2Source;
	protected int[] visitedFlag;
	
	public GraphWithWids() throws Exception{
		this.distance2Source = new short[Global.numNodes];
		this.visitedFlag = new int[Global.numNodes];
		for (int i = 0; i < this.visitedFlag.length; i++) {
			this.visitedFlag[i] = -1;
		}
		
		graph = new GraphByArray(Global.numNodes);
		graph.loadGraph(Global.inputDirectoryPath + Global.edgeFile);
		allDW = AllDateWidNodes.loadFromFile(Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile);
	}
	
	/**
	 * 获得nids可达的wid
	 * @param nids
	 * @return
	 * @throws Exception
	 */
	public Set<Integer>	getConnectWids(Set<Integer> nids) throws Exception{
		Set<Integer> wids = new HashSet<>();
		Set<Integer> accessNids = new HashSet<>();
		
		LoopQueue<Integer> queue = new LoopQueue<>(500000);
		DWid tDWid = null;
		int j;
		int[] adjList = null;
		Integer nid = null;
		
		for(Integer source : nids) {
			if(accessNids.contains(source))	continue;
			
			// 重置bfs需要的数据
			queue.reset();
			
			// 初始化队列
			queue.push(source);
			accessNids.add(source);
			
			// bfs
			while(null != (nid = queue.poll())) {
				// 遍历访问的点
				if(null != (adjList = this.graph.getEdge(nid))) {
					for (j = 0; j < adjList.length; j++) {
						int adjVertex = adjList[j];
						if (visitedFlag[adjVertex] != source && !accessNids.contains(adjVertex)) {
							// not visited yet
							accessNids.add(adjVertex);	// 添加碰到的点
							
							distance2Source[adjVertex] = (short)(1 + distance2Source[nid]);
							if(distance2Source[adjVertex] < 0) {
								throw new Exception("distance2Source[adjVertex]大于short最大值");
							}
							visitedFlag[adjVertex] = source;
							if(!queue.push(adjVertex)) {
								throw new Exception("> 队列" + queue.size() + "太短");
							}
						}
					}
				}
				
				if(null != (tDWid = allDW.get(nid))) {
					for(Integer w : tDWid.wids)	wids.add(w);
				}
			}
		}
		return wids;
	}
	
	public static void main(String[] args) throws Exception{
		GraphWithWids graph = new GraphWithWids();
		Set<Integer> nids = new HashSet<>();
		nids.add(15762);
		nids.add(100);
		
		Set<Integer> wids = graph.getConnectWids(nids);
		
		System.out.println(wids.size());
	}
}
