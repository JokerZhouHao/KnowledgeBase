package file.reader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import utility.LocalFileInfo;

public class GZIPReader {
	private String sourcePath = null;
	private BufferedReader br = null;
	
	public GZIPReader(String sour) {
		this.sourcePath = sour;
	}
	
	// 打开文件流
	public Boolean open() {
		try {
			File file = new File(this.sourcePath);
			FileInputStream fis = new FileInputStream(file); 
			BufferedInputStream bis = new BufferedInputStream(fis);
			GZIPInputStream gzis = new GZIPInputStream(bis);
			br = new BufferedReader(new InputStreamReader(gzis));
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	// 读取一行数据
	public String readLine() {
		if(null == this.br) this.open();
		String str = null;
		try {
			str = this.br.readLine();
		} catch (Exception e) {
			e.printStackTrace();
			return "readLine-E";
		}
		return str;
	}
	
	// 关闭文件流
	public Boolean close() {
		try {
			this.br.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) throws Exception{
		String gzipDataFilePath = LocalFileInfo.get("gzipDataFilePath");
		System.out.println(gzipDataFilePath);
		GZIPReader reader = new GZIPReader(gzipDataFilePath);
		int i = 0;
		while(i!=5) {
			System.out.println(reader.readLine());
			i++;
		}
		reader.close();
	}
}
