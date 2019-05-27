package utility;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import entity.freebase.Pair;

/**
 * 加载文件
 * @author ZhouHao
 * @since 2019年5月18日
 */
public class FileLoader {
	/**
	 * 读取keywordIdMapYagoVB.txt
	 * @return
	 * @throws Exception
	 */
	public static Map<Integer, String> loadWid2Word() throws Exception {
		BufferedReader br = IOUtility.getBR(Global.inputDirectoryPath + "keywordIdMapYagoVB.txt");
		String line = null;
		String[] arr = null;
		Map<Integer, String> wid2Word = new HashMap<>();
		while(null != (line = br.readLine())) {
			if(line.contains("#"))	continue;
			arr = line.split(Global.delimiterLevel1);
			wid2Word.put(Integer.parseInt(arr[0]), arr[1]);
		}
		br.close();
		return wid2Word;
	}
	
	/**
	 * 加载坐标文件
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static Map<Integer, Pair<Double, Double>> loadCoord(String path) throws Exception {
		BufferedReader br = IOUtility.getBR(path);
		String line = null;
		Map<Integer, Pair<Double, Double>> id2Coord = new TreeMap<>();
		while(null != (line = br.readLine())) {
			if(line.endsWith(Global.delimiterPound))	continue;
			String[] arr = line.split(Global.delimiterLevel1);
			int id = Integer.parseInt(arr[0]);
			arr = arr[1].split(Global.delimiterSpace);
			Pair<Double, Double> pair = new Pair<>(Double.parseDouble(arr[0]), Double.parseDouble(arr[1]));
			id2Coord.put(id, pair);
		}
		return id2Coord; 
	}
	
}
