package com.cxwl.guangxi.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class WeatherDto implements Parcelable{
	
	public String cityName = null;//城市名称
	public float minuteFall= 0;//逐分钟降水量
	public String aqi = null;//空气质量
	
	//平滑曲线
	public int hourlyTemp = 0;//逐小时温度
	public String hourlyTime = null;//逐小时时间
	public int hourlyCode = 0;//天气现象编号
	public int hourlyWindDirCode = 0;
	public int hourlyWindForceCode = 0;
	public String hourlyHumidity = null;
	public float x = 0;//x轴坐标点
	public float y = 0;//y轴坐标点
	
	//列表、趋势
	public String week = null;//周几
	public String date = null;//日期
	public String lowPhe = null;//晚上天气现象
	public int lowPheCode = 0;//晚上天气现象编号
	public int lowTemp = 0;//最低气温
	public int nightWindDir = 0;//晚上风向编号
	public int nightWindForce = 0;//晚上风力编号
	public float lowX = 0;//最低温度x轴坐标点
	public float lowY = 0;//最低温度y轴坐标点
	public String highPhe = null;//白天天气现象
	public int highPheCode = 0;//白天天气现象编号
	public int highTemp = 0;//最高气温
	public int dayWindDir = 0;//白天风向编号
	public int dayWindForce = 0;//白天风力编号
	public float highX = 0;//最高温度x轴坐标点
	public float highY = 0;//最高温度y轴坐标点
	public int windDir = 0;//风向编号
	public int windForce = 0;//风力编号

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.cityName);
		dest.writeFloat(this.minuteFall);
		dest.writeString(this.aqi);
		dest.writeInt(this.hourlyTemp);
		dest.writeString(this.hourlyTime);
		dest.writeInt(this.hourlyCode);
		dest.writeInt(this.hourlyWindDirCode);
		dest.writeInt(this.hourlyWindForceCode);
		dest.writeString(this.hourlyHumidity);
		dest.writeFloat(this.x);
		dest.writeFloat(this.y);
		dest.writeString(this.week);
		dest.writeString(this.date);
		dest.writeString(this.lowPhe);
		dest.writeInt(this.lowPheCode);
		dest.writeInt(this.lowTemp);
		dest.writeInt(this.nightWindDir);
		dest.writeInt(this.nightWindForce);
		dest.writeFloat(this.lowX);
		dest.writeFloat(this.lowY);
		dest.writeString(this.highPhe);
		dest.writeInt(this.highPheCode);
		dest.writeInt(this.highTemp);
		dest.writeInt(this.dayWindDir);
		dest.writeInt(this.dayWindForce);
		dest.writeFloat(this.highX);
		dest.writeFloat(this.highY);
		dest.writeInt(this.windDir);
		dest.writeInt(this.windForce);
	}

	public WeatherDto() {
	}

	protected WeatherDto(Parcel in) {
		this.cityName = in.readString();
		this.minuteFall = in.readFloat();
		this.aqi = in.readString();
		this.hourlyTemp = in.readInt();
		this.hourlyTime = in.readString();
		this.hourlyCode = in.readInt();
		this.hourlyWindDirCode = in.readInt();
		this.hourlyWindForceCode = in.readInt();
		this.hourlyHumidity = in.readString();
		this.x = in.readFloat();
		this.y = in.readFloat();
		this.week = in.readString();
		this.date = in.readString();
		this.lowPhe = in.readString();
		this.lowPheCode = in.readInt();
		this.lowTemp = in.readInt();
		this.nightWindDir = in.readInt();
		this.nightWindForce = in.readInt();
		this.lowX = in.readFloat();
		this.lowY = in.readFloat();
		this.highPhe = in.readString();
		this.highPheCode = in.readInt();
		this.highTemp = in.readInt();
		this.dayWindDir = in.readInt();
		this.dayWindForce = in.readInt();
		this.highX = in.readFloat();
		this.highY = in.readFloat();
		this.windDir = in.readInt();
		this.windForce = in.readInt();
	}

	public static final Creator<WeatherDto> CREATOR = new Creator<WeatherDto>() {
		@Override
		public WeatherDto createFromParcel(Parcel source) {
			return new WeatherDto(source);
		}

		@Override
		public WeatherDto[] newArray(int size) {
			return new WeatherDto[size];
		}
	};
}
