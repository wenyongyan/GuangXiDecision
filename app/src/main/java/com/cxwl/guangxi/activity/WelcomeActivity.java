package com.cxwl.guangxi.activity;

/**
 * 欢迎界面
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.utils.CommonUtil;

public class WelcomeActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		startIntentMain();
	}
	
	/**
	 * 启动线程进入主界面
	 */
	private void startIntentMain() {
		Handler handler = new Handler();
		handler.postDelayed(new MainRunnable(), 1000);
	}
	
	private class MainRunnable implements Runnable{
		@Override
		public void run() {
			SharedPreferences sharedPreferences = getSharedPreferences(CONST.SHOWGUIDE, Context.MODE_PRIVATE);
			String version = sharedPreferences.getString(CONST.VERSION, "");
			if (!TextUtils.equals(version, CommonUtil.getVersion(getApplicationContext()))) {
				startActivity(new Intent(getApplication(), GuideActivity.class));
			}else {
				startActivity(new Intent(getApplication(), LoginActivity.class));
			}
			finish();
		}
	}
	
	@Override
	public boolean onKeyDown(int KeyCode, KeyEvent event){
		if (KeyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return super.onKeyDown(KeyCode, event);
	}
	
}
