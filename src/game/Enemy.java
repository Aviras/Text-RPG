package game;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.Logger;

public class Enemy extends Wezen implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5177406482505521159L;
	private int ID;
	private int intelligence;
	private boolean stationary = false,uniqueGear = false;
	private int pursuitIndex = -1;
	private static Logger logger = Logger.getLogger(Enemy.class);
	
	// constructor with no gear specified, 3 defense values for head - body - legs
	public Enemy(Integer id, String name, Integer lvl, Integer hp, Integer intelligence, Integer gold, Integer strength, Integer dexterity, Integer intellect, String invItemIDs, int[] probabilities,int[] spellIDs, int[] defense, Boolean stationary){
		super(lvl.intValue(),name,hp.intValue(),gold.intValue(),strength.intValue(),dexterity.intValue(),intellect.intValue(),0);
		ID = id.intValue();
		
		this.intelligence = intelligence.intValue();
		
		this.stationary = stationary.booleanValue();
		
		wapen = nothing;
		schild = nothing;
		boog = nothing;
		helm = nothing;
		harnas = nothing;
		handschoenen = nothing;
		broek = nothing;
		schoenen = nothing;
		
		for(int j=0;j<this.defense.length;j++){
			this.defense[j] = defense[j];
		}
		if(!invItemIDs.equalsIgnoreCase("null")){
			String[] parts = invItemIDs.split(";");
			for(int j=0;j<parts.length;j++){
				String[] info = parts[j].split(":");
				
				int random = Global.generator.nextInt(100);
				random++;
				try{
					if(random <= probabilities[j]){
						if(info[0].equalsIgnoreCase("equipment")){
							addInventoryItem(Data.equipment.get(Integer.parseInt(info[1])));
						}
						else if(info[0].equalsIgnoreCase("consumable")){
							addInventoryItem(Data.consumables.get(Integer.parseInt(info[1])));
						}
						else if(info[0].equalsIgnoreCase("potion")){
							addInventoryItem(Data.potions.get(Integer.parseInt(info[1])));
						}
						else if(info[0].equalsIgnoreCase("clothing")){
							addInventoryItem(Data.clothes.get(Integer.parseInt(info[1])));
						}
						else if(info[0].equalsIgnoreCase("item")){
							addInventoryItem(Data.items.get(Integer.parseInt(info[1])));
						}
					}
				}catch(Exception e){
					e.printStackTrace();
					logger.error("Corrupt inventory item data for enemy ID " + ID,e);
				}
			}
		}
		if(spellIDs[0] != -1){
			for(int j = 0;j<spellIDs.length;j++){
				spreuken.add(Data.spells.get(spellIDs[j]));
			}
		}
		
		try{
			for(int j=0;j<10;j++){
				if(Data.abilities.get(j) != null){
					abilities.put(Data.abilities.get(j), 0);
				}
			}
		} catch(Exception e){
		}
	}
	// constructor with gear specified, player loots depending what it hit during combat
	public Enemy(Integer id, String name, Integer lvl, Integer hp, Integer intelligence, Integer gold, Integer strength, Integer dexterity, Integer intellect, int[] gear, String invItemIDs, int[] probabilities, int[] spellIDs, Boolean stationary){
		super(lvl.intValue(),name,hp.intValue(),gold.intValue(),strength.intValue(),dexterity.intValue(),intellect.intValue(),0);
		ID = id.intValue();
		
		this.intelligence = intelligence.intValue();
		
		this.stationary = stationary.booleanValue();
		
		wapen = initGear(gear[0]);
		schild = initGear(gear[1]);
		boog = initGear(gear[2]);
		helm = initGear(gear[3]);
		harnas = initGear(gear[4]);
		handschoenen = initGear(gear[5]);
		broek = initGear(gear[6]);
		schoenen = initGear(gear[7]);
		checkTreats();
		
		if(!invItemIDs.equalsIgnoreCase("null")){
			String[] parts = invItemIDs.split(";");
			for(int j=0;j<parts.length;j++){
				String[] info = parts[j].split(":");
				
				int random = Global.generator.nextInt(100);
				random++;
				try{
					if(random <= probabilities[j]){
						if(info[0].equalsIgnoreCase("equipment")){
							addInventoryItem(Data.equipment.get(Integer.parseInt(info[1])));
						}
						else if(info[0].equalsIgnoreCase("consumable")){
							addInventoryItem(Data.consumables.get(Integer.parseInt(info[1])));
						}
						else if(info[0].equalsIgnoreCase("potion")){
							addInventoryItem(Data.potions.get(Integer.parseInt(info[1])));
						}
						else if(info[0].equalsIgnoreCase("clothing")){
							addInventoryItem(Data.clothes.get(Integer.parseInt(info[1])));
						}
						else if(info[0].equalsIgnoreCase("item")){
							addInventoryItem(Data.items.get(Integer.parseInt(info[1])));
						}
					}
				}catch(Exception e){
					e.printStackTrace();
					logger.error("Corrupt inventory item data for enemy ID " + ID,e);
				}
			}
		}
		if(spellIDs[0] != -1){
			for(int j = 0;j<spellIDs.length;j++){
				spreuken.add(Data.spells.get(spellIDs[j]));
			}
		}
		try{
			for(int j=0;j<10;j++){
				if(Data.abilities.get(j) != null){
					abilities.put(Data.abilities.get(j), 0);
				}
			}
		} catch(Exception e){
		}
	}
	public void uniqueGear(){
		wapen = new Equipment(wapen);
		schild = new Equipment(schild);
		boog = new Equipment(boog);
		helm = new Equipment(helm);
		harnas = new Equipment(harnas);
		handschoenen = new Equipment(handschoenen);
		broek = new Equipment(broek);
		schoenen = new Equipment(schoenen);
		uniqueGear = true;
	}
	public boolean getUniqueGearDone(){
		return uniqueGear;
	}
	public void updateHPBar(){
		GameFrameCanvas.enemyPortrait.setBarLength((float)HP/maxHP);
	}
	// makes a seperate Enemy object, using the Enemy object in Data as a template, else there's only 1 Enemy object in the entire game
	public Enemy(Enemy another){
		super(another.level,another.naam,another.HP,another.goud,another.strength,another.dexterity,another.intellect,0);
		this.ID = another.ID;
		this.intelligence = another.intelligence;
		this.stationary = another.stationary;
		this.movement = another.movement;
		this.defense = another.defense;
		this.inventory = another.inventory;
		this.spreuken = another.spreuken;
		this.wapen = another.wapen;
		if(Math.random() < 0.5)
			this.schild = another.schild;
		else
			this.schild = nothing;
		this.boog = another.boog;
		this.helm = another.helm;
		this.harnas = another.harnas;
		this.handschoenen = another.handschoenen;
		this.broek = another.broek;
		this.schoenen = another.schoenen;
		this.abilities = another.abilities;
	}
	public Enemy(){
		
	}
	public int getID(){
		return ID;
	}
	public int getIntelligence(){
		return intelligence;
	}
	public void setIntelligence(int x){
		intelligence = x;
	}
	public void setDefense(int x,int index){
		defense[index] = x;
	}
	public boolean getStationarity(){
		return stationary;
	}
	public void setStationarity(boolean b){
		stationary = b;
	}
	public void respawn(){
		if(dood == true){
			if(Global.generator.nextInt(2) == 0){
				dood = false;
				HP = maxHP;
			}
		}
	}
	public int getPursuitIndex(){
		return pursuitIndex;
	}
	public void setPursuitIndex(int x){
		pursuitIndex = x;
	}
	public String predictAction(int[] playerPos, int[] myPos){
		double distance = new Point(playerPos[0],playerPos[1]).distance(myPos[0],myPos[1]);
		String prediction = null;
		
		if(distance < 70){
			prediction = predictPattern(RPGMain.speler.getMeleeActions());
		}
		else{
			prediction = predictPattern(RPGMain.speler.getRangedActions());
		}
		
		return prediction;
		
	}
	public String predictPattern(String actions){
		int patternNr = intelligence;

		String[] possibilities = {"head","torso","legs"};
		double[] occurences = new double[possibilities.length];
		double efficiency = 0;
		
		String prediction = possibilities[(int)(Math.random()*possibilities.length)];
		
		try{
			while(efficiency < (0.3-0.008*actions.length()) && patternNr > 0){
				String lastMoves = actions.substring(actions.length()-patternNr);
				String consideredMoves = actions.substring(0, actions.length()-patternNr);
				
				int i = consideredMoves.indexOf(lastMoves);
				while(i != -1){
					try{
						char s = consideredMoves.charAt(i+patternNr);
						//TODO check if torso is saved as t
						switch(s){
						case 'h': occurences[0]+=1; break;
						case 't': occurences[1]+=1; break;
						case 'l': occurences[2]+=1; break;
						}
						
						i = consideredMoves.indexOf(lastMoves,i+patternNr);
						
					} catch(IndexOutOfBoundsException exc){
						i = -1;
					}
				}
				for(int j=0;j<occurences.length;j++){
					if(occurences[j] > 0){
						occurences[j]*=1.0/((double)consideredMoves.length()/(double)(patternNr+1));
						if(occurences[j] >= efficiency){
							efficiency = occurences[j];
							prediction = possibilities[j];
						}
					}
				}
				patternNr--;
			}
		}catch(NullPointerException exc){
			
		}catch(IndexOutOfBoundsException exc){
			
		}
		return prediction;
	}
	public int[] determineMove(int[] playerPos,int[] myPos, int[] bestCoverPos, double bestCover, int[] bestHeightPos, double maxHeightModifier, boolean hasActed, HashMap<double[][],Double> covers, int mobIndex){
		
		// positions, distances and health perc get calculated, both of enemy and player
		// width and height of map window get calculated, path gets initiated
		int[] newPos = new int[2];
		newPos[0] = myPos[0];
		newPos[1] = myPos[1];
		double myHealthPerc = (double)HP/(double)maxHP;
		double plHealthPerc = (double)RPGMain.speler.getHP()/(double)RPGMain.speler.getMaxHP();
		double distance = new Point(playerPos[0],playerPos[1]).distance(myPos[0],myPos[1]);
		int width = GameFrameCanvas.battlefield.getWidth();
		int height = GameFrameCanvas.battlefield.getHeight();
		ArrayList<int[]> path = new ArrayList<int[]>();
		
		// in melee range
		if(distance*Battlefield.sizeInMeter/width < Data.abilities.get(0).getRange()[1]){
			switch(intelligence){
			// least intelligent, will always stay in melee range
			case 1:	break;
			// will stay in melee until below 33% health, then run away, or if better at ranged
			case 2: if(myHealthPerc < 0.34 || boog.getStrength() > wapen.getStrength()){
						// newPos = oldPos + movement*cos(theta) for x, sin(theta) for y
						// min, max so that it stays in playing field
						newPos[0] = Math.max(0,Math.min(width-2*Global.playerSize, myPos[0] + (int)(movement*((double)(myPos[0]-playerPos[0])/distance))));
						newPos[1] = Math.max(0,Math.min(height-2*Global.playerSize,myPos[1] + (int)(movement*((double)(myPos[1]-playerPos[1])/distance))));
					}
					break;
			case 3: if(bestCover > 0 && ((plHealthPerc-myHealthPerc) < 0.2 || (bestCover < 0.6 && boog.getStrength() > wapen.getStrength()))){
						newPos[0] = bestCoverPos[0];
						newPos[1] = bestCoverPos[1];
					}
					else if(maxHeightModifier > 0 && (!hasActed || boog.getStrength() > wapen.getStrength())){
						newPos[0] = bestHeightPos[0];
						newPos[1] = bestHeightPos[1];
					}
					else if(myHealthPerc < 0.34 || boog.getStrength() > wapen.getStrength()){
						newPos[0] = Math.max(0,Math.min(width-2*Global.playerSize, myPos[0] + (int)(movement*((double)(myPos[0]-playerPos[0])/distance))));
						newPos[1] = Math.max(0,Math.min(height-2*Global.playerSize,myPos[1] + (int)(movement*((double)(myPos[1]-playerPos[1])/distance))));
					}
			}
		}
		// out of melee range
		else{
			switch(intelligence){
			case 1: newPos[0] = myPos[0] - (int)(Math.min(distance - 2*Global.playerSize, movement)*((double)(myPos[0]-playerPos[0])/distance));
					newPos[1] = myPos[1] - (int)(Math.min(distance - 2*Global.playerSize, movement)*((double)(myPos[1]-playerPos[1])/distance));
					break;
			case 2: if(myHealthPerc < 0.34){
						// newPos = oldPos + movement*cos(theta) for x, sin(theta) for y
						// min, max so that it stays in playing field
						newPos[0] = Math.max(0,Math.min(width-2*Global.playerSize, myPos[0] + (int)(movement*((double)(myPos[0]-playerPos[0])/distance))));
						newPos[1] = Math.max(0,Math.min(height-2*Global.playerSize,myPos[1] + (int)(movement*((double)(myPos[1]-playerPos[1])/distance))));
					}
					else{
						newPos[0] = myPos[0] - (int)(Math.min(distance - 2*Global.playerSize, movement)*((double)(myPos[0]-playerPos[0])/distance));
						newPos[1] = myPos[1] - (int)(Math.min(distance - 2*Global.playerSize, movement)*((double)(myPos[1]-playerPos[1])/distance));
					}
					break;
					//TODO next cases
			}
		}
		path = Global.calculatePath(myPos, playerPos, (int)(movement*GameFrameCanvas.battlefield.getWidth()/Battlefield.sizeInMeter), covers,true);
		for(int j=0;j<path.size();j++){
			GameFrameCanvas.battlefield.updateBattlefield(covers, playerPos, path.get(j),mobIndex);
			try{
				Thread.sleep(1000/24);
			} catch(InterruptedException exc){
				
			}
			if(j == path.size()-1){
				return path.get(path.size()-1);
			}
		}
		return myPos;
	}
	public String determineAction(int[] playerPos, int[] myPos, boolean hasMoved){
		
		Random generator = new Random();
		
		// health perc and distance get calculated
		double enHealthPerc = (double)HP/(double)maxHP;
		double plHealthPerc = (double)RPGMain.speler.getHP()/(double)RPGMain.speler.getMaxHP();
		double distance = new Point(playerPos[0],playerPos[1]).distance(myPos[0],myPos[1])/GameFrameCanvas.battlefield.getWidth()*Battlefield.sizeInMeter;
		System.out.println("In determineAction");
		
		// stance is default at defensive
		String stance = " d";
		// possible targets to aim at
		String[] targets = {"head","torso","legs"};
		// check what target is best defended
		int strongest = 0;
		int defense = 0;
		for(int j=0;j<3;j++){
			if(defense < RPGMain.speler.getDefense(j)){
				defense = RPGMain.speler.getDefense(j);
				strongest = j;
			}
		}
		// choose other part to target at, so it is not the strongest one
		String toHit = targets[strongest];
		while(toHit.equalsIgnoreCase(targets[strongest])){
			toHit = targets[generator.nextInt(3)];
		}
		
		switch(intelligence){
		// always in offensive stance, uniform target
		case 1: if(distance < Data.abilities.get(0).getRange()[1]){
					return "hit " + targets[generator.nextInt(3)] + " o";
				}
				// if better at ranged: shoot, else charge
				else if(boog.getStrength() > wapen.getStrength()){
					return "shoot " + targets[generator.nextInt(3)] + " o";
				}
				else{
					return "charge " + targets[generator.nextInt(3)];
				}
		// if health > 30%, offensive, else into defensive, rest same as above
		case 2: if(enHealthPerc > 0.3){
					stance = " o";
				}
				if(distance < Data.abilities.get(0).getRange()[1]){
					return "hit " + targets[generator.nextInt(3)] + stance;
				}
				else if(boog.getStrength() > wapen.getStrength()){
					return "shoot " + targets[generator.nextInt(3)] + stance;
				}
				else{
					return "charge " + targets[generator.nextInt(3)];
				}
		// compares player and own health perc, will try to stun if possible, rest same as above	
		case 3: if((enHealthPerc-plHealthPerc) > 0.2){
					stance = " o";
				}
				if(distance < 35){
					if(hasStun()){
						//TODO see commentary on ability-class on Blackberry
						return null;
					}
					else{
						return "hit " + toHit + stance;
					}
				}
				else if(boog.getStrength() > wapen.getStrength()){
					return "shoot " + toHit + stance;
				}
				else{
					return "charge " + toHit;
				}
		
		case 4: 
		}
		
		
		return null;
	}
}
