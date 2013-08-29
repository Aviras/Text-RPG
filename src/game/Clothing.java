package game;

import java.io.Serializable;

public class Clothing extends Item implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String area;
	private int warmth;
	
	public Clothing(Integer id, String name, String description, Double weight,Integer cost, String area, Integer warmth){
		super(id,name,description,weight,cost);
		this.area = area;
		this.warmth = warmth;
	}
	public String getArea(){
		return area;
	}
	public int getWarmth(){
		return warmth;
	}
	public void setWarmth(int x){
		warmth = x;
	}
	public void showInfo(){
		RPGMain.printText(true,"-------------------------");
		RPGMain.printText(true,"- " + name);
		RPGMain.printText(true,"-------------------------");
		RPGMain.printText(true, "Area: " + area);
		RPGMain.printText(true, "Warmth: " + warmth);
		RPGMain.printText(true, "Weight: " + weight);
		RPGMain.printText(true, "Cost: " + cost);
	}

}
