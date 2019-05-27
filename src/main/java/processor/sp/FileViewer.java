package processor.sp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;

import precomputation.sp.IndexNidKeywordsListService;
import utility.FileLoader;
import utility.Global;

/**
 * 用于查看对比文件
 * @author ZhouHao
 * @since 2019年5月18日
 */
public class FileViewer {
	
	/**
	 * 查看测试样本中词的词频
	 * @throws Exception
	 */
	public static void viewSampleWidFrequency() throws Exception {
		Global.wordFrequency = IndexNidKeywordsListService.loadWordFrequency(Global.outputDirectoryPath + Global.wordFrequencyFile);
		BufferedReader br = new BufferedReader(new FileReader(Global.inputDirectoryPath + Global.testSampleFile + "." + 
						String.valueOf(Global.testOrgSampleNum) + ".t=0.wn=10"));
		Map<Integer, String> wid2Word = FileLoader.loadWid2Word();
		String lineStr = null;
		String[] arr = null;
		int num = 1;
		int wid = 0;
		while(null != (lineStr = br.readLine())) {
			System.out.print(String.valueOf(num) + Global.delimiterLevel1);
			arr = lineStr.split(Global.delimiterLevel1)[1].split(Global.delimiterSpace);
			for(int i=2; i<=11; i++) {
				wid = Integer.parseInt(arr[i]);
				System.out.print("(" + Global.wordFrequency.get(wid) + ", " + wid2Word.get(wid) + ")"+ " ");
			}
			System.out.println();
			num++;
		}
		br.close();
	}
	
	
	public static void main(String[] args) throws Exception{
		viewSampleWidFrequency();
	}
	
}
