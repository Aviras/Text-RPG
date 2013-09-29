/**
 * What does this class have to do for the player?
 * 1) Alter basic statistics like strength, intellect, etc
 * 2) Give spells or attacks an extra effect
 * 3) Learn new abilities
 * 4) Passive effects like dual wielding, tracking animals, etc
 * 
 * How to solve these things?
 * 1) In the getStrength() etc methods, check for stuff from skillElements
 * 2) Allow for code insertions into the code for abilities at specified places. Abilities can have extra 'slots'
 * 3) Just add the ability to the player's
 * 4) Possible with hardcoded booleans, but gets tedious
 * 
 * What does this class have to have?
 * 1) Related to specific skill: swords, hunting, etc
 * 2) Have a certain point investment requirement
 * 3) Have multiple levels of increasing effect
 * 4) Connections with other SkillElements in the same tree, or perhaps even a different one
 * 5) Description of effect
 * 6) Name
 * 7) ID
 * 8) Possible description for extra effect, during attack perhaps
 * 9) Category of effect, see above
 * 10) an activate() method, such that effects get applied when a saved game is loaded
 */
package game;


import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.jdom.Element;

import bsh.EvalError;

/**
 * @author aviras
 *
 */
public class SkillElement {
	
	private String name, skillDescription, skillTree, type, effectDescription, script, parameter;
	private int ID, levelReq, maxProgress, progress;
	private double magnitude;
	private int[] skillReq;
	
	private static final Logger logger = Logger.getLogger(SkillElement.class);
	
	// SkillElement with extra options
	public SkillElement(Integer ID, String name, String skillDescription, String skillTree, String type, Integer levelReq, Integer maxProgress, Element extraOptions){
		// Basic parameters
		this.ID = ID.intValue();
		this.name = name;
		this.skillDescription = skillDescription;
		this.skillTree = skillTree;
		this.type = type;
		this.levelReq = levelReq.intValue();
		this.maxProgress = maxProgress.intValue();
		
		progress = 0;
		
		// Options depending on type
		try{
			parameter = extraOptions.getChildTextTrim("parameter");
			magnitude = Double.parseDouble(extraOptions.getChildTextTrim("magnitude"));
		} catch(NullPointerException e){
		} catch(NumberFormatException e){
			e.printStackTrace();
			logger.error("Corrupted magnitude data for SkillElement ID " + ID,e);
		}
		
		try{
			String[] skillReqs = extraOptions.getChildText("skillRequirements").split(";");
			skillReq = new int[skillReqs.length];
			
			for(int j=0;j<skillReqs.length;j++){
				skillReq[j] = Integer.parseInt(skillReqs[j]);
			}
		} catch(NumberFormatException e){
			e.printStackTrace();
			logger.error(e);
		} catch(NullPointerException e){
		}
		
		script = extraOptions.getChildTextTrim("script");
	}
	
	public void activate(){
		if(type.equalsIgnoreCase("passive effect") && script != null){
			try {
				Global.beanShell.source(script);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				logger.error(e);
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(e);
			} catch (EvalError e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
	}
	
	public int getID(){
		return ID;
	}
	public String getName(){
		return name;
	}
	public String getScript(){
		return script;
	}
	public String getSkillDescription(){
		return skillDescription;
	}
	public String getParameter(){
		return parameter;
	}
	public String getSkillTree(){
		return skillTree;
	}
	public String getType(){
		return type;
	}
	public String getEffectDescription(){
		return effectDescription;
	}
	public int getLevelRequirement(){
		return levelReq;
	}
	public int getMaxProgress(){
		return maxProgress;
	}
	public double getRawMagnitude(){
		return magnitude;
	}
	public double getMagnitude(){
		return progress*magnitude;
	}
	public int[] getSkillRequirements(){
		return skillReq;
	}
	public int getProgress(){
		return progress;
	}
	public boolean isMaxedOut(){
		if(progress == maxProgress){
			return true;
		}
		return false;
	}
	public void setProgress(int x){
		progress = x;
		activate();
	}
	public void addProgress(int x){
		progress+=x;
		activate();
		GameFrameCanvas.updatePlayerInfoTable();
	}
	public boolean meetsLevelRequirement(){
		if(skillTree.equalsIgnoreCase("swords")){
			if(RPGMain.speler.getSwordSkill() < levelReq){
				return false;
			}
		}
		else if(skillTree.equalsIgnoreCase("axes")){
			if(RPGMain.speler.getAxeSkill() < levelReq){
				return false;
			}
		}
		else if(skillTree.equalsIgnoreCase("clubs")){
			if(RPGMain.speler.getClubSkill() < levelReq){
				return false;
			}
		}
		else if(skillTree.equalsIgnoreCase("archery")){
			if(RPGMain.speler.getArchery() < levelReq){
				return false;
			}
		}
		else if(skillTree.equalsIgnoreCase("stamina")){
			if(RPGMain.speler.getStamina() < levelReq){
				return false;
			}
		}
		else if(skillTree.equalsIgnoreCase("erudition")){
			if(RPGMain.speler.getErudition() < levelReq){
				return false;
			}
		}
		else if(skillTree.equalsIgnoreCase("thievery")){
			if(RPGMain.speler.getThievery() < levelReq){
				return false;
			}
		}
		else if(skillTree.equalsIgnoreCase("firemaking")){
			if(RPGMain.speler.getFireMaking() < levelReq){
				return false;
			}
		}
		else if(skillTree.equalsIgnoreCase("herbalism")){
			if(RPGMain.speler.getHerbalism() < levelReq){
				return false;
			}
		}
		else if(skillTree.equalsIgnoreCase("hunting")){
			if(RPGMain.speler.getHunting() < levelReq){
				return false;
			}
		}
		else if(skillTree.equalsIgnoreCase("animalKnowledge")){
			if(RPGMain.speler.getAnimalKnowledge() < levelReq){
				return false;
			}
		}
		else if(skillTree.equalsIgnoreCase("swimming")){
			if(RPGMain.speler.getSwimming() < levelReq){
				return false;
			}
		}
		
		return true;
	}
	
	public boolean meetsSkillRequirements(){
		if(skillReq != null){
			for(int i: skillReq){
				logger.debug("Progress for ID " + i + ": " + Data.skillElements.get(i).getProgress());
				if(!Data.skillElements.get(i).isMaxedOut()){
					return false;
				}
			}
		}
		return true;
	}
}
