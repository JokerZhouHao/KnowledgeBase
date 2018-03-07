package zhou.hao.yago2s.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import zhou.hao.service.ZipBase64ReaderService;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.TimeStr;

/**
 * 
 * @author Monica
 * @since 2018/03/02
 * 功能： 处理文件中与OnDate相关的信息，其中OnDate属性用~隔开
 */
public class NodeIdOnDateService {
	
	private String sourceZipPath = null;
	private String vbZipPath = null;
	private String nodeIdEntryName = null;
	private String nodeIdOnDateMapYagoVBPath = null;
	
	// 构造函数
	public NodeIdOnDateService(String sourceZipPath, String vbZipPath, String nodeIdEntryName,
			String nodeIdOnDateMapYagoVBPath) {
		super();
		this.sourceZipPath = sourceZipPath;
		this.vbZipPath = vbZipPath;
		this.nodeIdEntryName = nodeIdEntryName;
		this.nodeIdOnDateMapYagoVBPath = nodeIdOnDateMapYagoVBPath;
	}

	// 测试yago2s_ttl.zip的节点数
	public static void testYago2Node() {
		HashMap<String, Boolean> nodeMap1 = new HashMap<>();
		String filePath1 = LocalFileInfo.getDataSetPath() + "YagoVB.zip";
		ZipBase64ReaderService reader1 = new ZipBase64ReaderService(filePath1, "nodeIdMapYagoVB.txt");
		
		System.out.println(reader1.readLine());
		String strLine = null;
		int i = 0;
		System.out.println("-开始处理  " + TimeStr.getTime());
		while(null != (strLine=reader1.readLine())) {
			nodeMap1.put(strLine.substring(strLine.indexOf(' ')+1, strLine.length()), Boolean.TRUE);
		}
		System.out.println("-Over处理  " + TimeStr.getTime() + "\n");
		reader1.close();
		int nodeNum = nodeMap1.size();
		
		
		HashMap<String, Integer> nodeMap = new HashMap<>();
		String filePath = LocalFileInfo.getDataSetPath() + "yago2s_ttl.zip";
		ZipBase64ReaderService reader = new ZipBase64ReaderService(filePath);
		System.out.println("-开始处理yago2s_ttl.zip  " + TimeStr.getTime());
		String lineStr = null;
		ArrayList<String> li = null;
		LineDealService ldS = new LineDealService();
		String tempStr = null;;
		do {
			System.out.println("当前文件 : " + reader.getCurZipEntryName());
			while(null != (lineStr = reader.readLine())) {
				
				if(lineStr.length()>0 && lineStr.charAt(0) == '<') {
					li = ldS.dealLine(lineStr);
					
					tempStr = li.get(0);
					if(tempStr.charAt(0) == '<' && nodeMap1.get(tempStr)==Boolean.TRUE) {
						nodeMap1.put(tempStr, Boolean.FALSE);
						nodeNum--;
					}
					
					tempStr = li.get(2);
					if(tempStr.charAt(0) == '<' && nodeMap1.get(tempStr)==Boolean.TRUE) {
						nodeMap1.put(tempStr, Boolean.FALSE);
						nodeNum--;
					}
					
//					if(nodeMap.size()%8000000 == 0 ) 
//						System.out.println("已获得" + nodeMap.size());
				}
			}
			System.out.println();
		} while (null != (reader.changeToNextZipEntry()));
		
		System.out.println("-Over处理yago2s_ttl.zip  " + TimeStr.getTime());
		System.out.println(nodeNum);
	}
	
