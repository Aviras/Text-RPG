package game;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class House extends DistrictLocation implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(House.class);
	
	private ArrayList<Item> items;
	
	public House(String name, String description, String npcIDs, String presentItems){
		this.name = name;
		this.description = description;
		
		npcs = new ArrayList<NPC>();
		
		String[] t = npcIDs.split(";");
		
		for(int j=0;j<t.length;j++){
			try{
				npcs.add(Data.NPCs.get(Integer.parseInt(t[j])));
			} catch(NumberFormatException e){
				e.printStackTrace();
				logger.error("Corrupted npcID data",e);
			}
		}
		
		items = new ArrayList<Item>();
		
		String[] v = presentItems.split(":");
		String[] u = null;
		try{
			u = v[1].split(";");
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
				logger.error("Corrupted present item data",e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void enter() throws InterruptedException {
		RPGMain.printText(true, description);
		Global.pauseProg();
		
		while(true){
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
			
			RPGMain.printText(false, ">");
			String action = RPGMain.waitForMessage().toLowerCase().trim();
			
			if(action.startsWith("talk to")){
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
				
				try{
					Global.pauseProg(1000);
				} catch(InterruptedException e){
					
				}
			}
			else if(action.equalsIgnoreCase("cancel")){
				break;
			}
		
		}
	}

}
