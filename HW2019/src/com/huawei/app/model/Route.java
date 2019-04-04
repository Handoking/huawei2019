package com.huawei.app.model;

/**
 *Route -航线 相当于道路
 * Hub -航线交汇处 相当于路口
 * Tunnel -飞行通道 相当于车道
 *@author  handoking
 *@date  2019/3/15
 */
public class Route {

	private Integer routeId;
	private int routeLen;
	private int routeMaxSpeed;
	private int tunnelNum;//飞行通道数
	private int startHubId;// 起点（交汇处）Id
	private int endHubId;// 终点（交汇处// ）Id
	private int isDuplex;// 双向与否

	private Tunnel[] positiveTunnels = null;
	
	// 双向通道时被使用
	private Tunnel[] reverseTunnels = null;


	/**
	 *@params  [args]
	 *@return
	 *@author  handoking
	 *@date  2019/3/15
	 */
	public Route(int[] args) {
		this.routeId =args[0];this.routeLen =args[1];
		this.routeMaxSpeed =args[2];this.tunnelNum =args[3];
		this.startHubId =args[4];this.endHubId =args[5];
		this.isDuplex =args[6];
	}	
	
	
	public boolean isDuplex() {
		return this.isDuplex ==1;
	}
	
/**
 * （当前道路入口的信息）
 *@params  [crossId]
 *@return  com.huawei.app.model.Tunnel[]
 *@author  handoking
 *@date  2019/3/15
 */
	public Tunnel[] getInHubTunnel(int crossId) {
		
		if(isDuplex()&& startHubId ==crossId) {
			// 双向通道道路，并且当前在入口
			if(reverseTunnels ==null) initReverseTunnel();
			return reverseTunnels;
		}
		if(endHubId !=crossId)//如果此道路出口也不为当前路口，那就发生了错误
			throw new IllegalArgumentException("endHubId !=crossId err "+crossId);
		if(positiveTunnels ==null) initPositiveTunnels();
		
		return positiveTunnels;
	}
	
	/**
	 * 道路出口，返回当前道路的道路信息。
	 *@params  [crossId]
	 *@return  com.huawei.app.model.Tunnel[]
	 *@author  handoking
	 *@date  2019/3/15
	 */
	public Tunnel[] getOutHubTunnel(int crossId) {
		
		if(isDuplex()&& endHubId ==crossId) {
			// 双向通道时初始化反向车道
			if(reverseTunnels ==null) initReverseTunnel();
			return reverseTunnels;
		}
		
		if(startHubId !=crossId)
			throw new IllegalArgumentException("startHubId !=crossId err "+crossId);
		if(positiveTunnels ==null) initPositiveTunnels();
		return positiveTunnels;
		
	}
	
/**
 *@params  [oneCrossId]
 *@return  int
 *@author  handoking
 *@date  2019/3/15
 */
	public int getAnotherHubId(int oneCrossId) {
		if(startHubId ==oneCrossId) return endHubId;
		else if(endHubId ==oneCrossId) return startHubId;
		else 
			throw new IllegalArgumentException(oneCrossId+" not in Route "+ routeId);
	}
	
	/**
	 * 初始化创建正向通道
	 *@params  []
	 *@return  void
	 *@author  handoking
	 *@date  2019/3/15
	 */
	private void initPositiveTunnels() {
		positiveTunnels = new Tunnel[tunnelNum];
		for(int i = 0; i< tunnelNum; i++)
			positiveTunnels[i]= new Tunnel(routeId, i, routeMaxSpeed, routeLen);
	}
/**
 * 初始化创建反向通道
 *@params  []
 *@return  void
 *@author  handoking
 *@date  2019/3/15
 */
	private void initReverseTunnel() {
		reverseTunnels = new Tunnel[tunnelNum];
		for(int i = 0; i< tunnelNum; i++)
			reverseTunnels[i]= new Tunnel(routeId, i, routeMaxSpeed, routeLen);
	}

	public Integer getRouteId() {
		return routeId;
	}

	public int getRouteLen() {
		return routeLen;
	}

	public int getRouteMaxSpeed() {
		return routeMaxSpeed;
	}

	public int getStartHubId() {
		return startHubId;
	}

	public int getEndHubId() {
		return endHubId;
	}

//	public int getIsDuplex() {
//		return isDuplex;
//	}
}

