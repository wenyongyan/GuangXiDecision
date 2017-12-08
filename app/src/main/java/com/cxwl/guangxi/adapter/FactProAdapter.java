package com.cxwl.guangxi.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cxwl.guangxi.activity.FactCityActivity;
import com.cxwl.guangxi.R;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.common.ColumnData;
import com.cxwl.guangxi.dto.FactDto;
import com.cxwl.guangxi.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 全省实况资料
 */

public class FactProAdapter extends BaseAdapter{
	
	private Activity activity = null;
	private LayoutInflater mInflater = null;
	private List<FactDto> mArrayList = new ArrayList<>();
	public String stationName = "", area = "", val = "", timeString = "";
	public List<FactDto> realDatas = new ArrayList<>();
	private ColumnData columnData = null;
	
	private final class ViewHolder{
		TextView tvRailLevel;
		TextView tvCount;
		LinearLayout llContainer;
	}
	
	private ViewHolder mHolder = null;
	
	public FactProAdapter(Activity activity, List<FactDto> mArrayList, ColumnData columnData) {
		this.columnData = columnData;
		this.activity = activity;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_fact_pro, null);
			mHolder = new ViewHolder();
			mHolder.tvRailLevel = (TextView) convertView.findViewById(R.id.tvRailLevel);
			mHolder.tvCount = (TextView) convertView.findViewById(R.id.tvCount);
			mHolder.llContainer = (LinearLayout) convertView.findViewById(R.id.llContainer);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		FactDto dto = mArrayList.get(position);
		
		if (!TextUtils.isEmpty(dto.rainLevel)) {
			mHolder.tvRailLevel.setText(dto.rainLevel);
		}
		
		if (!TextUtils.isEmpty(dto.count)) {
			mHolder.tvCount.setText(dto.count);
		}
		
