package game;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.sound.sampled.*;

import org.apache.log4j.Logger;

public class SoundEngine {

	private static float musicGain,combatGain,effectsGain,ambientGain,weatherGain;
	private static SourceDataLine musicLine; 
	private static HashMap<String,DataLine> ambientLines,effectsLines,combatLines,weatherLines;
	private static Logger logger = Logger.getLogger(SoundEngine.class);

	//TODO loop system doesn't work

	public SoundEngine(){
		ambientLines = new HashMap<String,DataLine>();
		effectsLines = new HashMap<String,DataLine>();
		combatLines = new HashMap<String,DataLine>();
		weatherLines = new HashMap<String,DataLine>();
		musicGain = -10;
		ambientGain = -5;
		effectsGain = 0;
		weatherGain = -5;
	}

	public static void closeLine(String fileName, String type){
		if(type.equalsIgnoreCase("ambient")){
			for(String file:ambientLines.keySet()){
				if(file.equalsIgnoreCase(fileName)){
					ambientLines.get(file).stop();
					ambientLines.get(file).flush();
					ambientLines.get(file).close();
				}
			}
		}
		else if(type.equalsIgnoreCase("effects")){
			for(String file:effectsLines.keySet()){
				if(file.equalsIgnoreCase(fileName)){
					effectsLines.get(file).stop();
					effectsLines.get(file).flush();
					effectsLines.get(file).close();
				}
			}
		}
		else if(type.equalsIgnoreCase("combat")){
			for(String file:combatLines.keySet()){
				if(file.equalsIgnoreCase(fileName)){
					combatLines.get(file).stop();
					combatLines.get(file).flush();
					combatLines.get(file).close();
				}
			}
		}
		else if(type.equalsIgnoreCase("weather")){
			for(String file:weatherLines.keySet()){
				if(file.equalsIgnoreCase(fileName)){
					weatherLines.get(file).stop();
					weatherLines.get(file).flush();
					weatherLines.get(file).close();
				}
			}
		}
	}

	public static boolean setLineGain(String fileName,String type, float gain){

		if(type.equalsIgnoreCase("ambient")){
			for(String file:ambientLines.keySet()){
				if(file.equalsIgnoreCase(fileName)){
					setLineGain(gain,ambientLines.get(file));
					return true;
				}
			}
		}
		else if(type.equalsIgnoreCase("effects")){
			for(String file:effectsLines.keySet()){
				if(file.equalsIgnoreCase(fileName)){
					setLineGain(gain,effectsLines.get(file));
					return true;
				}
			}
		}
		else if(type.equalsIgnoreCase("combat")){
			for(String file:combatLines.keySet()){
				if(file.equalsIgnoreCase(fileName)){
					setLineGain(gain,combatLines.get(file));
					return true;
				}
			}
		}
		else if(type.equalsIgnoreCase("weather")){
			for(String file:weatherLines.keySet()){
				if(file.equalsIgnoreCase(fileName)){
					setLineGain(gain,weatherLines.get(file));
					return true;
				}
			}
		}
		return false;
	}

