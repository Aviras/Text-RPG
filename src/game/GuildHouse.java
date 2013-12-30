package game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import bsh.EvalError;

public class GuildHouse extends House {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int factionID;
	
	
	private static Logger logger = Logger.getLogger(GuildHouse.class);
	
	/*What does this class have to do?
	 * 1) Be of a certain type (Merchant's guild, Inventor's guild,...) DONE
	 * 2) Have some npcs with a certain hierarchy 
	 * 3) Have some advantages to players unlocked through reputation increase DONE
	 * 4) Consist of different rooms, accessible at different reputation levels DONE
	 * 5) Be interactive for the player when entering DONE
	 * 6) Player is able to carry a sign, like a tabard of some sort, which allows access or other advantages
	 * 7) Connection with artifacts: you can bring artifacts relevant to the guild type to them to increase your reputation and get specific rewards DONE
	 * 	  Why would you still bring them to a normal scientist? Bringing artifacts to the state improves general life, creates hope, increases reputation and rewards cash
	 * 8) 
	 */
	
	/* All guild houses have at least 4 rooms:
	 * 1) Common room at entrance
	 * 2) Supply room
	 * 3) Headmaster's study
	 * 4) Research room
	 * 
	 * What is the function of a GuildHouse compared to a normal house?
	 * Can bring in artifacts
	 * Specific interactivity from the faction
	 * Repeatable quests
	 * 
	 */
	
	public GuildHouse(String name, String description, String factionID, Element options){
		super(name, description, options);
		try{
			this.factionID = Integer.parseInt(factionID);
		} catch(NumberFormatException e){
			logger.error("Data parse error for factionID of Guild House " + name, e);
		}
		
		//Extra options
	}
	
	public int getFactionID(){
		return factionID;
	}

