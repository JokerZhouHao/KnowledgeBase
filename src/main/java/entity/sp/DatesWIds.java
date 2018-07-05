package entity.sp;

import java.util.ArrayList;
import java.util.List;

import utility.Global;

public class DatesWIds {
	private List<Integer> dateList = null;
	private List<Integer> wIdList = null;
	private String datesStr = null;
	
	public DatesWIds() {}
	
	public DatesWIds(String datesStr) {
		this.datesStr = datesStr;
		formatDatesStr();
	}
	
	private void formatDatesStr() {
		if(datesStr == null)	return;
		String tempArr[] = this.datesStr.split(Global.delimiterDate);
		dateList = new ArrayList<>();
		for(String st : tempArr) {
			dateList.add(Integer.parseInt(st));
		}
	}

	public void addWid(int wid) {
		if(null == wIdList)	wIdList = new ArrayList<>();
		wIdList.add(wid);
	}
	
	public void addDate(int date) {
		if(null == dateList)	dateList = new ArrayList<>();
		dateList.add(date);
	}
	
	public List<Integer> getDateList() {
//		if(null == dateList) formatDatesStr();s
		return dateList;
	}

	public List<Integer> getwIdList() {
		return wIdList;
	}
	
	public void clear() {
		if(null != dateList)	dateList.clear();
		if(null != this.wIdList) wIdList.clear();
	}
	
	public static void main(String[] args) {
		DatesWIds dw = new DatesWIds("-18094#14644");
		for(int in : dw.getDateList()) {
			System.out.println(in);
		}
	}
}