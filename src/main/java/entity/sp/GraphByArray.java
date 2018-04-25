/**
 * 
 */
package entity.sp;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.tartarus.snowball.ext.LovinsStemmer;

import entity.sp.NidToDateWidIndex;
import entity.sp.NidToDateWidIndex.DateWid;
import queryindex.VertexQwordsMap;
import utility.Global;
import utility.MComparator;
import utility.TimeUtility;
import utility.Utility;

/**
 * Unweighted directed graph with vertex id starting from 0 to the end
 * The graph is implemented by arrays.
 * 
 * @author jmshi 
 */
public class GraphByArray {
	protected int[][] adjLists;
	protected int numVertices;
	protected int[] preceder;
	protected int[] distance2Source;
	// Given a source vertex, if a vertex is visited, the vertex's visitedFlag is set to the source vertex id
	protected int[] visitedFlag;
	
	public GraphByArray(int numNodes) {
		this.numVertices = numNodes;
		this.adjLists = new int[numNodes][];
		this.preceder = new int[numNodes];
		this.distance2Source = new int[numNodes];
		this.visitedFlag = new int[numNodes];
		for (int i = 0; i < this.visitedFlag.length; i++) {
			this.visitedFlag[i] = -1;
		}
	}

	/**
	 * compute the alpha doc of vertex in BFS mode.
	 * */
	public PlaceRadiusNeighborhood alphaRadiusOfVertex(int vid, Integer radius, NidToDateWidIndex nidToDateWidIndex)
			throws IOException {
		PlaceRadiusNeighborhood radiusWN = new PlaceRadiusNeighborhood(radius);
		Queue<Integer> queue = new LinkedList<Integer>();
		int source = vid;
		preceder[vid] = -1;
		distance2Source[vid] = 0;
		visitedFlag[vid] = source;
		queue.add(vid);
		DateWid containedDateWid = null;
		
		while (!queue.isEmpty()) {
			int vertex = queue.poll();

			containedDateWid = nidToDateWidIndex.getDateWid(vertex);

			if (containedDateWid != null) {
				radiusWN.addDateWid(distance2Source[vertex], containedDateWid);;
			}
			
			// add the unvisited adj vertices of vertex into queue only
			// if the unvisited adj vertices with distance <= radius
			if (distance2Source[vertex] + 1 <= radius) {
				int[] adjList = this.adjLists[vertex];
				if (adjList == null) {
					// there is no out-going edge from vertex, dead end,
					// continue to next vertex
					continue;
				}
				for (int i = 0; i < adjList.length; i++) {
					int adjVertex = adjList[i];
					if (visitedFlag[adjVertex] != source) {
						// not visited yet
						preceder[adjVertex] = vertex;
						distance2Source[adjVertex] = 1 + distance2Source[vertex];
						visitedFlag[adjVertex] = source;
						queue.add(adjVertex);
					}
				}
			}
			
		}
		// this.reset();
		return radiusWN;
	}

	/**
	 * For BSP to get the looseness score and also the semantic tree rooted at source
	 * @param source
	 * @param qwords
	 * @param vertexQwordsMap
	 * @param semanticTree
	 * @return
	 * @throws Exception
	 */
	public double getSemanticPlaceB(int source, Integer[] qwords, VertexQwordsMap<Integer> vertexQwordsMap,
			List<List<Integer>> semanticTree) throws Exception {
		double looseness = 1.0;
		Set<Integer> keyVertices = new HashSet<Integer>();

		if (qwords.length == 0) {
			throw new IllegalArgumentException("must provide at least one query keyword");
		}

		Set<Integer> qwordsCopy = new HashSet<Integer>();
		for (int i = 0; i < qwords.length; i++) {
			qwordsCopy.add(qwords[i]);
		}

		if (source < 0 || source >= this.numVertices) {
			throw new Exception("source id is out of range, " + source + " should be in [0,"
					+ (this.numVertices - 1) + "]");
		}

		preceder[source] = -1;
		distance2Source[source] = 0;
		visitedFlag[source] = source;

		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(source);

		while (!queue.isEmpty()) {
			int vertex = queue.poll();
			Set<Integer> qwordsContainedByVertex = vertexQwordsMap.vertexQwordsMap.get(vertex);

			if (qwordsContainedByVertex != null && qwordsContainedByVertex.size() > 0) {
				// vertex contains some query keywords.
				int prevQwordSize = qwordsCopy.size();
				qwordsCopy.removeAll(qwordsContainedByVertex);
				int currQwordSize = qwordsCopy.size();
				if (prevQwordSize > currQwordSize) {
					// vertex is a key-vertex of keywords not discovered yet
					looseness += (prevQwordSize - currQwordSize) * distance2Source[vertex];
					keyVertices.add(vertex);
					if (qwordsCopy.size() == 0) {
						break;
					}
				}
			}

			// add the unvisited adj vertices of vertex into queue
			int[] adjList = this.adjLists[vertex];
			if (adjList == null) {
				// there is no out-going edge from vertex, dead end, continue to next vertex
				continue;
			}
			for (int i = 0; i < adjList.length; i++) {
				int adjVertex = adjList[i];
				if (visitedFlag[adjVertex] != source) {
					// not visited yet
					preceder[adjVertex] = vertex;
					distance2Source[adjVertex] = 1 + distance2Source[vertex];
					visitedFlag[adjVertex] = source;
					queue.add(adjVertex);
				}
			}
		}

		if (qwordsCopy.size() > 0) {
			looseness = Double.POSITIVE_INFINITY;
		}

		// compute semantic tree paths
		if (looseness != Double.POSITIVE_INFINITY) {
			for (Integer keyVertex : keyVertices) {
				semanticTree.add(this.getPath(source, keyVertex));
			}
		}
		return looseness;
	}

