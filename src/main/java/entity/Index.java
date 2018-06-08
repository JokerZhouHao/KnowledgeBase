package entity;

import java.io.File;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Index {
	protected IndexWriter indexWriter = null;
	protected Analyzer analyzer = null;
	protected IndexReader indexReader = null;
	protected IndexSearcher indexSearcher = null;
	protected String indexPath = null;
	
	public Index() {}
	
	public Index(String indexPath) {
		super();
		this.indexPath = indexPath;
	}
	
	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}
	
	// 打开索引写器
	protected void openIndexWriter() {
		try {
			analyzer = new StandardAnalyzer();
			if(!new File(indexPath).exists()) {
				new File(indexPath).mkdir();
			}
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
	
	// 关闭索引写
	protected void closeIndexWriter() {
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
}
