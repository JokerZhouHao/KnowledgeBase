package processor.sp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.IO;

import entity.freebase.Pair;
import utility.FileLoader;
import utility.Global;
import utility.IOUtility;
import utility.MLog;
import utility.TimeUtility;

/**
 * 用于去除placeIdMapYagoVB.txt和nodeIdKeywordListOnIntDateMapYagoVB_所有节点
 * @author ZhouHao
 * @since 2019年5月27日
 */
public class OrgFilePreprocessor {
	public static final double MIN_LAT = -90;
	public static final double MAX_LAT = 90;
	public static final double MIN_LON = -180;
	public static final double MAX_LON = 180;
	
	/**
	 * 过滤掉不合法的坐标
	 * @param pathInput
	 * @param pathOutput
	 * @throws Exception
	 */
	public static void filterCoord(String pathInput, String pathOutput) throws Exception {
		Map<Integer, Pair<Double, Double>> id2Coord = FileLoader.loadCoord(pathInput);
		List<Integer> invalidId = new ArrayList<>();
		for(Entry<Integer, Pair<Double, Double>> en : id2Coord.entrySet()) {
			Pair<Double, Double> coord = en.getValue();
			if(coord.getKey() >= MIN_LAT && coord.getKey() <= MAX_LAT &&
				coord.getValue() >= MIN_LON && coord.getValue() <= MAX_LON)	continue;
			invalidId.add(en.getKey());
		}
		for(Integer id : invalidId) {
			System.out.println(id + " " + id2Coord.get(id));
		}
		MLog.log("filterCoord over");
	}
	
	/**
	 * 过滤掉不在时间范围内的节点
	 * @param pathInput
	 * @param pathOutput
	 * @throws Exception
	 */
	public static void filterTime(String pathInput, String pathOutput) throws Exception {
		BufferedReader br = IOUtility.getBR(pathInput);
		BufferedWriter bw = IOUtility.getBW(pathOutput);
		String line = null;
		
		bw.write("                   \n");
		int num = 0, noAvailableNum = 0;
		while(null != (line = br.readLine())) {
			if(line.endsWith(Global.delimiterPound))	continue;
			int time = Integer.parseInt(line.split(Global.delimiterLevel1)[1].split(Global.delimiterPound)[0]);
			if(TimeUtility.noLegedDate(time)) {
				noAvailableNum++;
				continue;
			}
			num++;
			bw.write(line + "\n");
		}
		br.close();
		bw.close();
		IOUtility.writeFirstLine(pathOutput, String.valueOf(num + Global.delimiterPound));
		
		MLog.log("无效时间节点数: " + noAvailableNum);
		MLog.log("有效时间节点数: " + num);
		MLog.log("filterTime over");
	}
	
	/**
	 * 生成不带时间的关键词文件
	 * @param pathD
	 * @param pathK
	 * @param pathNoDK
	 * @throws Exception
	 */
	public static void buildNoDateNidKeywordsListFile(String pathD, String pathK, String pathNoDK) throws Exception {
		// 获得哪些词带有时间
		BufferedReader br = IOUtility.getBR(pathD);
		br.readLine();
		String line = null;
		Set<String> widHasDate = new HashSet<>();
		while(null != (line = br.readLine())) {
			String[] arr = line.split(Global.delimiterLevel1)[1].split(Global.delimiterPound)[1].split(Global.delimiterLevel2);
			for(String st : arr)	widHasDate.add(st.trim());
		}
		br.close();
		
		// 将带有时间的词从nid关键词文件中去掉
		br = IOUtility.getBR(pathK);
		br.readLine();
		BufferedWriter bw = IOUtility.getBW(pathNoDK);
		int numOrg = 0, numNow = 0;
		bw.write("                             \n");
		while(null != (line = br.readLine())) {
			numOrg++;
			String[] id2wids = line.split(Global.delimiterLevel1);
			List<String> ws = new ArrayList<>();
			String[] wids = id2wids[1].split(Global.delimiterLevel2);
			for(String w : wids) {
				if(!widHasDate.contains(w))	ws.add(w);
			}
			if(!ws.isEmpty()) {
				numNow++;
				bw.write(id2wids[0]);
				bw.write(Global.delimiterLevel1);
				for(String w : ws)	bw.write(w + Global.delimiterLevel2);
				bw.write('\n');
			}
		}
		br.close();
		bw.close();
		IOUtility.writeFirstLine(pathNoDK, numNow + Global.delimiterPound);
		
		MLog.log("numOrg : " + numOrg);
		MLog.log("numNow : " + numNow);
		MLog.log("buildNoDateNidKeywordsListFile over");
	}
	
