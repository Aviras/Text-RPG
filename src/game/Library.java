package game;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Library extends DistrictLocation implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Integer> IDs = new ArrayList<Integer>();
	private File bookFile = new File("Data/Books.xml");
	
	private static final Logger logger = Logger.getLogger(Library.class);
	
	public Library(String name, String description, String bookIDs){
		super(name,description);
		String[] parts = bookIDs.split(";");
		for(String s:parts){
			try{
				IDs.add(Integer.parseInt(s));
			}catch(NumberFormatException e){
				continue;
			}
		}
	}
	public Library(String name, String description, String bookIDs, String npcIDs){
		super(name,description);
		String[] parts = bookIDs.split(";");
		for(String s:parts){
			try{
				IDs.add(Integer.parseInt(s));
			}catch(NumberFormatException e){
				continue;
			}
		}
		npcs = new ArrayList<NPC>();
		parts = npcIDs.split(";");
		for(String s: parts){
			try{
				npcs.add(Data.NPCs.get(Integer.parseInt(s)));
			}catch(NumberFormatException e){
				continue;
			}
		}
	}
	
	public void enter() throws InterruptedException{
		RPGMain.printText(true, description);
		Global.pauseProg();
		if(!npcs.isEmpty()){
			while(true){
				for(NPC n: npcs){
					RPGMain.printText(true, new String[]{"* ", "Talk to ", n.getName()}, new String[]{"regular","bold","regular"});
				}
				RPGMain.printText(false, new String[]{"* ", "Read ", " a book\n* Cancel\n>"}, new String[]{"regular","bold","regular"});
				
				String input = RPGMain.waitForMessage().toLowerCase();
				if(input.startsWith("talk to")){
					String name = RPGMain.upperCaseSingle(input.split(" ")[2],0);
					
					for(NPC n: npcs){
						if(n.getName().equalsIgnoreCase(name)){
							logger.debug("talking to " + name);
							n.talk();
							break;
						}
					}
				}
				else if(input.equalsIgnoreCase("read")){
					readBooks();
				}
				else if(input.equalsIgnoreCase("cancel")){
					RPGMain.printText(true, "You walk out of the " + name + ".");
					break;
				}
			}
		}
		else{
			readBooks();
		}
	}
	
	public void readBooks(){
		while(true){
			RPGMain.printText(false, "What book do you want to read?");
			ArrayList<String> titles = new ArrayList<String>();
			for(int j=0;j<IDs.size();j++){
				int i = IDs.get(j);
				String[] path = {"book,id:" + i,"name"};
				String title = Global.rwtext.getContent(bookFile, path);
				path[1] = "author";
				String author = Global.rwtext.getContent(bookFile, path);
				titles.add(title + " by " + author);
				RPGMain.printText(false, "\n" + (j+1) + ": " + titles.get(j));
			}
			RPGMain.printText(false, "\n" + (titles.size()+1) + ": Go back" + "\n>");
			int choice = 0;
			try{
				choice = Integer.parseInt(RPGMain.waitForMessage())-1;
			}catch(NumberFormatException e){
				continue;
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			if(choice == titles.size()){
				break;
			}
			else if(choice > titles.size() || choice < 0){
				RPGMain.printText(true, "Not a valid option.");
			}
			else{
				String[] path = {"book,id:" + IDs.get(choice), "Content"};
				Global.makeDialog(bookFile, path);
				String[] logbookExtra = {path[0],"Log","path"};
				try{
					String logbookPath = Global.rwtext.getContent(bookFile, logbookExtra);
					logbookExtra[2] = "summary";
					String[] content = Global.rwtext.getContent(bookFile, logbookExtra).split(";");
					Logbook.addContent(logbookPath, Integer.parseInt(content[0]), content[1]);
				} catch(ArrayIndexOutOfBoundsException e){
					logger.error("Missing logbook data in book with ID " + IDs.get(choice),e);
					System.err.println("Missing data in book");
				} catch(NumberFormatException e){
					logger.error("Missing logbook data in book with ID " + IDs.get(choice),e);
					System.err.println("Missing data in book");
				} catch(Exception e){
					e.printStackTrace();
					logger.error(e);
				}
				continue;
			}
		}
	}

}
