package game;

import java.io.Serializable;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

public abstract class Location extends Data implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int ID;
	protected int[] positie = new int[2];
	protected int altitude;
	protected double temperature;
	protected double moistness;
	protected double snowfall;
	private static final Logger logger = Logger.getLogger(Location.class);
	
	public abstract int getID();
	public abstract String getName();
	public abstract int[] getPositie();
	public abstract int[] main_menu(String direction) throws InterruptedException;
	public abstract int[] main_menu() throws InterruptedException;
	public abstract String getLocationType();
	public abstract void setPlayerPosition();
	public abstract void calculateHopeImpact();
	public void setAltitude(int x){
		altitude = x;
	}
	public void addSnowfall(double d){
		snowfall = Math.min(1.0, Math.max(0, snowfall+d));
	}
	public void addMoistness(double d){
		moistness = Math.min(1.0, Math.max(0, moistness+d));
	}
	public void setPosition(int x,int y){
		positie[0] = x;
		positie[1] = y;
	}
	public int getAltitude(){
		return altitude;
	}
	public double getMoistness(){
		return moistness;
	}
	public double getSnowfall(){
		return snowfall;
	}
	public double getTemperature(){
		return temperature;
	}
	public void setTemperature(double t){
		//System.out.println("Setting T at " + positie[0] + ", " + positie[1]);
		temperature = t;
	}
	public void addTemperature(double x){
		//System.out.println("Changing T at " + positie[0] + ", " + positie[1]);
		temperature+=x;
	}
	public void initializeSounds(){
		Global.soundEngine.fadeLines("music");
		Global.soundEngine.fadeLines("effects");
		Global.soundEngine.fadeLines("ambient");
		
		/*Global.soundEngine.chooseAndPlaySound(WeatherSimulator.getWeather(positie[0], positie[1]), 
				getLocationType(), WeatherSimulator.getSolarIntensity(positie[0], positie[1]),
				true, new String[]{"music"}, 1,(int)(Math.random()*50000));*/
		Global.soundEngine.chooseAndPlaySound(WeatherSimulator.getWeather(positie[0], positie[1]), 
				getLocationType(), WeatherSimulator.getSolarIntensity(positie[0], positie[1]),
				true, new String[]{"ambient","streets"}, 1,0);
		WeatherSimulator.determineWeatherSounds();
	}
	
	public void managePanelVisibility(String type) throws InterruptedException{
		GameFrameCanvas.updatePlayerInfoTable();
		if(type.equalsIgnoreCase("town")){
			System.out.println("ID: " + ID);
			GameFrameCanvas.playerInfoScrollPane.setVisible(true);
			GameFrameCanvas.imagePanel.changeImage("Town " + ID);
			GameFrameCanvas.imagePanel.setVisible(true);
			GameFrameCanvas.playerPortrait.setVisible(true);
			GameFrameCanvas.enemyPortrait.setVisible(false);
			GameFrameCanvas.dungeonMap.setVisible(true);
			GameFrameCanvas.battlefield.setVisible(false);
			GameFrameCanvas.movementModePanel.setVisible(false);
			GameFrameCanvas.noiseMeter.setVisible(false);
			GameFrameCanvas.environmentScrollPane.setVisible(true);
		}
		else if(type.equalsIgnoreCase("hostileArea")){
			GameFrameCanvas.playerInfoScrollPane.setVisible(true);
			GameFrameCanvas.playerPortrait.setVisible(true);
			GameFrameCanvas.enemyPortrait.setVisible(false);
			GameFrameCanvas.imagePanel.setVisible(false);
			GameFrameCanvas.dungeonMap.setVisible(true);
			GameFrameCanvas.battlefield.setVisible(false);
			GameFrameCanvas.movementModePanel.setVisible(true);
			GameFrameCanvas.noiseMeter.setVisible(true);
			GameFrameCanvas.environmentScrollPane.setVisible(true);
		}
		GameFrame.mainPanel.placeLogo();
	}

	class TemperatureChecker extends Thread implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		double currentTemperature;
		boolean[] messaged = {false,false,false};
		int mantleValue,shirtValue,pantsValue;
		
		public TemperatureChecker(double c){
			currentTemperature = c;
			logger.info("Started temperature check");
			start();
		}
		public void run(){
			// too cold on chest: less strength, slower -> bigger chance to get hit
			// too cold on feet: slower movement, damage after a while
			// too cold on cloak: take damage because not shielded from elements
			logger.debug("Started run method for Temperature checker");
			
			
			while(RPGMain.speler != null && !RPGMain.speler.checkDood() && RPGMain.speler.getCurrentPosition() == positie){
				logger.debug("In while loop of TemperatureChecker");
				logger.debug("T: " + currentTemperature);
				
				checkChanges();
				resetClothingLabels();
				
				logger.debug("Clothing values: " + mantleValue + ", " + shirtValue + ", " + pantsValue);
				
				/*
				 * MANTLE
				 */
				// cold
				if(7*mantleValue + currentTemperature < 8){
					if(!messaged[0]){
						GameFrameCanvas.clothingIcons[0].setVisible(true);
						GameFrameCanvas.clothingIcons[0].setToolTipText("It's cold out here! The wind isn't helping either. You could use a thicker cloak. You're burning through your food reserves fast.");
					}
					//TODO
					RPGMain.speler.addHunger((int)(Math.abs(7*mantleValue+currentTemperature-8)));
					messaged[0] = true;
				}
				// hot
				else if(10*mantleValue + currentTemperature > 30){
					if(!messaged[0]){
						GameFrameCanvas.clothingIcons[0].setVisible(true);
						GameFrameCanvas.clothingIcons[0].setToolTipText("It's hot with that mantle on! Dehydration will kill you if you aren't careful. Better find something to drink soon.");
					}
					RPGMain.speler.addThirst((int)((10*mantleValue + currentTemperature - 35)));
					messaged[0]= true;
				}
				
				/*
				 * SHIRT
				 */
				// cold
				if(7*shirtValue + currentTemperature < 8){
					if(!messaged[1]){
						GameFrameCanvas.clothingIcons[1].setVisible(true);
						GameFrameCanvas.clothingIcons[1].setToolTipText("The cold enters your chest, and feel your arms going numb. Strength continually being reduced by " + Math.abs(7*shirtValue + currentTemperature-8) + "%, and your reflexes are slowed.");
					}
					// decrease strength and dexterity
					//TODO balance numbers
					//RPGMain.speler.increaseStat(0, -Math.abs(7*shirtValue + currentTemperature-8)/100*(double)RPGMain.speler.getStrength());
					//RPGMain.speler.increaseStat(1, -Math.abs(7*shirtValue + currentTemperature-8)/100*(double)RPGMain.speler.getDexterity());
					messaged[1] = true;
				}
				// hot
				else if(15*shirtValue + currentTemperature > 45){
					if(!messaged[1]){
						GameFrameCanvas.clothingIcons[1].setVisible(true);
						GameFrameCanvas.clothingIcons[1].setToolTipText("You're sweating heavily with this shirt, and the armour isn't getting any lighter. You're getting a bit short of breath. Strength continually being reduced by " + (15*shirtValue + currentTemperature - 45) + "%, and your reflexes are slowed");
					}
					//TODO balance numbers
					//RPGMain.speler.increaseStat(0, -(15*shirtValue + currentTemperature - 45)/100*(double)RPGMain.speler.getStrength());
					//RPGMain.speler.increaseStat(1, -(15*shirtValue + currentTemperature - 45)/100*(double)RPGMain.speler.getDexterity());
					messaged[1] = true;
				}

				/*
				 * PANTS
				 */
				// cold
				if(7*pantsValue + currentTemperature < 8){
					if(!messaged[2]){
						GameFrameCanvas.clothingIcons[2].setVisible(true);
						GameFrameCanvas.clothingIcons[2].setToolTipText("This cold isn't doing any good for your feet and legs. They're suffering, and it takes more and more effort to take another step. Movement continually being reduced by " + Math.abs(7*pantsValue + currentTemperature-8) + "%.");
					}
					//TODO balance numbers. Right now: decreases until at 33% of original movement
					RPGMain.speler.dimMovement(Math.abs(7*pantsValue + currentTemperature-8)/100);
					messaged[2] = true;
				}
				// hot
				else if(15*pantsValue + currentTemperature > 45){
					if(!messaged[2]){
						GameFrameCanvas.clothingIcons[2].setVisible(true);
						GameFrameCanvas.clothingIcons[2].setToolTipText("Your feet are dry, every step is starting to hurt more. Movement continually being reduced by " + (15*pantsValue + currentTemperature - 45) + "%");
					}
					//TODO balance numbers
					RPGMain.speler.dimMovement((15*pantsValue + currentTemperature - 45)/100);
					messaged[2] = true;
				}
				else{
					RPGMain.speler.dimMovement(-10);
				}
				
				try{
					logger.info("TemperatureChecker sleeping for 10 seconds.");
					sleep(10000);
				} catch(InterruptedException e){
				}
			}
		}
		public void updateTemperature(double t){
			currentTemperature = t;
		}
		public void checkChanges(){
			if(RPGMain.speler.getMantle().getWarmth() != mantleValue){
				messaged[0] = false;
			}
			if(RPGMain.speler.getShirt().getWarmth() != shirtValue){
				messaged[1] = false;
			}			
			if(RPGMain.speler.getPants().getWarmth() != pantsValue){
				messaged[2] = false;
			}
			
			mantleValue = RPGMain.speler.getMantle().getWarmth();
			shirtValue = RPGMain.speler.getShirt().getWarmth();
			pantsValue = RPGMain.speler.getPants().getWarmth();
		}
		public void resetClothingLabels(){
			for(int j=0;j<3;j++){
				if(!messaged[j]){
					JLabel jl = GameFrameCanvas.clothingIcons[j];
					jl.setVisible(false);
					jl.setToolTipText("");
				}
			}
		}
	}
}
