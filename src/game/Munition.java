package game;

import java.io.Serializable;

public class Munition extends Item implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int strength;

	public Munition(Integer id, String name, String description,Double weight, Integer cost, Integer strength){
		super(id,name,description,weight,cost);
		this.strength = strength;
	}
	public int getStrength(){
		return strength;
	}
}
