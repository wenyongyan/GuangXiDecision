package com.cxwl.guangxi.activity;

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
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.common.ColumnData;
import com.cxwl.guangxi.dto.NewsDto;
import com.cxwl.guangxi.fragment.ServiceMaterialFragment;
import com.cxwl.guangxi.utils.CustomHttpClient;
import com.cxwl.guangxi.utils.OkHttpUtil;
import com.cxwl.guangxi.view.MainViewPager;

import org.apache.http.NameValuePair;
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

/**
 * 服务材料
 * @author shawn_sun
 *
 */

public class ServiceMaterialActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private LinearLayout llContainer = null;
	private LinearLayout llContainer1 = null;
	private MainViewPager viewPager = null;
	private List<Fragment> fragments = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_service_material);
		mContext = this;
		initWidget();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		llContainer = (LinearLayout) findViewById(R.id.llContainer);
		llContainer1 = (LinearLayout) findViewById(R.id.llContainer1);

		ColumnData data = getIntent().getExtras().getParcelable("data");
		if (data != null) {
			if (!TextUtils.isEmpty(data.name)) {
				tvTitle.setText(data.name);
			}

			List<ColumnData> dataList = new ArrayList<>();
			dataList.addAll(data.child);
			initViewPager(dataList);
		}
	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager(List<ColumnData> dataList) {
		if (dataList.isEmpty()) {
			return;
		}
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		float density = dm.density;
		llContainer.removeAllViews();
		llContainer1.removeAllViews();
		int size = dataList.size();
		for (int i = 0; i < size; i++) {
			ColumnData dto = dataList.get(i);

			TextView tvName = new TextView(mContext);
			tvName.setGravity(Gravity.CENTER);
			tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
			tvName.setPadding(0, (int)(density*10), 0, (int)(density*10));
			tvName.setMaxLines(1);
			tvName.setOnClickListener(new MyOnClickListener(i));
			if (!TextUtils.isEmpty(dto.name)) {
				tvName.setText(dto.name);
			}

			TextView tvBar = new TextView(mContext);
			tvBar.setGravity(Gravity.CENTER);
			tvBar.setPadding(0, (int)(density*10), 0, (int)(density*10));
			tvBar.setOnClickListener(new MyOnClickListener(i));

			if (i == 0) {
				tvName.setTextColor(getResources().getColor(R.color.title_bg));
				tvBar.setBackgroundColor(getResources().getColor(R.color.title_bg));
			}else {
				tvName.setTextColor(getResources().getColor(R.color.text_color4));
				tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
			}

			llContainer.addView(tvName, i);
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
