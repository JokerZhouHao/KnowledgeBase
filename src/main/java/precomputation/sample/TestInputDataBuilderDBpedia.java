package precomputation.sample;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import entity.OptMethod;
import utility.Global;
import utility.IOUtility;

/**
 * 用于生成测试输入数据
 * 例如: $type 200 2 $k 3 1000000 50000000 1000
 * 字段对应: type ns r k nw r_len reach_len wf dr opt
 * @author ZhouHao
 * @since 2019年5月18日
 */
public class TestInputDataBuilderDBpedia {
	/**
	 * 生成测试字符串
	 * @param type
	 * @param ns
	 * @param r
	 * @param k
	 * @param nw
	 * @param rLen
	 * @param maxDataSpan
	 * @param wf
	 * @param dr
	 * @param opt
	 * @return
	 */
	public static String getTestString(int type, int ns, int r, int k, int nw, int rLen, int maxDataSpan, 
			int wf, int dr, String opt) {
		String str =  String.valueOf(type) + Global.delimiterLevel2 + String.valueOf(ns) + Global.delimiterLevel2 +
				String.valueOf(r) + Global.delimiterLevel2 + String.valueOf(k) + Global.delimiterLevel2 +
				String.valueOf(nw) + Global.delimiterLevel2 + String.valueOf(rLen) + Global.delimiterLevel2 +
				String.valueOf(maxDataSpan) + Global.delimiterLevel2 + String.valueOf(wf) + Global.delimiterLevel2 +
				String.valueOf(dr);
		if(null != opt)	str += Global.delimiterLevel2 + opt;
		return str;
	}
	
	/**
	 * 生成用于测试radius len 的串
	 * @return
	 */
	public static List<String> generateRLen(){
		int types[] = {0, 1};
		int ns = 200;
//		int rs[] = {1, 2, 3};
		int rs[] = {1, 3};
		int k = 5;
		int nw = 3;
//		int rLens[] = {100000, 1000000, 10000000};
		int rLens[] = {100, 1000};
		int maxDataSpan = 50000000;
		int wf = 50;
		int dr = 7;
		
		List<String> lines = new ArrayList<>();
		for(int r : rs) {
			for(int type : types) {
					for(int rLen : rLens) {
						lines.add(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wf, dr, "5"));
					}
			}
		}
		
		for(String line : lines)
			System.out.println(line);
		
