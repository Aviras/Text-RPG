package game;

import java.io.Serializable;
import java.util.Hashtable;

public class Gate extends DistrictLocation implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean closed = false;
	private String direction;

	public Gate(String name, String description,String type){
		this.name = name;
		this.description = description;
		direction = type.split(":")[1];
	}
	
	public void setClosed(boolean b){
		closed = b;
	}
	public boolean getClosed(){
		return closed;
	}
	public String getDirection(){
		return direction;
	}
	public int[] enter(int[] positie){
		
		int[] i = new int[2];
		RPGMain.printText(true, description);
		Global.pauseProg();
		while(true){
			RPGMain.printText(false, "What would you like to do?\n" + 
									 "1: Check nearby villages\n" +
									 "2: Look at the surroundings\n" + 
									 "3: Go back\n>");
			try{
				int choice = Integer.parseInt(RPGMain.waitForMessage());
				switch(choice){
				case 1: i = chooseCity(positie); break;
				case 2: i = checkSurroundings(positie); break;
				case 3: return i;
				}
				System.out.println(i[0] + "," + i[1]);
				if(i[0] == 0 && i[1] == 0){
				}
				else{
					break;
				}
			} catch(NumberFormatException e){
				RPGMain.printText(true, "Not a valid option.");
			} catch(InterruptedException e){
			}
		}
		System.out.println("Out of loop, returning");
		return i;
		
	}
	public int[] chooseCity(int[] positie) throws InterruptedException{

		RPGMain.printText(true,"Where would you like to go?");
		Hashtable<String,int[]> townMapping = new Hashtable<String,int[]>();
		Location[][] wereld = Data.wereld;
		
		int lowerK = -1;
		int higherK = 1;
		int lowerJ = -1;
		int higherJ = 1;
		
		if(direction.equalsIgnoreCase("east")){
			lowerK = 1;
		}
		else if(direction.equalsIgnoreCase("west")){
			higherK = -1;
		}
		else if(direction.equalsIgnoreCase("north")){
			higherJ = -1;
		}
		else if(direction.equalsIgnoreCase("south")){
			lowerJ = 1;
		}
		
		while(true){
			for(int k = lowerK;k<=higherK;k++){//kijken voor omringende steden
				for(int j = lowerJ;j<=higherJ;j++){
					if(k == 0 && j == 0){}
					else{
						try{//kijken of er iets is, en indien zo kijken of het een Town is
							if(wereld[positie[0] + k][positie[1] + j].getClass().equals(Town.class)){
								RPGMain.printText(true,new String[]{"* " + wereld[positie[0] + k][positie[1] + j].getName() + ", to the ", Town.getDirection(k,j)}, new String[]{"regular","bold"});
								townMapping.put(Town.getDirection(k,j).toLowerCase(), new int[] {k,j});
							}
						}catch(IndexOutOfBoundsException ex){
							continue;
						} catch(NullPointerException e){
							continue;
						}
					}
				}
			}
			
			RPGMain.printText(false,"* Cancel\n>");
			String stad = RPGMain.waitForMessage();
			stad = stad.toLowerCase();
			
			if(stad.equalsIgnoreCase("cancel")){
				RPGMain.printText(true, "You go back through the gate.");
				return new int[]{0,0};
			}
			else{
				if(townMapping.containsKey(stad)){
					if(Global.online) Global.message(RPGMain.speler.getName() + " moved to " + stad + ".");
					return townMapping.get(stad);
				}
				else{
					RPGMain.printText(true,"Not a valid option.");
				}
			}
		}
	}
	
	
	public int[] checkSurroundings(int[] positie) throws InterruptedException{
		
		RPGMain.printText(true,"Where would you like to go?");
		Hashtable<String,int[]> dungeonMapping = new Hashtable<String,int[]>();
		Location[][] wereld = Data.wereld;
		
		int lowerK = -1;
		int higherK = 1;
		int lowerJ = -1;
		int higherJ = 1;
		
		if(direction.equalsIgnoreCase("east")){
			lowerK = 1;
		}
		else if(direction.equalsIgnoreCase("west")){
			higherK = -1;
		}
		else if(direction.equalsIgnoreCase("north")){
			higherJ = -1;
		}
		else if(direction.equalsIgnoreCase("south")){
			lowerJ = 1;
		}
		
		while(true){
			for(int k = lowerK;k<=higherK;k++){//kijken voor omringende gebieden
				for(int j = lowerJ;j<=higherJ;j++){
					if(k == 0 && j == 0){}
					else{
						try{ //kijken of er iets is, en indien zo, checken of t n HostileArea is
							if(wereld[positie[0] + k][positie[1] + j].getClass().equals(HostileArea.class)){
								RPGMain.printText(true,new String[]{"* " + wereld[positie[0] + k][positie[1] + j].getName() + ", to the ", Town.getDirection(k,j)}, new String[]{"regular","bold"});
								dungeonMapping.put(Town.getDirection(k,j).toLowerCase(),new int[] {k,j});
							}
						}catch(IndexOutOfBoundsException ex){
							continue;
						} catch(NullPointerException np){
							continue;
						}
					}
				}
			}
			RPGMain.printText(false,"* Cancel\n>");
				
			String gebied = RPGMain.waitForMessage().toLowerCase();
			System.out.println(gebied);
			if(gebied.equalsIgnoreCase("cancel")){
				return new int[]{0,0};
			}
			else{
				if(dungeonMapping.containsKey(gebied)){
					//if(Global.online) Global.message(RPGMain.speler.getName() + " moved to " + gebied + ".");
					// main menu wordt geroepen uit hostileAreas van data, via hashtable
					System.out.println("Found key " + dungeonMapping.get(gebied));
					
					return dungeonMapping.get(gebied);
				}
				else{
					RPGMain.printText(true,"Not a valid option.");
				}
			}
		}
	}
	public void enter() throws InterruptedException {
	}
}
