package game;

import game.HostileArea.DungeonRoom;
import game.Town.District;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import org.apache.log4j.Logger;

public class MapArea extends JPanel implements KeyListener{
	
	private static final long serialVersionUID = 4489659904448610752L;
	private int width,height;
	private int currentLevel;
	private Color[] colors,colorsDesert,colorsForest,colorsMountains,colorsIce;
	private Color mobColor,npcColor,dungeonColor,herbColor;
	private HashMap<Integer,int[][]> elevation;
	private int[][] visible = new int[1][1];
	private int[] playerPos = new int[2];
	private int[] extraPlayerMovement = new int[2];
	private int[] locPos;
	private ArrayList<int[]> enemies = new ArrayList<int[]>();
	private ArrayList<int[]> npcs = new ArrayList<int[]>();
	private ArrayList<int[]> water = new ArrayList<int[]>();
	private ArrayList<int[]> events = new ArrayList<int[]>();
	private ArrayList<int[]> herbs = new ArrayList<int[]>();
	private int radius = 1;
	private int drawRadius = radius;
	private boolean drawCity = false;
	private int districtPixelRadius = 13;
	private int[][] districts;
	private int progress;
	private int[] direction;
	private boolean heightSpeedInfluence, outOfBounds;

	private HashMap<String, WalkEvent> walkEvents;
	
	private static final Logger logger = Logger.getLogger(MapArea.class);
	
	public MapArea(int width,int height){
		setLayout(null);
		setBackground(Color.black);
		setPreferredSize(new Dimension(width,height));
		colorsDesert = new Color[10];
		colorsForest = new Color[10];
		colorsMountains = new Color[10];
		colorsIce = new Color[10];
		
		colorsDesert[0] = new Color(232,213,172);
		colorsDesert[1] = new Color(232,200,132);
		colorsDesert[2] = new Color(223,179,84);
		colorsDesert[3] = new Color(190,141,34);
		colorsDesert[4] = new Color(141,103,20);
		colorsDesert[5] = new Color(75,45,9);
		colorsDesert[6] = new Color(70,31,7);
		colorsDesert[7] = new Color(64,20,2);
		colorsDesert[8] = new Color(35,14,6);
		colorsDesert[9] = new Color(22,11,6);
		
		//TODO better colours, softer
		colorsForest[0] = new Color(0,49,0);
		colorsForest[1] = new Color(40,90,0);
		colorsForest[2] = new Color(75,125,25);
		colorsForest[3] = new Color(100,175,30);
		colorsForest[4] = new Color(125,215,35);
		colorsForest[5] = new Color(215,225,30);
		colorsForest[6] = new Color(185,150,35);
		colorsForest[7] = new Color(185,100,0);
		colorsForest[8] = new Color(175,15,15);
		colorsForest[9] = new Color(255,30,15);
		
		colorsMountains[0] = new Color(28,57,31);
		colorsMountains[1] = new Color(47,78,50);
		colorsMountains[2] = new Color(60,78,62);
		colorsMountains[3] = new Color(60,68,60);
		colorsMountains[4] = new Color(86,91,84);
		colorsMountains[5] = new Color(117,124,116);
		colorsMountains[6] = new Color(144,149,143);
		colorsMountains[7] = new Color(178,188,190);
		colorsMountains[8] = new Color(210,225,224);
		colorsMountains[9] = new Color(240,240,240);
		mobColor = new Color(255,40,255);
		npcColor = new Color(115,225,210);
		dungeonColor = new Color(100,50,0);
		herbColor = Color.orange;
	}
	
	
	public void initCityMap(District[][] districts, int[] playerPos){
		width = districts.length;
		height = 0;
		for(int j=0;j<width;j++){
			try{
				if(height < districts[j].length){
					height = districts[j].length;
				}
			} catch(NullPointerException e){
				continue;
			}
		}
		
		this.districts = new int[width][height];
		
		logger.debug("width: " + width + " height: " + height);
		
		for(int j=0;j<width;j++){
			for(int k=0;k<height;k++){
				if(districts[j][k] != null){
					this.districts[j][k] = 1;
					if(playerPos[0] == j && playerPos[1] == k){
						this.districts[j][k] = 2;
					}
				}
			}
		}
		drawCity = true;
		
	}
	public void initDungeonMap(HashMap<Integer,DungeonRoom[][]> dungeon, int[] positie){
		drawCity = false;
		width = dungeon.get(0).length;
		height = dungeon.get(0)[0].length;
		elevation = new HashMap<Integer,int[][]>();
		visible = new int[width][height];
		enemies.clear();
		npcs.clear();
		water.clear();
		events.clear();
		herbs.clear();
		for(int level: dungeon.keySet()){
			elevation.put(level, new int[width][height]);
			for(int k=0;k<height;k++){
				for(int j=0;j<width;j++){
					try{
						elevation.get(level)[j][k] = dungeon.get(level)[j][k].getHeight();
						if(dungeon.get(level)[j][k].hasAliveEnemies()){
							enemies.add(new int[] {j,k,level});
						}
						if(dungeon.get(level)[j][k].getEvent() != null && dungeon.get(level)[j][k].getEvent().getNPC() != null){
							npcs.add(new int[] {j,k,level});
						}
						if(dungeon.get(level)[j][k].getTerrainType().equalsIgnoreCase("water")){
							water.add(new int[] {j,k,level});
						}
						if(dungeon.get(level)[j][k].getHerb() != null){
							herbs.add(new int[]{j,k,level});
						}
						try{
							if(dungeon.get(level)[j][k].getEvent() != null && !dungeon.get(level)[j][k].getEvent().getType().equalsIgnoreCase("lost")
									&& !dungeon.get(level)[j][k].getEvent().getType().equalsIgnoreCase("trap")){
								events.add(new int[] {j,k,level});
							}
						} catch(NullPointerException e){
							//System.err.println("Nullpointer at " + j + "," + k + "," + level);
						}
					} catch(NullPointerException e){
						//walls of dungeon are high so you can never look around the corner
						elevation.get(level)[j][k] = 10;
						//System.out.print("wall ");
					}
				}
			}
		}
		String type = dungeon.get(0)[0][0].getType();
		if(type.equalsIgnoreCase("loofbos")){
			//TODO testing purpuse radius and colour
			radius = 4;
			colors = colorsForest;
		}
		else if(type.equalsIgnoreCase("naaldbos")){
			radius = 6;
			colors = colorsForest;
		}
		else if(type.equalsIgnoreCase("plains")){
			radius = 6;
			colors = colorsForest;
		}
		
		locPos = new int[2];
		
		logger.debug("locPos:" + locPos[0] + "," + locPos[1] + "; positie:" + positie[0] + "," + positie[1]);
		
		this.locPos[0] = positie[0];
		this.locPos[1] = positie[1];
		extraPlayerMovement = new int[]{2,2};
		
		direction = new int[2];
		
		heightSpeedInfluence = true;
	
	}
	public void setMobPositions(ArrayList<int[]> a){
		enemies.clear();
		for(int[] i:a){
			enemies.add(i);
		}
		repaint();
	}
	public void addHerb(int[] a){
		herbs.add(a);
		repaint();
	}
	public void removeHerb(int[] a){
		for(int[] i: herbs){
			if(i[0] == a[0] && i[1] == a[1] && i[2] == a[2]){
				herbs.remove(i);
				break;
			}
		}
		repaint();
	}
	public void updatePlayerPos(int[] pos, int[] direction,int currentLevel){
		playerPos = pos;
		this.currentLevel = currentLevel;
		int newRadius = Math.max(drawRadius+1, 4);
		//+/-1 is to prevent system from bugging out when you do an event in this square and go in the opposite direction you came from afterwards,
		// since you never have the required condition anymore unless you go back down
		if(!outOfBounds){
			if(direction[0] == -1){
				extraPlayerMovement[0] = (int)(getWidth()/(2.0*newRadius+1.0))-1;
			}
			else if(direction[0] == 1){
				extraPlayerMovement[0] = 1;
			}
			else if(direction[1] == -1){
				extraPlayerMovement[1] = (int)(getHeight()/(2.0*newRadius+1.0))-1;
			}
			else if(direction[1] == 1){
				extraPlayerMovement[1] = 1;
			}
			logger.debug("in updatePlayerPosition");
		}
		repaint();
	}
	
