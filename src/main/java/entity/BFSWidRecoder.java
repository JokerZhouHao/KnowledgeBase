package entity;

import java.util.List;

import org.omg.PortableInterceptor.INACTIVE;

import entity.sp.AllPidWid;
import utility.Global;

/**
 * 记录BFS访问过的Wid
 * @author Monica
 *
 */    
public class BFSWidRecoder {
	
	private Boolean[] noAccessedNids = new Boolean[Global.numKeywords];
	private int noAccessedNidNum = 0;
	
	private BFSWidRecoder() {}
	
	public BFSWidRecoder(List<Integer> allWids) {
		noAccessedNidNum = allWids.size();
		for(int i=0; i<noAccessedNids.length; i++) {
			noAccessedNids[i] = Boolean.FALSE;
		}
		for(int wid : allWids) {
			noAccessedNids[wid-Global.numNodes] = Boolean.TRUE;
		}
	}
	
	public BFSWidRecoder copy() {
		BFSWidRecoder rec = new BFSWidRecoder();
		for(int i=0; i<noAccessedNids.length; i++) {
			rec.noAccessedNids[i] = noAccessedNids[i];
		}
		rec.noAccessedNidNum = noAccessedNidNum;
		return rec;
	}
	
	public Boolean accessOver(int wid) {
		if(noAccessedNids[wid-Global.numNodes]) {
			noAccessedNids[wid-Global.numNodes] = Boolean.FALSE;
			noAccessedNidNum--;
			if(0==noAccessedNidNum)	return Boolean.TRUE;
			else return Boolean.FALSE;
		} else return Boolean.FALSE;
	}
	
	public Boolean isOver() {
		if(0==noAccessedNidNum)	return Boolean.TRUE;
		else return Boolean.FALSE;
	}
	
	public static void main(String[] args) {
		List<Integer> allWids = AllPidWid.getAllWid();
		System.out.println(allWids.get(allWids.size()));
//		
//		Boolean[] bs = new Boolean[1000];
//		for(boolean bo : bs) {
//			if(bo) {
//				System.out.println("over");
//				break;
//			}
//		}
//		BFSWidRecoder rec = new BFSWidRecoder(AllPidWid.getAllWid());
//		System.out.println(rec.accessOver(8099960));
	}
}
