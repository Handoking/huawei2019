package com.huawei.app.model;

import java.util.Arrays;
import java.util.Map;

/**
 * Hub -交汇点 相当于cross
 *@author  handoking
 *@date  2019/3/15
 */
public class Hub {

	
	private final Integer hubId;
	public final int[] hubRouteIds;//交汇处按顺时针读取存放航线
	private int[] outRouteIds = {-1,-1,-1,-1};//从交汇处出的航线

	
	public Hub(int[] args) {
		this.hubId =args[0];
		this.hubRouteIds =Arrays.copyOfRange(args, 1, 5);
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
	private int getDirection(int routeId) {
		int res = 0;
		for(;res<4;res++) 
			if(hubRouteIds[res]==routeId) break;
		if (res==4) 
			throw new IllegalArgumentException("roadId not in hubRouteIds");
		return res;
	}
	
	
	/**
	 *@params  [inRoadDirection, outRoadId]
	 *@return  int
	 *@author  handoking
	 *@date  2019/3/16
	 */
	private int getTurnDirection(int inRouteDir, int outRouteId) {
		if(outRouteIds[(inRouteDir+1)%4]==outRouteId)
			return Course.L;
		else if(outRouteIds[(inRouteDir+2)%4]==outRouteId)
			return Course.GS;
		else if(outRouteIds[(inRouteDir+3)%4]==outRouteId)
			return Course.R;
		else 
			throw new IllegalArgumentException("outRoadId not in outRouteIds");
	}

	
	public int getTurnDireByRoad(int inRoadId,int outRoadId) {
		if(inRoadId<0||outRoadId<0||inRoadId==outRoadId) 
			throw new IllegalArgumentException("Error RoadId:inRoad="+inRoadId+",outRoad="+outRoadId);
		return getTurnDirection(getDirection(inRoadId),outRoadId);
	}
	
	

	public void setOutRouteIds(Map<Integer, Route> roads) {
		int routeId;
		Route re = null;
		for(int i=0;i<4;i++) {
			if((routeId= hubRouteIds[i])<0) continue;
			re = roads.get(routeId);
			if(re.isDuplex()||re.getStartHubId()== hubId)
				outRouteIds[i]=routeId;
		}
	}

	public Integer getHubId() {
		return hubId;
	}
}
