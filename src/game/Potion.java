package game;

import java.io.Serializable;

public class Potion extends Item implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6168327003004627616L;
	
	private int effect,strength,duration;
	private String[] effects = {"Strength","Dexterity","Intellect","Charisma"};
	private boolean used = false;
	
	public Potion(Integer id, String name, Integer effect, Integer strength, Integer duration, Double weight, Integer cost){
		super(id,name,"",weight,cost);
		this.effect = effect.intValue();
		this.strength = strength.intValue();
		this.duration = duration.intValue();
	}
	/*public Potion(Potion another){
		this.ID = another.ID;
		this.name = another.name;
		this.effect = another.effect;
		this.strength = another.strength;
		this.duration = another.duration;
		this.weight = another.weight;
		this.cost = another.cost;
		used = false;
	}*/
	
	/* GETTERS */
	public int getEffect(){
		return effect;
	}
	public int getStrength(){
		return strength;
	}
	public int getDuration(){
		return duration;
	}
	public boolean isUsed(){
		return used;
	}
	public void showInfo(){
		String description = "";
		RPGMain.printText(true,"-------------------------");
		RPGMain.printText(true,"- " + name);
		RPGMain.printText(true,"-------------------------");
		try{
			description+="Increases " + effects[effect] + " by " + strength;
		}catch(ArrayIndexOutOfBoundsException e){
			description+="Increases HP by " + strength;
		}
		if(duration>0){
			description+=" for " + duration + " minutes";
		}
		description+=".";
		RPGMain.printText(true, description);
	}
	
	
	@Override
	public void use(){
		switch(effect){
		case -1: RPGMain.printText(true, "You gain " + strength + " HP."); RPGMain.speler.addHP(strength); break;
		default: RPGMain.printText(true, effects[effect]  + " increased by " + strength + " for " + duration + " minutes.");
				 RPGMain.speler.increaseStat(effect,strength);
				 new BuffTimer(); break;
		}
		used = true;
		try {
			Global.pauseProg();
		} catch (InterruptedException e) {}
	}
	
	class BuffTimer extends Thread{
		
		public BuffTimer(){
			start();
		}
		public void run(){
			try {
				//duration is in minuten
				Thread.sleep(duration*60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// decrease the property again
			RPGMain.printText(true, "The effect of " + name + " has worn off.");
			RPGMain.speler.increaseStat(effect,-strength);
		}
	}

}
