package zhou.hao.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import zhou.hao.tools.LocalFileInfo;

public class ZipBase64ReaderService {
	
	private BufferedReader bufferedReader = null;
	private ZipArchiveInputStream zais = null;
	private ZipEntry curZipEntry = null;
	private String entryName = null;
	private String filePath = null;
	private ZipFile zipFile = null;
	
	public ZipBase64ReaderService() {}
	
	public ZipBase64ReaderService(String filePath) {
		this.filePath = filePath;
		init();
	}
	
	public ZipBase64ReaderService(String filePath, String entryName) {
		this.filePath = filePath;
		this.entryName = entryName;
		init();
	}
	
	public void init() {
		try {
			if(null != entryName) {
				zipFile = new ZipFile(new File(filePath));
				Enumeration<ZipArchiveEntry> enu = zipFile.getEntries();
				ZipArchiveEntry zae = null;
				boolean sign = false;
				while(enu.hasMoreElements()) {
					zae = enu.nextElement();
//					System.out.println(zae.getName() + "   " + entryName);
					if(zae.getName().contains(entryName)) {
						sign = true;
						break;
					}
				}
				if(sign) {
					bufferedReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(zipFile.getInputStream(zae))));
				} else {
					System.out.println("压缩包" + filePath + "不包含文件" + entryName);
				}
				return;
			}
			zais = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(new File(filePath))));;
			curZipEntry = zais.getNextZipEntry();
			bufferedReader = new BufferedReader(new InputStreamReader(zais));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getCurZipEntryName() {
		return curZipEntry.getName();
	}
	
	public BufferedReader getCurBufferedReader() {
		return bufferedReader;
	}
	
	public ZipEntry changeToNextZipEntry() {
		try {
			curZipEntry = zais.getNextZipEntry();
			if(null==curZipEntry)	return null;
			bufferedReader = new BufferedReader(new InputStreamReader(zais));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return curZipEntry;
	}
	
	public String readLine() {
		try {
			return bufferedReader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Boolean close() {
		try {
			if(null != bufferedReader)	bufferedReader.close();
			if(null != zais)	zais.close();
			if(null != zipFile)	zipFile.close();
		} catch (Exception e) {
			e.printStackTrace();
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	public static void main(String[] args) throws Exception{
		
//		ZipBase64ReaderService rs = new ZipBase64ReaderService(LocalFileInfo.getBasePath() + "\\data\\DataSet\\yago2s_ttl.zip");
//		Long totalLen = 0L;
//		do {
//			System.out.println("当前文件 ：" + rs.getCurZipEntryName());
//			int i = 0;
//			String str = null;
//			
//			while(null != (str = rs.readLine())) {
//				totalLen = totalLen + str.getBytes().length + 1;
//				if(str.contains("<El_Maïbiaa>")) {
//					System.out.println(str);
////					if(20==(++i))	
//						break;
//				}
////				System.out.println(str);
////				if(50==(++i))	break;
//			}
//			System.out.println("\n\n");
//		}while(null != rs.changeToNextZipEntry());
//		rs.close();
		
//		System.out.println(totalLen);
		
		ZipBase64ReaderService rs = new ZipBase64ReaderService(LocalFileInfo.getBasePath() + "\\data\\DataSet\\YagoVB.zip", "nodeIdMapYagoVB.txt");
		System.out.println(rs.readLine());
		
		
		rs.close();
		
		
//		ZipBase64ReaderService rs = new ZipBase64ReaderService(LocalFileInfo.getBasePath() + "\\data\\DataSet\\zipTest.zip", "3");
//		BufferedReader reader = rs.getCurBufferedReader();
//		System.out.println(rs.getCurZipEntryName());
//		System.out.println(reader.readLine());
//		System.out.println(reader.readLine());
//		rs.close();
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	private BufferedReader bufferedReader = null;
//	private InputStream inputS = null;
//	private ZipFile zipFile= null;
//	private Enumeration<ZipArchiveEntry> zipEntryEnum = null;
//	private ZipArchiveEntry curZipEntry = null;
//	private String entryName = null;
//	private String filePath = null;
//	
//	public ZipBase64ReaderService() {}
//	
//	public ZipBase64ReaderService(String filePath) {
//		this.filePath = filePath;
//		init();
//	}
//	
//	public ZipBase64ReaderService(String filePath, String entityName) {
//		this.filePath = filePath;
//		this.entryName = entryName;
//		init();
//	}
//	
//	public void init() {
//		try {
//			
//			zipFile = new ZipFile(new File(filePath));
//			zipEntryEnum = zipFile.getEntries();
//			curZipEntry = zipEntryEnum.nextElement();
//			if(null!=curZipEntry) {
//				if(null != entryName) {
//					do {
//						if(curZipEntry.getName().contains(entryName))	break;
//					}while(null != (curZipEntry = zipEntryEnum.nextElement()));
//					if(null==curZipEntry)	{
//						System.out.println("压缩包" + filePath + "不包含文件" + entryName);
//						return;
//					}
//				}
//				inputS = zipFile.getInputStream(curZipEntry);
//				bufferedReader = new BufferedReader(new InputStreamReader(inputS));
//			} else {
//				System.out.println("压缩包" + filePath + "无文件 ！");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public String getCurZipEntryName() {
//		return curZipEntry.getName();
//	}
//	
//	public BufferedReader getCurBufferedReader() {
//		return bufferedReader;
//	}
//	
//	public ZipEntry changeToNextZipEntry() {
//		try {
//			if(zipEntryEnum.hasMoreElements())	
//				curZipEntry = zipEntryEnum.nextElement();
//			else return null;
//			
//			inputS = zipFile.getInputStream(curZipEntry);
//			bufferedReader = new BufferedReader(new InputStreamReader(inputS));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return curZipEntry;
//	}
//	
//	public Boolean close() {
//		try {
//			if(null != bufferedReader)	bufferedReader.close();
//			if(null != inputS)	inputS.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return Boolean.TRUE;
//		}
//		return Boolean.FALSE;
//	}
//	
//	public static void main(String[] args) throws Exception{
//		ZipBase64ReaderService rs = new ZipBase64ReaderService(LocalFileInfo.getBasePath() + "\\data\\DataSet\\zipTest.zip");
//		BufferedReader reader = null;
//		do {
//			reader = rs.getCurBufferedReader();
//			System.out.println("当前文件 ：" + rs.getCurZipEntryName());
//			int i = 0;
////			while(50!=(++i))
//			String str = null;
////			while(null != (str = reader.readLine()))
//				System.out.println(reader.readLine());
//			System.out.println("\n");
//		}while(null != rs.changeToNextZipEntry());
//	}
}
