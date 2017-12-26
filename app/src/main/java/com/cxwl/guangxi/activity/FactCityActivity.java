package com.cxwl.guangxi.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Text;
import com.cxwl.guangxi.R;
import com.cxwl.guangxi.adapter.FactDetailAdapter;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.common.ColumnData;
import com.cxwl.guangxi.dto.FactDto;
import com.cxwl.guangxi.utils.CommonUtil;
import com.cxwl.guangxi.utils.OkHttpUtil;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.tsz.afinal.FinalBitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 城市实况资料
 */

public class FactCityActivity extends BaseActivity implements View.OnClickListener, AMap.OnCameraChangeListener, AMap.OnMarkerClickListener {

	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle, tvControl;
	private LinearLayout llContainer = null;
	private LinearLayout llContainer1 = null;
	private int width = 0;
	private float density = 0;
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private float zoom = 6.5f;
	private ScrollView scrollView = null;
	private ListView listView = null;
	private FactDetailAdapter mAdapter = null;
	private List<FactDto> realDatas = new ArrayList<>();
	private LinearLayout listTitle = null;
	private LinearLayout ll1, ll2, ll3;
	private TextView tv1, tv2, tv3;
	private ImageView iv1, iv2, iv3;
	private boolean b1 = false, b2 = false, b3 = false;//false为将序，true为升序
	private TextView tvLayerName, tvIntro, tvToast;//图层名称
	private ImageView ivChart = null;//图例
	private ProgressBar progressBar = null;
	private List<Polygon> polygons = new ArrayList<>();//图层数据
	private List<Text> texts = new ArrayList<>();//等值线数值
	private List<Polyline> polylines = new ArrayList<>();//广西边界市县边界线
	private List<Marker> personStations = new ArrayList<>();//人工站
	private List<Marker> autoStations = new ArrayList<>();//自动站
	private List<FactDto> cityInfos = new ArrayList<>();//城市信息
	private List<FactDto> districtNames = new ArrayList<>();
	private ColumnData data = null;
	private String adCode = "450100";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fact_city);
		mContext = this;
		initWidget();
		initAmap(savedInstanceState);
		initListView();
	}

	/**
	 * 初始化高德地图
	 */
	private void initAmap(Bundle bundle) {
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setVisibility(View.VISIBLE);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}

		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CONST.centerLatLng, zoom));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.showMapText(false);
		aMap.setOnCameraChangeListener(this);
		aMap.setOnMarkerClickListener(this);

		aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {
			@Override
			public void onTouch(MotionEvent arg0) {
				if (scrollView != null) {
					if (arg0.getAction() == MotionEvent.ACTION_UP) {
						scrollView.requestDisallowInterceptTouchEvent(false);
					}else {
						scrollView.requestDisallowInterceptTouchEvent(true);
					}
				}
			}
		});

		LatLngBounds bounds = new LatLngBounds.Builder()
//		.include(new LatLng(57.9079, 71.9282))
//		.include(new LatLng(3.9079, 134.8656))
				.include(new LatLng(1, -179))
				.include(new LatLng(89, 179))
				.build();
		aMap.addGroundOverlay(new GroundOverlayOptions()
				.anchor(0.5f, 0.5f)
				.positionFromBounds(bounds)
				.image(BitmapDescriptorFactory.fromResource(R.drawable.empty))
				.transparency(0.0f));
		aMap.runOnDrawFrame();
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Point leftPoint = new Point(0, dm.heightPixels);
		Point rightPoint = new Point(dm.widthPixels, 0);
		LatLng leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
		LatLng rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);

