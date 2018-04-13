package entity.freebase;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class IndividualBlockingQueue<T> {
	private ArrayList<T> list = null;
	private volatile int head = 0;
	private volatile int tail = 0;
	
	public IndividualBlockingQueue(int cap, String sort) {
		list = new ArrayList<T>();
		for(int i=0; i<cap+1; i++) {
			if(sort.contains("String"))
				this.list.add((T)new Pair<Integer, String>(i, ""));
			else if(sort.contains("Integer"))
				this.list.add((T)new Pair<Integer, Integer>(i, 0));
		}
	}
	
	public Boolean isEmpty() {
		if(head==tail)
			return Boolean.TRUE;
		else return Boolean.FALSE;
	}
	
	public Boolean isFull() {
		if(tail+1==head || (tail==list.size()-1 && 0==head))
			return Boolean.TRUE;
		else return Boolean.FALSE;
	}
	
	public void put(T t) {
		int i = 2;
		while(this.isFull()) {
			i = i*2;
//			try {
//				Thread.sleep(0, 5);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
//		synchronized (list.get(tail)) {
			list.set(tail, t);
			tail++;
			if(tail==list.size())	tail = 0;
//		}
	}
	
	public T take() {
		int i = 2;
		while(this.isEmpty()) {
			i = i*2;
//			try {
// 				Thread.sleep(0, 5);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
//		synchronized (list.get(head)) {
			T t = list.get(head);
			head++;
			if(head==list.size())	head = 0;
			return t;
//		}
	}
	
	public T indexOf(int i) {
		return this.list.get(i);
	}
	
	public String getHeadAndTail() {
		return String.valueOf(this.head) + " " + String.valueOf(this.tail);
	}
}
