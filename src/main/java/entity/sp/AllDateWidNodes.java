package entity.sp;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utility.Global;
import utility.IOUtility;

/**
 * 记录所有带有date和wid的节点
 * @author Monica
 *
 */
public class AllDateWidNodes {
	public static class DWid{
		public int[] dates = null;
		public int[] wids = null;
	}
	
	private Map<Integer, DWid> allDWNode = new HashMap<>();
	
	/**
	 * 从文件读取
	 * @param fp
	 * @return
	 */
	public static Map<Integer, DWid> loadFromFile(String fp){
		System.out.println("> 读取文件" + fp + " . . . ");
		Map<Integer, DWid> map = new HashMap<>();
		try {
			BufferedReader br = IOUtility.getBR(fp);
			String line = null;
			br.readLine();
			
			int index1, index2, i, nid;
			String tArr[] = null;
			
			DWid tDW = null;
			
			while(null != (line = br.readLine())) {
				index1 = line.indexOf(Global.delimiterLevel1);
				nid = Integer.parseInt(line.substring(0, index1));
				index1 += Global.delimiterLevel1.length();
				
				index2 = line.lastIndexOf(Global.delimiterDate);
				tArr = line.substring(index1, index2).split(Global.delimiterDate);
				tDW = new DWid();
				map.put(nid, tDW);
				tDW.dates = new int[tArr.length];
				for(i=0; i<tArr.length; i++) {
					tDW.dates[i] = Integer.parseInt(tArr[i]);
				}
				
				index2 += Global.delimiterDate.length();
				tArr = line.substring(index2).split(Global.delimiterLevel2);
				tDW.wids = new int[tArr.length];
				for(i=0; i<tArr.length; i++) {
					tDW.wids[i] = Integer.parseInt(tArr[i]);
				}
			}
			br.close();
		} catch (Exception e) {
			System.err.println("> 读取文件" + fp + "失败");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("> 成功读取文件" + fp + "。");
		return map;
	}
	
	public static void main(String[] args) {
		AllDateWidNodes.loadFromFile(Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile);
	}
}
