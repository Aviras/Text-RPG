package game;

import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import java.io.IOException;
import javax.swing.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class GameFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(GameFrame.class);
	private static GraphicsDevice vc;
	protected static GameFrameCanvas mainPanel;
	public static Logbook logbook;
	
	public GameFrame(String title){

		super(title);
		
		logger.info("Starting application");
				
		setResizable(false);
		
		// sets up the different GUI components
		mainPanel = new GameFrameCanvas();
		getContentPane().add(mainPanel);
		
		//create logbook
		logbook = new Logbook();
		
		//start loading all the data
		RPGMain.initialize();
		
		//setUndecorated(true);
		
		
	    addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
				int choice = JOptionPane.showConfirmDialog(null, "Do you want to save first?", "Save before exit?", JOptionPane.YES_NO_CANCEL_OPTION);

				if(choice == JOptionPane.CANCEL_OPTION){
					return;
				}
				else{
					if(choice == JOptionPane.YES_OPTION){
						try {
							RPGMain.saveGame();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					logger.info("Exiting game");
					System.exit(0);
				}
	        }
	    });
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
		
		/*GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
		vc=ge.getDefaultScreenDevice();
		
		vc.setFullScreenWindow(this);*/
		
		//setExtendedState(Frame.MAXIMIZED_BOTH);
		setLocationRelativeTo(null);
		setVisible(true);
		
		GameFrameCanvas.textField.requestFocus();
		(new RPGMain()).execute();
			
	}
	public static void main(String[] pArgs) throws IOException,InterruptedException {
		
		PropertyConfigurator.configure("LoggingConfig.txt");
		
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new GameFrame("Text RPG");
            }
        });
	}
}
