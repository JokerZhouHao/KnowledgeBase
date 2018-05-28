package precomputation.freebase;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.Token;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import utility.LocalFileInfo;
import utility.PatternAnalyzer;
import utility.TimeUtility;

/**
 * 
 * @author Monica
 * @since 2017/12/07
 * 功能：提供与NodeMap相关的服务
 */
public class IndexNodeMapService {
	private String indexPath = null;
	private IndexWriter indexWriter = null;
	private Analyzer analyzer = null;
	private IndexReader indexReader = null;
	private IndexSearcher indexSearcher = null;
	private QueryParser nodeIdParser = null;
	private QueryParser keywordsParser = null;
	private QueryParser edgesParser = null;
	
	private int nodeNum = 0;
	private long keywordAvgLen = 0L;
	
	private Boolean hasCalInfo = Boolean.FALSE;
	private long wordNumNoContainSame = 0L;
	private long wordNumContainSame = 0L;
	private long sumDocFreq = 0L;
	public static int sumTermNum = 114837967;
	
	public IndexNodeMapService(String indexPath) {
		this.indexPath = indexPath;
		analyzer = new StandardAnalyzer();
		nodeIdParser = new QueryParser("nodeId", analyzer);
//		nodeIdParser = IntPoint.newExactQuery("nodeId", 0);
		keywordsParser = new QueryParser("keywords", analyzer);
		edgesParser = new QueryParser("edges", analyzer);
	}
	
