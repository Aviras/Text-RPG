package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.apache.log4j.Logger;

public class SkillTreePanel {
	
	private Canvas canvas;
	private static String n,s,e,w;
	private static JFrame frame;
	
	private static final Logger logger = Logger.getLogger(SkillTreePanel.class);
	
	public SkillTreePanel(){
		canvas = new Canvas();
	}
	
	public void showSkillTree(String skillTree){
		
		//TODO use a scrollPane
		frame = new JFrame(skillTree);
		
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		canvas.init(skillTree);
		
		frame.getContentPane().add(canvas);
		
		//frame.setMinimumSize(new Dimension(300,600));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private class Canvas extends JPanel{
	
		private Image background;
		private SpringLayout layout;
		private int HEIGHT, WIDTH;
		private ArrayList<SkillElement> skillElements;
		private JLabel availPointsL;
		private int availPoints;
		
		public Canvas(){
			HEIGHT = 600;
			WIDTH = 300;
		}
		
		public void init(String skillTree){
			
			skillElements = getSkillElements(skillTree);
			
			availPoints = RPGMain.speler.getSkill(skillTree);
			
			if(availPoints < 0){
				RPGMain.printText(true, "Not a valid skill.");
				return;
			}
			if(skillElements.size() == 0){
				RPGMain.printText(true, "Skill tree not yet implemented.", "redbold");
				return;
			}
			for(SkillElement se: skillElements){
				availPoints-=se.getProgress();
			}
			
			availPointsL = new JLabel("Available Points: " + availPoints);
			
			add(availPointsL);
			
			/*try{
				background = ImageIO.read(new File("Images/SkillTree_BG.png"));
			} catch(Exception e){
				logger.error(e);
				e.printStackTrace();
			}*/
			
			//positional strings used
			w = SpringLayout.WEST;
			e = SpringLayout.EAST;
			n = SpringLayout.NORTH;
			s = SpringLayout.SOUTH;
			
			layout = new SpringLayout();
			setLayout(layout);
			
			SkillLabel[] skillNodes = new SkillLabel[skillElements.size()];
			
			logger.debug("Found " + skillElements.size() + " SkillElements.");
			
			//see how many there are at every level => divide the screen into equal parts
			int index = 0;
			while(index < skillElements.size()){
				int level = skillElements.get(index).getLevelRequirement();
				int number = 0;
				for(int j=index;j<skillElements.size();j++){
					if(skillElements.get(j).getLevelRequirement() != level){
						break;
					}
					skillNodes[j] = new SkillLabel(skillElements.get(j));
					
					add(skillNodes[j]);
					number++;
				}
				
				logger.debug("Found " + number + " skill elements at level " + level);
				
				for(int j=index;j<index+number;j++){
					logger.debug("Positioning Skill Element " + skillNodes[j].getName() + " (index: " + j + ")");
					layout.putConstraint(n, skillNodes[j], level*20 + 30, n, this);
					layout.putConstraint(w, skillNodes[j], (j-index+1)*WIDTH/(number+1), w, this);
				}
				index+=number;
			}
			
			layout.putConstraint(n, availPointsL, 10, n, this);
			layout.putConstraint(w, availPointsL, 100, w, this);
			
			try{
				layout.putConstraint(e, this, 100, e, skillNodes[1]);
				layout.putConstraint(s, this, 50, s, skillNodes[skillNodes.length-1]);
			} catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
				logger.error("Need to implement skill tree for " + skillTree, e);
				frame.dispose();
			}
		}
		
		public ArrayList<SkillElement> getSkillElements(String skillTree){
			ArrayList<SkillElement> a = new ArrayList<SkillElement>();
			
			for(SkillElement se: Data.skillElements.values()){
				if(se.getSkillTree().equalsIgnoreCase(skillTree)){
					// put them in the right order
					int index = a.size();
					for(int j=0;j<a.size();j++){
						if(se.getLevelRequirement() <= a.get(j).getLevelRequirement()){
							index = j;
							break;
						}
					}
					a.add(index,se);
				}
			}
			
			return a;
		}
		
		public void paintComponent(Graphics g){
			super.paintComponent(g);
		}
		
	
		private class SkillLabel extends JLabel implements MouseListener{
			
			private JFrame frame;
			private SkillElement skillEl;
			
			public SkillLabel(SkillElement skillEl){
				super(skillEl.getName());
				
				this.skillEl = skillEl;
				
				addMouseListener(this);
			}
			
			public String getName(){
				return skillEl.getName();
			}
	
			@Override
			public void mouseClicked(MouseEvent e) {
			}
	
			@Override
			public void mousePressed(MouseEvent e) {
				//TODO Increase progress if possible
				if(availPoints > 0 && !skillEl.isMaxedOut() && skillEl.meetsLevelRequirement() && skillEl.meetsSkillRequirements()){
					skillEl.addProgress(1);
					frame.dispose();
					buildFrame();
					
					availPoints = RPGMain.speler.getSkill(skillEl.getSkillTree());
					for(SkillElement se: skillElements){
						availPoints-=se.getProgress();
					}
					
					availPointsL.setText("Available Points: " + availPoints);
				}
			}
	
			@Override
			public void mouseReleased(MouseEvent e) {
			}
	
			@Override
			public void mouseEntered(MouseEvent evt) {
				logger.debug("Entered " + skillEl.getName() + " node.");
				buildFrame();
			}
	
			@Override
			public void mouseExited(MouseEvent e) {
				frame.dispose();
			}
			
			public void buildFrame(){
				SpringLayout layout = new SpringLayout();
				
				frame = new JFrame(skillEl.getName());
				
				frame.setUndecorated(true);
				
				frame.setResizable(false);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				
				JPanel panel = new JPanel();
				
				panel.setLayout(layout);
				
				JLabel nameL = new JLabel(skillEl.getName());
				
				nameL.setForeground(Color.white);
				
				panel.add(nameL);
				
				JLabel progressL = new JLabel(skillEl.getProgress() + "/" + skillEl.getMaxProgress());
				
				panel.add(progressL);
				
				JLabel descriptionL = new JLabel(skillEl.getSkillDescription());
				
				panel.add(descriptionL);
				
				JLabel requirementL = new JLabel("Requires level " + skillEl.getLevelRequirement());
				
				if(!skillEl.meetsSkillRequirements()){
					requirementL = new JLabel("You do not meet the skill requirements.");
					
					requirementL.setForeground(Color.red);
				}
				
				if(!skillEl.meetsLevelRequirement()){
					requirementL.setForeground(Color.red);
				}
				
				panel.add(requirementL);
				
				layout.putConstraint(n, nameL, 2, n, panel);
				layout.putConstraint(w, nameL, 2, w, panel);
				layout.putConstraint(n, progressL, 3, s, nameL);
				layout.putConstraint(w, progressL, 0, w, nameL);
				layout.putConstraint(n, descriptionL, 3, s, progressL);
				layout.putConstraint(w, descriptionL, 0, w, nameL);
				
				layout.putConstraint(n, requirementL, 3, s, descriptionL);
				layout.putConstraint(w, requirementL, 0, w, nameL);
				
				layout.putConstraint(s, panel, 3, s, requirementL);
				layout.putConstraint(e, panel, 3, e, descriptionL);
				
				
				panel.setBackground(Color.black);
				
				
				frame.getContentPane().add(panel);
				frame.pack();
				frame.setLocation((int)(this.getLocationOnScreen().getX() + this.getWidth() + 10), (int)(this.getLocationOnScreen().getY() + 10));
				frame.setVisible(true);
			}
			
		}
	}

}
