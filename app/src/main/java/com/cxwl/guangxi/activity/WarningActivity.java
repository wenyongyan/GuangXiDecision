package com.cxwl.guangxi.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
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
import com.amap.api.services.district.DistrictSearch.OnDistrictSearchListener;
import com.amap.api.services.district.DistrictSearchQuery;
import com.cxwl.guangxi.R;
import com.cxwl.guangxi.adapter.WarningAdapter;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.dto.WarningDto;
import com.cxwl.guangxi.manager.DBManager;
import com.cxwl.guangxi.utils.CommonUtil;
import com.cxwl.guangxi.utils.CustomHttpClient;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 预警
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat") 
public class WarningActivity extends BaseActivity implements OnClickListener, OnMapClickListener, 
OnMarkerClickListener, InfoWindowAdapter, OnCameraChangeListener, OnDistrictSearchListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ImageView ivControl = null;
	private RelativeLayout rePrompt = null;
	private TextView tvPrompt = null;//没有数据时提示
	private TextView tvPro = null;
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private Marker selectMarker = null;
//	private String warningUrl = "http://decision.tianqi.cn/alarm12379/grepalarm2.php?areaid=45";//预警地址
	private String warningUrl = "http://decision-admin.tianqi.cn/Home/extra/getwarnsguangxi";//预警地址
	private List<WarningDto> warningList = new ArrayList<WarningDto>();
	private List<WarningDto> proList = new ArrayList<WarningDto>();//省级预警
	private RelativeLayout reList = null;
	private ListView cityListView = null;
	private WarningAdapter cityAdapter = null;
	private ProgressBar progressBar = null;
	private List<Marker> markerList = new ArrayList<Marker>();
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.warning);
		mContext = this;
		initAmap(savedInstanceState);
		initWidget();
		initListView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		ivControl = (ImageView) findViewById(R.id.ivControl);
		ivControl.setOnClickListener(this);
		rePrompt = (RelativeLayout) findViewById(R.id.rePrompt);
		tvPrompt = (TextView) findViewById(R.id.tvPrompt);
		tvPro = (TextView) findViewById(R.id.tvPro);
		tvPro.setOnClickListener(this);
		reList = (RelativeLayout) findViewById(R.id.reList);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		drawDistrict("450000");
		
		asyncQuery(warningUrl);
    }
	
	/**
	 * 绘制区域
	 */
	private void drawDistrict(String keywords) {
		DistrictSearch search = new DistrictSearch(mContext);
		DistrictSearchQuery query = new DistrictSearchQuery();
		query.setKeywords(keywords);//传入关键字
		query.setKeywordsLevel(DistrictSearchQuery.KEYWORDS_DISTRICT);
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
		List<DistrictItem> districtItems = districtResult.getDistrict();
		for (int i = 0; i < districtItems.size(); i++) {
			DistrictItem item = districtResult.getDistrict().get(i);
			if (item == null) {
				return;
			}
			String[] polyStr = item.districtBoundary();
			if (polyStr == null || polyStr.length == 0) {
				return;
			}

			for (String str : polyStr) {
				String[] lat = str.split(";");
				PolylineOptions polylineOption = new PolylineOptions();

				for (String latstr : lat) {
					String[] lats = latstr.split(",");
					polylineOption.add(new LatLng(Double.parseDouble(lats[1]), Double.parseDouble(lats[0])));
				}

				polylineOption.width(8).color(getResources().getColor(R.color.title_bg));
				aMap.addPolyline(polylineOption);
			}
		}

	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		cityListView = (ListView) findViewById(R.id.listView);
		cityAdapter = new WarningAdapter(mContext, warningList, false);
		cityListView.setAdapter(cityAdapter);
		cityListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				WarningDto data = warningList.get(arg2);
				Intent intentDetail = new Intent(mContext, WarningDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", data);
				intentDetail.putExtras(bundle);
				startActivity(intentDetail);
			}
		});
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
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 6.4f));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMapClickListener(this);
		aMap.setOnMarkerClickListener(this);
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
		private List<NameValuePair> nvpList = new ArrayList<>();
		
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
			progressBar.setVisibility(View.GONE);
			if (requestResult != null) {
				try {
					JSONObject object = new JSONObject(requestResult);
					if (object != null) {
						HashMap<String, List<WarningDto>> map = new HashMap<String, List<WarningDto>>();
						map.clear();
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
								
								dto.item0 = item0;
								dto.provinceId = item0.substring(0, 2);
								dto.type = item2.substring(0, 5);
								dto.color = item2.substring(5, 7);
								dto.time = item1;
								dto.lng = tempArray.optString(2);
								dto.lat = tempArray.optString(3);
								dto.name = tempArray.optString(0);
								
								if (!dto.name.contains("解除")) {
									warningList.add(dto);
									
									String key = dto.lat+","+dto.lng;
									if (map.containsKey(key)) {
										map.get(key).add(0, dto);
									}else {
										List<WarningDto> mapList = new ArrayList<WarningDto>();
										mapList.clear();
										mapList.add(dto);
										map.put(key, mapList);
									}
								}
							}
							
							String count = warningList.size()+"";
							if (count != null) {
								if (count.equals("0")) {
									tvPrompt.setText("广西暂无预警"+"\n"+sdf.format(new Date())+"更新");
									rePrompt.setVisibility(View.VISIBLE);
									ivControl.setVisibility(View.GONE);
									return;
								}else {
									tvPrompt.setText("广西共有" + count + "条预警");
									rePrompt.setVisibility(View.VISIBLE);
									ivControl.setVisibility(View.VISIBLE);
									setWarningInfo();
								}
							}
							
							if (cityAdapter != null) {
								cityAdapter.notifyDataSetChanged();
							}
							
							removeMarkers();
							addMarkersToMap(map);
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
	
	private void setWarningInfo() {
		int provinceCount = 0;//省级预警条数
		int cityCount = 0;//地市级预警条数
		int districtCount = 0;//区县级预警条数
		String types = "";
		proList.clear();
		for (int i = 0; i < warningList.size(); i++) {
			WarningDto dto = warningList.get(i);
			
			if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
				provinceCount += 1;
				proList.add(dto);
			}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
				cityCount += 1;
			}else {
				districtCount += 1;
			}
			
			if (!types.contains(dto.type)) {
				types = types + dto.type + ",";
			}
		}
		if (!TextUtils.isEmpty(types) && types.contains(",")) {
			List<WarningDto> typeList = new ArrayList<WarningDto>();
			String[] typeArray = types.split(",");
			for (int i = 0; i < typeArray.length; i++) {
				WarningDto dto = new WarningDto();
				dto.type = typeArray[i];
				for (int j = 0; j < warningList.size(); j++) {
					if (TextUtils.equals(typeArray[i], warningList.get(j).type)) {
						dto.itemList.add(warningList.get(j));
					}
				}
				typeList.add(dto);
			}
			
			String content = "";
			for (int i = 0; i < typeList.size(); i++) {
				String name = queryWarningType(typeList.get(i).type);
				content = content + name+":";
				String redColor ="", orangeColor = "", yellowColor = "", blueColor = "";
				int red = 0, orange = 0, yellow = 0, blue = 0;
				for (int j = 0; j < typeList.get(i).itemList.size(); j++) {
					WarningDto dto = typeList.get(i).itemList.get(j);
					if (TextUtils.equals(dto.color, "04")) {
						red++;
					}else if (TextUtils.equals(dto.color, "03")) {
						orange++;
					}else if (TextUtils.equals(dto.color, "02")) {
						yellow++;
					}else if (TextUtils.equals(dto.color, "01")) {
						blue++;
					}
				}
				if (red > 0) {
					redColor = red+"红"+" ";
				}
				if (orange > 0) {
					orangeColor = orange+"橙"+" ";
				}
				if (yellow > 0) {
					yellowColor = yellow+"黄"+" ";
				}
				if (blue > 0) {
					blueColor = blue+"蓝"+" ";
				}
				
				content = content+redColor+orangeColor+yellowColor+blueColor+"; ";
			}
			
			content = "省级预警"+provinceCount+"条; "+"地市级预警"+cityCount+"条; "+"区县级预警"+districtCount+"条; "+content;
			
			tvPrompt.setText(tvPrompt.getText().toString()+"（"+content+"）"+"\n"+sdf.format(new Date())+"更新");
			
			if (provinceCount != 0) {
				tvPro.setVisibility(View.VISIBLE);
			}
		}
	}
	
	/**
	 * 初始化数据库
	 */
	private String queryWarningType(String type) {
		String name = "";
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = null;
		cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME2 + " where WarningId like " + "\"%" + type + "%\"",null);
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			name = cursor.getString(cursor.getColumnIndex("WarningName"));
			name = name.substring(0, name.length()-6);
		}
		return name;
	}
	
	private void removeMarkers() {
		for (int i = 0; i < markerList.size(); i++) {
			markerList.get(i).remove();
		}
		markerList.clear();
	}
	
	/**
	 * 在地图上添加marker
	 */
	private void addMarkersToMap(HashMap<String, List<WarningDto>> map) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (String key : map.keySet()) {
			List<WarningDto> mapList = map.get(key);
			for (int i = 0; i < mapList.size(); i++) {
				WarningDto dto = mapList.get(i);
		    	MarkerOptions optionsTemp = new MarkerOptions();
		    	optionsTemp.title(dto.lat);
		    	optionsTemp.snippet(dto.lng);
		    	if (!TextUtils.isEmpty(dto.lat) && !TextUtils.isEmpty(dto.lng)) {
		    		double lat = Double.valueOf(dto.lat);
		    		double lng = Double.valueOf(dto.lng);
		    		int size = mapList.size();
		    		if (size == 1) {
		    			lat = Double.valueOf(dto.lat);
			    		lng = Double.valueOf(dto.lng);
					}else if (size == 2) {
						if (i == 0) {
							lat = Double.valueOf(dto.lat);
				    		lng = Double.valueOf(dto.lng);
						}else if (i == 1) {
							lat = Double.valueOf(dto.lat);
							lng = lng+0.05;
						}
					}
		    		optionsTemp.position(new LatLng(lat, lng));
		    	}
		    	
		    	float anchorX = 0.5f;
		    	float anchorY = 0.5f;
//		    	if (size == 1) {
//		    		anchorX = 0.5f;
//					anchorY = 0.5f;
//				}else if (size == 2) {
//					if (i == 0) {
//						anchorX = 0.0f;
//						anchorY = 0.5f;
//					}else if (i == 1) {
//						anchorX = 1.0f;
//						anchorY = 0.5f;
//					}
//				}else if (size == 3) {
//					if (i == 0) {
//						anchorX = 0.0f;
//						anchorY = 1.0f;
//					}else if (i == 1) {
//						anchorX = 1.0f;
//						anchorY = 1.0f;
//					}else if (i == 2) {
//						anchorX = 0.0f;
//						anchorY = 0.0f;
//					}
//				}else if (size == 4) {
//					if (i == 0) {
//						anchorX = 0.0f;
//						anchorY = 1.0f;
//					}else if (i == 1) {
//						anchorX = 1.0f;
//						anchorY = 1.0f;
//					}else if (i == 2) {
//						anchorX = 0.0f;
//						anchorY = 0.0f;
//					}else if (i == 3) {
//						anchorX = 1.0f;
//						anchorY = 0.0f;
//					}
//				}
		    	optionsTemp.anchor(anchorX, anchorY);
//		    	optionsTemp.anchor((float) (1.0f-new Random().nextDouble()*2), (float) (1.0f-new Random().nextDouble()*2));
		    	
		    	View view = inflater.inflate(R.layout.warning_marker_view, null);
		    	ImageView ivMarker = (ImageView) view.findViewById(R.id.ivMarker);
		    	
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
		    	ivMarker.setImageBitmap(bitmap);
		    	optionsTemp.icon(BitmapDescriptorFactory.fromView(view));
		    	Marker marker = aMap.addMarker(optionsTemp);
		    	markerList.add(marker);
			}
		}
	}
	
	@Override
	public void onMapClick(LatLng arg0) {
		if (selectMarker != null) {
			selectMarker.hideInfoWindow();
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		selectMarker = marker;
		marker.showInfoWindow();
		return true;
	}
	
	@Override
	public View getInfoContents(final Marker marker) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.warning_marker_info, null);
		ListView mListView = null;
		WarningAdapter mAdapter = null;
		final List<WarningDto> infoList = new ArrayList<WarningDto>();
		
		addInfoList(warningList, marker, infoList);
		
		mListView = (ListView) view.findViewById(R.id.listView);
		mAdapter = new WarningAdapter(mContext, infoList, true);
		mListView.setAdapter(mAdapter);
		LayoutParams params = mListView.getLayoutParams();
		if (infoList.size() == 1) {
			params.height = (int) CommonUtil.dip2px(mContext, 50);
		}else if (infoList.size() == 2) {
			params.height = (int) CommonUtil.dip2px(mContext, 100);
		}else if (infoList.size() > 2){
			params.height = (int) CommonUtil.dip2px(mContext, 150);
		}
		mListView.setLayoutParams(params);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentDetail(infoList.get(arg2));
			}
		});
		return view;
	}
	
	private void intentDetail(WarningDto data) {
		Intent intentDetail = new Intent(mContext, WarningDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("data", data);
		intentDetail.putExtras(bundle);
		startActivity(intentDetail);
	}
	
	private void addInfoList(List<WarningDto> list, Marker marker, List<WarningDto> infoList) {
		for (int i = 0; i < list.size(); i++) {
			WarningDto dto = list.get(i);
			if (TextUtils.equals(marker.getTitle(), dto.lat) && TextUtils.equals(marker.getSnippet(), dto.lng)) {
				infoList.add(dto);
			}
		}
	}
	
	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @param flag false为显示map，true为显示list
	 */
	private void startAnimation(boolean flag, final RelativeLayout reList) {
		//列表动画
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation = null;
		if (flag == false) {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f,
					Animation.RELATIVE_TO_SELF,0f);
		}else {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f);
		}
		animation.setDuration(400);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		reList.startAnimation(animationSet);
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				reList.clearAnimation();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivControl:
			if (reList.getVisibility() == View.GONE) {
				startAnimation(false, reList);
				reList.setVisibility(View.VISIBLE);
				ivControl.setImageResource(R.drawable.iv_warning_map);
			}else {
				startAnimation(true, reList);
				reList.setVisibility(View.GONE);
				ivControl.setImageResource(R.drawable.iv_warning_list);
			}
			break;
		case R.id.tvPro:
			Intent intent = null;
			if (proList.size() > 1) {
				intent = new Intent(mContext, HeadWarningActivity.class);
				intent.putParcelableArrayListExtra("warningList", (ArrayList<? extends Parcelable>) proList);
				startActivity(intent);
			}else if (proList.size() == 1){
				intent = new Intent(mContext, WarningDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", proList.get(0));
				intent.putExtras(bundle);
				startActivity(intent);
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		// TODO Auto-generated method stub
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Point leftPoint = new Point(0, dm.heightPixels);
		Point rightPoint = new Point(dm.widthPixels, 0);
		LatLng leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
		LatLng rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);
		
//		CONST.centerLat = (leftlatlng.latitude+rightLatlng.latitude)/2;
//		CONST.centerLng = (leftlatlng.longitude+rightLatlng.longitude)/2;
	}

}
