package game;
import java.util.List;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jdom.Element;

public class Town extends Location{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String naam,image;
	protected int grootte,imageID;
	protected Inn inn;
	protected District[][] districts;
	private int[] playerPosition;
	boolean firstTime = true;
	private double prevHope;
	
	private static final Logger logger = Logger.getLogger(Town.class);
	
	public Town(){
		
	}
	
	public Town(Integer id, String name, String image, Element el){
		ID = id.intValue();
		naam = name;
		this.image = image;
		prevHope = 0;
		buildCity(el);
	}
	
	@SuppressWarnings("unchecked")
	public void buildCity(Element el){
		List<Element> d = el.getChildren();
		Iterator<Element> i = d.iterator();
		int maxX = 0,maxY = 0;
		while(i.hasNext()){
			Element district = i.next();
			int x = Integer.parseInt(district.getAttributeValue("x"));
			int y = Integer.parseInt(district.getAttributeValue("y"));
			if(x>maxX){
				maxX = x;
			}
			if(y>maxY){
				maxY = y;
			}
		}
		districts = new District[maxX+1][maxY+1];
		i = d.iterator();
		while(i.hasNext()){
			Element district = i.next();
			int x = Integer.parseInt(district.getAttributeValue("x"));
			int y = Integer.parseInt(district.getAttributeValue("y"));
			String name = district.getChildText("name");
			String description = district.getChildText("description");
			String qnpcs = district.getChildText("npcs");
			List<Element> locations = district.getChildren("location");
			districts[x][y] = new District(new int[] {x,y},name,description,qnpcs,locations);
		}
	}
	
	public void calculateHopeImpact(){
		//TODO
		// hope determines general mental state of NPCs,
		// if some stores close, or higher/lower prices and even increase possible products?
		// people die from diseases,
		// 
	}
	
	/* GETTERS */
	
	public int getID(){
		return ID;
	}
	public int[] getPositie(){
		return positie;
	}
	public String getLocationType(){
		return "town";
	}
	public String getImage(){
		return image;
	}
	public String getType(){
		return "Town";
	}
	public boolean hasInn(){
		for(int j=0;j<districts.length;j++){
			for(int k=0;k<districts[0].length;k++){
				try{
					if(districts[j][k].hasInn())
					{
						return true;
					}
				} catch(NullPointerException e){
					
				} catch(Exception e){
					e.printStackTrace();
					logger.error(e);
				}
			}
		}
		return false;
	}
	public boolean hasGate(String direction){
		if(districts[playerPosition[0]][playerPosition[1]].getGate(direction) != null){
			return true;
		}
		
		return false;
	}
	public static String getDirection(int x,int y){
		// origin is in left upper corner
		//TODO if x=y=0
		int som = x+y;
		
		if(som == 2) return "South-East";
		if(som == -2) return "North-West";
		if(som == 0){
			if(x == 1) return "North-East";
			else return "South-West";
		}
		if(som == 1){
			if(x == 1) return "East";
			else return "South";
		}
		if(som == -1){
			if(x == -1) return "West";
			else return "North";
		}
		if(x == 10 && y == 10){
			return "START";
		}
		return null;
	}
	public String getName(){
		return naam;
	}
	
	/* END OF GETTERS */
	public void setPlayerPosition(){
		RPGMain.speler.setCurrentPosition(positie);
	}
	
