package game;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

public abstract class Wezen implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;
	protected int HP, maxHP, goud,strength,dexterity,intellect,charisma;
	protected double level;
	protected String naam,stance = "defensive";
	protected boolean dood, knockedDown;
	protected double movement = 3.5;
	protected LinkedHashMap<Item,Integer> inventory;
	protected ArrayList<Spell> spreuken = new ArrayList<Spell>();
	protected double[] statIncrease = new double[4];
	protected int[] resistance = new int[4];
	protected int initWeight = 15;
	protected int[] defense = new int[3];
	protected Equipment boog;
	protected Equipment wapen;
	protected Equipment schild;
	protected Equipment helm,harnas,handschoenen,broek,schoenen;
	protected Equipment nothing = new Equipment(-1,"Nothing",0,0,0,0,0.0);
	protected Clothing mantle,shirt,pants;
	protected Clothing noClothing = new Clothing(-1,"Nothing","",0.0,0,null,0);
	protected Munition arrow = new Munition(-1,"Arrow","A simple wooden arrow.",0.08,1,3);
	protected double dimMov = 0;
	protected HashMap<Ability,Integer> abilities = new HashMap<Ability,Integer>();
	protected ArrayList<Buff> buffs = new ArrayList<Buff>();
	
	private transient Logger logger = Logger.getLogger(Wezen.class);
	
	
	public Wezen(double lvl, String name, int hp, int gold,int strength,int dexterity,int intellect,int charisma){
		level = lvl;
		HP = hp;
		goud = gold;
		naam = name;
		maxHP = hp;
		this.strength = strength;
		this.dexterity = dexterity;
		this.intellect = intellect;
		this.charisma = charisma;
		dood = false;
		knockedDown = false;

		inventory = new LinkedHashMap<Item,Integer>();
	}
	public Wezen(){
		
	}
	
	public void updateHPBar(){
	}
	
	public HashMap<Ability,Integer> getAbilities(){
		return abilities;
	}
	
	/*
	 * 
	 * SPELL METHODS
	 * 
	 */
	
	public boolean addSpreuk(Spell spreuk){
		
		for(int j=0;j<spreuken.size();j++){
			if(spreuken.get(j) == spreuk){
				RPGMain.printText(true,"You have already learned this spell.");
				return false;
			}
			if(spreuken.get(j) == null){
				spreuken.add(spreuk);
				return true;
			}
		}
		return false;
	}
	public int getAantalSpreuken(){
		int aantal = 0;
		for(int j = 0;j<spreuken.size();j++){
			if(spreuken.get(j) != null) aantal++;
		}
		return aantal;
	}
	public Spell getSpreuk(int index){
		return spreuken.get(index);
	}
	public Spell getSpreuk(String naam){
		for(int j = 0;j<spreuken.size();j++){
			if(spreuken.get(j).getName().equalsIgnoreCase(naam)){
				return spreuken.get(j);
			}
		}
		return null;
	}
	
	/*
	 * 
	 * INVENTORY METHODS
	 * 
	 */
	public Item getInventoryItem(int index){
		int j=0;
		for(Item i:inventory.keySet()){
			if(j == index){
				return i;
			}
			j++;
		}
		return null;
	}
	public Item getInventoryItem(String name){
		for(Item i:inventory.keySet()){
			if(i.getName().equalsIgnoreCase(name)){
				return i;
			}
		}
		return null;
	}
	public int getInventoryItemIndex(String name){
		int j=0;
		for(Item i:inventory.keySet()){
			if(i.getName().equalsIgnoreCase(name)){
				return j;
			}
			j++;
		}
		return -1;
	}
	public int getItemNumber(String name){
		for(Item i:inventory.keySet()){
			if(i.getName().equalsIgnoreCase(name)){
				return inventory.get(i);
			}
		}
		return 0;
	}
	public int getItemNumber(Item i){
		return inventory.get(i);
	}
	public boolean hasItem(String name){
		if(getInventoryItemIndex(name) == -1){
			return false;
		}
		return true;
	}
	public int getInventorySize(){
		return inventory.size();
	}
	public void addInventoryItem(Item item, int number){
		for(int i=0;i<number;i++){
			addInventoryItem(item);
		}
	}
	public void addInventoryItem(Item item){
		if(getInventorySpace() >= item.getWeight()){
			//TODO

			if(item instanceof Equipment){
				Equipment e = new Equipment((Equipment)item);
				inventory.put(e, 1);
			}
			else if(item instanceof Consumable){
				Consumable c = new Consumable((Consumable)item);
				inventory.put(c, 1);
			}
			else if(item instanceof Herb){
				Herb h = new Herb((Herb)item);
				//TODO add required item
				if(hasItem("")){
					h.setQuality((int)Math.round(2+Math.random()));
				}
				else{
					h.setQuality((int)Math.round(0.4+Math.random()));
				}
				inventory.put(h, 1);
			}
			else{
				if(hasItem(item.getName())){
					//Since item is different instance of what is already in there, if you would just add the item, you'd have the original with the original amount,
					//PLUS the new item with 1+original amount, thereby duplicating items
					inventory.put(this.getInventoryItem(item.getName()), getItemNumber(item.getName())+1);
				}
				else{
					inventory.put(item,1);
				}
			}
			try{
				Logbook.addContent(item.getLogbookPath(), 1, item.getLogbookSummary());
			} catch(Exception e){
				e.printStackTrace();
				logger.error(e);
			}
			try{
				GameFrameCanvas.updatePlayerInfoTable();
			}catch(Exception e){
			}
			if(RPGMain.speler != null){
				for(Quest q: RPGMain.speler.getQuestLog()){
					try{
						q.checkProgress(item.getClass().getName().split("\\.")[1],item.getID());
					}catch(ArrayIndexOutOfBoundsException e){
						e.printStackTrace();
						continue;
					} catch(Exception e){
						e.printStackTrace();
						logger.error(e);
					}
				}
			}
		}
		else{
			RPGMain.printText(true, "You are too heavily loaded.");
		}
	}
	public void delInventoryItem(Item i){
		inventory.put(i, inventory.get(i)-1);
		if(inventory.get(i) <= 0){
			inventory.remove(i);
		}
	}
	public void delInventoryItem(String s, int number){
		delInventoryItem(getInventoryItem(s),number);
	}
	public void delInventoryItem(Item item, int number){
		for(int i=0;i<number;i++){
			delInventoryItem(item);
		}
	}
	public void delInventoryItem(int index){
		Item i = getInventoryItem(index);
		inventory.put(i, inventory.get(i)-1);
		if(inventory.get(i) <= 0){
			inventory.remove(i);
		}
	}
	public void delInventoryItem(int index, int number){
		delInventoryItem(getInventoryItem(index),number);
	}
	public double getInventorySpace(){
		double g = getMaxWeight();
		for(Item e: inventory.keySet()){
			g-=(e.getWeight()*inventory.get(e));
		}
		return g;
	}
	
	/* GETTERS/SETTERS */
	
	public Equipment getEquipped(int type){
		switch(type){
		case 1: return wapen;
		case 2: return wapen;
		case 3: return schild;
		case 4: return boog;
		case 5: return helm;
		case 6: return harnas;
		case 7: return handschoenen;
		case 8: return broek;
		case 9: return schoenen;
		default: return null;
		}
	}
	public double getMaxWeight(){
		return initWeight + strength/2.0;
	}
	public void setAbilityCooldown(Ability a, int cd){
		abilities.put(a, cd);
	}
	public void decrAllAbilitiesCooldown(){
		for(Ability a: abilities.keySet()){
			abilities.put(a,Math.max(abilities.get(a)-1,0));
		}
	}
	public void setKnockedDown(boolean b){
		knockedDown = b;
	}
	public boolean getKnockedDown(){
		return knockedDown;
	}
	public Clothing getMantle(){
		return mantle;
	}
	public Clothing getShirt(){
		return shirt;
	}
	public Clothing getPants(){
		return pants;
	}
	public void setMantle(Clothing c){
		if(c.getArea().equalsIgnoreCase("mantle")){
			mantle = c;
		}
	}
	public void setShirt(Clothing c){
		if(c.getArea().equalsIgnoreCase("shirt")){
			shirt = c;
		}
	}
	public void setPants(Clothing c){
		if(c.getArea().equalsIgnoreCase("pants")){
			pants = c;
		}
	}
	public String getStance(){
		return stance;
	}
	public void setStance(String s){
		stance = s;
	}
	public double getStanceAttackModifier(){
		if(stance.equalsIgnoreCase("offensive")){
			return 1.3;
		}
		return 1.0;
	}
	public double getStanceDefenseModifier(){
		if(stance.equalsIgnoreCase("offensive")){
			return 0.8;
		}
		return 1.0;
	}
	protected Equipment initGear(int index){
		if(Data.equipment.get(index) != null){
			return Data.equipment.get(index);
		}
		else
			return nothing;
	}
	public void checkTreats(){
		statIncrease = new double[4];
		checkTreats(wapen);
		checkTreats(helm);
		checkTreats(harnas);
		checkTreats(handschoenen);
		checkTreats(broek);
		checkTreats(schoenen);
		checkTreats(schild);
		checkTreats(boog);
	}
	public double getEquippedWeight(){
		double weight = 0;
		for(int j=1;j<10;j++){
			try{
				weight+=getEquipped(j).getWeight();
			}catch(NullPointerException exc){
				continue;
			}
		}
		return weight;
	}
	public int getArrows(){
		int j = 0;
		for(Item i:inventory.keySet()){
			if(i.getClass().equals(Munition.class)){
				j+=inventory.get(i).intValue();
			}
		}
		return j;
	}
	public void addArrows(int x){
		while(x > 0){
			addInventoryItem(arrow);
			x--;
		}
		if(x < 0){
			for(int j=x;j<0;j++){
				delInventoryItem(arrow);
			}
		}
	}
	public void checkTreats(Equipment item){
		if(item != null){
			String treat = item.getTreat();
			if(treat != null){
				if(treat.substring(0,treat.lastIndexOf(" ")).equalsIgnoreCase("strength")){
					statIncrease[0] += Integer.parseInt(treat.substring(treat.lastIndexOf(" ")+1));
				}
				else if(treat.substring(0,treat.lastIndexOf(" ")).equalsIgnoreCase("dexterity")){
					statIncrease[1] += Integer.parseInt(treat.substring(treat.lastIndexOf(" ")+1));
				}
				else if(treat.substring(0,treat.lastIndexOf(" ")).equalsIgnoreCase("intellect")){
					statIncrease[2] += Integer.parseInt(treat.substring(treat.lastIndexOf(" ")+1));
				}
				else if(treat.substring(0,treat.lastIndexOf(" ")).equalsIgnoreCase("charisma")){
					statIncrease[3] += Integer.parseInt(treat.substring(treat.lastIndexOf(" ")+1));
				}
			}
		}
	}
	public int getMeleeWeaponSkill(){
		return 1;
	}
	public int getArchery(){
		return 1;
	}
	public int getDefense(int index){
		if(defense[0] > 0){
			return defense[index];
		}
		else{
			if(index == 0){
				return Math.max((int)level,helm.getStrength());
			}
			else if(index == 1){
				return Math.max((int)level,harnas.getStrength());
			}
			else if(index == 2){
				return Math.max((int)level,(int)(broek.getStrength()));
			}
			return 0;
		}
	}
	public Equipment getWapen(){
		return wapen;
	}
	public Equipment getHarnas(){
		return harnas;
	}
	public Equipment getSchild(){
		return schild;
	}
	public Equipment getBoog(){
		return boog;
	}
	public int getLevel(){
		return (int)level;
	}
	public double getFullLevel(){
		return level;
	}
	public int getHP(){
		if(HP <0) HP=0;
		return HP;
	}
	public int getMaxHP(){
		return maxHP;
	}
	public int getGoud(){
		return goud;
	}
	public String getName(){
		return naam;
	}
	public double getMovement(){
		double extraWeight = getEquippedWeight();
		// the more extra weight you have, the less you can walk, however the stronger you are, the more you can bear the weight
		double dummyMovement = movement*(1-extraWeight/50.0+0.01*strength) - dimMov;
		return dummyMovement;
	}
	public void setLevel(int x){
		level=x;
	}
	public void addHP(int x){
		HP+=x;
		if(HP > maxHP){
			HP = maxHP;
		}
		if(HP < 0){
			HP = 0;
		}
		updateHPBar();
	}
	public void setHP(int x){
		HP = x;
		updateHPBar();
	}
	public void setMaxHP(int x){
		maxHP = x;
	}
	public void addGoud(int x){
		goud+=x;
		if(goud < 0) goud = 0;
	}
	public void setStrength(int x){
		strength = x;
	}
	public int getStrength(){
		return strength+(int)statIncrease[0];
	}
	public void setDexterity(int x){
		dexterity = x;
	}
	public int getDexterity(){
		return dexterity+(int)statIncrease[1];
	}
	public void setIntellect(int x){
		intellect = x;
	}
	public int getIntellect(){
		return intellect+(int)statIncrease[2];
	}
	public void setCharisma(int x){
		charisma = x;
	}
	public void dimMovement(double x){
		dimMov+=x*movement;
		// maximally at 33% of initial movement, and no extra movement
		dimMov = (int)Math.max(0,Math.min(dimMov, 2.0*movement/3.0));
		if(this.getClass().equals(Avatar.class)){
			GameFrameCanvas.updatePlayerInfoTable();
		}
	}
	public boolean hasStun(){
		//TODO
		return false;
	}
	public boolean hasSpell(String name){
		for(Spell s:spreuken){
			try{
				if(s.getName().equalsIgnoreCase(name))
					return true;
			}catch(NullPointerException exc){
			}
		}
		return false;
	}
	public void wearOut(int damage, int aimFor){
		switch(aimFor){
		case 0: helm.addKwaliteit(-(int)(damage/(helm.getStrength()+0.01))); break;
		case 1: harnas.addKwaliteit(-(int)(damage/(harnas.getStrength()+0.01)));
				handschoenen.addKwaliteit(-(int)(damage/(handschoenen.getStrength()+0.01))); break;
		case 2: broek.addKwaliteit(-(int)(damage/(broek.getStrength()+0.01)));
				schoenen.addKwaliteit(-(int)(damage/(schoenen.getStrength()+0.01)));break;
		default: break;
		}
	}
	public int getCharisma(){
		return charisma+(int)statIncrease[3];
	}
	public boolean checkDood(){
		if(HP<=0){
			HP = 0;
			dood = true;
		}
		else{
			dood = false;
		}
		return dood;
	}
	public void increaseStat(int index,double amount){
		statIncrease[index]+=amount;
	}
	public void removeEquippedItem(Equipment item){
		switch(item.getType()){
		case 1: wapen = nothing; break;
		case 2: wapen = nothing; break;
		case 3: schild = nothing; break;
		case 4: boog = nothing; break;
		case 5: helm = nothing; break;
		case 6: harnas = nothing; break;
		case 7: handschoenen = nothing; break;
		case 8: broek = nothing; break;
		case 9: schoenen = nothing; break;
		default: break;
		}
	}
	
	public void addBuff(String name, String type, int amount, int duration, int intervalTime, String description){
		buffs.add(new Buff(name,type,amount,duration,intervalTime,description, false));
	}
	
	public void tickBuffs(){
		for(Buff b: buffs){
			b.activate();
		}
	}
	public void autoRunBuffs(){
		for(Buff b: buffs){
			b.start();
		}
	}
	
	class Buff extends Thread implements Serializable{
		
		private String name,description,type;
		private int amount,duration,currentPhase;
		private int intervalTime;
		private boolean isPlayer;
		
		public Buff(String name, String type, int amount, int duration, int intervalTime, String description, boolean isPlayer){
			this.name = name;
			this.type = type;
			this.amount = amount;
			this.duration = duration;
			this.intervalTime = intervalTime;
			this.description = description;
			this.isPlayer = isPlayer;
			
			currentPhase = 0;
		}
		
		public void run(){
			while(amount != 0 && currentPhase < duration && !checkDood()){
				try{
					activate();
					
					sleep(intervalTime);
				} catch(InterruptedException e){
					e.printStackTrace();
					logger.error(e);
				}
			}
		}
		
		public void activate(){
			if(!checkDood() && currentPhase >= 0 && currentPhase < duration){
				if(amount < 0){
					if(isPlayer){
						RPGMain.printText(true, new String[]{naam," suffers from ", name, ", and recieves ", amount + " damage."}, new String[]{"greenbold","regular","bold","regular","redbold"});
					}
					else{
						RPGMain.printText(true, new String[]{naam," suffers from ", name, ", and recieves ", amount + " damage."}, new String[]{"redbold","regular","bold","regular","redbold"});
					}
				}
				else{
					if(isPlayer){
						RPGMain.printText(true, new String[]{naam, " enjoys the effect of ", name, " and recieves ", amount + " HP."}, new String[]{"greenbold","regular","bold","regular","greenbold"});
					}
					else{
						RPGMain.printText(true, new String[]{naam, " enjoys the effect of ", name, " and recieves ", amount + " HP."}, new String[]{"redbold","regular","bold","regular","greenbold"});
					}
				}
				if(description != null && !description.equalsIgnoreCase("")){
					RPGMain.printText(true, description);
				}
				addHP(amount);
				currentPhase++;
			}
			else{
				buffs.remove(this);
			}
		}
		
		public String getType(){
			return type;
		}
		public String getBuffName(){
			return name;
		}
		public String getDescription(){
			return description;
		}
		public int getAmount(){
			return amount;
		}
		public int getDuration(){
			return duration;
		}
		public int getCurrentPhase(){
			return currentPhase;
		}
	}
}
