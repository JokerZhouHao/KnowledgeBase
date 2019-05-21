package entity.sp.date;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import entity.Index;
import entity.sp.AllDateWidNodes;
import entity.sp.AllDateWidNodes.DWid;
import entity.sp.DateNidNode;
import entity.sp.RunRecord;
import entity.sp.SortedDateWidCReach;
import entity.sp.SortedDateWidIndex;
import utility.Global;
import utility.IOUtility;
import utility.TimeUtility;

/**
 * 输出所有wid的DateNid对
 * @author Monica
 *
 */
public class Wid2DateNidPairIndex extends Index{
	
	private static long startTime = System.currentTimeMillis();
	
	public Wid2DateNidPairIndex(String indexPath) {
		super(indexPath);
	}
	
	public TreeMap<Integer, TreeMap<Integer, List<Integer>>> loadFile(String filePath) throws Exception{
		System.out.println("> 开始读取文件" + filePath + ". . . " + TimeUtility.getTime());
		Map<Integer, DWid> allDW = null;
		allDW = AllDateWidNodes.loadFromFile(filePath);
		TreeMap<Integer, TreeMap<Integer, List<Integer>>> wid2DateNid = new TreeMap<>();
		TreeMap<Integer, List<Integer>> map = null;
		List<Integer> list = null;
		int nid = 0;
		DWid tDW = null;
		for(Entry<Integer, DWid> en : allDW.entrySet()) {
			nid = en.getKey();
			tDW = en.getValue();
			for(int wid : tDW.wids) {
				if(null == (map = wid2DateNid.get(wid))) {
					map = new TreeMap<>();
					wid2DateNid.put(wid, map);
				}
				for(int date : tDW.dates) {
					if(null == (list = map.get(date))) {
						list = new ArrayList<>();
						map.put(date, list);
					}
					list.add(nid);
				}
			}
		}
		allDW.clear();
		System.out.println("> 结束读取文件" + filePath + "！！！" + TimeUtility.getTailTime());
		return wid2DateNid;
	}
	
	public void writeFile(String writeToPath, Map<Integer, Map<Integer, List<Integer>>> wid2DateNid) throws Exception{
		System.out.println("> 开始写文件" + writeToPath + ". . . " + TimeUtility.getTime());
		DataOutputStream dos = IOUtility.getDos(writeToPath);
		dos.writeInt(wid2DateNid.size());
		for(int wid : wid2DateNid.keySet()) {
			dos.writeInt(wid);
		}
		int num = 0;
		int date = 0;
		for(Entry<Integer, Map<Integer, List<Integer>>> en : wid2DateNid.entrySet()) {
			num = 0;
			dos.writeInt(en.getKey());
			for(Entry<Integer, List<Integer>> en1 : en.getValue().entrySet()) {
				num += en1.getValue().size();
			}
			dos.writeInt(num);
			for(Entry<Integer, List<Integer>> en1 : en.getValue().entrySet()) {
				date = en1.getKey();
				for(int ni : en1.getValue()) {
					dos.writeInt(date);
					dos.writeInt(ni);
				}
			}
		}
		dos.close();
		System.out.println("> 结束写文件" + writeToPath + "！！！" + TimeUtility.getTailTime());
	}
	
	private byte[] mapToBytes(Map<Integer, List<Integer>> map) {
		int num = 0;
		for(List<Integer> li : map.values()) {
			num += li.size();
		}
		int byteNum = (num*2+1)*4;
		ByteBuffer bb = ByteBuffer.allocate(byteNum);
		bb.rewind();
		bb.putInt(num);
		int date = 0;
		for(Entry<Integer, List<Integer>> en : map.entrySet()) {
			date = en.getKey();
			for(int nid : en.getValue()) {
				bb.putInt(date);
				bb.putInt(nid);
			}
		}
		return bb.array();
	}
	
	// 添加document
	public void addDoc(int wid, Map<Integer, List<Integer>> map) {
		Document doc = new Document();
		doc.add(new IntPoint("wid", wid));
		doc.add(new StoredField("dateNidPair", new BytesRef(mapToBytes(map))));
		try {
			indexWriter.addDocument(doc);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("添加索引失败而退出！！！");
			System.exit(0);
		}
	}
	
	public void createIndex(String filePath) throws Exception{
		System.out.println("> 开始创建索引 . . . " + TimeUtility.getTime());
		TreeMap<Integer, TreeMap<Integer, List<Integer>>> wid2DateNid = this.loadFile(filePath);
		openIndexWriter();
		for(Entry<Integer, TreeMap<Integer, List<Integer>>> en : wid2DateNid.entrySet()) {
			addDoc(en.getKey(), en.getValue());
		}
		closeIndexWriter();
		System.out.println("> 结束创建索引 ！！！" + TimeUtility.getTailTime());
	}
	
