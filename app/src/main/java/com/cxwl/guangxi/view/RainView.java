package com.cxwl.guangxi.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.cxwl.guangxi.common.CONST;
import com.cxwl.guangxi.dto.FactDto;
import com.cxwl.guangxi.utils.CommonUtil;

/**
 * 降水曲线
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class RainView extends View{
	
	private Context mContext = null;
	private List<FactDto> tempList = new ArrayList<FactDto>();
	private float maxValue = 0;
	private float minValue = 0;
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private Bitmap highBitmap = null;
	private int maxIndex = 0;
	private int minIndex = 0;
	private int saveTime = 0;
	private int time = 0;//发布时间
	
	public RainView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public RainView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public RainView(Context context, AttributeSet attrs, int defStyleAttr) {
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
		
		highBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_marker_pressure), 
				(int)(CommonUtil.dip2px(mContext, 25)), (int)(CommonUtil.dip2px(mContext, 25)));
	}
	
	/**
	 * 对cubicView进行赋值
	 */
	public void setData(List<FactDto> dataList, int time) {
		this.time = time;
		this.saveTime = time;
		if (!dataList.isEmpty()) {
			tempList.clear();
			tempList.addAll(dataList);
			if (tempList.isEmpty()) {
				return;
			}
			
			String highValue = tempList.get(0).rain;
			for (int i = 0; i < tempList.size(); i++) {
				FactDto dto = tempList.get(i);
				if (!TextUtils.isEmpty(dto.rain) && !TextUtils.equals(dto.rain, CONST.noValue)) {
					highValue = dto.rain;
					break;
				}
			}
			String lowValue = tempList.get(0).rain;
			for (int i = 0; i < tempList.size(); i++) {
				FactDto dto = tempList.get(i);
				if (!TextUtils.isEmpty(dto.rain) && !TextUtils.equals(dto.rain, CONST.noValue)) {
					lowValue = dto.rain;
					break;
				}
			}
			
			if (!TextUtils.isEmpty(highValue) && !TextUtils.equals(highValue, CONST.noValue)
					&& !TextUtils.isEmpty(lowValue) && !TextUtils.equals(lowValue, CONST.noValue)) {
				maxValue = Float.valueOf(highValue);
				minValue = Float.valueOf(lowValue);
				for (int i = 0; i < tempList.size(); i++) {
					FactDto dto = tempList.get(i);
					if (!TextUtils.isEmpty(dto.rain) && !TextUtils.equals(dto.rain, CONST.noValue)) {
						if (maxValue <= Float.valueOf(dto.rain)) {
							maxValue = Float.valueOf(dto.rain);
							maxIndex = i;
						}
						
						if (minValue >= Float.valueOf(dto.rain)) {
							minValue = Float.valueOf(dto.rain);
							minIndex = i;
						}
					}
				}
			}
			
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (tempList.isEmpty()) {
			return;
		}
		
		canvas.drawColor(Color.TRANSPARENT);
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		float chartW = w-CommonUtil.dip2px(mContext, 40);
		float chartH = h-CommonUtil.dip2px(mContext, 60);
		float leftMargin = CommonUtil.dip2px(mContext, 20);
		float rightMargin = CommonUtil.dip2px(mContext, 20);
		float topMargin = CommonUtil.dip2px(mContext, 30);
		float bottomMargin = CommonUtil.dip2px(mContext, 30);
		float chartMaxH = chartH * maxValue / (Math.abs(maxValue)+Math.abs(minValue));//同时存在正负值时，正值高度
		
		int size = tempList.size();
		float columnWidth = chartW/(size-1);
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			FactDto dto = tempList.get(i);
			dto.x = columnWidth*i+leftMargin;
			dto.y = 0;
			if (!TextUtils.isEmpty(dto.rain) && !TextUtils.equals(dto.rain, CONST.noValue)) {
				float value = Float.valueOf(dto.rain);
				if (value >= 0) {
					dto.y = chartMaxH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
					if (minValue >= 0) {
						dto.y = chartH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
					}
				}else {
					dto.y = chartMaxH + chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
					if (maxValue < 0) {
						dto.y = chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
					}
				}
				tempList.set(i, dto);
			}
		}
		
		for (int i = 0; i < size; i++) {
			FactDto dto = tempList.get(i);
			//绘制区域
			Path rectPath = new Path();
			rectPath.moveTo(dto.x-columnWidth/3, dto.y);
			rectPath.lineTo(dto.x+columnWidth/3, dto.y);
			rectPath.lineTo(dto.x+columnWidth/3, h-bottomMargin);
			rectPath.lineTo(dto.x-columnWidth/3, h-bottomMargin);
			rectPath.close();
			lineP.setColor(0xff3f84bb);
			lineP.setStyle(Style.FILL_AND_STROKE);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
			if (!TextUtils.isEmpty(dto.rain) && Float.valueOf(dto.rain) > 0) {
				canvas.drawPath(rectPath, lineP);
			}
		}
		
		FactDto highDto = tempList.get(maxIndex);
		if (!TextUtils.isEmpty(highDto.rain) && !TextUtils.equals(highDto.rain, CONST.noValue)) {
			//绘制曲线上每个点的数据值
			textP.setColor(Color.WHITE);
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			float tempWidth = textP.measureText(highDto.rain);
			canvas.drawBitmap(highBitmap, highDto.x-highBitmap.getWidth()/2, highDto.y-highBitmap.getHeight()-CommonUtil.dip2px(mContext, 2.5f), textP);
			canvas.drawText(highDto.rain, highDto.x-tempWidth/2, highDto.y-highBitmap.getHeight()/2, textP);
		}
		
		textP.setColor(0xff3f84bb);
		for (int i = size-1; i >= 0; i--) {
			FactDto dto = tempList.get(i);
			
			if (time < 0) {
				time = 23;
			}
			
			float text = textP.measureText(time+"");
			canvas.drawText(time+"", dto.x-text/2, h-CommonUtil.dip2px(mContext, 10f), textP);
			time--;
		}
		
		time = saveTime;
		
		lineP.reset();
		textP.reset();
	}

}
