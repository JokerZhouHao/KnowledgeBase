package zhou.hao.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

/**
 * 
 * @author Monica
 *
 * 从./data/dataInfo.txt读取有关项目的文件信息
 * 2017/11/02
 */
public class LocalFileInfo {
	private static HashMap<String, String> fileInfoMap = new HashMap<>();
	
	public static int gFreeBaseNodeNum = 194505961;
	
	private static String basePath = null;
	
	static {
		LocalFileInfo.fileInfoMap = LocalFileInfo.getFileInfoMap();
	}
	
	/**
	 * 
	 * @param degreeNum
	 * @param nodeNum
	 * @return 索引路径
	 */
	public static String getIndexPath(int nodeNum) {
		String str = "";
		if(0==nodeNum || LocalFileInfo.gFreeBaseNodeNum==nodeNum) str += "Original";
		else	str += String.valueOf(nodeNum) + "Node";
		return LocalFileInfo.getBasePath() + "data" + File.separator + "index" + File.separator + str;
	}
	
	/**
	 * 
	 * @param degreeNum
	 * @param nodeNum
	 * @return edgeBlankZipPath路径
	 */
	public static String getEdgeBlankZipPath(int degreeNum, int nodeNum) {
		String str = "";
		if(0!=degreeNum)	str += String.valueOf(degreeNum) + "Degree";
		if(0==nodeNum) str += "Original";
		else	str += String.valueOf(nodeNum) + "Node";
		str += "Edge.zip";
		return LocalFileInfo.getBasePath() + "data" + File.separator + "edge" + File.separator + str;
	}
	
	public static String getMemoryAndTime() {
		return " " + MemoryInfo.getTotalFreeUsedAvailable() + " " + TimeStr.getTime();
	}
	
	private static HashMap<String, String> getFileInfoMap(){
		
		// 获得基本目录
//		String infoPath = new File(LocalFileInfo.class.getResource("/").getPath()).toString();
//		System.out.println("infoPath : " + infoPath);
//		fileInfoMap.put("basePath", infoPath.substring(0, infoPath.indexOf("KnowledgeBase") + 13) + File.separator);
		try {
			FileReader fr = new FileReader(getBasePath() + "data" + File.separator + "localFileInfo.txt");
			BufferedReader br = new BufferedReader(fr);
			String str = null;
			while((str = br.readLine())!=null) {
				String strArr[] = str.split(";");
				fileInfoMap.put(strArr[0].trim(), strArr[1].trim());
			}
			br.close();
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("> 文件不存在而退出！！！");
			System.exit(0);
			return null;
		}
		return fileInfoMap;
	}
	
	public static String get(String key) {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else return LocalFileInfo.fileInfoMap.get(key);
	}
	
	public static String getBasePath() {
		if(null==basePath) {
			basePath = LocalFileInfo.class.getResource("").getPath();
			basePath = basePath.substring(1, basePath.indexOf("KnowledgeBase") + 14).replace("/", File.separator);
			if(File.separator.equals("/")) basePath = File.separator + basePath;
		}
		return basePath;
	}
	
	public static String getGzipDataFilePath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + LocalFileInfo.fileInfoMap.get("gzipDataFilePath").replace("[0]", File.separator);
		}
	}
	
	public static String getKeywordIdMapGoogleFreebasePath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + LocalFileInfo.fileInfoMap.get("keywordIdMapGoogleFreebase").replace("[0]", File.separator);
		}
	}
	
	public static String getNodeIdMapGoogleFreebasePath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + LocalFileInfo.fileInfoMap.get("nodeIdMapGoogleFreebase").replace("[0]", File.separator);
		}
	}
	
	public static String getEdgeGoogleFreebasePath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + LocalFileInfo.fileInfoMap.get("edgeGoogleFreebase").replace("[0]", File.separator);
		}
	}
	
	public static String getResultZipGoogleFreebasePath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + LocalFileInfo.fileInfoMap.get("resultZipGoogleFreebase").replace("[0]", File.separator);
		}
	}
	
	public static String getTestGzipPath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + LocalFileInfo.fileInfoMap.get("testGzipPath").replace("[0]", File.separator);
		}
	}
	
	public static String getTestObjectStreamPath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + LocalFileInfo.fileInfoMap.get("testObjectStreamPath").replace("[0]", File.separator);
		}
	}
	
	public static String getTempPath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + LocalFileInfo.fileInfoMap.get("tempPath").replace("[0]", File.separator);
		}
	}
	
	public static String getNodeIdAndKeywordAndEdgeZipPath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + LocalFileInfo.fileInfoMap.get("nodeIdAndKeywordAndEdgeZip").replace("[0]", File.separator);
		}
	}
	
	public static String getKeywordBlankZipPath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + LocalFileInfo.fileInfoMap.get("keywordBlankZip").replace("[0]", File.separator);
		}
	}
	
	public static String getEdgeBlankZipPath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + LocalFileInfo.fileInfoMap.get("edgeBlankZip").replace("[0]", File.separator);
		}
	}
	
	public static String getIndexPath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + LocalFileInfo.fileInfoMap.get("index").replace("[0]", File.separator);
		}
	}
	
	public static String getTestIndexPath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + LocalFileInfo.fileInfoMap.get("testIndex").replace("[0]", File.separator);
		}
	}
	
	public static String getSampleIndexPath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + LocalFileInfo.fileInfoMap.get("sampleIndex").replace("[0]", File.separator);
		}
	}
	
	public static String getDataSetPath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + "data" + File.separator + "DataSet" + File.separator;
		}
	}
	
	public static String getYagoZipIndexBasePath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getBasePath() + File.separator + "data" + File.separator + "DataSet" + File.separator + "index" + File.separator + "yagoVB" + File.separator;
		}
	}
	
	public static String getTFLableBasePath() {
		if(null==LocalFileInfo.fileInfoMap)	return null;
		else {
			return LocalFileInfo.getDataSetPath() + "TFLabel" + File.separator ;
		}
	}
	
	public static void main(String[] args) {
//		System.out.println(LocalFileInfo.get("in"));
//		String basePathStr = LocalFileInfo.class.getResource("").getPath();
//		basePathStr = basePathStr.substring(0, basePathStr.indexOf("KnowledgeBase") + 14);
		System.out.println(LocalFileInfo.getBasePath());
	}
}
