package entity.sp.date;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import utility.Utility;

import java.util.Set;

/**
 * 
 * @author Monica
 * @since 2018/6/1
 * 功能：表示可达时间
 */
abstract class ReachDate {
	protected String filePath = null;
	
	public void writeAllTimes(DataOutputStream dos, Map<Integer, List<Integer>> allTimes) throws Exception{
		Map<Integer, List<Integer>> tempM = new HashMap<>();
		for(Entry<Integer, List<Integer>> en : allTimes.entrySet()) {
			if(en.getValue().get(0) == -1) {
				tempM.put(en.getKey(), en.getValue());
			} else {
				dos.writeInt(en.getKey());
				dos.writeInt(en.getValue().size());
				for(int in : en.getValue()) {
					dos.writeInt(in);
				}
			}
		}
		if(!tempM.isEmpty()) {
			for(Entry<Integer, List<Integer>> en : tempM.entrySet()) {
				dos.writeInt(en.getKey());
				for(int in : en.getValue()) {
					dos.writeInt(in);
				}
			}
		}
		
		tempM.clear();
		dos.writeInt(Integer.MAX_VALUE);
	}
	
	public void writeAllSetTimes(DataOutputStream dos, Map<Integer, Set<Integer>> allTimes) throws Exception{
//		Map<Integer, List<Integer>> tempM = new HashMap<>();
//		Boolean hasEqual = Boolean.FALSE;
//		int tIn = -1;
//		Set<Integer> tSet = null;
//		List<Integer> tLi = null;
		for(Entry<Integer, Set<Integer>> en : allTimes.entrySet()) {
//			tIn = en.getKey();
//			tSet = en.getValue();
//			hasEqual = Boolean.FALSE;
//			for(Entry<Integer, Set<Integer>> en1 : allTimes.entrySet()) {
//				if(tIn == en1.getKey())	break;
//				if(Utility.isEqualSet(tSet, en1.getValue())) {
//					tLi = new ArrayList<>();
//					tLi.add(-1);
//					tLi.add(en1.getKey());
//					tempM.put(tIn, tLi);
//					hasEqual = Boolean.TRUE;
//					break;
//				}
//			}
//			if(hasEqual)	continue;
			
			dos.writeInt(en.getKey());
			dos.writeInt(en.getValue().size());
			for(int in : en.getValue()) {
				dos.writeInt(in);
			}
		}
//		if(!tempM.isEmpty()) {
//			for(Entry<Integer, List<Integer>> en : tempM.entrySet()) {
//				dos.writeInt(en.getKey());
//				for(int in : en.getValue()) {
//					dos.writeInt(in);
//				}
//			}
//		}
		
//		tempM.clear();
	}
	
	public void writeWDates(DataOutputStream dos, Map<Integer, DateArr> widDates) throws Exception{
		int n = 0;
		int startN = 0;
		boolean[] rec = null;
		for(Entry<Integer, DateArr> en : widDates.entrySet()) {
			dos.writeInt(en.getKey());
			rec = en.getValue().data();
			for(boolean bo : rec) {
				if(bo)	n++;
			}
			dos.writeInt(n);
			startN = en.getValue().startN();
			for(n=0; n < rec.length; n++) {
				if(rec[n])	dos.writeInt(startN + n);
			}
		}
	}
	
	public Map<Integer, List<Integer>> loadAllTimes(DataInputStream dis) throws Exception{
		Map<Integer, List<Integer>> allTimes = new HashMap<>();
		int key = 0, i = 0, size = 0;
		while(true) {
			key = dis.readInt();
			if(key == Integer.MAX_VALUE)	break;
			
			size = dis.readInt();
			if(-1 == size) {
				allTimes.put(key, allTimes.get(dis.readInt()));
			} else {
				ArrayList<Integer> li = new ArrayList<>();
				for(i=0; i<size; i++) {
					li.add(dis.readInt());
				}
				allTimes.put(key, li);
			}
		}
		return allTimes;
	}
	
	public abstract void writeToFile();
	
	public abstract void loadFromFile();
	
}
