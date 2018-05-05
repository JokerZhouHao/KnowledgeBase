package utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TimeUtility {
	
	// 一天的毫秒数
	public final static int totalMillOfOneDay = 86400000;
	
	public final static int zoomTimeOffset = 28800000; // 时区差
	
	public static String getTime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		return df.format(new Date());
	}
	
	public static String getSpendTimeStr(Long startTime, Long endTime) {
		Long spendTime = endTime - startTime;
		return  spendTime/1000/3600 + "h" + spendTime/1000%3600/60 + "m" + spendTime/1000%3600000%60 + "s";
	}
	
	// 计算两个日期之间的天数差，同一天返回1
	public static int calGapBetweenDate(Date d1, Date d2) {
		return (int)(Math.abs((d1.getTime()-d2.getTime())/86400000) + 1);
	}
	
	// 获得当前年月日
	public static Date getNowDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return sdf.parse(sdf.format(new Date()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// 格式化时间串
	public static Date getDate(String dateStr) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return sdf.parse(dateStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// 计算与所给时间最小的日期差
	public static int getMinDateSpan(int curDate, List<Integer> dateList) {
		int reIndex = Collections.binarySearch(dateList, curDate);
		if(0 <= reIndex) // 存在相等的日期
			return 1;
		else {
			reIndex = -reIndex;
			if(0 < reIndex-1 && reIndex-1 < dateList.size()) {	// 当前日期在所有日期中间
				if(curDate - dateList.get(reIndex -1) < dateList.get(reIndex) - curDate)
					return curDate - dateList.get(reIndex -1) + 1;
				else
					return dateList.get(reIndex)-curDate + 1;
			} else if(reIndex == dateList.size() + 1) {	// 当前日期晚于于当前所有日期
				return curDate - dateList.get(dateList.size() -1) + 1;
			} else {	// 当前时间早于所有时间
				return dateList.get(0) - curDate + 1;
			}
		}
	}
	
	public static int getIntDate(Date date) {
//		System.out.println(date.getTime()/TimeUtility.totalMillOfOneDay);
		return (int)((date.getTime() + TimeUtility.zoomTimeOffset)/TimeUtility.totalMillOfOneDay);
	}
	
	public static String getOffsetDate(String dateStr, int offset) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			long lo = ((long)(TimeUtility.getIntDate(sdf.parse(dateStr)) + offset)) * TimeUtility.totalMillOfOneDay;
			return sdf.format(new Date(lo));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
//		1970, 00:00:00
//		System.out.println(TimeUtility.getOffsetDate("1991-03-04", 4));
		System.out.println(TimeUtility.getOffsetDate("1970-06-29", 4));
	}
}
