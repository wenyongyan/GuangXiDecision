package com.cxwl.guangxi.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.cxwl.guangxi.view.MyDialog2;


public class BaseActivity extends Activity{

	private Context mContext = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
	}

	public MyDialog2 mDialog = null;

	public void showDialog() {
		if (mDialog == null) {
			mDialog = new MyDialog2(mContext);
		}
		mDialog.show();
	}

	public void cancelDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}
	
}
