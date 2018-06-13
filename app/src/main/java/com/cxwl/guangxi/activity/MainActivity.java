package com.cxwl.guangxi.activity;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.cxwl.guangxi.R;
import com.cxwl.guangxi.adapter.MainAdapter;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.common.ColumnData;
import com.cxwl.guangxi.common.MyApplication;
import com.cxwl.guangxi.dto.WarningDto;
import com.cxwl.guangxi.dto.WeatherDto;
import com.cxwl.guangxi.fragment.OneHourFragment;
import com.cxwl.guangxi.fragment.SevenDayFragment;
import com.cxwl.guangxi.manager.DBManager;
import com.cxwl.guangxi.manager.DataCleanManager;
import com.cxwl.guangxi.utils.AutoUpdateUtil;
import com.cxwl.guangxi.utils.CommonUtil;
import com.cxwl.guangxi.utils.OkHttpUtil;
import com.cxwl.guangxi.utils.WeatherUtil;
import com.cxwl.guangxi.view.MainViewPager;
import com.cxwl.guangxi.view.MyVerticalScrollView;

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

public class MainActivity extends BaseActivity implements OnClickListener, AMapLocationListener, MyApplication.NavigationListener{
	
	private Context mContext = null;
	private TextView tvLocation = null;
	private ImageView ivSetting = null;//设置按钮
	private TextView tvTime = null;
	private TextView tvPhe = null;
	private TextView tvTemperature = null;
	private TextView tvHumidity = null;
	private TextView tvWind = null;
	private RelativeLayout reFact = null;
	private long mExitTime;//记录点击完返回按钮后的long型时间
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private GridView gridView = null;
	private MainAdapter mAdapter = null;
	private ArrayList<ColumnData> channelList = new ArrayList<>();
	private String cityName = null;
	private String cityId = null;
	private double lng = 0;
	private double lat = 0;
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH");
	private List<WarningDto> warningList = new ArrayList<>();//预警列表
	private TextView tvWarning = null;
	private ImageView ivWarning = null;
	private MyVerticalScrollView svDetail = null;
	private TextView tvDetail = null;
	private TextView tvKnow = null;
	private ProgressBar proDetail = null;
	private TextView tvAqi = null;
	private SimpleDateFormat sdf3 = new SimpleDateFormat("MM月dd日");
	private SimpleDateFormat sdf4 = new SimpleDateFormat("MM月dd日 HH:mm");
	private SimpleDateFormat sdf5 = new SimpleDateFormat("MM月dd日 HH:00");
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	private ImageView[] ivTips = null;//装载点的数组
	private ViewGroup viewGroup = null;
	private ProgressBar progressBar = null;
	private RelativeLayout reTitle = null;
	private RelativeLayout reContent = null;
	private boolean isReplace = false;//判断实况温度是否大于当天最高气温
	private int height = 0;
	
