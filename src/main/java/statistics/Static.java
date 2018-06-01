package statistics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import utility.Global;

public class Static {
	// 统计所用时间分布和平均时间
	public static void staticTimeDistribute() throws Exception{
		String fPath = Global.inputDirectoryPath + "10.100.testSampleResultFile1";
		ArrayList<Integer> scopeList = new ArrayList<>();
		scopeList.add(0);
		scopeList.add(10);
		scopeList.add(30);
		scopeList.add(60);
		scopeList.add(120);
		ArrayList<Integer> countList = new ArrayList<>();
		for(int i=0; i<scopeList.size(); i++) {
			countList.add(0);
		}
		int sum = 0;
		
		String line = null;
		int x = 0;
		int i = 0;
		BufferedReader br = new BufferedReader(new FileReader(fPath));
		while(null != (line = br.readLine())) {
			if(line.length()>0 && !line.contains(Global.delimiterPound) && line.charAt(0)>='0' && line.charAt(0)<='9') {
				x = Integer.parseInt(line.split(" ")[23]);
				System.out.println(x);
				for(i=0; i<scopeList.size(); i++) {
					if(x < scopeList.get(i))	break;
				}
				countList.set(i-1, countList.get(i-1) + 1);
				if(x > 120)	x = 120;
				sum += x;
			}
		}
		br.close();
		
		for(i=0; i<scopeList.size(); i++) {
			if(i!=scopeList.size()-1)
				System.out.println(scopeList.get(i) + "--" + scopeList.get(i+1) + " : " + countList.get(i));
			else
				System.out.println(scopeList.get(i) + "--  " + " : " + countList.get(i));
			
		}
		System.out.println("avg : " + (sum/100));
	}
	
	// 输出所用总时间
	public static void printAllTotal() throws Exception{
		ArrayList<String> pathList = new ArrayList<>();
		pathList.add(Global.inputDirectoryPath + "10.100.testSampleResultFile1");
		pathList.add(Global.inputDirectoryPath + "10.100.testSampleResultFile2");
		pathList.add(Global.inputDirectoryPath + "10.100.testSampleResultFile3");
		
		ArrayList<Integer> postList = new ArrayList<>();
		postList.add(23);
		postList.add(23);
		postList.add(23);
		
		ArrayList<BufferedReader> brList = new ArrayList<>();
		for(String st : pathList) {
			brList.add(new BufferedReader(new FileReader(st)));
		}
		
		String lineArr[] = new String[pathList.size()];
		int count = 1;
		String line = null;
		while(null != (lineArr[0] = brList.get(0).readLine())) {
			for(int i=1; i<pathList.size(); i++) {
				lineArr[i] = brList.get(i).readLine();
			}
			Boolean sign = Boolean.FALSE;
			for(int i=0; i<lineArr.length; i++) {
				line = lineArr[i];
				if(line.length()>0 && !line.contains(Global.delimiterPound) && line.charAt(0)>='0' && line.charAt(0)<='9') {
					if(!sign) {
						System.out.print((count++) + " : ");
					}
					sign = Boolean.TRUE;
					System.out.print(line.split(" ")[postList.get(i)] + " ");
				}
			}
			if(sign)	System.out.println();
		}
		for(BufferedReader br : brList) {
			br.close();
		}
	}
	
	public static void displayPoint() throws Exception{
		String fPath = Global.inputDirectoryPath + "10.100.testSampleResultFile1";
		ArrayList<Integer> scopeList = new ArrayList<>();
		scopeList.add(0);
		scopeList.add(2);
		scopeList.add(3);
		scopeList.add(4);
		
		
		scopeList.add(10);
		scopeList.add(11);
		scopeList.add(23);
		
		String line = null;
		int x = 0;
		int i = 0;
		String[] strArr = null;
		BufferedReader br = new BufferedReader(new FileReader(fPath));
		while(null != (line = br.readLine())) {
			if(line.length()>0 && !line.contains(Global.delimiterPound) && line.charAt(0)>='0' && line.charAt(0)<='9') {
				strArr = line.split(" ");
				x = Integer.parseInt(line.split(" ")[23]);
				
				if(x>=120) {
					for(i=0; i<scopeList.size(); i++) {
						System.out.print(strArr[scopeList.get(i)]+ " ");
					}
					System.out.println();
				}
			}
		}
		br.close();
	}
	
	public static void main(String[] args) throws Exception{
//		Static.printAllTotal();
		Static.staticTimeDistribute();
		Static.displayPoint();
//		String fPath = Global.inputDirectoryPath + "07.if12.else.10.100.testSampleResultFile";
//		Static.staticTotalTime(fPath);
		
//		System.out.println((int)'9');
//		int[] rec = new int[6];
//		for(int i=0; i<rec.length; i++) {
//			System.out.println(rec[i]);
//		}
	}
}
