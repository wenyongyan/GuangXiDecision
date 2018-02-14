package com.cxwl.guangxi.fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.dto.WeatherDto;

/**
 * 7天预报
 * @author shawn_sun
 *
 */

public class SevenDayFragment extends Fragment{
	
	private LinearLayout llContainer2 = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_seven_day, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	@SuppressWarnings("unchecked")
	private void initWidget(View v) {
		llContainer2 = (LinearLayout) v.findViewById(R.id.llContainer2);
		llContainer2.removeAllViews();
		
		List<WeatherDto> weeklyList = new ArrayList<>();
		weeklyList.addAll(getArguments().<WeatherDto>getParcelableArrayList("weeklyList"));
		for (int i = 0; i < weeklyList.size(); i++) {
			WeatherDto dto = weeklyList.get(i);
			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.weekly_layout, null);
			TextView tvWeek = (TextView) view.findViewById(R.id.tvWeek);
			ImageView ivPheHigh = (ImageView) view.findViewById(R.id.ivPheHigh);
			ImageView ivPheLow = (ImageView) view.findViewById(R.id.ivPheLow);
			TextView tvTemp = (TextView) view.findViewById(R.id.tvTemp);
			if (i == 0) {
				tvWeek.setText(getString(R.string.today));
				boolean isReplace = getArguments().getBoolean("isReplace");
				if (isReplace) {
					StringBuffer buffer = new StringBuffer();
					buffer.append(dto.highTemp+getString(R.string.unit_degree)+"/"+dto.lowTemp+getString(R.string.unit_degree));
					
					SpannableStringBuilder builder = new SpannableStringBuilder(buffer.toString());
					ForegroundColorSpan builderSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
					ForegroundColorSpan builderSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.white));
					
					builder.setSpan(builderSpan1, 0, String.valueOf(dto.highTemp+getString(R.string.unit_degree)).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					builder.setSpan(builderSpan2, String.valueOf(dto.highTemp+getString(R.string.unit_degree)).length(), buffer.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					tvTemp.setText(builder);
				}else {
					tvTemp.setText(dto.highTemp+getString(R.string.unit_degree)+"/"+dto.lowTemp+getString(R.string.unit_degree));
				}
			}else {
				String weekStr = getActivity().getString(R.string.week)+dto.week.substring(dto.week.length()-1, dto.week.length());
				tvWeek.setText(weekStr);
				tvTemp.setText(dto.highTemp+getString(R.string.unit_degree)+"/"+dto.lowTemp+getString(R.string.unit_degree));
			}
			Drawable hd = getActivity().getResources().getDrawable(R.drawable.phenomenon_drawable);
			hd.setLevel(dto.highPheCode);
			ivPheHigh.setBackground(hd);
			Drawable ld = getActivity().getResources().getDrawable(R.drawable.phenomenon_drawable_night);
			ld.setLevel(dto.lowPheCode);
			ivPheLow.setBackground(ld);
			llContainer2.addView(view);
		}
	}
	
}
