package utility;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * tokenizer工具
 * @author ZhouHao
 * @since 2019年4月3日
 */
public class TokenizerUtility {
	private Analyzer analyzer = new StandardAnalyzer();
	
	/**
	 * 对str分词
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public List<String> tokens(String str) throws Exception{
		List<String> li = new ArrayList<>();
		TokenStream ts = analyzer.tokenStream("k", str);
		CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
		ts.reset();
		while(ts.incrementToken()) {
			li.add(term.toString());
		}
		ts.end();
		ts.close();
		return li;
	}
	
	/**
	 * 获得文件中不同词的数量
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static int numTokens(String path) throws Exception{
		System.out.println(path);
		TokenizerUtility tu = new TokenizerUtility();
		HashSet<String> tokens = new HashSet<>();
		String[] ss = null;
		BufferedReader br = IOUtility.getBR(path);
		String line = null;
		while(null != (line = br.readLine())) {
			if(line.contains(Global.delimiterLevel1)) {
				line = line.split(Global.delimiterLevel1)[1].split(Global.delimiterPound)[1];
				ss = line.split(Global.delimiterLevel2);
				for(String st : ss) {
					if(!tokens.contains(st)) tokens.add(st);
				}
			}
		}
		System.out.println(tokens.size());
		return tokens.size();
	}
	
	public static void main(String[] args) throws Exception{
		String yagoPath = Global.inputDirectoryPath + "nodeIdKeywordListOnIntDateMapYagoVB.txt";
		TokenizerUtility.numTokens(yagoPath);
	}
}
