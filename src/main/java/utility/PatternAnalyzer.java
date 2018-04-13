package utility;

import java.io.Reader;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.pattern.PatternTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * 
 * @author Monica
 * @since 2018/03/02
 * 功能 : 自定义正则分词器
 */
public class PatternAnalyzer extends Analyzer {
	
	private String regex;
	
	public PatternAnalyzer(String regex) {
		super();
		this.regex = regex;
	}

	@Override
	protected TokenStreamComponents createComponents(String arg0) {
		return new TokenStreamComponents(new PatternTokenizer(Pattern.compile(regex), -1));
	}
	
	public static void main(String[] args) throws Exception{
		PatternAnalyzer pa = new PatternAnalyzer(",");
		TokenStream ts = pa.tokenStream("k", "17531#   12,");
		CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
		ts.reset();
		while(ts.incrementToken()) {
			System.out.println(term);
		}
		ts.end();
		ts.close();
	}
}
