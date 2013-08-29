package game;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.apache.log4j.Logger;

public class ImagePanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static Image image;
	
	private static final Logger logger = Logger.getLogger(ImagePanel.class);

	public ImagePanel(Dimension dim){
		setLayout(null);
		setBackground(Color.BLACK);
		setBorder(BorderFactory.createBevelBorder(0));
		setPreferredSize(dim);
		
		try{
			File file = new File("Images/Map_test.png");
			image = ImageIO.read(file);
		} catch(IOException e){
			e.printStackTrace();
		}
		repaint();
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		g.drawImage(image, 0, 0,this.getWidth(),this.getHeight(), null);
	}
	
	public void changeImage(String id) throws InterruptedException{
		String[] idComponents = id.split(" ");
		System.out.println(id);
		try{
			if(idComponents[0].equalsIgnoreCase("hostileArea"))
				image = ImageIO.read(new File("Images/" + Data.hostileAreas.get(Integer.parseInt(idComponents[1])).getImage()));
			else if(idComponents[0].equalsIgnoreCase("town"))
				image = ImageIO.read(new File("Images/" + Data.towns.get(Integer.parseInt(idComponents[1])).getImage()));
		} catch(IOException e){
			e.printStackTrace();
			logger.debug(e);
		}
		repaint();
	}
}
