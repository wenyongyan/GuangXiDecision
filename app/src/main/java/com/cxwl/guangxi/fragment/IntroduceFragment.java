package com.cxwl.guangxi.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cxwl.guangxi.R;

/**
 * 服务材料
 * @author shawn_sun
 *
 */

public class IntroduceFragment extends Fragment{
	
	private TextView tvTitle = null;
	private TextView tvContent = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_introduce, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	private void initWidget(View view) {
		tvTitle = (TextView) view.findViewById(R.id.tvTitle);
		tvContent = (TextView) view.findViewById(R.id.tvContent);
		
		tvTitle.setText(getArguments().getString("title"));
		tvContent.setText(getArguments().getString("content"));
	}
	
}
