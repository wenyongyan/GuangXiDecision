package com.cxwl.guangxi.view;

/**
 * 绘制平滑曲线
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.cxwl.guangxi.R;
import com.cxwl.guangxi.dto.WeatherDto;
import com.cxwl.guangxi.utils.CommonUtil;
import com.cxwl.guangxi.utils.WeatherUtil;

@SuppressLint({ "SimpleDateFormat", "DrawAllocation" })
public class CubicView extends View{
	
	private Context mContext = null;
	private SimpleDateFormat sdf0 = new SimpleDateFormat("yyyyMMddHHmm");
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH时");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH");
	private List<WeatherDto> tempList = new ArrayList<WeatherDto>();
	private int maxTemp = 0;//最高温度
	private int minTemp = 0;//最低温度
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	
	public CubicView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public CubicView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public CubicView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}
	
	private void init() {
		lineP = new Paint();
		lineP.setStyle(Paint.Style.STROKE);
		lineP.setStrokeCap(Paint.Cap.ROUND);
		lineP.setAntiAlias(true);
		
		textP = new Paint();
		textP.setAntiAlias(true);
	}
	
	/**
	 * 对cubicView进行赋值
	 */
	public void setData(List<WeatherDto> dataList) {
		if (!dataList.isEmpty()) {
			tempList.clear();
			tempList.addAll(dataList);
			
			maxTemp = tempList.get(0).hourlyTemp;
			minTemp = tempList.get(0).hourlyTemp;
			for (int i = 0; i < tempList.size(); i++) {
				if (maxTemp <= tempList.get(i).hourlyTemp) {
					maxTemp = tempList.get(i).hourlyTemp;
				}
				if (minTemp >= tempList.get(i).hourlyTemp) {
					minTemp = tempList.get(i).hourlyTemp;
				}
			}
			
			int totalDivider = 0;
			if (maxTemp > 0 && minTemp > 0) {
				totalDivider = (int) (maxTemp-minTemp);
			}else if (maxTemp >= 0 && minTemp <= 0) {
				totalDivider = (int) (maxTemp-minTemp);
			}else if (maxTemp < 0 && minTemp < 0) {
				totalDivider = (int) (minTemp-maxTemp);
			}
			int itemDivider = 1;
			if (totalDivider <= 5) {
				itemDivider = 1;
			}else if (totalDivider > 5 && totalDivider <= 10) {
				itemDivider = 2;
			}else if (totalDivider > 10 && totalDivider <= 20) {
				itemDivider = 4;
			}else if (totalDivider > 20 && totalDivider <= 40) {
				itemDivider = 8;
			}else if (totalDivider > 40) {
				itemDivider = 10;
			}
			maxTemp = maxTemp+itemDivider;
			minTemp = minTemp-itemDivider;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (tempList.isEmpty()) {
			return;
		}
		
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		canvas.drawColor(Color.TRANSPARENT);
		float chartW = w-CommonUtil.dip2px(mContext, 60);
		float chartH = h-CommonUtil.dip2px(mContext, 80);
		float leftMargin = CommonUtil.dip2px(mContext, 40);
		float rightMargin = CommonUtil.dip2px(mContext, 20);
		float topMargin = CommonUtil.dip2px(mContext, 0);
		float bottomMargin = CommonUtil.dip2px(mContext, 40);
		
		int size = tempList.size();
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			WeatherDto dto = tempList.get(i);
			dto.x = (chartW/(size-1))*i + leftMargin;
			
			float temp = tempList.get(i).hourlyTemp;
			if (maxTemp > 0 && minTemp > 0) {
				dto.y = chartH - chartH*(temp-minTemp)/(maxTemp-minTemp) - topMargin;
			}else if (maxTemp >= 0 && minTemp <= 0) {
				dto.y = chartH - chartH*temp/(maxTemp-minTemp) - topMargin;
			}else if (maxTemp < 0 && minTemp < 0) {
				dto.y = chartH*(temp-maxTemp)/(minTemp-maxTemp) - topMargin;
			}
			tempList.set(i, dto);
		}
		
		//绘制刻度线
		int totalDivider = 0;
		if (maxTemp > 0 && minTemp > 0) {
			totalDivider = (int) (maxTemp-minTemp);
		}else if (maxTemp >= 0 && minTemp <= 0) {
			totalDivider = (int) (maxTemp-minTemp);
		}else if (maxTemp < 0 && minTemp < 0) {
			totalDivider = (int) (minTemp-maxTemp);
		}
		int itemDivider = 1;
		if (totalDivider <= 5) {
			itemDivider = 1;
		}else if (totalDivider > 5 && totalDivider <= 10) {
			itemDivider = 2;
		}else if (totalDivider > 10 && totalDivider <= 20) {
			itemDivider = 4;
		}else if (totalDivider > 20 && totalDivider <= 40) {
			itemDivider = 8;
		}else if (totalDivider > 40) {
			itemDivider = 10;
		}
		for (int i = minTemp; i <= maxTemp; i+=itemDivider) {
			float dividerY = 0;
			int value = i;
			if (maxTemp > 0 && minTemp > 0) {
				dividerY = chartH - chartH*(value-minTemp)/(maxTemp-minTemp) - topMargin;
			}else if (maxTemp >= 0 && minTemp <= 0) {
				dividerY = chartH - chartH*value/(maxTemp-minTemp) - topMargin;
			}else if (maxTemp < 0 && minTemp < 0) {
				dividerY = chartH*(value-maxTemp)/(minTemp-maxTemp) - topMargin;
			}
			lineP.setColor(0x303072ff);
			canvas.drawLine(leftMargin, dividerY, w-rightMargin, dividerY, lineP);
			textP.setColor(getResources().getColor(R.color.title_bg));
			textP.setTextSize(CommonUtil.dip2px(mContext, 12));
			canvas.drawText(String.valueOf(i)+"℃", CommonUtil.dip2px(mContext, 5), dividerY, textP);
		}
		
		for (int i = 0; i < size-1; i++) {
			float x1 = tempList.get(i).x;
			float y1 = tempList.get(i).y;
			float x2 = tempList.get(i+1).x;
			float y2 = tempList.get(i+1).y;
			
			float wt = (x1 + x2) / 2;
			
			float x3 = wt;
			float y3 = y1;
			float x4 = wt;
			float y4 = y2;
			
			Path pathLow = new Path();
			pathLow.moveTo(x1, y1);
			pathLow.cubicTo(x3, y3, x4, y4, x2, y2);
			lineP.setColor(getResources().getColor(R.color.title_bg));
			lineP.setStrokeWidth(3.0f);
			canvas.drawPath(pathLow, lineP);
		}
		
		float halfX = (tempList.get(1).x - tempList.get(0).x)/2;
		for (int i = 0; i < tempList.size(); i++) {
			WeatherDto dto = tempList.get(i);
			
			//绘制曲线上每个时间点marker
			lineP.setColor(getResources().getColor(R.color.title_bg));
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 5));
			canvas.drawPoint(dto.x, dto.y, lineP);
			
			//绘制曲线上每个点的信息
			try {
				long zao8 = sdf2.parse("06").getTime();
				long wan8 = sdf2.parse("20").getTime();
				long current = sdf2.parse(sdf2.format(sdf0.parse(tempList.get(i).hourlyTime))).getTime();
				Bitmap lb = null;
				if (current >= zao8 && current < wan8) {
					lb = WeatherUtil.getBitmap(mContext, tempList.get(i).hourlyCode);
				}else {
					lb = WeatherUtil.getNightBitmap(mContext, tempList.get(i).hourlyCode);
				}
				Bitmap newLbit = ThumbnailUtils.extractThumbnail(lb, (int)(CommonUtil.dip2px(mContext, 18)), (int)(CommonUtil.dip2px(mContext, 18)));
				canvas.drawBitmap(newLbit, tempList.get(i).x-newLbit.getWidth()/2, tempList.get(i).y-(int)(CommonUtil.dip2px(mContext, 30)), textP);
				textP.setColor(getResources().getColor(R.color.title_bg));
				textP.setTextSize(CommonUtil.dip2px(mContext, 12));
				float tempWidth = textP.measureText(String.valueOf(tempList.get(i).hourlyTemp)+mContext.getString(R.string.unit_degree));
				canvas.drawText(String.valueOf(tempList.get(i).hourlyTemp)+mContext.getString(R.string.unit_degree), tempList.get(i).x-tempWidth/2, tempList.get(i).y+(int)(CommonUtil.dip2px(mContext, 20)), textP);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			//绘制风速风向
			lineP.setColor(0x303072ff);
			lineP.setStyle(Style.FILL);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
			float windHeight = h-CommonUtil.dip2px(mContext, 25);
			String windForce = WeatherUtil.getHourWindForce(dto.hourlyWindForceCode);
			if (TextUtils.equals(windForce, "微风")) {
				windHeight = h-CommonUtil.dip2px(mContext, 25);
			}else if (TextUtils.equals(windForce, "1级")) {
				windHeight = h-CommonUtil.dip2px(mContext, 30);
			}else if (TextUtils.equals(windForce, "2级")) {
				windHeight = h-CommonUtil.dip2px(mContext, 35);
			}else if (TextUtils.equals(windForce, "3级")) {
				windHeight = h-CommonUtil.dip2px(mContext, 40);
			}else if (TextUtils.equals(windForce, "4级")) {
				windHeight = h-CommonUtil.dip2px(mContext, 45);
			}else if (TextUtils.equals(windForce, "5级")) {
				windHeight = h-CommonUtil.dip2px(mContext, 50);
			}else if (TextUtils.equals(windForce, "6级")) {
				windHeight = h-CommonUtil.dip2px(mContext, 55);
			}else if (TextUtils.equals(windForce, "7级")) {
				windHeight = h-CommonUtil.dip2px(mContext, 60);
			}else if (TextUtils.equals(windForce, "8级")) {
				windHeight = h-CommonUtil.dip2px(mContext, 65);
			}else if (TextUtils.equals(windForce, "9级")) {
				windHeight = h-CommonUtil.dip2px(mContext, 70);
			}else if (TextUtils.equals(windForce, "10级")) {
				windHeight = h-CommonUtil.dip2px(mContext, 75);
			}else if (TextUtils.equals(windForce, "11级")) {
				windHeight = h-CommonUtil.dip2px(mContext, 80);
			}else if (TextUtils.equals(windForce, "12级")) {
				windHeight = h-CommonUtil.dip2px(mContext, 85);
			}
			
			lineP.setColor(0x303072ff);
			textP.setColor(getResources().getColor(R.color.title_bg));
			textP.setTextSize(CommonUtil.dip2px(mContext, 8));
			String windDir = mContext.getString(WeatherUtil.getWindDirection(dto.hourlyWindDirCode));
			float hWindDir = textP.measureText(windDir);
			canvas.drawText(windDir, dto.x-hWindDir/2, windHeight-CommonUtil.dip2px(mContext, 15f), textP);
			
			textP.setColor(getResources().getColor(R.color.title_bg));
			textP.setTextSize(CommonUtil.dip2px(mContext, 8));
			float hWindForce = textP.measureText(windForce);
			canvas.drawText(windForce, dto.x-hWindForce/2, windHeight-CommonUtil.dip2px(mContext, 3f), textP);
			canvas.drawRect(dto.x-halfX+CommonUtil.dip2px(mContext, 2), windHeight, dto.x+halfX-CommonUtil.dip2px(mContext, 2), h-CommonUtil.dip2px(mContext, 20), lineP);
			
			//绘制每个时间点上的时间值
			textP.setColor(getResources().getColor(R.color.title_bg));
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			try {
				String hourlyTime = sdf1.format(sdf0.parse(tempList.get(i).hourlyTime));
				canvas.drawText(hourlyTime, dto.x-CommonUtil.dip2px(mContext, 12.5f), h-CommonUtil.dip2px(mContext, 5), textP);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		
		//绘制时间点一行的直线
		lineP.setStyle(Style.STROKE);
		lineP.setColor(0x303072ff);
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
		canvas.drawLine(0, h-CommonUtil.dip2px(mContext, 20), w, h-CommonUtil.dip2px(mContext, 20), lineP);
	}
	
}
