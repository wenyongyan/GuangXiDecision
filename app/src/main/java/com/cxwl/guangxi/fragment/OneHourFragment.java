package com.cxwl.guangxi.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.dto.WeatherDto;
import com.cxwl.guangxi.utils.WeatherUtil;

/**
 * 未来1小时预报
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class OneHourFragment extends Fragment{
	
	private TextView tv1, tv2, tv3, tv4, tv5;
	private ImageView ivPhe = null;
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH");

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_one_hour, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	private void initWidget(View view) {
		tv1 = (TextView) view.findViewById(R.id.tv1);
		tv2 = (TextView) view.findViewById(R.id.tv2);
		tv3 = (TextView) view.findViewById(R.id.tv3);
		tv4 = (TextView) view.findViewById(R.id.tv4);
		tv5 = (TextView) view.findViewById(R.id.tv5);
		ivPhe = (ImageView) view.findViewById(R.id.ivPhe);
		
		WeatherDto hourDto = getArguments().getParcelable("hourDto");
		tv1.setText(hourDto.hourlyTemp+""+getString(R.string.unit_degree));
		tv2.setText(hourDto.hourlyHumidity+getString(R.string.unit_percent));
		tv3.setText(WeatherUtil.getHourWindForce(hourDto.hourlyWindForceCode));
		tv4.setText(getActivity().getString(WeatherUtil.getWindDirection(hourDto.hourlyWindDirCode)));
		
		Drawable drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
		try {
			long zao8 = sdf2.parse("06").getTime();
			long wan8 = sdf2.parse("20").getTime();
			long current = sdf2.parse(sdf2.format(new Date())).getTime();
			if (current >= zao8 && current < wan8) {
				drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
			}else {
				drawable = getResources().getDrawable(R.drawable.phenomenon_drawable_night);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		drawable.setLevel(hourDto.hourlyCode);
		ivPhe.setBackground(drawable);
		tv5.setText(getString(WeatherUtil.getWeatherId(hourDto.hourlyCode)));
	}
	
}