	private SortedDateWidIndex bytesToSortedDateWid(byte[] bytes, int sDate){
		SortedDateWidIndex sdw = new SortedDateWidIndex();
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		int size = bb.getInt();
		int date = 0;
		LinkedList<DateNidNode> dnns = new LinkedList<>();
		int maxDate = sDate;
		int maxSign = 1;
		int i=0;
		for(; i<size; i++) {
			date = bb.getInt();
			if(date == Integer.MAX_VALUE) {	// 没有时间属性的节点的时间指定为Integer.MAX_VALUE
				if(maxSign >= 0) {
					if(Math.abs(maxDate - sDate) >= Global.maxDateSpan)
						dnns.addLast(new DateNidNode(maxDate, bb.getInt(), Boolean.TRUE));
					else dnns.addLast(new DateNidNode(maxDate, bb.getInt(), Boolean.FALSE));
				} else {
					if(Math.abs(maxDate - sDate) >= Global.maxDateSpan)
						dnns.addFirst(new DateNidNode(maxDate, bb.getInt(), Boolean.TRUE));
					else dnns.addFirst(new DateNidNode(maxDate, bb.getInt(), Boolean.FALSE));
				}
			} else {
				if(Math.abs(date - sDate) > Math.abs(maxDate - sDate)) {
					maxDate = date;
					maxSign = maxDate - sDate;
				}
				if(Math.abs(date - sDate) >= Global.maxDateSpan) {
					dnns.addLast(new DateNidNode(date, bb.getInt(), Boolean.TRUE));
				} else {
					dnns.addLast(new DateNidNode(date, bb.getInt(), Boolean.FALSE));
				}
			}
			
//			date = bb.getInt();
//			if(date == Integer.MAX_VALUE) {	// 没有时间属性的节点的时间指定为Integer.MAX_VALUE
//				if(maxSign >= 0) {
//					if(Math.abs(maxDate - sDate) >= Global.maxDateSpan)
//						dnns.addLast(new DateNidNode(maxDate, bb.getInt(), Boolean.TRUE));
//					else dnns.addLast(new DateNidNode(maxDate, bb.getInt(), Boolean.FALSE));
//				} else {
//					if(Math.abs(maxDate - sDate) >= Global.maxDateSpan)
//						dnns.addFirst(new DateNidNode(maxDate, bb.getInt(), Boolean.TRUE));
//					else dnns.addFirst(new DateNidNode(maxDate, bb.getInt(), Boolean.FALSE));
//				}
//			} else {
//				if(Math.abs(date - sDate) > Math.abs(maxDate - sDate)) {
//					if(Math.abs(date - sDate) >= Global.defaultMaxDateSpan) {
//						maxSign = date - sDate;
//						date = maxSign * Global.defaultMaxDateSpan + sDate;
//						maxDate = date;
//					} else {
//						maxDate = date;
//						maxSign = maxDate - sDate;
//					}
//				}
//				if(Math.abs(date - sDate) >= Global.maxDateSpan) {
//					dnns.addLast(new DateNidNode(date, bb.getInt(), Boolean.TRUE));
//				} else {
//					dnns.addLast(new DateNidNode(date, bb.getInt(), Boolean.FALSE));
//				}
//			}
		}
		
		sdw.dateWidList = new ArrayList<>(dnns);
		
		return sdw;
	}
	
	
	
	
	
	
	public SortedDateWidIndex getDateNids(int wid, int sDate){
		try {
			TopDocs results = indexSearcher.search(IntPoint.newExactQuery("wid", wid), 1);
			ScoreDoc[] hits = results.scoreDocs;
			if(hits.length == 0)	return null;
			return bytesToSortedDateWid((indexSearcher.doc(hits[0].doc).getBinaryValue("dateNidPair").bytes), sDate);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("检索wid：" + wid + "失败而退出！！！");
			System.exit(0);
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception{
		String indexPath = Global.indexWid2DateNid;
		String filePath = Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile;
//		String writeToPath = Global.outputDirectoryPath + Global.wid2DateNidPairFile;
		Wid2DateNidPairIndex index = new Wid2DateNidPairIndex(indexPath);
//		index.createIndex(filePath);
		index.openIndexReader();
		SortedDateWidIndex sdw = index.getDateNids(4, TimeUtility.getIntDate(new Date()));
		index.closeIndexReader();
	}
}




















