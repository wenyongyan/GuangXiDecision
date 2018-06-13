package com.cxwl.guangxi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.cxwl.guangxi.R;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CommonUtil {

	/** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static float dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return dpValue * scale;
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static float px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return pxValue / scale;
    }

	/**
	 * 获取listview高度
	 * @param listView
	 */
	public static int getListViewHeightBasedOnChildren(ListView listView) {
		int height = 0;
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return height;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		return height;
	}
    
    /**
	 * 解决ScrollView与ListView共存的问题
	 * 
	 * @param listView
	 */
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0); 
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 0));
		((MarginLayoutParams) params).setMargins(0, 0, 0, 0);
		listView.setLayoutParams(params);
	}
	
	/**
	 * 解决ScrollView与GridView共存的问题
	 */
	public static void setGridViewHeightBasedOnChildren(GridView gridView) {
		ListAdapter listAdapter = gridView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		
		Class<GridView> tempGridView = GridView.class; // 获得gridview这个类的class
		int column = -1;
        try {
 
            Field field = tempGridView.getDeclaredField("mRequestedNumColumns"); // 获得申明的字段
            field.setAccessible(true); // 设置访问权限
            column = Integer.valueOf(field.get(gridView).toString()); // 获取字段的值
        } catch (Exception e1) {
        }

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i+=column) {
			View listItem = listAdapter.getView(i, null, gridView);
			listItem.measure(0, 0); 
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = gridView.getLayoutParams();
		params.height = totalHeight + (gridView.getVerticalSpacing() * (listAdapter.getCount()/column - 1));
		((MarginLayoutParams) params).setMargins(0, 0, 0, 0);
		gridView.setLayoutParams(params);
	}
	
	/**
	 * 判断白天或晚上对应的天气现象
	 * @param fa 白天天气现象编号
	 * @param fb 晚上天气现象编号
	 * @return
	 */
	public static int getPheCode(int fa, int fb) {
		int pheCode = 0;
		SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
		
		try {
			long currentTime = new Date().getTime();//当前时间
			long eight = sdf1.parse("08:00").getTime();//早上08:00
			long twenty = sdf1.parse("20:00").getTime();//晚上20:00
			
			if (currentTime >= eight && currentTime <= twenty) {
				pheCode = fa;
			}else {
				pheCode = fb;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return pheCode;
	}
	
	/**
	 * 从Assets中读取图片
	 */
	public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
		Bitmap image = null;
		AssetManager am = context.getResources().getAssets();
		try {
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}

	/**
	 * 获取圆角图片
	 * @param bitmap
	 * @param corner
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int corner) {
		try {
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(Color.BLACK);
			canvas.drawRoundRect(rectF, corner, corner, paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

			final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			canvas.drawBitmap(bitmap, src, rect, paint);
			bitmap.recycle();
			return output;
		} catch (Exception e) {
			return bitmap;
		}
	}

	/**
	 * 隐藏虚拟键盘
	 * @param editText 输入框
	 * @param context 上下文
	 */
	public static void hideInputSoft(EditText editText, Context context) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	/**
	 * 转换图片成圆形
	 *
	 * @param bitmap
	 *            传入Bitmap对象
	 * @return
	 */
	public static Bitmap toRoundBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}
		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}

	/**
     * 获取网落图片资源
     * @param url
     * @return
     */
	public static Bitmap getHttpBitmap(String url) {
		URL myFileURL;
		Bitmap bitmap = null;
		try {
			myFileURL = new URL(url);
			// 获得连接
			HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
			// 设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
			conn.setConnectTimeout(6000);
			// 连接设置获得数据流
			conn.setDoInput(true);
			// 不使用缓存
			conn.setUseCaches(false);
			// 这句可有可无，没有影响
			conn.connect();
			// 得到数据流
			InputStream is = conn.getInputStream();
			// 解析得到图片
			bitmap = BitmapFactory.decodeStream(is);
			// 关闭数据流
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * 转换图片成六边形
	 * @return
	 */
	public static Bitmap getHexagonShape(Bitmap bitmap) {
		int targetWidth = bitmap.getWidth();
		int targetHeight = bitmap.getHeight();
		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);

		float radius = targetHeight / 2;
		float triangleHeight = (float) (Math.sqrt(3) * radius / 2);
		float centerX = targetWidth / 2;
		float centerY = targetHeight / 2;

		Canvas canvas = new Canvas(targetBitmap);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG|Paint.ANTI_ALIAS_FLAG));
		Path path = new Path();
		path.moveTo(centerX, centerY + radius);
		path.lineTo(centerX - triangleHeight, centerY + radius / 2);
		path.lineTo(centerX - triangleHeight, centerY - radius / 2);
		path.lineTo(centerX, centerY - radius);
		path.lineTo(centerX + triangleHeight, centerY - radius / 2);
		path.lineTo(centerX + triangleHeight, centerY + radius / 2);
		path.moveTo(centerX, centerY + radius);
		canvas.clipPath(path);
		canvas.drawBitmap(bitmap, new Rect(0, 0, targetWidth, targetHeight), new Rect(0, 0, targetWidth, targetHeight), null);
		return targetBitmap;
	}

	/**
	 * 把本地的drawable转换成六边形图片
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	/**
	 * 根据颜色值判断颜色
	 * @param colorType
	 * @param val
	 * @return
	 */
	public static int colorForValue(String colorType, float val) {
		int color = 0;
		if (TextUtils.equals(colorType, "jiangshui")) {
			if (val >= 0 && val < 1) {
				return 0xff2EAD06;
			} else if (val >= 1 && val < 10) {
				return 0xff000000;
			} else if (val >= 10 && val < 25) {
				return 0xff0901EC;
			} else if (val >= 25 && val < 50) {
				return 0xffC804C8;
			} else if (val >= 50) {
				return 0xffC50724;
			}
		} else if (TextUtils.equals(colorType, "wendu")) {
			if (val < -30) {
				return 0xff201885;
			} else if (val >= -30 && val < -20) {
				return 0xff114AD9;
			} else if (val >= -20 && val < -10) {
				return 0xff4DB4F7;
			} else if (val >= -10 && val < 0) {
				return 0xffD1F8F3;
			} else if (val >= 0 && val < 10) {
				return 0xffF9F2BB;
			} else if (val >= 10 && val < 20) {
				return 0xffF9DE45;
			} else if (val >= 20 && val < 30) {
				return 0xffFFA800;
			} else if (val >= 30 && val < 40) {
				return 0xffFF6D00;
			} else if (val >= 40 && val < 50) {
				return 0xffE60000;
			} else if (val >= 50) {
				return 0xff9E0001;
			}
		} else if (TextUtils.equals(colorType, "bianwen")) {
			if (val > 0) {
				return 0xffFF0000;
			} else if (val == 0) {
				return 0xff000000;
			} else {
				return 0xff0000FF;
			}
		} else if (TextUtils.equals(colorType, "radar")) {
			return 0xffff00ff;
		} else if (TextUtils.equals(colorType, "shidu")) {
			if (val >= 0 && val < 10) {
				return 0xffFF6000;
			} else if (val >= 10 && val < 30) {
				return 0xffFEA51A;
			} else if (val >= 30 && val < 50) {
				return 0xffFFFC9F;
			} else if (val >= 50) {
				return 0xffD6E6DA;
			}
		}

		return color;

	}
	
	/**
	 * 读取assets下文件
	 * @param fileName
	 * @return
	 */
	public static String getFromAssets(Context context, String fileName) {
		String Result = "";
		try {
			InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			while ((line = bufReader.readLine()) != null)
				Result += line;
			return Result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Result;
	}
	
	/**
	 * 绘制西安边界
	 * @param context
	 * @param aMap
	 * @param fileName json文件名称，例如guangxi.json
	 */
	public static void drawDistrict(Context context, AMap aMap, String fileName) {
		String result = CommonUtil.getFromAssets(context, fileName);
		if (!TextUtils.isEmpty(result)) {
			try {
				JSONObject obj = new JSONObject(result);
				JSONArray array = obj.getJSONArray("geometries");
				List<PolylineOptions> list = new ArrayList<>();
				list.clear();
				for (int i = 0; i < array.length(); i++) {
					JSONObject itemObj = array.getJSONObject(i);
					JSONArray coordinates = itemObj.getJSONArray("coordinates");
					JSONArray coordinates1 = coordinates.getJSONArray(0);
					PolylineOptions polylineOption = new PolylineOptions();
					polylineOption.width(8).color(0xff059cd4);	
					for (int j = 0; j < coordinates1.length(); j++) {
						JSONArray itemArray = coordinates1.getJSONArray(j);
						double lng = itemArray.getDouble(0);
						double lat = itemArray.getDouble(1);
						polylineOption.add(new LatLng(lat, lng));
					}
					list.add(polylineOption);
				}
				
				if (list.size() > 0) {
					for (int i = 0; i < list.size(); i++) {
						aMap.addPolyline(list.get(i));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     * @param context
     * @return true 表示开启
     */ 
    public static final boolean isLocationOpen(final Context context) { 
        LocationManager locationManager  = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快） 
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); 
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位） 
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER); 
        if (gps || network) { 
            return true; 
        } 
        return false; 
    }
    
    /**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public static String getVersion(Context context) {
	    try {
	        PackageManager manager = context.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
	        return info.versionName;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "";
	    }
	}
	
	/**
     * 根据角度获取风向
     * @return
     */
    public static String getWindDir(String degree) {
    	if (TextUtils.isEmpty(degree)) {
			return "";
		}
    	float d = Float.valueOf(degree);
		if (d == 0 || d == 360) {
			return "北";
		}else if (d > 0 && d < 90)  {
			return "东北";
		} else if (d == 90)  {
			return "东";
		} else if (d > 90 && d < 180)  {
			return "东南";
		} else if (d == 180)  {
			return "南";
		} else if (d > 180 && d < 270)  {
			return "西南";
		} else if (d == 270)  {
			return "西";
		} else if (d > 270 && d < 360)  {
			return "西北";
		}
		return "";
	}

	/**
	 * 根据风速获取风向标
	 * @param context
	 * @param speed
	 * @return
	 */
	public static Bitmap getWindMarker(Context context, double speed) {
		Bitmap bitmap = null;
		if (speed <= 0.2) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind12);
		}else if (speed > 0.2 && speed <= 1.5) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind12);
		}else if (speed > 1.5 && speed <= 3.3) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind12);
		}else if (speed > 3.3 && speed <= 5.4) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind34);
		}else if (speed > 5.4 && speed <= 7.9) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind34);
		}else if (speed > 7.9 && speed <= 10.7) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind56);
		}else if (speed > 10.7 && speed <= 13.8) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind56);
		}else if (speed > 13.8 && speed <= 17.1) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind78);
		}else if (speed > 17.1 && speed <= 20.7) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind78);
		}else if (speed > 20.7 && speed <= 24.4) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind8s);
		}else if (speed > 24.4 && speed <= 28.4) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind8s);
		}else if (speed > 28.4 && speed <= 32.6) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind8s);
		}else if (speed > 32.6 && speed < 99999.0) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind8s);
		}
		return bitmap;
	}

	/**
	 * 获取http://decision.tianqi.cn域名的请求头
	 * @return
	 */
	public static String getRequestHeader(String url) {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd00");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd06");
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd12");
		SimpleDateFormat sdf4 = new SimpleDateFormat("yyyyMMdd18");
		SimpleDateFormat sdf5 = new SimpleDateFormat("yyyyMMddHH");
		long time1 = 0, time2 = 0, time3 = 0, time4 = 0;
		long currentTime = 0;
		try {
			time1 = sdf5.parse(sdf1.format(new Date())).getTime();
			time2 = sdf5.parse(sdf2.format(new Date())).getTime();
			time3 = sdf5.parse(sdf3.format(new Date())).getTime();
			time4 = sdf5.parse(sdf4.format(new Date())).getTime();
			currentTime = new Date().getTime();
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		String date = null;
		if (currentTime >= time1 && currentTime < time2) {
			date = sdf1.format(new Date());
		}else if (currentTime >= time2 && currentTime < time3) {
			date = sdf2.format(new Date());
		}else if (currentTime >= time3 && currentTime < time4) {
			date = sdf3.format(new Date());
		}else if (currentTime >= time4) {
			date = sdf4.format(new Date());
		}
		String baseUrl = "http://decision.tianqi.cn/";
		String publicKey = "http://decision.tianqi.cn/?date="+date;//公钥
		if (url.contains("http://decision.tianqi.cn/data/page/rank_tmp.html") ||
				url.contains("http://decision.tianqi.cn/data//page/js_sk.html")||
				url.contains("http://decision.tianqi.cn/data/page/imgs.html?http://decision.tianqi.cn/data/product/JC_YT_DL_WXZXCSYT.html") ||
				url.contains("http://decision.tianqi.cn/data/page/radar.html")||
				url.contains("http://decision.tianqi.cn/data/page/qdltqsk.html") ||
				url.contains("http://decision.tianqi.cn/data/page/yb_tmp.html")||
				url.contains("http://decision.tianqi.cn/data/page/js_yb.html") ||
				url.contains("http://decision.tianqi.cn/data/page/nyqxqb_zb/63066459251b792cce98e7d3adbe1695.html")||
				url.contains("http://decision.tianqi.cn/data/page/qgghjcyb/694dc7732eea21e8a054b494f2867b7c.html") ||
				url.contains("http://decision.tianqi.cn/data/page/trsf.html")||
				url.contains("http://decision.tianqi.cn/data/page/nytqyb/df6ff9dfb1a70cd911b8056b033c9c86.html") ||
				url.contains("http://decision.tianqi.cn/data/environment/") ||
				url.contains("http://decision.tianqi.cn/data/page/slhxqxyb/8c9f71bfc6b62b263be00f95a61b3aae.html")||
				url.contains("http://decision.tianqi.cn/data/page/cyhxqxyb/64575e19002f7ed565563d3aded2d509.html") ||
				url.contains("http://decision.tianqi.cn/data/page/qgglqxyb/e37f58a882d94babc601805ec02c7b91.html")||
				url.contains("http://decision.tianqi.cn/jujiao/") ||
				url.contains("http://decision.tianqi.cn/data/page/alarm.html")||
				url.contains("http://radar.tianqi.cn/rain/") ||
				url.contains("http://www.welife100.com/Wap/Fengc/index")||
				url.contains("http://radar.tianqi.cn/typhoon/typhoon.html")) {
			baseUrl = "http://decision.tianqi.cn/";
			publicKey = "http://decision.tianqi.cn/?date="+date;
		}else if (url.contains("https://decision.tianqi.cn/data/page/rank_tmp.html") ||
				url.contains("https://decision.tianqi.cn/data//page/js_sk.html")||
				url.contains("https://decision.tianqi.cn/data/page/imgs.html?http://decision.tianqi.cn/data/product/JC_YT_DL_WXZXCSYT.html") ||
				url.contains("https://decision.tianqi.cn/data/page/radar.html")||
				url.contains("https://decision.tianqi.cn/data/page/qdltqsk.html") ||
				url.contains("https://decision.tianqi.cn/data/page/yb_tmp.html")||
				url.contains("https://decision.tianqi.cn/data/page/js_yb.html") ||
				url.contains("https://decision.tianqi.cn/data/page/nyqxqb_zb/63066459251b792cce98e7d3adbe1695.html")||
				url.contains("https://decision.tianqi.cn/data/page/qgghjcyb/694dc7732eea21e8a054b494f2867b7c.html") ||
				url.contains("https://decision.tianqi.cn/data/page/trsf.html")||
				url.contains("https://decision.tianqi.cn/data/page/nytqyb/df6ff9dfb1a70cd911b8056b033c9c86.html") ||
				url.contains("https://decision.tianqi.cn/data/environment/") ||
				url.contains("https://decision.tianqi.cn/data/page/slhxqxyb/8c9f71bfc6b62b263be00f95a61b3aae.html")||
				url.contains("https://decision.tianqi.cn/data/page/cyhxqxyb/64575e19002f7ed565563d3aded2d509.html") ||
				url.contains("https://decision.tianqi.cn/data/page/qgglqxyb/e37f58a882d94babc601805ec02c7b91.html")||
				url.contains("https://decision.tianqi.cn/jujiao/") ||
				url.contains("https://decision.tianqi.cn/data/page/alarm.html")||
				url.contains("https://radar.tianqi.cn/rain/") ||
				url.contains("https://www.welife100.com/Wap/Fengc/index")||
				url.contains("https://radar.tianqi.cn/typhoon/typhoon.html")) {
			baseUrl = "https://decision.tianqi.cn/";
			publicKey = "https://decision.tianqi.cn/?date="+date;
		}
		String privateKye = "url_private_key_789";//私钥
		String result = "";
		try{
			byte[] rawHmac = null;
			byte[] keyBytes = privateKye.getBytes("UTF-8");
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);
			rawHmac = mac.doFinal(publicKey.getBytes("UTF-8"));
			result = Base64.encodeToString(rawHmac, Base64.DEFAULT);
//			result = URLEncoder.encode(result, "UTF-8");
			result = baseUrl+result;
		}catch(Exception e){
			Log.e("SceneException", e.getMessage(), e);
		}
		return result;
	}
	
}
