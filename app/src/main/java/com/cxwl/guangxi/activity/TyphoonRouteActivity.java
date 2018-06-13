package com.cxwl.guangxi.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.cxwl.guangxi.R;
import com.cxwl.guangxi.adapter.TyphoonNameAdapter;
import com.cxwl.guangxi.adapter.TyphoonYearAdapter;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.dto.TyphoonDto;
import com.cxwl.guangxi.utils.CommonUtil;
import com.cxwl.guangxi.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

@SuppressLint("SimpleDateFormat")
public class TyphoonRouteActivity extends BaseActivity implements OnClickListener, OnMapClickListener, 
OnMarkerClickListener, InfoWindowAdapter{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvTyphoonName = null;
	private MapView mapView = null;
	private AMap aMap = null;
	private ImageView ivLegend = null;//台风标注图
	private RelativeLayout reLegend = null;
	private ImageView ivCancelLegend = null;
	private ImageView ivTyphoonList = null;
	private RelativeLayout reTyphoonList = null;
	private ImageView ivCancelList = null;
	private ListView yearListView = null;
	private TyphoonYearAdapter yearAdapter = null;
	private List<TyphoonDto> yearList = new ArrayList<>();
	private ListView nameListView = null;
	private TyphoonNameAdapter nameAdapter = null;
	private List<TyphoonDto> nameList = new ArrayList<>();//某一年所有台风
	private List<TyphoonDto> startList = new ArrayList<>();//某一年活跃台风
	private List<ArrayList<TyphoonDto>> pointsList = new ArrayList<>();//存放某一年所有活跃台风
//	private List<TyphoonDto> points = new ArrayList<TyphoonDto>();//某个台风的数据点
//	private List<TyphoonDto> forePoints = new ArrayList<TyphoonDto>();//预报的点数据
	private RoadThread mRoadThread = null;//绘制台风点的线程
	private Marker rotateMarker = null;//台风旋转marker
	private Marker clickMarker = null;//被点击的marker
	private Circle circle, circle2;//七级风圈和十级风圈
	private float zoom = 4.0f;
	private List<Polyline> fullLines = new ArrayList<>();//实线数据
	private List<Polyline> dashLines = new ArrayList<>();//虚线数据
	private List<Marker> markerPoints = new ArrayList<>();//台风点数据
	private ImageView ivTyphoonPlay = null;//台风回放按钮
	private int MSG_PAUSETYPHOON = 100;//暂停台风
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmm");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("MM月dd日 HH时");
	private ProgressBar progressBar = null;
	private Marker factTimeMarker = null;//最后一个实况点时间
	private List<Marker> timeMarkers = new ArrayList<>();//预报时间markers

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.typhoon_route);
		mContext = this;
		initWidget();
		initAmap(savedInstanceState);
		initYearListView();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTyphoonName = (TextView) findViewById(R.id.tvTyphoonName);
		ivLegend = (ImageView) findViewById(R.id.ivLegend);
		ivLegend.setOnClickListener(this);
		reLegend = (RelativeLayout) findViewById(R.id.reLegend);
		ivCancelLegend = (ImageView) findViewById(R.id.ivCancelLegend);
		ivCancelLegend.setOnClickListener(this);
		ivTyphoonList = (ImageView) findViewById(R.id.ivTyphoonList);
		ivTyphoonList.setOnClickListener(this);
		reTyphoonList = (RelativeLayout) findViewById(R.id.reTyphoonList);
		ivCancelList = (ImageView) findViewById(R.id.ivCancelList);
		ivCancelList.setOnClickListener(this);
		ivTyphoonPlay = (ImageView) findViewById(R.id.ivTyphoonPlay);
		ivTyphoonPlay.setOnClickListener(this);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
	}
	
	private void initAmap(Bundle bundle) {
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
		aMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.setOnMarkerClickListener(this);
		aMap.setOnMapClickListener(this);
		aMap.setInfoWindowAdapter(this);
		
		drawWarningLines();
	}
	
	/**
	 * 绘制24h、48h警戒线
	 */
	private void drawWarningLines() {
		//24小时
		PolylineOptions line1 = new PolylineOptions();
		line1.width(CommonUtil.dip2px(mContext, 2));
		line1.color(getResources().getColor(R.color.red));
		line1.add(new LatLng(34.005024, 126.993568), new LatLng(21.971252, 126.993568));
		line1.add(new LatLng(17.965860, 118.995521), new LatLng(10.971050, 118.995521));
		line1.add(new LatLng(4.486270, 113.018959) ,new LatLng(-0.035506, 104.998939));
		aMap.addPolyline(line1);
		drawWarningText(getString(R.string.line_24h), getResources().getColor(R.color.red), new LatLng(30.959474, 126.993568));
		
		//48小时
		PolylineOptions line2 = new PolylineOptions();
		line2.width(CommonUtil.dip2px(mContext, 2));
		line2.color(getResources().getColor(R.color.yellow));
		line2.add(new LatLng(-0.035506, 104.998939), new LatLng(-0.035506, 119.962318));
		line2.add(new LatLng(14.968860, 131.981361) ,new LatLng(33.959474, 131.981361));
		aMap.addPolyline(line2);
		drawWarningText(getString(R.string.line_48h), getResources().getColor(R.color.yellow), new LatLng(30.959474, 131.981361));
	}
	
	/**
	 * 绘制警戒线提示问题
	 */
	private void drawWarningText(String text, int textColor, LatLng latLng) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.warning_line_markview, null);
		TextView tvLine = (TextView) view.findViewById(R.id.tvLine);
		tvLine.setText(text);
		tvLine.setTextColor(textColor);
		MarkerOptions options = new MarkerOptions();
		options.anchor(-0.3f, 0.2f);
		options.position(latLng);
		options.icon(BitmapDescriptorFactory.fromView(view));
		aMap.addMarker(options);
	}
	
	private void initYearListView() {
		yearList.clear();
		final int currentYear = Integer.valueOf(sdf1.format(new Date()));
		int years = 5;//要获取台风的年数
		for (int i = 0; i < years; i++) {
			TyphoonDto dto = new TyphoonDto();
			dto.yearly = currentYear - i;
			yearList.add(dto);
		}
		yearListView = (ListView) findViewById(R.id.yearListView);
		yearAdapter = new TyphoonYearAdapter(mContext, yearList);
		yearListView.setAdapter(yearAdapter);
		yearListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				for (int i = 0; i < yearList.size(); i++) {
					if (i == arg2) {
						yearAdapter.isSelected.put(i, true);
					}else {
						yearAdapter.isSelected.put(i, false);
					}
				}
				if (yearAdapter != null) {
					yearAdapter.notifyDataSetChanged();
				}
				
				for (int i = 0; i < nameList.size(); i++) {
					nameAdapter.isSelected.put(i, false);
				}
				if (nameAdapter != null) {
					nameAdapter.notifyDataSetChanged();
				}
				
				clearAllPoints();
				TyphoonDto dto = yearList.get(arg2);
				String url = "http://decision-admin.tianqi.cn/Home/extra/gettyphoon/list/" + dto.yearly;
				if (!TextUtils.isEmpty(url)) {
					OkHttpTyphoon(url, currentYear, dto.yearly);
				}
			}
		});
		
		String url = "http://decision-admin.tianqi.cn/Home/extra/gettyphoon/list/" + yearList.get(0).yearly;
		if (!TextUtils.isEmpty(url)) {
			OkHttpTyphoon(url, currentYear, yearList.get(0).yearly);
		}
	}
	
	/**
	 * 异步请求
	 * 获取某一年的台风信息
	 */
	private void OkHttpTyphoon(final String url, final int currentYear, final int selectYear) {
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
						final String requestResult = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								nameList.clear();
								startList.clear();
								if (!TextUtils.isEmpty(requestResult)) {
									String c = "(";
									String c2 = "})";
									String result = requestResult.substring(requestResult.indexOf(c)+c.length(), requestResult.indexOf(c2)+1);
									if (!TextUtils.isEmpty(result)) {
										try {
											JSONObject obj = new JSONObject(result);
											if (obj != null) {
												if (!obj.isNull("typhoonList")) {
													JSONArray array = obj.getJSONArray("typhoonList");
													for (int i = 0; i < array.length(); i++) {
														JSONArray itemArray = array.getJSONArray(i);
														TyphoonDto dto = new TyphoonDto();
														dto.id = itemArray.getString(0);
														dto.enName = itemArray.getString(1);
														dto.name = itemArray.getString(2);
														dto.code = itemArray.getString(4);
														dto.status = itemArray.getString(7);
														nameList.add(dto);

														//把活跃台风过滤出来存放
														if (TextUtils.equals(dto.status, "start")) {
															startList.add(dto);
														}
													}

													for (int i = startList.size()-1; i >= 0; i--) {
														TyphoonDto data = startList.get(i);
														if (TextUtils.isEmpty(data.name) || TextUtils.equals(data.name, "null")) {
															tvTyphoonName.setText(data.code + " " + data.enName);
														}else {
															tvTyphoonName.setText(data.code + " " + data.name + " " + data.enName);
														}
														String detailUrl = "http://decision-admin.tianqi.cn/Home/extra/gettyphoon/view/" + data.id;
														OkHttpTyphoonDetail(detailUrl, tvTyphoonName.getText().toString());
													}

													if (startList.size() == 0) {// 没有生效台风
														if (currentYear == selectYear) {// 判断选中年数==当前年数
															tvTyphoonName.setText(getString(R.string.no_typhoon));
														}else {
															tvTyphoonName.setText(selectYear+getString(R.string.year));
														}
														ivTyphoonPlay.setVisibility(View.GONE);

//									mRadarManager = new CaiyunManager(getApplicationContext());
//									asyncQueryMinutes("http://api.tianqi.cn:8070/v1/img.py");
//									asyncQueryCloud("http://scapi.weather.com.cn/product/list/cloudnew_20.html");
//									asyncQueryWind("http://scapi.weather.com.cn/weather/micaps/windfile");
													} else if (startList.size() == 1) {// 1个生效台风
														ivTyphoonPlay.setVisibility(View.VISIBLE);
													} else {// 2个以上生效台风
														ivTyphoonPlay.setVisibility(View.GONE);
													}
													tvTyphoonName.setVisibility(View.VISIBLE);
													progressBar.setVisibility(View.GONE);
												}

												initNameListView();
											}
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}
								}else {
									tvTyphoonName.setText(getString(R.string.no_typhoon));
									tvTyphoonName.setVisibility(View.VISIBLE);
									progressBar.setVisibility(View.GONE);
								}
							}
						});
					}
				});
			}
		}).start();
	}

	private void initNameListView() {
		nameListView = (ListView) findViewById(R.id.nameListView);
		nameAdapter = new TyphoonNameAdapter(mContext, nameList);
		nameListView.setAdapter(nameAdapter);
		nameListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				for (int i = 0; i < nameList.size(); i++) {
					if (i == arg2) {
						nameAdapter.isSelected.put(i, true);
					}else {
						nameAdapter.isSelected.put(i, false);
					}
				}
				if (nameAdapter != null) {
					nameAdapter.notifyDataSetChanged();
				}
				
				startList.clear();
				pointsList.clear();
				TyphoonDto dto = nameList.get(arg2);
				if (TextUtils.isEmpty(dto.name) || TextUtils.equals(dto.name, "null")) {
					tvTyphoonName.setText(dto.code + " " + dto.enName);
				}else {
					tvTyphoonName.setText(dto.code + " " + dto.name + " " + dto.enName);
				}
//				String detailUrl = "http://typhoon.nmc.cn/weatherservice/typhoon/jsons/view_" + dto.id + "?t="+new Date().getTime();
				String detailUrl = "http://decision-admin.tianqi.cn/Home/extra/gettyphoon/view/" + dto.id;
				OkHttpTyphoonDetail(detailUrl, tvTyphoonName.getText().toString());
				
				ivTyphoonPlay.setVisibility(View.VISIBLE);
				if (reTyphoonList.getVisibility() == View.GONE) {
					legendAnimation(false, reTyphoonList);
					reTyphoonList.setVisibility(View.VISIBLE);
					ivLegend.setClickable(false);
					ivTyphoonList.setClickable(false);
				}else {
					legendAnimation(true, reTyphoonList);
					reTyphoonList.setVisibility(View.GONE);
					ivLegend.setClickable(true);
					ivTyphoonList.setClickable(true);
				}
			}
		});
	}
	
	/**
	 * 异步请求
	 */
	private void OkHttpTyphoonDetail(final String url, final String name) {
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
						final String requestResult = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (requestResult != null) {
									String c = "(";
									String result = requestResult.substring(requestResult.indexOf(c)+c.length(), requestResult.indexOf(")"));
									if (!TextUtils.isEmpty(result)) {
										try {
											JSONObject obj = new JSONObject(result);
											if (!obj.isNull("typhoon")) {
												ArrayList<TyphoonDto> points = new ArrayList<TyphoonDto>();//台风实点
												List<TyphoonDto> forePoints = new ArrayList<TyphoonDto>();//台风预报点
												JSONArray array = obj.getJSONArray("typhoon");
												JSONArray itemArray = array.getJSONArray(8);
												for (int j = 0; j < itemArray.length(); j++) {
													JSONArray itemArray2 = itemArray.getJSONArray(j);
													TyphoonDto dto = new TyphoonDto();
													if (!TextUtils.isEmpty(name)) {
														dto.name = name;
													}
													long longTime = itemArray2.getLong(2);
													String time = sdf2.format(new Date(longTime));
													dto.time = time;
//									String time = itemArray2.getString(1);
													String str_year = time.substring(0, 4);
													if(!TextUtils.isEmpty(str_year)){
														dto.year = Integer.parseInt(str_year);
													}
													String str_month = time.substring(4, 6);
													if(!TextUtils.isEmpty(str_month)){
														dto.month = Integer.parseInt(str_month);
													}
													String str_day = time.substring(6, 8);
													if(!TextUtils.isEmpty(str_day)){
														dto.day = Integer.parseInt(str_day);
													}
													String str_hour = time.substring(8, 10);
													if(!TextUtils.isEmpty(str_hour)){
														dto.hour = Integer.parseInt(str_hour);
													}

													dto.lng = itemArray2.getDouble(4);
													dto.lat = itemArray2.getDouble(5);
													dto.pressure = itemArray2.getString(6);
													dto.max_wind_speed = itemArray2.getString(7);
													dto.move_speed = itemArray2.getString(9);
													String fx_string = itemArray2.getString(8);
													if( !TextUtils.isEmpty(fx_string)){
														String windDir = "";
														for (int i = 0; i < fx_string.length(); i++) {
															String item = fx_string.substring(i, i+1);
															if (TextUtils.equals(item, "N")) {
																item = "北";
															}else if (TextUtils.equals(item, "S")) {
																item = "南";
															}else if (TextUtils.equals(item, "W")) {
																item = "西";
															}else if (TextUtils.equals(item, "E")) {
																item = "东";
															}
															windDir = windDir+item;
														}
														dto.wind_dir = windDir;
													}

													String type = itemArray2.getString(3);
													if (TextUtils.equals(type, "TD")) {//热带低压
														type = "1";
													}else if (TextUtils.equals(type, "TS")) {//热带风暴
														type = "2";
													}else if (TextUtils.equals(type, "STS")) {//强热带风暴
														type = "3";
													}else if (TextUtils.equals(type, "TY")) {//台风
														type = "4";
													}else if (TextUtils.equals(type, "STY")) {//强台风
														type = "5";
													}else if (TextUtils.equals(type, "SuperTY")) {//超强台风
														type = "6";
													}
													dto.type = type;
													dto.isFactPoint = true;

													JSONArray array10 = itemArray2.getJSONArray(10);
													for (int m = 0; m < array10.length(); m++) {
														JSONArray itemArray10 = array10.getJSONArray(m);
														if (m == 0) {
															dto.radius_7 = itemArray10.getString(1);
															dto.en_radius_7 = itemArray10.getString(1);
															dto.es_radius_7 = itemArray10.getString(2);
															dto.wn_radius_7 = itemArray10.getString(3);
															dto.ws_radius_7 = itemArray10.getString(4);
														}else if (m == 1) {
															dto.radius_10 = itemArray10.getString(1);
															dto.en_radius_10 = itemArray10.getString(1);
															dto.es_radius_10 = itemArray10.getString(2);
															dto.wn_radius_10 = itemArray10.getString(3);
															dto.ws_radius_10 = itemArray10.getString(4);
														}
													}
													points.add(dto);

													if (!itemArray2.get(11).equals(null)) {
														JSONObject obj11 = itemArray2.getJSONObject(11);
														JSONArray array11 = obj11.getJSONArray("BABJ");
														if (array11.length() > 0) {
															forePoints.clear();
														}
														for (int n = 0; n < array11.length(); n++) {
															JSONArray itemArray11 = array11.getJSONArray(n);
															for (int i = 0; i < itemArray11.length(); i++) {
																TyphoonDto data = new TyphoonDto();
																if (!TextUtils.isEmpty(name)) {
																	data.name = name;
																}
																data.lng = itemArray11.getDouble(2);
																data.lat = itemArray11.getDouble(3);
																data.pressure = itemArray11.getString(4);
																data.move_speed = itemArray11.getString(5);

																long t1 = longTime;
																long t2 = itemArray11.getLong(0)*3600*1000;
																long ttt = t1+t2;
																String ttime = sdf2.format(new Date(ttt));
																data.time = ttime;
																String year = ttime.substring(0, 4);
																if(!TextUtils.isEmpty(year)){
																	data.year = Integer.parseInt(year);
																}
																String month = ttime.substring(4, 6);
																if(!TextUtils.isEmpty(month)){
																	data.month = Integer.parseInt(month);
																}
																String day = ttime.substring(6, 8);
																if(!TextUtils.isEmpty(day)){
																	data.day = Integer.parseInt(day);
																}
																String hour = ttime.substring(8, 10);
																if(!TextUtils.isEmpty(hour)){
																	data.hour = Integer.parseInt(hour);
																}

																String babjType = itemArray11.getString(7);
																if (TextUtils.equals(babjType, "TD")) {//热带低压
																	babjType = "1";
																}else if (TextUtils.equals(babjType, "TS")) {//热带风暴
																	babjType = "2";
																}else if (TextUtils.equals(babjType, "STS")) {//强热带风暴
																	babjType = "3";
																}else if (TextUtils.equals(babjType, "TY")) {//台风
																	babjType = "4";
																}else if (TextUtils.equals(babjType, "STY")) {//强台风
																	babjType = "5";
																}else if (TextUtils.equals(babjType, "SuperTY")) {//超强台风
																	babjType = "6";
																}
																data.type = babjType;
																data.isFactPoint = false;

																forePoints.add(data);
															}
														}
													}
												}

												points.addAll(forePoints);
												pointsList.add(points);

												if (startList.size() <= 1) {
													drawTyphoon(false, pointsList.get(0));
												}else {
													for (int i = 0; i < pointsList.size(); i++) {
														drawTyphoon(false, pointsList.get(i));
													}
												}
											}
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == MSG_PAUSETYPHOON) {
				if (ivTyphoonPlay != null) {
					ivTyphoonPlay.setImageResource(R.drawable.iv_typhoon_play);
				}
			}
		};
	};
	
	/**
	 * 清除一个台风
	 */
	private void clearOnePoint() {
		if (startList.size() <= 1) {
			for (int i = 0; i < fullLines.size(); i++) {//清除实线
				fullLines.get(i).remove();
			}
			for (int i = 0; i < dashLines.size(); i++) {//清除虚线
				dashLines.get(i).remove();
			}
			for (int i = 0; i < markerPoints.size(); i++) {//清除台风点
				markerPoints.get(i).remove();
			}
			if (circle != null) {//清除七级风圈
				circle.remove();
				circle = null;
			}
			if (circle2 != null) {//清除十级风圈
				circle2.remove();
				circle2 = null;
			}
			if (rotateMarker != null) {//清除旋转的台风标示
				rotateMarker.remove();
			}
			if (factTimeMarker != null) {
				factTimeMarker.remove();
				factTimeMarker = null;
			}
		}
		
		for (int i = 0; i < timeMarkers.size(); i++) {
			timeMarkers.get(i).remove();
		}
		timeMarkers.clear();
	}
	
	/**
	 * 清除所有台风
	 */
	private void clearAllPoints() {
		for (int i = 0; i < fullLines.size(); i++) {//清除实线
			fullLines.get(i).remove();
		}
		for (int i = 0; i < dashLines.size(); i++) {//清除虚线
			dashLines.get(i).remove();
		}
		for (int i = 0; i < markerPoints.size(); i++) {//清除台风点
			markerPoints.get(i).remove();
		}
		if (circle != null) {//清除七级风圈
			circle.remove();
			circle = null;
		}
		if (circle2 != null) {//清除十级风圈
			circle2.remove();
			circle2 = null;
		}
		if (rotateMarker != null) {//清除旋转的台风标示
			rotateMarker.remove();
		}
		if (factTimeMarker != null) {
			factTimeMarker.remove();
			factTimeMarker = null;
		}
		for (int i = 0; i < timeMarkers.size(); i++) {
			timeMarkers.get(i).remove();
		}
		timeMarkers.clear();
	}
	
	/**
	 * 绘制台风
	 * @param isAnimate
	 */
	private void drawTyphoon(boolean isAnimate, List<TyphoonDto> list) {
		if (list.isEmpty()) {
			return;
		}
		
		clearOnePoint();
		
		if (mRoadThread != null) {
			mRoadThread.cancel();
			mRoadThread = null;
		}
		mRoadThread = new RoadThread(list, isAnimate);
		mRoadThread.start();
	}
	
	/**
	 * 绘制台风点
	 */
	private class RoadThread extends Thread {
		private boolean cancelled = false;
		private List<TyphoonDto> mPoints = null;//整个台风路径信息
		private int delay = 200;
		private boolean isAnimate = true;
		private int i = 0;
		private TyphoonDto lastShikuangPoint;
		private TyphoonDto prevShikuangPoint;

		public RoadThread(List<TyphoonDto> points, boolean isAnimate) {
			mPoints = points;
			this.isAnimate = isAnimate;
		}

		@Override
		public void run() {
			lastShikuangPoint = null;
			final int len = mPoints.size();
			for (i = 0; i < len; i++) {
				if (i == len-1) {
					Message msg = new Message();
					msg.what = MSG_PAUSETYPHOON;
					handler.sendMessage(msg);
				}
				if (isAnimate) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (cancelled) {
					break;
				}
				final TyphoonDto start = mPoints.get(i);
				final TyphoonDto end = i >= (len - 1) ? null : mPoints.get(i + 1);
				final TyphoonDto lastPoint = null == end ? start : end;
				if (null == lastShikuangPoint && (TextUtils.isEmpty(lastPoint.type) || i == len - 1)) {
					lastShikuangPoint = prevShikuangPoint == null ? lastPoint : prevShikuangPoint;
				}
				prevShikuangPoint = lastPoint;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						drawRoute(start, end);
//						if (isAnimate || null != lastShikuangPoint) {
						if (null != lastShikuangPoint) {
							TyphoonDto point = lastShikuangPoint == null ? lastPoint : lastShikuangPoint;
							LatLng latlng = new LatLng(point.lat, point.lng);
							aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
						}
					}
				});
			}
		}

		public void cancel() {
			cancelled = true;
		}
	}
	
	private void drawRoute(TyphoonDto start, TyphoonDto end) {
		if (end == null) {//最后一个点
			return;
		}
		ArrayList<LatLng> temp = new ArrayList<LatLng>();
		if (end.isFactPoint) {//实况线
			PolylineOptions line = new PolylineOptions();
			line.width(CommonUtil.dip2px(mContext, 2));
			line.color(Color.RED);
			temp.add(new LatLng(start.lat, start.lng));
			LatLng latlng = new LatLng(end.lat, end.lng);
			temp.add(latlng);
			line.addAll(temp);
			Polyline fullLine = aMap.addPolyline(line);
			fullLines.add(fullLine);
		} else {//预报虚线
			LatLng start_latlng = new LatLng(start.lat, start.lng);
			double lng_start = start_latlng.longitude;
			double lat_start = start_latlng.latitude;
			LatLng end_latlng = new LatLng(end.lat, end.lng);
			double lng_end = end_latlng.longitude;
			double lat_end = end_latlng.latitude;
			double dis = Math.sqrt(Math.pow(lat_start - lat_end, 2)+ Math.pow(lng_start - lng_end, 2));
			int numPoint = (int) Math.floor(dis / 0.2);
			double lng_per = (lng_end - lng_start) / numPoint;
			double lat_per = (lat_end - lat_start) / numPoint;
			for (int i = 0; i < numPoint; i++) {
				PolylineOptions line = new PolylineOptions();
				line.color(Color.RED);
				line.width(CommonUtil.dip2px(mContext, 2));
				temp.add(new LatLng(lat_start + i * lat_per, lng_start + i * lng_per));
				if (i % 2 == 1) {
					line.addAll(temp);
					Polyline dashLine = aMap.addPolyline(line);
					dashLines.add(dashLine);
					temp.clear();
				}
			}
		}
		
		MarkerOptions options = new MarkerOptions();
		options.title(start.name+"|"+start.content(mContext));
		options.snippet(start.radius_7+","+start.radius_10);
		options.anchor(0.5f, 0.5f);
		options.position(new LatLng(start.lat, start.lng));
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.typhoon_point, null);
		ImageView ivPoint = (ImageView) view.findViewById(R.id.ivPoint);
		if (TextUtils.equals(start.type, "1")) {
			ivPoint.setImageResource(R.drawable.typhoon_level1);
		}else if (TextUtils.equals(start.type, "2")) {
			ivPoint.setImageResource(R.drawable.typhoon_level2);
		}else if (TextUtils.equals(start.type, "3")) {
			ivPoint.setImageResource(R.drawable.typhoon_level3);
		}else if (TextUtils.equals(start.type, "4")) {
			ivPoint.setImageResource(R.drawable.typhoon_level4);
		}else if (TextUtils.equals(start.type, "5")) {
			ivPoint.setImageResource(R.drawable.typhoon_level5);
		}else if (TextUtils.equals(start.type, "6")) {
			ivPoint.setImageResource(R.drawable.typhoon_level6);
		}else {//预报点
			ivPoint.setImageResource(R.drawable.typhoon_yb);
		}
		options.icon(BitmapDescriptorFactory.fromView(view));
		Marker marker = aMap.addMarker(options);
		markerPoints.add(marker);
		
		if (start.isFactPoint) {
//			marker.showInfoWindow();
//			clickMarker = marker;
			
			MarkerOptions tOption = new MarkerOptions();
			tOption.position(new LatLng(start.lat, start.lng));
			tOption.anchor(0.5f, 0.5f);
			ArrayList<BitmapDescriptor> iconList = new ArrayList<BitmapDescriptor>();
			for (int i = 1; i <= 9; i++) {
				iconList.add(BitmapDescriptorFactory.fromAsset("typhoon/typhoon_icon"+i+".png"));
			}
			tOption.icons(iconList);
			tOption.period(2);
			if (rotateMarker != null) {
				rotateMarker.remove();
				rotateMarker = null;
			}
			rotateMarker = aMap.addMarker(tOption);
			
			if (circle != null) {
				circle.remove();
				circle = null;
			}
			if (!TextUtils.isEmpty(start.radius_7)) {
				circle = aMap.addCircle(new CircleOptions().center(new LatLng(start.lat, start.lng))
						.radius(Double.valueOf(start.radius_7)*1000).strokeColor(Color.RED)
						.fillColor(0x30000000).strokeWidth(5));
			}
			
			if (circle2 != null) {
				circle2.remove();
				circle2 = null;
			}
			if (!TextUtils.isEmpty(start.radius_10)) {
				circle2 = aMap.addCircle(new CircleOptions().center(new LatLng(start.lat, start.lng))
						.radius(Double.valueOf(start.radius_10)*1000).strokeColor(Color.YELLOW)
						.fillColor(0x30ffffff).strokeWidth(5));
			}
			
			if (factTimeMarker != null) {
				factTimeMarker.remove();
				factTimeMarker = null;
			}
			View timeView = inflater.inflate(R.layout.layout_marker_time, null);
			TextView tvTime = (TextView) timeView.findViewById(R.id.tvTime);
			if (!TextUtils.isEmpty(start.time)) {
				try {
					tvTime.setText("最新位置："+sdf3.format(sdf2.parse(start.time)));
					tvTime.setTextColor(Color.BLACK);
					tvTime.setBackgroundResource(R.drawable.bg_corner_typhoon_time);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			MarkerOptions mo = new MarkerOptions();
			mo.anchor(1.1f, 0.5f);
			mo.position(new LatLng(start.lat, start.lng));
			mo.icon(BitmapDescriptorFactory.fromView(timeView));
			factTimeMarker = aMap.addMarker(mo);
		}else {
			View timeView = inflater.inflate(R.layout.layout_marker_time, null);
			TextView tvTime = (TextView) timeView.findViewById(R.id.tvTime);
			if (!TextUtils.isEmpty(start.time)) {
				try {
					tvTime.setText(sdf3.format(sdf2.parse(start.time)));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			MarkerOptions mo = new MarkerOptions();
			mo.anchor(-0.1f, 0.5f);
			mo.position(new LatLng(start.lat, start.lng));
			mo.icon(BitmapDescriptorFactory.fromView(timeView));
			Marker m = aMap.addMarker(mo);
			timeMarkers.add(m);
		}
		
		if (markerPoints.size() > 0) {
			double leftLat = markerPoints.get(0).getPosition().latitude;
			double leftLng = markerPoints.get(0).getPosition().longitude;
			double rightLat = markerPoints.get(0).getPosition().latitude;
			double rightLng = markerPoints.get(0).getPosition().longitude;
			for (int i = 0; i < markerPoints.size(); i++) {
				if (leftLat >= markerPoints.get(i).getPosition().latitude) {
					leftLat = markerPoints.get(i).getPosition().latitude;
				}
				if (leftLng >= markerPoints.get(i).getPosition().longitude) {
					leftLng = markerPoints.get(i).getPosition().longitude;
				}
				if (rightLat <= markerPoints.get(i).getPosition().latitude) {
					rightLat = markerPoints.get(i).getPosition().latitude;
				}
				if (rightLng <= markerPoints.get(i).getPosition().longitude) {
					rightLng = markerPoints.get(i).getPosition().longitude;
				}
			}
			
			final LatLng left = new LatLng(leftLat, leftLng);
			final LatLng right = new LatLng(rightLat, rightLng);
			//延时1秒开始地图动画
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						LatLngBounds bounds = new LatLngBounds.Builder()
						.include(left)
						.include(right).build();
						aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
					} catch (ArrayIndexOutOfBoundsException e) {
						e.printStackTrace();
					}
				}
			}, 500);
		}
	}
	
	@Override
	public boolean onMarkerClick(Marker arg0) {
		clickMarker = arg0;
		if (arg0 == null) {
			return false;
		}
		arg0.showInfoWindow();
		
		String[] snippet = arg0.getSnippet().split(",");
		if (circle != null) {
			circle.remove();
			circle = null;
		}
		if (!TextUtils.isEmpty(snippet[0]) && !TextUtils.equals(snippet[0], "null")) {
			circle = aMap.addCircle(new CircleOptions().center(arg0.getPosition())
					.radius(Double.valueOf(snippet[0])*1000).strokeColor(Color.RED)
					.fillColor(0x30000000).strokeWidth(5));
		}
		
		if (circle2 != null) {
			circle2.remove();
			circle2 = null;
		}
		if (!TextUtils.isEmpty(snippet[1]) && !TextUtils.equals(snippet[1], "null")) {
			circle2 = aMap.addCircle(new CircleOptions().center(arg0.getPosition())
					.radius(Double.valueOf(snippet[1])*1000).strokeColor(Color.YELLOW)
					.fillColor(0x30ffffff).strokeWidth(5));
		}
		
		return true;
	}
	
	@Override
	public void onMapClick(LatLng arg0) {
		if (clickMarker != null) {
			clickMarker.hideInfoWindow();
			if (circle != null) {//清除七级风圈
				circle.remove();
				circle = null;
			}
			if (circle2 != null) {//清除十级风圈
				circle2.remove();
				circle2 = null;
			}
		}
		
		if (reLegend.getVisibility() == View.VISIBLE) {
			legendAnimation(true, reLegend);
			reLegend.setVisibility(View.GONE);
			ivLegend.setClickable(true);
			ivTyphoonList.setClickable(true);
		}
		
		if (reTyphoonList.getVisibility() == View.VISIBLE) {
			legendAnimation(true, reTyphoonList);
			reTyphoonList.setVisibility(View.GONE);
			ivLegend.setClickable(true);
			ivTyphoonList.setClickable(true);
		}
	}
	
	@Override
	public View getInfoContents(Marker arg0) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.typhoon_marker_view, null);
		TextView tvName = (TextView) view.findViewById(R.id.tvName);
		TextView tvInfo = (TextView) view.findViewById(R.id.tvInfo);
		if (!TextUtils.isEmpty(arg0.getTitle())) {
			String[] str = arg0.getTitle().split("\\|");
			if (!TextUtils.isEmpty(str[0])) {
				tvName.setText(str[0]);
				tvTyphoonName.setText(str[0]);
			}
			if (!TextUtils.isEmpty(str[1])) {
				tvInfo.setText(str[1]);
			}
		}
		return view;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void legendAnimation(boolean flag, final RelativeLayout reLayout) {
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation = null;
		if (flag == false) {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, 1f, 
					Animation.RELATIVE_TO_SELF, 0);
		}else {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,1.0f);
		}
		animation.setDuration(400);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		reLayout.startAnimation(animationSet);
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				reLayout.clearAnimation();
			}
		});
	}
	
	/**
	 * 获取分钟级降水图
	 */

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivLegend:
		case R.id.ivCancelLegend:
			if (reLegend.getVisibility() == View.GONE) {
				legendAnimation(false, reLegend);
				reLegend.setVisibility(View.VISIBLE);
				ivLegend.setClickable(false);
				ivTyphoonList.setClickable(false);
			}else {
				legendAnimation(true, reLegend);
				reLegend.setVisibility(View.GONE);
				ivLegend.setClickable(true);
				ivTyphoonList.setClickable(true);
			}
			break;
		case R.id.ivTyphoonList:
		case R.id.ivCancelList:
			if (reTyphoonList.getVisibility() == View.GONE) {
				legendAnimation(false, reTyphoonList);
				reTyphoonList.setVisibility(View.VISIBLE);
				ivLegend.setClickable(false);
				ivTyphoonList.setClickable(false);
			}else {
				legendAnimation(true, reTyphoonList);
				reTyphoonList.setVisibility(View.GONE);
				ivLegend.setClickable(true);
				ivTyphoonList.setClickable(true);
			}
			break;
		case R.id.ivTyphoonPlay:
			ivTyphoonPlay.setImageResource(R.drawable.iv_typhoon_pause);
			if (!pointsList.isEmpty() && pointsList.get(0) != null) {
				drawTyphoon(false, pointsList.get(0));
			}
			break;

		default:
			break;
		}
	}

}