	public void playSound(ArrayList<String> fileNames, String type, int loop, int[] delay){
		new MusicPlayer(fileNames,type,loop,delay);
	}
	public void playSound(ArrayList<String> fileNames, String type, int loop, int[] delay,float gain){
		new MusicPlayer(fileNames,type,loop,delay,gain);
	}
	public void playSound(String fileName, String type, int loop, int delay, float gain, boolean newPlay){
		if(newPlay || !setLineGain(fileName,type,gain)){
			new MusicPlayer(fileName,type,loop,delay,gain);
		}
	}
	public void chooseAndPlaySound(String weather, String locationType, double lightIntensity, boolean outside,String[] type, int loop, int delay){

		Random generator = new Random();
		ArrayList<String> fileNames = new ArrayList<String>();

		//System.out.println("Locationtype: " + locationType);

		//MUSIC
		if(type[0].equalsIgnoreCase("music")){
			//different moods
			if(locationType.equalsIgnoreCase("town")){
				fileNames.add("Sounds/Music/Warrior's theme - take 1.wav");
			}
			else if(locationType.startsWith("forest")){
				fileNames.add("Sounds/Music/forrest theme - take 1.wav");
			}
			else if(locationType.equalsIgnoreCase("dungeon")){
				fileNames.add("Sounds/Music/Not As It Seems.mp3");
			}

			if(!fileNames.isEmpty()){
				//System.out.println("Playing " + fileNames.get(0));
				int[] delayArray = new int[fileNames.size()];
				for(int i=0;i<delayArray.length;i++){
					delayArray[i] = delay;
				}
				new MusicPlayer(fileNames,"music",loop,delayArray);
			}
		}

		//AMBIENT
		else if(type[0].equalsIgnoreCase("ambient")){

			if(locationType.equalsIgnoreCase("town")){

				if(lightIntensity > 0){
					fileNames.add("Sounds/Ambient/village_bells.mp3");
				}
				else{
					int index = generator.nextInt(3);
					switch(index){
					case 0: fileNames.add("Sounds/Ambient/night_wolves.wav"); break;
					case 1: fileNames.add("Sounds/Ambient/night_owls.mp3"); break;
					case 2: fileNames.add("Sounds/Ambient/night_crickets_dogs.aif"); break;
					}
				}

			}
			else if(locationType.equalsIgnoreCase("inn")){

			}
			else if(locationType.equalsIgnoreCase("smith")){
				if(lightIntensity > 0){

				}
			}
			else if(locationType.startsWith("forest")){
				if(lightIntensity > 0){
					fileNames.add("Sounds/Ambient/forest_birds_woodpecker.mp3");
				}
				else{

				}
			}


			if(!fileNames.isEmpty()){
				int[] delayArray = new int[fileNames.size()];
				for(int i=0;i<delayArray.length;i++){
					delayArray[i] = delay;
				}
				new MusicPlayer(fileNames,"ambient",loop,delayArray);
			}
		}

		//EFFECT
		else if(type[0].equalsIgnoreCase("effects")){
			if(type[1].equalsIgnoreCase("voice")){
				if(type[2].equalsIgnoreCase("greet")){
					if(type[3].equalsIgnoreCase("female")){
						fileNames.add("Sounds/Voices/f00_hello_there.mp3");
					}
					else{
						fileNames.add("Sounds/Voices/m00_hello.mp3");
					}
				}
				else if(type[2].equalsIgnoreCase("farewell")){
					if(type[3].equalsIgnoreCase("female")){
						fileNames.add("Sounds/Voices/f00_goodbye.mp3");
					}
					else{
						fileNames.add("Sounds/Voices/m00_goodbye.mp3");
					}
				}
			}
			else if(type[1].equalsIgnoreCase("footsteps")){
				System.err.println(locationType);
				if(locationType.startsWith("forest")){
					//TODO
					if(weather.contains("snow")){
						fileNames.add("Sounds/Effects/footsteps_snow.mp3");
					}
					else{
						fileNames.add("Sounds/Effects/footsteps_leaves_short.mp3");
					}
				}
				else if(locationType.equalsIgnoreCase("town")){
					fileNames.add("Sounds/Effects/footsteps_concrete.mp3");
				}
			}
			if(!fileNames.isEmpty()){
				int[] delayArray = new int[fileNames.size()];
				for(int i=0;i<delayArray.length;i++){
					delayArray[i] = delay;
				}
				new MusicPlayer(fileNames,"effects",loop,delayArray);
			}
		}

		//COMBAT
		else if(type[0].equalsIgnoreCase("combat")){

			if(type[1].equalsIgnoreCase("")){
				fileNames.add("");
			}

			if(!fileNames.isEmpty()){
				int[] delayArray = new int[fileNames.size()];
				for(int i=0;i<delayArray.length;i++){
					delayArray[i] = delay;
				}
				new MusicPlayer(fileNames,"combat",loop,delayArray);
			}
		}

		//WEATHER
		else if(type[0].equalsIgnoreCase("weather")){

			//System.err.println("Weather at soundEngine: " + weather);

			String[] weatherComponents = weather.split(" ");

			if(weatherComponents[1].equalsIgnoreCase("windy")){
				fileNames.add("Sounds/Weather/heavy_wind.mp3");
			}
			else if(weatherComponents[1].equalsIgnoreCase("storm")){
				fileNames.add("Sounds/Weather/heavy_wind.mp3");
			}

			if(weatherComponents[0].equalsIgnoreCase("rain")){
				if(weatherComponents[1].equalsIgnoreCase("windstill")){
					fileNames.add("Sounds/Weather/rain_town.mp3");
				}
				else if(weatherComponents[1].equalsIgnoreCase("windy")){
					fileNames.add("Sounds/Weather/rain_town.mp3");
				}
				else{
					fileNames.add("Sounds/Weather/heavy_wind_rain.mp3");
				}
			}
			if(weatherComponents[0].equalsIgnoreCase("snow")){

			}

			if(!fileNames.isEmpty()){
				int[] delayArray = new int[fileNames.size()];
				for(int i=0;i<delayArray.length;i++){
					delayArray[i] = delay;
				}
				new MusicPlayer(fileNames,"weather",loop,delayArray);
			}
		}
	}