	/**
	 * 将不带时间的节点与带时间的节点合并为一个文件
	 * 对于没有时间信息的nid，其时间信息使用Global.TIME_INAVAILABLE
	 * @param pathNidKD
	 * @param pathNidK
	 * @param pathM
	 * @throws Exception
	 */
	public static void merge(String pathNidKD, String pathNidK, String pathM) throws Exception{
		BufferedReader br = null;
		String line = null;
		String[] arr = null;
		Map<Integer, String> id2str = new TreeMap<>();
		
		// 读取pathNidK
		int numNoTime = 0;
		br = IOUtility.getBR(pathNidK);
		line = br.readLine();
		MLog.log(line);
		while(null != (line = br.readLine())) {
			numNoTime++;
			arr = line.split(Global.delimiterLevel1);
			id2str.put(Integer.parseInt(arr[0]), String.valueOf(Global.TIME_INAVAILABLE) + Global.delimiterPound + arr[1]);
		}
		br.close();
		
		// 读取pathNidKD
		int numHasTime = 0;
		br = IOUtility.getBR(pathNidKD);
		br.readLine();
		while(null != (line = br.readLine())) {
			numHasTime++;
			arr = line.split(Global.delimiterLevel1);
			id2str.put(Integer.parseInt(arr[0]), arr[1]);
		}
		br.close();
		
		// 输出文件
		BufferedWriter bw = IOUtility.getBW(pathM);
		bw.write("                    \n");
		for(Entry<Integer, String> en : id2str.entrySet()) {
			bw.write(String.valueOf(en.getKey()));
			bw.write(Global.delimiterLevel1);
			bw.write(en.getValue());
			bw.write('\n');
		}
		bw.close();
		IOUtility.writeFirstLine(pathM, id2str.size() + Global.delimiterPound);
		
		MLog.log("NumHasTime: " + numHasTime);
		MLog.log("NumNoTime: " + numNoTime);
		MLog.log("NumTotal: " + id2str.size());
		MLog.log("merge over");
	}
	
	
	
	public static void main(String[] args) throws Exception {
		String pathInput = Global.inputDirectoryPath + "pidCoordYagoVB.txt";
		String pathOutput = Global.inputDirectoryPath + "pidCoordYagoVB_avaiable.txt";
		
		/** 过滤不合法的坐标点  **/
//		pathInput = Global.inputDirectoryPath + "pidCoordYagoVB.txt";
//		pathOutput = Global.inputDirectoryPath + "pidCoordYagoVB_avaiable.txt";
//		filterCoord(pathInput, pathOutput);
		
		
		/** 过滤超过指定时间范围的节点  **/
//		pathInput = Global.inputDirectoryPath + "nodeIdKeywordListOnIntDateMapYagoVB_只有时间.txt";
//		pathOutput = Global.inputDirectoryPath + "nodeIdKeywordListOnIntDateMapYagoVB_available.txt";
//		filterTime(pathInput, pathOutput);
		
		String pathNidKD = Global.inputDirectoryPath + "nodeIdKeywordListOnIntDateMapYagoVB.txt";
		String pathNidK = Global.inputDirectoryPath + "nidKeywordsListMapYagoVB.txt";
		
		/** 从nidKeywordsListMapYagoVB.txt移除掉那些带时间的词  **/
//		pathInput = Global.inputDirectoryPath + "nodeIdKeywordListOnIntDateMapYagoVB_available.txt";
//		pathNidK = Global.inputDirectoryPath + "nidKeywordsListMapYagoVB_org.txt";
//		String pathNoKD = Global.inputDirectoryPath + "nidKeywordsListMapYagoVB_nodate.txt";
//		buildNoDateNidKeywordsListFile(pathInput, pathNidK, pathNoKD);
		
		
		/** 合并不带时间和带时间节点  **/
//		pathNidKD = Global.inputDirectoryPath + "nodeIdKeywordListOnIntDateMapYagoVB_available.txt";
//		pathNidK = Global.inputDirectoryPath + "nidKeywordsListMapYagoVB_nodate.txt";
//		String pathM = Global.inputDirectoryPath + "mergeIdKDAndIdK.txt";
//		merge(pathNidKD, pathNidK, pathM);
		
		
	}
}
