package com.cxwl.guangxi.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.dto.CityDto;
import com.cxwl.stickygridheaders.StickyGridHeadersSimpleAdapter;

public class CityLocalAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {

	private Context mContext = null;
	private List<CityDto> mArrayList = null;
	private LayoutInflater mInflater = null;

	public CityLocalAdapter(Context context, List<CityDto> mArrayList) {
		this.mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	private ViewHolder mHolder = null;
	
	private class ViewHolder {
		TextView tvCityName;
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return mArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			mHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.adapter_content, null);
			mHolder.tvCityName = (TextView) convertView.findViewById(R.id.tvCityName);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		CityDto dto = mArrayList.get(position);
		mHolder.tvCityName.setText(dto.areaName);

		return convertView;
	}
	
	private HeaderViewHolder mHeaderHolder = null;

	private class HeaderViewHolder {
		TextView tvName;
	}

	@Override
	public long getHeaderId(int position) {
		return mArrayList.get(position).section;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			mHeaderHolder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.adapter_header, null);
			mHeaderHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			convertView.setTag(mHeaderHolder);
		} else {
			mHeaderHolder = (HeaderViewHolder) convertView.getTag();
		}
		
		CityDto dto = mArrayList.get(position);
		mHeaderHolder.tvName.setText(dto.sectionName);

		return convertView;
	}

}
