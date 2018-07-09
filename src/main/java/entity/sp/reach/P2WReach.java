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
	public static Set<Integer>[] pid2Wids = null;
	
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
		int[] edges = null;
		
		DWid tDWid = null;
		
		int pid ;
		
		LoopQueue<Integer> queue = new LoopQueue<>(100000);
		for(int i = this.start; i<this.end; i++) {
			pid = allPid.get(i);
			queue.reset();
			Integer nid = -1;
			HashSet<Integer> rec = new HashSet<Integer>();
			queue.push(pid);
			rec.add(pid);
			HashSet<Integer> wids = new HashSet<>();
			while(null != (nid = queue.poll())) {
				// bfs
				if(null != (edges =  graph.getEdge(nid))) {
					for(int e : edges) {
						if(!rec.contains(e)) {
							if(!queue.push(e)) {
								System.err.println("> 队列" + queue.size() + "太短");
								System.exit(0);
							}
							rec.add(e);
						}
					}
				}
				
				// 添加date
				if(null != (tDWid = allDW.get(nid))) {
					for(int wid : tDWid.wids) {
						wids.add(wid);
					}
				}
			}
			
			if(!wids.isEmpty()) {
				blockingQueue.put(new TempClass(pid, wids));
			}
			
			dealedNum++;
			
			if(dealedNum%40000 == 0) {
				System.out.println("> 已处理" + dealedNum + "个pid, 用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()));
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
				dos.writeInt(tc.wids.size());
				for(int in : tc.wids) {
					dos.writeInt(in);
				}
				pid2Wids[tc.pid] = tc.wids;
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
		P2WReach.pid2Wids = new HashSet[Global.numPid];
		int start = 0, end = 0;
		int span = zipContianNodeNum;
		while(end < Global.numPid) {
			start = end;
			end += span;
			if(end > Global.numPid)	end = Global.numPid;
			ArrayBlockingQueue<TempClass> bQueue = new ArrayBlockingQueue<>(1);
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
//		PWReach.getWidToPidFile();
	}
}
