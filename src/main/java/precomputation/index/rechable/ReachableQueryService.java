package precomputation.index.rechable;

import java.io.File;
import java.util.Map;
import java.util.Scanner;

import utility.Global;
import utility.TFlabelUtility;
import utility.LocalFileInfo;

/**
 * @author zhou
 * 两点是否可达查询
 */
public class ReachableQueryService {
	private native void initQuery(int sccN, String ind_filename);
	private native void freeQuery(int sccN);
	private native boolean queryReachable(int p, int q, int sccN);
	
	private Map<Integer, Integer> vertexSCCMap = null;
	
	/**
	 * 初始化
	 * @param sccFilePath
	 * @param indexPath
	 */
	public ReachableQueryService(String sccFilePath, String indexPath) {
		System.out.println("> 初始化TF_lable index . . . ");
		try {
			vertexSCCMap = TFlabelUtility.loadVertexSCCMap(sccFilePath);
			System.loadLibrary("TFLabelReachable");
			this.initQuery(Global.numSCCs, indexPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("> 初始化TF_lable index ending ! ! ! ");
	}
	
	/**
	 * 查询可达性
	 * @param p
	 * @param q
	 * @return
	 */
	public boolean queryReachable(int p, int q) {
		if(p < Global.numNodes) {
			if(q < Global.numNodes) {
				return this.queryReachable(vertexSCCMap.get(p), vertexSCCMap.get(q), Global.numSCCs);
			} else {
				return this.queryReachable(vertexSCCMap.get(p), q, Global.numSCCs);
			}
		} else return Boolean.FALSE;
	}
	
	/**
	 * 释放内存
	 */
	public void freeQuery() {
		this.freeQuery(Global.numSCCs);
	}
	
	/**
	 * 测试TF-label
	 */
	public void display() {
		System.out.println("> test TF-label . . . ");
		for(int i=0; i<Global.numNodes; i++) {
			System.out.print(i + ": ");
			for(int j=0; j<Global.numSCCs; j++) {
				if(i != j && this.queryReachable(i, j))
					System.out.print(j + " ");
			}
			System.out.println();
		}
	}
	
	/**
	 * 主方法
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		String sccFile = LocalFileInfo.getDataSetPath() + "test/edgeYagoVB.SCC";
		String indexPath = LocalFileInfo.getDataSetPath() + "testIndex" + File.separator + "nid_nid" + File.separator;
		ReachableQueryService rqs = new ReachableQueryService(sccFile, indexPath);
		rqs.display();
		
		// 测试
		Scanner keyboard = new Scanner(System.in);
		String inputStr = null;
		int p=1, q;
		while(true) {
			inputStr = keyboard.nextLine();
			if(null != inputStr && !inputStr.equals("")) {
				p = Integer.parseInt(inputStr.split(" ")[0]);
				q = Integer.parseInt(inputStr.split(" ")[1]);
				System.out.println(rqs.queryReachable(p, q));
			} else break;
		}
		rqs.freeQuery();
	}
}
