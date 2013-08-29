package game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class Stables extends DistrictLocation {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private NPC stableMaster;
	private int[][] connectedCities;
	private HashMap<Integer,ArrayList<int[]>> bestPaths;
	
	private static TreeMap<Double,ArrayList<int[]>> queue = new TreeMap<Double,ArrayList<int[]>>();
	
	private static Logger logger = Logger.getLogger(Stables.class);

	public Stables(String name, String description, String npcID, String cityIDs){
		this.name = name;
		this.description = description;
		try{
			int id = Integer.parseInt(npcID);
			stableMaster = Data.NPCs.get(id);
		} catch(NumberFormatException e){
			e.printStackTrace();
			logger.error("Error for " + name,e);
		}
		String[] ids = cityIDs.split(";");
		connectedCities = new int[ids.length][2];
		for(int j=0;j<ids.length;j++){
			try{
				connectedCities[j][0] = Integer.parseInt(ids[j]);
			} catch(NumberFormatException e){
				e.printStackTrace();
				logger.error("Error for " + name,e);
			}
		}
	}
	
	public void calculateCost(int cityID){
		//modified A*
		//use a heuristic as sum of height difference squared + distance between two points + danger
		
		int[] startPos = RPGMain.speler.getCurrentPosition();
		int[] endPos = Data.towns.get(cityID).getPositie();
		
		int[][] heightMap = new int[Data.wereld.length][Data.wereld[0].length];
		
		for(int j=0;j<Data.wereld.length;j++){
			for(int k=0;k<Data.wereld[0].length;k++){
				try{
					heightMap[j][k] = Data.wereld[j][k].getAltitude();
				} catch(NullPointerException e){
					continue;
				}
			}
		}
		
		ArrayList<int[]> startPath = new ArrayList<int[]>();
		// ArrayList for partial path that is first in the queue
		ArrayList<int[]> firstInQueue = new ArrayList<int[]>();
		startPath.add(startPos);
		queue.clear();
		queue.put(0.0,startPath);
		
		int stepSize = 1;
		
		try{
		while(!queue.isEmpty()){
			//get the first in queue
			firstInQueue = queue.get(queue.firstKey());
			//get the last coord pair of the first partial path in the queue
			int[] lastPos = firstInQueue.get(firstInQueue.size()-1);
			//remove the first partial path from the queue
			queue.remove(queue.firstKey());
			
			// checks around the current position: -stepSize,0,+stepSize for both j and k
			for(int j=-stepSize;j<=stepSize;j=j+stepSize){
				for(int k=-stepSize;k<=stepSize;k=k+stepSize){
					if((k == 0 && j==0) || k*j != 0){
						// do nothing for current position, and don't consider diagonal movements
					}
					else{
						// add the new coord pair to the partial path of the first in queue
						firstInQueue.add(new int[] {lastPos[0] + k,lastPos[1] + j});
						// check if the path is valid, and add it to the queue
						addPath(firstInQueue,startPos,endPos,heightMap,1);
						// remove the last coord pair, and repeat with the next around it
						firstInQueue.remove(firstInQueue.size()-1);
					}
				}
			}
			
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
								if(q[0] == k[0] && q[1] == k[1] && (p.size()-1)> (j-1)*stepSize){
									// use arrayList to delete afterwards to avoid concurrentmodificationexception
									toDelete.add(d);
									break;
								}
							}
						}
					}
				}
				for(Double d: toDelete){
					System.out.println("Removing path " + d);
					queue.remove(d);
				}
				toDelete.clear();
			}catch(Exception e){
				e.printStackTrace();
				logger.error("Error at calculate path. " , e);
			}
			ArrayList<int[]> bestPath = queue.get(queue.firstKey());
			int[] lp = bestPath.get(bestPath.size()-1);
			
			if(lp[0] == endPos[0] && lp[1] == endPos[1]){
				logger.debug("Bestpath found");
				for(int[] in: bestPath){
					System.out.print(in[0] + "," + in[1] + "->");
				}
				break;
			}

		}
		bestPaths.put(cityID, queue.get(queue.firstKey()));
		
		double cost = queue.firstKey();
		
		connectedCities[cityID][1] = (int)cost;
		
		} catch(Exception e){
			e.printStackTrace();
			logger.error(e);
		}
		
	}
	public static void addPath(ArrayList<int[]> path, int[] startPos, int[] endPos, int[][] heightMap, int stepSize){
		
		try{
		// get the last coord pair in the partial path
		int[] newPos = path.get(path.size()-1);
		
		// check that it is still within the window
		if(newPos[0] < heightMap.length && newPos[0] >= 0 && newPos[1] >= 0 && newPos[1] < heightMap[0].length){
			//sum of height differences + distance between two points + danger
			double heuristic = 0;
			// check for loops
			for(int j=0;j<path.size();j++){
				int[] p = path.get(j);
				
				try{
					//Great height differences means extra distance, and heavier burden
					heuristic+=Math.abs(heightMap[p[0]][p[1]] - heightMap[path.get(j+1)[0]][path.get(j+1)[1]]);
				} catch(IndexOutOfBoundsException e){
					logger.error(e);
					break;
				}
				// check for overlap between a coord, and all the ones following it
				for(int k = j+1;k<path.size();k++){
					int[] l = path.get(k);
					
					if(Data.wereld[l[0]][l[1]] instanceof HostileArea){
						//more dangerous to go through hostile areas than through towns
						heuristic+=3;
					}
					else if(Data.wereld[l[0]][l[1]] instanceof Town){
					}
					else{
						return;
					}
					
					// if there is overlap, don't add the path to the queue
					if(p[0] == l[0] && p[1] == l[1]){
						return;
					}
					// if there is no overlap, and the whole partial path is checked, add the path to the queue
					else if((p[0] != l[0] || p[1] != l[1]) && k == (path.size()-1) && j == (path.size()-2)){
						try{
							//add the distances to the heuristic
							heuristic+=getTraveledDistance(path) + Point.distance(newPos[0], newPos[1], endPos[0], endPos[1]);
							queue.put(heuristic,cloneArrayList(path));
							System.err.println("Added path to queue ending on " + newPos[0] + "," + newPos[1]);
						} catch(Exception e){
							e.printStackTrace();
							logger.error(e);
						}
					}
				}
			}
		}
		else{
			// don't add anything to the queue
			return;
		}
		} catch(Exception e){
			e.printStackTrace();
			logger.error(e);
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
	
	public void enter() throws InterruptedException{
		
	}
	
	public int[] chooseDestination() throws InterruptedException{
		RPGMain.printText(true, description);
		
		int[] dir = new int[2];
		
		bestPaths = new HashMap<Integer,ArrayList<int[]>>();
		for(int j=0;j<connectedCities.length;j++){
			System.out.println("Starting calculation for id " + connectedCities[j][0]);
			calculateCost(connectedCities[j][0]);
		}
		
		if(Math.random() < 0.5){
			ArrayList<String> fileNames = new ArrayList<String>();
			fileNames.add("Sounds/Effects/horse_whiny.mp3");
			if(Math.random() < 0.5){
				fileNames.add("Sounds/Effects/horse_snort.mp3");
				Global.soundEngine.playSound(fileNames, "effects", 0, new int[]{0,0});
			}
			else{
				Global.soundEngine.playSound(fileNames, "effects", 0, new int[]{0});
			}
		}
		
		int choice = 0;
		while(true){
			
			RPGMain.printText(true, "Where do you want to go to?");
			for(int j=1;j<=connectedCities.length;j++){
				RPGMain.printText(true, j + ": " + Data.towns.get(connectedCities[j-1][0]).getName() + "(" + connectedCities[j-1][1] + " gold)");
			}
			RPGMain.printText(true, (connectedCities.length+1) + ": Talk to " + stableMaster.getName());
			RPGMain.printText(true, (connectedCities.length+2) + ": Cancel");
			RPGMain.printText(false, ">");
	
			try{
				choice = Integer.parseInt(RPGMain.waitForMessage())-1;
				if(choice >= 0 && choice < connectedCities.length){
					if(connectedCities[choice][1] > RPGMain.speler.getGoud()){
						RPGMain.printText(true, "You do not have enough money to pay the ride.");
					}
					else{
						RPGMain.speler.addGoud(-connectedCities[choice][1]);
						
						Global.pauseProg(2000);
						
						Global.soundEngine.fadeLines("ambient");
						
						ArrayList<String> fileNames = new ArrayList<String>();
						fileNames.add("Sounds/Effects/horse_run.mp3");
						Global.soundEngine.playSound(fileNames, "effects", 0, new int[]{300});
						
						//city you start from doesn't contribute
						for(int j=1;j<bestPaths.get(connectedCities[choice][0]).size();j++){
							
							//TODO stuff happens in the meantime, use Wouter's suggestion of for ex typing for gold reward, meeting people, w/e
							
							int[] i = bestPaths.get(connectedCities[choice][0]).get(j);
							
							dir[0] = i[0] - bestPaths.get(connectedCities[choice][0]).get(0)[0];
							dir[1] = i[1] - bestPaths.get(connectedCities[choice][0]).get(0)[1];
							
							//city you arrive at doesn't have events
							if(j == bestPaths.get(connectedCities[choice][0]).size()-1){
								break;
							}
							
							RPGMain.printText(true, "Entering " + Data.wereld[i[0]][i[1]].getName() + ".");
							if(Data.wereld[i[0]][i[1]] instanceof Town){
								GameFrameCanvas.imagePanel.changeImage("Town " + connectedCities[choice][0]);
							}
							else{
								GameFrameCanvas.imagePanel.changeImage("HostileArea " + connectedCities[choice][0]);
							}
							try{
								Global.pauseProg(2000);
							} catch(InterruptedException e){
							}
							
							try{
								int seconds = Global.generator.nextInt(4);
								Thread.sleep(seconds*1000);
								double r = Math.random();
								//TODO probabilities, other events
								if(r < 0.5){
									RPGMain.printText(false, "You see an old lady waving at you while holding a bag with fresh chicken. Take them or wave? [take/wave] 6s\n>");
									String action = RPGMain.waitForMessage(6).toLowerCase();
									if(action.equalsIgnoreCase("take")){
										RPGMain.printText(true, "The lady looks at you, clearly shocked, as you rip the bag out of her hands. Probably not the best choice for your reputation, but at least you have chicken.");
										RPGMain.speler.addInventoryItem(Data.consumables.get(0));
										//TODO right item, and create hope event
									}
									else if(action.equalsIgnoreCase("wave")){
										RPGMain.printText(true, "You greet the lady and a smile appears on her aged face. Another deed well done today.");
									}
								}
								Thread.sleep((6-seconds)*1000);
							} catch(InterruptedException e){
							}
						}
						//to differentiate between global level and town district level
						dir[0]*=5;
						dir[1]*=5;
						RPGMain.speler.setOnHorse(true);
						return dir;
					}
				}
				else if(choice == connectedCities.length){
					stableMaster.talk();
				}
				else if(choice == (connectedCities.length+1)){
					return new int[2];
				}
				else{
					RPGMain.printText(true, "Not a valid option.");
				}
			} catch(NumberFormatException e){
				RPGMain.printText(true, "Not a number.");
				continue;
			}
		}
	}
}
