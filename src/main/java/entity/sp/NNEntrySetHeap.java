package entity.sp;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import spatialindex.rtree.NNEntry;

public class NNEntrySetHeap{
	class NNEntryListComporator implements Comparator{
		public int compare(Object o1, Object o2)
		{
			LinkedList<NNEntry> n1 = (LinkedList) o1;
			LinkedList<NNEntry> n2 = (LinkedList) o2;

			if (n1.getFirst().m_minDist < n2.getFirst().m_minDist) return -1;
			if (n1.getFirst().m_minDist > n2.getFirst().m_minDist) return 1;
			return 0;
		}
	}
	
	
	private TreeSet<LinkedList<NNEntry>> ts = new TreeSet<LinkedList<NNEntry>>(new NNEntryListComporator());
	private LinkedList<NNEntry> tList = null;
	private LinkedList<NNEntry> lis = null;
	
	public NNEntrySetHeap() {
		lis = new LinkedList<>();
		lis.add(null);
	}
	
	private int size = 0;
	
	public NNEntry poll() {
		if(!ts.isEmpty()) {
			size--;
			tList = ts.first();
			if(tList.size()==1) {
				NNEntry ne = tList.getFirst();
				tList.clear();
				ts.pollFirst();
				return ne;
			} else {
				return tList.poll();
			}
		} else return null;
	}
	
//	public void put(NNEntry ne) {
//		size++;
//		lis.set(0, ne);
//		if(null == (tList=ts.)) {
//			tList = new LinkedList<>();
//			tList.add(ne);
//			tm.put(ne.m_minDist, tList);
//		} else {
//			tList.add(ne);
//		}
//	}
	
	public int size() {
		return size;
	}
}
