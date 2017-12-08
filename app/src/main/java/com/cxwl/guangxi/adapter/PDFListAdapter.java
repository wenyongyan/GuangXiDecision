package com.cxwl.guangxi.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.dto.NewsDto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PDFListAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<NewsDto> mArrayList = new ArrayList<>();
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
	
	private final class ViewHolder{
		TextView tvTitle;
		TextView tvTime;
	}
	
	private ViewHolder mHolder = null;
	
	public PDFListAdapter(Context context, List<NewsDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_pdflist, null);
			mHolder = new ViewHolder();
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		NewsDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.title)) {
			mHolder.tvTitle.setText(dto.title);
		}
		if (!TextUtils.isEmpty(dto.time)) {
			mHolder.tvTime.setText(dto.time);
			try {
				String time1 = sdf2.format(sdf1.parse(dto.time));
				String time2 = sdf2.format(new Date());
				if (TextUtils.equals(time1, time2)) {
					mHolder.tvTitle.setTextColor(Color.RED);
					mHolder.tvTime.setTextColor(Color.RED);
				}else {
					mHolder.tvTitle.setTextColor(mContext.getResources().getColor(R.color.text_color3));
					mHolder.tvTime.setTextColor(mContext.getResources().getColor(R.color.text_color4));
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return convertView;
	}

}
