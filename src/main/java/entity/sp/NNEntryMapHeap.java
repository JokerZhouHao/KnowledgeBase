package entity.sp;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import spatialindex.rtree.NNEntry;

public class NNEntryMapHeap{
	private TreeMap<Double, LinkedList<NNEntry>> tm = new TreeMap<>();
	private LinkedList<NNEntry> tList = null;
	private int size = 0;
	
	public NNEntry poll() {
		if(!tm.isEmpty()) {
			size--;
			tList = tm.firstEntry().getValue();
			if(tList.size()==1) {
				NNEntry ne = tList.getFirst();
				tList.clear();
				tm.pollFirstEntry();
				return ne;
			} else {
				return tList.poll();
			}
		} else return null;
	}
	
	public void put(NNEntry ne) {
		size++;
		if(null == (tList=tm.get(ne.m_minDist))) {
			tList = new LinkedList<>();
			tList.add(ne);
			tm.put(ne.m_minDist, tList);
		} else {
			tList.add(ne);
		}
	}
	
	public int size() {
		return size;
	}
}
