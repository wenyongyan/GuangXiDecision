package com.cxwl.guangxi.activity;

/**
 * 登录界面
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.cxwl.guangxi.R;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.common.ColumnData;
import com.cxwl.guangxi.utils.CommonUtil;
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

public class LoginActivity extends BaseActivity implements OnClickListener, AMapLocationListener{
	
	private Context mContext = null;
	private EditText etUserName = null;//用户名
	private EditText etPwd = null;//密码
	private TextView tvLogin = null;//登陆
	private TextView tvRegister = null;
	private TextView tvForgetPwd = null;//忘记密码
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private String lat = "0";
	private String lng = "0";
	private List<ColumnData> dataList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		mContext = this;
		initWidget();
		if (CommonUtil.isLocationOpen(mContext)) {
			startLocation();
		}else {
			locationDialog(mContext);
		}
	}
	
	private void locationDialog(Context context) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_location, null);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(context, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.setCancelable(false);
		dialog.show();
		
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				if (!TextUtils.isEmpty(etUserName.getText().toString()) && !TextUtils.isEmpty(etPwd.getText().toString())) {
	    			doLogin();
	    		}
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivityForResult(intent, 1);
			}
		});
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		etUserName = (EditText) findViewById(R.id.etUserName);
		etPwd = (EditText) findViewById(R.id.etPwd);
		tvLogin = (TextView) findViewById(R.id.tvLogin);
		tvLogin.setOnClickListener(this);
		tvRegister = (TextView) findViewById(R.id.tvRegister);
		tvRegister.setOnClickListener(this);
		tvForgetPwd = (TextView) findViewById(R.id.tvForgetPwd);
		tvForgetPwd.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		
		SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
		String uid = sharedPreferences.getString(CONST.UserInfo.uId, null);
		String userName = sharedPreferences.getString(CONST.UserInfo.userName, null);
		String pwd = sharedPreferences.getString(CONST.UserInfo.passWord, null);
		
		CONST.UID = uid;
		CONST.USERNAME = userName;
		CONST.PASSWORD = pwd;
		
		etUserName.setText(userName);
		etPwd.setText(pwd);
		
		if (!TextUtils.isEmpty(etUserName.getText().toString()) && !TextUtils.isEmpty(etPwd.getText().toString())) {
			etUserName.setSelection(etUserName.getText().toString().length());
			showDialog();
		}
	}
	
	/**
	 * 开始定位
	 */
	private void startLocation() {
		mLocationOption = new AMapLocationClientOption();//初始化定位参数
        mLocationClient = new AMapLocationClient(mContext);//初始化定位
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
        mLocationOption.setWifiActiveScan(true);//设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();//启动定位
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getErrorCode() == 0) {
        	lat = String.valueOf(amapLocation.getLatitude());
        	lng = String.valueOf(amapLocation.getLongitude());
        	
        	if (!TextUtils.isEmpty(etUserName.getText().toString()) && !TextUtils.isEmpty(etPwd.getText().toString())) {
    			doLogin();
    		}
        }
	}
	
	private void doLogin() {
		if (checkInfo()) {
			showDialog();
			OkHttpLogin(CONST.GUIZHOU_LOGIN);
		}
	}
	
	/**
	 * 验证用户信息
	 */
	private boolean checkInfo() {
		if (TextUtils.isEmpty(etUserName.getText().toString())) {
			Toast.makeText(mContext, getResources().getString(R.string.input_username), Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etPwd.getText().toString())) {
			Toast.makeText(mContext, getResources().getString(R.string.input_password), Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	/**
	 * 异步请求
	 */
	private void OkHttpLogin(final String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("username", etUserName.getText().toString());
		builder.add("password", etPwd.getText().toString());
		builder.add("appid", CONST.APPID);
		builder.add("device_id", "");
		builder.add("platform", "android");
		builder.add("os_version", android.os.Build.VERSION.RELEASE);
		builder.add("software_version", CommonUtil.getVersion(mContext));
		builder.add("mobile_type", android.os.Build.MODEL);
		builder.add("address", "");
		builder.add("lat", lat);
		builder.add("lng", lng);
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
													JSONArray array = new JSONArray(object.getString("column"));
													dataList.clear();
													for (int i = 0; i < array.length(); i++) {
														JSONObject obj = array.getJSONObject(i);
														ColumnData data = new ColumnData();
														if (!obj.isNull("localviewid")) {
															data.id = obj.getString("localviewid");
														}
														if (!obj.isNull("name")) {
															data.name = obj.getString("name");
														}
														if (!obj.isNull("icon")) {
															data.icon = obj.getString("icon");
														}
														if (!obj.isNull("desc")) {
															data.desc = obj.getString("desc");
														}
														if (!obj.isNull("showtype")) {
															data.showType = obj.getString("showtype");
														}
														if (!obj.isNull("dataurl")) {
															data.dataUrl = obj.getString("dataurl");
														}
														if (!obj.isNull("child")) {
															JSONArray childArray = new JSONArray(obj.getString("child"));
															for (int j = 0; j < childArray.length(); j++) {
																JSONObject childObj = childArray.getJSONObject(j);
																ColumnData dto = new ColumnData();
																if (!childObj.isNull("localviewid")) {
																	dto.id = childObj.getString("localviewid");
																}
																if (!childObj.isNull("name")) {
																	dto.name = childObj.getString("name");
																}
																if (!childObj.isNull("desc")) {
																	dto.desc = childObj.getString("desc");
																}
																if (!childObj.isNull("icon")) {
																	dto.icon = childObj.getString("icon");
																}
																if (!childObj.isNull("showtype")) {
																	dto.showType = childObj.getString("showtype");
																}
																if (!childObj.isNull("dataurl")) {
																	dto.dataUrl = childObj.getString("dataurl");
																}
																data.child.add(dto);
															}
														}
														dataList.add(data);
													}

													if (!object.isNull("info")) {
														JSONObject obj = new JSONObject(object.getString("info"));
														if (!obj.isNull("id")) {
															String uid = obj.getString("id");
															if (uid != null) {
																//把用户信息保存在sharedPreferance里
																SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
																Editor editor = sharedPreferences.edit();
																editor.putString(CONST.UserInfo.uId, uid);
																editor.putString(CONST.UserInfo.userName, etUserName.getText().toString());
																editor.putString(CONST.UserInfo.passWord, etPwd.getText().toString());
																editor.putString(CONST.UserInfo.department, obj.getString("department"));
																editor.putString(CONST.UserInfo.area, obj.getString("area"));
																editor.putString(CONST.UserInfo.areaid, obj.getString("areaid"));
																editor.commit();

																CONST.UID = uid;
																CONST.USERNAME = etUserName.getText().toString();
																CONST.PASSWORD = etPwd.getText().toString();
																CONST.DEPARTMENT = obj.getString("department");
																CONST.AREA = obj.getString("area");
																CONST.AREAID = obj.getString("areaid");

																cancelDialog();

																Intent intent = new Intent(mContext, MainActivity.class);
																Bundle bundle = new Bundle();
																bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) dataList);
																intent.putExtras(bundle);
																startActivity(intent);
																finish();

															}
														}
													}
												}else {
													//失败
													if (!object.isNull("msg")) {
														final String msg = object.getString("msg");
														if (msg != null) {
															Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
														}
														cancelDialog();

													}
												}
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvLogin:
			doLogin();
			break;
		case R.id.tvRegister:
			startActivityForResult(new Intent(mContext, RegisterActivity.class), 2);
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 0) {
			switch (requestCode) {
			case 1:
				if (CommonUtil.isLocationOpen(mContext)) {
					startLocation();
				}else {
					locationDialog(mContext);
				}
				break;

			default:
				break;
			}
		}else if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 2:
				Bundle bundle = data.getExtras();
				String userName = bundle.getString("userName");
				String pwd = bundle.getString("pwd");
				etUserName.setText(userName);
				etPwd.setText(pwd);
				doLogin();
				break;

			default:
				break;
			}
		}
	}
}
