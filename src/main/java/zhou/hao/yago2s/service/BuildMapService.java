package zhou.hao.yago2s.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate;

import zhou.hao.service.ZipBase64ReaderService;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.TimeStr;

/**
 * 
 * @author Monica
 * @since 2018/3/8
 * 功能 : 构造yago2s图
 */
public class BuildMapService {
	private ArrayList<Integer>[] yago2sArrMap = new ArrayList[Yago2sInfoService.nodeNum];
//	private ArrayList<Integer>[] yago2sArrMap = new ArrayList[4];
	private String zipPath = null;
	private String entryName = null;
	
	public BuildMapService(String zipPath, String entryName) {
		this.zipPath = zipPath;
		this.entryName = entryName;
	}
	
	// 构造Map
	public ArrayList<Integer>[] buildMap(){
		Long startTime = System.currentTimeMillis();
		System.out.println("> 开始构造Yago2sMap(" + yago2sArrMap.length + "个节点) . . .  " + TimeStr.getTime());
		ZipBase64ReaderService ser = new ZipBase64ReaderService(zipPath, entryName);
		ser.readLine();
		String lineStr = null;
		int i, j, k, nodeId;
		String[] tempStrArr = null;
		ArrayList<Integer> tempList = null;
		while(null != (lineStr = ser.readLine())) {
			k = lineStr.indexOf(':');
			nodeId = Integer.parseInt(lineStr.substring(0, k));
			k += 2;
			tempStrArr = lineStr.substring(k, lineStr.length()).split(",");
			if(null == yago2sArrMap[nodeId])
				yago2sArrMap[nodeId] = tempList = new ArrayList<>();
			for(String st : tempStrArr)
				tempList.add(Integer.parseInt(st));
		}
		ser.close();
		System.out.println("> 结束构造Yago2sMap(" + yago2sArrMap.length + "个节点), 花时" + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()) + "  ！ ！！"+ TimeStr.getTime() + "\n");
		return yago2sArrMap;
	}
	
	// 查找节点的边
	public ArrayList<Integer> findEdges(int nodeId){
		return this.yago2sArrMap[nodeId];
	}
	
	// 将把word也当做节点的图写到文件nodeAndWordMapYagoVB.txt中
	public static void writenodeAndWordMapYagoVBTxt() throws Exception{
		String basePath = LocalFileInfo.getDataSetPath() + "test/";
		StringBuffer[] nodeAndWordMap = new StringBuffer[24];
		String[] rPath = new String[2];
		rPath[0] = "edgeYagoVB.txt";
		rPath[1] = "nidKeywordsListMapYagoVB.txt";
		BufferedReader br = null;
		int i = 0, nodeId;
		String lineStr = null;
		
		// 读文件
		for(String path : rPath) {
			br = new BufferedReader(new FileReader(new File(basePath + path)));
			br.readLine();
			while(null != (lineStr = br.readLine())) {
				i = lineStr.indexOf(':');
				nodeId = Integer.parseInt(lineStr.substring(0, i));
				if(null == nodeAndWordMap[nodeId]) {
					nodeAndWordMap[nodeId] = new StringBuffer();
				}
				nodeAndWordMap[nodeId].append(lineStr.substring(i +2));
			}
			br.close();
		}
		
		// 写文件
		int size = nodeAndWordMap.length;
		BufferedWriter bw = new BufferedWriter(new FileWriter(basePath + "nodeAndWordMapYagoVB.txt"));
		bw.write("                        \n");
		long totalDegree = 0;
		int curDegree = 0;
		String[] tempArr = null;
		for(i=0; i<size; i++) {
			if(null != nodeAndWordMap[i]) {
				bw.write(String.valueOf(i));
				tempArr = nodeAndWordMap[i].toString().split(",");
				curDegree = tempArr.length;
				totalDegree += curDegree;
				bw.write(" " + String.valueOf(curDegree));
				for(String st : tempArr)
					bw.write(" " + st);
				bw.write('\n');
			}
		}
		bw.close();
		
		RandomAccessFile raf = new RandomAccessFile(new File(basePath + "nodeAndWordMapYagoVB.txt"), "rw");
		raf.seek(0);
		raf.write((String.valueOf(size) + " " + String.valueOf(totalDegree)).getBytes());
		raf.close();
		
	}
	
	
	// 主函数
	public static void main(String[] args) throws Exception{
		
		BuildMapService.writenodeAndWordMapYagoVBTxt();
		
//		BuildMapService ser = new BuildMapService(LocalFileInfo.getDataSetPath() + "test.zip", "test");
//		BuildMapService ser = new BuildMapService(LocalFileInfo.getDataSetPath() + "YagoVB.zip", "edgeYagoVB.txt");
//		ser.buildMap();
//		for(int i : ser.findEdges(8)) {
//			System.out.print(i + " ");
//		}
//		System.out.println();
	}
}





















