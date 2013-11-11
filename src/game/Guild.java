package game;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jdom.Element;

public class Guild {
	
	
	/*What should this class do?
	 * 1) Be of a certain type (Merchant's guild, Inventor's guild,...)
	 * 2) Have a certain reputation level
	 * 3) Have some npcs with a certain hierarchy
	 * 4) Have some quests opened up at various reputation levels, possibly repeatable
	 * 5) Have some advantages to players unlocked through reputation increase
	 * 6) Consist of different rooms, accessible at different reputation levels
	 * 7) Be interactive for the player when entering
	 * 8) Player is able to carry a sign, like a tabard of some sort, which allows access or other advantages
	 * 9) Connection with artifacts: you can bring artifacts relevant to the guild type to them to increase your reputation and get specific rewards
	 * 	  Why would you still bring them to a normal scientist? Bringing artifacts to the state improves general life, creates hope, increases reputation and rewards cash
	 * 10) Relations with other guilds
	 */
	
	private String name, description, scriptPath;
	private int ID, reputation, power;
	private ArrayList<Integer> questIDs;
	private HashMap<Integer, Double> relations;
	
	private static Logger logger = Logger.getLogger(Guild.class);
	
	public Guild(Integer id, String name, String description, Element options){
		ID = id.intValue();
		this.name = name;
		this.description = description;
		
		power = 1;
		
		questIDs = new ArrayList<Integer>();
		
		try{
			for(String s: options.getChildText("quests").split(";")){
				try{
					questIDs.add(Integer.parseInt(s));
				} catch(NumberFormatException e){
					e.printStackTrace();
					logger.error("Corrupted quest data for Guild " + name, e);
				}
			}
		} catch(NullPointerException e){
		}
		
		scriptPath = options.getChildText("scriptPath");
		
		relations = new HashMap<Integer, Double>();
		
		try{
			for(String s: options.getChildText("relations").split(";")){
				relations.put(Integer.parseInt(s.split(":")[0]), Double.parseDouble(s.split(":")[1]));
			}
		} catch(NullPointerException e){
		} catch(NumberFormatException e){
			logger.error("Corrupted data for relations of guild " + ID + ", " + name);
		}
	}
	
	public int getPower(){
		return power;
	}
	public String getName(){
		return name;
	}
	public String getDescription(){
		return description;
	}
	public int getID(){
		return ID;
	}
	public int getReputation(){
		return reputation;
	}
	public String getScriptPath(){
		return scriptPath;
	}
	public void addReputation(int x){
		reputation+=x;
	}
	public void increasePower(int x){
		power+=x;
	}
}
