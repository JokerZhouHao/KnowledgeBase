package entity.sp;

import java.util.ArrayList;
import java.util.Collections;

import precomputation.rechable.ReachableQueryService;
import utility.MComparator;

/**
 * 
 * @author zhou
 * @since 2018/05/12
 */
public class SortedDateWid {
	
	private MComparator<DateNidNode> comparator = new MComparator<DateNidNode>();
	
	private DateNidNode head = new DateNidNode();
	private ArrayList<DateNidNode> dateWidList = null;
	
	public DateNidNode addDateWid(DateNidNode dw) {
		DateNidNode p1 = head;
		DateNidNode p2 = p1.getNext();
		while(null != p2) {
			if(p2.getDate() >= dw.getDate()) {
				p1.setNext(dw);
				dw.setNext(p2);
				return dw;
			} else if(p2.getDate() < dw.getDate()) {
				p1 = p2;
				p2 = p2.getNext();
			}
		}
		p1.setNext(dw);
		return dw;
	}
	
	public DateNidNode addDateWid(DateNidNode startNode, DateNidNode dw) {
		DateNidNode p1 = startNode;
		DateNidNode p2 = p1.getNext();
		while(null != p2) {
			if(p2.getDate() >= dw.getDate()) {
				p1.setNext(dw);
				dw.setNext(p2);
				return dw;
			} else if(p2.getDate() < dw.getDate()) {
				p1 = p2;
				p2 = p2.getNext();
			}
		}
		p1.setNext(dw);
		return dw;
	}
	
	public void formatDateWidList() {
		DateNidNode p = head.getNext();
		dateWidList = new ArrayList<>();
		while(null != p) {
			dateWidList.add(p);
			p = p.getNext();
		}
	}
	
	public int getMinDateSpan(int sDate, int p, ReachableQueryService rsSer) {
		int i = Collections.binarySearch(dateWidList, new DateNidNode(sDate, -1), comparator);
		int left = 0;
		int right = 0;
		DateNidNode tempNode = null;
		if(i >= 0) {
			tempNode = dateWidList.get(i);
			if(rsSer.queryReachable(p, tempNode.getNid())) {
				return 1;
			}
			left = i - 1;
			right = i + 1;
		} else {
			left = (-i) - 2;
			right = (-i) - 1;
		}
		int leftSpan = 0, rightSpan = 0;
		while(true) {
			if(left >= 0) {
				leftSpan = Math.abs(sDate - dateWidList.get(left).getDate()) + 1;
				if(dateWidList.size() > right) {
					rightSpan = Math.abs(sDate - dateWidList.get(right).getDate()) + 1;
				}
			} else {
				if(dateWidList.size() > right) {
					rightSpan = Math.abs(sDate - dateWidList.get(right).getDate()) + 1;
				} else return -1;
			}
			if(left >= 0 && rsSer.queryReachable(p, dateWidList.get(left).getNid())) {
				if(dateWidList.size() > right && rsSer.queryReachable(p, dateWidList.get(right).getNid())) {
					return leftSpan<rightSpan?leftSpan:rightSpan;
				} else {
					return leftSpan;
				}
			} else {
				if(dateWidList.size() > right && rsSer.queryReachable(p, dateWidList.get(right).getNid())) {
					return rightSpan;
				} else {
					left--;
					right++;
				}
			}
		}
		
	}
}
