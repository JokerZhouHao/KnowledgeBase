package zhou.hao.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ReadLine {
	private String path = null;
	private BufferedReader br = null;
	
	public ReadLine(String path) {
		super();
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	private Boolean open() {
		try {
			br = new BufferedReader(new FileReader(new File(path)));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public String readLine() {
		if(null==br) this.open();
		try {
			String lineStr = br.readLine();
			if(null!=lineStr)	return lineStr;
			else
				br.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public Boolean close() {
		try {
			this.br.close(); 
			return true;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
