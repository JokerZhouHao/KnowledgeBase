package zhou.hao.yago2s.service;

import java.util.ArrayList;

import zhou.hao.service.ZipReaderService;
import zhou.hao.tools.LocalFileInfo;

public class LineDealService {
	private ArrayList<String> eleList = new ArrayList<>(3);
	
	public LineDealService() {
		for(int i=0; i<3; i++)
			eleList.add("");
	}
	
	public ArrayList<String> dealLine(String lineStr){
		int i, j;
		int len = lineStr.length();
		char c = 0;
		
		// 获取主语
		i = 0;
		while('>' != lineStr.charAt(i))  i++;
		eleList.set(0, lineStr.substring(0, ++i));
		
		// 获取谓语
		while(' ' == (c=lineStr.charAt(i)) || '\t' == c)	i++;
		j = i;
		while(' ' != (c=lineStr.charAt(i)) && '\t' != c)	i++;
		eleList.set(1, lineStr.substring(j, i));
		
		// 获取宾语
		i++;
		while(' ' == (c=lineStr.charAt(i)) || '\t' == c)	i++;
		j = i;
		eleList.set(2, lineStr.substring(j, len-2));
		
		return eleList;
	}
	
	public static void main(String args[]) {
//		System.out.println('\t'=='d');
		String filePath = LocalFileInfo.getDataSetPath() + "yago2选取.zip";
		ZipReaderService zrs = new ZipReaderService(filePath);
		LineDealService lds = new LineDealService();
		String str = null;
		while(null != (str = zrs.readLine())) {
			if(str.length()>0 && str.charAt(0)=='<') {
				System.out.println(str);
				System.out.println(lds.dealLine(str));
				System.out.println();
			}
		}
		zrs.close();
		
//		ArrayList<String> li = null;
//		
//		li = lds.dealLine("<A>	<linksTo>	<English_language> .");
//		for(String s : li)
//			System.out.println(s);
	}
}
