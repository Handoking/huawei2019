package com.huawei.app.model;
/**
 * KunFighter:昆式战斗机（《神盾局》）
 *@author  handoking
 *@date  2019/3/15
 */
public class KunFighter {
	private Integer fighterId;//昆式战斗机ID
	private int starPoint;//出发地点
	private int endPoint;// 目的地
	private int kunMaxSpeed;// 车辆最高速度
	private int flyTime;//出发时刻

	
	public KunFighter(int[] args) {
		this.fighterId =args[0];this.starPoint =args[1];
		this.endPoint =args[2];this.kunMaxSpeed =args[3];
		this.flyTime =args[4];
	}	
	

	public Integer getFighterId() {
		return fighterId;
	}


	public int 	getStartPoint() {
		return starPoint;
	}


	public int getEndPoint() {
		return endPoint;
	}


	public int getKunMaxSpeed() {
		return kunMaxSpeed;
	}


	public int getFlyTime() {
		return flyTime;
	}
	
}
