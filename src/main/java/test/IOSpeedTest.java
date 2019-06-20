package test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import utility.Global;
import utility.IOUtility;
import utility.MLog;

public class IOSpeedTest {
	public static void main(String[] args) throws Exception {
		MLog.log("test IO speed . . . ");
		int num = 0;
		long sum = 0;
		byte[] bs = new byte[1024];
		long startTime = System.currentTimeMillis();
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(Global.inputDirectoryPath + "edgeYagoVB.txt"));
//		FileInputStream bis = new FileInputStream(new File(Global.inputDirectoryPath + "pidCoordYagoVB.txt"));
		while(true) {
			num = bis.read(bs);
			sum += num;
			if(num != bs.length) {
				break;
			}
		}
		bis.close();
		MLog.log("speed = " + sum/(System.currentTimeMillis() - startTime) * 1000 + " Byte/s");
		sum = 0;
		MLog.log("test over");
	}
}
