package game;

import java.util.*;
import java.awt.Point;
import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import bsh.Interpreter;

public class Global implements Serializable{
	
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 2669996259816941079L;
	public static DataOutputStream out = null;
	public static boolean online = false;
	public static int numberOnline = 1;
	public static boolean isServer = false;
	public static Hashtable<Socket,DataOutputStream> outputStreams = new Hashtable<Socket,DataOutputStream>();
	public static Hashtable<String,Socket> playerData = new Hashtable<String,Socket>();
	public static ArrayList<String> playerNames = new ArrayList<String>();
	public static boolean wait = false;
	public static File log = new File("Log");
	public static boolean pause = false;
	public static Random generator = new Random();
	public static ReadWriteTextFile rwtext = new ReadWriteTextFile();
	public static SoundEngine soundEngine = new SoundEngine();
	public static boolean busy = false;
	public static int culture,technology,religion,economy;
	
	public static TreeMap<Double,ArrayList<int[]>> queue = new TreeMap<Double,ArrayList<int[]>>();
	
	public static ArrayList<Integer> artifactsDiscovered = new ArrayList<Integer>();
	
	public static final int playerSize = 14;
	public static int coverSize = 180;
	
	public static Interpreter beanShell = new Interpreter();
	
	private static final Logger logger = Logger.getLogger(Global.class);
	
	public static void pauseProg() throws InterruptedException{
		RPGMain.printText(false,"...");
		GameFrameCanvas.textField.setText("");
		pause = true;
		RPGMain.waitForMessage();
		pause = false;
	}
	public static void pauseProg(int ms) throws InterruptedException{
		GameFrameCanvas.textField.setEnabled(false);
		GameFrameCanvas.textField.setText("");
		pause = true;
		for(int j=0;j<3;j++){
			RPGMain.printText(false, ".");
			Thread.sleep(ms/4);
		}
		pause = false;
		RPGMain.printText(true, "");
		GameFrameCanvas.textField.setEnabled(true);
		GameFrameCanvas.textField.requestFocus();
	}
	public static void addDiscoveredArtifactID(int ID, boolean playerFound){
		artifactsDiscovered.add(ID);
		
		boolean found = false;
		
		//remove artifact from world if NPC found it
		if(!playerFound){
			for(int j=0;j<Data.wereld.length;j++){
				for(int k=0;k<Data.wereld[0].length;k++){
					if(!found && Data.wereld[j][k] instanceof HostileArea){
						HostileArea h = (HostileArea)Data.wereld[j][k];
						HashMap<Integer,int[]> localArtifacts = h.getArtifacts();
						if(localArtifacts != null){
							for(int id: localArtifacts.keySet()){
								if(id == ID){
									h.removeArtifact(ID);
									found = true;
									logger.debug("Found and removed the artifact (ID:" + id + ") in HostileArea " + h.getName() + " at " + 
											h.getPositie()[0] + "," + h.getPositie()[1] + ", at " + 
											localArtifacts.get(id)[0] + "," + localArtifacts.get(id)[1] + "," + localArtifacts.get(id)[2] + ".");
									//normally, artifact should only exist in one place
									break;
								}
							}
							break;
						}
					}
				}
			}
		}
	}
	
	public static void increaseKnowledge(String type, int amount){
		/* What has to happen here?
		 * 1) increase the respective parameter
		 * 2) see what effect it has on different aspects of society
		 * 3) give the player a message, of show him in some way the progress made?
		 */
		if(type.equalsIgnoreCase("culture")){
			culture+=amount;
			/* What happens when culture is increased?
			 * 1) More books in existing libraries
			 * 2) More people becoming scholars
			 * 3) Build theatre halls and make plays players can attend, or poetry
			 * 4) Make bards, people travelling and telling stories
			 * 5) Change conversation trees of NPCs
			 * 6) Build libraries in towns where they have none
			 */
		}
		else if(type.equalsIgnoreCase("technology")){
			technology+=amount;
			/* What happens when technology is increased?
			 * 1) Prices in shops become lower due to improvements in farming etc
			 * 2) Creation of technology guilds, scientists gathering to discuss and create, possibly give new quests
			 * 3) More people becoming inventors
			 * 4) New technology items in shops
			 * 5) 
			 */
		}
		else if(type.equalsIgnoreCase("religion")){
			religion+=amount;
			/* What happens when religion is increased?
			 * 1) Building more churches/religious places
			 * 2) More people becoming "priests"
			 * 3) Role of religion still has to be thought out
			 */
		}
		else if(type.equalsIgnoreCase("economy")){
			economy+=amount;
			/* What happens when economy is increased?
			 * 1) Prices become cheaper
			 * 2) Banks open
			 * 3) More shops selling different, exotic things
			 * 4) Travelling becomes cheaper because roads are improved
			 * 5) Cities become bigger
			 * 6) Less diseases and bad events due to improved quality of life
			 * 7) More cash money for discovered artifacts
			 */
		}
		
		logger.debug("Culture: " + culture + "; Technology: " + technology + "; Religion: " + religion + "; Economy: " + economy);
	}
	// Send messages to server, interpreted at server
	public static void message(String message){
			try {
				out.writeUTF(message);
				String date = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
				ReadWriteTextFile.setContents(log,date + ": " + message, true);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Failed to deliver message.", "Error", JOptionPane.INFORMATION_MESSAGE);
			}
	}
	
