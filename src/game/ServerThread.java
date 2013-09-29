package game;
import java.net.*;
import java.io.*;

import javax.swing.JOptionPane;
/* MESSAGE PROTOCOL
 * CLIENT MAKES CHARACTER WITH NEW NAME 'X' => NAME: X
 * CLIENT DOES 'Y' DAMAGE TO MOB AT INDEX 'X' => DMG: X Y
 * CLIENT HEALS ALLY 'X' FOR 'Y' => HEAL: X Y
 * MOB IS DEFEATED, XP DISTRIBUTED, AMOUNT X, GOLD AMOUNT 'Y' => REW: X Y
 * 
 * CONCLUSION: FIRST TARGET, THEN AMOUNT IF APPLICABLE
 * 
 */
public class ServerThread extends Thread {
	
	private Socket client = null;
	private String naamClient = null;
	private String tradeTarget;
	
	public ServerThread(Socket client){
		this.client = client;
		start();
	}
	public void run(){
		try{
			DataInputStream in = new DataInputStream(client.getInputStream());
			
			while(true){
				String message = in.readUTF();
				//...check for data message
				if(checkData(message) != 2){
					ConnectThread.sendToAll(message, client);
				}
			}
		} catch(SocketException se){
			JOptionPane.showMessageDialog(null, "Connection with " + client.getInetAddress().getHostName() + " lost.", "Connection lost", JOptionPane.INFORMATION_MESSAGE);
			Global.message("Connection with " + client.getInetAddress().getHostName() + " lost.");
		} catch(IOException io){
			
		} finally {
			//The connection is closed for one reason or another,
			//so have the server dealing with it
			ConnectThread.removeConnection( client );
		}
	}
	public int checkData(String message){// returns 0 if it's text, 1 if it's data for all, 2 for data directed to single client, doesn't get sent in sendMessageToAll
		String data,target,recipient;
		/*int amount,indexOfMob,dungeonID,gold;
		Enemy[] mobs;*/
		
		if(message.contains("NAME: ")){
			data = message.substring(6);
			Global.playerData.put(data,client);// put socket information together with player names
			naamClient = data;
			return 2;
		}
		if(message.contains("DMG: ")){
			return 1;
		}
		if(message.contains("HEAL: ")){
			data = message.substring(6);
			target = data.substring(0,data.indexOf(""));
			// if directed to client, get client socket out of Hashtable<String,Socket> playerdata,
			// then get outputstream out of Hashtable<Socket,DataOutputStream> Global.outputStreams, then write message
			// execution is handled in MessageInThread from client
			DataOutputStream dout = null;
			try{
				dout = findOutputStream(Global.playerData.get(target));
			} catch(NullPointerException np){
				RPGMain.printText(true,"Couldn't find player \"" + target + "\".");
			}
			try {
				dout.writeUTF(message);
			} catch(NullPointerException np){
				//do nothing, already explained above
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Failed to deliver data", "Error", JOptionPane.ERROR_MESSAGE);
			} finally{
				try {
					dout.flush();
					dout.close();
				} catch (IOException e) {
				}

			}
			return 2;
		}
		/*if(message.contains("REW: ")){
			amount = Integer.parseInt(message.substring(message.indexOf(" ")+1,message.lastIndexOf(" ")));
			gold = Integer.parseInt(message.substring(message.lastIndexOf(" ")+1));
			RPGMain.speler.addExperience(amount);
			RPGMain.speler.addGoud(gold);
			JOptionPane.showMessageDialog(null, "You gained " + amount + " experience, and " + gold + " gold.", "Experience", JOptionPane.INFORMATION_MESSAGE);
			return 1;
		}*/
		if(message.equalsIgnoreCase("REQPN")){
			ConnectThread.sendPlayerNames(client);
			return 2;
		}
		if(message.contains("REQTRADE: ")){// trade request sent to tradeTarget, in sending player's serverThread!
			tradeTarget = message.substring(10);
			DataOutputStream dout = findOutputStream(Global.playerData.get(tradeTarget));
			try {
				dout.writeUTF("REQTRADE: " + naamClient);
			} catch (IOException e) {
			}
			finally{
				try {
					dout.flush();
					dout.close();
				} catch (IOException e) {
				}

			}
			return 2;
		}
		if(message.contains("CHTRADE: ")){// response from other trading player, in responding player's serverThread !
			recipient = message.substring(message.lastIndexOf(" ")+1);
			DataOutputStream dout = null;
			try{
				dout = findOutputStream(Global.playerData.get(recipient));
				dout.writeUTF(message);
			} catch (IOException e){
				JOptionPane.showMessageDialog(null, "Failed to deliver message", "Error", JOptionPane.INFORMATION_MESSAGE);
			}
			finally{
				try {
					dout.flush();
					dout.close();
				} catch (IOException e) {
				} catch(NullPointerException e){
				}

			}
			return 2;
		}
		if(message.contains("ITEMID: ")){// in both requesting and accepting player's serverThread !
				recipient = message.substring(message.lastIndexOf(" ")+1);
				RPGMain.printText(true,"Message in ServerThread ITEMID: " + message);
				if(message.length() > 12){
					message = message.substring(0,message.lastIndexOf(" ")) + " " + naamClient;
					RPGMain.printText(true,"Message after change: " + message);
				}
				DataOutputStream dout = null;
			while(true){
				try{
					dout = findOutputStream(Global.playerData.get(recipient));
				} catch(NullPointerException np){
					recipient = tradeTarget;
					message+=" " + naamClient;
					continue;
				}
				try{
					dout.writeUTF(message);
					break;
				} catch(IOException e){
					RPGMain.printText(true,"Failed to deliver message.");
				}
				finally{
					try {
						dout.flush();
						dout.close();
					} catch (IOException e) {
					}

				}
			}
			return 2;
		}
		
		if(message.contains("FINDEC: ")){
			recipient = message.substring(message.lastIndexOf(" ")+1);
			DataOutputStream dout = findOutputStream(Global.playerData.get(recipient));
			try{
				dout.writeUTF(message);
			} catch(IOException e){
			}
			finally{
				try {
					dout.flush();
					dout.close();
				} catch (IOException e) {
				}

			}
			return 2;
		}
		return 0;
	}
	public DataOutputStream findOutputStream(Socket s){
		return Global.outputStreams.get(s);
	}
}
