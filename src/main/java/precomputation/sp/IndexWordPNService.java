package precomputation.sp;

import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import utility.Global;
import utility.PatternAnalyzer;
import utility.TimeUtility;

public class IndexWordPNService {
	
	private String indexPath = null;
	private IndexWriter indexWriter = null;
	private IndexReader indexReader = null;
	private IndexSearcher indexSearcher = null;
	
	public IndexWordPNService(String indexPath) {
		this.indexPath = indexPath;
	}
	
	// 打开索引写器
	public void openIndexWriter() {
		try {
			Directory indexDir = FSDirectory.open(Paths.get(indexPath));
			IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
			iwc.setOpenMode(OpenMode.CREATE);
			indexWriter = new IndexWriter(indexDir, iwc);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("打开IndexWriter失败而退出！！！");
			System.exit(0);
		}
	}
	
	public void addDoc(int wId, String pIdDates) {
		Document doc = new Document();
		doc.add(new IntPoint("wId", wId));
		doc.add(new StoredField("pIdDates", pIdDates));
		try {
			indexWriter.addDocument(doc);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void closeIndexWriter() {
		try {
			this.indexWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
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
	
	public String getPlaceNeighborhoodStr(int wid) {
		try {
			TopDocs results = indexSearcher.search(IntPoint.newExactQuery("wId", wid), 1);
			if(Global.isTest) {
				Global.tempTime = System.currentTimeMillis();
			}
			ScoreDoc[] hits = results.scoreDocs;
			if(hits.length == 0)	return null;
			
			String st = indexSearcher.doc(hits[0].doc).get("pIdDates");
			if(Global.isTest) {
				if(Global.isFirstReadPn) {
					Global.isFirstReadPn = false;
				}
				Global.tempTime = System.currentTimeMillis();
			}
			return st;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("检索seachedWId：" + wid + "失败而退出！！！");
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
	
	public static void main(String[] args) {
		String st = null;
		IndexWordPNService ser = new IndexWordPNService(Global.outputDirectoryPath + Global.indexWidPN);
		ser.openIndexReader();
		for(int i = Global.numNodes; i < Global.numNodes + Global.numKeywords; i++) {
			if(null != (st = ser.getPlaceNeighborhoodStr(i))) {
				System.out.println(i + ": " + st);
			}
		}
		ser.closeIndexReader();
	}
}
