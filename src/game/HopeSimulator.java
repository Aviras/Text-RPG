package game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import bsh.EvalError;

public class HopeSimulator implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static double[][] hopeMap,reputationMap;
	private static ArrayList<HopeCenter> hopeCenters;
	private static final Logger logger = Logger.getLogger(HopeSimulator.class);
	
	public HopeSimulator(){
		hopeMap = new double[Data.wereld.length][Data.wereld[0].length];
		reputationMap = new double[Data.wereld.length][Data.wereld[0].length];
		hopeCenters = new ArrayList<HopeCenter>();
	}
	public static void createHopeCenter(int[] position, double value, int radius, String townTalk, boolean playerInduced){
		hopeCenters.add(new HopeCenter(position,value,radius,townTalk,playerInduced));
	}
	public static double getHope(int x,int y){
		return hopeMap[x][y];
	}
	public static double getReputation(int x, int y){
		return reputationMap[x][y];
	}
	public static void addReputation(int x, int y, double value){
		reputationMap[x][y]+=value;
	}
	public static void calculateHope(){
		for(HopeCenter h: hopeCenters){
			int[] position = h.getPosition();
			int phase = h.getPhase();
			double value = h.getValue();
			for(int j=-phase;j<=phase;j++){
				for(int k=-phase;k<=phase;k++){
					// emulate a traveling wave, hope doesn't get added continuously,
					// would make things less transparent
					if(Math.abs(Math.sqrt(j*j + k*k) - phase) < 0.5){
						//TODO arrayindexoutofboundsexception
						hopeMap[j+position[0]][k+position[1]]+=value/Math.sqrt(j*j+k*k);
						if(h.getPlayerInduced()){
							reputationMap[j+position[0]][k+position[1]]+=value/Math.sqrt(j*j+k*k);
						}
					}
				}
			}
			h.addPhase(1);
		}
	}
	public void createRandomHopeEvent(){
		Random generator = new Random();
		if(generator.nextDouble() < 0.1){
			int x = -1;
			int y = -1;
			while((x == -1 && y == -1) || Data.wereld[x][y] != null){
				x = generator.nextInt(Data.wereld[0].length);
				y = generator.nextInt(Data.wereld.length);
			}
			
			logger.debug("Creating new hope event at " + x + "," + y);
			//FIXME
			/*int x = RPGMain.speler.getCurrentPosition()[0];
			int y = RPGMain.speler.getCurrentPosition()[1];*/
			
			Element el = null;
			try {
				Document doc = Data.parser.build(new File("Data/EventScripts.xml"));
				Element root = doc.getRootElement();
				el = root;
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
			//TODO implement political events
			int number = 0;
			List<Element> events = new ArrayList<Element>();
			if(Data.wereld[x][y] instanceof Town){
				events = el.getChildren("TownEvent");
				number = events.size();
			}
			else if(Data.wereld[x][y] instanceof HostileArea){
				events = el.getChildren("HostileAreaEvent");
				number = events.size();
			}
			if(events.size() != 0){
				int type = generator.nextInt(number);
				Element chosenEvent = events.get(type);
				int gravity = Global.generator.nextInt(2) + 1;
				try {
					Global.beanShell.set("_area", Data.wereld[x][y]);
					Global.beanShell.set("RPGMain",new RPGMain());
					Global.beanShell.set("Global", new Global());
					Global.beanShell.set("gravity", gravity);
					
					Global.beanShell.source(chosenEvent.getChild("script").getTextTrim());
					//TODO put significant events in logbook
				} catch (EvalError e) {
					e.printStackTrace();
					logger.error(e);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					logger.error(e);
				} catch (IOException e) {
					e.printStackTrace();
					logger.error(e);
				}
				createHopeCenter(new int[]{x,y},gravity,2*gravity,chosenEvent.getChild("description").getTextTrim(),false);
				logger.debug("Hope Center created.");
			}
		}
	}
	public void checkHopeImpact(){
		/*for(int j=0;j<hopeMap[0].length;j++){
			for(int k=0;k<hopeMap.length;k++){
				if(Math.abs(hopeMap[j][k]) >= 1){
					//TODO let town have different parameters, according to evolution on different areas, for ex trade (roads), culture & research (libraries & knowledge), etc..
					
				}
			}
		}*/
		for(int j=0;j<hopeMap.length;j++){
			for(int k=0;k<hopeMap[0].length;k++){
				try{
					Data.wereld[j][k].calculateHopeImpact();
				} catch(NullPointerException e){
					
				}
			}
		}
	}
	public void checkTermination(){
		ArrayList<HopeCenter> delete = new ArrayList<HopeCenter>();
		for(HopeCenter h: hopeCenters){
			if(h.getPhase() == h.getRadius()){
				delete.add(h);
			}
		}
		for(HopeCenter h: delete){
			hopeCenters.remove(h);
		}
		delete.clear();
	}
	
	
	static class HopeCenter{
		private int radius,phase;
		private double value;
		private int[] position;
		private boolean playerInduced;
		private String townTalk;
		
		//random event
		public HopeCenter(int[] position, double value, int radius, String townTalk, boolean playerInduced){
			this.position = position;
			this.value = value;
			this.radius = radius;
			this.townTalk = townTalk;
			this.playerInduced = playerInduced;
			phase = 1;
		}
		
		public double getValue(){
			return value;
		}
		public String getTownTalk(){
			return townTalk;
		}
		public int getRadius(){
			return radius;
		}
		public int getPhase(){
			return phase;
		}
		public boolean getPlayerInduced(){
			return playerInduced;
		}
		public int[] getPosition(){
			return position;
		}
		public void addPhase(int x){
			phase+=1;
		}
	}
}
