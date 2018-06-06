package entity.sp.date;

import java.util.ArrayList;
import java.util.List;

public class WidDates {
	public int wid = -1;
	public int dates[] = null;
	
	public WidDates(int wid, int num) {
		this.wid = wid;
		dates = new int[num];
	}
}
