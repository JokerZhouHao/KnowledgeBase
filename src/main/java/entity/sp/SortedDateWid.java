package entity.sp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import precomputation.rechable.ReachableQueryService;
import utility.Global;
import utility.MComparator;

/**
 * 
 * @author zhou
 * @since 2018/05/12
 */
public class SortedDateWid {
	
	private static MComparator<DateNidNode> comparator = new MComparator<DateNidNode>();
	
	private ArrayList<DateNidNode> dateWidList = new ArrayList<>();
	
	private int addDateWid(DateNidNode dw, int start, int end) {
		if(end < 0) {
			dateWidList.add(dw);
			return 0;
		}
		int mid = 0;
		while(true) {
			mid = (start + end)/2;
			switch(dw.compareDate(dateWidList.get(mid))) {
				case 1 : {
					if(start == end) {
						if(end+1 == dateWidList.size()) {
							dateWidList.add(dw);
						} else {
							dateWidList.add(end + 1, dw);
						}
						return end+1;
					}
					start = mid + 1;
					break;
				}
				case 0 : {
					dateWidList.add(mid, dw);
					return mid;
				}
				case -1 : {
					if(mid == start) {
						dateWidList.add(mid, dw);
						return mid;
					}
					end = mid - 1;
					break;
				}
			}
		}
	}
	
	public int addDateWid(DateNidNode dw) {
		return this.addDateWid(dw, 0, this.dateWidList.size()-1);
	}
	
	public int addDateWid(DateNidNode dw, int start) {
		return this.addDateWid(dw, start, dateWidList.size()-1);
	}
	
	
	public int getMinDateSpan(int sDate) {
		int reIndex = Collections.binarySearch(this.dateWidList, new DateNidNode(sDate, -1), comparator);
		if(0 <= reIndex) // 存在相等的日期
			return 1;
		else {
			reIndex = -reIndex;
			if(0 < reIndex-1 && reIndex-1 < dateWidList.size()) {	// 当前日期在所有日期中间
				if(sDate - dateWidList.get(reIndex -2).getDate() < dateWidList.get(reIndex - 1).getDate() - sDate)
					return sDate - dateWidList.get(reIndex -2).getDate() + 1;
				else
					return dateWidList.get(reIndex - 1).getDate() - sDate + 1;
			} else if(reIndex == dateWidList.size() + 1) {	// 当前日期晚于于当前所有日期
				return sDate - dateWidList.get(dateWidList.size() -1).getDate() + 1;
			} else {	// 当前时间早于所有时间
				return dateWidList.get(0).getDate() - sDate + 1;
			}
		}
	}
	
