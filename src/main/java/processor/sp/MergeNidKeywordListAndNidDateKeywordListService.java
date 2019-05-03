package processor.sp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;

import utility.Global;
import utility.IOUtility;

/**
 * 合并文件nidKeywordsListMapYagoVB.txt和nodeIdKeywordListOnIntDateMapYagoVB.txt，对于
 * nidKeywordsListMapYagoVB.txt没有文件时间信息的，添加时间为Integer.MAX_VALUE
 * @author ZhouHao
 * @since 2019年4月30日
 */
public class MergeNidKeywordListAndNidDateKeywordListService {
	
	/**
	 * 合并pathNidKD和pathNidK，对于没有时间信息的pid，其时间信息使用Integer.MAX_VALUE
	 * @param pathNidKD
	 * @param pathNidK
	 * @param pathM
	 * @throws Exception
	 */
	public static void merge(String pathNidKD, String pathNidK, String pathM) throws Exception{
		// 读取pathNidKD
		BufferedReader br = IOUtility.getBR(pathNidKD);
		Map<String, String> id2str = new HashMap<>();
		String line = null;
		String[] arr = null;
		br.readLine();
		while(null != (line = br.readLine())) {
			arr = line.split(Global.delimiterLevel1);
			id2str.put(arr[0], arr[1]);
		}
		br.close();
		
		// 输出合并后的文件
		br = IOUtility.getBR(pathNidK);
		BufferedWriter bw = IOUtility.getBW(pathM);
		line = br.readLine();
		bw.write(line + "\n");
		while(null != (line = br.readLine())) {
			arr = line.split(Global.delimiterLevel1);
			bw.write(arr[0]);
			bw.write(Global.delimiterLevel1);
			if(id2str.containsKey(arr[0])) {
				bw.write(id2str.get(arr[0]));
			} else {
				bw.write(String.valueOf(Integer.MAX_VALUE));
				bw.write(Global.delimiterPound);
				bw.write(arr[1]);
			}
			bw.write('\n');
		}
		br.close();
		bw.close();
		System.out.println("> over");
	}
	
	public static void main(String[] args) throws Exception{
		String pathNidKD = Global.inputDirectoryPath + "nodeIdKeywordListOnIntDateMapYagoVB.txt";
		String pathNidK = Global.inputDirectoryPath + "nidKeywordsListMapYagoVB.txt";
		String pathM = Global.inputDirectoryPath + "mergeIdKDAndIdK.txt";
		merge(pathNidKD, pathNidK, pathM);
	}
}
