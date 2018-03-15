package zhou.hao.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.ArchiveUtils;
import org.apache.commons.compress.utils.BoundedInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.lucene.analysis.CharArrayMap.EntrySet;

import zhou.hao.entry.IndividualBlockingQueue;
import zhou.hao.entry.Node;
import zhou.hao.entry.Pair;
import zhou.hao.helper.MComparator;
import zhou.hao.service.GZIPReaderService;
import zhou.hao.service.GoogleFreebaseLineProcessService;
import zhou.hao.service.IndexNodeMapService;
import zhou.hao.service.NodeIdHashMapWriteServiceList;
import zhou.hao.service.ZipBase64ReaderService;
import zhou.hao.service.ZipReaderService;
import zhou.hao.service.ZipWriterService;
import zhou.hao.tools.LocalFileInfo;

public class Test {
	
	public static void main(String[] args) throws Exception{
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		Date date = sdf.parse("1-1-1");
//		System.out.println(sdf.format(new Date(-62135798400000L)));
//		System.out.println(date.getTime());
		BufferedReader reader = new BufferedReader(new FileReader(new File(LocalFileInfo.getDataSetPath() + "nodeIdOnDateMapYagoVB.txt")));
		reader.readLine();
		String lineStr = null;
		int k;
		String strArr[] = null;
		String dateArr[] = null;
		String year, month, day;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		while(null != (lineStr = reader.readLine())) {
			k = Integer.parseInt(lineStr.substring(0, lineStr.indexOf(':')));
			lineStr = lineStr.substring(lineStr.indexOf('"') + 1, lineStr.length());
			strArr = lineStr.split("\"");
			System.out.print(k + " > " + lineStr + " > ");
			for(String st : strArr) {
				if(!st.startsWith("^")) {
					if(st.startsWith("-")) st = "#" + st.substring(1);
					dateArr = st.split("-");
					year = dateArr[0];
					if(year.contains("-"))	year = year.replace('-', '0');
					if(year.contains("#"))	year = year.replace("#", "0");
					month = dateArr[1];
					if(month.startsWith("#"))	month = "01";
					else if(month.charAt(1)=='#')	month = month.replace("#", "0");
					day = dateArr[2];
					if(day.startsWith("#"))	day = "01";
					else if(day.charAt(1)=='#')	day = day.replace("#", "0");
					System.out.print(sdf1.format(sdf.parse(year +'-' +  month + '-' + day)) + " ");
				}
			}
			System.out.println();
		}
		reader.close();
		
//		String lineStr = "5610: 40.8 -81.93333333333334\"1808-##-##\"^^xsd:date~";
//		String strArr[]  = null;
//		lineStr = lineStr.substring(lineStr.indexOf('"') + 1, lineStr.length());
//		strArr = lineStr.split("\"");
//		for(String st : strArr) {
//			if(!st.startsWith("^"))
//				System.out.println("  " + st);
//		}
		
		
		
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		Date date = sdf.parse("2002-##-##");
//		Date date1 = new Date(date.getTime());
//		System.out.println(date.getTime());
//		System.out.println(sdf.format(date1));
		
		
		
//		ZipBase64ReaderService re1 = new ZipBase64ReaderService(LocalFileInfo.getDataSetPath() + "yagoVB.zip", "nodeIdMapYagoVB.txt");
//		ZipBase64ReaderService re2 = new ZipBase64ReaderService(LocalFileInfo.getDataSetPath() + "yagoVB.zip", "nodeIdMapYagoVB.txt");
//		re1.readLine();
//		re2.readLine();
//		String s1 = null, s2 = null;
//		for(int i = 0; i<4774796; i++) {
//			s1 = re1.readLine();
//			s2 = re2.readLine();
//			if(!s1.equals(s2))	break;
//		}
//		System.out.println(s1 + "     " + s2);
//		ArrayList<Integer> lit = new ArrayList<>();
//		lit.add(4);
//		System.out.println(lit);
		
		
//		boolean sign[] = new boolean[100];
//		for(int i=0; i<10; i++)
//			System.out.println(sign[i]);
		
		
//		ArrayList<Integer> lit = new ArrayList<>();
//		lit.add(4);
//		lit.add(2);
//		lit.add(9);
//		lit.sort(new MComparator<Integer>());
//		for(int i=0; i<lit.size(); i++)
//			System.out.println(lit.get(i));
		
//		TreeSet<Integer> ts = new TreeSet<>();
//		ts.add(32);
//		ts.add(11);
//		ts.add(44);
//		ts.add(11);
//		ArrayList<Integer> li = new ArrayList<>(ts);
//		for(int i : li)
//			System.out.println(i);
		
//		ZipBase64ReaderService reader = new ZipBase64ReaderService(LocalFileInfo.getDataSetPath() + "n3.zip");
//		for(int i=0; i<100; i++)
//			System.out.println(reader.readLine());
//		reader.close();
		
//		Test.displayOnDateLineOfFreebase();
		
//		ArrayList<String> li = new ArrayList<>();
//		li.add(null);
		
		
//		String filePath = LocalFileInfo.getBasePath() + "\\data\\DataSet\\tt.7z";
//		String filePath = LocalFileInfo.getBasePath() + "\\data\\DataSet\\COMPRESS-256.7z";
//		String filePath = LocalFileInfo.getBasePath() + "\\data\\DataSet\\zipTest.zip";
//		ArchiveOutputStream ais = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.SEVEN_Z, new FileOutputStream(new File(filePath)));
//		CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.BROTLI, new FileInputStream(new File(filePath)));
//		ArchiveEntry ae = null;
//		while(null != (ae = ais.getNextEntry())) {
//			System.out.println(ae.getName());
//			System.out.println(ais.getBytesRead());
//			BufferedReader br = new BufferedReader(new InputStreamReader(ais));
//			String lineStr = null;
////			while(null != (lineStr = br.readLine()))	
//				lineStr = br.readLine();
//				System.out.println(lineStr);
//			System.out.println();
//		}
//		System.out.println(ais);
//		System.out.println(ais.getBytesRead());
		
//		ais.close();
		
		
		
//		SevenZArchiveEntry ent = null;
//		String filePath = LocalFileInfo.getBasePath() + "\\data\\DataSet\\yago2s_ttl.7z";
//		SevenZFile sevenZFile = new SevenZFile(new File(filePath));
////		ArArchiveInputStream is = new Ar
//		
//		Iterator<SevenZArchiveEntry> enu = sevenZFile.getEntries().iterator();
//		byte[] bys = new byte[100];
//		ent = sevenZFile.getNextEntry();
////		System.out.println(ent.);
//		
//		sevenZFile.read(bys);
//		sevenZFile.
//		System.out.println(ent.hasStream());
//		System.out.println(new String(bys));
//		System.out.println();
//		sevenZFile.close();
//		System.out.println(ent.hasStream());
//		displayNLineOfFreebase();
//		System.out.println("onDate".contains("Da"));
//		displayOnDateLineOfFreebase();
	}
	
