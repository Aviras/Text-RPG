package game;
import java.io.*;
import java.util.ArrayList;

import org.apache.log4j.Logger;
public class Inn extends DistrictLocation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4843765633893197943L;
	private static Logger logger = Logger.getLogger(Inn.class);
	/**
	 * 
	 */
	private NPC innkeeper;
	
	public Inn(String name, String description, String innkeeperID){
		this.name = name;
		this.description = description;
		
		try{
			innkeeper = Data.NPCs.get(Integer.parseInt(innkeeperID));
		} catch(NumberFormatException e){
		}
	}
	public Inn(String name, String description, String innkeeperID, String npcIDs){
		this.name = name;
		this.description = description;
		
		try{
			innkeeper = Data.NPCs.get(Integer.parseInt(innkeeperID));
		}catch(NumberFormatException e){
		}
		
		npcs = new ArrayList<NPC>();
		String[] parts = npcIDs.split(";");
		for(String s: parts){
			try{
				npcs.add(Data.NPCs.get(Integer.parseInt(s)));
			} catch(NumberFormatException e){
			}
		}
	}
	public void enter() throws InterruptedException{
		RPGMain.printText(true, description);
		Global.pauseProg();
		if(npcs != null && !npcs.isEmpty()){
			while(true){
				for(NPC n: npcs){
					RPGMain.printText(true, new String[]{"* ", "Talk to ", n.getFullName()}, new String[]{"regular","bold","regular"});
				}
				RPGMain.printText(false, new String[]{"* ", "Visit", " the bar\n* Cancel\n>"}, new String[]{"regular","bold","regular"});
				
				String input = RPGMain.waitForMessage().toLowerCase();
				if(input.startsWith("talk to")){
					String name = RPGMain.upperCaseSingle(input.split(" ")[2],0);
					
					for(NPC n: npcs){
						if(n.getName().equalsIgnoreCase(name)){
							logger.debug("talking to " + name);
							n.talk();
							break;
						}
					}
				}
				else if(input.startsWith("visit")){
					bar();
				}
				else if(input.equalsIgnoreCase("cancel")){
					RPGMain.printText(true, "You walk out of the " + name + ".");
					break;
				}
			}
		}
		else{
			bar();
		}
	}
	
	public void bar() throws InterruptedException{
		int actie = 0;
		while(true){
			RPGMain.printText(false,"Can I offer you a drink, or a bed perhaps to stay for the night?\n" +
									"1: Have a drink (1 gold piece)\n" +
									"2: Stay for the night (5 gold pieces)\n" +
									"3: Ask for a warm meal (3 gold pieces)\n" +
									"4: Talk to " + innkeeper.getName() + ", the Innkeeper\n" +
									"5: Go back out\n" +
									"Your gold: " + RPGMain.speler.getGoud() + "\n>");
			try{
				actie = Integer.parseInt(RPGMain.waitForMessage());
			}catch(NumberFormatException e){
				RPGMain.printText(true,"Not a valid option.");
				Global.pauseProg();
				continue;
			}
			switch(actie){
			case 1: if(RPGMain.speler.getGoud()>0){
						RPGMain.printText(true,"Beer tastes like heaven after a hard days work, or even without.");
						RPGMain.speler.addThirst(-10);
						RPGMain.speler.addGoud(-1);
						int dronken = 0;
						String[] drunkText = {"Beer has no effect on you, you're such a beast.","People are looking at you strangely, but you don't care.","You are a master of tabledancing, or so the beergoggles tell you."};
						while(true){
							if(RPGMain.speler.getGoud()>0){
								RPGMain.printText(false,"Have another? [y/n]\n>");
								String keuze = RPGMain.waitForMessage();
								if(keuze.equalsIgnoreCase("y") && dronken < 3){
									RPGMain.printText(true,drunkText[dronken]);
									RPGMain.speler.addGoud(-1);
									RPGMain.speler.addThirst(-10);
									dronken++;
								}
								else if(keuze.equalsIgnoreCase("n")){
									break;
								}
								if(dronken > 2){
									Global.pauseProg();
									RPGMain.printText(true,"Passed out on your barstool, the innkeeper carries you upstairs.\nHe leaves you on a room, taking 6 gold pieces with him, for the crap you pulled.");
									RPGMain.printText(true,"HP set to 75% of maximum HP due to hangover.");
									RPGMain.speler.addGoud(-6);
									RPGMain.speler.setHP((int)Math.floor(0.75*RPGMain.speler.getMaxHP()));
									if(RPGMain.speler.getGoud() < 0){
										RPGMain.printText(true,"You now owe the innkeeper " + -RPGMain.speler.getGoud() + " gold pieces.");
									}
									Global.pauseProg();
									break;
								}
							}
							else{
								RPGMain.printText(true,"You don't have enough gold pieces.");
								break;
							}
						}
					}
					else{
						RPGMain.printText(true,"You don't have enough gold pieces.");
					}
					break;
					
					
			case 2: if(RPGMain.speler.getGoud()>4){
						RPGMain.printText(true,"You fall asleep, and wake up reborn.\nHP maxed out.");
						RPGMain.speler.addGoud(-5);
						RPGMain.speler.setHP(RPGMain.speler.getMaxHP());
						RPGMain.speler.resetFitness();
						RPGMain.speler.dimMovement(-100);
						DayLightThread.rest(5);
						Global.pauseProg();
					}
					else{
						RPGMain.printText(true,"You don't have enough gold pieces.");
					}
					break;
					
					
			case 3: if(RPGMain.speler.getGoud() >= 3){
						RPGMain.speler.addHunger(-60);
						RPGMain.speler.addGoud(-3);
						RPGMain.printText(true, "You enjoy a nice, hot meal. You feel better already.");
					}
					else{
						RPGMain.printText(true, "Sorry, but you can't seem to afford it.");
					}
					break;
			case 4: innkeeper.talk();
					break;
			default: break;
			}
			if(actie == 5){
				RPGMain.printText(true, "You walk back out of the " + name + ".");
				break;
			}
		}
	}
}
