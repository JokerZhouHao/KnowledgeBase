package entity.sp.date;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

import utility.Global;
import utility.IOUtility;
import utility.TimeUtility;
import utility.Utility;

class PidMapWidDate{
	public int pid;
	public int widNum;
	public Map<Integer, List<WidDates>> rec = null;
	public PidMapWidDate(int p, int wNum, Map<Integer, List<WidDates>> rec) {
		this.pid = p;
		this.widNum = wNum;
		this.rec = rec;
	}
}

/**
 * 合并place节点中的word相同的时间串
 * @author Monica
 *
 */
public class PWReachDateCompress extends Thread{
	
	private int type = 0;
	private int start = 0;
	private int end = 0;
	private String fp = null;
	private ArrayBlockingQueue<PidMapWidDate> queue = null;
	
	private static int count = 0;
	private static long startTime = System.currentTimeMillis();
	
	public PWReachDateCompress(int type, int s, int e, ArrayBlockingQueue<PidMapWidDate> qu) {
		this.type = type;
		this.start = s;
		this.end = e;
		if(type==1) {
			fp = Global.pWReachTimesPath + "." + String.valueOf(start) + "." + String.valueOf(end);
		} else if(type==2) {
			fp = Global.noRepWReachTimesPath + "." + String.valueOf(start) + "." + String.valueOf(end);
		}
		this.queue = qu;
	}
	
	// 计算重复
	public void dealData() {
		System.out.println("> 开始处理文件" + fp + " . . . " + TimeUtility.getTime());
		long start = System.currentTimeMillis();
		DataInputStream dis = null;
		try {
			dis = IOUtility.getDGZis(fp);
			int pid, wid;
			Map<Integer, List<WidDates>> rec = null;;
			List<WidDates> tList = null;
			int dates[] = null;
			WidDates tWD = null;
			int i = 0;
			int repNum = 0, num=0, widNum = 0, lessTwoNum = 0;
//			long start1 = 0;
			while(true) {
//				start1 = System.currentTimeMillis();
				repNum = 0;
				widNum = 0;
				lessTwoNum = 0;
				
				rec = new HashMap<>();
				
				// 读取pid的时间
				pid = dis.readInt();
				count++;
				while(true) {
					wid = dis.readInt();
					if(wid == Integer.MAX_VALUE)	break;
					widNum++;
					num = dis.readInt();
					tWD = new WidDates(wid, num);
					dates = tWD.dates;
					for(i=0; i<num; i++) {
						dates[i] = dis.readInt();
					}
					
					if(null == (tList = rec.get(num))) {
						tList = new ArrayList<>();
						rec.put(num, tList);
					}
					tList.add(tWD);
				}
				
				// 查找重复的
				for(List<WidDates> list : rec.values()) {
//					widNum -= list.size();
					for(WidDates wd : list) {
						for(WidDates wd1 : list) {
							if(wd.wid == wd1.wid)	break;
							else {
								if(Utility.isEqualIntArr(wd.dates, wd1.dates)){
									if(wd.dates.length <=2)	lessTwoNum++;
									wd.dates = new int[2];
									wd.dates[0] = -1;
									wd.dates[1] = wd1.wid;
									repNum++;
									break;
								}
							}
						}
					}
				}
				
				// 清空内存
//				for(List<WidDates> li : rec.values()) {
//					li.clear();
//				}
//				rec.clear();
//				System.out.format("%9d%9d%9d%9d%9d\n", count, widNum, repNum, lessTwoNum, System.currentTimeMillis()-startTime);
				
				// 放入队列
				queue.put(new PidMapWidDate(pid, widNum, rec));
				
				if(count%1200 == 0) {
//					System.out.format("%9d%9d%9d%9d\n", count, widNum, repNum, System.currentTimeMillis()-start1);
					System.out.format("> 已处理%d个pid，用时%s\n", count, TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()));
				}
			}
		} catch (EOFException e) {
			try {
				queue.put(new PidMapWidDate(-1, 0, null));
				dis.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
			System.out.println("> 处理文件" + fp + "完成. 用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()) + "  " + TimeUtility.getTime());
		} catch (Exception e) {
			System.err.println("> 处理" + fp + "异常而退出！！！");
			e.printStackTrace();
		}
	}
	
	// 写数据
	public void writeDate() {
		try {
			DataOutputStream dos = IOUtility.getDGZos(fp);
			PidMapWidDate pmwd = null;
			List<WidDates> tList = new ArrayList<>();
			while(true) {
				pmwd = queue.take();
				if(pmwd.pid == -1)	break;
				dos.writeInt(pmwd.pid);
				dos.writeInt(pmwd.widNum);
				for(List<WidDates> li : pmwd.rec.values()) {
					for(WidDates wd : li) {
						if(wd.dates[0]==-1) {
							tList.add(wd);
							continue;
						}
						dos.writeInt(wd.wid);
						dos.writeInt(wd.dates.length);
						for(int in : wd.dates) {
							dos.writeInt(in);
						}
					}
				}
				for(WidDates wd : tList) {
					dos.writeInt(wd.wid);
					for(int in : wd.dates) {
						dos.writeInt(in);
					}
				}
				
				// 清理内存
				for(List<WidDates> li : pmwd.rec.values()) {
					li.clear();
				}
				pmwd.rec.clear();
				tList.clear();
			}
			dos.close();
		} catch (Exception e) {
			System.err.println("> 写文件" + fp + "异常退出 . . ");
			e.printStackTrace();
		}
	}
	
	public void run() {
		if(1 == type) {
			this.dealData();
		} else if(2 == type) {
			this.writeDate();
		}
	}
	
	public static void mmain() {
		int start = 0, end = 0;
		int span = PWReachDate.zipContianNodeNum;
		while(end < Global.numPid) {
			start = end;
			end += span;
			if(end > Global.numPid)	end = Global.numPid;
			ArrayBlockingQueue<PidMapWidDate> queue = new ArrayBlockingQueue<>(5);
			PWReachDateCompress pwc1 = new PWReachDateCompress(1, start, end, queue);
			PWReachDateCompress pwc2 = new PWReachDateCompress(2, start, end, queue);
			pwc1.start();
			pwc2.start();
//			if(0 == start)	break;
		}
	}
	
	public static void main(String[] args) {
//		PWReachDateCompress pwc1 = new PWReachDateCompress(1, 0, PWReachDate.zipContianNodeNum, null);
//		pwc1.dealData();
		PWReachDateCompress.mmain();
	}
}
