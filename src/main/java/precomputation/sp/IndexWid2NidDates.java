package precomputation.sp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.compress.utils.ByteUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;

import entity.Index;
import entity.sp.DatesWIds;
import utility.ByteUtility;
import utility.Global;
import utility.TimeUtility;

/**
 * 建立wid到nid和dates的索引
 * @author Monica
 *
 */
public class IndexWid2NidDates extends Index{
	
	public IndexWid2NidDates(String indexPath) {
		super(indexPath);
	}
	
	public byte[] listToBytes(List<Integer> li) {
		int byteNum = li.size() * 4;
		ByteBuffer bb = ByteBuffer.allocate(byteNum);
		bb.rewind();
		for(int in : li) {
			bb.putInt(in);
		}
		return bb.array();
	}
	
	// 创建wid到nid和dates的索引
	public void createWid2NidDatesIndex(String filePath) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		System.out.println("> " + reader.readLine());
		this.openIndexWriter();
		String lineStr = null;
		int i, nodeId, tempI;
		String[] strArr = null;
		Document doc = null;
		List<Integer> list = new ArrayList<>();
		int wid;
		while(null != (lineStr = reader.readLine())) {
			list.clear();
			doc = new Document();
			i = lineStr.indexOf(Global.delimiterLevel1);
			
			// 添加nid
			nodeId = Integer.parseInt(lineStr.substring(0, i));
			list.add(nodeId);
			
			// 添加dates
			tempI = lineStr.lastIndexOf(Global.delimiterDate);
			strArr = lineStr.substring(i+2, tempI).split(Global.delimiterDate);
			list.add(strArr.length);
			for(String st : strArr) {
				list.add(Integer.parseInt(st));
			}
			
			// 添加wids
			strArr = lineStr.substring(tempI + 1).split(Global.delimiterLevel2);
			list.add(strArr.length);
			for(String s : strArr) {
				wid = Integer.parseInt(s);
				list.add(wid);
				doc.add(new IntPoint("wids",wid));
			}
			
			doc.add(new StoredField("NidDatesWids", new BytesRef(listToBytes(list))));
			
			indexWriter.addDocument(doc);
		}
		this.closeIndexWriter();
		reader.close();
	}
	
	// 搜索
	public Map<Integer, DatesWIds> searchWid(int[] wids){
		Map<Integer, DatesWIds> resultMap = null;
		Document doc = null;
		try {
//			TopDocs results = indexSearcher.search(IntPoint.newExactQuery("wids", wid), Integer.MAX_VALUE);
			TopDocs results = indexSearcher.search(IntPoint.newSetQuery("wids", wids), Integer.MAX_VALUE);
			ScoreDoc[] hits = results.scoreDocs;
			
			if(hits.length <= 0)	return null;
			
			System.out.println(hits.length);
			
			Set<Integer> widsSet = new HashSet();
			for(int wid : wids) {
				widsSet.add(wid);
			}
			
			resultMap = new HashMap<>();
			int nid = 0;
			DatesWIds dw = null;
			ByteBuffer bb = null;
			int num;
			int j = 0, wid, k;
			List<Integer> tList = new ArrayList<>();
			for(int i=0; i<hits.length; i++) {
				tList.clear();
				
				doc = indexSearcher.doc(hits[i].doc);
				bb = ByteBuffer.wrap(doc.getBinaryValue("NidDatesWids").bytes);
				
				nid = bb.getInt();
				
				dw = new DatesWIds(wids.length);
				
				num = bb.getInt();
				for(j=0; j<num; j++) {
					dw.addDate(bb.getInt());
				}
				
				num = bb.getInt();
				for(j=0; j<num; j++) {
					wid = bb.getInt();
					tList.add(wid);
				}
				
				k = 0;
				for(j=0; j<tList.size(); j++) {
					for(;k<wids.length; k++) {
						if(tList.get(j) < wids[k])	break;
						else if(tList.get(j) == wids[k]) {
							dw.addWid(k, wids[k]);
						}
					}
					if(k== wids.length)	break;
				}
				
				resultMap.put(nid, dw);
			}
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("检索seachedWIdList：" + resultMap + "失败而退出！！！");
			System.exit(0);
		}
		return null;
	}
	
	/**
	 * 创建索引
	 * @param hasConvert
	 * @throws Exception
	 */
	public static void mainToCreateNidWidDataIndex(boolean hasConvert) throws Exception{
		if(!new File(Global.inputDirectoryPath).exists()) {
			throw new DirectoryNotEmptyException("目录inputDirectoryPath ： " + Global.inputDirectoryPath + "不存在");
		}
		String souFile = Global.inputDirectoryPath + Global.nodeIdKeywordListOnDateFile;
		String nWIntDateFile = Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile;
		
		long startTime = System.currentTimeMillis();
		System.out.println("> 开始创建wid to nid dates index . . .");
		
		if(!hasConvert) {
			System.out.println("> 开始将" + Global.inputDirectoryPath + "下的" + Global.nodeIdKeywordListOnDateFile + "转化为" + Global.nodeIdKeywordListOnIntDateFile + " . . . ");
			IndexNidKeywordsListService.convertToNodeIdKeywordListOnIntDateTxt(souFile, nWIntDateFile);
			System.out.println("> 转化完成.");
		}
		
		if(!new File(Global.outputDirectoryPath).exists()) {
			throw new DirectoryNotEmptyException("目录outputDirectoryPath ： " + Global.outputDirectoryPath + "不存在");
		}
		
		System.out.println("> 开始创建" +  nWIntDateFile + "的索引wid to nid dates index . . .");
		String nWIntDateIndex = Global.outputDirectoryPath + Global.indexNIdWordDate;
		IndexWid2NidDates ser = new IndexWid2NidDates(nWIntDateIndex);
		ser.createWid2NidDatesIndex(nWIntDateFile);
		System.out.println("> 完成创建" +  nWIntDateFile + "的索引wid to nid、date、wid index");
		System.out.println("> 完成创建wid to nid dates index , 用时：" + TimeUtility.getSpendTimeStr(startTime, System.currentTimeMillis())); 
	}
	
	/**
	 * 主函数
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception{
//		IndexWid2NidDates.mainToCreateNidWidDataIndex(Boolean.TRUE);
		IndexWid2NidDates index = new IndexWid2NidDates(Global.outputDirectoryPath + Global.indexNIdWordDate);
		index.openIndexReader();
		int[] wids = new int[2];
		wids[0] = 11381939;
		wids[1] = 10561570;
//		for(int wid : wids) {
			long start = System.currentTimeMillis();
			index.searchWid(wids);
			System.out.println((System.currentTimeMillis() - start)/1000);
//		}
		index.closeIndexReader();
	}
}

























