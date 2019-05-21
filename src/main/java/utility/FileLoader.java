package utility;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

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
}