	public Color[] getCurrentColorSet(){
		return colors;
	}
	
	public int getDungeonLevel(){
		return currentLevel;
	}
	
	public int[][] getElevation(){
		return elevation.get(currentLevel);
	}
	public void setElevation(int e, int x, int y, int z){
		elevation.get(z)[x][y] = e;
		repaint();
	}
	public int[] getPlayerPosition(){
		return playerPos;
	}
	public Color getNpcColor(){
		return npcColor;
	}
	public Color getDungeonColor(){
		return dungeonColor;
	}
	public Color getEnemyColor(){
		return mobColor;
	}
	public Color getHerbColor(){
		return herbColor;
	}
	
	public void removeEnemy(int[] pos, int layer){
		for(int j=0;j<enemies.size();j++){
			if(enemies.get(j)[0] == pos[0] && enemies.get(j)[1] == pos[1] && layer == enemies.get(j)[2]){
				enemies.remove(j);
				break;
			}
		}
	}
	public void removeEvent(int[] pos, int layer){
		for(int j=0;j<events.size();j++){
			if(events.get(j)[0] == pos[0] && events.get(j)[1] == pos[1] && layer == events.get(j)[2]){
				events.remove(j);
				break;
			}
		}
	}
	
	
	/**
	 * 
	 * DRAW FUNCTIONS FOR HEIGHT MAP
	 * 
	 */
	public void getNewDrawingRadius(){
		
		//reduced vision during night time
		double dayLight = WeatherSimulator.getSolarIntensity(locPos[0], locPos[1]);
		
		if(currentLevel == 0){
			//radiusExt is a bigger radius due to height effects
			int radiusExt = radius;
			//height is all the same in upper/lower layers, so only useful for main map
			//calculate average height around player and compare it
			double height = 0;
			double number = 0;
			for(int j=-radius;j<radius;j++){
				for(int k=-radius;k<radius;k++){
					try{
						height+=elevation.get(0)[playerPos[0] + j][playerPos[1] + k];
						number++;
					}catch(ArrayIndexOutOfBoundsException exc){
						continue;
					}
				}
			}
			double averageHeight = height/number;
			if(elevation.get(0)[playerPos[0]][playerPos[1]] > averageHeight){
				radiusExt = (int)(radius*elevation.get(0)[playerPos[0]][playerPos[1]]/averageHeight);
			}
			
			if(dayLight > 0){
				drawRadius=(int)((0.5+dayLight/1.3)*radiusExt);
			}
			else if(dayLight > -0.3){
				drawRadius=(int)(radiusExt/2.5);
			}
			else if(RPGMain.speler.hasItem("Torch") || RPGMain.speler.hasItem("Lamp")){
				drawRadius=(int)(radiusExt/2.0);
			}
			else{
				drawRadius = 0;
			}
		}
		else{
			drawRadius = radius;
		}
		
		String weather = WeatherSimulator.getWeather(locPos[0], locPos[1]);
		
		double dummyRadius = drawRadius;
		
		if(weather.startsWith("misty")){
			dummyRadius*=0.5;
		}
		else if(weather.startsWith("rain")){
			dummyRadius*=0.8;
		}
		else if(weather.startsWith("snow")){
			dummyRadius*=1.2;
		}
		
		calculateVisibility((int)dummyRadius);
		
		//dummyRadius can be bigger because of reflection of snow
		drawRadius = (int)Math.max(drawRadius, dummyRadius+1);
		
		//logger.debug("Calling getNewDrawRadius. dummyradius: " + dummyRadius + "; drawRadius: " + drawRadius + ". Weather: " + weather + ". Daylight: " + dayLight);
		
	}
	public void drawElevation(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		
		//newRadius is the radius for the entire screen, independent of the actual viewing radius
		int	newRadius = Math.max(drawRadius+1, 4);
		/*int minXSide = Math.min(playerPos[0], width-playerPos[0]-1);
		int minYSide = Math.min(playerPos[1], width-playerPos[0]-1);
		
		newRadius = Math.min(minYSide,Math.min(minXSide, newRadius));*/
		//TODO radius can be bigger than distance from player to the side
		
		//if player is closer to an edge than its drawRadius, middlePos is the middle of the map. 
		int[] middlePos = {Math.max(Math.min(playerPos[0], width-newRadius-1), newRadius),Math.max(Math.min(playerPos[1], height-newRadius-1), newRadius)};

		for(int j=middlePos[0]-newRadius;j<=middlePos[0]+newRadius;j++){
			for(int k=middlePos[1]-newRadius;k<=middlePos[1] + newRadius;k++){
				int x = Math.round((j-(middlePos[0] - newRadius))*this.getWidth()/(2*newRadius+1));
				int y = Math.round((k-(middlePos[1] - newRadius))*this.getHeight()/(2*newRadius+1));
				
				try{
					//visibility check was done with the dummyRadius (see upper function)
					if(visible[j][k] == 2){
						if(currentLevel == 0){
							g.setColor(colors[elevation.get(currentLevel)[j][k]]);
						}
						else{
							g.setColor(dungeonColor);
						}
						if(hasWater(j,k)){
							g.setColor(new Color(69,121,252));
						}
					}
					else if(visible[j][k] == 1){
						if(currentLevel == 0){
							g.setColor(colors[elevation.get(currentLevel)[j][k]].darker().darker());
						}
						else{
							g.setColor(dungeonColor.darker().darker());
						}
						if(hasWater(j,k)){
							g.setColor(new Color(69,121,252).darker().darker());
						}
					}
					else{
						g.setColor(Color.black);
					}
					if(currentLevel != 0 && elevation.get(currentLevel)[j][k] > 9){
						if(currentLevel < 0){
							g.setColor(Color.black);
						}
						else{
							g.setColor(colors[elevation.get(0)[j][k]].darker().darker());
						}
					}
					if(middlePos[0] == playerPos[0] && middlePos[1] == playerPos[1]){
						g.fillRect(x - extraPlayerMovement[0], y - extraPlayerMovement[1],(int)((double)this.getWidth()/(double)(2*newRadius+1))+4, (int)((double)this.getHeight()/(double)(2*newRadius+1))+4);
					}
					else if(direction[1] == 0 && middlePos[0] == playerPos[0]){
						g.fillRect(x - extraPlayerMovement[0], y,(int)((double)this.getWidth()/(double)(2*newRadius+1))+4, (int)((double)this.getHeight()/(double)(2*newRadius+1))+4);
					}
					else if(direction[0] == 0 && middlePos[1] == playerPos[1]){
						g.fillRect(x, y - extraPlayerMovement[1],(int)((double)this.getWidth()/(double)(2*newRadius+1))+4, (int)((double)this.getHeight()/(double)(2*newRadius+1))+4);
					}
					else{
						g.fillRect(x, y, (int)((double)this.getWidth()/(double)(2*newRadius+1))+4, (int)((double)this.getHeight()/(double)(2*newRadius+1))+4);
					}
				}catch(IndexOutOfBoundsException e){	
				}
			}
		}
	}
	
