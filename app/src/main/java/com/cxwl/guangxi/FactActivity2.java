package com.cxwl.guangxi;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.cxwl.guangxi.adapter.FactAdapter2;
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.common.ColumnData;
import com.cxwl.guangxi.dto.FactDto;
import com.cxwl.guangxi.utils.CommonUtil;
import com.cxwl.guangxi.utils.OkHttpUtil;

import net.tsz.afinal.FinalBitmap;

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
 * 实况资料
 */

public class FactActivity2 extends BaseActivity implements View.OnClickListener, AMap.OnCameraChangeListener {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle = null;
    private LinearLayout llContainer = null;
    private LinearLayout llContainer1 = null;
    private int width = 0;
    private float density = 0;
    private MapView mapView = null;//高德地图
    private AMap aMap = null;//高德地图
    private float zoom = 6.5f;
    private ScrollView scrollView = null;
    private ListView listView = null;
    private FactAdapter2 factAdapter = null;
    private List<FactDto> factList = new ArrayList<>();
    private LinearLayout listTitle = null;
    private TextView tv1, tv2, tv3;
    private TextView tvLayerName, tvIntro, tvToast;//图层名称
    private ImageView ivChart = null;//图例
    private ProgressBar progressBar = null;
    private List<Polygon> polygons = new ArrayList<>();//图层数据
    private List<Text> texts = new ArrayList<>();//等值线数值
    private List<Polyline> polylines = new ArrayList<>();//广西边界市县边界线

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fact2);
        mContext = this;
        initWidget();
        initAmap(savedInstanceState);
        initListView();
    }

    /**
     * 初始化高德地图
     */
    private void initAmap(Bundle bundle) {
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setVisibility(View.VISIBLE);
        mapView.onCreate(bundle);
        if (aMap == null) {
            aMap = mapView.getMap();
        }

        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CONST.centerLatLng, zoom));
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.showMapText(false);
        aMap.setOnCameraChangeListener(this);

        aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent arg0) {
                if (scrollView != null) {
                    if (arg0.getAction() == MotionEvent.ACTION_UP) {
                        scrollView.requestDisallowInterceptTouchEvent(false);
                    }else {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                    }
                }
            }
        });

        LatLngBounds bounds = new LatLngBounds.Builder()
