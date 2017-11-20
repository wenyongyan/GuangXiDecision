package com.cxwl.guangxi.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.cxwl.guangxi.R;
import com.cxwl.guangxi.dto.FactDto;

import java.util.ArrayList;
import java.util.List;

/**
 * 实况资料详情
 */

public class FactDetailAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<FactDto> mArrayList = new ArrayList<>();
	
	private final class ViewHolder{
		TextView tvStationName;
		TextView tvArea;
		TextView tvValue;
	}
	
	private ViewHolder mHolder = null;
	
	public FactDetailAdapter(Context context, List<FactDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_fact_detail, null);
			mHolder = new ViewHolder();
			mHolder.tvStationName = (TextView) convertView.findViewById(R.id.tvStationName);
			mHolder.tvArea = (TextView) convertView.findViewById(R.id.tvArea);
			mHolder.tvValue = (TextView) convertView.findViewById(R.id.tvValue);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		FactDto dto = mArrayList.get(position);
		
		if (!TextUtils.isEmpty(dto.stationName)) {
			mHolder.tvStationName.setText(dto.stationName);
		}
		
		if (!TextUtils.isEmpty(dto.area1)) {
			mHolder.tvArea.setText(dto.area1);
		}
		
		mHolder.tvValue.setText(dto.val+"");
		
		if (position % 2 == 0) {
			convertView.setBackgroundColor(0xffeaeaea);
		}else {
			convertView.setBackgroundColor(0xfff5f5f5);
		}
		
		return convertView;
	}

}
