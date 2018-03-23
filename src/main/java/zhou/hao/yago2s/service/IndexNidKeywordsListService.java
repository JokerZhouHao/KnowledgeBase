package zhou.hao.yago2s.service;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

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

import zhou.hao.helper.MComparator;
import zhou.hao.helper.PatternAnalyzer;
import zhou.hao.service.ZipBase64ReaderService;
import zhou.hao.tools.LocalFileInfo;
import zhou.hao.tools.TimeStr;
import zhou.hao.yago2s.processor.BSPProcessor;
import zhou.hao.yago2s.processor.BSPProcessor.DateSpanNodeRec;
import zhou.hao.yago2s.service.IndexNidKeywordsListService.KeywordIdDateList;

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
//			System.out.println(lineStr);
//			System.out.println(Integer.parseInt(lineStr.substring(0, lineStr.indexOf(':'))));
//			System.out.println(lineStr.substring(lineStr.indexOf(' ')+1, lineStr.lastIndexOf('#')));
//			System.out.println(lineStr.substring(lineStr.lastIndexOf('#')+1));
//			System.out.println();
			this.addDoc(Integer.parseInt(lineStr.substring(0, lineStr.indexOf(':'))), 
					lineStr.substring(lineStr.indexOf(' ')+1, lineStr.lastIndexOf('#')),
					lineStr.substring(lineStr.lastIndexOf('#')+1));
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
	public void addDoc(int nodeId, String dateStr, String keyListStr) {
		String[] wordIdArr = keyListStr.split(",");
		Document doc = null;
		doc = new Document();
		doc.add(new StoredField("nodeId", nodeId));
		doc.add(new StoredField("date", dateStr));
		for(String st : wordIdArr) {
			doc.add(new IntPoint("keywordIdList", Integer.parseInt(st)));
		}
		try {
			indexWriter.addDocument(doc);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("添加索引失败而退出！！！");
			System.exit(0);
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
	public ArrayList<Integer> searchKeywordIdReNodeIds(Integer searchedWordId){
		ArrayList<Integer> resultList = null;
		try {
//			TopDocs results = indexSearcher.search(new TermQuery(new Term("keywordList", searchedWordId)), Integer.MAX_VALUE);
			TopDocs results = indexSearcher.search(IntPoint.newExactQuery("keywordIdList", searchedWordId), Integer.MAX_VALUE);
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
	
	// 记录节点id和date
	public class NodeIdDate{
		private int nodeId = -1;
		private ArrayList<Date> dateList = new ArrayList<>();
		public int getNodeId() {
			return nodeId;
		}
	}
	
	// 检索关键字id，获得对应点和date
	public ArrayList<NodeIdDate> searchKeywordIdReNodeIdDate(Integer searchedWordId){
		ArrayList<NodeIdDate> resultList = null;
		try {
//				TopDocs results = indexSearcher.search(new TermQuery(new Term("keywordList", searchedWordId)), Integer.MAX_VALUE);
			TopDocs results = indexSearcher.search(IntPoint.newExactQuery("keywordIdList", searchedWordId), Integer.MAX_VALUE);
			ScoreDoc[] hits = results.scoreDocs;
			
			resultList = new ArrayList<NodeIdDate>();
			
			NodeIdDate tempNID = null;
			String dateStr = null;
			String[] strArr = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			for(int i=0; i<hits.length; i++) {
				tempNID = new NodeIdDate();
				tempNID.nodeId = Integer.parseInt(indexSearcher.doc(hits[i].doc).get("nodeId"));
				
				dateStr = indexSearcher.doc(hits[i].doc).get("date");
				strArr = dateStr.split("#");
				for(String st : strArr)
					tempNID.dateList.add(sdf.parse(st));
				resultList.add(tempNID);
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
	
	// 记录keywordList和date
	public class KeywordIdDateList{
		private ArrayList<Integer> keywordIdList = null;
		private ArrayList<Date> dateList = null;
		public ArrayList<Integer> getKeywordIdList() {
			return keywordIdList;
		}
		public ArrayList<Date> getDateList() {
			return dateList;
		}
	}
	
	// 搜索多个关键字
	public HashMap<Integer, KeywordIdDateList> searchKeywordIdListReNodeIdMap(ArrayList<Integer> searchedWordList){
		MComparator<NodeIdDate> mcompNId = new MComparator<NodeIdDate>();
		MComparator<Integer> mcompInt = new MComparator<Integer>();
		searchedWordList.sort(mcompInt);
		ArrayList<LinkedList<NodeIdDate>> resultList  = new ArrayList<>();
		ArrayList<NodeIdDate> tempList = null;
		
		// 添加搜索结果
		for(Integer in : searchedWordList) {
			tempList = this.searchKeywordIdReNodeIdDate(in);
			tempList.sort(mcompNId);
			resultList.add(new LinkedList<>(tempList));
		}
		
		// 构造结果Map
		HashMap<Integer, KeywordIdDateList> resultMap = new HashMap<>();
		
		int len = 0, minIn =  Integer.MAX_VALUE, i = 0;
		len = searchedWordList.size();
		LinkedList<NodeIdDate> tempLink = null;
		NodeIdDate tempNID = null;
		KeywordIdDateList tempKIDL = null;
		while(true) {
			minIn = Integer.MAX_VALUE;
			for(i=0; i<len; i++) {
				tempLink = resultList.get(i);
				if(!tempLink.isEmpty() && tempLink.getFirst().nodeId < minIn) {
					tempNID = tempLink.getFirst();
					minIn = tempNID.nodeId;
				}
			}
			if(minIn == Integer.MAX_VALUE)	break;
			
			tempKIDL = new KeywordIdDateList();
			tempKIDL.dateList = tempNID.dateList;
			
			tempList = new ArrayList<>();
			for(i=0; i<len; i++) {
				tempLink = resultList.get(i);
				if(!tempLink.isEmpty() && tempLink.getFirst().nodeId == minIn) {
					if(null == tempKIDL.keywordIdList)	tempKIDL.keywordIdList = new ArrayList<>();
					tempKIDL.keywordIdList.add(searchedWordList.get(i));
					tempLink.poll();
				}
			}
			resultMap.put(minIn, tempKIDL);
		}
		
		
		HashMap<Integer, DateSpanNodeRec> dateSpanMap = new HashMap<>();
		BSPProcessor prc = new BSPProcessor();
		for(Integer in : searchedWordList)	dateSpanMap.put(in, prc.new DateSpanNodeRec());
		int tempI1 = Integer.MAX_VALUE, tempI2 = 0;
		DateSpanNodeRec tempDSNR = null;
		for(Entry<Integer, KeywordIdDateList> en : resultMap.entrySet()) {
			tempI1 = Integer.MAX_VALUE;
			for(Date da : en.getValue().getDateList()) {
				if((tempI2 = TimeStr.calGapBetweenDate(TimeStr.getNowDate(), da)) < tempI1)	tempI1 = tempI2;
			}
			for(Integer in : en.getValue().getKeywordIdList()) {
				tempDSNR = dateSpanMap.get(in);
				if(tempDSNR.dateSpan > tempI1) {
					tempDSNR.dateSpan = tempI1;
					tempDSNR.nodeList.clear();
					tempDSNR.nodeList.add(en.getKey());
				} else if (tempDSNR.dateSpan == tempI1) {
					tempDSNR.nodeList.add(en.getKey());
				}
			}
		}
		
		
		return resultMap;
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
//		ser.createIndex(LocalFileInfo.getDataSetPath() + "YagoVB.zip",  "nodeIdKeywordListOnDateMapYagoVB.txt");
		IndexNidKeywordsListService ser = new IndexNidKeywordsListService(LocalFileInfo.getDataSetPath() + "testIndex");
		ser.createIndex(LocalFileInfo.getDataSetPath() + "test.zip",  "nodeIdKeywordListOnDateMapYagoVB.txt");
		ser.openIndexReader();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		for(NodeIdDate nid : ser.searchKeywordIdReNodeIdDate(16)) {
//			System.out.print(nid.nodeId + " > ");
//			for(Date da : nid.dateList) {
//				System.out.print(sdf.format(da) + "   ");
//			}
//			System.out.println();
//		}
		ArrayList<Integer> wordList = new ArrayList<>();
		wordList.add(12);
		wordList.add(16);
		wordList.add(19);
		int i = 0;
		HashMap<Integer, KeywordIdDateList> resMap = ser.searchKeywordIdListReNodeIdMap(wordList);
		for(Entry<Integer, KeywordIdDateList> en : resMap.entrySet()) {
			System.out.print(en.getKey() + " : ");
			for(Date da : en.getValue().dateList)
				System.out.print(sdf.format(da) + " ");
			System.out.print(" : ");
			for(Integer in : en.getValue().keywordIdList)
				System.out.print(in + " ");
			System.out.println();
		}
//		for(int i : ser.searchKeywordIdReNodeIds(2))
//			System.out.println(i);
		ser.closeIndexReader();
	}
}
