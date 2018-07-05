package entity.sp.reach;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

import java.util.Map.Entry;

import utility.Global;
import utility.IOUtility;
import utility.TimeUtility;

/**
 * 翻转文件recPidToWidReach.gz，以获得recWidToPidReach.gz
 * @author Monica
 *
 */
public class W2PReachWriter extends Thread{
	
	public static long startTime = System.currentTimeMillis();
	public static Object object = new Object();
	private ArrayBlockingQueue<Object> signQueue = null;
	public int start = 0;
	public int end = 0;
	private String pTwFile  = null;
	
	public W2PReachWriter(ArrayBlockingQueue<Object> qu, int s, int e) {
		this.signQueue = qu;
		this.start = s;
		this.end = e;
	}
	
	public W2PReachWriter(ArrayBlockingQueue<Object> qu, String fp) {
		this.signQueue = qu;
		this.pTwFile = fp;
	}
	
	public void run() {
		try {
			writeWidToPidFile();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void writeWidToPidFile() throws Exception{
		DataInputStream dis = null;
		Map<Integer, Set<Integer>> rec = new TreeMap<>();
		Set<Integer> pids = null;
		int pid, wid, wid_num, i;
		try {
			if(pTwFile == null)	pTwFile = Global.recPidWidReachPath + "." + String.valueOf(start) + "." + String.valueOf(end);
			System.out.println("> 开始读取文件" + pTwFile + " . . . " + TimeUtility.getTime());
			dis = IOUtility.getDis(pTwFile);
			while(true) {
				pid = dis.readInt();
				wid_num = dis.readInt();
				for(i=0; i<wid_num; i++) {
					wid = dis.readInt();
					if(null == (pids = rec.get(wid))) {
						pids = new HashSet<>();
						rec.put(wid, pids);
					}
					pids.add(pid);
				}
			}
		} catch(EOFException e) {
			try {
				dis.close();
			} catch (Exception e2) {
				e2.printStackTrace();
				System.exit(0);
			}
			System.out.println("> 完成读取文件" + pTwFile + ", 用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()) + ". " + TimeUtility.getTime());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		// 写文件
		if(!pTwFile.contains("rtree")) pTwFile = Global.recWidPidReachPath + "." + String.valueOf(start) + "." + String.valueOf(end);
		else {
			pTwFile = Global.recWidPidReachPath + ".rtree";
		}
		System.out.println("> 开始写文件" + pTwFile + " . . . " + TimeUtility.getTime());
		DataOutputStream dos = IOUtility.getDos(pTwFile);
		
		// 写所有wid
		dos.writeInt(rec.size());
		for(int in : rec.keySet()) {
			dos.writeInt(in);
		}
		// 写所有pid
		for(Set<Integer> vSet : rec.values()) {
			dos.writeInt(vSet.size());
			for(int in : vSet) {
				dos.writeInt(in);
			}
		}
		
		// 清理内存
		for(Set<Integer> vSet : rec.values()) {
			vSet.clear();
		}
		rec.clear();
		
		dos.close();
		signQueue.put(object);
		System.out.println("> 完成写文件" + pTwFile + ", 用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()) + ". " + TimeUtility.getTime());
	}

	public static void buildingWPReach() throws Exception{
		System.out.println("> 开始反转recPidToWidReach.bin . . . " + TimeUtility.getTime());
		System.out.println("> 开始反转pid . . . " + TimeUtility.getTime());
		int start = 0, end = 0;
		int span = P2WReach.zipContianNodeNum;
		int zipNum = P2WReach.zipNum;
		ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(20);
		int i = 0;
		for(i=0; i<20; i++) {
			start = end;
			end += span;
			if(end >= Global.numPid)	break;
			new W2PReachWriter(queue, start, end).start();
		}
		if(end >= Global.numPid)	end = start;
		while(true) {
			queue.take();
			zipNum--;
			if(zipNum == 0)	break;
			if(end < Global.numPid) {
				start = end;
				end += span;
				if(end > Global.numPid)	end = Global.numPid;
				new W2PReachWriter(queue, start, end).start();
			}
		}
		System.out.println("\n\n> 成功反转所有的pid !!! " + TimeUtility.getTailTime() + "\n");
		System.out.println("> 开始反转rtree node . . . ");
		new W2PReachWriter(queue, Global.recPidWidReachPath + ".rtree").start();
		queue.take();
		System.out.println("> 成功反转rtree node ！！！ " + TimeUtility.getTailTime());
		System.out.println("> 成功反转recPidToWidReach.bin ！ ！！ " + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()) + "。 " + TimeUtility.getTime());
	}
	
	public static void main(String[] args) throws Exception{
		W2PReachWriter.buildingWPReach();
		
//		DataInputStream dis = IOUtility.getDis(Global.recWidPidReachPath + ".rtree");
//		int nid = 0;
//		int size = 0, wid = 0;
//		int k = 10, k0 = 5;
//		int num1, num2;
//		num1 = dis.readInt();
//		for(int i=0; i<num1; i++) {
//			if(0!=k)	{
//				System.out.print(dis.readInt() + " ");
//				k--;
//			} else {
//				dis.readInt();
//			}
//		}
//		System.out.println();
//		k = 10;
//		while(k != 0) {
//			k--;
//			size = dis.readInt();
//			num1 = 1;
//			for(int i=0; i<size; i++) {
//				if(num1==1) {
//					num1 = 2; 
//					System.out.println(dis.readInt());
//				}
//				else dis.readInt();
//			}
////			wid = dis.readInt();
////			System.out.print(wid + " : ");
////			size = dis.readInt();
////			k0 = 5;
////			for(int i=0; i<size; i++) {
////				nid = dis.readInt();
////				if(k0 != 0) {
////					k0--;
////					System.out.print(nid + " ");
////				}
////			}
////			System.out.println();
//		}
//		dis.close();
	}
}
