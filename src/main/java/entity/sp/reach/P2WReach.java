package entity.sp.reach;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import entity.BFSWidRecoder;
import entity.sp.AllDateWidNodes;
import entity.sp.AllDateWidNodes.DWid;
import entity.sp.AllPidWid;
import entity.sp.GraphByArray;
import utility.Global;
import utility.IOUtility;
import utility.LoopQueue;
import utility.TimeUtility;

/**
 * 处理所有pid到wid的可达性情况
 * @author Monica
 *
 */
public class P2WReach implements Runnable{
	private static GraphByArray graph = null;
	private static Map<Integer, DWid> allDW = null;
	private static List<Integer> allPid = null;
	private static boolean hasInit = false;
	private static long startTime = System.currentTimeMillis();
	public static Map<Integer, Short>[] pid2Wids = null;
	
	protected short[] distance2Source;
	protected int[] visitedFlag;
	
	private String filePath;
	
	private int start;
	private int end;
	
	private static int dealedNum = 0;
	
	public static int zipContianNodeNum = Global.numPid / 40;
	public static int zipNum = Global.numPid%zipContianNodeNum==0?Global.numPid/zipContianNodeNum:Global.numPid/zipContianNodeNum+1;
	
	private ArrayBlockingQueue<TempClass> blockingQueue = null;
	private static ArrayBlockingQueue<Integer> endSignQueue = null; 
	
	private int type = 1;
	
	public P2WReach() {
		this.init();
	}
	
	public P2WReach(String fp) {
		this.filePath = fp;
	}
	
	public P2WReach(int type, ArrayBlockingQueue<TempClass> qu, int start, int end){
		this.type = type;
		this.start = start;
		this.end = end;
		if(2==type) {
			this.filePath = Global.recPidWidReachPath + "." + String.valueOf(start) + "." + String.valueOf(end);
		}
		this.blockingQueue = qu;
		this.init();
	}
	
