package game;

import java.io.Serializable;

public class Consumable extends Item implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String type;
	protected int effect,freshness,quantity;
	protected boolean used;
	
	public Consumable(Integer id,String name, String description, Double weight, Integer cost,String type, Integer effect,Integer freshness){
		super(id,name,description,weight,cost);
		this.type = type;
		this.effect = effect;
		this.freshness = freshness;
		quantity = 100;
		used = false;
	}
	public Consumable(Integer id,String name,String description, Double weight, Integer cost, String type, Integer effect, String logbookPath, String logbookSummary){
		super(id,name,description,weight,cost);
		this.type = type;
		this.effect = effect;
		this.logbookSummary = logbookSummary;
		this.logbookPath = logbookPath;
		quantity = 100;
		used = false;
	}
	public Consumable(Consumable another){
		this.ID = another.ID;
		this.name = another.name;
		this.description = another.description;
		this.weight = another.weight;
		this.cost = another.cost;
		this.type = another.type;
		this.effect = another.effect;
		this.freshness = another.freshness;
		this.quantity = another.quantity;
		this.logbookSummary = another.logbookSummary;
		this.logbookPath = another.logbookPath;
		used = false;
	}
	@Override
	public String getName(){
		if(freshness > 0){
			return name + " (" + quantity + "%)";
		}
		else if(freshness > -5){
			return name + " (Overdue, " + quantity + "%)";
		}
		else if(freshness > -24){
			return name + " (Really overdue, " + quantity + "%)";
		}
		else {
			return "Rotting " + name  + " (" + quantity + "%)";
		}
	}
	public void decrFreshness(int x){
		freshness-=x;
	}
	public void addQuantity(int i){
		quantity = Math.min(100, Math.max(0,quantity+i));
	}
	public int getQuantity(){
		return quantity;
	}
	public int getFreshness(){
		return freshness;
	}
	public String getType(){
		return type;
	}
	public int getEffect(){
		return effect;
	}
	public boolean isUsed(){
		return used;
	}
	public void showInfo(){
		RPGMain.printText(true,"-------------------------");
		RPGMain.printText(true,"- " + getName());
		RPGMain.printText(true,"-------------------------");
		RPGMain.printText(true, description);
		if(freshness > 0){
			RPGMain.printText(true, "Fresh for " + freshness + " hours.");
		}
		else if(freshness > -5){
			RPGMain.printText(true, "It doesn't look so good anymore..");
		}
		else if(freshness > -24){
			RPGMain.printText(true, "I think I best not eat this.");
		}
		else{
			RPGMain.printText(true, "Any appetite you had, you just lost looking at this. " + 
					name + " is now covered with mould, smelling of decay. " +
				"It would be wise to throw it away before it starts to impair your other supplies.");
		}
	}
	public void use(){
		int percent = 0;
		while(true){
			RPGMain.printText(false, "How much % of it would you like to consume?\n>");
			try{
				percent = Integer.parseInt(RPGMain.waitForMessage());
				percent = Math.max(0,Math.min(quantity, percent));
				quantity-=percent;
				break;
			}catch(NumberFormatException e){
				continue;
			}catch(InterruptedException e){
				continue;
			}
		}
		int finalEffect = (int)(effect*percent/100.0);
		RPGMain.printText(false, "You consume the " + name);
		if(freshness > 0){
			RPGMain.printText(true, ", and you feel replenished. Your " + type + " seems less urgent now. " +
					"(" + finalEffect + "%)");
		}
		else if(freshness > -5){
			finalEffect /= 2;
			RPGMain.printText(true, ". It doesn't taste nor look that great anymore, " +
					"but it's better than nothing. Your " + type + " seems less urgent now. (" + finalEffect + "%)");
		}
		else if(freshness > -24){
			finalEffect /= -2;
			RPGMain.printText(true, ", be it hesitant. Immediatly afterwards you realize it wasn't such a good idea" + 
					" after all. Now your " + type + " seems to have only gotten worse. (" + finalEffect + "%");
		}
		else{
			finalEffect *=(freshness/24);
			RPGMain.printText(true, "...From the first bite you knew this was a terrible idea, but you keep going anyway." +
					" Ten seconds later you can't take it any more, and there you are, spewing on the ground, and more drained" +
					" than you were before.");
			RPGMain.speler.addHP(-RPGMain.speler.getHP()/4);
			if(type.equalsIgnoreCase("hunger")){
				RPGMain.speler.addThirst(-finalEffect/2);
				RPGMain.speler.addFitness(finalEffect/2);
			}
			else if(type.equalsIgnoreCase("thirst")){
				RPGMain.speler.addHunger(-finalEffect/2);
				RPGMain.speler.addFitness(finalEffect/2);
			}
			else if(type.equalsIgnoreCase("fitness")){
				RPGMain.speler.addHunger(-finalEffect/2);
				RPGMain.speler.addThirst(-finalEffect/2);
			}
		}
		
		if(type.equalsIgnoreCase("hunger")){
			RPGMain.speler.addHunger(-finalEffect);
		}
		else if(type.equalsIgnoreCase("thirst")){
			RPGMain.speler.addThirst(-finalEffect);
		}
		else if(type.equalsIgnoreCase("fitness")){
			RPGMain.speler.addFitness(finalEffect);
		}
		//flasks can be refilled
		if(quantity <= 0 && !name.toLowerCase().contains("flask")){
			quantity = 0;
			used = true;
		}
		
		try {
			Global.pauseProg();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//TODO idea: instead of instant effect of eating bad food, make a timer, so that you get consequences afterwards, like not being able to move in combat, or getting a debuff
	}

}
