package com.huawei.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.huawei.app.model.*;
import com.huawei.app.model.KunFighter;

public class InputFormat {

	
	/**
	 *  >加载文件 并去除文件中的#()字符 
	 * @param path
	 * @return
	 */
	public static List<String> loadAndFormat(String path){
		List<String> res = null;
		List<String> rs = new ArrayList<String>();
		try {
			res =Files.readAllLines(Paths.get(path),StandardCharsets.UTF_8);

			for(String item : res) {
				if(!item.contains("#") && item.length() >2) {
					String temp = item;
					temp = temp.replace("(","");
					temp = temp.replace(")", "");
					temp = temp.replace(" ","");
					rs.add(temp);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	
	static Map<Integer, KunFighter> formatCars(List<String> lines){
		if(lines==null)
			return null;
		Map<Integer, KunFighter> res = new HashMap<>();

		for(String line : lines) {
			String [] temp = line.split(",");
			int[] param = strs2ints(temp);
			res.put(param[0], new KunFighter(param));

		}
		return res;
	}
	
	
	static  Map<Integer, Route> formatRoad(List<String> lines){
		if(lines==null)
			return null;
		Map<Integer, Route> res = new HashMap<>();
		for(String line : lines) {
			String [] temp = line.split(",");
			int[] param = strs2ints(temp);
			res.put(param[0], new Route(param));

		}
		return res;
	}
	
	
	static Map<Integer, Hub> formatCross(List<String> lines){
		if(lines==null) return null;
		Map<Integer, Hub> res = new HashMap<>();
		for(String line : lines) {
			String [] temp = line.split(",");
			int[] param = strs2ints(temp);
			res.put(param[0], new Hub(param));

		}
		return res;
	}
	
	public static Map<Integer,Answer> formatAnswer(List<String> lines){
		if(lines==null) return null;
		Map<Integer,Answer> res = new HashMap<>();
		for(String line : lines) {
			String [] temp = line.split(",");
			int[] param = strs2ints(temp);
			res.put(param[0], new Answer(param));

		}
		return res;
	}
	
	
	
	private static int[] strs2ints(String[] ss) {
		int[] res = new int[ss.length];
		for(int i=0;i<ss.length;i++) 
			res[i]=Integer.parseInt(ss[i]);
		return res;
	}
	
	
	

	private final static String firstLine = "#(kunId,StartTime,RoadId...)";
	static void PrintAnswer(String path, Collection<FighterStatus> cars) {
		try {
			File f  = Paths.get(path).toFile();
			//if(!f.exists()) f.createNewFile();
			try(BufferedWriter bw  
					= new BufferedWriter(new FileWriter(f))){
				bw.write(firstLine+"\n");
				
				for(FighterStatus c:cars)bw.write(c.printRoute()+"\n");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


}
