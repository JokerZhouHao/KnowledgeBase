package utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TimeUtility {
	
	public final static int minDate = -719164;	// 1-1-1
	public final static int maxDate = 2932895;	// 5000-1-1  2932895
	
	public static Boolean noLegedDate(int date) {	// 限制时间在1-1-1 ----- 9999-12-30
		if(date >= minDate && date <= maxDate || date == Integer.MAX_VALUE) return Boolean.FALSE;
		else return Boolean.TRUE;
	}
	
	// 一天的毫秒数
	public final static long totalMillOfOneDay = 86400000;
	
	public final static long zoomTimeOffset = 28800000; // 时区差
	
	public static String getTime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		return df.format(new Date());
	}
	
	public static String getSpendTimeStr(Long startTime, Long endTime) {
		Long spendTime = endTime - startTime;
		return  spendTime/1000/3600 + "h" + spendTime/1000%3600/60 + "m" + spendTime/1000%3600000%60 + "s";
	}
	
	public static String getSpanSecondStr(Long startTime, Long endTime) {
		return String.valueOf((endTime - startTime)/1000);
	}
	
	public static Long getSpanSecond(Long startTime, Long endTime) {
		return (endTime - startTime)/1000;
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
				if(curDate - dateList.get(reIndex -2) < dateList.get(reIndex - 1) - curDate)
					return curDate - dateList.get(reIndex -2) + 1;
				else
					return dateList.get(reIndex - 1)-curDate + 1;
			} else if(reIndex == dateList.size() + 1) {	// 当前日期晚于于当前所有日期
				return curDate - dateList.get(dateList.size() -1) + 1;
			} else {	// 当前时间早于所有时间
				return dateList.get(0) - curDate + 1;
			}
		}
	}
	
	public static int getMinDateSpan(int curDate, List<Integer> dateList, int maxDateSpan) {
		int reIndex = Collections.binarySearch(dateList, curDate);
		if(0 <= reIndex) // 存在相等的日期
			return 1;
		else {
			reIndex = -reIndex;
			if(0 < reIndex-1 && reIndex-1 < dateList.size()) {	// 当前日期在所有日期中间
				if(dateList.get(reIndex -2) == Integer.MAX_VALUE && dateList.get(reIndex - 1) == Integer.MAX_VALUE)
					return maxDateSpan;
				if(dateList.get(reIndex - 1) == Integer.MAX_VALUE)	return curDate - dateList.get(reIndex -2) + 1;
				
				if(curDate - dateList.get(reIndex -2) < dateList.get(reIndex - 1) - curDate)
					return curDate - dateList.get(reIndex -2) + 1;
				else
					return dateList.get(reIndex - 1)-curDate + 1;
			} else if(reIndex == dateList.size() + 1) {	// 当前日期晚于于当前所有日期
				return curDate - dateList.get(dateList.size() -1) + 1;
			} else {	// 当前时间早于所有时间
				if(dateList.get(0) == Integer.MAX_VALUE)	return maxDateSpan;
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
	
	public static String getTailTime() {
		return getSpendTimeStr(Global.globalStartTime, System.currentTimeMillis()) + ".  " + getTime();
	}
	
	public static String getDateByIntDate(int days) {
		Date date = new Date(days * TimeUtility.totalMillOfOneDay - TimeUtility.zoomTimeOffset);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
		return df.format(date);
	}
	
	public static void main(String[] args) {
//		1970, 00:00:00
//		System.out.println(TimeUtility.getOffsetDate("1991-03-04", 4));
//		System.out.println(TimeUtility.getOffsetDate("1970-06-29", 4));
//		int iDate = TimeUtility.getIntDate(TimeUtility.getDate("2000-10-2"));
//		Date date = TimeUtility.getDate("2000-10-2");
//		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
//		System.out.println(df.format(date));
//		System.out.println(iDate);
//		System.out.println(TimeUtility.getDateByIntDate(iDate));
//		Date sDate = TimeUtility.getDate("2000-1-14");
//		Date eDate = TimeUtility.getDate("2000-1-30");
//		Date mDate = TimeUtility.getDate(TimeUtility.getDateByIntDate((TimeUtility.getIntDate(sDate) + TimeUtility.getIntDate(eDate))/2));
		
		Date mDate = new Date();
		String sDate = "1818-01-01";
		
		mDate = TimeUtility.getDate(sDate);
		int iDate = TimeUtility.getIntDate(mDate);
		System.out.println(iDate);
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(df.format(mDate));
		
		System.out.println(System.getProperty("os.name"));
		
	}
}
