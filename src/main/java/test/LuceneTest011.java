package test;

import java.nio.file.Paths;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import precomputation.freebase.IndexNodeMapService;

public class LuceneTest011 {
	
	// 创建索引
	public static void createIndex() throws Exception{
		String indexPath = "./data/index/";
		String docsPath = "./data";
		
		Directory indexDir = FSDirectory.open(Paths.get(indexPath));
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		
		IndexWriter writer = new IndexWriter(indexDir, iwc);
		
		Document document = new Document();
		document.add(new IntPoint("nodeId", 1));
		document.add(new TextField("keywords", "ab at the btk ab", Field.Store.NO));
		document.add(new StoredField("nodeId", 1));
		document.add(new StoredField("edges", "2,3,"));
		writer.addDocument(document);
		
		document = new Document();
		document.add(new IntPoint("nodeId", 2));
		document.add(new TextField("keywords", "ab man woman child key", Field.Store.NO));
		document.add(new StoredField("nodeId", 2));
		document.add(new StoredField("edges", "4,"));
		writer.addDocument(document);
		
		document = new Document();
		document.add(new IntPoint("nodeId", 3));
		document.add(new TextField("keywords", "task as ab ks", Field.Store.NO));
		document.add(new StoredField("nodeId", 3));
		document.add(new StoredField("edges", ""));
		writer.addDocument(document);
		
		document = new Document();
		document.add(new IntPoint("nodeId", 4));
		document.add(new TextField("keywords", "", Field.Store.NO));
		document.add(new StoredField("nodeId", 4));
		document.add(new StoredField("edges", "2,"));
		writer.addDocument(document);
		
		document = new Document();
		document.add(new IntPoint("nodeId", 5));
		document.add(new TextField("keywords", "", Field.Store.NO));
		document.add(new StoredField("nodeId", 5));
		document.add(new StoredField("edges", ""));
		writer.addDocument(document);
		
		writer.close();
		
		System.err.println("Index create success");
		
		
		
	}
	
	// 获取参数
	public static void displayIndexInfo(String indexPath) throws Exception{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
//		Term term = new Term("keywords", "ab");
		
		Fields fields = MultiFields.getFields(reader);
		Terms terms = fields.terms("keywords");
		System.out.println("terms size : " + terms.size());
		TermsEnum termsEnum = terms.iterator();
		BytesRef text = null;
		while((text=termsEnum.next())!=null) {
			System.out.println(text.utf8ToString());
			System.out.println(termsEnum.docFreq());
			System.out.println(termsEnum.totalTermFreq() + "\n");
		}
//		termsEnum.next();
//		termsEnum.next();
		
		
		
//		System.out.println("docNun : " + reader.numDocs());
//		System.out.println("docCount : " + reader.getDocCount("keywords"));
		System.out.println("SumDocFreq : " + reader.getSumDocFreq("keywords"));
		System.out.println("SumTotalTermFreq : " + reader.getSumTotalTermFreq("keywords"));
//		System.out.println("totalTermFreq : " + reader.totalTermFreq(term));
//		System.out.println("Maxdoc : " + reader.maxDoc());
//		System.out.println("delDoc : " + reader.numDeletedDocs());
//		System.out.println("leave lean : " + reader.leaves().size());
//		TermsEnum te = reader.getTermVector(docID, field)
	}
	
	// 依据索引查找
	public static void search(String qu) throws Exception{
		String indexPath = "./data/index/";
		String field = "keywords";
		String queries = qu;
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser(field, analyzer);
		
		Query query = parser.parse(queries);
		System.err.println("Search for : " + queries);
		
		TopDocs results = searcher.search(query, 2);
		ScoreDoc[] hits = results.scoreDocs;
		
		int numTotalHits = Math.toIntExact(results.totalHits);
		System.out.println("Only results 1 - " + hits.length + " of " + numTotalHits + " total matching documents collected.");
		
		for(int i=0; i<hits.length; i++) {
			System.out.print("doc=" + hits[i].doc + " score=" + hits[i].score + " ");
			Document doc = searcher.doc(hits[i].doc);
			System.out.println("nodeId=" + doc.get("nodeId") + " edge=" + doc.get("edge") + " keywords=" + doc.get("keywords"));
		}
		
		System.out.println("Search Over");
	}
	
	public static void main(String[] args) throws Exception{
//		createIndex();
//		displayIndexInfo("./data/index/");
//		search("ab");
		
		IndexNodeMapService indexService = new IndexNodeMapService("./data/index/");
		indexService.openIndexWriter();
		indexService.addDoc(1, "ab at btk ab", "2,3,");
		indexService.addDoc(2, "ab man woman child key", "4,");
		indexService.addDoc(3, "task as	ab", "");
		indexService.addDoc(4, "", "2,");
		indexService.addDoc(5, "", "");
		indexService.closeIndexWriter();
		
		indexService.openIndexReader();
		System.out.println(indexService.searchNodeIdReEdges(4));
		for(Integer in : indexService.searchWordReNodeIds("ab", 100)) {
			System.out.println(in);
		}
//		indexService.diplayAllWords();
//		indexService.calInfo();
		indexService.closeIndexReader();
		
	}
}
