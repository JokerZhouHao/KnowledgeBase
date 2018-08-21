package precomputation.sample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import utility.Global;
import utility.IOUtility;

public class GraphSample {
	int numNode = 0;
	String sampleEdgeType = "_RNN_";
	
	private void adjustEdgeFile(String oldPath, String newPath, Map<Integer, Integer> nid2Nid) throws Exception{
		BufferedReader br = IOUtility.getBR(oldPath);
		BufferedWriter bw = IOUtility.getBW(newPath);
		
		String line = null;
		line = br.readLine();
		bw.write(line + "\n");
		br.readLine();
		
		while(null!=(line = br.readLine())) {
			String[] strArr = line.split(Global.delimiterLevel1);
			
			Integer nid = nid2Nid.get(Integer.parseInt(strArr[0]));
			if(null == nid)	continue;
			
			bw.write(String.valueOf(nid) + Global.delimiterLevel1);
			
			strArr = strArr[1].split(Global.delimiterLevel2);
			for(String st : strArr) {
				bw.write(String.valueOf(nid2Nid.get(Integer.parseInt(st))) + Global.delimiterLevel2);
			}
			bw.write('\n');
		}
		br.close();
		bw.close();
	}
	
	public void adjustNodeIdKeywordListOnIntDateFile(String oldPath, String newPath, Map<Integer, Integer> nid2Nid) throws Exception{
		BufferedReader br = IOUtility.getBR(oldPath);
		BufferedWriter bw = IOUtility.getBW(newPath);
		
		int numNode = 0;
		
		String line = null;
		line = br.readLine();
		bw.write("       \n");
		
		while(null != (line = br.readLine())) {
			String[] strArr = line.split(Global.delimiterLevel1);
			
			Integer nid = nid2Nid.get(Integer.valueOf(strArr[0]));
			if(null == nid)	continue;
			
			numNode++;
			bw.write(String.valueOf(nid) + Global.delimiterLevel1);
			bw.write(strArr[1]);
			bw.write('\n');
		}
		br.close();
		bw.close();
		
		RandomAccessFile raf = new RandomAccessFile(newPath, "rw");
		raf.seek(raf.getFilePointer());
		raf.writeBytes(String.valueOf(numNode) + Global.delimiterPound);
		raf.close();
	}
	
	public void adjustPidFile(String oldPath, String newPath, Map<Integer, Integer> nid2Nid) throws Exception{
		BufferedReader br = IOUtility.getBR(oldPath);
		BufferedWriter bw = IOUtility.getBW(newPath);
		
		int numNode = 0;
		
		String line = null;
		line = br.readLine();
		bw.write("          \n");
		
		while(null != (line = br.readLine())) {
			String[] strArr = line.split(Global.delimiterLevel1);
			
			Integer nid = nid2Nid.get(Integer.valueOf(strArr[0]));
			if(null == nid)	continue;
			
			numNode++;
			bw.write(String.valueOf(nid) + Global.delimiterLevel1 + strArr[1] + "\n");
		}
		br.close();
		bw.close();
		
		RandomAccessFile raf = new RandomAccessFile(newPath, "rw");
		raf.seek(raf.getFilePointer());
		raf.writeBytes(String.valueOf(numNode) + Global.delimiterPound);
		raf.close();
	}
	
	public void adjustSampleGraph(LinkedList<Integer> sampleNums) throws Exception{
		System.out.println("> 开始调整样本子图  . . . ");
		while(!sampleNums.isEmpty()) {
			numNode = sampleNums.poll();
			String edgePath = Global.inputDirectoryPath + String.valueOf(numNode) + sampleEdgeType + Global.edgeFile;
			BufferedReader br = IOUtility.getBR(edgePath);
			br.readLine();
			String line = br.readLine();
			String[] strArr = line.split(",");
			Map<Integer, Integer> nid2Nid = new HashMap<>();
			for(int i=0; i<strArr.length; i++) {
				nid2Nid.put(Integer.parseInt(strArr[i]), i);
			}
			br.close();
			
			// 重写边文件
			String oldEdgePath = edgePath;
			String newEdgePath = Global.inputDirectoryPath + String.valueOf(numNode) + sampleEdgeType + "NEW_" + Global.edgeFile;
			adjustEdgeFile(oldEdgePath, newEdgePath, nid2Nid);
			
			// 重写节点属性文件
			String oldNodeAttrPath = Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile;
			String newNodeAttrPath = Global.inputDirectoryPath + String.valueOf(numNode) + sampleEdgeType + Global.nodeIdKeywordListOnIntDateFile;
			adjustNodeIdKeywordListOnIntDateFile(oldNodeAttrPath, newNodeAttrPath, nid2Nid);
			
			// 重写坐标文件
			String oldPidPath = Global.inputDirectoryPath + Global.pidCoordFile;
			String newPidPath = Global.inputDirectoryPath + String.valueOf(numNode) + sampleEdgeType + Global.pidCoordFile;
			adjustPidFile(oldPidPath, newPidPath, nid2Nid);
			
			System.out.println("> 完成处理" + String.valueOf(numNode) + "的子图");
		}
		System.out.println("> Over");
	}
}
