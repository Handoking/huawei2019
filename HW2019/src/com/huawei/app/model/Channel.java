package com.huawei.app.model;

public class Channel {

	private int roadId=0;
	private int channelId=0;
	private int carCount =0;// 通道内车辆计数
	// 当前车道新车进入的初始速度
	// 当 当前车道无车时，车道速度恢复为 Road的 maxSpeed;
	private int inChannelInitSpeed;
	// 车道的长度、与道路的长度相同
	private int channelLen;
	private CarAttr[] channel =null;
	
	public Channel(int roadId, int channelId, int maxSpeed, int channelLen) {
		this.roadId=roadId;this.channelId=channelId;
		this.inChannelInitSpeed =maxSpeed;this.channelLen = channelLen;
	}
	
	public int getRoadId() {
		return roadId;
	}
	public int getChannelId() {
		return channelId;
	}
	public int getCarCount() {
		return carCount;
	}

	public int incAndGetCarCot() {
		return ++carCount;
	}

	public int decAndGetCarCot() {
		return --carCount;
	}
	
	public int getInChannelInitSpeed() {
		return inChannelInitSpeed;
	}
	public void setInChannelInitSpeed(int inChannelInitSpeed) {
		this.inChannelInitSpeed = inChannelInitSpeed;
	}
	public int getChannelLen() {
		return channelLen;
	}

	/**
	 * lazy 生成车道空间
	 * @return
	 */
	public CarAttr[] getChanel() {
		if(channel==null)
			channel = new CarAttr[channelLen];
		return channel;
	}

	
	
	
}
