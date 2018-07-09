package entity.sp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import utility.Global;

/**
 * 提供处理所有pid和wid的相关方法
 * @author Monica
 *
 */
public class AllPidWid {
	
	private static DataOutputStream getDos(String fp) throws Exception{
		return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fp)));
	}
	
	private static DataInputStream getDis(String fp) throws Exception{
		return new DataInputStream(new BufferedInputStream(new FileInputStream(fp)));
	}
	
	/**
	 * 写所有pid
	 * @throws Exception
	 */
	public static Boolean writeAllPid() throws Exception{
		String line = null;
		BufferedReader br = new BufferedReader(new FileReader(Global.inputDirectoryPath + Global.pidFile));
		DataOutputStream dos = AllPidWid.getDos(Global.inputDirectoryPath + Global.allPidFile);
		br.readLine();
		while(null != (line = br.readLine())) {
			dos.writeInt(Integer.parseInt(line.substring(0, line.indexOf(Global.delimiterLevel1))));
		}
		br.close();
		dos.flush();
		dos.close();
		System.out.println("end");
		return Boolean.TRUE;
	}
	
	public static void deleteAllPidFile() throws Exception{
		if(new File(Global.inputDirectoryPath + Global.allPidFile).exists()) {
			new File(Global.inputDirectoryPath + Global.allPidFile).delete();
		}
	}
	
	/**
	 * 写所有wid
	 * @throws Exception
	 */
	public static Boolean writeWid() throws Exception{
		String line = null;
		BufferedReader br = new BufferedReader(new FileReader(Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile));
		String[] strArr = null;
		TreeSet<Integer> rec = new TreeSet<>();
		int wid = 0;
		br.readLine();
		while(null != (line = br.readLine())) {
			strArr = line.substring(line.lastIndexOf(Global.delimiterDate) + 1).split(Global.delimiterLevel2);
			for(String st : strArr) {
				wid = Integer.parseInt(st);
				if(!rec.contains(wid)) {
					rec.add(wid);
				}
			}
		}
		DataOutputStream dos = AllPidWid.getDos(Global.inputDirectoryPath + Global.allWidFile);
		for(int in : rec) {
			dos.writeInt(in);
		}
		br.close();
		dos.flush();
		dos.close();
		System.out.println("end");
		return Boolean.TRUE;
	}
	
	public static void deleteAllWidFile() throws Exception{
		if(new File(Global.inputDirectoryPath + Global.allWidFile).exists()) {
			new File(Global.inputDirectoryPath + Global.allWidFile).delete();
		}
	}
	
	/**
	 * 获得所有pid
	 * @return
	 */
	public static List<Integer> getAllPid() {
		List<Integer> li = new ArrayList<>();
		DataInputStream dis = null;
		try {
			File fp = new File(Global.inputDirectoryPath + Global.allPidFile);
			if(!fp.exists()) {
				writeAllPid();
			}
			dis = AllPidWid.getDis(Global.inputDirectoryPath + Global.allPidFile);
			while(true) {
				li.add(dis.readInt());
			}
		} catch (EOFException e) {
			System.out.println("> 读取" + Global.allPidFile + "完成");
			try {
				dis.close();
			} catch (Exception e2) {
			}
		} catch (Exception e) {
			System.err.println("> 读取" + Global.allPidFile + "失败");
			e.printStackTrace();
			System.exit(0);
		}
		return li;
	}
	
	/**
	 * 获得所有wid
	 * @return
	 */
	public static List<Integer> getAllWid() {
		List<Integer> li = new ArrayList<>();
		DataInputStream dis = null;
		try {
			File fp = new File(Global.inputDirectoryPath +Global.allWidFile);
			if(!fp.exists()) {
				writeWid();
			}
			dis = AllPidWid.getDis(Global.inputDirectoryPath + Global.allWidFile);
			while(true) {
				li.add(dis.readInt());
			}
		} catch (EOFException e) {
			System.out.println("> 读取" + Global.allWidFile + "完成");
			try {
				dis.close();
			} catch (Exception e2) {
			}
		} catch (Exception e) {
			System.err.println("> 读取" + Global.allWidFile + "失败");
			e.printStackTrace();
			System.exit(0);
		}
		return li;
	} 
	
	public static void main(String[] args) throws Exception {
		AllPidWid.writeAllPid();
		AllPidWid.writeWid();
//		System.out.println(AllPidWid.getAllPid().size());
//		List<Integer> wids = AllPidWid.getAllWid();
//		System.out.println(wids.size());
//		for(int i=0; i<20; i++) {
//			System.out.print(wids.get(i*i) + " ");
//		}
	}
}