	// 打开索引写器
	public void openIndexWriter() {
		try {
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
	public void addDoc(int nodeId, String keywords, String edges) {
		nodeNum++;
		keywordAvgLen += keywords.length();
		Document doc = new Document();
		doc.add(new IntPoint("nodeId", nodeId));
		doc.add(new StoredField("nodeId", nodeId));
		
		doc.add(new TextField("keywords", keywords, Field.Store.YES));
		
//		doc.add(new TextField("edges", edges, Field.Store.NO));
//		doc.add(new StoredField("edges", edges));
		try {
			indexWriter.addDocument(doc);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("添加索引失败而退出！！！");
			System.exit(0);
		}
		
	}
	
	// 添加document
		public void addDoc(int nodeId, String keywords) {
			nodeNum++;
			keywordAvgLen += keywords.length();
			Document doc = new Document();
			doc.add(new IntPoint("nodeId", nodeId));
			doc.add(new StoredField("nodeId", nodeId));
			
			doc.add(new TextField("keywords", keywords, Field.Store.YES));
			
//			doc.add(new TextField("edges", edges, Field.Store.NO));
//			doc.add(new StoredField("edges", edges));
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
			keywordAvgLen = keywordAvgLen/nodeNum;
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
	
	// 检索关键字，获得对应点
	public ArrayList<Integer> searchWordReNodeIds(String searchedWord, int maxId){
		ArrayList<Integer> resultList = null;
		try {
			Query query = keywordsParser.parse(searchedWord);
			
			TopDocs results = indexSearcher.search(query, Integer.MAX_VALUE);
			ScoreDoc[] hits = results.scoreDocs;
			
			resultList = new ArrayList<Integer>();
			
			for(int i=0; i<hits.length; i++) {
				Integer in = Integer.parseInt(indexSearcher.doc(hits[i].doc).get("nodeId"));
				if(in<maxId)	resultList.add(in);
//				resultList.add(Integer.parseInt(indexSearcher.doc(hits[i].doc).get("nodeId")));
			}
			
			System.out.println("     【" + searchedWord + "】  > " + "命中的点数 = " + resultList.size() + " " + TimeUtility.getTime());
			
			return resultList;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("检索searchWordReNodeIds：" + searchedWord + "失败而退出！！！");
			System.exit(0);
		}
		return null;
	}
	
	// 返回id的属性
	public String searchNodeIdReAtt(int nodeId) {
		try {
			TopDocs results = indexSearcher.search(IntPoint.newExactQuery("nodeId", nodeId), 1);
			ScoreDoc[] hits = results.scoreDocs;
			if(hits.length==0)	return null;
			return indexSearcher.doc(hits[0].doc).get("keywords");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("检索searchNodeIdReAtt：" + nodeId + "失败而退出！！！");
			System.exit(0);
		}
		return null;
	}
	
	// 检索nodeId返回边
	public String searchNodeIdReEdges(int nodeId) {
		try {
//			Query query = nodeIdParser.parse(String.valueOf(nodeId));
			Query query = IntPoint.newExactQuery("nodeId", nodeId);
			
			TopDocs results = indexSearcher.search(query, Integer.MAX_VALUE);
			ScoreDoc[] hits = results.scoreDocs;
		
			if(hits.length>0) {
				return indexSearcher.doc(hits[0].doc).get("edges");
			} else return null;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("检索searchNodeIdReEdges：" + nodeId + "失败而退出！！！");
			System.exit(0);
		}
		return null;
	}
	
	// 获得放所有term数组
	public String[] getTermArr(String[] termArr) {
		try {
			Fields fileds = MultiFields.getFields(indexReader);
			Terms terms = fileds.terms("keywords");
			TermsEnum termsEnum = terms.iterator();
			BytesRef text = null;
			int i = 0;
			int len = termArr.length;
			while((text=termsEnum.next())!=null) {
				if(i!= len)
					termArr[i] = text.utf8ToString();
				else break;
				i++;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return termArr;
	}
	
	// 返回termIndexList指定的第n个term
	public ArrayList<String> getSearchedTerms(ArrayList<Integer> termIndexList) {
		ArrayList<String> searchedTerms = new ArrayList<>();
		try {
			Fields fileds = MultiFields.getFields(indexReader);
			Terms terms = fileds.terms("keywords");
			TermsEnum termsEnum = terms.iterator();
			BytesRef text = null;
			int i = -1, j = 0, k = termIndexList.get(0), size = termIndexList.size();
			while((text=termsEnum.next())!=null) {
				i++;
				if(i==k) {
					searchedTerms.add(text.utf8ToString());
					if(++j==size)	break;
					k = termIndexList.get(j);
//					System.out.println("k = " + k);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return searchedTerms;
	}
	
	// 返回与指向被检索点的点
	public ArrayList<Integer> searchTargetNodeReSourceNodeArr(int tarNodeId){
		ArrayList<Integer> sourNodeList = null;
		try {
//			Query query = nodeIdParser.parse(String.valueOf(nodeId));
//			Query query = IntPoint.newExactQuery("edges", tarNodeId);
			Query query = edgesParser.parse(String.valueOf(tarNodeId));
			
			TopDocs results = indexSearcher.search(query, Integer.MAX_VALUE);
			ScoreDoc[] hits = results.scoreDocs;
		
			if(hits.length>0) {
				sourNodeList = new ArrayList<>();
				int hitsNum = hits.length;
				for(int i=0; i<hitsNum; i++) {
					sourNodeList.add(Integer.parseInt(indexSearcher.doc(hits[i].doc).get("nodeId")));
				}
				return sourNodeList;
			} else return null;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("检索searchTargetNodeReSourceNodeArr：" + tarNodeId + "失败而退出！！！");
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
	
	// 打印所有的词
	public void diplayAllWords() {
		try {
			Fields fields = MultiFields.getFields(indexReader);
			Terms terms = fields.terms("keywords");
			TermsEnum termsEnum = terms.iterator();
			BytesRef text = null;
			while((text = termsEnum.next())!=null) {
				System.out.println(text.utf8ToString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void diplayAllEdgesWords() {
		try {
			Fields fields = MultiFields.getFields(indexReader);
			Terms terms = fields.terms("edges");
			TermsEnum termsEnum = terms.iterator();
			BytesRef text = null;
			while((text = termsEnum.next())!=null) {
				System.out.println(text.utf8ToString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 计算索引后产生的信息
	public void calInfo() {
		try {
			Fields fileds = MultiFields.getFields(indexReader);
			Terms terms = fileds.terms("keywords");
			TermsEnum termsEnum = terms.iterator();
			BytesRef text = null;
			while((text=termsEnum.next())!=null) {
				wordNumNoContainSame++;
//				wordNumContainSame+=termsEnum.totalTermFreq();
//				sumDocFreq += termsEnum.docFreq();
			}
//			wordNumNoContainSame = terms.size();
			wordNumContainSame = indexReader.getSumTotalTermFreq("keywords");
			sumDocFreq = indexReader.getSumDocFreq("keywords");
			
			hasCalInfo = Boolean.TRUE;
			System.err.println("wordNumNoContainSame = " + wordNumNoContainSame);
			System.err.println("wordsNumContainSame = " + wordNumContainSame);
			System.err.println("wo = " + sumDocFreq);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public long getWordNumNoContainSame() {
		if(!hasCalInfo) calInfo();
		return wordNumNoContainSame;
	}

	public long getWordNumContainSame() {
		if(!hasCalInfo) calInfo();
		return wordNumContainSame;
	}

	public long getSumDocFreq() {
		if(!hasCalInfo) calInfo();
		return sumDocFreq;
	}

	public long getKeywordAvgLen() {
		return keywordAvgLen;
	}

	public static void main(String[] args) {
		
		IndexNodeMapService indexService = new IndexNodeMapService(LocalFileInfo.getIndexPath(7));
		indexService.openIndexWriter();
		indexService.addDoc(0, "v0");
		indexService.addDoc(1, "v1");
		indexService.addDoc(2, "v2");
		indexService.addDoc(3, "v3");
		indexService.addDoc(4, "v4");
		indexService.addDoc(5, "v5");
		indexService.addDoc(6, "v6");
		indexService.closeIndexWriter();
		
//		Analyzer analyzer = new StandardAnalyzer();
//		 try {
//	            //将一个字符串创建成Token流
//	            TokenStream stream  = analyzer.tokenStream("", "zhou hao 123");
//	            //保存相应词汇
//	            CharTermAttribute cta = stream.addAttribute(CharTermAttribute.class);
//	            stream.reset();
//	            while(stream.incrementToken()){
//	                System.out.print("[" + cta + "]");
//	            }
//	            System.out.println();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
		
//		IndexNodeMapService indexService = new IndexNodeMapService(LocalFileInfo.getBasePath() + File.separator + "data" + File.separator + "testIndex");
//		indexService.openIndexWriter();
//		indexService.addDoc(0, "k0", "1 2 ");
//		indexService.addDoc(1, "k1", "0 ");
//		indexService.addDoc(2, "k2", "1 ");
//		indexService.closeIndexWriter();
//		System.exit(0);
		
//		IndexNodeMapService indexService = new IndexNodeMapService(LocalFileInfo.getTestIndexPath());
//		IndexNodeMapService indexService = new IndexNodeMapService(LocalFileInfo.getIndexPath());
//		indexService.openIndexReader();
//		indexService.calInfo();
//		System.out.println(indexService.searchNodeIdReEdges(0));
//		ArrayList<Integer> idList = new ArrayList<>();
//		idList.add(10000);
//		idList.add(20000);
//		idList.add(114837967);
//		ArrayList<String> strList = indexService.getSearchedTerms(idList);
//		for(String in : strList) {
//			System.out.println(in);
//		}
//		System.out.println(indexService.searchTargetNodeReSourceNodeArr(1).size());
//		indexService.closeIndexReader();
		
//		ZipReaderService reader = new ZipReaderService(LocalFileInfo.getNodeIdAndKeywordAndEdgeZipPath(), "edges");
//		for(int i=0; i<2; i++)
//			System.out.println(reader.readLine());
//		reader.close();
		
////		IndexNodeMapService indexService = new IndexNodeMapService(LocalFileInfo.getIndexPath());
//		indexService.openIndexWriter();
//		indexService.addDoc(1, "v1", "3 ");
//		indexService.addDoc(2, "v2", "3 ");
//		indexService.addDoc(3, "v3", "2 1 7 ");
//		indexService.addDoc(4, "v4", "3 5 ");
//		indexService.addDoc(5, "v5", "4 6 ");
//		indexService.addDoc(6, "v6", "5 ");
//		indexService.addDoc(7, "v7", "3 ");
//		indexService.closeIndexWriter();
		
//		indexService.openIndexReader();
//		
//		indexService.diplayAllEdgesWords();
//		
//		System.out.println();
//		
//		ArrayList<Integer> list = indexService.searchTargetNodeReSourceNodeArr(3);
//		if(null!=list) {
//			for(Integer in : list)
//				System.out.println(in);
//		}  
		
//		indexService.diplayAllWords();
		
//		indexService.getWordNumNoContainSame();
//		System.out.println(indexService.searchWordReNodeIds("31").isEmpty());
//		System.err.println(indexService.searchNodeIdReEdges(7));
//		for(Integer in : indexService.searchWordReNodeIds("v1")) {
//			System.out.println(in);
//		}
		
//		indexService.calInfo();
//		indexService.closeIndexReader();
	}
	
}
