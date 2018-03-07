package zhou.hao.processor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import zhou.hao.entry.LineList;
import zhou.hao.service.GZIPReaderService;
import zhou.hao.tools.LocalFileInfo;

/**
 * 
 * @author Monica
 *
 * 一行一行读取文件，并放到lineList
 */
public class LineProductor implements Runnable{
	private LineList<String> lineList[] = null;
	private LinkedBlockingQueue<Integer> sendQueue = null;
	private LinkedBlockingQueue<Integer> recQueue = null;
	private GZIPReaderService reader = null;
	
	public LineProductor(LineList<String> lineList[], LinkedBlockingQueue<Integer> sendQueue, LinkedBlockingQueue<Integer> recQueue) {
		this.lineList = lineList;
		this.sendQueue = sendQueue;
		this.recQueue = recQueue;
//		reader = new GZIPReaderService(LocalFileInfo.getGzipDataFilePath());
		reader = new GZIPReaderService(LocalFileInfo.getTestGzipPath());
	}
	
	public void run() {
		while(true) {
			try {
				Integer sign = recQueue.take();
				//System.out.println("生产者：" + sign);
				lineList[sign].resetWritePosition();
				for(Integer i=0; i<lineList[sign].size(); i++) {
					String lineStr = this.reader.readLine();
//					System.out.println(lineStr);
					if(null!=lineStr) {
						//System.out.println(lineStr);
						if(!lineStr.isEmpty() && lineStr.startsWith("<")) {
								lineList[sign].write(lineStr);
								//System.out.println(lineStr);
						}
					} else {	// 已读完文件
						if(0 != lineList[sign].getWritePosition()) this.sendQueue.put(sign);
						this.sendQueue.put(-1);
						this.reader.close();
						return;
					}
					//System.out.println("生产" + sign +  "-->" + lineStr);
					//Thread.sleep(500);
				}
				this.sendQueue.put(sign);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
