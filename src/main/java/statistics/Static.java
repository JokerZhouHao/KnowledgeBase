package statistics;

import java.io.BufferedReader;
import java.io.FileReader;

import utility.Global;

public class Static {
	// 统计所用时间分布
	public static void staticTotalTime(String fPath) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(fPath));
		String line = null;
		int[] rec = new int[6];
		int x = 0;
		while(null != (line = br.readLine())) {
			if(line.length()>0 && !line.contains(Global.delimiterPound) && line.charAt(0)>='0' && line.charAt(0)<='9') {
//				System.out.println(line.split(" ")[9]);
				x = (Integer.parseInt(line.split(" ")[9]) - 1)/5;
				if(x < 0)	x = 0;
				else if(x > 5) x = 5;
				rec[x]++;
			}
		}
		br.close();
		
		for(int i=0; i<rec.length; i++) {
			if(i==0) {
				System.out.println("0--5 : " + rec[i]);
			} else if(i==rec.length - 1) {
				System.out.println(">30 : " + rec[i]);
			} else {
				System.out.println(String.valueOf(i*5 + 1) + "--" + String.valueOf((i+1)*5) + " : " + rec[i]);
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
		String fPath = Global.inputDirectoryPath + "10.100.testSampleResultFile";
		Static.staticTotalTime(fPath);
		
		
//		System.out.println((int)'9');
//		int[] rec = new int[6];
//		for(int i=0; i<rec.length; i++) {
//			System.out.println(rec[i]);
//		}
	}
}
