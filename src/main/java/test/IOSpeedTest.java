package test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import entity.OptMethod;
import entity.sp.QueryParams;
import entity.sp.RunRecord;
import entity.sp.WordRadiusNeighborhood;
import precomputation.sample.TestInputDataBuilder;
import precomputation.sp.IndexWordPNService;
import utility.FileMakeOrLoader;
import utility.Global;
import utility.IOUtility;
import utility.MLog;
import utility.TimeUtility;

public class IOSpeedTest {
	
	public static void speedIOByte() throws Exception{
		MLog.log("test IO speed . . . ");
		int num = 0;
		long sum = 0;
		byte[] bs = new byte[1024];
		long startTime = System.currentTimeMillis();
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(Global.inputDirectoryPath + "allPid.bin"));
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
	
	public static void speedIOPidDatePiar(String[] args) throws Exception {
		MLog.log("start test speedIOPidDatePiar . . . ");
		String samplePath = Global.inputDirectoryPath + File.separator + "sample_result" + File.separator + args[0];
		List<QueryParams> qps = TestInputDataBuilder.loadTestQuery(samplePath);
		if(qps.isEmpty()) {
			MLog.log("Over: " + samplePath + "无查询");
			return;
		}
		QueryParams.print(qps);
		
		QueryParams qp = qps.get(0);
		qp.rr = new RunRecord();
		Set<Integer> recWids = new HashSet<>();
		
		if(qp.optMethod == OptMethod.O0)	qp.MAX_PN_LENGTH = Integer.valueOf(Global.INFINITE_PN_LENGTH_STR);
		IndexWordPNService wIdPnSer = null;
		String wIdPNIndex = Global.outputDirectoryPath + Global.indexWidPN + "_" + String.valueOf(qp.radius) + "_" + String.valueOf(qp.MAX_PN_LENGTH) + File.separator;
		wIdPnSer = new IndexWordPNService(wIdPNIndex);
		wIdPnSer.openIndexReader();
		
		Set<Integer> widHasDate = FileMakeOrLoader.loadWidHasDate();
		
		ArrayList<Integer> qwords = new ArrayList<>();
		
		long startTime = 0;
		byte[] bs = null;
		
		long numQuery = 0;
		
		int samNum = qp.testSampleNum;
		BufferedReader br = new BufferedReader(new FileReader(Global.inputDirectoryPath + Global.testSampleFile + "." + String.valueOf(500) + ".wn=" + qp.numWid));
		String lineStr = null;
		while(samNum > 0) {
			lineStr = br.readLine().trim();
			if(lineStr.isEmpty() || lineStr.startsWith("#"))	continue;
			samNum--;
			
			String[] strArr = lineStr.split(Global.delimiterLevel1)[1].split(Global.delimiterSpace);
			
			qwords.clear();
			for(int i=2; i<2 + qp.numWid; i++) {
				qwords.add(Integer.parseInt(strArr[i]));
			}
			
			Boolean hasWid = Boolean.TRUE;
			for(int wid : qwords) {
				if(recWids.contains(wid)) {
					hasWid = Boolean.FALSE;
					break;
				}
			}
			if(!hasWid)	continue;
			
			numQuery++;
			for(int wid : qwords) {
				if(widHasDate.contains(wid) && !recWids.contains(wid)) {
					recWids.add(wid);
					startTime = System.currentTimeMillis();
					bs = wIdPnSer.getPlaceNeighborhoodBin(wid, qp);
					if(null != bs) {
						qp.rr.TimePNIORead += System.currentTimeMillis() - startTime;
						new WordRadiusNeighborhood(qp, bs);
						qp.rr.NumBytePNRead += bs.length;
					}
				}
			}
		}
		
		br.close();
		wIdPnSer.closeIndexReader();
		
		MLog.log("NumQuery: " + numQuery);
		MLog.log("TimePNIORead: " + (qp.rr.TimePNIORead / numQuery));
		MLog.log("NumPNPidDatePair: " + (qp.rr.NumPNPidDatePair / numQuery));
		MLog.log("NumBytePNRead: " + (qp.rr.NumBytePNRead / numQuery));
		
		MLog.log("over");
	}
	
	
	public static void main(String[] args) throws Exception {
		speedIOPidDatePiar(args);
	}
}
