package com.huawei.app.model;
import com.huawei.app.SHIELD.RoutesStatus;

public interface Evaluator {//状态判定

	int Scheduling(int kunId, int curCrossId, RoutesStatus ss);
	boolean PrepFly(int kunId, int hubId, RoutesStatus ss);

	/**
	 * 车辆正式上路状态
	 *@params  [kunId, hubId, ss]
	 *@return  void
	 *@author  handoking
	 *@date  2019/3/20
	 */
	void Flying(int kunId, int hubId, RoutesStatus ss);

	/**
	 * 通过hub时，通知ada
	 *@params  [kunId, curCrossId, ss]
	 *@return  void
	 *@author  handoking
	 *@date  2019/3/20
	 */
	void PassedHub(int kunId, int curCrossId, RoutesStatus ss);


	/**
	 * 准备通过hub时，返回给ada
	 *@params  [kunId, roaId, ss]
	 *@return  void
	 *@author  handoking
	 *@date  2019/3/20
	 */

	void nearHub(int kunId, int roaId, RoutesStatus ss);

	void Landed(int kunId, int hubId, RoutesStatus ss);



}
