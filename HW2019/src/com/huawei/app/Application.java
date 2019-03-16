package com.huawei.app;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.huawei.app.model.Answer;
import com.huawei.app.model.Car;
import com.huawei.app.model.CarAttr;
import com.huawei.app.model.Cross;
import com.huawei.app.model.Road;

public class Application {

	static class SkyEyes {
	    //天眼能看到所有的状况，包括车况，路况等
		
	    Map<Integer,Car> cars = null;
	    Map<Integer,Road> roads = null;
	    Map<Integer,Cross> crosses = null;
		Map<Integer, CarAttr>  statues=null;
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
        
        System.out.println("load finished!\ncars.size="+eyes.cars.size()+
        		"\nroads.size="+eyes.roads.size()+"\ncrosses.size="+eyes.crosses.size());
        
        Instant now = Instant.now();
        
        // 完成cars、roads、crosses的一些基础工作
        preprocess(eyes);
        // 创建规划器
//        StaticPathEvaluator evaluator = new StaticPathEvaluator(ctx);
        DynamicPathEvaluator evaluator = new DynamicPathEvaluator(eyes);
        // 创建模拟器
        Simulator sim = new Simulator(eyes);
//        BlockSimulator sim = new BlockSimulator(ctx);
        // 注册规划器
        sim.registerPlanner(evaluator);
        // 初始化
        evaluator.init();
        sim.init();
//        ctx.cars.keySet().forEach(v->{
//        	System.out.println(v+":"+evaluator.showPath(v));
//        });
        // 运行模拟器产生运行结果
        sim.run();

        long runingtime = Duration.between(now, Instant.now()).toMillis();
        
        System.out.println("running time:"+runingtime);
        
        // 记录所有车辆的行程
        InputFormat.saveAnswer(answerPath,  eyes.statues.values());
	}
	
	
public static void rerun(String[] args) {
		
        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];
        
        System.out.println("carPath = " + carPath + "\nroadPath = " + roadPath +
        		"\ncrossPath = " + crossPath + "\nanswerPath = " + answerPath);
        
        SkyEyes skyEyes = new SkyEyes();
        
        skyEyes.cars =
        		InputFormat.formatCars(InputFormat.loadAndFormat(carPath));
        skyEyes.roads =
        		InputFormat.formatRoad(InputFormat.loadAndFormat(roadPath));
        skyEyes.crosses =
        		InputFormat.formatCross(InputFormat.loadAndFormat(crossPath));
        
        Map<Integer,Answer> answers = 
        		InputFormat.formatAnswer(InputFormat.loadAndFormat(answerPath));
        
        System.out.println("load finished!\ncars.size="+skyEyes.cars.size()+
        		"\nroads.size="+skyEyes.roads.size()+"\ncrosses.size="+skyEyes.crosses.size());
        
        // 重新设置上路时间
        skyEyes.cars.values().forEach(car->{
        	car.setStartTime(answers.get(car.getCarId()).getStartTime());
        });
        
        
        Instant now = Instant.now();
        
        // 完成cars、roads、crosses的一些基础工作
        preprocess(skyEyes);
        // 创建规划器
//        StaticPathEvaluator evaluator = new StaticPathEvaluator(ctx);
        ReRunPathEvaluator planner = new ReRunPathEvaluator(skyEyes);
        // 创建模拟器
        Simulator sim = new Simulator(skyEyes);
//        BlockSimulator sim = new BlockSimulator(ctx);
        // 注册规划器
        sim.registerPlanner(planner);
        // 初始化
        planner.init(answers);
        sim.init();
//        ctx.cars.keySet().forEach(v->{
//        	System.out.println(v+":"+evaluator.showPath(v));
//        });
        // 运行模拟器产生运行结果
        sim.run();

        long runingtime = Duration.between(now, Instant.now()).toMillis();
        
        System.out.println("running time:"+runingtime);
        

	}
	
	
	private static void preprocess(SkyEyes eye) {
		
		// 设置所有Cross中可以驶出的roadId
		eye.crosses.values()
		.forEach(v->v.setConnOutRoadIds(eye.roads));
	}
	
	
}
