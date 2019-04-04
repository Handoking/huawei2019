package com.huawei.app.model;

import java.util.LinkedList;
import java.util.List;
/**
 * FighterStatus -昆式战机的状态
 *@author  handoking
 *@date  2019/3/15
 */
public class FighterStatus implements Comparable<FighterStatus>{

	public int kunId;
	public KunFighter kunFighter;
	
	public int actStartTime;// 真正出发time
	private List<Integer> routes = new LinkedList<>();//保存经过的航线段
	public int curRouteSpeed;//此段航线上最大的速度
	public int preHubId;// 上一个HubID
	public int nextHubId;// 下一个HubID
	public int curRouteId;//当前行驶的RouteId
	public int curTunnelId;//当前行驶的通道Id
	public int updateTime;// 状态刷新时间
	public int locationOfTunnel;
	public int inRouteSAT;//飞入某段航线的系统时间
	public int askSHIELDHubId;//请求神盾局总部调度时战机的坐标（交汇处id）
	public int askSHIELDsAT;//最后一次请求神盾局的系统时间
	public KunFlag flag = KunFlag.PFTO;//处于等待起飞
	public int nextRouteId;
	public int kunSteering;//昆式战机的转向

	public enum KunFlag {

		//prepare for take off准备起飞的kun，满足起飞条件，等待神盾局调度。
		PFTO,
		//正常隐身飞行
		INVISIBLE,
		//处于调度中
		SCHEDULING,
		//处于预约调度中，处于调度区提前预约申请调度
		RESERVE_SCHEDULING,
		//到达目的地，降落
		LANDED

		
	}
	
	public FighterStatus(int kunId, KunFighter kunFighter, int curTime) {
		this.kunId = kunId;
		this.kunFighter = kunFighter;
		this.updateTime = curTime;
	}
	
	
	
	// 将经过的航线段id保存
	public void addRouteId(int routeId) {

		routes.add(routeId);
	}

/**
 * 打印出飞行航线
 *@params  []
 *@return  java.lang.String
 *@author  handoking
 *@date  2019/3/15
 */
	public String printRoute() {
		StringBuilder sb =new StringBuilder();
		sb.append("(");
		sb.append(kunId);
		sb.append(",").append(actStartTime);
		routes.forEach(v-> sb.append(",").append(v));
		sb.append(")");
		return sb.toString();
	}
	
	
	
	@Override
	public int compareTo(FighterStatus data) {
		if(updateTime !=data.updateTime) {
			return updateTime -data.updateTime;
		}
		if(flag == KunFlag.INVISIBLE) {
			return data.locationOfTunnel - locationOfTunnel;
		}else if(flag == KunFlag.SCHEDULING){
			if(data.locationOfTunnel != locationOfTunnel)
				return data.locationOfTunnel - locationOfTunnel;
			if(curTunnelId !=data.curTunnelId)
				return curTunnelId -data.curTunnelId;
			if(kunSteering !=data.kunSteering) {
				if(kunSteering == Course.R) return 1;
				else if(kunSteering == Course.GS) return -1;
				else if(data.kunSteering == Course.GS) return 1;
				else return -1;
			}
			return 0;
		}else if(flag == KunFlag.PFTO) {
			// 准备起飞的kun,id小的优先，
			return kunId -data.kunId;
		}
	return 0;

	}

}
