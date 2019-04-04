package com.huawei.app;

import com.huawei.app.Application.SkyEyes;
import com.huawei.app.model.*;
import com.huawei.app.SHIELD.RoutesStatus;
import java.util.*;
import java.util.stream.Collectors;
/**
 * Ada -Ada的升级版superAda,神盾局复原的超级大脑艾达
 * @author :Handoking
 * @date : 2019/3/18
 * 初始化飞行状况,更新空况的当前代价,实现最短路径算法
 */
public class Ada implements Evaluator {

	private SkyEyes skyEyes;
	private Map<Integer, Route> routs;
	private Map<Integer, Hub> hubs;
    private Map<Integer,Integer> hubIdMap =null;
    private List<Integer> hubIdList =null;
	private int[] hubStart;
	private int[] hubStop;
	private int[] hubPassed;
	private Map<Integer,PathNode> initKunPath;
	private Map<Integer, TimeSlidWin> routeAvg;
	private Map<Integer, TimeSlidWin> hubAvg;


    // 航线网
    private Route[][] routeNet = null;
    // 权重图
    private int[][] weights = null;

    // 当前系统时间
    private int curTime = -1;
	private int winSize =10;
	private int finallySAT = -1;


	private class PathNode {
    	int hubId;
    	int inRouteId;
    	PathNode next;
		PathNode(PathNode next, int hubId, int inRouteId) {
			this.next = next;
			this.hubId = hubId;
			this.inRouteId = inRouteId;
    	}
    }

    Ada(SkyEyes skyEyes) {
    	this.skyEyes = skyEyes;
    	routs = skyEyes.roads;
    	hubs = skyEyes.crosses;
		Map<Integer, KunFighter> cars = skyEyes.cars;
    	indexHub(hubs.keySet());//keySet()将所有cross文件包括的键值存入set（即crossId）
		initKunPath = new HashMap<>(cars.size());
		routeAvg = new HashMap<>(routs.size());
		hubAvg = new HashMap<>(hubs.size());
		hubStart = new int[hubs.size()];
		hubStop = new int[hubs.size()];
		hubPassed = new int[hubs.size()];
    }


	void routeInit(){
		if(hubs.size()>150){
			winSize = 5;
		}
		routeNet = new Route[hubs.size()][hubs.size()];
		weights = new int[hubs.size()][hubs.size()];
		routs.values().forEach(route->{
			routeAvg.put(route.getRouteId(), new TimeSlidWin(winSize));
			int i = keyToHubIndex(route.getStartHubId());
			int j = keyToHubIndex(route.getEndHubId());
			routeNet[i][j] = route;
			if(route.isDuplex()){
				routeNet[j][i] = route;
			}
		});
		hubs.values().forEach(hub->{
			hubAvg.put(hub.getHubId(),new TimeSlidWin(winSize));
		});
	}
	/**
	 *@params  [allCrossIds]
	 *@return  void
	 *@author  handoking
	 *@date  2019/3/16
	 */
	private void indexHub(Collection<Integer> allCrossIds){
		Map<Integer,Integer> hashMap = new HashMap<>();
		List<Integer> ids = allCrossIds.stream()
				.sorted(Integer::compare)
				.collect(Collectors.toCollection(ArrayList::new));
		for(int i=0;i<ids.size();i++) {
			hashMap.put(ids.get(i), i);
		}
		hubIdList = ids;//list中放入hubId
		hubIdMap = hashMap;//以序号为key，以hubId为value
	}
	private int keyToHubIndex(int hubId) {
		return hubIdMap.get(hubId);
	}
	private int indexGetHubId(int index) {
		return hubIdList.get(index);
	}
	/**
	 * 根据速度来更新航线的权重
	 *@params  [speed]
	 *@return  void
	 *@author  handoking
	 *@date  2019/3/20
	 */
	private void updateWeight(FighterStatus fs){
		for(int i = 0; i< weights.length; i++){
			for(int j = 0; j< weights.length; j++) {
				if(routeNet[i][j]==null) weights[i][j] = Integer.MAX_VALUE;
				else {
					Route route = routeNet[i][j];
					if(route.getRouteId()==fs.curRouteId)
						weights[i][j]=Integer.MAX_VALUE;
					else {
						int feedback = (int) Math.ceil(hubAvg.get(indexGetHubId(i)).getAvg(curTime));
						weights[i][j] = culWeight(route, fs)+feedback;
                        //weights[i][j] = culWeight(route, fs);
					}
				}
			}
		}
	}


