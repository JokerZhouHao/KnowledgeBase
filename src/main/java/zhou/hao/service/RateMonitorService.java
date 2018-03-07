package zhou.hao.service;

import zhou.hao.processor.LineConsumer;
import zhou.hao.processor.MinMemoryConsumer;
import zhou.hao.tools.MemoryInfo;
import zhou.hao.tools.TimeStr;

public class RateMonitorService implements Runnable {
	private boolean endSign = false;
	private MinMemoryConsumer lineConsumer = null;
	
	public RateMonitorService(MinMemoryConsumer lc) {
		this.lineConsumer = lc;
	}
	
	public boolean isEndSign() {
		return endSign;
	}

	public void setEndSign(boolean endSign) {
		this.endSign = endSign;
	}

	@Override
	public void run() {
		while(!this.endSign) {
			try {
				Thread.sleep(180000);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("RateMonitorService出现异常");
			}
			Long nowTime = System.nanoTime();
			System.out.println("> 已处理: " + lineConsumer.getPrecessedLineNum()*10000/this.lineConsumer.getTotalLine() + "%%, 共" + lineConsumer.getPrecessedLineNum()
					+ "行，已生成" + lineConsumer.getNodeNum() + "个节点, 当前时间, " + TimeStr.getTime() + ", 共用时 : " + (nowTime - lineConsumer.getStartTime())/1000000000/3600 + "h" + (nowTime - lineConsumer.getStartTime())/1000000000%3600/60
					+ "m" + (nowTime - lineConsumer.getStartTime())/1000000000%3600%60 + "s"
					+ ", 当前内存使用情况如下:\n" + MemoryInfo.getTotalFreeUsedAvailable() + "\n");
		}
	}

}