	public void drawRoster(Graphics g){
		g.setColor(Color.black);
		int newRadius = Math.max(drawRadius+1, 4);
		int[] middlePos = {Math.max(Math.min(playerPos[0], width-newRadius-1), newRadius),Math.max(Math.min(playerPos[1], height-newRadius-1), newRadius)};
		
		//vertical lines
		for(int j=0;j<=(2*newRadius+1);j++){
			int pos = Math.round(j*this.getWidth()/(2*newRadius+1));
			if((middlePos[0] == playerPos[0] && middlePos[1] == playerPos[1]) || (direction[1] == 0 && middlePos[0] == playerPos[0])){
				g.drawLine(pos-extraPlayerMovement[0], 0, pos-extraPlayerMovement[0], this.getHeight());
			}
			else{
				g.drawLine(pos, 0, pos, this.getHeight());
			}
		}
		//horizontal lines
		for(int j=0;j<=(2*newRadius+1);j++){
			int pos = Math.round(j*this.getHeight()/(2*newRadius+1));
			if(middlePos[0] == playerPos[0] && middlePos[1] == playerPos[1] || (direction[0] == 0 && middlePos[1] == playerPos[1])){
				g.drawLine(0, pos-extraPlayerMovement[1], this.getWidth(), pos-extraPlayerMovement[1]);
			}

			else{
				g.drawLine(0, pos, this.getWidth(), pos);
			}
		}
	}
	private boolean hasWater(int j,int k){
		for(int l=0;l<water.size();l++){
			if(j == water.get(l)[0] && k == water.get(l)[1] && currentLevel == water.get(l)[2]){
				return true;
			}
		}
		return false;
	}
	public void drawHerbs(Graphics g){
		g.setColor(herbColor);
		
		int newRadius = Math.max(drawRadius+1, 4);
		
		int[] middlePos = {Math.max(Math.min(playerPos[0], width-newRadius-1), newRadius),Math.max(Math.min(playerPos[1], height-newRadius-1), newRadius)};
		
		for(int j=0;j<herbs.size();j++){
			if(Point.distance(playerPos[0], playerPos[1], herbs.get(j)[0], herbs.get(j)[1]) <= radius && currentLevel == herbs.get(j)[2] && visible[herbs.get(j)[0]][herbs.get(j)[1]] == 2){
				int x = Math.round((newRadius + herbs.get(j)[0]-middlePos[0])*this.getWidth()/(2*newRadius+1));
				int y = Math.round((newRadius + herbs.get(j)[1]-middlePos[1])*this.getHeight()/(2*newRadius+1));
				int w = (int)((double)this.getWidth()/(double)(2*newRadius+1)/2.0);
				int h = (int)((double)this.getHeight()/(double)(2*newRadius+1)/2.0);
				if(middlePos[0] == playerPos[0] && middlePos[1] == playerPos[1]){
					g.fillOval(x + w/2 - extraPlayerMovement[0], y + h/2 - extraPlayerMovement[1], w, h);
				}
				else if(direction[1] == 0 && middlePos[0] == playerPos[0]){
					g.fillOval(x + w/2 - extraPlayerMovement[0], y + h/2, w, h);
				}
				else if(direction[0] == 0 && middlePos[1] == playerPos[1]){
					g.fillOval(x + w/2, y + h/2 - extraPlayerMovement[1], w, h);
				}
				else{
					g.fillOval(x + w/2, y + h/2, w, h);
				}
			}
		}
	}
	public void drawNPCs(Graphics g){
		g.setColor(mobColor);
		
		int newRadius = Math.max(drawRadius+1, 4);
		
		int[] middlePos = {Math.max(Math.min(playerPos[0], width-newRadius-1), newRadius),Math.max(Math.min(playerPos[1], height-newRadius-1), newRadius)};
		
		for(int j=0;j<enemies.size();j++){
			try{
				//only draw if they are within actual radius (if for instance player climbs a tree)
				if(Point.distance(playerPos[0], playerPos[1], enemies.get(j)[0], enemies.get(j)[1]) <= radius && currentLevel == enemies.get(j)[2] && visible[enemies.get(j)[0]][enemies.get(j)[1]] == 2){
					int x = Math.round((newRadius + enemies.get(j)[0]-middlePos[0])*this.getWidth()/(2*newRadius+1));
					int y = Math.round((newRadius + enemies.get(j)[1]-middlePos[1])*this.getHeight()/(2*newRadius+1));
					int w = (int)((double)this.getWidth()/(double)(2*newRadius+1)/2.0);
					int h = (int)((double)this.getHeight()/(double)(2*newRadius+1)/2.0);
					if(middlePos[0] == playerPos[0] && middlePos[1] == playerPos[1]){
						g.fillOval(x + w/2 - extraPlayerMovement[0], y + h/2 - extraPlayerMovement[1], w, h);
					}
					else if(direction[1] == 0 && middlePos[0] == playerPos[0]){
						g.fillOval(x + w/2 - extraPlayerMovement[0], y + h/2, w, h);
					}
					else if(direction[0] == 0 && middlePos[1] == playerPos[1]){
						g.fillOval(x + w/2, y + h/2 - extraPlayerMovement[1], w, h);
					}
					else{
						g.fillOval(x + w/2, y + h/2, w, h);
					}
				}
			}catch(NullPointerException e){
			}
		}
		g.setColor(npcColor);
		for(int j=0;j<npcs.size();j++){
			//only draw if they are within actual radius (if for instance player climbs a tree)
			if(Point.distance(playerPos[0], playerPos[1], npcs.get(j)[0], npcs.get(j)[1]) <= radius && currentLevel == npcs.get(j)[2] && visible[npcs.get(j)[0]][npcs.get(j)[1]] == 2){
				int x = Math.round((newRadius + npcs.get(j)[0] - middlePos[0])*this.getWidth()/(2*newRadius+1));
				int y = Math.round((newRadius + npcs.get(j)[1] - middlePos[1])*this.getHeight()/(2*newRadius+1));
				int w = (int)((double)this.getWidth()/(double)(2*newRadius+1)/2.0);
				int h = (int)((double)this.getHeight()/(double)(2*newRadius+1)/2.0);
				if(middlePos[0] == playerPos[0] && middlePos[1] == playerPos[1]){
					g.fillOval(x + w/2 - extraPlayerMovement[0], y + h/2 - extraPlayerMovement[1], w, h);
				}
				else if(direction[1] == 0 && middlePos[0] == playerPos[0]){
					g.fillOval(x + w/2 - extraPlayerMovement[0], y + h/2, w, h);
				}
				else if(direction[0] == 0 && middlePos[1] == playerPos[1]){
					g.fillOval(x + w/2, y + h/2 - extraPlayerMovement[1], w, h);
				}
				else{
					g.fillOval(x + w/2, y + h/2, w, h);
				}
			}
		}
	}
	public void drawEvents(Graphics g){
		g.setColor(Color.gray);
		
		int newRadius = Math.max(drawRadius+1, 4);
		
		int[] middlePos = {Math.max(Math.min(playerPos[0], width-newRadius-1), newRadius),Math.max(Math.min(playerPos[1], height-newRadius-1), newRadius)};
		
		for(int j=0;j<events.size();j++){
			//only draw if they are within actual radius (if for instance player climbs a tree)
			if(Point.distance(playerPos[0], playerPos[1], events.get(j)[0], events.get(j)[1]) <= radius && currentLevel == events.get(j)[2] && visible[events.get(j)[0]][events.get(j)[1]] == 2){
				int x = Math.round((newRadius + events.get(j)[0] - middlePos[0])*this.getWidth()/(2*newRadius+1));
				int y = Math.round((newRadius + events.get(j)[1] - middlePos[1])*this.getHeight()/(2*newRadius+1));
				int w = (int)((double)this.getWidth()/(double)(2*newRadius+1)/2.0);
				int h = (int)((double)this.getHeight()/(double)(2*newRadius+1)/2.0);
				if(middlePos[0] == playerPos[0] && middlePos[1] == playerPos[1]){
					g.fillOval(x + w/2 - extraPlayerMovement[0], y + h/2 - extraPlayerMovement[1], w, h);
				}
				else if(direction[1] == 0 && middlePos[0] == playerPos[0]){
					g.fillOval(x + w/2 - extraPlayerMovement[0], y + h/2, w, h);
				}
				else if(direction[0] == 0 && middlePos[1] == playerPos[1]){
					g.fillOval(x + w/2, y + h/2 - extraPlayerMovement[1], w, h);
				}
				else{
					g.fillOval(x + w/2, y + h/2, w, h);
				}
			}
		}
	}
	
