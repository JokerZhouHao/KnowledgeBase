package entity.sp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
	public static void writeAllPid() throws Exception{
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
	}
	
	/**
	 * 写所有wid
	 * @throws Exception
	 */
	public static void writeWid() throws Exception{
		String line = null;
		BufferedReader br = new BufferedReader(new FileReader(Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile));
		DataOutputStream dos = AllPidWid.getDos(Global.inputDirectoryPath + Global.allWidFile);
		String[] strArr = null;
		HashSet<Integer> rec = new HashSet<>();
		int wid = 0;
		br.readLine();
		while(null != (line = br.readLine())) {
			strArr = line.substring(line.lastIndexOf(Global.delimiterDate) + 1).split(Global.delimiterLevel2);
			for(String st : strArr) {
				wid = Integer.parseInt(st);
				if(!rec.contains(wid)) {
					dos.writeInt(wid);
					rec.add(wid);
				}
				
			}
		}
		br.close();
		dos.flush();
		dos.close();
		System.out.println("end");
	}
	
	/**
	 * 获得所有pid
	 * @return
	 */
	public static List<Integer> getAllPid() {
		List<Integer> li = new ArrayList<>();
		DataInputStream dis = null;
		try {
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
//		AllPidWid.writeAllPid();
//		AllPidWid.writeWid();
		System.out.println(AllPidWid.getAllPid().size());
		System.out.println(AllPidWid.getAllWid().size());
	}
}





