	private int culWeight(Route route, FighterStatus fs){
		//int count =0;
		int fbWeight = (int) Math.ceil(routeAvg.get(route.getRouteId()).getAvg(curTime));
		//int avg =0;
		int speed = Math.min(route.getRouteMaxSpeed(),fs.kunFighter.getKunMaxSpeed());
		//int minTime = (int)Math.ceil(route.getRouteLen()*1.0/speed+(fs.kunFighter.getKunMaxSpeed()*1.0/route.getRouteMaxSpeed())*2);
        int minTime = (int)Math.ceil(route.getRouteLen()*1.0/speed)  ;
//		Tunnel[] tul = route.getOutHubTunnel(route.getStartHubId());
//		FighterStatus[] fighterStatuses;
//		for (Tunnel tunnel:tul){
//			fighterStatuses = tunnel.getTunnel();
//			for (FighterStatus fss:fighterStatuses){
//				if (fss!=null&&fss.kunId>=0){
//					count++;
//				}
//			}
//		}
//		if (route.isDuplex()){
//			tul = route.getInHubTunnel(route.getEndHubId());
//			for (Tunnel tunnel:tul){
//				fighterStatuses = tunnel.getTunnel();
//				for (FighterStatus fss:fighterStatuses){
//					if (fss!=null&&fss.kunId>=0){
//						count++;
//					}
//				}
//			}
//			count = count/2;
//		}
		//avg = (int)Math.ceil(count*1.0/route.getRouteLen())*2;
		return minTime+fbWeight;
        //count = count/(tul.length * tul[0].getTunnelLen());
		//return minTime+count;
	}
    private static int CurRouteKunNum(Route route){
	    int count = 0;
        Tunnel[] tul = route.getOutHubTunnel(route.getStartHubId());
		FighterStatus[] fighterStatuses;
		for (Tunnel tunnel:tul){
			fighterStatuses = tunnel.getTunnel();
			for (FighterStatus fss:fighterStatuses){
				if (fss!=null&&fss.kunId>=0){
					count++;
				}
			}
		}
		if (route.isDuplex()){
			tul = route.getInHubTunnel(route.getEndHubId());
			for (Tunnel tunnel:tul){
				fighterStatuses = tunnel.getTunnel();
				for (FighterStatus fss:fighterStatuses){
					if (fss!=null&&fss.kunId>=0){
						count++;
					}
				}
			}
			count = count/2;
		}
	    return count;
    }

	/**
	 * 迪杰斯特拉算法
	 *@params  [kunFighter, startPoint, endPoint]
	 *@return  com.huawei.app.Ada.PathNode
	 *@author  handoking
	 *@date  2019/3/18
	 */
	private PathNode Dijkstra(KunFighter kunFighter, int startPoint, int endPoint) throws IllegalArgumentException {
		if(kunFighter ==null||startPoint==endPoint){
			throw new IllegalArgumentException("err:无战机或者起止点相同");
		}
		int start = keyToHubIndex(startPoint);
		int end = keyToHubIndex(endPoint);
		int[] weight = new int[hubs.size()];
		boolean[] visited = new boolean[hubs.size()];
		int[] path= new int[hubs.size()];
		int pose = 0;
		//初始化各种数组
		for(int i = 0; i< hubs.size(); i++){
			if(i!=start){
				path[i] = -1;
			}
		}
		for (int i =0;i<weights.length;i++){
			visited[i] = false;
			if(i!=start){
				weight[i] = weights[start][i];
				if(routeNet[start][i]!=null){
					path[i] = start;
				}
			}
		}
		visited[start] = true;
		for(int i = 0; i< hubs.size(); i++){
			int minWei = Integer.MAX_VALUE;
			for(int j = 0; j< hubs.size(); j++){
				if(!visited[j]&&weight[j]<minWei){
					minWei = weight[j];
					pose = j;
				}
			}
			visited[pose] = true;
			if (pose == end) {
				break;
			}
			int temp =0;
			for (int j = 0; j< hubs.size(); j++){
				if (weights[pose][j]<Integer.MAX_VALUE){
					if (weight[j]>minWei+weights[pose][j]){
						weight[j] = minWei+weights[pose][j];
						path[j] = pose;
						temp =j;
					}
//					if (weight[j]==minWei+weights[pose][j]){
//						//当代价相同时选择限定速度较大的航线段
//						int speed3=0;
//						int speed4=0;
//						for(Route road:routs.values()){
//							if (road.getStartHubId()==indexGetHubId(pose)&&road.getEndHubId()==indexGetHubId(j)){
//								speed3=road.getRouteMaxSpeed();
//							}
//							if (road.getStartHubId()==indexGetHubId(pose)&&road.getEndHubId()==indexGetHubId(temp)){
//								speed4=road.getRouteMaxSpeed();
//							}
//							if(speed3>speed4){
//								path[j] = pose;
//							}
//						}
//					}
				}
			}
		}
		//按照最短路径将下一段航线返回
		PathNode pn = new PathNode(null,endPoint,-1);
			int i;
			int j=end;
			while((i=path[j])!=start) {
				pn = new PathNode(pn, indexGetHubId(i), routeNet[i][j].getRouteId());
				j=i;
			}
			pn = new PathNode(pn, indexGetHubId(i), routeNet[i][j].getRouteId());

			return pn;
	}
	
	
	@Override
	public int Scheduling(int kunId, int curHubId, RoutesStatus rs) {
		if(curTime<0||curTime<rs.getCurTime()) curTime = rs.getCurTime();
		hubPassed[keyToHubIndex(curHubId)]++;
		FighterStatus fs = skyEyes.flag.get(kunId);
		KunFighter kunFighter = fs.kunFighter;
		if(curHubId== kunFighter.getEndPoint()){
            return -1;
        }
		if(finallySAT <0|| finallySAT ==curTime) {
			initKunPath.put(kunId,null);
			finallySAT =curTime;
		}
		int timeDelay = 6;
		if(finallySAT ==curTime-1) finallySAT =curTime+ timeDelay;
		PathNode curPath ;
//		if((curPath= initKunPath.get(kunId))==null) {
//			updateWeight(fs);
//			curPath = Dijkstra(kunFighter,curHubId, kunFighter.getEndPoint());
//			initKunPath.put(kunId, curPath);
//		}
        updateWeight(fs);
        curPath = Dijkstra(kunFighter,curHubId, kunFighter.getEndPoint());
        initKunPath.put(kunId, curPath);
		while(curPath!=null&&curPath.hubId !=curHubId){
			curPath=curPath.next;
		}
		if(curPath==null)
			throw new IllegalArgumentException("HubId:"+curHubId+" is not in kunPath");
		return curPath.inRouteId;
	}

