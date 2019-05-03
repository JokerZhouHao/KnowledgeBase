package entity.sp;

import java.util.ArrayList;
import java.util.List;

import utility.Global;

public class DatesWIds {
	private List<Integer> dateList = null;
	private int wids[] = null;
	
	private String datesStr = null;
	
	public DatesWIds(int numWids) {
		this.wids = new int[numWids];
	}
	
	public DatesWIds(String datesStr, int numWids) {
		this.datesStr = datesStr;
		this.wids = new int[numWids];
		formatDatesStr();
	}
	
	public DatesWIds(Integer date, int numWids) {
		dateList = new ArrayList<>();
		dateList.add(date);
		this.wids = new int[numWids];
	}
	
	private void formatDatesStr() {
		if(datesStr == null)	return;
		String tempArr[] = this.datesStr.split(Global.delimiterDate);
		dateList = new ArrayList<>();
		for(String st : tempArr) {
			dateList.add(Integer.parseInt(st));
		}
	}

	public void addWid(int indexWid, int wid) {
		this.wids[indexWid] = wid;
	}
	
	public void addDate(int date) {
		if(null == dateList)	dateList = new ArrayList<>();
		dateList.add(date);
	}
	
	public List<Integer> getDateList() {
//		if(null == dateList) formatDatesStr();s
		return dateList;
	}

	public int[] getWids() {
		return wids;
	}
	
	public void clear() {
		if(null != dateList)	dateList.clear();
	}
	
	public static void main(String[] args) {
		DatesWIds dw = new DatesWIds("-18094#14644", 4);
		for(int in : dw.getDateList()) {
			System.out.println(in);
		}
	}
}