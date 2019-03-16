package com.huawei.app.model;
public class Car {
	private Integer carId;//车辆ID
	private int starPoint;//出发地点
	private int endPoint;// 目的地
	private int maxSpeed;// 车辆最高速度
	private int startTime;//出发时刻
	

	
	public Car(int carId,int oriCrossId,int desCrossId,
			int maxSpeed,int startTime) {
		this.carId=carId;this.starPoint =oriCrossId;
		this.endPoint =desCrossId;this.maxSpeed=maxSpeed;
		this.startTime=startTime;
	}
	
	public Car(int[] args) {
		this.carId=args[0];this.starPoint =args[1];
		this.endPoint =args[2];this.maxSpeed=args[3];
		this.startTime=args[4];
	}	
	

	public Integer getCarId() {
		return carId;
	}


	public int getStartPoint() {
		return starPoint;
	}


	public int getEndPoint() {
		return endPoint;
	}


	public int getMaxSpeed() {
		return maxSpeed;
	}


	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	
}
