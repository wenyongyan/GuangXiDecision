package com.cxwl.guangxi;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.dto.NewsDto;
import com.cxwl.guangxi.fragment.ServiceMaterialFragment;
import com.cxwl.guangxi.utils.CustomHttpClient;
import com.cxwl.guangxi.view.MainViewPager;
import com.cxwl.guangxi.view.MyDialog2;

/**
 * 服务材料
 * @author shawn_sun
 *
 */

public class ServiceMaterialActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private MyDialog2 mDialog = null;
	private LinearLayout llContainer = null;
	private LinearLayout llContainer1 = null;
	private MainViewPager viewPager = null;
	private List<Fragment> fragments = new ArrayList<Fragment>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_service_material);
		mContext = this;
		showDialog();
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
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		llContainer = (LinearLayout) findViewById(R.id.llContainer);
		llContainer1 = (LinearLayout) findViewById(R.id.llContainer1);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		String url = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(url)) {
			asyncQuery(url);
		}
	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager(List<NewsDto> aqiList) {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		llContainer.removeAllViews();
		llContainer1.removeAllViews();
		for (int i = 0; i < aqiList.size(); i++) {
			NewsDto dto = aqiList.get(i);
			
			TextView tvName = new TextView(mContext);
			tvName.setGravity(Gravity.CENTER);
			tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			tvName.setPadding(0, (int)(dm.density*10), 0, (int)(dm.density*10));
			tvName.setOnClickListener(new MyOnClickListener(i));
			if (i == 0) {
				tvName.setTextColor(getResources().getColor(R.color.title_bg));
			}else {
				tvName.setTextColor(getResources().getColor(R.color.text_color4));
			}
			if (!TextUtils.isEmpty(dto.title)) {
				tvName.setText(dto.title);
			}
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.weight = 1.0f;
			tvName.setLayoutParams(params);
			llContainer.addView(tvName, i);
			
			TextView tvBar = new TextView(mContext);
			tvBar.setGravity(Gravity.CENTER);
			tvBar.setOnClickListener(new MyOnClickListener(i));
			if (i == 0) {
				tvBar.setBackgroundColor(getResources().getColor(R.color.title_bg));
			}else {
				tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
			}
			LayoutParams params1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params1.weight = 1.0f;
			params1.height = (int) (dm.density*2);
			tvBar.setLayoutParams(params1);
			llContainer1.addView(tvBar, i);
			
			ServiceMaterialFragment fragment = new ServiceMaterialFragment();
			Bundle bundle = new Bundle();
			bundle.putParcelable("data", dto);
			fragment.setArguments(bundle);
			fragments.add(fragment);
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
			if (llContainer != null) {
				for (int i = 0; i < llContainer.getChildCount(); i++) {
					TextView tvName = (TextView) llContainer.getChildAt(i);
					if (i == arg0) {
						tvName.setTextColor(getResources().getColor(R.color.title_bg));
					}else {
						tvName.setTextColor(getResources().getColor(R.color.text_color4));
					}
				}
			}
			
			if (llContainer1 != null) {
				for (int i = 0; i < llContainer1.getChildCount(); i++) {
					TextView tvBar = (TextView) llContainer1.getChildAt(i);
					if (i == arg0) {
						tvBar.setBackgroundColor(getResources().getColor(R.color.title_bg));
					}else {
						tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
					}
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
	 * 头标点击监听
	 * @author shawn_sun
	 */
	private class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			if (viewPager != null) {
				viewPager.setCurrentItem(index, true);
			}
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
				ft.commit();
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
	 * 获取服务材料数据
	 */
	private void asyncQuery(String url) {
		//异步请求数据
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
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
			cancelDialog();
			if (result != null) {
				try {
					List<NewsDto> dataList = new ArrayList<NewsDto>();
					JSONArray array = new JSONArray(result);
					for (int i = 0; i < array.length(); i++) {
						JSONObject itemObj = array.getJSONObject(i);
						NewsDto dto = new NewsDto();
						if (!itemObj.isNull("url")) {
							dto.detailUrl = itemObj.getString("url");
						}
						if (!itemObj.isNull("name")) {
							dto.title = itemObj.getString("name");
						}
						
						if (!itemObj.isNull("data")) {
							JSONArray itemArray = itemObj.getJSONArray("data");
							List<NewsDto> itemList = new ArrayList<NewsDto>();
							for (int j = 0; j < itemArray.length(); j++) {
								NewsDto itemDto = new NewsDto();
								String url = itemArray.getString(j);
								if (!TextUtils.isEmpty(url)) {
									itemDto.detailUrl = url;
									String title = url.substring(url.lastIndexOf("/")+1, url.lastIndexOf("."));
									itemDto.title = title;
									itemList.add(itemDto);
								}
							}
							dto.itemList.addAll(itemList);
						}
						
						dataList.add(dto);
					}
					
					if (dataList.size() > 0) {
						initViewPager(dataList);
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
