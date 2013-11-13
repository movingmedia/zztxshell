/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.wymd.cordova;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.DroidGap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.wymd.cordova.applications.AppContents;
import com.wymd.cordova.utils.ZipUtil;

public class zztxshell extends DroidGap {

	private CordovaWebView cWebVeiw;
	private final ExecutorService threadPool = Executors.newCachedThreadPool();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// super.onCreate(savedInstanceState);
		// super.init();
		// Set by <content src="index.html" /> in config.xml
		// super.loadUrl("file:///android_asset/www/index.html");
		// super.loadUrl("file:///android_asset/www/index.html");
		// super.setIntegerProperty("splashscreen", R.drawable.icon);
		// super.loadUrl("file:///android_asset/www/index.html", 10000);
		// Show splashscreen
		// this.setIntegerProperty("splashscreen", R.drawable.icon);

		// super.loadUrl("file:///android_asset/www/splashscreen/index.html",
		// 2000);
		super.onCreate(savedInstanceState);
		this.setIntegerProperty("splashscreen", R.drawable.icon);
		super.loadUrl("file:///android_asset/www/index.html", 10000);

		// setContentView(R.layout.main);
		// cWebVeiw = (CordovaWebView) findViewById(R.id.cordovaWebView);
		// 用Activity时，必须启用Config.init,否则页面初始化不完全
		// Config.init(this);
		// cWebVeiw.loadUrl(Config.getStartUrl());
		// cWebVeiw.setBackgroundResource(R.drawable.icon);
		// cWebVeiw.loadUrl("file:///android_asset/www/index.html");

		// Config.init(this);
		// 是否是测试
		// if (false) {
		// File initFile = new File(AppContents.UPDATE_SAVE_PATH
		// + AppContents.sysShellPath);
		// if (!initFile.exists()) {
		// // 复制资源文件到本地
		// ZipUtil.copyDataBase(this, AppContents.sysShellName,
		// AppContents.UPDATE_SAVE_PATH + File.separator);
		// // 解压文件
		// ZipUtil.execute(AppContents.UPDATE_SAVE_PATH
		// + AppContents.sysShellPath,
		// AppContents.UPDATE_RELEASE_PATH);
		// }
		//
		// // cWebVeiw.loadUrl("file://" + AppContents.UPDATE_RELEASE_PATH
		// // + "www/chat/index.html");
		// } else {
		//
		// // cWebVeiw.loadUrl(Config.getStartUrl());
		// }

	}

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public ExecutorService getThreadPool() {
		return threadPool;
	}

	@Override
	public Object onMessage(String arg0, Object arg1) {
		return null;
	}

	@Override
	public void setActivityResultCallback(CordovaPlugin arg0) {

	}

	@Override
	public void startActivityForResult(CordovaPlugin arg0, Intent arg1, int arg2) {

	}

	// @Override
	// protected void onDestroy() {
	// super.onDestroy();
	// if (cWebVeiw != null) {
	// // Send destroy event to JavaScript
	// cWebVeiw.handleDestroy();
	// }
	// }

}
