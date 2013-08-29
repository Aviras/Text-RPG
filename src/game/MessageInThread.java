package game;
import java.io.*;
import java.net.*;
import java.util.InputMismatchException;

import javax.swing.*;

/* MESSAGE PROTOCOL
 * CLIENT MAKES CHARACTER WITH NEW NAME 'X' => NAME: X
 * CLIENT DOES 'Y' DAMAGE TO MOB AT INDEX 'X' => DMG: X Y
 * CLIENT HEALS ALLY 'X' FOR 'Y' => HEAL: X Y
 * MOB IS DEFEATED, XP DISTRIBUTED, AMOUNT X, GOLD AMOUNT 'Y' => REW: X Y
 * 
 * CONCLUSION: FIRST TARGET, THEN AMOUNT IF APPLICABLE
 * 
 */
public class MessageInThread extends Thread {//voor elke client wachten tot een message binnenkomt, dan tonen in messagebox
	
	private Socket client = null;
	private String myTradeItemName = null,otherTradeItemName = null;
	private boolean acceptedTrade;
	private int myTradeItemID,otherTradeItemID,myTradeItemIndex;
	
	public MessageInThread(Socket client){
		this.client = client;
		Global.online = true;
		start();
	}
	public void run(){
		try{
			DataInputStream din = new DataInputStream(client.getInputStream());
			while(true){
				String message = din.readUTF();
				// check for data
				try{
					if(!checkData(message)){// data doesn't get shown
						JOptionPane.showMessageDialog(null, message,"Action",JOptionPane.INFORMATION_MESSAGE);
					}
				} catch(InterruptedException e){}
			}
		} catch(SocketException se){
			RPGMain.printText(true,"Server went offline.");
		} catch( EOFException ie ) {
			//This doesn't need an error message
		} catch( IOException ie ) {
			//This does; tell the world!
			ie.printStackTrace();
		} finally {
			//The connection is closed for one reason or another,
			//so have the server dealing with it
			ConnectThread.removeConnection( client );
		}
	}
	public boolean checkData(String message) throws InterruptedException{//returns true if it's data, false if it's just text
		String data,sender;
		int amount,indexOfMob,dungeonID,gold;
		Enemy[] mobs;
		setPriority(10);
		
		if(message.contains("DMG: ")){
			data = message.substring(5);
			indexOfMob = Integer.parseInt(data.substring(0,data.indexOf(" ")));
			amount = Integer.parseInt(data.substring(data.indexOf(" ")+1,data.lastIndexOf(" ")));
			dungeonID = Integer.parseInt(data.substring(data.lastIndexOf(" ")+1));
			// kopieert de pointers in een andere array zodat we die hier kunnen manipuleren
			//TODO
			//mobs = Data.hostileAreas.get(dungeonID).getMobs();
			//mobs[indexOfMob].addHP(-amount);
			return true;
		}
		
		
		if(message.contains("HEAL: ")){
			data = message.substring(6);
			amount = Integer.parseInt(data.substring(data.indexOf(" ")+1));
			RPGMain.speler.addHP(amount);
			JOptionPane.showMessageDialog(null, "You get healed for " + amount, "Healed", JOptionPane.INFORMATION_MESSAGE);
			return true;
		}
		
		
		if(message.contains("REW: ")){
			amount = Integer.parseInt(message.substring(message.indexOf(" ")+1,message.lastIndexOf(" ")));
			gold = Integer.parseInt(message.substring(message.lastIndexOf(" ")+1));
			//RPGMain.speler.addExperience(amount);
			RPGMain.speler.addGoud(gold);
			JOptionPane.showMessageDialog(null, "You gained " + amount + " experience, and " + gold + " gold.", "Experience", JOptionPane.INFORMATION_MESSAGE);
			return true;
		}
		
		
		if(message.contains("PN: ")){
			data = message.substring(4);
			/*
			 * Returns a string representation of this collection. 
			 * The string representation consists of a list of the collection's elements in the order they are returned by its iterator,
			 * enclosed in square brackets ("[]"). Adjacent elements are separated by the characters ", " (comma and space). 
			 */
			String naam = data;
			int index = 1;
			try{
				while(data.substring(index).length()>0){
					naam = data.substring(index, data.indexOf("]"));
					if(!Global.playerNames.contains(naam)){
						Global.playerNames.add(naam);
					}
					index+= naam.length()+3;
				}
			} catch (StringIndexOutOfBoundsException e){
				
			}
			return true;
		}
		
		
		if(message.contains("REQTRADE: ")){// request to trade
			sender = message.substring(10);
			Global.wait = true;
			if(RPGMain.speler.getOpenForTrade() == false){
				Global.message("CHTRADE: -1 " + sender);
				return true;
			}
			int choice = JOptionPane.showConfirmDialog(null, "Accept trade request from " + sender + "?", "Trade Request", JOptionPane.YES_NO_OPTION);
			Global.message("CHTRADE: " + choice + " " + sender);
			if(choice == JOptionPane.YES_OPTION){
				// show inventory contents
				int keuze = 0;
			
				RPGMain.speler.showInventory();
				RPGMain.printText(false,(RPGMain.speler.getInventorySize()+1) + ": Nothing\n>");
				//TODO
				synchronized(this){
					while(true){
						try{
							keuze = Integer.parseInt(RPGMain.waitForMessage());
							myTradeItemIndex = keuze-1;
							break;
						} catch(IndexOutOfBoundsException e){
							continue;
						} catch(InputMismatchException e){
							RPGMain.printText(true,"\nWrong input.");
							continue;
						}
					}
				}
				if(keuze != (RPGMain.speler.getInventorySize()+1)){
					myTradeItemName = RPGMain.speler.getInventoryItem(keuze-1).getName();
					myTradeItemID = RPGMain.speler.getInventoryItem(keuze-1).getID();
					RPGMain.printText(true,"You want to trade " + myTradeItemName + "." +
							"\nWaiting for response...");
					Global.message("ITEMID: " + myTradeItemID + " " + sender);
				}
				else{
					RPGMain.printText(true,"You want to give nothing.\nWaiting for response...");
					Global.message("ITEMID: -1 " + sender);
					myTradeItemName = "nothing";
				}
			}
			Global.wait = false;
			return true;
		}
		
		
		if(message.contains("CHTRADE: ")){// response from other trading player, in requesting player's thread
				int choice = Integer.parseInt(message.substring(9,message.lastIndexOf(" ")));
				if(choice == -1){
					JOptionPane.showMessageDialog(null, "Both players must be in the same town.", "Error", JOptionPane.ERROR_MESSAGE);
					return true;
				}
				if(choice == JOptionPane.YES_OPTION){
					RPGMain.printText(true,"\nTrade request accepted.");
					RPGMain.speler.showInventory();
					RPGMain.printText(false,(RPGMain.speler.getInventorySize()+1) + ": Nothing\n>");
					int keuze;
					while(true){
						try{
							keuze = Integer.parseInt(RPGMain.waitForMessage());
							myTradeItemIndex = keuze-1;
							break;
						} catch(IndexOutOfBoundsException e){
							continue;
						} catch(NumberFormatException e){
							continue;
						}
					}
					try{
						myTradeItemName = RPGMain.speler.getInventoryItem(keuze-1).getName();
						myTradeItemID = RPGMain.speler.getInventoryItem(keuze-1).getID();
						RPGMain.printText(true,"You want to trade " + myTradeItemName + "." +
								"\nWaiting for response...");
						Global.message("ITEMID: " + myTradeItemID);
					} catch(NullPointerException np){
						RPGMain.printText(true,"Wrong input.");
					} catch(ArrayIndexOutOfBoundsException e){
						RPGMain.printText(true,"You want to give nothing.\nWaiting for response...");
						Global.message("ITEMID: -1");
						myTradeItemName = "nothing";
					}
				}
				else{
					RPGMain.printText(true,"\nTrade request denied.");
				}
			return true;
		}
		
		
		if(message.contains("ITEMID: ")){
				data = message.substring(8);
				otherTradeItemID = Integer.parseInt(data.substring(0,data.lastIndexOf(" ")));
				sender = data.substring(data.indexOf(" ")+1);
				int choice;
				if(otherTradeItemID != -1){
					choice = JOptionPane.showConfirmDialog(null, sender + " wants to trade " + Data.equipment.get(otherTradeItemID).getName() + ".", "Item trade", JOptionPane.OK_CANCEL_OPTION);
					otherTradeItemName = Data.equipment.get(otherTradeItemID).getName();
				}
				else{
					choice = JOptionPane.showConfirmDialog(null,sender + " wants to give nothing.", null, JOptionPane.OK_CANCEL_OPTION);
					otherTradeItemName = "nothing";
				}
				if(choice == JOptionPane.OK_OPTION)	{acceptedTrade = true;}
				else {acceptedTrade = false;}
				RPGMain.printText(true,"Sender in ITEMID: " + sender);
				Global.message("FINDEC: " + choice + " " + sender);
			return true;
		}
		
		if(message.contains("FINDEC: ")){
			data = message.substring(8);
			int decision = Integer.parseInt(data.substring(0,data.indexOf(" ")));
			if(decision == JOptionPane.OK_OPTION && acceptedTrade == true){
				JOptionPane.showMessageDialog(null, "Trading " + myTradeItemName + " for " + otherTradeItemName + ".","Trade summary",JOptionPane.INFORMATION_MESSAGE);
				if(!myTradeItemName.equalsIgnoreCase("nothing")){
					RPGMain.speler.delInventoryItem(myTradeItemIndex);
				}
				try{
					RPGMain.speler.addInventoryItem(Data.equipment.get(otherTradeItemID));
				} catch(NullPointerException np){
				} catch(ArrayIndexOutOfBoundsException e){
				}
				JOptionPane.showMessageDialog(null, "Trade Successful", "Succes!", JOptionPane.INFORMATION_MESSAGE);
			}
			Global.wait = false;
			return true;
		}
		return false;
	}
}

