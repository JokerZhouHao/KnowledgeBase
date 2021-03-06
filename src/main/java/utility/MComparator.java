package utility;

import java.util.Comparator;

import entity.sp.DateNidNode;
import precomputation.sp.IndexNidKeywordsListService;
import statistics.ReachTime;

/**
 * 
 * @author Monica
 * @param <T>
 * @since 2018/3/8
 * 功能：自定义比较器
 */
public class MComparator<T> implements Comparator<T> {
	
	public int compare(T t1, T t2) {
		if(t1 instanceof DateNidNode) {
			int i1 = ((DateNidNode)t1).getDate();
			int i2 = ((DateNidNode)t2).getDate();
			if(i1 > i2)	return 1;
			else if (i1 == i2)	return 0;
			else return -1;
		} else if(t1 instanceof IndexNidKeywordsListService.NodeIdDate) {
			int i1 = ((IndexNidKeywordsListService.NodeIdDate)t1).getNodeId();
			int i2 = ((IndexNidKeywordsListService.NodeIdDate)t2).getNodeId();
			if(i1 > i2)	return 1;
			else if (i1 == i2)	return 0;
			else return -1;
		} else if(t1 instanceof ReachTime.ReachDoublePoint) {
			int i1 = ((ReachTime.ReachDoublePoint)t1).reachTime;
			int i2 = ((ReachTime.ReachDoublePoint)t2).reachTime;
			if(i1 > i2)	return 1;
			else if (i1 == i2)	return 0;
			else return -1;
		} else if(t1 instanceof Integer) {
			int i1 = (Integer)t1, i2 = (Integer)t2;
			if(i1 > i2)	return 1;
			else if (i1 == i2)	return 0;
			else return -1;
		} else if(t1 instanceof String) {
			return ((String)t1).compareTo((String)t2);
		}
		return 0;
	}
}
