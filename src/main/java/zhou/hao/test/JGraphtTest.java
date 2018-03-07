package zhou.hao.test;

import java.util.Set;

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath;
import org.jgrapht.graph.*;

public class JGraphtTest {
	public static void main(String[] args) {
		DirectedWeightedMultigraph<Integer, DefaultWeightedEdge> nodeMap = new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);
		
//		nodeMap.addVertex(1);
//		nodeMap.addVertex(2);
//		nodeMap.addVertex(3);
//		nodeMap.addVertex(4);
//		nodeMap.addVertex(5);
//		nodeMap.addVertex(6);
//		nodeMap.addVertex(7);
//		
//		DefaultWeightedEdge edge = null;
		
//		edge = nodeMap.addEdge(1, 3);
//		nodeMap.setEdgeWeight(edge, 1);
//		
//		edge = nodeMap.addEdge(2, 3);
//		nodeMap.setEdgeWeight(edge, 1);
//		
//		edge = nodeMap.addEdge(3, 2);
//		nodeMap.setEdgeWeight(edge, 1);
//		edge = nodeMap.addEdge(3, 7);
//		nodeMap.setEdgeWeight(edge, 1);
//		edge = nodeMap.addEdge(3, 1);
//		nodeMap.setEdgeWeight(edge, 1);
//		edge = nodeMap.addEdge(3, 4);
//		nodeMap.setEdgeWeight(edge, 1);
//
//		edge = nodeMap.addEdge(4, 3);
//		nodeMap.setEdgeWeight(edge, 1);
//		edge = nodeMap.addEdge(4, 5);
//		nodeMap.setEdgeWeight(edge, 1);
//		
//		edge = nodeMap.addEdge(5, 4);
//		nodeMap.setEdgeWeight(edge, 1);
//		edge = nodeMap.addEdge(5, 6);
//		nodeMap.setEdgeWeight(edge, 1);
//		
//		edge = nodeMap.addEdge(6, 5);
//		nodeMap.setEdgeWeight(edge, 1);

		nodeMap.addVertex(1);
		nodeMap.addVertex(2);
		nodeMap.addVertex(3);
		nodeMap.addVertex(4);
		nodeMap.addVertex(5);
		
		DefaultWeightedEdge edge = null;
		edge = nodeMap.addEdge(1, 2);
		nodeMap.setEdgeWeight(edge, 1);
		edge = nodeMap.addEdge(1, 4);
		nodeMap.setEdgeWeight(edge, 1);
//		edge = nodeMap.addEdge(1, 3);
//		nodeMap.setEdgeWeight(edge, 1);
		
//		edge = nodeMap.addEdge(2, 3);
//		nodeMap.setEdgeWeight(edge, 1);
		
		edge = nodeMap.addEdge(3, 2);
		nodeMap.setEdgeWeight(edge, 1);
		
//		edge = nodeMap.addEdge(4, 5);
//		nodeMap.setEdgeWeight(edge, 1);
		
		edge = nodeMap.addEdge(5, 3);
		nodeMap.setEdgeWeight(edge, 1);
		
		BidirectionalDijkstraShortestPath<Integer, DefaultWeightedEdge> searcher = new BidirectionalDijkstraShortestPath<>(nodeMap);
		System.out.println(searcher.getPathWeight(1, 3)==Double.POSITIVE_INFINITY);
		
//		Set<Integer> vSet = nodeMap.vertexSet();
//		System.err.println("vSet --- >");
//		for(Integer in : vSet) {
//			System.out.println(in);
//		}
		
//		System.out.println(nodeMap.toString());
//		Set<DefaultWeightedEdge> edgeSet = nodeMap.edgeSet();
//		
//		System.out.println("edgeSet --- >");
//		for(DefaultWeightedEdge in : edgeSet) {
//			System.out.println(in);
//		}
//		System.out.println(nodeMap.getEdgeWeight(1));
	}
}
