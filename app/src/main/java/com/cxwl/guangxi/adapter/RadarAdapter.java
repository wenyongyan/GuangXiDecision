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
import com.cxwl.guangxi.dto.RadarDto;

public class RadarAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<RadarDto> mArrayList = new ArrayList<RadarDto>();
	private String selected = "1";//选中
	private String unselected = "0";//未选中
	
	private final class ViewHolder{
		TextView tvName;
	}
	
	private ViewHolder mHolder = null;
	
	public RadarAdapter(Context context, List<RadarDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_radar, null);
			mHolder = new ViewHolder();
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		RadarDto dto = mArrayList.get(position);
		mHolder.tvName.setText(dto.radarName);
		if (dto.isSelect.equals(selected)) {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.blue));
		}else if (dto.isSelect.equals(unselected)){
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.text_color2));
		}
		
		return convertView;
	}

}
