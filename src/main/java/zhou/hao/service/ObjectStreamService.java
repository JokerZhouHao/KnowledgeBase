package zhou.hao.service;

import java.io.*;

public class ObjectStreamService {
	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;
	
	// 从文件中读取对象
	public Object read(String filePath) {
		File file = new File(filePath);
		try {
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
			return ois.readObject();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Object read() {
		if(null!=ois) {
			try {
				return ois.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	// 关闭对象流
	public void closeOis() {
		if(null!=ois) {
			try {
				ois.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// 写入对象
	public Boolean write(String filePath, Object o) {
		File file = new File(filePath);
		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			oos.writeObject(o);
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public Boolean write(Object o) {
		if(null!=oos) {
			try {
				oos.writeObject(o);
				oos.flush();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	// 关闭写入流
	public void closeOos() {
		if(null!=oos) {
			try {
				oos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
	}
}	
