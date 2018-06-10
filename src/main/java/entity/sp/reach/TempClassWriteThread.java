package entity.sp.reach;

import java.io.DataOutputStream;
import java.util.concurrent.ArrayBlockingQueue;

import utility.IOUtility;

public class TempClassWriteThread extends Thread{
	private ArrayBlockingQueue<TempClass> queue = null;
	private String fp = null;
	public TempClassWriteThread(ArrayBlockingQueue<TempClass> qu, String fp) {
		queue = qu;
		this.fp = fp;
	}
	
	public void run() {
		try {
			TempClass tc = null;
			DataOutputStream dos = IOUtility.getDos(fp);
			while(true) {
				tc = queue.take();
				if(tc.pid == Integer.MIN_VALUE)	break;
				dos.writeInt(tc.pid);
				dos.writeInt(tc.wids.size());
				for(int in : tc.wids) {
					dos.writeInt(in);
				}
			}
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
