package entity.sp.date;

import java.io.DataOutputStream;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 用来写wid dates线程
 * @author Monica
 * @since 2018/06/02
 */
public class TWidDateWriter extends Thread{
	private ArrayBlockingQueue<PidWidDates> queue = null;
	private DataOutputStream dos = null;
	
	public TWidDateWriter(ArrayBlockingQueue<PidWidDates> qu, DataOutputStream dos){
		this.queue = qu;
		this.dos = dos;
	}
	
	public void run(){
		try {
			PidWidDates pwd = null;
			while(true) {
				pwd = queue.take();
				if(-1 == pwd.pid)	break;
				dos.writeInt(pwd.pid);
				for(Entry<Integer, Set<Integer>> en : pwd.widDates.entrySet()) {
					dos.writeInt(en.getKey());
					dos.writeInt(en.getValue().size());
					for(int in : en.getValue()) {
						dos.writeInt(in);
					}
				}
				dos.writeInt(Integer.MAX_VALUE);
				for(Entry<Integer, Set<Integer>> en : pwd.widDates.entrySet()) {
					en.getValue().clear();
				}
				pwd.widDates.clear();
			}
		} catch (Exception e) {
			System.err.println("> TWidDateWriter异常退出");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
