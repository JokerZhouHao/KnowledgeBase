package processor.sp;

import java.util.ArrayList;

import file.reader.ZipBase64Reader;
import utility.LocalFileInfo;

/**
 * 
 * @author Monica
 * @since 2018/3/9
 * 功能 : 构造yago2s的NidKeywordListMap
 */
public class BuildNidKeywordListMapService {
	private int nodeNum = Yago2sInfoService.nodeNum;
	private ArrayList<Integer>[] nidKeywordsArr = null;
	private String zipPath = LocalFileInfo.getDataSetPath() + "yagoVB.zip";
	private String entryName = "nidKeywordsListMapYagoVB";
	
	public BuildNidKeywordListMapService() {
		
	}

	public BuildNidKeywordListMapService(int nodeNum) {
		super();
		this.nodeNum = nodeNum;
	}

	public BuildNidKeywordListMapService(String zipPath, String entryName, int nodeNum) {
		super();
		this.nodeNum = nodeNum;
		this.zipPath = zipPath;
		this.entryName = entryName;
	}
	
	// 构建NidKeywordListMap
	public ArrayList<Integer>[] buildNidKeywordListMapArr(){
		System.out.println("> 开始构造NidKeywordListMapArr . . . ");
		nidKeywordsArr = new ArrayList[nodeNum];
		ZipBase64Reader reader = new ZipBase64Reader(zipPath, entryName);
		reader.readLine();
		ArrayList<Integer> tempList = null;
		int tempI = 0;
		String[] tempArr = null;
		String lineStr = null;
		int k;
		for(int i=0; i<nodeNum; i++) {
			lineStr = reader.readLine();
			if(null==lineStr)	break;
			k = lineStr.indexOf(':');
			tempI = Integer.parseInt(lineStr.substring(0, k));
			k += 2;
			tempArr = lineStr.substring(k, lineStr.length()).split(",");
			if(null == (tempList = nidKeywordsArr[tempI])) {
				nidKeywordsArr[tempI] = tempList = new ArrayList<>();
			}
			for(String st : tempArr)	tempList.add(Integer.parseInt(st));
		}
		reader.close();
		System.out.println("> over构造NidKeywordListMapArr ! ! ! ");
		return nidKeywordsArr;
	}

	public ArrayList<Integer>[] getNidKeywordsArr() {
		if(null==nidKeywordsArr)	return this.buildNidKeywordListMapArr();
		return nidKeywordsArr;
	}
	
	// 主函数
	public static void main(String[] args) {
		int i = 0;
		BuildNidKeywordListMapService ser = new BuildNidKeywordListMapService();
		ArrayList<Integer>[] arr = ser.getNidKeywordsArr();
		System.out.println(arr);
		for(i=0; i<10; i++) {
			System.out.print(i + " > " + arr[i]);
			System.out.println();
		}
	}
}

















