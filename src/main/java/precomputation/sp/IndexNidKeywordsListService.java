package precomputation.sp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.function.DoubleConsumer;
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

import utility.MComparator;
import utility.PatternAnalyzer;
import file.reader.ZipBase64Reader;
import utility.Global;
import utility.LocalFileInfo;
import utility.TimeUtility;
import main.sp.SPIncompletion;
import precomputation.sp.IndexNidKeywordsListService.KeywordIdDateList;

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
		
		System.out.println("-开始建立" + sourcePath + "里的文件" + entryName + "的索引   " + TimeUtility.getTime());
		ZipBase64Reader reader = new ZipBase64Reader(sourcePath, entryName);
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
		System.out.println("-over建立" + sourcePath + "里的文件" + entryName + "的索引   " + TimeUtility.getTime());
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
		
//		System.out.println("-开始建立" + sourcePath + "里的文件" + entryName + "的索引   " + TimeUtility.getTime());
//		ZipBase64ReaderService reader = new ZipBase64ReaderService(sourcePath, entryName);
		BufferedReader reader = new BufferedReader(new FileReader(sourcePath));
		System.out.println("> " + reader.readLine());
		
		this.openIndexWriter();
		String lineStr = null;
		int i, nodeId, tempI;
		String[] strArr = null;
		Document doc = null;
		while(null != (lineStr = reader.readLine())) {
			doc = new Document();
			i = lineStr.indexOf(Global.delimiterLevel1);
			nodeId = Integer.parseInt(lineStr.substring(0, i));
			doc.add(new StoredField("nid", nodeId));
//			doc.add(new StoredField("dateWId", lineStr.substring(i+2)));
			tempI = lineStr.lastIndexOf(Global.delimiterDate);
			doc.add(new StoredField("dates", lineStr.substring(i+2, tempI)));
			strArr = lineStr.substring(tempI + 1).split(Global.delimiterLevel2);
			for(String s : strArr) {
				doc.add(new IntPoint("wids", Integer.parseInt(s)));
			}
			indexWriter.addDocument(doc);
		}
		this.closeIndexWriter();
		reader.close();
