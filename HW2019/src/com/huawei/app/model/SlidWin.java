package com.huawei.app.model;

public class SlidWin {
	private int sum;
	private long n;
	private int[] arr;
	private int culLen;
	/**
	 *
	 *@params  [size]
	 *@author  handoking
	 *@date  2019/3/29
	 */
	SlidWin(int size){
		culLen = size>0?size:1;
		sum = 0;
		n =0;
		arr = new int[size];
	}
	void add(int v) {
		sum+=v;
		int i =(int) (n % culLen);
		arr[i] += v;
	}
	double getAvg() {
		if(n < culLen)
			return (sum*1.0/(n +1));
		return sum*1.0/ culLen;
	}
	
	void next() {
		n++;
		if(n >= culLen) {
			sum-= arr[(int) (n % culLen)];
			arr[(int) (n % culLen)]=0;
		}
	}
	
	public static void main(String[] args) {
		SlidWin sw = new SlidWin(3);
		System.out.println(sw.getAvg());
		sw.add(1);
		System.out.println(sw.getAvg());
		sw.next();
		System.out.println(sw.getAvg());
		sw.add(2);
		System.out.println(sw.getAvg());
		sw.next();
		System.out.println(sw.getAvg());
		sw.add(3);
		System.out.println(sw.getAvg());
		sw.next();
		System.out.println(sw.getAvg());
		sw.add(4);
		System.out.println(sw.getAvg());
		sw.next();
	}

}
