package game;

import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jdom.Element;

public class Herb extends Item{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int quality;
	private HashMap<String, Object[]> effects;
	private boolean used = false;
	private static Logger logger = Logger.getLogger(Herb.class);
	private boolean prepared = false;
	private String prepareMode = "";

	public Herb(Integer id,String name, String description, Element effectsData, Double weight, Integer cost, String logbookPath, String logbookSummary){
		super(id,name,description,weight,cost);
		effects = new HashMap<String, Object[]>();

		this.logbookSummary = logbookSummary;
		this.logbookPath = logbookPath;
		List<Element> children = effectsData.getChildren();
		Iterator<Element> it = children.iterator();
		/* <action>
		 * 	<description>
		 * 	<effect>
		 * 	<type>
		 * 	<effectType>
		 */
		while(it.hasNext()){
			Element e = it.next();
			String action = e.getName().toLowerCase();
			String effectDescription = e.getChildText("description");
			int effect = 0;
			try{
				effect = Integer.parseInt(e.getChildText("effect"));
			}catch(NumberFormatException exc){
				exc.printStackTrace();
				logger.error("Corrupted data for the effect of " + name + ", ID: " + id,exc);
				effect = 0;
			} catch(NullPointerException exc){
				effect = 0;
			}
			String type = e.getChildText("type");
			String effectType = e.getChildText("effecttype");
			effects.put(action,new Object[]{type,effectDescription,effect,effectType});
		}
		used = false;
	}
	public Herb(Herb another){
		this.ID = another.ID;
		this.name = another.name;
		this.description = another.description;
		this.effects = another.effects;
		this.logbookPath = another.logbookPath;
		this.logbookSummary = another.logbookSummary;
		this.quality = another.quality;
		prepared = false;
		prepareMode = "";
		used = false;
	}

