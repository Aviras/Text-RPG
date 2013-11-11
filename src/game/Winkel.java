package game;

import java.io.*;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class Winkel extends DistrictLocation implements Serializable {

	private static final long serialVersionUID = -5838400015040263384L;
	protected int vriendelijkheid;
	protected ArrayList<Item> waar;
	protected NPC shopkeeper;
	protected boolean busted = false;
	protected boolean isLocked = false;
	protected int[] position;
	
	public Winkel(String name,int kindness, String description, String itemsPresent, String npcID){
		this.name = name;
		vriendelijkheid = kindness;
		this.description = description;
		waar = new ArrayList<Item>();
		String[] itemsSplit = itemsPresent.split(";");
		for(String s: itemsSplit){
			String[] split = s.split(":");
			try{
				String[] items = split[1].split(",");
				int[] ids = new int[items.length];
				try{
					for(int j=0;j<items.length;j++){
						ids[j] = Integer.parseInt(items[j]);
					}
					addItems(split[0],ids);
				} catch(NumberFormatException e){
				}
			} catch(ArrayIndexOutOfBoundsException e){
			}
		}
		try{
			shopkeeper = Data.NPCs.get(Integer.parseInt(npcID));
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	public void addItems(String type, int[] items){
		for(int j=0;j<items.length;j++){
			if(type.equalsIgnoreCase("equipment")){
				waar.add(Data.equipment.get(items[j]));
			}
			else if(type.equalsIgnoreCase("potion")){
				waar.add(Data.potions.get(items[j]));
			}
			else if(type.equalsIgnoreCase("item")){
				waar.add(Data.items.get(items[j]));
			}
			else if(type.equalsIgnoreCase("consumable")){
				waar.add(Data.consumables.get(items[j]));
			}
		}
	}
	
	public void setPosition(int[] p){
		position[0] = p[0];
		position[1] = p[1];
	}
	
	public void enter() throws InterruptedException{
		
		RPGMain.printText(true, description);
		
		isLocked = false;

		while(true){
			RPGMain.printText(false,"1: Check out his wares\n" +
									"2: Sell items\n" +
									"3: Talk to " + name + "\n" +
									"4: Leave\n>");
			try{
				int keuze = Integer.parseInt(RPGMain.waitForMessage());
				switch(keuze){
				case 1: RPGMain.printText(true,"\nI hope my wares can please you.\nIn total I've got " + waar.size() + " items for sale.");
						verkoop(); break;
				case 2: vendorItem(); break;
				case 3: shopkeeper.talk(); break;
				default: break;
				}
				if(keuze == 4){
					break;
				}
			}catch(NumberFormatException IME){
				RPGMain.printText(true,"Not a valid option.");
				Global.pauseProg();
				continue;
			}
		}
		RPGMain.printText(true,"You walk out of " + name + ".");
	}
	//komt vanuit WINKEL menu
	public void verkoop() throws InterruptedException {
		String action = null;
		int keuze = 0;
		String textMode = "regular";
		double reputation = HopeSimulator.getReputation(RPGMain.speler.getCurrentPosition()[0], RPGMain.speler.getCurrentPosition()[1]);
		if(reputation > 0 || (reputation > 0 && RPGMain.speler.getCharisma()/100.0 > 0.1)){
			textMode = "greenbold";
		}
		else if(reputation < 0 && RPGMain.speler.getCharisma()/100.0 > reputation/20.0){
			textMode = "orangebold";
		}
		else if(reputation < 0){
			textMode = "redbold";
		}
		while(true){
			for(int j=0;j<waar.size();j++){
				try{
					RPGMain.printText(true,new String[]{(j+1) +": " + waar.get(j).getName() + " for ",  "" + Math.round(waar.get(j).getCost()*(1.0-RPGMain.speler.getCharisma()/100.0) - reputation/20.0), " gold pieces."}, new String[]{"regular",textMode,"regular"});
				}catch(NullPointerException ex){
					
				}
			}
			RPGMain.printText(true,"Your gold: " + 	RPGMain.speler.getGoud() + ".");
			RPGMain.printText(false,"(Type \"buy <index>\", \"info <index>\" or \"cancel\".)\n>");
			action = RPGMain.waitForMessage();
			/*
			 * 
			 * COMMAND STARTS WITH BUY
			 * 
			 */
			if(action.startsWith("buy")){
				try{
					keuze = Integer.parseInt(action.substring(4).trim());
				} catch(NumberFormatException e){
					RPGMain.printText(true,"Index is not a number.");
					continue;
				}
				if(keuze < (waar.size()+1)){
					if(RPGMain.speler.getGoud() >= Math.round(waar.get(keuze-1).getCost()*(1.0-RPGMain.speler.getCharisma()/100.0-reputation/20.0))){// genoeg geld
						if(RPGMain.speler.getInventorySpace() >= waar.get(keuze-1).getWeight()){ // plaats in de inventory
							RPGMain.printText(true,"You have succesfully purchased " + waar.get(keuze-1).getName() + ".");
							
							RPGMain.speler.addGoud(-(int)Math.round(waar.get(keuze-1).getCost()*(1.0-RPGMain.speler.getCharisma()/100.0 - reputation/20.0)));
							RPGMain.speler.addInventoryItem(waar.get(keuze-1));
							
							// if the item is armour/weapon
							if(waar.get(keuze-1).getClass().equals(Equipment.class)){
								RPGMain.printText(false,"Equip it ? [y/n] (Current: " + RPGMain.speler.getEquipped(((Equipment)waar.get(keuze-1)).getType()).getName() + ")\n>");
								if(RPGMain.waitForMessage().equalsIgnoreCase("y")){
									RPGMain.speler.equipItem(RPGMain.speler.getInventoryItemIndex(waar.get(keuze-1).getName()));
								}
							}
							RPGMain.printText(false,"Buy something else? [y/n]\n>");
							String actie = RPGMain.waitForMessage();
							if(!actie.equalsIgnoreCase("y")){
								break;
							}
						}
						else{
							RPGMain.printText(true,"You don't have enough room in your inventory.");
						}
					}
					else{
						RPGMain.printText(false,"You do not have enough gold pieces for this item.\nBuy something else? [y/n]\n>");
						String actie = RPGMain.waitForMessage();
						if(!actie.equalsIgnoreCase("y")){
							break;
						}
					}
				}
			}
			/*
			 * 
			 * COMMAND STARTS WITH INFO
			 * 
			 */
			else if(action.startsWith("info")){
				try{
					keuze = Integer.parseInt(action.substring(5).trim());
				} catch(NumberFormatException e){
					RPGMain.printText(true,"Index is not a number.");
					continue;
				}
				try{
					waar.get(keuze-1).showInfo();
					Global.pauseProg();
				} catch(NullPointerException e){
					RPGMain.printText(true,"Not a valid option.");
				}
			}
			
			else if(action.equalsIgnoreCase("cancel")){
				break;
			}
			
			else{
				RPGMain.printText(true,"Not a valid option.");
			}
		}
	}
	
	public void vendorItem() throws InterruptedException{
		int index = 0;
		int keuze = 0;
		String textMode = "regular";
		double reputation = HopeSimulator.getReputation(RPGMain.speler.getCurrentPosition()[0], RPGMain.speler.getCurrentPosition()[1]);
		if(reputation > 0 || (reputation > 0 && RPGMain.speler.getCharisma()/100.0 > 0.1)){
			textMode = "greenbold";
		}
		else if(reputation < 0 && RPGMain.speler.getCharisma()/100.0 > reputation/20.0){
			textMode = "orangebold";
		}
		else if(reputation < 0){
			textMode = "redbold";
		}
		while(true){
			RPGMain.printText(true,"Sell items.");
			for(int j = 0;j<RPGMain.speler.getInventorySize();j++){
				if(RPGMain.speler.getInventoryItem(j) != null){
					RPGMain.printText(true, new String[]{(j+1) + ": " + RPGMain.speler.getInventoryItem(j).getName() + " worth ", "" + (int)Math.floor(RPGMain.speler.getInventoryItem(j).getCost()/2.0*(1.0+RPGMain.speler.getCharisma()/100.0 + reputation/20.0)), " gold pieces."}, new String[]{"regular",textMode,"regular"});
					index = j+1;
				}
			}
			RPGMain.printText(false,(index+1) + ": Cancel\n>");
			try{
				keuze = Integer.parseInt(RPGMain.waitForMessage())-1;
				
				if(keuze != index){
					try{
						int amount = RPGMain.speler.getItemNumber(RPGMain.speler.getInventoryItem(keuze));
						if(amount > 1){
							while(true){
								RPGMain.printText(false, "How many? (Max. " + amount + ")\n>");
								int x = Integer.parseInt(RPGMain.waitForMessage());
								if(x <= amount && x >= 0){
									amount = x;
									break;
								}
								else{
									RPGMain.printText(true, "You cannot sell that amount.");
								}
							}
						}
						if(amount != 0){
							RPGMain.printText(false,"Really sell " + amount + " " + RPGMain.speler.getInventoryItem(keuze).getName() + "? [y/n]\n>");
							if(RPGMain.waitForMessage().toLowerCase().startsWith("y")){
								RPGMain.printText(true,"You have succesfully sold " + amount + " " + RPGMain.speler.getInventoryItem(keuze).getName() + ".\n" +
										(int)Math.floor(RPGMain.speler.getInventoryItem(keuze).getCost()/2*(1.0+RPGMain.speler.getCharisma()/100.0 + reputation/20.0))*amount + " Gold pieces added.");
								RPGMain.speler.addGoud((int)Math.floor(RPGMain.speler.getInventoryItem(keuze).getCost()/2*(1.0+RPGMain.speler.getCharisma()/100.0 + reputation/20.0))*amount);
								RPGMain.speler.delInventoryItem(keuze,amount);
								Global.pauseProg();
								
								RPGMain.printText(false, "Do you want to sell something else? [y/n]\n>");
								String actie = RPGMain.waitForMessage();
								if(!actie.startsWith("y")){
									break;
								}
							}
						}
					} catch(NullPointerException np){
						RPGMain.printText(true,"Not a valid option.\n");
					}
				}
				else{
					break;
				}
			}catch(NumberFormatException IME){
				RPGMain.printText(true,"Not a valid option.");
				Global.pauseProg();
				continue;
			}
		}
	}
	
	public void trespass() throws InterruptedException{
		if(WeatherSimulator.getSolarIntensity(position[0], position[1]) > 0){
			RPGMain.printText(true,"The sun has risen, meaning the smith has returned.");
			Global.pauseProg(2000);
			return;
		}
		int locked = Global.generator.nextInt(3);
		if(locked > 1 && !isLocked){
			RPGMain.printText(true, "The door is locked.");
			isLocked = true;
			Global.pauseProg();
		}
		
		busted = false;
		if(!isLocked){
			double time = 15;
			// mean 0.0, std dev 1.0
			double law = Global.generator.nextGaussian();
			time = Math.abs(time*law);
			Busted trespass = new Busted((int)time);
			RPGMain.printText(true,"You managed to get inside.");
			while(true){
				if(busted == false){
					for(int j=0;j<waar.size();j++){
						try{
							RPGMain.printText(true,(j+1) +": " + waar.get(j).getName() + " worth " +  waar.get(j).getCost() + " gold pieces.");
						}catch(NullPointerException ex){
						}
					}
					RPGMain.printText(false,"(Type \"take <index>\" or \"flee\")\n>");
					String command = RPGMain.waitForMessage().toLowerCase();
					if(busted == true){
						command = "flee";
					}
					if(command.startsWith("take")){
						try{
							int index = Integer.parseInt(command.substring(5));
							RPGMain.speler.addInventoryItem(waar.get(index-1));
							RPGMain.printText(true,"You have succesfully stolen " + waar.get(index-1).getName() + ".");
							trespass.stopThread();
							Global.pauseProg();
							break;
						} catch(NumberFormatException e){
							RPGMain.printText(true,"Index is not a number.");
							continue;
						}
					}
					else if(command.equalsIgnoreCase("flee")){
						trespass.stopThread();
					}
				}
				break;
			}
		}
	}
	class Busted extends Thread{
		int sleep;
		private boolean stop = false;
		public Busted(int time){
			sleep = time*1000;
			start();
		}
		public void run(){
				try {
					sleep(sleep);
				} catch (InterruptedException e) {
				}
				if(stop == false){ // CHECK IF PLAYER HAS ALREADY ESCAPED
					RPGMain.recieveMessage("u");
					RPGMain.printText(true,"\nYou are caught trespassing!");
					busted = true;
					String[] telwoorden = {"st","nd","rd"};
					RPGMain.speler.addCaughtTrespassing(1);
					int timesCaught = RPGMain.speler.getCaughtTrespassing();
					try{
						RPGMain.printText(true,"This is the " + timesCaught + telwoorden[timesCaught-1] + " time you got caught.");
					} catch(ArrayIndexOutOfBoundsException e){
						RPGMain.printText(true,"This is the " + timesCaught + "th time you got caught.");
					}
					/*
					 * PAYING OF THE FINE
					 */
					double fine = Math.pow((double)timesCaught, (double)timesCaught) + 3.0;
					int aGold = RPGMain.speler.getGoud();
					RPGMain.printText(true,"Amount to pay: " + fine + ".");
					Global.pauseProg(1500);
					if(fine > aGold){
						RPGMain.printText(true,"You have an insufficient amount of money to buy off your sentence.\nYou will thereby be sent to prison, your current gold and items removed until the total of the fine is collected.");
					}
					/*
					 * TRY TO RANSOM
					 * 
					 */
					int amountRansom;
					while(true){
						try{
							amountRansom = Integer.parseInt(JOptionPane.showInputDialog(null, "Try to ransom for how much gold?\nYour gold: " + aGold + ".", "Ransom", JOptionPane.QUESTION_MESSAGE));
							break;
						} catch(NumberFormatException e){
						}
					}
					if(amountRansom > aGold){
						amountRansom = aGold;
						RPGMain.printText(true,"Amount adjusted to " + aGold);
					}
					if(amountRansom > 0){
						// ABILITY TO RANSOM: RANDOM + CHARISMA + RELATION AMOUNT OFFERED TO FINE
						/*
						 * Stel charisma 4,fine 10,amountRansom 5
						 * ransom is dan rand + 2 - 1 = rand + 1, gemiddeld 11, 40% succes
						 */
						int ransom = Global.generator.nextInt(20) + (int)0.4*RPGMain.speler.getCharisma() - (int)(fine/2/amountRansom);
						if(ransom > 13){
							RPGMain.printText(true,"Succesful. You both agree this never happened.\nWhat charisma and some luck can do.");
							RPGMain.printText(true,"You lose " + amountRansom + " gold.");
							RPGMain.speler.addGoud(-amountRansom);
							RPGMain.speler.addCaughtTrespassing(-1);
							return;
						}
						else{ // RANSOM FAILED
							RPGMain.printText(true,"The man looks at the money and shakes his head.\nBetter pay up.");
							payFine((int)fine,aGold);
						}
					}
					else{// no ransom money
						RPGMain.printText(true,"You choose not to resist, and pay the fine.");
						payFine((int)fine,aGold);
					}
					RPGMain.printText(true,"...");
				}
		}
		private void payFine(int fine,int aGold){
			RPGMain.speler.addGoud(-(int)fine);
			int paid = fine;
			if(fine > aGold){
				paid = aGold;
				int j = 0;
				while((fine-paid) > 0){// LOSE INV ITEMS UNTIL FINE IS PAYED, OR PLAYER IS BROKE
					try{
						paid+=RPGMain.speler.getInventoryItem(j).getCost();
						RPGMain.printText(true,"Lost " + RPGMain.speler.getInventoryItem(j).getName() + ".");
						RPGMain.speler.delInventoryItem(j);
					} catch(NullPointerException np){
						j++;
					}
					if(j == RPGMain.speler.getInventorySize()){
						break;
					}
				}
			}
			RPGMain.printText(true,"Lost " + paid + " gold in cash and property.");
		}
		public void stopThread(){
			stop = true;
		}

	}
}
