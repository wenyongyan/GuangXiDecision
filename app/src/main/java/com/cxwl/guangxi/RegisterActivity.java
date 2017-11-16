package com.cxwl.guangxi;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.guangxi.adapter.SelectAreaAdapter;
import com.cxwl.guangxi.dto.CityDto;
import com.cxwl.guangxi.utils.CustomHttpClient;
import com.cxwl.guangxi.view.MyDialog2;

public class RegisterActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private EditText etPhone, etPwd, etPwdConfirm, etUnit;
	private TextView tvArea = null;
	private String areaId = null;//默认为广西全区
	private TextView tvRegister = null;
	private MyDialog2 mDialog = null;
	private ListView listView = null;
	private SelectAreaAdapter areaAdapter = null;
	private List<CityDto> areaList = new ArrayList<CityDto>();//关注区域列表

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		mContext = this;
		initWidget();
	}
	
	private void showDialog() {
		if (mDialog == null) {
			mDialog = new MyDialog2(mContext);
		}
		mDialog.show();
	}
	
	private void cancelDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("用户注册");
		etPhone = (EditText) findViewById(R.id.etPhone);
		etPwd = (EditText) findViewById(R.id.etPwd);
		etPwdConfirm = (EditText) findViewById(R.id.etPwdConfirm);
		etUnit = (EditText) findViewById(R.id.etUnit);
		tvArea = (TextView) findViewById(R.id.tvArea);
		tvArea.setOnClickListener(this);
		tvRegister = (TextView) findViewById(R.id.tvRegister);
		tvRegister.setOnClickListener(this);
		
		asyncQueryArea("https://decision-admin.tianqi.cn/Home/Work/area_search");
	}
	
	private void register() {
		if (checkInfo()) {
			showDialog();
			asyncQueryRegister("https://decision-admin.tianqi.cn/Home/Work/setup");
		}
	}
	
	/**
	 * 验证用户信息
	 */
	private boolean checkInfo() {
		if (TextUtils.isEmpty(etPhone.getText().toString())) {
			Toast.makeText(mContext, "请输入手机号", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etPwd.getText().toString())) {
			Toast.makeText(mContext, "请输入密码", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etPwdConfirm.getText().toString())) {
			Toast.makeText(mContext, "请再次输入密码", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (!TextUtils.equals(etPwd.getText().toString(), etPwdConfirm.getText().toString())) {
			Toast.makeText(mContext, "密码不一致", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etUnit.getText().toString())) {
			Toast.makeText(mContext, "请输入单位名称", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(areaId)) {
			Toast.makeText(mContext, "请选择关注区域", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	/**
	 * 用户注册
	 */
	private void asyncQueryRegister(String requestUrl) {
		HttpAsyncTaskRegister task = new HttpAsyncTaskRegister();
		task.setMethod("POST");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	private class HttpAsyncTaskRegister extends AsyncTask<String, Void, String> {
		private String method = "POST";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTaskRegister() {
			transParams();
		}
		
		/**
		 * 传参数
		 */
		private void transParams() {
			NameValuePair pair1 = new BasicNameValuePair("username", etPhone.getText().toString());
	        NameValuePair pair2 = new BasicNameValuePair("password", etPwd.getText().toString());
	        NameValuePair pair3 = new BasicNameValuePair("appid", com.cxwl.guangxi.common.CONST.APPID);
	        NameValuePair pair4 = new BasicNameValuePair("department", etUnit.getText().toString());
	        NameValuePair pair5 = new BasicNameValuePair("area", areaId);
	        
			nvpList.add(pair1);
			nvpList.add(pair2);
			nvpList.add(pair3);
			nvpList.add(pair4);
			nvpList.add(pair5);
		}

		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			if (requestResult != null) {
				try {
					JSONObject object = new JSONObject(requestResult);
					if (object != null) {
						if (!object.isNull("status")) {
							int status  = object.getInt("status");
							if (status == 1) {//成功
								Intent intent = new Intent();
								intent.putExtra("userName", etPhone.getText().toString());
								intent.putExtra("pwd", etPwd.getText().toString());
								setResult(RESULT_OK, intent);
								finish();
							}else {
								//失败
								if (!object.isNull("msg")) {
									String msg = object.getString("msg");
									if (msg != null) {
										Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
									}
								}
							}
							cancelDialog();
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
	}
	
	/**
	 * 获取关注区域列表
	 */
	private void asyncQueryArea(String requestUrl) {
		HttpAsyncTaskArea task = new HttpAsyncTaskArea();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	private class HttpAsyncTaskArea extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTaskArea() {
			transParams();
		}
		
		/**
		 * 传参数
		 */
		private void transParams() {
		}

		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			if (requestResult != null) {
				try {
					JSONArray array = new JSONArray(requestResult);
					areaList.clear();
					for (int i = 0; i < array.length(); i++) {
						JSONObject itemObj = array.getJSONObject(i);
						CityDto dto = new CityDto();
						if (!itemObj.isNull("id")) {
							dto.areaId = itemObj.getString("id");
						}
						if (!itemObj.isNull("province")) {
							dto.areaName = itemObj.getString("province");
						}
						areaList.add(dto);
					}
					if (areaList.size() > 0 && areaAdapter != null) {
						areaAdapter.notifyDataSetChanged();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
	}
	
	/**
	 * 选择关注区域
	 * @param context
	 */
	private void selectDialog(Context context) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_select_area, null);
		listView = (ListView) view.findViewById(R.id.listView);
		areaAdapter = new SelectAreaAdapter(mContext, areaList);
		listView.setAdapter(areaAdapter);
		
		final Dialog dialog = new Dialog(context, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				dialog.dismiss();
				CityDto dto = areaList.get(arg2);
				tvArea.setText(dto.areaName);
				areaId = dto.areaId;
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.tvArea:
			selectDialog(mContext);
			break;
		case R.id.tvRegister:
			register();
			break;

		default:
			break;
		}
	}
	
}
