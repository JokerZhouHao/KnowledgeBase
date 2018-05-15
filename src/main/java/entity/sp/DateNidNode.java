package entity.sp;

/**
 * @author zhou
 * @since 2018/5/12
 */
public class DateNidNode {
	private int date = 0;
	private int nid = 0;
	private DateNidNode next = null;
	
	public DateNidNode() {}
	
	public DateNidNode(int date, int nid) {
		this.date = date;
		this.nid = nid;
	}

	public int getDate() {
		return date;
	}

	public void setDate(int date) {
		this.date = date;
	}

	public int getNid() {
		return nid;
	}

	public void setNid(int nid) {
		this.nid = nid;
	}

	public DateNidNode getNext() {
		return next;
	}

	public void setNext(DateNidNode next) {
		this.next = next;
	}
}
