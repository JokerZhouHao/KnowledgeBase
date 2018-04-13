package file.reader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import utility.LocalFileInfo;

public class ZipReader {
	private String filePath = null;
	private String entityName = null;
	private ZipFile zipFile = null;
	private BufferedReader bufferedReader = null;
	private InputStream zipInputStream = null;
	private Enumeration enties = null;
	private ZipEntry nowEntry = null;
	
	public ZipReader() {}
	
	public ZipReader(String filePath) {
		this.filePath = filePath;
		init();
	}
	
	public ZipReader(String filePath, String entityName) {
		this.filePath = filePath;
		this.entityName = entityName;
		init();
	}
	
	public void init() {
		try {
			zipFile = new ZipFile(new File(this.filePath));
			enties = zipFile.entries();
			while(enties.hasMoreElements()) {
				nowEntry = (ZipEntry)enties.nextElement();
				if(null==this.entityName) break;
				else if(nowEntry.getName().contains(this.entityName)){
					break;
				}
			}
			zipInputStream = zipFile.getInputStream(nowEntry);
			bufferedReader = new BufferedReader(new InputStreamReader(zipInputStream));
//			System.out.println(bufferedReader.readLine());
//			bufferedReader = new BufferedReader(new InputStreamReader(zipInputStream));
//			if(null!=this.entityName) {
//				System.out.println(zipInputStream.getNextEntry().getName());
//				System.out.println(zipInputStream.getNextEntry().getName());
//				System.out.println(zipInputStream.getNextEntry().getName());
////				while(!zipInputStream.getNextEntry().getName().contains(entityName));
//			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("> 文件不存在而退出！！！");
			System.exit(0);
		}
	}
	
