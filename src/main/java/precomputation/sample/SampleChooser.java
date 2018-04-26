package precomputation.sample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import utility.Global;
import utility.RandomNumGenerator;
import utility.TimeUtility;

public class SampleChooser {
	public static void productTestSample(int sampleNum, String vidFile, String pidCoordFile, String sampleFile) throws Exception{
		Map<Integer, float[]> pidCoordMap = new HashMap<>();
		float[] floatArr = null;
		BufferedReader br = new BufferedReader(new FileReader(pidCoordFile));
		String line = br.readLine();
		String[] strArr = null;
		int id = 0;
		
		// 将坐标信息读入内存
		while(null != (line = br.readLine())) {
			strArr = line.split(Global.delimiterLevel1);
			id = Integer.parseInt(strArr[0]);
			strArr = strArr[1].split(Global.delimiterSpace);
			floatArr = new float[2];
			for(int i=0; i<strArr.length; i++) {
				floatArr[i] = Float.parseFloat(strArr[0]);
			}
			pidCoordMap.put(id, floatArr);
		}
		br.close();
		
		// 写样本
		BufferedWriter bw = new BufferedWriter(new FileWriter(sampleFile));
		bw.write(String.valueOf(sampleNum) + Global.delimiterPound + "\n");
		// 偏移的行数
		RandomNumGenerator lineOffsetGe = new RandomNumGenerator(1, (int)(Global.numContainCoordWordDate/(sampleNum * 1.5)));
		int lineOffset = lineOffsetGe.getRandomInt();
		// 搜索日期的偏移数
		RandomNumGenerator dateOffsetGe = new RandomNumGenerator(1, 10);
		
		int sampK = 10;
		float[] sampCoord = null;
		String sampDate = null;
		ArrayList<Integer> sampQwords = null;
		int i =0;
		Set<Integer> recSet = new TreeSet<Integer>();
		
		while(0 < sampleNum) {
			br = new BufferedReader(new FileReader(vidFile));
			br.readLine();
			while(null != (line = br.readLine())) {
				if(--lineOffset == 0) {
					lineOffset = lineOffsetGe.getRandomInt();
					strArr = line.split(Global.delimiterLevel1);
					id = Integer.parseInt(strArr[0]);
					if(!recSet.contains(id) && null != (sampCoord = pidCoordMap.get(id))) {
						// 坐标
						sampCoord[0] += RandomNumGenerator.getRandomFloat();
						sampCoord[1] += RandomNumGenerator.getRandomFloat();
						if (sampCoord[0] >= -90 && sampCoord[0] <= 90 && sampCoord[1] >= -180 && sampCoord[1] <= 180) {
							strArr = strArr[1].split(Global.delimiterDate);
							// 时间
							sampDate = TimeUtility.getOffsetDate(strArr[0], lineOffsetGe.getRandomInt());
							// qwords
							strArr = strArr[strArr.length - 1].split(Global.delimiterLevel2);
							if(strArr.length > 2) {
								sampQwords = new ArrayList<>();
								for(i =0 ;i < 2; i++) {
									sampQwords.add(Integer.parseInt(strArr[i]));
								}
								// 输出
								bw.write(String.valueOf(sampK) + " ");
								for(i=0; i<sampCoord.length; i++) {
									bw.write(String.valueOf(sampCoord[i]) + " ");
								}
								for(i=0; i<sampQwords.size(); i++) {
									bw.write(String.valueOf(sampQwords.get(i)) + " ");
								}
								bw.write(sampDate + " ");
								bw.write('\n');
								recSet.add(id);
								if(0 == (--sampleNum))	break;
							}
						}
					}
				}
			}
			br.close();
		}
		bw.close();
	}
	
	public static void main(String[] args) throws Exception{
		SampleChooser.productTestSample(100, Global.inputDirectoryPath + Global.nodeIdKeywordListOnDateFile, 
				Global.inputDirectoryPath + Global.pidCoordFile, 
				Global.inputDirectoryPath + Global.testSampleFile);
	}
}
