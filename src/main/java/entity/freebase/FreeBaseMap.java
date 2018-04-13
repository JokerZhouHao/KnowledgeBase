package entity.freebase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import file.reader.ZipReader;
import utility.LocalFileInfo;
import utility.MemoryInfo;
import utility.TimeUtility;

/**
 * 
 * @author Monica
 * @since 2017/12/20
 * google-base-map 记录正向和反向的边
 */
public class FreeBaseMap {
	
//	private Integer[] intArr = null;
	private int nodeNum = 0;
	private ArrayList<Integer>[] positiveEdgesArr = null;
	private ArrayList<Integer>[] reverseEdgesArr = null;
	private HashMap<Integer, ArrayList<Integer>> tempReverseEdges = new HashMap<Integer, ArrayList<Integer>>();
	
	public FreeBaseMap(String freeBaseZipPath) {
		this(0, 0);
	}
	
	public FreeBaseMap(int degreeNum, int nodeNum) {
		System.out.println("  > 开始初始化" + nodeNum + "个节点的GFreeBaseMap . . . " + LocalFileInfo.getMemoryAndTime());
		
		String strLine = null;
		String[] strArr = null;
		ArrayList<Integer> tempList = null;
		int i, j;
		String freeBaseZipPath = LocalFileInfo.getEdgeBlankZipPath(degreeNum, nodeNum);
		
		// 初始rEdge
		System.out.println("    > 开始初始化rEdge . . . " + LocalFileInfo.getMemoryAndTime());
		ZipReader zipReader = new ZipReader(freeBaseZipPath, "rEdge");
		reverseEdgesArr = new ArrayList[nodeNum];
		zipReader.readLine();
		for(i=0; i<nodeNum; i++) {
			strLine = zipReader.readLine();
			if(!strLine.equals("")) {
				tempList = new ArrayList<>();
				strArr = strLine.split(" ");
				for(String s : strArr) {
					tempList.add(Integer.parseInt(s));
				}
				reverseEdgesArr[i] = tempList;
			}
		}
		zipReader.close();
		System.out.println("    > 完成rEdge的初始化工作！！！" + LocalFileInfo.getMemoryAndTime());
		
		
		// 初始化edge
		System.out.println("    > 开始初始化edge . . . " + LocalFileInfo.getMemoryAndTime());
		zipReader = new ZipReader(freeBaseZipPath, "pEdge");
		positiveEdgesArr = new ArrayList[nodeNum];
		zipReader.readLine();
		for(i=0; i<nodeNum; i++) {
			strLine = zipReader.readLine();
			if(!strLine.equals("")) {
				strArr = strLine.split(" ");
				tempList = new ArrayList<>();
				for(String s : strArr) {
					tempList.add(Integer.parseInt(s));
				}
				positiveEdgesArr[i] = tempList;
			}
		}
		zipReader.close();
		System.out.println("    > 完成edge的初始化工作！！！" + LocalFileInfo.getMemoryAndTime());
		System.out.println("  > 完成" + nodeNum + "个节点的GFreeBaseMap初始化工作！！！" + TimeUtility.getTime() + "\n");
	}
	
	// 获得正向边
	public ArrayList<Integer> getPositiveEdges(int nodeId) {
		return positiveEdgesArr[nodeId];
	}
	
	// 获得反向边
	public ArrayList<Integer> getReverseEdges(int nodeId) {
		return reverseEdgesArr[nodeId];
	}
	
	// 打印
	public void display() {
		System.out.println("> show FreeBaseMap --------------------------------");
		for(int i =0; i<nodeNum; i++) {
			System.out.println("nodeId = " + i);
			System.out.print("positiveEdges > ");
			if(null!=positiveEdgesArr[i]) {
				for(Integer in : positiveEdgesArr[i]) {
					System.out.print(in + " ");
				}
			}
			System.out.println();
			
			System.out.print("reverseEdges > ");
			if(null!=reverseEdgesArr[i]) {
				for(Integer in : reverseEdgesArr[i]) {
					System.out.print(in + " ");
				}
			}
			System.out.println("\n");
		}
	}
	
	public static void main(String[] args) {
//		FreeBaseMap map = new FreeBaseMap(LocalFileInfo.getNodeIdAndKeywordAndEdgeZipPath(), 20);
		System.out.println(TimeUtility.getTime());
//		FreeBaseMap map = new FreeBaseMap(LocalFileInfo.getNodeIdAndKeywordAndEdgeZipPath(), 2000000);
//		FreeBaseMap map = new FreeBaseMap(LocalFileInfo.getBasePath() + "orginal_code\\data\\testedges.zip");
		System.out.println(TimeUtility.getTime());
//		map.display();
	}
}