	public void init() {
		this.distance2Source = new short[Global.numNodes];
		this.visitedFlag = new int[Global.numNodes];
		for (int i = 0; i < this.visitedFlag.length; i++) {
			this.visitedFlag[i] = -1;
		}
		
		if(hasInit)	return;
		hasInit = true;
		try {
			System.out.println("> 开始初始化PWReach . . . ");
			graph = new GraphByArray(Global.numNodes);
			graph.loadGraph(Global.inputDirectoryPath + Global.edgeFile);
			allDW = AllDateWidNodes.loadFromFile(Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile);
			allPid = AllPidWid.getAllPid();
			
			System.out.println("> 成功初始化PWReach 。 ");
		} catch (Exception e) {
			System.err.println("> 初始化PWReach失败！");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	
	/**
	 * 通过BFS的方式来创建pwTimes
	 * @throws Exception
	 */
	public void buildingByBFS() throws Exception{
		System.out.println("> 开始buildingByBFS . . . " + TimeUtility.getTime());
		DWid tDWid = null;
		LoopQueue<Integer> queue = new LoopQueue<>(500000);
		int[] adjList = null;
		int i, j;
		int source;
		Integer nid = -1;
		
		/********** 普通BFS ***********/
		for(i = this.start; i<this.end; i++) {
			if(i >= allPid.size())	continue;
			source = allPid.get(i);
			queue.reset();
			distance2Source[source] = 1;
			visitedFlag[source] = source;
			
//			BFSWidRecoder bfsWidRec = Global.orgBFSWidRecoder.copy();
			queue.push(source);
			Map<Integer, Short> widDis = new HashMap<>();
			
			while(null != (nid = queue.poll())) {
				// 添加wid
				if(null != (tDWid = allDW.get(nid))) {
					for(int wid : tDWid.wids) {
						if(Global.wordFrequency.get(wid) >= Global.MAX_WORD_FREQUENCY) {
							if(!widDis.containsKey(wid)) {
								widDis.put(wid, distance2Source[nid]);
							}
						}
//						if(bfsWidRec.accessOver(wid))	break;
					}
//					if(bfsWidRec.isOver())	break;
				}
				
				// bfs
				if(null != (adjList = this.graph.getEdge(nid))) {
					for (j = 0; j < adjList.length; j++) {
						int adjVertex = adjList[j];
						if (visitedFlag[adjVertex] != source) {
							// not visited yet
							distance2Source[adjVertex] = (short)(1 + distance2Source[nid]);
							if(distance2Source[adjVertex] < 0) {
								throw new Exception("distance2Source[adjVertex]大于short最大值");
							}
							visitedFlag[adjVertex] = source;
							if(!queue.push(adjVertex)) {
								throw new Exception("> 队列" + queue.size() + "太短");
							}
						}
					}
				}
			}
			
			if(!widDis.isEmpty()) {
				blockingQueue.put(new TempClass(source, widDis));
			}
			
			dealedNum++;
			
			if(dealedNum%40000 == 0) {
				System.out.println("> 已处理" + dealedNum + "个pid, 用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()));
				System.gc();
				System.gc();
			}
		}
		
		blockingQueue.put(new TempClass(-1, null));
		System.out.println("> 结束 . " + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()) + "   " + TimeUtility.getTime());
	}
	
	/**
	 * 写文件
	 */
	public void writeToFile() {
		try {
			DataOutputStream dos = IOUtility.getDos(filePath);
			TempClass tc = null;
			while(true) {
				tc = blockingQueue.take();
				if(tc.pid == -1)	break;
				dos.writeInt(tc.pid);
				dos.writeInt(tc.widDis.size());
				for(Entry<Integer, Short> en : tc.widDis.entrySet()) {
					dos.writeInt(en.getKey());
					dos.writeShort(en.getValue());
				}
				if(Global.graphWithWids == null)	pid2Wids[tc.pid] = tc.widDis;
			}
			dos.flush();
			dos.close();
			if(null != P2WReach.endSignQueue)	P2WReach.endSignQueue.put(1);
		} catch (Exception e) {
			System.err.println("> 写文件" + Global.pWReachTimesPath + " 失败！！！");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * 读文件
	 */
	public void loadFromFile() {
	}
	
	public void run(){
		try {
			if(1==type)	this.buildingByBFS();
			else this.writeToFile();
		} catch (Exception e) {
			System.err.println("> PWReachDate异常退出");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	// 计算出记录pid到wid可达情况的文件
	public static void buildingPidToWidReachFile(ArrayBlockingQueue<Integer> endSignQueue) throws Exception{
		P2WReach.endSignQueue = endSignQueue;
		P2WReach.pid2Wids = new Map[Global.numPid];
		int start = 0, end = 0;
		int span = zipContianNodeNum;
		while(end < Global.numPid) {
			start = end;
			end += span;
			if(end > Global.numPid)	end = Global.numPid;
			ArrayBlockingQueue<TempClass> bQueue = new ArrayBlockingQueue<>(50);
			P2WReach pwd1 = new P2WReach(1, bQueue, start, end);
			P2WReach pwd2 = new P2WReach(2, bQueue, start, end);
			new Thread(pwd1).start();
			new Thread(pwd2).start();
		}
	}
	
	// 删除文件
	public static void deleteAllFiles() throws Exception{
		int start = 0, end = 0;
		int span = zipContianNodeNum;
		while(end < Global.numPid) {
			start = end;
			end += span;
			if(end > Global.numPid)	end = Global.numPid;
			String fp = Global.recPidWidReachPath + "." + String.valueOf(start) + "." + String.valueOf(end);
			new File(fp).delete();
		}
	}
	
	public static void main(String[] args) throws Exception{
		P2WReach.buildingPidToWidReachFile(null);
		
//		System.out.println(P2WReach.zipNum);
		
//		int start = 0, end = 0;
//		int i=0;
//		int span = zipContianNodeNum;
//		while(end < Global.numPid) {
//			start = end;
//			end += span;
//			if(end > Global.numPid)	end = Global.numPid;
//			i++;
//		}
//		System.out.println(i);
		
//		PWReach.getWidToPidFile();
	}
}