	public void drawPlayerPos(Graphics g){
		g.setColor(npcColor);
		int newRadius = Math.max(drawRadius+1, 4);
		//TODO radius can be bigger than distance from player to the side
		
		int[] middlePos = {Math.max(Math.min(playerPos[0], width-newRadius-1), newRadius),Math.max(Math.min(playerPos[1], height-newRadius-1), newRadius)};
		if(width == 0)
			return;
		int x = (int)Math.round((double)this.getWidth()/(2.0*newRadius+1.0)*(newRadius - 0.25 -(middlePos[0]-playerPos[0])));
		int y = (int)Math.round((double)this.getHeight()/(2.0*newRadius+1.0)*(newRadius - 0.25 -(middlePos[1]-playerPos[1])));
		int w = (int)(this.getWidth()/(2*newRadius+1)/2.0);
		int h = (int)(this.getHeight()/(2*newRadius+1)/2.0);
	
		if(middlePos[0] == playerPos[0] && middlePos[1] == playerPos[1]){
			g.fillOval(x/* + w*/, y/* + h*/, w, h);
		}
		else if(direction[1] == 0 && middlePos[0] == playerPos[0]){
			g.fillOval(x, y + extraPlayerMovement[1], w, h);
		}
		else if(direction[0] == 0 && middlePos[1] == playerPos[1]){
			g.fillOval(x + extraPlayerMovement[0], y, w, h);
		}
		else{
			g.fillOval(x/* + w*/ + extraPlayerMovement[0], y/* + h*/ + extraPlayerMovement[1], w, h);
		}
	}
	