//		if (leftlatlng.latitude <= 3.9079 || rightLatlng.latitude >= 57.9079 || leftlatlng.longitude <= 71.9282
//				|| rightLatlng.longitude >= 134.8656 || arg0.zoom < 6.0f) {
//			aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CONST.centerLatLng, 6.5f));
//		}
		if (zoom == arg0.zoom) {
			return;
		}
		handler.removeMessages(1001);
		Message msg = handler.obtainMessage();
		msg.what = 1001;
		msg.obj = arg0.zoom;
		handler.sendMessageDelayed(msg, 1000);

		zoom = arg0.zoom;
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 1001:
					float msgZoom = (Float)msg.obj;
					removePersonStations();
					removeAutoStations();
					if (msgZoom >= 10.0) {
						//绘制自动站
						for (int i = 0; i < realDatas.size(); i++) {
							FactDto dto = realDatas.get(i);
							LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							View view = inflater.inflate(R.layout.layout_fact_value, null);
							TextView tvValue = (TextView) view.findViewById(R.id.tvValue);
							TextView tvName = (TextView) view.findViewById(R.id.tvName);
							if (dto.val >= 99999) {
								tvValue.setText("");
							}else {
								tvValue.setText(dto.val+"");
							}
							if (!TextUtils.isEmpty(dto.stationName)) {
								tvName.setText(dto.stationName);
							}
							MarkerOptions options = new MarkerOptions();
							options.title(dto.stationName);
							options.anchor(0.5f, 0.5f);
							options.position(new LatLng(dto.lat, dto.lng));
							options.icon(BitmapDescriptorFactory.fromView(view));
							Marker marker = aMap.addMarker(options);
							marker.setVisible(true);
							autoStations.add(marker);
						}
					} else {
						//绘制人工站
						for (int i = 0; i < cityInfos.size(); i++) {
							FactDto dto = cityInfos.get(i);
							LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							View view = inflater.inflate(R.layout.layout_fact_value, null);
							TextView tvValue = (TextView) view.findViewById(R.id.tvValue);
							TextView tvName = (TextView) view.findViewById(R.id.tvName);
							if (dto.val >= 99999) {
								tvValue.setText("");
							}else {
								tvValue.setText(dto.val+"");
							}
							if (!TextUtils.isEmpty(dto.stationName)) {
								tvName.setText(dto.stationName);
							}
							MarkerOptions options = new MarkerOptions();
							options.title(dto.stationName);
							options.anchor(0.5f, 0.5f);
							options.position(new LatLng(dto.lat, dto.lng));
							options.icon(BitmapDescriptorFactory.fromView(view));
							Marker marker = aMap.addMarker(options);
							marker.setVisible(true);
							personStations.add(marker);
						}
					}
					break;
			}
		}
	};

	@Override
	public boolean onMarkerClick(Marker marker) {
		for (int i = 0; i < cityInfos.size(); i++) {
			FactDto dto = cityInfos.get(i);
			if (TextUtils.equals(marker.getTitle(), dto.stationName)) {
				Intent intent = new Intent(mContext, FactDetailChartActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
		}
		for (int i = 0; i < realDatas.size(); i++) {
			FactDto dto = realDatas.get(i);
			if (TextUtils.equals(marker.getTitle(), dto.stationName)) {
				Intent intent = new Intent(mContext, FactDetailChartActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
		}

		return true;
	}

	private void initListView() {
		listView = (ListView) findViewById(R.id.listView);
		mAdapter = new FactDetailAdapter(mContext, realDatas);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				FactDto dto = realDatas.get(arg2);
				Intent intent = new Intent(mContext, FactDetailChartActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvControl = (TextView) findViewById(R.id.tvControl);
		tvControl.setOnClickListener(this);
		tvControl.setText("广西全区");
		llContainer = (LinearLayout) findViewById(R.id.llContainer);
		llContainer1 = (LinearLayout) findViewById(R.id.llContainer1);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		listTitle = (LinearLayout) findViewById(R.id.listTitle);
		ll1 = (LinearLayout) findViewById(R.id.ll1);
		ll1.setOnClickListener(this);
		ll2 = (LinearLayout) findViewById(R.id.ll2);
		ll2.setOnClickListener(this);
		ll3 = (LinearLayout) findViewById(R.id.ll3);
		ll3.setOnClickListener(this);
		tv1 = (TextView) findViewById(R.id.tv1);
		tv2 = (TextView) findViewById(R.id.tv2);
		tv3 = (TextView) findViewById(R.id.tv3);
		iv1 = (ImageView) findViewById(R.id.iv1);
		iv2 = (ImageView) findViewById(R.id.iv2);
		iv3 = (ImageView) findViewById(R.id.iv3);
		tvLayerName = (TextView) findViewById(R.id.tvLayerName);
		tvIntro = (TextView) findViewById(R.id.tvIntro);
		tvToast = (TextView) findViewById(R.id.tvToast);
		ivChart = (ImageView) findViewById(R.id.ivChart);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		density = dm.density;

		if (TextUtils.equals(CONST.AREAID, "450000") || TextUtils.isEmpty(CONST.AREAID)) {
			tvControl.setVisibility(View.GONE);
		}else {
			tvControl.setVisibility(View.VISIBLE);
		}

		if (getIntent().hasExtra("data")) {
            data = getIntent().getExtras().getParcelable("data");
            if (!TextUtils.isEmpty(data.name)) {
                tvTitle.setText(data.name);
            }

            adCode = getIntent().getStringExtra("adCode");
            addColumn(data);
        }
	}

	/**
	 * 添加子栏目
	 * @param data
	 */
	private void addColumn(ColumnData data) {
		llContainer.removeAllViews();
		llContainer1.removeAllViews();
		int size = data.child.size();
		if (size <= 0) {
			return;
		}
		for (int i = 0; i < size; i++) {
			ColumnData itemDto = data.child.get(i);
			TextView tvName = new TextView(mContext);
			tvName.setGravity(Gravity.CENTER);
			tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
			tvName.setPadding(0, (int)(density*10), 0, (int)(density*10));
			tvName.setMaxLines(1);

			TextView tvBar = new TextView(mContext);
			tvBar.setGravity(Gravity.CENTER);
			tvBar.setPadding((int)(density*10), 0, (int)(density*10), 0);

			if (!TextUtils.isEmpty(itemDto.name)) {
				tvName.setText(itemDto.name);
				tvName.setTag(itemDto.dataUrl+","+itemDto.id);
			}
			if (i == 0) {
				tvName.setTextColor(getResources().getColor(R.color.title_bg));
				tvBar.setBackgroundColor(getResources().getColor(R.color.title_bg));
				OkHttpFact(itemDto.dataUrl);
			}else {
				tvName.setTextColor(getResources().getColor(R.color.text_color3));
				tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
			}
			llContainer.addView(tvName);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tvName.getLayoutParams();
			if (size <= 4) {
				if (size == 2) {
					params.width = width/2;
				}else if (size == 3) {
					params.width = width/3;
				}else {
					params.width = width/4;
				}
			}else {
				params.setMargins((int)(density*10), 0, (int)(density*10), 0);
			}
			tvName.setLayoutParams(params);

			int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			tvName.measure(w, h);
			llContainer1.addView(tvBar);
			LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) tvBar.getLayoutParams();
			if (size <= 4) {
				if (size == 2) {
					params1.width = width/2;
				}else if (size == 3) {
					params1.width = width/3;
				}else {
					params1.width = width/4;
				}
			}else {
				params1.setMargins((int)(density*10), 0, (int)(density*10), 0);
				params1.width = tvName.getMeasuredWidth();
			}
			params1.height = (int) (density*2);
			params1.gravity = Gravity.CENTER;
			tvBar.setLayoutParams(params1);

			tvName.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (llContainer != null) {
						for (int i = 0; i < llContainer.getChildCount(); i++) {
							TextView tvName = (TextView) llContainer.getChildAt(i);
							TextView tvBar = (TextView) llContainer1.getChildAt(i);
							if (TextUtils.equals((String) arg0.getTag(), (String) tvName.getTag())) {
								tvName.setTextColor(getResources().getColor(R.color.title_bg));
								tvBar.setBackgroundColor(getResources().getColor(R.color.title_bg));
								String[] tags = ((String) arg0.getTag()).split(",");
								OkHttpFact(tags[0]);
							}else {
								tvName.setTextColor(getResources().getColor(R.color.text_color4));
								tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
							}
						}
					}
				}
			});

		}
	}

	/**
	 * 获取实况信息
	 * @param url
	 */
	private void OkHttpFact(String url) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		progressBar.setVisibility(View.VISIBLE);
		url = url + "&city=" + adCode;
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
						if (result != null) {
							try {
								JSONObject obj = new JSONObject(result);

								if (!obj.isNull("th")) {
									JSONObject itemObj = obj.getJSONObject("th");
									if (!itemObj.isNull("stationName")) {
										tv1.setText(itemObj.getString("stationName"));
									}
									if (!itemObj.isNull("area")) {
										tv2.setText(itemObj.getString("area"));
									}
									if (!itemObj.isNull("val")) {
										tv3.setText(itemObj.getString("val"));
									}
								}

								if (!obj.isNull("title")) {
									tvLayerName.setText(obj.getString("title"));
									tvLayerName.setVisibility(View.VISIBLE);
								}

								if (!obj.isNull("cutlineUrl")) {
									FinalBitmap finalBitmap = FinalBitmap.create(mContext);
									finalBitmap.display(ivChart, obj.getString("cutlineUrl"), null, 0);
								}

								if (!obj.isNull("zx")) {
									tvIntro.setText(obj.getString("zx"));
								}

								if (!obj.isNull("t")) {
									cityInfos.clear();
									JSONArray array = obj.getJSONArray("t");
									for (int i = 0; i < array.length(); i++) {
										FactDto f = new FactDto();
										JSONObject o = array.getJSONObject(i);
										if (!o.isNull("name")) {
											f.stationName = o.getString("name");
										}
										if (!o.isNull("stationCode")) {
											f.stationCode = o.getString("stationCode");
										}
										if (!o.isNull("lon")) {
											f.lng = Double.parseDouble(o.getString("lon"));
										}
										if (!o.isNull("lat")) {
											f.lat = Double.parseDouble(o.getString("lat"));
										}
										if (!o.isNull("value")) {
											f.val = Double.parseDouble(o.getString("value"));
										}
										cityInfos.add(f);
									}
								}

								if (!obj.isNull("dataUrl")) {
									String dataUrl = obj.getString("dataUrl");
									if (!TextUtils.isEmpty(dataUrl)) {
										OkHttpJson(dataUrl);
									}
								}

								if (!obj.isNull("realDatas")) {
									realDatas.clear();
									JSONArray array = new JSONArray(obj.getString("realDatas"));
									for (int i = 0; i < array.length(); i++) {
										JSONObject itemObj = array.getJSONObject(i);
										FactDto dto = new FactDto();
										if (!itemObj.isNull("stationCode")) {
											dto.stationCode = itemObj.getString("stationCode");
										}
										if (!itemObj.isNull("stationName")) {
											dto.stationName = itemObj.getString("stationName");
										}
										if (!itemObj.isNull("area")) {
											dto.area = itemObj.getString("area");
										}
										if (!itemObj.isNull("area1")) {
											dto.area1 = itemObj.getString("area1");
										}
										if (!itemObj.isNull("val")) {
											dto.val = itemObj.getDouble("val");
										}
										if (!itemObj.isNull("lat")) {
											dto.lat = itemObj.getDouble("lat");
										}
										if (!itemObj.isNull("lon")) {
											dto.lng = itemObj.getDouble("lon");
										}

										if (!TextUtils.isEmpty(dto.stationName) && !TextUtils.isEmpty(dto.area)) {
											realDatas.add(dto);
										}
									}
									if (realDatas.size() > 0 && mAdapter != null) {
										mAdapter.notifyDataSetChanged();
//										CommonUtil.setListViewHeightBasedOnChildren(listView);
										tvIntro.setVisibility(View.VISIBLE);
										listTitle.setVisibility(View.VISIBLE);
										listView.setVisibility(View.VISIBLE);
									}else {
										tvIntro.setVisibility(View.GONE);
										listTitle.setVisibility(View.GONE);
										listView.setVisibility(View.GONE);
									}
								}
								//详情结束

								tvLayerName.setFocusable(true);
								tvLayerName.setFocusableInTouchMode(true);
								tvLayerName.requestFocus();

							} catch (JSONException e) {
								e.printStackTrace();
							}
						}else {
							removePolygons();
							progressBar.setVisibility(View.GONE);
							tvToast.setVisibility(View.VISIBLE);
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									tvToast.setVisibility(View.GONE);
								}
							}, 1000);
						}
					}
				});
			}
		});
	}

	/**
	 * 返回中文的首字母
	 * @param str
	 * @return
	 */
	public static String getPinYinHeadChar(String str) {
		String convert = "";
		int size = str.length();
		if (size >= 2) {
			size = 2;
		}
		for (int j = 0; j < size; j++) {
			char word = str.charAt(j);
			String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
			if (pinyinArray != null) {
				convert += pinyinArray[0].charAt(0);
			} else {
				convert += word;
			}
		}
		return convert;
	}

	/**
	 * 请求图层数据
	 * @param url
	 */
	private void OkHttpJson(String url) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
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
						if (result != null) {
							drawDataToMap(result);
						}else {
							removePolygons();
							progressBar.setVisibility(View.GONE);
							tvToast.setVisibility(View.VISIBLE);
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									tvToast.setVisibility(View.GONE);
								}
							}, 1000);
						}
					}
				});
			}
		});
	}

	private void removeTexts() {
		for (int i = 0; i < texts.size(); i++) {
			texts.get(i).remove();
		}
		texts.clear();
	}

	/**
	 * 清除图层
	 */
	private void removePolygons() {
		for (int i = 0; i < polygons.size(); i++) {
			polygons.get(i).remove();
		}
		polygons.clear();
	}

	/**
	 * 清除边界线
	 */
	private void removePolylines() {
		for (int i = 0; i < polylines.size(); i++) {
			polylines.get(i).remove();
		}
		polylines.clear();
	}

	/**
	 * 清除人工站
	 */
	private void removePersonStations() {
		for (int i = 0; i < personStations.size(); i++) {
			personStations.get(i).remove();
		}
		personStations.clear();
	}

	/**
	 * 清除自动站
	 */
	private void removeAutoStations() {
		for (int i = 0; i < autoStations.size(); i++) {
			autoStations.get(i).remove();
		}
		autoStations.clear();
	}

	/**
	 * 绘制图层
	 */
	private void drawDataToMap(String result) {
		if (TextUtils.isEmpty(result) || aMap == null) {
			return;
		}
		removeTexts();
		removePolygons();
		removePolylines();
		removePersonStations();
		removeAutoStations();

		try {
			JSONObject obj = new JSONObject(result);
			JSONArray array = obj.getJSONArray("l");
			int length = array.length();
			double leftLat = 0;
			double leftLng = 0;
			double rightLat = 0;
			double rightLng = 0;
			for (int i = 0; i < length; i++) {
				JSONObject itemObj = array.getJSONObject(i);
				JSONArray c = itemObj.getJSONArray("c");
				int r = c.getInt(0);
				int g = c.getInt(1);
				int b = c.getInt(2);
				int a = (int) (c.getInt(3)*255*1.0);

				double centerLat = 0;
				double centerLng = 0;
				String p = itemObj.getString("p");
				if (!TextUtils.isEmpty(p)) {
					String[] points = p.split(";");
					PolygonOptions polygonOption = new PolygonOptions();
					polygonOption.fillColor(Color.argb(a, r, g, b));
					polygonOption.strokeColor(0xffd9d9d9);
					polygonOption.strokeWidth(1);
					for (int j = 0; j < points.length; j++) {
						String[] value = points[j].split(",");
						double lat = Double.valueOf(value[1]);
						double lng = Double.valueOf(value[0]);
						polygonOption.add(new LatLng(lat, lng));
						if (j == points.length/2) {
							centerLat = lat;
							centerLng = lng;
						}

						if (i == 0 && j == 0) {
							leftLat = lat;
							leftLng = lng;
							rightLat = lat;
							rightLng = lng;
						}
						if (leftLat >= lat) {
							leftLat = lat;
						}
						if (leftLng >= lng) {
							leftLng = lng;
						}
						if (rightLat <= lat) {
							rightLat = lat;
						}
						if (rightLng <= lng) {
							rightLng = lng;
						}
					}
					Polygon polygon = aMap.addPolygon(polygonOption);
					polygons.add(polygon);
				}

//				if (!itemObj.isNull("v")) {
//					double v = itemObj.getDouble("v");
//					TextOptions options = new TextOptions();
//					options.position(new LatLng(centerLat, centerLng));
//					options.fontColor(Color.BLACK);
//					options.fontSize(30);
//					options.text(v+"");
//					options.backgroundColor(Color.TRANSPARENT);
//					Text text = aMap.addText(options);
//					texts.add(text);
//				}
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
						aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
					} catch (ArrayIndexOutOfBoundsException e) {
						e.printStackTrace();
					}
				}
			}, 500);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		drawCity(adCode);
		progressBar.setVisibility(View.GONE);
	}

	private void drawCity(String adCode) {
		if (aMap == null) {
			return;
		}
		String result = CommonUtil.getFromAssets(mContext, "json/"+adCode+".json");
		if (!TextUtils.isEmpty(result)) {
			try {
				JSONObject obj = new JSONObject(result);
				JSONArray array = obj.getJSONArray("features");
				for (int i = 0; i < array.length(); i++) {
					JSONObject itemObj = array.getJSONObject(i);

					JSONObject geometry = itemObj.getJSONObject("geometry");
					JSONArray coordinates = geometry.getJSONArray("coordinates");
					for (int m = 0; m < coordinates.length(); m++) {
						JSONArray array1 = coordinates.getJSONArray(m);
						for (int k = 0; k < array1.length(); k++) {
							JSONArray array2 = array1.getJSONArray(k);
							PolylineOptions polylineOption = new PolylineOptions();
							polylineOption.width(2).color(0xffd9d9d9);
							for (int j = 0; j < array2.length(); j++) {
								JSONArray itemArray = array2.getJSONArray(j);
								double lng = itemArray.getDouble(0);
								double lat = itemArray.getDouble(1);
								polylineOption.add(new LatLng(lat, lng));
							}
							Polyline polyLine = aMap.addPolyline(polylineOption);
							polylines.add(polyLine);
						}
					}

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		//绘制人工站
		for (int i = 0; i < cityInfos.size(); i++) {
			FactDto dto = cityInfos.get(i);
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.layout_fact_value, null);
			TextView tvValue = (TextView) view.findViewById(R.id.tvValue);
			TextView tvName = (TextView) view.findViewById(R.id.tvName);
			if (dto.val >= 99999) {
				tvValue.setText("");
			}else {
				tvValue.setText(dto.val+"");
			}
			if (!TextUtils.isEmpty(dto.stationName)) {
				tvName.setText(dto.stationName);
			}
			MarkerOptions options = new MarkerOptions();
			options.title(dto.stationName);
			options.anchor(0.5f, 1.0f);
			options.position(new LatLng(dto.lat, dto.lng));
			options.icon(BitmapDescriptorFactory.fromView(view));
			Marker marker = aMap.addMarker(options);
			marker.setVisible(true);
			personStations.add(marker);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				finish();
				break;
			case R.id.tvControl:
				Intent intent = new Intent(mContext, FactProActivity.class);//雨情雨量o
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", data);
				intent.putExtras(bundle);
				startActivityForResult(intent, 1000);
				break;
			case R.id.ll1:
				if (b1) {//升序
					b1 = false;
					iv1.setImageResource(R.drawable.arrow_up);
					iv1.setVisibility(View.VISIBLE);
					iv2.setVisibility(View.INVISIBLE);
					iv3.setVisibility(View.INVISIBLE);
					Collections.sort(realDatas, new Comparator<FactDto>() {
						@Override
						public int compare(FactDto arg0, FactDto arg1) {
							if (TextUtils.isEmpty(arg0.stationName) || TextUtils.isEmpty(arg1.stationName)) {
								return 0;
							}else {
								return getPinYinHeadChar(arg0.stationName).compareTo(getPinYinHeadChar(arg1.stationName));
							}
						}
					});
				}else {//将序
					b1 = true;
					iv1.setImageResource(R.drawable.arrow_down);
					iv1.setVisibility(View.VISIBLE);
					iv2.setVisibility(View.INVISIBLE);
					iv3.setVisibility(View.INVISIBLE);
					Collections.sort(realDatas, new Comparator<FactDto>() {
						@Override
						public int compare(FactDto arg0, FactDto arg1) {
							if (TextUtils.isEmpty(arg0.stationName) || TextUtils.isEmpty(arg1.stationName)) {
								return -1;
							}else {
								return getPinYinHeadChar(arg1.stationName).compareTo(getPinYinHeadChar(arg0.stationName));
							}
						}
					});
				}
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
				break;
			case R.id.ll2:
				if (b2) {//升序
					b2 = false;
					iv2.setImageResource(R.drawable.arrow_up);
					iv1.setVisibility(View.INVISIBLE);
					iv2.setVisibility(View.VISIBLE);
					iv3.setVisibility(View.INVISIBLE);
					Collections.sort(realDatas, new Comparator<FactDto>() {
						@Override
						public int compare(FactDto arg0, FactDto arg1) {
							if (TextUtils.isEmpty(arg0.area1) || TextUtils.isEmpty(arg1.area1)) {
								return 0;
							}else {
								return getPinYinHeadChar(arg0.area1).compareTo(getPinYinHeadChar(arg1.area1));
							}
						}
					});
				}else {//将序
					b2 = true;
					iv2.setImageResource(R.drawable.arrow_down);
					iv1.setVisibility(View.INVISIBLE);
					iv2.setVisibility(View.VISIBLE);
					iv3.setVisibility(View.INVISIBLE);
					Collections.sort(realDatas, new Comparator<FactDto>() {
						@Override
						public int compare(FactDto arg0, FactDto arg1) {
							if (TextUtils.isEmpty(arg0.area1) || TextUtils.isEmpty(arg1.area1)) {
								return -1;
							}else {
								return getPinYinHeadChar(arg1.area1).compareTo(getPinYinHeadChar(arg0.area1));
							}
						}
					});
				}
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
				break;
			case R.id.ll3:
				if (b3) {//升序
					b3 = false;
					iv3.setImageResource(R.drawable.arrow_up);
					iv1.setVisibility(View.INVISIBLE);
					iv2.setVisibility(View.INVISIBLE);
					iv3.setVisibility(View.VISIBLE);
					Collections.sort(realDatas, new Comparator<FactDto>() {
						@Override
						public int compare(FactDto arg0, FactDto arg1) {
							return Double.valueOf(arg0.val).compareTo(Double.valueOf(arg1.val));
						}
					});
				}else {//将序
					b3 = true;
					iv3.setImageResource(R.drawable.arrow_down);
					iv1.setVisibility(View.INVISIBLE);
					iv2.setVisibility(View.INVISIBLE);
					iv3.setVisibility(View.VISIBLE);
					Collections.sort(realDatas, new Comparator<FactDto>() {
						@Override
						public int compare(FactDto arg0, FactDto arg1) {
							return Double.valueOf(arg1.val).compareTo(Double.valueOf(arg0.val));
						}
					});
				}
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case 1000:
					adCode = intent.getExtras().getString("adCode");
					addColumn(data);
					break;
			}
		}
	}

}
