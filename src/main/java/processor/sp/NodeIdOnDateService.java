package processor.sp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import file.reader.ZipBase64Reader;
import utility.IOUtility;
import utility.LocalFileInfo;
import utility.TimeUtility;

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
	public NodeIdOnDateService() {}
	
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
		ZipBase64Reader reader1 = new ZipBase64Reader(filePath1, "nodeIdMapYagoVB.txt");
		
		System.out.println(reader1.readLine());
		String strLine = null;
		int i = 0;
		System.out.println("-开始处理  " + TimeUtility.getTime());
		while(null != (strLine=reader1.readLine())) {
			nodeMap1.put(strLine.substring(strLine.indexOf(' ')+1, strLine.length()), Boolean.TRUE);
		}
		System.out.println("-Over处理  " + TimeUtility.getTime() + "\n");
		reader1.close();
		int nodeNum = nodeMap1.size();
		
		
		HashMap<String, Integer> nodeMap = new HashMap<>();
		String filePath = LocalFileInfo.getDataSetPath() + "yago2s_ttl.zip";
		ZipBase64Reader reader = new ZipBase64Reader(filePath);
		System.out.println("-开始处理yago2s_ttl.zip  " + TimeUtility.getTime());
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
		
		System.out.println("-Over处理yago2s_ttl.zip  " + TimeUtility.getTime());
		System.out.println(nodeNum);
	}
	
	// 测试yagoVB.zip的节点数
	public static void testNodeMap() {
		HashMap<String, Integer> nodeMap = new HashMap<>();
		
		String filePath = LocalFileInfo.getDataSetPath() + "YagoVB.zip";
		ZipBase64Reader reader = new ZipBase64Reader(filePath, "nodeIdMapYagoVB.txt");
		
		System.out.println(reader.readLine());
		String strLine = null;
		int i = 0;
		System.out.println("-开始处理  " + TimeUtility.getTime());
		while(null != (strLine=reader.readLine())) {
			nodeMap.put(strLine.substring(strLine.indexOf(' ')+1, strLine.length()), i++);
		}
		System.out.println("-over处理  " + TimeUtility.getTime());
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
		ZipBase64Reader reader = new ZipBase64Reader(vbZipPath, nodeIdEntryName);
		
		System.out.println(reader.readLine());
		String lineStr = null;
		int i = 0;
		System.out.println("-开始处理" + vbZipPath + "里的" + nodeIdEntryName + " " + TimeUtility.getTime());
		while(null != (lineStr=reader.readLine())) {
			nodeIdMap.put(lineStr.substring(lineStr.indexOf(' ')+1, lineStr.length()), i++);
		}
		System.out.println("-Over处理  " + TimeUtility.getTime() + "\n");
		reader.close();
		int nodeNum = nodeIdMap.size();
		
		// 初始化nodeIdOnDateList
		ArrayList<HashSet<String>> nodeIdOnDateList = new ArrayList<>();
		for(i=0; i<nodeNum; i++)	nodeIdOnDateList.add(null);
		
		// 构造nodeIdOnDateList
		reader = new ZipBase64Reader(sourceZipPath);
		System.out.println("-开始提取" + sourceZipPath + "中包含的OnDate属性 " + TimeUtility.getTime());
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
		System.out.println("-Over处理" + sourceZipPath + TimeUtility.getTime() + "\n");
		
		// 开始写nodeIdOnDate文件
		System.out.println("-开始写文件" + nodeIdOnDateMapYagoVBPath + " " + TimeUtility.getTime());
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
			System.out.println("-Over写文件" + nodeIdOnDateMapYagoVBPath + " " + TimeUtility.getTime());
		} catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
	
	// 输出标准的只含有节点的OnDate属性的节点txt文档 ： nodeIdStandardOnDateMapYagoVB.txt
	public void writeNodeIdStandardOnDateTxt() throws Exception{
		System.out.println("> 开始写nodeIdStandardOnDateMapYagoVB.txt . . . ");
		BufferedReader reader = new BufferedReader(new FileReader(new File(LocalFileInfo.getDataSetPath() + "nodeIdOnDateMapYagoVB.txt")));
		BufferedWriter bw = new BufferedWriter(new FileWriter(LocalFileInfo.getDataSetPath() + "nodeIdStandardOnDateMapYagoVB.txt"));
		reader.readLine();
		bw.write("        \n");
		String lineStr = null;
		int k;
		int nodeNum = 0;
		String strArr[] = null;
		String dateArr[] = null;
		String year, month, day;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		while(null != (lineStr = reader.readLine())) {
			if(!lineStr.contains("\""))	continue;
			k = Integer.parseInt(lineStr.substring(0, lineStr.indexOf(':')));
			lineStr = lineStr.substring(lineStr.indexOf('"') + 1, lineStr.length());
			strArr = lineStr.split("\"");
//			System.out.println(k + " > " + lineStr + " > ");
			bw.write(String.valueOf(k));
			bw.write(": ");
			for(String st : strArr) {
				if(!st.startsWith("^")) {
					if(st.startsWith("-")) st = "#" + st.substring(1);
					dateArr = st.split("-");
					
					year = dateArr[0];
					if(year.contains("-"))	year = year.replace('-', '0');
					if(year.contains("#"))	year = year.replace("#", "0");
//					if(Integer.parseInt(year) >2017) {
//						System.out.println(year + "太大   ");
//						return;
//					}
					
					month = dateArr[1];
					if(month.startsWith("#"))	month = "01";
					else if(month.length() ==2 && month.charAt(1)=='#')	month = month.replace("#", "0");
					
					day = dateArr[2];
					if(day.startsWith("#"))	day = "01";
					else if(day.length() ==2 && day.charAt(1)=='#')	day = day.replace("#", "0");
					
					bw.write(year + '-' + month + '-' + day + '#');
//					date2 = sdf.parse(year +'-' +  month + '-' + day);
//					if(date1.getTime() < date2.getTime())	date1 = date2;
				}
			}
			bw.write("\n");
			nodeNum++;
//			System.out.println();
		}
		reader.close();
		bw.flush();
		bw.close();
		
		RandomAccessFile raf = new RandomAccessFile(new File(LocalFileInfo.getDataSetPath() + "nodeIdStandardOnDateMapYagoVB.txt"), "rw");
		raf.seek(0);
//		raf.writeUTF(String.valueOf(nodeNum) + "#");
//		raf.writeChars(String.valueOf(nodeNum) + "#");
		raf.write((String.valueOf(nodeNum) + "#").getBytes());
		raf.close();
		System.out.println("> over写nodeIdStandardOnDateMapYagoVB.txt . . . ");
	}
	
	// 输出只含有节点的OnDate和坐标属性的节点txt文档 ： nodeIdCoordOnDateMapYagoVB.txt
	public Boolean writeNodeIdCoordOnDateMapTxt() throws Exception{
		System.out.println("> 开始写nodeIdCoordOnDateMapYagoVB.txt . . . ");
		HashMap<Integer, String> nodeMap = new HashMap<>();
		
		// 读pidCoordYagoVB.txt
		ZipBase64Reader zipReader = new ZipBase64Reader(LocalFileInfo.getDataSetPath() + "YagoVB.zip", "pidCoordYagoVB.txt");
		int k = 0;
		String lineStr = null;
		zipReader.readLine();
		while(null != (lineStr = zipReader.readLine())) {
			nodeMap.put(Integer.parseInt(lineStr.substring(0, lineStr.indexOf(':'))), lineStr);
		}
		zipReader.close();
		
		// 写nodeIdCoordOnDateMapYagoVB.txt
		BufferedWriter bw = new BufferedWriter(new FileWriter(LocalFileInfo.getDataSetPath() + "nodeIdCoordOnDateMapYagoVB.txt"));
		bw.write("      \n");	// 留出空位来写点数
		BufferedReader br = new BufferedReader(new FileReader(LocalFileInfo.getDataSetPath() + "nodeIdOnDateMapYagoVB.txt"));
		String tempStr = null;
		int nodeNum = 0;
		br.readLine();
		while(null != (lineStr = br.readLine())) {
			if(-1 != lineStr.indexOf('"')) {
				k = Integer.parseInt(lineStr.substring(0, lineStr.indexOf(':')));
				if(null != (tempStr = nodeMap.get(k))) {
					nodeNum++;
					bw.write(tempStr);
					bw.write(lineStr.substring(lineStr.indexOf('"'), lineStr.length()));
					bw.write('\n');
					nodeMap.remove(k);
				}
					
			}
		}
		br.close();
		bw.flush();
		bw.close();
		
		RandomAccessFile raf = new RandomAccessFile(new File(LocalFileInfo.getDataSetPath() + "nodeIdCoordOnDateMapYagoVB.txt"), "rw");
		raf.seek(0);
//		raf.writeUTF(String.valueOf(nodeNum) + "#");
//		raf.writeChars(String.valueOf(nodeNum) + "#");
		raf.write((String.valueOf(nodeNum) + "#").getBytes());
		raf.close();
		
		System.out.println("> 结束写nodeIdCoordOnDateMapYagoVB.txt . . . ");
		return Boolean.TRUE;
	}
	
	// 输出只含有节点的OnDate和keywordList的节点txt文档 ： nodeIdKeywordListOnDateMapYagoVB.txt
	public void writeNodeIdKeywordListOnDateMapYagoVBTxt() throws Exception{
		System.out.println("> 开始写nodeIdKeywordListOnDateMapYagoVB.txt . . . ");
		
		BufferedReader br = new BufferedReader(new FileReader(LocalFileInfo.getDataSetPath() + "nodeIdStandardOnDateMapYagoVB.txt"));
		int id;
		String lineStr = null;
		HashMap<Integer, String> nodeMap = new HashMap<>();
		br.readLine();
		while(null != (lineStr = br.readLine())) {
			nodeMap.put(Integer.parseInt(lineStr.substring(0, lineStr.indexOf(':'))), lineStr.substring(lineStr.indexOf(':')+1, lineStr.length()));
		}
		br.close();
		
		int nodeNum = 0;
		String dateStr = null;
		ZipBase64Reader zipReader = new ZipBase64Reader(LocalFileInfo.getDataSetPath() + "YagoVB.zip", "nidKeywordsListMapYagoVB.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(LocalFileInfo.getDataSetPath() + "nodeIdKeywordListOnDateMapYagoVB.txt"));
		bw.write("       \n");
		zipReader.readLine();
		while(null != (lineStr = zipReader.readLine())) {
			id = Integer.parseInt(lineStr.substring(0, lineStr.indexOf(':')));
			if(null != (dateStr = nodeMap.get(id))) {
				nodeMap.remove(id);
				bw.write(String.valueOf(id) + ":");
				bw.write(dateStr);
				bw.write(lineStr.substring(lineStr.indexOf(':') + 2));
				bw.write('\n');
				nodeNum++;
			}
		}
		zipReader.close();
		bw.flush();
		bw.close();
		
		int i = 0;
		for(Entry<Integer, String> en : nodeMap.entrySet()) {
			if(3 != i++)	System.out.println(en.getKey());
			else break;
		}
		
		RandomAccessFile raf = new RandomAccessFile(new File(LocalFileInfo.getDataSetPath() + "nodeIdKeywordListOnDateMapYagoVB.txt"), "rw");
		raf.seek(0);
//		raf.writeUTF(String.valueOf(nodeNum) + "#");
//		raf.writeChars(String.valueOf(nodeNum) + "#");
		raf.write((String.valueOf(nodeNum) + "#").getBytes());
		raf.close();
		
		System.out.println("> over写nodeIdKeywordListOnDateMapYagoVB.txt . . . ");
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
			ZipBase64Reader zrs = new ZipBase64Reader(vbZipPath, "nidKeywordsListMapYagoVB");
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
	
	// 输出包好OnDate属性的三元组
	public static void outputRDFOnDate(String zipFP, String onDateFp) throws Exception{
		String lineStr = null;
		LineDealService ldS = new LineDealService();
		ZipBase64Reader reader = new ZipBase64Reader(zipFP);
		BufferedWriter bw = IOUtility.getBW(onDateFp);
		System.out.println("-开始提取" + zipFP + "中包含的OnDate三元组 " + TimeUtility.getTime());
		try {
			do {
				System.out.println("处理文件 : " + reader.getCurZipEntryName());
				while(null != (lineStr = reader.readLine())) {
					if(lineStr.contains(("OnDate")))
						bw.write(lineStr + "\n");
				}
			} while (null != (reader.changeToNextZipEntry()));
			reader.close();
			bw.close();
			System.out.println("-Over处理" + zipFP + TimeUtility.getTime() + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception{
		
		String zipFP = LocalFileInfo.getDataSetPath() + "yago2s_ttl.zip";
		String onDateFp = LocalFileInfo.getDataSetPath() + "yago2s_onDateRDF.txt";
		NodeIdOnDateService.outputRDFOnDate(zipFP, onDateFp);
		
//		new NodeIdOnDateService().writeNodeIdKeywordListOnDateMapYagoVBTxt();
		
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