	public void resetExtraPlayerMovement(){
		int newRadius = Math.max(drawRadius+1, 4);
		double widthRatio = (double)this.getWidth()/(2*newRadius+1);
		double heightRatio = (double)this.getHeight()/(2*newRadius+1);
		extraPlayerMovement = new int[]{(int)(widthRatio/2.0),(int)(heightRatio/2.0)};
		repaint();
	}
	public void setHeightSpeedInfluence(boolean b){
		heightSpeedInfluence = b;
	}
	
	public void movePlayer(int[] x){
		int newRadius = Math.max(drawRadius+1, 4);
		double widthRatio = (double)this.getWidth()/(2*newRadius+1);
		double heightRatio = (double)this.getHeight()/(2*newRadius+1);
		double speed = 1.0;
		
		direction = x;
		
		if(RPGMain.speler.getMovementMode().equalsIgnoreCase("running")){
			speed = 2.0;
		}
		
		if(currentLevel == 0 && heightSpeedInfluence){
			try{
				double heightDiff = elevation.get(0)[playerPos[0]][playerPos[1]] - elevation.get(0)[playerPos[0] + x[0]][playerPos[1] + x[1]];
				speed*=(1.0+heightDiff/2.0);
				speed = Math.max(0.25, speed);
			} catch(ArrayIndexOutOfBoundsException e){
			}
		}
		//TODO take weight and damage to legs into account
		int mod = Math.max(1, (int)(1000.0/(widthRatio*HostileArea.KEYSLEEPPERIOD*speed)));
		
		if(progress%mod == 0 && elevation.get(currentLevel)[playerPos[0] + (int)(extraPlayerMovement[0]/widthRatio)][playerPos[1] + (int)(extraPlayerMovement[1]/heightRatio)] != 10){
			outOfBounds = false;
			//moving in x direction
			if(x[1] == 0){
				extraPlayerMovement[0]+=x[0];
			}
			//moving in y direction
			else if(x[0] == 0){
				extraPlayerMovement[1]+=x[1];
			}
			
			//if player is across the border of his square
			if((x[0] == -1 && extraPlayerMovement[0] == 0) || (x[0] == 1 && Math.abs(extraPlayerMovement[0]/widthRatio) >= 1) || (x[1] == -1 && extraPlayerMovement[1] == 0) || (x[1] == 1 && Math.abs(extraPlayerMovement[1]/heightRatio) >= 1)){
				try{
					//when on the normal play area, level 0
					if(elevation.get(currentLevel)[playerPos[0] + x[0]][playerPos[1] + x[1]] != 10){
						String random = "";
						for(int j=0;j<10;j++){
							random+=Global.generator.nextInt(10);
						}
						HostileArea.setKeyCode(random);
						
						RPGMain.recieveMessage(random);
					}
					//when in dungeon, bump into wall
					else{
						extraPlayerMovement[0]-=x[0];
						extraPlayerMovement[1]-=x[1];
					}
				//when out of bounds on the map, moving to next area
				} catch(ArrayIndexOutOfBoundsException e){
					outOfBounds = true;
					extraPlayerMovement[0]-=x[0];
					extraPlayerMovement[1]-=x[1];
					String random = "Random";
					HostileArea.setKeyCode(random);
					
					RPGMain.recieveMessage(random);
				}
			}
		}
		repaint();
		progress++;
	}
	
