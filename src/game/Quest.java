package game;
import java.io.*;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

public class Quest extends Data implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3L;

	private String naam;
	private boolean completed;
	private int level,ID;
	private int[] prerequisites;
	private HashMap<String,int[]> objectives, rewards;
	
	private static final Logger logger = Logger.getLogger(Quest.class);
	
	//TODO allow events to complete a quest
	// problem, events don't have an ID => use position
	public Quest(){
		
	}
	public Quest(Integer id,String name,Integer lvl,String objects, int[] maxProgress,int[] prerequisites){
		ID = id.intValue();
		naam = name;
		completed = false;
		level = lvl.intValue();
		
		objectives = new HashMap<String,int[]>();
		rewards = new HashMap<String,int[]>();
		
		int j = 0;
		for(String s: objects.split(";")){
			try{
				// int[] is stored as: objective ID, maxAmount, currentAmount
				objectives.put(s.split(":")[0], new int[]{Integer.parseInt(s.split(":")[1]),maxProgress[j],0});
				j++;
			} catch(NumberFormatException e){
				e.printStackTrace();
				logger.error("Corrupted objective data for quest ID " + ID,e);
			}
		}
		this.prerequisites = prerequisites;
	}
	public Quest(Integer id,String name,Integer lvl,String objects,int[] maxProgress,int[] prerequisites, String rewardTypes, int[] reward){
		ID = id.intValue();
		naam = name;
		completed = false;
		level = lvl.intValue();

		objectives = new HashMap<String,int[]>();
		rewards = new HashMap<String,int[]>();
		
		int j = 0;
		for(String s: objects.split(";")){
			try{
				// int[] is stored as: objective ID, maxAmount, currentAmount
				objectives.put(s.split(":")[0], new int[]{Integer.parseInt(s.split(":")[1]),maxProgress[j],0});
				j++;
			} catch(NumberFormatException e){
				e.printStackTrace();
				logger.error("Corrupted objective data for quest ID " + ID,e);
			}
		}
		
		j = 0;
		for(String s: rewardTypes.split(";")){
			try{
				//int[] is stored as: ID, amount
				if(s.split(":").length > 1){
					rewards.put(s.split(":")[0], new int[]{Integer.parseInt(s.split(":")[1]),reward[j]});
				}
				else{
					rewards.put(s.split(":")[0], new int[]{0,reward[j]});
				}
				j++;
			} catch(NumberFormatException e){
				e.printStackTrace();
				logger.error("Corrupted reward data for quest ID " + ID,e);
			} catch(Exception e){
				e.printStackTrace();
				logger.error(e);
			}
		}
		this.prerequisites = prerequisites;
	}
	public void showQuest() throws InterruptedException{
		RPGMain.printText(false,"Show description ? [y/n]\n>");
		String keuze = RPGMain.waitForMessage();
		if(keuze.equalsIgnoreCase("y")){
			RPGMain.printText(true,"\n"+naam + "\n");
			String[] path = {naam,"Uncompleted"};
			Global.makeDialog(new File("Data/QuestDialog.xml"), path);
		}
	}
	public String getNaam(){
		return naam;
	}
	public int getID(){
		return ID;
	}
	public boolean getCompleted(){
		return completed;
	}
	public void setCompleted(boolean b){
		completed = b;
		RPGMain.printText(true,naam + " completed.");
	}
	public int getLevel(){
		return level;
	}
	public int[] getPrerequisiteIDs(){
		return prerequisites;
	}
	public void addProgress(String objectiveType, int x){
		int[] i = objectives.get(objectiveType);
		objectives.put(objectiveType, new int[]{i[0],i[1],i[2]+x});
	}
	public int getMaxProgress(String objectiveType){
		return objectives.get(objectiveType)[1];
	}
	public Set<String> getObjectType(){
		return objectives.keySet();
	}
	public void giveReward(){
		for(String rewardType: rewards.keySet()){
			int rewardID = rewards.get(rewardType)[0];
			int amount = rewards.get(rewardType)[1];
			
			if(rewardType.equalsIgnoreCase("gold")){
				RPGMain.speler.addGoud(amount);
				RPGMain.printText(true,"You recieved " + amount + " gold.");
			}
			else if(rewardType.equalsIgnoreCase("equipment")){
				RPGMain.speler.addInventoryItem(Data.equipment.get(rewardID),amount);
			}
			else if(rewardType.equalsIgnoreCase("spell")){
				RPGMain.speler.addSpreuk(Data.spells.get(rewardID));
			}
			else if(rewardType.equalsIgnoreCase("potion")){
				RPGMain.speler.addInventoryItem(Data.potions.get(rewardID),amount);
			}
		}
	}
	public void getProgress(String objectiveType){
		RPGMain.printText(true,"Updated progress " + objectives.get(objectiveType)[2] + "/" + objectives.get(objectiveType)[1] + " for " + naam);
	}
	public void checkProgress(String objectiveType, int objectID){
		logger.debug("Checking progress for quest " + naam + " for objectiveType: " + objectiveType + ":" + objectID);
		for(String objectType: objectives.keySet()){
			if(objectiveType.equalsIgnoreCase(objectType) && objectives.get(objectType)[0] == objectID && completed == false){
				addProgress(objectType,1);
				getProgress(objectType);
			}
		}
		for(String objectType: objectives.keySet()){
			if(objectives.get(objectType)[2] < objectives.get(objectType)[1]){
				return;
			}
		}
		completed = true;
	}
	public boolean meetsRequirements(){
		// has done previous quests necessary for this one
		for(int i: prerequisites){
			if(i != -1 && !RPGMain.speler.isQuestCompleted(i)){
				return false;
			}
		}
		// if objective is discovery of an artifact, make sure it isn't discovered yet by an npc
		for(String objectType: objectives.keySet()){
			if(objectType.equalsIgnoreCase("artifact")){
				for(int id: Global.artifactsDiscovered){
					if(id == objectives.get(objectType)[0]){
						return false;
					}
				}
			}
		}
		return true;
	}
	public void complete(){
		
		// check reward
		giveReward();
		
		// clean up, delete Q from log, put ID with completed quests
		RPGMain.speler.delQuest(naam);
		RPGMain.speler.completeQuest(ID);
		
		try{
			Global.pauseProg();
		} catch(InterruptedException e){}
	}
}
