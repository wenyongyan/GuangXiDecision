package com.cxwl.guangxi.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.dto.FactDto;
import com.cxwl.guangxi.utils.CommonUtil;
import com.cxwl.guangxi.view.RainView;
import com.cxwl.guangxi.view.TempView;
import com.cxwl.guangxi.view.TempViewHigh;
import com.cxwl.guangxi.view.TempViewLow;
import com.cxwl.guangxi.view.WindView;

@SuppressLint("SimpleDateFormat")
public class FactAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<FactDto> mArrayList = new ArrayList<FactDto>();
	private int width = 0;
	public String selectId = null;//选中的站点
	public List<FactDto> rankList = new ArrayList<FactDto>();
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHH");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH");
	public String colomnId = null;
	private String TEMPID = "107";
	private String WINDID = "106";
	private String RAINID = "105";
	public String flag = null;
	private String TEMP_08_MAX = "08_tmax";
	private String TEMP_08_MIN = "08_tmin";
	private String TEMP_20_MAX = "20_tmax";
	private String TEMP_20_MIN = "20_tmin";
	private String RAIN_08 = "08";
	private String RAIN_20 = "20";
	
	private final class ViewHolder{
		TextView tvName;
		TextView tvValue;
		ImageView ivArrow;
		RelativeLayout reContainer;
		LinearLayout llContainer;
		ProgressBar progressBar;
	}
	
	private ViewHolder mHolder = null;
	
	@SuppressWarnings("deprecation")
	public FactAdapter(Context context, List<FactDto> mArrayList, String colomnId) {
		mContext = context;
		this.colomnId = colomnId;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
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
			convertView = mInflater.inflate(R.layout.adapter_fact, null);
			mHolder = new ViewHolder();
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.tvValue = (TextView) convertView.findViewById(R.id.tvValue);
			mHolder.ivArrow = (ImageView) convertView.findViewById(R.id.ivArrow);
			mHolder.reContainer = (RelativeLayout) convertView.findViewById(R.id.reContainer);
			mHolder.llContainer = (LinearLayout) convertView.findViewById(R.id.llContainer);
			mHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		FactDto dto = mArrayList.get(position);
		mHolder.tvName.setText(dto.name);
		if (TextUtils.equals(colomnId, TEMPID)) {//温度
			mHolder.tvValue.setText(dto.temp);
		}else if (TextUtils.equals(colomnId, WINDID)) {//风速风向
			mHolder.tvValue.setText(CommonUtil.getWindDir(dto.windDir)+"风 "+dto.windSpeed);
		}else if (TextUtils.equals(colomnId, RAINID)) {//降水
			mHolder.tvValue.setText(dto.rain);
		}
		
		if (TextUtils.equals(dto.id, selectId)) {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.title_bg));
			mHolder.tvValue.setTextColor(mContext.getResources().getColor(R.color.title_bg));
			mHolder.ivArrow.setImageResource(R.drawable.iv_arrow_black_down);
			mHolder.llContainer.removeAllViews();
			mHolder.reContainer.setVisibility(View.VISIBLE);
			mHolder.progressBar.setVisibility(View.VISIBLE);
			try {
				if (!rankList.isEmpty()) {
					if (TextUtils.equals(colomnId, TEMPID)) {//温度
						if (TextUtils.equals(flag, TEMP_08_MAX) || TextUtils.equals(flag, TEMP_20_MAX)) {//温度
							TempViewHigh tempView = new TempViewHigh(mContext);
							tempView.setData(rankList, Integer.valueOf(sdf2.format(sdf1.parse(rankList.get(0).time))));
							mHolder.llContainer.addView(tempView, width, (int)CommonUtil.dip2px(mContext, 120));
						}else if (TextUtils.equals(flag, TEMP_08_MIN) || TextUtils.equals(flag, TEMP_20_MIN)) {//温度
							TempViewLow tempView = new TempViewLow(mContext);
							tempView.setData(rankList, Integer.valueOf(sdf2.format(sdf1.parse(rankList.get(0).time))));
							mHolder.llContainer.addView(tempView, width, (int)CommonUtil.dip2px(mContext, 120));
						}else {
							TempView tempView = new TempView(mContext);
							tempView.setData(rankList, Integer.valueOf(sdf2.format(sdf1.parse(rankList.get(0).time))));
							mHolder.llContainer.addView(tempView, width, (int)CommonUtil.dip2px(mContext, 120));
						}
					}else if (TextUtils.equals(colomnId, WINDID)) {//风速风向
						WindView windView = new WindView(mContext);
						windView.setData(rankList, Integer.valueOf(sdf2.format(sdf1.parse(rankList.get(0).time))));
						mHolder.llContainer.addView(windView, width, (int)CommonUtil.dip2px(mContext, 120));
					}else if (TextUtils.equals(colomnId, RAINID)) {//降水
						RainView rainView = new RainView(mContext);
						rainView.setData(rankList, Integer.valueOf(sdf2.format(sdf1.parse(rankList.get(0).time))));
						mHolder.llContainer.addView(rainView, width, (int)CommonUtil.dip2px(mContext, 120));
					}
					mHolder.progressBar.setVisibility(View.GONE);
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}else {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.text_color4));
			mHolder.tvValue.setTextColor(mContext.getResources().getColor(R.color.text_color4));
			mHolder.ivArrow.setImageResource(R.drawable.iv_arrow_black_right);
			mHolder.llContainer.removeAllViews();
			mHolder.reContainer.setVisibility(View.GONE);
			mHolder.progressBar.setVisibility(View.GONE);
		}
		
		return convertView;
	}

}
