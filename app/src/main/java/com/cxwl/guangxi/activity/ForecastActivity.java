package com.cxwl.guangxi.activity;

/**
 * 天气预报
 */

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.dto.WarningDto;
import com.cxwl.guangxi.dto.WeatherDto;
import com.cxwl.guangxi.manager.DBManager;
import com.cxwl.guangxi.utils.CommonUtil;
import com.cxwl.guangxi.utils.OkHttpUtil;
import com.cxwl.guangxi.utils.WeatherUtil;
import com.cxwl.guangxi.view.CubicView;
import com.cxwl.guangxi.view.MyVerticalScrollView;
import com.cxwl.guangxi.view.WeeklyView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ForecastActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvControl = null;
	private TextView tvTime = null;//更新时间
	private TextView tvPhe = null;
	private TextView tvTemperature = null;
	private TextView tvHumidity = null;
	private TextView tvWind = null;
	private TextView tvAqi = null;
	private TextView tvWeekly = null;
	private TextView tvHourly = null;
	private LinearLayout llContainer1 = null;//加载逐小时预报曲线容器
	private LinearLayout llContainer2 = null;//一周预报曲线容器
	private HorizontalScrollView hScrollView1 = null;
	private HorizontalScrollView hScrollView2 = null;
	private RelativeLayout reContent = null;
	private int width = 0;
	private List<WeatherDto> weeklyList = new ArrayList<>();
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("MM月dd日");
	private SimpleDateFormat sdf4 = new SimpleDateFormat("MM月dd日 HH:mm");
	private SimpleDateFormat sdf5 = new SimpleDateFormat("MM月dd日 HH:00");
	private TextView tvWarning = null;
	private ImageView ivWarning = null;
	private MyVerticalScrollView svDetail = null;
	private TextView tvDetail = null;
	private TextView tvKnow = null;
	private ProgressBar proDetail = null;
	private List<WarningDto> warningList = new ArrayList<>();//预警列表
	private boolean isReplace = false;//判断实况温度是否大于当天最高气温
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forecast);
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
		//解决scrollView嵌套listview，动态计算listview高度后，自动滚动到屏幕底部
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvTime.setFocusable(true);
		tvTime.setFocusableInTouchMode(true);
		tvTime.requestFocus();
		tvTemperature = (TextView) findViewById(R.id.tvTemperature);
		tvHumidity = (TextView) findViewById(R.id.tvHumidity);
		tvWind = (TextView) findViewById(R.id.tvWind);
		tvAqi = (TextView) findViewById(R.id.tvAqi);
		tvPhe = (TextView) findViewById(R.id.tvPhe);
		llContainer1 = (LinearLayout) findViewById(R.id.llContainer1);
		llContainer2 = (LinearLayout) findViewById(R.id.llContainer2);
		reContent = (RelativeLayout) findViewById(R.id.reContent);
		tvWeekly = (TextView) findViewById(R.id.tvWeekly);
		tvWeekly.setOnClickListener(this);
		tvHourly = (TextView) findViewById(R.id.tvHourly);
		tvHourly.setOnClickListener(this);
		hScrollView1 = (HorizontalScrollView) findViewById(R.id.hScrollView1);
		hScrollView2 = (HorizontalScrollView) findViewById(R.id.hScrollView2);
		tvControl = (TextView) findViewById(R.id.tvControl);
		tvControl.setOnClickListener(this);
		tvControl.setVisibility(View.VISIBLE);
		tvControl.setText("城市查询");
		ivWarning = (ImageView) findViewById(R.id.ivWarning);
		ivWarning.setOnClickListener(this);
		tvWarning = (TextView) findViewById(R.id.tvWarning);
		tvWarning.setOnClickListener(this);
		tvWarning.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
		svDetail = (MyVerticalScrollView) findViewById(R.id.svDetail);
		tvKnow = (TextView) findViewById(R.id.tvKnow);
		tvKnow.setOnClickListener(this);
		tvDetail = (TextView) findViewById(R.id.tvDetail);
		proDetail = (ProgressBar) findViewById(R.id.proDetail);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		
		String cityName = getIntent().getStringExtra("cityName");
		String cityId = getIntent().getStringExtra("cityId");
		refresh(cityName, cityId);
	}
	
	private void refresh(String cityName, String cityId) {
		showDialog();
		if (!TextUtils.isEmpty(cityName)) {
			tvTitle.setText(cityName);
		}
		
		if (!TextUtils.isEmpty(cityId)) {
			getWeatherInfo(cityId);
		}
	}
	
	/**
	 * 获取天气数据
	 */
	private void getWeatherInfo(String cityId) {
		WeatherAPI.getWeather2(mContext, cityId, Language.ZH_CN, new AsyncResponseHandler() {
			@Override
			public void onComplete(Weather content) {
				super.onComplete(content);
				if (content != null) {
					//实况信息
					JSONObject object = content.getWeatherFactInfo();
					try {
						if (!object.isNull("l7")) {
							String time = object.getString("l7");
							if (time != null) {
								try {
									time = sdf3.format(new Date())+" "+time;
									time = sdf5.format(sdf4.parse(time));
									tvTime.setText(time + "实况");
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}
						}
						if (!object.isNull("l5")) {
							String pheCode = WeatherUtil.lastValue(object.getString("l5"));
							Drawable drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
							try {
								long zao8 = sdf2.parse("06").getTime();
								long wan8 = sdf2.parse("18").getTime();
								long current = sdf2.parse(sdf2.format(new Date())).getTime();
								if (current >= zao8 && current < wan8) {
									drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
								}else {
									drawable = getResources().getDrawable(R.drawable.phenomenon_drawable_night);
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
							drawable.setLevel(Integer.valueOf(pheCode));
							tvPhe.setText(getString(WeatherUtil.getWeatherId(Integer.valueOf(pheCode))));
						}
						if (!object.isNull("l1")) {
							String factTemp = WeatherUtil.lastValue(object.getString("l1"));
							tvTemperature.setText(factTemp);
						}
						if (!object.isNull("l2")) {
							String humidity = WeatherUtil.lastValue(object.getString("l2"));
							tvHumidity.setText(getString(R.string.humidity) + " " + humidity + getString(R.string.unit_percent));
						}
						if (!object.isNull("l4")) {
							String windDir = WeatherUtil.lastValue(object.getString("l4"));
							if (!object.isNull("l3")) {
								String windForce = WeatherUtil.lastValue(object.getString("l3"));
								tvWind.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir))) + " " + 
										WeatherUtil.getFactWindForce(Integer.valueOf(windForce)));
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					//空气质量
					try {
						JSONObject obj = content.getAirQualityInfo();
						if (obj != null) {
							if (!obj.isNull("k3")) {
								String num = WeatherUtil.lastValue(obj.getString("k3"));
								if (!TextUtils.isEmpty(num)) {
									tvAqi.setText("AQI"+ " "+num + " " +WeatherUtil.getAqi(mContext, Integer.valueOf(num)));
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					//逐小时预报信息
					try {
						JSONArray hourlyArray = content.getHourlyFineForecast2();
						List<WeatherDto> hourlyList = new ArrayList<>();
						for (int i = 0; i < hourlyArray.length(); i++) {
							JSONObject itemObj = hourlyArray.getJSONObject(i);
							WeatherDto dto = new WeatherDto();
							dto.hourlyCode = Integer.valueOf(itemObj.getString("ja"));
							dto.hourlyTemp = Integer.valueOf(itemObj.getString("jb"));
							dto.hourlyTime = itemObj.getString("jf");
							dto.hourlyWindDirCode = Integer.valueOf(itemObj.getString("jc"));
							dto.hourlyWindForceCode = Integer.valueOf(itemObj.getString("jd"));
							hourlyList.add(dto);
						}
						
						//逐小时预报信息
						CubicView cubicView = new CubicView(mContext);
						cubicView.setData(hourlyList);
						llContainer1.removeAllViews();
						llContainer1.addView(cubicView, width*2, LayoutParams.WRAP_CONTENT);
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
					
					//一周预报信息
					try {
						weeklyList.clear();
						//这里只去一周预报，默认为15天，所以遍历7次
						for (int i = 1; i <= 7; i++) {
							WeatherDto dto = new WeatherDto();
							
							JSONArray weeklyArray = content.getWeatherForecastInfo(i);
							JSONObject weeklyObj = weeklyArray.getJSONObject(0);

							//晚上
							dto.lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
							dto.lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))));
							dto.lowTemp = Integer.valueOf(weeklyObj.getString("fd"));
							dto.windDir = Integer.valueOf(weeklyObj.getString("ff"));
							dto.windForce = Integer.valueOf(weeklyObj.getString("fh"));
							
							//白天数据缺失时，就使用晚上数据
							if (TextUtils.isEmpty(weeklyObj.getString("fa"))) {
								JSONObject secondObj = content.getWeatherForecastInfo(2).getJSONObject(0);
								dto.highPheCode = Integer.valueOf(secondObj.getString("fa"));
								dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(secondObj.getString("fa"))));
								dto.windDir = Integer.valueOf(secondObj.getString("fe"));
								dto.windForce = Integer.valueOf(secondObj.getString("fg"));
								
								int time1 = Integer.valueOf(secondObj.getString("fc"));
								int time2 = Integer.valueOf(weeklyObj.getString("fd"));
								if (time1 <= time2) {
									dto.highTemp = time2 + 2;
								}else {
									dto.highTemp = Integer.valueOf(secondObj.getString("fc"));
								}
								
								if (i == 1) {
									if (!TextUtils.isEmpty(tvTemperature.getText().toString())) {
										if (dto.highTemp < Integer.valueOf(tvTemperature.getText().toString())) {
											dto.highTemp = Integer.valueOf(tvTemperature.getText().toString());
											isReplace = true;
										}
									}
								}
							}else {
								//白天
								dto.highPheCode = Integer.valueOf(weeklyObj.getString("fa"));
								dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))));
								dto.highTemp = Integer.valueOf(weeklyObj.getString("fc"));
								dto.windDir = Integer.valueOf(weeklyObj.getString("fe"));
								dto.windForce = Integer.valueOf(weeklyObj.getString("fg"));
								
								if (i == 1) {
									if (!TextUtils.isEmpty(tvTemperature.getText().toString())) {
										if (dto.highTemp < Integer.valueOf(tvTemperature.getText().toString())) {
											dto.highTemp = Integer.valueOf(tvTemperature.getText().toString());
											isReplace = true;
										}
									}
								}
							}
							
							JSONArray timeArray =  content.getTimeInfo(i);
							JSONObject timeObj = timeArray.getJSONObject(0);
							dto.week = timeObj.getString("t4");//星期几
							dto.date = timeObj.getString("t1");//日期
							
							weeklyList.add(dto);
						}
						
						//一周预报曲线
						WeeklyView weeklyView = new WeeklyView(mContext);
						weeklyView.setData(weeklyList, isReplace);
						llContainer2.removeAllViews();
						llContainer2.addView(weeklyView, width, LayoutParams.WRAP_CONTENT);
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}

					cancelDialog();
					reContent.setVisibility(View.VISIBLE);
				}
			}
			
			@Override
			public void onError(Throwable error, String content) {
				super.onError(error, content);
			}
		});
		
		//获取预警信息
		tvWarning.setText("");
		tvWarning.setVisibility(View.GONE);
		ivWarning.setVisibility(View.GONE);
		svDetail.setVisibility(View.GONE);
		tvDetail.setText("");
		String warningId = queryWarningIdByCityId(cityId);
		if (!TextUtils.isEmpty(warningId)) {
			if (warningId.substring(0, 2).equals("45")) {
				OkHttpWarning("http://decision-admin.tianqi.cn/Home/extra/getwarnsguangxi", warningId);
			}else {
				OkHttpWarning("http://decision-admin.tianqi.cn/Home/extra/getwarns?order=0&areaid="+warningId, warningId);
			}
		}
	}
	
	/**
	 * 获取预警id
	 */
	private String queryWarningIdByCityId(String cityId) {
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = null;
		cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME3 + " where cid = " + "\"" + cityId + "\"",null);
		String warningId = null;
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			warningId = cursor.getString(cursor.getColumnIndex("wid"));
		}
		return warningId;
	}
	
	/**
	 * 异步请求
	 */
	private void OkHttpWarning(final String url, final String warningId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
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
											if (!object.isNull("data")) {
												warningList.clear();
												JSONArray jsonArray = object.getJSONArray("data");
												for (int i = 0; i < jsonArray.length(); i++) {
													JSONArray tempArray = jsonArray.getJSONArray(i);
													WarningDto dto = new WarningDto();
													dto.html = tempArray.optString(1);
													String[] array = dto.html.split("-");
													String item0 = array[0];
													String item1 = array[1];
													String item2 = array[2];

													dto.provinceId = item0.substring(0, 2);
													dto.type = item2.substring(0, 5);
													dto.color = item2.substring(5, 7);
													dto.time = item1;
													dto.lng = tempArray.optString(2);
													dto.lat = tempArray.optString(3);
													dto.name = tempArray.optString(0);

													if (!dto.name.contains("解除")) {
														if (!TextUtils.isEmpty(warningId)) {
															if (TextUtils.equals(warningId, item0) || TextUtils.equals(warningId.substring(0, 4)+"00", item0)) {
																warningList.add(dto);
															}
														}
													}
												}

												if (warningList.size() > 0) {
													tvWarning.setText("发布"+warningList.size()+"条预警");
													tvWarning.setVisibility(View.VISIBLE);

													WarningDto dto = warningList.get(0);
													Bitmap bitmap = null;
													if (dto.color.equals(CONST.blue[0])) {
														bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.blue[1]+CONST.imageSuffix);
														if (bitmap == null) {
															bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
														}
													}else if (dto.color.equals(CONST.yellow[0])) {
														bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.yellow[1]+CONST.imageSuffix);
														if (bitmap == null) {
															bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
														}
													}else if (dto.color.equals(CONST.orange[0])) {
														bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.orange[1]+CONST.imageSuffix);
														if (bitmap == null) {
															bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
														}
													}else if (dto.color.equals(CONST.red[0])) {
														bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.red[1]+CONST.imageSuffix);
														if (bitmap == null) {
															bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
														}
													}
													ivWarning.setImageBitmap(bitmap);
													ivWarning.setVisibility(View.VISIBLE);

													if (TextUtils.equals(dto.provinceId, "45")) {
														OkHttpWarningDetail("http://decision-admin.tianqi.cn/infomes/data/nanning/decision_guangxi_yj/content2/"+dto.html);
													}else {
														OkHttpWarningDetail("http://decision.tianqi.cn/alarm12379/content2/"+dto.html);
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
	
	/**
	 * 异步请求
	 */
	private void OkHttpWarningDetail(final String url) {
		proDetail.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
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
								proDetail.setVisibility(View.GONE);
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("description")) {
												tvDetail.setText(object.getString("description"));
												svDetail.setVisibility(View.VISIBLE);
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
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.tvControl:
			startActivityForResult(new Intent(mContext, CityActivity.class), 0);
			break;
		case R.id.tvWeekly:
			tvWeekly.setTextColor(getResources().getColor(R.color.title_bg));
			tvHourly.setTextColor(getResources().getColor(R.color.text_color4));
			hScrollView1.setVisibility(View.GONE);
			hScrollView2.setVisibility(View.VISIBLE);
			break;
		case R.id.tvHourly:
			tvWeekly.setTextColor(getResources().getColor(R.color.text_color4));
			tvHourly.setTextColor(getResources().getColor(R.color.title_bg));
			hScrollView1.setVisibility(View.VISIBLE);
			hScrollView2.setVisibility(View.GONE);
			break;
		case R.id.tvWarning:
			Intent intent = new Intent(mContext, HeadWarningActivity.class);
			intent.putParcelableArrayListExtra("warningList", (ArrayList<? extends Parcelable>) warningList);
			startActivity(intent);
			break;
		case R.id.ivWarning:
			if (svDetail.getVisibility() == View.GONE) {
				svDetail.setVisibility(View.VISIBLE);
			}else {
				svDetail.setVisibility(View.GONE);
			}
			break;
		case R.id.tvKnow:
			svDetail.setVisibility(View.GONE);
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 0:
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					String cityName = bundle.getString("cityName");
					String cityId = bundle.getString("cityId");
					refresh(cityName, cityId);
				}
				break;

			default:
				break;
			}
		}
	}

}