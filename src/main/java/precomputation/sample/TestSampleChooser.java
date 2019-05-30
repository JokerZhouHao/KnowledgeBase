package precomputation.sample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import entity.sp.GraphByArray;
import utility.FileMakeOrLoader;
import utility.Global;
import utility.IOUtility;
import utility.LoopQueue;
import utility.MLog;
import utility.RandomNumGenerator;
import utility.TimeUtility;

public class TestSampleChooser {
	private static GraphByArray graph = null;
	
	/**
	 * 选取测试样本，随机选点，然后选取点的首词
	 * @param sampleNum
	 * @param vidFile
	 * @param pidCoordFile
	 * @param sampleFile
	 * @throws Exception
	 */
	public static void productTestSampleByFirstWid(int sampleNum, String vidFile, String pidCoordFile, String sampleFile) throws Exception{
		Long start = System.currentTimeMillis();
		System.out.println("> 开始从原始文件中随机选取" + sampleNum + "个测试样本 . . . ");
		Map<Integer, double[]> pidCoordMap = new HashMap<>();
		double[] doubleArr = null;
		BufferedReader br = new BufferedReader(new FileReader(pidCoordFile));
		String line = br.readLine();
		String[] strArr = null;
		int id = 0;
		int recSamNum = 1;
		
		// 将坐标信息读入内存
		while(null != (line = br.readLine())) {
			strArr = line.split(Global.delimiterLevel1);
			id = Integer.parseInt(strArr[0]);
			strArr = strArr[1].split(Global.delimiterSpace);
			doubleArr = new double[2];
			for(int i=0; i<strArr.length; i++) {
				doubleArr[i] = Double.parseDouble(strArr[i]);
			}
			pidCoordMap.put(id, doubleArr);
		}
		br.close();
		
		// 写样本
		BufferedWriter bw = new BufferedWriter(new FileWriter(sampleFile));
		bw.write(String.valueOf(sampleNum) + Global.delimiterPound + "\n");
		// 偏移的行数
		RandomNumGenerator lineOffsetGe = new RandomNumGenerator(1, (int)(Global.numContainCoordWordDate/(sampleNum * 1.5)));
		int lineOffset = lineOffsetGe.getRandomInt();
		// 搜索日期的偏移数
		RandomNumGenerator dateOffsetGe = new RandomNumGenerator(1, 10);
		
		int sampK = 10;
		double[] sampCoord = null;
		double[] tempCoord = null;
		String sampDate = null;
		ArrayList<Integer> sampQwords = null;
		int i =0;
		Set<Integer> recSet = new TreeSet<Integer>();
		
		while(recSamNum <= sampleNum) {
			br = new BufferedReader(new FileReader(vidFile));
			br.readLine();
			while(null != (line = br.readLine())) {
				if(--lineOffset == 0) {
					lineOffset = lineOffsetGe.getRandomInt();
					strArr = line.split(Global.delimiterLevel1);
					id = Integer.parseInt(strArr[0]);
					if(!recSet.contains(id) && null != (tempCoord = pidCoordMap.get(id))) {
						// 坐标
						sampCoord = new double[2];
						sampCoord[0] = tempCoord[0] + RandomNumGenerator.getRandomFloat();
						sampCoord[1] = tempCoord[1] + RandomNumGenerator.getRandomFloat();
						if (sampCoord[0] >= -90 && sampCoord[0] <= 90 && sampCoord[1] >= -180 && sampCoord[1] <= 180) {
							strArr = strArr[1].split(Global.delimiterDate);
							// 时间
							String orgDate = strArr[0];
							sampDate = TimeUtility.getOffsetDate(strArr[0], dateOffsetGe.getRandomInt());
							// qwords
							strArr = strArr[strArr.length - 1].split(Global.delimiterLevel2);
							if(strArr.length > 2) {
								sampQwords = new ArrayList<>();
								for(i =0 ;i < 2; i++) {
									sampQwords.add(Integer.parseInt(strArr[i]));
								}
								// 输出
								bw.write(String.valueOf(recSamNum) + Global.delimiterLevel1 + String.valueOf(id) + " ");
								bw.write(String.valueOf(sampK) + " ");
								bw.write(String.valueOf(tempCoord[0]) + " ");
								bw.write(String.valueOf(tempCoord[1]) + " ");
								bw.write(strArr[0] + " ");
								bw.write(strArr[1] + " ");
								bw.write(orgDate + "\n");
								bw.write(String.valueOf(sampK) + " ");
								for(i=0; i<sampCoord.length; i++) {
									bw.write(String.valueOf(sampCoord[i]) + " ");
								}
								for(i=0; i<sampQwords.size(); i++) {
									bw.write(String.valueOf(sampQwords.get(i)) + " ");
								}
								bw.write(sampDate + "\n");
								recSet.add(id);
								if(++recSamNum > sampleNum)	break;
							}
						}
					}
				}
			}
			br.close();
		}
		bw.close();
		System.out.println("> 完成选取" + sampleNum + "个测试样本.");
	}
	
