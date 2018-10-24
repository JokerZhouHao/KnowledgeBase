/**
 * 
 */
package entity.sp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import utility.IOUtility;
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
	
	private int[] recKeyVectices = new int[3];
	private double[] recKeyDis = new double[3];
	private int[] recCandVectices= new int[3];
	private double[] recCandDis= new double[3];
	private List<Integer> recCanWidIndex = new ArrayList<>();
	private List<Integer> recSearchWidIndex = new ArrayList<>();
	private List<Integer> recOkWidIndex = new ArrayList<>();
	private static int signNone = -1;
	
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
	
	public int[] getEdge(int nid) {
		return adjLists[nid];
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
	 * 单个时间
	 * @param source
	 * @param qwords
	 * @param loosenessThreshold
	 * @param vertexQwordsMap
	 * @param semanticTree
	 * @return
	 * @throws Exception
	 */
	public double getSemanticPlaceP(int source, int[] sortQwords, int date, double loosenessThreshold, DatesWIds searchedDatesWids[],
			int[][] wordMinDateSpans, int[] pid2WidPathDis, List<List<Integer>> semanticTree) throws Exception {

		if (sortQwords.length == 0) {
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
		
		int i, j, k, t;
		if(sortQwords.length != recKeyVectices.length) {
			recKeyVectices = new int[sortQwords.length];
			recKeyDis = new double[sortQwords.length];
			recCandVectices= new int[sortQwords.length];
			recCandDis= new double[sortQwords.length];
		}
		for(i=0; i<sortQwords.length; i++) {
			recKeyVectices[i] = signNone;
			recKeyDis[i] = signNone;
			recCandVectices[i] = signNone;
			recCandDis[i] = signNone;
		}
		recSearchWidIndex.clear();
		recCanWidIndex.clear();
		recOkWidIndex.clear();
		for(i=0; i<sortQwords.length; i++)	recSearchWidIndex.add(i);
		
		int qwordsNum = sortQwords.length;
		int numCurFindedWid = 0;
		int numCandWid = 0;
		
		List<Integer> tempList = new ArrayList<>();
		int tempWids[] = null;
		DatesWIds dateWid = null;
		Double d1 = null;
		
		preceder[source] = -1;
		distance2Source[source] = 1;
		visitedFlag[source] = source;
		
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(source);
		double preRadius = 0;
		int vertex = 0;
		double currentRadius = 0;
		Boolean isFound = Boolean.FALSE;
		
//		int numAccessNid = 0;
		
		
		while (!queue.isEmpty()) {
			vertex = queue.poll();
			
			///////////////////////////////////////////////////
//			numAccessNid++;
			
			currentRadius = distance2Source[vertex];
			if (currentRadius != preRadius) {
				preRadius = currentRadius;
				// 计算新层下的looseness
				if((numCurFindedWid + numCandWid) != qwordsNum) {
					looseness = 0;
					
					for(int in : recSearchWidIndex) {
						if(null==pid2WidPathDis) looseness += (wordMinDateSpans[in][0] * currentRadius);
						else looseness += (wordMinDateSpans[in][0] * (pid2WidPathDis[in]>=currentRadius?pid2WidPathDis[in]:currentRadius));
					}
					
					for(int in : recOkWidIndex) {
						looseness += recKeyDis[in];
					}
					
					if(looseness >= loosenessThreshold) {
						if(Global.isTest) {
							Global.rr.numCptPruneInSemanticTree++;
						}
						return Double.POSITIVE_INFINITY;
					}
				}
				// 计算那些候选节点可成为tree里面的节点
				tempList.clear();
				for(int candWidIndex : recCanWidIndex) {
					if(recCandDis[candWidIndex] <= currentRadius * wordMinDateSpans[candWidIndex][0]) {
						recKeyVectices[candWidIndex] = recCandVectices[candWidIndex];
						recKeyDis[candWidIndex] = recCandDis[candWidIndex];
						recOkWidIndex.add(candWidIndex);
						numCurFindedWid++;
						
						recSearchWidIndex.remove((Object)candWidIndex);
						
						recCandVectices[candWidIndex] = signNone;
						recCandDis[candWidIndex] = signNone;
						tempList.add(candWidIndex);
						numCandWid--;
						
						if(qwordsNum == numCurFindedWid) {
							isFound = Boolean.TRUE;
							break;
						}
						
					}
				}
				
				if(!tempList.isEmpty()) {
					for(int in : tempList) {
						recCanWidIndex.remove((Object)in);
					}
				}
				
				if(isFound)	break;
			}
			
			if(null != (dateWid = searchedDatesWids[vertex])){
				tempWids = dateWid.getWids();
				
				k = Integer.MIN_VALUE;
				double disMSpan = 0;
				j = 0;
				
				tempList.clear();
				for(int searchedWidIndex : recSearchWidIndex) {
					t = sortQwords[searchedWidIndex];
					if(t == tempWids[searchedWidIndex]) {
						if(k == Integer.MIN_VALUE) {
							k = TimeUtility.getMinDateSpan(date, dateWid.getDateList());
						}
						disMSpan = currentRadius * k;
						if(wordMinDateSpans[searchedWidIndex][0] == k) {
							recKeyVectices[searchedWidIndex] = vertex;
							recKeyDis[searchedWidIndex] = disMSpan;
							recOkWidIndex.add(searchedWidIndex);
							numCurFindedWid++;
							
							if(recCandVectices[searchedWidIndex] != signNone) {
								recCandVectices[searchedWidIndex] = signNone;
								recCandDis[searchedWidIndex] = signNone;
								recCanWidIndex.remove((Object)searchedWidIndex);
								numCandWid--;
								
							}
							
							tempList.add(searchedWidIndex);
							
							if(numCurFindedWid == qwordsNum) {
								isFound = Boolean.TRUE;
								break;
							}
						} else {
							if(signNone != (d1 = recCandDis[searchedWidIndex])) {
								if(d1 > disMSpan) {
									recCandVectices[searchedWidIndex] = vertex;
									recCandDis[searchedWidIndex] = disMSpan;
								}
							} else {
								recCandVectices[searchedWidIndex] = vertex;
								recCandDis[searchedWidIndex] = disMSpan;
								recCanWidIndex.add(searchedWidIndex);
								numCandWid++;
							}
						} 
					}
				}
				for(int in : tempList)	recSearchWidIndex.remove((Object)in);
			}
			
			if(isFound)	break;
			
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
		
		///////////////////////////////////////////////////
//		if(loosenessThreshold!=Double.POSITIVE_INFINITY) {
//			System.out.print("GetSemanticPlaceP >  " + 
//								"loosenessThreshold = " + String.valueOf(loosenessThreshold) + "    " + 
//								"looseness=" + String.valueOf(looseness) + "    " + 
//								"numAccessNid=" + String.valueOf(numAccessNid) + "    " + 
//								"queue.isEmpty=" + String.valueOf(queue.isEmpty()) + "    " +  
//								"isFound=" + String.valueOf((numCurFindedWid + numCandWid) == qwordsNum) + "    ");
//		}
		
		if(numCurFindedWid + numCandWid != qwordsNum) {
			return Double.POSITIVE_INFINITY;
		}
		
		
		
//		System.out.println(recSearchWidIndex);
//		for(int in : recKeyVectices) {
//			System.out.print(in + " ");
//		}
//		System.out.println();
//		
//		System.out.println(recCanWidIndex);
//		for(int in : recCandVectices) {
//			System.out.print(in + " ");
//		}
//		System.out.println();
//		
//		System.out.println(recOkWidIndex);
//		for(int in : recKeyVectices) {
//			System.out.print(in + " ");
//		}
//		System.out.println();
//		System.exit(0);
		
		
		for(int in : recCanWidIndex) {
			recKeyVectices[in] = recCandVectices[in];
			recKeyDis[in] = recCandDis[in];
		}
		
		// compute semantic tree paths
		looseness = 0;
		for(i=0; i<qwordsNum; i++) {
			looseness += recKeyDis[i];
			semanticTree.add(this.getPath(source, recKeyVectices[i]));
		}
		
		////////////////////////////////////////////////////////
//		if(loosenessThreshold!=Double.POSITIVE_INFINITY) {
//			System.out.println("looseness of keys = " + String.valueOf(looseness));
//		}
		
		if(looseness <= loosenessThreshold)	return looseness;
		else return Double.POSITIVE_INFINITY;
	}
	
	/**
	 * 范围查找计算
	 * @param source
	 * @param sortQwords
	 * @param sDate
	 * @param eDate
	 * @param loosenessThreshold
	 * @param dateWIdMap
	 * @param wordMinDateSpanMap
	 * @param semanticTree
	 * @return
	 * @throws Exception
	 */
	public double getSemanticPlaceP(int source, int[] sortQwords, int sDate, int eDate, double loosenessThreshold, DatesWIds searchedDatesWids[],
			List<List<Integer>> semanticTree) throws Exception {

		if (sortQwords.length == 0) {
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
		int i;
		
		if(sortQwords.length != recKeyVectices.length) {
			recKeyVectices = new int[sortQwords.length];
			recKeyDis = new double[sortQwords.length];
		}
		for(i=0; i<sortQwords.length; i++) {
			recKeyVectices[i] = signNone;
			recKeyDis[i] = signNone;
		}
		recSearchWidIndex.clear();
		recOkWidIndex.clear();
		for(i=0; i<sortQwords.length; i++)	recSearchWidIndex.add(i);
		
		
		int numQwords = sortQwords.length;
		int numFindedWid = 0;
		
		List<Integer> tempList = new ArrayList<>();
		int[] tempWids = null;
		DatesWIds dateWid = null;
		
		preceder[source] = -1;
		distance2Source[source] = 1;
		visitedFlag[source] = source;
		
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(source);
		double preRadius = 0;
		int vertex = 0;
		double currentRadius = 0;
		double delt = 0;
		
		while (!queue.isEmpty()) {
			vertex = queue.poll();
			currentRadius = distance2Source[vertex];
			if (currentRadius != preRadius) {
				delt = currentRadius - preRadius;
				preRadius = currentRadius;
				// 计算新层下的looseness
				looseness += delt * recSearchWidIndex.size();
				if(looseness >= loosenessThreshold) {
					if(Global.isTest) {
						Global.rr.numCptPruneInSemanticTree++;
					}
					return Double.POSITIVE_INFINITY;
				}
			}
			
			if(null != (dateWid = searchedDatesWids[vertex])) {
				tempWids = dateWid.getWids();
				tempList.clear();
				for(int searchWidIndex : recSearchWidIndex) {
					if(sortQwords[searchWidIndex]==tempWids[searchWidIndex]) {
						recKeyVectices[searchWidIndex] = vertex;
						recKeyDis[searchWidIndex] = currentRadius;
						recOkWidIndex.add(searchWidIndex);
						tempList.add(searchWidIndex);
						numFindedWid++;
					}
				}
				
				for(int in : tempList) {
					recSearchWidIndex.remove((Object)in);
				}
			}
			if(numFindedWid == numQwords)	break;
			
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

		// compute semantic tree paths
		if(numFindedWid != numQwords) {
			return Double.POSITIVE_INFINITY;
		}
		
		for(i=0; i<numQwords; i++) {
			semanticTree.add(this.getPath(source, recKeyVectices[i]));
		}
		
		if(looseness <= loosenessThreshold)	return looseness;
		else return Double.POSITIVE_INFINITY;
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
		System.out.println("> 开始读取文件" + edgefile + " . . .");
		// add edges
		String line;
		int cntlines = 0;
		BufferedReader reader = Utility.getBufferedReader(edgefile);

		int countEdges = 0;
		String[] adjListStr;
		String[] adjVerticesStr;
		while ((line = reader.readLine()) != null) {
			cntlines++;
			if (cntlines % 1000000 == 0) {
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
		System.out.println("> 完成读取文件" + edgefile + " . . .");
	}
	
	/**
	 * 创建记录每个节点度的文件
	 * @param degreeFileName
	 * @throws Exception
	 */
	public void buildDegreeFile(String degreeFileName) throws Exception{
		System.out.println("> 开始创建文件" + degreeFileName);
		BufferedWriter bw = IOUtility.getBW(degreeFileName);
		for(int i=0; i<Global.numNodes; i++) {
			if(null == this.getEdge(i)) bw.write(String.valueOf(i) + Global.delimiterLevel1 + String.valueOf(0) + "\n");
			else bw.write(String.valueOf(i) + Global.delimiterLevel1 + String.valueOf(this.getEdge(i).length) + "\n");
		}
		bw.close();
		System.out.println("> Over");
	}
	
	
	/**
	 * For test purpose
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		GraphByArray graph = new GraphByArray(Global.numNodes);
		String edgefile = Global.inputDirectoryPath + Global.edgeFile;
		graph.loadGraph(edgefile);
		String degreeFile = Global.outputDirectoryPath + Global.degreeFile;
		graph.buildDegreeFile(degreeFile);
		
		
		
//		if (args.length != 2) {
//			System.out.println("usage:runnable configfile edgefile");
//			System.exit(-1);
//		}
//		// Utility.loadInitialConfig(args[0]);
//		Global.numNodes = 8;
//		GraphByArray graph = new GraphByArray(Global.numNodes);
//		String edgefile = args[1];
//		graph.loadGraph(edgefile);
	}
}
