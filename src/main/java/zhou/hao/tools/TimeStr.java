package zhou.hao.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeStr {
	
	// 一天的毫秒数
	public final static int totalMillOfOneDay = 86400000;
	
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
}
