package test;

import java.io.Serializable;

import entity.freebase.GoogleFreebaseMap;
import file.writer.freebase.GoogleFreebaseMapWriteService;
import file.ObjectStreamService;
import utility.LocalFileInfo;

class T1 implements Serializable{
	private String name = "wwwwwwwww";
	private T1 next = null;
	
	public T1() {}
	public T1(String s) {
		this.name = s;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public T1 getNext() {
		return next;
	}

	public void setNext(T1 next) {
		this.next = next;
	}
	
}

public class ObjectStreamTest {
	
	public static void main(String[] args) {
		ObjectStreamService oss = new ObjectStreamService();
		GoogleFreebaseMap gfm = (GoogleFreebaseMap)oss.read(LocalFileInfo.getTestObjectStreamPath());
		oss.closeOis();
//		new GoogleFreebaseMapWriteService(gfm).writeMap(gfm.getPrefixMap());
		
//		T1 t11 = new T1();
//		t11.setNext(new T1("sssss"));
//		ObjectStreamService oss = new ObjectStreamService();
//		oss.write(LocalFileInfo.getTestObjectStreamPath(), t11);
//		oss.closeOos();
//		T1 t12 = (T1)oss.read(LocalFileInfo.getTestObjectStreamPath());
//		oss.closeOis();
//		System.out.println(t12.getName() + " " + t12.getNext().getName());
	}
	
	
}
