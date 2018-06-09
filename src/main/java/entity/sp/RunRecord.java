package entity.sp;

import utility.Global;

// 记录运行的相关情况
public class RunRecord {
	public static long startTime = System.currentTimeMillis();
	
	public long frontTime = 0;
	public void setFrontTime() {
		frontTime = System.currentTimeMillis();
	}
	public long getTimeSpan() {
		return System.currentTimeMillis() - frontTime;
	}
	
	// 初始化时间
	public long timeLoadTFLable = 0;
	public long timeBuildRGI = 0;
	public long timeBuildSPCompleteDisk = 0;
	
	public String getInitInfo() {
		return  String.valueOf(Global.testOrgSampleNum) + "#\n" +
				"timeLoadTFLable : " + String.valueOf(timeLoadTFLable/1000) + "\n" + 
				"timeBuildRGI : " + String.valueOf(timeBuildRGI/1000) + "\n" + 
				"timeBuildSPCompleteDisk : " + String.valueOf(timeBuildSPCompleteDisk/1000) + "\n";
	}
	
	// bsp方法里面的时间
	public long timeBspStart = 0;
	
	public long timeBspSearchWid2DateNid = 0;
	public long timeBspBuidingWid2DateNid = 0;
	public long numBspWid2DateWid = 0;
	public long limitBuidingWid2DateNid = 1000 * 180;
	
	public long timeBspGetPN = 0;
	
	public long timeEnterkSPComputation = 0;
	
	public long timeKSPComputation = 0;
	public void setTimeKSPComputation() {
		timeKSPComputation = System.currentTimeMillis() - timeEnterkSPComputation;
	}
	
	public long timeBspClearJob = 0;
	
	public long timeBsp = 0;
	public void setTimeBsp() {
		timeBsp = System.currentTimeMillis() - timeBspStart;
	}
	
	
	// KSPComputation计算里面的时间
	public long timeCptQueueRemove = 0;
	public long numCptQueueRemove = 0;
	
	public long numCptTotalPid2Wids = 0;
	public long numCptPrunePid2Wids = 0;
	public long timeCptPid2Wids = 0;
	
	
	public long timeCptGetMinDateSpan = 0;
	public long numCptGetMinDateSpan = 0;
	
	public long limitBsp = 120 * 1000;
	
	public Boolean isCptOverTime() {
		if(System.currentTimeMillis() - timeBspStart > limitBsp) {
			return Boolean.TRUE;
		} else return Boolean.FALSE;
	}
	
	public long timeCptQueuePut = 0;
	public long numCptQueuePut = 0;
	
	public long numCptPruneRTeeNode = 0;
	public long numCptPruneRTreePid = 0;
	
	public long numGetSemanticTree = 0;
	
	public long numCptPruneInSemanticTree = 0;
	public long timeCptGetSemanticTree = 0;
	
	public long numLastQueue = 0;
	public long numCptGetMinDateSpanLeftSpan = 0;
	public long numCptGetMinDateSpanRightSpan = 0;
	
	public double queueLastValue = 0;
	public double kthScore = 0;
	public int resultSize = 0;
	
	public String getHeader() {
		return "id,timeBspSearchWid2DateNid,numBspWid2DateWid,timeBspBuidingWid2DateNid,timeBspGetPN,"
				+ "numCptQueueRemove,timeCptQueueRemove,"
				+ "numCptTotalPid2Wids,numCptPrunePid2Wids,timeCptPid2Wids,"
				+ "numCptPruneRTeeNode,numCptPruneRTreePid,numCptPruneInSemanticTree,"
				+ "numCptGetMinDateSpan,timeCptGetMinDateSpan,"
				+ "numCptQueuePut,timeCptQueuePut,numLastQueue,"
				+ "numGetSemanticTree,timeCptGetSemanticTree,"
				+ "numCptGetMinDateSpanLeftSpan,numCptGetMinDateSpanRightSpan,"
				+ "timeKSPComputation,timeBsp,"
				+ "queueLastValue,kthScore,resultSize,\n";
	}
	
	public String getBspInfo(int id, int base) {
		return  String.valueOf(id) + "," + String.valueOf(timeBspSearchWid2DateNid/base) + "," + String.valueOf(numBspWid2DateWid) + "," + String.valueOf(timeBspBuidingWid2DateNid/base) + "," + String.valueOf(timeBspGetPN/base) + "," + 
				String.valueOf(numCptQueueRemove) + "," + String.valueOf(timeCptQueueRemove/base) + "," + 
				String.valueOf(numCptTotalPid2Wids) + "," + String.valueOf(numCptPrunePid2Wids) + "," + String.valueOf(timeCptPid2Wids/base) + "," +
				String.valueOf(numCptPruneRTeeNode) + "," + String.valueOf(numCptPruneRTreePid) + "," + String.valueOf(numCptPruneInSemanticTree) + "," +
				String.valueOf(numCptGetMinDateSpan) + "," + String.valueOf(timeCptGetMinDateSpan/base) + "," + 
				String.valueOf(numCptQueuePut) + "," + String.valueOf(timeCptQueuePut/base) + "," + String.valueOf(numLastQueue) + "," +
				String.valueOf(numGetSemanticTree) + "," + String.valueOf(timeCptGetSemanticTree/base) + "," +
				String.valueOf(numCptGetMinDateSpanLeftSpan) + "," + String.valueOf(numCptGetMinDateSpanRightSpan) + "," + 
				String.valueOf(timeKSPComputation/base) + "," + String.valueOf(timeBsp/base) + "," +
				String.valueOf(queueLastValue) + "," + String.valueOf(kthScore/base) + "," + String.valueOf(resultSize) + ",\n";
	}
}
