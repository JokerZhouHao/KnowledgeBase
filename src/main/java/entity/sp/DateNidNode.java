package entity.sp;

import java.util.ArrayList;
import java.util.Collections;


import utility.MComparator;

/**
 * @author zhou
 * @since 2018/5/12
 */
public class DateNidNode {
	private int date = 0;
	private int nid = 0;
	
	public DateNidNode() {}
	
	public DateNidNode(int date, int nid) {
		this.date = date;
		this.nid = nid;
	}
	
	public int compareDate(DateNidNode dn) {
		if(date > dn.date)	return 1;
		else if(date == dn.date)	return 0;
		else return -1;
	}
	
	public int getDate() {
		return date;
	}

	public void setDate(int date) {
		this.date = date;
	}

	public int getNid() {
		return nid;
	}

	public void setNid(int nid) {
		this.nid = nid;
	}
	
	public static void main(String args[]) {
		ArrayList<DateNidNode> dateWidList = new ArrayList<>();
		dateWidList.add(new DateNidNode(1, -1));
		dateWidList.add(new DateNidNode(3, -1));
		dateWidList.add(new DateNidNode(5, -1));
		System.out.println(Collections.binarySearch(dateWidList, new DateNidNode(-9, -1), new MComparator<DateNidNode>()));
	}

}
