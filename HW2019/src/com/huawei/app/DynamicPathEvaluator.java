package com.huawei.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.huawei.app.Application.SkyEyes;
import com.huawei.app.model.*;
import com.huawei.app.model.Evaluator;

/**
 *
 *
 * 动态静态可实现的规划器模板
 *
 *	方法1（静态）：假设当前系统内无任何车辆时，为每辆车进行规划一条路线，
 *		所有车辆之后按照自己路线行驶
 *	方法2（轻微动态，整体静态）：所有车辆在上路前，根据当前系统车况初始化一条路线，之后车辆按照该路线行驶
 *	方法3（局部静态，整体动态）：所有车辆会保留若干时间前计算的一条路线，到达时间有限期后失效重新计算路线
 *	方法4（实时）：所有车辆在路口调度之前必须重新计算当前最优路线
 *	
 *	以上方法可以配合 onStart() 来限制在系统中车辆的数量
 *	
 *
 */
public class DynamicPathEvaluator implements Evaluator {

	private SkyEyes skyEyes;
	private Map<Integer,Road> roads;
    private Map<Integer,Cross> crosses;
    private Map<Integer,Integer> crossIdMap =null;
    private List<Integer> crossIdList =null;

	
    // 道路图
    private Road[][] roadNet = null;
    // 权重图
    private int[][] Cost = null;
    
    // 更新
    private int UPDATE_DELAY=10;
    
    // 当前系统时间
    private int curTime = -1;
    
	private Map<Integer, PathNode> roadNode =null;
    
    private class PathNode {
    	int curCrossId;
    	int nextRoadId;
    	PathNode next;
    	PathNode(int crossId, int roadId, PathNode next){
    		this.curCrossId=crossId;
    		this.nextRoadId=roadId;
    		this.next =next;
    	}
    }
    
    
    DynamicPathEvaluator(SkyEyes skyEyes) {
    	this.skyEyes = skyEyes;
		Map<Integer, Car> cars = skyEyes.cars;
    	roads= skyEyes.roads;//传入road文件中的道路信息
    	crosses= skyEyes.crosses;
    	createCrossIdx(crosses.keySet());//keySet()将所有cross文件包括的键值存入set（即crossId）
    	roadNode = new HashMap<>();
    }


	void init() {
		// 初始化道路网（数组roadNet[][]）
		roadNet = new Road[crosses.size()][crosses.size()];
		Cost = new int[crosses.size()][crosses.size()];
		roads.values().forEach(road->{
			int i= indexCross(road.getStartCrossId());//i,j为当前道路的起止点
			int j= indexCross(road.getEndCrossId());
			roadNet[i][j]=road;
			if(road.isDuplex()) 
				roadNet[j][i]=road;
		});
	}
	//将路口信息
	private void createCrossIdx(Collection<Integer> crossIds){
		Map<Integer,Integer> res = new HashMap<>();
		List<Integer> ids = crossIds.stream()
			.sorted(Integer::compare)
			.collect(Collectors.toCollection(ArrayList::new));
		for(int i=0;i<ids.size();i++) {
			res.put(ids.get(i), i);
		}
		crossIdList = ids;
		crossIdMap = res;
	}

	private void updateCost(int speed) {
		for(int i = 0; i< Cost.length; i++)
			for(int j = 0; j< Cost.length; j++) {
				if(roadNet[i][j]==null) Cost[i][j] = Integer.MAX_VALUE;
				else {
					Road road = roadNet[i][j];
					Cost[i][j]=cost(road,speed);
				}
			}
	}
	
	
	/**
	 * idx
	 * @param crossId
	 * @return
	 */
	private int indexCross(int crossId) {
		return crossIdMap.get(crossId);
	}//返回index

	
	private int crsId(int index) {
		return crossIdList.get(index);
	}
	
