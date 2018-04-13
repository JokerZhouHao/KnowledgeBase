 package processor.freebase;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import entity.freebase.GoogleFreebaseMap;
import entity.freebase.LineList;
import entity.freebase.NodeIdHashMapTestList;
import entity.freebase.NodeIdHashMapTestMap;
import file.reader.GZIPReader;
import processor.freebase.GoogleFreebaseLineProcessService;
import processor.freebase.GoogleFreebaseLineProcessService.Node;
import file.writer.freebase.GoogleFreebaseMapWriteService;
import file.writer.freebase.NodeIdHashMapWriteServiceList;
import file.writer.freebase.NodeIdHashMapWriteServiceMap;
import utility.LocalFileInfo;
import utility.MemoryInfo;
import utility.TimeUtility;

/**
 * 
 * @author Monica
 *
 * 处理LineProductor得到的line
 */
public class MinMemoryConsumer implements Runnable{
	private LineList<String> lineList[] = null;
	
	private LinkedBlockingQueue<Integer> sendQueue = null;
	private LinkedBlockingQueue<Integer> recQueue = null;
	
	private ArrayBlockingQueue<String> lineQueue = new ArrayBlockingQueue<>(1000, true);
	private ArrayBlockingQueue<GoogleFreebaseLineProcessService.Node> lineNodeQueue = new ArrayBlockingQueue<>(1000, true);
	
	private GoogleFreebaseLineProcessService lineProcessService = new GoogleFreebaseLineProcessService();
	
	private NodeIdHashMapTestMap nodeIdHashMapTest = new NodeIdHashMapTestMap();
	private NodeIdHashMapWriteServiceMap nodeIdHashMapWriteService = new NodeIdHashMapWriteServiceMap(nodeIdHashMapTest);
	
	private Long startTime = System.nanoTime();
	
//	private Long totalLine = 3130753066L;
	private Long totalLine = 20L;
	private Long precessedLineNum = 0L;
	private Long tempProcessedLineNum = 0L;
	
	private Long processedLineInBlock = this.totalLine/1;
	private int hasProcessedBlock = 0;
	
	public MinMemoryConsumer(LineList<String> lineList[], LinkedBlockingQueue<Integer> sendQueue, LinkedBlockingQueue<Integer> recQueue) {
		this.lineList = lineList;
		this.sendQueue = sendQueue;
		this.recQueue = recQueue;
//		ExecutorService exec = Executors.newCachedThreadPool();
//		exec.execute(new AddNodeThread());
//		exec.shutdown();
	}
	
	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getPrecessedLineNum() {
		return precessedLineNum;
	}

	public void setPrecessedLineNum(Long precessedLineNum) {
		this.precessedLineNum = precessedLineNum;
	}

	public Long getTotalLine() {
		return totalLine;
	}

	public void setTotalLine(Long totalLine) {
		this.totalLine = totalLine;
	}

	public int getNodeNum() {
		return this.nodeIdHashMapTest.getNodeNum();
	}
	
