package game;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JOptionPane;

public class ConnectThread extends Thread {

	private int port;
	protected ServerSocket server = null;
	private Socket client = null;
	
	public ConnectThread(ServerSocket server){
		this.server = server;
		port = server.getLocalPort();
		start();
	}
	public void run(){
		RPGMain.printText(true,"Waiting for connection on port " + port + "...");
		while(Global.numberOnline <= 4){
			try {
				client = server.accept();
				String clientIP = client.getInetAddress().getHostName();
				DataOutputStream dout = new DataOutputStream(client.getOutputStream());
				if(!Global.isServer){// connection from 
					int accept = JOptionPane.showConfirmDialog(null, "Connect with " + clientIP + "?", "Permission to connect", JOptionPane.YES_NO_OPTION);
					
					if(accept != JOptionPane.YES_OPTION){
						dout.writeUTF("Connection Refused.");
						removeConnection(client);
					}
					else{//connection confirmed
						Global.online = true;
						Global.numberOnline++;
						Global.outputStreams.put(client, dout);
						dout.writeUTF("Connection Accepted");
						new ServerThread(client);
					}
				}
				else{// connection to self
					JOptionPane.showMessageDialog(null, "Connection established.", "Success", JOptionPane.INFORMATION_MESSAGE);
					Global.isServer = false;
					Global.outputStreams.put(client, dout);
					new ServerThread(client);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		RPGMain.printText(true,"Maximum amount of players reached.");
	}
	static Enumeration<DataOutputStream> getOutputStreams(Socket s) {
		return Global.outputStreams.elements();
	}
	public static void sendToAll(String message,Socket sender){
		synchronized( Global.outputStreams ) {
			// For each client ...
			for (Enumeration<DataOutputStream> e = getOutputStreams(sender); e.hasMoreElements(); ) {
				// ... get the output stream ...
				DataOutputStream dout = (DataOutputStream)e.nextElement();
				// ... and send the message
				try {
					if(sender != null && dout != Global.outputStreams.get(sender)) dout.writeUTF( message );
					if(sender == null) dout.writeUTF(message);
				} catch( IOException ie ) { ie.printStackTrace(); }
			}
		}
	}
	public static void sendDataToAll(String[] args){//moet ook andere functie komen voor inlezen van data, ipv messagebox
		
	}
	
	public static void sendPlayerNames(Socket s){
		
		ArrayList<String> names = new ArrayList<String>();
		String naam = null;
		synchronized(Global.playerData){
			for(Enumeration<String> e = Global.playerData.keys();e.hasMoreElements();){
				naam = (String)e.nextElement();
				if(!Global.playerData.get(naam).equals(s))// eigen naam moet er niet bij
					names.add(naam);
			}
		}
		DataOutputStream dout = Global.outputStreams.get(s);
		try {
			dout.writeUTF("PN: " + names.toString());
			//RPGMain.printText(true,names.toString());
		} catch (IOException e) {
			RPGMain.printText(true,"Unable to write playernames to client.");
			e.printStackTrace();
		}
	}
	static void removeConnection( Socket s ) {
		// Synchronize so we don't mess up sendToAll() while it walks
		// down the list of all output streams
		synchronized( Global.outputStreams ) {
			// Remove it from our hashtable/list
			Global.outputStreams.remove( s );
			//remove from online players
			Global.numberOnline--;
			// Tell everyone he went offline
			Global.message(s.getInetAddress().getHostName() + " has gone offline.");
			//check if we're still in multiplayer
			if(Global.numberOnline <= 1) Global.online = false;
			// Make sure it's closed
			try {
				s.close();
			} catch( IOException ie ) {
				RPGMain.printText(true, "Error closing "+s );
				ie.printStackTrace();
			}
		}
	}

}
