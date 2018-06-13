package com.cxwl.guangxi.activity;

/**
 * 用户注册
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.adapter.SelectAreaAdapter;
import com.cxwl.guangxi.dto.CityDto;
import com.cxwl.guangxi.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private EditText etPhone, etPwd, etPwdConfirm, etUnit;
	private TextView tvArea = null;
	private String areaId = null;//默认为广西全区
	private TextView tvRegister = null;
	private ListView listView = null;
	private SelectAreaAdapter areaAdapter = null;
	private List<CityDto> areaList = new ArrayList<>();//关注区域列表

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		mContext = this;
		initWidget();
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

		OkHttpArea("https://decision-admin.tianqi.cn/Home/Work/area_search");
	}
	
	private void register() {
		if (checkInfo()) {
			showDialog();
			OkHttpRegister("https://decision-admin.tianqi.cn/Home/Work/setup");
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
	private void OkHttpRegister(final String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("username", etPhone.getText().toString());
		builder.add("password", etPwd.getText().toString());
		builder.add("appid", com.cxwl.guangxi.common.CONST.APPID);
		builder.add("department", etUnit.getText().toString());
		builder.add("area", areaId);
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
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
						});

					}
				});
			}
		}).start();
	}
	
	/**
	 * 获取关注区域列表
	 */
	private void OkHttpArea(final String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("province", "广西");
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										areaList.clear();
										JSONArray array = new JSONArray(result);
										for (int i = 0; i < array.length(); i++) {
											JSONObject itemObj = array.getJSONObject(i);
											CityDto dto = new CityDto();
											if (!itemObj.isNull("id")) {
												dto.areaId = itemObj.getString("id");
											}
											if (!itemObj.isNull("city")) {
												dto.areaName = itemObj.getString("city");
											}
											areaList.add(dto);
										}

										if (areaAdapter != null) {
											areaAdapter.notifyDataSetChanged();
										}

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						});

					}
				});
			}
		}).start();
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
