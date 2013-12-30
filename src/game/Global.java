package game;

import java.util.*;
import java.awt.Point;
import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import bsh.EvalError;
import bsh.Interpreter;

public class Global implements Serializable{
	
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 2669996259816941079L;
	public static DataOutputStream out = null;
	public static boolean online = false;
	public static int numberOnline = 1;
	public static boolean isServer = false;
	public static Hashtable<Socket,DataOutputStream> outputStreams = new Hashtable<Socket,DataOutputStream>();
	public static Hashtable<String,Socket> playerData = new Hashtable<String,Socket>();
	public static ArrayList<String> playerNames = new ArrayList<String>();
	public static boolean wait = false;
	public static File log = new File("Log");
	public static boolean pause = false;
	public static Random generator = new Random();
	public static ReadWriteTextFile rwtext = new ReadWriteTextFile();
	public static SoundEngine soundEngine = new SoundEngine();
	public static boolean busy = false;
	public static int culture,technology,religion,economy;
	public static double foodPriceMod, artifactCashMod, weaponPriceMod, armourPriceMod, travelCostMod;
	
	public static TreeMap<Double,ArrayList<int[]>> queue = new TreeMap<Double,ArrayList<int[]>>();
	
	public static ArrayList<Integer> artifactsDiscovered = new ArrayList<Integer>();
	
	public static final int playerSize = 14;
	public static int coverSize = 180;
	
	public static Interpreter beanShell = new Interpreter();
	
	private static final Logger logger = Logger.getLogger(Global.class);
	
	public static void initModifiers(){
		foodPriceMod = 1.0;
		weaponPriceMod = 1.0;
		armourPriceMod = 1.0;
		
		travelCostMod = 1.0;
		
		artifactCashMod = 1.0;
	}
	
