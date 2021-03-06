package game;
import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class Data {
	
	public Data(){

	}
	
	public static void setWereld(Location[][] world){
		wereld = world;
	}
	
	public static HashMap<Integer,Equipment> equipment = new HashMap<Integer,Equipment>();
	public static HashMap<Integer,Item> items = new HashMap<Integer,Item>();
	public static HashMap<Integer,Clothing> clothes = new HashMap<Integer,Clothing>();
	public static HashMap<Integer,Potion> potions = new HashMap<Integer,Potion>();
	public static HashMap<Integer,Spell> spells = new HashMap<Integer,Spell>();
	public static HashMap<Integer,Quest> quests = new HashMap<Integer,Quest>();
	public static HashMap<Integer,NPC> NPCs = new HashMap<Integer,NPC>();
	public static HashMap<Integer,Enemy> enemies = new HashMap<Integer,Enemy>();
	public static HashMap<Integer,HostileArea> hostileAreas = new HashMap<Integer,HostileArea>();
	public static HashMap<Integer,Town> towns = new HashMap<Integer,Town>();
	public static HashMap<Integer,Sea> seas = new HashMap<Integer,Sea>();
	public static HashMap<Integer,Ability> abilities = new HashMap<Integer,Ability>();
	public static HashMap<Integer,Consumable> consumables = new HashMap<Integer,Consumable>();
	public static HashMap<Integer,Herb> herbs = new HashMap<Integer, Herb>();
	public static HashMap<Integer, Artifact> artifacts = new HashMap<Integer, Artifact>();
	public static HashMap<Integer, SkillElement> skillElements = new HashMap<Integer, SkillElement>();
	public static HashMap<Integer, Guild> guilds = new HashMap<Integer, Guild>();
	public static Location[][] wereld;
	public static SAXBuilder parser = new SAXBuilder();
	
	private transient final Logger logger = Logger.getLogger(Data.class);
	
	public static Object getObject(String type, int id){
		if(type.equalsIgnoreCase("equipment")){
			return equipment.get(id);
		}
		else if(type.equalsIgnoreCase("item")){
			return items.get(id);
		}
		else if(type.equalsIgnoreCase("clothes")){
			return clothes.get(id);
		}
		else if(type.equalsIgnoreCase("potion")){
			return potions.get(id);
		}
		else if(type.equalsIgnoreCase("spell")){
			return spells.get(id);
		}
		else if(type.equalsIgnoreCase("quest")){
			return quests.get(id);
		}
		else if(type.equalsIgnoreCase("npc")){
			return NPCs.get(id);
		}
		else if(type.equalsIgnoreCase("enemy")){
			return enemies.get(id);
		}
		else if(type.equalsIgnoreCase("hostileArea")){
			return hostileAreas.get(id);
		}
		else if(type.equalsIgnoreCase("town")){
			return towns.get(id);
		}
		else if(type.equalsIgnoreCase("sea")){
			return seas.get(id);
		}
		else if(type.equalsIgnoreCase("ability")){
			return abilities.get(id);
		}
		else if(type.equalsIgnoreCase("consumable")){
			return consumables.get(id);
		}
		else if(type.equalsIgnoreCase("herb")){
			return herbs.get(id);
		}
		else if(type.equalsIgnoreCase("artifact")){
			return artifacts.get(id);
		}
		else if(type.equalsIgnoreCase("skillElement")){
			return skillElements.get(id);
		}
		else if(type.equalsIgnoreCase("guild")){
			return guilds.get(id);
		}
		return null;
	}
	
	public void loadEquipment(File aFile){
		Equipment testItem;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testItem = (Equipment)Global.rwtext.loadData(Equipment.class, (Element)i.next());
				equipment.put(testItem.getID(),testItem);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadGuilds(File aFile){
		Guild testGuild;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testGuild = (Guild)Global.rwtext.loadData(Guild.class, (Element)i.next());
				guilds.put(testGuild.getID(),testGuild);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadConsumables(File aFile){
		Consumable testConsumable;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testConsumable = (Consumable)Global.rwtext.loadData(Consumable.class, (Element)i.next());
				consumables.put(testConsumable.getID(),testConsumable);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadHerbs(File aFile){
		Herb testHerb;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testHerb = (Herb)Global.rwtext.loadData(Herb.class, (Element)i.next());
				herbs.put(testHerb.getID(),testHerb);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadArtifacts(File aFile){
		Artifact testArtifact;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testArtifact = (Artifact)Global.rwtext.loadData(Artifact.class, (Element)i.next());
				artifacts.put(testArtifact.getID(),testArtifact);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadSkillElements(File aFile){
		SkillElement testSkillElement;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testSkillElement = (SkillElement)Global.rwtext.loadData(SkillElement.class, (Element)i.next());
				skillElements.put(testSkillElement.getID(),testSkillElement);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadClothes(File aFile){
		Clothing testCloth;
		try{
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testCloth = (Clothing)Global.rwtext.loadData(Clothing.class, (Element)i.next());
				clothes.put(testCloth.getID(), testCloth);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadPotions(File aFile){
		Potion testPotion;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testPotion = (Potion)Global.rwtext.loadData(Potion.class, (Element)i.next());
				potions.put(testPotion.getID(),testPotion);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadSpells(File aFile){
		Spell testSpell;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testSpell = (Spell)Global.rwtext.loadData(Spell.class, (Element)i.next());
				spells.put(testSpell.getID(),testSpell);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadQuests(File aFile){
		Quest testQuest;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testQuest = (Quest)Global.rwtext.loadData(Quest.class, (Element)i.next());
				quests.put(testQuest.getID(),testQuest);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}

	public void loadNPCs(File aFile){
		NPC testNPC;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testNPC = (NPC)Global.rwtext.loadData(NPC.class, (Element)i.next());
				NPCs.put(testNPC.getID(),testNPC);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadAbilities(File aFile){
		Ability testAbility;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testAbility = (Ability)Global.rwtext.loadData(Ability.class, (Element)i.next());
				abilities.put(testAbility.getID(),testAbility);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadEnemies(File aFile){
		Enemy testEnemy;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testEnemy = (Enemy)Global.rwtext.loadData(Enemy.class, (Element)i.next());
				enemies.put(testEnemy.getID(),testEnemy);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadTowns(File aFile){
		Town testTown;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testTown = (Town)Global.rwtext.loadData(Town.class, (Element)i.next());
				towns.put(testTown.getID(),testTown);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadHostileAreas(File aFile){
		HostileArea testHostileArea;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testHostileArea = (HostileArea)Global.rwtext.loadData(HostileArea.class, (Element)i.next());
				hostileAreas.put(testHostileArea.getID(),testHostileArea);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadSeas(File aFile){
		Sea testSea;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testSea = (Sea)Global.rwtext.loadData(Sea.class, (Element)i.next());
				seas.put(testSea.getID(),testSea);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadItems(File aFile){
		Item testItem;
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				testItem = (Item)Global.rwtext.loadData(Item.class, (Element)i.next());
				items.put(testItem.getID(),testItem);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			logger.debug(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(e);
		}
	}
	public void loadWereld(File aFile){
		try {
			Document doc = parser.build(aFile);
			//there is only 1 element, so the root element is the World Map objects
			//all children are already the actual properties
			Element root = doc.getRootElement();
			@SuppressWarnings("unchecked")
			List<Element> objects = root.getChildren();
			//i iterates over the actual properties
			Iterator<Element> i = objects.iterator();
			String[] heightMap = null;
			while(i.hasNext()){
				Element child = i.next();
				if(child.getName().equalsIgnoreCase("heightmap")){
					heightMap = initiateWorld(child.getTextTrim());
				}
				else if(child.getName().equalsIgnoreCase("extra")){
					populateWorld(child,heightMap);
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
	public String[] initiateWorld(String s){
		String[] lines = s.split(System.getProperty("line.separator"));
		if(lines.length == 1){
			lines = s.split("\n");
		}
		int height = lines.length;
		int width = lines[0].length();
		
		wereld = new Location[width][height];
		
		return lines;
	}
	public void populateWorld(Element e, String[] heightMap){
		@SuppressWarnings("unchecked")
		List<Element> locations = e.getChildren();
		Iterator<Element> i = locations.iterator();
		/*for(int j=0;j<wereld.length;j++){
			for(int k=0;k<wereld[0].length;k++){
				wereld[j][k] = new Sea(0,"Default Sea", new int[]{j,k},null);
			}
		}*/
		// populate world with towns & hostileAreas
		while(i.hasNext()){
			Element child = i.next();
			int x = Integer.parseInt(child.getAttributeValue("x"));
			int y = Integer.parseInt(child.getAttributeValue("y"));
			if(child.getName().equalsIgnoreCase("town")){
				wereld[x][y] = towns.get(Integer.parseInt(child.getTextNormalize()));
				towns.get(Integer.parseInt(child.getTextNormalize())).setPosition(x, y);
			}
			else if(child.getName().equalsIgnoreCase("hostileArea")){
				wereld[x][y] = hostileAreas.get(Integer.parseInt(child.getTextNormalize()));
				hostileAreas.get(Integer.parseInt(child.getTextNormalize())).setPosition(x, y);
			}
			else if(child.getName().equalsIgnoreCase("sea")){
				wereld[x][y] = new Sea(0,"Default Sea", new int[]{x,y},null);
				/*try{
					//TODO
					//TODO make a linear mapping from the in-game map to the drawn map, so it's easy to put the right position
					wereld[x][y] = seas.get(Integer.parseInt(child.getTextNormalize()));
					seas.get(Integer.parseInt(child.getTextNormalize())).setPosition(x, y);
				} catch(NullPointerException exc){
				} catch(ArrayIndexOutOfBoundsException exc){
				}*/
			}
		}
		// set world level heights
		for(int j=0;j<heightMap.length;j++){
			for(int k=0;k<heightMap[0].length();k++){
				try{
					wereld[k][j].setAltitude(Character.digit(heightMap[j].charAt(k), 10));
				}catch(NullPointerException np){
				}
			}
		}
	}

}

