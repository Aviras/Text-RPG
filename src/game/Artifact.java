package game;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;

import bsh.EvalError;

public class Artifact extends Item {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int worth, difficulty, amount, questID = -1;
	private String effect, script;
	private boolean canCarry;
	private HashMap<Integer,String> extractedInfo;
	private static Logger logger = Logger.getLogger(Artifact.class);
	
	public Artifact(Integer id, String name, String description, String effect, Integer amount, Integer worth, Integer difficulty, boolean canCarry, String logbookpath, Element logbookSummaries){
		super(id,name,description,0.0,-1,logbookpath,null);
		
		this.effect = effect;
		this.amount = amount;
		this.worth = worth;
		this.difficulty = difficulty;
		this.canCarry = canCarry;
		extractedInfo = new HashMap<Integer,String>();
		List<Element> children = logbookSummaries.getChildren();
		Iterator<Element> it = children.iterator();
		while(it.hasNext()){
			Element el = it.next();
			extractedInfo.put(Integer.parseInt(el.getAttributeValue("level")), el.getTextTrim());
		}
	}
	
	public Artifact(Integer id, String name, String description, String effect, Integer amount, Integer worth, Integer difficulty, String script, boolean canCarry, String logbookpath, Element logbookSummaries){
		super(id,name,description,0.0,-1,logbookpath,null);
		
		this.effect = effect;
		this.amount = amount;
		this.worth = worth;
		this.difficulty = difficulty;
		this.script = script;
		this.canCarry = canCarry;
		extractedInfo = new HashMap<Integer,String>();
		List<Element> children = logbookSummaries.getChildren();
		Iterator<Element> it = children.iterator();
		while(it.hasNext()){
			Element el = it.next();
			extractedInfo.put(Integer.parseInt(el.getAttributeValue("level")), el.getTextTrim());
		}
	}
	
	public Artifact(Integer id, String name, String description, Double weight, Integer cost, String effect, Integer amount, Integer worth, Integer difficulty, boolean canCarry, String logbookpath, Element logbookSummaries){
		super(id,name,description,weight,cost,logbookpath,null);
		
		this.effect = effect;
		this.amount = amount;
		this.worth = worth;
		this.difficulty = difficulty;
		this.canCarry = canCarry;
		extractedInfo = new HashMap<Integer,String>();
		List<Element> children = logbookSummaries.getChildren();
		Iterator<Element> it = children.iterator();
		while(it.hasNext()){
			Element el = it.next();
			extractedInfo.put(Integer.parseInt(el.getAttributeValue("level")), el.getTextTrim());
		}
	}
	
	public Artifact(Integer id, String name, String description, Double weight, Integer cost, String effect, Integer amount, Integer worth, Integer difficulty, boolean canCarry, Integer questID, String logbookpath, Element logbookSummaries){
		super(id,name,description,weight,cost,logbookpath,null);
		
		this.effect = effect;
		this.amount = amount;
		this.worth = worth;
		this.difficulty = difficulty;
		this.canCarry = canCarry;
		this.questID = questID;
		extractedInfo = new HashMap<Integer,String>();
		List<Element> children = logbookSummaries.getChildren();
		Iterator<Element> it = children.iterator();
		while(it.hasNext()){
			Element el = it.next();
			extractedInfo.put(Integer.parseInt(el.getAttributeValue("level")), el.getTextTrim());
		}
	}
	
	public Artifact(Artifact another){
		this.ID = another.ID;
		this.name = another.name;
		this.description = another.description;
		this.effect = another.effect;
		this.amount = another.amount;
		this.worth = another.worth;
		this.difficulty = another.difficulty;
		this.script = another.script;
		this.canCarry = another.canCarry;
		this.questID = another.questID;
		this.logbookPath = another.logbookPath;
		this.extractedInfo = another.extractedInfo;
		
		this.weight = another.weight;
		this.cost = another.cost;
	}
	
	public void activate(boolean playerFound){
		//what has to happen?
		/*
		 * 1) Show the description 
		 * 2) if present, execute the attached script
		 * 3) print extracted info and put it in logbook
		 * 4) reward player and show effect on society
		 * 5) add artifact to inventory if possible
		 * 6) check if its discovery completed some quest
		 * 7) chance to increase erudition
		 */
		
		int level = (int)(Math.max(1.0, Math.min(3.0, 2.0*RPGMain.speler.getErudition()/difficulty)));
		
		double multiplier = (double)level/2.0;
		
		//effect on society
		Global.increaseKnowledge(effect, (int)(multiplier*amount));
		
		//put it in the list of already discovered artifacts
		Global.addDiscoveredArtifactID(ID, playerFound);
		
		// only if it was the player that discovered the artifact
		if(playerFound){
			
			RPGMain.printText(true, description);
			
			//execute script if present
			if(script != null){
				try {
					//TODO set beanShell values
					Global.beanShell.eval(script);
				} catch (EvalError e) {
					e.printStackTrace();
				}
			}
			
			RPGMain.printText(true,"You try to decipher what it could mean, or what it could have been used for.");
			
			try{
				Global.pauseProg();
			} catch(InterruptedException e){
				e.printStackTrace();
				logger.error(e);
			}
			
			RPGMain.printText(true, extractedInfo.get(level));
			
			// create small minigame for extracting info, it gets easier/harder dependent on the ratio erudition/difficulty
			// minigame based on lockpick (perhaps in an abstract form), information was being saved in containers, more important information has harder coding
			// different layers of security, each layer gives new information
			
			//TODO reward player
			// possibilities for delivering the info: 
			// * make an Item as a letter with the description being a letter format. Have to hand it in somewhere
			// * use something like a post pigeon to deliver your message to the HQ
			// * create instant travel to cities, and be able to quickly get back to where you left off
			// * 
			RPGMain.printText(true,"You note your findings in a letter to the capital.");
		
			//put info in logbook
			Logbook.addContent(logbookPath, 0, description);
			Logbook.addContent(logbookPath, 1, extractedInfo.get(level));
			
			//add it to inventory if player can carry it
			if(canCarry){
				RPGMain.speler.addInventoryItem(this);
				RPGMain.printText(true, "You recieved " + name + ".");
			}
			
			// check if its discovery completed some quest
			for(Quest q: RPGMain.speler.getQuestLog()){
				q.checkProgress("Artifact", ID);
			}
			
			//add new quest if it gives one, player doesn't have it already (somehow) and hasn't completed it before (somehow)
			if(questID >= 0 && RPGMain.speler.getQuest(questID) == null && !RPGMain.speler.isQuestCompleted(questID)){
				RPGMain.speler.addQuest(questID);
			}
			
			// increase erudition
			//TODO check influence of amount
			int addEr = (int)Math.abs((4+level)*(1+amount)*Global.generator.nextGaussian()) + 1;
			RPGMain.speler.addErudition(addEr);
		}
		
	}
	
	
	
	

}
