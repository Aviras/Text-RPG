package game;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.Timer;


import org.apache.log4j.Logger;
import org.jdom.Element;

import bsh.EvalError;
/* MESSAGE PROTOCOL
 * CLIENT MAKES CHARACTER WITH NEW NAME 'X' => NAME: X
 * CLIENT DOES 'Y' DAMAGE TO MOB AT INDEX 'X' => DMG: X Y
 * CLIENT HEALS ALLY 'X' FOR 'Y' => HEAL: X Y
 * 
 * CONCLUSION: FIRST TARGET, THEN AMOUNT IF APPLICABLE
 * 
 */
public class HostileArea extends Location implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String naam,image,action;
	private String category,type;
	private String weather;
	private ArrayList<int[]> playerPath = new ArrayList<int[]>();
	private HashMap<Integer,int[]> artifacts;
	private HashMap<Integer,DungeonRoom[][]> dungeon;
	private int WIDTH,HEIGHT,SWIMTIREDNESS = 8;
	private int[] playerPos = new int[2];
	private String[] dungeonStringsDay,dungeonStringsNight;
	private ArrayList<int[]> herbPos;
	private int dungeonStringProgression = 0;
	private TemperatureChecker tC;
	private boolean fleeSuccess = false;
	private int currentLevel = 0;
	private boolean justEntered = false;
	private int[] playerNoiseCentre;
	private static boolean keyPressed;
	private static int[] direction;
	private static String keyCode;
	private static boolean inCombat = false;
	private static boolean busyEvent = false;
	
	public static final int KEYSLEEPPERIOD = 20;
	
	private static final Logger logger = Logger.getLogger(HostileArea.class);
	
	public HostileArea(){
		
	}
	
	public HostileArea(Integer id,String name,String category,String type, Element dungeonMap, Element extra,String image){
		ID = id.intValue();
		naam = name;
		this.category = category;
		this.type = type;
		this.image = image;
		buildMap(dungeonMap);
		herbPos = new ArrayList<int[]>();
		@SuppressWarnings("unchecked")
		List<Element> children = extra.getChildren();
		Iterator<Element> i = children.iterator();
		while(i.hasNext()){
			Element child = i.next();
			int X = Integer.parseInt(child.getAttributeValue("x"));
			int Y = Integer.parseInt(child.getAttributeValue("y"));
			int Z = 0;
			int z1 = 0;
			int z2 = 0;
			try{
				Z = Integer.parseInt(child.getAttributeValue("z"));
			} catch(NumberFormatException e){
				z1 = Integer.parseInt(child.getAttributeValue("z1"));
				z2 = Integer.parseInt(child.getAttributeValue("z2"));
			}
			try{
				// set enemy
				if(child.getName().equalsIgnoreCase("enemy")){
					dungeon.get(Z)[X][Y].addEnemy(Integer.parseInt(child.getTextTrim()));
				}
				else if(child.getName().equalsIgnoreCase("npc")){
					dungeon.get(Z)[X][Y].setDungeonNPC(Integer.parseInt(child.getText()));
				}
				else if(child.getName().equalsIgnoreCase("lake")){
					dungeon.get(Z)[X][Y].setTerrainType("water");
				}
				else if(child.getName().equalsIgnoreCase("herb")){
					herbPos.add(new int[]{X,Y,Z,Integer.parseInt(child.getText())});
					dungeon.get(Z)[X][Y].addHerb(Integer.parseInt(child.getText()));
				}
				else if(child.getName().equalsIgnoreCase("event")){
					String eventType = child.getAttributeValue("type");
					String logbookPath = child.getAttributeValue("path");
					if(logbookPath == null || logbookPath.equalsIgnoreCase("")){
						dungeon.get(Z)[X][Y].setDungeonEvent(eventType,child.getTextTrim());
					}
					else{
						dungeon.get(Z)[X][Y].setDungeonEvent(eventType, child.getTextTrim(), logbookPath);
					}
				}
				else if(child.getName().equalsIgnoreCase("entrance")){
					for(Element texts: (List<Element>)child.getChildren("text")){
						if(Integer.parseInt(texts.getAttributeValue("z")) == z1){
							try{
								dungeon.get(z1)[X][Y].setDungeonEvent("entrance", texts.getTextTrim(),z2);
							} catch(NullPointerException e){
								logger.debug("Corrupt data, incorrect entrance on layer " + z1 + " at " + X + ", " + Y + ".");
							}
						}
						else{
							try{
								dungeon.get(z2)[X][Y].setDungeonEvent("entrance", texts.getTextTrim(),z1);
							} catch(NullPointerException e){
								logger.debug("Corrupt data, incorrect entrance on layer " + z1 + " at " + X + ", " + Y + ".");
							}
						}
					}
				}
				else if(child.getName().equalsIgnoreCase("artifact")){
					if(artifacts == null){
						artifacts = new HashMap<Integer,int[]>();
					}
					String eventType = child.getAttributeValue("type");
					
					try{
						artifacts.put(Integer.parseInt(eventType.split(":")[1]), new int[]{X,Y,Z});
						dungeon.get(Z)[X][Y].setDungeonEvent(eventType, child.getTextTrim());
					} catch(Exception e){
						e.printStackTrace();
						logger.error(e);
					}
				}
			} catch(Exception e){
				e.printStackTrace();
				logger.debug("Exception thrown for " + child.getName() + " in " + name + " for x=" + X + ", y=" + Y + ", z=" + Z + ", z1=" + z1 + ", z2=" + z2, e);
			}
		}
	}
	public DungeonRoom[][] getDungeon(int layer){
		return dungeon.get(layer);
	}
	public HashMap<Integer,DungeonRoom[][]> getDungeon(){
		return dungeon;
	}
	public HashMap<Integer,int[]> getArtifacts(){
		return artifacts;
	}
	public void removeArtifact(int ID){
		int[] artifactPos = artifacts.get(ID);
		dungeon.get(artifactPos[2])[artifactPos[0]][artifactPos[1]].removeEvent();
		artifacts.remove(ID);
	}
	public void buildMap(Element dungeonMap){

		Element heightMapEl = dungeonMap.getChild("heightMap");
		List<Element> layers = dungeonMap.getChildren("layer");
		Iterator<Element> layersIt = layers.iterator();
		
		dungeon = new HashMap<Integer,DungeonRoom[][]>();
		
		
		//implement the main heightMap
		String heightMap = heightMapEl.getTextTrim();
		// get # rows and columns
		String[] lines = heightMap.split(System.getProperty("line.separator"));
		if(lines.length == 1){
			lines = heightMap.split("\n");
		}
		lines = trimStringArray(lines);
		HEIGHT = lines.length;
		WIDTH = 0;
		for(String str: lines){
			if(str.length() > WIDTH) WIDTH = str.length();
		}
		DungeonRoom[][] mainLevel = new DungeonRoom[WIDTH][HEIGHT];
		
		for(int k=0;k<HEIGHT;k++){
			for(int j=0;j<WIDTH;j++){
				int height = Character.digit(lines[k].charAt(j),10);
				int[] position = {j,k};
				mainLevel[j][k] = new DungeonRoom(height,position);
			}
		}
		dungeon.put(0, mainLevel);
		
		//implement the layers
		while(layersIt.hasNext()){
			Element layer = layersIt.next();
			String layerMap = layer.getTextTrim();
			int level = Integer.parseInt(layer.getAttributeValue("level"));

			String[] layerLines = layerMap.split(System.getProperty("line.separator"));
			if(layerLines.length == 1){
				layerLines = layerMap.split("\n");
			}
			layerLines = trimStringArray(layerLines);
			
			DungeonRoom[][] layerLevel = new DungeonRoom[WIDTH][HEIGHT];
			
			for(int k=0;k<HEIGHT;k++){
				for(int j=0;j<WIDTH;j++){
					if(layerLines[k].charAt(j) != 'o'){
						layerLevel[j][k] = new DungeonRoom(0,new int[]{j,k});
					}
				}
			}
			dungeon.put(level, layerLevel);
		}
		//TODO
		
		String[] pathDay = {category.toLowerCase(),"day"};
		String[] pathNight = {category.toLowerCase(),"night"};
		try{
			dungeonStringsDay = Global.rwtext.getContent(new File("Data/DungeonStrings.xml"), pathDay).split(System.getProperty("line.separator"));
			dungeonStringsNight = Global.rwtext.getContent(new File("Data/DungeonStrings.xml"), pathNight).split(System.getProperty("line.separator"));
			dungeonStringsDay = trimStringArray(dungeonStringsDay);
			dungeonStringsDay = mixArray(dungeonStringsDay);
			dungeonStringsNight = mixArray(dungeonStringsNight);
		} catch(NullPointerException e){
			logger.error(e);
		}
	}
	public void calculateHopeImpact(){
		//TODO
		// changes the amount of wildlife,
		// changes the respawn timer on enemies
		// changes the respawn timer on herbs, different for poisonous and beneficial herbs
		// changes types of events that happen
		// water can become poisoned
		// different description when entering
		
	}
	public String getPositionDescription(){
		String text = "";
		String terrainType = dungeon.get(currentLevel)[playerPos[0]][playerPos[1]].getTerrainType();
		
		if(currentLevel == 0){
			if(terrainType.equalsIgnoreCase("water")){
				text+=" in a river,";
			}
		}
		else if(currentLevel < 0){
			if(terrainType.equalsIgnoreCase("water")){
				text+=" in a small pool";
			}
			text+=" in a cave,";
		}
		else if(currentLevel > 0){
			text+=" up high in the trees,";
		}
		
		double heightRatio = (double)playerPos[1]/(double)HEIGHT;
		double widthRatio = (double)playerPos[0]/(double)WIDTH;
		
		text+=" in the ";
		
		if(heightRatio < 0.33){
			text+="North";
		}
		else if(heightRatio > 0.66){
			text+="South";
		}
		if(widthRatio < 0.33){
			if(heightRatio < 0.33 || heightRatio > 0.66){
				text+="-";
			}
			text+="West";
		}
		else if(widthRatio > 0.66){
			if(heightRatio < 0.33 || heightRatio > 0.66){
				text+="-";
			}
			text+="East";
		}
		
		text+=" of " + naam;
		
		//TODO
		/*if(category.equalsIgnoreCase("forest")){
			text+=" woods.";
		}*/
		
		return text;
	}
	public static void setKeyCode(String s){
		keyCode = s;
	}
	public String[] trimStringArray(String[] array){

		for(int j=0;j<array.length;j++){
			array[j] = array[j].trim();
		}
		return array;
	}
	public String[] mixArray(String[] array){
		//create empty string array
		String[] b = new String[array.length];
		Random generator = new Random();
		int i = 0;
		while(i<array.length){
			int k = generator.nextInt(array.length);
			if(array[k] != null){
				b[i] = array[k];
				array[k] = null;
				i++;
			}
		}
		return b;
	}
	/* GETTERS */
	public String getName(){
		return naam;
	}
	public String getImage(){
		return image;
	}
	public int getWidth(){
		return WIDTH;
	}
	public int getHeight(){
		return HEIGHT;
	}
	public String getType(){
		return type;
	}
	public int[] getPlayerPosition(){
		return playerPos;
	}
	public int getID(){
		return ID;
	}
	public int[] getPositie(){
		return positie;
	}
	public int[] getPlayerNoiseCentre(){
		return playerNoiseCentre;
	}
	public String getLocationType(){
		String locationType = category + " " + type;
		
		if(altitude >= 5){
			locationType+=" mountain";
		}
		
		return locationType.toLowerCase();
	}
	public static int sumIntArray(int[] array){
		int sum = 0;
		for(int j = 0;j<array.length;j++){
			sum+= array[j];
		}
		return sum;
	}
	public void printStory() throws InterruptedException{
		String[] path = {naam,"Story"};
		Global.makeDialog(new File("Data/LocationDialog.xml"),path);
	}
	public void setPlayerPosition(){
		RPGMain.speler.setCurrentPosition(positie);
	}
	public void setPlayerNoiseCentre(int[] i){
		playerNoiseCentre[0] = i[0];
		playerNoiseCentre[1] = i[1];
		playerNoiseCentre[2] = i[2];
	}
	public int[] findEntrance(String direction){
		int[] entrance = {0,0};
		if(direction.equalsIgnoreCase("East")){
			if(Data.wereld[positie[0]-1][positie[1]] instanceof HostileArea){
				HostileArea dummy = (HostileArea)Data.wereld[positie[0]-1][positie[1]];
				entrance[1] = dummy.getPlayerPosition()[1]*HEIGHT/dummy.getHeight();
			}
			else{
				entrance[1] = HEIGHT/2;
			}
		}
		else if(direction.equalsIgnoreCase("West")){
			entrance[0] = WIDTH-1;
			if(Data.wereld[positie[0]+1][positie[1]] instanceof HostileArea){
				HostileArea dummy = (HostileArea)Data.wereld[positie[0]+1][positie[1]];
				entrance[1] = dummy.getPlayerPosition()[1]*HEIGHT/dummy.getHeight();
			}
			else{
				entrance[1] = HEIGHT/2;
			}
		}
		else if(direction.equalsIgnoreCase("North")){
			entrance[1] = HEIGHT-1;
			if(Data.wereld[positie[0]][positie[1]+1] instanceof HostileArea){
				HostileArea dummy = (HostileArea)Data.wereld[positie[0]][positie[1]+1];
				entrance[0] = dummy.getPlayerPosition()[0]*WIDTH/dummy.getWidth();
			}
			else{
				entrance[0] = WIDTH/2;
			}
		}
		else if(direction.equalsIgnoreCase("South")){
			entrance[1] = 0;
			if(Data.wereld[positie[0]][positie[1]-1] instanceof HostileArea){
				HostileArea dummy = (HostileArea)Data.wereld[positie[0]][positie[1]-1];
				entrance[0] = dummy.getPlayerPosition()[0]*WIDTH/dummy.getWidth();
			}
			else{
				entrance[0] = WIDTH/2;
			}
		}
		else if(direction.equalsIgnoreCase("South-west")){
			entrance[0] = WIDTH-1;
		}
		else if(direction.equalsIgnoreCase("North-west")){
			entrance[0] = WIDTH-1;
			entrance[1] = HEIGHT-1;
		}
		else if(direction.equalsIgnoreCase("North-east")){
			entrance[1] = HEIGHT-1;
		}
		else if(direction.equalsIgnoreCase("START")){
			entrance[0] = playerPos[0];
			entrance[1] = playerPos[1];
		}
		return entrance;
	}
	// is only externally called when player can't enter next area, and so it doesn't reset as if he was never there before
	public int[] main_menu() throws InterruptedException{
		
		logger.info("Entering " + naam);
		
		////System.out.println("Temperature: " + temperature + " Weather: " + weather);
		
		//xPos,yPos,radius
		playerNoiseCentre = new int[3];
		
		int[] dir = dungeon.get(0)[playerPos[0]][playerPos[1]].enter();
		
		while(true && dir != null && !RPGMain.speler.checkDood()){
			int[] prevPosDung = playerPos;
			int height = dungeon.get(currentLevel)[prevPosDung[0]][prevPosDung[1]].getHeight();
			try{
				//only when travelling across the main height map
				if(currentLevel == 0){
					int heightDiff = height - dungeon.get(0)[prevPosDung[0] + dir[0]][prevPosDung[1] + dir[1]].getHeight();
					//take falling damage proportional to sqrt(height)*mass, starting at height difference = 2
					if(heightDiff >= 2){
						int damage = (int)(RPGMain.speler.getMaxHP()*RPGMain.speler.getPlayerWeight()*Math.sqrt(heightDiff)/220);
						RPGMain.printText(true, "You fall from a too high distance and hurt yourself as the ground greets your face. Lost " + damage + " damage");
						RPGMain.speler.addHP(-damage);
						RPGMain.speler.addFitness(-20);
					}
					//see if it isn't too steep to move, and check for Mountaineering gear
					else if(heightDiff <= -2){
						if(RPGMain.speler.hasItem("Mountaineering gear")){
							RPGMain.printText(true, "Carefully, you climb up the steep hill.");
							Global.pauseProg(1500);
							RPGMain.speler.addHunger((int)Math.pow(Math.abs(heightDiff)+1,1.5));
							RPGMain.speler.addThirst((int)Math.pow(Math.abs(heightDiff)+1,2));
							RPGMain.speler.addFitness(-(int)((1.0-2.0*RPGMain.speler.getStamina()/300.0)*Math.pow(Math.abs(heightDiff)+1, 2.5)));
						}
						else{
							RPGMain.printText(true, "That direction is too steep. You need Mountaineering gear to climb up here.");
							dir = new int[]{0,0};
							setKeyPressed(false);
							Global.pauseProg(1500);
						}
					}
					else{
						double speed = 1.0;
						if(RPGMain.speler.getMovementMode().equalsIgnoreCase("running")){
							speed = 2.0;
						}
						RPGMain.speler.addHunger((int)Math.abs(heightDiff*speed)+1);
						RPGMain.speler.addThirst((int)Math.pow(Math.abs(heightDiff*speed)+1,1.5));
						RPGMain.speler.addFitness(-(int)((1.0-2.0*RPGMain.speler.getStamina()/300.0)*Math.pow(Math.abs(heightDiff*speed)+1, 1.5)));
					}
				}
				
				dir = dungeon.get(currentLevel)[prevPosDung[0] + dir[0]][prevPosDung[1] + dir[1]].enter();
				logger.info("Direction: " + dir[0] + ", " + dir[1] + " currentLevel: " + currentLevel);
			} catch(ArrayIndexOutOfBoundsException e){
				logger.error("MapArray out of bounds, going up. Dir: " + dir[0] + ", " + dir[1]);
				setKeyPressed(false);
				return dir;
			} 
		}
		setKeyPressed(false);
		logger.debug("Returning null");
		return null;
	}
	public int[] main_menu(String direction) throws InterruptedException{
		playerPos = findEntrance(direction);
		
		initializeSounds();
		
		setPlayerPosition();
		
		new KeyChecker();
		
		new HerbPlacer();
		
		weather = WeatherSimulator.getWeather(positie[0], positie[1]);
		
		Global.soundEngine.chooseAndPlaySound(weather, type, WeatherSimulator.getSolarIntensity(positie[0], positie[1]), true, new String[]{"ambient"}, 10, 0);
		
		//FIXME
		/*GameFrameCanvas.imagePanel.changeImage("HostileArea "+ID);
		GameFrameCanvas.imagePanel.setVisible(true);*/
		
		RPGMain.printText(true, "Entering " + naam + ".");
		Global.pauseProg(2000);
		
		// visibility of different panels
		GameFrameCanvas.dungeonMap.initDungeonMap(dungeon, positie);
		managePanelVisibility("hostileArea");
		
		new EnemyMover();
		//TODO activate temperatureChecker
		tC = new TemperatureChecker(temperature);
		
		return main_menu();
	}
	
	public static boolean lineOfSight(int[] playerCoord, int[] enemyCoord,HashMap<double[][],Double> covers){
		if(getCover(playerCoord,enemyCoord,false,covers) > 0.6){
			return false;
		}
		return true;
	}
	public double getMobDensity(int radius){
		
		int amount = 0;
		
		for(int j=-radius;j<=radius;j++){
			for(int k=-radius;k<=radius;k++){
				try{
					amount+=dungeon.get(0)[playerPos[0]+j][playerPos[1]+k].getMobs().size();
				} catch(NullPointerException e){
					logger.error("No dungeon initialized",e);
				} catch(ArrayIndexOutOfBoundsException e){
				}
			}
		}
		
		return amount/(Math.PI*radius*radius);
	}
	
	// gets the cover the target is in, with respect to the actor
	// effectiveCover is boolean if necessary to correct for distance behind cover. ie, more cover when right behind it, than when 10 feet behind a rock
	public static double getCover(int[] actorCoord, int[] targetCoord, boolean effectiveCover, HashMap<double[][],Double> covers){
		
		//logger.info("Entering getCover()");
		
		// the algorithm goes on a straight line between target and actor, and checks for every cover if its radius reaches the line
		double rico = (double)(actorCoord[1] - targetCoord[1])/(double)(actorCoord[0] - targetCoord[0]);
		double offset = actorCoord[1] - rico*actorCoord[0];
		
		double lowestX = Math.min(actorCoord[0], targetCoord[0]);
		double highestX = Math.max(actorCoord[0], targetCoord[0]);
		
		double highestCover = 0;
		
		Iterator<double[][]> coverSizes = covers.keySet().iterator();

		while(coverSizes.hasNext()){
			double[][] sizes = coverSizes.next();
			Double cover;
			// take in effect of distance to cover
			if(effectiveCover){
				// angle relative to horizontal between target and actor
				double angle = Math.atan(rico);
				// radius of the cover, as an ellipsoid at that angle
				double radius = 1.0/Math.sqrt(Math.pow(Math.cos(angle)/(Global.coverSize/2*sizes[1][0]), 2) + (1-Math.cos(angle)*Math.cos(angle))/Math.pow(Global.coverSize/2*sizes[1][1],2));
				// correction made according to proximity to the cover (ie more cover when right behind, less when 3 feet behind it)
				if(new Point(targetCoord[0],targetCoord[1]).distance(sizes[0][0],sizes[0][1]) < 2.5*radius){
					////System.out.println("Original cover: " + covers.get(sizes));
					cover = covers.get(sizes)*(1.2*radius/new Point(targetCoord[0],targetCoord[1]).distance(sizes[0][0],sizes[0][1]));
					////System.out.println("After effective cover: " + cover);
				}
				else{
					cover = 0.2;
				}
			}
			else{
				cover = covers.get(sizes);
			}
			// x=x+5 to save calculation time, not every pixel between them needs to be checked
			for(double x=lowestX;x<highestX;x=x+5){
				//TODO check what I actually did here
				double cosTheta = (double)(x-sizes[0][0])/Math.sqrt(Math.pow(x - sizes[0][0], 2) + Math.pow(rico*x+offset - sizes[0][1], 2));
				double radius = 1.0/Math.sqrt(Math.pow(cosTheta/(Global.coverSize/2*sizes[1][0]), 2) + (1-cosTheta*cosTheta)/Math.pow(Global.coverSize/2*sizes[1][1],2));
				if(new Point((int)x,(int)(rico*x + offset)).distance(sizes[0][0],sizes[0][1]) < radius && cover > highestCover){
					highestCover = cover;
				}
			}
		}
		return highestCover;
	}
	
	private void setKeyPressed(boolean b){
		keyPressed = b;
		direction = new int[2];
	}
	
	private class KeyChecker extends Thread{
		
		public KeyChecker(){
			direction = new int[2];
			
			InputMap inputMap = GameFrameCanvas.textField.getInputMap();
			
			inputMap.put(KeyStroke.getKeyStroke("LEFT"), "pressedLeft");
			inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "pressedRight");
			inputMap.put(KeyStroke.getKeyStroke("UP"), "pressedUp");
			inputMap.put(KeyStroke.getKeyStroke("DOWN"), "pressedDown");
			inputMap.put(KeyStroke.getKeyStroke("released LEFT"), "releasedLeft");
			inputMap.put(KeyStroke.getKeyStroke("released RIGHT"), "releasedRight");
			inputMap.put(KeyStroke.getKeyStroke("released UP"), "releasedUp");
			inputMap.put(KeyStroke.getKeyStroke("released DOWN"), "releasedDown");
			
			ActionMap actionMap = GameFrameCanvas.textField.getActionMap();
			
			actionMap.put("pressedLeft", new DirectionAction(new int[]{-1,0}));
			actionMap.put("pressedRight", new DirectionAction(new int[]{1,0}));
			actionMap.put("pressedUp", new DirectionAction(new int[]{0,-1}));
			actionMap.put("pressedDown", new DirectionAction(new int[]{0,1}));
			
			
			Action releasedKey = new AbstractAction(){
				public void actionPerformed(ActionEvent evt){
					keyPressed = false;
				}
			};
			
			actionMap.put("releasedLeft", releasedKey);
			actionMap.put("releasedRight", releasedKey);
			actionMap.put("releasedUp", releasedKey);
			actionMap.put("releasedDown", releasedKey);
			
			start();
		}
		
		public void run(){
			while(RPGMain.speler != null && !RPGMain.speler.checkDood() && RPGMain.speler.getCurrentPosition()[0] == positie[0] && RPGMain.speler.getCurrentPosition()[1] == positie[1]){
				try{
					sleep(KEYSLEEPPERIOD);
				} catch(InterruptedException e){
					e.printStackTrace();
					logger.error(e);
				}
				if(keyPressed && !inCombat && !Global.pause && !busyEvent && !Global.busy){
					GameFrameCanvas.dungeonMap.movePlayer(direction);
				}
				else{
					setKeyPressed(false);
				}
			}
		}
	}
	
	private class DirectionAction extends AbstractAction{
		private int[] dir;
		
		public DirectionAction(int[] dir){
			super();
			this.dir = dir;
		}
		
		public void actionPerformed(ActionEvent evt){
			keyPressed = true;
			direction = dir;
		}
	}
	
	
	
	
	public class DungeonRoom implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private DungeonEvent event = null;
		private ArrayList<Enemy> mobs = new ArrayList<Enemy>();
		private Herb herb = null;
		private int targetIndex = 0;
		private int[] position;
		private int height;
		private String terrainType = "ground";
		private Random generator = new Random();
		private int[] playerCoord;
		private ArrayList<int[]> enemyCoords = new ArrayList<int[]>();
		private int actionsCompleted = 0;
		private double heightModifier;
		private double chargeModifier = 1;
		private HashMap<double[][],Double> covers = new HashMap<double[][],Double>();
		private TreeMap<Double,Integer> turnMap;
		private HashMap<int[],Item> itemsOnFloor;
		
		
		public DungeonRoom(){
			
		}
		public DungeonRoom(int height,int[] position){
			this.height = height;
			this.position = position;
		}
		public void addEnemy(int id){
			mobs.add(new Enemy(Data.enemies.get(id)));
		}
		public void addEnemy(Enemy mob, int z){
			mobs.add(mob);
			if(position[0] == playerPos[0] && position[1] == playerPos[1] && currentLevel == z){
				if(inCombat){
					int fieldWidth = GameFrameCanvas.battlefield.getWidth();
					int fieldHeight = GameFrameCanvas.battlefield.getHeight();
					
					int[] enemyCoord = new int[2];
					enemyCoord[0] = generator.nextInt(fieldWidth);
					enemyCoord[1] = generator.nextInt(fieldHeight);
					while(checkObstruction(enemyCoord)){
						enemyCoord[0] = generator.nextInt(fieldWidth);
						enemyCoord[1] = generator.nextInt(fieldHeight);
					}
					synchronized(turnMap){
						enemyCoords.add(enemyCoord);
						turnMap.put(Math.random(), enemyCoords.size()-1);
					}
					GameFrameCanvas.battlefield.updateBattlefield(covers, playerCoord, enemyCoords);
					RPGMain.recieveMessage("enemyAdded\n");
					RPGMain.printText(true, "Another enemy has joined the fight!");
				}
				else{
					RPGMain.recieveMessage("addedEnemy");
				}
			}
		}
		public void addHerb(int ID){
			herb = Data.herbs.get(ID);
		}
		public void addEvent(DungeonEvent event){
			this.event = event;
		}
		public void removeEvent(){
			event = null;
		}
		public boolean hasAliveEnemies(){
			for(Enemy mob: mobs){
				if(!mob.checkDood()){
					logger.debug("present enemy: " + mob.getName() + ", ID: " + mob.getID());
					return true;
				}
			}
			return false;
		}
		public void removeEnemy(Enemy mob){
			mobs.remove(mob);
		}
		public ArrayList<Enemy> getMobs(){
			return mobs;
		}
		public void setTerrainType(String s){
			terrainType = s;
		}
		public String getTerrainType(){
			return terrainType;
		}
		public void setDungeonEvent(String type, String description){
			event = new DungeonEvent(type,description);
		}
		public void setDungeonEvent(String type, String description, String path){
			event = new DungeonEvent(type,description,path);
		}
		public void setDungeonEvent(String type, String description, int levelTo){
			event = new DungeonEvent(type,description,levelTo);
		}
		public void setDungeonNPC(int id){
			event = new DungeonEvent(id);
		}
		public void updatePlayerCoord(int[] coords){
			playerCoord[0] = coords[0];
			playerCoord[1] = coords[1];
		}
		public int[] enter() throws InterruptedException{
			
			logger.info("I'm at " + position[0] + "," + position[1] + "," + currentLevel);
			playerPos[0] = position[0];
			playerPos[1] = position[1];
			// for enemies to track player
			playerPath.add(new int[] {playerPos[0],playerPos[1]});
			if(playerPath.size() > 25){
				playerPath.remove(0);
			}
			weather = WeatherSimulator.getWeather(positie[0], positie[1]);
			////System.out.println(weather);
			////System.out.println("LightIntensity:" + WeatherSimulator.getSolarIntensity(positie[0], positie[1]));
			GameFrameCanvas.dungeonMap.updatePlayerPos(playerPos,direction,currentLevel);
			tC.updateTemperature(temperature);
			
			logger.debug("Past weather stuff");
			
			if(RPGMain.speler.getMovementMode().equalsIgnoreCase("running")){
				if(Math.random() < 0.5/Math.pow(RPGMain.speler.getStamina(),1.5)){
					RPGMain.speler.addStamina(1);
				}
			}
			else if(RPGMain.speler.getMovementMode().equalsIgnoreCase("walking")){
				if(Math.random() < 0.5/Math.pow(RPGMain.speler.getStamina(),2)){
					RPGMain.speler.addStamina(1);
				}
			}
			else{
				if(Math.random() < 0.5/Math.pow(RPGMain.speler.getStamina(),2.5)){
					RPGMain.speler.addStamina(1);
				}
			}
			
			double distToWater = 100;
			double distToEntrance = 0;
			
			for(int j=-30;j<=30;j++){
				for(int k=-30;k<=30;k++){
					try{
						if(dungeon.get(currentLevel)[playerPos[0] + j][playerPos[1] + k].getTerrainType().equalsIgnoreCase("water") && Math.sqrt(j*j + k*k) < distToWater){
							distToWater = Math.sqrt(j*j + k*k);
						}
						if(dungeon.get(currentLevel)[playerPos[0] + j][playerPos[1] + k].getEvent().getType().equalsIgnoreCase("entrance")){
							distToEntrance = Math.sqrt(j*j + k*k);
						}
					} catch(ArrayIndexOutOfBoundsException e){
					} catch(NullPointerException e){
					}
				}
			}
			
			
			// SOUND STUFF
			if(distToWater < 10 && temperature > -5){
				Global.soundEngine.playSound("Sounds/Ambient/river_soft.mp3", "ambient", 100, 0,(float)(-4*distToWater),false);
				logger.info("Playing water sound");
			}
			else{
				SoundEngine.closeLine("Sounds/Ambient/river_soft.mp3", "ambient");
			}
			if(currentLevel < 0){
				SoundEngine.setWeatherGain(Math.max(-50,(int)distToEntrance*currentLevel*8));
				SoundEngine.setAmbientGain(Math.max(-50,(int)distToEntrance*currentLevel*8));
			}
			
			logger.debug("Past sound stuff");
			
			
			// WATER IN SQUARE
			if(terrainType.equalsIgnoreCase("water")){
				if(temperature <= 0 && temperature > -5){
					RPGMain.printText(true, "The ice is only slightly frozen, careful not to fall through.");
					Global.pauseProg(2000);
					if(Math.random() < 0.25){
						RPGMain.printText(true, "The ice gave way beneath you feet and fall in the bitterly cold water. You manage to get back out again, but you feel weakened and hungry.");
						RPGMain.speler.addHunger(30);
						RPGMain.speler.addFitness(-30);
					}
				}
				else if(temperature <= -5){
					RPGMain.printText(true, "The water is frozen and it looks safe to walk over.");
				}
				else{
					////System.out.println("I'm swimming!");
					SWIMTIREDNESS--;
					if(Math.random() < 0.5/Math.sqrt(RPGMain.speler.getSwimming())){
						RPGMain.speler.addSwimming(1);
					}
					switch(SWIMTIREDNESS){
					case 5: RPGMain.printText(true, "Better not to swim too far.");
							break;
					case 2: RPGMain.printText(true, "Your arms are growing really tired. You should better head back.");
							break;
					case 0: RPGMain.printText(true, "This is it, you're exhausted.");
							Global.pauseProg(2000);
							double rescue = Math.random();
							if(rescue > 0.7){
								RPGMain.printText(true, "A nearby boat saw you and got you onboard.\nYou got lucky there.");
								SWIMTIREDNESS = Math.min(SWIMTIREDNESS, RPGMain.speler.getSwimming()/2 + 7);
							}
							else{
								RPGMain.speler.addHP(-RPGMain.speler.getMaxHP());
								return null;
							}
					}
				}
			}
			else{
				SWIMTIREDNESS++;
				SWIMTIREDNESS = Math.min(SWIMTIREDNESS, RPGMain.speler.getSwimming()/2 + 7);
			}
			
			logger.debug("Past swimming stuff");
			
			while(true && !RPGMain.speler.checkDood()){
				//combat
				if(mobs != null && hasAliveEnemies()){
					try{
						logger.info("Entering combat at " + position[0] + "," + position[1] + "," + currentLevel);
						fleeSuccess = false;
						inCombat = true;
						combat();
						inCombat = false;
						targetIndex = 0;
						logger.info("Exited combat");
						GameFrameCanvas.battlefield.setPlayerTargetIndex(0);
					}catch(Exception e){
						e.printStackTrace();
						logger.debug(e);
					}
				}
				GameFrameCanvas.enemyPortrait.setVisible(false);
				
				if(RPGMain.speler.checkDood()){
					return null;
				}
				logger.debug("Right before if for event");
				if(event != null && !(event.getType().equalsIgnoreCase("entrance") && justEntered)){
					logger.info("Doing event at " + position[0] + "," + position[1]);
					busyEvent = true;
					int[] newPos = event.doAction();
					busyEvent = false;
					action = null;
					if(newPos != null){
						//TODO do what here?
						// put it back on a random place in the map, only possible for the general height map
						/*dungeon.get(0)[generator.nextInt(WIDTH)][generator.nextInt(HEIGHT)].addEvent(event);
						event = null;*/
						logger.debug("Returning a position since newPos from event is not null. " + newPos[0] + "," + newPos[1]);
						return new int[] {newPos[0] - playerPos[0],newPos[1] - playerPos[1]};
					}
				}
				justEntered = false;
				if(action == null || !action.equalsIgnoreCase(keyCode)){
					RPGMain.printText(false, "Choose your next action:\n>");
				}
				action = RPGMain.waitForMessage().toLowerCase();
				if(currentLevel == 0){
					GameFrameCanvas.dungeonMap.setElevation(height, position[0], position[1], 0);
					SoundEngine.setWeatherGain(-5);
					SoundEngine.setAmbientGain(-5);
				}
				//MOVEMENT ACTIONS
				if(action.equalsIgnoreCase("go north")){
					return move("north");
				}
				else if(action.equalsIgnoreCase("go south")){
					return move("south");
				}
				else if(action.equalsIgnoreCase("go east")){
					return move("east");
				}
				else if(action.equalsIgnoreCase("go west")){
					return move("west");
				}
				else if(action.equalsIgnoreCase("collect water")){
					if(distToWater <= 1){
						if(RPGMain.speler.hasItem("Flask")){
							Consumable flask = (Consumable)RPGMain.speler.getInventoryItem("Flask");
							flask.addQuantity(100);
							RPGMain.printText(true, "Your water flask is filled to the brim.");
						}
						else{
							RPGMain.printText(true, "You don't have anything to keep the water in.");
						}
					}
					else{
						RPGMain.printText(true, "There is no water available here.");
					}
				}
				else if(action.equalsIgnoreCase("drink")){
					if(distToWater < 2){
						RPGMain.printText(true, "The water seems safe, and you drink until your thirst has lessened.");
						RPGMain.speler.addThirst(-100);
						Global.soundEngine.playSound("Sounds/Effects/drink_water_fountain.wav", "forest", 0, 0, 0, true);
						try{
							Global.pauseProg(1000);
						} catch(InterruptedException e){
							e.printStackTrace();
							logger.error(e);
						}
					}
					else{
						RPGMain.printText(true, "There is no water available here.");
					}
				}
				else if(action.equalsIgnoreCase("climb tree")){
					if(category.equalsIgnoreCase("forest") && currentLevel == 0 && !terrainType.equalsIgnoreCase("water")){
						RPGMain.printText(true, "You climb the nearest tree, and try to have a better look at your surroundings.");
						try{
							Global.pauseProg(1000);
						} catch(InterruptedException e){
						}
						GameFrameCanvas.dungeonMap.setElevation(height + 2, position[0], position[1], 0);
						
						try{
							//TODO smooth sound transition
							SoundEngine.setAmbientGain(-25);
							SoundEngine.setWeatherGain(6);
							Global.soundEngine.playSound("Sounds/Weather/heavy_wind.mp3", "weather", 5, 0, 6, false);
						} catch(Exception e){
							e.printStackTrace();
							logger.error(e);
						}
						
						//TODO let player slip and fall due to rain, wind, attack by bird,..
					}
					else{
						RPGMain.printText(true, "There are no trees to climb here.");
					}
				}
				else if(action.equalsIgnoreCase("look") || action.equalsIgnoreCase("look around")){
					if(herb != null){
						while(true){
							RPGMain.printText(false, "In the undergrow you see a small batch of " + herb.getRawName() + ". Take it? [y/n]\n>");
							String choice = RPGMain.waitForMessage().toLowerCase();
							
							if(choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")){
								RPGMain.printText(true, "You gather the " + herb.getRawName() + ".");
								RPGMain.speler.addInventoryItem(herb);
								herb = null;
								GameFrameCanvas.dungeonMap.removeHerb(new int[]{position[0],position[1],currentLevel});
								break;
							}
							else if(choice.equalsIgnoreCase("n") || choice.equalsIgnoreCase("no")){
								break;
							}
							else{
								RPGMain.printText(true, "Not a valid option.");
							}
						}
					}
				}
				else if(action.equalsIgnoreCase(keyCode)){
					return direction;
				}
				else if(action.equalsIgnoreCase("heightSpeedInfluence false")){
					GameFrameCanvas.dungeonMap.setHeightSpeedInfluence(false);
				}
				else if(action.equalsIgnoreCase("heightSpeedInfluence true")){
					GameFrameCanvas.dungeonMap.setHeightSpeedInfluence(true);
				}
			}
			return null;
		}
		
		public int[] move(String direction) throws InterruptedException{

			
			ActionListener actionListener = new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					if(Math.random() < 0.3 && currentLevel == 0){
						logger.debug("Displaying dungeon string. dungeonStringProgression: " + dungeonStringProgression + ". DayLength: " + dungeonStringsDay.length);
						
						try{
							if(WeatherSimulator.getSolarIntensity(positie[0], positie[1]) > 0)
								RPGMain.printText(true, dungeonStringsDay[dungeonStringProgression%dungeonStringsDay.length], "darkblue");
							else
								RPGMain.printText(true, dungeonStringsNight[dungeonStringProgression%dungeonStringsNight.length], "darkblue");
							dungeonStringProgression++;
						} catch(Exception e){
							e.printStackTrace();
							logger.error(e);
						}
					}
				}
			};
			
			//TODO set timer delay
			Timer timer = new Timer(0, actionListener);
			timer.setRepeats(false);
			timer.start();
			
			int[] dir = new int[2];
			if(direction.equalsIgnoreCase("east")){
				dir[0] = 1;
				dir[1] = 0;
			}
			else if(direction.equalsIgnoreCase("west")){
				dir[0] = -1;
				dir[1] = 0;
			}
			else if(direction.equalsIgnoreCase("north")){
				dir[0] = 0;
				dir[1] = -1;
			}
			else if(direction.equalsIgnoreCase("south")){
				dir[0] = 0;
				dir[1] = 1;
			}
			
			//TODO
			//Global.soundEngine.chooseAndPlaySound(WeatherSimulator.getWeather(positie[0], positie[1]), getLocationType(), WeatherSimulator.getSolarIntensity(positie[0], positie[1]), true, new String[]{"effects","footsteps"}, 0, 0);
			//GameFrameCanvas.dungeonMap.setPlayerMovement(dir);
			logger.debug("Returning dir: " + dir[0] + "," + dir[1]);
			return dir;
		}
		
		public double getHeightModifier(int[] coords){
			double modifier = 1;
			if(currentLevel == 0){
				try{
					// north west
					if(coords[0] < GameFrameCanvas.battlefield.getWidth()/3 && coords[1] < GameFrameCanvas.battlefield.getHeight()/3){
						modifier = dungeon.get(currentLevel)[playerPos[0]-1][playerPos[1]-1].getHeight();
					}
					// west
					else if(coords[0] < GameFrameCanvas.battlefield.getWidth()/3 && coords[1] < 2*GameFrameCanvas.battlefield.getHeight()/3){
						modifier = dungeon.get(currentLevel)[playerPos[0]-1][playerPos[1]].getHeight();
					}
					// south west
					else if(coords[0] < GameFrameCanvas.battlefield.getWidth()/3 && coords[1] > 2*GameFrameCanvas.battlefield.getHeight()/3){
						modifier = dungeon.get(currentLevel)[playerPos[0]-1][playerPos[1]+1].getHeight();
					}
					// north east
					else if(coords[0] > 2*GameFrameCanvas.battlefield.getWidth()/3 && coords[1] < GameFrameCanvas.battlefield.getHeight()/3){
						modifier = dungeon.get(currentLevel)[playerPos[0]+1][playerPos[1]-1].getHeight();
					}
					// east
					else if(coords[0] > 2*GameFrameCanvas.battlefield.getWidth()/3 && coords[1] < 2*GameFrameCanvas.battlefield.getHeight()/3){
						modifier = dungeon.get(currentLevel)[playerPos[0]+1][playerPos[1]].getHeight();
					}
					// south east
					else if(coords[0] > 2*GameFrameCanvas.battlefield.getWidth()/3 && coords[1] > 2*GameFrameCanvas.battlefield.getHeight()/3){
						modifier = dungeon.get(currentLevel)[playerPos[0]+1][playerPos[1]+1].getHeight();
					}
					// north
					else if(coords[1] < GameFrameCanvas.battlefield.getHeight()/3 ){
						modifier = dungeon.get(currentLevel)[playerPos[0]][playerPos[1]-1].getHeight();
					}
					// south
					else if(coords[1] > 2*GameFrameCanvas.battlefield.getHeight()/3){
						modifier = dungeon.get(currentLevel)[playerPos[0]][playerPos[1]+1].getHeight();
					}
					else
						modifier = dungeon.get(currentLevel)[playerPos[0]][playerPos[1]].getHeight();
				}catch(ArrayIndexOutOfBoundsException e){
				}
			}
			return modifier;
		}
		
		public void combat() throws InterruptedException{
			
			//PREPARATION
			GameFrameCanvas.enemyPortrait.setVisible(true);
			logger.info("ScrollPane Height:" + GameFrameCanvas.scrollPane.getHeight() + " Battlefield Height:" + GameFrameCanvas.battlefield.getHeight());
			
			GameFrameCanvas.battlefield.setVisible(true);
			//GameFrameCanvas.textPane.setSize(new Dimension(GameFrameCanvas.textPane.getWidth(), 120));
			GameFrameCanvas.scrollPane.setPreferredSize(new Dimension(GameFrameCanvas.scrollPane.getWidth(), GameFrameCanvas.scrollPane.getHeight() - GameFrameCanvas.battlefield.getHeight() - 20));
			GameFrameCanvas.layout.putConstraint(GameFrameCanvas.n, GameFrameCanvas.scrollPane, 20, GameFrameCanvas.s, GameFrameCanvas.battlefield);
			
			GameFrameCanvas.textPane.setCaretPosition(GameFrameCanvas.textPane.getDocument().getLength());
			//GameFrameCanvas.scrollPane.setBounds(20, GameFrameCanvas.battlefield.getY() + GameFrameCanvas.battlefield.getHeight() + 20, GameFrameCanvas.scrollPane.getWidth(), 130);
			
			//GameFrameCanvas.moveScrollPane(GameFrameCanvas.battlefield, 1000);
			
			GameFrameCanvas.scrollPane.validate();
			
			logger.info("ScrollPane Height:" + GameFrameCanvas.scrollPane.getHeight());
			mobs.get(targetIndex).updateHPBar();
			//enemies are initialized with general versions of gear, 
			//this makes sure every enemy you'll fight has its own instance of the equipment it's carrying
			for(Enemy mob: mobs){
				logger.debug("Mob ID: " + mob.getID());
				if(!mob.getUniqueGearDone())
					mob.uniqueGear();
			}
			int fieldWidth = GameFrameCanvas.battlefield.getWidth();
			int fieldHeight = GameFrameCanvas.battlefield.getHeight();
			//randomize number, position and quality of cover positions
			int numberCovers = generator.nextInt(3) + 1;
			for(int j=0;j<numberCovers;j++){
				// coverSizes:
				// posX posY row 0
				// sizX sizY row 1
				double[][] coverSizes = new double[2][2];
				double coverQuality;
				coverSizes[0][0] = generator.nextInt(fieldWidth);
				coverSizes[0][1] = generator.nextInt(fieldHeight);
				coverSizes[1][0] = Math.max(0.2,generator.nextDouble());
				coverSizes[1][1] = Math.max(0.2,generator.nextDouble());
				coverQuality = Math.min(1, generator.nextDouble()+0.2);
				covers.put(coverSizes, coverQuality);
			}
			// decide player position
			playerCoord = new int[2];
			enemyCoords.clear();
			playerCoord[0] = generator.nextInt(fieldWidth);
			playerCoord[1] = generator.nextInt(fieldHeight);
			while(checkObstruction(playerCoord)){
				playerCoord[0] = generator.nextInt(fieldWidth);
				playerCoord[1] = generator.nextInt(fieldHeight);
				//randomize player entry position in outer box of battlefield, according to what direction the player entered the field
				if(action.equalsIgnoreCase("n")){playerCoord[1]%=fieldHeight/3;playerCoord[1]+=2*fieldHeight/3;}
				else if(action.equalsIgnoreCase("s")){playerCoord[1]%=fieldHeight/3;}
				else if(action.equalsIgnoreCase("e")){playerCoord[0]%=fieldWidth/3;}
				else if(action.equalsIgnoreCase("w")){playerCoord[0]%=fieldWidth/3;playerCoord[0]+=2*fieldWidth/3;}
			}
			//System.out.println("Amount of mobs: " + mobs.size());
			
			//decide enemy positions
			for(int j=0;j<mobs.size();j++){
				// enemy can stand anywhere
				int[] enemyCoord = new int[2];
				enemyCoord[0] = generator.nextInt(fieldWidth);
				enemyCoord[1] = generator.nextInt(fieldHeight);
				while(checkObstruction(enemyCoord)){
					enemyCoord[0] = generator.nextInt(fieldWidth);
					enemyCoord[1] = generator.nextInt(fieldHeight);
				}
				enemyCoords.add(enemyCoord);
			}
			//public Equipment(Integer id,String name,Integer strength,Integer cost,Integer durability,Integer type,Double weight){
			Equipment stone = new Equipment(-1,"Stone",5,0,10,12,0.0);
			// put items on the floor
			itemsOnFloor = new HashMap<int[],Item>();
			int number = (int)(5*Math.random());
			for(int j=0;j<number;j++){
				//TODO different stuff for on the floor
				double random = Math.random();
				stone.setStrength((int)(RPGMain.speler.getStrength()/2*random));
				stone.setWeight(3*random);
				
				int[] pos = new int[2];
				pos[0] = generator.nextInt(fieldWidth);
				pos[1] = generator.nextInt(fieldHeight);
				while(checkObstruction(pos)){
					pos[0] = generator.nextInt(fieldWidth);
					pos[1] = generator.nextInt(fieldHeight);
				}
				
				itemsOnFloor.put(pos, new Equipment(stone));
			}
			
			//show the battlefield
			GameFrameCanvas.battlefield.updateBattlefield(covers, playerCoord, enemyCoords);
			
			//decide the rotation who gets to strike first
			turnMap = new TreeMap<Double,Integer>();
			for(int j=0;j<enemyCoords.size();j++){
				turnMap.put(Math.random(), j);
			}
			double playerThrow = Math.random();
			turnMap.put(playerThrow, -1);
			/*for(int i:turnMap.values()){
				System.out.println(i);
			}*/
			if(playerThrow > turnMap.firstKey()){
				RPGMain.printText(true, mobs.get(turnMap.get(turnMap.firstKey())).getName() + " has the first turn!");
			}
			
			/*
			 * ACTUAL COMBAT LOOP
			 */
			//as long as player still lives, there are alive enemies, and player hasn't successfully fled
			while(!RPGMain.speler.checkDood() && hasAliveEnemies() && !fleeSuccess){
				try{
					synchronized(turnMap){
						Set<Double> keys = turnMap.keySet();
						//sets hasMoved in map to false
						//GameFrameCanvas.battlefield.newTurn();
						//iterate over order of attack
						Iterator<Double> turnOrder = keys.iterator();
						while(turnOrder.hasNext()){
							int index = turnMap.get(turnOrder.next());
							//player has a negative index, enemies a positive
							if(index < 0){
								playerTurn();
								// return when fled successfully or dead
								if(fleeSuccess || RPGMain.speler.checkDood()){
									break;
								}
							}
							else{
								Global.beanShell.set("mobIndex", index);
								enemyTurn(index);
							}
							GameFrameCanvas.battlefield.updateBattlefield(covers, playerCoord, enemyCoords);
						}
					}
				}catch(Exception e){
					e.printStackTrace();
					logger.debug(e);
					continue;
				}
				logger.info("End of full turn.");
				
				//decrease all ability cooldown times by one
				RPGMain.speler.decrAllAbilitiesCooldown();
				for(Enemy mob: mobs){
					mob.decrAllAbilitiesCooldown();
				}
			}//end of combat while loop
			
			//remove all dead enemies from the map so they don't show up
			for(Enemy mob:mobs){
				if(mob.checkDood())
					GameFrameCanvas.dungeonMap.removeEnemy(position,currentLevel);
			}
			//go back to showing the actual map
			GameFrameCanvas.battlefield.updateBattlefield(null,null,null);
			GameFrameCanvas.battlefield.setVisible(false);
			GameFrameCanvas.scrollPane.setPreferredSize(new Dimension(GameFrameCanvas.scrollPane.getWidth(), 600));
			GameFrameCanvas.layout.putConstraint(GameFrameCanvas.n, GameFrameCanvas.scrollPane, 20, GameFrameCanvas.s, GameFrameCanvas.playerPortrait);
			
			GameFrameCanvas.textPane.setCaretPosition(GameFrameCanvas.textPane.getDocument().getLength());
			
			GameFrameCanvas.scrollPane.validate();
			
		}
		
		
		public boolean checkObstruction(int[] newPos){
			Iterator<double[][]> coverPos = covers.keySet().iterator();
			while(coverPos.hasNext()){
				double[][] pos = coverPos.next();
				double cosTheta = (double)(newPos[0]-pos[0][0])/Math.sqrt(Math.pow(newPos[0] - pos[0][0], 2) + Math.pow(newPos[1] - pos[0][1], 2));
				double radius = 1.0/Math.sqrt(Math.pow(cosTheta/(Global.coverSize/2*pos[1][0]), 2) + (1-cosTheta*cosTheta)/Math.pow((Global.coverSize/2*pos[1][1]),2));
				if(new Point(newPos[0],newPos[1]).distance(pos[0][0],pos[0][1]) <= (radius+Global.playerSize/2)){
					return true;
				}
			}
			return false;
		}
		
		
		public void playerTurn() throws InterruptedException{
			logger.info("Player turn");
			//activate all buffs from previous turns
			RPGMain.speler.tickBuffs();
			
			if(!RPGMain.speler.checkDood()){
				// reinitialize parameters
				actionsCompleted = 0;
				
				GameFrameCanvas.battlefield.setHasMovedMax(false);
				logger.debug("hasMovedMax: " + GameFrameCanvas.battlefield.getHasMovedMax());
				// run a checker to see if player has moved
				new Checker();
				// run the timer to limit turn time
				//timer = new Timer();
				//timer.schedule(new CountDown("Turn ended."), (long)20000);
				
				while(actionsCompleted < 2){
					
					// stand back up if you were knocked down, counts as 1 action
					if(RPGMain.speler.getKnockedDown()){
						RPGMain.speler.setKnockedDown(false);
						actionsCompleted++;
						RPGMain.printText(true, "You stand back up.");
						try{
							Global.pauseProg(2000);
						} catch(InterruptedException e){
						}
					}
					
					RPGMain.printText(false, "Choose your next action:\n>");
					String message = RPGMain.waitForMessage().toLowerCase();
					if(!message.equalsIgnoreCase("enemyAdded\n") && !message.equalsIgnoreCase("turnDone\n")){
						actionInterpreter(message,RPGMain.speler,mobs.get(targetIndex),targetIndex);
						//update battlefield, possible it changed through f.e. 'push'
						GameFrameCanvas.battlefield.updateBattlefield(covers, playerCoord, enemyCoords);
						if(fleeSuccess){
							return;
						}
					}
					// player loses an action by getting knocked down
					if(RPGMain.speler.getKnockedDown()){
						actionsCompleted++;
					}
					
					casualtyCheck(mobs.get(targetIndex));
					
					if(!hasAliveEnemies()){
						actionsCompleted = 2;
					}
				}
				GameFrameCanvas.battlefield.setHasMovedMax(true);
			}
		}
		
		public void actionInterpreter(String action, Wezen actor, Wezen target, int mobIndex){
			
			////System.out.println("Actor: " + actor.getName() + " action: " + action + " Player coords in actionInterpret: " + playerCoord[0] + ", " + playerCoord[1]);
			////System.out.println("Enemy coords: " + enemyCoords.get(mobIndex)[0] + ", " + enemyCoords.get(mobIndex)[1]);
			
			// get all the actions the actor can do
			HashMap<Ability,Integer> combatActions = actor.getAbilities();
			// split the given action into all separate components
			String[] actionContent = action.split(" ");
			
			// see if a stance was specified
			try{
				String stance = "defensive";
				if(actionContent[actionContent.length-1].equalsIgnoreCase("o")){
					stance = "offensive";
				}
				////System.out.println(actor.getName() + "Stance: " + stance);
				actor.setStance(stance);
			} catch(ArrayIndexOutOfBoundsException exc){
				actor.setStance("defensive");
			}
			
			// calculate heightModifier
			// > 1 when actor is standing higher than target & vice versa
			if(actor.getClass().equals(Avatar.class))
				heightModifier = 1 + (getHeightModifier(playerCoord)-getHeightModifier(enemyCoords.get(mobIndex)))/3;
			else
				heightModifier = 1 + (getHeightModifier(enemyCoords.get(mobIndex))-getHeightModifier(playerCoord))/3;
			
			////System.out.println("heightMod: " + heightModifier);
			
			/*if(heightModifier > 1){
				//System.out.println(actor.getName() + " attacks from a strategically good location!");
			}*/
			
			// check if actor can do what he wrote
			boolean success = false;
			for(Ability a:combatActions.keySet()){
				if(a.getCommand().equalsIgnoreCase(actionContent[0])){
					double[] coefficients = new double[]{heightModifier,chargeModifier};
					if(actor instanceof Avatar){
						try{
							success = a.activate(actor, target, playerCoord, enemyCoords.get(mobIndex), actionContent[1],covers, coefficients);
						} catch(ArrayIndexOutOfBoundsException e){
						}
						catch(Exception e){
							e.printStackTrace();
							logger.debug(e);
							success = a.activate(actor, target, playerCoord, enemyCoords.get(mobIndex), null,covers,coefficients);
						}
					}
					else{
						success = a.activate(actor, target, enemyCoords.get(mobIndex), playerCoord, actionContent[1],covers,coefficients);
						try {
							Global.pauseProg(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					// coefficients can be changed through a script, but since the values in the array don't point to the elements it's made of,
					// the individual coeff don't get changed
					heightModifier = coefficients[0];
					chargeModifier = coefficients[1];
					
					// see if action was successful. If so, it counts as a completed action
					if(success){
						actionsCompleted++;
					}
					break;
				}
			}
			
			if(actionContent[0].equalsIgnoreCase("target")){
				if(actionContent[1] == null){
					RPGMain.printText(true, "You must specify a target.");
					return;
				}
				try{
					int i = Integer.parseInt(actionContent[1]) - 1;
					try{
						if(lineOfSight(playerCoord,enemyCoords.get(i),covers)){
							targetIndex = i;
							GameFrameCanvas.enemyPortrait.setVisible(true);
							mobs.get(targetIndex).updateHPBar();
							GameFrameCanvas.battlefield.setPlayerTargetIndex(targetIndex);
						}
						else{
							RPGMain.printText(true, "That enemy is not visible.");
						}
					}catch(IndexOutOfBoundsException e){
						RPGMain.printText(true, "Invalid target.");
					}
				} catch(NumberFormatException e){
					RPGMain.printText(true, "\"" + actionContent[1] + "\"" + " is not a valid target. Write a number.");
				}
			}
			
			// see if he tries to flee
			//TODO can enemy flee?
			else if(actionContent[0].equalsIgnoreCase("flee")){
				double r = generator.nextDouble();
				boolean visible = false;
				for(int j=0;j<enemyCoords.size();j++){
					if(lineOfSight(playerCoord,enemyCoords.get(j),covers)){
						visible = true;
						break;
					}
				}
				if(!visible || r < 0.6){
					RPGMain.printText(true, "You have fled succesfully.");
					fleeSuccess = true;
				}
				else{
					RPGMain.printText(true, "The enemy saw your attempt to flee and was able to intercept you.");
				}
			}
		}
		
		public void enemyTurn(int index){
			
			mobs.get(index).tickBuffs();
			
			if(!mobs.get(index).checkDood()){
				try {
					Global.beanShell.set("enemyCoords", enemyCoords);
					Global.beanShell.set("index", index);
					Global.beanShell.set("mob", mobs.get(index));
					Global.beanShell.set("logger", logger);
					Global.beanShell.set("playerCoord", playerCoord);
					Global.beanShell.set("covers", covers);
					Global.beanShell.set("hostA", new HostileArea());
					Global.beanShell.set("speler", RPGMain.speler);
					Global.beanShell.set("abilities", Data.abilities);
					Global.beanShell.set("battlefield", GameFrameCanvas.battlefield);
					Global.beanShell.set("dungeonRoom", this);
					Global.beanShell.source("Data/Scripts/AI.bsh");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (EvalError e) {
					e.printStackTrace();
				}
				try {
					casualtyCheck(mobs.get(index));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		//TODO better idea: research collision detector, and do it with sprite for cover and invisible bullet-like sprite
		// other idea: use light cones. From the position of the player you can make 2 straight lines connecting the sides of the cover perpendicular to the player,
		// anything in between those 2 lines is not visible, do it in cover reference frame so no worry with changing offset
		
		public int[] getClosestCover(int[] pos){
			int[] closestCoverPos = new int[2];
			Point p = new Point(pos[0],pos[1]);
			Iterator<double[][]> e = covers.keySet().iterator();
			while(e.hasNext()){
				double[][] coverPos = e.next();
				if(p.distance(coverPos[0][0],coverPos[0][1]) < p.distance(closestCoverPos[0],closestCoverPos[1])){
					closestCoverPos[0] = (int)coverPos[0][0];
					closestCoverPos[1] = (int)coverPos[0][1];
				}
			}
			return closestCoverPos;
		}
		public void casualtyCheck(Enemy mob) throws InterruptedException{
			
			if(mob.checkDood()){
				//casualty check
				//experience = (int)(Math.pow(mob.getLevel()/RPGMain.speler.getLevel(),2)*5 + (RPGMain.speler.getLevel()-1)*4);
				try{
					RPGMain.printText(true,Global.rwtext.getContent(new File("Data/Bestiary.xml"), new String[]{"Enemy,id:"+  mob.getID(),"death"}));
				} catch(NullPointerException e){
					
				}
				RPGMain.speler.addGoud(mob.getGoud());
				//RPGMain.speler.addExperience(experience);
				//if(Global.online) Global.message("REW: " + experience + " " + mob.getGoud());
				Global.pauseProg();
				RPGMain.speler.checkLevelUp();
				for(int j=0;j<RPGMain.speler.getQuestlogSize();j++){
					try{
						System.out.println("Enemy name and ID: " + mob.getName() + "," + mob.getID());
						RPGMain.speler.getQuest(j).checkProgress("Enemy",mob.getID());//check progress op kill
						for(int k=0;k<mob.getInventorySize();k++){
							RPGMain.speler.getQuest(k).checkProgress(mob.getInventoryItem(k).getClass().getName().split(".")[1], mob.getInventoryItem(k).getID());//check progress op quest item loot
						}
					}
					catch(NullPointerException np){
						//np.printStackTrace();
					} catch(IndexOutOfBoundsException e){
					}
				}
				LinkedHashMap<Item,Integer> loot = new LinkedHashMap<Item,Integer>();
				//get all equiped gear
				for(int j=2;j<10;j++){
					if(!mob.getEquipped(j).getName().equalsIgnoreCase("nothing")){
						loot.put(mob.getEquipped(j),1);
					}
				}
				//get all in inventory
				for(int j=0;j<mob.getInventorySize();j++){
					if(mob.getInventoryItem(j) != null){
						loot.put(mob.getInventoryItem(j),mob.getItemNumber(mob.getInventoryItem(j).getName()));
					}
				}
				while(loot.size() > 0){
					int index = 1;
					//print all loot
					for(Item i: loot.keySet()){
						RPGMain.printText(false, index + ": " + i.getName());
						
						if(loot.get(i) > 1){
							RPGMain.printText(true, " (" + loot.get(i) + ")");
						}
						else{
							RPGMain.printText(true, "");
						}
						index++;
					}
					RPGMain.printText(false, (loot.size()+1) + ": Cancel" + "\nTake loot?\n>");
					//interpret answer
					try{
						String action = RPGMain.waitForMessage().trim();
						if(action.startsWith("info")){
							int k = Character.getNumericValue(action.charAt(5));
							int j = 1;
							for(Item i: loot.keySet()){
								if(j == k){
									i.showInfo();
									break;
								}
								j++;
							}
							try{
								Global.pauseProg();
							}catch(InterruptedException exc){
								exc.printStackTrace();
							}
						}
						else{
							int choice = Integer.parseInt(action);
							if(choice == (loot.size()+1)){
								break;
							}
							else{
								try{
									int k = 1;
									Item item = null;
									int amount = 1;
									
									for(Item i: loot.keySet()){
										if(choice == k){
											item = i;
											break;
										}
										k++;
									}
									if(loot.get(item) > 1){
										while(true){
											RPGMain.printText(false, "How many?\n>");
											try{
												int i = Integer.parseInt(RPGMain.waitForMessage());
												if(i <= loot.get(item)){
													amount = i;
													break;
												}
												else{
													RPGMain.printText(true, "There aren't that many.");
												}
											}catch(NumberFormatException e){
												RPGMain.printText(true, "Not a valid option.");
												continue;
											}
										}
									}
									
									RPGMain.speler.addInventoryItem(item, amount);
									RPGMain.printText(true, "You recieved " + amount + item.getName() + ".");
									// all equipment an enemy has, he's wearing it, no equipment in inventory
									if(item.getClass().equals(Equipment.class)){
										mob.removeEquippedItem((Equipment)item);
									}
									else{
										mob.delInventoryItem(item,amount);
									}
									loot.put(item, loot.get(item)-amount);
									if(loot.get(item) <= 0){
										loot.remove(item);
									}
									
								}catch(IndexOutOfBoundsException exc){
									RPGMain.printText(true, "Not a valid option.");
									continue;
								}
							}
						}
					}catch(NumberFormatException exc){
						RPGMain.printText(true, "Not a valid option.");
						continue;
					}catch(IndexOutOfBoundsException exc){
						RPGMain.printText(true, "Not a valid option.");
						continue;
					}
				}
				// add log info about enemy
				try{
					String[] logbookPath = {"Enemy,id:" + mob.getID(),"path"};
					String nodePath = Global.rwtext.getContent(new File("Data/Bestiary.xml"), logbookPath);
					logbookPath[1] = "content";
					String[] content = Global.rwtext.getContent(new File("Data/Bestiary.xml"), logbookPath).split(";");
					Logbook.addContent(nodePath, Integer.parseInt(content[0]), content[1]);
				} catch(NumberFormatException e){
					logger.debug("No bestiary file for " + mob.getName());
				} catch(ArrayIndexOutOfBoundsException e){
					logger.debug("No bestiary content for " + mob.getName());
				} catch(NullPointerException e){
					logger.debug("No bestiary content for " + mob.getName());
				} catch(Exception e){
					e.printStackTrace();
					logger.error(e);
				}
			}
		}
		
		/*GETTERS*/
		public String getType(){
			return type;
		}
		public DungeonEvent getEvent(){
			return event;
		}
		public Enemy getMob(int index){
			return mobs.get(index);
		}
		public int getHeight(){
			return height;
		}
		public int[] getPosition(){
			return position;
		}
		
		public boolean hasMob(){
			if(mobs != null) return true;
			return false;
		}
		public Herb getHerb(){
			return herb;
		}
		
		
		//checks if the player moved in combat
		class Checker extends Thread implements Serializable{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			public Checker(){
				start();
			}
			public void run(){
				while(actionsCompleted < 2){
					try{
						sleep(100);
					} catch(InterruptedException exc){
						exc.printStackTrace();
					}
					int[] newCoords = GameFrameCanvas.battlefield.getPlayerCoord();
					if(!(newCoords[0] == playerCoord[0] && newCoords[1] == playerCoord[1])){
						System.out.println("Found new coords: " + newCoords[0] + "," + newCoords[1] + ". Old coords:" + playerCoord[0] + "," + playerCoord[1]);
						ArrayList<int[]> path = Global.calculatePath(playerCoord, newCoords, (int)(RPGMain.speler.getMovement()/Battlefield.sizeInMeter*GameFrameCanvas.battlefield.getWidth()), covers,false);
						for(int j=0;j<path.size();j++){
							GameFrameCanvas.battlefield.updateBattlefield(covers, path.get(j),enemyCoords);
							try{
								Thread.sleep(1000/24);
							} catch(InterruptedException exc){
							}
						}
						playerCoord[0] = newCoords[0];
						playerCoord[1] = newCoords[1];
						GameFrameCanvas.battlefield.updateBattlefield(covers,playerCoord,enemyCoords);
						actionsCompleted++;
					}
				}
				RPGMain.recieveMessage("turnDone\n");
			}
		}
		
		public class DungeonEvent implements Serializable {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			private String type,description,logbookPath;
			private int id,goToLevel;
			private NPC npc;
			private boolean active = true;
			
			public DungeonEvent(String eventType,String description){
				String[] content = eventType.split(":");
				type = content[0];
				try{
					id = Integer.parseInt(content[1]);
				}catch(NumberFormatException e){
					e.printStackTrace();
					logger.debug(e);
				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace();
					logger.debug(e);
				}
				this.description = description;
			}
			public DungeonEvent(String eventType, String description, int level){
				type = eventType;
				this.description = description;
				goToLevel = level;
			}
			public DungeonEvent(String eventType,String description,String logbookPath){
				String[] content = eventType.split(":");
				type = content[0];
				try{
					id = Integer.parseInt(content[1]);
				}catch(NumberFormatException e){
					e.printStackTrace();
					logger.debug(e);
				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace();
					logger.debug(e);
				}
				this.description = description;
				this.logbookPath = logbookPath;
			}
			public DungeonEvent(int npcID){
				npc = Data.NPCs.get(npcID);
				type = "NPC";
			}
			public DungeonEvent(String type, int id){
				this.type = type;
				this.id = id;
			}
			public int[] doAction(){
				if(npc != null){
					logger.debug("present NPC:" + npc.getName());
					npc.talk();
				}
				else if(active){
					// do stuff
					// get lost
					// pitfall
					// bad weather
					//
					RPGMain.printText(true, description);
					try {
						Global.pauseProg();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if(type.equalsIgnoreCase("Surprise Enemy")){
						addEnemy(id);
						RPGMain.recieveMessage("addedEnemy");
						active = false;
					}
					else if(type.equalsIgnoreCase("Surprise QNPC")){
						RPGMain.printText(true, description);
						try{
							Global.pauseProg();
						} catch(InterruptedException e){
							e.printStackTrace();
							logger.error(e);
						}
						Data.NPCs.get(id).talk();
					}
					else if(type.equalsIgnoreCase("Entrance")){
						RPGMain.printText(false, "Go through? [y/n]\n>");
						String enter = "n";
						try {
							enter = RPGMain.waitForMessage().toLowerCase();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if(enter.startsWith("y")){
							currentLevel = goToLevel;
							justEntered = true;
							GameFrameCanvas.dungeonMap.resetExtraPlayerMovement();
							return new int[]{playerPos[0],playerPos[1]};
						}
					}
					else if(type.equalsIgnoreCase("Surprise Equipment")){
						RPGMain.printText(true, "You find " + Data.equipment.get(id).getName() + ".");
						//RPGMain.printText(true, "\"" +  Data.equipment.get(id).getDescription() + "\".");
						RPGMain.printText(false, "Take it? [y/n]\n>");
						String choice;
						try {
							choice = RPGMain.waitForMessage().toLowerCase();
							if(choice.startsWith("y")){
								RPGMain.speler.addInventoryItem(Data.equipment.get(id));
								active = false;
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					else if(type.equalsIgnoreCase("Surprise Item")){
						RPGMain.printText(true, "You find a " + Data.items.get(id).getName() + ".");
						RPGMain.printText(true, "\"" +  Data.items.get(id).getDescription() + "\".");
						while(true){
							RPGMain.printText(false, "Take it? [y/n]\n>");
							String choice;
							try {
								choice = RPGMain.waitForMessage().toLowerCase();
								logger.debug(choice);
								if(choice.startsWith("y")){
									RPGMain.speler.addInventoryItem(Data.items.get(id));
									active = false;
									break;
								}
								else if(choice.startsWith("n")){
									break;
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
								logger.error(e);
							} catch(Exception e){
								e.printStackTrace();
								logger.error(e);
							}
						}
					}
					else if(type.equalsIgnoreCase("Surprise Potion")){
						RPGMain.printText(true, "You find " + Data.potions.get(id).getName() + ".");
						RPGMain.printText(false, "Take it? [y/n]\n>");
						String choice;
						try {
							choice = RPGMain.waitForMessage().toLowerCase();
							if(choice.startsWith("y")){
								RPGMain.speler.addInventoryItem(Data.potions.get(id));
								active = false;
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					else if(type.equalsIgnoreCase("Surprise Clothing")){
						RPGMain.printText(true, "You find " + Data.clothes.get(id).getName() + ".");
						RPGMain.printText(false, "Take it? [y/n]\n>");
						String choice;
						try {
							choice = RPGMain.waitForMessage().toLowerCase();
							if(choice.startsWith("y")){
								RPGMain.speler.addInventoryItem(Data.clothes.get(id));
								active = false;
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					else if(type.equalsIgnoreCase("Surprise Consumable")){
						RPGMain.printText(true, "You find " + Data.consumables.get(id).getName() + ".");
						RPGMain.printText(false, "Take it? [y/n]\n>");
						String choice;
						try {
							choice = RPGMain.waitForMessage().toLowerCase();
							if(choice.startsWith("y")){
								RPGMain.speler.addInventoryItem(Data.consumables.get(id));
								active = false;
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					else if(type.equalsIgnoreCase("Surprise Quest")){
						RPGMain.speler.addQuest(id);
					}
					else if(type.equalsIgnoreCase("Artifact")){
						RPGMain.printText(true, "You uncover an ancient artifact. It looks like a " + Data.artifacts.get(id).getName() + ".","redbold");
						try{
							Global.pauseProg();
						} catch(InterruptedException e){
							e.printStackTrace();
							logger.error(e);
						}
						
						Data.artifacts.get(id).activate(true);
						
						if(Data.artifacts.get(id).getCanCarry()){
							active = false;
						}
					}
					else if(type.equalsIgnoreCase("Trap")){
						//TODO
					}
					else if(type.equalsIgnoreCase("Lost")){
						int[] newPos = new int[2];
						newPos[0] = generator.nextInt(WIDTH);
						newPos[1] = generator.nextInt(HEIGHT);
						Logbook.addContent(logbookPath, 1, description);
						return newPos;
					}
					Logbook.addContent(logbookPath, 1, description);
					if(!active){
						GameFrameCanvas.dungeonMap.removeEvent(position, currentLevel);
					}
				}
				logger.debug("Returning null");
				return null;
			}
			/* GETTERS */
			public String getType(){
				return type;
			}
			public String getDescription(){
				return description;
			}
			public NPC getNPC(){
				return npc;
			}
			public String getLogbookPath(){
				return logbookPath;
			}
		}// end of DungeonEvent class
		
	}// end of DungeonRoom class
	
	class HerbPlacer extends Thread{
		public HerbPlacer(){
			start();
		}
		
		public void run(){
			while(RPGMain.speler != null && RPGMain.speler.getCurrentPosition()[0] == positie[0] && RPGMain.speler.getCurrentPosition()[1] == positie[1] && !RPGMain.speler.checkDood()){
				for(int j=0;j<herbPos.size();j++){
					if(Math.random() < 0.5){
						int[] pos = herbPos.get(j);
						if(dungeon.get(pos[2])[pos[0]][pos[1]].getHerb() == null){
							dungeon.get(pos[2])[pos[0]][pos[1]].addHerb(pos[3]);
							GameFrameCanvas.dungeonMap.addHerb(new int[]{pos[0],pos[1],pos[2]});
						}
					}
				}
				
				try{
					sleep(10000);
				} catch(InterruptedException e){
					e.printStackTrace();
					logger.error(e);
				}
			}
		}
	}
	
	ArrayList<int[]> mobPos = new ArrayList<int[]>();
	HashMap<Enemy,int[]> pursuitPos = new HashMap<Enemy,int[]>();
	
	class EnemyMover extends Thread{
		/**
		 * 
		 */
		public EnemyMover(){
			start();
		}
		public void run(){
			Random generator = new Random();
			HashMap<Enemy,Boolean> enemiesMoved = new HashMap<Enemy,Boolean>();
			
			// while player is still in the hostileArea
			while(RPGMain.speler.getCurrentPosition()[0] == positie[0] && RPGMain.speler.getCurrentPosition()[1] == positie[1] && !RPGMain.speler.checkDood()){
				try{
					sleep(5000);
				} catch(InterruptedException e){
					e.printStackTrace();
					logger.error(e);
				}
				mobPos.clear();
				enemiesMoved.clear();
				int i1 = 0;
				int i2 = 0;
				for(int level: dungeon.keySet()){
					for(int j=0;j<WIDTH;j++){
						for(int k=0;k<HEIGHT;k++){
							//not all positions will have a DungeonRoom in the upper/lower layers
							if(dungeon.get(level)[j][k] != null){
								boolean inCombat = false;
								//if an enemy is present at the current player location, means they're in combat
								if(j == playerPos[0] && k == playerPos[1] && level == currentLevel){
									inCombat = true;
								}
								//add all mobs in (j,k,level)
								ArrayList<Enemy> dummy = new ArrayList<Enemy>();
								for(Enemy mob: dungeon.get(level)[j][k].getMobs()){
									dummy.add(mob);
								}
								for(Enemy mob: dummy){
									//mob might get moved to a spot that hasn't been checked yet, and so might move twice. hasMoved avoids this
									boolean hasMoved;
									try{
										hasMoved = enemiesMoved.get(mob);
									}catch(NullPointerException e){
										hasMoved = false;
									}
									if(!mob.checkDood() && !hasMoved){
										//logger.debug("In Main loop of enemymover");
										// mob is allowed to move, isn't pursuing player, and isn't in combat
										if(!mob.getStationarity() && mob.getPursuitIndex() < 0 && !inCombat && Math.random() < 0.3){
											while(true){
												try{
													double distance = Math.sqrt(Math.pow(playerNoiseCentre[0] - j, 2.0) + Math.pow(playerNoiseCentre[1] - k, 2.0));
													i1 = 0;
													i2 = 0;
													if(distance <= playerNoiseCentre[2]){
														//(int)cos theta / cos (pi/4); cos(pi/4) = 1/sqrt(2)
														i1 = (int)((j-playerNoiseCentre[0])/distance*Math.sqrt(2.0));
														//(int)sin theta / sin(pi/4); sin(pi/4) = 1/sqrt(2)
														i2 = (int)((k-playerNoiseCentre[1])/distance*Math.sqrt(2.0));
													}
													else{
														i1 = generator.nextInt(3)-1;
														i2 = generator.nextInt(3)-1;
													}
													
													boolean hasFoundPath = false;
													//mobs will only follow when they are on the main map, so won't enter caves/whatever, and won't go into water
													if(!(i1 == 0 && i2 == 0) && !dungeon.get(level)[j+i1][k+i2].getTerrainType().equalsIgnoreCase("water")){
														if(level == 0){
															ArrayList<int[]> pathDummy = Global.cloneArrayList(playerPath);
															for(int l=0;l<pathDummy.size()-1;l++){
																//check to see if the mob found the player's path
																if(pathDummy.get(l)[0] == (j+i1) && pathDummy.get(l)[1] == (k+i2) && Math.random() < 0.18*mob.getIntelligence()){
																	mob.setPursuitIndex(l);
																	hasFoundPath = true;
																	pursuitPos.put(mob, new int[] {(j+i1),(k+i2),level});
																	logger.info("Enemy found player path!");
																	new PursuitModerator(mob);
																	break;
																}
															}
														}
														//will throw nullpointerexc for mobs in lower/upper layers, or indexoutofbounds on edges
														dungeon.get(level)[j+i1][k+i2].addEnemy(mob, level);
														dungeon.get(level)[j][k].removeEnemy(mob);
														//System.err.println("Moved enemy");
														if(!hasFoundPath){
															mobPos.add(new int[] {j+i1,k+i2,level});
														}
														break;
													}
												} catch(NullPointerException e){
													continue;
												} catch(IndexOutOfBoundsException e){
													continue;
												} catch(Exception e){
													e.printStackTrace();
													logger.debug(e);
													continue;
												}
											}
										}
										//mob is not pursuing, may be dead or in combat
										else if(mob.getPursuitIndex() < 0){
											mobPos.add(new int[] {j,k,level});
										}
										enemiesMoved.put(mob, true);
									}
									// don't add if mob is dead or has already moved
									else{
									}
								}
							}
						}
					}
				}
				//System.out.println("New cycle. mobPos size: " + mobPos.size() + ", pursuitPos size: " + pursuitPos.size());
				GameFrameCanvas.dungeonMap.setMobPositions(new ArrayList<int[]>() { 
					private static final long serialVersionUID = 1L;

					{addAll(mobPos); addAll(pursuitPos.values());}
				});
				playerNoiseCentre = new int[3];
			}
		}
	}
	class PursuitModerator extends Thread{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Enemy mob;
		public PursuitModerator(Enemy mob){
			this.mob = mob;
			start();
		}
		public void run(){
			//mob isn't dead, is pursuing player, hasn't caught up with him, player is still in the area, and enemy is not at the current player position through intersection in playerpath
			while(!mob.checkDood() && mob.getPursuitIndex() >=0 && mob.getPursuitIndex() < (playerPath.size()-1) && RPGMain.speler.getCurrentPosition().equals(positie)
					&& !(playerPath.get(mob.getPursuitIndex())[0] == playerPath.get(playerPath.size()-1)[0] && playerPath.get(mob.getPursuitIndex())[1] == playerPath.get(playerPath.size()-1)[1])){
				try{
					sleep(3000);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
				//only mobs on the main map will follow
				dungeon.get(0)[playerPath.get(mob.getPursuitIndex())[0]][playerPath.get(mob.getPursuitIndex())[1]].removeEnemy(mob);
				mob.setPursuitIndex(mob.getPursuitIndex()+1);
				dungeon.get(0)[playerPath.get(mob.getPursuitIndex())[0]][playerPath.get(mob.getPursuitIndex())[1]].addEnemy(mob,0);
				pursuitPos.put(mob,new int[] {playerPath.get(mob.getPursuitIndex())[0],playerPath.get(mob.getPursuitIndex())[1],0});
				
				GameFrameCanvas.dungeonMap.setMobPositions(new ArrayList<int[]>() { 
					private static final long serialVersionUID = 1L;

				{addAll(mobPos); addAll(pursuitPos.values());} });
			}
			//mob isn't dead, and at current player position, waiting for combat to finish
			while(playerPath.get(mob.getPursuitIndex())[0] == playerPath.get(playerPath.size()-1)[0] && playerPath.get(mob.getPursuitIndex())[1] == playerPath.get(playerPath.size()-1)[1]
					&& !mob.checkDood()){
				try{
					sleep(500);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
			//System.out.println("Out of loop.");
			mob.setPursuitIndex(-1);
			pursuitPos.remove(mob);
		}
	}
}
