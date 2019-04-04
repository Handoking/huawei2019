package com.huawei.app;

import com.huawei.app.Application.SkyEyes;
import com.huawei.app.model.*;

import com.huawei.app.model.Route;
import com.huawei.app.model.FighterStatus.KunFlag;
import com.huawei.app.model.FighterStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

//SHIELD --神盾局总部
public class SHIELD {
	
	private SkyEyes eyes;
    private Map<Integer, KunFighter> kunFighters;
    private Map<Integer, Route> routs;
    private Map<Integer, Hub> hubs;
    private Map<Integer, FighterStatus> statues = null;
    private PriorityQueue<FighterStatus> flyingQue;
    private PriorityQueue<FighterStatus> schedulingQue;
    private PriorityQueue<FighterStatus> canFlyingQue;
    private int curTime = 0;
    
    private int freshCount = 0;//每次有战机成功更新位置信息+1，简单的死锁判断。
    private int kunCount = 0;//航线网络中战机的数量

    private int remainKunCount = 0;

    private RoutesStatus routesStatus = new RoutesStatus();

    //判定器
	private Evaluator evaluator = null;

	/**
	 *@params  [eyes]
	 *@author  handoking
	 *@date  2019/3/16
	 */
    SHIELD(SkyEyes eyes) {
    	this.eyes = eyes;
    	kunFighters = eyes.cars;
    	routs = eyes.roads;
    	hubs = eyes.crosses;
    	flyingQue = new PriorityQueue<>();
    	schedulingQue = new PriorityQueue<>();
    	canFlyingQue = new PriorityQueue<>();
	}

    public class RoutesStatus {
    	// 当前系统时间，当前航道网络中的战机的数量
    	private RoutesStatus() {}
    	int getKunCount() {
    		return kunCount;
    	}
    	int getCurTime() {
    		return curTime;
    	}
    }
    
	/**
	 * 判定器在神盾局备案
	 *@params  [e]
	 *@return  void
	 *@author  handoking
	 *@date  2019/3/16
	 */
	void recordEvaluator(Evaluator e) {
    	this.evaluator = e;
    }

