package com.cxwl.guangxi.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.dto.CityDto;

import java.util.List;

/**
 * 选择城市、区域
 */

public class DialogSelectAreaAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<CityDto> groupList;
    private List<List<CityDto>> childList;
    private LayoutInflater mInflater = null;

    public DialogSelectAreaAdapter(Context context, List<CityDto> groupList, List<List<CityDto>> childList){
        mContext = context;
        this.groupList = groupList;
        this.childList = childList;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private GroupHolder groupHolder = null;
    class GroupHolder{
        TextView tvCityName;
    }

    private ChildHolder childHolder = null;
    class ChildHolder{
        TextView tvAreaName;
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.adapter_select_area_group, null);
            groupHolder = new GroupHolder();
            groupHolder.tvCityName = (TextView) convertView.findViewById(R.id.tvCityName);
            convertView.setTag(groupHolder);
        }else{
            groupHolder = (GroupHolder) convertView.getTag();
        }

        CityDto dto = groupList.get(groupPosition);
        if (!TextUtils.isEmpty(dto.cityName)) {
            groupHolder.tvCityName.setText(dto.cityName);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.adapter_select_area_child, null);
            childHolder = new ChildHolder();
            childHolder.tvAreaName = (TextView) convertView.findViewById(R.id.tvAreaName);
            convertView.setTag(childHolder);
        }else{
            childHolder = (ChildHolder) convertView.getTag();
        }

        CityDto dto = childList.get(groupPosition).get(childPosition);
        if (!TextUtils.isEmpty(dto.areaName)) {
            childHolder.tvAreaName.setText(dto.areaName);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
