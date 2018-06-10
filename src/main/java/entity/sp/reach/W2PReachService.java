package entity.sp.reach;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import utility.Global;

/**
 * wid到pid的可达性服务
 * @author Monica
 * @since 2018/6/8
 */
public class W2PReachService {
	private Map<Integer, Integer> widToBlock = null;
	private W2PIndex[] indexs = null;
	private String basePath = null;
	
	public W2PReachService(String basePath) {
		this.basePath = basePath;
		init();
	}
	
	public void init() {
		int start = 0, end = 0, i =0, j = 0;
		int span = W2PIndex.widSpan;
		int blockNum = W2PIndex.W2PIndexNum;
		widToBlock = new HashMap();
		String indexPath = null;
		indexs = new W2PIndex[blockNum];
		List<Integer> wids = W2PIndex.allWids;
		for(i=0; i<blockNum; i++) {
			start = end;
			end += span;
			if(end > W2PIndex.allWids.size()) {
				end = W2PIndex.allWids.size();
			}
			indexPath = this.basePath + String.valueOf(blockNum) + "_" + String.valueOf(i);
			indexs[i] = new W2PIndex(indexPath);
			for(j=start; j<end; j++) {
				widToBlock.put(wids.get(j), i);
			}
		}
	}
	
	public void openIndexs() {
		for(W2PIndex ind : indexs) {
			ind.openIndexReader();
		}
	}
	
	public Set<Integer> getPids(int wid){
		Integer blockIndex = null;
		if(null != (blockIndex = widToBlock.get(wid))) {
			return indexs[blockIndex].getPids(wid);
		}
		return null;
	}
	
	public void closeIndexs() {
		for(W2PIndex ind : indexs) {
			ind.closeIndexReader();
		}
	}
	
	public static void main(String[] args) {
		W2PReachService ser = new W2PReachService(Global.indexWid2PidBase);
		ser.openIndexs();
		int wid = 0;
		Scanner scan = new Scanner(System.in);
		Set<Integer> pids = null;
		while(true) {
			System.out.print("> 请输入wid : ");
			wid = Integer.parseInt(scan.nextLine());
			if(-1==wid) break;
			pids = ser.getPids(wid);
			int k = 5;
			for(int in : pids) {
				if(0 != k) {
					k--;
					System.out.print(in + " ");
				}
				if(in < 0) {
					System.out.print(in + " ");
				}
			}
			System.out.println();
		}
		ser.closeIndexs();
	}
	
}
