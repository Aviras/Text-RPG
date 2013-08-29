package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

public class Battlefield extends JPanel implements MouseListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Boolean> visibleEnemies = new ArrayList<Boolean>();
	private HashMap<double[][],Double> covers = new HashMap<double[][],Double>();
	private ArrayList<int[]> enemyCoords = new ArrayList<int[]>();
	private int playerTargetIndex = 0;
	private int[] playerCoords = new int[2];
	private boolean atEnemy = false,hasMovedMax = false;
	public static double sizeInMeter = 20.0;
	
	private static Logger logger = Logger.getLogger(Battlefield.class);
	
	public Battlefield(int width, int height){
		addMouseListener(this);
		setLayout(null);
		setBackground(Color.white);
		setPreferredSize(new Dimension(width,height));
		Global.coverSize = (int)(3.5/20.0*width);
	}
	
	public int[] getPlayerCoord(){
		return playerCoords;
	}
	public void setPlayerTargetIndex(int x){
		playerTargetIndex = x;
		repaint();
	}
	public boolean getHasMovedMax(){
		return hasMovedMax;
	}
	
	/**
	 * 
	 * DRAW AND UPDATE BATTLEFIELD
	 *
	 * 
	 */
	public void updateBattlefield(HashMap<double[][],Double> covers,int[] newPlayerCoords,ArrayList<int[]> newEnemyCoords){
		atEnemy = true;
		//will recieve null when enemy is dead
		enemyCoords.clear();
		visibleEnemies.clear();
		if(newEnemyCoords == null){
			atEnemy = false;
		}
		else{
			this.covers = covers;
			playerCoords[0] = newPlayerCoords[0];
			playerCoords[1] = newPlayerCoords[1];
			for(int j=0;j<newEnemyCoords.size();j++){
				enemyCoords.add(newEnemyCoords.get(j));
				visibleEnemies.add(HostileArea.lineOfSight(playerCoords, newEnemyCoords.get(j), covers));
			}
		}
		//System.out.println("Repaint will get called from updateBattlefield. Playercoord: " + playerCoords[0] + ", " + playerCoords[1]);
		repaint();
	}
	public void updateBattlefield(HashMap<double[][],Double> covers, int[] newPlayerCoords,int[] newEnemyCoords,int mobIndex){
		ArrayList<int[]> dummy = Global.cloneArrayList(enemyCoords);
		dummy.set(mobIndex, newEnemyCoords);
		updateBattlefield(covers,newPlayerCoords,dummy);
	}
	public void updateBattlefield(int[] newPlayerCoords){
		logger.info("newPlayerCoords: " + newPlayerCoords[0] + "," + newPlayerCoords[1]);
		ArrayList<int[]> dummy = Global.cloneArrayList(enemyCoords);
		updateBattlefield(covers,newPlayerCoords,dummy);
	}
	public void newTurn(){
		logger.info("Called newTurn()");
		hasMovedMax = false;
	}

	public void setHasMovedMax(boolean b){
		hasMovedMax = b;
	}
	
	public void drawBattlefield(Graphics g){
		//System.out.println("drawBattlefield gets called");
		Color[] colors = GameFrameCanvas.dungeonMap.getCurrentColorSet();
		Color npcColor = GameFrameCanvas.dungeonMap.getNpcColor();
		Color dungeonColor = GameFrameCanvas.dungeonMap.getDungeonColor();
		Color enemyColor = GameFrameCanvas.dungeonMap.getEnemyColor();
		
		int currentLevel = GameFrameCanvas.dungeonMap.getDungeonLevel();
		int[][] elevation = GameFrameCanvas.dungeonMap.getElevation();
		int[] playerPos = GameFrameCanvas.dungeonMap.getPlayerPosition();
		if(currentLevel == 0){
			//MIDDLE
			g.setColor(colors[elevation[playerPos[0]][playerPos[1]]]);
			setBackground(colors[elevation[playerPos[0]][playerPos[1]]]);
			//SOUTH
			try{
				g.setColor(colors[elevation[playerPos[0]][playerPos[1]+1]]);
				g.fillRect(getWidth()/3, 2*getHeight()/3, getWidth()/3, getHeight()/3);
			}catch(ArrayIndexOutOfBoundsException e){
			}
			//WEST
			try{
				g.setColor(colors[elevation[playerPos[0]-1][playerPos[1]]]);
				g.fillRect(0, getHeight()/3, getWidth()/3, getHeight()/3);
			} catch(ArrayIndexOutOfBoundsException e){
			}
			//EAST
			try{
				g.setColor(colors[elevation[playerPos[0]+1][playerPos[1]]]);
				g.fillRect(2*getWidth()/3, getHeight()/3, getWidth()/3, getHeight()/3);
			} catch(ArrayIndexOutOfBoundsException e){
			}
			//NORTH
			try{
				g.setColor(colors[elevation[playerPos[0]][playerPos[1]-1]]);
				g.fillRect(getWidth()/3, 0, getWidth()/3, getHeight()/3);
			} catch(ArrayIndexOutOfBoundsException e){
			}
			//NORTH EAST
			try{
				g.setColor(colors[elevation[playerPos[0]+1][playerPos[1]-1]]);
				g.fillRect(2*getWidth()/3, 0, getWidth()/3, getHeight()/3);
			} catch(ArrayIndexOutOfBoundsException e){
			}
			//NORTH west
			try{
				g.setColor(colors[elevation[playerPos[0]-1][playerPos[1]-1]]);
				g.fillRect(0, 0, getWidth()/3, getHeight()/3);
			} catch(ArrayIndexOutOfBoundsException e){
			}
			//SOUTH EAST
			try{
				g.setColor(colors[elevation[playerPos[0]+1][playerPos[1]+1]]);
				g.fillRect(2*getWidth()/3, 2*getHeight()/3, getWidth()/3, getHeight()/3);
			} catch(ArrayIndexOutOfBoundsException e){
			}
			//SOUTH WEST
			try{
				g.setColor(colors[elevation[playerPos[0]-1][playerPos[1]+1]]);
				g.fillRect(0, 2*getWidth()/3, getWidth()/3, getHeight()/3);
			} catch(ArrayIndexOutOfBoundsException e){
			}
			/*File file;
			try{
				file = new File("Textures/Grass0138_35_S.jpg");
				Image background = ImageIO.read(file);
				g.drawImage(background, 0, 0, this.getWidth(), this.getHeight(), null);
			} catch(Exception e){
				e.printStackTrace();
				logger.error(e);
			}*/
		}
		else{
			g.setColor(dungeonColor);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		//COVERS
		g.setColor(Color.black);
		Set<double[][]> c = covers.keySet();
		Iterator<double[][]> coverPos = c.iterator();
		while(coverPos.hasNext()){
			// key looks like this:
			// posX posY
			// sizX sizY
			g.setColor(Color.black);
			double[][] key = coverPos.next();
			g.fillOval((int)key[0][0] - (int)(Global.coverSize/2*key[1][0]), (int)key[0][1] - (int)(Global.coverSize/2*key[1][1]), (int)(Global.coverSize*key[1][0]), (int)(Global.coverSize*key[1][1]));
			g.setColor(Color.white);
			DecimalFormat df = new DecimalFormat("#.##");
			g.drawString("" + df.format(covers.get(key)),(int)key[0][0] - (int)(Global.coverSize/2*key[1][0]), (int)key[0][1] - (int)(Global.coverSize/2*key[1][1]));
		}
		//PLAYER & ENEMIES
		g.setColor(npcColor);
		//System.out.println("Player coords at drawBattlefield: " + playerCoords[0] + "," + playerCoords[1]);
		g.fillOval(playerCoords[0] - Global.playerSize/2, playerCoords[1] - Global.playerSize/2, Global.playerSize, Global.playerSize);
		if(!hasMovedMax){
			g.setColor(Color.red);
			int movementInPixels = (int)(RPGMain.speler.getMovement()/sizeInMeter*getWidth());
			logger.info("MovementInPixels: " + movementInPixels + "Movement in meters: " + RPGMain.speler.getMovement());
			g.drawOval(playerCoords[0]-movementInPixels, playerCoords[1] - movementInPixels, 2*movementInPixels, 2*movementInPixels);
		}
		for(int j=0;j<enemyCoords.size();j++){
			int[] enemyCoord = enemyCoords.get(j);
			if(visibleEnemies.get(j)){
				if(j == playerTargetIndex){
					g.setColor(Color.GREEN);
				}
				else{
					g.setColor(enemyColor);
				}
				g.fillOval(enemyCoord[0] - Global.playerSize/2, enemyCoord[1] - Global.playerSize/2, Global.playerSize, Global.playerSize);
				g.setColor(Color.black);
				g.drawString("" + (j+1), enemyCoord[0] + Global.playerSize/2, enemyCoord[1] - Global.playerSize/2);
			}
			else{
				g.fillOval(enemyCoord[0], enemyCoord[1], 5, 5);
			}
		}
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		try{
			drawBattlefield(g);
		} catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * MOUSE LISTENER FOR MOVEMENT ON BATTLEFIELD
	 */
	public void mouseClicked(MouseEvent arg0) {
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
	}
	@Override
	public void mousePressed(MouseEvent arg0) {
		if(atEnemy && !hasMovedMax){
			PointerInfo a = MouseInfo.getPointerInfo();
			Point point = new Point(a.getLocation());
			SwingUtilities.convertPointFromScreen(point, this);
			if(point.distance(new Point(playerCoords[0],playerCoords[1])) <= RPGMain.speler.getMovement()/sizeInMeter*getHeight()){
				playerCoords[0] = (int)point.getX();
				playerCoords[1] = (int)point.getY();
			}
		}
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

}