	public int getQuality(){
		return quality;
	}
	public void setQuality(int x){
		quality = x;
	}
	public void showInfo(){
		RPGMain.printText(true,"--------------------------------");
		if(prepared){
			RPGMain.printText(false,"- Prepared " + name);
		}
		else{
			RPGMain.printText(false, "- " + name);
		}
		String[] qualities = {"poor","good","excellent"};
		RPGMain.printText(true, " (Quality: " + qualities[quality] + ")");
		RPGMain.printText(true,"--------------------------------");
		if(prepared){
			RPGMain.printText(true, effects.get(prepareMode)[1].toString());
		}
		else{
			RPGMain.printText(true, description);
		}
	}
	public void use(){
		if(!prepared){
			String input = "";
			boolean cont = false;
			while(!cont){
				RPGMain.printText(true, "There are several possibilities to use " + name + ":");
				for(String action: effects.keySet()){
					RPGMain.printText(true, new String[]{"* ",RPGMain.upperCaseSingle(action, 0)," the herb"}, new String[]{"regular","bold","regular"});
				}
				RPGMain.printText(false, "* Cancel\n>");
				try {
					input = RPGMain.waitForMessage().toLowerCase();

					if(input.equalsIgnoreCase("cancel")){
						return;
					}
					for(String s: effects.keySet()){
						if(s.equalsIgnoreCase(input)){
							cont = true;
							break;
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					logger.error(e);
					continue;
				}
			}
			prepareMode = input;
			prepared = true;

			if(input.equalsIgnoreCase("chewing")){
				RPGMain.printText(true, "You take some leaves, put them in your mouth and start chewing. As with most plants, it tastes quite bitter.");
				try{
					Global.pauseProg(4000);
				} catch(InterruptedException e){
					e.printStackTrace();
					logger.error(e);
				}
				use();
			}
			else if(input.equalsIgnoreCase("grinding")){
				RPGMain.printText(true, "You take some leaves, put them in an improvised bowl and start grinding until you see the sap.");
				try{
					Global.pauseProg(4000);
					while(true){
						RPGMain.printText(false, "Do you want to use it now? [y/n]\n>");
						String choice = RPGMain.waitForMessage().toLowerCase();
						
						if(choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")){
							use();
							break;
						}
						else if(choice.equalsIgnoreCase("n") || choice.equalsIgnoreCase("no")){
							break;
						}
						else{
							RPGMain.printText(true, "Not a valid option.");
						}
					}
				} catch(InterruptedException e){
					e.printStackTrace();
					logger.error(e);
				}
			}
			else if(input.equalsIgnoreCase("boiling")){
				//TODO need fire, water
			}
		}
		else{
			RPGMain.printText(true, effects.get(prepareMode)[1].toString());
			
			String type = effects.get(prepareMode)[0].toString();
			
			if(type.equalsIgnoreCase("Poison") || type.equalsIgnoreCase("Debuff")){
				HashMap<Integer,Equipment> weaponsInInv = new HashMap<Integer,Equipment>();
				while(true){
					RPGMain.printText(true, "Apply the poison to");
					RPGMain.printText(true, "1: " + RPGMain.speler.getWapen().getName() + "(Equipped)");
					int j = 2;
					for(Item i: RPGMain.speler.getInventory().keySet()){
						if(i instanceof Equipment){
							Equipment e = (Equipment)i;
							if(e.getWeaponType() != null){
								RPGMain.printText(true, j + ": " + e.getName());
								weaponsInInv.put(j, e);
								j++;
							}
						}
					}
					RPGMain.printText(false, j + ": Cancel\n>");

					try{
						int choice = Integer.parseInt(RPGMain.waitForMessage());

						if(choice == j){
							break;
						}
						else if(choice > j || choice < 1){
							RPGMain.printText(true, "Not a valid option");
						}
						else if(choice == 1){
							applyPoison(RPGMain.speler.getWapen(),effects.get(prepareMode));
							used = true;
							break;
						}
						else{
							applyPoison(weaponsInInv.get(choice),effects.get(prepareMode));
							used = true;
							break;
						}

					}catch(NumberFormatException exc){
						RPGMain.printText(true,"Not a valid option.");
						continue;
					}catch(InterruptedException exc){
						exc.printStackTrace();
						logger.error(exc);
					}
				}
			}
			else if(type.equalsIgnoreCase("Healing")){
				String effectType = effects.get(prepareMode)[3].toString();
				if(effectType.equalsIgnoreCase("hp")){
					try{
						double effect = Integer.parseInt(effects.get(prepareMode)[2].toString());
						effect*=1.0 + (quality-2.0)/2.0;
						if(effect > 0){
							RPGMain.printText(true, "You feel the healing power of the herb, and regain " + effect + " HP.");
						}
						else{
							RPGMain.printText(true, "This wasn't such a great idea. You have cramps and a slight fever, and lose " + effect + " HP.");
						}
						RPGMain.speler.addHP((int)effect);
						used = true;
					} catch(NumberFormatException e){
						e.printStackTrace();
						logger.error("Corrupted data for herb " + name,e);
					}
				}
			}
			try {
				Global.pauseProg();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				logger.error(e1);
			}
			try{
				int maxPhase = Logbook.getPhase(logbookPath);
				Logbook.addContent(logbookPath, maxPhase+1, effects.get(prepareMode)[1].toString());
			} catch(Exception e){
				e.printStackTrace();
				logger.error(e);
			}
		}

	}
	public boolean isUsed(){
		return used;
	}
	
	@Override
	public String getName(){
		String[] qualities = {"poor","good","excellent"};
		if(prepared){
			return "Prepared " + name + " (Quality: " + qualities[quality] + ")";
		}
		return name  + " (Quality: " + qualities[quality] + ")";
	}
	
	public String getRawName(){
		return name;
	}

	public void applyPoison(Equipment e, Object[] params){
		if(e.getPoison() != null){
			while(true){
				RPGMain.printText(false, "This weapon already has a poison applied to it. Replace it? [y/n]\n>");

				try{
					String replace = RPGMain.waitForMessage().toLowerCase();

					if(replace.equalsIgnoreCase("n") || replace.equalsIgnoreCase("no")){
						break;
					}
					else if(replace.equalsIgnoreCase("y") || replace.equalsIgnoreCase("yes")){
						e.setPoison(params[0].toString(), name, params[1].toString(), Integer.parseInt(params[2].toString()), params[3].toString(), (int)(Math.pow(quality, 2)*(1+RPGMain.speler.getHerbalism()/100.0)));
					}
				}catch(InterruptedException exc){
					exc.printStackTrace();
					logger.error(exc);
				}
			}
		}
		else{
			e.setPoison(params[0].toString(), name, params[1].toString(), (int)params[2], params[3].toString(), (int)(Math.pow(quality, 2)*(1+RPGMain.speler.getHerbalism()/100.0)));

		}
		RPGMain.printText(true, name + " poison was applied to " + e.getName() + ".");
		e.showInfo();
	}
}