	/**
	 * For SPP to get the looseness score and also the semantic tree rooted at source
	 * @param source
	 * @param qwords
	 * @param loosenessThreshold
	 * @param vertexQwordsMap
	 * @param semanticTree
	 * @return
	 * @throws Exception
	 */
	public double getSemanticPlaceP(int source, ArrayList<Integer> qwords, int date, double loosenessThreshold, Map<Integer, DateWId> dateWIdMap,
			HashMap<Integer, Integer> wordMinDateSpanMap, List<List<Integer>> semanticTree) throws Exception {

		if (qwords.size() == 0) {
			throw new IllegalArgumentException("must provide at least one query keyword");
		}
		
		if (loosenessThreshold < 0) {
			throw new IllegalArgumentException("radius limitation must be >= 0");
		}

		if (source < 0 || source >= this.numVertices) {
			throw new Exception("source id is out of range, " + source + " should be in [0,"
					+ (this.numVertices - 1) + "]");
		}
		
		double looseness = 0;
		Map<Integer, Integer> recKeyVecticesMap = new HashMap<>();
		Map<Integer, Double> recKeyDisMap = new HashMap<>();
		Map<Integer, Integer> recCandVecticesMap = new HashMap<>();
		Map<Integer, Double> recCandDisMap = new HashMap<>();
		ArrayList<Integer> sortedQwordsList = new ArrayList<>(qwords);
		sortedQwordsList.sort(new MComparator<Integer>());
		int qwordsNum = sortedQwordsList.size();
		List<Integer> tempList = new ArrayList<>();
		DateWId dateWid = null;
		int i, j, k, t;
		Double d1 = null;
		
		preceder[source] = -1;
		distance2Source[source] = 1;
		visitedFlag[source] = source;
		
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(source);
		double preRadius = 0;
		int candidateNum = 0;
		
		while (!queue.isEmpty()) {
			int vertex = queue.poll();
			double currentRadius = distance2Source[vertex];
			if (currentRadius != preRadius) {
				preRadius = currentRadius;
				// 计算新层下的looseness
				if(candidateNum != qwordsNum) {
					looseness = 0;
					for(Integer in : sortedQwordsList) {
						d1 += wordMinDateSpanMap.get(in);
					}
					looseness += d1 * currentRadius;
					for(Double doub : recKeyDisMap.values()) {
						looseness += doub;
					}
					if(looseness >= loosenessThreshold) {
						return Double.POSITIVE_INFINITY;
					}
				}
				// 计算那些候选节点可成为tree里面的节点
				tempList.clear();
				for(Entry<Integer, Double> en : recCandDisMap.entrySet()) {
					if(en.getValue() <= currentRadius * wordMinDateSpanMap.get(en.getKey())) {
						recKeyVecticesMap.put(en.getKey(), recCandVecticesMap.get(en.getKey()));
						recKeyDisMap.put(en.getKey(), recCandDisMap.get(en.getKey()));
						if(qwordsNum == recKeyVecticesMap.size())	break;
						recCandVecticesMap.remove(en.getKey());
						tempList.add(en.getKey());
					}
				}
				if(!tempList.isEmpty()) {
					for(Integer in : tempList) {
						recCandDisMap.remove(in);
					}
				}
			}
			
			dateWid = dateWIdMap.get(vertex);
			tempList = dateWid.getDateList();
			k = Integer.MIN_VALUE;
			Boolean isFound = Boolean.FALSE;
			double disMSpan = 0;
			j = 0;
			for(i=0; i<tempList.size(); i++) {
				for(; j<sortedQwordsList.size(); j++) {
					if(tempList.get(i) == (t = sortedQwordsList.get(j))) {
						if(k == Integer.MIN_VALUE) {
							k = TimeUtility.getMinDateSpan(date, dateWid.getDateList());
						}
						disMSpan = currentRadius * k;
						if(wordMinDateSpanMap.get(t) == k) {
							recKeyVecticesMap.put(t, vertex);
							recKeyDisMap.put(t, disMSpan);
							recCandDisMap.remove(t);
							recCandVecticesMap.remove(t);
							sortedQwordsList.remove((Object)t);
							j--;
							if(recKeyVecticesMap.size() == qwordsNum) {
								isFound = Boolean.TRUE;
								break;
							}
						} else {
							if(null != (d1 = recCandDisMap.get(t))) {
								if(d1 > disMSpan) {
									recCandDisMap.put(t, disMSpan);
									recCandVecticesMap.put(t, vertex);
								}
							} else {
								recCandDisMap.put(t, currentRadius * k);
								recCandVecticesMap.put(t, vertex);
							}
						}
					} else if(tempList.get(i) < t) {
						break;
					}
				}
				if(isFound)	break;
			}
			
			// add the unvisited adj vertices of vertex into queue
			int[] adjList = this.adjLists[vertex];
			if (adjList == null) {
				// there is no out-going edge from vertex, dead end, continue to next vertex
				continue;
			}
			for (i = 0; i < adjList.length; i++) {
				int adjVertex = adjList[i];
				if (visitedFlag[adjVertex] != source) {
					// not visited yet
					preceder[adjVertex] = vertex;
					distance2Source[adjVertex] = 1 + distance2Source[vertex];
					visitedFlag[adjVertex] = source;
					queue.add(adjVertex);
				}
			}
		}

		if (sortedQwordsList.size() > 0) {
			return Double.POSITIVE_INFINITY;
		}

		// compute semantic tree paths
		looseness = 0;
		Set<Integer> keyVertices = new HashSet<Integer>();
		for(Entry<Integer, Double> en : recKeyDisMap.entrySet()) {
			keyVertices.add(en.getKey());
			looseness += en.getValue();
		}
		for (Integer keyVertex : keyVertices) {
			semanticTree.add(this.getPath(source, keyVertex));
		}
		return looseness;
	}