	/**
	 * 选取测试单个时间所需要的样本
	 * bfs随机选取wNum/2--wNum*2个点，从这些点的词构成的词集中随机选择wNum个词，时间范围为包含bfs选取的点的时间的最小范围
	 * @param sampleNum
	 * @param wNum
	 * @throws Exception
	 */
	public static void productSingleDateTestSampleForPaper(int sampleNum, int wNum) throws Exception{
		System.out.println("> 开始从原始文件中随机选取" + sampleNum + "个测试样本 . . . ");
		
		// 读取坐标信息
		double[][] pidCoords = new double[Global.numPid][];
		for(int i=0; i<pidCoords.length; i++) {
			pidCoords[i] = new double[2];
		}
		String fp = Global.inputDirectoryPath + Global.pidCoordFile;
		BufferedReader br = IOUtility.getBR(fp);
		String line = br.readLine();
		String[] strArr = null;
		int i= 0;
		while(null != (line = br.readLine())) {
			strArr = line.split(Global.delimiterLevel1)[1].split(Global.delimiterSpace);
			pidCoords[i][0] = Double.parseDouble(strArr[0]);
			pidCoords[i][1] = Double.parseDouble(strArr[1]);
			i++;
		}
		br.close();
		
		// 读取
		Map<Integer, int[][]> nidDatesWids = new HashMap<>();
		fp = Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile;
		br = IOUtility.getBR(fp);
		int[][] tIntArr = null;
		String[] dates = null;
		String[] wids = null;
		Integer nid;
		
		br.readLine();
		while(null != (line=br.readLine())) {
			tIntArr = new int[2][];
			
			strArr = line.split(Global.delimiterLevel1);
			nid = Integer.parseInt(strArr[0]);
			
			dates = strArr[1].split(Global.delimiterDate);
			tIntArr[0] = new int[dates.length-1];
			for(i=0; i<tIntArr[0].length; i++) {
				tIntArr[0][i] = Integer.parseInt(dates[i]);
			}
			
			wids = dates[dates.length-1].split(Global.delimiterLevel2);
			tIntArr[1] = new int[wids.length];
			for(i=0; i<tIntArr[1].length; i++) {
				tIntArr[1][i] = Integer.parseInt(wids[i]);
			}
			
			nidDatesWids.put(nid, tIntArr);
		}
		br.close();
		
		// 加载图
		if(null == graph) {
			graph = new GraphByArray(Global.numNodes);
			graph.loadGraph(Global.inputDirectoryPath + Global.edgeFile);
		}
		int[] edges = null;
		
		// 开始生成样本文件
		BufferedWriter bw = IOUtility.getBW(Global.inputDirectoryPath + Global.testSampleFile + "." + String.valueOf(sampleNum) + ".t=0.wn=" + String.valueOf(wNum));
		int numSample = Global.testOrgSampleNum;
		int numSampleWids = wNum;
		double[] sampCoords = new double[2];
		Set<Integer> allWidsSet = new HashSet<>();
		List<Integer> allWidList = new ArrayList<>();
		List<Integer> sampWids = new ArrayList<>();
		int minDate = 0;
		int maxDate = 0;
		Integer pid;
		
		LoopQueue<Integer> queue = new LoopQueue<>(100000);
		HashSet<Integer> recBfs = new HashSet<Integer>();
		Set<Integer> recPids = new HashSet<>();
		
		// pid随机随机产生器
		RandomNumGenerator pidGe = new RandomNumGenerator(0, Global.numPid);
		// 选取样本点数产生器
//		RandomNumGenerator sampNodeNumGe = new RandomNumGenerator(numSampleWids/2, numSampleWids * 2);
//		RandomNumGenerator sampNodeNumGe = new RandomNumGenerator(numSampleWids/3, numSampleWids/3 + 3);
		// bfs碰到多少个带有时间的点时才停止
//		RandomNumGenerator numBfsMeetDateNode = new RandomNumGenerator(1, 10);
//		RandomNumGenerator numBfsMeetDateNode = new RandomNumGenerator(1, 2);
		
		while(0 != numSample) {
			while(true) {
				pid = pidGe.getRandomInt();
				if(!recPids.contains(pid)) {
					// 生成坐标
					while(true) {
						sampCoords[0] = pidCoords[pid][0] + RandomNumGenerator.getRandomFloat();
						sampCoords[1] = pidCoords[pid][1] + RandomNumGenerator.getRandomFloat();
						if (sampCoords[0] >= -90 && sampCoords[0] <= 90 && sampCoords[1] >= -180 && sampCoords[1] <= 180)
							break;
					}
					recPids.add(pid);
					break;
				}
			}
			
//			int sampNodeNum = sampNodeNumGe.getRandomInt();
//			int numMeetDateNode = numBfsMeetDateNode.getRandomInt();
			int sampNodeNum = 3;
			int numMeetDateNode = 1;
			allWidsSet.clear();
			minDate = Integer.MAX_VALUE;
			maxDate = Integer.MIN_VALUE;
			
			// bfs
			nid = pid;
			queue.reset();
			queue.push(nid);
			recBfs.clear();
			while(null != (nid = queue.poll())) {
				if(null != (edges =  graph.getEdge(nid))) {
					for(int e : edges) {
						if(!recBfs.contains(e)) {
							if(!queue.push(e)) {
								throw new Exception("队列" + queue.size() + "太短");
							}
							recBfs.add(e);
						}
					}
				}
				// 更新时间和词
				if(null != (tIntArr = nidDatesWids.get(nid))) {
					for(int ii : tIntArr[0]) {
						if(ii != Integer.MAX_VALUE && TimeUtility.noLegedDate(ii))	continue;
						for(int kk : tIntArr[1]) {
							allWidsSet.add(kk);
						}
						if(ii == Integer.MAX_VALUE)	continue;
						if(minDate > ii)	minDate = ii;
						if(maxDate < ii)	maxDate = ii;
						numMeetDateNode--;
					}
				}
				if(--sampNodeNum <= 0)	break;
			}
			
			if(minDate == Integer.MAX_VALUE)	continue;
			
			// 处理生成的样本数据
			if(allWidsSet.size()>=10) {
				allWidList.clear();
				sampWids.clear();
				for(int ii : allWidsSet) {
					allWidList.add(ii);
				}
				int wSpan = allWidList.size()/numSampleWids;
				if(0==wSpan) wSpan = 1;
				for(i=0; i<allWidList.size(); i+=wSpan) {
					sampWids.add(allWidList.get(i));
					if(sampWids.size()==numSampleWids)	break;
				}
			} else continue;
			
			// 写样本文件
			bw.write(String.valueOf(sampleNum- numSample + 1) + Global.delimiterLevel1);
			bw.write(String.valueOf(sampCoords[0]) + " " + String.valueOf(sampCoords[1]) + " ");
			for(int ii : sampWids) {
				bw.write(String.valueOf(ii) + " ");
			}
			bw.write(TimeUtility.getDateByIntDate(minDate) + " ");
			bw.write(TimeUtility.getDateByIntDate(maxDate) + "\n");
			numSample--;
		}
		bw.close();
		System.out.println("> Over从原始文件中随机选取" + sampleNum + "个测试样本, " + TimeUtility.getTailTime());
	}
	
	
	public static void productRangeDateTestSampleForPaper(int sampleNum, int wNum) throws Exception{
		System.out.println("> 开始从原始文件中随机选取" + sampleNum + "个测试样本 . . . ");
		
		// 读取坐标信息
		String fp = Global.inputDirectoryPath + Global.pidCoordFile;
		double[][] pidCoords = FileMakeOrLoader.loadArrayCoord(fp);
		
		// 读取
		fp = Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile;
		Map<Integer, int[][]> nidDatesWids = FileMakeOrLoader.loadArrayNid2DatesWids(fp);
		
		// 加载图
		if(null == graph) {
			graph = new GraphByArray(Global.numNodes);
			graph.loadGraph(Global.inputDirectoryPath + Global.edgeFile);
		}
		int[] edges = null;
		
		int[][] tIntArr = null;
		Integer nid, i;
		
		// 开始生成样本文件
		BufferedWriter bw = IOUtility.getBW(Global.inputDirectoryPath + Global.testSampleFile + "." + String.valueOf(sampleNum) + ".t=1.wn=" + String.valueOf(wNum));
		int numSample = Global.testOrgSampleNum;
		int numSampleWids = wNum;
		double[] sampCoords = new double[2];
		Set<Integer> allWidsSet = new HashSet<>();
		List<Integer> allWidList = new ArrayList<>();
		List<Integer> sampWids = new ArrayList<>();
		int minDate = 0;
		int maxDate = 0;
		Integer pid;
		
		LoopQueue<Integer> queue = new LoopQueue<>(100000);
		HashSet<Integer> recBfs = new HashSet<Integer>();
		Set<Integer> recPids = new HashSet<>();
		
		// pid随机随机产生器
		RandomNumGenerator pidGe = new RandomNumGenerator(0, Global.numPid);
		
		// 日期偏移
		RandomNumGenerator dateGe = new RandomNumGenerator(1, 6);
		
		while(0 != numSample) {
			while(true) {
				pid = pidGe.getRandomInt();
				if(!recPids.contains(pid)) {
					// 生成坐标
					while(true) {
						sampCoords[0] = pidCoords[pid][0] + RandomNumGenerator.getRandomFloat();
						sampCoords[1] = pidCoords[pid][1] + RandomNumGenerator.getRandomFloat();
						if (sampCoords[0] >= -90 && sampCoords[0] <= 90 && sampCoords[1] >= -180 && sampCoords[1] <= 180)
							break;
					}
					recPids.add(pid);
					break;
				}
			}
			
			// bfs
			nid = pid;
			queue.reset();
			queue.push(nid);
			recBfs.clear();
			tIntArr = null;
			while(null != (nid = queue.poll())) {
				if(null != (edges =  graph.getEdge(nid))) {
					for(int e : edges) {
						if(!recBfs.contains(e)) {
							if(!queue.push(e)) {
								System.err.println("> 队列" + queue.size() + "太短");
								System.exit(0);
							}
							recBfs.add(e);
							
							// 更新时间和词
							if(null != (tIntArr = nidDatesWids.get(nid)) && tIntArr[1].length >= wNum) {
								break;
							}
							
						}
					}
					if(null != tIntArr && tIntArr[1].length >= wNum)	break;
				}
			}
			if(queue.poll()==null)	continue;
			
			// 处理生成的样本数据
			if(tIntArr[0].length >= 2) {
				// 下面的-1和+1是为了避免linux和本机的时区不一样，通过相同的date字符串转换出的天数多一天或少一天
				minDate = tIntArr[0][0] - 1;
				maxDate = tIntArr[0][tIntArr.length-2] + 1; 
			} else {
//				minDate = tIntArr[0][0] - dateGe.getRandomInt();
//				maxDate = tIntArr[0][0] + dateGe.getRandomInt();
				minDate = tIntArr[0][0];
				maxDate = tIntArr[0][0];
			}
			sampWids.clear();
			for(i=0; i<wNum; i++) {
				sampWids.add(tIntArr[1][i]);
			}
			
			// 写样本文件
			bw.write(String.valueOf(sampleNum- numSample + 1) + Global.delimiterLevel1);
			bw.write(String.valueOf(sampCoords[0]) + " " + String.valueOf(sampCoords[1]) + " ");
			for(int ii : sampWids) {
				bw.write(String.valueOf(ii) + " ");
			}
			bw.write(TimeUtility.getDateByIntDate(minDate) + " ");
			bw.write(TimeUtility.getDateByIntDate(maxDate) + "\n");
			numSample--;
		}
		bw.close();
		System.out.println("> Over从原始文件中随机选取" + sampleNum + "个测试样本, " + TimeUtility.getTailTime());
	}
	
	
	
