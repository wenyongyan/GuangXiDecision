package com.cxwl.guangxi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.amap.api.services.district.DistrictSearch.OnDistrictSearchListener;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.dto.CityDto;
import com.cxwl.guangxi.utils.CustomHttpClient;
import com.cxwl.guangxi.utils.WeatherUtil;
import com.cxwl.guangxi.view.ExpandableTextView;
import com.cxwl.guangxi.view.MyDialog2;

/**
 * 天气预报
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class WeatherActivity extends BaseActivity implements OnClickListener, OnMarkerClickListener, OnMapClickListener, 
InfoWindowAdapter, OnCameraChangeListener, OnDistrictSearchListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private MyDialog2 mDialog = null;
	private List<CityDto> cityList1 = new ArrayList<CityDto>();//市级
	private List<CityDto> cityList2 = new ArrayList<CityDto>();//区县级
	private List<Marker> markerList1 = new ArrayList<Marker>();
	private List<Marker> markerList2 = new ArrayList<Marker>();
	private Marker selectMarker = null;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM月dd日");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("HH");
	private ExpandableTextView llContainer = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);
		mContext = this;
		showDialog();
		initAmap(savedInstanceState);
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
		llContainer = (ExpandableTextView) findViewById(R.id.llContainer);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		drawDistrict("广西");
		
		String url = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(url)) {
			asyncQuery(url);
		}
    }
	
	/**
	 * 绘制区域
	 */
	private void drawDistrict(String keywords) {
		DistrictSearch search = new DistrictSearch(mContext);
		DistrictSearchQuery query = new DistrictSearchQuery();
		query.setKeywords(keywords);//传入关键字
//		query.setKeywordsLevel(DistrictSearchQuery.KEYWORDS_CITY);
		query.setShowBoundary(true);//是否返回边界值
		search.setQuery(query);
		search.setOnDistrictSearchListener(this);//绑定监听器
		search.searchDistrictAnsy();//开始搜索
	}
	
	@Override
	public void onDistrictSearched(DistrictResult districtResult) {
		if (districtResult == null|| districtResult.getDistrict()==null) {
			return;
		}
		
		final DistrictItem item = districtResult.getDistrict().get(0);
		if (item == null) {
			return;
		}
		
		new Thread() {
			public void run() {
				String[] polyStr = item.districtBoundary();
				if (polyStr == null || polyStr.length == 0) {
					return;
				}
				for (String str : polyStr) {
					String[] lat = str.split(";");
					PolylineOptions polylineOption = new PolylineOptions();
					boolean isFirst=true;
					LatLng firstLatLng=null;
					for (String latstr : lat) {
						String[] lats = latstr.split(",");
						if(isFirst){
							isFirst=false;
							firstLatLng=new LatLng(Double.parseDouble(lats[1]), Double.parseDouble(lats[0]));
						}
						polylineOption.add(new LatLng(Double.parseDouble(lats[1]), Double.parseDouble(lats[0])));
					}
					if(firstLatLng!=null){
						polylineOption.add(firstLatLng);
					}
					
					polylineOption.width(8).color(getResources().getColor(R.color.title_bg));	 
					aMap.addPolyline(polylineOption);
				}
			}
 		}.start();
	}
	
	/**
	 * 初始化高德地图
	 */
	private void initAmap(Bundle bundle) {
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		
		LatLng centerLatLng = new LatLng(CONST.centerLat, CONST.centerLng);
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 6.3f));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMarkerClickListener(this);
		aMap.setOnMapClickListener(this);
		aMap.setInfoWindowAdapter(this);
		aMap.setOnCameraChangeListener(this);
	}
	
	/**
	 * 异步请求
	 */
	private void asyncQuery(String requestUrl) {
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTask() {
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
			if (!TextUtils.isEmpty(requestResult)) {
				try {
					JSONObject obj = new JSONObject(requestResult);
					if (!obj.isNull("txts")) {
						JSONArray array = obj.getJSONArray("txts");
						String content = "";
						for (int i = 0; i < array.length(); i++) {
							if (!TextUtils.isEmpty(array.getString(i))) {
								if (i == array.length()-1) {
									content = content+array.getString(i);
								}else {
									if (i % 2 == 0) {
										content = content+array.getString(i)+"\n";
									}else if (i % 2 == 1) {
										content = content+array.getString(i)+"\n\n";
									}
								}
							}
						}
						llContainer.setText(content);
						llContainer.setVisibility(View.VISIBLE);
					}
					if (!obj.isNull("cityInfos")) {
						cityList1.clear();
						cityList2.clear();
						List<String> cityIds = new ArrayList<String>();
						JSONArray array = obj.getJSONArray("cityInfos");
						for (int i = 0; i < array.length(); i++) {
							CityDto dto = new CityDto();
							JSONArray itemArray = array.getJSONArray(i);
							dto.cityId = itemArray.getString(0);
							dto.cityName = itemArray.getString(1);
							dto.lat = Double.valueOf(itemArray.getString(2));
							dto.lng = Double.valueOf(itemArray.getString(3));
							dto.level = itemArray.getString(4);
							
							if (TextUtils.equals(dto.level, "1")) {
								cityIds.add(dto.cityId);
								cityList1.add(dto);
							}else {
								cityList2.add(dto);
							}
						}
						
						addMarker(cityList2, "2");
						getWeatherInfos(cityIds, cityList1);
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
	 * 获取多个城市天气信息
	 * @param cityIds
	 */
	private void getWeatherInfos(List<String> cityIds, final List<CityDto> cityList) {
		WeatherAPI.getWeathers2(mContext, cityIds, Language.ZH_CN, new AsyncResponseHandler() {
			@Override
			public void onComplete(List<Weather> content) {
				super.onComplete(content);
				try {
					for (int i = 0; i < content.size(); i++) {
						CityDto dto = cityList.get(i);
						Weather weather = content.get(i);
						
						//获取明天预报信息
						JSONArray weeklyArray = weather.getWeatherForecastInfo(2);
						JSONObject weeklyObj = weeklyArray.getJSONObject(0);

						dto.lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
						dto.highPheCode = Integer.valueOf(weeklyObj.getString("fa"));
						dto.lowTemp = weeklyObj.getString("fd");
						dto.highTemp = weeklyObj.getString("fc");
					}

					addMarker(cityList, "1");
					cancelDialog();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onError(Throwable error, String content) {
				super.onError(error, content);
			}
		});
	}
	
	private void removeMarkers() {
		for (int i = 0; i < markerList1.size(); i++) {
			markerList1.get(i).remove();
		}
		markerList1.clear();
		
		for (int i = 0; i < markerList2.size(); i++) {
			markerList2.get(i).remove();
		}
		markerList2.clear();
	}
	
	private void addMarker(List<CityDto> list, String level) {
		if (list.isEmpty()) {
			return;
		}
		
		for (int i = 0; i < list.size(); i++) {
			CityDto dto = list.get(i);
			if (TextUtils.equals(dto.level, level)) {
				MarkerOptions options = new MarkerOptions();
				options.title(dto.cityId);
				options.snippet(dto.cityName);
				options.anchor(0.5f, 0.5f);
				options.position(new LatLng(dto.lat, dto.lng));
				if (TextUtils.equals(level, "1")) {
					options.icon(BitmapDescriptorFactory.fromView(getTextBitmap1(dto)));
					Marker marker = aMap.addMarker(options);
					if (marker != null) {
						marker.setVisible(true);
						markerList1.add(marker);
					}
				}else if(TextUtils.equals(level, "2")) {
					options.icon(BitmapDescriptorFactory.fromView(getTextBitmap2(dto.cityName)));
					Marker marker = aMap.addMarker(options);
					if (marker != null) {
						marker.setVisible(false);
						markerList2.add(marker);
					}
				}
			}
		}
	}
	
	private void setMarkerVisible(List<Marker> markerList, boolean visible) {
		for (int i = 0; i < markerList.size(); i++) {
			Marker marker = markerList.get(i);
			marker.setVisible(visible);
		}
	}
	
	/**
	 * 给marker添加文字
	 * @param name 城市名称
	 * @return
	 */
	private View getTextBitmap1(CityDto dto) {      
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.layout_weather1, null);
		if (view == null) {
			return null;
		}
		TextView tvName = (TextView) view.findViewById(R.id.tvName);
		ImageView ivPhe = (ImageView) view.findViewById(R.id.ivPhe);
		TextView tvTemp = (TextView) view.findViewById(R.id.tvTemp);
		
		tvName.setText(dto.cityName);
		Drawable drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
		try {
			long zao8 = sdf3.parse("06").getTime();
			long wan8 = sdf3.parse("20").getTime();
			long current = sdf3.parse(sdf3.format(new Date())).getTime();
			if (current >= zao8 && current < wan8) {
				drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
				drawable.setLevel(dto.highPheCode);
			}else {
				drawable = getResources().getDrawable(R.drawable.phenomenon_drawable_night);
				drawable.setLevel(dto.lowPheCode);
			}
			ivPhe.setBackground(drawable);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		tvTemp.setText(dto.highTemp+" | "+dto.lowTemp+"℃");
		
		return view;
	}
	
	/**
	 * 给marker添加文字
	 * @param name 城市名称
	 * @return
	 */
	private View getTextBitmap2(String name) {      
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.layout_weather2, null);
		if (view == null) {
			return null;
		}
		TextView tvName = (TextView) view.findViewById(R.id.tvName);
		if (!TextUtils.isEmpty(name) && name.length() > 2) {
			name = name.substring(0, 2)+"\n"+name.substring(2, name.length());
		}
		tvName.setText(name);
		return view;
	}
	
	@Override
	public void onMapClick(LatLng arg0) {
		if (selectMarker != null) {
			selectMarker.hideInfoWindow();
		}
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		if (marker != null) {
			selectMarker = marker;
			selectMarker.showInfoWindow();
		}
		return true;
	}
	
	@Override
	public View getInfoContents(final Marker marker) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.weather_marker_info, null);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		TextView tvDetail = (TextView) view.findViewById(R.id.tvDetail);
		
		tvDetail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intentLo = new Intent(mContext, ForecastActivity.class);
				intentLo.putExtra("cityName", marker.getSnippet());
				intentLo.putExtra("cityId", marker.getTitle());
				intentLo.putExtra("lng", marker.getPosition().longitude+"");
				intentLo.putExtra("lat", marker.getPosition().latitude+"");
				startActivity(intentLo);
			}
		});
		tvContent.setText("");
		getWeatherInfo(marker.getTitle(), marker.getSnippet(), tvContent, progressBar, tvDetail);
		return view;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		return null;
	}
	
	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		if (arg0.zoom <= 8.0f) {
			setMarkerVisible(markerList1, true);
			setMarkerVisible(markerList2, false);
		}else {
			setMarkerVisible(markerList1, true);
			setMarkerVisible(markerList2, true);
		}
	}
	
	/**
	 * 获取天气数据
	 */
	private void getWeatherInfo(String cityId, final String cityName, final TextView tvContent, final ProgressBar progressBar, final TextView tvDetail) {
		WeatherAPI.getWeather2(mContext, cityId, Language.ZH_CN, new AsyncResponseHandler() {
			@Override
			public void onComplete(Weather content) {
				super.onComplete(content);
				if (content != null) {
					//实况信息
					String factContent = cityName+"预报";
					
					try {
						JSONObject object = content.getWeatherFactInfo();
						if (!object.isNull("l7")) {
							String time = object.getString("l7");
							if (time != null) {
								factContent = factContent + "（"+time+"）"+"发布：\n";
							}
						}
						
						//获取明天预报信息
						JSONArray weeklyArray = content.getWeatherForecastInfo(2);
						JSONObject weeklyObj = weeklyArray.getJSONObject(0);

						JSONArray timeArray =  content.getTimeInfo(2);
						JSONObject timeObj = timeArray.getJSONObject(0);
						String week = timeObj.getString("t4");//星期几
						String date = timeObj.getString("t1");//日期
						if (week != null && date != null) {
							try {
								factContent = factContent + sdf2.format(sdf1.parse(date)) + "（"+week+"），";
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
						
						String lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))));
						String highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))));
						if (highPhe != null && lowPhe != null) {
							String phe = lowPhe;
							if (!TextUtils.equals(highPhe, lowPhe)) {
								phe = lowPhe + "转" + highPhe;
							}else {
								phe = lowPhe;
							}
							factContent = factContent + phe + "，";
						}
						
						String lowTemp = weeklyObj.getString("fd");
						String highTemp = weeklyObj.getString("fc");
						if (lowTemp != null && highTemp != null) {
							factContent = factContent + lowTemp + " ~ " + highTemp + "℃，";
						}
						
						String lowDir = getString(WeatherUtil.getWindDirection(Integer.valueOf(weeklyObj.getString("ff"))));
						String lowForce = WeatherUtil.getDayWindForce(Integer.valueOf(weeklyObj.getString("fh")));
						String highDir = getString(WeatherUtil.getWindDirection(Integer.valueOf(weeklyObj.getString("fe"))));
						String highForce = WeatherUtil.getDayWindForce(Integer.valueOf(weeklyObj.getString("fg")));
						if (!TextUtils.equals(lowDir+lowForce, highDir+highForce)) {
							factContent = factContent + lowDir + lowForce + "转" + highDir + highForce;
						}else {
							factContent = factContent + lowDir + lowForce;
						}
						
						tvContent.setText(factContent);
						tvDetail.setVisibility(View.VISIBLE);
						progressBar.setVisibility(View.GONE);
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {
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
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}

}
