package entity.sp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashSet;
import java.util.Set;

import utility.Global;
import utility.IOUtility;
import utility.Utility;

/**
 * 反向图
 * @author ZhouHao
 * @since 2019年5月13日
 */
public class ReverseGraphByArray {
	/**
	 * 读取反向边文件
	 * @param revEdgeFile
	 * @return
	 * @throws Exception
	 */
	public static int[][] load(String revEdgeFile) throws Exception{
		int[][] revAdjList = new int[Global.numNodes][];
		
		System.out.println("> 开始读取文件" + revEdgeFile + " . . .");
		// add edges
		String line;
		int cntlines = 0;
		BufferedReader reader = Utility.getBufferedReader(revEdgeFile);

		int countEdges = 0;
		String[] adjListStr;
		String[] adjVerticesStr;
		while ((line = reader.readLine()) != null) {
			cntlines++;
//			if (cntlines % 1000000 == 0) {
//				System.out.println("adding vertex with edges " + cntlines);
//			}
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
			revAdjList[vertex] = adjVertices;

			countEdges += adjVertices.length;
		}
		reader.close();
		System.out.println("> 完成读取文件" + revEdgeFile + " . . .");
		return revAdjList;
	}
	
	/**
	 * 生成反向边文件
	 * @param path
	 * @throws Exception
	 */
	public static void generateReverseGraph(String revEdgeFile) throws Exception{
		GraphByArray graph = new GraphByArray(Global.numNodes);
		graph.loadGraph(Global.inputDirectoryPath + Global.edgeFile);
		int[][] adjLists = graph.adjLists;
		
		HashSet<Integer>[] revEdges = new HashSet[Global.numNodes];
		HashSet<Integer> tSet = null;
		int[] edge = null;
		
		System.out.println("> 开始反转图");
		// 获得每个点的反向点
		for(int i=0; i<adjLists.length; i++) {
			edge = adjLists[i];
			if(null == edge) continue;
			
			for(int id : edge) {
				if(null == (tSet = revEdges[id])) {
					tSet = new HashSet<>();
					revEdges[id] = tSet;
				}
				revEdges[id].add(i);
			}
			
		}
		
		// 输出反向边文件
		System.out.println("> 开始输出反向图" + revEdgeFile);
		BufferedWriter bw = IOUtility.getBW(revEdgeFile);
		bw.write(String.valueOf(Global.numNodes) + "#\n");
		for(int i=0; i<revEdges.length; i++) {
			tSet = revEdges[i];
			if(null == tSet)	continue;
			
			bw.write(String.valueOf(i));
			bw.write(Global.delimiterLevel1);
			for(Integer id : tSet) {
				bw.write(String.valueOf(id));
				bw.write(Global.delimiterLevel2);
			}
			bw.write('\n');
		}
		bw.close();
		System.out.println("> over");
	}
	
	public static void main(String[] args) throws Exception{
		ReverseGraphByArray.generateReverseGraph(Global.inputDirectoryPath + Global.edgeReverseFile);
		ReverseGraphByArray.load(Global.inputDirectoryPath + Global.edgeReverseFile);
	}
}
