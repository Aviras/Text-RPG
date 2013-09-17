package game;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

public class NPC extends Data implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String name,soundGreet,soundFarewell,description,activeArea,function;
	private int ID,conversationTreeID,reqQuestID;
	private HashMap<String,Double> emotionValues;
	private double importance;
	private String gender,physicalState = "Alive";
	private boolean keyPerson = false, allied = false;
	private ArrayList<Integer>[] questIDs;
	private final int NUMBEREMOTIONS = 4;
	private boolean isTalking = false;

	private static final Logger logger = Logger.getLogger(NPC.class);
	
	//normal npc
	public NPC(Integer ID,String name,String gender,Integer conversationTreeID,String soundGreet,String soundFarewell){
		this.ID = ID;
		this.name = name;
		this.gender = gender;
		this.conversationTreeID = conversationTreeID;
		this.soundGreet = soundGreet;
		this.soundFarewell = soundFarewell;
		prepareEmoValues();
	}
	
	// npc with extra options
	public NPC(Integer ID,String name,String gender,Integer conversationTreeID,String soundGreet,String soundFarewell, Element extraOptions){
		this.ID = ID;
		this.name = name;
		this.gender = gender;
		this.conversationTreeID = conversationTreeID;
		this.soundGreet = soundGreet;
		this.soundFarewell = soundFarewell;
		prepareEmoValues();
		
		// extra options
		try{
			manageQuests(extraOptions.getChild("Quests"));
		} catch(Exception e){
			e.printStackTrace();
			logger.error(e);
		}
		
		try{
			String mentalState = extraOptions.getChildText("mentalState");
			if(mentalState != null && !mentalState.equalsIgnoreCase("")){
				addEmotionValue(mentalState,1.0);
			}
		} catch(Exception e){
			e.printStackTrace();
			logger.error(e);
		}
		
		try{
			function = extraOptions.getChildText("function");
		} catch(Exception e){
			logger.error(e);
			e.printStackTrace();
		}
	}
	// key person
	public NPC(Integer ID,String name,String gender,Integer conversationTreeID,String soundGreet,String soundFarewell,String description,String activeArea,Double importance, int requiredQuestID, Element quests){
		this.ID = ID;
		this.name = name;
		this.gender = gender;
		this.conversationTreeID = conversationTreeID;
		this.soundGreet = soundGreet;
		this.soundFarewell = soundFarewell;
		this.description = description;
		this.activeArea = activeArea;
		this.importance = importance;
		this.reqQuestID = requiredQuestID;
		// plans for keypersons:
		// way to manipulate politics
		// constant source of hope
		// reputation gain
		// not all are intent on doing good for the people, depending on who you choose, it'll be better for you but not the people, importance will be negative
		keyPerson = true;
		prepareEmoValues();
		manageQuests(quests);
	}
	private void prepareEmoValues(){
		emotionValues = new HashMap<String,Double>();
		emotionValues.put("happy",1.0/NUMBEREMOTIONS);
		emotionValues.put("angry",1.0/NUMBEREMOTIONS);
		emotionValues.put("hopeful",1.0/NUMBEREMOTIONS);
		emotionValues.put("frightened",1.0/NUMBEREMOTIONS);
		addEmotionValue("happy",0.5*Math.random());
		addEmotionValue("angry",0.5*Math.random());
		addEmotionValue("hopeful",0.5*Math.random());
		addEmotionValue("frightened",0.5*Math.random());
	}

	public void manageQuests(Element quests){

		questIDs = new ArrayList[3];

		questIDs[0] = new ArrayList<Integer>();
		questIDs[1] = new ArrayList<Integer>();
		questIDs[2] = new ArrayList<Integer>();

		try{
			String[] startIDs = quests.getChildText("starts").split(";");
			for(String s: startIDs){
				try{
					questIDs[0].add(Integer.parseInt(s));
					logger.info(name + " starts quest ID " + s);
				}catch(Exception e){
					logger.error("Corrupted NPC quests start data",e);
				}
			}
		} catch(NullPointerException e){
		}
		try{
			String[] endIDs = quests.getChildText("ends").split(";");
			for(String s: endIDs){
				try{
					questIDs[1].add(Integer.parseInt(s));
					logger.info(name + " ends quest ID " + s);
				}catch(Exception e){
					logger.error("Corrupted NPC quests ends data",e);
				}
			}
		} catch(NullPointerException e){

		}
		try{
			String[] infoIDs = quests.getChildText("knows").split(";");
			for(String s: infoIDs){
				try{
					questIDs[2].add(Integer.parseInt(s));
				}catch(Exception e){
					logger.error("Corrupted NPC quests info data",e);
				}
			}
		} catch(NullPointerException e){

		}
	}

	public int getID(){
		return ID;
	}
	public String getName(){
		return name;
	}
	public String getFullName(){
		if(function != null){
			return name + " (" + function + ")";
		}
		return name;
	}
	public int getConversationTreeID(){
		return conversationTreeID;
	}
	public ArrayList<Integer> getStartQuestIDs(){
		return questIDs[0];
	}
	public ArrayList<Integer> getEndQuestIDs(){
		return questIDs[1];
	}
	public double getImportance(){
		return importance;
	}
	public String getDescription(){
		return description;
	}
	public boolean isKeyPerson(){
		return keyPerson;
	}
	public String getPhysicalState(){
		return physicalState;
	}
	public boolean isAllied(){
		return allied;
	}
	public String getMentalState(){
		String mentalState = null;
		double max = 0;
		for(String s: emotionValues.keySet()){
			if(emotionValues.get(s) > max){
				max = emotionValues.get(s);
				mentalState = s;
			}
		}
		return mentalState;
	}
	public void addEmotionValue(String emotion, double value){

		String oldMentalState = getMentalState();

		emotionValues.put(emotion, Math.max(Math.min(emotionValues.get(emotion)+value,0.75),0));

		double sum = 0;
		for(String s: emotionValues.keySet()){
			sum+=emotionValues.get(s);
		}
		if(sum != 1){
			for(String s: emotionValues.keySet()){
				emotionValues.put(s, emotionValues.get(s)/sum);
			}
		}

		String newMentalState = getMentalState();
		if(!newMentalState.equalsIgnoreCase(oldMentalState) && isTalking){
			if(newMentalState.equalsIgnoreCase("happy")){
				RPGMain.printText(true, name + " is pleased with what you said.","green");
			}
			else if(newMentalState.equalsIgnoreCase("hopeful")){
				RPGMain.printText(true, name + " seems to have found new hope because of what you said.","green");
			}
			else if(newMentalState.equalsIgnoreCase("angry")){
				RPGMain.printText(true, name + " is not pleased with what you said, and appears angered.","red");
			}
			else if(newMentalState.equalsIgnoreCase("frightened")){
				RPGMain.printText(true, name + " appears frightened by what you said.","red");
			}
		}
	}
	public HashMap<String,Double> getEmotionValues(){
		return emotionValues;
	}
	public void setPhysicalState(String s){
		physicalState = s;
	}
	public void talk(){
		// boolean is used to give a description if player changed the NPC's mood, but only while talking to him of course
		isTalking = true;

		if(physicalState.equalsIgnoreCase("dead")){
			logger.debug("NPC is dead.");
			return;
		}
		else if(physicalState.equalsIgnoreCase("diseased")){
			RPGMain.printText(true, "Better not come too close, " + name + " will be coughing all over you.");
			try{ Global.pauseProg(3000); } catch(InterruptedException e){}
			return;
		}
		logger.debug("beyond physical states");
		
		
		//GREETING
		double reputation = HopeSimulator.getReputation(RPGMain.speler.getCurrentPosition()[0], RPGMain.speler.getCurrentPosition()[1]);
		logger.debug("Reputation: " + reputation);
		try {
			Document doc = parser.build(new File("Data/Greetings.xml"));
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				Element e = (Element)i.next();
				if(reputation < Integer.parseInt(e.getAttributeValue("reputation"))){
					RPGMain.printText(true, e.getTextTrim().replaceAll("name", name).replaceAll("gender", gender));
					if(gender.equalsIgnoreCase("male")){
						RPGMain.printText(false, "He");
					}
					else{
						RPGMain.printText(false, "She");
					}
					RPGMain.printText(true, " appears " + getMentalState() + ".");
					Global.pauseProg(3000);
					break;
				}
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch(InterruptedException e){
			e.printStackTrace();
			logger.debug(e);
		}
		
		//Decide which conversation to have
		if(reputation > -10){
			Global.soundEngine.playSound("Sounds/Voices/" + soundGreet, "effects", 0, 0, 0, true);

			String talkType = "Conversation";
			Element myConvTree = null;
			HashMap<Integer,Integer> possibleAnswers = new HashMap<Integer,Integer>();
			int answer = -2;
			Document doc;

			while(true){
				int j = 2;
				RPGMain.printText(true,"1: Small talk");
				possibleAnswers.put(1, -1);
				if(function.equalsIgnoreCase("scientist")){
					RPGMain.printText(true, j + ": Talk about new Artifact Discoveries.");
					possibleAnswers.put(j, -2);
					j++;
				}
				try{
					for(int i: questIDs[0]){
						logger.debug("Starts quest ID " + i);
						//Player doesn't have the quest, and hasn't completed it in the past, and meets the requirements
						if(RPGMain.speler.getQuest(i) == null && !RPGMain.speler.isQuestCompleted(i) && Data.quests.get(i).meetsRequirements()){
							RPGMain.printText(true, j + ": Ask about " + Data.quests.get(i).getNaam());
							possibleAnswers.put(j, i);
							j++;
						}
					}
					for(int i: questIDs[1]){
						logger.debug("Ends quest ID " + i);
						//Player is on the quest, busy or completed
						if(RPGMain.speler.getQuest(i) != null){
							RPGMain.printText(true, j + ": Tell " + name + " about " + Data.quests.get(i).getNaam());
							possibleAnswers.put(j, i);
							j++;
						}
					}
				} catch(NullPointerException exc){
					exc.printStackTrace();
					logger.error(exc);
				}
				RPGMain.printText(false, j + ": Cancel\n>");
				possibleAnswers.put(j, -3);

				try {
					answer = Integer.parseInt(RPGMain.waitForMessage());
				} catch (NumberFormatException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				if(possibleAnswers.get(answer) == null){
					continue;
				}

				int answerValue = possibleAnswers.get(answer);

				try{
					switch(answerValue){
					case -1: 	doc = parser.build(new File("Data/ConversationTrees.xml"));
								Element root = doc.getRootElement();
								List<?> objects = root.getChildren();
								Iterator<?> i = objects.iterator();
								while(i.hasNext()){
									Element e = (Element)i.next();
									if(e.getAttributeValue("id").equalsIgnoreCase("" + conversationTreeID)){
										myConvTree = e;
										break;
									}
								}
								break;
					case -2:	handleArtifacts();
								break;
					case -3:	Global.soundEngine.playSound("Sounds/Voices/" + soundFarewell, "effects", 0, 0, 0, true);
								return;
					default:	doc = parser.build(new File("Data/QuestDialog.xml"));
								root = doc.getRootElement();
								objects = root.getChildren();
								i = objects.iterator();
								while(i.hasNext()){
									Element e = (Element)i.next();
									if(e.getAttributeValue("id").equalsIgnoreCase("" + answerValue)){
										logger.debug("answervalue: " + answerValue + " isQuestCompleted: " + RPGMain.speler.isQuestCompleted(answerValue));
										if(questIDs[0].contains(answerValue) && !RPGMain.speler.questCompleted(answerValue) && RPGMain.speler.getQuest(answerValue) == null){
											myConvTree = e.getChild("startdialog");
											talkType = "newQuest";
										}
										else if(RPGMain.speler.getQuest(answerValue).getCompleted()){
											myConvTree = e.getChild("enddialog");
											talkType = "complQuest";
										}
										else{
											myConvTree = e.getChild("busydialog");
											talkType = "busyQuest";
										}
										break;
									}
								}
								break;
					}

				} catch (JDOMException e) {
					e.printStackTrace();
					logger.debug(e);
				} catch (IOException e) {
					e.printStackTrace();
					logger.debug(e);
				}

				Element nodeText = null;
				List<Element> replies = null;
				Iterator<Element> repliesIter = null;

				String mentalState = getMentalState();

				Element currentTopElement = myConvTree;
				HashMap<Integer,Element> replyMap = new HashMap<Integer,Element>();

				boolean interrupted = false;

				//ACTUAL CONVERSATION
				while(currentTopElement != null){
					replyMap.clear();
					String childName = "text_" + mentalState;

					nodeText = currentTopElement.getChild(childName);
					if(nodeText == null){
						childName = "text_all";
						nodeText = currentTopElement.getChild(childName);
					}
					if(nodeText == null){
						break;
					}
					logger.info("MentalState: " + getMentalState());
					RPGMain.printText(true, name + ": " + nodeText.getTextTrim().replace("playerName", RPGMain.speler.getName()));

					try{
						Global.pauseProg();
					} catch(InterruptedException exc){
					}

					while(true){
						try{
							replies = currentTopElement.getChildren("reply");
						} catch(NullPointerException e){
							break;
						}
						boolean hasText = false;
						if(!replies.isEmpty()){
							repliesIter = replies.iterator();
							//list all possible replies
							while(repliesIter.hasNext()){
								Element r = repliesIter.next();
								String text = r.getChildText(childName);
								if(text != null){
									hasText = true;
									RPGMain.printText(true, r.getAttributeValue("value") + ": " + r.getChildText(childName));
									replyMap.put(Integer.parseInt(r.getAttributeValue("value")), r);
								}
							}
							// end the conversation if there are no more replies
							if(!hasText){
								currentTopElement = null;
								break;
							}
							RPGMain.printText(false, ">");
							// let player choose reply and interpret it
							try {
								int replyChoice = Integer.parseInt(RPGMain.waitForMessage());
								// replies are mapped
								Element chosenReply = replyMap.get(replyChoice);
								if(chosenReply == null){
									RPGMain.printText(true, "Not a valid option.");
								}
								else{
									currentTopElement = chosenReply.getChild("node");
									try{
										interrupted = Boolean.parseBoolean(chosenReply.getAttributeValue("interrupt"));
									} catch(Exception e){
									}
									try{
										String[] emotionValue = chosenReply.getAttributeValue("emo").split(":");
										addEmotionValue(emotionValue[0],Double.parseDouble(emotionValue[1]));
									} catch(Exception e){
									}
									break;
								}
							} catch (NumberFormatException e1) {
								RPGMain.printText(true, "Your choice was not a number");
								continue;
							} catch (InterruptedException e1) {
							}
						}
						else{
							currentTopElement = null;
							break;
						}
					}

				}

				//END OF CONVERSATION, CHECK IF QUEST WAS GIVEN OR COMPLETED
				if(talkType.equalsIgnoreCase("newQuest") && !interrupted){
					RPGMain.printText(true, "You take notice of what he says, and write the information safely in your logbook.");
					RPGMain.speler.addQuest(answerValue);
				}
				else if(talkType.equalsIgnoreCase("complQuest") && !interrupted){
					if(reqQuestID == answerValue){
						int[] playerPos = RPGMain.speler.getCurrentPosition();
						HopeSimulator.createHopeCenter(playerPos, importance, 1, "Allied with " + name + ".", true);
						HopeSimulator.addReputation(playerPos[0], playerPos[1], importance);
						allied = true;
					}
					Data.quests.get(answerValue).complete();
				}

				//Global.soundEngine.playSound("Sounds/Voices/" + soundFarewell, "effects", 0, 0, 0, true);
			}
		}
		
		isTalking = false;
	}
	
	public void handleArtifacts(){
		/* What has to happen here?
		 * 1) Check what artifacts player has discovered
		 * 2) Present a menu to let player choose which artifact to discuss
		 * 3) Give a message of how you hand it to him and his reaction, based on the importance and type of artifact
		 * 4) Check whether the player has an item corresponding to the ID of the letter
		 * 5) If player has item, ask him to hand it over for more cash so that other can study it
		 * 6) Reward player in cash depending on how much they learned from it and its own value
		 * 7) Increase society parameter
		 */
		while(true){
			
			RPGMain.printText(true,"\'My dear adventurer, you have found some artifacts?\', says " + name + " with a hopeful expression.");
			int j = 1;
			
			HashMap<Item,Integer> inv = RPGMain.speler.getInventory();
			ArrayList<Integer> artifactIDs = new ArrayList<Integer>();
			
			for(Item i: inv.keySet()){
				if(i.getName().startsWith("Letter")){
					RPGMain.printText(true,j + ": Report about the " + i.getName().split(": ")[1]);
					artifactIDs.add(-i.getID());
					j++;
				}
			}
			if(j > 1){
				RPGMain.printText(false, j + ": Cancel\n>");
			}
			else{
				RPGMain.printText(true,"You have nothing to report.");
				try {
					Global.pauseProg();
				} catch (InterruptedException e) {
					e.printStackTrace();
					logger.error(e);
				}
				break;
			}
			
			try{
				int choice = Integer.parseInt(RPGMain.waitForMessage());
				
				if(choice < j && choice > 0){
					int artifactID = artifactIDs.get(choice-1);
					
					Artifact a = Data.artifacts.get(artifactID);
					
					int discoveredInfoPhase = a.getDiscoveredInfo();
					
					String g = "he";
					if(gender.equalsIgnoreCase("female")){
						g = "she";
					}
					//TODO more variation
					switch(discoveredInfoPhase){
					case 1:	RPGMain.printText(true,name + " carefully reads your report. \'It is obvious we need to study this further,\', he says, \'but you help our people as a whole by this discovery.\'".replace("he", g));
							break;
					case 2: RPGMain.printText(true, name + " carefully reads your report, and is visibly intrigued. \'You seem to have found a great deal already by yourself,\' he says, \'but I think we are not yet at the bottom of this. Either way, your research cannot go unrewarded, for it is invaluable to the further research. For that, you have my sincere gratitude.\'".replace("he", g));
							break;
					case 3: RPGMain.printText(true, name + " carefully reads your report, and is enthused by your findings! \'This is incredible, " + RPGMain.speler.getName() + "! Not only the artifact itself, but also your splendid research on it!\' Visibly thrilled, " + name + " goes looking after your reward all the while thinking out loud what implications this might have for the people.".replace("he", g));
							break;
					}
					
					int modifier = 0;
					
					if(RPGMain.speler.hasItem(a.getName())){
						RPGMain.printText(false, "Would you also be so kind to hand in " + a.getName() + ", so that it can be further studied? " + name + " asks you with a look of dedication. [y/n]\n>");
						String s = RPGMain.waitForMessage().toLowerCase();
						
						if(s.startsWith("y")){
							modifier = 1;
							RPGMain.speler.delInventoryItem(a);
						}
					}
					
					int reward = (int)(a.getWorth()*discoveredInfoPhase/2.0) + modifier*a.getWorth();
					
					RPGMain.speler.addGoud(reward);
					
					RPGMain.printText(true, name + " hands you over " + reward + " gold pieces, for your hard work and contribution to society. It will be seen to that your info reaches the other scientists in the capital.");
					
					//effect on society
					Global.increaseKnowledge(a.getEffect(), (int)(discoveredInfoPhase*a.getAmount()/2.0));
					
					//destroy the letter
					Item letter = null;
					for(Item i: RPGMain.speler.getInventory().keySet()){
						if(i.getID() == -artifactID && i.getName().startsWith("Letter")){
							logger.debug("Deleting letter with ID " + i.getID());
							letter = i;
							break;
						}
					}
					if(letter != null){
						RPGMain.speler.delInventoryItem(letter);
					}
					
					//there was only one artifact to report, so stop method
					if(j == 2){
						break;
					}
				}
				else if(choice > j || choice < 0){
					RPGMain.printText(true, "Not a valid option.");
				}
				else{
					break;
				}
			} catch(NumberFormatException e){
				RPGMain.printText(true, "Not a valid option.");
				continue;
			} catch(InterruptedException e){
				e.printStackTrace();
				logger.error(e);
			}
		}
	}
}
