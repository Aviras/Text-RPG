package game;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public abstract class DistrictLocation implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String name,description;
	protected ArrayList<NPC> npcs;
	protected boolean closed = false;
	
	private static Logger logger = Logger.getLogger(DistrictLocation.class);
	
	public DistrictLocation(){
		
	}
	
	public DistrictLocation(String name, String description){
		this.name = name;
		this.description = description;
	}
	public DistrictLocation(String name, String description, String npcIDs){
		this.name = name;
		this.description = description;
		
		npcs = new ArrayList<NPC>();
		for(String s: npcIDs.split(";")){
			try{
				npcs.add(Data.NPCs.get(Integer.parseInt(s)));
			} catch(NumberFormatException e){
				e.printStackTrace();
				logger.error("NPC data error for GuildHouse " + name, e);
			}
		}
	}
	public String getName(){
		return name;
	}
	public String getDescription(){
		return description;
	}
	public boolean getClosed(){
		return closed;
	}
	public abstract void enter() throws InterruptedException;
}
