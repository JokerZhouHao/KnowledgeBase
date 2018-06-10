package entity.sp.date;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.lucene.analysis.CharArrayMap.EntrySet;

import entity.sp.AllDateWidNodes;
import entity.sp.AllDateWidNodes.DWid;
import entity.sp.AllPidWid;
import entity.sp.DateNidNode;
import entity.sp.DatesWIds;
import entity.sp.GraphByArray;
import entity.sp.SortedDateWidCReach;
import precomputation.rechable.ReachableQueryService;
import precomputation.sp.IndexNidKeywordsListService;
import utility.Global;
import utility.IOUtility;
import utility.LoopQueue;
import utility.TimeUtility;
import utility.Utility;

/**
 * 表示：
 * 		w1		w2
 * p1	times1	times2
 * p2	times3	times4
 * @author Monica
 *
 */

class PidWidDates{
	public int pid = -1;
	public Map<Integer, Set<Integer>> widDates = null;
	public PidWidDates(int pid, Map<Integer, Set<Integer>> wd) {
		this.pid = pid;
		this.widDates = wd;
	}
}

public class PWReachDate extends ReachDate implements Runnable{
	
	private Map<Integer, Map<Integer, List<Integer>>> pwTimes = new HashMap<>();
	
	private static GraphByArray graph = null;
	private static Map<Integer, DWid> allDW = null;
	private static List<Integer> allPid = null;
	private static boolean hasInit = false;
	
	private int start;
	private int end;
	
	private static int dealedNum = 0;
	
	private static int zipNum = 40;
	public static int zipContianNodeNum = Global.numPid / zipNum;
	
	private ArrayBlockingQueue<PidWidDates> blockingQueue = null;
	
	public PWReachDate() {
		this.init();
	}
	
	public PWReachDate(String fp) {
		this.filePath = fp;
	}
	
	public PWReachDate(ArrayBlockingQueue<PidWidDates> qu, int start, int end){
		this.init();
		this.blockingQueue = qu;
		this.start = start;
		this.end = end;
	}
	
	public void init() {
		if(hasInit)	return;
		hasInit = true;
		try {
			System.out.println("> 开始初始化PWReachDate . . . ");
			graph = new GraphByArray(Global.numNodes);
			graph.loadGraph(Global.inputDirectoryPath + Global.edgeFile);
			allDW = AllDateWidNodes.loadFromFile(Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile);
			allPid = AllPidWid.getAllPid();
			System.out.println("> 成功初始化PWReachDate 。 ");
		} catch (Exception e) {
			System.err.println("> 初始化PWReachDate失败！");
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
		long start = System.currentTimeMillis();
		int[] edges = null;
		long templ = 0;
		
		DWid tDWid = null;
		Set<Integer> tTS = null;
		
		int pid ;
		
		LoopQueue<Integer> queue = new LoopQueue<>(100000);
		for(int i = this.start; i<this.end; i++) {
			pid = allPid.get(i);
			long startb = System.currentTimeMillis();
			Map<Integer, Set<Integer>> widDates = new HashMap<>();	// 记录各个word的时间串
			queue.reset();
			Integer nid = -1;
			HashSet<Integer> rec = new HashSet<Integer>();
			queue.push(pid);
			rec.add(pid);
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
						if(null == (tTS = widDates.get(wid))) {
							tTS = new TreeSet<>();
							widDates.put(wid, tTS);
						}
						for(int da : tDWid.dates) {
							tTS.add(da);
						}
					}
				}
			}
			rec.clear();
			
			if(!widDates.isEmpty()) {
//				dos.writeInt(pid);
//				this.writeAllSetTimes(dos, widDates);
//				dos.writeInt(Integer.MAX_VALUE);
				blockingQueue.put(new PidWidDates(pid, widDates));
			}
			
			dealedNum++;
//			templ = System.currentTimeMillis() - startb;
//			if(templ > 0) {
//				System.out.println(dealedNum + Global.delimiterLevel1 + templ + " " + widDates.size());
//			}
			
//			for(Entry<Integer, Set<Integer>> en : widDates.entrySet()) {
//				en.getValue().clear();
//			}
//			widDates.clear();
			
			if(dealedNum%40000 == 0) {
				System.out.println("> 已处理" + dealedNum + "个pid, 用时：" + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()));
			}
		}
		blockingQueue.put(new PidWidDates(-1, null));
