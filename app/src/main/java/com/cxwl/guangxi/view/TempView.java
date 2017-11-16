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
 * 温度曲线
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class TempView extends View{
	
	private Context mContext = null;
	private List<FactDto> tempList = new ArrayList<FactDto>();
	private float maxValue = 0;
	private float minValue = 0;
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private Bitmap highBitmap = null;
	private Bitmap lowBitmap = null;
	private Bitmap highPoint = null;
	private int maxIndex = 0;
	private int minIndex = 0;
	private int saveTime = 0;
	private int time = 0;//发布时间
	
	public TempView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public TempView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public TempView(Context context, AttributeSet attrs, int defStyleAttr) {
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
		
		highBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_marker_temp), 
				(int)(CommonUtil.dip2px(mContext, 25)), (int)(CommonUtil.dip2px(mContext, 25)));
		lowBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_marker_temp_bottom), 
				(int)(CommonUtil.dip2px(mContext, 25)), (int)(CommonUtil.dip2px(mContext, 25)));
		highPoint = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_point_temp), 
				(int)(CommonUtil.dip2px(mContext, 10)), (int)(CommonUtil.dip2px(mContext, 10)));
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
			
			String highValue = tempList.get(0).temp;
			for (int i = 0; i < tempList.size(); i++) {
				FactDto dto = tempList.get(i);
				if (!TextUtils.isEmpty(dto.temp) && !TextUtils.equals(dto.temp, CONST.noValue)) {
					highValue = dto.temp;
					break;
				}
			}
			String lowValue = tempList.get(0).temp;
			for (int i = 0; i < tempList.size(); i++) {
				FactDto dto = tempList.get(i);
				if (!TextUtils.isEmpty(dto.temp) && !TextUtils.equals(dto.temp, CONST.noValue)) {
					lowValue = dto.temp;
					break;
				}
			}
			
			if (!TextUtils.isEmpty(highValue) && !TextUtils.equals(highValue, CONST.noValue)
					&& !TextUtils.isEmpty(lowValue) && !TextUtils.equals(lowValue, CONST.noValue)) {
				maxValue = Float.valueOf(highValue);
				minValue = Float.valueOf(lowValue);
				for (int i = 0; i < tempList.size(); i++) {
					FactDto dto = tempList.get(i);
					if (!TextUtils.isEmpty(dto.temp) && !TextUtils.equals(dto.temp, CONST.noValue)) {
						if (maxValue <= Float.valueOf(dto.temp)) {
							maxValue = Float.valueOf(dto.temp);
							maxIndex = i;
						}
						
						if (minValue >= Float.valueOf(dto.temp)) {
							minValue = Float.valueOf(dto.temp);
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
		float chartH = h-CommonUtil.dip2px(mContext, 80);
		float leftMargin = CommonUtil.dip2px(mContext, 20);
		float rightMargin = CommonUtil.dip2px(mContext, 20);
		float topMargin = CommonUtil.dip2px(mContext, 30);
		float bottomMargin = CommonUtil.dip2px(mContext, 50);
		float chartMaxH = chartH * maxValue / (Math.abs(maxValue)+Math.abs(minValue));//同时存在正负值时，正值高度
		
		int size = tempList.size();
		float columnWidth = chartW/(size-1);
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			FactDto dto = tempList.get(i);
			dto.x = columnWidth*i+leftMargin;
			dto.y = 0;
			if (!TextUtils.isEmpty(dto.temp) && !TextUtils.equals(dto.temp, CONST.noValue)) {
				float value = Float.valueOf(dto.temp);
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
		
		//绘制曲线
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

			if (y2 != 0 && y3 != 0 && y4 != 0) {
				Path linePath = new Path();
				linePath.moveTo(x1, y1);
				linePath.cubicTo(x3, y3, x4, y4, x2, y2);
				lineP.setColor(0xffe54b00);
				lineP.setStyle(Style.STROKE);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 2));
				canvas.drawPath(linePath, lineP);
			}
		}
		
		FactDto highDto = tempList.get(maxIndex);
		if (!TextUtils.isEmpty(highDto.temp) && !TextUtils.equals(highDto.temp, CONST.noValue)) {
			canvas.drawBitmap(highPoint, highDto.x-highPoint.getWidth()/2, highDto.y-highPoint.getHeight()/2, lineP);
			
			//绘制曲线上每个点的数据值
			textP.setColor(Color.WHITE);
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			float tempWidth = textP.measureText(highDto.temp);
			canvas.drawBitmap(highBitmap, highDto.x-highBitmap.getWidth()/2, highDto.y-highBitmap.getHeight()-CommonUtil.dip2px(mContext, 2.5f), textP);
			canvas.drawText(highDto.temp, highDto.x-tempWidth/2, highDto.y-highBitmap.getHeight()/2, textP);
		}
		
		FactDto lowDto = tempList.get(minIndex);
		if (!TextUtils.isEmpty(lowDto.temp) && !TextUtils.equals(lowDto.temp, CONST.noValue)) {
			canvas.drawBitmap(highPoint, lowDto.x-highPoint.getWidth()/2, lowDto.y-highPoint.getHeight()/2, lineP);
			
			//绘制曲线上每个点的数据值
			textP.setColor(Color.WHITE);
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			float tempWidth = textP.measureText(lowDto.temp);
			canvas.drawBitmap(lowBitmap, lowDto.x-lowBitmap.getWidth()/2, lowDto.y+CommonUtil.dip2px(mContext, 2.5f), textP);
			canvas.drawText(lowDto.temp, lowDto.x-tempWidth/2, lowDto.y+lowBitmap.getHeight()*3/4, textP);
		}
		
		textP.setColor(0xffe54b00);
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
