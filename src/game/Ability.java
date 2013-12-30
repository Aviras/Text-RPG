package game;

import java.awt.Point;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.log4j.Logger;

import bsh.EvalError;

public class Ability implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int ID, duration, cooldown;
	private String name, description, type, command, scriptFile;
	private String[] possibleTargets;
	private int[] range,cost;
	
	private static final Logger logger = Logger.getLogger(Ability.class);
	
	public Ability(Integer ID, String name, String command, String type, String possibleTargets, Integer duration, Integer cooldown, int[] range, int[] cost, String scriptFile){
		this.ID = ID;
		this.name = name;
		this.command = command;
		this.range = range;
		this.type = type;
		this.possibleTargets = possibleTargets.split(";");
		this.duration = duration;
		this.cooldown = cooldown;
		this.cost = cost;
		this.scriptFile = scriptFile;
	}

	public boolean activate(Wezen actor, Wezen target, int[] actorPos, int[] targetPos, String aimFor, HashMap<double[][], Double> covers, double[] coefficients){

		logger.info("Activating ability " + name + " by " + actor.getName());
		//TODO check requirements
		//checks for cooldown
		if(actor.getAbilities().get(this) == 0){
			//distance is now independent of window size, only relative size. Width of map is always defined as 15m.
			double meter = GameFrameCanvas.dungeonMap.getWidth()/15;
			double distance = new Point(actorPos[0],actorPos[1]).distance(targetPos[0],targetPos[1])/meter;
			if(distance >= range[0]){
				if(distance <= range[1]){
					try {
						
						Global.beanShell.set("actor", actor);
						Global.beanShell.set("target", target);
						Global.beanShell.set("actorPos", actorPos);
						Global.beanShell.set("targetPos", targetPos);
						Global.beanShell.set("actionTarget", aimFor);
						Global.beanShell.set("enemyPortrait", GameFrameCanvas.enemyPortrait);
						Global.beanShell.set("dungeonMap", GameFrameCanvas.dungeonMap);
						Global.beanShell.set("distance", distance);
						Global.beanShell.set("covers", covers);
						Global.beanShell.set("hitAbility", Data.abilities.get(0));
						Global.beanShell.set("logger", logger);
						Global.beanShell.set("coefficients", coefficients);
						
						Object o = Global.beanShell.source(scriptFile);
						
						boolean b = true;
						try{
							b = Boolean.parseBoolean(o.toString());
						} catch(Exception e){
						}
						
						actor.setAbilityCooldown(this,cooldown);
						logger.info("Ability completed");
						return b;
						
					} catch (IOException e) {
						e.printStackTrace();
						logger.debug(e);
					} catch (EvalError e) {
						e.printStackTrace();
						logger.debug(e);
					}
				}
				else{
					RPGMain.printText(true, "Target is too far away to do that.");
				}
			}
			else{
				RPGMain.printText(true, "Target is too close to do that.");
			}
		}
		else{
			RPGMain.printText(true, "You are not recovered yet from the last time you did that.");
		}
		logger.info("Ability failed");
		return false;
	}
	
	/* GETTERS */
	public int getID(){
		return ID;
	}
	public int getDuration(){
		return duration;
	}
	public int getCooldown(){
		return cooldown;
	}
	public String getName(){
		return name;
	}
	public String getCommand(){
		return command;
	}
	public String getDescription(){
		return description;
	}
	public String getType(){
		return type;
	}
	public String[] getTargets(){
		return possibleTargets;
	}
	public int[] getRange(){
		return range;
	}
	public int[] cost(){
		return cost;
	}
}