	/**
	 * 计算车辆通过这条路段需要花费的代价=畅通（当前车速跑全程）时的最小代价+车的数量
	 *@params  [road, speed]
	 *@return  int
	 *@author  handoking
	 *@date  2019/3/15
	 */
	private int cost(Road road,int speed) {
		int curSpeed = Math.min(road.getMaxRoadSpeed(),speed);
		int minCost = (int)Math.ceil(road.getRoadLength()*1.0/curSpeed);
		int cout = 0;
		CarAttr[] carAttr = null;
		Channel[] channel = road.getOutCrossChannels(road.getStartCrossId());//初始化后的正向车道信息数组
		for(Channel cl :channel) {
			carAttr = cl.getChanel();//以车道长度为数组size创建车辆属性数组
			for(CarAttr ca:carAttr)
				if(ca!=null&&ca.carId>=0)cout++;
		}
		if(road.isDuplex()) {
			channel = road.getInCrossChannels(road.getEndCrossId());//初始化后的反向车道信息数组（包括channelId,maxSpeed,车道长度）
			for(Channel rc :channel) {
				carAttr = rc.getChanel();
				for(CarAttr ca:carAttr)
					if(ca!=null&&ca.carId>=0)cout++;
			}
			cout = cout/2;
		}

		return minCost+cout;
	}
	
	
	private PathNode dij(Car car, int startPoint, int endPoint) {
		
		if(startPoint==endPoint)
			System.err.println(car.getEndPoint()+" "+car.getStartPoint());
		if(car==null||startPoint==endPoint)
			throw new IllegalArgumentException("car==null or startPoint==endPoint");
		int[] dist = new int[crosses.size()];
		int[] path = new int[crosses.size()];

		int oriIndex = indexCross(startPoint);
		int desIndex = indexCross(endPoint);
		
		// 初始化代价
		Set<Integer> set = new HashSet<>(crosses.size());		
		for(int i=0;i<crosses.size();i++)
			if(i!=oriIndex) set.add(i);
		Arrays.fill(path, -1);
		for(int i = 0; i< Cost.length; i++) {
			dist[i]= Cost[oriIndex][i];
			if(roadNet[oriIndex][i]!=null)path[i]=oriIndex;
		}

		for(int i=1;i<crosses.size();i++) {
			int tmp=Integer.MAX_VALUE;
			int k = -1;
			for(int v:set) {
				if(dist[v]<tmp) {
					tmp= dist[v];
					k=v;
				}
			}
			if(k<0) throw new IllegalArgumentException("k<0!");
			if(k==desIndex) break;// 已经寻找到结尾
			set.remove(k);
			for(int v:set) {
				if(Cost[k][v]<Integer.MAX_VALUE&&
						(tmp=dist[k]+ Cost[k][v])<dist[v]) {
					dist[v]=tmp;
					path[v]=k;//更新父节点
				}
			}
		}// end 
		
		// 路径恢复
		// 创建一个尾节点
		PathNode next = new PathNode(crsId(desIndex),-1,null);
		int par;
		int son=desIndex;
		while((par=path[son])!=oriIndex) {
			next = new PathNode(crsId(par),
					roadNet[par][son].getRoadId(),next);
			son=par;
		}
		next = new PathNode(crsId(par),
				roadNet[par][son].getRoadId(),next);
		
		return next;	
	}
	
	
	@Override
	public int onScheduling(int carId, int curCrossId) {
		
		CarAttr cs = skyEyes.statues.get(carId);
		Car car = cs.car;
		// 注意已经到达目的地，返回-1
		if(curCrossId==car.getEndPoint())
			return -1;
		
		PathNode cur;
		if((cur= roadNode.get(carId))==null) {
			updateCost(car.getMaxSpeed());
			cur = dij(car,car.getStartPoint(),car.getEndPoint());
			roadNode.put(carId, cur);
		}
		while(cur!=null&&cur.curCrossId!=curCrossId)
			cur=cur.next;
		if(cur==null) 
			throw new IllegalArgumentException("CrossId:"+curCrossId+" is not in carpath");
			
		return cur.nextRoadId;
	}
	
	@Override
	public boolean onStart(int carId, int crossId, int remCars) {
		// TODO Auto-generated method stub
		return remCars<300;
	}
	
	@Override
	public boolean onStop(int carId, int crossId, int curSAT) {
		// TODO Auto-generated method stub
		System.err.println("Car:"+carId+"->Cross:"+crossId+"->time:"+curSAT);
		return false;
	}
	
	public String showPath(int carId) {
		StringBuffer sb = new StringBuffer();
		PathNode node = roadNode.get(carId);
		while(node!=null) {
			sb.append("("+node.curCrossId+","+node.nextRoadId+")->");
			node=node.next;
		}
		return sb.toString();
	}
	

}