	public void fadeLines(String type){
		if(type.equalsIgnoreCase("music")){
			if(musicLine != null)
				new Fade(musicLine,musicGain);
		}
		else if(type.equalsIgnoreCase("ambient")){
			for(DataLine c:ambientLines.values()){
				if(c != null)
					new Fade(c,ambientGain);
			}
		}
		else if(type.equalsIgnoreCase("combat")){
			for(DataLine c:combatLines.values()){
				if(c != null)
					new Fade(c,combatGain);
			}
		}
		else if(type.equalsIgnoreCase("effects")){
			for(DataLine c:effectsLines.values()){
				if(c != null)
					new Fade(c,effectsGain);
			}
		}
		else if(type.equalsIgnoreCase("weather")){
			for(DataLine c:weatherLines.values()){
				if(c != null)
					new Fade(c,weatherGain);
			}
		}
	}
	public static void setLineGain(float g,DataLine line){
		FloatControl gainCtrl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
		gainCtrl.setValue(g);
	}

	public static void setMusicGain(float g){
		musicGain = g;
		setLineGain(musicGain,musicLine);
	}
	public static void setEffectGain(float g){
		effectsGain = g;
		for(DataLine effectsLine:effectsLines.values()){
			setLineGain(effectsGain,effectsLine);
		}
	}

	public static void setAmbientGain(float g){
		ambientGain = g;
		for(DataLine ambientLine:ambientLines.values()){
			setLineGain(ambientGain,ambientLine);
		}
	}

	public static void setCombatGain(float g){
		combatGain = g;
		for(DataLine combatLine:combatLines.values()){
			setLineGain(combatGain,combatLine);
		}
	}
	public static void setWeatherGain(float g){
		weatherGain = g;
		for(DataLine weatherLine:weatherLines.values()){
			setLineGain(weatherGain,weatherLine);
		}
	}

	class MusicPlayer extends Thread{

		private String type;
		private String[] fileNames;
		private float gain = -100;
		private int loop,index;
		private int[] delay;
		private final int INITLOOP;
		private DataLine dLine = null;

		public MusicPlayer(ArrayList<String> fileNames,String type, int loop, int[] delay,float gain){
			this.fileNames = new String[fileNames.size()];
			for(int i=0;i<fileNames.size();i++){
				this.fileNames[i] = fileNames.get(i);
			}
			this.type = type;
			this.INITLOOP = loop;
			this.loop = loop;
			this.delay = delay;
			this.gain = gain;
			start();
		}
		public MusicPlayer(String fileName,String type, int loop, int delay, float gain){
			this.fileNames = new String[]{fileName};
			this.type = type;
			this.INITLOOP = loop;
			this.loop = loop;
			this.delay = new int[]{delay};
			this.gain = gain;
			start();
		}