	public static void initBeanShell(){
		try{
			Global.beanShell.set("RPGMain",new RPGMain());
			Global.beanShell.set("hostA", new HostileArea());
			Global.beanShell.set("Global", new Global());
			Global.beanShell.set("generator", Global.generator);
			Global.beanShell.set("Data", new Data());
			
			beanShell.source("Data/Scripts/Test.bsh");
		} catch(EvalError e){
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void changeModifier(String mod, double amount){
		if(mod.equalsIgnoreCase("foodPrice")){
			foodPriceMod+=amount;
		}
		else if(mod.equalsIgnoreCase("weaponPrice")){
			weaponPriceMod+=amount;
		}
		else if(mod.equalsIgnoreCase("armourPrice")){
			armourPriceMod+=amount;
		}
		else if(mod.equalsIgnoreCase("travelCost")){
			travelCostMod+=amount;
		}
		else if(mod.equalsIgnoreCase("artifactCash")){
			artifactCashMod+=amount;
		}
	}
	
	public static void pauseProg(){
		RPGMain.printText(false,"...");
		GameFrameCanvas.textField.setText("");
		pause = true;
		RPGMain.waitForMessage();
		pause = false;
	}
	public static void pauseProg(int ms){
		GameFrameCanvas.textField.setEnabled(false);
		GameFrameCanvas.textField.setText("");
		pause = true;
		for(int j=0;j<3;j++){
			RPGMain.printText(false, ".");
			try{
				Thread.sleep(ms/4);
			} catch(InterruptedException e){
				e.printStackTrace();
				logger.error(e);
			}
		}
		pause = false;
		RPGMain.printText(true, "");
		GameFrameCanvas.textField.setEnabled(true);
		GameFrameCanvas.textField.requestFocus();
	}
	public static void pauseProg(int ms, String message){
		RPGMain.printText(false, message);
		pauseProg(ms);
	}
	public static void addDiscoveredArtifactID(int ID, boolean playerFound){
		artifactsDiscovered.add(ID);
		
		boolean found = false;
		
		//remove artifact from world if NPC found it
		if(!playerFound){
			for(int j=0;j<Data.wereld.length;j++){
				for(int k=0;k<Data.wereld[0].length;k++){
					if(!found && Data.wereld[j][k] instanceof HostileArea){
						HostileArea h = (HostileArea)Data.wereld[j][k];
						HashMap<Integer,int[]> localArtifacts = h.getArtifacts();
						if(localArtifacts != null){
							for(int id: localArtifacts.keySet()){
								if(id == ID){
									//TODO no need to remove if it's non-carryable
									h.removeArtifact(ID);
									found = true;
									logger.debug("Found and removed the artifact (ID:" + id + ") in HostileArea " + h.getName() + " at " + 
											h.getPositie()[0] + "," + h.getPositie()[1] + ", at " + 
											localArtifacts.get(id)[0] + "," + localArtifacts.get(id)[1] + "," + localArtifacts.get(id)[2] + ".");
									//normally, artifact should only exist in one place
									break;
								}
							}
							break;
						}
					}
				}
			}
		}
	}
	
	public static void increaseKnowledge(String type, int amount){
		
		logger.debug("Entered increaseKnowledge: type: " + type + " amount " + amount);
		/* What has to happen here?
		 * 1) increase the respective parameter
		 * 2) see what effect it has on different aspects of society
		 * 3) give the player a message, or show him in some way the progress made?
		 */
		/* TODO
		 * Make general config files of what has to happen at what levels of the different params
		 * from a certain level, create guilds that offer quests
		 * increasing the level opens up new quests
		 * Guilds should offer some advantages to players
		 */
		
		/*
		 * Verschil tussen artifacts ingeven bij gecentraliseerde regering en individuele organisaties:
		 * Bij regering zorgt voor passieve bonussen mbt de categorie, genereert hoop, en levert direct cash op
		 * Bij organisaties zorgt voor stijgen in de rangen en vergroot de power van de organisatie
		 */
		
		/*
		 * Maak Staat ook een 'guild'? Geef guilds meer controle over wat er gebeurt wanneer je iets bij hen binnenbrengt.
		 * Hier enkel passieve effecten
		 */
		
		/*
		 * Problem with creating new buildings are the descriptions
		 */
		
		/*
		 * Maak soort stadhuis of verzamelplaats in steden met allemaal wetenschappers in om artifacts bij de staat binnen te brengen
		 */
		
		/* Concluding: 3 things
		 * 1) Put all scientists/whatever related to artifacts together in a house in a city
		 * 2) Increasing a parameter increases some passive effects
		 * 3) At certain points, special things are introduced as in guildhouses
		 */
		
		int param = 0;
		
		if(type.equalsIgnoreCase("culture")){
			culture+=amount;
			param = culture;
			/* What happens when culture is increased?
			 * 
			 * PASSIVE:
			 * 1) More books in existing libraries DONE
			 * 2) More people becoming scholars DONE
			 * 3) Extend amount of plays in existing Culture Centres DONE
			 * 
			 * ACTIVE:
			 * 1) Build new Culture Centres
			 * 2) Build new Libraries
			 * 3) Make bards, people travelling and telling stories
			 * 4) Change conversation trees of NPCs
			 */
			
			for(Location[] larr: Data.wereld){
				for(Location l: larr){
					if(l != null && l.getClass().equals(Town.class)){
						Town t = (Town)l;
						
						logger.debug("Found town " + t.getName());
						/* Add books to existing libraries
						 * It can't just be any book, some books might be obtained through artifact findings or when a parameter reached a specific value
						 */
						Library lb = (Library)t.getDistrictLocation("Library");
						
						if(lb != null){
							logger.debug("Found library");
							ArrayList<Integer> bookIDs = lb.getBookIDs();
							
							ArrayList<Integer> newBookIDs = new ArrayList<Integer>();
							try {
								Document doc = Data.parser.build(new File("Data/Books.xml"));
								Element root = doc.getRootElement();
								List<?> objects = root.getChildren();
								Iterator<?> i = objects.iterator();
								while(i.hasNext()){
									Element el = (Element)i.next();
									if(el.getAttributeValue("category").equalsIgnoreCase("free") && !bookIDs.contains(Integer.parseInt(el.getAttributeValue("id")))){
										newBookIDs.add(Integer.parseInt(el.getAttributeValue("id")));
									}
								}
							} catch (JDOMException e) {
								e.printStackTrace();
								logger.debug(e);
							} catch (IOException e) {
								e.printStackTrace();
								logger.debug(e);
							}
							
							//TODO tweak probability
							if(!newBookIDs.isEmpty() && Math.random() < 1){
								int index = generator.nextInt(newBookIDs.size());
								lb.addBook(newBookIDs.get(index));
								logger.info("Added book " + newBookIDs.get(index) + " to library in " + t.getName());
							}
							
							
							/*
							 * Make more people scholars
							 * Question: Make new NPCs and somehow save them to load next time OR
							 * 			 Turn existing NPCs into scholars and move them?
							 * Solution: Make new NPCs, either have to make a lot of original NPCs, or the world will become empty quickly + simulates population growth
							 * 
							 * Question: How to save these new NPCs such that they are loaded correctly next time? Same applies for all that changes in the world?
							 * 			 2 Options: Either save all changes made and apply them to a brand new world afterwards OR
							 * 			 Save everything in the state at the save made
							 * Remarks: Option 1 might become lengthy, and need to make sure everything stays consistent. For loading procedure there is also a high need
							 * 			for efficient parsing to distinguish all possible actions. Effectively same as making scripting language for the game
							 * 			Option 2 throws away all of the original data for Towns and HostileAreas after first use
							 */
							
							/*
							 * Need name database
							 */
							
							NPC newNPC = createNewNPC();
							
							newNPC.setFunction("scholar");
							
							lb.addNPC(newNPC);

						}
						
						//Add new Performances to existing Culture Centres.
						CultureCentre cc = (CultureCentre)t.getDistrictLocation("culturecentre");
						
						if(cc != null){
							ArrayList<Integer> performanceIDs = cc.getPerformanceIDs();
							
							ArrayList<Integer> newPerformanceIDs = new ArrayList<Integer>();
							
							try {
								Document doc = Data.parser.build(new File("Data/Performances.xml"));
								Element root = doc.getRootElement();
								List<?> objects = root.getChildren();
								Iterator<?> i = objects.iterator();
								while(i.hasNext()){
									Element el = (Element)i.next();
									if(el.getAttributeValue("category").equalsIgnoreCase("free") && !performanceIDs.contains(Integer.parseInt(el.getAttributeValue("id")))){
										newPerformanceIDs.add(Integer.parseInt(el.getAttributeValue("id")));
									}
								}
							} catch (JDOMException e) {
								e.printStackTrace();
								logger.debug(e);
							} catch (IOException e) {
								e.printStackTrace();
								logger.debug(e);
							}
							
							if(!newPerformanceIDs.isEmpty() && Math.random() < 0.5){
								int index = generator.nextInt(newPerformanceIDs.size());
								lb.addBook(newPerformanceIDs.get(index));
								logger.info("Added Performance " + newPerformanceIDs.get(index) + " to CultureCentre in " + t.getName());
							}
						}
					}
				}
			}
		}
		else if(type.equalsIgnoreCase("technology")){
			technology+=amount;
			param = technology;
			/* What happens when technology is increased?
			 * 1) Prices in shops become lower due to improvements in farming etc
			 * 2) Creation of technology guilds, scientists gathering to discuss and create, possibly give new quests
			 * 3) More people becoming inventors
			 * 4) New technology items in shops
			 * 5) 
			 * 
			 * 
			 * PASSIVE:
			 * 1) More people becoming inventors
			 * 
			 * ACTIVE:
			 * 1) New technology items in shops
			 * 2) Better food available in shops
			 * 3) Prices become lower in shops, food due to farming improvements, weapons and armour due to new techniques, new ores (through guilds as well perhaps)
			 */
		}
		else if(type.equalsIgnoreCase("religion")){
			religion+=amount;
			param = religion;
			/* What happens when religion is increased?
			 * 1) Building more churches/religious places
			 * 2) More people becoming "priests"
			 * 3) Role of religion still has to be thought out
			 */
		}
		else if(type.equalsIgnoreCase("economy")){
			economy+=amount;
			param = economy;
			/* What happens when economy is increased?
			 * 1) Prices become cheaper
			 * 2) Banks open
			 * 3) More shops selling different, exotic things
			 * 4) Travelling becomes cheaper because roads are improved
			 * 5) Cities become bigger
			 * 6) Less diseases and bad events due to improved quality of life
			 * 7) More cash money for discovered artifacts
			 * 
			 * PASSIVE:
			 * 1) Prices become cheaper
			 * 2) Less diseases and bad events due to improved quality of life
			 * 3) More cash money for discovered artifacts DONE
			 * 
			 * ACTIVE:
			 * 1) More shops selling different, exotic things
			 * 2) Travelling becomes cheaper because roads are improved
			 * 3) Cities become bigger
			 * 
			 */
			
			artifactCashMod+=0.01*amount;
			
		}
		
		//All active things happening, as written in KnowledgeEvents.xml data file
		try {
			Document doc = Data.parser.build(new File("Data/KnowledgeEvents.xml"));
			Element root = doc.getRootElement();
			Element typeEvents = root.getChild(RPGMain.upperCaseSingle(type, 0));
			List<Element> events = typeEvents.getChildren();
			Iterator<Element> it = events.iterator();
			while(it.hasNext()){
				Element el = it.next();
				if(Integer.parseInt(el.getAttributeValue("value")) <= param){
					beanShell.eval(el.getTextTrim());
				}
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (EvalError e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			e.printStackTrace();
		}
		
		
		logger.debug("Culture: " + culture + "; Technology: " + technology + "; Religion: " + religion + "; Economy: " + economy);
	}
	
	public static NPC createNewNPC(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader("Data/First_Names.csv"));
			List<String> male = new ArrayList<>();
			List<String> female = new ArrayList<>();
			String line = null;
			while ((line = reader.readLine()) != null) {
			    if(line.split(",")[1].equalsIgnoreCase("m")){
			    	male.add(line.split(",")[0]);
			    }
			    else{
			    	female.add(line.split(",")[0]);
			    }
			}
			
			reader = new BufferedReader(new FileReader("Data/Last_Names.csv"));
			List<String> lastNames = new ArrayList<>();
			line = null;
			while((line = reader.readLine()) != null){
				lastNames.add(line);
			}
			
			String newName = null;
			String gender = null;
			
			if(generator.nextDouble() < 0.5){
				newName = RPGMain.upperCaseSingle(male.get(generator.nextInt(male.size())),0);
				gender = "male";
			}
			else{
				newName = RPGMain.upperCaseSingle(female.get(generator.nextInt(female.size())),0);
				gender = "female";
			}
			newName+=" " + lastNames.get(generator.nextInt(lastNames.size()));
			logger.info("Creating new NPC name " + newName);
			
			//TODO conversation tree, function and sound effects
			//public NPC(Integer ID,String name,String gender,Integer conversationTreeID,String soundGreet,String soundFarewell)
			NPC newNPC = new NPC(-1, newName, gender, 0, null, null);
			return newNPC;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	// Send messages to server, interpreted at server
	public static void message(String message){
			try {
				out.writeUTF(message);
				String date = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
				ReadWriteTextFile.setContents(log,date + ": " + message, true);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Failed to deliver message.", "Error", JOptionPane.INFORMATION_MESSAGE);
			}
	}
	
	/** Allows the printing of dialogs from text files
	 * @param repeated Separate text pieces must be marked with a repeated String
	 * 				   with an integer. ex.: "HISTORY 1" \n 'Dialog text' "HISTORY 2"
	 * @param optionalAnswers If a dialog requests user input. UNDER CONSTRUCTION
	 * @throws InterruptedException 
	 */
	public static String makeDialog(File aFile,String[] path){
		SAXBuilder parser = new SAXBuilder();

		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			Element el = root;
			for(int j=0;j<path.length;j++){
				String s = path[j];
				String[] t = s.split(",");
				try{
					String[] u = t[1].split(":");
					@SuppressWarnings("unchecked")
					List<Element> children = el.getChildren(t[0]);
					//iterate over children to see if attribute value fits
					for(Element e:children){
						if(e.getAttributeValue(u[0]).equalsIgnoreCase(u[1])){
							el = e;
						}
					}
				}catch(Exception e){
					el = el.getChild(s);
					System.out.println(t[0]);
				}
			}
			return makeDialog(el);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			RPGMain.printText(true, "File " + aFile.getAbsolutePath() + " not found!");
		}
		return null;
	}
	public static String makeDialog(Element el){
		StringBuilder sb = new StringBuilder();
	
		try{
			List<?> dialog = el.getChildren();
			Iterator<?> i = dialog.iterator();
			while(i.hasNext()){
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
					RPGMain.printText(false, name + ": ");
				}
				// check message for player name or race and change it accordingly
				String message = d.getTextNormalize();
				message = message.replaceAll("playerName", RPGMain.speler.getName());
				message = message.replaceAll("playerRace", RPGMain.speler.getRace());
				RPGMain.printText(true,message);
				sb.append(message);
				// pause program for specified delay
				if(ms == 0){
					pauseProg();
				}
				else Thread.sleep(ms);
			}
			if(dialog.isEmpty()){
				RPGMain.printText(true, el.getTextTrim());
				sb.append(el.getTextTrim());
			}
			return sb.toString();
		} catch(InterruptedException e){
		}
		
		return null;
	}
	// modified A* algorithm
	public static ArrayList<int[]> calculatePath(int[] startPos, int[] endPos, int movement, HashMap<double[][],Double> covers, boolean withinMelee){
		ArrayList<int[]> startPath = new ArrayList<int[]>();
		// ArrayList for partial path that is first in the queue
		ArrayList<int[]> firstInQueue = new ArrayList<int[]>();
		startPath.add(startPos);
		queue.clear();
		queue.put(0.0,startPath);
		
		final int stepSize = 3;
		
		logger.info("Entered calculatePath. startPos: " + startPos[0] + "," + startPos[1] + "; endPos: " + endPos[0] + "," + endPos[1] + "; Movement: " + movement + ".");
		
		while(!queue.isEmpty()){
			//get the first in queue
			firstInQueue = queue.get(queue.firstKey());
			
			logger.info("First in Queue: " + firstInQueue.get(firstInQueue.size()-1)[0] + "," + firstInQueue.get(firstInQueue.size()-1)[1] + ". Movement:" + movement);
			//get the last coord pair of the first partial path in the queue
			int[] lastPos = firstInQueue.get(firstInQueue.size()-1);
			//remove the first partial path from the queue
			queue.remove(queue.firstKey());
			
			// checks around the current position: -stepSize,0,+stepSize for both j and k
			for(int j=-stepSize;j<=stepSize;j=j+stepSize){
				for(int k=-stepSize;k<=stepSize;k=k+stepSize){
					if(k == 0 && j==0){
						// do nothing for current position
					}
					else{
						// add the new coord pair to the partial path of the first in queue
						firstInQueue.add(new int[] {lastPos[0] + k,lastPos[1] + j});
						// check if the path is valid, and add it to the queue
						addPath(firstInQueue,startPos,endPos,covers,stepSize);
						// remove the last coord pair, and repeat with the next around it
						firstInQueue.remove(firstInQueue.size()-1);
					}
				}
			}
			
			logger.debug("Queue size after creation of new paths:" + queue.size());
			
			// Because of the use of a TreeMap, it's constantly sorted
			ArrayList<Double> toDelete = new ArrayList<Double>();
			Set<Double> keys = queue.keySet();
			Iterator<Double> i = keys.iterator();
			try{
				// iterate over all paths in the queue
				while(i.hasNext()){
					Double d = i.next();
					ArrayList<int[]> p = queue.get(d);
					keys = queue.keySet();
					// get the last coord pair of path p
					int[] k = p.get(p.size()-1);
					//iterator over all other paths in the queue
					Iterator<Double> l = keys.iterator();
					while(l.hasNext()){
						Double m = l.next();
						ArrayList<int[]> a = queue.get(m);
						if(!p.equals(a)){
							//check if path a has overlap with p, and if it has and p has already moved more to get to that point than in path a, remove p
							for(int j=1;j<a.size();j++){
								int[] q = a.get(j);
								// effective distance travelled is (j-1)*5
								if(q[0] == k[0] && q[1] == k[1] && (p.size()-1)*stepSize > (j-1)*stepSize){
									// use arrayList to delete afterwards to avoid concurrentmodificationexception
									toDelete.add(d);
									break;
								}
							}
						}
					}
				}
				for(Double d: toDelete){
					queue.remove(d);
				}
				toDelete.clear();
			}catch(Exception e){
				e.printStackTrace();
				logger.error("Error at calculate path. " , e);
			}
			//System.out.println("Past redundancy checking");
			ArrayList<int[]> bestPath = queue.get(queue.firstKey());
			int[] currentPos = bestPath.get(bestPath.size()-1);

			if(getTraveledDistance(bestPath) >= movement || (withinMelee && new Point(endPos[0],endPos[1]).distance(currentPos[0],currentPos[1]) < Global.playerSize)){
				System.out.println("Goal reached. Movement: " + movement + " distance: " + new Point(startPos[0],startPos[1]).distance(currentPos[0],currentPos[1]) + " traveled: " + getTraveledDistance(bestPath));
				return bestPath;
			}
		}
		
		return null;
	}
	
	public static void addPath(ArrayList<int[]> path, int[] startPos, int[] endPos,HashMap<double[][],Double> covers,int stepSize){
		int width = GameFrameCanvas.battlefield.getWidth();
		int height = GameFrameCanvas.battlefield.getHeight();
		
		// get the last coord pair in the partial path
		int[] newPos = path.get(path.size()-1);
		// check that it is still within the window
		if(newPos[0] > 0 && newPos[0] < width && newPos[1] > 0 && newPos[1] < height){
			Iterator<double[][]> coverPos = covers.keySet().iterator();
			// check if it isn't in cover
			while(coverPos.hasNext()){
				double[][] pos = coverPos.next();
				// covers are treated as ellipses
				// get the value of the cosine for the angle made between the center of the cover and the new coord, and the x-axis
				double cosTheta = (double)(newPos[0]-pos[0][0])/Math.sqrt(Math.pow(newPos[0] - pos[0][0], 2) + Math.pow(newPos[1] - pos[0][1], 2));
				double radius = 1.0/Math.sqrt(Math.pow(cosTheta/(Global.coverSize/2*pos[1][0]), 2) + (1-cosTheta*cosTheta)/Math.pow(Global.coverSize/2*pos[1][1],2));
				if(new Point(newPos[0],newPos[1]).distance(pos[0][0],pos[0][1]) <= (radius+Global.playerSize/2)){
					return;
				}
			}
			// check for loops
			for(int j=0;j<path.size()-1;j++){
				int[] p = path.get(j);
				// check for overlap between a coord, and all the ones following it
				for(int k = j+1;k<path.size();k++){
					int[] l = path.get(k);
					
					// if there is overlap, don't add the path to the queue
					if(p[0] == l[0] && p[1] == l[1]){
						return;
					}
					// if there is no overlap, and the whole partial path is checked, add the path to the queue
					else if((p[0] != l[0] || p[1] != l[1]) && k == (path.size()-1) && j == (path.size()-2)){
						try{
							queue.put(0.5*getTraveledDistance(path) + new Point(newPos[0],newPos[1]).distance(endPos[0],endPos[1]),cloneArrayList(path));
						} catch(Exception e){
							e.printStackTrace();
							logger.error(e);
						}
					}
				}
			}
		}
		else{
			return;
		}
	}
	public static ArrayList<int[]> cloneArrayList(ArrayList<int[]> a){
		ArrayList<int[]> b = new ArrayList<int[]>(a.size());
		for(int[] o:a){
			b.add(o.clone());
		}
		return b;
	}
	public static double getTraveledDistance(ArrayList<int[]> a){
		double distance = 0;
		for(int j=0;j<a.size()-1;j++){
			distance+= new Point(a.get(j)[0],a.get(j)[1]).distance(a.get(j+1)[0],a.get(j+1)[1]);
		}
		return distance;
	}
}