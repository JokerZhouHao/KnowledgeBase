package entity.sp.reach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

import entity.sp.AllDateWidNodes;
import entity.sp.AllPidWid;
import entity.sp.GraphByArray;
import entity.sp.ReverseGraphByArray;
import entity.sp.AllDateWidNodes.DWid;
import utility.Global;
import utility.LoopQueue;
import utility.TimeUtility;

/**
 * 优化后的P2WReach文件生成类（最终证明是错误的T.T）
 * @author ZhouHao
 * @since 2019年5月13日
 */
public class P2WReachOpt implements Runnable{
	private static GraphByArray graph = null;
	private static int[][] revGraph = null;
	private static Map<Integer, DWid> allDW = null;
	private static long startTime = System.currentTimeMillis();
	public static Map<Integer, Short>[] pid2Wids = null;
	
	private boolean[] signAccess = null;
	
	private Map<Integer, Short>[] nidDatas = null;
	
	protected short[] distance2Source;
	protected int[] visitedFlag;
	
	private ArrayBlockingQueue<TempClass> blockingQueue = null;
	private static ArrayBlockingQueue<Integer> endSignQueue = null; 
	
	private Set<Integer> allPids = null;
	
	private static P2WFileWriter fileWriter = null;
	
	private static Boolean hasInit = Boolean.FALSE;
	
	private int type = 1;
	
	public P2WReachOpt(int type, ArrayBlockingQueue<TempClass> blockingQueue, ArrayBlockingQueue<Integer> endSignQueue) throws Exception {
		this.type = type;
		this.blockingQueue = blockingQueue;
		this.endSignQueue = endSignQueue;
		if(type == 2 && null == fileWriter)	fileWriter = new P2WFileWriter();
		if(!hasInit) {
			init();
			hasInit = Boolean.TRUE;
		}
	}
	
