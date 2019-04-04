package com.huawei.app.model;

/**
 * Tunnel - 时空隧道 相当于channel
 *@author  handoking
 *@date  2019/3/15
 */

public class Tunnel {

	private int kunId;
	private int tunnelId;
	private int kunCount =0;
	// 进入通道kunFighter的初始速度
	private int inTunnelInitSpeed;
	private int TunnelLen;
	private FighterStatus[] tunnel =null;
	Tunnel(int kunId, int tunnelId, int maxSpeed, int TunnelLen) {
		this.kunId = kunId;this.tunnelId = tunnelId;
		this.inTunnelInitSpeed =maxSpeed;this.TunnelLen = TunnelLen;
	}

	public int getTunnelLen() {
		return TunnelLen;
	}

	/**
	 * 创建通道
	 */
	public FighterStatus[] getTunnel() {
		if(tunnel ==null)
			tunnel = new FighterStatus[TunnelLen];
		return tunnel;
	}

	
	
	
}
