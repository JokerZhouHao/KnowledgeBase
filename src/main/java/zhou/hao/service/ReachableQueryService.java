package zhou.hao.service;

import java.io.File;
import java.util.Scanner;

import zhou.hao.tools.LocalFileInfo;

/**
 * @author zhou
 * 两点是否可达查询
 */
public class ReachableQueryService {
	public native void initQuery(int sccN, String ind_filename);
	public native void freeQuery(int sccN);
	public native boolean queryReachable(int p, int q, int sccN);
	
	// 总的点数
	public static int nodeNum = 6; 
	
	public static void main(String[] args) {
		System.loadLibrary("TFLabelReachable");
		ReachableQueryService rqs = new ReachableQueryService();
		System.out.println((LocalFileInfo.getDataSetPath() + "test" + File.separator + "testIndex" + File.separator + "nid_nid" + File.separator + "node").length());
//		System.out.println(LocalFileInfo.getDataSetPath() + "test" + File.separator + "testIndex" + File.separator + "nid_nid" + File.separator + "node");
		rqs.initQuery(nodeNum, LocalFileInfo.getDataSetPath() + "testIndex" + File.separator + "nid_nid" + File.separator + "node");
//		rqs.initQuery(nodeNum, LocalFileInfo.getTFLableBasePath() + "index" + File.separator + "p2p_scc");
		Scanner keyboard = new Scanner(System.in);
		String inputStr = null;
		int p=1, q;
		for(int i=0; i<nodeNum; i++) {
			System.out.print(i + ": ");
			for(int j=0; j<nodeNum; j++) {
				if(i != j && rqs.queryReachable(i, j, nodeNum)) {
					System.out.print(j + " ");
				}
			}
			System.out.println();
		}
		
		while(true) {
			inputStr = keyboard.nextLine();
			if(null != inputStr && !inputStr.equals("")) {
				p = Integer.parseInt(inputStr.split(" ")[0]);
				q = Integer.parseInt(inputStr.split(" ")[1]);
				System.out.println(rqs.queryReachable(p, q, nodeNum));
			} else break;
		}
		
		rqs.freeQuery(nodeNum);
	}
}
