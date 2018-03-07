package zhou.hao.test;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import zhou.hao.service.GZIPReaderService;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.ReadLine;

public class ReadFreeBaseGz {
	
	// 读取指定个数的字符, 并写入到指定文件
	public boolean writeToFile(int n, String sourcePath, String targetPath) throws Exception{
		// 创建源文件的输入流
		File sourceFile = new File(sourcePath);
		FileInputStream sourceInputStreamFile = new FileInputStream(sourceFile);
		GZIPInputStream gzipSourceFileInputStream = new GZIPInputStream(sourceInputStreamFile);
		
		// 创建目标文件输出流
		File targetFile = new File(targetPath);
		if(!targetFile.exists())
			if(!targetFile.createNewFile())	return false;
		FileOutputStream targetOutputStream = new FileOutputStream(targetFile);
		int temp = 0;
		int readNum = 0;
		while((temp = gzipSourceFileInputStream.read())!=-1 && readNum <= n) {
			targetOutputStream.write(temp);
			readNum++;
		}
		
		// 关闭输入流
		gzipSourceFileInputStream.close();
		targetOutputStream.close();
		return true;
	}
	
	// 查找字符串
	public static boolean findStr(String findedStr, int nLines) throws Exception{
		long startTime = System.nanoTime();
		long endTime = System.nanoTime();
		String gzipDataFilePath = LocalFileInfo.get("gzipDataFilePath");
		System.out.println(gzipDataFilePath);
		GZIPReaderService reader = new GZIPReaderService(gzipDataFilePath);
		boolean hasBlankNode = false, hasComment = false;
		//System.out.println(Integer.MAX_VALUE);
		int curLine = 0;
		String tempStr = null;
		long totalLines = 0;
		long tLines = 0;
		while(null != (tempStr = reader.readLine())) {
//			System.out.println(tempStr);
//			Thread.sleep(1000);
			tLines = totalLines;
			totalLines++;
			if(tLines>totalLines) {
				System.out.println("tLines = " + tLines + " " + "totalLines = " + totalLines);
				System.out.println("The number of T-triples over the bound of long");
				endTime = System.nanoTime();
				System.out.println("toal time " + (endTime-startTime)/1000000000/3600 + "h" + (endTime-startTime)/1000000000%3600/60 + 
						"m" + (endTime-startTime)/1000000000%3600%60 + "s");
				reader.close();
				return false;
			}
			if(!hasBlankNode || !hasComment) {
				if(tempStr.startsWith("_:")) {
					System.out.println(totalLines + " _:--->" + tempStr);
					hasBlankNode = true;
				}
				if(tempStr.startsWith("#")) {
					System.out.println(totalLines + " #--->" + tempStr);
					hasComment = true;
				}
			}
			
		}
		reader.close();
		System.out.println("total line " + totalLines);
		endTime = System.nanoTime();
		System.out.println("toal time " + (endTime-startTime)/1000000000/3600 + "h" + (endTime-startTime)/1000000000%3600/60 + 
				"m" + (endTime-startTime)/1000000000%3600%60 + "s");
//		
//		while(curLine++<nLines) {
//			tempStr = reader.readLine();
//			if(tempStr.contains(findedStr)) {
//				System.out.println(tempStr);
//				return true;
//			}
//		}
		return false;
		
	}
	
	
	// 创建gzip测试文件
	public static void createTestGzip(int n) {
		BufferedWriter zipWriter = null;
		System.out.println(LocalFileInfo.getGzipDataFilePath());
		GZIPReaderService lineReader = new GZIPReaderService(LocalFileInfo.getGzipDataFilePath());
		try {
			File zipFile = new File(LocalFileInfo.getTestGzipPath());
			if(zipFile.exists()) zipFile.delete();
			zipFile.createNewFile();
			zipWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(zipFile))));
			for(int i=0; i<n; i++) {
				String str = lineReader.readLine() + "\n";
				//System.out.print(str);
				zipWriter.write(str);
			}
			zipWriter.flush();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			zipWriter.close();
			lineReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// 获得gzip文件的总行数
	public static Long getTotalLine(String filePath) {
		Long num = 0L;
		GZIPReaderService grs = new GZIPReaderService(filePath);
		while(null != grs.readLine()) {
			//System.out.println(grs.readLine());
			num++;
		}
		return num;
	}
	
	
	public static void main(String[] args) throws Exception{
		
//		Long totalLine = ReadFreeBaseGz.getTotalLine(LocalFileInfo.getGzipDataFilePath());
//		System.out.println(LocalFileInfo.getGzipDataFilePath() + "总行：" + totalLine);
		
		// 创建gzip测试文件
		ReadFreeBaseGz.createTestGzip(20);
		System.err.println("创建完成");
		
		//ReadFreeBaseGz.findStr("_:", 1900000000);
		
		// 写入文件测试
//		String sourcePath = "F:" + File.separator + "mask" +  File.separator + "KnowledgeBase" +  File.separator + 
//							"data" + File.separator + "Google-freebase-rdf-latest.gz";
//		String targetPath = "F:" + File.separator + "mask" + File.separator + "KnowledgeBase" + File.separator + 
//				"code" + File.separator + "data" + File.separator + "fragment-Google-freebase-rdf-latest.txt";
//		System.out.println(sourcePath + "\n" + targetPath);
//		if(false == new ReadFreeBaseGz().writeToFile(10000000, sourcePath, targetPath))
//			System.out.println("写入失败");
//		else System.out.println("写入成功");
	}

}

