	public Boolean setNextEntry() {
		if(enties.hasMoreElements()) {
			nowEntry = (ZipEntry)enties.nextElement();
			try {
				zipInputStream = zipFile.getInputStream(nowEntry);
				bufferedReader = new BufferedReader(new InputStreamReader(zipInputStream));
			} catch (Exception e) {
				e.printStackTrace();
				return Boolean.FALSE;
			}
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	public String getNowEntryName() {
		if(null != nowEntry)	return nowEntry.getName();
		return null;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public BufferedReader getBufferedReader() {
		return bufferedReader;
	}

	public void setBufferedReader(BufferedReader bufferedReader) {
		this.bufferedReader = bufferedReader;
	}

	public InputStream getZipInputStream() {
		return zipInputStream;
	}

	public void setZipInputStream(InputStream zipInputStream) {
		this.zipInputStream = zipInputStream;
	}

	public String readLine() {
		try {
			return bufferedReader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Boolean close() {
		try {
			if(null!=bufferedReader) bufferedReader.close();
			else zipInputStream.close();
			return Boolean.TRUE;
		} catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
	}
	
	public void getKeywordBlankZip() throws Exception{
//		ZipReaderService zrs = new ZipReaderService(LocalFileInfo.getNodeIdAndKeywordAndEdgeZipPath(), "keyword");
		ZipReader zrs = new ZipReader(LocalFileInfo.getBasePath() + "orginal_code\\data\\temp\\nodeIdAndKeywordAndEdge.zip", "keyword");
		String lineStr = zrs.readLine();
		System.out.println(lineStr);
		int keyNum = Integer.parseInt(lineStr.split("#")[0]);
		System.out.println("总node数：" + keyNum);
		ZipOutputStream zos = new ZipOutputStream(new CheckedOutputStream(new FileOutputStream(new File(LocalFileInfo.getBasePath() + "orginal_code\\data\\temp\\keywordBlank.zip")), new Adler32()));
		BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(zos));
		zos.putNextEntry(new ZipEntry("keywordBlank.txt"));
		bos.write(lineStr + "\n");
		int i = 0;
		while((lineStr=zrs.readLine())!=null) {
			bos.write(lineStr.replaceAll("\\[\\^\\\\\\]", " ") + "\n");
			if((++i)%20000000==0) System.out.println("已处理：" + i);
//			if(i==100) break;
		}
		System.out.println("共处理i = " + i);
		
//		int i = 0;
//		for(i=0; i<3; i++) {
//			String str = zrs.readLine();
//			System.out.println(str);
//			String[] strA = str.split("\\[\\^\\\\\\]");
//			for(String s : strA)
//				System.out.println(s);
//			String ss = str.replaceAll("\\[\\^\\\\\\]", " ");
//			System.out.println(ss + "\n");
//		}
		bos.close();
		zrs.close();
	}
	
	public void getEdgeBlankZip() throws Exception{
		ZipReader zrs = new ZipReader(LocalFileInfo.getNodeIdAndKeywordAndEdgeZipPath(), "edge");
		String lineStr = zrs.readLine();
		System.out.println(lineStr);
		int keyNum = Integer.parseInt(lineStr.split("#")[0]);
		System.out.println("总node数：" + keyNum);
		ZipOutputStream zos = new ZipOutputStream(new CheckedOutputStream(new FileOutputStream(new File(LocalFileInfo.getBasePath() + "orginal_code\\data\\temp\\edgeBlank.zip")), new Adler32()));
		BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(zos));
		zos.putNextEntry(new ZipEntry("keywordBlank.txt"));
		bos.write(lineStr + "\n");
		int i = 0;
		while((lineStr=zrs.readLine())!=null) {
//			System.out.println(lineStr);
			bos.write(lineStr.replaceAll(",", " ") + "\n");
			if((++i)%20000000==0) System.out.println("已处理：" + i);
//			if(i==1) break;
		}
		System.out.println("共处理i = " + i);
		
//		int i = 0;
//		for(i=0; i<3; i++) {
//			String str = zrs.readLine();
//			System.out.println(str);
//			String[] strA = str.split("\\[\\^\\\\\\]");
//			for(String s : strA)
//				System.out.println(s);
//			String ss = str.replaceAll("\\[\\^\\\\\\]", " ");
//			System.out.println(ss + "\n");
//		}
		bos.close();
		zrs.close();
	}
	
	public static void main(String[] args) throws Exception{
		
//		String pathString = LocalFileInfo.getBasePath() + "\\data\\DataSet\\yago2s_ttl.7z";
//		 try {
//	            SevenZFile sevenZFile = new SevenZFile(new File(pathString));
//	            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
//	            while (entry != null) {
//	                // System.out.println(entry.getName());
//	                if (entry.isDirectory()) {
//	                    new File(pathString + entry.getName()).mkdirs();
//	                    entry = sevenZFile.getNextEntry();
//	                    continue;
//	                }
//	                sevenZFile.get
//	                FileOutputStream out = new FileOutputStream(tarpath
//	                        + File.separator + entry.getName());
//	                byte[] content = new byte[(int) entry.getSize()];
//	                sevenZFile.read(content, 0, content.length);
//	                out.write(content);
//	                out.close();
//	                entry = sevenZFile.getNextEntry();
//	            }
//	            sevenZFile.close();
//	        } catch (FileNotFoundException e) {
//	            e.printStackTrace();
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
		
		
		
		
//		System.out.println(LocalFileInfo.getBasePath() + "\\data\\DataSet\\zipTest.zip");
//		String pathString = LocalFileInfo.getBasePath() + "\\data\\DataSet\\zipTest.zip";
//		ZipReaderService ser = new ZipReaderService(pathString);
//		do {
//			System.out.println(ser.readLine());
//			System.out.println(ser.getNowEntryName() + "\n");
//		}while(ser.setNextEntry());
//		ser.close();
		
		
		String pathString = LocalFileInfo.getBasePath() + "\\data\\DataSet\\zipTest.zip";
		org.apache.commons.compress.archivers.zip.ZipFile zf = new org.apache.commons.compress.archivers.zip.ZipFile(pathString);
		Enumeration<ZipArchiveEntry> enu = zf.getEntries();
		ZipArchiveEntry zae = enu.nextElement();
		ZipArchiveInputStream zais = new ZipArchiveInputStream(new FileInputStream(new File(pathString)));
		ZipEntry ze = null;
		
		BufferedReader br = null;
//		zais.getNextZipEntry();
//		System.out.println(zais.read());
//		System.out.println(zais.available());
		
//		System.out.println(zais.getNextZipEntry().getName());
//		System.out.println(zais.getNextZipEntry().getName());
		br = new BufferedReader(new InputStreamReader(zais));
		while(null != (ze = zais.getNextZipEntry())) {
			System.out.println("文件名 : " + ze.getName());
			br = new BufferedReader(new InputStreamReader(zais));
//			while(br.readLine().startsWith("#")) ;
//			System.out.println(br.readLine());
			int i = 0;
			String str = null;
			while((str=br.readLine()) != null) {
				System.out.println(str);
				if(1 == (++i))	break;
			}
			System.out.println("");
//			break;
		}
		
		br.close();
		zais.close();
		
		//		zais.
//		zais.
//		
//		ZipReaderService ser = new ZipReaderService(pathString);
		
//		do {
//			System.out.println(ser.readLine());
//			System.out.println(ser.getNowEntryName() + "\n");
//		}while(ser.setNextEntry());
//		ser.close();
		
//		ZipReaderService ser = new ZipReaderService(LocalFileInfo.getEdgeBlankZipPath());
//		ZipReaderService ser1 = new ZipReaderService(LocalFileInfo.getNodeIdAndKeywordAndEdgeZipPath(), "edge");
//		for(int i=0; i<2; i++) {
//			System.out.println(ser.readLine());
//			System.out.println(ser1.readLine());
//		}
//		ser.close();
//		ser.getEdgeBlankZip();
	}
}
