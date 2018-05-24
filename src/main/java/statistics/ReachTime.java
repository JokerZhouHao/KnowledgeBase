package statistics;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import file.reader.GZIPReader;
import utility.Global;

public class ReachTime {
	
	public static void getReachTimeNoZero() throws Exception{
		GZIPReader gr = new GZIPReader(Global.fileReachGZip);
		gr.open();
		int index;
		long reacheTime = 0;
		String line = null;
		long count = 0;
		long res1 = 0;
		long res2 = 0;
		while(null != (line = gr.readLine())) {
			count++;
			res2 += Long.parseLong(line.substring(line.lastIndexOf(' ') + 1));
			if(res1 > res2) {
				System.out.println("long不能放下所有的时间和！");
				break;
			}
			res1 = res2;
		}
		gr.close();
		System.out.println("count: " + count);
		System.out.println("res1: " + res1);
		System.out.println("res2: " + res2);
		System.out.println("平均时间： " + (res1/count));
	}
	
	public static void main(String[] args) throws Exception{
		ReachTime.getReachTimeNoZero();
	}

}
