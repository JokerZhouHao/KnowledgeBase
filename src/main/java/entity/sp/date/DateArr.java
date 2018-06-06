package entity.sp.date;

/**
 * 为了避免date排序花时，特地用数组来直接放date
 * @author Monica
 *
 */
public class DateArr {
	private boolean[] rec = null;
	private int start = -1;
	
	public DateArr(int[] minmax) {
		start = minmax[0];
		rec = new boolean[minmax[1] - minmax[0] + 1];
	}
	
	public boolean get(int date) {
		return rec[date - start];
	}
	
	public void set(int date) {
		rec[date - start] = true;
	}
	
	public void set(int[] dates) {
		for(int da : dates) {
			rec[da - start] = true;
		}
	}
	
	public int size() {
		int n = 0;
		for(boolean bo : rec) {
			if(bo)	n++;
		}
		return n;
	}
	
	public boolean[] data() {
		return rec;
	}
	
	public int startN() {
		return start;
	}
}
