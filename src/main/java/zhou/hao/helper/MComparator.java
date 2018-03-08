package zhou.hao.helper;

import java.util.Comparator;

/**
 * 
 * @author Monica
 * @param <T>
 * @since 2018/3/8
 * 功能：自定义比较器
 */
public class MComparator<T> implements Comparator<T> {
	
	public int compare(T t1, T t2) {
		if(t1 instanceof Integer) {
			int i1 = (int)t1, i2 = (int)t2;
			if(i1 > i2)	return 1;
			else if (i1 == i2)	return 0;
			else return -1;
		} else if(t1 instanceof String) {
			return ((String)t1).compareTo((String)t2);
		}
		return 0;
	}
}
