package entity.sp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.tukaani.xz.lz.Matches;

import entity.sp.reach.CReach;
import precomputation.rechable.ReachableQueryService;
import utility.Global;
import utility.MComparator;

/**
 * 
 * @author zhou
 * @since 2018/05/12
 */
public class SortedDateWidIndex {
	
	private static MComparator<DateNidNode> comparator = new MComparator<DateNidNode>();
	
	private ArrayList<DateNidNode> dateWidList = new ArrayList<>();
	
	public void addLast(DateNidNode dw) {
		dateWidList.add(dw);
	}
	
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
	
	public int getMinDateSpan(HashSet<Integer> rec, int sDate, int p, CReach rsSer) {
		int mid = Collections.binarySearch(dateWidList, new DateNidNode(sDate, -1), comparator);
		int left = 0;
		int right = 0;
		int i = mid;
		DateNidNode tempNode = null;
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
		
//		int leftMinIndex = 0;
//		int rightMaxIndex = 0;
		
		int leftSpan = Integer.MAX_VALUE, rightSpan = Integer.MAX_VALUE, tempSpan = 0;
		DateNidNode dnn = null;
		int size = dateWidList.size();
		if(left + 1 <= (size - right)) {
			i = -1;
//			leftMinIndex = left - Global.leftMaxIndexSpan;
//			if(leftMinIndex<0)	leftMinIndex = 0;
			
			while(left >= 0) {
				dnn = dateWidList.get(left);
				if(dnn.isMax) {
					leftSpan = Global.maxDateSpan;
					break;
				}
				if(i == dnn.getNid()) {
					left--;
					continue;
				} else {
					i = dnn.getNid();
				}
				if(rec.contains(i)) {
//				if(rec.contains(tempL + i)) {
					left--;
				} else {
					if(rsSer.queryReachable(p, i)){
						leftSpan = Math.abs(sDate - dateWidList.get(left).getDate()) + 1;
						break;
					} else {
						rec.add(i);
//						rec.add(tempL + i);
					}
				}
			}
			
//			Global.rr.numCptGetMinDateSpanLeftSpan  = Global.rr.numCptGetMinDateSpanLeftSpan < mid - left?mid-left:Global.rr.numCptGetMinDateSpanLeftSpan;
			
			i = -1;
//			rightMaxIndex = right + Global.rightMaxIndexSpan;
//			if(rightMaxIndex >= dateWidList.size()) rightMaxIndex = dateWidList.size();
			
			while(right < size) {
				dnn = dateWidList.get(right);
				if(dnn.isMax) {
					rightSpan = Global.maxDateSpan;
					break;
				}
				tempSpan = Math.abs(sDate - dnn.getDate()) + 1;
				if(tempSpan >= leftSpan)	break;
				if(i == dnn.getNid()) {
					right++;
					continue;
				} else {
					i = dnn.getNid();
				}
				if(rec.contains(i)) {
//				if(rec.contains(tempL + i)) {
					right++;
				} else {
					if(rsSer.queryReachable(p, i)){
						rightSpan = tempSpan;
						break;
					} else {
						rec.add(i);
//						rec.add(tempL + i);
					}
				}
			}
			
//			Global.rr.numCptGetMinDateSpanRightSpan  = Global.rr.numCptGetMinDateSpanRightSpan < right-mid?right-mid:Global.rr.numCptGetMinDateSpanRightSpan;
			
		} else {
			i = -1;
			
//			rightMaxIndex = right + Global.rightMaxIndexSpan;
//			if(rightMaxIndex >= dateWidList.size()) rightMaxIndex = dateWidList.size();
			
			while(right < size) {
				dnn = dateWidList.get(right);
				if(dnn.isMax) {
					rightSpan = Global.maxDateSpan;
					break;
				}
				if(i == dnn.getNid()) {
					right++;
					continue;
				} else {
					i = dnn.getNid();
				}
				if(rec.contains(i)) {
//				if(rec.contains(tempL + i)) {
					right++;
				} else {
					if(rsSer.queryReachable(p, i)){
						rightSpan = Math.abs(sDate - dateWidList.get(right).getDate()) + 1;
						break;
					} else {
						rec.add(i);
//						rec.add(tempL + i);
					}
				}
			}
//			Global.rr.numCptGetMinDateSpanRightSpan  = Global.rr.numCptGetMinDateSpanRightSpan < right-mid?right-mid:Global.rr.numCptGetMinDateSpanRightSpan;
			
			i = -1;
			
//			leftMinIndex = left - Global.leftMaxIndexSpan;
//			if(leftMinIndex<0)	leftMinIndex = 0;
			
			while(left >= 0) {
				dnn = dateWidList.get(left);
				if(dnn.isMax) {
					leftSpan = Global.maxDateSpan;
					break;
				}
				tempSpan = Math.abs(sDate - dateWidList.get(left).getDate()) + 1;
				if(tempSpan >= rightSpan)	break;
				if(i == dnn.getNid()) {
					left--;
					continue;
				} else {
					i = dnn.getNid();
				}
				if(rec.contains(i)) {
//				if(rec.contains(tempL + i)) {
					left--;
				} else {
					if(rsSer.queryReachable(p, i)){
						leftSpan = tempSpan;
						break;
					} else {
						rec.add(i);
//						rec.add(tempL + i);
					}
				}
			}
			
//			Global.rr.numCptGetMinDateSpanLeftSpan  = Global.rr.numCptGetMinDateSpanLeftSpan < mid - left?mid-left:Global.rr.numCptGetMinDateSpanLeftSpan;
			
		}
		if(leftSpan == Integer.MAX_VALUE) {
			if(rightSpan == Integer.MAX_VALUE)	return -1;
			else return rightSpan;
		} else {
			if(rightSpan == Integer.MAX_VALUE)	return leftSpan;
			else return leftSpan<=rightSpan?leftSpan:rightSpan;
		}
	}
	
