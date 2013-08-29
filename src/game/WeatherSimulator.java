package game;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jfree.ui.RefineryUtilities;

public class WeatherSimulator implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static ArrayList<PressurePoint> pressurePoints;
	private static ArrayList<Cloud> clouds;
	private static double[][] worldIntensities;
	private static String weatherAtPlayer;
	private static Weatherplots cp = null;
	private static Weatherplots sp = null;
	private static Weatherplots vp = null;
	private static double w_1 = 2*Math.PI/24; //periodicity of one day
	private static double w_2 = 2*Math.PI/365; //periodicity of one year
	private static double seasonOffset = 3*Math.PI/2; //seasonOffset determines which season to start with, pi/2 is winter, sin = +1
	
	private static Logger logger = Logger.getLogger(WeatherSimulator.class);
	
	public WeatherSimulator(){
		clouds = new ArrayList<Cloud>();
		pressurePoints = new ArrayList<PressurePoint>();
		worldIntensities = new double[Data.wereld.length][Data.wereld[0].length];
		weatherAtPlayer = "clear windstill";
	}
	public static String getWeather(int x, int y){
		
		String weather = "clear";
		double temperature = Data.wereld[x][y].getTemperature();
		
		for(Cloud c:clouds){
			if(new Point(x,y).distance(c.getPosition()[0],c.getPosition()[1]) <= c.getRadius()){
				if(c.getRaining()){
					if(temperature < 0){
						weather = "snow";
					}
					else{
						weather = "rain";
					}
				}
				else if(c.getHeight() - Data.wereld[x][y].getAltitude() < 1){
					weather = "misty";
				}
				else{
					weather = "clouded";
				}
			}
		}
		
		double[] windSpeed = new double[2];
		for(PressurePoint p:pressurePoints){
			double distance = new Point(x,y).distance(p.getPosition()[0], p.getPosition()[1]);
			double value = p.getPressure()/distance;
			windSpeed[0]+=value*((x-p.getPosition()[0])/distance);
			windSpeed[1]+=value*((y-p.getPosition()[1])/distance);
		}
		double windValue = Math.sqrt(Math.pow(windSpeed[0],2) + Math.pow(windSpeed[1], 2));
		//TODO check values
		//System.out.println("Wind: " + windValue);
		if(windValue > 1){
			weather+=" storm";
		}
		else if(windValue > 0.3){
			weather+=" windy";
		}
		else{
			weather+=" windstill";
		}
		
		return weather;
		
	}
	
	public static String getWeatherAtPlayer(){
		return weatherAtPlayer;
	}
	
	public static void getDescriptiveWeather(int x, int y){
		String[] weather = getWeather(x,y).split(" ");
		
		// TEMPERATURE
		double temperature = Data.wereld[x][y].getTemperature();
		if(temperature < 0){
			
		}
		
		// PRECIPITATION
		if(weather[0].equalsIgnoreCase("clear")){
			if(worldIntensities[x][y] > 0){
				RPGMain.printEnvironmentInfo(false, "The skies are clear, and sunlight pierces through the few clouds overhead. The world around you bathes in light.", "green");
			}
			else{
				RPGMain.printEnvironmentInfo(false, "The skies are clear of clouds, and starlight dots the black dome above you. Faint moonlight illuminates you surroundings.", "green");
			}
		}
		else if(weather[0].equalsIgnoreCase("clouded")){
			if(worldIntensities[x][y] > 0){
				RPGMain.printEnvironmentInfo(false, "It is clouded. Some sun rays pierce through the cloud cover, and the world is overlaid by a grey veil.", "green");
			}
			else{
				RPGMain.printEnvironmentInfo(false, "Slumbering clouds hide the moon, and the light it reflects is smeared out in the sky. There is little light to see by.", "green");
			}
		}
		else if(weather[0].equalsIgnoreCase("rain")){
				RPGMain.printEnvironmentInfo(false, "Rain is pouring from the sky, obstructing your view and carrying that typical smell.", "green");
		}
		else if(weather[0].equalsIgnoreCase("snow")){
			if(worldIntensities[x][y] > 0){
				RPGMain.printEnvironmentInfo(false, "All around you nature is coated by a white blanket, conceiling life underneath. Snow crystals are spiralling from the heavens.", "green");
			}
			else{
				RPGMain.printEnvironmentInfo(false, "The white snow blanket around you is efficiently reflecting light. The glow enables you to glance further in the distance.", "green");
			}
		}
		else if(weather[0].equalsIgnoreCase("misty")){
			if(worldIntensities[x][y] > 0){
				RPGMain.printEnvironmentInfo(false, "Clouds are hanging low, and you find yourself surrounded by a wall of fog. Sounds beyond your vision appear muffled.", "green");
			}
			else{
				RPGMain.printEnvironmentInfo(false, "Your vision is obscured by a dense fog, expelling the little light that was still present from your surroundings.", "green");
			}
		}
		
		
		// WIND SPEED
		if(weather[1].equalsIgnoreCase("windstill")){
			RPGMain.printEnvironmentInfo(true, "There is next to no wind.", "green");
		}
		else if(weather[1].equalsIgnoreCase("windy")){
			RPGMain.printEnvironmentInfo(true, "The wind is whistling in your ears, and leaves and sand are exploring the ground in sudden bursts of movement.", "green");
		}
		else if(weather[1].equalsIgnoreCase("storm")){
			RPGMain.printEnvironmentInfo(true, "Heavy winds are picking up, and every so often you need to brace yourself to remain upright. Your hearing is reduced to the constant buzzing and whistling of the wind.", "green");
		}
		
	}
	public static void determineWeatherSounds(){
		int[] playerPos = RPGMain.speler.getCurrentPosition();
		logger.info("Player position: " + playerPos[0] + "," + playerPos[1]);
		String newWeatherAtPlayer = getWeather(playerPos[0],playerPos[1]);
		//System.err.println("Weather at player: " + newWeatherAtPlayer);
		if(!newWeatherAtPlayer.equalsIgnoreCase(weatherAtPlayer)){
			weatherAtPlayer = newWeatherAtPlayer;
			getDescriptiveWeather(playerPos[0],playerPos[1]);
			Global.soundEngine.fadeLines("weather");
			//System.out.println("Calling chooseandplaysound");
			Global.soundEngine.chooseAndPlaySound(weatherAtPlayer, null, worldIntensities[playerPos[0]][playerPos[1]], true, new String[]{"weather"}, 100, 0);
		}
	}
	public static double getSolarIntensity(int x, int y){
		return worldIntensities[x][y];
	}
	public static double calcSolarIntensity(int x, int y){

		//double k = 2*Math.PI/(50*Data.wereld.length); //world map is 1/50 of a wavelength, approx constant over surface
		double sigma_2 = (double)(4*Data.wereld[0].length); //determines fall-off of gaussian in y-direction
		
		try{
			// reset the temperature every 5 days at noon should things get out of hand
			if(DayLightThread.getHour() == 12 && RPGMain.speler.getDaysPlayed()%5 == 0){
				// 40 e^{(y-(4L + 2L*sin(seasonOffset + w_2*days)))^2/(sqrt(2)*sigma)^2}-15
				if(x == 0 && y == 0){
					logger.info("Resetting temperature. Cloud #: " + clouds.size() + "; PP #: " + pressurePoints.size());
				}
				Data.wereld[x][y].setTemperature(40*Math.exp(-Math.pow((y - (4*Data.wereld[0].length + 2.0*Data.wereld[0].length*Math.sin(seasonOffset + w_2*RPGMain.speler.getDaysPlayed())))/(Math.sqrt(2)*sigma_2),2))-15);
			}
		} catch(NullPointerException e){
		}
		double A = 1;
		
		for(Cloud c: clouds){
			if(Point.distance(x, y, c.getPosition()[0], c.getPosition()[1]) <= c.getRadius()){
				A-=c.getMatterAmount()/(Math.pow(c.getRadius(), 2));
			}
		}
		A = Math.max(0.25, A);
		
		double heatTransport = A*Math.sin(w_1*(double)DayLightThread.getHour() - Math.PI/2);
		
		return heatTransport;
	}
	
	public void calcTemperatureChange(){
		worldIntensities = new double[Data.wereld.length][Data.wereld[0].length];
		logger.info("In temp change, hour: " + DayLightThread.getHour() + " days played: " + RPGMain.speler.getDaysPlayed() + " Cloud #: " + clouds.size() + "; PP #: " + pressurePoints.size());
		double[][] temperatures = new double[Data.wereld.length][Data.wereld[0].length];
		double A = 1.5;
		for(int j=0;j<worldIntensities.length;j++){
			for(int k=0;k<worldIntensities[0].length;k++){
				worldIntensities[j][k] = calcSolarIntensity(j,k);
				try{
					Data.wereld[j][k].addTemperature(A*worldIntensities[j][k]);
					//System.out.println(Data.wereld[j][k].getTemperature() + " " + j + "," + k);
					temperatures[j][k] = Data.wereld[j][k].getTemperature();
				} catch(NullPointerException e){
					continue;
				}
			}
		}
		/*try{
			cp.dispose();
			sp.dispose();
			vp.dispose();
		} catch(NullPointerException e){
		}
		cp = new Weatherplots("Temperature, hour " + DayLightThread.getHour(), temperatures);
		cp.pack();
		RefineryUtilities.centerFrameOnScreen(cp);
		cp.setVisible(true);
		
		ArrayList<double[]> cloudPos = new ArrayList<double[]>();
		for(Cloud c: clouds){
			cloudPos.add(c.getPosition());
		}
		
		cloudPos.add(new double[]{RPGMain.speler.getCurrentPosition()[0], RPGMain.speler.getCurrentPosition()[1]});
		
		sp = new Weatherplots("Cloud Position, hour " + DayLightThread.getHour(), cloudPos);
		sp.pack();
		sp.setVisible(true);
		
		HashMap<int[],double[]> velocityField = new HashMap<int[],double[]>();
		
		for(int x=0;x<Data.wereld.length;x++){
			for(int y=0;y<Data.wereld[0].length;y++){
				double[] velocity = new double[2];
				for(PressurePoint p: pressurePoints){
					double[] pressurePosition = p.getPosition();
					double distance = Point.distance(pressurePosition[0], pressurePosition[1], x, y);
					if(distance > 0.2){
					double value = p.getPressure();
						velocity[0] += 1.3*value*(x-pressurePosition[0])/Math.pow(distance,2);
						velocity[1] -= 1.3*value*(y-pressurePosition[1])/Math.pow(distance,2);
					}
				}
				velocityField.put(new int[]{x,y}, velocity);
			}
		}
		
		vp = new Weatherplots("Wind Speeds, hour " + DayLightThread.getHour(), velocityField);
		vp.pack();
		vp.setLocation(1000, 100);
		vp.setVisible(true);*/
	}
	
	public void calculatePressurePoints(){
		
		HashMap<double[],Double> candidatePressurePoints = new HashMap<double[],Double>();
		
		int heatEquator = (int)(4*Data.wereld[0].length + 2*Data.wereld[0].length*Math.sin(w_2*RPGMain.speler.getDaysPlayed() + seasonOffset));
		
		for(int j=0;j<Data.wereld.length;j+=(int)(Data.wereld.length/4.0)){
			for(int k=0;k<Data.wereld[0].length;k+=(int)(Data.wereld[0].length/3.0)){
				// determine pressure, periodical cosine to simulate hadley cells
				double value = Math.cos(10*Math.PI/(2*Data.wereld[0].length/5)*(k-heatEquator));
				if(Math.abs(value) > 0.7){
					candidatePressurePoints.put(new double[] {j,k},value);
				}
			}
		}
		// newly inserted points get checked as well, not efficient and not necessary,
		// so use cloned arraylist in second for-loop
		ArrayList<PressurePoint> clonedPressurePoints = cloneArrayList(pressurePoints);
		for(double[] cPPos: candidatePressurePoints.keySet()){
			boolean alreadyAdded = false;
			for(PressurePoint p: clonedPressurePoints){
				//see if candidate pressure points are close to already existing ones, if so sum their values
				if(Point.distance(p.getPosition()[0],p.getPosition()[1], cPPos[0],cPPos[1]) <= 5){
					p.addPressure(candidatePressurePoints.get(cPPos));
					alreadyAdded = true;
					break;
				}
			}
			//add candidate pressure point that wasn't close to others
			if(!alreadyAdded){
				pressurePoints.add(new PressurePoint(cPPos,candidatePressurePoints.get(cPPos),new double[] {2*Math.random(),2*Math.random()}));
			}
		}
		ArrayList<PressurePoint> delete = new ArrayList<PressurePoint>();
		for(PressurePoint p: pressurePoints){
			if(p.getPressure() < 0.2)
				delete.add(p);
		}
		for(PressurePoint p: delete){
			pressurePoints.remove(p);
		}
		delete.clear();
		//System.err.println("Amount of pressurePoints: " + pressurePoints.size());
		candidatePressurePoints.clear();
	}
	
	public void calculatePressurePointMovement(){
		for(PressurePoint p:pressurePoints){
			p.setPosition(new double[]{p.getPosition()[0] + p.getVelocity()[0],p.getPosition()[1] + p.getVelocity()[1]});
		}

		// check pressurePoints overlap
		for(int i=0;i<2;i++){
			for(int j=0;j<pressurePoints.size();j++){
				for(int k=j+1;k<pressurePoints.size();k++){
					if(Point.distance(pressurePoints.get(j).getPosition()[0], pressurePoints.get(j).getPosition()[1], pressurePoints.get(k).getPosition()[0], pressurePoints.get(k).getPosition()[1]) <= 5){
						pressurePoints.get(j).addPressure(pressurePoints.get(k).getPressure());
						pressurePoints.remove(k);
						k--;
					}
				}
			}
		}
		
	}
	
	public void calculateCloudMatterDevelopment(){
		
		for(int j=0;j<Data.wereld.length;j++){
			for(int k=0;k<Data.wereld[0].length;k++){
				try{
					if(Data.wereld[j][k].getClass().equals(Sea.class) && worldIntensities[j][k] > 0){
						//cast to int floors value, so to height factor-1
						int height = (int)(Math.random()*5.0);
						clouds.add(new Cloud(new double[] {j,k},height,Math.random()+1,(Math.random() + 1.5)*worldIntensities[j][k]));
						//System.out.println("Making a cloud at " + j + "," + k + " with radius " + clouds.get(clouds.size()-1).getRadius() + " at height " + clouds.get(clouds.size()-1).getHeight() + " (lightIntensity: " + worldIntensities[j][k] + ")");
					}
				} catch(NullPointerException e){
					continue;
				}
			}
		}
		logger.info("Clouds size " + clouds.size());
		// check cloud overlap
		for(int l=0;l<2;l++){
			for(int j=0;j<clouds.size();j++){
				for(int k=j+1;k<clouds.size();k++){
					if(Point.distance(clouds.get(j).getPosition()[0], clouds.get(j).getPosition()[1], clouds.get(k).getPosition()[0], clouds.get(k).getPosition()[1]) <= (clouds.get(j).getRadius() + clouds.get(k).getRadius())
							&& Math.abs((clouds.get(j).getHeight() - clouds.get(k).getHeight())) < 3){
						clouds.get(j).addMatterAmount(clouds.get(k).getMatterAmount());
						clouds.get(j).setRadius(Math.min(3,clouds.get(j).getRadius()*1.1));
						//System.out.println("New radius at " + clouds.get(j).getPosition()[0] + "," + clouds.get(j).getPosition()[1] + ": " + clouds.get(j).getRadius());
						clouds.remove(k);
						k--;
					}
				}
			}
		}
		logger.info("Clouds size after overlap check: " + clouds.size());
		
		/*for(Cloud c: clouds){
			//System.out.println("Cloud at " + c.getPosition()[0] + ", " + c.getPosition()[1] + " with radius " + c.getRadius() + " at height " + c.getHeight());
		}*/
		
	}
	
	public void calculateCloudMovement(){
		// use pressurepoints as a 1/r² force field like electrodynamics to calculate velocity
		double[] velocity = new double[2];
		double[] cloudPosition = new double[2];
		ArrayList<Cloud> delete = new ArrayList<Cloud>();
		int m = 0;
		for(Cloud c:clouds){
			cloudPosition = c.getPosition();
			for(PressurePoint p: pressurePoints){
				double[] pressurePosition = p.getPosition();
				double distance = Point.distance(pressurePosition[0], pressurePosition[1], cloudPosition[0], cloudPosition[1]);
				double value = p.getPressure();
				if(m == 0){
					////System.out.println("pressurePoints pos:" + pressurePosition[0] + "," + pressurePosition[1] + ":" + value);
				}
				velocity[0] += 1.3*value*(cloudPosition[0]-pressurePosition[0])/Math.pow(distance,2);
				velocity[1] -= 1.3*value*(cloudPosition[1]-pressurePosition[1])/Math.pow(distance,2);
			}
			m++;
			// clouds move slower over terrain that is higher, and rise to move over them
			// check the entire area under the cloud
			int heightDifference = 0;
			for(int j=-(int)Math.ceil(c.getRadius());j<(int)Math.ceil(c.getRadius());j++){
				for(int k=-(int)Math.ceil(c.getRadius());k<(int)Math.ceil(c.getRadius());k++){
					if(Math.sqrt(j*j + k*k) <= c.getRadius()){
						try{
							int newHeightDifference = Data.wereld[(int)(cloudPosition[0] + Math.ceil(velocity[0])) + j][(int)(cloudPosition[1] + Math.ceil(velocity[1])) + k].getAltitude() - c.getHeight();
							if(newHeightDifference > 0 && newHeightDifference > heightDifference){
								heightDifference = newHeightDifference;
								//System.err.println("Velocity diminished for cloud at " + cloudPosition[0] + ", " + cloudPosition[1] + ". Height: " + c.getHeight());
							}
						}catch(NullPointerException e){
						}catch(ArrayIndexOutOfBoundsException e){
						}
					}
				}
			}
			if(heightDifference > 0){
				velocity[0]/=heightDifference+2;
				velocity[1]/=heightDifference+2;
			}
			////System.out.println("Velocity for " + c.getPosition()[0] + "," + c.getPosition()[1] + ": " + velocity[0] + "," + velocity[1]);
			// within world boundaries
			if(cloudPosition[0] + velocity[0] < Data.wereld.length && cloudPosition[0] + velocity[0] >= 0
					&& cloudPosition[1] + velocity[1] < Data.wereld[0].length && cloudPosition[1] + velocity[1] >= 0){
				////System.out.println("Cloud from " + cloudPosition[0] + "," + cloudPosition[1] + " to " + (cloudPosition[0]+velocity[0]) + "," + (cloudPosition[1]+velocity[1]) + "; Radius:" + c.getRadius() + ";Height: " + c.getHeight());
				c.addPosition(velocity);
			}
			else{
				if(Math.random() < 0.25){
					c.addPosition(new double[]{-velocity[0],-velocity[1]});
				}
				else{
					delete.add(c);
				}
			}
		}
		for(Cloud c:delete){
			clouds.remove(c);
			//System.out.println("Cloud out of bounds at " + c.getPosition()[0] + "," + c.getPosition()[1]);
		}
		logger.info("Clouds size after movement: " + clouds.size());
		delete.clear();
	
	}
	
	public void calculateRainFall(){
		// check rainfall, either due to height difference, or critical value of matter density reached
		// diminish amount of matter, and diminish radius
		ArrayList<Cloud> delete = new ArrayList<Cloud>();
		for(Cloud c: clouds){
			c.setRaining(false);
			// amount of matter per unit of surface (determined by its radius)
			double matterDensity = c.getMatterAmount()/(Math.PI*Math.pow(c.getRadius(), 2));
			////System.out.println("matterDensity for " + c.getPosition()[0] +"," + c.getPosition()[1] + ": " + matterDensity);
			// check to see for height differences within cloud radius
			for(int j=-(int)Math.ceil(c.getRadius());j<=Math.ceil(c.getRadius());j++){
				for(int k=-(int)Math.ceil(c.getRadius());k<=Math.ceil(c.getRadius());k++){
					if(Math.sqrt(k*k + j*j) <= c.getRadius()){
						try{
							int heightDifference = Data.wereld[(int)c.getPosition()[0] + j][(int)c.getPosition()[1] + k].getAltitude() - c.getHeight();
							if(heightDifference > 0 && matterDensity > 0.5){
								c.addMatterAmount(-0.1*c.getMatterAmount()*(heightDifference));
								c.setRadius(0.9*c.getRadius());
								c.setHeight(c.getHeight()+1);
								c.setRaining(true);
								//System.out.println("Raining at " + c.getPosition()[0] + "," + c.getPosition()[1] + " because of height diff. Height difference " + heightDifference);
							}
						}catch(NullPointerException e){
							continue;
						}catch(ArrayIndexOutOfBoundsException e){
							continue;
						}
					}
				}
			}
			
			//check for rainfall due to critical density
			if(matterDensity > 1 && !c.getRaining()){
				c.addMatterAmount(-0.40*c.getMatterAmount());
				c.setRadius(0.90*c.getRadius());
				c.setRaining(true);
				//System.out.println("Raining at " + c.getPosition()[0] + "," + c.getPosition()[1] + " because of crit dens. Height: " + c.getHeight() + ", " + matterDensity);
			}
			else if(matterDensity < 0.2){
				delete.add(c);
			}
		}
		for(Cloud c: delete){
			clouds.remove(c);
		}
		delete.clear();
	}
	
	public static ArrayList<Cloud> getClouds(){
		return clouds;
	}
	public static ArrayList<PressurePoint> getPressurePoints(){
		return pressurePoints;
	}
	public ArrayList<PressurePoint> cloneArrayList(ArrayList<PressurePoint> a){
		ArrayList<PressurePoint> b = new ArrayList<PressurePoint>(a.size());
		for(PressurePoint p:a){
			b.add(new PressurePoint(p));
		}
		return b;
	}
	class PressurePoint{
		
		private double[] position = new double[2];
		private double pressure;
		private double[] velocity = new double[2];
		
		public PressurePoint(double[] position, double pressure, double[] velocity){
			this.position = position;
			this.pressure = pressure;
			this.velocity = velocity;
		}
		public PressurePoint(PressurePoint p){
			this.position = p.position;
			this.pressure = p.pressure;
			this.velocity = p.velocity;
		}
		public void addPressure(double p){
			pressure+=p;
		}
		public void setPosition(double[] x){
			position[0] = x[0];
			position[1] = x[1];
		}
		public double[] getPosition(){
			return position;
		}
		public double getPressure(){
			return pressure;
		}
		public double[] getVelocity(){
			return velocity;
		}
	}
	
	class Cloud{
		
		private double[] position = new double[2];
		private double radius,matterAmount;
		private int height;
		private boolean raining = false;
		
		public Cloud(double[] position, int height, double radius, double matterAmount){
			this.position = position;
			this.height = height;
			this.radius = radius;
			this.matterAmount = matterAmount;
		}
		public double[] getPosition(){
			return position;
		}
		public int getHeight(){
			return height;
		}
		public double getRadius(){
			return radius;
		}
		public double getMatterAmount(){
			return matterAmount;
		}
		public boolean getRaining(){
			return raining;
		}
		public void setRaining(boolean b){
			raining = b;
		}
		public void setRadius(double x){
			radius = x;
		}
		public void setHeight(int x){
			height = x;
		}
		public void addMatterAmount(double x){
			matterAmount+=x;
		}
		public void setPosition(double[] x){
			position[0] = x[0];
			position[1] = x[1];
		}
		public void addPosition(double[] x){
			position[0] += x[0];
			position[1] += x[1];
		}
	}

}