	public static void displayOnDateLineOfFreebase() {
		String pathStr = LocalFileInfo.getBasePath() + "\\data\\DataSet\\Google-freebase-rdf-latest.gz";
		GZIPReaderService ser = new GZIPReaderService(pathStr);
		int i = 0;
		String str = null;
		while(null != (str = ser.readLine())) {
			if(str.contains("OnDate"))	System.out.println(str);
		}
		ser.close();
	}
	
	
	public static void displayNLineOfFreebase() {
		String pathStr = LocalFileInfo.getBasePath() + "\\data\\DataSet\\Google-freebase-rdf-latest.gz";
		GZIPReaderService ser = new GZIPReaderService(pathStr);
		int i = 0;
		while(true) {
			System.out.println(ser.readLine());
			if(200 == (++i))	break;
		}
		ser.close();
	}
	
	public static String displayNodeName(int nodeId) {
		ZipReaderService reader = new ZipReaderService(LocalFileInfo.getNodeIdAndKeywordAndEdgeZipPath(), "nodeId");
		System.out.println(reader.readLine());
		for(int i=0; i<nodeId; i++)	reader.readLine();
		String s = reader.readLine();
		System.out.println(s);
		reader.close();
		return s;
	}
	
	public static void displayNodeEdge(int nodeId) {
		ZipReaderService reader = new ZipReaderService(LocalFileInfo.getNodeIdAndKeywordAndEdgeZipPath(), "edge");
		System.out.println(reader.readLine());
		for(int i=0; i<nodeId; i++)	reader.readLine();
		System.out.println("edgeNum = " + reader.readLine().split(",").length);
		reader.close();
		
	}
	
	public static int getNodeEdgeNum(String nodeName) {
		GZIPReaderService reader = new GZIPReaderService(LocalFileInfo.getGzipDataFilePath());
		int num = 0;
		String str = null;
		while(null!=(str=reader.readLine())) {
			if(str.contains(nodeName)) {
				num++;
//				if(num%10==0)	System.out.println("num = " + num);
			}
		}
		return num;
	}
	
	public static int getNodeEdgeNum(int nodeNum) {
		ZipReaderService reader = new ZipReaderService(LocalFileInfo.getNodeIdAndKeywordAndEdgeZipPath(), "edge");
		reader.readLine();
		for(int i=0; i<nodeNum; i++)	reader.readLine();
		return reader.readLine().split(",").length;
	}
	
	public static void statistic(int maxNodeId) {
		int baseNum[] = new int[10];
		int recNum[] = new int[10];
		int i = 0, j = 0, k = 0, size = 0;
		for(i=0; i<10; i++) {
			baseNum[i] = (int)Math.pow(10, i);
			recNum[i] = 0;
		}
		ZipReaderService reader = new ZipReaderService(LocalFileInfo.getNodeIdAndKeywordAndEdgeZipPath(), "edge");
		System.out.println(reader.readLine());
		String lineStr = null;
		int edgeNum = 0;
		int readedLineNum = 0;
		String strArr[] = null;
		while(null!=(lineStr = reader.readLine())) {
			edgeNum = 0;
			if(!lineStr.equals("")) {
				if(maxNodeId==-1) edgeNum = lineStr.split(",").length;
				else {
					strArr = lineStr.split(",");
					size = strArr.length;
					for(k=0; k<size; k++) {
						if(Integer.parseInt(strArr[k])<=maxNodeId)	edgeNum++;
					}
				}
//				System.out.println(edgeNum);
				for(j=0; j<10; j++) {
					if(edgeNum<baseNum[j])	break;
					else recNum[j]++;
				}
			}
			if(maxNodeId == ++readedLineNum)  break;
		}
		if(-1==maxNodeId) {
			System.out.println("> 统计如下：");
		} else {
			System.out.println("> 在节点id为0--" + maxNodeId + "之间统计如下:");
		}
		for(i=0; i<10; i++) {
			System.out.println("边数不小于" + baseNum[i] + "的点有" + recNum[i] + "个");
		}
		
		reader.close();
	}
	
}
