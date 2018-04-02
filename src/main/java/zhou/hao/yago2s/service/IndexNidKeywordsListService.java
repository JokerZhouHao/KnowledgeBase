package zhou.hao.yago2s.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;
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
	
	// 打开索引写器
	private void openIndexWriter() {
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
	
	// 创建nodeIdKeywordListOnIntDateMapYagoVB.txt的索引
	public void createNIDKeyListDateIndex(String sourcePath, String entryName) throws Exception{
		this.sourcePath = sourcePath;
		this.entryName = entryName;
		
		System.out.println("-开始建立" + sourcePath + "里的文件" + entryName + "的索引   " + TimeStr.getTime());
//		ZipBase64ReaderService reader = new ZipBase64ReaderService(sourcePath, entryName);
		BufferedReader reader = new BufferedReader(new FileReader(sourcePath));
		System.out.println(reader.readLine());
		
		this.openIndexWriter();
		String lineStr = null;
		int i, nodeId;
		String[] strArr = null;
		Document doc = null;
		while(null != (lineStr = reader.readLine())) {
			doc = new Document();
			i = lineStr.indexOf(':');
			nodeId = Integer.parseInt(lineStr.substring(0, i));
			doc.add(new StoredField("nodeId", nodeId));
			doc.add(new StoredField("dateWId", lineStr.substring(i+2)));
			strArr = lineStr.substring(lineStr.lastIndexOf('#') + 1).split(",");
			for(String s : strArr) {
				doc.add(new IntPoint("keywordIdList", Integer.parseInt(s)));
			}
			indexWriter.addDocument(doc);
		}
		this.closeIndexWriter();
		reader.close();
		System.out.println("-over建立" + sourcePath + "里的文件" + entryName + "的索引   " + TimeStr.getTime());
	}
	
	// 通过nodeIdKeywordListOnIntDateMapYagoVB.txt的索引检索文件
	public HashMap<Integer, String> searchNIDKeyListDateIndex(ArrayList<Integer> seachedWIdList){
		HashMap<Integer, String> resultMap = null;
		Document doc = null;
		try {
//			TopDocs results = indexSearcher.search(new TermQuery(new Term("keywordList", searchedWordId)), Integer.MAX_VALUE);
			TopDocs results = indexSearcher.search(IntPoint.newSetQuery("keywordIdList", seachedWIdList), Integer.MAX_VALUE);
			ScoreDoc[] hits = results.scoreDocs;
			
			resultMap = new HashMap<>();
			for(int i=0; i<hits.length; i++) {
				doc = indexSearcher.doc(hits[i].doc);
				resultMap.put(Integer.parseInt(doc.get("nodeId")), doc.get("dateWId"));
			}
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("检索seachedWIdList：" + resultMap + "失败而退出！！！");
			System.exit(0);
		}
		return null;
	}
	
	// 创建wordIdOnIntDateYagoVB.txt的索引
	public void createWIDDateIndex(String sourcePath, String entryName) throws Exception{
		this.sourcePath = sourcePath;
		this.entryName = entryName;
		
		System.out.println("-开始建立" + sourcePath + "里的文件" + entryName + "的索引   " + TimeStr.getTime());
//		ZipBase64ReaderService reader = new ZipBase64ReaderService(sourcePath, entryName);
		BufferedReader reader = new BufferedReader(new FileReader(sourcePath));
		System.out.println(reader.readLine());
		
		this.openIndexWriter();
		String lineStr = null;
		int i;
		Document doc = null;
		while(null != (lineStr = reader.readLine())) {
			doc = new Document();
			i = lineStr.indexOf(':');
			doc.add(new IntPoint("wId", Integer.parseInt(lineStr.substring(0, i))));
			doc.add(new StoredField("date", lineStr.substring(i+2)));
			indexWriter.addDocument(doc);
		}
		this.closeIndexWriter();
		reader.close();
		System.out.println("-over建立" + sourcePath + "里的文件" + entryName + "的索引   " + TimeStr.getTime());
	}
		
	// 通过nodeIdKeywordListOnIntDateMapYagoVB.txt的索引检索文件
	public String searchWIDDateIndex(int seachedWId){
		try {
//			TopDocs results = indexSearcher.search(new TermQuery(new Term("keywordList", searchedWordId)), Integer.MAX_VALUE);
			TopDocs results = indexSearcher.search(IntPoint.newExactQuery("wId", seachedWId), 1);
			ScoreDoc[] hits = results.scoreDocs;
			return indexSearcher.doc(hits[0].doc).get("date");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("检索seachedWId：" + seachedWId + "失败而退出！！！");
			System.exit(0);
		}
		return null;
	}
	
	
	// 关闭索引写
	private void closeIndexWriter() {
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
	
	/* 
	 * 文件nodeIdKeywordListOnDateMapYagoVB.txt ： 4: 【2018-3-22#2018-3-23#2018-1-1#27,】转化为下面两个文件
	 * nodeIdKeywordListOnIntDateMapYagoVB.txt : 【4: 150#100#200#27】其中100对应的是天数
	 * wordIdOnIntDateYagoVB.txt : 【27: 100#150#200#】 按由小到大排列
	 */
	public static void convertNodeIdKeywordListOnDateMapTxt(String souPath, String nWIntDatPath, String wIntDatePath) throws Exception{
		BufferedReader sBr = new BufferedReader(new FileReader(new File(souPath)));
		BufferedWriter nWDBw = new BufferedWriter(new FileWriter(new File(nWIntDatPath)));
		BufferedWriter wDBw = new BufferedWriter(new FileWriter(new File(wIntDatePath)));
		TreeMap<Integer, TreeSet<Integer>> wDMap = new TreeMap<>();
		TreeSet<Integer> wDSet = null;
		String lineStr = null;
		// 减少算法的比较次数
		TreeSet<Integer> tempSet = null;
		lineStr = sBr.readLine();
		nWDBw.write(lineStr + '\n');
		wDBw.write(lineStr + '\n');
		wDBw.flush();
		wDBw.close();
		int i = 0, j = 0, k = 0, nodeId = 0;
		String[] sArr = null;
		String tempStr = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		TreeSet<Integer> dateSet = null;
		while(null != (lineStr = sBr.readLine())) {
			// 处理nodeId
			i = lineStr.indexOf(':');
			nodeId = Integer.parseInt(lineStr.substring(0, i));
			i += 2;
			nWDBw.write(lineStr.substring(0, i));
			
			// 处理dateStr
			j = lineStr.lastIndexOf('#');
			tempStr = lineStr.substring(i, j);
			sArr = tempStr.split("#");
			dateSet = new TreeSet<>();
			for(String str : sArr) {
				k = (int)(sdf.parse(str).getTime()/86400000);
				dateSet.add(k);
			}
			for(int in : dateSet) {
				nWDBw.write(String.valueOf(in));
				nWDBw.write('#');
			}
			
			// 处理nodeList
			j++;
			tempStr = lineStr.substring(j);
//			nWDBw.write(tempStr);
//			nWDBw.write('\n');
			sArr = tempStr.split(",");
			tempSet = new TreeSet<>();
			for(String str : sArr) {
				k = Integer.parseInt(str);
				tempSet.add(k);
				if(null == (wDSet = wDMap.get(k))) {
					wDSet = new TreeSet<>();
					wDMap.put(k, wDSet);
				}
				wDSet.addAll(dateSet);
			}
			for(int in : tempSet) {
				nWDBw.write(String.valueOf(in));
				nWDBw.write(',');
			}
			nWDBw.write('\n');
		}
		nWDBw.flush();
		nWDBw.close();
		sBr.close();
		
		// 写文件wordIdOnIntDateYagoVB.txt
		wDBw = new BufferedWriter(new FileWriter(new File(wIntDatePath), true));
		for(Entry<Integer, TreeSet<Integer>> en : wDMap.entrySet()) {
			wDBw.write(String.valueOf(en.getKey()));
			wDBw.write(": ");
			for(Integer in : en.getValue()) {
				wDBw.write(String.valueOf(in));
				wDBw.write(',');
			}
			wDBw.write('\n');
		}
		wDBw.flush();
		wDBw.close();
	}
	
	
	
	
	public static void main(String args[]) throws Exception{
		String filePath = LocalFileInfo.getDataSetPath() + "test/";
		String indexPath = LocalFileInfo.getDataSetPath() + "testIndex/";
		
		// 转化
//		System.out.println("> start convert . . . ");
//		IndexNidKeywordsListService.convertNodeIdKeywordListOnDateMapTxt(filePath + "nodeIdKeywordListOnDateMapYagoVB.txt", 
//				filePath + "nodeIdKeywordListOnIntDateMapYagoVB.txt", 
//				filePath + "wordIdOnIntDateYagoVB.txt");
//		System.out.println("> end convert . . . ");
		
		// 建立nodeIdKeywordListOnIntDateMapYagoVB.txt的索引
//		IndexNidKeywordsListService iSer = new IndexNidKeywordsListService(indexPath + "nid_dateWid_wid");
//		iSer.createNIDKeyListDateIndex(filePath + "nodeIdKeywordListOnIntDateMapYagoVB.txt", null);
//		iSer.openIndexReader();
//		ArrayList<Integer> wIdList = new ArrayList<>();
//		wIdList.add(12);
//		wIdList.add(17);
//		wIdList.add(19);
//		wIdList.add(23);
//		HashMap<Integer, String> map = iSer.searchNIDKeyListDateIndex(wIdList);
//		for(Entry<Integer, String> en : map.entrySet()) {
//			System.out.println(en.getKey() + ": " + en.getValue());
//		}
//		iSer.closeIndexReader();
		
//		 建立nodeIdKeywordListOnIntDateMapYagoVB.txt的索引
		IndexNidKeywordsListService iSer = new IndexNidKeywordsListService(indexPath + "wid_date");
		iSer.createWIDDateIndex(filePath + "wordIdOnIntDateYagoVB.txt", null);
		iSer.openIndexReader();
		System.out.println(iSer.searchWIDDateIndex(27));
		iSer.closeIndexReader();
		
		
		
		
		
//		System.out.println(LocalFileInfo.getYagoZipIndexBasePath() + "NidKeywordsListMapDBpediaVBTxt");
//		IndexNidKeywordsListService ser = new IndexNidKeywordsListService(LocalFileInfo.getYagoZipIndexBasePath() + "NidKeywordsListMapDBpediaVBTxt");
//		ser.createIndex(LocalFileInfo.getDataSetPath() + "YagoVB.zip",  "nodeIdKeywordListOnDateMapYagoVB.txt");
//		IndexNidKeywordsListService ser = new IndexNidKeywordsListService(LocalFileInfo.getDataSetPath() + "testIndex");
//		ser.createIndex(LocalFileInfo.getDataSetPath() + "test.zip",  "nodeIdKeywordListOnDateMapYagoVB.txt");
//		ser.openIndexReader();
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		for(NodeIdDate nid : ser.searchKeywordIdReNodeIdDate(16)) {
//			System.out.print(nid.nodeId + " > ");
//			for(Date da : nid.dateList) {
//				System.out.print(sdf.format(da) + "   ");
//			}
//			System.out.println();
//		}
//		ArrayList<Integer> wordList = new ArrayList<>();
//		wordList.add(12);
//		wordList.add(16);
//		wordList.add(19);
//		int i = 0;
//		HashMap<Integer, KeywordIdDateList> resMap = ser.searchKeywordIdListReNodeIdMap(wordList);
//		for(Entry<Integer, KeywordIdDateList> en : resMap.entrySet()) {
//			System.out.print(en.getKey() + " : ");
//			for(Date da : en.getValue().dateList)
//				System.out.print(sdf.format(da) + " ");
//			System.out.print(" : ");
//			for(Integer in : en.getValue().keywordIdList)
//				System.out.print(in + " ");
//			System.out.println();
//		}
//		for(int i : ser.searchKeywordIdReNodeIds(2))
//			System.out.println(i);
//		ser.closeIndexReader();
	}
}
