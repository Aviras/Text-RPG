package game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	 * 1) Be of a certain type (Merchant's guild, Inventor's guild,...)
	 * 2) Have some npcs with a certain hierarchy
	 * 3) Have some advantages to players unlocked through reputation increase
	 * 4) Consist of different rooms, accessible at different reputation levels
	 * 5) Be interactive for the player when entering
	 * 6) Player is able to carry a sign, like a tabard of some sort, which allows access or other advantages
	 * 7) Connection with artifacts: you can bring artifacts relevant to the guild type to them to increase your reputation and get specific rewards
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
				if(function.equalsIgnoreCase("Supply")){
					LinkedHashMap<Item, Integer> items = new LinkedHashMap<Item, Integer>();
					try {
						Document doc = Data.parser.build(new File("Data/GuildProgress.xml"));
						Element root = doc.getRootElement();
						List<Element> objects = root.getChildren();
						Iterator<Element> it = objects.iterator();
						while(it.hasNext()){
							Element el = it.next();
							int reqReputation = Integer.parseInt(el.getAttributeValue("reputation"));
							int reqPower = Integer.parseInt(el.getAttributeValue("power"));
							
							if(Data.guilds.get(factionID).getPower() >= reqPower && Data.guilds.get(factionID).getReputation() >= reqReputation){
								String[] s = el.getTextTrim().split(";");
								for(String u: s){
									String type = u.split(":")[0];
									int id = Integer.parseInt(u.split(":")[1].split(",")[0]);
									int amount = 1;
									try{
										amount = Integer.parseInt(u.split(":")[1].split(",")[1]);
									} catch(NumberFormatException e){
									}
									
									items.put((Item)Data.getObject(type, id), amount);
								}
							}
						}
						if(items.size() > 0){
							while(true){
								RPGMain.printText(true, "You currently have access to the following items:");
								int j = 1;
								for(Item i: items.keySet()){
									RPGMain.printText(true, j + ": " + i.getName() + "(" + items.get(i) + ")");
									j++;
								}
								RPGMain.printText(false, "(Type \"info\" <index>, \"take\" <index> or \"cancel\")\n>");
								String action = RPGMain.waitForMessage();
								
								if(action.startsWith("info")){
									try{
										int index = Integer.parseInt(action.split(" ")[1]);
										j = 1;
										for(Item i: items.keySet()){
											if(j == index){
												i.showInfo();
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
										j = 1;
										for(Item i: items.keySet()){
											if(j == index){
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
								else if(action.equalsIgnoreCase("cancel")){
									break;
								}
								
							}
						}
						else{
							RPGMain.printText(true, "You currently don't have access to any items.");
						}
					} catch (JDOMException e) {
						logger.error(e);
					} catch (IOException e) {
						logger.error(e);
					} catch(NumberFormatException e){
						logger.error(e);
					}
				}
				else if(function.equalsIgnoreCase("Interactive")){
					String scriptPath = Data.guilds.get(factionID).getScriptPath();
					//TODO set beanshell values
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

}
