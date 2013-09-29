package game;

import java.util.Calendar;
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


	/**
	 * @param worth: how much the discovery of the artifact is worth in gold
	 * @param difficulty: the difficulty of the info of the artifact 1-100 compared to erudition
	 * @param amount: the amount of the effect on the field in society
	 */
	private int worth, difficulty, amount, questID = -1, lockDifficulty;
	private String effect, script, riddle, riddleSolution;
	private boolean canCarry;
	private HashMap<Integer,String> extractedInfo;
	private int discoveredInfo;
	private static Logger logger = Logger.getLogger(Artifact.class);

	//basic Artifact
	public Artifact(Integer id, String name, String description, String effect, Integer amount, Integer worth, Integer difficulty, Boolean canCarry, String logbookpath, Element logbookSummaries){
		super(id,name,description,0.0,0,logbookpath,null);

		this.effect = effect;
		this.amount = amount;
		this.worth = worth;
		this.difficulty = difficulty;
		this.canCarry = canCarry.booleanValue();
		extractedInfo = new HashMap<Integer,String>();
		List<Element> children = logbookSummaries.getChildren();
		Iterator<Element> it = children.iterator();
		while(it.hasNext()){
			Element el = it.next();
			extractedInfo.put(Integer.parseInt(el.getAttributeValue("level")), el.getTextTrim());
		}
	}

	// Artifact with extra options
	public Artifact(Integer id, String name, String description, String effect, Integer amount, Integer worth, Integer difficulty, Boolean canCarry, String logbookpath, Element logbookSummaries, Element extraOptions){
		super(id,name,description,0.0,0,logbookpath,null);

		this.effect = effect;
		this.amount = amount;
		this.worth = worth;
		this.difficulty = difficulty;
		this.canCarry = canCarry.booleanValue();
		extractedInfo = new HashMap<Integer,String>();
		List<Element> children = logbookSummaries.getChildren();
		Iterator<Element> it = children.iterator();
		while(it.hasNext()){
			Element el = it.next();
			extractedInfo.put(Integer.parseInt(el.getAttributeValue("level")), el.getTextTrim());
		}

		//extra Options
		// QUEST ID
		try{
			questID = Integer.parseInt(extraOptions.getChildTextTrim("questID"));
		} catch(NumberFormatException e){
			if(extraOptions.getChildTextTrim("questID") != null){
				logger.error("Corrupted questID Data for Artifact ID " + id,e);
				e.printStackTrace();
			}
		}

		// SPECIFIED WEIGHT
		script = extraOptions.getChildTextTrim("script");

		try{
			weight = Double.parseDouble(extraOptions.getChildTextTrim("weight"));
		} catch(NumberFormatException e){
			if(extraOptions.getChildTextTrim("weight") != null){
				logger.error("Corrupted weight Data for Artifact ID " + id,e);
				e.printStackTrace();
			}
		} catch(NullPointerException e){
		}

		// SPECIFIED COST
		try{
			cost = Integer.parseInt(extraOptions.getChildTextTrim("cost"));
		} catch(NumberFormatException e){
			if(extraOptions.getChildTextTrim("cost") != null){
				logger.error("Corrupted cost data for Artifact ID " + id,e);
				e.printStackTrace();
			}
		}

		// RIDDLE
		try{
			riddle = extraOptions.getChildTextTrim("riddle");
			riddleSolution = extraOptions.getChildTextTrim("riddleSolution");
		} catch(Exception e){
			e.printStackTrace();
			logger.error(e);
		}

		// LOCK
		try{
			lockDifficulty = Integer.parseInt(extraOptions.getChildTextTrim("lock"));
		} catch(NumberFormatException e){
			if(extraOptions.getChildTextTrim("lock") != null){
				logger.error("Corrupted lock Data for Artifact ID " + id, e);
				e.printStackTrace();
			}
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
		this.discoveredInfo = another.discoveredInfo;
		this.riddle = another.riddle;
		this.riddleSolution = another.riddleSolution;
		this.lockDifficulty = another.lockDifficulty;

		this.weight = another.weight;
		this.cost = another.cost;
	}

	public boolean getCanCarry(){
		return canCarry;
	}
	public int getDiscoveredInfo(){
		return discoveredInfo;
	}
	public String getEffect(){
		return effect;
	}
	public int getAmount(){
		return amount;
	}
	public int getWorth(){
		return worth;
	}

	public void use(){

		// haven't discovered any info yet, so not yet opened, and has a sort of lock
		if(discoveredInfo == 0 && (lockDifficulty != 0 || riddle != null)){
			//TODO start minigame
			boolean opened = openLock();

			if(!opened){
				return;
			}
		}

		int oldDiscoveredInfo = discoveredInfo;

		discoveredInfo = (int)(Math.max(1.0, Math.min(3.0, 2.0*RPGMain.speler.getErudition()/difficulty)));

		logger.debug("Artifact relative level: " + discoveredInfo);

		if(oldDiscoveredInfo == discoveredInfo){
			RPGMain.printText(true, "You learn nothing that you did not know before.");
		}
		else{

			RPGMain.printText(true,"You try to decipher what it could mean, or what it could have been used for.");

			Global.pauseProg();

			RPGMain.printText(true, "It seems to be some sort of " + name + ".");

			for(int j=1;j<=discoveredInfo;j++){
				Global.pauseProg(2000);
				logger.debug("Printing info for level " + j);
				RPGMain.printText(true, extractedInfo.get(j));
			}

			Global.pauseProg();

			// create small minigame for extracting info, it gets easier/harder dependent on the ratio erudition/difficulty
			// minigame based on lockpick (perhaps in an abstract form), information was being saved in containers, more important information has harder coding
			// different layers of security, each layer gives new information
			// Work with a puzzle system of wires. You try to open the lock by adjusting the wires, without causing the pressure in the wire to cross a boundary
			// If you cross the boundary, it snaps shut
			// Is this the right option? Make it too hard, and it becomes more of a puzzle game and quickly frustrating, make it too easy, and there's no challenge
			// Use a system based on a number code with a wire and a certain pressure on it. Certain combinations of numbers increase pressure, goal is to get pressure to zero, and you can open the box
			// See notes on cellphone new new ideas
			//TODO also make a letter if player is unable to open it

			//TODO reward player
			// possibilities for delivering the info: 
			// * make an Item as a letter with the description being a letter format. Have to hand it in somewhere
			// * use something like a post pigeon to deliver your message to the HQ
			// * create instant travel to cities, and be able to quickly get back to where you left off
			// * Make a scientist NPC class. Put them in frontier villages and bigger cities. Create a report on what you found, and give it to a scientist for the reward
			RPGMain.printText(true,"You note your findings in a letter.");

			Calendar cal = Calendar.getInstance();
			String text = "Author: " + RPGMain.speler.getName() + "\n";
			//TODO change to in-game time
			text+="Date: " + cal.getTime() + "\n\n";
			int[] playerPos = RPGMain.speler.getCurrentPosition();
			HostileArea dummy = (HostileArea)Data.wereld[playerPos[0]][playerPos[1]];
			text+="Concerning Discovery of Artifact in " + dummy.getName() + ".\n\n";
			text+="An Artifact was found" + dummy.getPositionDescription() + " This is what I have found so far: ";
			text+=description + "\n";
			for(int j=1;j<=discoveredInfo;j++){
				text+=extractedInfo.get(j) + "\n";
			}
			text+="\nThis might be valuable information for the advancement of our " + effect + ".";

			//TODO differentiate in name between different artifacts found in same region
			Item letter = new Item(-ID,"Letter: Artifact Discovery in " + dummy.getName(),text,0.01,0);
			// destroy original letter if player found new info by examining it again
			try{
				if(RPGMain.speler.getInventoryItem("Letter: Artifact Discovery in " + dummy.getName()).getID() == letter.getID()){
					RPGMain.speler.delInventoryItem("Letter: Artifact Discovery in " + dummy.getName(), 1);
				}
			} catch(NullPointerException e){
			}
			RPGMain.speler.addInventoryItem(letter);

			//put info in logbook
			Logbook.addContent(logbookPath, 0, description);
			for(int j=1;j<=discoveredInfo;j++){
				Logbook.addContent(logbookPath, j, extractedInfo.get(j));
			}
			// increase erudition
			//TODO check influence of amount
			//TODO erudition already being increased in Logbook.addContent method
			/*int addEr = (int)Math.abs((4+level)*(1+amount)*Global.generator.nextGaussian()) + 1;
			RPGMain.speler.addErudition(addEr);*/
		}
	}

	public void activate(boolean playerFound){
		//what has to happen?
		/*
		 * 1) Show the description 
		 * 2) if present, execute the attached script
		 * 3) see if it's locked. If so, start minigame
		 * 4) print extracted info and put it in logbook
		 * 5) add artifact to inventory if possible
		 * 6) check if its discovery completed some quest
		 * 7) Make the letter reporting what you found
		 * 8) chance to increase erudition
		 */

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

			use();

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

		}

	}

	public boolean openLock(){
		RPGMain.printText(true, "What you have found seems to be protected by some form of protection, but with a mechanism preventing you from opening it.");
		Global.pauseProg();
		if(riddle != null){
			RPGMain.printText(true, "There seems to be a riddle inscribed into the casing. There are some turning wheels with letters on them to form an answer.");
			Global.pauseProg();

			while(true){
				RPGMain.printText(true, "The riddle goes: " + riddle);
				RPGMain.printText(false, ">");

				String answer = RPGMain.waitForMessage();

				if(!answer.equalsIgnoreCase("cancel")){
					logger.debug("Answer: " + riddleSolution);
					if(answer.equalsIgnoreCase(riddleSolution)){
						RPGMain.printText(true, "You hear a satisfactory click, and you find that you are now able to open the container.");
						Global.pauseProg(2000);
						return true;
					}
					else{
						RPGMain.printText(true, "You wait a few moments, unsure if anything is to happen. There is no sound but your sigh, meaning it wasn't the right answer.");
						Global.pauseProg(2000);
					}
				}
				else{
					break;
				}
			}
		}
		return false;
	}



}
