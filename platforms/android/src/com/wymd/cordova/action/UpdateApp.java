package com.wymd.cordova.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.wymd.cordova.R;
import com.wymd.cordova.utils.ZipUtil;

public class UpdateApp extends CordovaPlugin {

	/* 版本号检查路径 */
	private String checkPath;
	/* 新版本号 */
	private int newVerCode;
	/* 新版本名称 */
	private String newVerName;
	/* APK 下载路径 */
	private String downloadPath;
	/* 下载中 */
	private static final int DOWNLOAD = 1;
	/* 下载结束 */
	private static final int DOWNLOAD_FINISH = 2;
	/* 下载保存路径 */
	private String mSavePath;
	/* 记录进度条数量 */
	private int progress;
	/* 是否取消更新 */
	private boolean cancelUpdate = false;
	/* 上下文 */
	private Context mContext;
	/* 更新进度条 */
	private ProgressBar mProgress;
	private Dialog mDownloadDialog;

	private String TAG = "UpdateAppPlugin";

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		Log.d(TAG, "execute的action=" + action);
		this.mContext = cordova.getActivity();
		// zipUtil = new ZipUtil();

		// 判断本地文件是否全
		// if (checkAllNativeFile(this.mContext)) {
		//
		// }

		if (action.equals("checkAndUpdate")) {
			this.checkPath = args.getString(0);

			Log.d(TAG, "go into checkAndUpdate");

			checkAndUpdate();
		} else if (action.equals("getCurrentVersion")) {
			Log.d(TAG, "action=getCurrentVersion");
			// 优化 缩短传输内容，减少流量
			// JSONObject obj = new JSONObject();
			// obj.put("versionCode", this.getCurrentVerCode());
			// obj.put("versionName", this.getCurrentVerName());
			callbackContext.success(this.getCurrentVerCode() + "");

		} else if (action.equals("getServerVersion")) {
			this.checkPath = args.getString(0);

			Log.d(TAG, "action-getServerVersion-checkPath=" + checkPath);
			if (this.getServerVerInfo()) {
				// 优化 缩短传输内容，减少流量
				// JSONObject obj = new JSONObject();
				// obj.put("serverVersionCode", newVerCode);
				// obj.put("serverVersionName", newVerName);

				Log.d(TAG, "action-getServerVersion-newVerCode=" + newVerCode);
				callbackContext.success(newVerCode + "");
			} else {
				Log.d(TAG,
						"can't connect to the server!please check [checkpath]");
				callbackContext
						.error("can't connect to the server!please check [checkpath]");
			}

		}
		return false;
	}

	/**
	 * 检查更新
	 */
	private void checkAndUpdate() {
		if (getServerVerInfo()) {
			int currentVerCode = getCurrentVerCode();
			if (newVerCode > currentVerCode) {

				this.showNoticeDialog();
			}
		}
	}

	/**
	 * 获取应用当前版本代码
	 * 
	 * @param context
	 * @return
	 */
	private int getCurrentVerCode() {
		String packageName = this.mContext.getPackageName();
		int currentVer = -1;
		try {
			currentVer = this.mContext.getPackageManager().getPackageInfo(
					packageName, 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		Log.d(TAG, "currentVer=" + currentVer);
		return currentVer;
	}

	/**
	 * 获取应用当前版本名称
	 * 
	 * @param context
	 * @return
	 */
	private String getCurrentVerName() {
		String packageName = this.mContext.getPackageName();
		String currentVerName = "";
		try {
			currentVerName = this.mContext.getPackageManager().getPackageInfo(
					packageName, 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return currentVerName;
	}

	/**
	 * 获取应用名称
	 * 
	 * @param context
	 * @return
	 */
	private String getAppName() {
		return this.mContext.getResources().getText(R.string.app_name)
				.toString();
	}

	/**
	 * 获取服务器上的版本信息
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private boolean getServerVerInfo() {
		try {
			StringBuilder verInfoStr = new StringBuilder();
			URL url = new URL(checkPath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "UTF-8"), 8192);
			String line = null;
			while ((line = reader.readLine()) != null) {
				verInfoStr.append(line + "\n");
			}
			reader.close();

			JSONArray array = new JSONArray(verInfoStr.toString());
			if (array.length() > 0) {
				JSONObject obj = array.getJSONObject(0);
				newVerCode = obj.getInt("verCode");
				newVerName = obj.getString("verName");
				downloadPath = obj.getString("apkPath");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;

	}

	/**
	 * 显示软件更新对话框
	 */
	private void showNoticeDialog() {

		Log.d(TAG, "go into showNoticeDialog");
		// 构造对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.soft_update_title);
		builder.setMessage(R.string.soft_update_info);
		// 更新
		builder.setPositiveButton(R.string.soft_update_updatebtn,
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 显示下载对话框
						showDownloadDialog();
					}
				});
		// 稍后更新
		builder.setNegativeButton(R.string.soft_update_later,
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
	}

	/**
	 * 显示软件下载对话框
	 */
	private void showDownloadDialog() {
		// 构造软件下载对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.soft_updating);
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.softupdate_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
		builder.setView(v);
		// 取消更新
		builder.setNegativeButton(R.string.soft_update_cancel,
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 设置取消状态
						cancelUpdate = true;
					}
				});
		mDownloadDialog = builder.create();
		mDownloadDialog.show();
		// 现在文件
		downloadApk();
	}

	/**
	 * 下载apk文件
	 */
	private void downloadApk() {
		// 启动新线程下载软件
		new downloadApkThread().start();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// 正在下载
			case DOWNLOAD:
				// 设置进度条位置
				mProgress.setProgress(progress);
				break;
			case DOWNLOAD_FINISH:
				// 安装文件
				installApk();
				break;
			default:
				break;
			}
		};
	};

	/**
	 * 下载文件线程
	 */
	private class downloadApkThread extends Thread {
		@Override
		public void run() {
			try {
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// 获得存储卡的路径
					String sdpath = Environment.getExternalStorageDirectory()
							+ "/";
					mSavePath = sdpath + "download";

					Log.d(TAG, "this file download save path=" + mSavePath);
					Log.d(TAG, "this file download save fileName=" + newVerName);

					URL url = new URL(downloadPath);
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.connect();
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);
					// 判断文件目录是否存在
					if (!file.exists()) {
						file.mkdir();
					}
					File apkFile = new File(mSavePath, newVerName);

					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do {
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) (((float) count / length) * 100);
						// 更新进度
						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0) {
							// 下载完成
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// 取消下载对话框显示
			mDownloadDialog.dismiss();
		}
	};

	/**
	 * 安装APK文件
	 */
	private void installApk() {
		File apkfile = new File(mSavePath, newVerName);
		if (!apkfile.exists()) {
			return;
		}

		Log.d(TAG, "install apk file:// + apkfile.toString()=" + "file://"
				+ apkfile.toString());

		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		mContext.startActivity(i);
	}

	/**
	 * 判断本地文件是否全.
	 * 
	 * @param context
	 * @return
	 */
	private boolean checkAllNativeFile(Context context) {

		// 判断该文件是否都存在.
		// try {

		// zipUtil.execute(parentPath + downLoadPath + "/chat.zip",
		// parentPath
		// + binFilePath);

		// 对源文件目录进行打包，判断文件是否相同
		// ZipUtil.zipFiles(ZipUtil.listLinkedFiles(parentPath + binFilePath),
		// temp);

		// List<String> testNameList = ZipUtil.getEntriesNames(test);
		// Log.d(TAG, "testNameList=" + testNameList);
		//
		// List<String> binTestNameList = ZipUtil.getEntriesNames(temp);
		// Log.d(TAG, "binTestNameList=" + binTestNameList);
		//
		// if (testNameList.size() > binTestNameList.size()) {
		//
		// }

		// } catch (ZipException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		return true;
	}

	/***/
	private void initDownLoad() {

		String pathTo = "/com.movingmedia.zztx.activity/www/";
		// String pathTo =SDPATH + "com/";
		String pathFrom = "file:///android_asset/chat.zip";
		ZipUtil zipUtil = new ZipUtil();
		// try {
		// zipUtil.execute(pathFrom, pathTo);
		// // Toast.makeText(.this, "解压成功", Toast.LENGTH_LONG)
		// // .show();
		// } catch (ZipException e) {
		// // Toast.makeText(MainActivity.this, "Zip异常",
		// // Toast.LENGTH_LONG).show();
		// e.printStackTrace();
		// } catch (IOException e) {
		// // Toast.makeText(MainActivity.this, "io异常", Toast.LENGTH_LONG)
		// // .show();
		// e.printStackTrace();
		// }
	}

	/**
	 * 收集设备参数信息
	 * 
	 * @param ctx
	 */
	public void collectDeviceInfo(Context ctx) {
		// try {
		// PackageManager pm = ctx.getPackageManager();
		// PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
		// PackageManager.GET_ACTIVITIES);
		// if (pi != null) {
		// String versionName = pi.versionName == null ? "null"
		// : pi.versionName;
		// String versionCode = pi.versionCode + "";
		// infos.put("versionName", versionName);
		// infos.put("versionCode", versionCode);
		//
		// }
		// } catch (NameNotFoundException e) {
		// Log.e(TAG, "an error occured when collect package info", e);
		// }
		// Field[] fields = Build.class.getDeclaredFields();
		//
		// for (Field field : fields) {
		// try {
		// field.setAccessible(true);
		// infos.put(field.getName(), field.get(null).toString());
		// Log.d(TAG, field.getName() + " : " + field.get(null));
		// } catch (Exception e) {
		// Log.e(TAG, "an error occured when collect crash info", e);
		// }
		// }
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return 返回文件名称,便于将文件传送到服务器
	 */
	private String saveCrashInfo2File(Throwable ex) {

		// StringBuffer sb = new StringBuffer();
		// for (Map.Entry<String, String> entry : infos.entrySet()) {
		// String key = entry.getKey();
		// String value = entry.getValue();
		// sb.append(key + "=" + value + "\n");
		// }
		//
		// Writer writer = new StringWriter();
		// PrintWriter printWriter = new PrintWriter(writer);
		// ex.printStackTrace(printWriter);
		// Throwable cause = ex.getCause();
		// while (cause != null) {
		// cause.printStackTrace(printWriter);
		// cause = cause.getCause();
		// }
		// printWriter.close();
		// String result = writer.toString();
		// sb.append(result);
		// try {
		// long timestamp = System.currentTimeMillis();
		//
		// String time = formatter.format(new Date());
		// String fileName = "crash-" + time + "-" + timestamp + ".log";
		// if (Environment.getExternalStorageState().equals(
		// Environment.MEDIA_MOUNTED)) {
		//
		// File dir = new File(Settings.APK_CRASH_LOG);
		// if (!dir.exists()) {
		// dir.mkdirs();
		// }
		// FileOutputStream fos = new FileOutputStream(
		// Settings.APK_CRASH_LOG + "/" + fileName);
		// fos.write(sb.toString().getBytes());
		// fos.close();
		// }
		// return fileName;
		// } catch (Exception e) {
		// Log.e(TAG, "an error occured while writing file...", e);
		// }

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
