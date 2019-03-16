package com.huawei.app.model;
public class Road {

	private Integer roadId;
	private int roadLength;
	private int maxRoadSpeed;
	private int channelNum;
	private int startCrossId;// 起始路口
	private int endCrossId;// 终点路口
	private int isDuplex;// 是否为双向通道
	

	
	// 默认单车道的正向车道，如果双向也作为正向车道
	private Channel[] positiveChannels = null;
	
	// 双向通道时被使用
	private Channel[] reverseChannels = null;

	/**
	 * 构造函数
	 *@params  [roadId, roadLength, maxRoadSpeed, channelNum, startCrossId, endCrossId, isDuplex]
	 *@return
	 *@author  handoking
	 *@date  2019/3/15
	 */
	public Road(int roadId, int roadLength, int maxRoadSpeed,
				int channelNum, int startCrossId, int endCrossId, int isDuplex) {
		this.roadId=roadId;this.roadLength=roadLength;
		this.maxRoadSpeed = maxRoadSpeed;this.channelNum = channelNum;
		this.startCrossId = startCrossId;this.endCrossId = endCrossId;
		this.isDuplex = isDuplex;
	}
	/**
	 *@params  [args]
	 *@return
	 *@author  handoking
	 *@date  2019/3/15
	 */
	public Road(int[] args) {
		this.roadId=args[0];this.roadLength=args[1];
		this.maxRoadSpeed =args[2];this.channelNum =args[3];
		this.startCrossId =args[4];this.endCrossId =args[5];
		this.isDuplex =args[6];
	}	
	
	
	public boolean isDuplex() {
		return this.isDuplex ==1;
	}
	
/**
 * （当前道路入口的信息）
 *@params  [crossId]
 *@return  com.huawei.app.model.Channel[]
 *@author  handoking
 *@date  2019/3/15
 */
	public Channel[] getInCrossChannels(int crossId) {
		
		if(isDuplex()&& startCrossId ==crossId) {
			// 双向通道道路，并且当前在入口
			if(reverseChannels ==null) initReverseChannels();
			return reverseChannels;
		}
		if(endCrossId !=crossId)//如果此道路出口也不为当前路口，那就发生了错误
			throw new IllegalArgumentException("endCrossId !=crossId err "+crossId);
		if(positiveChannels==null) initPositiveChannels();
		
		return positiveChannels;
	}
	
	/**
	 *  道路出口，返回当前道路的道路信息。
	 * @param crossId
	 * @return
	 */
	public Channel[] getOutCrossChannels(int crossId) {
		
		if(isDuplex()&& endCrossId ==crossId) {
			// 双向通道时初始化反向车道
			if(reverseChannels ==null) initReverseChannels();
			return reverseChannels;
		}
		
		if(startCrossId !=crossId)
			throw new IllegalArgumentException("startCrossId !=crossId err "+crossId);
		if(positiveChannels==null) initPositiveChannels();
		return positiveChannels;		
		
	}
	
/**
 *@params  [oneCrossId]
 *@return  int
 *@author  handoking
 *@date  2019/3/15
 */
	public int getAnotherCrossId(int oneCrossId) {
		if(startCrossId ==oneCrossId) return endCrossId;
		else if(endCrossId ==oneCrossId) return startCrossId;
		else 
			throw new IllegalArgumentException(oneCrossId+" not in Road "+roadId);
	}
	
	/**
	 * 初始化创建正向车道
	 *@params  []
	 *@return  void
	 *@author  handoking
	 *@date  2019/3/15
	 */
	private void initPositiveChannels() {
		positiveChannels= new Channel[channelNum];
		for(int i = 0; i< channelNum; i++)
			positiveChannels[i]= new Channel(roadId, i, maxRoadSpeed, roadLength);
	}
/**
 * 初始化创建反向车道
 *@params  []
 *@return  void
 *@author  handoking
 *@date  2019/3/15
 */
	private void initReverseChannels() {
		reverseChannels = new Channel[channelNum];
		for(int i = 0; i< channelNum; i++)
			reverseChannels[i]= new Channel(roadId, i, maxRoadSpeed, roadLength);
	}

	public Integer getRoadId() {
		return roadId;
	}

	public int getRoadLength() {
		return roadLength;
	}

	public int getMaxRoadSpeed() {
		return maxRoadSpeed;
	}

	public int getChannelNum() {
		return channelNum;
	}

	public int getStartCrossId() {
		return startCrossId;
	}

	public int getEndCrossId() {
		return endCrossId;
	}

	public int getIsDuplex() {
		return isDuplex;
	}
}