	// 测试yagoVB.zip的节点数
	public static void testNodeMap() {
		HashMap<String, Integer> nodeMap = new HashMap<>();
		
		String filePath = LocalFileInfo.getDataSetPath() + "YagoVB.zip";
		ZipBase64ReaderService reader = new ZipBase64ReaderService(filePath, "nodeIdMapYagoVB.txt");
		
		System.out.println(reader.readLine());
		String strLine = null;
		int i = 0;
		System.out.println("-开始处理  " + TimeStr.getTime());
		while(null != (strLine=reader.readLine())) {
			nodeMap.put(strLine.substring(strLine.indexOf(' ')+1, strLine.length()), i++);
		}
		System.out.println("-over处理  " + TimeStr.getTime());
		System.out.println(nodeMap.size());
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			// TODO: handle exception
		}
		reader.close();
	}
	
	// 输出只含有节点的OnDate属性的节点txt文档 ： nodeIdOnDateMapYagoVB.txt
	public boolean writeNodeIdOnDateTxt() {
		// 读取yagoVB.zip的nodeIdMap文件
		HashMap<String, Integer> nodeIdMap = new HashMap<>();
		ZipBase64ReaderService reader = new ZipBase64ReaderService(vbZipPath, nodeIdEntryName);
		
		System.out.println(reader.readLine());
		String lineStr = null;
		int i = 0;
		System.out.println("-开始处理" + vbZipPath + "里的" + nodeIdEntryName + " " + TimeStr.getTime());
		while(null != (lineStr=reader.readLine())) {
			nodeIdMap.put(lineStr.substring(lineStr.indexOf(' ')+1, lineStr.length()), i++);
		}
		System.out.println("-Over处理  " + TimeStr.getTime() + "\n");
		reader.close();
		int nodeNum = nodeIdMap.size();
		
		// 初始化nodeIdOnDateList
		ArrayList<HashSet<String>> nodeIdOnDateList = new ArrayList<>();
		for(i=0; i<nodeNum; i++)	nodeIdOnDateList.add(null);
		
		// 构造nodeIdOnDateList
		reader = new ZipBase64ReaderService(sourceZipPath);
		System.out.println("-开始提取" + sourceZipPath + "中包含的OnDate属性 " + TimeStr.getTime());
		ArrayList<String> li = null;
		LineDealService ldS = new LineDealService();
		String tempStr = null;
		Integer tempInt = null;
		HashSet<String> tempSet = null;
		int hasOnDateAttNodeNum = 0;
		do {
			System.out.println("处理文件 : " + reader.getCurZipEntryName());
			while(null != (lineStr = reader.readLine())) {
				
				if(lineStr.length()>0 && lineStr.charAt(0) == '<') {
					li = ldS.dealLine(lineStr);
					
					if(li.get(1).contains("OnDate")) {
						tempStr = li.get(0);
						if('<' == tempStr.charAt(0) && null != (tempInt=nodeIdMap.get(tempStr))) {
							if(null == (tempSet=nodeIdOnDateList.get(tempInt))) {
								tempSet = new HashSet<>();
								nodeIdOnDateList.set(tempInt, tempSet);
								hasOnDateAttNodeNum++;
							}
							tempSet.add(li.get(2));
						}
					}
				}
			}
		} while (null != (reader.changeToNextZipEntry()));
		reader.close();
		System.out.println("有OnDate属性的点数：" + hasOnDateAttNodeNum);
		System.out.println("-Over处理" + sourceZipPath + TimeStr.getTime() + "\n");
		
		// 开始写nodeIdOnDate文件
		System.out.println("-开始写文件" + nodeIdOnDateMapYagoVBPath + " " + TimeStr.getTime());
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(nodeIdOnDateMapYagoVBPath));
			bw.write(String.valueOf(nodeNum) + "#\n");
			i = 0;
			for(HashSet<String> hs : nodeIdOnDateList) {
				bw.write(String.valueOf(i++));
				bw.write(": ");
				if(null != hs) {
					for(String st : hs) {
						bw.write(st);
						bw.write("~");
					}
				}
				bw.write("\n");
			}
			bw.flush();
			bw.close();
			System.out.println("-Over写文件" + nodeIdOnDateMapYagoVBPath + " " + TimeStr.getTime());
		} catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
	
	// 统计
	public void displayStatisticResult() {
		try {
			ArrayList<Boolean> signList = new ArrayList<>();
			// 统计有OnDate属性的节点数
			BufferedReader br = new BufferedReader(new FileReader(nodeIdOnDateMapYagoVBPath));
			System.out.println("总节点数：" + br.readLine());
			int containOnDateNodeNum = 0;
			String str = null;
			int i = 0;
			while(null != (str = br.readLine())) {
				if(str.indexOf(' ')+1 == str.length()) {
					signList.add(Boolean.FALSE);
				} else {
					signList.add(Boolean.TRUE);
					containOnDateNodeNum++;
				}
			}
			br.close();
			System.out.println("包含OnDate属性的节点数：" + containOnDateNodeNum + "\n");
			
			// 统计同时包含OnDate属性和其他属性的节点数
			containOnDateNodeNum = 0;
			ZipBase64ReaderService zrs = new ZipBase64ReaderService(vbZipPath, "nidKeywordsListMapYagoVB");
			System.out.println(zrs.readLine());
			while(null != (str = zrs.readLine())) {
				if(signList.get(Integer.parseInt(str.substring(0, str.indexOf(':')))))
					containOnDateNodeNum++;
			}
			System.out.println("同时包含OnDate和其他属性的节点数：" + containOnDateNodeNum);
			zrs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) throws Exception{
//		OnDateAddService.testNodeMap();
//		NodeIdOnDateService.testYago2Node();
		
//		String basePath = LocalFileInfo.getDataSetPath();
//		NodeIdOnDateService ser = new NodeIdOnDateService(basePath + "yago2s_ttl.zip", 
//				basePath + "YagoVB.zip", 
//				"nodeIdMapYagoVB.txt", 
//				basePath + "nodeIdOnDateMapYagoVB.txt");
////		ser.writeNodeIdOnDateTxt();
//		ser.displayStatisticResult();
		
//		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(LocalFileInfo.getDataSetPath() + "nodeIdOnDateMapYagoVB.txt")));
//		bw.write("11");
//		bw.flush();
//		bw.close();
		
//		BufferedReader br = new BufferedReader(new FileReader(LocalFileInfo.getDataSetPath() + "nodeIdOnDateMapYagoVB.txt"));
//		String str = null;
//		while(null != (str = br.readLine())) {
//			if(str.length()>9)	System.out.println(str);
//		}
//		br.close();
		
	}
}