	@Override
	public void enter() throws InterruptedException {
		RPGMain.printText(true, description);
		Global.pauseProg();
		
		while(true){
			int newRoom = rooms.get(currentRoom).enterRoom();
			if(newRoom == -1){
				currentRoom = 0;
				break;
			}
			else if(newRoom == -2){
				//specific function
				String function = rooms.get(currentRoom).getFunction();
				logger.debug("In specific function: " + function);
				
				if(function.equalsIgnoreCase("artifactTest")){
					handleQuestsArtifacts();
				}
				else if(function.equalsIgnoreCase("supply room")){
					supplyRoom(newRoom);
				}
				else if(function.equalsIgnoreCase("Interactive")){
					String scriptPath = Data.guilds.get(factionID).getScriptPath();
					try {
						Global.beanShell.source(scriptPath);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (EvalError e) {
						e.printStackTrace();
					}
				}
			}
			else{
				currentRoom = newRoom;
			}
		}

	}
	
	private void handleQuestsArtifacts(){
		//TODO accept artifacts and give quests
		RPGMain.printText(true, new String[]{"* Bring in an ", "artifact"}, new String[]{"regular","bold"});
		if(!Data.guilds.get(factionID).getQuestIDs().isEmpty()){
			RPGMain.printText(true, new String[]{"* Ask about ", "quests"}, new String[]{"regular","bold"});
		}
		RPGMain.printText(false, ">");
		String action = RPGMain.waitForMessage().toLowerCase();
		
		if(action.equalsIgnoreCase("artifact")){
			while(true){
				LinkedHashMap<Item, Integer> inv = RPGMain.speler.getInventory();
				ArrayList<Integer> artifactIDs = new ArrayList<Integer>();
				
				int j = 1;
				for(Item i: inv.keySet()){
					if(i.getName().startsWith("Letter")){
						RPGMain.printText(true, j + ": Report about " + i.getName().split(": ")[1]);
						artifactIDs.add(-i.getID());
						j++;
					}
				}
				if(j > 1){
					RPGMain.printText(false, j + ": Cancel\n>");
				}
				else{
					RPGMain.printText(true,"You have nothing to report.");
					Global.pauseProg();
					break;
				}
				
				try{
					int choice = Integer.parseInt(RPGMain.waitForMessage());
					
					if(choice > 0 && choice <= j){
						int artifactID = artifactIDs.get(choice-1);
						
						Artifact a = Data.artifacts.get(artifactID);
						
						int discoveredInfoPhase = a.getDiscoveredInfo();
						
						//TODO more variation
						switch(discoveredInfoPhase){
						case 1:	RPGMain.printText(true, "The historian carefully reads your report. \'It is obvious we need to study this further,\', he says, \'but you help our people as a whole by this discovery.\'");
								break;
						case 2: RPGMain.printText(true, "The historian carefully reads your report, and is visibly intrigued. \'You seem to have found a great deal already by yourself,\' he says, \'but I think we are not yet at the bottom of this. Either way, your research cannot go unrewarded, for it is invaluable to the further research. For that, you have my sincere gratitude.\'");
								break;
						case 3: RPGMain.printText(true, "The historian carefully reads your report, and is enthused by your findings! \'This is incredible, " + RPGMain.speler.getName() + "! Not only the artifact itself, but also your splendid research on it!\' Visibly thrilled, " + name + " goes looking after your reward all the while thinking out loud what implications this might have for the people.");
								break;
						}
						
						int modifier = 0;
						
						if(RPGMain.speler.hasItem(a.getName())){
							RPGMain.printText(false, "Would you also be so kind to hand in " + a.getName() + ", so that it can be further studied?, he asks you with a look of dedication. [y/n]\n>");
							String s = RPGMain.waitForMessage().toLowerCase();
							
							if(s.startsWith("y")){
								modifier = 1;
								RPGMain.speler.delInventoryItem(a);
							}
						}
						
						//give reputation and perhaps reward, or notify player of rewards that have now become available
						int reputation = (int)(a.getWorth()*discoveredInfoPhase + modifier*a.getWorth());
						
						Global.pauseProg(2000, "Your reputation with " + name + " has increased by " + reputation + ".");
						
						//check if player now has access to new ROOMS
						for(Room r: rooms.values()){
							if(r.getRequiredReputation() > Data.guilds.get(factionID).getReputation() && r.getRequiredReputation() <= (Data.guilds.get(factionID).getReputation() + reputation)){
								RPGMain.printText(true, "You now have access to room: " + r.getName() + ".");
							}
						}
						
						//check if player now has access to new QUESTS
						for(int qID: Data.guilds.get(factionID).getQuestIDs().keySet()){
							int reqRep = Data.guilds.get(factionID).getQuestIDs().get(qID);
							if(reqRep > Data.guilds.get(factionID).getReputation() && reqRep <= (Data.guilds.get(factionID).getReputation() + reputation)){
								RPGMain.printText(true, "You now have access to quest: " + Data.quests.get(qID).getNaam() + ".");
							}
						}
						
						//check if player now has access to new ITEMS
						
						
						Data.guilds.get(factionID).addReputation(reputation);
						
						//destroy the letter
						Item letter = null;
						for(Item i: RPGMain.speler.getInventory().keySet()){
							if(i.getID() == -artifactID && i.getName().startsWith("Letter")){
								logger.debug("Deleting letter with ID " + i.getID());
								letter = i;
								break;
							}
						}
						if(letter != null){
							RPGMain.speler.delInventoryItem(letter);
						}
						
						//there was only one artifact to report, so stop method
						if(j == 2){
							break;
						}
					}
					else{
						RPGMain.printText(true, "Not a valid option from the list.");
					}
				} catch(NumberFormatException e){
					RPGMain.printText(true, "Not a number.");
					continue;
				}
			}
		}
		else if(action.startsWith("quest") && !Data.guilds.get(factionID).getQuestIDs().isEmpty()){
			while(true){
				int j = 1;
				for(int id: Data.guilds.get(factionID).getQuestIDs().keySet()){
					if(Data.guilds.get(factionID).getReputation() >= Data.guilds.get(factionID).getQuestIDs().get(id)){
						if(RPGMain.speler.getQuest(id) == null){
							RPGMain.printText(true, j + ": Ask about " + Data.quests.get(id).getNaam());
						}
						else if(RPGMain.speler.getQuest(id).getCompleted()){
							RPGMain.printText(true, j + ": Report about " + Data.quests.get(id).getNaam());
						}
						else{
							RPGMain.printText(true, j + ": Repeat assignment about " + Data.quests.get(id).getNaam());
						}
						j++;
					}
				}
				RPGMain.printText(true, j + ": Cancel");
				try{
					int choice = Integer.parseInt(RPGMain.waitForMessage());
					if(choice > 0 && choice < j){
						NPC questMaster = new NPC(-1, "Quest Master", "male", -1, null, null);
						Document doc = Data.parser.build(new File("Data/QuestDialog.xml"));
						Element root = doc.getRootElement();
						List<Element> objects = root.getChildren();
						Iterator<Element> i = objects.iterator();
						Element myConvTree = null;
						String talkType = "";
						int answerValue = 0;
						for(int k=0;k<Data.guilds.get(factionID).getQuestIDs().size();k++){
							if(k == choice-1){
								answerValue = Data.guilds.get(factionID).getQuestIDs().get(k);
								break;
							}
						}
						while(i.hasNext()){
							Element e = (Element)i.next();
							if(e.getAttributeValue("id").equalsIgnoreCase("" + answerValue)){
								logger.debug("answervalue: " + answerValue + " isQuestCompleted: " + RPGMain.speler.isQuestCompleted(answerValue));
								if(!RPGMain.speler.questCompleted(answerValue) && RPGMain.speler.getQuest(answerValue) == null){
									myConvTree = e.getChild("startdialog");
									talkType = "newQuest";
								}
								else if(RPGMain.speler.getQuest(answerValue).getCompleted()){
									myConvTree = e.getChild("enddialog");
									talkType = "complQuest";
								}
								else{
									myConvTree = e.getChild("busydialog");
									talkType = "busyQuest";
								}
								break;
							}
						}
						questMaster.converse(myConvTree, talkType, answerValue);
					}
					else if(choice == j){
						break;
					}
					else{
						RPGMain.printText(true, "There is no quest corresponding to that number.");
					}
				} catch(NumberFormatException e){
					RPGMain.printText(true, "Not a number.");
					continue;
				} catch (JDOMException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else{
			RPGMain.printText(true, "Not a valid action.");
		}
	}
	
	private void supplyRoom(int newRoom){
		LinkedHashMap<Item, Integer> items = new LinkedHashMap<Item, Integer>();
		try {
			//TODO load stuff in the beginning, now it gets reset every time the function gets called, infinite supply
			logger.debug("Inside supply function.");
			Document doc = Data.parser.build(new File("Data/GuildProgress.xml"));
			Element root = doc.getRootElement();
			List<Element> objects = root.getChildren();
			Iterator<Element> it = objects.iterator();
			while(it.hasNext()){
				Element el = it.next();
				if(Integer.parseInt(el.getAttributeValue("id")) == factionID){
					List<Element> perks = el.getChildren("Perk");
					Iterator<Element> perkIt = perks.iterator();
					while(perkIt.hasNext()){
						Element perk = perkIt.next();
						int reqReputation = Integer.parseInt(perk.getAttributeValue("reputation"));
						int reqPower = Integer.parseInt(perk.getAttributeValue("power"));
						
						if(Data.guilds.get(factionID).getPower() >= reqPower && Data.guilds.get(factionID).getReputation() >= reqReputation){
							logger.debug("Inside supply function, past constraints");
							String[] s = perk.getTextTrim().split(";");
							for(String u: s){
								String type = u.split(":")[0];
								int id = Integer.parseInt(u.split(":")[1].split(",")[0]);
								int amount = 1;
								try{
									amount = Integer.parseInt(u.split(":")[1].split(",")[1]);
								} catch(NumberFormatException e){
								} catch(ArrayIndexOutOfBoundsException e){
								}
								logger.debug("Found item " + type + ": " + id);
								items.put((Item)Data.getObject(type, id), amount);
							}
						}
					}
				}
			}
			if(items.size() == 0){
				RPGMain.printText(true, "You currently don't have access to any items.");
			}
			while(items.size() > 0){
				RPGMain.printText(true, "You currently have access to the following items:");
				int j = 1;
				for(Item i: items.keySet()){
					RPGMain.printText(true, j + ": " + i.getName() + " (" + items.get(i) + ")");
					j++;
				}
				RPGMain.printText(false, "(Type \"info\" <index>, \"take\" <index> or \"cancel\")\n>");
				String action = RPGMain.waitForMessage().toLowerCase();
				
				if(action.startsWith("info")){
					try{
						int index = Integer.parseInt(action.split(" ")[1]);
						j = 1;
						for(Item i: items.keySet()){
							if(j == index){
								i.showInfo();
								Global.pauseProg();
								break;
							}
							j++;
						}
						if(j > items.size()){
							RPGMain.printText(true, "The entered number was not one of the possibilities.");
						}
					} catch(NumberFormatException e){
						RPGMain.printText(true, "Not a valid number.");
					}
				}
				else if(action.startsWith("take")){
					try{
						int index = Integer.parseInt(action.split(" ")[1]);
						logger.debug("Index: " + index);
						j = 1;
						for(Item i: items.keySet()){
							if(j == index){
								logger.debug("Found object, " + i.getName());
								if(items.get(i) > 1){
									while(true){
										RPGMain.printText(false, "How many? (max. " + items.get(i) + ")\n>");
										try{
											int chosenAmount = Integer.parseInt(RPGMain.waitForMessage());
											if(chosenAmount > 0 && chosenAmount <= items.get(i)){
												RPGMain.speler.addInventoryItem(i, chosenAmount);
												items.put(i, items.get(i) - chosenAmount);
												if(items.get(i) == 0){
													items.remove(i);
												}
												break;
											}
											else if(chosenAmount == 0){
												break;
											}
											else{
												RPGMain.printText(true,"Not a correct amount.");
											}
										} catch(NumberFormatException e){
											RPGMain.printText(true, "Not a valid number.");
										}
									}
								}
								else{
									RPGMain.printText(true, "You recieve a " + i.getName() + ".");
									RPGMain.speler.addInventoryItem(i);
									items.remove(i);
								}
								j--;
								break;
							}
							else{
								j++;
							}
						}
						if(j > items.size()){
							RPGMain.printText(true, "The entered number was not one of the possibilities.");
						}
					} catch(NumberFormatException e){
						RPGMain.printText(true, "Not a valid number.");
					}
				}
				else if(action.startsWith("cancel")){
					if(!rooms.get(currentRoom).hasItems() && !rooms.get(currentRoom).hasNPCs()){
						while(newRoom == -2){
							for(int i: rooms.get(currentRoom).getRoomConnections()){
								RPGMain.printText(true, new String[]{"* ", "Enter ", rooms.get(i).getName()}, new String[]{"regular","bold","regular"});
							}
							RPGMain.printText(false, ">");
							String destination = RPGMain.waitForMessage().toLowerCase();
							
							if(destination.startsWith("enter")){
								try{
									for(int i: rooms.get(currentRoom).getRoomConnections()){
										if(rooms.get(i).getName().toLowerCase().contains(destination.split(" ")[1])){
											currentRoom = i;
											newRoom = i;
											break;
										}
									}
									if(newRoom == -2){
										RPGMain.printText(true, "Room " + destination.split(" ")[1] + " does not exist.");
									}
								} catch(ArrayIndexOutOfBoundsException e){
									RPGMain.printText(true, "You must add a destination, enter <name>.");
									continue;
								}
							}
							else{
								RPGMain.printText(true, "Not a valid command. Use \"enter <name>\".");
							}
						}
					}
					break;
				}
			}
		} catch (JDOMException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch(NumberFormatException e){
			logger.error(e);
		}
	}

}
