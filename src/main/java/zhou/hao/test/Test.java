package zhou.hao.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
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
		
//		ZipBase64ReaderService reader = new ZipBase64ReaderService(LocalFileInfo.getDataSetPath() + "n3.zip");
//		for(int i=0; i<100; i++)
//			System.out.println(reader.readLine());
//		reader.close();
		
		Test.displayOnDateLineOfFreebase();
		
		
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
