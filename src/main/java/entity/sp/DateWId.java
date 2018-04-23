package entity.sp;

import java.util.ArrayList;
import java.util.List;

import utility.Global;

public class DateWId {
	private List<Integer> dateList = null;
	private List<Integer> wIdList = null;
	private String str = null;
	
	public DateWId(String str) {
		this.str = str;
	}
	
	public void init(String str) {
		int i = str.lastIndexOf(Global.delimiterDate);
		String tempArr[] = null;
		tempArr = str.substring(0, i).split(Global.delimiterDate);
		dateList = new ArrayList<>();
		for(String st : tempArr) {
			dateList.add(Integer.parseInt(st));
		}
		
		tempArr = str.substring(i + 1).split(Global.delimiterLevel2);
		wIdList = new ArrayList<>();
		for(String st : tempArr) {
			wIdList.add(Integer.parseInt(st));
		}
	}

	public List<Integer> getDateList() {
		if(null == dateList) init(str);
		return dateList;
	}

	public List<Integer> getwIdList() {
		if(null == wIdList)	init(str);
		return wIdList;
	}
}