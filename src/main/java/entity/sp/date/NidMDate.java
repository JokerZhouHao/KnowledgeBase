package entity.sp.date;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 记录节点的最大或最小时间
 * @author Monica
 * @since 2018/7/4
 */
public class NidMDate {
    public static final Comparator<NidMDate> MDATE_ENDPOINT_ORDER  = new MDateComparator();

    private int nid;
    private int mDate;

    public NidMDate(int nid, int mDate) {
    	this.nid = nid;
    	this.mDate = mDate;
    }
    
    public int getNid() {
    	return nid;
    }
    
    public int mDate() {
    	return mDate;
    }
    
    /**
     * Returns a string representation of this interval.
     *
     * @return a string representation of this interval in the form [min, max]
     */
    public String toString() {
        return "[" +  nid + ", "+ mDate + "]";
    }

    /**
     * Compares this transaction to the specified object.
     *
     * @param  other the other interval
     * @return {@code true} if this interval equals the other interval;
     *         {@code false} otherwise
     */
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != this.getClass()) return false;
        NidMDate that = (NidMDate) other;
        return this.mDate == that.mDate;
    }

    /**
     * Returns an integer hash code for this interval.
     *
     * @return an integer hash code for this interval
     */
    public int hashCode() {
    	int hash1 = ((Integer) nid).hashCode();
        int hash2 = ((Integer) mDate).hashCode();
        return 31*hash1 + hash2;
    }

    // ascending order of min endpoint, breaking ties by max endpoint
    private static class MDateComparator implements Comparator<NidMDate> {
        public int compare(NidMDate a, NidMDate b) {
            if      (a.mDate < b.mDate) return -1;
            else if (a.mDate > b.mDate) return +1;
            else                    return  0;
        }
    }

    public static void main(String[] args) {
    	NidMDate arr[] = new NidMDate[4];
    	arr[0] = new NidMDate(0, 0);
    	arr[1] = new NidMDate(1, 1);
    	arr[2] = new NidMDate(2, 2);
    	arr[3] = new NidMDate(3, 1);
    	
//    	Arrays.sort(arr, MinMaxDate.MIN_ENDPOINT_ORDER);
    	Arrays.sort(arr, NidMDate.MDATE_ENDPOINT_ORDER);
    	for(NidMDate mmd : arr) {
    		System.out.println(mmd);
    	}
    }
}
