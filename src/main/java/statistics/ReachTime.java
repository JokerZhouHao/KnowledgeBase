package statistics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import file.reader.GZIPReader;
import precomputation.rechable.ReachableQueryService;
import utility.Global;
import utility.MComparator;
import utility.TimeUtility;

public class ReachTime {
	
	// 统计
	public static void staticReachTimeNoZero() throws Exception{
		System.out.println("开始处理 . . . ");
		long start = System.currentTimeMillis();
		GZIPReader gr = new GZIPReader(Global.fileReachGZip);
		gr.open();
		int index;
		long reacheTime = 0;
		String line = null;
		long count = 0;
		long countNoZero = 0;
		long res1 = 0;
		long res2 = 0;
		long tempLong = 0;
		while(null != (line = gr.readLine())) {
			count++;
			tempLong = Long.parseLong(line.substring(line.lastIndexOf(' ') + 1));
			if(tempLong > 0) {
				countNoZero++;
				res2 += tempLong;
			}
			if(res1 > res2) {
				System.out.println("long不能放下所有的时间和！");
				break;
			}
			res1 = res2;
		}
		gr.close();
		System.out.println("count: " + count);
		System.out.println("countNoZero: " + countNoZero);
		System.out.println("res1: " + res1);
		System.out.println("res2: " + res2);
		System.out.println("平均时间： " + ((double)res1/countNoZero));
		System.out.println("完成，用时：" + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()));
	}
	
	
	public static class ReachDoublePoint{
		public String doublePoint = null;
		public int reachTime = 0;
		
		public ReachDoublePoint(String dp, int rt) {
			this.doublePoint = dp;
			this.reachTime = rt;
		}
		
		public String toString() {
			return doublePoint;
		}
	}
	
	// 输出非0记录
	public static void outputNoZero() throws Exception{
		System.out.println("开始处理 . . . ");
		long start = System.currentTimeMillis();
		
		long rStart = 0;
		String sccPath = Global.outputDirectoryPath + Global.sccFile;
		String tfLabelIndex = Global.outputDirectoryPath + Global.indexTFLabel;
		ReachableQueryService reachableQuerySer = new ReachableQueryService(sccPath, tfLabelIndex);
		int p, q;
		long rTime = 0;
		
		GZIPReader gr = new GZIPReader(Global.fileReachGZip);
		gr.open();
		int index;
		String line = null;
		int count = 0;
		int countNoZero = 0;
		int res1 = 0;
		int res2 = 0;
		int tempI = 0;
		ArrayList<ReachDoublePoint> li1 = new ArrayList<>();
		ArrayList<ReachDoublePoint> li2 = new ArrayList<>();
		String strArr[] = null;
		while(null != (line = gr.readLine())) {
			count++;
			strArr = line.split(" ");
			p = Integer.parseInt(strArr[1]);
			q = Integer.parseInt(strArr[2]);
			tempI = Integer.parseInt(strArr[3]);
			
			rStart = System.currentTimeMillis();
			reachableQuerySer.queryReachable(p, q);
			rTime = System.currentTimeMillis() - rStart;
			
			strArr[0] = strArr[0] + ',' + String.valueOf(p) + ',' + String.valueOf(q) + ',' + String.valueOf(tempI) + ',' + String.valueOf(rTime);
			
			if(tempI > 0) {
				countNoZero++;
				res2 += tempI;
				li1.add(new ReachDoublePoint(strArr[0], tempI));
			}
			
			if(rTime > 0) {
				li2.add(new ReachDoublePoint(strArr[0], (int)rTime));
			}
			
			if(res1 > res2) {
				System.out.println("int不能放下所有的时间和！");
				break;
			}
			res1 = res2;
		}
		gr.close();
		
		// 写文件
		System.out.println("开始输出文件reachNoZero1.csv . . . ");
		li1.sort(new MComparator<ReachDoublePoint>());
		BufferedWriter bw1 = new BufferedWriter(new FileWriter(Global.outputDirectoryPath + "reachNoZero1.csv"));
		int len = li1.size();
		for(int i = len-1; i>=0; i--) {
			bw1.write(li1.get(i).toString() + '\n');
		}
		bw1.close();
		System.out.println("成功输出文件reachNoZero1.csv . . . ");
		
		System.out.println("开始输出文件reachNoZero2.csv . . . ");
		li2.sort(new MComparator<ReachDoublePoint>());
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(Global.outputDirectoryPath + "reachNoZero2.csv"));
		len = li2.size();
		for(int i = len-1; i>=0; i--) {
			bw2.write(li2.get(i).toString() + '\n');
		}
		bw2.close();
		System.out.println("成功输出文件reachNoZero2.csv . . . ");
		
		System.out.println("count: " + count);
		System.out.println("countNoZero: " + countNoZero);
		System.out.println("tmSize: " + len);
		System.out.println("res1: " + res1);
		System.out.println("res2: " + res2);
		System.out.println("平均时间： " + ((double)res1/countNoZero));
		System.out.println("完成，用时：" + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()));
	}
	
	public static void main(String[] args) throws Exception{
//		ReachTime.staticReachTimeNoZero();
		ReachTime.outputNoZero();
//		ArrayList<ReachDoublePoint> li = new ArrayList<>();
//		li.add(new ReachDoublePoint("12", 12));
//		li.add(new ReachDoublePoint("9", 9));
//		li.sort(new MComparator<ReachDoublePoint>());
//		for(ReachDoublePoint rd : li) {
//			System.out.println(rd);
//		}
	}

}
