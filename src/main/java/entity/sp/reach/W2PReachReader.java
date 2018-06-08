package entity.sp.reach;

import java.io.DataInputStream;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

import utility.Global;
import utility.IOUtility;
import utility.TimeUtility;

/**
 * 读取recWidToPidReach.gz文件
 * @author Monica
 * @since 2018/6/7
 */
public class W2PReachReader extends Thread{
	private static long startTime = System.currentTimeMillis();
	private ArrayBlockingQueue<Integer> widQueue = null;
	private ArrayBlockingQueue<List<Integer>> pidsQueue = null;
	private int start = 0;
	private int end = 0;
	private int[] wids = null;
	private static List<Integer> signNoList = null;
	private static List<Integer> signReadOver = null;
	
	public W2PReachReader(ArrayBlockingQueue<Integer> widQueue, ArrayBlockingQueue<List<Integer>> pidsQueue, int start, int end) {
		this.widQueue = widQueue;
		this.pidsQueue = pidsQueue;
		this.start = start;
		this.end = end;
		
		if(signNoList==null) {
			signNoList = new ArrayList<>();
			signNoList.add(-1);
			signReadOver = new ArrayList<>();
			signReadOver.add(-2);
		}
	}
	
	public void run() {
		DataInputStream dis = null;
		String fp =Global.recWidPidReachPath + "." + String.valueOf(start) + "." + String.valueOf(end);
		System.out.println("> 开始读文件" + fp + ". . . " + TimeUtility.getTime());
		int nWid = 0;
		try {
			dis = IOUtility.getDis(fp);
			
			int num = dis.readInt();
			wids = new int[num];
			int i = 0;
			for(i=0; i<num; i++) {
				wids[i] = dis.readInt();
			}
			
			int curIndex = 0;
			int curWid = wids[curIndex];
			List<Integer> pids = null;
			
			while(true) {
				if(null == pids) {
					pids = new ArrayList<>();
					num = dis.readInt();
					for(i=0; i<num; i++) {
						pids.add(dis.readInt());
					}
				}
				
				nWid = widQueue.take();
				
				if(curWid == nWid) {
					pidsQueue.put(pids);
					pids = null;
					curIndex++;
					if(curIndex == wids.length)	break;
					curWid = wids[curIndex];
				} else {
					pidsQueue.put(signNoList);
				}
			}
		} catch(EOFException e) {
			System.err.println("> EOFException进入文件" + fp + "而退出！！！");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		try {
			dis.close();
			System.out.println("> 完成读取文件" + fp + ", 用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()) + ". " + TimeUtility.getTime());
			nWid = 0;
			while(true) {
				nWid = widQueue.take();
				pidsQueue.put(signReadOver);
				if(-1==nWid) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception{
		String fPath = Global.recWidPidReachPath + "." + String.valueOf(0) + "." + String.valueOf(P2WReach.zipContianNodeNum);
		DataInputStream dis = IOUtility.getDis(fPath);
		int i, j ,k;
		List<Integer> wids = new ArrayList<>();
		i = dis.readInt();
		for(j=0; j<i; j++) {
			wids.add(dis.readInt());
		}
		for(j=0; j<3; j++) {
			System.out.println(wids.get(j) + " ");
		}
		System.out.println();
		int wid = 0;
		Scanner sca = new Scanner(System.in);
		while(true) {
			System.out.print("> 请输入wid : ");
			wid = Integer.parseInt(sca.nextLine());
			i = dis.readInt();
			wid = -1;
			for(j=0; j<i; j++) {
				if(wid==-1)	wid = dis.readInt();
			}
			System.out.println(wid);
		}
	}
}























