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
		Long start = System.currentTimeMillis();
		System.out.println("> 开始从原始文件中随机选取" + sampleNum + "个测试样本 . . . ");
		
		Map<Integer, double[]> pidCoordMap = new HashMap<>();
		double[] doubleArr = null;
		BufferedReader br = new BufferedReader(new FileReader(pidCoordFile));
		String line = br.readLine();
		String[] strArr = null;
		int id = 0;
		int samNum = sampleNum;
		
		// 将坐标信息读入内存
		while(null != (line = br.readLine())) {
			strArr = line.split(Global.delimiterLevel1);
			id = Integer.parseInt(strArr[0]);
			strArr = strArr[1].split(Global.delimiterSpace);
			doubleArr = new double[2];
			for(int i=0; i<strArr.length; i++) {
				doubleArr[i] = Double.parseDouble(strArr[i]);
			}
			pidCoordMap.put(id, doubleArr);
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
		double[] sampCoord = null;
		double[] tempCoord = null;
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
					if(!recSet.contains(id) && null != (tempCoord = pidCoordMap.get(id))) {
						// 坐标
						sampCoord = new double[2];
						sampCoord[0] = tempCoord[0] + RandomNumGenerator.getRandomFloat();
						sampCoord[1] = tempCoord[1] + RandomNumGenerator.getRandomFloat();
						if (sampCoord[0] >= -90 && sampCoord[0] <= 90 && sampCoord[1] >= -180 && sampCoord[1] <= 180) {
							strArr = strArr[1].split(Global.delimiterDate);
							// 时间
							String orgDate = strArr[0];
							sampDate = TimeUtility.getOffsetDate(strArr[0], dateOffsetGe.getRandomInt());
							// qwords
							strArr = strArr[strArr.length - 1].split(Global.delimiterLevel2);
							if(strArr.length > 2) {
								sampQwords = new ArrayList<>();
								for(i =0 ;i < 2; i++) {
									sampQwords.add(Integer.parseInt(strArr[i]));
								}
								// 输出
								bw.write(String.valueOf(sampK) + " ");
								bw.write(String.valueOf(tempCoord[0]) + " ");
								bw.write(String.valueOf(tempCoord[1]) + " ");
								bw.write(strArr[0] + " ");
								bw.write(strArr[1] + " ");
								bw.write(orgDate + "\n");
								bw.write(String.valueOf(sampK) + " ");
								for(i=0; i<sampCoord.length; i++) {
									bw.write(String.valueOf(sampCoord[i]) + " ");
								}
								bw.write('\n');
								for(i=0; i<sampQwords.size(); i++) {
									bw.write(String.valueOf(sampQwords.get(i)) + " ");
								}
								bw.write(sampDate + "\n");
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
		System.out.println("> 完成选取" + samNum + "个测试样本.");
	}
	
	public static void main(String[] args) throws Exception{
		SampleChooser.productTestSample(100, Global.inputDirectoryPath + Global.nodeIdKeywordListOnDateFile, 
				Global.inputDirectoryPath + Global.pidCoordFile, 
				Global.inputDirectoryPath + Global.testSampleFile);
	}
}
