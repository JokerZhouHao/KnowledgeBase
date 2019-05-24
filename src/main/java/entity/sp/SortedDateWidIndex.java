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
	
	public ArrayList<DateNidNode> dateWidList = new ArrayList<>();
	
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
		if(dateWidList.isEmpty())	return 1;
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
	
	/**
	 * pid getMinDateSpan
	 * @param rec
	 * @param sDate
	 * @param p
	 * @param rsSer
	 * @param initSpan
	 * @param widMinDateSpan
	 * @return
	 * @throws Exception
	 */
	public int[] getMinDateSpan(HashSet<Integer> rec, int sDate, int p, CReach rsSer, int initSpan, int widMinDateSpan[], int maxDateSpan) throws Exception{
//		if(dateWidList.isEmpty() || widMinDateSpan[1]==-1 || widMinDateSpan[2]==-1) {
		if(dateWidList.isEmpty()) {
			widMinDateSpan[0] = 1;
			return widMinDateSpan;
		}
		
		if(widMinDateSpan[0] == maxDateSpan) return widMinDateSpan;	// 已经遍历完了
		
//		if(widMinDateSpan[1]==-1 || widMinDateSpan[2]==-1) {
//			System.out.println(widMinDateSpan[0] + " " + widMinDateSpan[1] + " " + widMinDateSpan[2]);
//			System.exit(0);
//		}		
		
		int left = widMinDateSpan[1];
		int right = widMinDateSpan[2];
		int i=0;
		int leftSpan = widMinDateSpan[0], rightSpan = Integer.MAX_VALUE, tempSpan = 0;
		DateNidNode dnn = null;
		int size = dateWidList.size();
		
		if(left + 1 <= (size - right)) {
			i = -1;
			
			while(left >= 0) {
				dnn = dateWidList.get(left);
				if(dnn.isMax) {
					leftSpan = Global.maxDateSpan;
					break;
				}

				while(left>=0 && i==dateWidList.get(left).getNid()) {
					left--;
				}
				if(left<0)	break;
				
				dnn = dateWidList.get(left);
				i = dateWidList.get(left).getNid();
				
				if(rec.contains(i)) {
					left--;
				} else {
					if(rsSer.queryReachable(p, i)){
						leftSpan = sDate - dnn.getDate() + 1;
						break;
					} else {
						rec.add(i);
						left--;
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
				rightSpan = tempSpan = dnn.getDate() - sDate + 1;
				if(tempSpan >= leftSpan)	break;

				while(right < size && i==dateWidList.get(right).getNid()) {
					right++;
				}
				if(right>=size)	break;
				
				dnn = dateWidList.get(right);
				i = dateWidList.get(right).getNid();
				
				if(rec.contains(i)) {
					right++;
				} else {
					if(rsSer.queryReachable(p, i)){
						rightSpan = dnn.getDate() - sDate + 1;
						break;
					} else {
						rec.add(i);
						right++;
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

				while(right < size && i==dateWidList.get(right).getNid()) {
					right++;
				}
				if(right>=size)	break;
				
				dnn = dateWidList.get(right);
				i = dateWidList.get(right).getNid();
				
				if(rec.contains(i)) {
//				if(rec.contains(tempL + i)) {
					right++;
				} else {
					if(rsSer.queryReachable(p, i)){
						rightSpan = dnn.getDate() - sDate + 1;
						break;
					} else {
						rec.add(i);
						right++;
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
				leftSpan = tempSpan = sDate - dnn.getDate() + 1;
				if(tempSpan >= rightSpan)	break;

				while(left>=0 && i==dateWidList.get(left).getNid()) {
					left--;
				}
				if(left<=0)	break;
				
				dnn = dateWidList.get(left);
				i = dateWidList.get(left).getNid();
				
				if(rec.contains(i)) {
//				if(rec.contains(tempL + i)) {
					left--;
				} else {
					if(rsSer.queryReachable(p, i)){
						leftSpan = sDate - dnn.getDate() + 1;
						break;
					} else {
						rec.add(i);
						left--;
//						rec.add(tempL + i);
					}
				}
			}
			
//			Global.rr.numCptGetMinDateSpanLeftSpan  = Global.rr.numCptGetMinDateSpanLeftSpan < mid - left?mid-left:Global.rr.numCptGetMinDateSpanLeftSpan;
			
		}
		widMinDateSpan[1] = left;
		widMinDateSpan[2] = right;
		if(left < 0 && right >= size) {	// 该pid不可达带有该词且有时间的节点
			widMinDateSpan[0] = maxDateSpan;
		} else widMinDateSpan[0] = leftSpan<=rightSpan?leftSpan:rightSpan;
		
		return widMinDateSpan;
	}
	
	public int[] getMinDateSpan(Set<Integer> rec, int sDate, int widMinDateSpan[], int maxDateSpan) throws Exception{
		if(dateWidList.isEmpty()) {	// 不存在带时间的节点
			widMinDateSpan[0] = 1;
			return widMinDateSpan;
		} 
		
		if(widMinDateSpan[0] == maxDateSpan) return widMinDateSpan;	// 已经遍历完了
		
		int mid=0, left=0, right=0, i=0;
		DateNidNode tempNode = null;
		
		int leftSpan = Integer.MAX_VALUE, rightSpan = Integer.MAX_VALUE, tempSpan = 0;
		DateNidNode dnn;
		int size = dateWidList.size();
		
		if(widMinDateSpan[0]==-1) {
			mid = Collections.binarySearch(dateWidList, new DateNidNode(sDate, -1), comparator);
			i = mid;
			if(i >= 0) {
				tempNode = dateWidList.get(i);
				if(rec.contains(tempNode.getNid())) {
					widMinDateSpan[0] = 1;
					widMinDateSpan[1] = mid;
					widMinDateSpan[2] = mid + 1;
					return widMinDateSpan;
				}
				mid = i;
				left = i - 1;
				right = i + 1;
			} else {
				left = (-i) - 2;
				mid = left;
				right = (-i) - 1;
			}
		} else {
			leftSpan = widMinDateSpan[0];
			left = widMinDateSpan[1];
			right = widMinDateSpan[2];
		}
		
		if(left + 1 <= (size - right)) {
			i = -1;
			while(left >= 0) {
				dnn = dateWidList.get(left);
				if(dnn.isMax) {
					leftSpan = Global.maxDateSpan;
					break;
				}
				
				while(left>=0 && i==dateWidList.get(left).getNid()) {
					left--;
				}
				if(left<0)	break;
				
				dnn = dateWidList.get(left);
				i = dateWidList.get(left).getNid();
				
				if(rec.contains(i)) {
					leftSpan = sDate - dnn.getDate() + 1;
					break;
				} else left--;
			}
			
			Global.rr.numCptGetMinDateSpanLeftSpan  = Global.rr.numCptGetMinDateSpanLeftSpan < mid - left?mid-left:Global.rr.numCptGetMinDateSpanLeftSpan;
			
			i = -1;
			while(right < size) {
				dnn = dateWidList.get(right);
				if(dnn.isMax) {
					rightSpan = Global.maxDateSpan;
					break;
				}
				rightSpan = tempSpan = dnn.getDate() - sDate + 1;
				if(tempSpan >= leftSpan)	break;

				while(right < size && i==dateWidList.get(right).getNid()) {
					right++;
				}
				if(right>=size)	break;
				
				dnn = dateWidList.get(right);
				i = dateWidList.get(right).getNid();
				
				if(rec.contains(i)) {
					rightSpan = dnn.getDate() - sDate + 1;
					break;
				}	else right++;
			}
			
			Global.rr.numCptGetMinDateSpanRightSpan  = Global.rr.numCptGetMinDateSpanRightSpan < right-mid?right-mid:Global.rr.numCptGetMinDateSpanRightSpan;
			
		} else {
			i = -1;
			while(right < size) {
				dnn = dateWidList.get(right);
				if(dnn.isMax) {
					rightSpan = Global.maxDateSpan;
					break;
				}

				while(right < size && i==dateWidList.get(right).getNid()) {
					right++;
				}
				if(right==size)	break;
				
				dnn = dateWidList.get(right);
				i = dateWidList.get(right).getNid();
				
				if(rec.contains(i)) {
					rightSpan = dnn.getDate() - sDate + 1;
					break;
				} else right++;
			}
			Global.rr.numCptGetMinDateSpanRightSpan  = Global.rr.numCptGetMinDateSpanRightSpan < right-mid?right-mid:Global.rr.numCptGetMinDateSpanRightSpan;
			
			i = -1;
			while(left >= 0) {
				dnn = dateWidList.get(left);
				if(dnn.isMax) {
					rightSpan = Global.maxDateSpan;
					break;
				}
				leftSpan = tempSpan = sDate - dnn.getDate() + 1;
				if(tempSpan >= rightSpan)	break;
				
				while(left>=0 && i==dateWidList.get(left).getNid()) {
					left--;
				}
				if(left<0)	break;
				
				dnn = dateWidList.get(left);
				i = dateWidList.get(left).getNid();
				
				if(rec.contains(i)) {
					leftSpan = sDate - dnn.getDate() + 1;
					break;
				} else left--;
			}
			
			Global.rr.numCptGetMinDateSpanLeftSpan  = Global.rr.numCptGetMinDateSpanLeftSpan < mid - left?mid-left:Global.rr.numCptGetMinDateSpanLeftSpan;
			
		}
//		if(leftSpan == Integer.MAX_VALUE) {
//			if(rightSpan == Integer.MAX_VALUE)	return -1;
//			else return rightSpan;
//		} else {
//			if(rightSpan == Integer.MAX_VALUE)	return leftSpan;
//			else return leftSpan<=rightSpan?leftSpan:rightSpan;
//		}
		
		widMinDateSpan[1] = left;
		widMinDateSpan[2] = right;
		if(left < 0 && right >= size) {	// 该rtree不可达带有该词且有时间的节点
			widMinDateSpan[0] = maxDateSpan;
//			throw new Exception("未找到RTreeNode GetMinDateSpan");
//			return null;
		} else widMinDateSpan[0] = leftSpan<=rightSpan?leftSpan:rightSpan;
		
		return widMinDateSpan;
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
