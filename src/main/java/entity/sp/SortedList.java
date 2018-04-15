package entity.sp;

/**
 * 有序放置int类型数据列表
 * @author Monica
 *
 */
public class SortedList {
	
	/**
	 * 节点
	 * @author Monica
	 *
	 */
	public class SortedListNode{
		private int value;
		private SortedListNode next = null;
		
		public SortedListNode(int v) {
			this.value = v;
		}

		public int getValue() {
			return value;
		}

		public SortedListNode getNext() {
			return next;
		}
	}
	
	private SortedListNode head = null;
	private int size = 0;
	
	public SortedListNode add(int value) {
		if(null == head) {
			head = new SortedListNode(value);
			size++;
			return head;
		}
		SortedListNode p = head;
		while(null != p.next) {
			p = p.next;
		}
		p.next = new SortedListNode(value);
		size++;
		return p.next;
	}
	
	public SortedListNode add(SortedListNode tail, int value) {
		tail.next = new SortedListNode(value);
		size++;
		return tail.next;
	}
	
	/**
	 * 移除dealedSdl与类中相交的节点，并返回dealedSdl
	 * @param dealedSdl
	 * @return
	 */
	public SortedList removeIntersection(SortedList sdl) {
		SortedListNode p1 = new SortedListNode(-1);
		SortedListNode p2 = head;
		p1.next = p2;
		
		SortedListNode dp1 = sdl.head;
		
		while(null != dp1) {
			if(p2.value == dp1.value) {
				if(p2 == head)	head = p2.next;
				p1.next = p2.next;
				p2 = p2.next;
				if(0 == (--size))	return null;
				if(null == p2)	break;
				
				dp1 = dp1.next;
			} else if(p2.value < dp1.value) {
				p1 = p2;
				p2 = p2.next;
				if(null == p2)	break;
			} else {
				dp1 = dp1.next;
			}
		}
		return this;
	}
	
	/**
	 * 合并SortedList
	 * @param sList
	 */
	public boolean merge(SortedList sList) {
		if(null == sList)	return Boolean.FALSE;
		SortedListNode p1 = new SortedListNode(-1);
		SortedListNode p2 = head;
		p1.next = p2;
		
		SortedListNode sp1 = sList.head;
		SortedListNode tp = null;
		
		while(null != p2) {
			if(sp1.value < p2.value) {
				tp = sp1.next;
				p1.next = sp1;
				p1 = sp1;
				p1.next = p2;
				if(head == p2)	head = sp1;
				sp1 = tp;
				if(null == sp1)	break;
			} else if(sp1.value > p2.value) {
				p1 = p2;
				p2 = p2.next;
			} else {
				if(null == (sp1 = sp1.next))	break;
				p1 = p2;
				p2 = p2.next;
			}
		}
		return Boolean.TRUE;
	}
	
	public SortedListNode getHead() {
		return head;
	}

	public int getSize() {
		return size;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		SortedListNode dn = head;
		if(null == dn)	return "empty ！";
		while(null != dn) {
			sb.append(String.valueOf(dn.value));
			sb.append(' ');
			dn = dn.next;
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		SortedList sdl1 = new SortedList();
		SortedList sdl2 = new SortedList();
		SortedListNode dn = sdl1.add(3);
		dn = sdl1.add(dn, 4);
		dn = sdl1.add(dn, 9);
		dn = sdl1.add(dn, 13);
//		System.out.println(sdl1);
		
		dn = sdl2.add(10);
		dn = sdl2.add(dn, 12);
//		dn = sdl2.add(dn, 4);
//		dn = sdl2.add(dn, 5);
//		dn = sdl2.add(dn, 9);
//		dn = sdl2.add(dn, 10);
//		dn = sdl2.add(dn, 13);
//		sdl1.removeIntersection(sdl2);
		sdl1.removeIntersection(sdl2);
		System.out.println(sdl1);
	}
	
}
