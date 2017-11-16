package com.cxwl.guangxi.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.cxwl.guangxi.PDFActivity;
import com.cxwl.guangxi.R;
import com.cxwl.guangxi.adapter.PDFListAdapter;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.dto.NewsDto;

/**
 * 服务材料
 * @author shawn_sun
 *
 */

public class ServiceMaterialFragment extends Fragment{
	
	private ListView listView = null;
	private PDFListAdapter mAdapter = null;
	private List<NewsDto> mList = new ArrayList<NewsDto>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_service_material, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initListView(view);
	}
	
	private void initListView(View view) {
		NewsDto data = getArguments().getParcelable("data");
		if (data != null) {
			mList.clear();
			mList.addAll(data.itemList);
		}
		
		listView = (ListView) view.findViewById(R.id.listView);
		mAdapter = new PDFListAdapter(getActivity(), mList);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				NewsDto dto = mList.get(arg2);
				Intent intent = new Intent(getActivity(), PDFActivity.class);
				intent.putExtra(CONST.ACTIVITY_NAME, dto.title);
				intent.putExtra(CONST.WEB_URL, dto.detailUrl);
				startActivity(intent);
			}
		});
	}
	
}
