package game;

import game.Logbook.LogbookNode;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;


public class RPGMain extends SwingWorker<Void,String> {

	private static Socket client = null;
	public static Avatar speler;
	private static String fieldMessage = null;
	
	private static final Logger logger = Logger.getLogger(RPGMain.class);
	
	private static DayLightThread dayLightThread;
	
	public RPGMain(){
		
	}

	protected Void doInBackground() throws Exception {
		start();
		return null;
	}

	public void start()  throws InterruptedException{
		
		File about = new File("Data/About.xml");

		while(true){
			
			GameFrameCanvas.enemyPortrait.setVisible(false);
			GameFrameCanvas.playerPortrait.setVisible(false);
			GameFrameCanvas.logoPortrait.setVisible(true);
			GameFrameCanvas.dungeonMap.setVisible(false);
			GameFrameCanvas.battlefield.setVisible(false);
			GameFrameCanvas.imagePanel.setVisible(true);
			GameFrameCanvas.environmentScrollPane.setVisible(false);
			
			GameFrameCanvas.textPane.setText("");
			
			// main menu
			printText(false,"1: New Game\n" +
							"2: Load Game\n" +
							"3: Start Multiplayer\n" +
							"4: About\n" +
							"5: Exit\n>");

			try{
				switch(Integer.parseInt(waitForMessage())){
				// NEW GAME
				case 1: logger.info("Starting new game.");
						if(Global.online) Global.message(InetAddress.getLocalHost().getHostName() + " is starting a new game.");
						
						// Create new player
						newGame();
						
						dayLightThread = new DayLightThread();
						//starting point, argument is direction where you're going to
						int[] direction = Data.towns.get(1).main_menu(Town.getDirection(10, 10));
						//main loop of the game
						mainLoop(direction);
						
						break;
				case 2: logger.info("Loading previous game");
						loadGame();
						if(speler != null){
							if(Global.online) Global.message(InetAddress.getLocalHost().getHostName() + " has loaded a character and is now at " + Data.wereld[speler.getCurrentPosition()[0]][speler.getCurrentPosition()[1]].getName() + ", known as " + speler.getName() + ".");
							dayLightThread = new DayLightThread();
							int[] dir = Data.wereld[speler.getCurrentPosition()[0]][speler.getCurrentPosition()[1]].main_menu(Town.getDirection(10, 10));
							// main loop of the game
							mainLoop(dir);
						}
						
						break;
				case 3: logger.info("Setting up online");
						setupOnline();
						break;
				case 4: logger.info("Showing 'About'");
						printText(true,Global.rwtext.getContent(about, new String[]{"about"}));
						Global.pauseProg();
						break;
				case 5: logger.info("Exiting game");
						System.exit(0);
				default: break;
				}
			}catch(NumberFormatException e){
				printText(true,"Not a valid option.");
				continue;
			} catch (UnknownHostException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
	}
	
	public void newGame() throws InterruptedException{
		
		File race = new File("Data/Races.xml");
		
		File initializeF = new File("Data/Initialization.xml");
		
		String s = Global.rwtext.getContent(initializeF, new String[] {"equipment"});
		String t = Global.rwtext.getContent(initializeF, new String[] {"clothing"});

		
		String[] p = s.split(",");
		String[] u = t.split(",");
		
		int[] startEquipmentIDs = new int[p.length];
		int[] startClothingIDs = new int[u.length];
		
		for(int j=0;j<p.length;j++){
			startEquipmentIDs[j] = Integer.parseInt(p[j]);
		}
		for(int j=0;j<u.length;j++){
			startClothingIDs[j] = Integer.parseInt(u[j]);
		}
		
		printText(false,"Welcome to Text RPG v0.8...\nHow would you like to be named ?\n>");
		speler = new Avatar(1,upperCaseSingle(waitForMessage(),0),100,50,0,0,0,0,startEquipmentIDs,startClothingIDs);
		
		try{
			if(Global.online){
				Global.message(InetAddress.getLocalHost().getHostName() + " is now known as " + speler.getName() + ".");
				Global.message("NAME: " + speler.getName());
			}
		} catch(UnknownHostException e){
			
		}
		
		
		
		//ASK FOR GENDER
		String gender = "";
		while(!(gender.equalsIgnoreCase("m") || gender.equalsIgnoreCase("male")) && !(gender.equalsIgnoreCase("f") || gender.equalsIgnoreCase("female"))){
			printText(false,"\nWhat is your gender? [m/f]\n>");
			gender = waitForMessage();
			if(gender.equalsIgnoreCase("f")){
				GameFrameCanvas.playerPortrait.changeImage("Images/female_portrait_zoom.jpg");
			}
		}

		//ASK FOR RACE
		boolean completed = false;
		while(!completed){
			printText(true,"\nWhat is your race ?");
			try {
				//ArrayList<String> raceNames = new ArrayList<String>();
				Document doc = Data.parser.build(race);
				Element root = doc.getRootElement();
				@SuppressWarnings("unchecked")
				List<Element> children = root.getChildren();
				Iterator<Element> i = children.iterator();
				Element paramHolder;
				while(i.hasNext()){
					Element child = i.next();
					printText(true,"* " + child.getAttributeValue("name"));
					paramHolder = child.getChild("Parameters");
					@SuppressWarnings("unchecked")
					List<Element> params = paramHolder.getChildren();
					Iterator<Element> it = params.iterator();
					while(it.hasNext()){
						Element param = it.next();
						printText(true,"\t" + upperCaseSingle(param.getName(),0) + ": " + param.getTextTrim());
					}
					//raceNames.add(child.getAttributeValue("name"));
				}
				printText(false,"(Type info <race> for background lore)\n>");
				String keuze = waitForMessage().toLowerCase().trim();
				
				// info about race
				if(keuze.startsWith("info")){
					keuze = upperCaseSingle(keuze,5);
					printText(true,"\n**** " + keuze.substring(5) + " ****");
					String[] path = {"race,name:" + keuze.substring(5), "Info"};
					Global.makeDialog(race, path);
				}
				// check if correct choice has been made
				else{
					keuze = upperCaseSingle(keuze,0);
					for(Element e:children){
						if(e.getAttributeValue("name").equalsIgnoreCase(keuze)){
							printText(true,"You are now an " + keuze + ".");
							speler.setStrength(Integer.parseInt(e.getChild("Parameters").getChildTextTrim("strength")));
							speler.setDexterity(Integer.parseInt(e.getChild("Parameters").getChildTextTrim("dexterity")));
							speler.setIntellect(Integer.parseInt(e.getChild("Parameters").getChildTextTrim("intellect")));
							speler.setCharisma(Integer.parseInt(e.getChild("Parameters").getChildTextTrim("charisma")));
							completed = true;
							break;
						}
					}
				}
			} catch (JDOMException | IOException e) {
				e.printStackTrace();
				logger.debug(e);
			}
		}
		//TODO
		speler.addInventoryItem(Data.herbs.get(1),2);
		speler.addInventoryItem(Data.herbs.get(2),2);
		speler.addInventoryItem(Data.herbs.get(0),1);
		speler.addInventoryItem(Data.herbs.get(3));
		speler.addInventoryItem(Data.herbs.get(4));
		GameFrameCanvas.updatePlayerInfoTable();
		speler.setCurrentPosition(Data.towns.get(0).getPositie());
		speler.initiateLogbook();
	}
	
	public void mainLoop(int[] initialDir){
		int[] direction = initialDir;
		
		while(true){
			// is either initialized from newGame() or from loaded profile of save file
			int[] prevPos = speler.getCurrentPosition();
			
			try{
				// see if weather is different in the next area. If so, give a description of the weather.
				String newWeather = WeatherSimulator.getWeather(prevPos[0] + direction[0], prevPos[1] + direction[1]);
				if(!newWeather.equalsIgnoreCase(WeatherSimulator.getWeatherAtPlayer())){
					WeatherSimulator.getDescriptiveWeather(prevPos[0] + direction[0], prevPos[1] + direction[1]);
				}
				GameFrameCanvas.movementModePanel.setVisible(false);
				logger.info("Direction: " + direction[0] + ", " + direction[1] + "." + "Current pos: " + prevPos[0] + ", " + prevPos[1] + ". ");
				// when you try entering a city whose gate is closed on that side, or there is no gate
				if(direction[0] == 0 && direction[1] == 0){
					direction = Data.wereld[prevPos[0]][prevPos[1]].main_menu();
				}
				else{
					direction = Data.wereld[prevPos[0] + direction[0]][prevPos[1]+direction[1]].main_menu(Town.getDirection(direction[0], direction[1]));
				}
			}catch(NullPointerException e){
				if(speler.checkDood()){
					//reload all the data
					initialize();
					speler = null;
					GameFrameCanvas.playerInfoScrollPane.setVisible(false);
					GameFrameCanvas.clothingPanel.setVisible(false);
					break;
				}
				if(!GameFrameCanvas.textPane.getText().endsWith("You can't go there.\n")){
					RPGMain.printText(true, "You can't go there.");
					logger.debug("Document length: " + GameFrameCanvas.textPane.getText());
				}
				e.printStackTrace();
				logger.error(e);
				//tried to go off the map, let him just stay in the current location
				try{
					logger.debug("Previous position: " + prevPos[0] + "," + prevPos[1] + ". Direction: " + direction[0] + "," + direction[1]);
					direction = Data.wereld[prevPos[0]][prevPos[1]].main_menu();
					continue;
				} catch(Exception exc){
					exc.printStackTrace();
					logger.error(exc);
				}
			} catch(InterruptedException e){
				e.printStackTrace();
				logger.error(e);
			} catch(Exception e){
				e.printStackTrace();
				logger.error(e);
			}
		}
	}
	
	// LOAD DATA
	public static void initialize(){
		try{
			Data data = new Data();
			logger.info("Started loading");
			data.loadEquipment(new File("Data/Equipment.xml"));
			logger.info("Equipment loaded.");
			data.loadClothes(new File("Data/Clothing.xml"));
			logger.info("Clothing loaded.");
			data.loadPotions(new File("Data/Potions.xml"));
			logger.info("Potions loaded.");
			data.loadConsumables(new File("Data/Consumables.xml"));
			logger.info("Consumables loaded.");
			data.loadSpells(new File("Data/Spells.xml"));
			logger.info("Spells loaded.");
			data.loadQuests(new File("Data/Quests.xml"));
			logger.info("Quests loaded.");
			data.loadQNPCs(new File("Data/NPCs.xml"));
			logger.info("NPCs loaded.");
			data.loadItems(new File("Data/Items.xml"));
			logger.info("Items loaded.");
			data.loadHerbs(new File("Data/Herbs.xml"));
			logger.info("Herbs loaded.");
			data.loadAbilities(new File("Data/Abilities.xml"));
			logger.info("Abilities loaded.");
			data.loadEnemies(new File("Data/Enemies.xml"));
			logger.info("Enemies loaded.");
			data.loadHostileAreas(new File("Data/HostileAreas.xml"));
			logger.info("Hostile Areas loaded.");
			data.loadTowns(new File("Data/Towns.xml"));
			logger.info("Towns loaded.");
			//TODO
			//data.loadSeas(new File("Data/Seas.xml"));
			data.loadWereld(new File("Data/WorldMap.xml"));
			logger.info("WorldMap loaded.");
			
			logger.info("Load succesful");
		} catch(Exception e){
			e.printStackTrace();
			logger.debug("Loading failed",e);
		}
	}

	// hoofdmethode voor tekst in JTextArea te zetten
	public static void printText(boolean newLine, String message){
		if(newLine) message+="\n";
		GameFrameCanvas.printText(message, "regular");
	}
	public static void printText(boolean newLine, String message, String style){
		if(newLine) message+="\n";
		GameFrameCanvas.printText(message, style);
	}
	public static void printText(boolean newLine, String[] message, String[] styles){
		if(newLine) message[message.length-1]+="\n";
		GameFrameCanvas.printText(message, styles);
	}
	public static void printEnvironmentInfo(boolean newLine, String message, String style){
		if(newLine) message+="\n";
		GameFrameCanvas.printEnvironmentinfo(message, style);
	}
	// wordt gecalled door JTextField wanneer user iets typt en op enter duwt
	public static void recieveMessage(String message){
		fieldMessage = message;
	}
	public static String waitForMessage(int seconds) throws InterruptedException{
		long start = System.nanoTime();
		
		while(fieldMessage == null){
			Thread.sleep(50);
			if(seconds > 0 && (System.nanoTime()-start)/1000000000 > seconds){
				printText(true,"Not fast enough");
				return "";
			}
		}
		String message = fieldMessage;
		fieldMessage = null;
		try{
			Global.busy = true;
			if(message.equalsIgnoreCase("inventory") || message.equalsIgnoreCase("inv")){
				speler.showInventory();
				message = "";
			}
			else if(message.equalsIgnoreCase("stats") || message.equalsIgnoreCase("character panel") || message.equalsIgnoreCase("char")){
				new CharacterPanel();
				CharacterPanel.showCharacterPanel();
				message = "";
			}
			else if(message.equalsIgnoreCase("questlog")){
				speler.showQuestlog();
				message = "";
			}
			else if(message.equalsIgnoreCase("sleep")){
				speler.sleep();
				message="";
			}
			else if(message.equalsIgnoreCase("clock") || message.equalsIgnoreCase("hour")){
				DayLightThread.giveDescriptiveHour();
				message="";
			}
			else if(message.equalsIgnoreCase("weather")){
				int[] playerPos = speler.getCurrentPosition();
				WeatherSimulator.getDescriptiveWeather(playerPos[0], playerPos[1]);
				message="";
			}
			else if(message.equalsIgnoreCase("log") || message.equalsIgnoreCase("logbook")){
				Logbook.showLogbook();
				message = "";
			}
			else if(message.equalsIgnoreCase("help")){
				printText(true,"General Options:","darkblue");
				printText(true, new String[]{"* inventory || inv: ","Shows your inventory and allows  you to interact with the items within"}, new String[]{"bold","regular"});
				printText(true, new String[]{"* stats || character panel || char: ","Shows your character panel. In here you find all your currently equipped items, your basic information and a graphical representation of your character"}, new String[]{"bold","regular"});
				printText(true, new String[]{"* clock || hour: ","Shows a description of the current time of day"}, new String[]{"bold","regular"});
				printText(true, new String[]{"* log || logbook: ","Shows your logbook, which records your travels and new things you come across while doing so. It is your help in surviving in this world."}, new String[]{"bold","regular"});
				printText(true, new String[]{"* sleep: ","Let your avatar sleep, dependent on your current location"}, new String[]{"bold","regular"});
				printText(true, new String[]{"* weather: ","Shows a description of the weather at your current location"}, new String[]{"bold","regular"});
				Global.pauseProg();
			}
			else if(message.equalsIgnoreCase("save")){
				saveGame();
				message = "";
			}
			else if(message.equalsIgnoreCase("exit")){
				printText(false,"Do you want to save first? [y/n/cancel]\n>");
				String choice = waitForMessage().toLowerCase();
				if(choice.equalsIgnoreCase("cancel")){
					message = "";
				}
				else{
					if(choice.startsWith("y")){
						saveGame();
					}
					logger.info("Exiting game");
					System.exit(0);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e);
		}
		
		Global.busy = false;
		return message;
	}
	// blijft wachten tot user iets getypt heeft, gebruikt voor input als Scanner vervangmiddel
	public static String waitForMessage() throws InterruptedException{
		return waitForMessage(-1);
	}

	public static String upperCaseSingle(String str,int index){
		str = str.toLowerCase();
		return (str.substring(0,index)+str.substring(index,index+1).toUpperCase() + str.substring(index+1));
	}

	public static void setupOnline() throws InterruptedException{
		ServerSocket server = null;
		int serverPort = 0;
		String serverIP = null;
		printText(false,"1: Start own server\n2: Connect to someone else\n>");
		try{
			switch(Integer.parseInt(waitForMessage())){
			case 1: printText(false,"Port number: ");
			serverPort = Integer.parseInt(waitForMessage());
			server = new ServerSocket(serverPort);
			Global.isServer = true;
			new ConnectThread(server);
			client = new Socket("127.0.0.1",serverPort);//zorgt voor het connecteren, max 4 spelers
			break;
			
			case 2: printText(false,"IP address: ");
			serverIP = waitForMessage();
			printText(false,"Port number: ");
			serverPort = Integer.parseInt(waitForMessage());
			printText(true,"Connecting with " + serverIP + " on port " + serverPort);
			client = new Socket(serverIP,serverPort);
			break;
			default: break;
			}
			Global.out = new DataOutputStream(client.getOutputStream());
			new MessageInThread(client); // luistert voor inkomende boodschappen
			/*
			 * 
			 * 	OPSTARTEN VAN CHATPROGRAMMA
			 * 
			 */
			String OS = System.getProperty("os.name");
			/**
			 * UNDER CONSTRUCTION TO RUN ON LINUX
			 */
			if(OS.equalsIgnoreCase("Linux")){

			}
			else{
				ReadWriteTextFile.setContents(new File("ChatProgram.bat"), "", false);//reset file contents
				String exec1 = "@echo off";
				String exec2 = "java -jar ChatProgram.jar " + Integer.toString(serverPort+1) + " " + Global.isServer + " " + serverIP;
				ReadWriteTextFile.setContents(new File("ChatProgram.bat"), exec1,true);
				ReadWriteTextFile.setContents(new File("ChatProgram.bat"), "\n",true);
				ReadWriteTextFile.setContents(new File("ChatProgram.bat"), exec2,true);
				String[] cmd = {"cmd.exe","/C","start ChatProgram.bat"};
				Runtime.getRuntime().exec(cmd);
			}
			Global.pauseProg();
		} catch(ConnectException co){
			printText(true,"Unable to connect.");
			Global.pauseProg();
		} catch(NoRouteToHostException e){
			printText(true,"Unable to reach the specified host.");
		} catch(IOException io){
			io.printStackTrace();
			Global.pauseProg();
		}
	}
	
	public static boolean saveGame() throws InterruptedException{
		/*/
		 * 
		 * What does it need to save?
		 * - Avatar
		 * - Logbook
		 * - Made discoveries
		 * - State of the society
		 * - Hope map
		 * - Current season and weather
		 * - State of all NPCs
		 * - 
		 */
		
		ObjectOutputStream outputStream = null;
		FileOutputStream fos = null;
		String fileName = null;
		while(true){
			printText(false,"Under what name would you like to save your progress?\n>");
			fileName = waitForMessage();
			fileName = "SaveGames/" + fileName + ".ser";
			if(new File(fileName).exists()){
				printText(false,"This filename is already taken. Overwrite ? [y/n]\n>");
				if(waitForMessage().toLowerCase().startsWith("y")){
					new File(fileName).delete();
					break;
				}
			}
			else if(!new File("SaveGames").exists()){
				new File("SaveGames").mkdir();
				break;
			}
			else{
				break;
			}
		}
		try {
			//Construct the LineNumberReader object
			fos = new FileOutputStream(fileName,true);
			outputStream = new ObjectOutputStream(fos); 
			// write all objects to Stream
			outputStream.writeObject(speler);
			outputStream.writeObject(Logbook.getRootNode());
			outputStream.writeObject(Data.wereld);
			outputStream.writeObject(dayLightThread);
			printText(true,"Progress succesfully saved.");

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			logger.debug(ex);
		} catch (IOException ex) {
			ex.printStackTrace();
			logger.debug(ex);
		} catch(Exception e){
			e.printStackTrace();
			logger.error(e);
			printText(true,"An error occurred while trying to save.", "red");
		} finally {
			//Close the ObjectOutputStream
			try {
				if (outputStream != null) {
					outputStream.flush();
					outputStream.close();
					fos.close();
					Global.pauseProg();
					return true;
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				logger.debug(ex);
			}
		}
		return false;
	}
	
	public static void loadGame() throws InterruptedException {
		//Alle nodige variabelen aanmaken
		ObjectInputStream inputStream = null;
		String curDir = System.getProperty("user.dir");
		File dir = new File(curDir + "/SaveGames");
		String filename = null;
		String[] dirList = dir.list();

		//Naamfilter zodat er alleen .ser (save) files worden getoond 
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".ser");
			}
		};
		//Printen van de mogelijkheden
		dirList = dir.list(filter);
		if(dirList.length != 0){
			while(true){
				for(int j=0;j<dirList.length;j++){
					printText(true,(j+1)+ ": " + dirList[j].split("\\.")[0]);
				}
				printText(true,(dirList.length+1) + ": Cancel");
				printText(false,"What file would you like to load?\n>");
				int keuze = Integer.parseInt(waitForMessage());
				if((keuze < dirList.length+1) && (keuze > 0)){
					filename = "SaveGames/" + dirList[keuze-1];
					break;
				} else if (keuze == dirList.length+1) {
					return;
				}
				else{
					printText(true,"Not a valid option.");
				}
			}
			try {

				//Construct the ObjectInputStream object
				inputStream = new ObjectInputStream(new FileInputStream(filename));

				speler = (Avatar)inputStream.readObject();
				GameFrame.logbook.setLogbook((LogbookNode)inputStream.readObject());
				Data.setWereld((Location[][])inputStream.readObject());
				dayLightThread = (DayLightThread)inputStream.readObject();

			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
				logger.error(e);
				printText(true,"An error occurred while loading.", "red");
			}finally {
				//Close the ObjectInputStream
				try {
					if (inputStream != null) {
						inputStream.close();
						printText(true,"Progress succesfully loaded.\nWelcome back, " + speler.getName() + ".");
						
						speler.addHP(0);
						GameFrameCanvas.playerPortrait.setShowStats(true);
						GameFrameCanvas.playerPortrait.setHunger(speler.getHunger());
						GameFrameCanvas.playerPortrait.setThirst(speler.getThirst());
						GameFrameCanvas.playerPortrait.setFitness(speler.getFitness());
						
						GameFrameCanvas.setWalking.setVisible(true);
						GameFrameCanvas.setRunning.setVisible(true);
						GameFrameCanvas.setCrouching.setVisible(true);
						
						Global.pauseProg();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		else {
			printText(true,"There are no saved files.");
			Global.pauseProg();
		}
	}
}
