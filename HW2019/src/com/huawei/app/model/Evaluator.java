package com.huawei.app.model;
//import com.huawei.app.Simulator.SimStatus;

public interface Evaluator {//判定器

	public int onScheduling(int carId, int curCrossId);
	

	/**
	 * 当前准备上路的车,根据路况返回是否可以上路
	 * remCars 表示当期模拟器中车辆的数量
	 * @return
	 */
	//public boolean onTryStart(int carId,int crossId,SimStatus ss);

	 //车辆正式上路时通知
	public boolean onStart(int carId, int crossId, int curRemCar);
	/**
	 * 表示car在curSAT到达路口crossId,结束行程
	 * remCars 表示当期模拟器中车辆的数量
	 * @return
	 */
	public boolean onStop(int carId, int crossId, int curSAT);
	
	
}
