package com.wymd.cordova.applications;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 应用通用信息类
 */
public class AppContents {

	// 更新、临时文件保存路径
	public static String UPDATE_SAVE_PATH = "";

	// 更新文件释放路径
	public static String UPDATE_RELEASE_PATH = "";

	// 应用程序崩溃，日志保存目录
	public static String APK_CRASH_LOG = "";

	// 创建临时zip文件
	public static File TEMP_ZIP;

	// 系统更新文件后缀
	public static File SYS_SHELL;

	/** 下载保存路径后缀. */
	public static String downLoadPath = "/Download";
	/** 正式项目引用文件路径后缀. */
	public static String binFilePath = "/bin/";
	/** 崩溃日志保存文件路径后缀. */
	public static String crashLogPath = "/crash_log";
	/** 临时ZIP文件路径后缀. */
	public static String tempFilePath = "/temp.zip";
	/** 系统ZIP文件路径后缀. */
	public static String sysShellPath = "/sys_shell.zip";
	public static String sysShellName = "sys_shell.zip";

	private static SharedPreferences shared;

	public static String getUrl(Context context) {
		SharedPreferences share = context.getSharedPreferences("url",
				Context.MODE_PRIVATE);
		String url2 = share.getString("URL", "http://112.124.46.161/");

		// public static String URL = "http://223.4.148.90/";
		// public static String URL = "http://192.168.1.208:8080/";
		// url2 = "http://192.168.1.157:8080/";
		// url2 = "http://192.168.1.151:8080/";
		// url2 = "http://112.124.46.161/";
		// url2 = "http://223.4.148.90/";
		// url2 = "http://c.papermedia.com.cn/";
		// url2 = "http://121.199.7.3/";
		return url2;
	}

	public static void setUrl(String url, Context context) {
		shared = context.getSharedPreferences("url", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = shared.edit();
		editor.putString("URL", url);
		editor.commit();
	}

	public static String getInvalidBgUrl(Context context) {
		SharedPreferences share = context.getSharedPreferences("url",
				Context.MODE_PRIVATE);
		String invalidBgUrl = share.getString("invalidBgUrl", "");
		return invalidBgUrl;
	}

	public static void setInvalidBgUrl(String invalidBgUrl, Context context) {
		shared = context.getSharedPreferences("url", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = shared.edit();
		editor.putString("invalidBgUrl", invalidBgUrl);
		editor.commit();
	}
}