	public void setPlayerMovement(int[] x){
		//amount of pixels per square
		int newRadius = Math.max(drawRadius+1, 4);
		double widthRatio = (double)this.getWidth()/(2*newRadius+1);
		double heightRatio = (double)this.getHeight()/(2*newRadius+1);
		double speed = 1.0;
		boolean[] addedEvents = new boolean[8];
		
		double miliseconds = 7000;
		
		addKeyListener(this);
		requestFocus();
		
		if(RPGMain.speler.getMovementMode().equalsIgnoreCase("running")){
			speed = 2.0;
		}
		
		GameFrameCanvas.textField.setEditable(false);
		
		boolean addedNoiseCentre = false;
		
		walkEvents = new HashMap<String,WalkEvent>();
		
		//moving in the x direction
		if(x[1] == 0){
			for(int j=0;j<widthRatio;j++){
				//move 1 pixel
				extraPlayerMovement[0]+=x[0];
				try{
					// 2/speed seconds in total
					Thread.sleep((int)(miliseconds/widthRatio/speed));
					
					calculateNoiseLevel(widthRatio, speed, addedNoiseCentre, j, miliseconds);
					
					int direction = Global.generator.nextInt(8);
					//logger.debug("probability: " + 0.5/widthRatio);
					if(j < widthRatio/2.0 && j%3 == 0){
						if(!addedEvents[direction] && Math.random() < 0.25){
							logger.debug("Adding event for " + direction);
							addEvent(direction);
							addedEvents[direction] = true;
						}
					}
				} catch(InterruptedException e){
				}
				repaint();
			}
		}
		//moving in the y direction
		else{
			for(int j=0;j<heightRatio;j++){
				//move 1 pixel
				extraPlayerMovement[1]+=x[1];
				try{
					Thread.sleep((int)(miliseconds/heightRatio/speed));
					
					calculateNoiseLevel(heightRatio, speed, addedNoiseCentre, j, miliseconds);
					
					int direction = Global.generator.nextInt(8);
					if(!addedEvents[direction] && Math.random() < 0.25/heightRatio){
						logger.debug("Adding event for " + direction);
						addEvent(direction);
						addedEvents[direction] = true;
					}
				} catch(InterruptedException e){
				}
				repaint();
			}
		}
		//set textfield editable and focused
		GameFrameCanvas.textField.setEditable(true);
		GameFrameCanvas.textField.requestFocus();
		
		//slide noiselevel back to low values
		float noiseLevel = GameFrameCanvas.noiseMeter.getNoiseLevel()-0.15f;
		for(int j=30;j>0;j--){
			GameFrameCanvas.noiseMeter.addNoiseLevel(-noiseLevel/30.0f);
			try {
				Thread.sleep((int)(500.0/30.0));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		extraPlayerMovement[0] = 0;
		extraPlayerMovement[1] = 0;
	}
	
	/**
	 * 	DRAW FUNCTION FOR CITY
	 */
	
	public void drawCity(Graphics g){
		int xUnit = getWidth()/(width+1);
		int yUnit = getHeight()/(height+1);
		
		//TODO change radius of the dot depending on how many buildings/people there are, change colour if it has a gate etc
		for(int j=0;j<width;j++){
			for(int k=0;k<height;k++){
				if(districts[j][k] == 1){
					g.setColor(Color.gray);
					g.fillOval((j+1)*xUnit-districtPixelRadius/2, (k+1)*yUnit-districtPixelRadius/2, 2*districtPixelRadius, 2*districtPixelRadius);
				}
				else if(districts[j][k] == 2){
					g.setColor(Color.cyan);
					g.fillOval((j+1)*xUnit-districtPixelRadius/2, (k+1)*yUnit-districtPixelRadius/2, 2*districtPixelRadius, 2*districtPixelRadius);
				}
			}
		}
		int[] currentPos = RPGMain.speler.getCurrentPosition();
		Graphics2D g2d = (Graphics2D)g;
		AffineTransform saveAT = g2d.getTransform();
		FontMetrics fm = getFontMetrics(getFont());
		int width = 0;
		String message = "";
		Town t = (Town)Data.wereld[currentPos[0]][currentPos[1]];
		
		g2d.setColor(Color.white);
		try{
			message = "N: " + Data.wereld[currentPos[0]][currentPos[1]-1].getName();
			width = fm.stringWidth(message);
			if(t.hasGate("North")){
				g2d.setColor(Color.green);
			}
			g2d.drawString(message, (int)(getWidth()/2.0 - width/2.0) , 10);
		} catch(NullPointerException e){
		} catch(ArrayIndexOutOfBoundsException e){
		}
		g2d.setColor(Color.white);
		try{
			message = "S: " + Data.wereld[currentPos[0]][currentPos[1]+1].getName();
			width = fm.stringWidth(message);
			if(t.hasGate("South")){
				g2d.setColor(Color.green);
			}
			g2d.drawString(message,(int)(getWidth()/2.0 - width/2.0) , getHeight() - 10);
		} catch(NullPointerException e){
		} catch(ArrayIndexOutOfBoundsException e){
		}
		g2d.setColor(Color.white);
		g2d.rotate(Math.PI/2.0);
		try{
			message = "W: " + Data.wereld[currentPos[0]-1][currentPos[1]].getName();
			width = fm.stringWidth(message);
			if(t.hasGate("West")){
				g2d.setColor(Color.green);
			}
			g2d.drawString(message,(int)(getHeight()/2.0 - width/2.0), - 10);
		} catch(NullPointerException e){
		} catch(ArrayIndexOutOfBoundsException e){
		}
		g2d.setColor(Color.white);
		g2d.rotate(-Math.PI);
		try{
			message = "E: " + Data.wereld[currentPos[0]+1][currentPos[1]].getName();
			width = fm.stringWidth(message);
			if(t.hasGate("East")){
				g2d.setColor(Color.green);
			}
			g2d.drawString(message,(int)(getHeight()/2.0 - width/2.0) - getWidth(), getWidth() - 10);
		} catch(NullPointerException e){
		} catch(ArrayIndexOutOfBoundsException e){
		}
		g2d.setTransform(saveAT);
	}
	
	public void setPlayerDistrictPosition(int[] newPos){
		for(int j=0;j<width;j++){
			for(int k=0;k<height;k++){
				if(districts[j][k] == 2 && !(j == newPos[0] && k == newPos[1])){
					districts[j][k] = 1;
				}
				else if(j == newPos[0] && k == newPos[1]){
					districts[j][k] = 2;
				}
			}
		}
		repaint();
	}
	
	public void calculateNoiseLevel(double ratio, double speed, boolean addedNoiseCentre, int j, double miliseconds){
		double t = miliseconds/speed/ratio*j;
		double w = 2.0*Math.PI/(miliseconds/speed/8.0);
		if(RPGMain.speler.getMovementMode().equalsIgnoreCase("walking")){
			GameFrameCanvas.noiseMeter.setNoiseLevel((float)Math.cos(w*t)*0.05f + 0.5f);
		}
		else if(RPGMain.speler.getMovementMode().equalsIgnoreCase("running")){
			GameFrameCanvas.noiseMeter.setNoiseLevel((float)Math.cos(w*t)*0.10f + 0.7f);
		}
		else{
			GameFrameCanvas.noiseMeter.setNoiseLevel((float)Math.cos(w*t)*0.05f + 0.1f);
		}
		if(j%5 == 0 && !addedNoiseCentre){
			double detectionProb = Math.pow(GameFrameCanvas.noiseMeter.getNoiseLevel(),2.0)/(ratio/5.0);
			if(Math.random() < detectionProb){
				int[] currentPos = RPGMain.speler.getCurrentPosition();
				HostileArea currentHA = (HostileArea)Data.wereld[currentPos[0]][currentPos[1]];
				currentHA.setPlayerNoiseCentre(new int[]{playerPos[0],playerPos[1],(int)Math.ceil(5.0*GameFrameCanvas.noiseMeter.getNoiseLevel())});
				addedNoiseCentre = true;
			}
		}
	}
	
	private void addEvent(int direction){
		//pick an event type, then make the event
		String type = "Test";
		
		String[] keys = {"Z","E","D","C","X","W","Q","A"};
		
		walkEvents.put(keys[direction], new WalkEvent(type));
		
		GameFrameCanvas.walkIcons[direction].setVisible(true);
		
		logger.debug("JLabel visible: " + GameFrameCanvas.walkIcons[direction].isVisible());
		
		int delay = (int)(1500*(Math.random()+1.0));
		TaskPerformer taskPerformer = new TaskPerformer(direction);
		Timer timer = new Timer(delay, taskPerformer);
		timer.setRepeats(false);
		timer.start();
		
	}
	
	private class TaskPerformer implements ActionListener{
		
		private int direction;
		
		public TaskPerformer(int direction){
			this.direction = direction;
		}
		public void actionPerformed(ActionEvent e) {
			GameFrameCanvas.walkIcons[direction].setVisible(false);
			logger.debug("JLabel set to invisible for dir " + direction);
		}
	}
	
	public void calculateVisibility(int radius){
		
		//mark all spots previously visited as 1
		for(int j=0;j<width;j++){
			for(int k=0;k<height;k++){
				if(visible[j][k] == 2){
					visible[j][k] = 1;
				}
			}
		}
		//mark spots in current line of sight as 2
		for(int k=-radius;k<=radius;k++){
			for(int j=-radius;j<=radius;j++){
				try{
					boolean isVisible = false;
					
					if(Math.sqrt(j*j+k*k) > radius){
						isVisible = false;
					}
					else{
						//determine which squares are in line of sight
						//the origin is in the playerPos
						//set 1 is in the plane x-y
						double rico1;
						//set 2 is in the plane (player->point)-z
						double rico2 = (double)(elevation.get(currentLevel)[playerPos[0] + j][playerPos[1] + k]-elevation.get(currentLevel)[playerPos[0]][playerPos[1]])/(double)(Math.sqrt(j*j + k*k));
						
						if(j==0){
							for(int l=0;l<=Math.abs(k);l++){
								if(((double)elevation.get(currentLevel)[playerPos[0]][playerPos[1] + l*(int)Math.signum(k)] - (double)(rico2*l) - (double)elevation.get(currentLevel)[playerPos[0]][playerPos[1]]) < 0.3){
									isVisible = true;
								}
								else{
									isVisible = false;
									break;
								}
							}
							if(k==0){
								isVisible = true;
							}
						}
						else{
							if(Math.abs(k) > Math.abs(j)){
								rico1 = (double)j/(double)k;
								for(int l=0;l<=Math.abs(k);l++){
									if(((double)elevation.get(currentLevel)[playerPos[0] + (int)(rico1*(double)(Math.signum(k)*l))][playerPos[1] + (int)Math.signum(k)*l] - ((rico2*Math.sqrt(l*l + (int)(rico1*(double)l*rico1*(double)l)))+(double)elevation.get(currentLevel)[playerPos[0]][playerPos[1]])) < 0.3){
										isVisible = true;
									}
									else{
										isVisible = false;
										break;
									}
								}
							}
							else{
								rico1 = (double)k/(double)j;
								for(int l=0;l<=Math.abs(j);l++){
									if(((double)elevation.get(currentLevel)[playerPos[0] + (int)Math.signum(j)*l][playerPos[1] + (int)(rico1*(double)(Math.signum(j)*l))] - ((rico2*Math.sqrt(l*l + (int)(rico1*(double)l*rico1*(double)l)))+(double)elevation.get(currentLevel)[playerPos[0]][playerPos[1]])) < 0.3){
										isVisible = true;
									}
									else{
										isVisible = false;
										break;
									}
								}
							}
						}
					}
					//if it is in line of sight and visible, mark as 2
					if(isVisible){
							visible[playerPos[0] + j][playerPos[1] + k] = 2;
					}
				}catch(ArrayIndexOutOfBoundsException exc){
					continue;
				}
			}
		}
	}
	
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		if(drawCity){
			drawCity(g);
		}
		else{
			getNewDrawingRadius();
			drawElevation(g);
			drawRoster(g);
			drawPlayerPos(g);
			drawEvents(g);
			drawNPCs(g);
			drawHerbs(g);
		}
	}
	
	
	/**
	 * 
	 * KEY LISTENER FOR WALKING EVENTS
	 */
	@Override
	public void keyTyped(KeyEvent e) {
	}
	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		
		logger.debug("Key pressed, keyCode: " + key);
		
		switch(key){
		case KeyEvent.VK_Z: if(GameFrameCanvas.walkIcons[0].isVisible()){
								try{
									walkEvents.get("Z").execute();
									GameFrameCanvas.walkIcons[0].setVisible(false);
								} catch(NullPointerException exc){
								}
							}
							break;
		case KeyEvent.VK_E: if(GameFrameCanvas.walkIcons[1].isVisible()){
								try{
									walkEvents.get("E").execute();
									GameFrameCanvas.walkIcons[1].setVisible(false);
								} catch(NullPointerException exc){
								}
							}
							break;
							
		case KeyEvent.VK_D: if(GameFrameCanvas.walkIcons[2].isVisible()){
								try{
									walkEvents.get("D").execute();
									GameFrameCanvas.walkIcons[2].setVisible(false);
								} catch(NullPointerException exc){
								}
							}
							break;
		case KeyEvent.VK_C: if(GameFrameCanvas.walkIcons[3].isVisible()){
								try{
									walkEvents.get("C").execute();
									GameFrameCanvas.walkIcons[3].setVisible(false);
								} catch(NullPointerException exc){
								}
							}
							break;
		case KeyEvent.VK_X: if(GameFrameCanvas.walkIcons[4].isVisible()){
								try{
									walkEvents.get("X").execute();
									GameFrameCanvas.walkIcons[4].setVisible(false);
								} catch(NullPointerException exc){
								}
							}
							break;
		case KeyEvent.VK_W: if(GameFrameCanvas.walkIcons[5].isVisible()){
								try{
									walkEvents.get("W").execute();
									GameFrameCanvas.walkIcons[5].setVisible(false);
								} catch(NullPointerException exc){
								}
							}
							break;
		case KeyEvent.VK_Q: if(GameFrameCanvas.walkIcons[6].isVisible()){
								try{
									walkEvents.get("Q").execute();
									GameFrameCanvas.walkIcons[6].setVisible(false);
								} catch(NullPointerException exc){
								}
							}
							break;
		case KeyEvent.VK_A: if(GameFrameCanvas.walkIcons[7].isVisible()){
								try{
									walkEvents.get("A").execute();
									GameFrameCanvas.walkIcons[7].setVisible(false);
								} catch(NullPointerException exc){
								}
							}
							break;
		case KeyEvent.VK_LEFT: break;
		}
		
	}
	@Override
	public void keyReleased(KeyEvent e) {
	}
	
	/**
	 * 
	 * Class for walking events
	 * 
	 *
	 */
	private class WalkEvent{
		
		private String type;
		
		public WalkEvent(String type){
			this.type = type;
			logger.debug("Created walkEvent type " + type);
		}
		
		public void execute(){
			logger.debug("Executing walkEvent type " + type);
			RPGMain.printText(true, "This is a test event during walking.");
		}
	}
}