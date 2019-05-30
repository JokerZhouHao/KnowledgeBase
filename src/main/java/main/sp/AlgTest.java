package main.sp;

import java.io.File;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import entity.sp.QueryParams;
import precomputation.sample.TestInputDataBuilder;
import utility.Global;
import utility.MLog;
import utility.TimeUtility;

/**
 * 多线程测试算法类
 * @author ZhouHao
 * @since 2019年5月26日
 */
public class AlgTest {
	public final static QueryParams SIGN_OVER_TEST = new QueryParams();
	private static int numTask = 0;
	
	public static synchronized void decreaseTask() {
		numTask--;
//		if(numTask==0) {
//			MLog.log("numTask = 0");
//		}
	}
	
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		
		// args：算法类型[SPBest SPBase SPBestOpt] 线程数   查询集文件名
		if(args.length != 3) {
			throw new Exception("参数必须是：算法类型[SPBest SPBase SPBestOpt] 线程数   查询集文件名");
		}
		
		// 加载查询集
		String samplePath = Global.inputDirectoryPath + File.separator + "sample_result" + File.separator + args[2];
		List<QueryParams> qps = TestInputDataBuilder.loadTestQuery(samplePath);
		if(qps.isEmpty()) {
			MLog.log(samplePath + "无查询");
		}
		numTask = qps.size();
		
		int numThread = Integer.parseInt(args[1]);
		numThread = numThread <= qps.size() ? numThread : qps.size();
		
		MLog.log("开始测试" + args[0] + "    线程数: " + numThread + "    组数: " + qps.size());
		QueryParams.print(qps);
		
		ArrayBlockingQueue<QueryParams> queue = new ArrayBlockingQueue<>(numThread);
		SPInterface sps[] = null;
		if(args[0].equals("SPBest")) {
			sps = new SPBest[numThread];
			for(int i=0; i<numThread; i++)	sps[i] = new SPBest(queue);
		} else {
			throw new Exception("参数必须是：算法类型[SPBest SPBase SPBestOpt] 线程数   查询集文件名");
		}
		
		// 开启线程
		for(SPInterface sb : sps)	new Thread(sb).start();
		
		// 运行测试
		for(QueryParams q : qps) {
			queue.put(q);
		}
		// 发送结束信号
		for(int i=0; i<numThread; i++) {
			queue.put(SIGN_OVER_TEST);
		}
		
		// 是否结束
		while(numTask != 0) {
			Thread.sleep(30000);
		}
		MLog.log("测试结束, 总用时: " + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()));
	}
}