//		dos.close();
		System.out.println("> 结束 . " + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()) + "   " + TimeUtility.getTime());
	}
	
	/**
	 * 利用可达性测试创建pwTimes
	 * @param nIdWIdDateSer
	 * @param rqSer
	 */
	public void buildingByReach(IndexNidKeywordsListService nIdWIdDateSer, ReachableQueryService rqSer) {
		long start = System.currentTimeMillis();
		System.out.println("> 开始building pWTimesFile . . . " + TimeUtility.getTime());
		
		int dealWidSpan = 4; // 每次处理的wid数
		int curWidIndex = 0;
		int end = 0;
		
		Boolean sign = null;
		Boolean noDealWidOver = Boolean.TRUE;
		
		int frontDate = 0;
		
		List<Integer> allWid = AllPidWid.getAllWid();
		int numPid = allPid.size();
		int numWid = allWid.size();
		int wid = -1;
		int pid = -1;
		
		int wordNumSpan = numWid / 1000;
		int fDealedWordNum = 0;
		int dealedWordNum = 0;
		
		int i = 0, j = 0;
		long tempL = 0;
		Map<Integer, List<Integer>> wTimes = null;
		
		while(noDealWidOver){
			curWidIndex = i;
			
			// 建立dealWidSpan个SortedDateWid
			List<SortedDateWidCReach> wTimesList = new ArrayList<>();
			for(; i<curWidIndex+dealWidSpan; i++) {
				if(i == numWid) {
					noDealWidOver = Boolean.FALSE;
					break;
				}
				wid = allWid.get(i);
				Map<Integer, String> tempMap = nIdWIdDateSer.searchNIDKeyListDate(wid);
				System.out.println("> tempMap.size = " + tempMap.size());
				SortedDateWidCReach sdw = new SortedDateWidCReach();
				List<Integer> dateList = new ArrayList<>();
				String tempArr[] = null;
				for(Entry<Integer, String> en : tempMap.entrySet()) {
					tempArr = en.getValue().split(Global.delimiterDate);
					for(String st : tempArr) {
						dateList.add(Integer.parseInt(st));
					}
					int t0 = -1;
					for(int din : dateList) {
						if(-1 == t0)	t0 = sdw.addDateWid(new DateNidNode(din, en.getKey()));
						else t0 = sdw.addDateWid(new DateNidNode(din, en.getKey()), t0);
					}
					dateList.clear();
				}
				tempMap.clear();
				dateList.clear();
				wTimesList.add(sdw);
			}
			
			System.out.println("> 获得" + wTimesList.size() + "个SortedDateWid  " + TimeUtility.getTime());
			
			// 构造place word times
			for(j=0; j<numPid; j++) {
				pid = allPid.get(j);
				tempL = pid * Global.numSCCs0;
				HashMap<Long, Boolean> recReach = new HashMap<>();
				end = curWidIndex;
				wTimes = null;
				for(SortedDateWidCReach sdw : wTimesList) {
					List<Integer> dateList = new ArrayList<>();
					frontDate = Integer.MAX_VALUE;
					sign = Boolean.FALSE;
					for(DateNidNode dn : sdw.getDateWidList()) {
						if(sign && frontDate == dn.getDate())	continue;
						if(null==(sign = recReach.get(tempL+ dn.getNid()))) {
							sign = rqSer.queryReachable(pid, dn.getNid());
							recReach.put(tempL + dn.getNid(), sign);
						}
						if(sign) {
							dateList.add(dn.getDate());
							frontDate = dn.getDate();
						}
					}
					
					if(dateList.isEmpty())	continue;
					
					if(null == wTimes && null == (wTimes = pwTimes.get(pid))){
						wTimes = new HashMap<>();
						pwTimes.put(pid, wTimes);
					}
					
//					for(Entry<Integer, List<Integer>> en : wTimes.entrySet()) {
//						if(Utility.isEqualList(en.getValue(), dateList)) {
//							dateList.clear();
//							dateList.add(-1);
//							dateList.add(en.getKey());
//							break;
//						}
//					}
					
					wTimes.put(allWid.get(end), dateList);
					end++;
				}
				recReach.clear();
			}
			
			System.out.println("> 处理了" + wTimesList.size() + "个word  " + TimeUtility.getTime());
			
			dealedWordNum += wTimesList.size();
			
			for(SortedDateWidCReach sdw : wTimesList) {
				sdw.clear();
			}
			wTimesList.clear();
			
			// 输出处理进度
			if(dealedWordNum - fDealedWordNum >= wordNumSpan) {
				System.out.println("> 总词数" + numWid + ", 已处理" + dealedWordNum + "(" + (dealedWordNum * 1000 / numWid)
									+ "/1000), 用时：" + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()) + ". " + TimeUtility.getTime());
				fDealedWordNum = dealedWordNum;
			}
		}
		
		System.out.println("> buiding完成，" + numWid + ", 已处理" + dealedWordNum + "(" + (dealedWordNum * 1000 / numWid)
				+ "/1000), 用时：" + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()) + ". " + TimeUtility.getTime() + "\n");
	}
	
	/**
	 * 写文件
	 */
	@Override
	public void writeToFile() {
		long start = System.currentTimeMillis();
		System.out.println("> 开始写文件" + Global.pWReachTimesPath + " . . . " + TimeUtility.getTime());
		int numDeal = 0;
		int fNumDeal = 0;
		int numPid = pwTimes.size();
		int spanPid = numPid / 100;
		try {
			DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(filePath))));
			for(Entry<Integer, Map<Integer, List<Integer>>> en : this.pwTimes.entrySet()) {
				dos.writeInt(en.getKey());
				this.writeAllTimes(dos, en.getValue());
				if((++numDeal)-fNumDeal > spanPid) {
					System.out.println("> 总pid为" + numPid + ", 已输出" + numDeal + "(" + (numDeal * 1000 / numPid)
							+ "/1000), 用时：" + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()) + ". " + TimeUtility.getTime());
					fNumDeal = numDeal;
				}
			}
			dos.flush();
			dos.close();
		} catch (Exception e) {
			System.err.println("> 写文件" + Global.pWReachTimesPath + " 失败！！！");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("> Over写文件" + Global.pWReachTimesPath + "，" + numPid + ", 已处理" + numDeal + "(" + (numDeal * 1000 / numPid)
				+ "/1000), 用时：" + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()) + ". " + TimeUtility.getTime() + "\n");
	}
	
	/**
	 * 读文件
	 */
	@Override
	public void loadFromFile() {
		long start = System.currentTimeMillis();
		System.out.println("> 开始读文件" + Global.pWReachTimesPath + " . . . " + TimeUtility.getTime());
		int numDeal = 0;
		int fNumDeal = 0;
		int numPid = pwTimes.size();
		int spanPid = numPid / 100;
		
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(Global.pWReachTimesPath))));
			int pid = 0;
			while(true) {
				pid = dis.readInt();
				pwTimes.put(pid, this.loadAllTimes(dis));
				if((++numDeal)-fNumDeal > spanPid) {
					System.out.println("> 总pid为" + numPid + ", 已读取" + numDeal + "(" + (numDeal * 1000 / numPid)
							+ "/1000), 用时：" + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()) + ". " + TimeUtility.getTime());
					fNumDeal = numDeal;
				}
			}
		} catch(EOFException e) {
			try {
				dis.close();
			} catch (Exception e2) {
			}
		} catch (Exception e) {
			System.err.println("> 读文件" + Global.pWReachTimesPath + " 失败！！！");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("> Over读文件" + Global.pWReachTimesPath + "，" + numPid + ", 已处理" + numDeal + "(" + (numDeal * 1000 / numPid)
				+ "/1000), 用时：" + TimeUtility.getSpendTimeStr(start, System.currentTimeMillis()) + ". " + TimeUtility.getTime() + "\n");
	}
	
	public void run(){
		try {
			this.buildingByBFS();
		} catch (Exception e) {
			System.err.println("> PWReachDate异常退出");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	// 计算出原始数据
	public static void getOrginalData() throws Exception{
		int start = 0, end = 0;
		int span = zipContianNodeNum;
		while(end < Global.numPid) {
			start = end;
			end += span;
			if(end > Global.numPid)	end = Global.numPid;
			ArrayBlockingQueue<PidWidDates> bQueue = new ArrayBlockingQueue<>(1);
			DataOutputStream dos = IOUtility.getDGZos(Global.pWReachTimesPath + "." + String.valueOf(start) + "." + String.valueOf(end));
			PWReachDate pwd = new PWReachDate(bQueue, start, end);
			TWidDateWriter tdw = new TWidDateWriter(bQueue, dos);
			new Thread(pwd).start();
			tdw.start();
		}
	}
	
	public static void main(String[] args) throws Exception{
		
		
//		PWReachDate pw = new PWReachDate();
//		pw.buildingByBFS();
		
//		PWReachDate.bfsTest();
		
//		PWReachTimes pWRTimes = new PWReachTimes(Global.pWReachTimesPath);
//		IndexNidKeywordsListService nIdWIdDateSer = new IndexNidKeywordsListService(Global.outputDirectoryPath + Global.indexNIdWordDate);
//		nIdWIdDateSer.openIndexReader();
//		ReachableQueryService reachableQuerySer = new ReachableQueryService(Global.outputDirectoryPath + Global.sccFile, Global.outputDirectoryPath + Global.indexTFLabel);
//		pWRTimes.building(nIdWIdDateSer, reachableQuerySer);
//		nIdWIdDateSer.closeIndexReader();
//		pWRTimes.writeToFile();
	}
}