	/**
	 * 0.8概率取有时间的，0.2概率取无时间的
	 * @return
	 */
	public static Boolean isDate() {
		if(RandomNumGenerator.getRandomFloat() <= 0.8)	return Boolean.TRUE;
		else return Boolean.FALSE;
	}
	
	/**
	 * 使用论文上的方法产生测试样本
	 * @param sampleNum
	 * @param wNum
	 * @throws Exception
	 */
	public static void productTestSampleByPaper(int sampleNum, int wNum) throws Exception {
		String pathSample = Global.inputDirectoryPath + Global.testSampleFile + "." + String.valueOf(sampleNum) + ".wn=" + String.valueOf(wNum);
		MLog.log("开始生成测试文件 " + pathSample + " . . . ");
		
		// 读取坐标信息
		String fp = Global.inputDirectoryPath + Global.pidCoordFile;
		double[][] pidCoords = FileMakeOrLoader.loadArrayCoord(fp);
		// 读取
		fp = Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile;
		Map<Integer, int[][]> nidDatesWids = FileMakeOrLoader.loadArrayNid2DatesWids(fp);
		
		// 加载图
		if(null == graph) {
			graph = new GraphByArray(Global.numNodes);
			graph.loadGraph(Global.inputDirectoryPath + Global.edgeFile);
		}
		int[] edges = null;
		
		int[][] tIntArr = null;
		Integer nid;
		
		// 开始生成样本文件
		BufferedWriter bw = IOUtility.getBW(pathSample);
		double[] sampCoords = new double[2];
		
		Integer pid;
		
		LoopQueue<Integer> queue = new LoopQueue<>(100000);
		HashSet<Integer> recBfs = new HashSet<Integer>();
		Set<Integer> recPids = new HashSet<>();
		
		// pid随机随机产生器
		RandomNumGenerator pidGe = new RandomNumGenerator(0, Global.numPid);
		
		MLog.log("数据加载完成");
		
		int index = 1;
		
		// 开始生成测试样本
		while(index <= sampleNum) {
			// 生成坐标
			while(true) {
				pid = pidGe.getRandomInt();
				if(!recPids.contains(pid)) {
					while(true) {
						sampCoords[0] = pidCoords[pid][0] + RandomNumGenerator.getRandomFloat();
						sampCoords[1] = pidCoords[pid][1] + RandomNumGenerator.getRandomFloat();
						if (sampCoords[0] >= -90 && sampCoords[0] <= 90 && sampCoords[1] >= -180 && sampCoords[1] <= 180)
							break;
					}
					recPids.add(pid);
					break;
				}
			}
			
			// 取[n/2, n*2]
			int numBfsNode = RandomNumGenerator.getRandomInt(wNum/2 > 0 ? wNum/2 : 1, wNum * 2);
			List<Integer> bfsNode = new ArrayList<>();
			nid = pid;
			queue.reset();
			queue.push(nid);
			recBfs.clear();
			recBfs.add(nid);
			while(null != (nid = queue.poll())) {
				if(null != (edges = graph.getEdge(nid))) {
					for(int e : edges) {
						if(!recBfs.contains(e)) {
							if(!queue.push(e)) {
								MLog.log("队列" + queue.size() + "太短");
								System.exit(0);
							}
							recBfs.add(e);
						}
					}
				}
				
				bfsNode.add(nid);
				if((--numBfsNode) == 0)	break;
			}
			if(numBfsNode != 0)	continue;
			
			// 取其中随机个[1, n]个node
			int numRandomNode = RandomNumGenerator.getRandomInt(1, wNum);
			Set<Integer> randomNids = new HashSet<>();
			RandomNumGenerator g = new RandomNumGenerator(0, bfsNode.size() - 1);
			while(numRandomNode-- > 0) {
				randomNids.add(bfsNode.get(g.getRandomInt()));
			}
			
			// 提取时间、词
			List<Integer> dates = new ArrayList<>();
			List<Integer> widsHasDate = new ArrayList<>();
			List<Integer> widsNoDate = new ArrayList<>();
			for(int nd : randomNids) {
				tIntArr = nidDatesWids.get(nd);
				if(tIntArr != null) {
					if(tIntArr[0][0] != Global.TIME_INAVAILABLE) {
						dates.add(tIntArr[0][0]);
						for(int nd1 : tIntArr[1])	widsHasDate.add(nd1);
					} else {
						for(int nd1 : tIntArr[1])	widsNoDate.add(nd1);
					}
				}
			}
			
			// 选取样本
			if(dates.isEmpty() || widsHasDate.size() < wNum || widsNoDate.size() < wNum)	continue;
			int samDate = 0;
			List<Integer> samWids = new ArrayList<>();
			int numWid = wNum;
			samDate = dates.get(RandomNumGenerator.getRandomInt(0, dates.size() - 1));
			while(numWid-- > 0) {
				int ii = 0;
				if(isDate()) {
					ii = RandomNumGenerator.getRandomInt(0, widsHasDate.size() - 1);
					samWids.add(widsHasDate.get(ii));
					widsHasDate.remove(ii);
				} else {
					ii = RandomNumGenerator.getRandomInt(0, widsNoDate.size() - 1);
					samWids.add(widsNoDate.get(ii));
					widsNoDate.remove(ii);
				}
			}
			
			// 写样本
			String line = index + Global.delimiterLevel1;
			for(double d : sampCoords)	line += d + Global.delimiterSpace;
			for(int ii : samWids)	line += ii + Global.delimiterSpace;
			line += TimeUtility.getDateByIntDate(samDate);
			bw.write(line + "\n");
			
//			MLog.log("已生成" + index + " : " + line + " 个样本");
			index++;
		}
		
		bw.close();
		MLog.log("productTestSampleByBfsForPaper: <" + fp + "> over");
	}
	
	
	public static void main(String[] args) throws Exception{
//		SampleChooser.productTestSampleByFirstWid(100, Global.inputDirectoryPath + Global.nodeIdKeywordListOnDateFile, 
//				Global.inputDirectoryPath + Global.pidCoordFile, 
//				Global.inputDirectoryPath + Global.testSampleFile);
		int a[] = new int[1];
		a[0] = 3;
//		a[1] = 3;
//		a[2] = 5;
//		a[3] = 8;
//		a[4] = 10;
		for(int ii : a) {
//			TestSampleChooser.productSingleDateTestSampleForPaper(500, ii);
//			TestSampleChooser.productRangeDateTestSampleForPaper(500, ii);
			TestSampleChooser.productTestSampleByPaper(500, ii);
		}
	}
}