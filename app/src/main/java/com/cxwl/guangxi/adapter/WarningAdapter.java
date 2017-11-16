package com.cxwl.guangxi.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.dto.WarningDto;
import com.cxwl.guangxi.utils.CommonUtil;

@SuppressLint("SimpleDateFormat")
public class WarningAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<WarningDto> mArrayList = null;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private boolean isMarkerCell = false;
	
	private final class ViewHolder {
		ImageView imageView;//预警icon
		TextView tvName;//预警信息名称
		TextView tvTime;//时间
	}
	
	private ViewHolder mHolder = null;
	
	public WarningAdapter(Context context, List<WarningDto> mArrayList, boolean isMarkerCell) {
		mContext = context;
		this.isMarkerCell = isMarkerCell;
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
			if (isMarkerCell == false) {
				convertView = mInflater.inflate(R.layout.warning_cell, null);
			}else {
				convertView = mInflater.inflate(R.layout.warning_marker_info_cell, null);
			}
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		WarningDto dto = mArrayList.get(position);
		
        Bitmap bitmap = null;
		if (dto.color.equals(CONST.blue[0])) {
			bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.blue[1]+CONST.imageSuffix);
			if (bitmap == null) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
			}
		}else if (dto.color.equals(CONST.yellow[0])) {
			bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.yellow[1]+CONST.imageSuffix);
			if (bitmap == null) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
			}
		}else if (dto.color.equals(CONST.orange[0])) {
			bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.orange[1]+CONST.imageSuffix);
			if (bitmap == null) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
			}
		}else if (dto.color.equals(CONST.red[0])) {
			bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.red[1]+CONST.imageSuffix);
			if (bitmap == null) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
			}
		}
		mHolder.imageView.setImageBitmap(bitmap);
		
		if (!TextUtils.isEmpty(dto.name)) {
			if (isMarkerCell == false) {
				mHolder.tvName.setText(dto.name);
			}else {
				if (dto.name.contains("解除")) {
					mHolder.tvName.setText(dto.name.replace("解除", "解除"+"\n"));
				}else if (dto.name.contains("发布")) {
					mHolder.tvName.setText(dto.name.replace("发布", "发布"+"\n"));
				}else if (dto.name.contains("为")) {
					mHolder.tvName.setText(dto.name.replace("为", "为"+"\n"));
				}
			}
		}
		
		try {
			mHolder.tvTime.setText(sdf2.format(sdf1.parse(dto.time)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}

}
