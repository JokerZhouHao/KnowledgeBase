package entity.sp.reach;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.BinaryDocValuesField;
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
import org.apache.lucene.util.BytesRef;

import entity.sp.AllPidWid;
import utility.Global;
import utility.LocalFileInfo;
import utility.PatternAnalyzer;
import utility.TimeUtility;

public class W2PIndex {
	private static long startTime = System.currentTimeMillis();
	private IndexWriter indexWriter = null;
	private Analyzer analyzer = null;
	private IndexReader indexReader = null;
	private IndexSearcher indexSearcher = null;
	private String indexPath = null;
	
	public final static List<Integer> allWids = AllPidWid.getAllWid();
	public final static int widSpan = allWids.size()/40;
	public final static int W2PIndexNum = allWids.size()%widSpan==0?allWids.size()/widSpan:allWids.size()/widSpan+1;
	
	public W2PIndex(String indexPath) {
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
	
	private byte[] listToBytes(List<Integer> li) {
		int byteNum = (1 + li.size()) * 4;
		ByteBuffer bb = ByteBuffer.allocate(byteNum);
		bb.rewind();
		bb.putInt(li.size());
		for(int in : li) {
			bb.putInt(in);
		}
		return bb.array();
	}
	
	private Set<Integer> bytesToSet(byte[] bytes){
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		Set<Integer> set = new HashSet<>();
		int size = bb.getInt();
		for(int i=0; i<size; i++) {
			set.add(bb.getInt());
		}
		return set;
	}
	
	// 添加document
	public void addDoc(int wid, List<Integer> pids) {
		Document doc = new Document();
		doc.add(new IntPoint("wid", wid));
		doc.add(new StoredField("pids", new BytesRef(listToBytes(pids))));
		try {
			indexWriter.addDocument(doc);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("添加索引失败而退出！！！");
			System.exit(0);
		}
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
	public Set<Integer> getPids(Integer wid){
		try {
			TopDocs results = indexSearcher.search(IntPoint.newExactQuery("wid", wid), 1);
			ScoreDoc[] hits = results.scoreDocs;
			if(hits.length == 0)	return null;
			return bytesToSet(indexSearcher.doc(hits[0].doc).getBinaryValue("pids").bytes);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("检索wid：" + wid + "失败而退出！！！");
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
	
	
	public static void buidingW2PIndex() throws Exception{
		System.out.println("> 开始创建W2PIndex . . . " + TimeUtility.getTime());
		int start = 0, end = 0;
		int span = P2WReach.zipContianNodeNum;
		int zipNum = 0;
		
		if(Global.numPid%span !=0) zipNum = Global.numPid/span + 1;
		else zipNum = Global.numPid/span;
		ArrayBlockingQueue<Integer>[] widQueues = new ArrayBlockingQueue[zipNum];
		int i = 0;
		for(i=0; i<zipNum; i++) {
			widQueues[i] = new ArrayBlockingQueue<Integer>(1);
		}
		ArrayBlockingQueue<List<Integer>> pidsQueue = new ArrayBlockingQueue<>(zipNum);
		
		for(i=0; i<zipNum; i++) {
			start = end;
			end += span;
			if(end > Global.numPid)	end = Global.numPid;
			new W2PReachReader(widQueues[i], pidsQueue, start, end).start();
		}
		
		String basePath = Global.indexWid2Pid;
		if(!new File(basePath).exists()) {
			new File(basePath).mkdir();
		}
		
		List<Integer> allWids = AllPidWid.getAllWid();
		start = end = 0;
		span = W2PIndex.widSpan;
		String indexPath = null;
		File file = null;
		int j = 0, k;
		int curWid = 0;
		W2PIndex index = null;
		List<Integer> pids = null;
		List<Integer> tList = null;
		
		int widNum = allWids.size();
		
		for(i=0; i<W2PIndexNum; i++) {
			indexPath = basePath + "wids_block_" + String.valueOf(W2PIndexNum) + "_" + String.valueOf(i);
			file = new File(indexPath);
			if(!file.exists()) {
				file.mkdir();
			}
			index = new W2PIndex(indexPath);
			index.openIndexWriter();
			
			start = end;
			end += span;
			if(end > allWids.size()) end = allWids.size();
			for(j=start; j<end; j++) {
				curWid = allWids.get(j);
				for(k=0; k<zipNum; k++) {
					widQueues[k].put(curWid);
				}
				for(k=0; k<zipNum; k++) {
					tList = pidsQueue.take();
					if(tList.get(0) > -1) {
						if(null == pids) {
							pids = new ArrayList<>();
						}
						for(int in : tList) {
							pids.add(in);
						}
						tList.clear();
					}
				}
				if(null!=pids && !pids.isEmpty()) {
					index.addDoc(curWid, pids);
					pids.clear();
				}
				
			}
			index.closeIndexWriter();
			System.out.println("> 已处理" + end + "个wid，共" + widNum + "个wid, 用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()));
		}
		System.out.println("> 完成索引创建，用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis()) + ". " + TimeUtility.getTime());
		System.err.println("> 发送结束读信号 . . . ");
		for(i=0; i<zipNum; i++) {
			widQueues[i].put(-1);
		}
		for(i=0; i<zipNum; i++) {
			tList = pidsQueue.take();
			if(tList.get(0) != -2) break;
		}
		if(i != zipNum) {
			System.err.println("> 发送的结束信号未正常收到");
		} else {
			System.err.println("> 发送的结束信号正常收到 ！！！");
		}
	}
	
	
	public static void test() {
//		ByteBuffer byteB = ByteBuffer.allocate(10);
//		byteB.putInt(23);
//		byteB.rewind();
//		System.out.println(byteB.getInt());
//		byte b[] = byteB.array();
//		System.out.println(b.length);
		
		String indexPath = LocalFileInfo.getDataSetPath() + "lucene_test";
		W2PIndex index = new W2PIndex(indexPath);
		int wid = 1;
		List<Integer> pids = new ArrayList<>();
		
		index.openIndexWriter();
		wid = 1;
		pids.add(11);
		pids.add(111);
		index.addDoc(wid, pids);
		pids.clear();
		wid = 2;
		pids.add(22);
		pids.add(222);
		index.addDoc(wid, pids);
		index.closeIndexWriter();
		
		index.openIndexReader();
//		pids = index.getPids(1);
//		pids = index.getPids(2);
		index.closeIndexReader();
		
	}
	
	public static void main(String[] args) throws Exception{
		W2PIndex.buidingW2PIndex();
	}
}