		public MusicPlayer(ArrayList<String> fileNames,String type, int loop, int[] delay){
			this.fileNames = new String[fileNames.size()];
			for(int i=0;i<fileNames.size();i++){
				this.fileNames[i] = fileNames.get(i);
			}
			this.type = type;
			this.INITLOOP = loop;
			this.loop = loop;
			this.delay = delay;
			start();
		}
		public void run(){
			while(loop >= 0 && index < fileNames.length){
				logger.debug("New cycle of " + fileNames[index]);
				playSound();
				if(loop < 0){
					logger.debug("Incrementing index for " + fileNames[index]);
					loop = INITLOOP;
					index++;
				}
				try{
					if(!dLine.isOpen()){
						logger.debug("Returning because of externally closed line.");
						loop = -1;
						index++;
					}
				} catch(NullPointerException e){
					logger.debug("Returning because of nullpointerexception on line.");
					loop = -1;
					index++;
				}
				if(type.equalsIgnoreCase("ambient")){
					removeClosed(ambientLines);
				}
				else if(type.equalsIgnoreCase("combat")){
					removeClosed(combatLines);
				}
				else if(type.equalsIgnoreCase("effects")){
					removeClosed(effectsLines);
				}
				else if(type.equalsIgnoreCase("weather")){
					removeClosed(weatherLines);
				}
			}
			try{
				logger.debug("Thread for " + fileNames[index-1] + " has ended.");
			} catch(ArrayIndexOutOfBoundsException e){
				logger.debug("Thread ended with ioob exception. " + fileNames[index-2]);
				e.printStackTrace();
				logger.error(e);
			}
		}
		private void playSound(){
			FloatControl volCtrl = null;
			//Clip clip = null;
			SourceDataLine line = null;

			logger.debug("In new playSound method for " + fileNames[index]);

			if(gain == -100){
				logger.debug("Adjusting gain for " + fileNames[index]);
				if(type.equalsIgnoreCase("music")){
					gain = musicGain;
				}
				else if(type.equalsIgnoreCase("combat")){
					gain = combatGain;
				}
				else if(type.equalsIgnoreCase("effects")){
					gain = effectsGain;
				}
				else if(type.equalsIgnoreCase("ambient")){
					gain = ambientGain;
				}
				else if(type.equalsIgnoreCase("weather")){
					gain = weatherGain;
				}
			}

			try{
				if(musicLine.isOpen() && type.equalsIgnoreCase("music")){
					if(!musicLine.isActive()){
						musicLine.stop();
						musicLine.flush();
						musicLine.close();
					}
					else{
						new Fade(musicLine,gain);
					}
				}
			} catch(NullPointerException e){
			}

			logger.debug("New Line for " + fileNames[index] + " " + line + ", looping " + loop + " times.");

			try {
				//System.out.println(fileNames[index]);
				AudioInputStream in = AudioSystem.getAudioInputStream(new File(fileNames[index]));
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

				line = getLine(decodedFormat);
				addLine(line);

				volCtrl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
				volCtrl.setValue(gain);

				try{
					logger.debug(type + " delay " + delay[index]);
					sleep(delay[index]);
				} catch(InterruptedException e){
					e.printStackTrace();
					logger.error(e);
				}
				if(line.isOpen()){
					try{
						rawplay(decodedFormat, din,line);
						loop--;
					} catch(Exception e){
						e.printStackTrace();
						logger.error(e);
					}
				}
				else{
					loop = -1;
				}
				//close lines
				in.close();
				din.close();
			} catch (UnsupportedAudioFileException e1) {
				e1.printStackTrace();
				logger.error(e1);
				loop = -1;
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.error(e1);
				loop = -1;
			} catch(LineUnavailableException e){
				e.printStackTrace();
				logger.error(e);
				loop = -1;
			} catch (ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
				logger.error(e);
				loop = -1;
			} catch(Exception e){
				e.printStackTrace();
				logger.error(e);
				loop = -1;
			}
		}

