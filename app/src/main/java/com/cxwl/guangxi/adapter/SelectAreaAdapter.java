package com.cxwl.guangxi.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.dto.CityDto;

/**
 * 选择关注区域
 * @author shawn_sun
 *
 */

public class SelectAreaAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<CityDto> mArrayList = new ArrayList<CityDto>();
	
	private final class ViewHolder{
		TextView tvArea;
	}
	
	private ViewHolder mHolder = null;
	
	public SelectAreaAdapter(Context context, List<CityDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_select_area, null);
			mHolder = new ViewHolder();
			mHolder.tvArea = (TextView) convertView.findViewById(R.id.tvArea);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		try {
			CityDto dto = mArrayList.get(position);
			mHolder.tvArea.setText(dto.areaName);
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}

}
