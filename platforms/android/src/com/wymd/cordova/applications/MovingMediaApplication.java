package com.wymd.cordova.applications;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

/**
 * 应用全局处理类
 */
public class MovingMediaApplication extends Application {

	private static final String TAG = MovingMediaApplication.class.getName();

	// 定义全局应用
	private static MovingMediaApplication mInstance = null;

	/**
	 * @return the mInstance
	 */
	public synchronized static MovingMediaApplication getInstance() {
		return mInstance;
	}

	// 自定义List，模拟栈管理各Activity的销毁、清空,存放用户显示的activity
	// public List<Activity> mActivityList = new ArrayList<Activity>();

	PendingIntent restartIntent;
	// 用来存储设备信息和异常信息
	private Map<String, String> infos = new HashMap<String, String>();
	// 用于格式化日期,作为日志文件名的一部分
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	// 互动消息，保存本地数据库
	// private ChatMessageNativeDb chatMsgDB;
	// 新消息冒泡，保存本地数据库
	// private NewMessageRemindDb newMsgRemindDB;

	/**
	 * 程序启动时的处理
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();

		// 以下用来捕获程序崩溃异常
		// Intent intent = new Intent();
		// // 参数1：包名，参数2：程序入口的activity
		// intent.setClass(this, LoadingActivity.class);
		// restartIntent = PendingIntent.getActivity(this, 0, intent,
		// Intent.FLAG_ACTIVITY_NEW_TASK);

		// 出现应用级异常时的处理
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			public void uncaughtException(final Thread thread,
					final Throwable throwable) {
				new Thread(new Runnable() {

					public void run() {

						Looper.prepare();

						handleException(throwable);

						finish();
						System.exit(1);

						// new
						// AlertDialog.Builder(mActivityList.get(mActivityList
						// .size() <= 1 ? 0 : mActivityList.size() - 1))
						// .setTitle("客官,不小心崩溃啦")
						// // .setMessage("")
						// .setPositiveButton("立即重启",
						// new DialogInterface.OnClickListener() {
						// @Override
						// public void onClick(
						// DialogInterface dialog,
						// int which) {
						//
						// // 自定义方法，关闭当前打开的所有avtivity
						// finish();
						//
						// // 1秒钟后重启应用
						// AlarmManager mgr = (AlarmManager)
						// getSystemService(Context.ALARM_SERVICE);
						// mgr.set(AlarmManager.RTC,
						// System.currentTimeMillis() + 1000,
						// restartIntent);
						//
						// // 强制退出程序,退出服务
						// System.exit(1);
						//
						// }
						// })
						//
						// .setNegativeButton("立即退出",
						// new DialogInterface.OnClickListener() {
						// @Override
						// public void onClick(
						// DialogInterface dialog,
						// int which) {
						//
						// // 自定义方法，关闭当前打开的所有avtivity
						// finish();
						//
						// // 这个只能关闭当前的Activity，也就是对于一个只有单个Activity
						// // 的应用程序有效，如果对于有多外Activity的应用程序它就无能为力了。
						// // android.os.Process
						// // .killProcess(android.os.Process
						// // .myPid());
						//
						// // 强制退出服务
						// System.exit(1);
						//
						// }
						// }).show();

						// 如果用户没有处理则让系统默认的异常处理器来处理
						// if (handleException(throwable)) {
						//
						// // 1秒钟后重启应用
						// AlarmManager mgr = (AlarmManager)
						// getSystemService(Context.ALARM_SERVICE);
						// mgr.set(AlarmManager.RTC,
						// System.currentTimeMillis() + 1000,
						// restartIntent);
						//
						// // 强制退出程序,退出服务
						// System.exit(0);
						// // 自定义方法，关闭当前打开的所有avtivity
						// finish();
						//
						// } else {
						//
						// // 退出程序
						// android.os.Process.killProcess(android.os.Process
						// .myPid());
						//
						// // 强制退出服务
						// System.exit(0);
						// // 自定义方法，关闭当前打开的所有avtivity
						// finish();
						// }

						Looper.loop();
					}

				}).start();

				// 错误LOG
				Log.e(TAG, throwable.getMessage(), throwable);
			}
		});

		start();

		mInstance = this;

		// chatMsgDB = new ChatMessageNativeDb(this);
		// newMsgRemindDB = new NewMessageRemindDb(this);
	}

	// public synchronized ChatMessageNativeDb getChatMsgDB() {
	// if (chatMsgDB == null)
	// chatMsgDB = new ChatMessageNativeDb(this);
	// return chatMsgDB;
	// }

	// public synchronized NewMessageRemindDb getNewMsgRemindDB() {
	// if (newMsgRemindDB == null)
	// newMsgRemindDB = new NewMessageRemindDb(this);
	// return newMsgRemindDB;
	// }

	/**
	 * 启动程序时的处理
	 */
	public void start() {
		// 获得屏幕高度（像素）
		// Settings.DISPLAY_HEIGHT =
		// getResources().getDisplayMetrics().heightPixels;
		// // 获得屏幕宽度（像素）
		// Settings.DISPLAY_WIDTH =
		// getResources().getDisplayMetrics().widthPixels;
		// // 获得系统状态栏高度（像素）
		// Settings.STATUS_BAR_HEIGHT = getStatusBarHeight();

		// 文件路径设置
		String parentPath = null;
		// 存在SDCARD的时候，路径设置到SDCARD
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			parentPath = Environment.getExternalStorageDirectory().getPath()
					+ File.separator + getPackageName();

			// 不存在SDCARD的时候，路径设置到ROM
		} else {
			parentPath = Environment.getDataDirectory().getPath() + "/data/"
					+ getPackageName();
		}

