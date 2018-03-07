package zhou.hao.service;

import org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.TimeStr;

/**
 * 
 * @author Monica
 * @since 2017/12/08
 * 功能：使用JGraphT包构建nodeMap，并提供一些方法
 */
public class NodeJGraphTService {
	DirectedWeightedMultigraph<Integer, DefaultWeightedEdge> nodeMap = new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);
	BidirectionalDijkstraShortestPath<Integer, DefaultWeightedEdge> shortestPathSeacher = null;
	
	public NodeJGraphTService() {
		init();
	}
	
	// 初始化构建图
	public void init() {
		ZipReaderService reader = new ZipReaderService(LocalFileInfo.getNodeIdAndKeywordAndEdgeZipPath(), "edge");
		
		String lineStr = reader.readLine();
		int nodeNum = Integer.parseInt(lineStr.split("#")[0]);
		int i = 0;
		
		System.out.println("> 构图开始时间：" + TimeStr.getTime());
		Long startTime = System.currentTimeMillis();
		// 初始化点
		System.out.println("> 开始构造点：" + TimeStr.getTime());
		for(i=0; i<nodeNum; i++) {
			nodeMap.addVertex(i);
			if(i%13000000==0) System.out.println("> 已添加" + (i+1) + "个点");
		}
		System.out.println("> 构造点结束：" + TimeStr.getTime() + ", 用时" + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()));
		
		// 初始化边
		System.out.println("> 开始构造边：" + TimeStr.getTime());
		DefaultWeightedEdge edge = null;
		for(i=0; i<2; i++) {
			lineStr = reader.readLine();
			if(!lineStr.equals("")) {
				String[] edgeStrArr = lineStr.split(",");
				for(String s : edgeStrArr) {
//					System.err.println(s);
					edge = nodeMap.addEdge(i, Integer.parseInt(s));
					nodeMap.setEdgeWeight(edge, 1);
				}
			}
			if(i%13000000==0) System.out.println("> 已添加" + (i+1) + "个点的边");
		}
		System.out.println("> 边构造完成，构图完成：" + TimeStr.getTime() + ", 用时" + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()));
		shortestPathSeacher = new BidirectionalDijkstraShortestPath<>(nodeMap);
	}
	
	// 求最短路径距离，注意：如果没有最短路径会返回Double.POSITIVE_INFINITY
	public Double getShortestByDijkstra(int source, int sink) {
		return shortestPathSeacher.getPathWeight(source, sink);
	}
	
	
	
	public static void main(String[] args) {
		NodeJGraphTService nodeMapService = new NodeJGraphTService();
		
	}
	
}
