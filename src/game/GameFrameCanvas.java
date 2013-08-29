package game;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GameFrameCanvas extends JPanel{

	private static final long serialVersionUID = 1L;
	//different UI components
	public static JTextPane textPane,environmentPane;
	public static JScrollPane scrollPane,playerInfoScrollPane,environmentScrollPane;
	public static JTable playerInfo;
	public static JTextField textField;
	public static ImagePanel imagePanel;
	public static MapArea dungeonMap;
	public static Battlefield battlefield;
	public static Portrait playerPortrait,enemyPortrait,logoPortrait;
	public static JButton setWalking,setRunning,setCrouching;
	public static JPanel movementModePanel, clothingPanel;
	public static NoiseMeter noiseMeter;
	public static JLabel[] walkIcons, clothingIcons;
	public static SpringLayout layout;
	public static String n,e,s,w;
	private static Color normalColor, enemyTextColor, playerTextColor, townLocationColor, dungeonStringColor, playerInputColor;
	private static Logger logger = Logger.getLogger(GameFrameCanvas.class);
	
	
	//audio components
	public static boolean soundOn = true;
	public ArrayList<Clip> clips = new ArrayList<Clip>();
	public static int LOOP = 0,SOUNDINDEX = 0;
	//background image
	public static Image background = null;
	
	public GameFrameCanvas(){

		layout = new SpringLayout();
		setLayout(layout);
		
		//load background image
		try {
			background = ImageIO.read(new File("Images/plasma_ab_single.jpg"));
		} catch (IOException e1) {
		}
		
		setBackground(new Color(30,30,30));
		//construct UI components
		imagePanel = new ImagePanel(new Dimension(450,450));
		imagePanel.setVisible(false);
		dungeonMap = new MapArea(300,300);
		battlefield = new Battlefield(400,400);
		logoPortrait = new Portrait("Images/Dragon-Age-Logo.png",new Dimension(450,120),new Dimension(450,120));
		playerPortrait = new Portrait("Images/richard_big.jpg",new Dimension(220,100),new Dimension(100,100));
		playerPortrait.setVisible(false);
		enemyPortrait = new Portrait("Images/hildebadt_big.jpg",new Dimension(220,100),new Dimension(100,100));
		enemyPortrait.setVisible(false);
		
		noiseMeter = new NoiseMeter("Images/gradient-stops.jpg", new Dimension(150,10));
		
		textField = new JTextField(52);
		textField.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent evt) {
		        String text = textField.getText().trim();
		        if(!text.isEmpty() || Global.pause){
			        printText(text + "\n", "sienna");
			        RPGMain.recieveMessage(text);
			        textField.selectAll();
	
			        //Make sure the new text is visible, even if there
			        //was a selection in the text area.
			        textPane.setCaretPosition(textPane.getDocument().getLength());
		        }
		    }
		});
		textField.setFont(new Font("SansSerif",Font.PLAIN,16));
		
		textPane = new JTextPane();
		textPane.setDisabledTextColor(Color.black);
		textPane.setEditable(false);
		StyledDocument doc = textPane.getStyledDocument();
		addStylesToDocument(doc,18);
		scrollPane = new JScrollPane(textPane);
		scrollPane.setPreferredSize(new Dimension(800, 600));
		scrollPane.setMinimumSize(new Dimension(10, 10));
		
		environmentPane = new JTextPane();
		environmentPane.setDisabledTextColor(Color.black);
		environmentPane.setEditable(false);
		StyledDocument envDoc = environmentPane.getStyledDocument();
		addStylesToDocument(envDoc,14);
		environmentScrollPane = new JScrollPane(environmentPane);
		environmentScrollPane.setPreferredSize(new Dimension(300,150));
		
		movementModePanel = new JPanel();
		movementModePanel.setOpaque(false);
		
		setWalking = new JButton("W");
		setWalking.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				RPGMain.speler.setMovementMode("walking");
				textField.requestFocus();
			}
		});
		
		setRunning = new JButton("R");
		setRunning.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				RPGMain.speler.setMovementMode("running");
				textField.requestFocus();
			}
		});
		
		setCrouching = new JButton("C");
		setCrouching.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				RPGMain.speler.setMovementMode("crouching");
				textField.requestFocus();
			}
		});
		
		walkIcons = new JLabel[8];
		
		walkIcons[0] = new JLabel("Z");
		walkIcons[1] = new JLabel("E");
		walkIcons[2] = new JLabel("D");
		walkIcons[3] = new JLabel("C");
		walkIcons[4] = new JLabel("X");
		walkIcons[5] = new JLabel("W");
		walkIcons[6] = new JLabel("Q");
		walkIcons[7] = new JLabel("A");
		
		for(JLabel jl: walkIcons){
			jl.setForeground(Color.white);
			jl.setVisible(false);
		}
		
		
		movementModePanel.add(setWalking);
		movementModePanel.add(setRunning);
		movementModePanel.add(setCrouching);
		
		movementModePanel.setVisible(false);
		
		noiseMeter.setVisible(false);
		
		clothingIcons = new JLabel[3];
		
		clothingIcons[0] = new JLabel("Mantle");
		clothingIcons[1] = new JLabel("Shirt");
		clothingIcons[2] = new JLabel("Pants");
		
		clothingPanel = new JPanel();
		clothingPanel.setOpaque(false);
		
		for(JLabel jl: clothingIcons){
			jl.setForeground(Color.white);
			jl.setVisible(false);
			clothingPanel.add(jl);
		}
		
		String[] columnNames = {"Variable","Value"};
		
		String[][] data = {{"Name","playerName"},{"Level","1"},{"Strength","1"},{"Dexterity","1"},{"Intellect","1"},{"Charisma","1"},{"Swords","1"},{"Axes","1"},{"Clubs","1"},{"Archery","1"}};
		
		playerInfo = new JTable(data, columnNames);
		playerInfo.setFocusable(false);
		playerInfo.setFont(new Font("SansSerif",Font.PLAIN,16));
		
		playerInfoScrollPane = new JScrollPane(playerInfo);
		playerInfoScrollPane.setPreferredSize(new Dimension(300,300));
		playerInfoScrollPane.setVisible(false);

		
		/*JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		
		newGame = new JButton("New Game");
		newGame.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				textArea.append("Succesfully starting new game.\n");
				(new RPGMain()).execute();
				textField.requestFocus();
			}
		});
		
		sound = new JButton("Sound off");
		sound.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				soundOn = !soundOn;
				if(soundOn)
				{
					//clips.get(SOUNDINDEX).start();
					clips.get(SOUNDINDEX).loop(LOOP);
					sound.setText("Sound off");
				}
				else{
					clips.get(SOUNDINDEX).stop();
					//(mp3Player = new MusicPlayer("Heroic Age.mp3")).execute();
					sound.setText("Sound on");
				}
			}
		});
		
		exit = new JButton("Exit");
		exit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				System.exit(0);
			}
		});
		buttonPanel.add(newGame);
		buttonPanel.add(sound);
		buttonPanel.add(exit);*/
		
		//add all the elements to the panel
		//add(imagePanel);
		add(dungeonMap);
		add(battlefield);
		add(logoPortrait);
		add(playerPortrait);
		add(enemyPortrait);
		add(scrollPane);
		add(environmentScrollPane);
		add(textField);
		add(noiseMeter);
		add(movementModePanel);
		add(clothingPanel);
		add(playerInfoScrollPane);
		for(JLabel jl: walkIcons){
			add(jl);
		}
		
		//positional strings used
		w = SpringLayout.WEST;
		e = SpringLayout.EAST;
		n = SpringLayout.NORTH;
		s = SpringLayout.SOUTH;
		
		//organize the components
		layout.putConstraint(w, logoPortrait, 25, w, this);
		layout.putConstraint(n, logoPortrait, 40, n, this);
		layout.putConstraint(w, playerPortrait, 25, w, this);
		layout.putConstraint(n, playerPortrait, 50, n, this);
		layout.putConstraint(w, enemyPortrait, 10, e, playerPortrait);
		layout.putConstraint(n, enemyPortrait, 50, n, this);
		layout.putConstraint(n, noiseMeter, -50, n, dungeonMap);
		layout.putConstraint(w, noiseMeter, 0, w, dungeonMap);
		layout.putConstraint(n, clothingPanel, 10, n, this);
		layout.putConstraint(w, clothingPanel, 10, e, playerPortrait);
		/*layout.putConstraint(w, imagePanel, 100, e, scrollPane);
		layout.putConstraint(n, imagePanel, 30, n, this);*/
		layout.putConstraint(w, environmentScrollPane, 50, e, scrollPane);
		layout.putConstraint(n, environmentScrollPane, 10, s, playerInfoScrollPane);
		layout.putConstraint(w, dungeonMap, 50, e, scrollPane);
		layout.putConstraint(n, dungeonMap, 10, s, environmentScrollPane);
		layout.putConstraint(w, playerInfoScrollPane, 0, w, dungeonMap);
		layout.putConstraint(n, playerInfoScrollPane, 40, n, this);
		layout.putConstraint(w, battlefield, 0, w, playerPortrait);
		layout.putConstraint(n, battlefield, 20, s, playerPortrait);
		layout.putConstraint(s, walkIcons[0], -10, n, dungeonMap);
		layout.putConstraint(w, walkIcons[0], 220/*-walkIcons[0].getWidth()/2*/, w, dungeonMap);
		layout.putConstraint(n, walkIcons[4], 10, s, dungeonMap);
		layout.putConstraint(w, walkIcons[4], 0, w, walkIcons[0]);
		layout.putConstraint(n, walkIcons[6], 220/*-walkIcons[6].getHeight()/2*/, n, dungeonMap);
		layout.putConstraint(e, walkIcons[6], -10, w, dungeonMap);
		layout.putConstraint(n, walkIcons[2], 0, n, walkIcons[6]);
		layout.putConstraint(w, walkIcons[2], 10, e, dungeonMap);
		layout.putConstraint(n, walkIcons[1], 0, n, walkIcons[0]);
		layout.putConstraint(w, walkIcons[1], 0, w, walkIcons[2]);
		layout.putConstraint(n, walkIcons[3], 0, n, walkIcons[4]);
		layout.putConstraint(w, walkIcons[3], 0, w, walkIcons[2]);
		layout.putConstraint(e, walkIcons[5], 0, e, walkIcons[6]);
		layout.putConstraint(n, walkIcons[5], 0, n, walkIcons[4]);
		layout.putConstraint(e, walkIcons[7], 0, e, walkIcons[6]);
		layout.putConstraint(n, walkIcons[7], 0, n, walkIcons[0]);
		layout.putConstraint(w, scrollPane, 20, w, this);
		layout.putConstraint(n, scrollPane, 20, s, playerPortrait);
		layout.putConstraint(w, textField, 0, w, scrollPane);
		layout.putConstraint(n, textField, 5, s, scrollPane);
		
		layout.putConstraint(e, this, 30, e, dungeonMap);
		layout.putConstraint(s, this, 40, s, textField);
        
        /*loadSounds("Music/Intro Theme.wav");
        loadSounds("Music/Warrior's theme - take 1.wav");*/
	}
	
	public static void updatePlayerInfoTable(){
		
		Avatar sp = RPGMain.speler;
		Object[][] data = {{"<html><b>General Info</b></html>"},{"Name",sp.getName()},{"Level",sp.getLevel() + " (" + (int)((sp.getFullLevel()-sp.getLevel())*100.0) + "%)"},{"Strength",sp.getStrength()},{"Dexterity",sp.getDexterity()},{"Intellect",sp.getIntellect()},{"Charisma",sp.getCharisma()},{"Movement",String.format("%.2g%n", sp.getMovement()) + "m (" + sp.getExtraMovementPercentage() + "%)"},{"Gold",sp.getGoud()},{"<html><b>Weapon Skills</b></html>"},{"Swords",sp.getSwordSkill()},{"Axes",sp.getAxeSkill()},{"Clubs",sp.getClubSkill()},{"Archery",sp.getArchery()},{"<html><b>Survival</b></html>"},{"Making Fire", sp.getFireMaking()},{"Herbalism",sp.getHerbalism()},{"Hunting",sp.getHunting()},{"Animal Knowledge",sp.getAnimalKnowledge()},{"<html><b>Athleticism</b></html>"},{"Stamina",sp.getStamina()},{"Swimming",sp.getSwimming()},{"Thievery",sp.getThievery()},{"<html><b>Mental Skills</b></html>"},{"Erudition",sp.getErudition()}};
		
		playerInfo.setModel(new DefaultTableModel(data, new String[]{"Variable","Value"}));
		playerInfo.setTableHeader(null);
		playerInfo.setRowHeight(25);
		playerInfo.setRowSelectionAllowed(false);
		playerInfo.setColumnSelectionAllowed(false);
		
		playerInfoScrollPane.setVisible(true);
	}
	
	public static void moveComponents(JComponent compOne, JComponent compTwo, int heightOne, int time){
		int xOne = compOne.getX();
		int xTwo = compTwo.getX();
		int wOne = compOne.getWidth();
		int wTwo = compTwo.getWidth();
		int hOne = compOne.getHeight();
		int hTwo = compTwo.getHeight();
		
		compOne.setVisible(true);
		compTwo.setVisible(true);
		
		layout.putConstraint(n, compTwo, 10, s, compOne);
		
		for(int j=0;j<30;j++){
			
			int yOne = compOne.getY();
			int yTwo = compTwo.getY();
			
			
			compOne.setPreferredSize(new Dimension(wOne, yTwo-yOne + j*(heightOne-hOne)/30));
			compTwo.setPreferredSize(new Dimension(wTwo, hTwo - (yTwo - yOne) - j*(heightOne-hOne)/30));
			
			
			try{
				Thread.sleep((int)((double)time/30.0));
				System.out.println("Sleeping");
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	
	public static void moveScrollPane(JComponent comp, int time){
		int hComp = comp.getHeight();
		int hScrollPane = scrollPane.getHeight();
		
		comp.setVisible(true);
		
		for(int j=0;j<30;j++){
			
			scrollPane.setSize(new Dimension(scrollPane.getWidth(),hScrollPane-j*(comp.getY()+hComp-scrollPane.getY())/30));
			
			layout.putConstraint(n, scrollPane, 20 - j*(hComp+comp.getY()-scrollPane.getY())/30, s, comp);
			
			try{
				Thread.sleep(time/30);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	
	public void moveComponent(JComponent comp, int x, int y){
		layout.putConstraint(n, comp, y, n, this);
		layout.putConstraint(w, comp, x, w, this);
	}
	
	public void placeLogo(){
		logoPortrait.setPreferredSize(new Dimension(150,75));
		layout.putConstraint(w, logoPortrait, 20, e, playerPortrait);
		layout.putConstraint(n, logoPortrait, 0, n, playerPortrait);
		layout.putConstraint(w, enemyPortrait, 20, e, logoPortrait);
	}
	
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		//g.drawImage(background, 0, 0,this.getWidth(),this.getHeight(), null);
	}
	
    protected void addStylesToDocument(StyledDocument doc, int fontSize) {
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);
 
        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");
        StyleConstants.setFontSize(regular, fontSize);
        //TODO incorporate options for different text colors
        //StyleConstants.setForeground(regular, normalColor);
 
        Style s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);
 
        s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);
 
        s = doc.addStyle("small", regular);
        StyleConstants.setFontSize(s, 10);
 
        s = doc.addStyle("large", regular);
        StyleConstants.setFontSize(s, 16);
        
        s = doc.addStyle("red", regular);
        StyleConstants.setForeground(s, new Color(196,7,7));
        
        s = doc.addStyle("redbold", regular);
        StyleConstants.setForeground(s, new Color(196,7,7));
        StyleConstants.setBold(s, true);
        
        s = doc.addStyle("green", regular);
        StyleConstants.setForeground(s, new Color(44,173,14));
        
        s = doc.addStyle("greenbold", regular);
        StyleConstants.setForeground(s, new Color(44,173,14));
        StyleConstants.setBold(s, true);
        
        s = doc.addStyle("sienna", regular);
        StyleConstants.setForeground(s, new Color(247,175,2));
        
        s = doc.addStyle("darkblue", regular);
        StyleConstants.setForeground(s, new Color(16,34,128));
        
        s = doc.addStyle("darkbluebold", regular);
        StyleConstants.setForeground(s, new Color(16,34,128));
        StyleConstants.setBold(s, true);
    }
    
    public static void printText(String str, String style){
    	StyledDocument doc = textPane.getStyledDocument();
    	try {
			doc.insertString(textPane.getDocument().getLength(),str,doc.getStyle(style.toLowerCase()));
			textPane.setCaretPosition(textPane.getDocument().getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
			logger.error(e);
		}
    }
    
    public static void printText(String[] str, String[] style){
    	for(int i=0;i<str.length;i++){
    		printText(str[i],style[i]);
    	}
    }
    
    public static void printEnvironmentinfo(String str, String style){
    	StyledDocument doc = environmentPane.getStyledDocument();
    	try{
    		doc.insertString(environmentPane.getStyledDocument().getLength(), str, doc.getStyle(style.toLowerCase()));
    		environmentPane.setCaretPosition(environmentPane.getDocument().getLength());
    	} catch(BadLocationException e){
    		e.printStackTrace();
    		logger.error(e);
    	}
    }
    
    public void loadSounds(String fileName){
    	try {
			AudioInputStream in = AudioSystem.getAudioInputStream(new File(fileName));
			DataLine.Info info = new DataLine.Info(Clip.class,in.getFormat());
			Clip clip = (Clip) AudioSystem.getLine(info);
			clip.open(in);
			clips.add(clip);
		} catch (UnsupportedAudioFileException e) {
		} catch (IOException e) {
		} catch (LineUnavailableException e) {
		}
    }
    public void playSound(int index,int loop){
    	LOOP = loop;
    	SOUNDINDEX = index;
    	clips.get(index).loop(LOOP);
    }
 
    
/*	public class Mp3Player extends SwingWorker<Void,Void>{
		
		private String filename = null;
			
		public Mp3Player(String filename){
			this.filename = filename;
		}
			
		protected Void doInBackground(){
			playSound(filename);
			return null;
		}

		public void playSound(String filename)
		{
		  try {
		    File file = new File(filename);
		    AudioInputStream in = AudioSystem.getAudioInputStream(file);
		    AudioInputStream din = null;
		    AudioFormat baseFormat = in.getFormat();
		    AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
		                                                                                  baseFormat.getSampleRate(),
		                                                                                  16,
		                                                                                  baseFormat.getChannels(),
		                                                                                  baseFormat.getChannels() * 2,
		                                                                                  baseFormat.getSampleRate(),
		                                                                                  false);
		    din = AudioSystem.getAudioInputStream(decodedFormat, in);
		    // Play now.
		    rawplay(decodedFormat, din);
		    in.close();
		  } catch (Exception e)
		    {
			  e.printStackTrace();
		        //Handle exception.
		    }
		}
		@SuppressWarnings("unused")
		private void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException,                                                                                                LineUnavailableException
		{
		  byte[] data = new byte[4096];
		  DataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
		  SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
		  if (line != null)
		  {
		    // Start
			line.open();
		    line.start();
		    int nBytesRead = 0, nBytesWritten = 0;
		    while (nBytesRead != -1 && soundOn == true)
		    {
		        nBytesRead = din.read(data, 0, data.length);
		        if (nBytesRead != -1) nBytesWritten = line.write(data, 0, nBytesRead);
		    }
		    // Stop
		    line.drain();
		    line.stop();
		    line.close();
		    din.close();
		  }
		}
	}*/
}
