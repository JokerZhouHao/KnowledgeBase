package utility;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sun.org.apache.regexp.internal.recompile;

import entity.freebase.Pair;

/**
 * 加载文件
 * @author ZhouHao
 * @since 2019年5月18日
 */
public class FileMakeOrLoader {
	/**
	 * 读取keywordIdMapYagoVB.txt
	 * @return
	 * @throws Exception
	 */
	public static Map<Integer, String> loadWid2Word() throws Exception {
		BufferedReader br = IOUtility.getBR(Global.inputDirectoryPath + "keywordIdMapYagoVB.txt");
		String line = null;
		String[] arr = null;
		Map<Integer, String> wid2Word = new HashMap<>();
		while(null != (line = br.readLine())) {
			if(line.contains("#"))	continue;
			arr = line.split(Global.delimiterLevel1);
			wid2Word.put(Integer.parseInt(arr[0]), arr[1]);
		}
		br.close();
		return wid2Word;
	}
	
	/**
	 * 加载数组形式ArrayNid2DatesWids
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static Map<Integer, int[][]> loadArrayNid2DatesWids(String path) throws Exception {
		Map<Integer, int[][]> nidDatesWids = new HashMap<>();
		BufferedReader br = IOUtility.getBR(path);
		int[][] tIntArr = null;
		String[] dates = null;
		String[] wids = null;
		Integer nid, i;
		String line = null;
		String[] strArr = null;
		br.readLine();
		while(null != (line=br.readLine())) {
			tIntArr = new int[2][];
			
			strArr = line.split(Global.delimiterLevel1);
			nid = Integer.parseInt(strArr[0]);
			
			dates = strArr[1].split(Global.delimiterDate);
			tIntArr[0] = new int[dates.length-1];
			for(i=0; i<tIntArr[0].length; i++) {
				tIntArr[0][i] = Integer.parseInt(dates[i]);
			}
			
			wids = dates[dates.length-1].split(Global.delimiterLevel2);
			tIntArr[1] = new int[wids.length];
			for(i=0; i<tIntArr[1].length; i++) {
				tIntArr[1][i] = Integer.parseInt(wids[i]);
			}
			
			nidDatesWids.put(nid, tIntArr);
		}
		br.close();
		return nidDatesWids;
	}
	
	/**
	 * 加载坐标文件
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static Map<Integer, Pair<Double, Double>> loadCoord(String path) throws Exception {
		BufferedReader br = IOUtility.getBR(path);
		br.readLine();
		String line = null;
		Map<Integer, Pair<Double, Double>> id2Coord = new TreeMap<>();
		while(null != (line = br.readLine())) {
			String[] arr = line.split(Global.delimiterLevel1);
			int id = Integer.parseInt(arr[0]);
			arr = arr[1].split(Global.delimiterSpace);
			Pair<Double, Double> pair = new Pair<>(Double.parseDouble(arr[0]), Double.parseDouble(arr[1]));
			id2Coord.put(id, pair);
		}
		br.close();
		return id2Coord; 
	}
	
	/**
	 * 加载数组形式的坐标
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static double[][] loadArrayCoord(String path) throws Exception {
		double[][] pidCoords = new double[Global.numPid][];
		for(int i=0; i<pidCoords.length; i++) {
			pidCoords[i] = new double[2];
		}
		BufferedReader br = IOUtility.getBR(path);
		String line = br.readLine();
		String[] strArr = null;
		int i= 0;
		while(null != (line = br.readLine())) {
			strArr = line.split(Global.delimiterLevel1)[1].split(Global.delimiterSpace);
			pidCoords[i][0] = Double.parseDouble(strArr[0]);
			pidCoords[i][1] = Double.parseDouble(strArr[1]);
			i++;
		}
		br.close();
		return pidCoords;
	}
	
	
	
	/**
	 * 生成记录带有时间关键词的文件
	 * @throws Exception
	 */
	public static void makeWidHasDateFile() throws Exception {
		String pathInput = Global.inputDirectoryPath + "nodeIdKeywordListOnIntDateMapYagoVB.txt";
		String pathOutput = Global.outputDirectoryPath + "widHasDate.bin";
		
		// 读取
		BufferedReader br = IOUtility.getBR(pathInput);
		br.readLine();
		String line = null;
		Set<Integer> wids = new HashSet<>();
		while(null != (line = br.readLine())) {
			String[] arr = line.split(Global.delimiterLevel1)[1].split(Global.delimiterPound);
			if(Integer.parseInt(arr[0]) == Global.TIME_INAVAILABLE)	continue;
			arr = arr[1].split(Global.delimiterLevel2);
			for(String st : arr)	wids.add(Integer.parseInt(st));
		}
		br.close();
		
		// 写出
		DataOutputStream dos = IOUtility.getDos(pathOutput);
		dos.writeInt(wids.size());
		for(int w : wids)	dos.writeInt(w);
		dos.close();
		
		MLog.log("makeWidHasDateFile over");
	}
	
	/**
	 * 加载带有时间的关键词
	 * @return
	 * @throws Exception
	 */
	public static Set<Integer> loadWidHasDate() throws Exception {
		String path = Global.outputDirectoryPath + "widHasDate.bin";
		DataInputStream dis = IOUtility.getDis(path);
		int size = dis.readInt();
		Set<Integer> wids = new HashSet<>();
		for(int i=0; i < size; i++)	
			wids.add(dis.readInt());
		dis.close();
		return wids;
	}
	
	
	
	
	public static Set<Integer> loadAllNid() throws Exception {
		BufferedReader br = IOUtility.getBR(Global.inputDirectoryPath + "edgeYagoVB.txt");
		Set<Integer> nids = new HashSet<>();
		String line = br.readLine();
		while(null != (line = br.readLine())) {
			String[] arr = line.split(Global.delimiterLevel1);
			int nid = Integer.parseInt(arr[0]);
			if(!nids.contains(nid))	nids.add(nid);
			arr = arr[1].split(Global.delimiterLevel2);
			for(String st : arr) {
				nid = Integer.parseInt(st);
				if(!nids.contains(nid))	nids.add(nid);
			}
		}
		br.close();
		MLog.log("nid_num: " + nids.size());
		return nids;
	}
	
	
	public static void main(String[] args) throws Exception {
//		makeWidHasDateFile(pathInput, pathOutput);
//		loadWidHasDate();
		loadAllNid();
	}
	
}
