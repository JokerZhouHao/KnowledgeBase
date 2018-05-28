package precomputation.rechable;

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
	
	public ReachableQueryService() {}
	
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
		int pp = vertexSCCMap.get(p);
		int qq = 0;
		if(q < Global.numNodes) {
			qq = vertexSCCMap.get(q);
		} else qq = q;
		
		if(p < Global.numNodes) {
			Boolean res = Boolean.FALSE;
			Global.timeRecTemp = System.currentTimeMillis();
			res = this.queryReachable(pp, qq, Global.numSCCs);
			Global.timeRecTemp1 = System.currentTimeMillis();
			try {
				Global.recReachBW.write(String.valueOf(Global.curRecIndex + 1) + " " + String.valueOf(p) + " " + String.valueOf(q) + " " + String.valueOf(System.currentTimeMillis() - Global.timeRecTemp) + '\n');
				Global.timeRecReachable += System.currentTimeMillis() - Global.timeRecTemp1;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
			return res;
//			if(q < Global.numNodes) {
//				return this.queryReachable(vertexSCCMap.get(p), vertexSCCMap.get(q), Global.numSCCs);
//			} else {
//				return this.queryReachable(vertexSCCMap.get(p) , q, Global.numSCCs);
//			}
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
//		ReachableQueryService rqs = new ReachableQueryService();
//		String indexPath = LocalFileInfo.getDataSetPath() + "testIndex" + File.separator + Global.indexTFLabel;
//		System.loadLibrary("TFLabelReachable");
//		int numScc = 8;
//		rqs.initQuery(numScc, indexPath);
//		System.out.println("> test TF-label . . . ");
//		for(int i=0; i<numScc; i++) {
//			System.out.print(i + ": ");
//			for(int j=0; j<numScc; j++) {
//				if(i != j && rqs.queryReachable(i, j, numScc))
//					System.out.print(j + " ");
//			}
//			System.out.println();
//		}
//		rqs.freeQuery(numScc);
		
		
		
		String sccFile = Global.outputDirectoryPath + Global.sccFile;
		String indexPath = Global.outputDirectoryPath + Global.indexTFLabel;
		ReachableQueryService rqs = new ReachableQueryService(sccFile, indexPath);
		int p, q = 0;
		String line = null;
		Scanner scan = new Scanner(System.in);
		while(null != (line = scan.nextLine()) && !line.isEmpty()) {
			long start = System.currentTimeMillis();
			p = Integer.parseInt(line.split(",")[0]);
			q = Integer.parseInt(line.split(",")[1]);
			rqs.queryReachable(p, q);
			System.out.println(System.currentTimeMillis() - start);
		}
		rqs.freeQuery();
		
//		rqs.display();
		
		// 测试
//		Scanner keyboard = new Scanner(System.in);
//		String inputStr = null;
//		int p=1, q;
//		while(true) {
//			inputStr = keyboard.nextLine();
//			if(null != inputStr && !inputStr.equals("")) {
//				p = Integer.parseInt(inputStr.split(" ")[0]);
//				q = Integer.parseInt(inputStr.split(" ")[1]);
//				System.out.println(rqs.queryReachable(p, q));
//			} else break;
//		}
	}
}
