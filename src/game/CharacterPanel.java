package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;

import org.apache.log4j.Logger;

public class CharacterPanel extends JPanel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(CharacterPanel.class);
	private static Canvas canvas;
	
	public CharacterPanel(){
		canvas = new Canvas();
	}
	
	public static void showCharacterPanel(){
		
		JFrame frame = new JFrame("Character Panel");
		
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		canvas.init();
		
		frame.getContentPane().add(canvas);
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	class Canvas extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Image background,body, face;
		private Image[] equipment;
		private JLabel nameL,hpL,strengthL,dexterityL,intellectL,charismaL,archeryL,swordsL,clubsL,axesL;
		private ItemLabel headSlot, chestSlot, glovesSlot, leggingsSlot, bootsSlot, weaponSlot, shieldSlot, bowSlot, mantleSlot, shirtSlot, pantsSlot;
		private JTabbedPane tabbedPane;
		private JPanel statPanel, repPanel, skillsPanel;
		private CharacterImagePanel imagePanel;
		private SpringLayout layout,statLayout,skillsLayout;
		private String w,s,e,n;
		
		public Canvas(){
		}
		
		public void init(){
			
			//setBackground(Color.black);
			
			//setForeground(Color.white);
			
			File file;
			try{
				/*file = new File("Images/CharPanel_BG.png");
				background = ImageIO.read(file);*/
				
				file = new File("Images/Sketch_Figure_Char_Panel.png");
				body = ImageIO.read(file);
			} catch(Exception e){
				e.printStackTrace();
				logger.error(e);
			}
			
			//Read all extra images
			
			
			//positional strings used
			w = SpringLayout.WEST;
			e = SpringLayout.EAST;
			n = SpringLayout.NORTH;
			s = SpringLayout.SOUTH;
			
			layout = new SpringLayout();
			setLayout(layout);
			
			nameL = new JLabel(RPGMain.speler.getName());
			
			/*TYPE
			*0 = geen equipment
			*1 = wapen 1H
			*2 = wapen 2H
			*3 = schild
			*4 = boog 
			*5 = helm
			*6 = harnas
			*7 = handschoenen
			*8 = broek
			*9 = schoenen*/
			
			headSlot = new ItemLabel("Head",RPGMain.speler.getEquipped(5));
			chestSlot = new ItemLabel("Chest",RPGMain.speler.getEquipped(6));
			glovesSlot = new ItemLabel("Gloves",RPGMain.speler.getEquipped(7));
			leggingsSlot = new ItemLabel("Leggings",RPGMain.speler.getEquipped(8));
			bootsSlot = new ItemLabel("Boots",RPGMain.speler.getEquipped(9));
			
			weaponSlot = new ItemLabel("Melee Weapon",RPGMain.speler.getEquipped(1));
			shieldSlot = new ItemLabel("Shield",RPGMain.speler.getEquipped(3));
			bowSlot = new ItemLabel("Ranged Weapon",RPGMain.speler.getEquipped(4));
			
			mantleSlot = new ItemLabel("Coat",RPGMain.speler.getMantle());
			pantsSlot = new ItemLabel("Pants",RPGMain.speler.getPants());
			shirtSlot = new ItemLabel("Sweater",RPGMain.speler.getShirt());
			
			imagePanel = new CharacterImagePanel(150,300,"Images/Sketch_Figure_Char_Panel.png");
			
			tabbedPane = new JTabbedPane();
			
			// STATS Panel
			statPanel = new JPanel();
			statLayout = new SpringLayout();
			
			//statPanel.setBackground(Color.black);
			
			statPanel.setLayout(statLayout);
			
			hpL = new JLabel("HP: " + RPGMain.speler.getHP() + "/" + RPGMain.speler.getMaxHP());
			strengthL = new JLabel("Strength: " + RPGMain.speler.getStrength());
			dexterityL = new JLabel("Dexterity: " + RPGMain.speler.getDexterity());
			intellectL = new JLabel("Intellect: " + RPGMain.speler.getIntellect());
			charismaL = new JLabel("Charisma: " + RPGMain.speler.getCharisma());
			
			hpL.setForeground(Color.green);
			
			/*strengthL.setForeground(Color.white);
			dexterityL.setForeground(Color.white);
			intellectL.setForeground(Color.white);
			charismaL.setForeground(Color.white);*/
			
			statPanel.add(hpL);
			statPanel.add(strengthL);
			statPanel.add(dexterityL);
			statPanel.add(intellectL);
			statPanel.add(charismaL);
			
			// REP panel
			repPanel = new JPanel();
			
			
			// Skills panel
			skillsPanel = new JPanel();
			skillsLayout = new SpringLayout();
			
			//skillsPanel.setBackground(Color.black);
			
			skillsPanel.setLayout(skillsLayout);
			
			archeryL = new JLabel("Archery: " + RPGMain.speler.getArchery());
			swordsL = new JLabel("Swords: " + RPGMain.speler.getSwordSkill());
			clubsL = new JLabel("Clubs: " + RPGMain.speler.getClubSkill());
			axesL = new JLabel("Axes: " + RPGMain.speler.getAxeSkill());
			
			/*archeryL.setForeground(Color.white);
			swordsL.setForeground(Color.white);
			clubsL.setForeground(Color.white);
			axesL.setForeground(Color.white);*/
			
			skillsPanel.add(archeryL);
			skillsPanel.add(swordsL);
			skillsPanel.add(clubsL);
			skillsPanel.add(axesL);
			
			
			tabbedPane.addTab("Stats",statPanel);
			tabbedPane.addTab("Skills",skillsPanel);
			
			// STATS layout
			statLayout.putConstraint(w, hpL, 10, w, statPanel);
			statLayout.putConstraint(n, hpL, 10, n, statPanel);
			statLayout.putConstraint(n, strengthL, 10, s, hpL);
			statLayout.putConstraint(w, strengthL, 0, w, hpL);
			statLayout.putConstraint(n, dexterityL, 10, s, hpL);
			statLayout.putConstraint(w, dexterityL, 100, w, hpL);
			statLayout.putConstraint(n, intellectL, 10, s, strengthL);
			statLayout.putConstraint(w, intellectL, 0, w, hpL);
			statLayout.putConstraint(n, charismaL, 10, s, dexterityL);
			statLayout.putConstraint(w, charismaL, 0, w, dexterityL);
			
			statLayout.putConstraint(e, statPanel, 20, e, dexterityL);
			statLayout.putConstraint(s, statPanel, 20, s, intellectL);
			
			// SKILLS layout
			skillsLayout.putConstraint(w, archeryL, 10, w, skillsPanel);
			skillsLayout.putConstraint(n, archeryL, 10, n, statPanel);
			skillsLayout.putConstraint(n, swordsL, 10, s, archeryL);
			skillsLayout.putConstraint(w, swordsL, 0, w, archeryL);
			skillsLayout.putConstraint(n, clubsL, 0, n, archeryL);
			skillsLayout.putConstraint(w, clubsL, 100, w, archeryL);
			skillsLayout.putConstraint(n, axesL, 10, s, clubsL);
			skillsLayout.putConstraint(w, axesL, 0, w, clubsL);
			
			skillsLayout.putConstraint(e, skillsPanel, 20, e, clubsL);
			skillsLayout.putConstraint(s, skillsPanel, 20, s, swordsL);
			
			
			
			add(nameL);
			
			add(headSlot);
			add(mantleSlot);
			add(chestSlot);
			add(shirtSlot);
			add(glovesSlot);
			add(leggingsSlot);
			add(pantsSlot);
			add(bootsSlot);
			
			add(weaponSlot);
			add(shieldSlot);
			add(bowSlot);
			
			add(imagePanel);
			
			add(tabbedPane);
			
			tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
			
			nameL.setForeground(Color.white);
			
			layout.putConstraint(n, nameL, 10, n, this);
			layout.putConstraint(w, nameL, 40, w, imagePanel);
			
			layout.putConstraint(n, headSlot, 10, s, nameL);
			layout.putConstraint(w, headSlot, 10, w, this);
			layout.putConstraint(n, mantleSlot, 30, s, headSlot);
			layout.putConstraint(w, mantleSlot, 0, w, headSlot);
			layout.putConstraint(n, chestSlot, 30, s, mantleSlot);
			layout.putConstraint(w, chestSlot, 0, w, headSlot);
			layout.putConstraint(n, shirtSlot, 30, s, chestSlot);
			layout.putConstraint(w, shirtSlot, 0, w, headSlot);
			
			layout.putConstraint(n, glovesSlot, 0, n, headSlot);
			layout.putConstraint(w, glovesSlot, 50, e, imagePanel);
			layout.putConstraint(n, leggingsSlot, 30, s, glovesSlot);
			layout.putConstraint(w, leggingsSlot, 0, w, glovesSlot);
			layout.putConstraint(n, pantsSlot, 30, s, leggingsSlot);
			layout.putConstraint(w, pantsSlot, 0, w, glovesSlot);
			layout.putConstraint(n, bootsSlot, 30, s, pantsSlot);
			layout.putConstraint(w, bootsSlot, 0, w, glovesSlot);
		
			layout.putConstraint(n, weaponSlot, 10, s, imagePanel);
			layout.putConstraint(w, weaponSlot, 20, w, headSlot);
			layout.putConstraint(n, shieldSlot, 0, n, weaponSlot);
			layout.putConstraint(w, shieldSlot, 10, e, weaponSlot);
			layout.putConstraint(n, bowSlot, 0, n, weaponSlot);
			layout.putConstraint(w, bowSlot, 10, e, shieldSlot);
			
			layout.putConstraint(n, imagePanel, 20, s, nameL);
			layout.putConstraint(w, imagePanel, 60, e, headSlot);
			
			layout.putConstraint(n, tabbedPane, 20, s, weaponSlot);
			layout.putConstraint(w, tabbedPane, 30, w, this);
			
			layout.putConstraint(s, this, 20, s, tabbedPane);
			layout.putConstraint(e, this, 30, e, glovesSlot);
			
		}
		
		
		public void setEquipment(int[] IDs){
			equipment = new Image[IDs.length];
			//TODO need to enter data in the order of appearance when drawn
			//ordering: bow, quiver, cloak, pants, leggings, shoes, shirt, harness, gloves, helm, sword, shield
		}
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			
			/*try{
				g.drawImage(background, 0, 0, this.getWidth(), this.getHeight(), null);
				g.drawImage(body, 0, 0, this.getWidth(), this.getHeight(), null);
				
				for(Image i:equipment){
					g.drawImage(i, 0, 0, this.getWidth(), this.getHeight(), null);
				}
			} catch(Exception e){
				e.printStackTrace();
				logger.error(e);
			}*/
		}
		
	}
	
	class CharacterImagePanel extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Image image;
		
		public CharacterImagePanel(int width, int height, String fileName){
			setLayout(null);
			setBackground(Color.WHITE);
			setBorder(BorderFactory.createBevelBorder(0));
			setPreferredSize(new Dimension(width, height));
			
			try{
				File file = new File(fileName);
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
	}
	
	class ItemLabel extends JLabel implements MouseListener{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String name,w,n,s,e;
		private Item item;
		private JFrame frame;
		
		public ItemLabel(String name, Item i){
			super(name);
			this.name = name;
			item = i;
			
			addMouseListener(this);
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			
		}

		@Override
		public void mouseEntered(MouseEvent me) {
			System.out.println("Entered " + name + " slot");
			
			HashMap<JLabel,Integer> labelSizes = new HashMap<JLabel,Integer>();
			
			
			//positional strings used
			w = SpringLayout.WEST;
			e = SpringLayout.EAST;
			n = SpringLayout.NORTH;
			s = SpringLayout.SOUTH;
			
			SpringLayout layout = new SpringLayout();
			
			frame = new JFrame(name);
			
			frame.setUndecorated(true);
			
			frame.setResizable(false);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			JPanel panel = new JPanel();
			
			panel.setLayout(layout);
			
			JLabel nameL = new JLabel(item.getName());
			
			nameL.setForeground(Color.white);
			
			panel.add(nameL);
			
			labelSizes.put(nameL,nameL.getText().length());
		
			layout.putConstraint(n, nameL, 10, n, panel);
			layout.putConstraint(w, nameL, 10, w, panel);
			
			if(!item.getName().equalsIgnoreCase("nothing")){
				JLabel weightL = new JLabel("Weight: " + item.getWeight() + " kg");
				weightL.setForeground(Color.white);
				
				panel.add(weightL);
				
				if(item instanceof Equipment){
					Equipment eq = (Equipment)item;
					
					JLabel strengthL = new JLabel("Strength: " + eq.getStrength());
					strengthL.setForeground(Color.white);
					labelSizes.put(strengthL, strengthL.getText().length());
					
					JLabel durabilityL = new JLabel("Durability: " + eq.getKwaliteit() + "/" + eq.getMaxKwaliteit());
					durabilityL.setForeground(Color.white);
					labelSizes.put(durabilityL, durabilityL.getText().length());
					
					JLabel typeL = new JLabel(eq.getFullType());
					typeL.setForeground(Color.white);
					labelSizes.put(typeL, typeL.getText().length());
					
					panel.add(strengthL);
					panel.add(typeL);
					panel.add(durabilityL);
					
					layout.putConstraint(n, typeL, 10, s, nameL);
					layout.putConstraint(w, typeL, 0, w, nameL);
					layout.putConstraint(n, strengthL, 10, s, typeL);
					layout.putConstraint(w, strengthL, 0, w, nameL);
					layout.putConstraint(n, durabilityL, 10, s, strengthL);
					layout.putConstraint(w, durabilityL, 0, w, nameL);
					layout.putConstraint(n, weightL, 10, s, durabilityL);
					layout.putConstraint(w, weightL, 0, w, nameL);
					

				}
				else if(item instanceof Clothing){
					Clothing cl = (Clothing)item;
					
					JLabel areaL = new JLabel(cl.getArea());
					areaL.setForeground(Color.white);
					labelSizes.put(areaL, areaL.getText().length());
					
					JLabel warmthL = new JLabel("Warmth: " + cl.getWarmth());
					warmthL.setForeground(Color.white);
					labelSizes.put(warmthL, warmthL.getText().length());
					
					panel.add(areaL);
					panel.add(warmthL);
					
					layout.putConstraint(n, areaL, 10, s, nameL);
					layout.putConstraint(w, areaL, 0, w, nameL);
					layout.putConstraint(n, warmthL, 10, s, areaL);
					layout.putConstraint(w, warmthL, 0, w, nameL);
					layout.putConstraint(n, weightL, 10, s, warmthL);
					layout.putConstraint(w, weightL, 0, w, nameL);
				}
				JLabel biggestL = nameL;
				for(JLabel l: labelSizes.keySet()){
					if(labelSizes.get(l) > labelSizes.get(biggestL)){
						biggestL = l;
					}
				}
				
				layout.putConstraint(e, panel, 20, e, biggestL);
				layout.putConstraint(s, panel, 20, s, weightL);
			}
			else{
				layout.putConstraint(e, panel, 20, e, nameL);
				layout.putConstraint(s, panel, 20, s, nameL);
			}
			
			panel.setBackground(Color.black);
			
			
			frame.getContentPane().add(panel);
			frame.pack();
			frame.setLocation((int)(this.getLocationOnScreen().getX() + this.getWidth() + 10), (int)(this.getLocationOnScreen().getY() + 10));
			frame.setVisible(true);
			
			//TODO do what?
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			frame.dispose();
		}
		
	}
	
}