		private int rawplay(AudioFormat targetFormat, AudioInputStream din, SourceDataLine line) throws IOException, LineUnavailableException{
			logger.debug("In Rawplay for " + fileNames[index] + " " + din + " " + line);
			byte[] data = new byte[4096];
			if (line != null)
			{
				logger.debug("Flushing and starting lines for " + fileNames[index]);
				// Start
				line.flush();
				line.start();
				int nBytesRead = 0, nBytesWritten = 0;
				while (nBytesRead != -1)
				{
					nBytesRead = din.read(data, 0, data.length);
					if (nBytesRead != -1){
						nBytesWritten = line.write(data, 0, nBytesRead);

					}
				}
				if(loop == 0){
					// Stop after 500ms, else line gets closed before audio can play if it's a short sample
					try {
						sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					logger.debug("Stopping and closing lines for " + fileNames[index]);
					line.stop();
					line.flush();
					din.close();
				}
				return nBytesWritten;
			}
			return 0;
		}
		// get an opened SourceDataLine for the specified audioFormat
		private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException
		{
			SourceDataLine res = null;
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
			res = (SourceDataLine) AudioSystem.getLine(info);
			res.open(audioFormat);
			return res;
		}
		// add the line to the HashMap depending on the type, so it is easy to manipulate individually later
		public void addLine(DataLine line){
			dLine = line;
			if(type.equalsIgnoreCase("music")){
				musicLine = (SourceDataLine)line;
			}
			else if(type.equalsIgnoreCase("combat")){
				combatLines.put(fileNames[index],dLine);
			}
			else if(type.equalsIgnoreCase("effects")){
				effectsLines.put(fileNames[index],dLine);
			}
			else if(type.equalsIgnoreCase("ambient")){
				ambientLines.put(fileNames[index],dLine);
			}
			else if(type.equalsIgnoreCase("weather")){
				weatherLines.put(fileNames[index],dLine);
			}
		}
		//remove all closed lines from the HashMap
		public void removeClosed(HashMap<String,DataLine> hashMap){
			HashMap<String,DataLine> h = new HashMap<String,DataLine>();
			h.putAll(hashMap);
			for(String key:h.keySet()){
				if(!h.get(key).isOpen()){
					hashMap.remove(key);
				}
			}
		}
	}

	class Fade extends Thread{

		private DataLine line1,line2;
		private float gain;

		public Fade(DataLine line1, DataLine line2, float gain){
			this.line1 = line1;
			this.line2 = line2;
			this.gain = gain;
			//System.out.println("Fading: " + line1 + " " + line2);
			start();
		}
		public Fade(DataLine line,float gain){
			if(!line.isActive()){
				//System.err.println("Stopping inactive line");
				line.stop();
				line.flush();
				line.close();
			}
			else{
				line1 = line;
				this.gain = gain;
				//System.out.println("Fading: " + line1);
				start();
			}
		}

		public void run(){
			try{
				FloatControl volCtrl1 = (FloatControl)line1.getControl(FloatControl.Type.MASTER_GAIN);
				FloatControl volCtrl2 = null;
				try{
					volCtrl2 = (FloatControl)line2.getControl(FloatControl.Type.MASTER_GAIN);
				} catch(NullPointerException e){
				}
				for(float i=gain;i>-50;i--){
					try {
						sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					volCtrl1.setValue(i);
					try{
						volCtrl2.setValue(-1*i-50);
					} catch(NullPointerException e){
						continue;
					}
				}
				line1.stop();
				line1.flush();
				line1.close();
				if(!line1.isOpen()){
					//System.out.println("Succesful " + line1);
				}
				//System.out.println("Past all for " + line1);
			} catch(NullPointerException e){
			}
		}
	}
	/*else{
		DataLine.Info info = new DataLine.Info(Clip.class, din.getFormat());
		clip = (Clip)Audio//System.getLine(info);
		addLine(clip);
		//System.out.println("New Line for " + fileNames[index] + " " + clip);

		try{
			volCtrl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
		} catch(IllegalArgumentException e){
		}
		try{
			volCtrl.setValue(gain);
		} catch(Exception e){
		}

		clip.open(din);
		clip.addLineListener(this);

		in.close();
		din.close();
		try{
			//System.out.println(type + " delay " + delay[index]);
			sleep(delay);
		} catch(InterruptedException e){
		}
		if(clip.isOpen()){
			clip.start();
			clip.loop(loop);
			loop = -1;
		}
		else{
			loop = -1;
			return;
		}
    }*/
}
