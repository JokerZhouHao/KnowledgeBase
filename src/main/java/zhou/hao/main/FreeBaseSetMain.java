package zhou.hao.main;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import zhou.hao.entry.LineList;
import zhou.hao.processor.LineConsumer;
import zhou.hao.processor.LineProductor;
import zhou.hao.processor.MinMemoryConsumer;
import zhou.hao.service.RateMonitorService;
import zhou.hao.tools.TimeStr;

public class FreeBaseSetMain {
	public static void main(String[] args) throws Exception{
		LineList<String> lineList[] = new LineList[2];
		lineList[0] = new LineList<String>(8);
		lineList[1] = new LineList<String>(8);
		LinkedBlockingQueue<Integer> que1 = new LinkedBlockingQueue<Integer>(2);
		LinkedBlockingQueue<Integer> que2 = new LinkedBlockingQueue<Integer>(2);
		que2.put(0);
		que2.put(1);
		ExecutorService exec = Executors.newCachedThreadPool();
		System.out.println("> " + TimeStr.getTime() + ", 开始处理. . .\n");
		exec.execute(new LineProductor(lineList, que1, que2));
//		exec.execute(new LineConsumer(lineList, que2, que1));
		MinMemoryConsumer mmc = new MinMemoryConsumer(lineList, que2, que1);
		exec.execute(mmc);
		exec.execute(new RateMonitorService(mmc));
		exec.shutdown();
	}
}
