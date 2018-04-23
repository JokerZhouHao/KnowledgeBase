/**
 * 
 */
package entity.sp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import entity.sp.SortedList.SortedListNode;
import utility.Global;
import utility.MComparator;
import utility.Utility;

/**
 * index the dates and keywords  of the nid
 * @author Monica
 *
 */

public class NidToDateWidIndex {
	
	/**
	 * record dates and keyword ids
	 * @author Monica
	 *
	 */
	public class DateWid{
		private String dateWidStr = null;
		private SortedList dateList = null;
		private ArrayList<Integer>	widList = null;
		
		DateWid(String str) {
			this.dateWidStr = str;
		}
		
		private void format() {
			if(null == dateWidStr)	return;
			dateList = new SortedList();
			widList = new ArrayList<Integer>();
			
			String strArr[] = null;
			int i;
			
			i = dateWidStr.lastIndexOf(Global.delimiterDate);
			strArr = dateWidStr.substring(0, i).split(Global.delimiterDate);
			SortedListNode p = null;
			p = dateList.add(Integer.parseInt(strArr[0]));
			if(1 < strArr.length) {
				for(int j = 1; j < strArr.length; j++)
					p = dateList.add(p, Integer.parseInt(strArr[j]));
			}
			
			strArr = dateWidStr.substring(i + 1).split(Global.delimiterLevel2);
			for(String st : strArr)	widList.add(Integer.parseInt(st));
			
			dateWidStr = null;
		}
		
		public SortedList getDateList() {
			if(null == dateList)	format();
			SortedList sl = new SortedList();
			SortedListNode slNd = null;
			SortedListNode nd = dateList.getHead();
			slNd = sl.add(nd.getValue());
			nd = nd.getNext();
			while(null != nd) {
				sl.add(slNd, nd.getValue());
				nd = nd.getNext();
			}
			return sl;
		}

		public ArrayList<Integer> getWidList(){
			if(null == widList)	format();
			return widList;
		}
		
		public void display() {
			if(null != dateWidStr)	format();
			System.out.print("dates: " + dateList);
			
			System.out.print("wids:");
			for(Integer in : widList)
				System.out.print(in + " ");
			System.out.println();
		}
	}
	
	private Map<Integer, DateWid> nidToDateWidIndex = null;
	
	public NidToDateWidIndex(Map<Integer, String> nIdDateWidMap) {
		nidToDateWidIndex = new HashMap<Integer, DateWid>();
		for(Entry<Integer, String> en : nIdDateWidMap.entrySet()) {
			nidToDateWidIndex.put(en.getKey(), new DateWid(en.getValue()));
		}
	}
	
	public NidToDateWidIndex(String nidToDateWidFile) throws Exception {
		this.loadNidToDateWidIndex(nidToDateWidFile);
	}
	
	/**
	 * only load postinglists of given keywords
	 * */
//	public NidToDateWidIndex(String invertedIndexFile, Integer[] qwords) throws Exception {
//		this.loadInvertedIndex(invertedIndexFile, qwords);
//	}
	
	/**
	 * load nodeIdKeywordListOnIntDateMapYagoVB.txt
	 * @param nidToDateWidIndexPath
	 * @throws Exception
	 */
	private void loadNidToDateWidIndex(String nidToDateWidIndexPath) throws Exception {
		nidToDateWidIndex = new HashMap<Integer, DateWid>();

		BufferedReader reader = Utility.getBufferedReader(nidToDateWidIndexPath);
		String line = reader.readLine();
		// first line is metadata in format: numlines#totalCount#

		int cntlines = 0;
		while ((line = reader.readLine()) != null) {
			cntlines++;
			if (line.contains(Global.delimiterPound)) {
				continue;
			}
			String[] splits = line.split(Global.delimiterLevel1);
			if (splits.length != 2) {
				throw new Exception("Inverted index " + line + "splits should be 2, but is " + splits.length
						+ " at line " + cntlines);
			}

			int wid = Integer.parseInt(splits[0]);

			nidToDateWidIndex.put(wid, new DateWid(splits[1]));

			if (cntlines % 100000 == 0) {
				System.out.print(cntlines + " postinglist loaded,");
			}
		}
	}
	
//	private void loadInvertedIndex(String invertedIndexFile, Integer[] qwords) throws Exception {
//		invertedIndex = new HashMap<Integer, List<Integer>>();
//
//		Set<Integer> qwordSet = new HashSet<Integer>();
//		for (int i = 0; i < qwords.length; i++) {
//			qwordSet.add(qwords[i]);
//		}
//		
//		BufferedReader reader = Utility.getBufferedReader(invertedIndexFile);
//		String line = reader.readLine();
//		// first line is metadata in format: numlines#totalCount#
//		// keyword: id1,id2,...
//		String[] stat = line.split(Global.delimiterPound);
//		int numlines = Integer.parseInt(stat[0]);
//		if (numlines != Global.numKeywords) {
//			throw new Exception("configuration numKeywords is " + Global.numKeywords
//					+ " but stat in read-file is " + numlines);
//		}
//
//		int cntlines = 0;
//		while ((line = reader.readLine()) != null) {
//			cntlines++;
//			String[] splits = line.split(Global.delimiterLevel1);
//			if (splits.length != 2) {
//				throw new Exception("Inverted index " + line + "splits should be 2, but is " + splits.length
//						+ " at line " + cntlines);
//			}
//
//			int keyword = Integer.parseInt(splits[0]);
//			if (keyword >= Global.numNodes + Global.numKeywords || keyword < Global.numNodes) {
//				throw new Exception("inverted index " + line + " kid out of range");
//			}
//			
//			//only load postinglists of provided keywords
//			if (!qwordSet.contains(keyword)) {
//				continue;
//			}
//
//			String[] idsStr = splits[1].split(Global.delimiterLevel2);
//
//			ArrayList<Integer> ids = new ArrayList<Integer>(idsStr.length);
//			for (int i = 0; i < idsStr.length; i++) {
//				ids.add(i, Integer.parseInt(idsStr[i]));
//			}
//			invertedIndex.put(keyword, ids);
//			
//			qwordSet.remove(keyword);
//			System.out.print(keyword + "'s postinglist loaded,");
//
//			if (qwordSet.size() == 0) {
//				break;//postinglists of all keywords are loaded
//			}		
//		}
//	}
	
	public Map<Integer, DateWid> getNidToDateWidIndex() {
		return nidToDateWidIndex;
	}
	
	public DateWid getDateWid(int index) {
		return nidToDateWidIndex.get(index);
	}

	public int size() {
		return nidToDateWidIndex.size();
	}
	
	public static void main(String[] args) throws Exception{
		NidToDateWidIndex idx = new NidToDateWidIndex(Global.inputDirectoryPath + Global.nodeIdKeywordListOnIntDateFile);
		for(Entry<Integer, DateWid> en : idx.getNidToDateWidIndex().entrySet()) {
			System.out.print(en.getKey() + " > ");
			en.getValue().display();
		}
	}
}
