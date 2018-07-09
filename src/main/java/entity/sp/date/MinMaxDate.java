package entity.sp.date;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 记录节点的时间范围
 * @author Monica
 * @since 2018/7/4
 */
public class MinMaxDate {
    public static final Comparator<MinMaxDate> MIN_ENDPOINT_ORDER  = new MinEndpointComparator();

    public static final Comparator<MinMaxDate> MAX_ENDPOINT_ORDER = new MaxEndpointComparator();

    public static final Comparator<MinMaxDate> LENGTH_ORDER = new LengthComparator();
    
    private int nid;
    private int min;
    private int max;

    public MinMaxDate(int nid, int min, int max) {
    	this.nid = nid;
    	this.min = min;
    	this.max = max;
    }
    
    public int getNid() {
    	return nid;
    }
    
    public int min() {
    	return min;
    }
    
    public int max() {
    	return max;
    }
    
    // 交集
    public boolean intersects(MinMaxDate that) {
        if (this.max < that.min) return false;
        if (that.max < this.min) return false;
        return true;
    }

    // 包含
    public boolean contains(int x) {
        return (min <= x) && (x <= max);
    }
    
    // 时间差
    public int span() {
        return max - min;
    }

    /**
     * Returns a string representation of this interval.
     *
     * @return a string representation of this interval in the form [min, max]
     */
    public String toString() {
        return "[" +  nid + ", "+ min + ", " + max + "]";
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
        MinMaxDate that = (MinMaxDate) other;
        return this.min == that.min && this.max == that.max;
    }

    /**
     * Returns an integer hash code for this interval.
     *
     * @return an integer hash code for this interval
     */
    public int hashCode() {
        int hash1 = ((Integer) min).hashCode();
        int hash2 = ((Integer) max).hashCode();
        return 31*hash1 + hash2;
    }

    // ascending order of min endpoint, breaking ties by max endpoint
    private static class MinEndpointComparator implements Comparator<MinMaxDate> {
        public int compare(MinMaxDate a, MinMaxDate b) {
            if      (a.min < b.min) return -1;
            else if (a.min > b.min) return +1;
            else                    return  0;
        }
    }

    // ascending order of max endpoint, breaking ties by min endpoint
    private static class MaxEndpointComparator implements Comparator<MinMaxDate> {
        public int compare(MinMaxDate a, MinMaxDate b) {
            if      (a.max < b.max) return -1;
            else if (a.max > b.max) return +1;
            else                    return  0;
        }
    }

    // ascending order of length
    private static class LengthComparator implements Comparator<MinMaxDate> {
        public int compare(MinMaxDate a, MinMaxDate b) {
            double alen = a.span();
            double blen = b.span();
            if      (alen < blen) return -1;
            else if (alen > blen) return +1;
            else                  return  0;
        }
    }
    
    public static void main(String[] args) {
    	List<MinMaxDate> arr = new ArrayList<>();
    	arr.add(new MinMaxDate(0, 0, 10));
    	arr.add(new MinMaxDate(1, 0, 10));
    	arr.add(new MinMaxDate(2, 2, 8));
    	arr.add(new MinMaxDate(3, 3, 9));
    	arr.add(new MinMaxDate(4, 3, 9));
    	arr.add(new MinMaxDate(5, 2, 8));
    	arr.add(new MinMaxDate(6, 2, 8));
//    	Arrays.sort(arr, MinMaxDate.MIN_ENDPOINT_ORDER);
//    	Arrays.sort(arr, MinMaxDate.MAX_ENDPOINT_ORDER);
    	arr.sort(MinMaxDate.MIN_ENDPOINT_ORDER);
    	for(MinMaxDate mmd : arr) {
    		System.out.println(mmd);
    	}
    	System.out.println(Collections.binarySearch(arr, new MinMaxDate(1, 2, 10), MinMaxDate.MIN_ENDPOINT_ORDER));
    }
}
