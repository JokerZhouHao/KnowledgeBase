package entity.sp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import entity.sp.SortedList;

import entity.sp.NidToDateWidIndex.DateWid;

/**
 * 记录place's radius word neighborhood
 * @author Monica
 *
 */
public class PlaceRadiusNeighborhood {
	private HashMap<Integer, Integer> firstAppearWordMap = null;
	
	private HashMap<Integer, SortedList>[] eachLayerWN = null;
	
	public PlaceRadiusNeighborhood(int radius) {
		eachLayerWN = new HashMap[radius + 1];
		firstAppearWordMap = new HashMap<>();
	}
	
	/**
	 * 添加第layer层的dates和wordIds
	 * @param layer
	 * @param dateWid
	 */
	public void addDateWid(int layer, DateWid dateWid) {
		Integer i = null;
		SortedList tempList = null;
		SortedList dateList = null;;
		SortedList dateList1 = dateWid.getDateList();
		HashMap<Integer, SortedList> tempMap = null;
		for(int wid : dateWid.getWidList()) {
			dateList = dateList1.copy();		// 因为SortedList为引用对象，所以采用复制方式
			if(null == (i = firstAppearWordMap.get(wid))) {
				if(null == (tempMap = eachLayerWN[layer]))
					tempMap = eachLayerWN[layer] = new HashMap<>();
				tempMap.put(wid, dateList);
				firstAppearWordMap.put(wid, layer);
			} else {
				// 处理在i--layer层出现的wid
				for(; i < layer; i++) {
					if(null != (tempMap = eachLayerWN[i]) && null != (tempList = tempMap.get(wid))) {
						if(null == dateList.removeIntersection(tempList)) {
							// 当前添加的wid的dates在第layer层上面几层全部出现过
							break;
						}
					}
				}
				if(0 != dateList.getSize()) {
					// 处理剩下的wid中没在第layer层上面几层出现过的dates
					if(null == (tempMap = eachLayerWN[layer])) {
						tempMap = eachLayerWN[layer] = new HashMap<>();
					}
					if(null == (tempList = tempMap.get(wid))) {
						tempMap.put(wid, dateList);
					} else {
						tempList.merge(dateList);
					}
				}
			}
		}
	}
	
	/**
	 * 合并radiusWN
	 * @param radiusWN
	 */
	public void merge(PlaceRadiusNeighborhood radiusWN) {
		int size = eachLayerWN.length;
		HashMap<Integer, SortedList> tempMap1 = null;
		HashMap<Integer, SortedList> tempMap2 = null;
		Integer fLayer = null;
		SortedList tempList = null;
		for(int curLayer = 0; curLayer < size; curLayer++) {
			// 合并第curLayer的wid
			// 判断当前类的第curLayer层是否为空
			if(null == (tempMap1 = eachLayerWN[curLayer])) {
				// 判断radiusWN的第curLayer层是否为空
				if(null != (tempMap2 = radiusWN.eachLayerWN[curLayer])) {
					tempMap1 = eachLayerWN[curLayer] = new HashMap<>();
					tempMap1.putAll(tempMap2);
				} else
					continue;
			} else {
				if(null != (tempMap2 = radiusWN.eachLayerWN[curLayer])) {
					for(Entry<Integer, SortedList> en : tempMap1.entrySet()) {
						if(en.getValue().merge(tempMap2.get(en.getKey()))) {
							// tempMap2有该wid，移除wid
							tempMap2.remove(en.getKey());
						}
					}
					tempMap1.putAll(tempMap2);
				}
			}
			
			// 移除在curLayer层之前出现的date
			ArrayList<Integer> recordList = new ArrayList<>();
			for(Entry<Integer, SortedList> en : tempMap1.entrySet()) {
				if(null == (fLayer = firstAppearWordMap.get(en.getKey()))) {
					firstAppearWordMap.put(en.getKey(), curLayer);
				} else {
					if(fLayer < curLayer) {
						for(; fLayer < curLayer; fLayer++) {
							if(null != (tempMap2 = eachLayerWN[fLayer]) && null != (tempList = tempMap2.get(en.getKey()))) {
								if(null == en.getValue().removeIntersection(tempList)) break;
							}
						}
						if(0 == en.getValue().getSize())	recordList.add(en.getKey());
					} else if (fLayer > curLayer) {
						firstAppearWordMap.put(en.getKey(), curLayer);
					}
				}
			}
			for(Integer in : recordList) {
				tempMap1.remove(in);
			}
			if(0 == tempMap1.size()) {
				eachLayerWN[curLayer] = null;
			}
		}
	}
	
	public void clear() {
		for(int i=0; i<eachLayerWN.length; i++) {
			if(null != eachLayerWN[i]) {
				eachLayerWN[i].clear();
				eachLayerWN[i] = null;
			}
		}
	}
	
	public HashMap<Integer, SortedList>[] getEachLayerWN() {
		return eachLayerWN;
	}
	
}