		return lines;
	}
	
	/**
	 * 生成测试词频用的串
	 * @return
	 */
	public static List<String> generateWf(){
		int types[] = {0, 1};
		int ns = 200;
		int r = 3;
		int k = 5;
		int nw = 3;
		int rLen = 10000000;
		int maxDataSpan = 300;
		int wfs[] = {100, 250, 500, 1000, 10000, 100000, 1000000};
		int dr = 3;
		
		List<String> lines = new ArrayList<>();
		
		for(int type : types) {
			for(int j = wfs.length - 1; j >= 0; j--) {
				lines.add(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wfs[j], dr, "5"));
			}
		}
		
		for(String line : lines)
			System.out.println(line);
		
		return lines;
	}
	
	/**
	 * 生成测试k的字符串
	 * @return
	 */
	public static List<String> generateK(){
		int types[] = {0, 1};
		int ns = 200;
		int r = 3;
		int ks[] = {20, 15, 10, 8, 5, 3, 1};
//		int ks[] = {1, 3, 4, 5};
		int nw = 3;
		int rLen = 10000000;
		int maxDataSpan = 300;
		int wf = 10000;
		int dr = 3;
		String[] opts = {"0", "5"};
		
		List<String> lines = new ArrayList<>();
		
		for(String opt : opts) {
			for(int type : types) {
				for(int k : ks) {
					lines.add(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wf, dr, opt));
				}
			}
		}
		
		for(String line : lines)
			System.out.println(line);
		
		return lines;
	}
	
	/**
	 * 生成测试nw的串
	 * @return
	 */
	public static List<String> generateNW(){
		int types[] = {0, 1};
		int ns = 200;
		int k = 5;
		int r = 3;
		int rLen = 10000000;
		int wf = 10000;
		int nws[] = {5, 4, 3, 2, 1};
		int maxDataSpan = 300;
		int dr = 3;
		String[] opts = {"0", "5"};
		
		List<String> lines = new ArrayList<>();
		
		for(String opt : opts) {
			for(int type : types) {
				for(int nw : nws) {
					lines.add(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wf, dr, opt));
				}
			}
		}
		
		for(String line : lines)
			System.out.println(line);
		
		return lines;
	}
	
	/**
	 * 生成测试dr的串
	 * @return
	 */
	public static List<String> generateDr(){
		int type = 1;
		int ns = 200;
		int k = 5;
		int r = 3;
		int rLen = 10000000;
		int nw = 3;
		int maxDataSpan = 300;
		int wf = 10000;
//		int drs[] = {0, 3, 7, 14, 30, 50, 100, 150};
		int drs[] = {2, 6, 14, 30, 60, 100, 200, 300};
		String[] opts = {"0", "5"};
		
		List<String> lines = new ArrayList<>();
		for(String opt : opts) {
			for(int dr : drs) {
				lines.add(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wf, (dr - 2)/2, opt));
			}
		}
		
		for(String line : lines)
			System.out.println(line);
		
		return lines;
	}
	
	/**
	 * 生成测试opt串
	 * @return
	 */
	public static List<String> generateOpt(){
		int types[] = {1};
		int ns = 200;
		int r = 3;
//		int ks[] = {1, 3, 5, 8, 10, 15, 20};
		int ks[] = {20, 15, 10, 8, 5, 3, 1};
		int nw = 3;
		int rLen = 10000000;
		int maxDataSpan = 50000000;
		int wf = 100;
		int dr = 7;
		String opts0[] = {"0", "1", "2", "3", "4"};
		String opts1[] = {"0", "2", "3", "4"};
		String opts[] = null;
		
		List<String> lines = new ArrayList<>();
		
		for(int type : types) {
			for(int k : ks) {
				if(type == 0) {
					opts = opts0;
				} else opts = opts1;
				for(String opt : opts) {
					lines.add(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wf, dr, opt));
					System.out.println(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wf, dr, opt));
				}
				System.out.println();
			}
			System.out.println();
		}
		
		return lines;
	}
	
	
	public static List<String> generateDiffSize(){
		int types[] = {0, 1};
		int ns = 200;
		int r = 3;
//		int ks[] = {1, 3, 5, 8, 10, 15, 20};
		int k= 5;
		int nw = 3;
		int rLen = 10000;
		int maxDataSpan = 50000000;
		int wf = 1000;
		int dr = 7;
		String opts[] = {"0", "5"};
		
		List<String> lines = new ArrayList<>();
		
		for(String opt : opts) {
			for(int type : types) {
				lines.add(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wf, dr, opt));
				System.out.println(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wf, dr, opt));
			}
		}
		
		return lines;
	}
	
	/**
	 * 从文件加载测试串
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static List<String[]> loadTestString(String path) throws Exception {
		List<String[]> testStrs = new ArrayList<>();
		List<String> list = new ArrayList<>();
		BufferedReader br = IOUtility.getBR(path);
		String line = null;
		String[] arr = null;
		while(null != (line = br.readLine())) {
			if(line.trim().isEmpty() || line.startsWith("#"))	continue;
			System.out.println(line.trim());
			list.clear();
			arr = line.split(Global.delimiterLevel2);
			for(String st : arr) {
				if(!st.trim().equals("")) {
					list.add(st.trim());
				}
			}
			list.toArray(arr);
			testStrs.add(arr);
		}
		
		return testStrs;
	}
	
	public static void main(String[] args) throws Exception{
//		generateRLen();
		generateWf();
//		generateK();
//		generateNW();
//		generateDr();
//		generateOpt();
//		generateDiffSize();
		
//		String path = Global.inputDirectoryPath + File.separator + "sample_result" + File.separator + "r_len.txt";
//		for(String[] sts : loadTestString(path)) {
//			for(String st : sts) {
//				System.out.print(st + " ");
//			}
//			System.out.println();
//		}
	}
	
}