//		.include(new LatLng(57.9079, 71.9282))
//		.include(new LatLng(3.9079, 134.8656))
                .include(new LatLng(1, 66))
                .include(new LatLng(60, 153))
                .build();
        aMap.addGroundOverlay(new GroundOverlayOptions()
                .anchor(0.5f, 0.5f)
                .positionFromBounds(bounds)
                .image(BitmapDescriptorFactory.fromResource(R.drawable.empty))
                .transparency(0.0f));
        aMap.runOnDrawFrame();
    }

    @Override
    public void onCameraChange(CameraPosition arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onCameraChangeFinish(CameraPosition arg0) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Point leftPoint = new Point(0, dm.heightPixels);
        Point rightPoint = new Point(dm.widthPixels, 0);
        LatLng leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
        LatLng rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);

        if (leftlatlng.latitude <= 3.9079 || rightLatlng.latitude >= 57.9079 || leftlatlng.longitude <= 71.9282
                || rightLatlng.longitude >= 134.8656 || arg0.zoom < 6.0f) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CONST.centerLatLng, 6.5f));
        }
    }

    private void initListView() {
        listView = (ListView) findViewById(R.id.listView);
        factAdapter = new FactAdapter2(mContext, factList);
        listView.setAdapter(factAdapter);
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        llContainer = (LinearLayout) findViewById(R.id.llContainer);
        llContainer1 = (LinearLayout) findViewById(R.id.llContainer1);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        listTitle = (LinearLayout) findViewById(R.id.listTitle);
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv3 = (TextView) findViewById(R.id.tv3);
        tvLayerName = (TextView) findViewById(R.id.tvLayerName);
        tvIntro = (TextView) findViewById(R.id.tvIntro);
        tvToast = (TextView) findViewById(R.id.tvToast);
        ivChart = (ImageView) findViewById(R.id.ivChart);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        density = dm.density;

        ColumnData data = getIntent().getExtras().getParcelable("data");
        if (!TextUtils.isEmpty(data.name)) {
            tvTitle.setText(data.name);
        }

        addColumn(data);
    }

    /**
     * 添加子栏目
     * @param data
     */
    private void addColumn(ColumnData data) {
        llContainer.removeAllViews();
        llContainer1.removeAllViews();
        int size = data.child.size();
        if (size <= 0) {
            return;
        }
        for (int i = 0; i < size; i++) {
            ColumnData itemDto = data.child.get(i);
            TextView tvName = new TextView(mContext);
            tvName.setGravity(Gravity.CENTER);
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            tvName.setPadding(0, (int)(density*10), 0, (int)(density*10));
            tvName.setMaxLines(1);

            TextView tvBar = new TextView(mContext);
            tvBar.setGravity(Gravity.CENTER);
            tvBar.setPadding((int)(density*10), 0, (int)(density*10), 0);

            if (!TextUtils.isEmpty(itemDto.name)) {
                tvName.setText(itemDto.name);
                tvName.setTag(itemDto.dataUrl+","+itemDto.id);
            }
            if (i == 0) {
                tvName.setTextColor(getResources().getColor(R.color.title_bg));
                tvBar.setBackgroundColor(getResources().getColor(R.color.title_bg));
                OkHttpFact(itemDto.dataUrl);
            }else {
                tvName.setTextColor(getResources().getColor(R.color.text_color3));
                tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
            }
            llContainer.addView(tvName);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tvName.getLayoutParams();
            if (size <= 4) {
                if (size == 2) {
                    params.width = width/2;
                }else if (size == 3) {
                    params.width = width/3;
                }else {
                    params.width = width/4;
                }
            }else {
                params.setMargins((int)(density*10), 0, (int)(density*10), 0);
            }
            tvName.setLayoutParams(params);

            int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            tvName.measure(w, h);
            llContainer1.addView(tvBar);
            LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) tvBar.getLayoutParams();
            if (size <= 4) {
                if (size == 2) {
                    params1.width = width/2;
                }else if (size == 3) {
                    params1.width = width/3;
                }else {
                    params1.width = width/4;
                }
            }else {
                params1.setMargins((int)(density*10), 0, (int)(density*10), 0);
                params1.width = tvName.getMeasuredWidth();
            }
            params1.height = (int) (density*2);
            params1.gravity = Gravity.CENTER;
            tvBar.setLayoutParams(params1);

            tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (llContainer != null) {
                        for (int i = 0; i < llContainer.getChildCount(); i++) {
                            TextView tvName = (TextView) llContainer.getChildAt(i);
                            TextView tvBar = (TextView) llContainer1.getChildAt(i);
                            if (TextUtils.equals((String) arg0.getTag(), (String) tvName.getTag())) {
                                tvName.setTextColor(getResources().getColor(R.color.title_bg));
                                tvBar.setBackgroundColor(getResources().getColor(R.color.title_bg));
                                String[] tags = ((String) arg0.getTag()).split(",");
                                OkHttpFact(tags[0]);
                            }else {
                                tvName.setTextColor(getResources().getColor(R.color.text_color4));
                                tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }
                        }
                    }
                }
            });

        }
    }

    /**
     * 获取实况信息
     * @param url
     */
    private void OkHttpFact(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result != null) {
                            try {
                                JSONObject obj = new JSONObject(result);

                                if (!obj.isNull("zh")) {
                                    JSONObject itemObj = obj.getJSONObject("zh");
                                    if (!itemObj.isNull("stationName")) {
                                        tv3.setText(itemObj.getString("stationName"));
                                    }
                                    if (!itemObj.isNull("area")) {
                                        tv2.setText(itemObj.getString("area"));
                                    }
                                    if (!itemObj.isNull("val")) {
                                        tv1.setText(itemObj.getString("val"));
                                    }
                                }

                                if (!obj.isNull("title")) {
                                    tvLayerName.setText(obj.getString("title"));
                                    tvLayerName.setVisibility(View.VISIBLE);
                                }

                                if (!obj.isNull("cutlineUrl")) {
                                    FinalBitmap finalBitmap = FinalBitmap.create(mContext);
                                    finalBitmap.display(ivChart, obj.getString("cutlineUrl"), null, 0);
                                }

                                if (!obj.isNull("zx")) {
                                    tvIntro.setText(obj.getString("zx"));
                                }

                                if (!obj.isNull("dataUrl")) {
                                    String dataUrl = obj.getString("dataUrl");
                                    if (!TextUtils.isEmpty(dataUrl)) {
                                        OkHttpJson(dataUrl);
                                    }
                                }

                                //详情开始
                                String stationName = "", area = "", val = "", timeString = "";
                                if (!obj.isNull("th")) {
                                    JSONObject itemObj = obj.getJSONObject("th");
                                    if (!itemObj.isNull("stationName")) {
                                        stationName = itemObj.getString("stationName");
                                    }
                                    if (!itemObj.isNull("area")) {
                                        area = itemObj.getString("area");
                                    }
                                    if (!itemObj.isNull("val")) {
                                        val = itemObj.getString("val");
                                    }
                                }
                                if (!obj.isNull("timeString")) {
                                    timeString = obj.getString("timeString");
                                }

                                List<FactDto> realDatas = new ArrayList<>();
                                realDatas.clear();
                                if (!obj.isNull("realDatas")) {
                                    JSONArray array = new JSONArray(obj.getString("realDatas"));
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject itemObj = array.getJSONObject(i);
                                        FactDto dto = new FactDto();
                                        if (!itemObj.isNull("stationCode")) {
                                            dto.stationCode = itemObj.getString("stationCode");
                                        }
                                        if (!itemObj.isNull("stationName")) {
                                            dto.stationName = itemObj.getString("stationName");
                                        }
                                        if (!itemObj.isNull("area")) {
                                            dto.area = itemObj.getString("area");
                                        }
                                        if (!itemObj.isNull("val")) {
                                            dto.val = itemObj.getDouble("val");
                                        }

                                        if (!TextUtils.isEmpty(dto.stationName) && !TextUtils.isEmpty(dto.area)) {
                                            realDatas.add(dto);
                                        }
                                    }
                                }
                                //详情结束

                                if (!obj.isNull("jb")) {
                                    factList.clear();
                                    JSONArray array = obj.getJSONArray("jb");
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject itemObj = array.getJSONObject(i);
                                        FactDto data = new FactDto();
                                        if (!itemObj.isNull("lv")) {
                                            data.rainLevel = itemObj.getString("lv");
                                        }
                                        if (!itemObj.isNull("count")) {
                                            data.count = itemObj.getInt("count")+"";
                                        }
                                        if (!itemObj.isNull("xs")) {
                                            JSONArray xsArray = itemObj.getJSONArray("xs");
                                            List<FactDto> list = new ArrayList<>();
                                            list.clear();
                                            for (int j = 0; j < xsArray.length(); j++) {
                                                FactDto d = new FactDto();
                                                d.area = xsArray.getString(j);
                                                list.add(d);
                                            }
                                            data.areaList.addAll(list);
                                        }
                                        factList.add(data);
                                    }
                                    if (factList.size() > 0 && factAdapter != null) {
                                        CommonUtil.setListViewHeightBasedOnChildren(listView);
                                        factAdapter.timeString = timeString;
                                        factAdapter.stationName = stationName;
                                        factAdapter.area = area;
                                        factAdapter.val = val;
                                        factAdapter.realDatas.clear();
                                        factAdapter.realDatas.addAll(realDatas);
                                        factAdapter.notifyDataSetChanged();
                                        tvIntro.setVisibility(View.VISIBLE);
                                        listTitle.setVisibility(View.VISIBLE);
                                        listView.setVisibility(View.VISIBLE);
                                    }
                                }else {
                                    tvIntro.setVisibility(View.GONE);
                                    listTitle.setVisibility(View.GONE);
                                    listView.setVisibility(View.GONE);
                                }

                                tvLayerName.setFocusable(true);
                                tvLayerName.setFocusableInTouchMode(true);
                                tvLayerName.requestFocus();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else {
                            removePolygons();
                            progressBar.setVisibility(View.GONE);
                            tvToast.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    tvToast.setVisibility(View.GONE);
                                }
                            }, 1000);
                        }
                    }
                });
            }
        });
    }

    /**
     * 请求图层数据
     * @param url
     */
    private void OkHttpJson(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result != null) {
                            drawDataToMap(result);
                        }else {
                            removePolygons();
                            progressBar.setVisibility(View.GONE);
                            tvToast.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    tvToast.setVisibility(View.GONE);
                                }
                            }, 1000);
                        }
                    }
                });
            }
        });
    }

    private void removeTexts() {
        for (int i = 0; i < texts.size(); i++) {
            texts.get(i).remove();
        }
        texts.clear();
    }

    /**
     * 清除图层
     */
    private void removePolygons() {
        for (int i = 0; i < polygons.size(); i++) {
            polygons.get(i).remove();
        }
        polygons.clear();
    }

    /**
     * 清除边界线
     */
    private void removePolylines() {
        for (int i = 0; i < polylines.size(); i++) {
            polylines.get(i).remove();
        }
        polylines.clear();
    }

    /**
     * 绘制图层
     */
    private void drawDataToMap(String result) {
        if (TextUtils.isEmpty(result) || aMap == null) {
            return;
        }
        removeTexts();
        removePolygons();
        removePolylines();

        try {
            JSONObject obj = new JSONObject(result);
            JSONArray array = obj.getJSONArray("l");
            int length = array.length();
//			if (length > 200) {
//				length = 200;
//			}
            for (int i = 0; i < length; i++) {
                JSONObject itemObj = array.getJSONObject(i);
                JSONArray c = itemObj.getJSONArray("c");
                int r = c.getInt(0);
                int g = c.getInt(1);
                int b = c.getInt(2);
                int a = (int) (c.getInt(3)*255*1.0);

                double centerLat = 0;
                double centerLng = 0;
                String p = itemObj.getString("p");
                if (!TextUtils.isEmpty(p)) {
                    String[] points = p.split(";");
                    PolygonOptions polygonOption = new PolygonOptions();
                    polygonOption.fillColor(Color.argb(a, r, g, b));
                    polygonOption.strokeColor(0xffd9d9d9);
                    polygonOption.strokeWidth(1);
                    for (int j = 0; j < points.length; j++) {
                        String[] value = points[j].split(",");
                        double lat = Double.valueOf(value[1]);
                        double lng = Double.valueOf(value[0]);
                        polygonOption.add(new LatLng(lat, lng));
                        if (j == points.length/2) {
                            centerLat = lat;
                            centerLng = lng;
                        }
                    }
                    Polygon polygon = aMap.addPolygon(polygonOption);
                    polygons.add(polygon);
                }

                if (!itemObj.isNull("v")) {
                    int v = itemObj.getInt("v");
                    TextOptions options = new TextOptions();
                    options.position(new LatLng(centerLat, centerLng));
                    options.fontColor(Color.BLACK);
                    options.fontSize(30);
                    options.text(v+"");
                    options.backgroundColor(Color.TRANSPARENT);
                    Text text = aMap.addText(options);
                    texts.add(text);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        drawAllDistrict();
        progressBar.setVisibility(View.GONE);
    }

    /**
     * 绘制广西市县边界
     */
    private void drawAllDistrict() {
        if (aMap == null) {
            return;
        }
        String result = CommonUtil.getFromAssets(mContext, "guangxi.json");
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject obj = new JSONObject(result);
                JSONArray array = obj.getJSONArray("features");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject itemObj = array.getJSONObject(i);

//					JSONObject properties = itemObj.getJSONObject("properties");
//					String name = properties.getString("name");
//						JSONArray cp = properties.getJSONArray("cp");
//						for (int m = 0; m < cp.length(); m++) {
//							double lat = cp.getDouble(1);
//							double lng = cp.getDouble(0);
//
//							LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//							View view = inflater.inflate(R.layout.rainfall_fact_marker_view2, null);
//							TextView tvName = (TextView) view.findViewById(R.id.tvName);
//							if (!TextUtils.isEmpty(name)) {
//								tvName.setText(name);
//							}
//							MarkerOptions options = new MarkerOptions();
//							options.anchor(0.5f, 0.5f);
//							options.position(new LatLng(lat, lng));
//							options.icon(BitmapDescriptorFactory.fromView(view));
//							aMap.addMarker(options);
//						}

                    JSONObject geometry = itemObj.getJSONObject("geometry");
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    for (int m = 0; m < coordinates.length(); m++) {
                        JSONArray array2 = coordinates.getJSONArray(m);
                        PolylineOptions polylineOption = new PolylineOptions();
                        polylineOption.width(1).color(0xffd9d9d9);
                        for (int j = 0; j < array2.length(); j++) {
                            JSONArray itemArray = array2.getJSONArray(j);
                            double lng = itemArray.getDouble(0);
                            double lat = itemArray.getDouble(1);
                            polylineOption.add(new LatLng(lat, lng));
                        }
                        Polyline polyLine = aMap.addPolyline(polylineOption);
                        polylines.add(polyLine);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
        }
    }
}
