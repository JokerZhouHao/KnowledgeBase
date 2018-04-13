package entity.freebase;

import java.util.ArrayList;

/**
 * 
 * @author Monica
 * @since 2017/12/07
 * 功能：记录sourceNode到个目标node的距离
 */
public class PathDistanceList {
	private int sourceNodeId = -1;
	private ArrayList<Pair<Integer, Integer>> distanceList = null;
	
	public PathDistanceList(int initSize, int sour) {
		this.sourceNodeId = sour;
		distanceList = new ArrayList<Pair<Integer, Integer>>();
		for(int i=0; i<initSize; i++) {
			distanceList.add(null);
		}
	}
	
	public int getSourceNodeId() {
		return sourceNodeId;
	}

	public void setSourceNodeId(int sourceNodeId) {
		this.sourceNodeId = sourceNodeId;
	}

	public ArrayList<Pair<Integer, Integer>> getDistanceList() {
		return distanceList;
	}

	public void setDistanceList(ArrayList<Pair<Integer, Integer>> distanceList) {
		this.distanceList = distanceList;
	}

	// 比较两个list是否相同
	public Boolean equal(PathDistanceList pl) {
		if(this.sourceNodeId==pl.getSourceNodeId()) {
			int size = this.distanceList.size();
			if(size==pl.getDistanceList().size()) {
				ArrayList<Pair<Integer, Integer>> plList = pl.getDistanceList();
				int i = 0;
				for(i=0; i<size; i++) {
					if(distanceList.get(i).getKey()!=plList.get(i).getKey() || distanceList.get(i).getValue()==plList.get(i).getValue()) break;
				}
				if(i==size)	return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
	
	//	设置distanceList中的节点
	public void setDistanceNode(int index, Pair<Integer, Integer> disPair) {
		distanceList.set(index, disPair);
	}
	
	// 获得总距离
	public int sumDistance() {
		int sum = 0;
		for(Pair<Integer, Integer> pa : distanceList)	sum += pa.getValue();
		return sum;
	}
	
	// 销毁
	public void destory() {
		distanceList.clear();
		distanceList = null;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		StringBuffer sb1 = new StringBuffer();
		int sum = 0;
		sb.append("sourceNodeId : ");
		sb.append(sourceNodeId);
		sb.append(" sumDistance = ");
		
		sb1.append(" (targetNodeId, distance)-->");
		for(Pair<Integer, Integer> pa : distanceList) {
			sum += pa.getValue();
			sb1.append(" (");
			sb1.append(pa.getKey());
			sb1.append(',');
			sb1.append(pa.getValue());
			sb1.append(')');
		}
		sb.append(sum);
		sb.append(sb1.toString());
		return sb.toString();
	}
	
	
}
