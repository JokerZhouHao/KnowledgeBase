package utility;

public class MLog {
	public static synchronized void log(String info) {
		System.out.println("[" + TimeUtility.getTime() + "] " + info);
	}
	
	public static void main(String[] args) {
		MLog.log("zhou");
	}
}
