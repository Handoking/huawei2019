package com.huawei;

import com.huawei.app.Application;

public class MainTest {

	private static String basePath = "C:\\Users\\Administrator\\Desktop\\华为\\SDK\\SDK_java\\bin\\map6";
    //private static String basePath = "C:\\Users\\Administrator\\Desktop\\华为\\SDK\\SDK_java\\bin";
    private static String carPath = basePath+"/car.txt";
    private static String roadPath = basePath+"/road.txt";
    private static String crossPath = basePath+"/cross.txt";
    private static String answerPath = basePath+"/answer.txt";
    
	public static void main(String[] args) {
		Application.run(new String[]{carPath,roadPath,crossPath,answerPath});
		
	}

}
