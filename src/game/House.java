package game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;

public class House extends DistrictLocation implements Serializable{

	/**
	 * 
	 */
	//TODO make a control mechanism such that when a connection is made in one room, it is also automatically made for the other direction
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(House.class);
	
	protected HashMap<Integer, Room> rooms;
	protected int currentRoom;
	
	public House(String name, String description, Element options){
		this.name = name;
		this.description = description;
		
		currentRoom = 0;
		
		rooms = new HashMap<Integer, Room>();
		
		List<Element> children = options.getChildren("Room");
		Iterator<Element> it = children.iterator();
		while(it.hasNext()){
			Element room = it.next();
			rooms.put(Integer.parseInt(room.getAttributeValue("number")), new Room(Integer.parseInt(room.getAttributeValue("number")), room.getChildText("name"), 
					room.getChildText("description"), room.getChildText("function"), room.getChildText("connections"), room.getChild("options")));
		}
	}

	public void enter() throws InterruptedException {
		RPGMain.printText(true, description);
		Global.pauseProg();
		
		while(true){
			currentRoom = rooms.get(currentRoom).enterRoom();
			if(currentRoom < 0){
				currentRoom = 0;
				break;
			}
		}
	}
	
	class Room extends DistrictLocation {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private int roomNumber;
		private int reqReputation;
		private String function;
		private int[] roomConnections;
		private ArrayList<NPC> npcs;
		private ArrayList<Item> items;
		
		public Room(int number, String name, String description, String function, String connections, Element options){
			super(name, description);
			this.roomNumber = number;
			this.function = function;
			roomConnections = new int[connections.split(";").length];
			
			int i = 0;
			for(String s: connections.split(";")){
				try{
					roomConnections[i] = Integer.parseInt(s);
					i++;
				} catch(NumberFormatException e){
					logger.error("Corrupted data for room " + name, e);
				}
			}
			
			
			npcs = new ArrayList<NPC>();
			
			try{
				String[] t = options.getChildText("npcs").split(";");
				
				for(int j=0;j<t.length;j++){
					try{
						npcs.add(Data.NPCs.get(Integer.parseInt(t[j])));
					} catch(NumberFormatException e){
						e.printStackTrace();
						logger.error("Corrupted npcID data for room " + name,e);
					}
				}
			} catch(NullPointerException e){
			}
			
			items = new ArrayList<Item>();
			
			try{
				for(String s: options.getChildText("items").split(";")){
					String[] v = s.split(":");
					String[] u = null;
					try{
						u = v[1].split(",");
					} catch(Exception e){
						logger.error(e);
						e.printStackTrace();
					}
					
					for(int j=0;j<u.length;j++){
						try{
							if(v[0].equalsIgnoreCase("equipment")){
								items.add(Data.equipment.get(Integer.parseInt(u[j])));
							}
							else if(v[0].equalsIgnoreCase("consumable")){
								items.add(Data.consumables.get(Integer.parseInt(u[j])));
							}
							else{
								//TODO
							}
						} catch(NumberFormatException e){
							logger.error("Corrupted present item data for room " + name,e);
							e.printStackTrace();
						}
					}
				}
			} catch(NullPointerException e){
			}
			
			try{
				reqReputation = Integer.parseInt(options.getChildText("reputation"));
			} catch(NumberFormatException e){
				reqReputation = -1;
				//logger.error("Corrupted data error in room " + name, e);
			}
		}
		
		public int getRequiredReputation(){
			return reqReputation;
		}
		
		public String getFunction(){
			return function;
		}
		
		public void enter() throws InterruptedException{
			
		}

		public int enterRoom() throws InterruptedException {
			// TODO implement methods for specific functions
			RPGMain.printText(true, description);
			
			int nextRoom = roomNumber;
			
			while(true){
				HashMap<String, Integer> availRooms = new HashMap<String, Integer>();
				for(int j=0;j<roomConnections.length;j++){
					RPGMain.printText(true, new String[]{"* ", "Enter ", rooms.get(roomConnections[j]).getName()}, new String[]{"regular","bold","regular"});
					availRooms.put(rooms.get(roomConnections[j]).getName(), roomConnections[j]);
				}
				if(!npcs.isEmpty())
					RPGMain.printText(true, "Persons present:");
				for(NPC n: npcs){
					RPGMain.printText(true, new String[]{"* ", "Talk to ", n.getFullName()}, new String[]{"regular","bold","regular"});
				}
				
				if(!items.isEmpty())
					RPGMain.printText(true, "Items present:");
				for(int j=0;j<items.size();j++){
					RPGMain.printText(true,new String[]{"* ","Take ",(j+1) + ": " + items.get(j).getName()}, new String[]{"regular","bold","regular"});
				}
				if(function != null && !function.equalsIgnoreCase("null")){
					RPGMain.printText(true, new String[]{"* Use its ", "function", " as a " + function}, new String[]{"regular","bold","regular"});
				}
				if(roomNumber == 0){
					RPGMain.printText(true, new String[]{"* ", "Leave"}, new String[]{"regular","bold"});
				}
				
				RPGMain.printText(false, ">");
				String action = RPGMain.waitForMessage().toLowerCase().trim();
				try{
					if(action.startsWith("enter")){
						String name = action.split(" ")[1];
						for(String s: availRooms.keySet()){
							if(s.toLowerCase().contains(name)){
								nextRoom = availRooms.get(s);
								break;
							}
						}
						if(nextRoom != currentRoom){
							break;
						}
					}
					else if(action.startsWith("talk to")){
						String name = RPGMain.upperCaseSingle(action.split(" ")[2],0);
						
						for(NPC n: npcs){
							if(n.getName().equalsIgnoreCase(name)){
								logger.debug("talking to " + name);
								n.talk();
								break;
							}
						}
					}
					else if(action.startsWith("take")){
						
						int index = Integer.parseInt(action.split(" ")[1])-1;
						
						if(!items.isEmpty()){
							try{
								RPGMain.speler.addInventoryItem(items.get(index));
								
								items.remove(index);
								
								RPGMain.printText(true, "Added item.");
							} catch(IndexOutOfBoundsException e){
								RPGMain.printText(true, "That number does not occur.");
							}
						}
						
						Global.pauseProg(1000);
					}
					else if(action.equalsIgnoreCase("function") && function != null && !function.equalsIgnoreCase("null")){
						//TODO other functions
						if(!function.equalsIgnoreCase("bedroom")){
							nextRoom = -2;
							break;
						}
						
					}
					else if(roomNumber == 0 && action.equalsIgnoreCase("leave")){
						nextRoom = -1;
						break;
					}
				} catch(Exception e){
					continue;
				}
			
			}
			
			return nextRoom;
		}

	}

}
