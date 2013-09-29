package game;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


public class Avatar extends Wezen implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<Quest> questlog = new ArrayList<Quest>();
	private int[] currentPosition = new int[2];
	private int age,daysPlayed;
	private int armour;
	private int hunger = 0;
	private int thirst = 0;
	private int fitness = 100;
	private int xTrespassing = 0;
	private String race,movementMode;
	private ArrayList<Integer> completedQuests = new ArrayList<Integer>();
	private boolean openForTrade = false, onHorse = false;
	private int archery,swords,axes,clubs,stamina,erudition,thievery,fireMaking,herbalism,hunting,animalKnowledge,swimming;
	private String meleeActions,rangedActions,magicActions;
	private LinkedHashMap<String,Integer> achievements = new LinkedHashMap<String,Integer>();

	private transient Logger logger = Logger.getLogger(Avatar.class);

	/*TYPE
	 *0 = geen equipment
	 *1 = wapen 1H
	 *2 = wapen 2H
	 *3 = schild
	 *4 = boog 
	 *5 = helm
	 *6 = harnas
	 *7 = handschoenen
	 *8 = broek
	 *9 = schoenen*/

	public Avatar(int lvl, String name,int hp, int gold, int strength,int dexterity,int intellect,int charisma,int[] startEquipmentID,int[] startClothingIDs){
		super(lvl,name,hp,gold,strength,dexterity,intellect,charisma);

		logger.debug("Past initial constructor");

		GameFrameCanvas.playerPortrait.setBarLength(1);
		// make new array to concat to startEquipmentID. 
		// In case there is not a value for an EquipmentID for weapon up until shoes, 
		// an exception is thrown of index out of bounds below. This is to prevent that.
		int[] nulls = new int[8-startEquipmentID.length];
		// initiate all as -1
		for(int j=0;j<nulls.length;j++){nulls[j]=-1;}

		int[] startEquipmentIDs = new int[8];
		//concat the arrays
		System.arraycopy(startEquipmentID, 0, startEquipmentIDs, 0, startEquipmentID.length);
		System.arraycopy(nulls, 0, startEquipmentIDs, startEquipmentID.length, nulls.length);

		age = 18;
		wapen = new Equipment(initGear(startEquipmentIDs[0]));
		schild = new Equipment(initGear(startEquipmentIDs[1]));
		boog = new Equipment(initGear(startEquipmentIDs[2]));
		helm = new Equipment(initGear(startEquipmentIDs[3]));
		harnas = new Equipment(initGear(startEquipmentIDs[4]));
		handschoenen = new Equipment(initGear(startEquipmentIDs[5]));
		broek = new Equipment(initGear(startEquipmentIDs[6]));
		schoenen = new Equipment(initGear(startEquipmentIDs[7]));
		inventory = new LinkedHashMap<Item,Integer>();
		checkTreats();

		try{
			mantle = (Clothing)Data.clothes.get(startClothingIDs[0]);
			//System.out.println("Wearing " + mantle.getName());
		}catch(ArrayIndexOutOfBoundsException e){
			mantle = noClothing;
		}
		try{
			shirt = (Clothing)Data.clothes.get(startClothingIDs[1]);
		}catch(ArrayIndexOutOfBoundsException e){
			shirt = noClothing;
		}
		try{
			pants = (Clothing)Data.clothes.get(startClothingIDs[2]);
		}catch(ArrayIndexOutOfBoundsException e){
			pants = noClothing;
		}

		try{
			for(int j=0;j<10;j++){
				if(Data.abilities.get(j) != null){
					abilities.put(Data.abilities.get(j), 0);
				}
			}
		} catch(Exception e){
		}

		spreuken.add(Data.spells.get(0));
		spreuken.add(Data.spells.get(1));

		meleeActions = "";
		rangedActions = "";
		magicActions = "";

		addArrows(30);

		// initialize skills
		archery = 1;
		swords = 1;
		axes = 1;
		clubs = 1;
		stamina = 1;
		erudition = 1;
		thievery = 1;
		swimming = 1;
		hunting = 5;
		fireMaking = 1;
		herbalism = 1;
		animalKnowledge = 1;
		//TODO others

		movementMode = "walking";

		GameFrameCanvas.playerPortrait.setShowStats(true);
		GameFrameCanvas.playerPortrait.setHunger(hunger);
		GameFrameCanvas.playerPortrait.setThirst(thirst);
		GameFrameCanvas.playerPortrait.setFitness(fitness);

		logger.info("Created player with name " + name);

	}

	public void initiateLogbook(){
		//TODO Write starting message
		Logbook.addContent("Introduction",1,"Take this, and record your travels with it. Write down what you experience, and use the knowledge and wisdom you gained to inspire the people you surround yourself with. Knowledge is power, guard it well.");
	}

	public void addBuff(String name, String type, int amount, int duration, int intervalTime, String description){
		buffs.add(new Buff(name,type,amount,duration,intervalTime,description, true));
	}

	public void setName(String name){
		this.naam = name;
	}

	/* GETTERS */
	public double getSkillParameterIncrease(String parameter){
		double d = 0.0;
		for(SkillElement se: Data.skillElements.values()){
			if(se.getProgress() > 0 && se.getParameter().equalsIgnoreCase(parameter)){
				d+=se.getMagnitude();
			}
		}
		return d;
	}
	public int getStrength(){
		return strength + (int)statIncrease[0] + (int)getSkillParameterIncrease("Strength");
	}
	public int getDexterity(){
		return dexterity + (int)statIncrease[1] + (int)getSkillParameterIncrease("Dexterity");
	}
	public int getIntellect(){
		return intellect + (int)statIncrease[2] + (int)getSkillParameterIncrease("Intellect");
	}
	public int getCharisma(){
		return charisma + (int)statIncrease[3] + (int)getSkillParameterIncrease("Charisma");
	}
	public double getSpeedModifier(){
		return 1.0 + getSkillParameterIncrease("Speed");
	}
	public String getRace(){
		return race;
	}
	public int getAge(){
		return age;
	}
	public int getHunger(){
		return hunger;
	}
	public int getThirst(){
		return thirst;
	}
	public int getDaysPlayed(){
		return daysPlayed;
	}
	public String getMovementMode(){
		return movementMode;
	}
	public int getFitness(){
		return fitness;
	}
	public String getMeleeActions(){
		return meleeActions;
	}
	public String getRangedActions(){
		return rangedActions;
	}
	public String getMagicActions(){
		return magicActions;
	}
	public int getCaughtTrespassing(){
		return xTrespassing;
	}
	public LinkedHashMap<Item,Integer> getInventory(){
		return inventory;
	}
	public boolean getOpenForTrade(){
		return openForTrade;
	}
	public int getQuestlogSize(){
		return questlog.size();
	}
	public Quest getQuest(int ID){
		for(Quest q:questlog){
			if(q.getID() == ID){
				return q;
			}
		}
		return null;
	}
	public ArrayList<Quest> getQuestLog(){
		return questlog;
	}
	public Quest getQuest(String name){
		for(int j = 0;j<questlog.size();j++){
			try{
				if(questlog.get(j).getNaam().equalsIgnoreCase(name)){
					return questlog.get(j);
				}
			}catch(NullPointerException np){

			}
		}
		return null;
	}
	public int[] getCurrentPosition(){
		return currentPosition;
	}
	public int getArmour(){
		armour = (int)(harnas.getStrength() + 1.5*schild.getStrength());
		return armour;
	}
	public ArrayList<Integer> getCompletedQuests(){
		return completedQuests;
	}
	public boolean isQuestCompleted(int id){
		for(int i:completedQuests){
			if(i == id)
				return true;
		}
		return false;
	}


	/* GET SKILLS */
	public int getArchery(){
		return archery;
	}
	public int getSwordSkill(){
		return swords;
	}
	public int getAxeSkill(){
		return axes;
	}
	public int getClubSkill(){
		return clubs;
	}
	public int getStamina(){
		return stamina;
	}
	public int getErudition(){
		return erudition;
	}
	public int getThievery(){
		return thievery;
	}
	public int getSwimming(){
		return swimming;
	}
	public int getFireMaking(){
		return fireMaking;
	}
	public int getHerbalism(){
		return herbalism;
	}
	public int getHunting(){
		return hunting;
	}
	public int getAnimalKnowledge(){
		return animalKnowledge;
	}
	public int getSkill(String skill){
		if(skill.equalsIgnoreCase("archery")){
			return archery;
		}
		else if(skill.equalsIgnoreCase("swords")){
			return swords;
		}
		else if(skill.equalsIgnoreCase("axes")){
			return axes;
		}
		else if(skill.equalsIgnoreCase("clubs")){
			return clubs;
		}
		else if(skill.equalsIgnoreCase("stamina")){
			return stamina;
		}
		else if(skill.equalsIgnoreCase("erudition")){
			return erudition;
		}
		else if(skill.equalsIgnoreCase("thievery")){
			return thievery;
		}
		else if(skill.equalsIgnoreCase("swimming")){
			return swimming;
		}
		else if(skill.equalsIgnoreCase("firemaking")){
			return fireMaking;
		}
		else if(skill.equalsIgnoreCase("herbalism")){
			return herbalism;
		}
		else if(skill.equalsIgnoreCase("hunting")){
			return hunting;
		}
		else if(skill.equalsIgnoreCase("animalknowledge")){
			return animalKnowledge;
		}
		return -1;
	}



	public double getPlayerWeight(){
		double weight = 60;
		weight+=getEquippedWeight();
		//System.out.println("Player Weight: " + weight);
		return weight;
	}
	public int getMeleeWeaponSkill(){
		//System.out.println(wapen.getWeaponType());
		if(wapen.getWeaponType().equalsIgnoreCase("sword")){
			return swords;
		}
		else if(wapen.getWeaponType().equalsIgnoreCase("axe")){
			return axes;
		}
		else if(wapen.getWeaponType().equalsIgnoreCase("club")){
			return clubs;
		}
		return 0;
	}
	public boolean getOnHorse(){
		return onHorse;
	}

	/* SETTERS */
	public void addHP(int x){
		super.addHP(x);
		if(HP <= 0){
			//TODO tell saga of death
			JOptionPane.showMessageDialog(null, "You have died!");
			Global.soundEngine.fadeLines("music");
			Global.soundEngine.fadeLines("ambient");
			Global.soundEngine.fadeLines("effects");
			Global.soundEngine.fadeLines("combat");
			Global.soundEngine.fadeLines("weather");
		}
	}
	public void addAchievement(String s,int x){
		//TODO make all possible achievements beforehand
		if(achievements.get(s) != null){
			achievements.put(s, achievements.get(s)+x);
		}
		else{
			achievements.put(s, x);
		}
	}
	public void setOnHorse(boolean b){
		onHorse = b;
	}
	public void setRace(String x){
		race = x;
	}
	public void setMovementMode(String s){
		movementMode = s;
	}
	public void incrAge(){
		age++;
	}
	public void addThirst(int x){
		//TODO
		x/=2;
		thirst+=x;
		thirst = Math.max(Math.min(100, thirst), 0);
		GameFrameCanvas.playerPortrait.setThirst(thirst);
	}
	public void resetThirst(){
		thirst = 0;
		GameFrameCanvas.playerPortrait.setThirst(thirst);
	}
	public void incrDaysPlayed(){
		daysPlayed++;
	}
	public void addMeleeAction(String s){
		meleeActions+= s.toLowerCase().charAt(0);
		if(meleeActions.length() > 25){
			meleeActions = meleeActions.substring(1);
		}
	}
	public void addRangedAction(String s){
		rangedActions+= s.toLowerCase().charAt(0);
		if(rangedActions.length() > 25){
			rangedActions = rangedActions.substring(1);
		}
	}
	public void addMagicAction(String s){
		magicActions+= s.toLowerCase().charAt(0);
		if(magicActions.length() > 25){
			magicActions = magicActions.substring(1);
		}
	}
	public void decrFoodFreshness(){
		for(Item i: inventory.keySet()){
			if(i instanceof Consumable){
				((Consumable) i).decrFreshness(1);
			}
		}
	}
	public void addHunger(int x){
		//TODO
		x/=2;
		hunger+=x;
		hunger = Math.max(Math.min(100, hunger), 0);
		GameFrameCanvas.playerPortrait.setHunger(hunger);
		/*switch(hunger){
		case 6: JOptionPane.showMessageDialog(null, "Your tummy starts to rumble.", "Tummy says brommromm", JOptionPane.OK_OPTION); break;
		case 3: JOptionPane.showMessageDialog(null, "Better find something to eat soon.","Tummy goes BROMrommbrom",JOptionPane.OK_OPTION); break;
		case 1: JOptionPane.showMessageDialog(null, "All that activity has left you hungry, like really hungry.", "Tummy screams 'FEED ME'", JOptionPane.OK_OPTION); break;
		}*/
	}
	@Override
	public double getMovement(){
		double extraWeight = getEquippedWeight();
		// the more extra weight you have, the less you can walk, however the stronger you are, the more you can bear the weight
		double dummyMovement = (movement*(1-0.5*extraWeight/25+0.01*strength) - dimMov)*(1.0+(double)getStamina()/100.0);
		return dummyMovement;
	}
	public int getExtraMovementPercentage(){
		return (int)(100*(getMovement()/movement-1));
	}
	public void resetHunger(){
		hunger = 0;
		GameFrameCanvas.playerPortrait.setHunger(hunger);
	}
	public void addFitness(int x){
		x/=2;
		fitness+=x;
		fitness = Math.max(Math.min(100, fitness), 0);
		GameFrameCanvas.playerPortrait.setFitness(fitness);
		/*switch(fitness){
		case 9: JOptionPane.showMessageDialog(null, "You're growing a bit tired. Better find some place to sleep.","Adventuring is tiring.",JOptionPane.OK_OPTION);
		}*/
	}
	public void resetFitness(){
		fitness = 100;
		GameFrameCanvas.playerPortrait.setFitness(fitness);
	}
	public void addCaughtTrespassing(int x){
		xTrespassing+= x;
	}
	public void setTrade(boolean b){
		openForTrade = b;
	}
	public void setCurrentPosition(int[] newPos){
		currentPosition = newPos;
	}
	public void updateHPBar(){
		GameFrameCanvas.playerPortrait.setBarLength((float)HP/maxHP);
	}
	public void completeQuest(int qID){
		completedQuests.add(qID);
	}
	public void addAbility(int ID){
		abilities.put(Data.abilities.get(ID), 0);
	}
	public boolean questCompleted(int qID){
		return completedQuests.contains(qID);
	}

	/* MANAGE SKILLS */
	public void addArchery(int d){
		archery+=d;
		RPGMain.printText(true, "Archery increased by " + d + "! You will hit targets more easily.", "darkblue");
		checkLevelUp();
	}
	public void addSwordSkill(int d){
		swords+=d;
		RPGMain.printText(true, "You skill with swords has increased by " + d + "! You will hit targets more easily.", "darkblue");
		checkLevelUp();
	}
	public void addAxeSkill(int d){
		axes+=d;
		RPGMain.printText(true, "You skill with axes has increased by " + d + "! You will hit targets more easily.", "darkblue");
		checkLevelUp();
	}
	public void addClubSkill(int d){
		clubs+=d;
		RPGMain.printText(true, "You skill with clubs has increased by " + d + "! You will hit targets more easily.", "darkblue");
		checkLevelUp();
	}
	public void addStamina(int d){
		stamina+=d;
		RPGMain.printText(true, "Stamina increased by " + d + "! You will tire less less quickly.", "darkblue");
		checkLevelUp();
	}
	public void addErudition(int d){
		erudition+=d;
		RPGMain.printText(true, "Erudition increased by " + d + "!", "darkblue");
		checkLevelUp();
	}
	public void addThievery(int d){
		thievery+=d;
		checkLevelUp();
	}
	public void addSwimming(int d){
		swimming+=d;
		RPGMain.printText(true, "Swimming skill increased by " + d + "! You are able to swim longer.", "darkblue");
		checkLevelUp();
	}
	public void addFireMaking(int d){
		fireMaking+=d;
		checkLevelUp();
	}
	public void addHerbalism(int d){
		herbalism+=d;
		checkLevelUp();
	}
	public void addHunting(int d){
		hunting+=d;
		checkLevelUp();
	}
	public void addAnimalKnowledge(int d){
		animalKnowledge+=d;
		checkLevelUp();
	}

	/* INVENTORY METHODS */

	/* SLOT
	 *0 = geen equipment
	 *1 = wapen 1H
	 *2 = wapen 2H
	 *3 = schild
	 *4 = boog 
	 *5 = helm
	 *6 = harnas
	 *7 = handschoenen
	 *8 = broek
	 *9 = schoenen*/

	public void equipItem(Equipment item) throws InterruptedException{
		equipItem(getInventoryItemIndex(item.getName()));
	}
	public void sleep(){
		if(Data.wereld[currentPosition[0]][currentPosition[1]] instanceof Town){
			//check if there's an Inn in the town, if not ask the villagers if you can stay with them
			//this is dependent on reputation
			Town current = (Town)Data.wereld[currentPosition[0]][currentPosition[1]];
			if(current.hasInn()){
				RPGMain.printText(true, "It appears this city has an Inn. You better try to look for a spot there.");
				Global.pauseProg(2000);
			}
			else{
				double reputation = HopeSimulator.getReputation(currentPosition[0], currentPosition[1]);
				//people are generally quite kind, so *1.25 standard
				double probability = Math.min(1, Math.random()*(1.25 + reputation/10.0));

				if(probability > 0.5){
					RPGMain.printText(true, "Someone was happy to take you in and stay for a good night sleep.");
					Global.pauseProg();

					int hours = 0;
					while(true){
						RPGMain.printText(true, "How long would you like to sleep?\n>");
						try{
							hours = Integer.parseInt(RPGMain.waitForMessage());
							break;
						} catch(NumberFormatException e){
							RPGMain.printText(true, "Not a number.");
							continue;
						}
					}
					DayLightThread.rest(hours);
					addFitness(17*hours);
					dimMovement(-10*hours);
					Global.pauseProg(2000);
				}
				else{
					RPGMain.printText(true, "You've asked everyone you saw, though noone was willing to take you in for a night. Looks like you'll have to find a place to sleep in the wild, or move on to the next town.");
				}
			}
		}
		else if(Data.wereld[currentPosition[0]][currentPosition[1]] instanceof HostileArea){

			//look for a decent place to sleep in the surroundings
			//chance of getting attacked depend on the quality of the sleep and the density of mobs around you
			//making a fire increases fitness gain, but increases probability of enemies finding you
			//fitness gain depends also on temperature and clothing

			//What determines quality of sleeping spot? Terrain type, vegetation, mountainous terrain
			//TODO height variation around player

			HostileArea current = (HostileArea)Data.wereld[currentPosition[0]][currentPosition[1]];

			//type is as "category type (mountain)"
			String[] type = current.getLocationType().split(" ");

			double quality = Math.random();

			if(type[0].equalsIgnoreCase("Forest")){
				//more cover in general, less trouble from the wind
				quality+=0.1;
			}
			if(type[1].equalsIgnoreCase("Loofbos")){
				//quite a bit of vegetation
				quality+=0.1;
			}
			else if(type[1].equalsIgnoreCase("Naaldbos")){
				//nearly no vegetation on the ground, reduced cover both from wind and from enemies
				quality-=0.15;
			}
			try{
				if(type[2].equalsIgnoreCase("mountain")){
					//mountainous terrain gives good chance of finding closed shelter, cavelike things
					quality+=0.1;
				}
			} catch(ArrayIndexOutOfBoundsException e){

			}

			if(hasItem("Tent")){
				quality+=0.25;
				RPGMain.printText(true, "You set up your tent and get everything ready. Good thing you brought this with you, you'll notice ");
			}

			quality = Math.min(1, quality);

			double mobDensity = current.getMobDensity(5);

			//light a fire
			if(hasItem("Wood") && hasItem("Flint")){
				while(true){
					RPGMain.printText(true, "Do you want to create a fire? [y/n]\n>");
					String answer = RPGMain.waitForMessage().toLowerCase();

					if(answer.startsWith("y")){
						String weather = WeatherSimulator.getWeather(currentPosition[0], currentPosition[1]);
						double r = Math.random();
						if(weather.startsWith("rain") && r > 0.5){
							RPGMain.printText(true, "You try your best but with all the wood you find wet to the core, it's impossible to get a fire going. You'll have to do without.");
							break;
						}
						else if(weather.endsWith("stormy") && r > 0.75){
							RPGMain.printText(true, "This heavy wind is giving you a hard time. You keep on trying, but eventually give up, frustrated.");
							break;
						}

						quality+=0.3;
						delInventoryItem("Wood",1);

						break;
					}
					else if(answer.startsWith("n")){
						break;
					}
					else{
						RPGMain.printText(true, "Not a valid option.");
						continue;
					}
				}
			}

			int hours = 0;
			int fitnessRate = (int)Math.round(8*(1+quality));
			//ask number of hours
			while(true){
				RPGMain.printText(false, "How many hours would you like to sleep? (rate: " + fitnessRate + "%/h)\n>");

				try{
					hours = Integer.parseInt(RPGMain.waitForMessage());
					break;
				} catch(NumberFormatException e){
					RPGMain.printText(true, "Not a number");
					continue;
				}
			}
			Global.pauseProg(4000);
			//rest and see if you get disturbed
			for(int j=0;j<hours;j++){
				DayLightThread.rest(1);
				addFitness((int)Math.round(fitnessRate));
				dimMovement(-10*quality*hours);
				if(Math.random()-0.3 > (quality-mobDensity)){
					//bad things happen
					RPGMain.printText(true, "Bad things happen here. Only rested for " + (j+1) + " hours.");
					break;
				}
			}
		}
		else{
			//at sea, sleep on your boat
		}
	}
	public void equipItem(int keuze) throws InterruptedException{
		if(getInventoryItem(keuze) instanceof Equipment){
			Equipment item = (Equipment)getInventoryItem(keuze);
			// if player wants to equip shield while holding a 2H weapon
			if((item.getType() == 2 && !schild.getName().equalsIgnoreCase("nothing")) || (item.getType() == 3 && wapen.getType() == 2)){
				RPGMain.printText(true, "You cannot equip a two-handed weapon and a shield simultaneously.");
			}
			else{
				RPGMain.printText(true,"You have succesfully equipped " + item.getName() + ".");
				if(!getEquipped(item.getType()).getName().equalsIgnoreCase("nothing")){
					if(getInventorySpace() >= (getEquipped(item.getType()).getWeight() - item.getWeight())){
						delInventoryItem(item);
						switch(item.getType()){
						case 1: addInventoryItem(wapen);
						wapen = item;
						break;
						case 2: addInventoryItem(wapen);
						wapen = item;
						break;
						case 3: addInventoryItem(schild);
						schild = item; 
						break;
						case 4: addInventoryItem(boog);
						boog = item;
						break;
						case 5: addInventoryItem(helm);
						helm = item;
						break;
						case 6: addInventoryItem(harnas);
						harnas = item;
						break;
						case 7: addInventoryItem(handschoenen);
						handschoenen = item;
						break;
						case 8: addInventoryItem(broek);
						broek = item;
						break;
						case 9: addInventoryItem(schoenen);
						schoenen = item;
						break;
						}
					}
					else{
						RPGMain.printText(true, "The item you are trying to replace is too heavy.");
					}
				}
				else{
					delInventoryItem(item);
					switch(item.getType()){
					case 1:	wapen = item;
					break;
					case 2: wapen = item;
					break;
					case 3: schild = item; 
					break;
					case 4: boog = item;
					break;
					case 5: helm = item;
					break;
					case 6: harnas = item;
					break;
					case 7: handschoenen = item;
					break;
					case 8: broek = item;
					break;
					case 9: schoenen = item;
					break;
					}
				}
				checkTreats();
			}
		}
		else{
			RPGMain.printText(true,"You can't equip that.");
		}
		Global.pauseProg();
	}
	public void removeUsedItems(){
		ArrayList<Item> delete = new ArrayList<Item>();
		for(Item i:inventory.keySet()){
			if(i instanceof Consumable){
				if(((Consumable) i).isUsed()){
					delete.add(i);
				}
			}
			else if(i instanceof Potion){
				if(((Potion) i).isUsed()){
					delete.add(i);
				}
			}
			else if(i instanceof Herb){
				if(((Herb)i).isUsed()){
					delete.add(i);
				}
			}
		}
		for(Item i:delete){
			delInventoryItem(i);
		}
		delete.clear();
	}
	public void showInventory() throws InterruptedException{

		while(true){
			RPGMain.printText(true,"\nInventory (" + String.format("%.2f", (getMaxWeight() - getInventorySpace())) + "/" + getMaxWeight() + "kg filled): ");
			int amount = 0;
			for(int j=0;j<inventory.size();j++){
				RPGMain.printText(false, (j+1) + ": " + getInventoryItem(j).getName());
				amount = inventory.get(getInventoryItem(j));
				if(amount > 1){
					RPGMain.printText(true, " (" + amount + ")");
				}
				else{
					RPGMain.printText(true, "");
				}
			}

			int aantalItems = inventory.size();

			if(aantalItems > 0){
				RPGMain.printText(false,"(Type \"info <index>\", \"use <index>\" or \"cancel\".)\n> ");
			}
			else{
				Global.pauseProg();
				break;
			}
			String keuze = RPGMain.waitForMessage().toLowerCase().trim();
			int index = 0;
			if(keuze.startsWith("info")){
				try{
					index = Integer.parseInt(keuze.substring(5));
				} catch(NumberFormatException e){
					RPGMain.printText(true,"Index is not a number.");
				}
				try{
					getInventoryItem(index-1).showInfo();
					Global.pauseProg();
				} catch(NullPointerException np){
					RPGMain.printText(true,"not a valid option.");
					continue;
				} catch(IndexOutOfBoundsException exc){
					RPGMain.printText(true, "Not a valid option.");
				}
			}

			else if(keuze.startsWith("use")){
				try{
					index = Integer.parseInt(keuze.substring(4));
					getInventoryItem(index-1).use();
					removeUsedItems();
				} catch(NumberFormatException e){
					RPGMain.printText(true,"Index is not a number.");
				} catch(IndexOutOfBoundsException exc){
					RPGMain.printText(true, "Not a valid option.");
				}
			}
			else if(keuze.equalsIgnoreCase("cancel")){
				break;
			}
			else{
				RPGMain.printText(true,"Not a valid option.");
			}
		}
	}
	// CHARACTER PANEL

	public void characterPanel() throws InterruptedException{
		RPGMain.printText(true,"Your name: " + naam);
		RPGMain.printText(true,"Level: " + level);
		RPGMain.printText(true,"HP: " + HP + "/" + maxHP);
		RPGMain.printText(true, "Fitness: " + fitness + "%");
		RPGMain.printText(true, "Hunger: " + hunger + "%");
		RPGMain.printText(true, "Thirst:" + thirst + "%");
		RPGMain.printText(false,"Strength: " + (strength+statIncrease[0]));
		RPGMain.printText(true,"          Dexterity: " + (dexterity+statIncrease[1]));
		RPGMain.printText(false,"Intellect: " + (intellect+statIncrease[2]));
		RPGMain.printText(true,"          Charisma: " + (charisma+statIncrease[3]));
		//RPGMain.printText(true,"Experience: " + experience + "/" + (int)(Math.pow(level+2, 3) + 6) + " (" + Math.rint((experience/((Math.pow(level+2, 3))+6))*100) + "%)");
		if(goud >= 0){
			RPGMain.printText(true,"Gold pieces: " + goud);
		}
		else{
			RPGMain.printText(true,"Gold pieces: 0\nDebt to innkeeper: " + -goud);
		}
		RPGMain.printText(true,"\nSpells: ");
		for(int j=0;j<spreuken.size();j++){
			if(spreuken.get(j) != null){
				RPGMain.printText(true,(j+1) + ": " + spreuken.get(j).getName() + " Strength: " + spreuken.get(j).getStrength() + " Casting time: " + spreuken.get(j).getCastingTime());
			}
		}
		while(true){
			RPGMain.printText(true,"\nInventory (" + getInventorySpace() + "/" + getMaxWeight() + "): ");
			int aantalItems = inventory.size();
			/*for(int j=0;j<inventory.size();j++){
				if(inventory.get(j) != null && !inventory.get(j).getClass().equals(Munition.class)){
					RPGMain.printText(true,(j+1) + ": " + inventory.get(j).getName());
					aantalItems++;
				}
			}*/
			showInventory();
			RPGMain.printText(true,"\nEquipped: ");
			RPGMain.printText(true,"Weapon: " + wapen.getName());
			RPGMain.printText(true,"Ranged Weapon: " + boog.getName());
			RPGMain.printText(true,"Shield: " + schild.getName());
			RPGMain.printText(true,"Armour: " + harnas.getName());
			if(aantalItems > 0){
				RPGMain.printText(false,"(Type \"info <index>\", \"use <index>\" or \"cancel\".)\n> ");
			}
			else{
				Global.pauseProg();
				break;
			}
			String keuze = RPGMain.waitForMessage().toLowerCase().trim();
			int index = 0;
			if(keuze.startsWith("info")){
				try{
					index = Integer.parseInt(keuze.substring(5));
				} catch(NumberFormatException e){
					RPGMain.printText(true,"Index is not a number.");
				}
				try{
					getInventoryItem(index-1).showInfo();
					Global.pauseProg();
				} catch(NullPointerException np){
					RPGMain.printText(true,"not a valid option.");
					continue;
				} catch(IndexOutOfBoundsException exc){
					RPGMain.printText(true, "Not a valid option.");
				}
			}

			else if(keuze.startsWith("use")){
				try{
					index = Integer.parseInt(keuze.substring(4));
					getInventoryItem(index-1).use();
					removeUsedItems();
				} catch(NumberFormatException e){
					RPGMain.printText(true,"Index is not a number.");
				} catch(IndexOutOfBoundsException exc){
					RPGMain.printText(true, "Not a valid option.");
				}
			}
			else if(keuze.equalsIgnoreCase("cancel")){
				break;
			}
			else{
				RPGMain.printText(true,"Not a valid option.");
			}
		}
	}

	/* QUEST METHODS */

	public void showQuestlog() throws InterruptedException{
		RPGMain.printText(true,"Active quests: ");
		int nullIndex = 1;
		for(int j=0;j<questlog.size();j++){
			try{
				RPGMain.printText(false,(j+1) + ": " + questlog.get(j).getNaam());
				if(questlog.get(j).getCompleted() == true){
					RPGMain.printText(false," (Completed)");
				}
				RPGMain.printText(true,"");
				nullIndex++;
			}catch(NullPointerException np){
			}
		}
		RPGMain.printText(true,nullIndex + ": Cancel");
		int keuze = 0;
		while(true){
			RPGMain.printText(false,"Access quest?\n>");
			try{
				keuze = Integer.parseInt(RPGMain.waitForMessage());
			}catch(InputMismatchException IME){
				RPGMain.printText(true,"Not a valid option.");
				continue;
			}
			if((keuze != nullIndex) && (keuze <= questlog.size()) && (questlog.get(keuze-1) != null)){
				questlog.get(keuze-1).showQuest();
			}
			else{

			}
			break;
		}
	}
	public void addQuest(int ID){
		questlog.add(Data.quests.get(ID));
		RPGMain.printText(true, "Acquired: \"" + getQuest(ID).getNaam() + "\".");

		try {
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(new File("Data/QuestDialog.xml"));
			Element root = doc.getRootElement();
			List<Element> children = root.getChildren();
			Iterator<Element> i = children.iterator();
			while(i.hasNext()){
				Element el = i.next();
				if(el.getAttributeValue("id").equalsIgnoreCase("" + ID)){
					try{
						Logbook.addContent("Story/Quests/" + el.getAttributeValue("name"), 1, el.getChild("summary").getTextTrim());
					} catch(NullPointerException e){
						logger.error("Need a summary for logbook for quest ID " + ID);
					}
				}
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
		for(Item i: inventory.keySet()){
			Data.quests.get(ID).checkProgress(i.getClass().getName().split("\\.")[1], i.getID());
		}
		Global.pauseProg(2000);
	}
	public void delQuest(String name){

		for(int j=0;j<questlog.size();j++){
			try{
				if(questlog.get(j).getNaam().equalsIgnoreCase(name)){
					questlog.remove(j);
				}
			}catch(NullPointerException np){
			}
		}
	}
	public void delQuest(int ID){
		for(int j=0;j<questlog.size();j++){
			try{
				if(questlog.get(j).getID() == ID){
					questlog.remove(j);
				}
			}catch(NullPointerException np){
			}
		}
	}
	public void delQuest(Quest q){
		questlog.remove(q);
	}

	public void checkLevelUp(){
		//TODO
		//values are such that level = 1 for statSum = 12 (12*1) and level = 10 for statSum = 1200 (all 100)
		double a = 2.8;
		double b = 12.0;
		double c = 0.25;
		double d = -4.776;
		int statSum = archery + swords + axes + clubs + stamina + erudition + thievery + fireMaking + herbalism + hunting + animalKnowledge + swimming;
		int oldLevel = (int)level;
		level = a*Math.pow(statSum+b, c) + d;
		GameFrameCanvas.updatePlayerInfoTable();
		if((int)level > oldLevel){
			RPGMain.printText(true, "You gave gained a level!\nYou are now level " + (int)level + ".","bold");
			int punten = 3;
			while(punten > 0){
				try{
					RPGMain.printText(false,"You have " + punten + " point(s) to spend.\nIncrease:" +
							"\n1: Strength (current: " + strength + ")" +
							"\n2: Dexterity (current: " + dexterity + ")" +
							"\n3: Intellect (current: " + intellect + ")" +
							"\n4: Charisma (current: " + charisma + ")\n>");
					switch(Integer.parseInt(RPGMain.waitForMessage())){
					case 1: RPGMain.printText(true,"Strength increased.");
					strength++;
					punten--;
					break;
					case 2: RPGMain.printText(true,"Dexterity increased.");
					dexterity++;
					punten--;
					break;
					case 3: RPGMain.printText(true,"Intellect increased.");
					intellect++;
					punten--;
					break;
					case 4: RPGMain.printText(true,"Charisma increased.");
					charisma++;
					punten--;
					break;
					default: break; 
					}
					Global.pauseProg(1000);
				}catch(NumberFormatException exc){
					RPGMain.printText(true,"Not a valid option.");
					continue;
				}
			}

			RPGMain.printText(true, "Visit the temple to continue your spiritual journey.");
			Global.pauseProg();
		}


		/*while(experience >= (Math.pow(level+2, 3) + 6)){
			experience-= Math.pow(level+2, 3) + 6;
			level++;
			RPGMain.printText(true,"You have gained a level!\nYou are now level " + level + ".\n" +
					"HP increased by " + Math.floor(0.25*maxHP) + ".");
			int punten = 5;
			while(punten > 0){
				try{
					RPGMain.printText(false,"You have " + punten + " point(s) to spend.\nIncrease:" +
							"\n1: Strength (current: " + strength + ")" +
								"\n2: Dexterity (current: " + dexterity + ")" +
										"\n3: Intellect (current: " + intellect + ")" +
												"\n4: Charisma (current: " + charisma + ")\n>");
					switch(Integer.parseInt(RPGMain.waitForMessage())){
					case 1: RPGMain.printText(true,"Strength increased.");
							strength++;
							punten--;
							break;
					case 2: RPGMain.printText(true,"Dexterity increased.");
							dexterity++;
							punten--;
							break;
					case 3: RPGMain.printText(true,"Intellect increased.");
							intellect++;
							punten--;
							break;
					case 4: RPGMain.printText(true,"Charisma increased.");
							charisma++;
							punten--;
							break;
					default: break; 
					}
					Global.pauseProg(1000);
				}catch(InputMismatchException ime){
					continue;
				} catch(InterruptedException e){
					e.printStackTrace();
					logger.error(e);
				}
			}
			maxHP+=Math.floor(0.25*maxHP);
			HP = maxHP;
			try{
			Global.pauseProg();
			}catch(InterruptedException e){
				e.printStackTrace();
				logger.error(e);
			}
		}*/
	}
}
