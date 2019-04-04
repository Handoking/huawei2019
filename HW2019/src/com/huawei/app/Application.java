package com.huawei.app;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.huawei.app.model.KunFighter;
import com.huawei.app.model.FighterStatus;
import com.huawei.app.model.Hub;
import com.huawei.app.model.Route;

public class Application {

	static class SkyEyes {
	    //天眼能看到所有的状况，包括机况，路况等
		
	    Map<Integer, KunFighter> cars = null;
	    Map<Integer, Route> roads = null;
	    Map<Integer, Hub> crosses = null;
		Map<Integer, FighterStatus> flag =null;
	}
	
	public static void run(String[] args) {
		
        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];
        System.out.println("carPath = " + carPath + "\nroadPath = " + roadPath +
        		"\ncrossPath = " + crossPath + "\nanswerPath = " + answerPath);
        SkyEyes eyes = new SkyEyes();
        eyes.cars = InputFormat.formatCars(InputFormat.loadAndFormat(carPath));
        eyes.roads = InputFormat.formatRoad(InputFormat.loadAndFormat(roadPath));
        eyes.crosses = InputFormat.formatCross(InputFormat.loadAndFormat(crossPath));

        
        Instant now = Instant.now();

        preprocess(eyes);
        // 艾达
        Ada ada = new Ada(eyes);
        // 空中管制规则
        SHIELD SHIELD = new SHIELD(eyes);
        // 备案判定器
        SHIELD.recordEvaluator(ada);
        // 初始化
        ada.routeInit();
        SHIELD.init();
        // 运行
        SHIELD.run();
        System.out.println("kunFighters.size="+eyes.cars.size()+
                "\nroutes.size="+eyes.roads.size()+"\nhubs.size="+eyes.crosses.size());
        long runingTime = Duration.between(now, Instant.now()).toMillis();
        
        System.out.println("running time:"+runingTime);


        InputFormat.PrintAnswer(answerPath,  eyes.flag.values());
	}
	
	private static void preprocess(SkyEyes eye) {
		
		// 从Hub中发出的route
        for(Hub i:eye.crosses.values()){
            i.setOutRouteIds(eye.roads);
        }
	}
	

}
