package com.cxwl.guangxi.activity;

/**
 * 热点新闻
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.adapter.NewsAdapter;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.dto.NewsDto;
import com.cxwl.guangxi.utils.OkHttpUtil;
import com.cxwl.guangxi.view.RefreshLayout;
import com.cxwl.guangxi.view.RefreshLayout.OnLoadListener;
import com.cxwl.guangxi.view.RefreshLayout.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class NewsActivity extends BaseActivity implements OnClickListener, OnRefreshListener, OnLoadListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private NewsAdapter mAdapter = null;
	private List<NewsDto> mList = new ArrayList<>();
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private String dataUrl = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news);
		mContext = this;
		showDialog();
		initRefreshLayout();
		initWidget();
		initListView();
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColor(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.BOTH);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setOnRefreshListener(this);
		refreshLayout.setOnLoadListener(this);
	}
	
	@Override
	public void onRefresh() {
		mList.clear();
		dataUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(dataUrl)) {
			OkHttpList(dataUrl);
		}
	}
	
	@Override
	public void onLoad() {
		if (!TextUtils.isEmpty(dataUrl)) {
			OkHttpList(dataUrl);
		}
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		String name = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		tvTitle.setText(name);

		dataUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(dataUrl)) {
			OkHttpList(dataUrl);
		}
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new NewsAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				NewsDto dto = mList.get(arg2);
				Intent intent = new Intent(mContext, UrlActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				intent.putExtra(CONST.WEB_URL, dto.detailUrl);
				startActivity(intent);
			}
		});
	}
	
	private void OkHttpList(final String url) {
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
								refreshLayout.setRefreshing(false);
								refreshLayout.setLoading(false);
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("l")) {
											JSONArray array = new JSONArray(obj.getString("l"));
											for (int i = 0; i < array.length(); i++) {
												JSONObject itemObj = array.getJSONObject(i);
												NewsDto dto = new NewsDto();
												dto.title = itemObj.getString("l1");
												dto.detailUrl = itemObj.getString("l2");
												dto.time = itemObj.getString("l3");
												dto.imgUrl = itemObj.getString("l4");
												mList.add(dto);
											}
										}
										if (!obj.isNull("prev")) {
											dataUrl = obj.getString("prev");
										}
										if (mAdapter != null) {
											mAdapter.notifyDataSetChanged();
										}
									} catch (JSONException e1) {
										e1.printStackTrace();
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
		if (v.getId() == R.id.llBack) {
			finish();
		}
	}
	
}
