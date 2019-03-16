package com.huawei.app.model;

import java.util.Arrays;
import java.util.Map;

public class Cross {

	
	private final Integer crossId;
	
	// 按顺时针排序Road的id,
	private final int[] crossRoadIds;
	// 注意这里的ID是指可以从这个路口出去的Road
	// 当无出去的路时，为-1;
	private int[] connOutRoadIds= {-1,-1,-1,-1};

	public Cross(int crossId,int[] crossRoadIds) {
		this.crossId=crossId;
		this.crossRoadIds = crossRoadIds;
	}
	
	public Cross(int[] args) {
		this.crossId=args[0];
		this.crossRoadIds =Arrays.copyOfRange(args, 1, 5);
	}

	/**
	 * 根据当前车辆行驶路径相对于路口的位置，计算下向前、向左、向右的RoadId
	 * 可以通过getDirectionByRoadId 计算roadDirectInCross
	 * 若放回-1 表示选择的方向上无Road或者无效输入
	 *@params  [roadDirectInCross, needNextDriveDirect]
	 *@return  int
	 *@author  handoking
	 *@date  2019/3/16
	 */
	public int getNextRoadId(int roadDirectInCross, int needNextDriveDirect) {
		if(roadDirectInCross < Compass.N || roadDirectInCross > Compass.W ||
				needNextDriveDirect < DriveDirection.RIGHT)
			return -1;// 无效方向
		else
			return connOutRoadIds[(roadDirectInCross+6+needNextDriveDirect)%4];
	}
	private int getDirectionByRoadId(int roadId) {
		int res = 0;
		for(;res<4;res++) 
			if(crossRoadIds[res]==roadId) break;
		if (res==4) 
			throw new IllegalArgumentException("roadId not in crossRoadIds");
		return res;
	}
	
	
	/**
	 * 根据进入的Road的相对于路口的位置和出去的RoadId判断，该行驶属于向左、直行、向右中的哪种
	 * 通过 getDirectionByRoadId计算过inRoadDirection
	 *@params  [inRoadDirection, outRoadId]
	 *@return  int
	 *@author  handoking
	 *@date  2019/3/16
	 */
	private int getTurnDirection(int inRoadDirection, int outRoadId) {
		if(connOutRoadIds[(inRoadDirection+1)%4]==outRoadId) 
			return DriveDirection.LEFT;
		else if(connOutRoadIds[(inRoadDirection+2)%4]==outRoadId)
			return DriveDirection.FOWARD;
		else if(connOutRoadIds[(inRoadDirection+3)%4]==outRoadId)
			return DriveDirection.RIGHT;
		else 
			throw new IllegalArgumentException("outRoadId not in connOutRoadIds");
	}

	
	public int getTurnDireByRoad(int inRoadId,int outRoadId) {
		if(inRoadId<0||outRoadId<0||inRoadId==outRoadId) 
			throw new IllegalArgumentException("Error RoadId:inRoad="+inRoadId+",outRoad="+outRoadId);
		return getTurnDirection(getDirectionByRoadId(inRoadId),outRoadId);
	}
	
	
	/**
	 * > 这个函数更新connOutRoadIds中的roadId
	 * 
	 */
	public void setConnOutRoadIds(Map<Integer,Road> roads) {
		int rid;
		Road rd = null;
		for(int i=0;i<4;i++) {
			if((rid= crossRoadIds[i])<0) continue;
			rd = roads.get(rid);
			if(rd.isDuplex()||rd.getStartCrossId()==crossId)
				connOutRoadIds[i]=rid;	
		}
	}
	

	/**
	 * 
	 */
	public void updateConnCrossIds(Map<Integer,Road> roads) {

	}
	
	
	public Integer getCrossId() {
		return crossId;
	}

	public int[] getCrossRoadIds() {
		return crossRoadIds;
	}


	
	
	
	
}
