package main.freebase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import file.reader.ZipReader;
import file.writer.ZipWriter;
import utility.LocalFileInfo;
import utility.TimeUtility;

/**
 * 
 * @author Monica
 * @since 2017/12/28
 * 生成反向的度不大于1000的FreeBaseMap的反向边的zip
 */

public class Product1000GFreebaseMapInvertedEdgeZip {
//	生成反向的度不大于1000的FreeBaseMap的反向边的zip
	public static void get1000GFreebaseMapInvertedEdgeZip() {
		String lineStr = null;
		int i, j, k, x;
		ZipReader reader = new ZipReader(LocalFileInfo.getBasePath() + File.separator + "data" + File.separator + "edge1000Degree.zip");
		ZipWriter writer = new ZipWriter(LocalFileInfo.getBasePath() + File.separator + "data" + File.separator + "invertedEdge1000Degree.zip");
		writer.addZipEntity("invertedEdge1000Degree.txt");
		lineStr = reader.readLine();
		int nodeNum = Integer.parseInt(lineStr.split("#")[0]);
		
		writer.write(lineStr);
		writer.write("\n");
		
		// 构造invertedEdgeArr
		System.out.println("> 开始构造invertedEdgeArr. . .  " + TimeUtility.getTime());
		ArrayList<Integer>[] invertedEdgeArr = new ArrayList[nodeNum];
		String[] tempLineArr = null;
		i = -1;
		lineStr = null;
		while((lineStr=reader.readLine())!=null) {
			i++;
			if(!lineStr.equals("")) {
				tempLineArr = lineStr.split(",");
				j = tempLineArr.length;
				for(k = 0; k<j; k++) {
					x = Integer.parseInt(tempLineArr[k]);
					if(null==invertedEdgeArr[x]) {
						invertedEdgeArr[x] = new ArrayList<>();
					}
					invertedEdgeArr[x].add(i);
				}
			}
//			System.out.println(lineStr + "\n");
//			if(i==10)	break;
		}
		tempLineArr = null;
		reader.close();
		System.out.println("> 构造invertedEdgeArr完成  " + TimeUtility.getTime() + "\n");
		
		// 写出
		System.out.println("> 开始输出文件invertedEdge1000Degree.zip . . .  " + TimeUtility.getTime());
		i = nodeNum;
		for(j=0; j<nodeNum; j++) {
			if(invertedEdgeArr[j]!=null) {
				for(int t : invertedEdgeArr[j]) {
					writer.write(String.valueOf(t));
					writer.write(",");
				}
			}
			writer.write("\n");
			if((i+1%10000000)==0) System.out.println("> 已添加" + (i+1) + "个点 . . . " + TimeUtility.getTime());
//			if(j==10)	break;
		}
		writer.close();
		System.out.println("> 输出文件invertedEdge1000Degree.zip成功  " + TimeUtility.getTime());
	}
	
//	生成反向的度不大于1000的FreeBaseMap的反向边的zip，去掉有超过1000个点指向改点的点
	public static void get1000GFreebaseMapInvertedEdgeZipNo1000() {
		String lineStr = null;
		int i, j;
		ZipReader rEdgeReader = new ZipReader(LocalFileInfo.getBasePath() + File.separator + "data" + File.separator + "invertedEdge1000Degree.zip");
		ZipWriter writer = new ZipWriter(LocalFileInfo.getBasePath() + File.separator + "data" + File.separator + "1000DegreeGFreeBaseMap.zip");
		
		writer.addZipEntity("rEdge.txt");
		lineStr = rEdgeReader.readLine();
		writer.write(lineStr + "\n");
//		writer.close();
		
		// 获得超过1000个点指向该点的点
		System.out.println("> 开始获得超过1000个点指向该点的点. . .  " + TimeUtility.getTime());
		HashMap<Integer, HashMap<Integer, Boolean>> record1000PointsMap = new HashMap<Integer, HashMap<Integer, Boolean>>();
		HashMap<Integer, Boolean> tempPointMap = null;
		i = 0;
		String strArr[] = null;
		int tempTotal = 0;
		while((lineStr = rEdgeReader.readLine()) != null) {
			if(!lineStr.equals("")) {
				strArr = lineStr.split(",");
				if(strArr.length>1000) {
					tempTotal++;
					tempPointMap = new HashMap<>();
					for(String st : strArr) {
						j = Integer.parseInt(st);
						if(record1000PointsMap.get(j) == null) {
							record1000PointsMap.put(j, new HashMap<Integer, Boolean>());
						}
						record1000PointsMap.get(j).put(i, Boolean.TRUE);
					}
				} else writer.write(lineStr);
			} 
			writer.write("\n");
			if((i+1)%10000000==0)
				System.out.println("> 已处理" + (i+1) + "个点 . . ." + TimeUtility.getTime());
			i++;
		}
//		writer.closeZipEntity();
		rEdgeReader.close();
		writer.flush();
//		writer.close();
		System.out.println("> " + i + " 共获得" + tempTotal + "个超过1000个点指向该点的点！  " + TimeUtility.getTime() + "\n");
		
		// 开始处理edge.txt
		System.out.println("> 开始处理edge.txt . . . " + TimeUtility.getTime());
		ZipReader edgeReader = new ZipReader(LocalFileInfo.getBasePath() + File.separator + "data" + File.separator + "edge1000Degree.zip");
		writer.addZipEntity("edge.txt");
		writer.write(edgeReader.readLine() + "\n");
		i=0;
		while((lineStr = edgeReader.readLine()) != null) {
			if(!lineStr.equals("")) {
				if((tempPointMap=record1000PointsMap.get(i))!=null) {
					strArr = lineStr.split(",");
					for(String st : strArr) {
						if(tempPointMap.get(Integer.parseInt(st)) == null) {
							writer.write(st);
							writer.write(",");
						}
					}
				} else writer.write(lineStr);
			}
			writer.write("\n");
			
			if((i+1)%10000000==0) System.out.println("> 已处理" + (i+1) + "个点. . . " + TimeUtility.getTime());
			i++;
		}
		edgeReader.close();
//		writer.closeZipEntity();
		writer.flush();
		writer.close();
		System.out.println("> 已处理edge.txt . . . " + TimeUtility.getTime() + "\n");
		System.out.println("> " + i + " 处理完成!" + TimeUtility.getTime());
	}
	
	public static void main(String[] args) {
		new Product1000GFreebaseMapInvertedEdgeZip().get1000GFreebaseMapInvertedEdgeZipNo1000();
	}
}
