package com.wymd.cordova.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * 时间工具类
 * 
 * @author way
 * 
 */
@SuppressLint("SimpleDateFormat")
public class ChatTimeUtil {

	private Map<String, Boolean> oldTimeMap;
	private long oldTime;
	private Map<String, String> oldSameKeyIndexMap;
	private Map<String, String> chatTime;
	private SimpleDateFormat paramFormat = new SimpleDateFormat(
			"yy-MM-dd HH:mm");

	public ChatTimeUtil(Context context) {
		oldTimeMap = new HashMap<String, Boolean>();
		oldTime = 0;
		oldSameKeyIndexMap = new HashMap<String, String>();
		chatTime = new HashMap<String, String>();
	}

	public String getChatTime(String timesamp) {

		if (chatTime.containsKey(timesamp)) {
			return chatTime.get(timesamp);
		}

		String result = "";
		SimpleDateFormat compareFormat = new SimpleDateFormat("yy-MM-dd");
		SimpleDateFormat hourAndMinFormat = new SimpleDateFormat("HH:mm");
		SimpleDateFormat longAgeFormat = new SimpleDateFormat("MM-dd HH:mm");
		Date longAgeDate = null;
		// long milliTime = 0;
		try {
			// 时间字符串转日期
			longAgeDate = (Date) paramFormat.parse(timesamp);
			// 从日期中获取毫秒数
			// milliTime = longAgeDate.getTime();

		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (longAgeDate == null) {
			chatTime.put(timesamp, result);
			return result;
		}

		// 获取系统当前时间
		Date today = new Date(System.currentTimeMillis());
		// 转换成“日”相减，判断是否近期信息
		// int temp = Integer.parseInt(loyalFormat.format(today))
		// - Integer.parseInt(loyalFormat.format(longAgeDate));

		// switch (temp) {
		// case 0:
		// result = "今天 " + hourAndMinFormat.format(longAgeDate);
		// break;
		// case 1:
		// result = "昨天 " + hourAndMinFormat.format(longAgeDate);
		// break;
		// case 2:
		// result = "前天 " + hourAndMinFormat.format(longAgeDate);
		// break;
		//
		// default:
		// result = longAgeFormat.format(longAgeDate);
		// break;
		// }

		if ((compareFormat.format(longAgeDate)).compareTo(compareFormat
				.format(today)) == 0) {
			result = "今天 " + hourAndMinFormat.format(longAgeDate);
		} else {
			result = longAgeFormat.format(longAgeDate);
		}

		chatTime.put(timesamp, result);
		return result;
	}

	public boolean checkAddTimeFlag(String timesamp, String msgId) {
		// Log.d("checkAddTimeFlag", "timesamp=" + timesamp + "&position=" +
		// msgId);
		// Log.d("checkAddTimeFlag", "oldTimeMap=" + oldTimeMap);
		// Log.d("checkAddTimeFlag", "oldSameKeyIndexMap=" +
		// oldSameKeyIndexMap);

		if (oldTimeMap.containsKey(timesamp)) {

			if (oldSameKeyIndexMap.get(timesamp).equals(msgId)) {

				return oldTimeMap.get(timesamp);
			} else {
				return false;
			}
		}

		SimpleDateFormat paramFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
		Date longAgeDate = null;
		long milliTime = 0;
		try {
			// 时间字符串转日期
			longAgeDate = (Date) paramFormat.parse(timesamp);
			// 从日期中获取毫秒数
			milliTime = longAgeDate.getTime();

		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (longAgeDate == null) {
			oldSameKeyIndexMap.put(timesamp, msgId);
			oldTimeMap.put(timesamp, false);
			return false;
		}

		long now = new Date(System.currentTimeMillis()).getTime();

		if (now - milliTime > 1000 * 60 * 30 && oldTime == 0) {

			// Log.d("checkAddTimeFlag", "now - milliTime=" + (now -
			// milliTime));
			oldSameKeyIndexMap.put(timesamp, msgId);
			oldTimeMap.put(timesamp, true);
			oldTime = milliTime;
			return true;

		} else if (oldTime - milliTime > 1000 * 60 * 30 && oldTime != 0) {

			// Log.d("checkAddTimeFlag", "oldTime - milliTime="
			// + (oldTime - milliTime));
			oldSameKeyIndexMap.put(timesamp, msgId);
			oldTimeMap.put(timesamp, true);
			oldTime = milliTime;
			return true;

		}

		oldSameKeyIndexMap.put(timesamp, msgId);
		oldTimeMap.put(timesamp, false);
		return false;
	}

	public static boolean getNetTimeForCurrentTime(String lastMsgTime) {

		// 请求网络最新时间，需要在异步线程中进行
		// URL url;
		// URLConnection uc = null;
		// String netTime = "";
		// try {
		// url = new URL("http://www.bjtime.cn");
		// // 取得资源对象
		// uc = url.openConnection();// 生成连接对象
		// uc.connect(); // 发出连接
		// long ld = uc.getDate(); // 取得网站日期时间
		// Date date = new Date(ld); // 转换为标准时间对象
		// // 分别取得时间中的小时，分钟和秒，并输出
		// // System.out.print(date.getHours() + "时" +
		// // date.getMinutes()
		// // + "分" + date.getSeconds() + "秒");
		//
		// netTime = paramFormat.format(date);
		// } catch (Exception e) {
		// e.printStackTrace();
		// netTime = paramFormat.format(new Date(System.currentTimeMillis()));
		// }

		SimpleDateFormat todayFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
		Date nowDate = new Date(System.currentTimeMillis());
		Date lastMsgDate = null;
		long lastMsgMilliTime = 0;
		try {
			// 时间字符串转日期
			lastMsgDate = (Date) todayFormat.parse(lastMsgTime);
			// 从日期中获取毫秒数
			lastMsgMilliTime = lastMsgDate.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (nowDate.getTime() - lastMsgMilliTime > 1000 * 60 * 30) {
			return true;

		} else {
			return false;
		}

	}
}
