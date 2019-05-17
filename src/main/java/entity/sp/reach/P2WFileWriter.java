package entity.sp.reach;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

import utility.Global;
import utility.IOUtility;

/**
 * 将pid可达的wid写到对应文件中
 * @author ZhouHao
 * @since 2019年5月13日
 */
public class P2WFileWriter {
	public final static int zipContianNodeNum = P2WReach.zipContianNodeNum;
	public final static int zipNum = P2WReach.zipNum;
	
	private DataOutputStream[] doss = null;
	
	public P2WFileWriter() throws Exception{
		init();
	}
	
	private void init() throws Exception{
		doss = new DataOutputStream[zipNum];
		int start = 0, end = 0;
		int span = zipContianNodeNum;
		int index = -1;
		String filePath = null;
		while(end < Global.numPid) {
			index++;
			start = end;
			end += span;
			if(end > Global.numPid)	end = Global.numPid;
			filePath = Global.recPidWidReachPath + "." + String.valueOf(start) + "." + String.valueOf(end);
			doss[index] = IOUtility.getDos(filePath);
		}
	}
	
	public void write(TempClass tc) throws Exception {
		DataOutputStream dos = doss[tc.pid/zipContianNodeNum];
		dos.writeInt(tc.pid);
		dos.writeInt(tc.widDis.size());
		for(Entry<Integer, Short> en : tc.widDis.entrySet()) {
			dos.writeInt(en.getKey());
			dos.writeShort(en.getValue());
		}
	}
	
	public void close() throws Exception{
		for(DataOutputStream dos : doss) {
			if(null != dos)	dos.close();
		}
	}
	
	public static void main(String[] args) throws Exception{
		P2WFileWriter writer = new P2WFileWriter();
		writer.close();
	}
}