	public int getMinDateSpan(HashSet<Integer> rec, int sDate, int p, ReachableQueryService rsSer) {
		int mid = Collections.binarySearch(dateWidList, new DateNidNode(sDate, -1), comparator);
		int left = 0;
		int right = 0;
		int i = mid;
		DateNidNode tempNode = null;
		long tempL = p * Global.numSCCs0;
		if(i >= 0) {
			tempNode = dateWidList.get(i);
			if(!rec.contains(tempNode.getNid()) && rsSer.queryReachable(p, tempNode.getNid())) {
//			if(!rec.contains(tempL + tempNode.getNid()) && rsSer.queryReachable(p, tempNode.getNid())) {
				return 1;
			} else {
				rec.add(tempNode.getNid());
//				rec.add(tempL + tempNode.getNid());
			}
			mid = i;
			left = i - 1;
			right = i + 1;
		} else {
			left = (-i) - 2;
			mid = left;
			right = (-i) - 1;
		}
		
		int leftSpan = Integer.MAX_VALUE, rightSpan = Integer.MAX_VALUE, tempSpan = 0;
		if(left + 1 <= (dateWidList.size() - right)) {
			i = -1;
			while(left >= 0) {
				if(i == dateWidList.get(left).getNid()) {
					left--;
					continue;
				} else {
					i = dateWidList.get(left).getNid();
				}
				if(rec.contains(i)) {
//				if(rec.contains(tempL + i)) {
					left--;
				} else {
					Global.recCount[2]++;
					if(rsSer.queryReachable(p, i)){
						leftSpan = Math.abs(sDate - dateWidList.get(left).getDate()) + 1;
						break;
					} else {
						rec.add(i);
//						rec.add(tempL + i);
					}
				}
			}
			
			Global.leftMaxSpan  = Global.leftMaxSpan < mid - left?mid-left:Global.leftMaxSpan;
			
			i = -1;
			while(right < dateWidList.size()) {
				tempSpan = Math.abs(sDate - dateWidList.get(right).getDate()) + 1;
				if(tempSpan >= leftSpan)	break;
				if(i == dateWidList.get(right).getNid()) {
					right++;
					continue;
				} else {
					i = dateWidList.get(right).getNid();
				}
				if(rec.contains(i)) {
//				if(rec.contains(tempL + i)) {
					right++;
				} else {
					Global.recCount[2]++;
					if(rsSer.queryReachable(p, i)){
						rightSpan = tempSpan;
						break;
					} else {
						rec.add(i);
//						rec.add(tempL + i);
					}
				}
			}
			
			Global.rightMaxSpan  = Global.rightMaxSpan < right-mid?right-mid:Global.rightMaxSpan;
			
		} else {
			i = -1;
			while(right < dateWidList.size()) {
				if(i == dateWidList.get(right).getNid()) {
					right++;
					continue;
				} else {
					i = dateWidList.get(right).getNid();
				}
				if(rec.contains(i)) {
//				if(rec.contains(tempL + i)) {
					right++;
				} else {
					Global.recCount[2]++;
					if(rsSer.queryReachable(p, i)){
						rightSpan = Math.abs(sDate - dateWidList.get(right).getDate()) + 1;
						break;
					} else {
						rec.add(i);
//						rec.add(tempL + i);
					}
				}
			}
			
			Global.rightMaxSpan  = Global.rightMaxSpan < right-mid?right-mid:Global.rightMaxSpan;
			
			i = -1;
			while(left >= 0) {
				tempSpan = Math.abs(sDate - dateWidList.get(left).getDate()) + 1;
				if(tempSpan >= rightSpan)	break;
				if(i == dateWidList.get(left).getNid()) {
					left--;
					continue;
				} else {
					i = dateWidList.get(left).getNid();
				}
				if(rec.contains(i)) {
//				if(rec.contains(tempL + i)) {
					left--;
				} else {
					Global.recCount[2]++;
					if(rsSer.queryReachable(p, i)){
						leftSpan = tempSpan;
						break;
					} else {
						rec.add(i);
//						rec.add(tempL + i);
					}
				}
			}
			
			Global.leftMaxSpan  = Global.leftMaxSpan < mid - left?mid-left:Global.leftMaxSpan;
			
		}
		if(leftSpan == Integer.MAX_VALUE) {
			if(rightSpan == Integer.MAX_VALUE)	return -1;
			else return rightSpan;
		} else {
			if(rightSpan == Integer.MAX_VALUE)	return leftSpan;
			else return leftSpan<=rightSpan?leftSpan:rightSpan;
		}
	}
	
	public void clear() {
		if(null != dateWidList)	this.dateWidList.clear();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(DateNidNode dn : dateWidList) {
			sb.append("<" + String.valueOf(dn.getDate()) + ", " + String.valueOf(dn.getNid()) + "> ");
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		SortedDateWid sdw = new SortedDateWid();
		sdw.addDateWid(new DateNidNode(1, 1));
		sdw.addDateWid(new DateNidNode(4, 2));
		sdw.addDateWid(new DateNidNode(2, 3));
		sdw.addDateWid(new DateNidNode(0, 4));
		sdw.addDateWid(new DateNidNode(2, 5));
		sdw.addDateWid(new DateNidNode(5, 6));
		System.err.println(sdw);
	}
}
