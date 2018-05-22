package statistics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import utility.Global;

public class Static {
	// 统计所用时间分布
	public static void staticTotalTime() throws Exception{
		String fPath = Global.inputDirectoryPath + "08.if13.else.10.100.testSampleResultFile";
		BufferedReader br = new BufferedReader(new FileReader(fPath));
		
		String line = null;
		int[] rec = new int[6];
		int x = 0;
		int ss = 0;
		while(null != (line = br.readLine())) {
			if(line.length()>0 && !line.contains(Global.delimiterPound) && line.charAt(0)>='0' && line.charAt(0)<='9') {
				System.out.println(line.split(" ")[15]);
				x = (Integer.parseInt(line.split(" ")[15]) - 1)/5;
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
	
	// 输出所用总时间
	public static void printTotal() throws Exception{
		ArrayList<String> pathList = new ArrayList<>();
		pathList.add(Global.inputDirectoryPath + "07.if12.else.now.10.100.testSampleResultFile");
		pathList.add(Global.inputDirectoryPath + "08.if13.else.10.100.testSampleResultFile");
//		pathList.add(Global.inputDirectoryPath + "09.if13.else2.10.100.testSampleResultFile");
		
		ArrayList<BufferedReader> brList = new ArrayList<>();
		for(String st : pathList) {
			brList.add(new BufferedReader(new FileReader(st)));
		}
		String lineArr[] = new String[pathList.size()];
		int count = 1;
		while(null != (lineArr[0] = brList.get(0).readLine())) {
			for(int i=1; i<pathList.size(); i++) {
				lineArr[i] = brList.get(i).readLine();
			}
			Boolean sign = Boolean.FALSE;
			for(String line : lineArr) {
				if(line.length()>0 && !line.contains(Global.delimiterPound) && line.charAt(0)>='0' && line.charAt(0)<='9') {
					if(!sign) {
						System.out.print((count++) + " : ");
					}
					sign = Boolean.TRUE;
					System.out.print(line.split(" ")[15] + " ");
				}
			}
			if(sign)	System.out.println();
		}
		for(BufferedReader br : brList) {
			br.close();
		}
	}
	
	public static void main(String[] args) throws Exception{
//		Static.printTotal();
		Static.staticTotalTime();
//		String fPath = Global.inputDirectoryPath + "07.if12.else.10.100.testSampleResultFile";
//		Static.staticTotalTime(fPath);
		
//		System.out.println((int)'9');
//		int[] rec = new int[6];
//		for(int i=0; i<rec.length; i++) {
//			System.out.println(rec[i]);
//		}
	}
}
