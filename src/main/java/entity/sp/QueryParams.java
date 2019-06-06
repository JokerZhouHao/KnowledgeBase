package entity.sp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElementDecl.GLOBAL;

import entity.OptMethod;
import utility.Global;
import utility.TimeUtility;

public class QueryParams {
	public int DEFAULT_DATE_SPAN = 300; 	// 对于不带时间的关键词，默认时间差
	public int searchType = 0;
	public int testSampleNum = 500;
	public int radius = 1;
	public int testK = 10;
	public int numWid = 518314;
	public int MAX_PN_LENGTH = 100000;
	public int maxDateSpan = 1000;
	public int MAX_WORD_FREQUENCY = 1000;
	public int DATE_RANGE = 7;
	public OptMethod optMethod = OptMethod.O5;
	public RunRecord rr = new RunRecord();
	public int curRecIndex = 1;	
	
	public long startTime = 0;
	
	public QueryParams() {}
	
	public QueryParams(String line) {
		init(line);
	}
	
	private void init(String line) {
		List<String> params = new ArrayList<>();
		String args[] = line.split(Global.delimiterLevel2);
		for(String st : args) {
			st = st.trim();
			if(st.isEmpty())	continue;
			params.add(st);
		}
		args = new String[params.size()];
		for(int i=0; i<args.length; i++)	args[i] = params.get(i);
		
		// 格式化参数
		searchType = Integer.parseInt(args[0]);
		testSampleNum = Integer.parseInt(args[1]);
		radius = Integer.parseInt(args[2]);
		testK = Integer.parseInt(args[3]);
		numWid = Integer.parseInt(args[4]);
		if(args.length>5) {
			MAX_PN_LENGTH= Integer.parseInt(args[5]);
		}
		if(args.length>6) {
			maxDateSpan = Integer.parseInt(args[6]);
		}
		if(args.length>7) {
			MAX_WORD_FREQUENCY = Integer.parseInt(args[7]);
		}
		if(args.length>8) {
			DATE_RANGE = Integer.parseInt(args[8]);
		}
		if(args.length > 9) {
			int sign = Integer.parseInt(args[9]);
			switch (sign) {
			case 0:
				optMethod = OptMethod.O0;
				break;
			case 1:
				optMethod = OptMethod.O1;
				break;
			case 2:
				optMethod = OptMethod.O2;
				break;
			case 3:
				optMethod = OptMethod.O3;
				break;
			case 4:
				optMethod = OptMethod.O4;
				break;
			case 5:
				optMethod = OptMethod.O5;
				break;
			default:
				break;
			}
		}
	}
	
	public String startInfo(String algName) {
		String st = "开始测试 " + algName + " 样本 " +
				"t=" + String.valueOf(searchType) + " " +
				"ns=" + String.valueOf(testSampleNum) + " " +
				"k=" + String.valueOf(testK) + " " +
				"nw=" + String.valueOf(numWid) + " " + 
				"r=" + String.valueOf(radius) + " " +
				"nwlen=" + String.valueOf(MAX_PN_LENGTH) + " " +   
				"wf=" + String.valueOf(MAX_WORD_FREQUENCY) + " " + 
				"mds=" + String.valueOf(maxDateSpan) + " " + 			
				"dr=" + String.valueOf(DATE_RANGE) + " ";
		if(null != optMethod)	st += optMethod + " ";
		return st + " . . . ";
	}
	
	public String endInfo(String algName) {
		String st = "结束测试" + algName + " 样本  " + 
				"t=" + String.valueOf(searchType) + " " +
				"ns=" + String.valueOf(testSampleNum) + " " +
				"k=" + String.valueOf(testK) + " " +
				"nw=" + String.valueOf(numWid) + " " + 
				"r=" + String.valueOf(radius) + " " +
				"nwlen=" + String.valueOf(MAX_PN_LENGTH) + " " +   
				"wf=" + String.valueOf(MAX_WORD_FREQUENCY) + " " + 
				"mds=" + String.valueOf(maxDateSpan) + " " + 			
				"dr=" + String.valueOf(DATE_RANGE) + " ";
		if(null != optMethod)	st += optMethod + " ";
		return st + "用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis());
	}
	
	public String resultPath(String algName) {
		String st = Global.inputDirectoryPath + Global.testSampleResultFile + "." + algName + "." +  
				"nwlen=" + String.valueOf(MAX_PN_LENGTH) + "."+ 
				"mds=" + String.valueOf(maxDateSpan) + "." + 
				"t=" + String.valueOf(searchType) + "." +
				"ns=" + String.valueOf(testSampleNum) + "." +
				"r=" + String.valueOf(radius) + "." +
				"k=" + String.valueOf(testK) + "." +
				"nw=" + String.valueOf(numWid) + "." + 
				"wf=" + String.valueOf(MAX_WORD_FREQUENCY) + "." +
				"dr=" + String.valueOf(DATE_RANGE);
		if(optMethod != null)	st += "." + optMethod;
		return st + ".csv";
	}
	
	
	public String toString() {
		String st = String.format("%-2d%-4d%-3d%-3d%-2d%-11d%-9d%-9d%-6d", 
				searchType, testSampleNum, testK, numWid, 
				radius, MAX_PN_LENGTH, MAX_WORD_FREQUENCY,
				maxDateSpan, DATE_RANGE);
		if(optMethod != null)	st += optMethod;
		return st;
	}
	
	public static void print(Collection<QueryParams> qps) {
		String out = "";
		out += String.format("%-2s%-4s%-3s%-3s%-2s%-11s%-9s%-9s%-6s%-3s", 
				"t", "ns", "k", "nw", 
				"r", "rlen", "wf",
				"mds", "dr", "O") + "\n";
		for(QueryParams qp : qps) out += qp.toString() + "\n";
		System.out.println(out);
	}
	
	public static void print(QueryParams qp) {
		List<QueryParams> qps = new ArrayList<>();
		qps.add(qp);
		print(qps);
	}
	
	public static void main(String[] args) {
		String line = "0,100,2,1,3,10000000,50000000,1000,7,0";
		QueryParams qp = new QueryParams(line);
//		System.out.println(qp.startInfo("SPBest"));
//		System.out.println(qp.resultPath("SPBest"));
		print(qp);
		
	}
	
}
