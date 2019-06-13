package precomputation.sample;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import entity.sp.QueryParams;
import utility.Global;
import utility.IOUtility;

/**
 * 用于生成测试输入数据
 * 例如: $type 200 2 $k 3 1000000 50000000 1000
 * 字段对应: type ns r k nw r_len reach_len wf dr opt
 * @author ZhouHao
 * @since 2019年5月18日
 */
public class TestInputDataBuilder {
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
		int rs[] = {1, 2, 3};
		int k = 5;
		int nw = 3;
		int rLens[] = {100000, 1000000, 10000000};
		int maxDataSpan = 50000000;
		int wf = 1000;
		int dr = 7;
		
		List<String> lines = new ArrayList<>();
		for(int type : types) {
			for(int r : rs) {
				for(int rLen : rLens) {
					lines.add(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wf, dr, null));
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
			for(int wf : wfs) {
				lines.add(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wf, dr, "5"));
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
		int r = 2;
		int ks[] = {1, 3, 20, 5, 8, 10};
		int nw = 3;
		int rLen = 1000000;
		int maxDataSpan = 50000000;
		int wf = 1000000;
		int dr = 7;
		
		List<String> lines = new ArrayList<>();
		
		for(int k : ks) {
			for(int type : types) {
				lines.add(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wf, dr, null));
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
		int r = 2;
		int k = 5;
		int nws[] = {1, 3, 5, 2, 4};
		int rLen = 1000000;
		int maxDataSpan = 50000000;
		int wf = 1000000;
		int dr = 7;
		
		List<String> lines = new ArrayList<>();
		
		for(int nw : nws) {
			for(int type : types) {
				lines.add(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wf, dr, null));
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
		int r = 2;
		int k = 5;
		int nw = 3;;
		int rLen = 1000000;
		int maxDataSpan = 50000000;
		int wf = 1000000;
//		int drs[] = {0, 3, 7, 14, 30, 50, 100, 150};
		int drs[] = {0, 7, 50, 100, 3, 14, 30, 150};
		
		List<String> lines = new ArrayList<>();
		for(int dr : drs) {
			lines.add(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wf, dr, null));
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
		int types[] = {0, 1};
		int ns = 200;
		int r = 2;
		int k = 5;
		int nw = 3;;
		int rLen = 100000;
		int maxDataSpan = 200000;
		int wf = 50;
		int dr = 7;
		String opts[] = {"O1", "O2", "O3", "O4"};
		
		List<String> lines = new ArrayList<>();
		for(int type : types) {
			for(String opt : opts) {
				lines.add(getTestString(type, ns, r, k, nw, rLen, maxDataSpan, wf, dr, opt));
			}
		}
		
		for(String line : lines)
			System.out.println(line);
		
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
	
	public static List<QueryParams> loadTestQuery(String path) throws Exception {
		List<QueryParams> list = new ArrayList<>();
		BufferedReader br = IOUtility.getBR(path);
		String line = null;
		while(null != (line = br.readLine())) {
			if(line.startsWith("-"))	break;
			if(line.trim().isEmpty() || line.startsWith("#"))	continue;
			list.add(new QueryParams(line.trim()));
		}
		br.close();
		return list;
	}
	
	public static void main(String[] args) throws Exception{
//		generateRLen();
		generateWf();
//		generateK();
//		generateNW();
//		generateDr();
//		System.out.println(generateOpt());
		
//		String path = Global.inputDirectoryPath + File.separator + "sample_result" + File.separator + "opt.txt";
//		for(String[] sts : loadTestString(path)) {
//			for(String st : sts) {
//				System.out.print(st + " ");
//			}
//			System.out.println();
//		}
		
		List<QueryParams> qps = TestInputDataBuilder.loadTestQuery(path);
//		for(QueryParams q : qps) {
//			System.out.println(q);
//		}
		
		
	}
	
}

