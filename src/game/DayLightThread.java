package game;

import java.io.Serializable;

import org.apache.log4j.Logger;

public class DayLightThread extends Thread implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int hour = 11; // starting hour, sunset at 7pm, sunrise at 7am
	private static final int DAY_MS = 500000;
	private static WeatherSimulator weatherSim = new WeatherSimulator();
	private static HopeSimulator hopeSim = new HopeSimulator();
	
	private static final Logger logger = Logger.getLogger(DayLightThread.class);
	
	public DayLightThread(){
		start();
		
		HopeSimulator.createHopeCenter(new int[]{5,10}, 10, 10, null, false);
	}
	public static WeatherSimulator getWeatherSim(){
		return weatherSim;
	}
	public static HopeSimulator getHopeSim(){
		return hopeSim;
	}
	public void run(){
		while(!RPGMain.speler.checkDood()) {
			
			updateHour();
			System.out.println("Hour " + hour);
			try{
				sleep(DAY_MS/24);
			} catch(InterruptedException e){
			}
			RPGMain.speler.addFitness(-5);
			RPGMain.speler.addHunger(1);
			RPGMain.speler.addThirst(2);
		}
	}
	public static void giveDescriptiveHour(){
		if(hour < 5){
			RPGMain.printText(true, "It is the middle of the night, and a pitchblack night.");
		}
		else if(hour < 8){
			RPGMain.printText(true, "It is the early morning, and the world is ready to awake.");
		}
		else if(hour < 12){
			RPGMain.printText(true, "It is morning, and the sun is on its way to the south.");
		}
		else if(hour < 15){
			RPGMain.printText(true, "It is noon, and the sun rises high in the sky.");
		}
		else if(hour < 19){
			RPGMain.printText(true, "It is afternoon, and the sun is soon leaving the world again.");
		}
		else if(hour < 25){
			RPGMain.printText(true, "It is evening. The sun has left the world again, and its inhabitants are preparing for the night.");
		}
		Global.pauseProg(2000);
		
	}
	@SuppressWarnings("unused")
	public static void updateHour(){
		
		int[] playerPos = RPGMain.speler.getCurrentPosition();
		double oldLightIntensity = WeatherSimulator.getSolarIntensity(playerPos[0], playerPos[1]);
		
		//regulate hope
		hopeSim.createRandomHopeEvent();
		hopeSim.calculateHope();
		hopeSim.checkTermination();
		hopeSim.checkHopeImpact();
		
		//regulate weather
		weatherSim.calcTemperatureChange();
		weatherSim.calculatePressurePoints();
		weatherSim.calculatePressurePointMovement();
		weatherSim.calculateCloudMatterDevelopment();
		weatherSim.calculateCloudMovement();
		weatherSim.calculateRainFall();
		WeatherSimulator.determineWeatherSounds();
		
		double newLightIntensity = WeatherSimulator.getSolarIntensity(playerPos[0], playerPos[1]);
		
		if(newLightIntensity*oldLightIntensity < 0){
			if(newLightIntensity > 0){
				RPGMain.printEnvironmentInfo(true, "The sun has come up.", "darkblue");
			}
			else{
				RPGMain.printEnvironmentInfo(true, "The sun has passed beyond the horizon and darkness is falling.", "darkblue");
			}
		}
		
		//dynamically update drawing radius
		GameFrameCanvas.dungeonMap.repaint();
		
		RPGMain.speler.decrFoodFreshness();
		
		// change time in GUI text box
		String am_pm;
		hour++;
		hour%=24;
		if(hour == 0) RPGMain.speler.incrDaysPlayed();
		if(hour>12) am_pm = "PM";
		else am_pm = "AM";
		
		logger.info("Hour: " + hour);
		// change textbox to hour%12 + am_pm
	}
	public static void rest(int hours){
		for(int j = 0;j<hours;j++)
			updateHour();
	}
	public static int getHour(){
		return hour;
	}
	public static int getDayLength(){
		return DAY_MS;
	}
}