	public void tellHistory() throws InterruptedException{
		RPGMain.printText(true,"-----------------------");
		RPGMain.printText(true,"--" + naam.toUpperCase());
		RPGMain.printText(true,"-----------------------");
		String[] path = {naam,"History"};
		String history = Global.makeDialog(new File("Data/CityDialog.xml"),path);
		try{
			Logbook.addContent("Lore/World/City/" + naam, 1, history);
		} catch(Exception e){
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	public int[] main_menu() throws InterruptedException{
		
		return null;
	}
	public int[] main_menu(String direction) throws InterruptedException{
		
		logger.info("Entering " + naam + " going to the " + direction);
		
		RPGMain.printText(true, "You enter " + naam + ".");
		
		//TODO
		//tellHistory();
		
		int[] position = new int[2];
		position[0] = -1;
		position[1] = -1;
		//player enters town on horseback from other town
		if(RPGMain.speler.getOnHorse()){
			RPGMain.speler.setOnHorse(false);
			for(int j=0;j<districts.length;j++){
				for(int k=0;k<districts[0].length;k++){
					try{
						if(districts[j][k].hasStables()){
							position[0] = j;
							position[1] = k;
							j = districts.length;
							k = districts[0].length;
							break;
						}
					} catch(Exception e){
						continue;
					}
				}
			}
			if(position[0] == -1 && position[1] == -1){
				for(int j=0;j<districts.length;j++){
					for(int k=0;k<districts[0].length;k++){
						if(districts[j][k] != null){
							position = new int[]{j,k};
							break;
						}
					}
				}
			}
		}
		else if(direction.equalsIgnoreCase("north")){
			for(int j=0;j<districts.length;j++){
				try{
					if(!districts[j][districts[j].length-1].getGate("south").getClosed()){
						position = new int[]{j,districts[j].length-1};
					}
				}catch(Exception e){
					continue;
				}
			}
		}
		else if(direction.equalsIgnoreCase("south")){
			for(int j=0;j<districts.length;j++){
				try{
					if(!districts[j][0].getGate("north").getClosed()){
						position = new int[]{j,0};
					}
				}catch(Exception e){
					continue;
				}
			}
		}
		else if(direction.equalsIgnoreCase("east")){
			for(int j=0;j<districts[0].length;j++){
				try{
					if(!districts[0][j].getGate("west").getClosed()){
						position = new int[]{0,j};
					}
				}catch(Exception e){
					continue;
				}
			}
		}
		else if(direction.equalsIgnoreCase("west")){
			for(int j=0;j<districts[0].length;j++){
				try{
					if(!districts[districts.length-1][j].getGate("east").getClosed()){
						position = new int[]{districts.length-1,j};
					}
				}catch(Exception e){
					continue;
				}
			}
		}
		else if(direction.equalsIgnoreCase("START")){
			for(int j=0;j<districts.length;j++){
				for(int k=0;k<districts[0].length;k++){
					try{
						if(!districts[j][k].getGates().isEmpty()){
							position[0] = j;
							position[1] = k;
							j = districts.length;
							k = districts[0].length;
						}
					} catch(Exception e){
						continue;
					}
				}
			}
		}
		if(position[0] == -1 && position[1] == -1){
			Global.pauseProg(3000);
			RPGMain.printText(true, "You can't enter the city from this direction.");
			logger.error("Could not enter");
			return new int[]{0,0};
		}
		
		logger.debug("Position: " + position[0] + "," + position[1]);
		playerPosition = position;
		
		// set this city to current player location
		setPlayerPosition();
		
		//TODO
		GameFrameCanvas.dungeonMap.initCityMap(districts, position);
		
		// manage UI component visibility
		managePanelVisibility("town");
		
		Global.pauseProg(3000);
		
		// initialize sounds
		initializeSounds();
		
		double newHope = HopeSimulator.getHope(positie[0], positie[1]);
		boolean updateEmoValues = false;
		
		if(newHope != prevHope){
			prevHope = newHope;
			updateEmoValues = true;
		}
		
		for(int j=0;j<districts.length;j++){
			for(int k=0;k<districts[0].length;k++){
				try{
					if(updateEmoValues){
						districts[j][k].updateEmoValues();
					}
					districts[j][k].determineNPCMentalState();
				} catch(Exception e){
					continue;
				}
			}
		}
		
		RPGMain.speler.setTrade(true);
		
		int[] dir = new int[2];
		while(true){
			try{
				dir = districts[playerPosition[0]][playerPosition[1]].enter();
				//signal coming from stables, 2 levels down
				if(Math.abs(dir[0]) + Math.abs(dir[1]) >= 5){
					dir[0]/=5;
					dir[1]/=5;
					return dir;
				}
				playerPosition[0]+=dir[0];
				playerPosition[1]+=dir[1];
				RPGMain.speler.addHunger(1);
				RPGMain.speler.addThirst(2);
				RPGMain.speler.addFitness(-1);
				Global.soundEngine.chooseAndPlaySound(WeatherSimulator.getWeather(positie[0], positie[1]), "town", 
														WeatherSimulator.getSolarIntensity(positie[0], positie[1]), 
														true, new String[]{"effects","footsteps"}, 0, 0);
				Global.pauseProg(5000);
			}catch(Exception e){
				return dir;
			}
		}
	}
	
	
	class District implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String districtName,enterDescription;
		private ArrayList<DistrictLocation> locations;
		private ArrayList<Gate> gates;
		private int[] districtPos;
		private ArrayList<NPC> presentNPCs;
		
		public District(int[] pos, String name, String description, String npcs, List<Element> extra){
			districtPos = pos;
			districtName = name;
			enterDescription = description;
			locations = new ArrayList<DistrictLocation>();
			presentNPCs = new ArrayList<NPC>();
			gates = new ArrayList<Gate>();
			
			if(npcs != null){
				String[] t = npcs.split(";");
				for(String s:t){
					try{
						presentNPCs.add(Data.NPCs.get(Integer.parseInt(s)));
					}catch(NumberFormatException e){
					}catch(IndexOutOfBoundsException e){
					}
				}
			}
			
			Iterator<Element> i = extra.iterator();
			while(i.hasNext()){
				Element el = i.next();
				String nameDummy = el.getChildText("name");
				String descriptionDummy = el.getChildText("description");
				String type = el.getChildText("type");
				String itemsPresent = el.getChildText("itemsPresent");
				//TODO allow multiple npcs and still separate main npc (innkeeper, shopkeeper)
				String npcID = el.getChildText("npcID");
				String otherNPCs = el.getChildText("npcs");
				String connectedCities = el.getChildText("connectedCities");
				String performances = el.getChildText("performances");
				if(type.equalsIgnoreCase("shop")){
					locations.add(new Winkel(nameDummy,0,descriptionDummy,itemsPresent,npcID));
				}
				else if(type.startsWith("gate")){
					gates.add(new Gate(nameDummy,descriptionDummy,type));
				}
				else if(type.equalsIgnoreCase("inn")){
					logger.debug("otherNPCs: " + otherNPCs);
					if(otherNPCs == null){
						locations.add(new Inn(nameDummy,descriptionDummy,npcID));
					}
					else{
						locations.add(new Inn(nameDummy,descriptionDummy,npcID,otherNPCs));
					}
				}
				//TODO use constructors with extra npcs
				else if(type.equalsIgnoreCase("library")){
					locations.add(new Library(nameDummy,descriptionDummy,itemsPresent));
				}
				else if(type.equalsIgnoreCase("house")){
					locations.add(new House(nameDummy,descriptionDummy,otherNPCs,itemsPresent));
				}
				else if(type.equalsIgnoreCase("stables")){
					locations.add(new Stables(nameDummy,descriptionDummy,npcID,connectedCities));
				}
				else if(type.equalsIgnoreCase("culturecentre")){
					locations.add(new CultureCentre(nameDummy, descriptionDummy, performances));
				}
			}
		}
		public void updateEmoValues(){
			for(NPC n: presentNPCs){
				n.addEmotionValue("happy", 0.10*prevHope);
				n.addEmotionValue("angry", -0.10*prevHope);
				n.addEmotionValue("hopeful", 0.15*prevHope);
				n.addEmotionValue("frightened", -0.15*prevHope);
			}
		}
		public void determineNPCMentalState(){
			HashMap<String,Double> emotions;
			for(NPC n: presentNPCs){
				
				emotions = n.getEmotionValues();
				double sum = 0;
				double random = Global.generator.nextDouble();
				for(String s: emotions.keySet()){
					sum+=emotions.get(s);
					if(random < sum){
						n.addEmotionValue(s, 1.0);
						break;
					}
				}
			}
		}
		public boolean hasInn(){
			for(DistrictLocation d: locations){
				if(d instanceof Inn){
					return true;
				}
			}
			return false;
		}
		public boolean hasStables(){
			for(DistrictLocation d: locations){
				if(d instanceof Stables){
					return true;
				}
			}
			return false;
		}
		
		public int[] enter(){
			
			logger.info("Entering " + districtName);
			
			GameFrameCanvas.dungeonMap.setPlayerDistrictPosition(districtPos);
			
			RPGMain.printText(true, enterDescription);
			Global.pauseProg();
			
			while(true){
				RPGMain.printText(true, new String[]{"You are in ",naam + ": " + districtName + "."}, new String[]{"darkblue","darkbluebold"});

				if(!presentNPCs.isEmpty()){
					RPGMain.printText(true, "Present citizens:");
					for(NPC q:presentNPCs){
						RPGMain.printText(true, new String[]{"Talk to ", q.getFullName()}, new String[]{"bold","regular"});
					}
				}
				for(DistrictLocation d:locations){
					if(d.getClass().equals(Winkel.class)){
						if(WeatherSimulator.getSolarIntensity(positie[0], positie[1]) >= 0){
							RPGMain.printText(true, new String[]{"* ", "Visit " , d.getName()}, new String[]{"regular","bold","regular"});
						}
						else{
							RPGMain.printText(true, new String[]{"* ", "Trespass" , " into " + d.getName()}, new String[]{"regular","bold","regular"});
						}
					}
					else{
						RPGMain.printText(true, new String[]{"* ", "Visit " , d.getName()}, new String[]{"regular","bold","regular"});
					}
				}
				for(Gate g: gates){
					RPGMain.printText(true, new String[]{"* ", "Enter " , "the " + g.getName()}, new String[]{"regular","bold","regular"});
				}
				HashMap<String,int[]> possibleDirections = new HashMap<String,int[]>();
				for(int j=-1;j<=1;j++){
					for(int k=-1;k<=1;k++){
						if((j == 0 && k!=0) || (j!=0 && k == 0)){
							try{
								RPGMain.printText(true, new String[]{"* ", Town.getDirection(j, k) + ": ", "Go to the " + districts[districtPos[0]+j][districtPos[1]+k].getName()}, new String[]{"regular","bold","regular"});
								possibleDirections.put(getDirection(j,k).toLowerCase(), new int[]{j,k});
							}catch(Exception e){
								continue;
							}
						}
					}
				}
				RPGMain.printText(false, "Where do you want to go?\n>");
				try {
					String choice = RPGMain.waitForMessage().toLowerCase();
					String[] parts = choice.split(" ",2);
					// origin is in upper left corner
					if(possibleDirections.containsKey(choice)){
						return possibleDirections.get(choice);
					}
					else if(choice.equalsIgnoreCase("look")){
						RPGMain.printText(true, enterDescription);
						Global.pauseProg();
					}
					else if(choice.equalsIgnoreCase("listen")){
						String[] path = {naam,"Conversation","district,name:" + districtName};
						Global.makeDialog(new File("Data/CityDialog.xml"), path);
						Global.pauseProg();
					}
					if(parts[0].equalsIgnoreCase("enter") || parts[0].equalsIgnoreCase("visit")){
						for(Gate g:gates){
							if(g.getName().toLowerCase().contains(parts[1])){
								ArrayList<String> fileNames = new ArrayList<String>();
								fileNames.add("Sounds/Effects/footsteps_concrete.mp3");
								Global.soundEngine.playSound(fileNames, "effects", 0, new int[]{0});
								Global.pauseProg(5000);
								fileNames.clear();
								int[] newPos = g.enter(positie);
								if(newPos[0] == 0 && newPos[1] == 0){
									RPGMain.printText(true, "You walk back to where you came from.");
									break;
								}
								else{
									return newPos;
								}
							}
						}
						for(DistrictLocation d:locations){
							if(d.getName().toLowerCase().contains(parts[1])){
								ArrayList<String> fileNames = new ArrayList<String>();
								fileNames.add("Sounds/Effects/footsteps_concrete.mp3");
								Global.soundEngine.playSound(fileNames, "effects", 0, new int[]{0});
								Global.pauseProg(5000);
								fileNames.clear();
								
								if(d.getClass().equals(Stables.class)){
									Stables s = (Stables)d;
									int[] newPos = s.chooseDestination();
									if(newPos[0] == 0 && newPos[1] == 0){
										RPGMain.printText(true, "You walk back to where you came from.");
										break;
									}
									else{
										return newPos;
									}
								}
								else{
									Global.soundEngine.fadeLines("music");
									Global.soundEngine.fadeLines("ambient");
									Global.soundEngine.fadeLines("weather");
									SoundEngine.setWeatherGain(-23);
									fileNames.add("Sounds/Effects/door_open_edit.mp3");
									fileNames.add("Sounds/Effects/door_close.wav");
									Global.soundEngine.playSound(fileNames, "effects", 0, new int[]{0,500});
									if(d.getClass().equals(Winkel.class)){
										Winkel w = (Winkel)d;
										if(WeatherSimulator.getSolarIntensity(positie[0], positie[1]) > 0){
											w.enter();
										}
										else{
											w.trespass();
										}
									}
									else{
										//TODO fire sound doesn't always have to be here
										Global.soundEngine.playSound("Sounds/Ambient/fireplace.wav", "ambient", 100, 0, 0, false);
										d.enter();
										SoundEngine.closeLine("Sounds/Ambient/fireplace.wav", "ambient");
									}
									SoundEngine.setWeatherGain(0);
									Global.soundEngine.playSound(fileNames, "effects", 0, new int[]{0,2000});
									
									Global.soundEngine.chooseAndPlaySound(WeatherSimulator.getWeather(positie[0], positie[1]), 
											getLocationType(), WeatherSimulator.getSolarIntensity(positie[0], positie[1]),
											true, new String[]{"ambient","streets"}, 1,0);
									Global.soundEngine.chooseAndPlaySound(WeatherSimulator.getWeather(positie[0], positie[1]), 
											getLocationType(), WeatherSimulator.getSolarIntensity(positie[0], positie[1]),
											true, new String[]{"weather"}, 1,0);
									fileNames.clear();
								}
								break;
							}
						}
					}
					else if(parts[0].equalsIgnoreCase("talk")){
						try{
							String name = (parts[1].split(" "))[1];
							for(NPC q:presentNPCs){
								if(q.getName().toLowerCase().contains(name)){
									q.talk();
									break;
								}
							}
						}catch(ArrayIndexOutOfBoundsException e){
							RPGMain.printText(true, "Syntax is \"talk to <name>\". " + choice);
						}
					}
				} catch(InterruptedException e){
				} catch(ArrayIndexOutOfBoundsException e){
				} catch(NullPointerException e){
					e.printStackTrace();
					RPGMain.printText(true,"Not a valid option");
				}
			}
		}
		public String getName(){
			return districtName;
		}
		public String getDescription(){
			return enterDescription;
		}
		public int[] getDistrictPosition(){
			return districtPos;
		}
		public ArrayList<Winkel> getShops(){
			ArrayList<Winkel> t = new ArrayList<Winkel>();
			for(DistrictLocation d:locations){
				if(d.getClass().equals(Winkel.class)){
					t.add((Winkel)d);
				}
			}
			return t;
		}
		public ArrayList<NPC> getPresentNPCs(){
			return presentNPCs;
		}
		public ArrayList<DistrictLocation> getDistrictLocations(){
			return locations;
		}
		public ArrayList<Gate> getGates(){
			return gates;
		}
		public Gate getGate(String direction){
			for(Gate g:gates){
				if(direction.equalsIgnoreCase(g.getDirection())){
					return g;
				}
			}
			return null;
		}
		
	}
}

