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
	protected String name,soundGreet,soundFarewell,description,activeArea;
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
	//npc with quests
	public NPC(Integer ID,String name,String gender,Integer conversationTreeID,String soundGreet,String soundFarewell, Element quests){
		this.ID = ID;
		this.name = name;
		this.gender = gender;
		this.conversationTreeID = conversationTreeID;
		this.soundGreet = soundGreet;
		this.soundFarewell = soundFarewell;
		prepareEmoValues();
		manageQuests(quests);
	}
	// npc with quests and definite mentalstate
	public NPC(Integer ID,String name,String gender,Integer conversationTreeID,String soundGreet,String soundFarewell,String mentalState, Element quests){
		this.ID = ID;
		this.name = name;
		this.gender = gender;
		this.conversationTreeID = conversationTreeID;
		this.soundGreet = soundGreet;
		this.soundFarewell = soundFarewell;
		prepareEmoValues();
		addEmotionValue(mentalState,1.0);
		manageQuests(quests);
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
				possibleAnswers.put(j, -2);

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
					case -2:	Global.soundEngine.playSound("Sounds/Voices/" + soundFarewell, "effects", 0, 0, 0, true);
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

	/*for(int i:questIDs){
	if(RPGMain.speler.getQuest(i) != null){
		childName = "text_quest_" + i;
		if(RPGMain.speler.getQuest(i).getCompleted()){
			childName+="_complete";
			if(reqQuestID == i){
				int[] playerPos = RPGMain.speler.getCurrentPosition();
				HopeSimulator.createHopeCenter(playerPos, importance, 1, "Allied with " + name + ".", true);
				HopeSimulator.addReputation(playerPos[0], playerPos[1], importance);
				allied = true;
			}
		}
		else{
			childName+="_busy";
		}
		break;
	}
}*/


	// check for present quests
	//Element quest = currentTopElement.getChild("quest");
	//if there is a quest, the player hasn't done it yet, and not doing it right now
	/*if(quest != null && RPGMain.speler.getQuest(Integer.parseInt(quest.getTextTrim())) == null && 
			!RPGMain.speler.isQuestCompleted(Integer.parseInt(quest.getTextTrim()))){
		RPGMain.printText(true, "You take notice of what he says, and write the information safely in your logbook.");
		RPGMain.speler.addQuest(Integer.parseInt(quest.getTextTrim()));
		questIDs.add(Integer.parseInt(quest.getTextTrim()));
		try {
			Document doc = parser.build(new File("Data/QuestSummary.xml"));
			Element root = doc.getRootElement();
			List<Element> children = root.getChildren();
			Iterator<Element> i = children.iterator();
			while(i.hasNext()){
				Element el = i.next();
				if(el.getAttributeValue("questID").equalsIgnoreCase(quest.getTextNormalize())){
					Logbook.addContent("Story/Quests/" + el.getAttributeValue("name"), 1, el.getTextTrim());
				}
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}*/
}
