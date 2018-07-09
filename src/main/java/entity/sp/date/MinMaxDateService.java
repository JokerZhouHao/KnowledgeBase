package entity.sp.date;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utility.Global;
import utility.IOUtility;
import utility.TimeUtility;

/**
 * 所有节点时间范围查询服务
 * @author Monica
 * @since 2018/7/4
 */
public class MinMaxDateService {
	private List<NidMDate> minList = null;
	private List<NidMDate> maxList = null;
	
	public MinMaxDateService(String path) throws Exception{
		init(path);
	}
	
	/**
	 * 读取文件初始化
	 * @param path
	 * @throws Exception
	 */
	private void init(String path) throws Exception{
		DataInputStream dis = IOUtility.getDis(path);
		
		// 读最小的dates
		System.out.println("> 开始读取minDatesList . . . ");
		minList = new ArrayList<>();
		int num = dis.readInt();
		for(int i=0; i<num; i++) {
			minList.add(new NidMDate(dis.readInt(), dis.readInt()));
		}
		
		// 读最大的dates
		System.out.println("> 开始读取maxDatesList . . . ");
		maxList = new ArrayList<>();
		num = dis.readInt();
		for(int i=0; i<num; i++) {
			maxList.add(new NidMDate(dis.readInt(), dis.readInt()));
		}
	}
	
	/**
	 * 写所有节点的最小最大时间
	 * @param souPath
	 * @param desPath
	 * @throws Exception
	 */
	public static void writeAllMinMax(String souPath, String desPath) throws Exception{
		long startTime = System.currentTimeMillis();
		System.out.println("> 开始写文件" + desPath + " . . . ");
		
		ArrayList<MinMaxDate> datas = new ArrayList<>();
		
		// 读文件
		BufferedReader br = IOUtility.getBR(souPath);
		br.readLine();
		String line = null;
		int nid, sDate, eDate;
		String strArr[] = null;
		while(null != (line = br.readLine())) {
			strArr = line.split(Global.delimiterLevel1);
			nid = Integer.parseInt(strArr[0]);
			strArr = strArr[1].split(Global.delimiterDate);
			sDate = Integer.parseInt(strArr[0]);
			eDate = Integer.parseInt(strArr[strArr.length-2]);
			datas.add(new MinMaxDate(nid, sDate, eDate));
		}
		br.close();
		
		// 写文件
		DataOutputStream dos = IOUtility.getDos(desPath);
		// 写按最小时间由小到大排列的列表
		datas.sort(MinMaxDate.MIN_ENDPOINT_ORDER);
		dos.writeInt(datas.size());
		for(MinMaxDate mmd : datas) {
			dos.writeInt(mmd.getNid());
			dos.writeInt(mmd.min());
		}
		
		// 写按最大时间由小到大排列的列表
		datas.sort(MinMaxDate.MAX_ENDPOINT_ORDER);
		dos.writeInt(datas.size());
		for(MinMaxDate mmd : datas) {
			dos.writeInt(mmd.getNid());
			dos.writeInt(mmd.max());
		}
		dos.close();
		
		System.out.println("> 完成写文件" + desPath + "，用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()));
	}
	
	public Set<Integer> search(int min, int max){
		Set<Integer> res = new HashSet<>();
		
		// 遍历最少的dates
		int index = Collections.binarySearch(minList, new NidMDate(0, min), NidMDate.MDATE_ENDPOINT_ORDER);
		if(index<0) {
			index = -index-1;
		} else {
			while((--index)>=0) {
				if(minList.get(index).mDate()!=min) {
					break;
				}
			}
			index++;
		}
		for(; index<minList.size(); index++) {
			if(minList.get(index).mDate() <= max)
				res.add(minList.get(index).getNid());
			else break;
		}
		
		// 遍历最大的dates
		index = Collections.binarySearch(maxList, new NidMDate(0, max), NidMDate.MDATE_ENDPOINT_ORDER);
		if(index<0) {
			index = -index-2;
		} else {
			while((++index)<maxList.size()) {
				if(maxList.get(index).mDate()!=max) {
					break;
				}
			}
			index--;
		}
		for(; index>=0; index--) {
			if(maxList.get(index).mDate() >= min)
				res.add(maxList.get(index).getNid());
			else break;
		}
		
		return res;
	}
	
	public static void main(String[] args) throws Exception{
		String souPath = Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile;
//		String souPath = Global.inputDirectoryPath + "test.txt";
		String desPath = Global.outputDirectoryPath + Global.minMaxDatesFile;
//		MinMaxDateService.writeAllMinMax(souPath, desPath);
		
		MinMaxDateService ser = new MinMaxDateService(desPath);
//		
		Set<Integer> se = ser.search(-55516, -55516);
//		se = null;
	}
}
