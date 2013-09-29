package game;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class NoiseMeter extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Image meter = null;
	private Dimension dim;
	private float noiseLevel;

	public NoiseMeter(String fileName, Dimension dim){
		this.dim = dim;
		try {
			meter = ImageIO.read(new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		noiseLevel = 0.0f;
		setPreferredSize(dim);
		setOpaque(false);
		repaint();
	}
	
	public void setNoiseLevel(float f){
		noiseLevel = f;
		repaint();
	}
	
	public float getNoiseLevel(){
		return noiseLevel;
	}
	
	public void addNoiseLevel(float f){
		noiseLevel+=f;
		repaint();
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		try{
			g.drawImage(meter, 0, 0, (int)(dim.width*noiseLevel), dim.height, 0, 0, (int)(meter.getWidth(null)*noiseLevel), meter.getHeight(null), null);
		} catch(NullPointerException e){
		}
	}
}
