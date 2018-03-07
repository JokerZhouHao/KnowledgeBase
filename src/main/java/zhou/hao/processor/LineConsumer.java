package zhou.hao.processor;

import java.util.concurrent.ArrayBlockingQueue;

import zhou.hao.entry.GoogleFreebaseMap;
import zhou.hao.entry.LineList;
import zhou.hao.entry.NodeIdHashMapTestList;
import zhou.hao.service.GZIPReaderService;
import zhou.hao.service.GoogleFreebaseLineProcessService;
import zhou.hao.service.GoogleFreebaseMapWriteService;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.MemoryInfo;
import zhou.hao.tools.TimeStr;

/**
 * 
 * @author Monica
 *
 * 处理LineProductor得到的line
 */
public class LineConsumer implements Runnable{
	private LineList<String> lineList[] = null;
	private ArrayBlockingQueue<Integer> sendQueue = null;
	private ArrayBlockingQueue<Integer> recQueue = null;
	private GoogleFreebaseLineProcessService lineProcessService = new GoogleFreebaseLineProcessService();
	
	private GoogleFreebaseMap freebaseMap = new GoogleFreebaseMap();
	
	// 测试
	private NodeIdHashMapTestList nodeIdHashMapTest = new NodeIdHashMapTestList();
	
	private Long startTime = System.nanoTime();
	private Long tempTime = startTime;
	private Long totalLine = 3130753066L;
//	private Long totalLine = 100000L;
	private Long precessedLineNum = 0L;
	private Long tempLineNum = precessedLineNum;
	
	private int tempLineNumForNode = 0;
	
	private Long avgLineNum = this.totalLine/100 + 1;
	
	public LineConsumer(LineList<String> lineList[], ArrayBlockingQueue<Integer> sendQueue, ArrayBlockingQueue<Integer> recQueue) {
		this.lineList = lineList;
		this.sendQueue = sendQueue;
		this.recQueue = recQueue;
	}
	
	public void run() {
		while(true) {
			try {
				Integer sign = recQueue.take();
				//System.out.println("消费者：" + sign);
				if(-1==sign) { // 已经读取完毕
					new GoogleFreebaseMapWriteService(this.freebaseMap).writeMap();
					
					// 测试
//					this.nodeIdHashMapTest.displayAllNodes();
//					Long nowTime = System.nanoTime();
//					System.out.println("已处理 " + this.precessedLineNum + "条，共生成" + this.nodeIdHashMapTest.getNodeNum() + "个节点, 当前时间：" + TimeStr.getTime() + ", 内存占用情况如下：");
//					System.out.println(MemoryInfo.getTotalFreeUsedAvailable() + "\n");
//					System.out.println("共用："+ (nowTime - this.tempTime)/1000000000/3600 + "h" + (nowTime - this.tempTime)/1000000000%3600/60 + 
//							"m" + (nowTime - this.tempTime)/1000000000%3600%60 + "s");
					
					
					Long nowTime = System.nanoTime();
					System.out.println("处理成功，共处理" + this.precessedLineNum + "行，生成" + this.freebaseMap.getSize() + "个点, "
							+ "用时：" + (nowTime - this.tempTime)/1000000000/3600 + "h" + (nowTime - this.tempTime)/1000000000%3600/60 + 
							"m" + (nowTime - this.tempTime)/1000000000%3600%60 + "s，"
							+ "文件放在:\n" + LocalFileInfo.getResultZipGoogleFreebasePath());
					return;
				}
				lineList[sign].resetReadPosition();
				if(0 != lineList[sign].getWritePosition()) {
					for(Integer i=0; i<lineList[sign].getWritePosition(); i++) {
						String lineStr = this.lineList[sign].read();
						this.freebaseMap.addNode(lineProcessService.processLineStr(lineStr));
						
						// 测试
//						this.nodeIdHashMapTest.addNode(lineProcessService.processLineStr(lineStr));
						
						this.precessedLineNum++;
						if(this.precessedLineNum-this.tempLineNum > avgLineNum) {
							Long nowTime = System.nanoTime();   
							System.out.println("已处理 : " + this.precessedLineNum*100/this.totalLine + "%  共用  : " + (nowTime - this.tempTime)/1000000000/3600 + "h" + (nowTime - this.tempTime)/1000000000%3600/60 + 
									"m" + (nowTime - this.tempTime)/1000000000%3600%60 + "s");
							//this.tempTime = nowTime;
							this.tempLineNum = this.precessedLineNum;
						}
//						if(this.nodeIdHashMapTest.getNodeNum()%1000000==0 && this.nodeIdHashMapTest.getNodeNum()/1000000>this.tempLineNumForNode) {
//							System.out.println("已处理 " + this.precessedLineNum + "条，共生成" + this.nodeIdHashMapTest.getNodeNum() + "个节点, 当前时间：" + TimeStr.getTime() + ", 内存占用情况如下：");
//							System.out.println(MemoryInfo.getTotalFreeUsedAvailable());
//							Long nowTime = System.nanoTime();
//							System.out.println("共用："+ (nowTime - this.tempTime)/1000000000/3600 + "h" + (nowTime - this.tempTime)/1000000000%3600/60 + 
//									"m" + (nowTime - this.tempTime)/1000000000%3600%60 + "s\n");
//							this.tempLineNumForNode = this.nodeIdHashMapTest.getNodeNum()/1000000;
//						}
//						System.out.println("消费" + sign);
						//Thread.sleep(500);
					}
					this.sendQueue.put(sign);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
