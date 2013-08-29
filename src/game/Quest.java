package game;
import java.io.*;

public class Quest extends Data implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3L;

	private String naam,objectType,rewardType;
	private boolean completed;
	private int level,progress,maxProgress,ID,reward,objectID;
	private int[] prerequisites;
	
	public Quest(){
		
	}
	public Quest(Integer id,String name,Integer lvl,String object,Integer maxProgress,int[] prerequisites){
		ID = id.intValue();
		naam = name;
		completed = false;
		level = lvl.intValue();
		progress = 0;
		this.objectType = object.split(":")[0];
		this.objectID = Integer.parseInt(object.split(":")[1]);
		System.out.println("Object ID");
		this.maxProgress = maxProgress.intValue();
		this.prerequisites = prerequisites;
		rewardType = null;
		reward = 0;
	}
	public Quest(Integer id,String name,Integer lvl,String object,Integer maxProgress,int[] prerequisites, String rewardType, Integer reward){
		ID = id.intValue();
		naam = name;
		completed = false;
		level = lvl.intValue();
		progress = 0;
		this.objectType = object.split(":")[0];
		this.objectID = Integer.parseInt(object.split(":")[1]);
		this.maxProgress = maxProgress.intValue();
		this.prerequisites = prerequisites;
		this.rewardType = rewardType;
		this.reward = reward;
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
	public int getExperience(int alvl){
		// (level^(2.5)/(2*alvl))*10 + 5*level
		return (int)Math.ceil(Math.pow(Math.pow(level, 2.5)/(2*alvl), 3)*10+5*level);
	}
	public void setProgress(int x){
		progress+=x;
	}
	public int getMaxProgress(){
		return maxProgress;
	}
	public String getObjectType(){
		return objectType;
	}
	public Object getReward(){
		if(rewardType.equalsIgnoreCase("gold")){
			RPGMain.speler.addGoud(reward);
			RPGMain.printText(true,"You recieved " + reward + " gold.");
		}
		else if(rewardType.equalsIgnoreCase("equipment")){
			return Data.equipment.get(reward);
		}
		else if(rewardType.equalsIgnoreCase("spell")){
			return Data.spells.get(reward);
		}
		else if(rewardType.equalsIgnoreCase("potion")){
			return Data.potions.get(reward);
		}
		return null;
	}
	public void getProgress(){
		RPGMain.printText(true,"Updated progress " + progress + "/" + maxProgress + " for " + naam);
	}
	public void checkProgress(String objectiveType, int objectID){
		System.err.println("Checking progress for quest " + naam + " for objectiveType: " + objectiveType + ":" + objectID);
		if(objectiveType.equalsIgnoreCase(objectType) && this.objectID == objectID && completed == false){
			progress++;
			getProgress();
			if(progress == maxProgress){
				setCompleted(true);
			}
		}
	}
	public boolean meetsRequirements(){
		for(int i: prerequisites){
			if(i != -1 && !RPGMain.speler.isQuestCompleted(i)){
				return false;
			}
		}
		
		return true;
	}
	public void complete(){
		
		// check reward
		Object reward = getReward();
		if(reward != null){
			if(reward instanceof Spell){
				RPGMain.speler.addSpreuk((Spell)reward);
			}
			else{
				RPGMain.speler.addInventoryItem((Item)reward);
			}
		}
		
		// clean up, delete Q from log, put ID with completed quests
		RPGMain.speler.delQuest(naam);
		RPGMain.speler.completeQuest(ID);
		
		try{
			Global.pauseProg();
		} catch(InterruptedException e){}
	}
}
