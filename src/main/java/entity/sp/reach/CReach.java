package entity.sp.reach;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import precomputation.rechable.ReachableQueryService;
import utility.Global;
import utility.IOUtility;
import utility.LocalFileInfo;
import utility.TFlabelUtility;
import utility.TimeUtility;

/**
 * 用java重写C的可达性测试代码
 * @author zhou
 * @since 2018/6/6
 */
public class CReach {
	private int[] dags = null;
	private int[] topos = null;
	private int[][] TLs = null;
	private int[] tlSizes = null;
	private int sccNum = 0;
	private int twoSccNum = 0;
	
	private Map<Integer, Integer> vertexSCCMap = null;
	
	public CReach(String indexPath, int sccN) {
		System.out.println("> 开始初始化Reach . . . ");
		this.sccNum = sccN;
		this.twoSccNum = sccN * 2;
		init(indexPath);
		System.out.println("> 成功初始化Reach . ");
	}
	
	public CReach(String sccFilePath, String indexPath, int sccN) {
		System.out.println("> 开始初始化Reach . . . ");
		try {
			System.out.println("> 开始初始化SCC . . . ");
			vertexSCCMap = TFlabelUtility.loadVertexSCCMap(sccFilePath);
			System.out.println("> 成功初始化SCC . ");
			this.sccNum = sccN;
			this.twoSccNum = sccN * 2;
			init(indexPath);
		} catch (Exception e) {
			System.err.println("> 加载SCC文件" + sccFilePath + "失败而退出！");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("> 成功初始化Reach . ");
	}
	
	public int C2J_Int( int num ){
		int i0 = num >> 24 & 0xff;
		int i1 = (num >> 8) & 0xff00;
		int i2 = (num << 8) & 0xff0000;
		int i3 = (num << 24) & 0xff000000;
		return i0|i1|i2|i3;
	}
	
	public void init(String fp) {
		dags = new int[sccNum];
		load(fp+"_dag_label", dags);
		topos = new int[sccNum];
		load(fp+"_topo_label", topos);
		tlSizes = new int[sccNum * 2];
		load(fp+"_tlstart", tlSizes);
		TLs = new int[sccNum * 2][];
		loadTL(fp + "_TL");
	}
	
	public void load(String fp, int[] iArr) {
		DataInputStream dis = null;
		try {
			System.out.println("> 开始读取文件" + fp + " . . . ");
			dis = IOUtility.getDis(fp);
			int i = 0;
			while(true) {
				iArr[i++] = C2J_Int(dis.readInt());
			}
		} catch (EOFException e) {
			try {
				dis.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			System.out.println("> 成功读取文件" + fp + " . ");
		} catch (Exception e) {
			System.err.println("> 读取文件" + fp + "失败而退出!");
			e.printStackTrace();
			System.exit(0);
		}
		
	}

	public void loadTL(String fp) {
		DataInputStream dis = null;
		try {
			System.out.println("> 开始读取文件" + fp + " . . . ");
			dis = IOUtility.getDis(fp);
			int ptr1=0, ptr2=0;
			int[] st = null;
			while(ptr1 != twoSccNum) {
				TLs[ptr1] = st = new int[tlSizes[ptr1]];
				do {
					st[ptr2] = C2J_Int(dis.readInt());
				}while((++ptr2)!=tlSizes[ptr1]);
				ptr1++;
				ptr2=0;
			}
		} catch (EOFException e) {
			try {
				dis.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			System.out.println("> 成功读取文件" + fp + " . ");
		} catch (Exception e) {
			System.err.println("> 读取文件" + fp + "失败而退出!");
			e.printStackTrace();
			System.exit(0);
		}
	}

	public Boolean queryNormalReachable(int p, int q) {
		if (p == q) {
			return Boolean.TRUE;
		}
		
		if (dags[p] != dags[q]){    //after callapsing into super node, in the same connected DAG?
			return Boolean.FALSE;
		} else {
			int[] st1 = TLs[p];
			int[] st2 = TLs[q + sccNum];
			int i=0, j=0;
//			if(st1.length > 500 || st2.length > 500) {
//				int j1, mid, k1, k2 = 3;
//				if(st1[0] > st2[0]) {
//					j++;
//					j1 = st2.length - 1;
//					mid = (j+j1)/2;
//					for(k1=0; k1<k2; k1++) {
//						if(st1[0] > st2[mid]) {
//							j = mid + 1;
//						} else if(st1[0] < st2[mid]) {
//							j1 = mid - 1;
//						} else return Boolean.TRUE;
//						mid = (j+j1)/2;
//						if(j >= j1)	break;
//					}
//				} else if(st1[0] < st2[0]) {
//					i++;
//					j1 = st1.length - 1;
//					mid = (i+j1)/2;
//					for(k1=0; k1<k2; k1++) {
//						if(st1[mid] < st2[0]) {
//							i = mid + 1;
//						} else if(st1[mid] > st2[0]) {
//							j1 = mid - 1;
//						} else return Boolean.TRUE;
//						mid = (i+j1)/2;
//						if(i >= j1)	break;
//					}
//				} else 	return Boolean.TRUE;
//			}
			while((i < st1.length) && (j<st2.length)) {
				if(st1[i] < st2[j]) {
					i++;
				} else if(st1[i] > st2[j]) {
					j++;
				} else {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		}
	}
	
	public Boolean queryReachable(int p, int q) {
		if(q < Global.numNodes) {
			return this.queryNormalReachable(vertexSCCMap.get(p), vertexSCCMap.get(q));
		} else {
			return this.queryNormalReachable(vertexSCCMap.get(p) , q);
		}
	}
	
	public static void showReachable() {
		String filePath = LocalFileInfo.getDataSetPath() + "testIndex" + File.separator + "tf_label" + File.separator;
		int sccN = 8;
		CReach cReach = new CReach(filePath, sccN);
		for(int i=0; i<sccN; i++) {
			System.out.print(i + " : ");
			for(int j = 0; j<sccN; j++) {
				if(i != j && cReach.queryNormalReachable(i, j)) {
					System.out.print(j + " ");
				}
			}
			System.out.println();
		}
	}
	
	public static void inputTest() {
		//输入测试
		String sccFile = Global.outputDirectoryPath + Global.sccFile;
		String indexPath = Global.outputDirectoryPath + Global.indexTFLabel;
		CReach reach = new CReach(sccFile, indexPath, Global.numSCCs);
		Scanner sca = new Scanner(System.in);
		String line = null;
		int p, q;
		long start = 0;
		while(true) {
			System.out.print("> 请输入p, q : ");
			line = sca.nextLine();
			p = Integer.parseInt(line.split(",")[0]);
			if(-1==p)	break;
			q = Integer.parseInt(line.split(",")[1]);
			start = System.currentTimeMillis();
			System.out.println(reach.queryReachable(p, q) + " time : " + (System.currentTimeMillis() - start));
		}
	}
	
	public static void main(String[] args) throws Exception{
		CReach.showReachable();
//		CReach.inputTest();
//		System.out.println(LocalFileInfo.getDataSetPath());
		
		/*
		System.out.println("> 开始测试 . . . . " + TimeUtility.getTime());
		String sccFile = Global.outputDirectoryPath + Global.sccFile;
		String indexPath = Global.outputDirectoryPath + Global.indexTFLabel;
		ReachableQueryService rqs = new ReachableQueryService(sccFile, indexPath);
		CReach reach = new CReach(sccFile, indexPath, Global.numSCCs);
		
		String fPath = LocalFileInfo.getDataSetPath() + "reachNoZero2.csv";
		BufferedReader br = IOUtility.getBR(fPath);
		String line = null;
		String sArr[] = null;
		TreeMap<Long, String> tm1 = new TreeMap<>();
		TreeMap<Long, String> tm2 = new TreeMap<>();
		TreeMap<Long, String> tm3 = new TreeMap<>();
		long start = 0;
		Boolean res = null;
		int p, q;
		long time1 = 0, time2=0;
		while(null != (line=br.readLine())) {
			sArr = line.split(",");
			p = Integer.parseInt(sArr[1]);
			q = Integer.parseInt(sArr[2]);
			
			start = System.currentTimeMillis();
			res = rqs.queryReachable(p, q);
			time1 = System.currentTimeMillis() - start;
			
			start = System.currentTimeMillis();
			if(res == reach.queryReachable(p, q)) {
				time2 = System.currentTimeMillis() - start;
			} else {
				System.err.println(sArr[0] + "," + String.valueOf(p) + "," + String.valueOf(q) + ", 结果不一样而退出！");
				System.exit(0);
			}
			
			line = sArr[0] + "," + String.valueOf(p) + "," + String.valueOf(q) + "," 
					+ String.valueOf(res) + "," + sArr[3] + "," + String.valueOf(time1) + "," + String.valueOf(time2);
			tm1.put(Long.parseLong(sArr[3]), line);
			tm2.put(time1, line);
			tm3.put(time2, line);
			
		}
		br.close();
		// 写文件
		TreeMap<Long, String> [] tmA = new TreeMap[3];
		tmA[0] = tm1;
		tmA[1] = tm2;
		tmA[2] = tm3;
		for(int i=0; i<3; i++) {
			BufferedWriter bw = IOUtility.getBW(fPath + String.valueOf(i+1));
			for(String st : tmA[i].values()) {
				bw.write(st);
				bw.write("\n");
			}
			bw.close();
		}
		System.out.println("> 测试结束 . " + TimeUtility.getTime());
		*/
	}
}



























