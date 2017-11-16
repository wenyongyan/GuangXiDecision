package com.cxwl.guangxi.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.dto.WarningDto;
import com.cxwl.guangxi.manager.DBManager;
import com.cxwl.guangxi.utils.CommonUtil;
import com.cxwl.guangxi.utils.CustomHttpClient;
import com.cxwl.guangxi.view.RefreshLayout;
import com.cxwl.guangxi.view.RefreshLayout.OnRefreshListener;

public class WarningDetailFragment extends Fragment{
	
	private ImageView imageView = null;//预警图标
	private TextView tvName = null;//预警名称
	private TextView tvTime = null;//预警时间
	private TextView tvIntro = null;//预警介绍
	private TextView tvGuide = null;//防御指南
	private String url = "http://decision.tianqi.cn/alarm12379/content2/";//详情页面url
	private WarningDto data = null;
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private ProgressBar progressBar = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.warning_detail_fragment, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initRefreshLayout(view);
		initWidget(view);
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout(View view) {
		refreshLayout = (RefreshLayout) view.findViewById(R.id.refreshLayout);
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
		data = getArguments().getParcelable("data");
		try {
			if (TextUtils.equals(data.provinceId, "45")) {
				url = "http://decision-admin.tianqi.cn/infomes/data/nanning/decision_guangxi_yj/content2/";
			}
			asyncQuery(url+data.html);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget(View view) {
		imageView = (ImageView) view.findViewById(R.id.imageView);
		tvName = (TextView) view.findViewById(R.id.tvName);
		tvTime = (TextView) view.findViewById(R.id.tvTime);
		tvIntro = (TextView) view.findViewById(R.id.tvIntro);
		tvGuide = (TextView) view.findViewById(R.id.tvGuide);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		
		refresh();
	}
	
	/**
	 * 初始化数据库
	 */
	private void initDBManager() {
		DBManager dbManager = new DBManager(getActivity());
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
			if (isAdded()) {//判断fragment是否已经添加
				if (requestResult != null) {
					try {
						JSONObject object = new JSONObject(requestResult);
						if (object != null) {
							if (!object.isNull("sendTime")) {
								tvTime.setText(getString(R.string.publish_time)+object.getString("sendTime"));
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
								bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+object.getString("eventType")+CONST.blue[1]+CONST.imageSuffix);
								if (bitmap == null) {
									bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
								}
							}else if (object.getString("severityCode").equals(CONST.yellow[0])) {
								bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+object.getString("eventType")+CONST.yellow[1]+CONST.imageSuffix);
								if (bitmap == null) {
									bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
								}
							}else if (object.getString("severityCode").equals(CONST.orange[0])) {
								bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+object.getString("eventType")+CONST.orange[1]+CONST.imageSuffix);
								if (bitmap == null) {
									bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
								}
							}else if (object.getString("severityCode").equals(CONST.red[0])) {
								bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+object.getString("eventType")+CONST.red[1]+CONST.imageSuffix);
								if (bitmap == null) {
									bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
								}
							}
							imageView.setImageBitmap(bitmap);
							
							initDBManager();
							refreshLayout.setRefreshing(false);
							progressBar.setVisibility(View.GONE);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
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

}