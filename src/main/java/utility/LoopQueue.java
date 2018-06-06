package utility;

import java.util.ArrayList;

/**
 * 循环队列
 * @author Monica
 *
 * @param <T>
 * @since 2018/6/2
 */
public class LoopQueue<T> {
	private ArrayList<T> list = null;
	private int size = -1;
	private int head = 0;
	private int tail = 0;
	
	public int tempSize = 0;
	
	public LoopQueue(int size) {
		this.size = size;
		list = new ArrayList<>();
		for(int i=0; i<size; i++) {
			list.add(null);
		}
	}
	
	public void setAll(T t) {
		for(int i=0; i<size; i++) {
			list.set(i, t);
		}
	}
	
	public void reset() {
		head = tail = 0;
		tempSize = 0;
	}
	
	public Boolean push(T t) {
		if(tail==head-1 || (head==0 && tail==size-1)) {
			return Boolean.FALSE;
		}
		list.set(tail, t);
		if((++tail) == size)	tail = 0;
		
		tempSize++;
		
		return Boolean.TRUE;
	}
	
	public T poll() {
		if(head==tail)	return null;
		T t = list.get(head);
		if((++head) == size)	head = 0;
		
		tempSize--;
		
		return t;
	}
	
	public void display() {
		int h = head;
		int t = tail;
		while(h != t) {
			System.out.print(list.get(h) + " ");
			if((++h) == size)	h = 0;
		}
		System.out.println();
	}
	
	public int size() {
		return size;
	}
}
