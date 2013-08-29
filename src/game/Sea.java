package game;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;

public class Sea extends Location{
	
	private String name;
	private HashMap<HostileArea,int[]> islands = new HashMap<HostileArea,int[]>();
	private static Logger logger = Logger.getLogger(Sea.class);
	
	public Sea(Integer id,String name, int[] position, Element extra){
		ID = id.intValue();
		this.name = name;
		positie = position;
		try{
			@SuppressWarnings("unchecked")
			List<Element> children = extra.getChildren();
			Iterator<Element> i = children.iterator();
			while(i.hasNext()){
				Element child = i.next();
				int X = Integer.parseInt(child.getAttributeValue("x"));
				int Y = Integer.parseInt(child.getAttributeValue("y"));
				int islandID = Integer.parseInt(child.getTextTrim());
				islands.put(Data.hostileAreas.get(islandID), new int[]{X,Y});
			}
		} catch(NullPointerException e){
		}
	}
	
	public void calculateHopeImpact(){
		
	}
	
	public int getID() {
		return ID;
	}
	public String getName() {
		return name;
	}
	public int[] getPositie() {
		return positie;
	}
	public String getLocationType(){
		return "sea";
	}
	public int[] main_menu() throws InterruptedException{
		//TODO
		return null;
	}
	public int[] main_menu(String direction) throws InterruptedException {
		//TODO
		RPGMain.speler.setCurrentPosition(positie);
		
		RPGMain.printText(true, "The sea spits you right back out. For now, at least.");
		logger.debug("In Sea main_menu");
		
		if(direction.equalsIgnoreCase("north")){
			return new int[]{0,1};
		}
		else if(direction.equalsIgnoreCase("south")){
			return new int[]{0,-1};
		}
		else if(direction.equalsIgnoreCase("east")){
			return new int[]{-1,0};
		}
		else if(direction.equalsIgnoreCase("west")){
			return new int[]{1,0};
		}
		
		return null;
	}
	public void setPlayerPosition() {
		RPGMain.speler.setCurrentPosition(positie);
	}

}