	/** Allows the printing of dialogs from text files
	 * @param repeated Separate text pieces must be marked with a repeated String
	 * 				   with an integer. ex.: "HISTORY 1" \n 'Dialog text' "HISTORY 2"
	 * @param optionalAnswers If a dialog requests user input. UNDER CONSTRUCTION
	 * @throws InterruptedException 
	 */
	public static String makeDialog(File aFile,String[] path){
		SAXBuilder parser = new SAXBuilder();
		StringBuilder sb = new StringBuilder();
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			Element el = root;
			for(int j=0;j<path.length;j++){
				String s = path[j];
				String[] t = s.split(",");
				try{
					String[] u = t[1].split(":");
					@SuppressWarnings("unchecked")
					List<Element> children = el.getChildren(t[0]);
					//iterate over children to see if attribute value fits
					for(Element e:children){
						if(e.getAttributeValue(u[0]).equalsIgnoreCase(u[1])){
							el = e;
						}
					}
				}catch(Exception e){
					el = el.getChild(s);
					System.out.println(t[0]);
				}
			}
			List<?> dialog = el.getChildren();
			Iterator<?> i = dialog.iterator();
			while(i.hasNext()){
				Element d = (Element)i.next();
				// get the NPC name, if there is one
				String name = d.getAttributeValue("name");
				int ms;
				try{
					// get the specified delay
					ms = Integer.parseInt(d.getAttributeValue("delay"));
				} catch(NumberFormatException e){ ms = 0;}
				// print the NPC name if there was one
				if(name != null) RPGMain.printText(false, name + ": ");
				// check message for player name or race and change it accordingly
				String message = d.getTextNormalize();
				message = message.replaceAll("playerName", RPGMain.speler.getName());
				message = message.replaceAll("playerRace", RPGMain.speler.getRace());
				RPGMain.printText(true,message);
				sb.append(message);
				// pause program for specified delay
				if(ms == 0) pauseProg();
				else Thread.sleep(ms);
			}
			if(dialog.isEmpty()){
				RPGMain.printText(true, el.getTextTrim());
				sb.append(el.getTextTrim());
			}
			return sb.toString();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			RPGMain.printText(true, "File " + aFile.getAbsolutePath() + " not found!");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	// modified A* algorithm
	public static ArrayList<int[]> calculatePath(int[] startPos, int[] endPos, int movement, HashMap<double[][],Double> covers, boolean withinMelee){
		ArrayList<int[]> startPath = new ArrayList<int[]>();
		// ArrayList for partial path that is first in the queue
		ArrayList<int[]> firstInQueue = new ArrayList<int[]>();
		startPath.add(startPos);
		queue.clear();
		queue.put(0.0,startPath);
		
		final int stepSize = 3;
		
		logger.info("Entered calculatePath. startPos: " + startPos[0] + "," + startPos[1] + "; endPos: " + endPos[0] + "," + endPos[1] + "; Movement: " + movement + ".");
		
		while(!queue.isEmpty()){
			//get the first in queue
			firstInQueue = queue.get(queue.firstKey());
			
			logger.info("First in Queue: " + firstInQueue.get(firstInQueue.size()-1)[0] + "," + firstInQueue.get(firstInQueue.size()-1)[1] + ". Movement:" + movement);
			//get the last coord pair of the first partial path in the queue
			int[] lastPos = firstInQueue.get(firstInQueue.size()-1);
			//remove the first partial path from the queue
			queue.remove(queue.firstKey());
			
			// checks around the current position: -stepSize,0,+stepSize for both j and k
			for(int j=-stepSize;j<=stepSize;j=j+stepSize){
				for(int k=-stepSize;k<=stepSize;k=k+stepSize){
					if(k == 0 && j==0){
						// do nothing for current position
					}
					else{
						// add the new coord pair to the partial path of the first in queue
						firstInQueue.add(new int[] {lastPos[0] + k,lastPos[1] + j});
						// check if the path is valid, and add it to the queue
						addPath(firstInQueue,startPos,endPos,covers,stepSize);
						// remove the last coord pair, and repeat with the next around it
						firstInQueue.remove(firstInQueue.size()-1);
					}
				}
			}
			
			logger.debug("Queue size after creation of new paths:" + queue.size());
			
			// Because of the use of a TreeMap, it's constantly sorted
			ArrayList<Double> toDelete = new ArrayList<Double>();
			Set<Double> keys = queue.keySet();
			Iterator<Double> i = keys.iterator();
			try{
				// iterate over all paths in the queue
				while(i.hasNext()){
					Double d = i.next();
					ArrayList<int[]> p = queue.get(d);
					keys = queue.keySet();
					// get the last coord pair of path p
					int[] k = p.get(p.size()-1);
					//iterator over all other paths in the queue
					Iterator<Double> l = keys.iterator();
					while(l.hasNext()){
						Double m = l.next();
						ArrayList<int[]> a = queue.get(m);
						if(!p.equals(a)){
							//check if path a has overlap with p, and if it has and p has already moved more to get to that point than in path a, remove p
							for(int j=1;j<a.size();j++){
								int[] q = a.get(j);
								// effective distance travelled is (j-1)*5
								if(q[0] == k[0] && q[1] == k[1] && (p.size()-1)*stepSize > (j-1)*stepSize){
									// use arrayList to delete afterwards to avoid concurrentmodificationexception
									toDelete.add(d);
									break;
								}
							}
						}
					}
				}
				for(Double d: toDelete){
					queue.remove(d);
				}
				toDelete.clear();
			}catch(Exception e){
				e.printStackTrace();
				logger.error("Error at calculate path. " , e);
			}
			//System.out.println("Past redundancy checking");
			ArrayList<int[]> bestPath = queue.get(queue.firstKey());
			int[] currentPos = bestPath.get(bestPath.size()-1);

			if(getTraveledDistance(bestPath) >= movement || (withinMelee && new Point(endPos[0],endPos[1]).distance(currentPos[0],currentPos[1]) < Global.playerSize)){
				System.out.println("Goal reached. Movement: " + movement + " distance: " + new Point(startPos[0],startPos[1]).distance(currentPos[0],currentPos[1]) + " traveled: " + getTraveledDistance(bestPath));
				return bestPath;
			}
		}
		
		return null;
	}
	
	public static void addPath(ArrayList<int[]> path, int[] startPos, int[] endPos,HashMap<double[][],Double> covers,int stepSize){
		int width = GameFrameCanvas.battlefield.getWidth();
		int height = GameFrameCanvas.battlefield.getHeight();
		
		// get the last coord pair in the partial path
		int[] newPos = path.get(path.size()-1);
		// check that it is still within the window
		if(newPos[0] > 0 && newPos[0] < width && newPos[1] > 0 && newPos[1] < height){
			Iterator<double[][]> coverPos = covers.keySet().iterator();
			// check if it isn't in cover
			while(coverPos.hasNext()){
				double[][] pos = coverPos.next();
				// covers are treated as ellipses
				// get the value of the cosine for the angle made between the center of the cover and the new coord, and the x-axis
				double cosTheta = (double)(newPos[0]-pos[0][0])/Math.sqrt(Math.pow(newPos[0] - pos[0][0], 2) + Math.pow(newPos[1] - pos[0][1], 2));
				double radius = 1.0/Math.sqrt(Math.pow(cosTheta/(Global.coverSize/2*pos[1][0]), 2) + (1-cosTheta*cosTheta)/Math.pow(Global.coverSize/2*pos[1][1],2));
				if(new Point(newPos[0],newPos[1]).distance(pos[0][0],pos[0][1]) <= (radius+Global.playerSize/2)){
					return;
				}
			}
			// check for loops
			for(int j=0;j<path.size()-1;j++){
				int[] p = path.get(j);
				// check for overlap between a coord, and all the ones following it
				for(int k = j+1;k<path.size();k++){
					int[] l = path.get(k);
					
					// if there is overlap, don't add the path to the queue
					if(p[0] == l[0] && p[1] == l[1]){
						return;
					}
					// if there is no overlap, and the whole partial path is checked, add the path to the queue
					else if((p[0] != l[0] || p[1] != l[1]) && k == (path.size()-1) && j == (path.size()-2)){
						try{
							queue.put(0.5*getTraveledDistance(path) + new Point(newPos[0],newPos[1]).distance(endPos[0],endPos[1]),cloneArrayList(path));
						} catch(Exception e){
							e.printStackTrace();
							logger.error(e);
						}
					}
				}
			}
		}
		else{
			return;
		}
	}
	public static ArrayList<int[]> cloneArrayList(ArrayList<int[]> a){
		ArrayList<int[]> b = new ArrayList<int[]>(a.size());
		for(int[] o:a){
			b.add(o.clone());
		}
		return b;
	}
	public static double getTraveledDistance(ArrayList<int[]> a){
		double distance = 0;
		for(int j=0;j<a.size()-1;j++){
			distance+= new Point(a.get(j)[0],a.get(j)[1]).distance(a.get(j+1)[0],a.get(j+1)[1]);
		}
		return distance;
	}
}