	public int getMinDateSpan(Set<Integer> rec, int sDate) {
		int mid = Collections.binarySearch(dateWidList, new DateNidNode(sDate, -1), comparator);
		int left = 0;
		int right = 0;
		int i = mid;
		DateNidNode tempNode = null;
		if(i >= 0) {
			tempNode = dateWidList.get(i);
			if(rec.contains(tempNode.getNid())) {
				return 1;
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
		DateNidNode dnn;
		int size = dateWidList.size();
		if(left + 1 <= (size - right)) {
			i = -1;
			while(left >= 0) {
				dnn = dateWidList.get(left);
				if(i == dnn.getNid()) {
					left--;
					continue;
				} else {
					i = dnn.getNid();
				}
				if(rec.contains(i)) {
					leftSpan = Math.abs(dnn.getDate() - sDate) + 1;
					break;
				}
			}
			
			Global.rr.numCptGetMinDateSpanLeftSpan  = Global.rr.numCptGetMinDateSpanLeftSpan < mid - left?mid-left:Global.rr.numCptGetMinDateSpanLeftSpan;
			
			i = -1;
			while(right < size) {
				dnn = dateWidList.get(right);
				tempSpan = Math.abs(sDate - dnn.getDate()) + 1;
				if(tempSpan >= leftSpan)	break;
				if(i == dnn.getNid()) {
					right++;
					continue;
				} else {
					i = dnn.getNid();
				}
				if(rec.contains(i)) {
					rightSpan = Math.abs(dnn.getDate() - sDate) + 1;
					break;
				}
			}
			
			Global.rr.numCptGetMinDateSpanRightSpan  = Global.rr.numCptGetMinDateSpanRightSpan < right-mid?right-mid:Global.rr.numCptGetMinDateSpanRightSpan;
			
		} else {
			i = -1;
			while(right < size) {
				dnn = dateWidList.get(right);
				if(i == dnn.getNid()) {
					right++;
					continue;
				} else {
					i = dnn.getNid();
				}
				if(rec.contains(i)) {
					rightSpan = Math.abs(dnn.getDate() - sDate) + 1;
					break;
				}
			}
			Global.rr.numCptGetMinDateSpanRightSpan  = Global.rr.numCptGetMinDateSpanRightSpan < right-mid?right-mid:Global.rr.numCptGetMinDateSpanRightSpan;
			
			i = -1;
			while(left >= 0) {
				dnn = dateWidList.get(left);
				tempSpan = Math.abs(sDate - dnn.getDate()) + 1;
				if(tempSpan >= rightSpan)	break;
				if(i == dnn.getNid()) {
					left--;
					continue;
				} else {
					i = dnn.getNid();
				}
				if(rec.contains(i)) {
					leftSpan = Math.abs(dnn.getDate() - sDate) + 1;
					break;
				}
			}
			
			Global.rr.numCptGetMinDateSpanLeftSpan  = Global.rr.numCptGetMinDateSpanLeftSpan < mid - left?mid-left:Global.rr.numCptGetMinDateSpanLeftSpan;
			
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
	
	public int size() {
		return dateWidList.size();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(DateNidNode dn : dateWidList) {
			sb.append("<" + String.valueOf(dn.getDate()) + ", " + String.valueOf(dn.getNid()) + "> ");
		}
		return sb.toString();
	}
	
	public ArrayList<DateNidNode> getDateWidList() {
		return dateWidList;
	}

	public static void main(String[] args) {
		SortedDateWidCReach sdw = new SortedDateWidCReach();
		sdw.addDateWid(new DateNidNode(1, 1));
		sdw.addDateWid(new DateNidNode(4, 2));
		sdw.addDateWid(new DateNidNode(2, 3));
		sdw.addDateWid(new DateNidNode(0, 4));
		sdw.addDateWid(new DateNidNode(2, 5));
		sdw.addDateWid(new DateNidNode(5, 6));
		System.err.println(sdw);
	}
}
