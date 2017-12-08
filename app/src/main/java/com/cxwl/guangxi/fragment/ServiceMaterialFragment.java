package com.cxwl.guangxi.fragment;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.activity.PDFActivity;
import com.cxwl.guangxi.adapter.DialogSelectAreaAdapter;
import com.cxwl.guangxi.adapter.PDFListAdapter;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.common.ColumnData;
import com.cxwl.guangxi.dto.CityDto;
import com.cxwl.guangxi.dto.NewsDto;
import com.cxwl.guangxi.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 服务材料
 * @author shawn_sun
 *
 */

public class ServiceMaterialFragment extends Fragment implements View.OnClickListener{
	
	private ListView listView = null;
	private PDFListAdapter mAdapter = null;
	private List<NewsDto> mList = new ArrayList<>();
	private List<NewsDto> dataList = new ArrayList<>();
	private TextView tvSelect, tvPrompt;
	private ExpandableListView listView2 = null;
	private DialogSelectAreaAdapter mAdapter2 = null;
	private List<CityDto> groupList = new ArrayList<>();
	private List<List<CityDto>> childList = new ArrayList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_service_material, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
		initListView(view);
	}

	private void initWidget(View view) {
		tvSelect = (TextView) view.findViewById(R.id.tvSelect);
		tvSelect.setOnClickListener(this);
		tvPrompt = (TextView) view.findViewById(R.id.tvPrompt);

		ColumnData data = getArguments().getParcelable("data");
		if (data != null) {
			if (data.name.contains("服务材料")) {
				tvSelect.setVisibility(View.VISIBLE);
				OkHttpCityInfo("http://decision-admin.tianqi.cn/Home/extra/getGxFwclAreas");
			}else {
				tvSelect.setVisibility(View.GONE);
			}
			if (!TextUtils.isEmpty(data.dataUrl)) {
				OkHttpList(data.dataUrl, data.name);
			}
		}
	}
	
	private void initListView(View view) {
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

	private void OkHttpList(String url, final String name) {
		OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				String result = response.body().string();
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject obj = new JSONObject(result);
						if (!obj.isNull("info")) {
							JSONArray array = obj.getJSONArray("info");
							for (int i = 0; i < array.length(); i++) {
								NewsDto dto = new NewsDto();
								JSONObject itemObj = array.getJSONObject(i);
								if (!itemObj.isNull("time")) {
									dto.time = itemObj.getString("time");
								}
								if (!itemObj.isNull("url")) {
									String url = itemObj.getString("url");
									if (!TextUtils.isEmpty(url)) {
										dto.detailUrl = url;
										String title = url.substring(url.lastIndexOf("/")+1, url.lastIndexOf("."));
										dto.title = title;
										if (name.contains("服务材料")) {
											if (TextUtils.equals(CONST.AREAID, "450000") || TextUtils.isEmpty(CONST.AREAID)) {
												dataList.add(dto);
											}else {
												if (isContain(CONST.AREAID, url)) {
													dataList.add(dto);
												}
											}
										}else {
											dataList.add(dto);
										}
									}
								}
							}

							mList.clear();
							mList.addAll(dataList);
						}

						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (mList.size() > 0 && mAdapter != null) {
									mAdapter.notifyDataSetChanged();
								}
							}
						});
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * 判断是否包含区县
	 * @param areaid
	 * @param title
	 * @return
	 */
	private boolean isContain(String areaid, String title) {
		boolean flag = false;
		if (TextUtils.equals(areaid, "450100")) {//南宁
			if (title.contains("南宁") || title.contains("邕宁") || title.contains("武鸣") || title.contains("马山")
					|| title.contains("上林") || title.contains("宾阳") || title.contains("横县") || title.contains("隆安")) {
				flag = true;
			}
		}else if (TextUtils.equals(areaid, "450200")) {//柳州
			if (title.contains("柳州") || title.contains("三江") || title.contains("融安") || title.contains("融水")
					|| title.contains("鹿寨") || title.contains("柳城") || title.contains("柳江")) {
				flag = true;
			}
		}else if (TextUtils.equals(areaid, "450300")) {//桂林
			if (title.contains("桂林") || title.contains("资源") || title.contains("全州") || title.contains("龙胜")
					|| title.contains("灵川") || title.contains("临桂") || title.contains("兴安") || title.contains("灌阳")
					|| title.contains("永福") || title.contains("阳朔") || title.contains("恭城") || title.contains("荔浦")
					|| title.contains("平乐")) {
				flag = true;
			}
		}else if (TextUtils.equals(areaid, "450400")) {//梧州
			if (title.contains("梧州") || title.contains("苍梧") || title.contains("藤县") || title.contains("岑溪")
					|| title.contains("蒙山")) {
				flag = true;
			}
		}else if (TextUtils.equals(areaid, "450500")) {//北海
			if (title.contains("北海") || title.contains("合浦")) {
				flag = true;
			}
		}else if (TextUtils.equals(areaid, "450600")) {//防城港
			if (title.contains("防城港") || title.contains("东兴") || title.contains("防城") || title.contains("上思")) {
				flag = true;
			}
		}else if (TextUtils.equals(areaid, "450700")) {//钦州
			if (title.contains("钦州") || title.contains("灵山") || title.contains("浦北")) {
				flag = true;
			}
		}else if (TextUtils.equals(areaid, "450800")) {//贵港
			if (title.contains("贵港") || title.contains("平南") || title.contains("桂平")) {
				flag = true;
			}
		}else if (TextUtils.equals(areaid, "450900")) {//玉林
			if (title.contains("玉林") || title.contains("容县") || title.contains("北流") || title.contains("陆川")
					|| title.contains("博白")) {
				flag = true;
			}
		}else if (TextUtils.equals(areaid, "451000")) {//百色
			if (title.contains("百色") || title.contains("凌云") || title.contains("乐业") || title.contains("田林")
					|| title.contains("隆林") || title.contains("西林") || title.contains("田阳") || title.contains("田东")
					|| title.contains("平果") || title.contains("德保") || title.contains("靖西") || title.contains("那坡")) {
				flag = true;
			}
		}else if (TextUtils.equals(areaid, "451100")) {//贺州
			if (title.contains("贺州") || title.contains("八步") || title.contains("富川") || title.contains("中山")
					|| title.contains("昭平")) {
				flag = true;
			}
		}else if (TextUtils.equals(areaid, "451200")) {//河池
			if (title.contains("河池") || title.contains("金城江") || title.contains("罗城") || title.contains("宜州")
					|| title.contains("都安") || title.contains("环江") || title.contains("南丹") || title.contains("天峨")
					|| title.contains("凤山") || title.contains("东兰") || title.contains("巴马") || title.contains("大化")) {
				flag = true;
			}
		}else if (TextUtils.equals(areaid, "451300")) {//来宾
			if (title.contains("来宾") || title.contains("兴宾") || title.contains("忻城") || title.contains("武宣")
					|| title.contains("金秀") || title.contains("象州")) {
				flag = true;
			}
		}else if (TextUtils.equals(areaid, "451400")) {//崇左
			if (title.contains("崇左") || title.contains("江州") || title.contains("天等") || title.contains("大新")
					|| title.contains("扶绥") || title.contains("宁明") || title.contains("龙州") || title.contains("凭祥")) {
				flag = true;
			}
		}
		return  flag;
	}

	private void OkHttpCityInfo(String url) {
		OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				final String result = response.body().string();
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!TextUtils.isEmpty(result)) {
							if (TextUtils.equals(CONST.AREAID, "450000") || TextUtils.isEmpty(CONST.AREAID)) {
								if (!TextUtils.isEmpty(result)) {
									try {
										groupList.clear();
										childList.clear();
										JSONArray array = new JSONArray(result);
										for (int i = 0; i < array.length(); i++) {
											JSONObject itemObj = array.getJSONObject(i);
											CityDto data = new CityDto();
											if (!itemObj.isNull("city")) {
												data.cityName = itemObj.getString("city");
											}
											if (i == 0) {
												tvSelect.setText(data.cityName);
											}
											if (!itemObj.isNull("adCode")) {
												data.adCode = itemObj.getString("adCode");
											}
											if (!itemObj.isNull("areas")) {
												List<CityDto> list = new ArrayList<>();
												JSONArray itemArray = itemObj.getJSONArray("areas");
												for (int j = 0; j < itemArray.length(); j++) {
													CityDto dto = new CityDto();
													dto.areaName = itemArray.getString(j);
													list.add(dto);
													if (i == 0 && j == 0) {
														tvSelect.setText(data.cityName+" - "+dto.areaName);
													}
												}
												childList.add(list);
											}
											groupList.add(data);
										}

										if (mAdapter2 != null) {
											mAdapter2.notifyDataSetChanged();
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}else {
								if (!TextUtils.isEmpty(result)) {
									try {
										groupList.clear();
										JSONArray array = new JSONArray(result);
										for (int i = 0; i < array.length(); i++) {
											JSONObject itemObj = array.getJSONObject(i);
											CityDto data = new CityDto();
											if (!itemObj.isNull("adCode")) {
												data.adCode = itemObj.getString("adCode");
											}
											if (TextUtils.equals(CONST.AREAID, data.adCode)) {
												if (!itemObj.isNull("areas")) {
													JSONArray itemArray = itemObj.getJSONArray("areas");
													for (int j = 0; j < itemArray.length(); j++) {
														CityDto dto = new CityDto();
														dto.cityName = itemArray.getString(j);
														groupList.add(dto);
														if (j == 0) {
															tvSelect.setText(dto.cityName);
														}
													}
													break;
												}
											}
										}

										if (mAdapter2 != null) {
											mAdapter2.notifyDataSetChanged();
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				});
			}
		});
	}

	/**
	 * 选择关注区域
	 */
	private void selectDialog() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = inflater.inflate(R.layout.dialog_select_city_area, null);
		listView2 = (ExpandableListView) dialogView.findViewById(R.id.listView2);
		mAdapter2 = new DialogSelectAreaAdapter(getActivity(), groupList, childList);
		listView2.setAdapter(mAdapter2);

		final Dialog dialog = new Dialog(getActivity(), R.style.CustomProgressDialog);
		dialog.setContentView(dialogView);
		dialog.show();

		listView2.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				CityDto dto = groupList.get(groupPosition);
				if (TextUtils.equals(CONST.AREAID, "450000") || TextUtils.isEmpty(CONST.AREAID)) {
					if (TextUtils.equals(dto.adCode, "450000")) {
						tvSelect.setText(dto.cityName);
						mList.clear();
						mList.addAll(dataList);
						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
						dialog.dismiss();
					}else {
						if (listView2.isGroupExpanded(groupPosition)) {
							listView2.collapseGroup(groupPosition);
						}else {
							listView2.expandGroup(groupPosition);
						}
					}
				}else {
					tvSelect.setText(dto.cityName);
					mList.clear();
					for (int i = 0; i < dataList.size(); i++) {
						NewsDto d = dataList.get(i);
						if (dto.cityName.contains("全部")) {
							mList.add(d);
						}else {
							if (d.detailUrl.contains(dto.cityName)) {
								mList.add(d);
							}
						}
					}
					if (mList.size() == 0) {
						tvPrompt.setText("暂无"+"\""+dto.cityName+"\""+"资料");
						tvPrompt.setVisibility(View.VISIBLE);
					}else {
						tvPrompt.setText("");
						tvPrompt.setVisibility(View.GONE);
					}
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
					dialog.dismiss();
				}
				return true;
			}
		});
		listView2.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				CityDto data = groupList.get(groupPosition);
				CityDto dto = childList.get(groupPosition).get(childPosition);
				tvSelect.setText(data.cityName+" - "+dto.areaName);

				mList.clear();
				for (int i = 0; i < dataList.size(); i++) {
					NewsDto d = dataList.get(i);
					if (dto.areaName.contains("全部")) {
						if (isContain(data.adCode, d.detailUrl)) {
							mList.add(d);
						}
					}else {
						if (d.detailUrl.contains(dto.areaName)) {
							mList.add(d);
						}
					}
				}
				if (mList.size() == 0) {
					tvPrompt.setText("暂无"+"\""+data.cityName+" - "+dto.areaName+"\""+"资料");
					tvPrompt.setVisibility(View.VISIBLE);
				}else {
					tvPrompt.setText("");
					tvPrompt.setVisibility(View.GONE);
				}
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}

				dialog.dismiss();
				return false;
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tvSelect:
				selectDialog();
				break;
		}
	}
}
