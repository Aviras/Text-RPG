package game;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Portrait extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8024272791774030091L;
	private Image portraitImage;
	Dimension fullDim,imgDim;
	private int maxBarLength,barLength;
	private int hunger,thirst,fitness;
	private boolean showStats = false;
	
	public Portrait(String fileName,Dimension fullDim,Dimension imgDim){
		try {
			portraitImage = ImageIO.read(new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.fullDim = fullDim;
		this.imgDim = imgDim;
		maxBarLength= fullDim.width - imgDim.width -10;
		barLength = maxBarLength;
		setPreferredSize(fullDim);
		setOpaque(false);
		repaint();
	}
	
	public void setBarLength(float percent){
		barLength=(int)(percent*maxBarLength);
		repaint();
	}
	public void setShowStats(boolean b){
		showStats = b;
	}
	public void setHunger(int x){
		hunger = x;
		repaint();
	}
	public void setThirst(int x){
		thirst = x;
		repaint();
	}
	public void setFitness(int x){
		fitness = x;
		repaint();
	}
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		g.drawImage(portraitImage, 0, 0,imgDim.width,imgDim.height, null);
		g.setColor(Color.red);//background HP bar
		g.fillRect(imgDim.width + 10, imgDim.height/5, maxBarLength, 7);
		g.setColor(Color.green); //green HP bar
		g.fillRect(imgDim.width + 10, imgDim.height/5, barLength, 7);
		if(showStats){
			g.setColor(Color.orange);
			g.drawString("H:" + hunger + "%", imgDim.width + 10, 5*imgDim.height/8);
			g.setColor(Color.cyan);
			g.drawString("T:" + thirst + "%", imgDim.width + 70, 5*imgDim.height/8);
			g.setColor(Color.pink);
			g.drawString("F:" + fitness + "%", imgDim.width + 10, 7*imgDim.height/8);
		}
	}
	
	public void changeImage(String fileName){
		try {
			portraitImage = ImageIO.read(new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		repaint();
	}

}