//		System.out.println("-over建立" + sourcePath + "里的文件" + entryName + "的索引   " + TimeUtility.getTime());
	}
	
	// 通过nodeIdKeywordListOnIntDateMapYagoVB.txt的索引检索文件
	public HashMap<Integer, String> searchNIDKeyListDate(int wid){
		HashMap<Integer, String> resultMap = null;
		Document doc = null;
		try {
//				TopDocs results = indexSearcher.search(new TermQuery(new Term("keywordList", searchedWordId)), Integer.MAX_VALUE);
			TopDocs results = indexSearcher.search(IntPoint.newExactQuery("wids", wid), Integer.MAX_VALUE);
			ScoreDoc[] hits = results.scoreDocs;
			
			resultMap = new HashMap<>();
			for(int i=0; i<hits.length; i++) {
				doc = indexSearcher.doc(hits[i].doc);
				resultMap.put(Integer.parseInt(doc.get("nid")), doc.get("dates"));
			}
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("检索seachedWIdList：" + resultMap + "失败而退出！！！");
			System.exit(0);
		}
		return null;
	}
	
	// 通过nodeIdKeywordListOnIntDateMapYagoVB.txt的索引检索文件
	public HashMap<Integer, String> searchNIDKeyListDateIndex(ArrayList<Integer> seachedWIdList){
		HashMap<Integer, String> resultMap = null;
		Document doc = null;
		try {
//			TopDocs results = indexSearcher.search(new TermQuery(new Term("keywordList", searchedWordId)), Integer.MAX_VALUE);
			TopDocs results = indexSearcher.search(IntPoint.newSetQuery("wids", seachedWIdList), Integer.MAX_VALUE);
			ScoreDoc[] hits = results.scoreDocs;
			
			resultMap = new HashMap<>();
			for(int i=0; i<hits.length; i++) {
				doc = indexSearcher.doc(hits[i].doc);
				resultMap.put(Integer.parseInt(doc.get("nid")), doc.get("dates"));
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
		
//		System.out.println("-开始建立" + sourcePath + "里的文件" + entryName + "的索引   " + TimeUtility.getTime());
//		ZipBase64ReaderService reader = new ZipBase64ReaderService(sourcePath, entryName);
		BufferedReader reader = new BufferedReader(new FileReader(sourcePath));
		System.out.println(reader.readLine());
		
		this.openIndexWriter();
		String lineStr = null;
		int i;
		Document doc = null;
		while(null != (lineStr = reader.readLine())) {
			doc = new Document();
			i = lineStr.indexOf(Global.delimiterLevel1);
			doc.add(new IntPoint("wId", Integer.parseInt(lineStr.substring(0, i))));
			doc.add(new StoredField("date", lineStr.substring(i+2)));
			indexWriter.addDocument(doc);
		}
		this.closeIndexWriter();
		reader.close();
//		System.out.println("-over建立" + sourcePath + "里的文件" + entryName + "的索引   " + TimeUtility.getTime());
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
	
	// 通过nodeIdKeywordListOnIntDateMapYagoVB.txt的索引检索文件
	public Map<Integer, String> searchWIDDateIndexReMap(ArrayList<Integer> seachedWIdList){
		Map<Integer, String> resultMap = null;
		Document doc = null;
		try {
			TopDocs results = indexSearcher.search(IntPoint.newSetQuery("wId", seachedWIdList), Integer.MAX_VALUE);
			ScoreDoc[] hits = results.scoreDocs;
			resultMap = new HashMap<>();
			for(int i=0; i<hits.length; i++) {
				doc = indexSearcher.doc(hits[i].doc);
				resultMap.put(Integer.parseInt(doc.get("wId")), doc.get("date"));
			}
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("检索失败而退出！！！");
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
			System.out.println("     【" + searchedWordId + "】  > " + "命中的点数 = " + resultList.size() + " " + TimeUtility.getTime());
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
			System.out.println("     【" + searchedWordId + "】  > " + "命中的点数 = " + resultList.size() + " " + TimeUtility.getTime());
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
	 * 文件nodeIdKeywordListOnDateMapYagoVB.txt ： 4: 【2018-3-22#2018-3-23#2018-1-1#27,】转化为下面文件
	 * nodeIdKeywordListOnIntDateMapYagoVB.txt : 【4: 150#100#200#27】其中100对应的是天数
	 */
	public void convertToNodeIdKeywordListOnIntDateTxt(String souPath, String nWIntDatePath) throws Exception{
		BufferedReader sBr = new BufferedReader(new FileReader(new File(souPath)));
		BufferedWriter nWDBw = new BufferedWriter(new FileWriter(new File(nWIntDatePath)));
		String lineStr = null;
		// 减少算法的比较次数
		lineStr = sBr.readLine();
		nWDBw.write(lineStr + '\n');
		int i = 0, j = 0, k = 0;
		String[] sArr = null;
		String tempStr = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		TreeSet<Integer> dateSet = null;
		while(null != (lineStr = sBr.readLine())) {
			// 处理nodeId
			i = lineStr.indexOf(Global.delimiterLevel1);
			i += 2;
			nWDBw.write(lineStr.substring(0, i));
			
			// 处理dateStr
			j = lineStr.lastIndexOf(Global.delimiterDate);
			tempStr = lineStr.substring(i, j);
			sArr = tempStr.split("#");
			dateSet = new TreeSet<>();
			for(String str : sArr) {
				k = TimeUtility.getIntDate(sdf.parse(str));
				dateSet.add(k);
			}
			for(int in : dateSet) {
				nWDBw.write(String.valueOf(in));
				nWDBw.write(Global.delimiterDate);
			}
			
			// 处理nodeList
			j++;
			tempStr = lineStr.substring(j);
			nWDBw.write(tempStr);
			nWDBw.write('\n');
		}
		nWDBw.flush();
		nWDBw.close();
		sBr.close();
	}
	
	
	/* 
	 * 文件nodeIdKeywordListOnDateMapYagoVB.txt ： 4: 【2018-3-22#2018-3-23#2018-1-1#27,】转化为下面两个文件
	 * nodeIdKeywordListOnIntDateMapYagoVB.txt : 【4: 150#100#200#27】其中100对应的是天数
	 * wordIdOnIntDateYagoVB.txt : 【27: 100#150#200#】 按由小到大排列
	 */
	public void convertNodeIdKeywordListOnDateMapTxt(String souPath, String nWIntDatePath, String wIntDatePath) throws Exception{
		BufferedReader sBr = new BufferedReader(new FileReader(new File(souPath)));
		BufferedWriter nWDBw = new BufferedWriter(new FileWriter(new File(nWIntDatePath)));
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
			i = lineStr.indexOf(Global.delimiterLevel1);
			nodeId = Integer.parseInt(lineStr.substring(0, i));
			i += 2;
			nWDBw.write(lineStr.substring(0, i));
			
			// 处理dateStr
			j = lineStr.lastIndexOf(Global.delimiterDate);
			tempStr = lineStr.substring(i, j);
			sArr = tempStr.split("#");
			dateSet = new TreeSet<>();
			for(String str : sArr) {
				k = (int)(sdf.parse(str).getTime()/86400000);
				dateSet.add(k);
			}
			for(int in : dateSet) {
				nWDBw.write(String.valueOf(in));
				nWDBw.write(Global.delimiterDate);
			}
			
			// 处理nodeList
			j++;
			tempStr = lineStr.substring(j);
//			nWDBw.write(tempStr);
//			nWDBw.write('\n');
			sArr = tempStr.split(Global.delimiterLevel2);
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
			wDBw.write(Global.delimiterLevel1);
			for(Integer in : en.getValue()) {
				wDBw.write(String.valueOf(in));
				wDBw.write(Global.delimiterDate);
			}
			wDBw.write('\n');
		}
		wDBw.flush();
		wDBw.close();
	}
	
	/**
	 * 创建nid to dates and wids 索引, 和wids to dates索引
	 * @param souPath
	 * @param nWIntDatePath
	 * @param wIntDatePath
	 * @throws Exception
	 */
	public void create_NidToDateWidIndex_WidDateIndex(String souPath, String nWIntDatePath, String wIntDatePath) throws Exception{
		this.convertNodeIdKeywordListOnDateMapTxt(souPath, nWIntDatePath, wIntDatePath);
	}
	
	public static void mainToCreateWidDataIndexAndNidWidDateIndex() throws Exception{
		if(!new File(Global.inputDirectoryPath).exists()) {
			throw new DirectoryNotEmptyException("目录inputDirectoryPath ： " + Global.inputDirectoryPath + "不存在");
		}
		String souFile = Global.inputDirectoryPath + Global.nodeIdKeywordListOnDateFile;
		String nWIntDateFile = Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile;
		String wIntDateFile = Global.inputDirectoryPath + Global.widOnIntDateFile;
		
		IndexNidKeywordsListService ser = new IndexNidKeywordsListService(null);
		long startTime = System.currentTimeMillis();
		System.out.println("> 开始创建wid to nid、date、wid index 和 wid to date index . . . ");
		
		System.out.println("> 开始将" + Global.inputDirectoryPath + "下的" + Global.nodeIdKeywordListOnDateFile + "转化为" + Global.nodeIdKeywordListOnIntDateFile + "和" + Global.widOnIntDateFile + " . . . ");
		ser.convertNodeIdKeywordListOnDateMapTxt(souFile, nWIntDateFile, wIntDateFile);
		System.out.println("> 转化完成.");
		
		if(!new File(Global.outputDirectoryPath).exists()) {
			throw new DirectoryNotEmptyException("目录outputDirectoryPath ： " + Global.outputDirectoryPath + "不存在");
		}
		
		System.out.println("> 开始创建" +  nWIntDateFile + "的索引wid to nid、date、wid index . . . ");
		String nWIntDateIndex = Global.outputDirectoryPath + Global.indexNIdWordDate;
		ser = new IndexNidKeywordsListService(nWIntDateIndex);
		ser.createNIDKeyListDateIndex(nWIntDateFile, null);
		System.out.println("> 完成创建" +  nWIntDateFile + "的索引wid to nid、date、wid index");
		
		System.out.println("> 开始创建" +  wIntDateFile + "的索引wid to date index . . . ");
		String wIntDateIndex = Global.outputDirectoryPath + Global.indexWIdDate;
		ser = new IndexNidKeywordsListService(wIntDateIndex);
		ser.createWIDDateIndex(wIntDateFile, null);
		System.out.println("> 完成创建" +  wIntDateFile + "的索引wid to date index");
		
		System.out.println("> 完成创建wid to nid、date、wid index 和 wid to date index，用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis())); 
	}
	
	public static void mainToCreateNidWidDataIndex(boolean hasConvert) throws Exception{
		if(!new File(Global.inputDirectoryPath).exists()) {
			throw new DirectoryNotEmptyException("目录inputDirectoryPath ： " + Global.inputDirectoryPath + "不存在");
		}
		String souFile = Global.inputDirectoryPath + Global.nodeIdKeywordListOnDateFile;
		String nWIntDateFile = Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile;
		
		IndexNidKeywordsListService ser = new IndexNidKeywordsListService(null);
		long startTime = System.currentTimeMillis();
		System.out.println("> 开始创建wid to nid、date、wid index . . . ");
		
		if(!hasConvert) {
			System.out.println("> 开始将" + Global.inputDirectoryPath + "下的" + Global.nodeIdKeywordListOnDateFile + "转化为" + Global.nodeIdKeywordListOnIntDateFile + " . . . ");
			ser.convertToNodeIdKeywordListOnIntDateTxt(souFile, nWIntDateFile);
			System.out.println("> 转化完成.");
		}
		
		if(!new File(Global.outputDirectoryPath).exists()) {
			throw new DirectoryNotEmptyException("目录outputDirectoryPath ： " + Global.outputDirectoryPath + "不存在");
		}
		
		System.out.println("> 开始创建" +  nWIntDateFile + "的索引wid to nid、date、wid index . . . ");
		String nWIntDateIndex = Global.outputDirectoryPath + Global.indexNIdWordDate;
		ser = new IndexNidKeywordsListService(nWIntDateIndex);
		ser.createNIDKeyListDateIndex(nWIntDateFile, null);
		System.out.println("> 完成创建" +  nWIntDateFile + "的索引wid to nid、date、wid index");
		
		System.out.println("> 完成创建wid to nid、date、wid index, 用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis())); 
	}
	
	/**
	 * 主函数
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception{
		IndexNidKeywordsListService.mainToCreateNidWidDataIndex(false);
//		String nWIntDateIndex = Global.outputDirectoryPath + Global.indexNIdWordDate;
//		IndexNidKeywordsListService ser = new IndexNidKeywordsListService(nWIntDateIndex);
//		int wid = 0;
//		Scanner sca = new Scanner(System.in);
//		ser.openIndexReader();
//		while(0 != (wid = sca.nextInt())) {
//			for(Entry<Integer, String> en : ser.searchNIDKeyListDate(wid).entrySet()) {
//				System.out.println(en.getKey() + " : " + en.getValue());
//			}
//		}
//		ser.closeIndexReader();
	}
}
