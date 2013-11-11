package game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

public class CultureCentre extends DistrictLocation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CultureCentre.class);
	private ArrayList<String> writtenPerformances;
	private boolean inRoom = false;
	
	private ArrayList<Integer> performanceIDs;
	
	public CultureCentre(String name, String description, String performances){
		super(name, description);
		writtenPerformances = new ArrayList<String>();
		performanceIDs = new ArrayList<Integer>();
		for(String s: performances.split(";")){
			try{
				performanceIDs.add(Integer.parseInt(s));
			} catch(NumberFormatException e){
				e.printStackTrace();
				logger.error("Corrupt performance Data for " + name + ".",e);
			}
		}
	}
	public CultureCentre(String name, String description, String performances, String npcIDs){
		super(name, description);
		writtenPerformances = new ArrayList<String>();
		performanceIDs = new ArrayList<Integer>();
		for(String s: performances.split(";")){
			try{
				performanceIDs.add(Integer.parseInt(s));
			} catch(NumberFormatException e){
				e.printStackTrace();
				logger.error("Corrupt performance Data for " + name + ".",e);
			}
		}
		npcs = new ArrayList<NPC>();
		for(String s: npcIDs.split(";")){
			try{
				npcs.add(Data.NPCs.get(Integer.parseInt(s)));
			} catch(NumberFormatException e){
				e.printStackTrace();
				logger.error("Corrupted npcID data for " + name + ".",e);
			}
		}
	}
	
	public void addPerformance(int id){
		performanceIDs.add(id);
	}
	
	public void addNPC(int id){
		npcs.add(Data.NPCs.get(id));
	}

	@Override
	public void enter() throws InterruptedException {
		
		inRoom = true;
		// TODO Add music, ambience
		RPGMain.printText(true, description);
		Global.pauseProg();
		/*
		 * What else can the player do in a culture centre, except for listening to plays/poetry/music?
		 * 1) Have a drink at a bar
		 * 2) Talk with present NPCs
		 * 3) Play music?
		 * 4) Engage in riddle battles, bet money on it, 3 attempts
		 * 5) Write your own plays or poetry, and it gets performed
		 * 6) 
		 */
		
		/*
		 * Decide if there is a play going on right now
		 */
		double random = Math.random();
		//TODO
		if(random < 1){
			RPGMain.printText(true, "There is a performance going on.");
			int index = Global.generator.nextInt(performanceIDs.size());
			
			int id = performanceIDs.get(index);
			
			try {
				Document doc = Data.parser.build(new File("Data/Performances.xml"));
				Element root = doc.getRootElement();
				List<Element> objects = root.getChildren();
				Iterator<Element> i = objects.iterator();
				while(i.hasNext()){
					Element next = i.next();
					if(next.getAttributeValue("id").equalsIgnoreCase(id + "")){
						if(next.getChildText("type").equalsIgnoreCase("poetry")){
							RPGMain.printText(true, "Somebody is reciting a poem.", "darkblue");
							new PerformanceTimer("poetry", next.getChildText("name"), next.getChildText("text"));
						}
						else if(next.getChildText("type").equalsIgnoreCase("play")){
							RPGMain.printText(true, "A play is being performed.", "darkblue");
							new PerformanceTimer("play", next.getChildText("name"), next.getChild("Play"));
						}
						else if(next.getChildText("type").equalsIgnoreCase("music")){
							//TODO silence other lines etc
							RPGMain.printText(true, "The musician prepares himself for his next piece.");
							new PerformanceTimer("music", next.getChildText("name"), next.getChildText("file"));
						}
						//TODO play applause sound file after performance
					}
				}
			} catch (JDOMException e) {
				e.printStackTrace();
				logger.debug(e);
			} catch (IOException e) {
				e.printStackTrace();
				logger.debug(e);
			}
		}
		
		while(true){
			RPGMain.printText(true, "What would you want to do?");
			RPGMain.printText(true, new String[]{"* Have a drink at the ","bar"}, new String[]{"regular","bold"});
			if(npcs != null){
				for(NPC n: npcs){
					RPGMain.printText(true, new String[]{"* Talk to ", n.getName()}, new String[]{"bold","regular"});
				}
			}
			RPGMain.printText(true, new String[]{"* Engage in a ","riddle"," battle"}, new String[]{"regular","bold","regular"});
			RPGMain.printText(true, new String[]{"* Write ", "your own poetry or play"}, new String[]{"bold","regular"});
			RPGMain.printText(false, new String[]{"* ","Leave","\n>"}, new String[]{"regular","bold","regular"});
			
			String action = RPGMain.waitForMessage().toLowerCase();
			
			if(action.equalsIgnoreCase("bar")){
				//TODO bar
				int drunk = 0;
				while(true){
					//TODO make up drinks and temporary effects, like cocktails
					RPGMain.printText(true, "Not yet implemented");
				}
			}
			else if(action.startsWith("talk to ")){
				String npcName = action.split(" ")[2];
				
				boolean foundNPC = false;
				for(NPC n: npcs){
					if(n.getName().contains(npcName)){
						n.talk();
						foundNPC = true;
						break;
					}
				}
				if(!foundNPC){
					RPGMain.printText(true, "There is noone by the name of " + npcName + ".");
				}
			}
			else if(action.equalsIgnoreCase("riddle")){
				Global.pauseProg(3000, "You try to find someone willing");
				RPGMain.printText(true, "Somewhere in a corner you find someone pleased with the challenge to do a battle of the minds with you. He says he has a riddle ready for you. " +
						"If you guess the answer within three attempts, he'll repay you twice of what you give him.");
				while(true){
					RPGMain.printText(false, "How much will you bet?\n>");
					try{
						int bet = Integer.parseInt(RPGMain.waitForMessage());
						if(bet <= 0){
							RPGMain.printText(true, "Cancel the bet?");
							if(RPGMain.waitForMessage().equalsIgnoreCase("y")){
								break;
							}
						}
						else{
							
							RPGMain.speler.addGoud(-bet);
							RPGMain.printText(true, "You hand over " + bet + " coins.");
							//TODO pick a random riddle from the DB
							String riddle = null;
							String[] solutions = null;
							try {
								Document doc = Data.parser.build(new File("Data/Riddles.xml"));
								Element root = doc.getRootElement();
								List<Element> objects = root.getChildren();
								Iterator<Element> i = objects.iterator();
								int randomID = Global.generator.nextInt(objects.size());
								int j = 0;
								while(i.hasNext()){
									Element next = i.next();
									if(j == randomID){
										riddle = next.getChildText("text");
										solutions = next.getChildText("solution").split(";");
									}
									j++;
								}
							} catch (JDOMException e) {
								e.printStackTrace();
								logger.debug(e);
							} catch (IOException e) {
								e.printStackTrace();
								logger.debug(e);
							}
							
							RPGMain.printText(true, "He looks at you intently, and starts to speak: " + riddle);
							int attempts = 3;
							boolean foundAnswer = false;
							
							while(attempts > 0 && !foundAnswer){
								RPGMain.printText(false, ">");
								String answer = RPGMain.waitForMessage().toLowerCase();
								for(String solution: solutions){
									if(answer.contains(solution.toLowerCase()) || solution.contains(answer.toLowerCase())){
										RPGMain.printText(true, "Slightly disappointed over the loss of his money, he nods silently. You recieve " + 2*bet + " coins, which you gladly recieve.");
										RPGMain.speler.addGoud(2*bet);
										foundAnswer = true;
										break;
									}
									else{
										//TODO
										logger.debug("Solution " + solution);
									}
								}
								if(!foundAnswer){
									RPGMain.printText(true, "He smiles slightly as he is getting nearer to the acquisition of his money. Try again.");
									attempts--;
								}
								Global.pauseProg(3000);
							}
							if(attempts == 0){
								RPGMain.printText(true, "Your three attempts are used up, and he appears quite happy, gracefully putting the " + bet + " coins in his pocket. \"Better luck next time\", he says.");
							}
							
							break;
						}
					} catch(NumberFormatException e){
						RPGMain.printText(true, "That is not a number.");
						continue;
					}
				}
			}
			else if(action.equalsIgnoreCase("write")){
				RPGMain.printText(true, "You find a small writing table, and wait for inspiration.");
				
				new Notebook();
				
			}
			else if(action.equalsIgnoreCase("leave")){
				break;
			}
		}
		inRoom = false;
	}
	
	private class Notebook{
		
		private final JTextArea textArea = new JTextArea(20,25);
		private JFrame frame;
		private JPanel panel;
		
		public Notebook(){
			GameFrameCanvas.textField.setEnabled(false);
			constructNotebook();
		}
	
		public void constructNotebook(){
			frame = new JFrame("Small notebook");
			panel = new JPanel();
			
			JButton submitB = new JButton("Submit");
			submitB.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					String text = textArea.getText();
					writtenPerformances.add(text);
					//TODO play the actual play
					RPGMain.printEnvironmentInfo(true, "You give your manuscript toe the playmaster.", "regular");
					GameFrameCanvas.textField.setEnabled(true);
					frame.dispose();
				}
			});
			
			JButton cancelB = new JButton("Cancel");
			cancelB.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					GameFrameCanvas.textField.setEnabled(true);
					frame.dispose();
				}
			});
			
			textArea.setEditable(true);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			
			//TODO layout
			panel.add(textArea);
			panel.add(submitB);
			panel.add(cancelB);
			
			frame.setContentPane(panel);
			frame.setResizable(false);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		}
	}
	
	private class PerformanceTimer extends Thread{
		private String type, text, name;
		private Element el;
		
		private PerformanceTimer(String type, String name, String text){
			this.type = type;
			this.text = text;
			this.name = name;
			start();
		}
		public PerformanceTimer(String type, String name, Element play){
			this.type = type;
			this.name = name;
			el = play;
			start();
		}
		
		public void run(){
			RPGMain.printEnvironmentInfo(true, name, "darkbluebold");
			
			if(type.equalsIgnoreCase("poetry")){
				String[] textPieces = text.split("\n|\r\n");
				int j = 0;
				while(inRoom && j < textPieces.length){
					try{
						sleep(4000);
					} catch(InterruptedException e){
						e.printStackTrace();
						logger.error(e);
					}
					
					RPGMain.printEnvironmentInfo(true, textPieces[j].trim(), "darkblue");
					
					j++;
				}
			}
			else if(type.equalsIgnoreCase("play")){	
				try{
					List<?> dialog = el.getChildren();
					Iterator<?> i = dialog.iterator();
					while(i.hasNext() && inRoom){
						Element d = (Element)i.next();
						// get the NPC name, if there is one
						String name = d.getAttributeValue("name");
						int ms;
						try{
							// get the specified delay
							ms = Integer.parseInt(d.getAttributeValue("delay"));
						} catch(NumberFormatException e){ 
							ms = 0;
						}
						// print the NPC name if there was one
						if(name != null){
							RPGMain.printEnvironmentInfo(false, name + ": ", "darkbluebold");
						}
						// check message for player name or race and change it accordingly
						String message = d.getTextNormalize();
						message = message.replaceAll("playerName", RPGMain.speler.getName());
						message = message.replaceAll("playerRace", RPGMain.speler.getRace());
						RPGMain.printEnvironmentInfo(true,message,"darkblue");
						// pause program for specified delay
						if(ms == 0){
							Global.pauseProg();
						}
						else Thread.sleep(ms);
					}
					if(dialog.isEmpty()){
						RPGMain.printText(true, el.getTextTrim());
					}
				} catch(InterruptedException e){
				}
			}
			else if(type.equalsIgnoreCase("music")){
				//TODO fade music lines, check if new song doesn't get added to lines being faded
				Global.soundEngine.fadeLines("music");
				Global.soundEngine.playSound(text, "music", 0, 2000, 0, true);
				
				while(inRoom){
					try{
						sleep(50);
					} catch(InterruptedException e){
						e.printStackTrace();
						logger.error(e);
					}
				}
				
				Global.soundEngine.fadeLines("music");
			}
		}
	}

}
