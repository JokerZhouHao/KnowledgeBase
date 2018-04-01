package zhou.hao.test;

public class JNITest {
	
	public native boolean testJNI(String str);
	
	public static void main(String[] args) {
		JNITest te = new JNITest();
		System.loadLibrary("JNITest");
		boolean bo = te.testJNI("JNITest success ");
		System.out.println(bo);
	}
}
