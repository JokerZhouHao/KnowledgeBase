package entity.sp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.analysis.CharArrayMap.EntrySet;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import entity.sp.SortedList;

import entity.sp.NidToDateWidIndex.DateWid;
import utility.Global;
import utility.TimeUtility;

/**
 * 记录word's palce neighborhood
 * @author Monica
 *
 */
public class WordRadiusNeighborhood {
	
	private HashMap<Integer, ArrayList<Integer>>[] eachLayerWN = null;
	private Integer radius = Global.radius;
	
	public WordRadiusNeighborhood() {
	}
	
	public WordRadiusNeighborhood(int radius, String placeNeigh) {
		this.radius = radius;
		eachLayerWN = new HashMap[this.radius  + 1];
		this.formate(placeNeigh);
	}
	
	public WordRadiusNeighborhood(int radius, byte[] binPlaceNeigh) {
		this.radius = radius;
		eachLayerWN = new HashMap[this.radius  + 1];
		this.formateBin(binPlaceNeigh);
	}
	
	
	/**
	 * 格式化
	 * @param placeNeigh
	 */
	private void formate(String placeNeigh) {
		String[] layers = null;
		String[] pIdDates = null;
		String[] dates = null;
		int i, j, pid;
		ArrayList<Integer> tempList = null;
		
		layers = placeNeigh.split(Global.delimiterLayer);
		if(layers.length != this.radius+1)	return;
		
		for(i = 0; i<layers.length; i++) {
			if(!layers[i].equals(Global.signEmptyLayer)) {
				if(null == eachLayerWN[i]) {
					eachLayerWN[i] = new HashMap<>();
				}
				pIdDates = layers[i].split(Global.delimiterSpace);
				for(String st : pIdDates) {
					j = st.indexOf(Global.delimiterLevel2);
					pid = Integer.parseInt(st.substring(0, j));
					
					dates = st.substring(j + 1).split(Global.delimiterDate);
					tempList = new ArrayList<>();
					for(String st1 : dates) {
						tempList.add(Integer.parseInt(st1));
					}
					eachLayerWN[i].put(pid, tempList);
				}
			}
		}
	}
	
	/**
	 * 格式化二进制格式的PN
	 * @param binPlaceNeigh
	 */
	private void formateBin(byte[] binPlaceNeigh) {
		ArrayList<Integer> tempList = null;
		int numWD = 0;
		int i, j, pid, k;
		int numDates = 0;
		ByteBuffer bb = ByteBuffer.wrap(binPlaceNeigh);
		bb.rewind();
		for(i=0; i<this.radius+1; i++) {
			numWD = bb.getInt();
			if(0==numWD) 	continue;
			eachLayerWN[i] = new HashMap<>();
			for(j=0; j<numWD; j++) {
				pid = bb.getInt();
				numDates = bb.getInt();
				tempList = new ArrayList<>();
				for(k=0; k<numDates; k++) {
					tempList.add(bb.getInt());
				}
				eachLayerWN[i].put(pid, tempList);
			}
		}
	}
	
	/**
	 * 获得节点id为pid的looseness
	 * @param pid
	 * @param date
	 * @return
	 */
	public int getLooseness(int pid, int date) {
		int minLoose = Integer.MAX_VALUE;
		int td = 0;
		for(int i=0; i < radius + 1; i++) {
			if(null != eachLayerWN[i] && null != eachLayerWN[i].get(pid) && minLoose > (td = ((i+1)*TimeUtility.getMinDateSpan(date, eachLayerWN[i].get(pid))))) {
				minLoose = td;
			}
		}
		return minLoose;
	}
	
	/**
	 * 获得时间范围内的looseness
	 * @param pid
	 * @param sDate
	 * @param eDate
	 * @return
	 */
	public int getLooseness(int pid, int sDate, int eDate) {
		List<Integer> dates = null;
		for(int i=0; i < radius + 1; i++) {
			if(null != eachLayerWN[i] && null != (dates=eachLayerWN[i].get(pid))) {
				if(dates.get(dates.size()-1)<sDate || dates.get(0)>eDate)	continue;
				return i+1;
			}
		}
		return radius+2;
	}
	
	/**
	 * 清空释放内存
	 */
	public void clear() {
		for(HashMap<Integer, ArrayList<Integer>> hm : eachLayerWN) {
			if(null != hm) {
				for(Entry<Integer, ArrayList<Integer>> en : hm.entrySet()) {
					if(en.getValue() != null)	en.getValue().clear();
				}
				hm.clear();
			}
		}
	}
	
	public HashMap<Integer, ArrayList<Integer>>[] getEachLayerWN() {
		return eachLayerWN;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(HashMap<Integer, ArrayList<Integer>> hm : eachLayerWN) {
			if(hm == null) {
				sb.append(Global.signEmptyLayer);
			} else {
				for(Entry<Integer, ArrayList<Integer>> en : hm.entrySet()) {
					sb.append(String.valueOf(en.getKey()) + Global.delimiterLevel2);
					for(Integer in : en.getValue()) {
						sb.append(String.valueOf(in + Global.delimiterDate));
					}
					sb.append(Global.delimiterSpace);
				}
			}
			sb.append(Global.delimiterLayer);
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		String st = "2,12@13@ -4,12@13@ L0,12@13@ -3,12@13@ L1,12@13@ L2,17612@ -4,17612@ -5,12@17612@ 6,12@17612@ L";
		WordRadiusNeighborhood wrn = new WordRadiusNeighborhood(Global.radius, st);
		System.out.println(st + "\n" + wrn);
	}
	
}
