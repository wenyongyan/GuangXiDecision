package com.cxwl.guangxi.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.fragment.IntroduceFragment;
import com.cxwl.guangxi.view.MainViewPager;

/**
 * 简介
 * @author shawn_sun
 *
 */

public class IntroduceActivity extends BaseActivity implements OnClickListener{
	
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private RelativeLayout re1, re2, re3;
	private TextView tv1, tv2, tv3, tv11, tv12, tv13;
	private MainViewPager viewPager = null;
	private List<Fragment> fragments = new ArrayList<>();
	private String title1 = "广西地形地貌及水系水库分布";
	private String title2 = "广西气候概况及主要气象灾害";
	private String title3 = "综合气象观测系统建设";
	private String content1 = "\t\t\t\t广西属于低纬地区。南临北部湾，面向东南亚，地处中、南亚热带季风气候区。地势由西北向东南倾斜。四周多被山地、高原环绕，呈盆地状。盆地边缘多缺口，桂东北、桂东、桂南沿江一带有大片谷地。"
			+"\n"+"\t\t\t\t广西境内河流分属珠江水系、长江水系、桂南沿海独流入海水系等，其中珠江水系在广西境内总长1239公里，流域面积达20多万平方公里，占广西总面积的85.2%，为广西第一大水系。广西有龙滩、天生桥、岩滩、百色、西津、澄碧河、小江等7座大1型水库，在调蓄洪水、防汛抗旱方面起到了重要作用。";
	private String content2 = "\t\t\t\t广西大部地区年降雨量在1300-2000毫米之间，具有夏长冬短、热量充沛、降水丰富、雨热同季的四大特点。受特殊的地理位置和地形环境影响，广西气象灾害种类多、分布广、发生频繁、危害严重，多干旱、暴雨洪涝、台风、高温、雷电、冰雹、大风、低温冷害、霜冻等气象灾害。"
			+"\n"+"\t\t\t\t暴雨：广西暴雨洪涝灾害多发生在4～9月，有四个典型暴雨区，分别是：以东兴市为中心的沿海暴雨区、以永福县为中心的桂东北山地暴雨区、以昭平县为中心的桂东暴雨区以及以都安县为中心的桂西山地暴雨区。其中东兴年均降雨量2657.8毫米，是中国大陆降雨量最多的地方。暴雨极易引发江河洪涝、城市内涝、山洪、山体滑坡、泥石流等次生灾害。2005年6月，受大范围持续性暴雨影响，西江出现百年一遇特大洪水，梧州水漫河堤。2016年6月，南宁、柳州两市先后遭遇特大暴雨，造成严重的城市内涝。"
			+"\n"+"\t\t\t\t台风：广西是受台风影响最严重的省份之一，具有如下特点：一、路径复杂多样，有长途跋涉型、东部深入型、再次登陆型、正面袭击型和西部侵入型五种特点不同的影响路径，每一路径都可能对广西造成很大影响，如2013年经三次登陆后进入广西的超强台风“威马逊”，仍对广西造成了严重影响。二、终台影响季节晚，晚秋台风影响严重，如2012年10月底影响广西的台风“山神”和2013年11月11日进入广西的超强台风“海燕”等，均给广西造成了大范围强降雨和大风天气。三、台风在广西滞留时间长，强风暴雨范围大，如2013年超强台风“尤特”在广西滞留时间长达112小时，导致严重暴雨洪涝灾害。"
			+"\n"+"\t\t\t\t干旱：危害广西的旱灾主要是春旱和秋旱。春旱以桂西地区居多，百色、崇左两市、防城港市北部、北海和南宁两市南部、河池市西部等地发生春旱的频率达70～90%。而秋旱多出现在桂东地区，桂东北大部、桂中盆地及其邻近地区等地发生秋旱的频率达70～90%。2009年8月到2010年4月广西出现严重的夏秋冬春连旱。2011年7-9月发生了历史罕见的汛期干旱。"
			+"\n"+"\t\t\t\t强对流：广西多冰雹、大风和雷暴等强对流天气。桂北山区和右江河谷是广西强对流天气的高发地。冰雹桂西多于桂东，山区多于平原。桂西北是广西的多雹区。大风主要有寒潮大风、台风大风、雷暴大风三类，涠洲岛是广西大风日数最多的地方，平均每年有23天。广西是我国雷暴活动最为频繁的地区之一，其中东兴年雷暴日数多达95天，为广西之最。"
			+"\n"+"\t\t\t\t冻害：广西虽然冬季短暂，但是寒潮入侵时，部分地区极端最低气温也会降到0℃或以下，大部地区都可能出现霜冻或冰冻天气，给农业生产造成灾难性后果。冻害主要发生区域在桂东北，1963年1月15日，桂林市资源县出现了零下8.4℃的有气象记录以来的广西历史极端最低气温。";
	private String content3 = "\t\t\t\t近年来，广西不断加强综合气象观测能力建设，充分利用现代化、高科技手段察云观天。目前拥有国家级地面气象观测站93个，高空气象观测站6个；多普勒雷达10部、数字化雷达3部、固定式风廓线雷达2部和车载式边界层风廓线雷达1部。广西还建立了由2500多个自动气象站组成的区域地面自动观测网，使得每个乡镇都有1个以上自动气象站。另外建有国家农业气象观测站22个，高速公路交通气象观测站8 个，大气成分监测站4个，酸雨监测站10个，雷电监测站300个，正在建设大气负氧离子监测站30个。并在40多个农业示范区开展农田小气候观测和在全区91个气象站增设回南天特色气象观测。"
			+"\n"+"\t\t\t\t形成了由地面、高空、新一代天气雷达以及农业、交通、生态、海洋等组成的立体化、自动化的综合气象观测体系，为做好气象预报预测预警工作奠定了强有力的基础。";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_introduce);
		initWidget();
		initViewPager();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		re1 = (RelativeLayout) findViewById(R.id.re1);
		re1.setOnClickListener(new MyOnClickListener(0));
		re2 = (RelativeLayout) findViewById(R.id.re2);
		re2.setOnClickListener(new MyOnClickListener(1));
		re3 = (RelativeLayout) findViewById(R.id.re3);
		re3.setOnClickListener(new MyOnClickListener(2));
		tv1 = (TextView) findViewById(R.id.tv1);
		tv2 = (TextView) findViewById(R.id.tv2);
		tv3 = (TextView) findViewById(R.id.tv3);
		tv11 = (TextView) findViewById(R.id.tv11);
		tv12 = (TextView) findViewById(R.id.tv12);
		tv13 = (TextView) findViewById(R.id.tv13);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		IntroduceFragment fragment = new IntroduceFragment();
		Bundle bundle = new Bundle();
		bundle.putString("title", title1);
		bundle.putString("content", content1);
		fragment.setArguments(bundle);
		fragments.add(fragment);
		
		fragment = new IntroduceFragment();
		bundle = new Bundle();
		bundle.putString("title", title2);
		bundle.putString("content", content2);
		fragment.setArguments(bundle);
		fragments.add(fragment);
		
		fragment = new IntroduceFragment();
		bundle = new Bundle();
		bundle.putString("title", title3);
		bundle.putString("content", content3);
		fragment.setArguments(bundle);
		fragments.add(fragment);
		
		viewPager = (MainViewPager) findViewById(R.id.viewPager);
		viewPager.setSlipping(true);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		viewPager.setAdapter(new MyPagerAdapter());
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			if (arg0 == 0) {
				tv1.setTextColor(getResources().getColor(R.color.title_bg));
				tv11.setBackgroundColor(getResources().getColor(R.color.title_bg));
				tv2.setTextColor(getResources().getColor(R.color.text_color4));
				tv12.setBackgroundColor(getResources().getColor(R.color.transparent));
				tv3.setTextColor(getResources().getColor(R.color.text_color4));
				tv13.setBackgroundColor(getResources().getColor(R.color.transparent));
			}else if (arg0 == 1) {
				tv1.setTextColor(getResources().getColor(R.color.text_color4));
				tv11.setBackgroundColor(getResources().getColor(R.color.transparent));
				tv2.setTextColor(getResources().getColor(R.color.title_bg));
				tv12.setBackgroundColor(getResources().getColor(R.color.title_bg));
				tv3.setTextColor(getResources().getColor(R.color.text_color4));
				tv13.setBackgroundColor(getResources().getColor(R.color.transparent));
			}else if (arg0 == 2) {
				tv1.setTextColor(getResources().getColor(R.color.text_color4));
				tv11.setBackgroundColor(getResources().getColor(R.color.transparent));
				tv2.setTextColor(getResources().getColor(R.color.text_color4));
				tv12.setBackgroundColor(getResources().getColor(R.color.transparent));
				tv3.setTextColor(getResources().getColor(R.color.title_bg));
				tv13.setBackgroundColor(getResources().getColor(R.color.title_bg));
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