	/**
	 * 初始化
	 */
	public void init() {
		this.distance2Source = new short[Global.numNodes];
		this.visitedFlag = new int[Global.numNodes];
		for (int i = 0; i < this.visitedFlag.length; i++) {
			this.visitedFlag[i] = -1;
		}
		
		try {
			System.out.println("> 开始初始化PWReachOpt . . . ");
			graph = new GraphByArray(Global.numNodes);
			graph.loadGraph(Global.inputDirectoryPath + Global.edgeFile);
			
			revGraph = ReverseGraphByArray.load(Global.inputDirectoryPath + Global.edgeReverseFile);
			
			allDW = AllDateWidNodes.loadFromFile(Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile);
			
			signAccess = new boolean[Global.numNodes];
			
			nidDatas = new Map[Global.numNodes];
			
			allPids = AllPidWid.getAllPidSet();
			System.out.println("> 成功初始化PWReachOpt 。 ");
		} catch (Exception e) {
			System.err.println("> 初始化PWReachOpt失败！");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void run(){
		try {
			if(1==type)	this.buildByBfs();
			else if(2==type)	this.writeToFile();
		} catch (Exception e) {
			System.err.println("> P2WReachOpt异常退出");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * bfs计算各个pid的wids
	 * @throws Exception
	 */
	public void buildByBfs() throws Exception {
		System.out.println("> 开始buildingByBFS . . . " + TimeUtility.getTime());
		DWid tDWid = null;
		int[] adjList = null;
		int i,j,index;
		int source;
		Integer nid = -1;
		LoopQueue<Integer> queue = new LoopQueue<>(500000);
		ArrayList<Integer> accessNids = new ArrayList<>();
		
		Map<Integer, Short> nid2Dis = null;
		Short dis = null;
		int[] edgeNids = null;
		
		int dealedNum = 0;
		
		for(source =0; source<Global.numNodes; source++) {
			if(!allPids.contains(source) || signAccess[source])	continue;	// 不是pid或者已经遍历过
			
			// 重置bfs需要的数据
			queue.reset();
			accessNids.clear();
			distance2Source[source] = 1;
			visitedFlag[source] = source;
			
			// 初始化队列
			queue.push(source);
			accessNids.add(source);
			
			// bfs
			while(null != (nid = queue.poll())) {
				// 遍历访问的点
				if(null != (adjList = this.graph.getEdge(nid))) {
					for (j = 0; j < adjList.length; j++) {
						int adjVertex = adjList[j];
						if (visitedFlag[adjVertex] != source) {
							// not visited yet
							accessNids.add(adjVertex);	// 添加碰到的点
							
							if(!signAccess[adjVertex])	{	// 从未访问过
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
			}
			
			// 计算bfs到的各个点可达的wid
			for(index = accessNids.size() - 1; index >= 0; index--) {
				nid = accessNids.get(index);
				if(signAccess[nid])	continue;	// 已经访问过
				
				Map<Integer, Short> widDis = new HashMap<>();	// 记录wid和dis
				
				edgeNids = graph.getEdge(nid);	// 获得对应的边
				if(edgeNids != null) {	// 有相邻节点
					for(int edgeNid : edgeNids) {	// 添加相邻节点的wids
						if(null == nid2Dis)
						for(Entry<Integer, Short> en : nidDatas[edgeNid].entrySet()) {
							if(null != (dis = widDis.get(en.getKey()))) {
								if(dis > en.getValue() + 1)	widDis.put(en.getKey(), (short)(en.getValue() + 1));
							} else widDis.put(en.getKey(), (short)(en.getValue() + 1));
						}
					}
				}
				
				// 添加自身的wid
				if(null != (tDWid = allDW.get(nid))) {
					for(int wid : tDWid.wids) {
						if(Global.wordFrequency.get(wid) >= Global.MAX_WORD_FREQUENCY) {
							widDis.put(wid, (short)1);
						}
					}
				}
				
				nidDatas[nid] = widDis;
				signAccess[nid] = Boolean.TRUE;  // 标记已遍历了
				
				// 发送给写文件线程
				if(!widDis.isEmpty()) {	// 非空
					blockingQueue.put(new TempClass(nid, widDis));
				}
			}
			
			// 释放那些指向其的节点都已经被访问了、的节点的wid2dis占用的空间
			for(index = 0; index < accessNids.size(); index++) {
				nid = accessNids.get(index);
				edgeNids = revGraph[nid]; // 获得指向nid的节点集合
				if(null == edgeNids) {
					nidDatas[nid] = null;
				} else {
					for(i=0; i<edgeNids.length; i++)
						if(!signAccess[i])	break;
					if(i == edgeNids.length)	nidDatas[nid] = null;	// 附近反向点已全部遍历完了	
				}
			}
			
			dealedNum++;
			
			if(dealedNum%100 == 0) {
				System.out.println("> 已处理" + dealedNum + "个pid, 用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()));
				System.gc();
				System.gc();
			}
		}
		
		blockingQueue.put(new TempClass(-1, null));	// 发送结束信号
	}
	
	/**
	 * 写文件
	 */
	public void writeToFile() {
		try {
			TempClass tc = null;
			while(true) {
				tc = blockingQueue.take();
				if(tc.pid == -1)	break;
				fileWriter.write(tc);
			}
			fileWriter.close();
			endSignQueue.put(0);	// 发送完成信号
		} catch (Exception e) {
			System.err.println("> 写文件" + Global.pWReachTimesPath + " 失败！！！");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * 启动
	 * @throws Exception
	 */
	public static void buildingPid2WidsFiles() throws Exception {
		System.out.println("> 开始写Pid2Wids文件 . . . . " + TimeUtility.getTime());
		long start = System.currentTimeMillis();
		ArrayBlockingQueue<TempClass> blockingQueue = new ArrayBlockingQueue<>(50);
		ArrayBlockingQueue<Integer> endSignQueue = new ArrayBlockingQueue<>(1);
		new Thread(new P2WReachOpt(1, blockingQueue, endSignQueue)).start();
		new Thread(new P2WReachOpt(2, blockingQueue, endSignQueue)).start();
		endSignQueue.take();
		System.out.println("> 结束写Pid2Wids文件 . " + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()) + "   " + TimeUtility.getTime());
	}
	
	public static void main(String[] args) throws Exception{
		buildingPid2WidsFiles();
	}
	
}