	//侧拉页面
	private DrawerLayout drawerlayout = null;
	private RelativeLayout reLeft = null;
	private TextView tvUserName = null;
	private TextView tvLogout = null;
	private LinearLayout llFeedBack = null;
	private LinearLayout llClearCache = null;
	private TextView tvCache = null;
	private LinearLayout llIntro = null;
	private LinearLayout llVersion = null;
	private TextView tvVersion = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        MyApplication.addDestoryActivity(MainActivity.this, "MainActivity");
        initWidget();
		initGridView();
		if (CommonUtil.isLocationOpen(mContext)) {
			startLocation();
		}else {
			locationDialog(mContext);
		}
		MyApplication.setNavigationListener(this);
    }

	@Override
	public void showNavigation(boolean show) {
		onLayoutMeasure();
	}

	@Override
	protected void onResume() {
		super.onResume();
		onLayoutMeasure();
	}

	/**
	 * 判断navigation是否显示，重新计算页面布局
	 */
	private void onLayoutMeasure() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		height = dm.heightPixels;

		int statusBarHeight = -1;//状态栏高度
		//获取status_bar_height资源的ID
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			//根据资源ID获取响应的尺寸值
			statusBarHeight = getResources().getDimensionPixelSize(resourceId);
		}

		reTitle.measure(0, 0);
		int height1 = reTitle.getMeasuredHeight();
		reContent.measure(0, 0);
		int height2 = reContent.getMeasuredHeight();

		if (mAdapter != null) {
			mAdapter.height = height-statusBarHeight-height1-height2;
			mAdapter.notifyDataSetChanged();
			ViewGroup.LayoutParams params = gridView.getLayoutParams();
			params.height = height-height1-statusBarHeight;
			gridView.setLayoutParams(params);
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
				tvLocation.setText("南宁");
	    		CONST.location = tvLocation.getText().toString();
	    		cityName = tvLocation.getText().toString();
	        	lng = 108.366129;
	        	lat = 22.817239;
	        	getWeatherInfo(lng, lat);
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
		AutoUpdateUtil.checkUpdate(this, mContext, "47", getString(R.string.app_name), true);
		
		tvLocation = (TextView) findViewById(R.id.tvLocation);
		tvLocation.setOnClickListener(this);
		tvLocation.setFocusable(true);
		tvLocation.setFocusableInTouchMode(true);
		tvLocation.requestFocus();
		ivSetting = (ImageView) findViewById(R.id.ivSetting);
		ivSetting.setOnClickListener(this);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvPhe = (TextView) findViewById(R.id.tvPhe);
		tvTemperature = (TextView) findViewById(R.id.tvTemperature);
		tvHumidity = (TextView) findViewById(R.id.tvHumidity);
		tvWind = (TextView) findViewById(R.id.tvWind);
		reFact = (RelativeLayout) findViewById(R.id.reFact);
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
		tvAqi = (TextView) findViewById(R.id.tvAqi);
		viewGroup = (ViewGroup) findViewById(R.id.viewGroup);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		reTitle = (RelativeLayout) findViewById(R.id.reTitle);
		reContent = (RelativeLayout) findViewById(R.id.reContent);
		
		drawerlayout = (DrawerLayout) findViewById(R.id.drawerlayout);
		drawerlayout.setVisibility(View.VISIBLE);
		reLeft = (RelativeLayout) findViewById(R.id.reLeft);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		tvLogout = (TextView) findViewById(R.id.tvLogout);
		tvLogout.setOnClickListener(this);
		llClearCache = (LinearLayout) findViewById(R.id.llClearCache);
		llClearCache.setOnClickListener(this);
		tvCache = (TextView) findViewById(R.id.tvCache);
		llVersion = (LinearLayout) findViewById(R.id.llVersion);
		llVersion.setOnClickListener(this);
		tvVersion = (TextView) findViewById(R.id.tvVersion);
		tvVersion.setText(CommonUtil.getVersion(mContext));
		llIntro = (LinearLayout) findViewById(R.id.llIntro);
		llIntro.setOnClickListener(this);
		llFeedBack = (LinearLayout) findViewById(R.id.llFeedBack);
		llFeedBack.setOnClickListener(this);
		reLeft = (RelativeLayout) findViewById(R.id.reLeft);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		height = dm.heightPixels;
		LayoutParams params = reLeft.getLayoutParams();
		params.width = dm.widthPixels-150;
		reLeft.setLayoutParams(params);
		
		SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
		String userName = sharedPreferences.getString(CONST.UserInfo.userName, null);
		if (userName != null) {
			tvUserName.setText(userName);
		}
		
		try {
			String cache = DataCleanManager.getCacheSize(mContext);
			tvCache.setText(cache);
		} catch (Exception e) {
			e.printStackTrace();
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
    		tvLocation.setText(amapLocation.getDistrict());
    		CONST.location = tvLocation.getText().toString();
    		cityName = amapLocation.getDistrict();
        	lng = amapLocation.getLongitude();
        	lat = amapLocation.getLatitude();
        	getWeatherInfo(amapLocation.getLongitude(), amapLocation.getLatitude());
        }
	}
	
	/**
	 * 获取天气数据
	 */
	private void getWeatherInfo(double lng, double lat) {
		WeatherAPI.getGeo(mContext, String.valueOf(lng), String.valueOf(lat), new AsyncResponseHandler(){
			@Override
			public void onComplete(JSONObject content) {
				super.onComplete(content);
				if (!content.isNull("geo")) {
					try {
						JSONObject geoObj = content.getJSONObject("geo");
						if (!geoObj.isNull("id")) {
							cityId = geoObj.getString("id");
							if (cityId != null) {
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
												JSONObject itemObj = hourlyArray.getJSONObject(0);
												WeatherDto hourDto = new WeatherDto();
												hourDto.hourlyCode = Integer.valueOf(itemObj.getString("ja"));
												hourDto.hourlyTemp = Integer.valueOf(itemObj.getString("jb"));
												hourDto.hourlyHumidity = itemObj.getString("je");
												hourDto.hourlyTime = itemObj.getString("jf");
												hourDto.hourlyWindDirCode = Integer.valueOf(itemObj.getString("jc"));
												hourDto.hourlyWindForceCode = Integer.valueOf(itemObj.getString("jd"));
												
												
												List<WeatherDto> weeklyList = new ArrayList<WeatherDto>();
												//这里只去一周预报，默认为15天，所以遍历7次
												for (int i = 1; i <= 7; i++) {
													WeatherDto dto = new WeatherDto();
													
													JSONArray weeklyArray = content.getWeatherForecastInfo(i);
													JSONObject weeklyObj = weeklyArray.getJSONObject(0);

													//晚上
													dto.lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
													dto.lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))));
													dto.lowTemp = Integer.valueOf(weeklyObj.getString("fd"));
													
													//白天数据缺失时，就使用晚上数据
													if (TextUtils.isEmpty(weeklyObj.getString("fa"))) {
														JSONObject secondObj = content.getWeatherForecastInfo(2).getJSONObject(0);
														dto.highPheCode = Integer.valueOf(secondObj.getString("fa"));
														dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(secondObj.getString("fa"))));
														
														int time1 = Integer.valueOf(secondObj.getString("fc"));
														int time2 = Integer.valueOf(weeklyObj.getString("fd"));
														if (time1 <= time2) {
															dto.highTemp = time2 + 2;
														}else {
															dto.highTemp = Integer.valueOf(secondObj.getString("fc"));
														}
														if (!TextUtils.isEmpty(tvTemperature.getText().toString())) {
															if (dto.highTemp < Integer.valueOf(tvTemperature.getText().toString())) {
																dto.highTemp = Integer.valueOf(tvTemperature.getText().toString());
																isReplace = true;
															}
														}
													}else {
														//白天
														dto.highPheCode = Integer.valueOf(weeklyObj.getString("fa"));
														dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))));
														dto.highTemp = Integer.valueOf(weeklyObj.getString("fc"));
														
														if (!TextUtils.isEmpty(tvTemperature.getText().toString())) {
															if (dto.highTemp < Integer.valueOf(tvTemperature.getText().toString())) {
																dto.highTemp = Integer.valueOf(tvTemperature.getText().toString());
																isReplace = true;
															}
														}
													}
													
													JSONArray timeArray =  content.getTimeInfo(i);
													JSONObject timeObj = timeArray.getJSONObject(0);
													dto.week = timeObj.getString("t4");//星期几
													dto.date = timeObj.getString("t1");//日期
													
													weeklyList.add(dto);
												}
												
												initIndexViewPager(hourDto, weeklyList);
											} catch (JSONException e) {
												e.printStackTrace();
											} catch (NullPointerException e) {
												e.printStackTrace();
											}
											
											reFact.setVisibility(View.VISIBLE);
											progressBar.setVisibility(View.GONE);
										}
									}
									
									@Override
									public void onError(Throwable error, String content) {
										super.onError(error, content);
									}
								});
								
								//获取预警信息
								String warningId = queryWarningIdByCityId(cityId);
								if (!TextUtils.isEmpty(warningId)) {
									if (warningId.substring(0, 2).equals("45")) {
										OkHttpWarning("http://decision-admin.tianqi.cn/Home/extra/getwarnsguangxi", warningId);
									}else {
										OkHttpWarning("http://decision-admin.tianqi.cn/Home/extra/getwarns?order=0&areaid="+warningId, warningId);
									}
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onError(Throwable error, String content) {
				super.onError(error, content);
			}
		});
	}
	
	/**
	 * 初始化生活指数viewPager
	 */
	private void initIndexViewPager(WeatherDto hourDto, List<WeatherDto> weeklyList) {
		fragments.clear();
		Fragment fragment = new OneHourFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable("hourDto", hourDto);
		fragment.setArguments(bundle);
		fragments.add(fragment);
		
		fragment = new SevenDayFragment();
		bundle = new Bundle();
		bundle.putBoolean("isReplace", isReplace);
		bundle.putParcelableArrayList("weeklyList", (ArrayList<? extends Parcelable>) weeklyList);
		fragment.setArguments(bundle);
		fragments.add(fragment);
		
		ivTips = new ImageView[2];
		viewGroup.removeAllViews();
		for (int i = 0; i < 2; i++) {
			ImageView imageView = new ImageView(mContext);
			imageView.setLayoutParams(new LayoutParams(5, 5));  
			ivTips[i] = imageView;  
			if(i == 0){  
				ivTips[i].setBackgroundResource(R.drawable.point_black);  
			}else{  
				ivTips[i].setBackgroundResource(R.drawable.point_gray);  
			}  
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));  
			layoutParams.leftMargin = 25;  
			layoutParams.rightMargin = 25;  
			viewGroup.addView(imageView, layoutParams);  
		}
		
		viewPager = (MainViewPager) findViewById(R.id.viewPager);
		viewPager.setSlipping(true);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		viewPager.setAdapter(new MyPagerAdapter());
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			for (int i = 0; i < fragments.size(); i++) {
				if(i == arg0){  
					ivTips[i].setBackgroundResource(R.drawable.point_black);  
				}else{  
					ivTips[i].setBackgroundResource(R.drawable.point_gray);  
				} 
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	/**
	 * @ClassName: MyPagerAdapter
	 * @Description: TODO填充ViewPager的数据适配器
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:37:47
	 *
	 */
	private class MyPagerAdapter extends PagerAdapter {
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(fragments.get(position).getView());
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = fragments.get(position);
			if (!fragment.isAdded()) { // 如果fragment还没有added
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.add(fragment, fragment.getClass().getSimpleName());
//				ft.commit();
				ft.commitAllowingStateLoss();
				/**
				 * 在用FragmentTransaction.commit()方法提交FragmentTransaction对象后
				 * 会在进程的主线程中,用异步的方式来执行。
				 * 如果想要立即执行这个等待中的操作,就要调用这个方法(只能在主线程中调用)。
				 * 要注意的是,所有的回调和相关的行为都会在这个调用中被执行完成,因此要仔细确认这个方法的调用位置。
				 */
				getFragmentManager().executePendingTransactions();
			}

			if (fragment.getView().getParent() == null) {
				container.addView(fragment.getView()); // 为viewpager增加布局
			}
			return fragment.getView();
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
		}).start();
	}

	private void initGridView() {
		channelList.clear();
		channelList.addAll(getIntent().getExtras().<ColumnData>getParcelableArrayList("dataList"));
		gridView = (GridView) findViewById(R.id.gridView);
		mAdapter = new MainAdapter(mContext, channelList);
		gridView.setAdapter(mAdapter);
		onLayoutMeasure();
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColumnData dto = channelList.get(arg2);
				Intent intent;
				if (TextUtils.equals(dto.showType, CONST.URL)) {
					intent = new Intent(mContext, UrlActivity.class);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					startActivity(intent);
				}else if (TextUtils.equals(dto.showType, CONST.NEWS)) {
					if (TextUtils.equals(dto.id, "6")) {
						intent = new Intent(mContext, NewsActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}
				}else if (TextUtils.equals(dto.showType, CONST.LOCAL)) {
					if (TextUtils.equals(dto.id, "104")) {//台风动态
						intent = new Intent(mContext, TyphoonRouteActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "108")) {//雷达回波
						intent = new Intent(mContext, RadarActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "102")) {//预警信息
						intent = new Intent(mContext, WarningActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "101")) {
						intent = new Intent(mContext, ServiceMaterialActivity.class);//服务材料
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						Bundle bundle = new Bundle();
						bundle.putParcelable("data", dto);
						intent.putExtras(bundle);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "107")) {//气温实况
						if (TextUtils.equals(CONST.AREAID, "450000") || TextUtils.isEmpty(CONST.AREAID)) {
							intent = new Intent(mContext, FactProActivity.class);
						}else {
							intent = new Intent(mContext, FactCityActivity.class);
						}
						intent.putExtra("adCode", CONST.AREAID);
						intent.putExtra(CONST.COLUMN_ID, dto.id);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						intent.putExtra(CONST.COLUMN_ID, dto.id);
						Bundle bundle = new Bundle();
						bundle.putParcelable("data", dto);
						intent.putExtras(bundle);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "106")) {//风向风速实况
						if (TextUtils.equals(CONST.AREAID, "450000") || TextUtils.isEmpty(CONST.AREAID)) {
							intent = new Intent(mContext, FactProActivity.class);
						}else {
							intent = new Intent(mContext, FactCityActivity.class);
						}
						intent.putExtra("adCode", CONST.AREAID);
						intent.putExtra(CONST.COLUMN_ID, dto.id);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						intent.putExtra(CONST.COLUMN_ID, dto.id);
						Bundle bundle = new Bundle();
						bundle.putParcelable("data", dto);
						intent.putExtras(bundle);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "105")) {//雨情雨量
						if (TextUtils.equals(CONST.AREAID, "450000") || TextUtils.isEmpty(CONST.AREAID)) {
							intent = new Intent(mContext, FactProActivity.class);
						}else {
							intent = new Intent(mContext, FactCityActivity.class);
						}
						intent.putExtra("adCode", CONST.AREAID);
						intent.putExtra(CONST.COLUMN_ID, dto.id);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						intent.putExtra(CONST.COLUMN_ID, dto.id);
						Bundle bundle = new Bundle();
						bundle.putParcelable("data", dto);
						intent.putExtras(bundle);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "103")) {//天气预报
						intent = new Intent(mContext, WeatherActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}
				}
			}
		});
	}
	
	/**
	 * 删除对话框
	 * @param message 标题
	 * @param content 内容
	 * @param flag 0删除本地存储，1删除缓存
	 */
	private void deleteDialog(final boolean flag, String message, String content, final TextView textView) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.delete_dialog, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (flag) {
					DataCleanManager.clearCache(mContext);
					try {
						String cache = DataCleanManager.getCacheSize(mContext);
						if (cache != null) {
							textView.setText(cache);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else {
//					ChannelsManager.clearData(mContext);//清除保存在本地的频道数据
					DataCleanManager.clearLocalSave(mContext);
					try {
						String data = DataCleanManager.getLocalSaveSize(mContext);
						if (data != null) {
							textView.setText(data);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				dialog.dismiss();
			}
		});
	}
	
	/**
	 * 删除对话框
	 * @param message 标题
	 * @param content 内容
	 */
	private void logout(String message, String content) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.delete_dialog, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
				Editor editor = sharedPreferences.edit();
				editor.clear();
				editor.commit();
				startActivity(new Intent(mContext, LoginActivity.class));
				finish();
				MyApplication.destoryActivity();
			}
		});
	}
	
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (drawerlayout != null) {
				if (drawerlayout.isDrawerOpen(reLeft)) {
					drawerlayout.closeDrawer(reLeft);
				}else {
					if ((System.currentTimeMillis() - mExitTime) > 2000) {
						Toast.makeText(mContext, getString(R.string.confirm_exit)+getString(R.string.app_name), Toast.LENGTH_SHORT).show();
						mExitTime = System.currentTimeMillis();
					} else {
						finish();
					}
				}
			}else {
				finish();
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivSetting:
			if (drawerlayout.isDrawerOpen(reLeft)) {
				drawerlayout.closeDrawer(reLeft);
			}else {
				drawerlayout.openDrawer(reLeft);
			}
			break;
		case R.id.tvLocation:
			Intent intentLo = new Intent(mContext, ForecastActivity.class);
			intentLo.putExtra("cityName", cityName);
			intentLo.putExtra("cityId", cityId);
			intentLo.putExtra("lng", lng);
			intentLo.putExtra("lat", lat);
			startActivity(intentLo);
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
		case R.id.llClearCache:
			deleteDialog(true, getString(R.string.delete_cache), getString(R.string.sure_delete_cache), tvCache);
			break;
		case R.id.llVersion:
			AutoUpdateUtil.checkUpdate(this, mContext, "47", getString(R.string.app_name), false);
			break;
		case R.id.tvLogout:
			logout(getString(R.string.logout), getString(R.string.sure_logout));
			break;
		case R.id.llIntro:
			Intent intentI = new Intent(mContext, IntroduceActivity.class);
			intentI.putExtra(CONST.ACTIVITY_NAME, getString(R.string.setting_build));
			startActivity(intentI);
			break;
		case R.id.llFeedBack:
			Intent intentF = new Intent(mContext, FeedbackActivity.class);
			intentF.putExtra(CONST.ACTIVITY_NAME, getString(R.string.setting_feedback));
			intentF.putExtra(CONST.INTENT_APPID, com.cxwl.guangxi.common.CONST.APPID);
			startActivity(intentF);
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
					initWidget();
				}else {
					locationDialog(mContext);
				}
				break;

			default:
				break;
			}
		}
	}

}
