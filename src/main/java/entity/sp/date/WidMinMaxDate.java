package entity.sp.date;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sun.nio.ch.IOUtil;
import utility.Global;
import utility.IOUtility;

/**
 * 提供处理每个词的最小和最大日期的相关方法
 * @author Monica
 * @since 2018/6/2
 */
public class WidMinMaxDate {
	class MinMax{
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		
		public void compare(int x) {
			if(min > x)	min = x;
			if(max < x)	max = x;
		}
		
		public int getSpan() {
			return max - min + 1;
		}
		
		public String toString() {
			return String.valueOf(min) + " " + String.valueOf(max);
		}
	}
	
	public void buildingFromFile(String orgFile, String tarFile) throws Exception{
		BufferedReader br = IOUtility.getBR(orgFile);
		String line = null;
		br.readLine();
		
		int index1, index2, i, j, k, nid;
		String tArr[] = null;
		List<Integer> tList = new ArrayList<>();
		
		Map<Integer, MinMax> widMap = new HashMap<>();
		MinMax widMM = new MinMax();
		MinMax tMM = null;
		
		while(null != (line = br.readLine())) {
			tList.clear();
			
			index1 = line.indexOf(Global.delimiterLevel1);
			nid = Integer.parseInt(line.substring(0, index1));
			index1 += Global.delimiterLevel1.length();
			
			index2 = line.lastIndexOf(Global.delimiterDate);
			tArr = line.substring(index1, index2).split(Global.delimiterDate);
			for(String st : tArr) {
				k = Integer.parseInt(st);
				tList.add(k);
			}
			
			index2 += Global.delimiterDate.length();
			tArr = line.substring(index2).split(Global.delimiterLevel2);
			for(String st : tArr) {
				k = Integer.parseInt(st);
				widMM.compare(k);
				if(null == (tMM = widMap.get(k))) {
					tMM = new MinMax();
					widMap.put(k, tMM);
				}
				for(int in : tList) {
					tMM.compare(in);
				}
			}
			
		}
		br.close();
		this.outputResult(tarFile, widMM, widMap);
	}
	
	private void outputResult(String file, MinMax widMM, Map<Integer, MinMax> widMap) throws Exception{
		BufferedWriter bw = IOUtility.getBW(file);
		bw.write(widMM + "\n");
		for(Entry<Integer, MinMax> en : widMap.entrySet()) {
			bw.write(String.valueOf(en.getKey()) + " " + en.getValue() + "\n");
		}
		bw.close();
	}
	
	public static Map<Integer, int[]> loadFromFiel(String fp) {
		System.out.println("> 开始读取文件" + fp + " . . . ");
		Map<Integer, int[]> map = new HashMap<>();
		try {
			BufferedReader br = IOUtility.getBR(fp);
			br.readLine();
			String line = null;
			String[] sArr = null;
			int[] iArr = null;
			while(null != (line = br.readLine())) {
				sArr = line.split(" ");
				iArr = new int[2];
				iArr[0] = Integer.parseInt(sArr[1]);
				iArr[1] = Integer.parseInt(sArr[2]);
				map.put(Integer.parseInt(sArr[0]), iArr);
			}
			br.close();
		} catch (Exception e) {
			System.err.println("> 读取文件" + fp + "失败");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("> 完成读取文件" + fp + " 。");
		return map;
		
	}
	
	public static void main(String[] args) throws Exception{
		
		WidMinMaxDate.loadFromFiel(Global.inputDirectoryPath + Global.widMinMaxDateFile);
		
//		WidMinMaxDate wmmd = new WidMinMaxDate();
//		System.out.println("> starting");
//		wmmd.buildingFromFile(Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile, Global.inputDirectoryPath + Global.widMinMaxDateFile);
//		System.out.println("> end");
	}
}
