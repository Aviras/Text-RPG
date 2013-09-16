package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.apache.log4j.Logger;

public class Logbook implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private static LogbookNode root;
	private static LogbookPanel panel;
	private static boolean initiated;
	private static String logbookPath;
	
	private static final Logger logger = Logger.getLogger(Logbook.class);
	
	public Logbook(){
		root = new LogbookNode("root");
		panel = new LogbookPanel();
		initiated = false;
	}
	
	public void setLogbook(LogbookNode l){
		root = l;
		panel = new LogbookPanel();
		panel.createPageNumbers();
		initiated = false;
	}
	
	public static LogbookNode getRootNode(){
		return root;
	}
	
	public static void addContent(String path, Integer phase, String content){
		if(path == null || path.equalsIgnoreCase("") || content == null || content.equalsIgnoreCase("")){
			return;
		}
		logbookPath = path;
		String[] partialPaths = path.split("/");
		LogbookNode currentNode = root;
		for(int j = 0;j<partialPaths.length;j++){
			boolean mapExists = false;
			String s = partialPaths[j];
			ArrayList<LogbookNode> currentChildren = currentNode.getChildren();
			for(int i=0;i<currentChildren.size();i++){
				if(currentChildren.get(i).getName().equalsIgnoreCase(s)){
					mapExists = true;
					currentNode = currentChildren.get(i);
					break;
				}
			}
			if(!mapExists){
				currentNode = currentNode.addChild(s);
			}
			if(j == partialPaths.length-1 && content != null){
				//phase = -1 is for shortcut
				if(phase != -1){
					currentNode.addContent(phase,content);
				}
				else{
					currentNode.addShortcut(content);
				}
			}
		}
		panel.createPageNumbers();

	}
	public static void addContent(String path, String shortcutPath){
		addContent(path,-1,shortcutPath);
	}
	
	public static int getPhase(String path){
		int phase = -1;
		if(path == null || path.equalsIgnoreCase("")){
			return phase;
		}
		logbookPath = path;
		String[] partialPaths = path.split("/");
		LogbookNode currentNode = root;
		for(int j = 0;j<partialPaths.length;j++){
			String s = partialPaths[j];
			ArrayList<LogbookNode> currentChildren = currentNode.getChildren();
			for(int i=0;i<currentChildren.size();i++){
				if(currentChildren.get(i).getName().equalsIgnoreCase(s)){
					currentNode = currentChildren.get(i);
					break;
				}
			}
		}
		phase = currentNode.getPhase();
		return phase;
	}
	
	public static void showLogbook(){
		//features:
		//works like a book, able to choose chapters, topics, scroll through pages
		//search function
		//different sections: personal story, own notes, world lore (bestiary, herbs, background on areas), achievements
		//has a 'bar' on top that shows what chapter you are in etc, works like hotlinks to maneuver quickly
		//use it to show the world map
		//some parts are editable
		
		JFrame frame = new JFrame("Logbook");
		
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		if(!initiated){
			panel.init();
			initiated = true;
		}
		
		frame.getContentPane().add(panel);
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		panel.setContent("Introduction");
		
	}
	
	private class LogbookPanel extends JPanel implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JButton bStory, bNotes, bLore, bAchiev, bNextPage, bPrevPage;
		private JTextArea textAreaLeft, textAreaRight;
		private JTextField searchField;
		private JLabel title,pageNrLeft,pageNrRight;
		private ArrayList<JButton> subCategories,location;
		private SpringLayout layout;
		private LinkedHashMap<Integer,String> pageNumbers;
		
		private Image background,imStory,imNotes,imLore,imAchiev,imLeftArrow,imRightArrow;
		
		private String w,n,s,e;
		
		public LogbookPanel(){
			
		}
		
		private void init(){
			
			//positional strings used
			w = SpringLayout.WEST;
			e = SpringLayout.EAST;
			n = SpringLayout.NORTH;
			s = SpringLayout.SOUTH;
			
			layout = new SpringLayout();
			setLayout(layout);
			//load background image
			try {
				background = ImageIO.read(new File("Images/open_book_2.jpg"));
				imLeftArrow = ImageIO.read(new File("Images/Left_Arrow.png"));
				imRightArrow = ImageIO.read(new File("Images/Right_Arrow.png"));
				imStory = ImageIO.read(new File(""));
				imNotes = ImageIO.read(new File(""));
				imLore = ImageIO.read(new File(""));
				imAchiev = ImageIO.read(new File(""));
			} catch (IOException e1) {
			}
			
			setBackground(Color.black);
			try{
				bStory = new JButton(new ImageIcon(imStory));
				bNotes = new JButton(new ImageIcon(imNotes));
				bLore = new JButton(new ImageIcon(imLore));
				bAchiev = new JButton(new ImageIcon(imAchiev));
				
				bNextPage = new JButton(new ImageIcon(imRightArrow));
				bPrevPage = new JButton(new ImageIcon(imLeftArrow));
				
			} catch(NullPointerException exc){
				bStory = new JButton("Story");
				bNotes = new JButton("Notes");
				bLore = new JButton("Lore");
				bAchiev = new JButton("Achievements");
				
				bNextPage = new JButton("Next");
				bPrevPage = new JButton("Previous");
			}
			
			title = new JLabel("Title");
			pageNrLeft = new JLabel("pageLeft");
			pageNrRight = new JLabel("pageRight");
			
			textAreaLeft = new JTextArea(20,25);
			textAreaRight = new JTextArea(25,25);
			
			textAreaLeft.setLineWrap(true);
			textAreaLeft.setWrapStyleWord(true);
			textAreaRight.setLineWrap(true);
			textAreaRight.setWrapStyleWord(true);
			
			//TODO turned off for testing purposes
			//textAreaLeft.setOpaque(false);
			//textAreaRight.setOpaque(false);
			
			searchField = new JTextField(20);
			searchField.setText("Search...");
			
			searchField.addFocusListener(new FocusListener(){
				public void focusGained(FocusEvent e) {
					searchField.setText("");
				}
				public void focusLost(FocusEvent e) {
					searchField.setText("Search...");
				}
			});
			searchField.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent evt) {
			        String text = searchField.getText().trim();
			        if(!text.isEmpty()){
			        	searchContent(text.toLowerCase());
			        	searchField.setText("");
			        }
			    }
			});
			
			subCategories = new ArrayList<JButton>();
			location = new ArrayList<JButton>();
			
			//all button actions
			bStory.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					setContent("Story");
				}
			});
			
			bNotes.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					setContent("Notes");
				}
			});
			bLore.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					setContent("Lore");
				}
			});
			bAchiev.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					setContent("Achievements");
				}
			});
			bNextPage.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					changePage(1);
				}
			});
			bPrevPage.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					changePage(-1);
				}
			});
			
			//add all components to the panel
			add(bStory);
			add(bNotes);
			add(bLore);
			add(bAchiev);
			add(bPrevPage);
			add(bNextPage);
			add(title);
			add(pageNrLeft);
			add(pageNrRight);
			add(textAreaLeft);
			add(textAreaRight);
			add(searchField);
			
			
			//layout for buttons
			layout.putConstraint(n, bStory, 30, n, this);
			layout.putConstraint(w, bStory, 20, w, this);
			layout.putConstraint(n, bNotes, 20, s, bStory);
			layout.putConstraint(w, bNotes, 0, w, bStory);
			layout.putConstraint(n, bLore, 20, s, bNotes);
			layout.putConstraint(w, bLore, 0, w, bStory);
			layout.putConstraint(n, bAchiev, 20, s, bLore);
			layout.putConstraint(w, bAchiev, 0, w, bStory);
			
			//layout for text
			layout.putConstraint(n, title, 70, n, this);
			layout.putConstraint(w, title, 140, w, this);
			layout.putConstraint(n, textAreaLeft, 20, s, title);
			layout.putConstraint(w, textAreaLeft, 0, w, title);
			layout.putConstraint(n, textAreaRight, 0, n, title);
			layout.putConstraint(w, textAreaRight, 80, e, textAreaLeft);
			
			//layout for stuff concerning pages
			layout.putConstraint(w, bPrevPage, -50, w, title);
			layout.putConstraint(n, bPrevPage, 20, s, textAreaLeft);
			layout.putConstraint(n, bNextPage, 0, n, bPrevPage);
			layout.putConstraint(e, bNextPage, 20, e, textAreaRight);
			layout.putConstraint(n, pageNrLeft, 0, n, bPrevPage);
			layout.putConstraint(w, pageNrLeft, 20, e, bPrevPage);
			layout.putConstraint(n, pageNrRight, 0, n, pageNrLeft);
			layout.putConstraint(e, pageNrRight, -20, w, bNextPage);
			
			//layout for search field
			layout.putConstraint(n, searchField, 10, n, this);
			layout.putConstraint(e, searchField, 0, e, textAreaRight);
			
			//layout for window
			layout.putConstraint(e, this, 30, e, textAreaRight);
			layout.putConstraint(s, this, 30, s, pageNrRight);
			
		}
		
		public void createPageNumbers(){
			try{
				pageNumbers.clear();
			} catch(NullPointerException exc){
				//first time
				pageNumbers = new LinkedHashMap<Integer,String>();
			}
			
			int page = 1;
			
			//hashmap for the current path, together with the indices for each to know what child to select next
			LinkedHashMap<LogbookNode,Integer> posInTree = new LinkedHashMap<LogbookNode,Integer>();
			posInTree.put(root,0);
			//depth first search through tree
			//4 is number of categories normally under root dir
			//should number change, it will always end because root will then be deleted at the first if, and the loop will end
			while(posInTree.get(root) < 4){
				Iterator<LogbookNode> i = posInTree.keySet().iterator();
				LogbookNode currentNode = null;
				//currentNode is the last LogbookNode added
				while(i.hasNext()){
					currentNode = i.next();
				}
				ArrayList<LogbookNode> children = currentNode.getChildren();
				//end of the chain, remove the lowest level, and increase the index of the level above it
				//check is redundant if you just deleted a level, since the upper level obviously has children
				//assuming checking if the index is equal to 0 takes as much time as checking if it's empty
				//if the index is equal to the size, that means you checked all children, prevents indexoutofboundsexc
				if(children.isEmpty() || posInTree.get(currentNode) == children.size()){
					posInTree.remove(currentNode);
					if(posInTree.isEmpty()){
						//if root gets removed at the end
						break;
					}
					else{
						i = posInTree.keySet().iterator();
						LogbookNode dummy = null;
						while(i.hasNext()){
							dummy = i.next();
						}
						//increase the index of the level above the one you just removed
						posInTree.put(dummy, posInTree.get(dummy)+1);
					}
				}
				else{
					currentNode = children.get(posInTree.get(currentNode));
					//add the lowest lying node to your current pos
					posInTree.put(currentNode, 0);
					
					String path = "";
					for(LogbookNode l:posInTree.keySet()){
						path+=l.getName() + "/";
					}
					//leaves the root/, and cuts the extra / at the end
					path=path.substring(5, path.length()-1);
					pageNumbers.put(page, path);
					////System.out.println("Set " + path + " to page " + page);
					page++;
					//all pages show two pages
					pageNumbers.put(page, path);
					////System.out.println("Set " + path + " to page " + page);
					page++;
				}
			}
			
			
		}

		public void changePage(int amount){
			int currentPageNumber = 1;
			String currentPath = "";
			for(JButton j:location){
				currentPath+=j.getText() + "/";
			}
			//cuts the last /
			currentPath = currentPath.substring(0,currentPath.length()-1);
			
			////System.out.println("Current location: " + currentPath);
			
			for(int i:pageNumbers.keySet()){
				if(pageNumbers.get(i).equalsIgnoreCase(currentPath)){
					currentPageNumber = i;
					break;
				}
			}
			//above always searches for the lowest pageNumber, eg the one on the left, while if amount > 0, so nextPage, it should take the number to the right
			if(amount > 0){
				currentPageNumber++;
			}
			String newPath = pageNumbers.get(currentPageNumber + amount);
			if(newPath == null){
				return;
			}
			
			setContent(newPath);
			
		}
		
		private void searchContent(String s){
			//remove all previous search results
			try{
				root.getChild("Search Results").removeAllChildren();
			}catch(NullPointerException exc){
				//first time a search is done
			}
			
			ArrayList<String> possPaths = new ArrayList<String>();
			//hashmap for the current path, together with the indices for each to know what child to select next
			LinkedHashMap<LogbookNode,Integer> posInTree = new LinkedHashMap<LogbookNode,Integer>();
			posInTree.put(root,0);
			//depth first search through tree
			//4 is number of categories normally under root dir
			//should number change, it will always end because root will then be deleted at the first if, and the loop will end
			while(posInTree.get(root) < 4){
				Iterator<LogbookNode> i = posInTree.keySet().iterator();
				LogbookNode currentNode = null;
				//currentNode is the last LogbookNode added
				while(i.hasNext()){
					currentNode = i.next();
				}
				ArrayList<LogbookNode> children = currentNode.getChildren();
				//end of the chain, remove the lowest level, and increase the index of the level above it
				//check is redundant if you just deleted a level, since the upper level obviously has children
				//assuming checking if the index is equal to 0 takes as much time as checking if it's empty
				//if the index is equal to the size, that means you checked all children, prevents indexoutofboundsexc
				if(children.isEmpty() || posInTree.get(currentNode) == children.size()){
					posInTree.remove(currentNode);
					if(posInTree.isEmpty()){
						break;
					}
					else{
						i = posInTree.keySet().iterator();
						LogbookNode dummy = null;
						while(i.hasNext()){
							dummy = i.next();
						}
						//LogbookNode dummy = posInTree.keySet().toArray(new LogbookNode[]{})[posInTree.size()-1];
						posInTree.put(dummy, posInTree.get(dummy)+1);
					}
				}
				else{
					currentNode = children.get(posInTree.get(currentNode));
					//add the lowest lying node to your current pos
					posInTree.put(currentNode, 0);
					if(currentNode.getName().toLowerCase().contains(s)){
						String path = "";
						for(LogbookNode l:posInTree.keySet()){
							path+=l.getName() + "/";
						}
						//leaves the root/, and cuts the extra / at the end
						path=path.substring(5, path.length()-1);
						possPaths.add(path);
					}
				}
			}
			for(String p: possPaths){
				String[] split = p.split("/");
				addContent("Search Results/" + split[split.length-1], p);
			}
			if(possPaths.isEmpty()){
				addContent("Search Results",1,"No results were found.");
				
			}
			setContent("Search Results");
			
		}
		
		public void setContent(String path){
			
			String currentPath="";
			for(JButton j:location){
				currentPath+=j.getText() + "/";
			}
			try{
				currentPath=currentPath.substring(0, currentPath.length()-1);
				
				////System.out.println("CurrentPath:" + currentPath);
				
				if(currentPath.equalsIgnoreCase(path)){
					////System.out.println("Same path.");
					return;
				}
			} catch(StringIndexOutOfBoundsException e){
			}
			
			String[] partialPaths = path.split("/");
			
			LogbookNode currentNode = root;
			for(int i=0;i<partialPaths.length;i++){
				currentNode = currentNode.getChild(partialPaths[i]);
			}
			if(currentNode == null){
				//System.err.println(path + " is not found in the logbook.");
				return;
			}
			
			for(JButton j: subCategories){
				remove(j);
			}
			for(JButton j: location){
				remove(j);
			}
			
			subCategories.clear();
			location.clear();
			
			
			//check where you are in the book
			for(int i=0;i<partialPaths.length;i++){
				String s = partialPaths[0];
				for(int j=1;j<=i;j++){
					s+="/" + partialPaths[j];
				}
				//make buttons for every phase you're in
				JButton dummy = new JButton(partialPaths[i]);
				dummy.addActionListener(new NewActionListener(s));
		        /*dummy.setFocusPainted(false);
		        dummy.setMargin(new Insets(0, 0, 0, 0));
		        dummy.setContentAreaFilled(false);
		        dummy.setBorderPainted(false);
		        dummy.setOpaque(false);*/
				location.add(dummy);
			}
			
			
			//add the locations to the UI
			for(int i=0;i<location.size();i++){
				add(location.get(i));
				layout.putConstraint(n, location.get(i), 20, n, this);
				if(i==0){
					layout.putConstraint(w, location.get(i), 0, w, title);
				}
				else{
					layout.putConstraint(w, location.get(i), 5, e, location.get(i-1));
				}
			}
			
			
			String content = currentNode.getContents();
			String name = currentNode.getName();
			
			title.setText(name);
			
			////System.out.println("Content: " + content);
			
			if(content == null){
				//get the subcategories
				ArrayList<LogbookNode> children = currentNode.getChildren();
				for(LogbookNode l: children){
					JButton dummy = new JButton(l.getName());
					if(l.getShortcut() != null){
						dummy.addActionListener(new NewActionListener(l.getShortcut()));
					}
					else{
						dummy.addActionListener(new NewActionListener(path + "/" + l.getName()));
					}
			        /*dummy.setFocusPainted(false);
			        dummy.setMargin(new Insets(0, 0, 0, 0));
			        dummy.setContentAreaFilled(false);
			        dummy.setBorderPainted(false);
			        dummy.setOpaque(false);*/
					subCategories.add(dummy);
				}
				//add the locations to the UI
				for(int i=0;i<subCategories.size();i++){
					add(subCategories.get(i));
					layout.putConstraint(w, subCategories.get(i), 0, w, title);
					if(i==0){
						layout.putConstraint(n, subCategories.get(i), 20, s, title);
					}
					else{
						layout.putConstraint(n, subCategories.get(i), 15, s, subCategories.get(i-1));
					}
				}
				textAreaLeft.setVisible(false);
				textAreaRight.setVisible(false);
				
			}
			else{
				String contentLeft,contentRight;
				textAreaLeft.setVisible(true);
				textAreaRight.setVisible(true);
				
				//TODO check how much text fits into one area
				int c = textAreaLeft.getColumns();
				int r = textAreaLeft.getRows();
				try{
					contentLeft = content.substring(0,(int)(textAreaLeft.getColumns()*textAreaLeft.getRows()*1.5));
					contentRight = content.substring((int)(textAreaLeft.getColumns()*textAreaLeft.getRows()*1.5));
				} catch(StringIndexOutOfBoundsException e){
					contentLeft = content;
					contentRight = "";
				}
				
				textAreaLeft.setText(contentLeft);
				textAreaRight.setText(contentRight);
			}
			
			int currentPageNumber = 1;
			for(int i: pageNumbers.keySet()){
				if(pageNumbers.get(i).equalsIgnoreCase(path)){
					currentPageNumber = i;
					break;
				}
			}
			
			pageNrLeft.setText("" + currentPageNumber);
			pageNrRight.setText("" + (currentPageNumber + 1));
			
		}
		
		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			g.drawImage(background, 0, 0,this.getWidth(),this.getHeight(), null);
		}
		
		private class NewActionListener implements ActionListener{
			String path;
			public NewActionListener(String s){
				path = s;
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				setContent(path);
			}
		}
	}//end of class LogbookPanel
	
	public class LogbookNode implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String nodeName,shortcut;
		private ArrayList<LogbookNode> children;
		private HashMap<Integer,String> contents;
		//TODO allow for description for every topic?
		
		public LogbookNode(String name){
			nodeName = name;
			children = new ArrayList<LogbookNode>();
		}
		public LogbookNode addChild(String name){
			LogbookNode l = new LogbookNode(name);
	
			int index = children.size();
			for(int j=0;j<children.size();j++){
				if(children.get(j).getName().compareToIgnoreCase(name) > 0){
					index = j;
					break;
				}
			}
			children.add(index, l);
			logger.debug("New Node added. " + name + " in " + nodeName);
			
			return l;
		}
		public void addShortcut(String path){
			shortcut = path;
		}
		public void addContent(Integer phase, String content){
			if(contents == null){
				contents = new HashMap<Integer, String>();
			}
			if(contents.get(phase) == null || !contents.get(phase).equalsIgnoreCase(content)){
				//TODO tell player he learned something new
				contents.put(phase, content);
				RPGMain.printText(true, "Added a new logbook item (" + logbookPath + ").");
				logger.info("Added logbook content: " + logbookPath);
				double random = Math.random();
				
				if(random < 1.0/Math.sqrt(RPGMain.speler.getErudition())){
					RPGMain.speler.addErudition(1);
				}
			}
		}
		public void removeAllChildren(){
			//TODO do the children of the children get automatically removed by the garbage collector?
			children.clear();
		}
		public void removeChild(int index){
			children.remove(index);
		}
		public void removeChild(String name){
			for(LogbookNode l:children){
				if(l.getName().equalsIgnoreCase(name)){
					children.remove(l);
					break;
				}
			}
		}
		public String getName(){
			return nodeName;
		}
		public String getShortcut(){
			return shortcut;
		}
		public ArrayList<LogbookNode> getChildren(){
			return children;
		}
		public LogbookNode getChild(String name){
			for(LogbookNode l:children){
				if(l.getName().equalsIgnoreCase(name)){
					return l;
				}
			}
			return null;
		}
		public String getContents(){
			String s = "";
			try{
				for(String t:contents.values()){
					s+=t + "\n\n";
				}
			} catch(NullPointerException e){
				return null;
			}
			return s;
		}
		public int getPhase(){
			int phase = -1;
			try{
				for(int i: contents.keySet()){
					if(i > phase){
						phase = i;
					}
				}
			} catch(Exception e){
				e.printStackTrace();
				logger.error(nodeName,e);
			}
			return phase;
		}
		
	}

}