	void init() {
    	statues = new HashMap<>();
    	for(KunFighter kun: kunFighters.values()){
			FighterStatus fs = new FighterStatus(kun.getFighterId(), kun, kun.getFlyTime());
			//初始化将所有的战机状态标记为等待起飞
			fs.flag = KunFlag.PFTO;
			fs.updateTime=kun.getFlyTime();
			fs.curRouteId =-1;//初始化 -1 ：空中管制
			fs.askSHIELDsAT =-1;
			fs.preHubId = -1;//默认即将起飞的战机前一段航线为空，取值-1。
			fs.nextHubId = kun.getStartPoint();// 战机起飞进入的第一段航线起点
			fs.kunSteering = Course.GS;//所有战机开始起飞第一段航线默认为直行
			statues.put(kun.getFighterId(),fs);
			canFlyingQue.add(fs);//将战机加入准备起飞队列
		}
    	eyes.flag =statues;
    	remainKunCount = kunFighters.size();
    	//起飞时间作为当前系统时间
    	if(canFlyingQue.size()>0)
    		curTime = canFlyingQue.peek().updateTime;
    	
    }
    /**
	 * 昆式战机出发！
     *@params  []
     *@return  void
     *@author  handoking
     *@date  2019/3/16
     */
	void run() {
    	System.err.println("start run,current system's time="+ curTime +", kunFighter.size="+ kunCount);
    	FighterStatus fs;
		boolean blFlag;
    	while(true) {
    		// 当前航线网络中有战机处于飞行状态
    		freshCount =0;
    		while(!flyingQue.isEmpty()&&
    			(fs= flyingQue.peek()).updateTime== curTime) {
    			flyingQue.poll();
    			fs = updateFlyingKunStatus(fs);
    			blFlag = fs.flag == KunFlag.SCHEDULING;
    			if(fs.flag == KunFlag.RESERVE_SCHEDULING) {
    				fs.flag = KunFlag.SCHEDULING;
    				fs = schedulKun(fs);
    			}
    			
    			if(fs.flag == KunFlag.INVISIBLE)
    				flyingQue.add(fs);
    			else if(fs.flag == KunFlag.SCHEDULING) {
    				schedulingQue.add(fs);
    			}
    			else if(fs.flag == KunFlag.LANDED) {
        			kunCount--;
        			remainKunCount--;
        			evaluator.Landed(fs.kunId, fs.nextHubId, routesStatus);
    			}
				if(blFlag) {
					evaluator.nearHub(fs.kunId, fs.curRouteId, routesStatus);
				}
			}

    		while(!schedulingQue.isEmpty()&&
    			(fs=schedulingQue.peek()).updateTime== curTime) {
    			schedulingQue.poll();
    			fs = schedulKun(fs);
    			if(fs.flag == KunFlag.INVISIBLE) {
    				evaluator.PassedHub(fs.kunId, fs.preHubId, routesStatus);
    				flyingQue.add(fs);
    			}

    			else if(fs.flag == KunFlag.SCHEDULING) {
    				schedulingQue.add(fs);
    			}

    			else if(fs.flag == KunFlag.LANDED) {
        			kunCount--;
        			remainKunCount--;
        			evaluator.Landed(fs.kunId, fs.nextHubId, routesStatus);
        			
    			}
    				
    		}
    		while(!canFlyingQue.isEmpty()&&
        			(fs= canFlyingQue.peek()).updateTime== curTime) {
    				canFlyingQue.poll();
        			fs = canFly(fs);
        			if(fs.flag == KunFlag.INVISIBLE)
        				flyingQue.add(fs);
        			else if(fs.flag == KunFlag.PFTO)
        				canFlyingQue.add(fs);
        		}
    		if(freshCount ==0) {System.err.println("Simulator may be dead locked!"); break;}
    		if(remainKunCount >0) curTime++;
    		else break;
    		
    	}
    	System.out.println("Task finished,AST="+ curTime);

	}
    

    private FighterStatus updateFlyingKunStatus(FighterStatus fs) {
    	Route curRoute = routs.get(fs.curRouteId);
    	Tunnel curTunnel = curRoute.
				getInHubTunnel(fs.nextHubId)[fs.curTunnelId];
    	FighterStatus[] fss = curTunnel.getTunnel();
    	int cLength = curTunnel.getTunnelLen();
    	int location= fs.locationOfTunnel;
    	if(location>=cLength- fs.curRouteSpeed) {
    		if(fs.askSHIELDHubId != fs.nextHubId) {
    			fs.askSHIELDHubId = fs.nextHubId;
    			fs.askSHIELDsAT = curTime;
    		}
    		fs.flag = KunFlag.RESERVE_SCHEDULING;
			fs.nextRouteId = evaluator.Scheduling(fs.kunId, fs.nextHubId, routesStatus);
			if(fs.nextRouteId <0)
				fs.kunSteering = Course.GS;
			else
				fs.kunSteering = hubs.get(fs.nextHubId)
					.getTurnDireByRoad(fs.curRouteId, fs.nextRouteId);
    	}else {
    		for(int i = 0; i< fs.curRouteSpeed; i++) {
    			if(fss[++location]!=null)
    			{location--;break;}
    		}

    		if(location> fs.locationOfTunnel) {
    			freshCount++;
        		fss[fs.locationOfTunnel]=null;
        		fss[location]= fs;
        		fs.locationOfTunnel =location;
    		} 

    		fs.updateTime++;
    		if(location>=cLength- fs.curRouteSpeed) {
    			fs.flag = KunFlag.SCHEDULING;
        		if(fs.askSHIELDHubId != fs.nextHubId) {
        			fs.askSHIELDHubId = fs.nextHubId;
        			fs.askSHIELDsAT = curTime;
        		}
    			fs.nextRouteId = evaluator.Scheduling(fs.kunId, fs.nextHubId, routesStatus);
    			if(fs.nextRouteId <0)
    				fs.kunSteering = Course.GS;
    			else
    				fs.kunSteering = hubs.get(fs.nextHubId)
    					.getTurnDireByRoad(fs.curRouteId, fs.nextRouteId);
    		}
    			
    	}
    	return fs;
    }