	public void run() {
		while(true) {
			try {
				Integer sign = recQueue.take();
				if(-1==sign) { // 已经读取完毕
					// 打印节点
//					this.nodeIdHashMapTest.displayAllNodes();
					
					this.nodeIdHashMapTest.notifyReadOver();
					
					Long nowTime = System.nanoTime();
					if(precessedLineNum>tempProcessedLineNum) {
						System.out.println("> 已处理 " + precessedLineNum + "条, 完成10000%%, 已生成" + nodeIdHashMapTest.getNodeNum() + "个节点, 当前时间：" + TimeUtility.getTime() + ", 内存占用情况如下：");
						System.out.println("  " + MemoryInfo.getTotalFreeUsedAvailable());
						System.out.println("  共用："+ (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
								"m" + (nowTime - startTime)/1000000000%3600%60 + "s");
						System.out.println("  现开始输出文件keywordAndEdge-" + hasProcessedBlock + ".zip. . .");
						if(Boolean.TRUE==nodeIdHashMapWriteService.writeKeywordAndEdgeBlock(hasProcessedBlock++)) {
							nowTime = System.nanoTime();
							System.out.print("  输出文件keywordAndEdge-" + (hasProcessedBlock-1) + ".zip成功, ");
							System.out.println("  共用："+ (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
									"m" + (nowTime - startTime)/1000000000%3600%60 + "s\n");
						} else {
							System.out.println("  输出文件keywordAndEdge-" + (hasProcessedBlock-1) + ".zip失败而退出! ! !");
							System.exit(0);
						}
					}
					
					// 输出blockNum
					System.out.println("> 输出文件blockNum. . .");
					if(Boolean.TRUE == nodeIdHashMapWriteService.writeNodeNum(hasProcessedBlock)) {
						System.out.println("  输出成功！\n");
					} else {
						System.out.println("  输出失败而退出！！！");
						System.exit(0);
					}
					
					// 输出nodeIdHashMap
					nowTime = System.nanoTime();
					System.out.println("> 已处理 " + precessedLineNum + "条, 完成10000%%, 已生成" + nodeIdHashMapTest.getNodeNum() + "个节点, 当前时间：" + TimeUtility.getTime() + ", 内存占用情况如下：");
					System.out.println("  " + MemoryInfo.getTotalFreeUsedAvailable());
					System.out.println("  共用："+ (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
							"m" + (nowTime - startTime)/1000000000%3600%60 + "s");
					System.out.println("  现输出文件tempNodeName.gz . . .");
					if(Boolean.TRUE == nodeIdHashMapWriteService.writeNodeHashMap()) {
						nowTime = System.nanoTime();
						System.out.print("  输出文件tempNodeName.gz成功, ");
						System.out.println("  共用："+ (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
								"m" + (nowTime - startTime)/1000000000%3600%60 + "s\n");
					} else {
						System.out.println("  输出失败而退出！！！");
						System.exit(0);
					}
					
					// 输出所有文件
					nodeIdHashMapWriteService.writeAllFile(startTime, precessedLineNum);
					return;
				}
				lineList[sign].resetReadPosition();
				if(0 != lineList[sign].getWritePosition()) {
					for(Integer i=0; i<lineList[sign].getWritePosition(); i++) {
						String str = this.lineList[sign].read();
						nodeIdHashMapTest.addNode(lineProcessService.processLineStr(str));
						precessedLineNum++;
						// 测试
						if((precessedLineNum-tempProcessedLineNum)==processedLineInBlock) {
							
							this.nodeIdHashMapTest.notifyWriteBlock();
							
							tempProcessedLineNum = precessedLineNum;
							Long nowTime = System.nanoTime();
							System.out.println("> 已处理 " + precessedLineNum + "条, 完成" + precessedLineNum*10000/totalLine + "%%, 已生成" + nodeIdHashMapTest.getNodeNum() + "个节点, 当前时间：" + TimeUtility.getTime() + ", 内存占用情况如下：");
							System.out.println("  " + MemoryInfo.getTotalFreeUsedAvailable());
							System.out.println("  共用: "+ (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
									"m" + (nowTime - startTime)/1000000000%3600%60 + "s");
							System.out.println("  现开始输出文件keywordAndEdge-" + hasProcessedBlock + ".zip. . .");
							// 输出块
							if(Boolean.TRUE == nodeIdHashMapWriteService.writeKeywordAndEdgeBlock(hasProcessedBlock++)) {
								nowTime = System.nanoTime();
								System.out.print("  输出文件keywordAndEdge-" + (hasProcessedBlock-1) + ".zip成功, ");
								System.out.println("共用: "+ (nowTime - startTime)/1000000000/3600 + "h" + (nowTime -startTime)/1000000000%3600/60 + 
										"m" + (nowTime - startTime)/1000000000%3600%60 + "s\n");
							} else {
								System.out.println("  输出文件keywordAndEdge-" + (hasProcessedBlock-1) + ".zip失败而退出! ! !");
								System.exit(0);
							}
						}
					}
					this.sendQueue.put(sign);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class AddNodeThread implements Runnable{
		
		@Override
		public void run() {
			try {
				Node no = lineNodeQueue.take();
//				System.out.println(no.getNext());
				while(null!=no.getNext()) {
					nodeIdHashMapTest.addNode(no);
					precessedLineNum++;
					// 测试
					if((precessedLineNum-tempProcessedLineNum)==processedLineInBlock) {
						
						nodeIdHashMapTest.notifyWriteBlock();	// 通知写块
						
						tempProcessedLineNum = precessedLineNum;
						Long nowTime = System.nanoTime();
						System.out.println("> 已处理 " + precessedLineNum + "条, 完成" + precessedLineNum*10000/totalLine + "%%, 已生成" + nodeIdHashMapTest.getNodeNum() + "个节点, 当前时间：" + TimeUtility.getTime() + ", 内存占用情况如下：");
						System.out.println("  " + MemoryInfo.getTotalFreeUsedAvailable());
						System.out.println("  共用: "+ (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
								"m" + (nowTime - startTime)/1000000000%3600%60 + "s");
						System.out.println("  现开始输出文件keywordAndEdge-" + hasProcessedBlock + ".zip. . .");
						// 输出块
						if(Boolean.TRUE == nodeIdHashMapWriteService.writeKeywordAndEdgeBlock(hasProcessedBlock++)) {
							nowTime = System.nanoTime();
							System.out.print("  输出文件keywordAndEdge-" + (hasProcessedBlock-1) + ".zip成功, ");
							System.out.println("共用: "+ (nowTime - startTime)/1000000000/3600 + "h" + (nowTime -startTime)/1000000000%3600/60 + 
									"m" + (nowTime - startTime)/1000000000%3600%60 + "s\n");
						} else {
							System.out.println("  输出文件keywordAndEdge-" + (hasProcessedBlock-1) + ".zip失败而退出! ! !");
							System.exit(0);
						}
					}
					
					no = lineNodeQueue.take();
				}// 一个处理行的线程停掉了
				
//				no = lineNodeQueue.take();
//				System.out.println(no.getNext());
				
				no = lineNodeQueue.take();
				while(null!=no.getNext()) {
					nodeIdHashMapTest.addNode(no);
					precessedLineNum++;
					no = lineNodeQueue.take();
				}// 两个处理行的线程都停掉了
				
				nodeIdHashMapTest.notifyReadOver();	// 通知文件已经全部读完
				
				// 输出文件keywordAndEdge
				Long nowTime = System.nanoTime();
				if(precessedLineNum>tempProcessedLineNum) {
					System.out.println("> 已处理 " + precessedLineNum + "条, 完成10000%%, 已生成" + nodeIdHashMapTest.getNodeNum() + "个节点, 当前时间：" + TimeUtility.getTime() + ", 内存占用情况如下：");
					System.out.println("  " + MemoryInfo.getTotalFreeUsedAvailable());
					System.out.println("  共用："+ (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
							"m" + (nowTime - startTime)/1000000000%3600%60 + "s");
					System.out.println("  现开始输出文件keywordAndEdge-" + hasProcessedBlock + ".zip. . .");
					if(Boolean.TRUE==nodeIdHashMapWriteService.writeKeywordAndEdgeBlock(hasProcessedBlock++)) {
						nowTime = System.nanoTime();
						System.out.print("  输出文件keywordAndEdge-" + (hasProcessedBlock-1) + ".zip成功, ");
						System.out.println("  共用："+ (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
								"m" + (nowTime - startTime)/1000000000%3600%60 + "s\n");
					} else {
						System.out.println("  输出文件keywordAndEdge-" + (hasProcessedBlock-1) + ".zip失败而退出! ! !");
						System.exit(0);
					}
				}
				
				// 输出blockNum
				System.out.println("> 输出文件blockNum. . .");
				if(Boolean.TRUE == nodeIdHashMapWriteService.writeNodeNum(hasProcessedBlock)) {
					System.out.println("  输出成功！\n");
				} else {
					System.out.println("  输出失败而退出！！！");
					System.exit(0);
				}
				
				// 输出nodeIdHashMap
				nowTime = System.nanoTime();
				System.out.println("> 已处理 " + precessedLineNum + "条, 完成10000%%, 已生成" + nodeIdHashMapTest.getNodeNum() + "个节点, 当前时间：" + TimeUtility.getTime() + ", 内存占用情况如下：");
				System.out.println("  " + MemoryInfo.getTotalFreeUsedAvailable());
				System.out.println("  共用："+ (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
						"m" + (nowTime - startTime)/1000000000%3600%60 + "s");
				System.out.println("  现输出文件tempNodeName.gz . . .");
				if(Boolean.TRUE == nodeIdHashMapWriteService.writeNodeHashMap()) {
					nowTime = System.nanoTime();
					System.out.print("  输出文件tempNodeName.gz成功, ");
					System.out.println("  共用："+ (nowTime - startTime)/1000000000/3600 + "h" + (nowTime - startTime)/1000000000%3600/60 + 
							"m" + (nowTime - startTime)/1000000000%3600%60 + "s\n");
				} else {
					System.out.println("  输出失败而退出！！！");
					System.exit(0);
				}
				
				// 输出所有文件
				nodeIdHashMapWriteService.writeAllFile(startTime, precessedLineNum);
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
