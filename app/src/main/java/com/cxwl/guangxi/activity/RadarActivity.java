package com.cxwl.guangxi.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.adapter.RadarAdapter;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.dto.RadarDto;
import com.cxwl.guangxi.manager.RadarManager;
import com.cxwl.guangxi.manager.RadarManager.RadarListener;
import com.cxwl.guangxi.utils.OkHttpUtil;

import net.tsz.afinal.FinalBitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class RadarActivity extends BaseActivity implements OnClickListener, RadarListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle = null;
	private List<RadarDto> radarList = new ArrayList<>();
	private ImageView imageView = null;
	private TextView tvPercent = null;
	private String baseUrl = "http://hfapi.tianqi.cn/data/";
	private final static String APPID = "fec60dca880595d7";//机密需要用到的AppId
	private final static String LEID_DATA = "leid_data";//加密秘钥名称
	private RadarManager mRadarManager = null;
	private RadarThread mRadarThread = null;
	private static final int HANDLER_SHOW_RADAR = 1;
	private static final int HANDLER_PROGRESS = 2;
	private static final int HANDLER_LOAD_FINISHED = 3;
	private static final int HANDLER_PAUSE = 4;
	private LinearLayout llSeekBar = null;
	private ImageView ivPlay = null;
	private SeekBar seekBar = null;
	private TextView tvTime = null;
	private GridView mGridView = null;
	private RadarAdapter mAdapter = null;
	private List<RadarDto> mList = new ArrayList<RadarDto>();
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmm");
	private String selected = "1";//选中
	private String unselected = "0";//未选中
	private RelativeLayout reContent = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_radar);
		mContext = this;
		showDialog();
		initWidget();
		initGridView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setOnClickListener(this);
		ivPlay = (ImageView) findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(seekbarListener);
		seekBar.setProgress(97);
		tvTime = (TextView) findViewById(R.id.tvTime);
		llSeekBar = (LinearLayout) findViewById(R.id.llSeekBar);
		reContent = (RelativeLayout) findViewById(R.id.reContent);
		tvPercent = (TextView) findViewById(R.id.tvPercent);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		mRadarManager = new RadarManager(mContext);
	}
	
	/**
	 * 初始化gridview
	 */
	private void initGridView() {
		mList.clear();
		String[] values = getResources().getStringArray(R.array.radars_station);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				String[] item = values[i].split(",");
				RadarDto dto = new RadarDto();
				dto.radarName = item[0];
				dto.radarCode = item[1];
				dto.isSelect = item[2];
				mList.add(dto);
			}

			OkHttpRadarData(mList.get(0).radarCode, sdf3.format(new Date()));
		}
		
		mGridView = (GridView) findViewById(R.id.gridView);
		mAdapter = new RadarAdapter(mContext, mList);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				for (int i = 0; i < mList.size(); i++) {
					if (i == arg2) {
						mList.get(arg2).isSelect = selected;
					}else {
						mList.get(i).isSelect = unselected;
					}
				}
				mAdapter.notifyDataSetChanged();
				
				if (mRadarThread != null) {
					mRadarThread.cancel();
					mRadarThread = null;
				}
				if (seekBar != null) {
					seekBar.setProgress(97);
				}
				
				tvPercent.setText("0%");
				RadarDto dto = mList.get(arg2);
				OkHttpRadarData(dto.radarCode, sdf3.format(new Date()));
			}
		});
	}
	
	private String getRadarUrl(String url, String areaid, String type, String date) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(url);
		buffer.append("?");
		buffer.append("areaid=").append(areaid);
		buffer.append("&");
		buffer.append("type=").append(type);
		buffer.append("&");
		buffer.append("date=").append(date);
		buffer.append("&");
		buffer.append("appid=").append(APPID);
		
		String key = getKey(LEID_DATA, buffer.toString());
		buffer.delete(buffer.lastIndexOf("&"), buffer.length());
		
		buffer.append("&");
		buffer.append("appid=").append(APPID.subSequence(0, 6));
		buffer.append("&");
		buffer.append("key=").append(key.substring(0, key.length()-3));
		String result = buffer.toString();
		return result;
	}
	
	private String getKey(String key, String src) {
		try{
			byte[] rawHmac = null;
			byte[] keyBytes = key.getBytes("UTF-8");
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);
			rawHmac = mac.doFinal(src.getBytes("UTF-8"));
			String encodeStr = Base64.encodeToString(rawHmac, Base64.DEFAULT);
			String keySrc = URLEncoder.encode(encodeStr, "UTF-8");
			return keySrc;
		}catch(Exception e){
			Log.e("SceneException", e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * 获取雷达图片集信息
	 */
	private void OkHttpRadarData(String radarCode, String currentTime) {
		final String url = getRadarUrl(baseUrl, radarCode, "product", currentTime);
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
								cancelDialog();
								radarList.clear();
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);
										String r2 = obj.getString("r2");
										String r3 = obj.getString("r3");
										String[] temp = obj.getString("r4").split("\\|");
										String r4 = temp[0];
										String r5 = obj.getString("r5");
										JSONArray array = new JSONArray(obj.getString("r6"));
										for (int i = array.length()-1; i >= 0 ; i--) {
											JSONArray itemArray = array.getJSONArray(i);
											String r6_0 = itemArray.getString(0);
											String r6_1 = itemArray.getString(1);

											String url = r2 + r4 + "/" + r5 + r6_0 + "." + r3;

											if (i == 0 && !TextUtils.isEmpty(url)) {
												FinalBitmap finalBitmap = FinalBitmap.create(mContext);
												finalBitmap.display(imageView, url, null, 0);
												try {
													tvTime.setText(sdf1.format(sdf2.parse(r6_1)));
												} catch (ParseException e) {
													e.printStackTrace();
												}
											}

											RadarDto dto = new RadarDto();
											if (i == 0) {
												dto.isSelect = "1";
											}else {
												dto.isSelect = "0";
											}
											dto.url = url;
											dto.time = r6_1;
											radarList.add(dto);
										}

										reContent.setVisibility(View.VISIBLE);
										cancelDialog();
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
	
	private void startDownLoadImgs(List<RadarDto> list) {
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
		if (list.size() > 0) {
			mRadarManager.loadImagesAsyn(list, this);
		}
	}
	
	@Override
	public void onResult(int result, List<RadarDto> images) {
		mHandler.sendEmptyMessage(HANDLER_LOAD_FINISHED);
		if (result == RadarListener.RESULT_SUCCESSED) {
			if (mRadarThread != null) {
				mRadarThread.cancel();
				mRadarThread = null;
			}
			if (images.size() > 0) {
				mRadarThread = new RadarThread(images);
				mRadarThread.start();
			}
		}
	}
	
	private class RadarThread extends Thread {
		static final int STATE_NONE = 0;
		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private List<RadarDto> images;
		private int state;
		private int index;
		private int count;
		private boolean isTracking = false;
		
		public RadarThread(List<RadarDto> images) {
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
				
				if (mRadarThread != null) {
					mRadarThread.pause();
					
					Message message = mHandler.obtainMessage();
					message.what = HANDLER_PAUSE;
					mHandler.sendMessage(message);
					if (seekBar != null) {
						seekBar.setProgress(97);
					}
				}
			}else {
				RadarDto radar = images.get(index);
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
					RadarDto radar = (RadarDto) msg.obj;
					if (radar != null) {
						Bitmap bitmap = BitmapFactory.decodeFile(radar.url);
						if (bitmap != null) {
							imageView.setImageBitmap(bitmap);
						}
					}
					changeProgress(radar.time, msg.arg2, msg.arg1);
					
					for (int i = 0; i < radarList.size(); i++) {
						if (i == msg.arg2) {
							radarList.get(msg.arg2).isSelect = selected;
						}else {
							radarList.get(i).isSelect = unselected;
						}
					}
					mAdapter.notifyDataSetChanged();
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
	
	private void changeProgress(String time, int progress, int max) {
		if (seekBar != null) {
			seekBar.setMax(max);
			seekBar.setProgress(progress);
		}
		try {
			tvTime.setText(sdf1.format(sdf2.parse(time)));
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
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
	}
	
	private OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.setCurrent(seekBar.getProgress());
				mRadarThread.stopTracking();
			}
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.startTracking();
			}
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		}
	};
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivPlay:
			if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PLAYING) {
				mRadarThread.pause();
				ivPlay.setImageResource(R.drawable.iv_play);
			} else if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PAUSE) {
				mRadarThread.play();
				ivPlay.setImageResource(R.drawable.iv_pause);
			} else if (mRadarThread == null) {
				tvPercent.setVisibility(View.VISIBLE);
				startDownLoadImgs(radarList);//开始下载
			}
			break;
		case R.id.imageView:
			for (int i = 0; i < radarList.size(); i++) {
				if (radarList.get(i).isSelect.equals("1")) {
					if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PLAYING) {
						mRadarThread.pause();
						ivPlay.setImageResource(R.drawable.iv_play);
					}
					
					Intent intent = new Intent(mContext, ImageZoomActivity.class);
					intent.putExtra(CONST.WEB_URL, radarList.get(i).url);
					startActivity(intent);
					break;
				}
			}
			break;

		default:
			break;
		}
	}

}
