package zhou.hao.yago2s.service;

import java.util.ArrayList;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate;

import zhou.hao.service.ZipBase64ReaderService;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.TimeStr;

/**
 * 
 * @author Monica
 * @since 2018/3/8
 * 功能 : 构造yago2s图
 */
public class BuildMapService {
	private ArrayList<Integer>[] yago2sArrMap = new ArrayList[Yago2sInfoService.nodeNum];
//	private ArrayList<Integer>[] yago2sArrMap = new ArrayList[4];
	private String zipPath = null;
	private String entryName = null;
	
	public BuildMapService(String zipPath, String entryName) {
		this.zipPath = zipPath;
		this.entryName = entryName;
	}
	
	// 构造Map
	public ArrayList<Integer>[] buildMap(){
		Long startTime = System.currentTimeMillis();
		System.out.println("> 开始构造Yago2sMap(" + yago2sArrMap.length + "个节点) . . .  " + TimeStr.getTime());
		ZipBase64ReaderService ser = new ZipBase64ReaderService(zipPath, entryName);
		ser.readLine();
		String lineStr = null;
		int i, j, k, nodeId;
		String[] tempStrArr = null;
		ArrayList<Integer> tempList = null;
		while(null != (lineStr = ser.readLine())) {
			k = lineStr.indexOf(':');
			nodeId = Integer.parseInt(lineStr.substring(0, k));
			k += 2;
			tempStrArr = lineStr.substring(k, lineStr.length()).split(",");
			if(null == yago2sArrMap[nodeId])
				yago2sArrMap[nodeId] = tempList = new ArrayList<>();
			for(String st : tempStrArr)
				tempList.add(Integer.parseInt(st));
		}
		ser.close();
		System.out.println("> 结束构造Yago2sMap(" + yago2sArrMap.length + "个节点), 花时" + TimeStr.getSpendTimeStr(startTime, System.currentTimeMillis()) + "  ！ ！！"+ TimeStr.getTime());
		return yago2sArrMap;
	}
	
	// 查找节点的边
	public ArrayList<Integer> findEdges(int nodeId){
		return this.yago2sArrMap[nodeId];
	}
	
	// 主函数
	public static void main(String[] args) {
//		BuildMapService ser = new BuildMapService(LocalFileInfo.getDataSetPath() + "test.zip", "test");
		BuildMapService ser = new BuildMapService(LocalFileInfo.getDataSetPath() + "YagoVB.zip", "edgeYagoVB.txt");
		ser.buildMap();
		for(int i : ser.findEdges(8)) {
			System.out.print(i + " ");
		}
		System.out.println();
	}
}





















