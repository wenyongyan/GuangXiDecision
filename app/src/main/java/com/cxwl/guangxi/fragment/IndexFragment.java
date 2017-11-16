package com.cxwl.guangxi.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.adapter.IndexFragmentAdapter;
import com.cxwl.guangxi.dto.IndexDto;
import com.cxwl.guangxi.manager.DBManager;

/**
 * 生活指数
 * @author shawn_sun
 *
 */

public class IndexFragment extends Fragment {

	private GridView mGridView = null;
	private IndexFragmentAdapter mAdapter = null;
	private List<IndexDto> indexList = new ArrayList<IndexDto>();
	private List<IndexDto> mList = new ArrayList<IndexDto>();
	private String[] indexs = {"fs", "ac", "pp", "pl", "ct", "tr", "gm", "ls", "lk",
			"cl", "xq", "uv", "pj", "nl", "jt", "zs", "hc", "xc", "mf", "co", "gj",
			"dy", "yd", "ag", "ys", "pk", "yh" };//所有生活指数缩写
    private ProgressBar progressBar = null;
    private int index = 0;
    private int cursorSize = 0;
    private String cityId = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_index, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
		initGridView(view);
	}
	
	private void initWidget(View view) {
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		
		index = getArguments().getInt("index");
		cursorSize = getArguments().getInt("cursorSize");
		cityId = getArguments().getString("cityId");
		
		getWeatherInfo(cityId);
	}
	
	
	/**
	 * 获取天气数据
	 */
	private void getWeatherInfo(String cityId) {
		if (TextUtils.isEmpty(cityId)) {
			return;
		}
		WeatherAPI.getWeather2(getActivity(), cityId, Language.ZH_CN, new AsyncResponseHandler() {
			@Override
			public void onComplete(Weather content) {
				super.onComplete(content);
				if (content != null) {
					//生活指数
					JSONArray array = content.getIndexInfo(getActivity(), indexs, Language.ZH_CN);
					for (int i = 0; i < array.length(); i++) {
						IndexDto dto = new IndexDto();
						try {
							JSONObject itemObj = array.getJSONObject(i);
							if (!itemObj.isNull("i1")) {
								dto.abbr = itemObj.getString("i1");
							}
							JSONArray itemArray = itemObj.getJSONArray("i4");
							JSONObject itemArrayObj = itemArray.getJSONObject(0);
							if (!itemArrayObj.isNull("ia")) {
								dto.number = itemArrayObj.getString("ia");
							}
							
							initDBManager(dto.abbr, dto.number, dto);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					
					for (int i = index; i < cursorSize; i++) {
						mList.add(indexList.get(i));
					}
					
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
					progressBar.setVisibility(View.GONE);
				}
			}
			
			@Override
			public void onError(Throwable error, String content) {
				super.onError(error, content);
				progressBar.setVisibility(View.GONE);
			}
		});
	}
	
	/**
	 * 初始化数据库
	 */
	private void initDBManager(String abbr, String number, IndexDto dto) {
		DBManager dbManager = new DBManager(getActivity());
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		
		Cursor cursor = database.rawQuery("select wi.abbr,wi.number,wil.level_zh ,win.name ,wii.intro_zh "
				+"from weather_index as wi ,weather_index_level as wil ,weather_index_name as win ,weather_index_introduction as wii "
				+"where wi.abbr = "+"'"+abbr+"'" + " and wi.number = "+"'"+number+"'"
				+" and wil.level=wi.level and win.abbr=wi.abbr and wii.intro = wi.intro", null);
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			dto.name = cursor.getString(cursor.getColumnIndex("name"));
			dto.abbr = cursor.getString(cursor.getColumnIndex("abbr"));
			dto.level_zh = cursor.getString(cursor.getColumnIndex("level_zh"));
			dto.intro_zh = cursor.getString(cursor.getColumnIndex("intro_zh"));
			dto.number = cursor.getString(cursor.getColumnIndex("number"));
			dto.isExpand = false;
			
			if (!TextUtils.isEmpty(dto.name)) {
				indexList.add(dto);
			}
		}
	}

	/**
	 * 初始化listview
	 */
	private void initGridView(View view) {
		mGridView = (GridView) view.findViewById(R.id.gridView);
		mAdapter = new IndexFragmentAdapter(getActivity(), mList);
		mGridView.setAdapter(mAdapter);
	}
	
}
