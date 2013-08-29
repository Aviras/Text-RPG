package game;

import java.io.Serializable;

public class Spell implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9073026913234472060L;
	private int ID,strength,castingTime;
	private String name,type,success,fail;
	
	public Spell(){}
	
	public Spell(Integer id,String name,Integer strength,Integer castingTime,String type,String success,String fail){
		ID = id.intValue();
		this.strength = strength.intValue();
		this.castingTime = castingTime.intValue();
		this.name = name;
		this.type = type;
		this.success = success;
		this.fail = fail;
	}
	
	/* GETTERS */
	public int getID(){
		return ID;
	}
	public String getName(){
		return name;
	}
	public int getStrength(){
		return strength;
	}
	public int getCastingTime(){
		return castingTime;
	}
	public String getType(){
		return type;
	}
	public String getSuccess(){
		return success;
	}
	public String getFail(){
		return fail;
	}

}
