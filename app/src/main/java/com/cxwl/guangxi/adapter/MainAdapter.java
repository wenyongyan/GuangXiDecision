package com.cxwl.guangxi.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.common.ColumnData;

import net.tsz.afinal.FinalBitmap;

import java.util.ArrayList;
import java.util.List;

public class MainAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<ColumnData> mArrayList = new ArrayList<>();
	private int height = 0;
	
	private final class ViewHolder{
		TextView tvName;
		ImageView icon;
	}
	
	private ViewHolder mHolder = null;
	
	public MainAdapter(Context context, List<ColumnData> mArrayList, int height) {
		mContext = context;
		this.height = height;
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
			convertView = mInflater.inflate(R.layout.adapter_main, null);
			mHolder = new ViewHolder();
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		ColumnData dto = mArrayList.get(position);
		mHolder.tvName.setText(dto.name);
		
		if (!TextUtils.isEmpty(dto.icon)) {
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.icon, dto.icon, null, 0);
		}
		
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.height = height/3;
		convertView.setLayoutParams(params);
		
		return convertView;
	}

}
