package com.cxwl.guangxi.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.dto.IndexDto;
import com.cxwl.guangxi.utils.CommonUtil;

public class IndexFragmentAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<IndexDto> mArrayList = null;
	
	private final class ViewHolder {
		private ImageView imageView;
		private TextView tvName;
		private TextView tvLevel_zh;
	}
	
	private ViewHolder mHolder = null;
	
	public IndexFragmentAdapter(Context context, List<IndexDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.fragment_index_item, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.tvLevel_zh = (TextView) convertView.findViewById(R.id.tvLevel_zh);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		IndexDto dto = mArrayList.get(position);
		mHolder.tvName.setText(dto.name);
		mHolder.tvLevel_zh.setText(dto.level_zh);
		
		Bitmap bitmap = CommonUtil.getImageFromAssetsFile(mContext, "index/"+dto.abbr+CONST.imageSuffix);
		if (bitmap != null) {
			mHolder.imageView.setImageBitmap(bitmap);
		}
		
		return convertView;
	}

}
