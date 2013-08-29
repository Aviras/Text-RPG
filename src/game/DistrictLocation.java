package game;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class DistrictLocation implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String name,description;
	protected ArrayList<NPC> npcs;
	protected boolean closed = false;
	
	public DistrictLocation(){
		
	}
	
	public DistrictLocation(String name, String description){
		this.name = name;
		this.description = description;
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