	/**
	 * @param sink
	 * @throws Exception
	 */
	public List<Integer> getPath(int source, int sink) throws Exception {
		List<Integer> path = new ArrayList<Integer>();
		path.add(sink);
		int precedeVertex = preceder[sink];
		while (precedeVertex != -1) {
			path.add(precedeVertex);
			precedeVertex = preceder[precedeVertex];
		}
		Collections.reverse(path);
		// String pathStr = "";
		// for (int i = 0; i < path.size(); i++) {
		// pathStr += path.get(i) + ",";
		// }
		// System.out.println(source + "->" + sink + ": " + pathStr);
		if (path.get(0) != source) {
			throw new Exception("shortest path extracted wrongly. " /* + pathStr */);
		}
		return path;
	}

	public void reset() {
		for (int i = 0; i < visitedFlag.length; i++) {
			visitedFlag[i] = -1;
			distance2Source[i] = 0;
			preceder[i] = 0;
		}
	}

	public void loadGraph(String edgefile) throws Exception {
		// add edges
		String line;
		int cntlines = 0;
		BufferedReader reader = Utility.getBufferedReader(edgefile);

		int countEdges = 0;
		String[] adjListStr;
		String[] adjVerticesStr;
		while ((line = reader.readLine()) != null) {
			cntlines++;
			if (cntlines % 10000 == 0) {
				System.out.println("adding vertex with edges " + cntlines);
			}
			if (line.contains(Global.delimiterPound)) {
				continue;
			}
			adjListStr = line.split(Global.delimiterLevel1);
			if (adjListStr.length != 2) {
				throw new Exception(line + " adjlist wrong");
			}
			int vertex = Integer.parseInt(adjListStr[0]);

			adjVerticesStr = adjListStr[1].split(Global.delimiterLevel2);
			int[] adjVertices = new int[adjVerticesStr.length];
			for (int i = 0; i < adjVerticesStr.length; i++) {
				adjVertices[i] = Integer.parseInt(adjVerticesStr[i]);
			}
			this.adjLists[vertex] = adjVertices;

			countEdges += adjVertices.length;
		}
		reader.close();
	}

	/**
	 * For test purpose
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.out.println("usage:runnable configfile edgefile");
			System.exit(-1);
		}
		// Utility.loadInitialConfig(args[0]);
		Global.numNodes = 8;
		GraphByArray graph = new GraphByArray(Global.numNodes);
		String edgefile = args[1];
		graph.loadGraph(edgefile);
	}
}