		mHolder.llContainer.removeAllViews();
		if (dto.areaList.size() > 0) {
			if (dto.areaList.size() <= 4) {
				mHolder.llContainer.setOrientation(LinearLayout.HORIZONTAL);
				for (int i = 0; i < dto.areaList.size(); i++) {
					final FactDto data = dto.areaList.get(i);
					if (!TextUtils.isEmpty(data.area)) {
						TextView tvArea = new TextView(activity);
						tvArea.setGravity(Gravity.CENTER);
						tvArea.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
						tvArea.setTextColor(activity.getResources().getColor(R.color.title_bg));
						tvArea.setPadding(10, 0, 10, 0);
						tvArea.setText(data.area);
						tvArea.setTag(data.area);
						mHolder.llContainer.addView(tvArea);
						tvArea.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
//								Intent intent = new Intent(activity, FactDetailActivity.class);
//								intent.putExtra("title", data.area);
//								intent.putExtra("timeString", timeString);
//								intent.putExtra("stationName", stationName);
//								intent.putExtra("area", area);
//								intent.putExtra("val", val);
//								Bundle bundle = new Bundle();
//								List<FactDto> list = new ArrayList<>();
//								for (int j = 0; j < realDatas.size(); j++) {
//									if (realDatas.get(j).area.contains(data.area)) {
//										list.add(realDatas.get(j));
//									}
//								}
//								bundle.putParcelableArrayList("realDatas", (ArrayList<? extends Parcelable>) list);
//								intent.putExtras(bundle);
//								activity.startActivity(intent);

								String adCode = getAdCode(data.area);
								if (TextUtils.equals(CONST.AREAID, "450000") || TextUtils.isEmpty(CONST.AREAID)) {
									Intent intent = new Intent(activity, FactCityActivity.class);
									intent.putExtra("adCode", adCode);
									Bundle bundle = new Bundle();
									bundle.putParcelable("data", columnData);
									intent.putExtras(bundle);
									activity.startActivity(intent);
								}else {
									Intent intent = new Intent();
									intent.putExtra("adCode", adCode);
									Bundle bundle = new Bundle();
									bundle.putParcelable("data", columnData);
									intent.putExtras(bundle);
									activity.setResult(Activity.RESULT_OK, intent);
									activity.finish();
								}
							}
						});
					}
				}
			}else {
				mHolder.llContainer.setOrientation(LinearLayout.VERTICAL);
				mHolder.llContainer.setGravity(Gravity.CENTER|Gravity.CENTER_VERTICAL);
				int containerCount = 0;
				if (dto.areaList.size() % 4 == 0) {
					containerCount = dto.areaList.size() / 4;
				}else {
					containerCount = dto.areaList.size() / 4 + 1;
				}
				for (int j = 0; j < containerCount; j++) {
					LinearLayout llItem = new LinearLayout(activity);
					llItem.setOrientation(LinearLayout.HORIZONTAL);
					llItem.setGravity(Gravity.CENTER|Gravity.CENTER_VERTICAL);
					
					int i = 0;
					int size = j*4+4;
					if (size >= dto.areaList.size()) {
						size = dto.areaList.size();
					}
					for (i = j*4; i < size; i++) {
						final FactDto data = dto.areaList.get(i);
						if (!TextUtils.isEmpty(data.area)) {
							TextView tvArea = new TextView(activity);
							tvArea.setGravity(Gravity.CENTER);
							tvArea.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
							tvArea.setTextColor(activity.getResources().getColor(R.color.title_bg));
							tvArea.setPadding(10, 0, 10, 0);
							tvArea.setText(data.area);
							tvArea.setTag(data.area);
							llItem.addView(tvArea);
							LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
							params.topMargin = (int) CommonUtil.dip2px(activity, 5);
							params.bottomMargin = (int)CommonUtil.dip2px(activity, 5);
							llItem.setLayoutParams(params);
							tvArea.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View arg0) {
//									Intent intent = new Intent(activity, FactDetailActivity.class);
//									intent.putExtra("title", data.area);
//									intent.putExtra("timeString", timeString);
//									intent.putExtra("stationName", stationName);
//									intent.putExtra("area", area);
//									intent.putExtra("val", val);
//									Bundle bundle = new Bundle();
//									List<FactDto> list = new ArrayList<>();
//									for (int j = 0; j < realDatas.size(); j++) {
//										if (realDatas.get(j).area.contains(data.area)) {
//											list.add(realDatas.get(j));
//										}
//									}
//									bundle.putParcelableArrayList("realDatas", (ArrayList<? extends Parcelable>) list);
//									intent.putExtras(bundle);
//									activity.startActivity(intent);


									String adCode = getAdCode(data.area);
									if (TextUtils.equals(CONST.AREAID, "450000") || TextUtils.isEmpty(CONST.AREAID)) {
										Intent intent = new Intent(activity, FactCityActivity.class);
										intent.putExtra("adCode", adCode);
                                        Bundle bundle = new Bundle();
                                        bundle.putParcelable("data", columnData);
                                        intent.putExtras(bundle);
										activity.startActivity(intent);
									}else {
										Intent intent = new Intent();
										intent.putExtra("adCode", adCode);
                                        Bundle bundle = new Bundle();
                                        bundle.putParcelable("data", columnData);
                                        intent.putExtras(bundle);
										activity.setResult(Activity.RESULT_OK, intent);
										activity.finish();
									}
								}
							});
						}
					}
					
					mHolder.llContainer.addView(llItem);
				}
			}
		}else {
			TextView tvArea = new TextView(activity);
			tvArea.setGravity(Gravity.CENTER);
			tvArea.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			tvArea.setTextColor(activity.getResources().getColor(R.color.text_color3));
			tvArea.setPadding(10, 0, 10, 0);
			tvArea.setText("无");
			mHolder.llContainer.addView(tvArea);
		}
		
		if (position % 2 == 0) {
			convertView.setBackgroundColor(0xffeaeaea);
		}else {
			convertView.setBackgroundColor(0xfff5f5f5);
		}
		
		return convertView;
	}

	private String getAdCode(String name) {
		String adCode = "450100";
		if (name.contains("南宁")) {
			adCode = "450100";
		}else if (name.contains("柳州")) {
			adCode = "450200";
		}else if (name.contains("桂林")) {
			adCode = "450300";
		}else if (name.contains("梧州")) {
			adCode = "450400";
		}else if (name.contains("北海")) {
			adCode = "450500";
		}else if (name.contains("防城港")) {
			adCode = "450600";
		}else if (name.contains("钦州")) {
			adCode = "450700";
		}else if (name.contains("贵港")) {
			adCode = "450800";
		}else if (name.contains("玉林")) {
			adCode = "450900";
		}else if (name.contains("百色")) {
			adCode = "451000";
		}else if (name.contains("贺州")) {
			adCode = "451100";
		}else if (name.contains("河池")) {
			adCode = "451200";
		}else if (name.contains("来宾")) {
			adCode = "451300";
		}else if (name.contains("崇左")) {
			adCode = "451400";
		}
		return adCode;
	}

}