    private class Supervisor {
    	int tunnelId;
    	int tunnelLocation;
    	Supervisor(int id, int location){
    		this.tunnelId =id;
    		this.tunnelLocation =location;
    	}
    }

    private Supervisor viewNextRoute(Tunnel[] tunnel, int limit) {
    	int tunnelId,location=0;
    	FighterStatus[] fss;
    	for(tunnelId=0;tunnelId<tunnel.length;tunnelId++) {
    		fss=tunnel[tunnelId].getTunnel();
    		if(fss[0]!=null) continue;
			for(location=1;location<limit;location++)
				if(fss[location]!=null)break;
			location--;
			break;
    	}
    	if(tunnelId>=tunnel.length)
    		return null;
    	else
    		return new Supervisor(tunnelId,location);
    }
    
/**
 * 调度方法。
 *@params  [fs]
 *@return  com.huawei.app.model.FighterStatus
 *@author  handoking
 *@date  2019/3/27
 */
    private FighterStatus schedulKun(FighterStatus fs) {
    	FighterStatus[] fss;
    	if(fs.flag == KunFlag.SCHEDULING) {
    		//处于调度状态时
    		Route route = routs.get(fs.curRouteId);//当前航线
    		Tunnel[] tunnels = route.
					getInHubTunnel(fs.nextHubId);//下一段航线的正向通道
        	Tunnel curTunnel = tunnels[fs.curTunnelId];
    		fss = curTunnel.getTunnel();
    		int location=fs.locationOfTunnel;
    		while(++location< curTunnel.getTunnelLen()) {
    			if(fss[location]!=null)  {
    				location--;
    				break;
    			}
    		}

    		boolean curKunFirst = true;
    		if(fs.curTunnelId >0) {
    			Tunnel tl = tunnels[fs.curTunnelId -1];
    			if(tl.getTunnel()[fs.locationOfTunnel]!=null)
    				curKunFirst = false;
    			
    		}
    		if(!curKunFirst||location< curTunnel.getTunnelLen()) {
    			if(location== route.getRouteLen()) location--;
    			if(location>fs.locationOfTunnel) freshCount++;
    			fss[fs.locationOfTunnel]=null;
    			fss[location]=fs;
    			fs.locationOfTunnel = location;
    			fs.updateTime++;
        		if(fs.askSHIELDHubId !=fs.nextHubId) {
        			fs.askSHIELDHubId = fs.nextHubId;
        			fs.askSHIELDsAT = curTime;
        		}
    			fs.nextRouteId = evaluator.Scheduling(fs.kunId, fs.nextHubId, routesStatus);
    			if(fs.nextRouteId <0)
    				fs.kunSteering = Course.GS;
    			else
    				fs.kunSteering = hubs.get(fs.nextHubId)
    					.getTurnDireByRoad(fs.curRouteId, fs.nextRouteId);
    			return fs;
    		}
    		if(fs.nextHubId ==fs.kunFighter.getEndPoint()) {
    			fs.flag = KunFlag.LANDED;
    			fss[fs.locationOfTunnel]=null;
    			freshCount++;
    			return fs;
    		}
    		route = routs.get(fs.nextRouteId);
    		int nextRouteSpeed = Math.min(route.getRouteMaxSpeed(),
    				fs.kunFighter.getKunMaxSpeed());
    		int remainLen = nextRouteSpeed-
    				(curTunnel.getTunnelLen()-fs.locationOfTunnel -1);
    		
    		Tunnel[] tun = route.getOutHubTunnel(fs.nextHubId);
    		Supervisor supervisor;

    		if(remainLen<=0||(supervisor= viewNextRoute(tun,remainLen))==null) {

    			location = curTunnel.getTunnelLen()-1;
    			if(location>fs.locationOfTunnel) freshCount++;
    			fss[fs.locationOfTunnel]=null;
    			fss[location]=fs;
    			fs.locationOfTunnel = location;
    			fs.updateTime++;
        		if(fs.askSHIELDHubId !=fs.nextHubId) {
        			fs.askSHIELDHubId = fs.nextHubId;
        			fs.askSHIELDsAT = curTime;
        		}
    			fs.nextRouteId = evaluator.Scheduling(fs.kunId, fs.nextHubId, routesStatus);
    			if(fs.nextRouteId <0)
    				fs.kunSteering = Course.GS;
    			else
    				fs.kunSteering = hubs.get(fs.nextHubId)
    					.getTurnDireByRoad(fs.curRouteId, fs.nextRouteId);
    			return fs;
    		}
    	
    		
    		freshCount++;
			fss[fs.locationOfTunnel]=null;
			
			
			fs.flag = KunFlag.INVISIBLE;
			fs.curRouteSpeed = nextRouteSpeed;
			fs.curRouteId =fs.nextRouteId;
			fs.curTunnelId =supervisor.tunnelId;
			fs.locationOfTunnel =supervisor.tunnelLocation;
			fs.preHubId =fs.nextHubId;
			fs.nextHubId = route.getAnotherHubId(fs.preHubId);
			fs.inRouteSAT = curTime;
			fs.updateTime++;
			fs.addRouteId(fs.curRouteId);
			fss = tun[fs.curTunnelId].getTunnel();
			fss[fs.locationOfTunnel]=fs;
			return fs;
   		
    	}
    	else 
    		throw new IllegalArgumentException("error"+fs.flag);
    }
    
    
    private FighterStatus canFly(FighterStatus fs) {
    	FighterStatus[] fss;
    	if(fs.flag == KunFlag.PFTO) {
    		if(!evaluator.PrepFly(fs.kunId, fs.nextHubId, routesStatus)) {
    			fs.updateTime++;
    			return fs;
    		}
    		if(fs.askSHIELDHubId !=fs.nextHubId) {
    			fs.askSHIELDHubId = fs.nextHubId;
    			fs.askSHIELDsAT = curTime;
    		}
    		fs.nextRouteId = evaluator.Scheduling(fs.kunId, fs.nextHubId, routesStatus);
    		Route nextRoute = routs.get(fs.nextRouteId);
    		int nextRoadMaxSpeed = Math.min(nextRoute.getRouteMaxSpeed(),
    				fs.kunFighter.getKunMaxSpeed());

			Tunnel[] tun = nextRoute.getOutHubTunnel(fs.nextHubId);
    		Supervisor supervisor;
			supervisor = viewNextRoute(tun, nextRoadMaxSpeed);
			if(supervisor==null) {
    			fs.updateTime++;
        		return fs;
    		}
    		evaluator.Flying(fs.kunId, fs.nextHubId, routesStatus);
    		freshCount++;
    		kunCount++;
			fs.flag = KunFlag.INVISIBLE;
			fs.curRouteSpeed = nextRoadMaxSpeed;
			fs.curRouteId =fs.nextRouteId;
			fs.curTunnelId =supervisor.tunnelId;
			fs.locationOfTunnel =supervisor.tunnelLocation;
			fs.preHubId =fs.nextHubId;
			fs.nextHubId = nextRoute.getAnotherHubId(fs.preHubId);
			fs.actStartTime =fs.updateTime;
			fs.addRouteId(fs.curRouteId);
			fs.inRouteSAT = curTime;
			fs.updateTime++;
			fss = tun[fs.curTunnelId].getTunnel();
			fss[fs.locationOfTunnel]=fs;
			return fs;
    	}
    	else 
    		throw new IllegalArgumentException("error: "+fs.flag);
    }
}