	@Override
	public boolean PrepFly(int kunId, int hubId, RoutesStatus rs) {
//        Hub hub = hubs.get(hubId);
//        int num1=0;
//        int num2=0;
//        int num3=0;
//        int num4=0;
//        int[] routeId = hub.hubRouteIds;
//        Route r1 = routs.get(routeId[0]);
//        Route r2  = routs.get(routeId[1]);
//        Route r3 = routs.get(routeId[2]);
//        Route r4  = routs.get(routeId[3]);
//        if (r1!=null){
//            num1 = Ada.CurRouteKunNum(r1);
//        }
//        if (r2!=null){
//            num2 = Ada.CurRouteKunNum(r2);
//        }
//        if (r3!=null){
//            num3 = Ada.CurRouteKunNum(r3);
//        }
//        if (r4!=null){
//            num4 = Ada.CurRouteKunNum(r4);
//        }
//        int sum = num1+num2+num3+num4;
//        if (sum>(rs.getKunCount()/hubs.size()*4)){
//            return false;
//        }else{
//            //return rs.getKunCount() <1200;
//			return rs.getKunCount() <5000;
//        }
		if (hubs.size()>150){
			return rs.getKunCount() <4500;
		}else{
			return rs.getKunCount()<5000;
		}
		//return rs.getKunCount() <4500;

	}
	@Override
	public void Flying(int kunId, int hubId, RoutesStatus rs) {
		hubStart[keyToHubIndex(hubId)]++;
	}
	@Override
	public void Landed(int kunId, int hubId, RoutesStatus rs) {
		System.err.println("KunFighter:"+kunId+"->Hub:"+hubId+"->time:"+rs.getCurTime());
		hubStop[keyToHubIndex(hubId)]++;
	}
	@Override
	public void PassedHub(int kunId, int curHubId, RoutesStatus rs) {
		FighterStatus ca = skyEyes.flag.get(kunId);
		int scheTime = rs.getCurTime()-ca.askSHIELDsAT;
		hubAvg.get(curHubId).add(rs.getCurTime(), scheTime);
	}

	@Override
	public void nearHub(int kunId, int routeId, RoutesStatus rs) {
		FighterStatus fs = skyEyes.flag.get(kunId);
		int minTime = fs.locationOfTunnel /fs.curRouteSpeed;
		int realtime = rs.getCurTime()-fs.inRouteSAT;
		routeAvg.get(routeId).add( rs.getCurTime(), realtime-minTime);
	}
}
