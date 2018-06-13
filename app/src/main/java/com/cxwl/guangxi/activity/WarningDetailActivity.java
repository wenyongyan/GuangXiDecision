package com.cxwl.guangxi.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.dto.WarningDto;
import com.cxwl.guangxi.manager.DBManager;
import com.cxwl.guangxi.utils.CommonUtil;
import com.cxwl.guangxi.utils.OkHttpUtil;
import com.cxwl.guangxi.view.RefreshLayout;
import com.cxwl.guangxi.view.RefreshLayout.OnRefreshListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class WarningDetailActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ImageView imageView = null;//预警图标
	private TextView tvName = null;//预警名称
	private TextView tvTime = null;//预警时间
	private TextView tvIntro = null;//预警介绍
	private TextView tvGuide = null;//防御指南
	private String url = "http://decision.tianqi.cn/alarm12379/content2/";//详情页面url
	private WarningDto data = null;
	private ScrollView scrollView = null;
	private ProgressBar progressBar = null;
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.warning_detail);
		mContext = this;
		initRefreshLayout();
		initWidget();
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColor(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.PULL_FROM_START);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}
	
	private void refresh() {
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			try {
				if (TextUtils.equals(data.provinceId, "45")) {
					url = "http://decision-admin.tianqi.cn/infomes/data/nanning/decision_guangxi_yj/content2/";
				}
				OkHttpWarningDetail(url+data.html);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.warning_detail));
		imageView = (ImageView) findViewById(R.id.imageView);
		tvName = (TextView) findViewById(R.id.tvName);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvIntro = (TextView) findViewById(R.id.tvIntro);
		tvGuide = (TextView) findViewById(R.id.tvGuide);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		refresh();
	}
	
	/**
	 * 初始化数据库
	 */
	private void initDBManager() {
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = null;
		cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME2 + " where WarningId = " + "\"" + data.type+data.color + "\"",null);
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			tvGuide.setText(getString(R.string.warning_guide)+cursor.getString(cursor.getColumnIndex("WarningGuide")));
		}
	}
	
	/**
	 * 异步请求
	 */
	private void OkHttpWarningDetail(final String url) {
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
											if (!object.isNull("sendTime")) {
												tvTime.setText(object.getString("sendTime"));
											}

											if (!object.isNull("description")) {
												tvIntro.setText(object.getString("description"));
											}

											String name = object.getString("headline");
											if (!TextUtils.isEmpty(name)) {
												tvName.setText(name.replace(getString(R.string.publish), getString(R.string.publish)+"\n"));
											}

											Bitmap bitmap = null;
											if (object.getString("severityCode").equals(CONST.blue[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+object.getString("eventType")+CONST.blue[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
												}
											}else if (object.getString("severityCode").equals(CONST.yellow[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+object.getString("eventType")+CONST.yellow[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
												}
											}else if (object.getString("severityCode").equals(CONST.orange[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+object.getString("eventType")+CONST.orange[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
												}
											}else if (object.getString("severityCode").equals(CONST.red[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+object.getString("eventType")+CONST.red[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
												}
											}
											imageView.setImageBitmap(bitmap);

											initDBManager();
											scrollView.setVisibility(View.VISIBLE);
											progressBar.setVisibility(View.GONE);
											refreshLayout.setRefreshing(false);
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
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}

}
