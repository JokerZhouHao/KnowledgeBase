package zhou.hao.tools;

public class MemoryInfo {
	private static Runtime rt = Runtime.getRuntime();
	
	public static String getFreeMemory() {
		return String.valueOf(rt.freeMemory()/1000000) + "M";
	}
	
	public static String getTotalMemory() {
		return String.valueOf(rt.totalMemory()/1000000) + "M";
	}
	
	public static String getUsedMemory() {
		return String.valueOf((rt.totalMemory()-rt.freeMemory())/1000000) + "M";
	}
	
	public static String getTotalFreeUsedAvailable() {
		return "totalMemory=" + MemoryInfo.getTotalMemory() + " freeMemory=" + MemoryInfo.getFreeMemory() + " usedMemory=" + MemoryInfo.getUsedMemory();
	}
}
