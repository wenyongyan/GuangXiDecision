package com.cxwl.guangxi.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.adapter.FactAdapter;
import com.cxwl.guangxi.adapter.FactSelectAdapter;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.dto.FactDto;
import com.cxwl.guangxi.manager.FactManager;
import com.cxwl.guangxi.manager.FactManager.FactListener;
import com.cxwl.guangxi.manager.RadarManager.RadarListener;
import com.cxwl.guangxi.utils.CommonUtil;
import com.cxwl.guangxi.utils.CustomHttpClient;

import net.tsz.afinal.FinalBitmap;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 气温实况、风速实况、降水实况
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class FactActivity extends BaseActivity implements OnClickListener, FactListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private List<FactDto> selectList = new ArrayList<>();
	private List<FactDto> imgs = new ArrayList<>();
	private List<FactDto> rankList = new ArrayList<>();
	private List<FactDto> saveRankList = new ArrayList<>();
	private ImageView imageView = null;
	private FactManager mRadarManager = null;
	private FactThread mFactThread = null;
	private static final int HANDLER_SHOW_RADAR = 1;
	private static final int HANDLER_PROGRESS = 2;
	private static final int HANDLER_LOAD_FINISHED = 3;
	private static final int HANDLER_PAUSE = 4;
	private LinearLayout llSeekBar = null;
	private ImageView ivPlay = null;
	private SeekBar seekBar = null;
	private TextView tvTime = null;
	private ScrollView scrollView = null;
	private TextView tvPercent = null;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private ListView listView = null;
	private FactAdapter factAdapter = null;
	private TextView tvSelect = null;
	private ImageView ivSelect = null;
	private RelativeLayout reListView = null;
	private ListView cityListView = null;
	private FactSelectAdapter cityAdapter = null;
	private TextView tvUnit = null;
	private String selected = "1";//选中
	private String unselected = "0";//未选中
	private TextView tvPublishTime = null;
	private LinearLayout llBottom = null;
	private LinearLayout llStation = null;
	private LinearLayout llSequnce = null;
	private TextView tvStation = null;
	private TextView tvSequnce = null;
	private ImageView ivSequnce = null;
	private boolean isAll = true;//默认为全国
	private boolean sequnce = true;//true为将序、false为升序
	private LinearLayout llSelect = null;
	private TextView tvAll, tvNation;
	private String colomnId = null;
	private String TEMPID = "107";
	private String WINDID = "106";
	private String RAINID = "105";
	private String flag = null;
	private String TEMP_08_MAX = "08_tmax";
	private String TEMP_08_MIN = "08_tmin";
	private String TEMP_20_MAX = "20_tmax";
	private String TEMP_20_MIN = "20_tmin";
	private String RAIN_08 = "08";
	private String RAIN_20 = "20";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fact);
		mContext = this;
		showDialog();
		initWidget();
		initSelectListView();
		initStationListView();
	}

	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setFocusable(true);
		tvTitle.setFocusableInTouchMode(true);
		tvTitle.requestFocus();
		imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setOnClickListener(this);
		ivPlay = (ImageView) findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(seekbarListener);
		seekBar.setProgress(97);
		tvTime = (TextView) findViewById(R.id.tvTime);
		llSeekBar = (LinearLayout) findViewById(R.id.llSeekBar);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		tvPercent = (TextView) findViewById(R.id.tvPercent);
		tvSelect = (TextView) findViewById(R.id.tvSelect);
		tvSelect.setOnClickListener(this);
		ivSelect = (ImageView) findViewById(R.id.ivSelect);
		ivSelect.setOnClickListener(this);
		reListView = (RelativeLayout) findViewById(R.id.reListView);
		tvUnit = (TextView) findViewById(R.id.tvUnit);
		tvPublishTime = (TextView) findViewById(R.id.tvPublishTime);
		llBottom = (LinearLayout) findViewById(R.id.llBottom);
		llStation = (LinearLayout) findViewById(R.id.llStation);
		llStation.setOnClickListener(this);
		llSequnce = (LinearLayout) findViewById(R.id.llSequnce);
		llSequnce.setOnClickListener(this);
		tvStation = (TextView) findViewById(R.id.tvStation);
		tvSequnce = (TextView) findViewById(R.id.tvSequnce);
		ivSequnce = (ImageView) findViewById(R.id.ivSequnce);
		llSelect = (LinearLayout) findViewById(R.id.llSelect);
		tvAll = (TextView) findViewById(R.id.tvAll);
		tvAll.setOnClickListener(this);
		tvNation = (TextView) findViewById(R.id.tvNation);
		tvNation.setOnClickListener(this);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		LayoutParams params = llSelect.getLayoutParams();
		params.width = dm.widthPixels/2;
		llSelect.setLayoutParams(params);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		colomnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		if (TextUtils.equals(colomnId, TEMPID)) {//温度
			tvUnit.setText("温度（℃）");
		}else if (TextUtils.equals(colomnId, WINDID)) {//风速风向
			tvUnit.setText("风速（m/s）");
		}else if (TextUtils.equals(colomnId, RAINID)) {//降水
			tvUnit.setText("降水（mm）");
		}
		
		mRadarManager = new FactManager(mContext);
		
		String url = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(url)) {
			asyncQuery(url);
		}
	}
	
	/**
	 * 初始化筛选listview
	 */
	private void initSelectListView() {
		cityListView = (ListView) findViewById(R.id.cityListView);
		cityAdapter = new FactSelectAdapter(mContext, selectList);
		cityListView.setAdapter(cityAdapter);
		cityListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				FactDto dto = selectList.get(arg2);
				if (TextUtils.equals(tvSelect.getText().toString(), dto.itemName)) {
					return;
				}
				flag = dto.flag;
				factAdapter.flag = dto.flag;
				tvSelect.setText(dto.itemName);
				closeList(reListView);
				
				imgs.clear();
				imgs.addAll(dto.imgs);
				
				for (int i = 0; i < imgs.size(); i++) {
					if (i == imgs.size()-1) {
						imgs.get(imgs.size()-1).isSelect = selected;
					}else {
						imgs.get(i).isSelect = unselected;
					}
				}
				
				List<FactDto> tempList = new ArrayList<FactDto>();
				tempList.clear();
				if (isAll && sequnce) {//全部，将序
					tempList.addAll(dto.nationList);
					tempList.addAll(dto.topList);
					Collections.sort(tempList, new Comparator<FactDto>() {
						@Override
						public int compare(FactDto arg0, FactDto arg1) {
							if (TextUtils.equals(colomnId, TEMPID)) {//温度
								return Float.valueOf(arg1.temp).compareTo(Float.valueOf(arg0.temp));
							}else if (TextUtils.equals(colomnId, WINDID)) {//风速风向
								return Float.valueOf(arg1.windSpeed).compareTo(Float.valueOf(arg0.windSpeed));
							}else if (TextUtils.equals(colomnId, RAINID)) {//降水
								return Float.valueOf(arg1.rain).compareTo(Float.valueOf(arg0.rain));
							}
							return 0;
						}
					});
				}else if (isAll && !sequnce) {//全部，升序
					tempList.addAll(dto.nationList);
					tempList.addAll(dto.bottomList);
					Collections.sort(tempList, new Comparator<FactDto>() {
						@Override
						public int compare(FactDto arg0, FactDto arg1) {
							if (TextUtils.equals(colomnId, TEMPID)) {//温度
								return Float.valueOf(arg0.temp).compareTo(Float.valueOf(arg1.temp));
							}else if (TextUtils.equals(colomnId, WINDID)) {//风速风向
								return Float.valueOf(arg0.windSpeed).compareTo(Float.valueOf(arg1.windSpeed));
							}else if (TextUtils.equals(colomnId, RAINID)) {//降水
								return Float.valueOf(arg0.rain).compareTo(Float.valueOf(arg1.rain));
							}
							return 0;
						}
					});
				}else if (!isAll && sequnce) {//国家站，将序
					tempList.addAll(dto.nationList);
					Collections.sort(tempList, new Comparator<FactDto>() {
						@Override
						public int compare(FactDto arg0, FactDto arg1) {
							if (TextUtils.equals(colomnId, TEMPID)) {//温度
								return Float.valueOf(arg1.temp).compareTo(Float.valueOf(arg0.temp));
							}else if (TextUtils.equals(colomnId, WINDID)) {//风速风向
								return Float.valueOf(arg1.windSpeed).compareTo(Float.valueOf(arg0.windSpeed));
							}else if (TextUtils.equals(colomnId, RAINID)) {//降水
								return Float.valueOf(arg1.rain).compareTo(Float.valueOf(arg0.rain));
							}
							return 0;
						}
					});
				}else if (!isAll && !sequnce) {//国家站，升序
					tempList.addAll(dto.nationList);
					Collections.sort(tempList, new Comparator<FactDto>() {
						@Override
						public int compare(FactDto arg0, FactDto arg1) {
							if (TextUtils.equals(colomnId, TEMPID)) {//温度
								return Float.valueOf(arg0.temp).compareTo(Float.valueOf(arg1.temp));
							}else if (TextUtils.equals(colomnId, WINDID)) {//风速风向
								return Float.valueOf(arg0.windSpeed).compareTo(Float.valueOf(arg1.windSpeed));
							}else if (TextUtils.equals(colomnId, RAINID)) {//降水
								return Float.valueOf(arg0.rain).compareTo(Float.valueOf(arg1.rain));
							}
							return 0;
						}
					});
				}
				
				rankList.clear();
				rankList.addAll(tempList);
				
				saveRankList.clear();
				saveRankList.addAll(tempList);
				
				if (saveRankList.size() > 0) {
					rankList.clear();
					if (sequnce == false) {
						if (isAll) {
							for (int i = saveRankList.size()-1; i >= 0; i--) {
								FactDto dtod = saveRankList.get(i);
								rankList.add(dtod);
							}
						}else {
							for (int i = saveRankList.size()-1; i >= 0; i--) {
								FactDto dtod = saveRankList.get(i);
								if (TextUtils.equals(dtod.id.substring(0, 1), "5")) {
									rankList.add(dtod);
								}
							}
						}
					}else {
						if (isAll) {
							for (int i = 0; i < saveRankList.size(); i++) {
								FactDto dtod = saveRankList.get(i);
								rankList.add(dtod);
							}
						}else {
							for (int i = 0; i < saveRankList.size(); i++) {
								FactDto dtod = saveRankList.get(i);
								if (TextUtils.equals(dtod.id.substring(0, 1), "5")) {
									rankList.add(dtod);
								}
							}
						}
					}
				}
				
				try {
					if (!TextUtils.isEmpty(dto.time)) {
						tvPublishTime.setText("发布时间："+sdf3.format(sdf1.parse(dto.time)));
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				if (factAdapter != null) {
					factAdapter.notifyDataSetChanged();
					CommonUtil.setListViewHeightBasedOnChildren(listView);
					
					scrollView.post(new Runnable() {
						@Override
						public void run() {
							scrollView.scrollTo(0, 0);
						}
					});
				}
			
				if (imgs.size() > 0) {
					FactDto imgDto = imgs.get(imgs.size()-1);
					if (!TextUtils.isEmpty(imgDto.imgUrl)) {
						FinalBitmap finalBitmap = FinalBitmap.create(mContext);
						finalBitmap.display(imageView, imgDto.imgUrl, null, 0);
						try {
							tvTime.setText(sdf2.format(sdf1.parse(imgDto.time)));
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
				
			}
		});
	}
	
	private void openMenuAnimation(RelativeLayout reContainer) {
		if (reContainer.getVisibility() == View.GONE) {
			openList(reContainer);
		}else {
			closeList(reContainer);
		}
	}
	
	private void openList(RelativeLayout reContainer) {
		rightControlAnimation(false, reContainer);
		reContainer.setVisibility(View.VISIBLE);
		ivSelect.setImageResource(R.drawable.iv_arrow_white_down);
	}
	
	private void closeList(RelativeLayout reContainer) {
		rightControlAnimation(true, reContainer);
		reContainer.setVisibility(View.GONE);
		ivSelect.setImageResource(R.drawable.iv_arrow_white_top);
	}
	
	private void rightControlAnimation(final boolean flag, final RelativeLayout layout) {
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation = null;
		if (flag == false) {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, -1.0f, 
					Animation.RELATIVE_TO_SELF, 0f);
		}else {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0,
					Animation.RELATIVE_TO_SELF,-1.0f);
		}
		animation.setDuration(300);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		layout.startAnimation(animationSet);
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				layout.clearAnimation();
			}
		});
	}
	
	/**
	 * 初始化站点列表
	 */
	private void initStationListView() {
		listView = (ListView) findViewById(R.id.listView);
		factAdapter = new FactAdapter(mContext, rankList, colomnId);
		listView.setAdapter(factAdapter);
		CommonUtil.setListViewHeightBasedOnChildren(listView);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final FactDto dto = rankList.get(arg2);
				if (TextUtils.equals(factAdapter.selectId, dto.id)) {
					return;
				}
				
				factAdapter.selectId = dto.id;
//				factAdapter.notifyDataSetChanged();
//				CommonUtil.setListViewHeightBasedOnChildren(listView);
				String detailUrl = "https://decision-admin.tianqi.cn/Home/Work/nnGetLiveStation/id/"+dto.id;
				if (TextUtils.equals(colomnId, TEMPID)) {//温度
					if (TextUtils.equals(flag, TEMP_08_MAX) || TextUtils.equals(flag, TEMP_08_MIN)) {
						detailUrl = "https://decision-admin.tianqi.cn/Home/Work/nnGetLiveStation08/id/"+dto.id;
					}else if (TextUtils.equals(flag, TEMP_20_MAX) || TextUtils.equals(flag, TEMP_20_MIN)) {
						detailUrl = "https://decision-admin.tianqi.cn/Home/Work/nnGetLiveStation20/id/"+dto.id;
					}
				}else if (TextUtils.equals(colomnId, RAINID)) {//雨量
					if (TextUtils.equals(flag, RAIN_08)) {
						detailUrl = "https://decision-admin.tianqi.cn/Home/Work/nnGetLiveStation08/id/"+dto.id;
					}else if (TextUtils.equals(flag, RAIN_20)) {
						detailUrl = "https://decision-admin.tianqi.cn/Home/Work/nnGetLiveStation20/id/"+dto.id;
					}
				}
				asyncQueryStation(detailUrl);
			}
		});
	}
	
	/**
	 * 获取服务材料数据
	 */
	private void asyncQuery(String url) {
		//异步请求数据
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(10000);
		task.execute(url);
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
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					selectList.clear();
					JSONArray array = new JSONArray(result);
					for (int i = 0; i < array.length(); i++) {
						FactDto dto = new FactDto();
						JSONObject obj = array.getJSONObject(i);
						
						if (!obj.isNull("id")) {
							dto.id = obj.getString("id");
						}
						
						if (!obj.isNull("name")) {
							dto.itemName = obj.getString("name");
						}
						
						if (!obj.isNull("flag")) {
							dto.flag = obj.getString("flag");
						}
						
						if (!obj.isNull("time")) {
							dto.time = obj.getString("time");
						}
						
						if (!obj.isNull("imgs")) {
							JSONArray imgsArray = obj.getJSONArray("imgs");
							for (int j = 0; j < imgsArray.length(); j++) {
								FactDto imgDto = new FactDto();
								imgDto.imgUrl = imgsArray.getString(j);
								
								if (j == 0) {
									imgDto.isSelect = "1";
								}else {
									imgDto.isSelect = "0";
								}
								
								imgDto.time = imgDto.imgUrl.substring(imgDto.imgUrl.length()-23, imgDto.imgUrl.indexOf(".png"));
								dto.imgs.add(imgDto);
							}
						}
						
						//国家站
						if (!obj.isNull("rank")) {
							JSONArray rankArray = obj.getJSONArray("rank");
							for (int j = 0; j < rankArray.length(); j++) {
								FactDto rankDto = new FactDto();
								JSONObject itemObj = rankArray.getJSONObject(j);
								if (!itemObj.isNull("id")) {
									rankDto.id = itemObj.getString("id");
								}
								if (!itemObj.isNull("name")) {
									rankDto.name = itemObj.getString("name");
								}
								if (!itemObj.isNull("temp")) {
									String temp = itemObj.getString("temp");
									float fTemp = 0;
									if (!TextUtils.isEmpty(temp) && temp.contains(".")) {
										fTemp  = Float.valueOf(temp);
									}
									rankDto.temp = fTemp+"";
								}
								if (!itemObj.isNull("wind_speed")) {
									String speed = itemObj.getString("wind_speed");
									float fSpeed = 0;
									if (!TextUtils.isEmpty(speed) && speed.contains(".")) {
										fSpeed = Float.valueOf(speed);
									}
									rankDto.windSpeed = fSpeed+"";
								}
								if (!itemObj.isNull("wind_direction")) {
									rankDto.windDir = itemObj.getString("wind_direction");
								}
								if (!itemObj.isNull("rain")) {
									String rain = itemObj.getString("rain");
									float fRain = 0;
									if (!TextUtils.isEmpty(rain) && rain.contains(".")) {
										fRain = Float.valueOf(rain);
									}
									rankDto.rain = fRain+"";
								}
								dto.nationList.add(rankDto);
							}
						}
						
						//前100自动站
						if (!obj.isNull("rank1")) {
							JSONArray rankArray = obj.getJSONArray("rank1");
							for (int j = 0; j < rankArray.length(); j++) {
								FactDto rankDto = new FactDto();
								JSONObject itemObj = rankArray.getJSONObject(j);
								if (!itemObj.isNull("id")) {
									rankDto.id = itemObj.getString("id");
								}
								if (!itemObj.isNull("name")) {
									rankDto.name = itemObj.getString("name");
								}
								if (!itemObj.isNull("temp")) {
									String temp = itemObj.getString("temp");
									float fTemp = 0;
									if (!TextUtils.isEmpty(temp) && temp.contains(".")) {
										fTemp  = Float.valueOf(temp);
									}
									rankDto.temp = fTemp+"";
								}
								if (!itemObj.isNull("wind_speed")) {
									String speed = itemObj.getString("wind_speed");
									float fSpeed = 0;
									if (!TextUtils.isEmpty(speed) && speed.contains(".")) {
										fSpeed = Float.valueOf(speed);
									}
									rankDto.windSpeed = fSpeed+"";
								}
								if (!itemObj.isNull("wind_direction")) {
									rankDto.windDir = itemObj.getString("wind_direction");
								}
								if (!itemObj.isNull("rain")) {
									String rain = itemObj.getString("rain");
									float fRain = 0;
									if (!TextUtils.isEmpty(rain) && rain.contains(".")) {
										fRain = Float.valueOf(rain);
									}
									rankDto.rain = fRain+"";
								}
								dto.topList.add(rankDto);
							}
						}
						
						//后100自动站
						if (!obj.isNull("rank2")) {
							JSONArray rankArray = obj.getJSONArray("rank2");
							for (int j = 0; j < rankArray.length(); j++) {
								FactDto rankDto = new FactDto();
								JSONObject itemObj = rankArray.getJSONObject(j);
								if (!itemObj.isNull("id")) {
									rankDto.id = itemObj.getString("id");
								}
								if (!itemObj.isNull("name")) {
									rankDto.name = itemObj.getString("name");
								}
								if (!itemObj.isNull("temp")) {
									String temp = itemObj.getString("temp");
									float fTemp = 0;
									if (!TextUtils.isEmpty(temp) && temp.contains(".")) {
										fTemp  = Float.valueOf(temp);
									}
									rankDto.temp = fTemp+"";
								}
								if (!itemObj.isNull("wind_speed")) {
									String speed = itemObj.getString("wind_speed");
									float fSpeed = 0;
									if (!TextUtils.isEmpty(speed) && speed.contains(".")) {
										fSpeed = Float.valueOf(speed);
									}
									rankDto.windSpeed = fSpeed+"";
								}
								if (!itemObj.isNull("wind_direction")) {
									rankDto.windDir = itemObj.getString("wind_direction");
								}
								if (!itemObj.isNull("rain")) {
									String rain = itemObj.getString("rain");
									float fRain = 0;
									if (!TextUtils.isEmpty(rain) && rain.contains(".")) {
										fRain = Float.valueOf(rain);
									}
									rankDto.rain = fRain+"";
								}
								dto.bottomList.add(rankDto);
							}
						}
						
						selectList.add(dto);
						
						if (i == 0) {
							imgs.clear();
							imgs.addAll(dto.imgs);
							
							List<FactDto> tempList = new ArrayList<FactDto>();
							tempList.clear();
							if (isAll && sequnce) {//全部，将序
								tempList.addAll(dto.nationList);
								tempList.addAll(dto.topList);
								Collections.sort(tempList, new Comparator<FactDto>() {
									@Override
									public int compare(FactDto arg0, FactDto arg1) {
										if (TextUtils.equals(colomnId, TEMPID)) {//温度
											return Float.valueOf(arg1.temp).compareTo(Float.valueOf(arg0.temp));
										}else if (TextUtils.equals(colomnId, WINDID)) {//风速风向
											return Float.valueOf(arg1.windSpeed).compareTo(Float.valueOf(arg0.windSpeed));
										}else if (TextUtils.equals(colomnId, RAINID)) {//降水
											return Float.valueOf(arg1.rain).compareTo(Float.valueOf(arg0.rain));
										}
										return 0;
									}
								});
							}else if (isAll && !sequnce) {//全部，升序
								tempList.addAll(dto.nationList);
								tempList.addAll(dto.bottomList);
								Collections.sort(tempList, new Comparator<FactDto>() {
									@Override
									public int compare(FactDto arg0, FactDto arg1) {
										if (TextUtils.equals(colomnId, TEMPID)) {//温度
											return Float.valueOf(arg0.temp).compareTo(Float.valueOf(arg1.temp));
										}else if (TextUtils.equals(colomnId, WINDID)) {//风速风向
											return Float.valueOf(arg0.windSpeed).compareTo(Float.valueOf(arg1.windSpeed));
										}else if (TextUtils.equals(colomnId, RAINID)) {//降水
											return Float.valueOf(arg0.rain).compareTo(Float.valueOf(arg1.rain));
										}
										return 0;
									}
								});
							}else if (!isAll && sequnce) {//国家站，将序
								tempList.addAll(dto.nationList);
								Collections.sort(tempList, new Comparator<FactDto>() {
									@Override
									public int compare(FactDto arg0, FactDto arg1) {
										if (TextUtils.equals(colomnId, TEMPID)) {//温度
											return Float.valueOf(arg1.temp).compareTo(Float.valueOf(arg0.temp));
										}else if (TextUtils.equals(colomnId, WINDID)) {//风速风向
											return Float.valueOf(arg1.windSpeed).compareTo(Float.valueOf(arg0.windSpeed));
										}else if (TextUtils.equals(colomnId, RAINID)) {//降水
											return Float.valueOf(arg1.rain).compareTo(Float.valueOf(arg0.rain));
										}
										return 0;
									}
								});
							}else if (!isAll && !sequnce) {//国家站，升序
								tempList.addAll(dto.nationList);
								Collections.sort(tempList, new Comparator<FactDto>() {
									@Override
									public int compare(FactDto arg0, FactDto arg1) {
										if (TextUtils.equals(colomnId, TEMPID)) {//温度
											return Float.valueOf(arg0.temp).compareTo(Float.valueOf(arg1.temp));
										}else if (TextUtils.equals(colomnId, WINDID)) {//风速风向
											return Float.valueOf(arg0.windSpeed).compareTo(Float.valueOf(arg1.windSpeed));
										}else if (TextUtils.equals(colomnId, RAINID)) {//降水
											return Float.valueOf(arg0.rain).compareTo(Float.valueOf(arg1.rain));
										}
										return 0;
									}
								});
							}
							
							rankList.clear();
							rankList.addAll(tempList);
							
							saveRankList.clear();
							saveRankList.addAll(tempList);
							
							try {
								if (!TextUtils.isEmpty(dto.time)) {
									tvPublishTime.setText("发布时间："+sdf3.format(sdf1.parse(dto.time)));
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
							
							if (factAdapter != null) {
								factAdapter.notifyDataSetChanged();
								CommonUtil.setListViewHeightBasedOnChildren(listView);
							}
							
							flag = dto.flag;
							tvSelect.setText(dto.itemName);
							tvSelect.setVisibility(View.VISIBLE);
							ivSelect.setVisibility(View.VISIBLE);
						}
					}
					
					if (cityAdapter != null) {
						cityAdapter.notifyDataSetChanged();
					}
					
					if (imgs.size() > 0) {
						FactDto imgDto = imgs.get(imgs.size()-1);
						if (!TextUtils.isEmpty(imgDto.imgUrl)) {
							FinalBitmap finalBitmap = FinalBitmap.create(mContext);
							finalBitmap.display(imageView, imgDto.imgUrl, null, 0);
							try {
								tvTime.setText(sdf2.format(sdf1.parse(imgDto.time)));
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					}
					
					scrollView.setVisibility(View.VISIBLE);
					llBottom.setVisibility(View.VISIBLE);
					cancelDialog();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			cancelDialog();
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
	 * 获取单站数据
	 */
	public void asyncQueryStation(String url) {
		HttpAsyncTaskStation task = new HttpAsyncTaskStation();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(url);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTaskStation extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTaskStation() {
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
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					JSONObject obj = new JSONObject(result);
					
					String time = null;
					if (!obj.isNull("time")) {
						time = obj.getString("time");
					}
					
					List<FactDto> dataList = new ArrayList<FactDto>();
					if (TextUtils.equals(colomnId, TEMPID)) {//温度
						if (TextUtils.equals(flag, TEMP_08_MAX) || TextUtils.equals(flag, TEMP_20_MAX)) {//温度
							if (!obj.isNull("tmax")) {
								JSONArray array = obj.getJSONArray("tmax");
								for (int i = 0; i < array.length(); i++) {
									FactDto dto = new FactDto();
									String temp = array.getString(i);
									float fTemp = 0;
									if (!TextUtils.isEmpty(temp)) {
										fTemp  = Float.valueOf(temp);
									}
									dto.tMax = fTemp+"";
									dto.time = time;
									dataList.add(dto);
								}
							}
						}else if (TextUtils.equals(flag, TEMP_08_MIN) || TextUtils.equals(flag, TEMP_20_MIN)) {//温度
							if (!obj.isNull("tmin")) {
								JSONArray array = obj.getJSONArray("tmin");
								for (int i = 0; i < array.length(); i++) {
									FactDto dto = new FactDto();
									String temp = array.getString(i);
									float fTemp = 0;
									if (!TextUtils.isEmpty(temp)) {
										fTemp  = Float.valueOf(temp);
									}
									dto.tMin = fTemp+"";
									dto.time = time;
									dataList.add(dto);
								}
							}
						}else {
							if (!obj.isNull("dtemp")) {
								JSONArray array = obj.getJSONArray("dtemp");
								for (int i = 0; i < array.length(); i++) {
									FactDto dto = new FactDto();
									String temp = array.getString(i);
									float fTemp = 0;
									if (!TextUtils.isEmpty(temp)) {
										fTemp  = Float.valueOf(temp);
									}
									dto.temp = fTemp+"";
									dto.time = time;
									dataList.add(dto);
								}
							}
						}
					}else if (TextUtils.equals(colomnId, WINDID)) {//风速风向
						if (!obj.isNull("winds")) {
							JSONArray array = obj.getJSONArray("winds");
							for (int i = 0; i < array.length(); i++) {
								FactDto dto = new FactDto();
								String speed = array.getString(i);
								float fSpeed = 0;
								if (!TextUtils.isEmpty(speed)) {
									fSpeed  = Float.valueOf(speed);
								}
								dto.windSpeed = fSpeed+"";
								dto.time = time;
								dataList.add(dto);
							}
						}
					}else if (TextUtils.equals(colomnId, RAINID)) {//降水
						if (!obj.isNull("rain")) {
							JSONArray array = obj.getJSONArray("rain");
							for (int i = 0; i < array.length(); i++) {
								FactDto dto = new FactDto();
								String rain = array.getString(i);
								float fRain = 0;
								if (!TextUtils.isEmpty(rain)) {
									fRain = Float.valueOf(rain);
								}
								dto.rain = fRain+"";
								dto.time = time;
								dataList.add(dto);
							}
						}
					}
					if (factAdapter != null) {
						factAdapter.rankList.clear();
						factAdapter.rankList.addAll(dataList);
						factAdapter.notifyDataSetChanged();
						CommonUtil.setListViewHeightBasedOnChildren(listView);
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
	
	private void startDownLoadImgs(List<FactDto> list) {
		if (mFactThread != null) {
			mFactThread.cancel();
			mFactThread = null;
		}
		if (list.size() > 0) {
			mRadarManager.loadImagesAsyn(list, this);
		}
	}
	
	@Override
	public void onResult(int result, List<FactDto> images) {
		mHandler.sendEmptyMessage(HANDLER_LOAD_FINISHED);
		if (result == RadarListener.RESULT_SUCCESSED) {
			if (mFactThread != null) {
				mFactThread.cancel();
				mFactThread = null;
			}
			if (images.size() > 0) {
				mFactThread = new FactThread(images);
				mFactThread.start();
			}
		}
	}
	
	private class FactThread extends Thread {
		static final int STATE_NONE = 0;
		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private List<FactDto> images;
		private int state;
		private int index;
		private int count;
		private boolean isTracking = false;
		
		public FactThread(List<FactDto> images) {
			this.images = images;
			this.count = images.size();
			this.index = 0;
			this.state = STATE_NONE;
			this.isTracking = false;
		}
		
		public int getCurrentState() {
			return state;
		}
		
		@Override
		public void run() {
			super.run();
			this.state = STATE_PLAYING;
			while (true) {
				if (state == STATE_CANCEL) {
					break;
				}
				if (state == STATE_PAUSE) {
					continue;
				}
				if (isTracking) {
					continue;
				}
				sendRadar();
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void sendRadar() {
			if (index >= count || index < 0) {
				index = 0;
				
				if (mFactThread != null) {
					mFactThread.pause();
					
					Message message = mHandler.obtainMessage();
					message.what = HANDLER_PAUSE;
					mHandler.sendMessage(message);
					if (seekBar != null) {
						seekBar.setProgress(97);
					}
				}
			}else {
				FactDto radar = images.get(index);
				Message message = mHandler.obtainMessage();
				message.what = HANDLER_SHOW_RADAR;
				message.obj = radar;
				message.arg1 = count - 1;
				message.arg2 = index ++;
				mHandler.sendMessage(message);
			}
			
		}
		
		public void cancel() {
			this.state = STATE_CANCEL;
		}
		public void pause() {
			this.state = STATE_PAUSE;
		}
		public void play() {
			this.state = STATE_PLAYING;
		}
		
		public void setCurrent(int index) {
			this.index = index;
		}
		
		public void startTracking() {
			isTracking = true;
		}
		
		public void stopTracking() {
			isTracking = false;
			if (this.state == STATE_PAUSE) {
				sendRadar();
			}
		}
	}

	@Override
	public void onProgress(String url, int progress) {
		Message msg = new Message();
		msg.obj = progress;
		msg.what = HANDLER_PROGRESS;
		mHandler.sendMessage(msg);
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case HANDLER_SHOW_RADAR: 
				if (msg.obj != null) {
					FactDto radar = (FactDto) msg.obj;
					if (radar != null) {
						Bitmap bitmap = BitmapFactory.decodeFile(radar.imgUrl);
						if (bitmap != null) {
							imageView.setImageBitmap(bitmap);
						}
					}
					changeProgress(radar.time, msg.arg2, msg.arg1);
					
					for (int i = 0; i < imgs.size(); i++) {
						if (i == msg.arg2) {
							imgs.get(msg.arg2).isSelect = selected;
						}else {
							imgs.get(i).isSelect = unselected;
						}
					}
				}
				break;
			case HANDLER_PROGRESS: 
				int progress = (Integer) msg.obj;
				tvPercent.setText(progress+""+"%");
				break;
			case HANDLER_LOAD_FINISHED: 
				tvPercent.setVisibility(View.GONE);
				llSeekBar.setVisibility(View.VISIBLE);
				if (ivPlay != null) {
					ivPlay.setImageResource(R.drawable.iv_pause);
				}
				break;
			case HANDLER_PAUSE:
				if (ivPlay != null) {
					ivPlay.setImageResource(R.drawable.iv_play);
				}
				break;
			default:
				break;
			}
			
		};
	};
	
	private OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			if (mFactThread != null) {
				mFactThread.setCurrent(seekBar.getProgress());
				mFactThread.stopTracking();
			}
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			if (mFactThread != null) {
				mFactThread.startTracking();
			}
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		}
	};
	
	private void changeProgress(String time, int progress, int max) {
		if (seekBar != null) {
			seekBar.setMax(max);
			seekBar.setProgress(progress);
		}
		try {
			tvTime.setText(sdf2.format(sdf1.parse(time)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mRadarManager != null) {
			mRadarManager.onDestory();
		}
		if (mFactThread != null) {
			mFactThread.cancel();
			mFactThread = null;
		}
	}
	
	private void legendAnimation(boolean flag, final LinearLayout view) {
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
		animation.setDuration(300);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		view.startAnimation(animationSet);
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				view.clearAnimation();
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivPlay:
			if (mFactThread != null && mFactThread.getCurrentState() == FactThread.STATE_PLAYING) {
				mFactThread.pause();
				ivPlay.setImageResource(R.drawable.iv_play);
			} else if (mFactThread != null && mFactThread.getCurrentState() == FactThread.STATE_PAUSE) {
				mFactThread.play();
				ivPlay.setImageResource(R.drawable.iv_pause);
			} else if (mFactThread == null) {
				tvPercent.setVisibility(View.VISIBLE);
				startDownLoadImgs(imgs);//开始下载
			}
			break;
		case R.id.tvSelect:
		case R.id.ivSelect:
			openMenuAnimation(reListView);
			break;
		case R.id.imageView:
			for (int i = 0; i < imgs.size(); i++) {
				if (imgs.get(i).isSelect.equals("1")) {
					if (mFactThread != null && mFactThread.getCurrentState() == FactThread.STATE_PLAYING) {
						mFactThread.pause();
						ivPlay.setImageResource(R.drawable.iv_play);
					}
					
					Intent intent = new Intent(mContext, ImageZoomActivity.class);
					intent.putExtra(CONST.WEB_URL, imgs.get(i).imgUrl);
					startActivity(intent);
					break;
				}
			}
			break;
		case R.id.llStation:
			tvStation.setTextColor(getResources().getColor(R.color.title_bg));
			tvSequnce.setTextColor(getResources().getColor(R.color.text_color4));
			ivSequnce.setImageResource(R.drawable.iv_sequnce_gray);
			
			if (llSelect.getVisibility() == View.GONE) {
				legendAnimation(false, llSelect);
				llSelect.setVisibility(View.VISIBLE);
			}else {
				legendAnimation(true, llSelect);
				llSelect.setVisibility(View.GONE);
			}
			break;
		case R.id.tvAll:
			isAll = true;
			if (saveRankList.size() > 0) {
				rankList.clear();
				if (sequnce) {
					rankList.addAll(saveRankList);
				}else {
					for (int i = saveRankList.size()-1; i >= 0; i--) {
						rankList.add(saveRankList.get(i));
					}
				}
				
				if (factAdapter != null) {
					factAdapter.notifyDataSetChanged();
					CommonUtil.setListViewHeightBasedOnChildren(listView);
				}
			}
			
			tvStation.setText(tvAll.getText().toString());
			tvAll.setTextColor(getResources().getColor(R.color.title_bg));
			tvNation.setTextColor(getResources().getColor(R.color.text_color4));
			if (llSelect.getVisibility() == View.GONE) {
				legendAnimation(false, llSelect);
				llSelect.setVisibility(View.VISIBLE);
			}else {
				legendAnimation(true, llSelect);
				llSelect.setVisibility(View.GONE);
			}
			break;
		case R.id.tvNation:
			isAll = false;
			if (saveRankList.size() > 0) {
				rankList.clear();
				if (sequnce) {
					for (int i = 0; i < saveRankList.size(); i++) {
						FactDto dto = saveRankList.get(i);
						if (!TextUtils.isEmpty(dto.id)) {
							if (TextUtils.equals(dto.id.substring(0, 1), "5")) {
								rankList.add(dto);
							}
						}
					}
				}else {
					for (int i = saveRankList.size()-1; i >= 0; i--) {
						FactDto dto = saveRankList.get(i);
						if (!TextUtils.isEmpty(dto.id)) {
							if (TextUtils.equals(dto.id.substring(0, 1), "5")) {
								rankList.add(dto);
							}
						}
					}
				}
				
				if (factAdapter != null) {
					factAdapter.notifyDataSetChanged();
					CommonUtil.setListViewHeightBasedOnChildren(listView);
				}
			}
			tvStation.setText(tvNation.getText().toString());
			tvAll.setTextColor(getResources().getColor(R.color.text_color4));
			tvNation.setTextColor(getResources().getColor(R.color.title_bg));
			if (llSelect.getVisibility() == View.GONE) {
				legendAnimation(false, llSelect);
				llSelect.setVisibility(View.VISIBLE);
			}else {
				legendAnimation(true, llSelect);
				llSelect.setVisibility(View.GONE);
			}
			break;
		case R.id.llSequnce:
			tvStation.setTextColor(getResources().getColor(R.color.text_color4));
			tvSequnce.setTextColor(getResources().getColor(R.color.title_bg));
			ivSequnce.setImageResource(R.drawable.iv_sequnce_blue);
			
			if (saveRankList.size() > 0) {
				rankList.clear();
				if (sequnce) {
					sequnce = false;
					
					if (isAll) {
						for (int i = saveRankList.size()-1; i >= 0; i--) {
							FactDto dto = saveRankList.get(i);
							rankList.add(dto);
						}
					}else {
						for (int i = saveRankList.size()-1; i >= 0; i--) {
							FactDto dto = saveRankList.get(i);
							if (TextUtils.equals(dto.id.substring(0, 1), "5")) {
								rankList.add(dto);
							}
						}
					}
				}else {
					sequnce = true;
					if (isAll) {
						for (int i = 0; i < saveRankList.size(); i++) {
							FactDto dto = saveRankList.get(i);
							rankList.add(dto);
						}
					}else {
						for (int i = 0; i < saveRankList.size(); i++) {
							FactDto dto = saveRankList.get(i);
							if (TextUtils.equals(dto.id.substring(0, 1), "5")) {
								rankList.add(dto);
							}
						}
					}
				}
				if (factAdapter != null) {
					factAdapter.notifyDataSetChanged();
					CommonUtil.setListViewHeightBasedOnChildren(listView);
				}
			}
			break;

		default:
			break;
		}
	}

}