		// 更新、临时文件保存路径
		AppContents.UPDATE_SAVE_PATH = parentPath + AppContents.downLoadPath;
		// 更新文件释放路径
		AppContents.UPDATE_RELEASE_PATH = parentPath + AppContents.binFilePath;
		// 应用崩溃，日志保存目录
		AppContents.APK_CRASH_LOG = parentPath + AppContents.crashLogPath;

		// 创建各目录
		File downLoadFile = new File(AppContents.UPDATE_SAVE_PATH);
		if (!downLoadFile.exists()) {
			downLoadFile.mkdirs();
		} else {
			if (getDirSize(downLoadFile) > 10) {
				// qrcode.delete();
				deleteDirectory(AppContents.UPDATE_SAVE_PATH);
			}
		}

		File binFile = new File(AppContents.UPDATE_RELEASE_PATH);
		if (!binFile.exists()) {
			binFile.mkdirs();
		}

		File carshLog = new File(AppContents.APK_CRASH_LOG);
		if (!carshLog.exists()) {
			carshLog.mkdirs();
		} else {
			if (getDirSize(carshLog) > 1) {
				// carshLog.delete();
				deleteDirectory(AppContents.APK_CRASH_LOG);
			}
		}

		// 创建临时zip文件
		AppContents.TEMP_ZIP = new File(AppContents.UPDATE_SAVE_PATH
				+ AppContents.tempFilePath);

		// 创建系统zip文件
		AppContents.SYS_SHELL = new File(AppContents.UPDATE_SAVE_PATH
				+ AppContents.sysShellPath);
	}

	/**
	 * 自定义方法，关闭当前打开的所有avtivity.
	 */
	public void finish() {

		onTerminate();

		// 调用系统垃圾处理
		System.gc();
	}

	/** ------------------------自定义栈管理Activity结束----------------- */

	// /**
	// * 获得系统状态栏高度
	// *
	// * @return 系统状态栏高度（像素）
	// */
	// private int getStatusBarHeight() {
	// try {
	// Class<?> cls = Class.forName("com.android.internal.R$dimen");
	// Object obj = cls.newInstance();
	// Field field = cls.getField("status_bar_height");
	// int x = Integer.parseInt(field.get(obj).toString());
	// return getResources().getDimensionPixelSize(x);
	// } catch (Exception e) {
	// }
	// return 0;
	// }

	public void startPushService() {
		// if (mXmppServiceManager == null) {
		// mXmppServiceManager = new XmppServiceManager(this,
		// Constants.ACTION_PUSH_RECEIVE);
		// mXmppServiceManager.startService();
		// }
	}

	public void stopPushService() {
		// if (mXmppServiceManager != null) {
		// mXmppServiceManager.stopService();
		// mXmppServiceManager = null;
		// }
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}

		// 收集设备参数信息
		collectDeviceInfo(this);

		// 保存日志文件
		saveCrashInfo2File(ex);
		return true;
	}

	/**
	 * 收集设备参数信息
	 * 
	 * @param ctx
	 */
	public void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null"
						: pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);

			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, "an error occured when collect package info", e);
		}
		Field[] fields = Build.class.getDeclaredFields();

		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				Log.d(TAG, field.getName() + " : " + field.get(null));
			} catch (Exception e) {
				Log.e(TAG, "an error occured when collect crash info", e);
			}
		}
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return 返回文件名称,便于将文件传送到服务器
	 */
	private String saveCrashInfo2File(Throwable ex) {

		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);
		try {
			long timestamp = System.currentTimeMillis();

			String time = formatter.format(new Date());
			String fileName = "crash-" + time + "-" + timestamp + ".log";
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {

				File dir = new File(AppContents.APK_CRASH_LOG);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(
						AppContents.APK_CRASH_LOG + "/" + fileName);
				fos.write(sb.toString().getBytes());
				fos.close();
			}
			return fileName;
		} catch (Exception e) {
			Log.e(TAG, "an error occured while writing file...", e);
		}

		return null;
	}

	/**
	 * 用到递归，依次获得目录下文件、子目录下文件的大小. 超过一定数量就自动删除.
	 * 
	 * @param file
	 * @return
	 */
	private double getDirSize(File file) {
		// 判断文件是否存在
		if (file.exists()) {
			// 如果是目录则递归计算其内容的总大小
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				double size = 0;
				for (File f : children) {
					size += getDirSize(f);
				}
				return size;
			} else {// 如果是文件则直接返回其大小,以“兆”为单位
				double size = (double) file.length() / 1024 / 1024;
				return size;
			}
		} else {
			return 0.0;
		}
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param sPath
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	public boolean deleteDirectory(String sPath) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}

		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}

		boolean flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {

			if (files[i].isFile()) {
				// 删除子文件
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			} else {
				// 删除子目录
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			}
		}
		if (!flag) {
			return false;
		}

		// 删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 删除单个文件
	 * 
	 * @param sPath
	 *            被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

}