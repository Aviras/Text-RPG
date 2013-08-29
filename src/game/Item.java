package game;

import java.io.Serializable;

public class Item implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4944628931569599521L;
	
	protected int ID,cost;
	protected double weight;
	protected String name,description,type,logbookPath,logbookSummary;
	
	public Item(){}
	
	public Item(Integer id,String name,String description,Double weight,Integer cost){
		ID = id.intValue();
		this.description = description;
		this.cost = cost.intValue();
		this.name = name;
		this.weight = weight.doubleValue();
	}
	public Item(Integer id,String name,String description,Double weight,Integer cost, String logbookPath, String logbookSummary){
		ID = id.intValue();
		this.description = description;
		this.cost = cost.intValue();
		this.name = name;
		this.weight = weight.doubleValue();
		this.logbookPath = logbookPath;
		this.logbookSummary = logbookSummary;
	}
	public Item(Item another){
		this.ID = another.ID;
		this.cost = another.cost;
		this.weight = another.weight;
		this.name = another.name;
		this.description = another.description;
		this.logbookPath = another.logbookPath;
		this.logbookSummary = another.logbookSummary;
	}
	public int getID(){
		return ID;
	}
	public String getName(){
		return name;
	}
	public int getStrength(){
		return 0;
	}
	public double getWeight(){
		return weight;
	}
	public int getCost(){
		return cost;
	}
	public String getLogbookPath(){
		return logbookPath;
	}
	public String getLogbookSummary(){
		return logbookSummary;
	}
	public void showInfo(){
		RPGMain.printText(true,"-------------------------");
		RPGMain.printText(true,"- " + name);
		RPGMain.printText(true,"-------------------------");
		RPGMain.printText(true, description);
	}
	public String getDescription(){
		return description;
	}
	public void use() throws InterruptedException{
		RPGMain.printText(true, "This item seems to have no immediate purpose.");
	}
}
