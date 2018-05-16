package entity.sp;

/**
 * @author zhou
 * @since 2018/5/12
 */
public class DateNidNode {
	private int date = 0;
	private int nid = 0;
	
	public DateNidNode() {}
	
	public DateNidNode(int date, int nid) {
		this.date = date;
		this.nid = nid;
	}
	
	public int compareDate(DateNidNode dn) {
		if(date > dn.date)	return 1;
		else if(date == dn.date)	return 0;
		else return -1;
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

}
