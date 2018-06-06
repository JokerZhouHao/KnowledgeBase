package entity.sp.date;

import java.io.DataInputStream;
import java.io.EOFException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import utility.Global;
import utility.IOUtility;
import utility.TimeUtility;

/**
 * 用来测试相关文件
 * @author Monica
 * @since 2018/6/5
 */
public class DateFileTest extends Thread{
	public static void testGZPidNum(String fp) throws Exception{
		DataInputStream dis = IOUtility.getDGZis(fp);
		int x, count = 0;
		try {
			while(true) {
//				System.out.println(dis.readInt());
				if(dis.readInt() == Integer.MAX_VALUE)	count++;
			}
		} catch (EOFException e) {
			try {
				dis.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
			System.out.println(count);
			System.out.println("Over");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public DateFileTest() {}
	
	private int start = 0;
	private int end = 0;
	private Map<Integer, Map<Integer, int[]>> rec = new HashMap<>();
	private static long startTime = System.currentTimeMillis();
	
	public DateFileTest(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	public void run() {
		String fp = Global.noRepWReachTimesPath + "." + String.valueOf(start) + "." + String.valueOf(end);
		try {
			System.out.println("> 开始读取文件" + fp + " . . . " + TimeUtility.getTime());
			DataInputStream dis = IOUtility.getDis(fp);
			int pid, wid, widNum, dateNum;
			int dates[] = null;
			int i = 0, j=0;
			Map<Integer, int[]> widDates = null;
			while(true) {
				pid = dis.readInt();
				if(null == (widDates = rec.get(pid))) {
					widDates = new HashMap<>();
					rec.put(pid, widDates);
				}
				
				widNum = dis.readInt();
				for(i=0; i<widNum; i++) {
					wid = dis.readInt();
					dateNum = dis.readInt();
					if(-1==dateNum) {
						widDates.put(wid, widDates.get(dis.readInt()));
					} else {
						dates = new int[dateNum];
						for(j=0; j<dateNum; j++) {
							dates[j] = dis.readInt();
						}
						widDates.put(wid, dates);
					}
				}
			}
		} catch (EOFException e) {
			System.out.println("> 读取文件" + fp + "完成 , 用时:" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()) + "  " + TimeUtility.getTime());
			try {
				Thread.sleep(3600000);
			} catch (Exception e2) {
				// TODO: handle exception
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public static void main(String[] args) throws Exception{
//		String fp = Global.pWReachTimesPath + "." + String.valueOf(0) + "." + String.valueOf(PWReachDate.zipContianNodeNum);
//		testGZPidNum(fp);
		int start = 0, end = 0;
		int span = PWReachDate.zipContianNodeNum;
		while(end < Global.numPid) {
			start = end;
			end += span;
			if(end > Global.numPid)	end = Global.numPid;
			DateFileTest dft = new DateFileTest(start, end);
			dft.start();
		}
	}
}
