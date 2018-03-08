package zhou.hao.yago2s.service;

import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import zhou.hao.helper.PatternAnalyzer;
import zhou.hao.service.ZipBase64ReaderService;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.TimeStr;

/**
 * 
 * @author Monica
 * @since 2018/03/02
 * 功能  : 为yagoVB.zip里的NidKeywordsList建立索引
 */
public class IndexNidKeywordsListService {
	
	private String sourcePath = null;
	private String entryName = null;
	private String indexPath = null;
	private IndexWriter indexWriter = null;
	private Analyzer analyzer = null;
	private IndexReader indexReader = null;
	private IndexSearcher indexSearcher = null;
	
	public IndexNidKeywordsListService(String indexPath) {
		super();
		this.indexPath = indexPath;
	}
	
	// 创建索引
	public void createIndex(String sourcePath, String entryName) {
		this.sourcePath = sourcePath;
		this.entryName = entryName;
		
		System.out.println("-开始建立" + sourcePath + "里的文件" + entryName + "的索引   " + TimeStr.getTime());
		ZipBase64ReaderService reader = new ZipBase64ReaderService(sourcePath, entryName);
		System.out.println(reader.readLine());
		
		this.openIndexWriter();
		String lineStr = null;
		while(null != (lineStr = reader.readLine())) {
//			System.out.println(lineStr.substring(0, lineStr.indexOf(':')));
//			System.out.println(lineStr.substring(lineStr.indexOf(' ')+1, lineStr.length()));
			this.addDoc(Integer.parseInt(lineStr.substring(0, lineStr.indexOf(':'))), lineStr.substring(lineStr.indexOf(' ')+1, lineStr.length()));
		}
		this.closeIndexWriter();
		reader.close();
		System.out.println("-over建立" + sourcePath + "里的文件" + entryName + "的索引   " + TimeStr.getTime());
	}
	
	// 打开索引写器
	public void openIndexWriter() {
		try {
			analyzer = new PatternAnalyzer(",");
			Directory indexDir = FSDirectory.open(Paths.get(indexPath));
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			indexWriter = new IndexWriter(indexDir, iwc);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("打开IndexWriter失败而退出！！！");
			System.exit(0);
		}
	}
	
	// 添加document
	public void addDoc(int nodeId, String keyListStr) {
		String[] wordIdArr = keyListStr.split(",");
		Document doc = null;
		for(String st : wordIdArr) {
			doc = new Document();
			doc.add(new StoredField("nodeId", nodeId));
			doc.add(new IntPoint("keywordIdList", Integer.parseInt(st)));
			try {
				indexWriter.addDocument(doc);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("添加索引失败而退出！！！");
				System.exit(0);
			}
		}
		
	}
	
	// 关闭索引写
	public void closeIndexWriter() {
		try {
			if(null!=indexWriter) indexWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 打开索引搜索器
	public void openIndexReader() {
		try {
			indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
			indexSearcher = new IndexSearcher(indexReader);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("打开IndexReader失败而退出！！！");
			System.exit(0);
		}
	}
	
	// 检索关键字id，获得对应点
	public ArrayList<Integer> searchKeywordIdReNodeIds(String searchedWordId){
		ArrayList<Integer> resultList = null;
		try {
//			TopDocs results = indexSearcher.search(new TermQuery(new Term("keywordList", searchedWordId)), Integer.MAX_VALUE);
			TopDocs results = indexSearcher.search(IntPoint.newExactQuery("keywordIdList", Integer.parseInt(searchedWordId)), Integer.MAX_VALUE);
			ScoreDoc[] hits = results.scoreDocs;
			
			resultList = new ArrayList<Integer>();
			
			for(int i=0; i<hits.length; i++) {
				resultList.add(Integer.parseInt(indexSearcher.doc(hits[i].doc).get("nodeId")));
			}
			System.out.println("     【" + searchedWordId + "】  > " + "命中的点数 = " + resultList.size() + " " + TimeStr.getTime());
			return resultList;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("检索KeywordIdReNodeIds：" + searchedWordId + "失败而退出！！！");
			System.exit(0);
		}
		return null;
	}
	
	// 关闭索引读流
	public void closeIndexReader() {
		if(indexReader!=null) {
			try {
				indexReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String args[]) {
//		System.out.println(LocalFileInfo.getYagoZipIndexBasePath() + "NidKeywordsListMapDBpediaVBTxt");
//		IndexNidKeywordsListService ser = new IndexNidKeywordsListService(LocalFileInfo.getYagoZipIndexBasePath() + "NidKeywordsListMapDBpediaVBTxt");
//		ser.createIndex(LocalFileInfo.getDataSetPath() + "YagoVB.zip",  "nidKeywordsListMapYagoVB.txt");
		IndexNidKeywordsListService ser = new IndexNidKeywordsListService(LocalFileInfo.getYagoZipIndexBasePath() + "test.index");
		ser.createIndex(LocalFileInfo.getDataSetPath() + "test.zip",  "test");
		ser.openIndexReader();
		for(int i : ser.searchKeywordIdReNodeIds("2"))
			System.out.println(i);
		ser.closeIndexReader();
	}
}
