package game;
import java.io.*;

public class Equipment extends Item implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

		private int sterkte,kwaliteit,type,amount,maxKwaliteit;
		private String treat,weaponType;
		private Poison poison;
		/*TYPE
		*0 = geen equipment
		*1 = wapen 1H
		*2 = wapen 2H
		*3 = schild
		*4 = boog 
		*5 = helm
		*6 = harnas
		*7 = handschoenen
		*8 = broek
		*9 = schoenen
		*10 = 1H sword
		*11 = 1H axe
		*12 = 1H club
		*20 = 2H sword
		*21 = 2H axe
		*22 = 2H club
		**/
		private String[] types = {"Nothing","One-handed","Two-handed","Shield","Bow","Helmet","Chest armour","Gloves","Leggings","Shoes"};
		private String[] weaponTypes = {"Sword","Axe","Club"};
		
		public Equipment(Integer id,String name,Integer strength,Integer cost,Integer durability,Integer type,Double weight){
			super(id,name,"",weight,cost);
			sterkte = strength.intValue();
			maxKwaliteit = durability.intValue();
			kwaliteit = maxKwaliteit;
			this.type = type.intValue();
			if(this.type >= 10){
				weaponType = weaponTypes[type.intValue()%10];
				this.type=Math.round(this.type/10);
			}
		}
		public Equipment(Integer id,String name,Integer strength,Integer cost,Integer durability,Integer type,Double weight,String treat,Integer amount){// voor stat-enhancing items
			super(id,name,"",weight,cost);
			sterkte = strength.intValue();
			maxKwaliteit = durability.intValue();
			kwaliteit = maxKwaliteit;
			this.type = type.intValue();
			if(this.type >= 10){
				weaponType = weaponTypes[type.intValue()%10];
				this.type=Math.round(this.type/10);
			}
			this.amount = amount.intValue();
			this.treat = RPGMain.upperCaseSingle(treat,0);
		}
		public Equipment(Integer id,String name,Integer strength,Integer cost,Integer durability,Integer type,Double weight,String treat,Integer amount, String logbookPath, String logbookSummary){// voor stat-enhancing items
			super(id,name,"",weight,cost,logbookPath,logbookSummary);
			sterkte = strength.intValue();
			maxKwaliteit = durability.intValue();
			kwaliteit = maxKwaliteit;
			this.type = type.intValue();
			if(this.type >= 10){
				weaponType = weaponTypes[type.intValue()%10];
				this.type=Math.round(this.type/10);
			}
			this.amount = amount.intValue();
			this.treat = RPGMain.upperCaseSingle(treat,0);
		}
		public Equipment(Equipment another){
			this.ID = another.ID;
			this.sterkte = another.sterkte;
			this.cost = another.cost;
			this.maxKwaliteit = another.maxKwaliteit;
			this.kwaliteit = another.kwaliteit;
			this.name = another.name;
			this.type = another.type;
			this.weaponType = another.weaponType;
			this.weight = another.weight;
			this.amount = another.amount;
			this.treat = another.treat;
			this.logbookPath = another.logbookPath;
			this.logbookSummary = another.logbookSummary;
		}
		
		/*GETTERS*/
		public String getName(){
			double d;
			if(maxKwaliteit == 0)
				d = 1;
			else
				d = (double)kwaliteit/(double)maxKwaliteit;
			if(d > 0.75)
				return name;
			else if(d > 0.5)
				return "Worn " + name;
			else if(d > 0.25)
				return "Damaged " + name;
			else
				return "Ruined " + name;
		}
		public int getStrength(){
			return sterkte;
		}
		public int getKwaliteit(){
			return kwaliteit;
		}
		public int getType(){
			return type;
		}
		public String getFullType(){
			String s = types[type];
			if(weaponType != null){
				s+= " " + weaponType;
			}
			return s;
		}
		public String getWeaponType(){
			return weaponType;
		}
		public String getTreat(){
			if(treat != null){
				return treat + " " + amount;
			}
			return null;
		}
		public int getMaxKwaliteit(){
			return maxKwaliteit;
		}
		/*SETTERS*/
		public void setStrength(int x){
			sterkte = x;
		}
		public void setWeight(double x){
			weight = x;
		}
		public void addCost(int x){
			cost+=x;
		}
		public void setKwaliteit(int x){
			kwaliteit+=x;
		}
		public void showInfo(){
			RPGMain.printText(true,"-------------------------");
			if(poison != null){
				RPGMain.printText(true,"- Poisoned " + name);
			}
			else{
				RPGMain.printText(true, "- " + name);
			}
			RPGMain.printText(true,"-------------------------");
			RPGMain.printText(true,"Strength: " + sterkte);
			RPGMain.printText(true,"Durability: " + kwaliteit + "/" + maxKwaliteit);
			RPGMain.printText(true,"Value: " + cost);
			RPGMain.printText(true,"Weight: " + weight);
			RPGMain.printText(false,"Type: " + types[type]);
			if(weaponType != null){
				RPGMain.printText(true, " " + weaponType);
			}
			else{
				RPGMain.printText(true,"");
			}
			if(treat != null){
				RPGMain.printText(true,"Increases " + treat + " by " + amount + ".");
			}
			if(poison != null){
				//TODO change colour
				RPGMain.printText(true, "Coated with " + poison.getName() + ". " + poison.getDescription());
			}
		}
		public void addKwaliteit(int x){
			kwaliteit+=x;
			if(kwaliteit < 0)
				kwaliteit = 0;
		}
		@Override
		public void use() throws InterruptedException{
			RPGMain.speler.equipItem(this);
		}
		public Poison getPoison(){
			return poison;
		}
		
		public void setPoison(String type, String name, String description, int effect, String effectType, int charges){
			poison = new Poison(type,name,description,effect,effectType,charges);
		}

		// POISONS ARE AWESOME
		class Poison{
			private String type,name,description, effectType;
			private int effect,charges, currentPhase;
			
			public Poison(String type, String name, String description, int effect, String effectType, int charges){
				this.type = type;
				this.name = name;
				this.description = description;
				this.effect = effect;
				this.effectType = effectType;
				this.charges = charges;
				currentPhase = 0;
			}
			public void activate(Wezen victim){
				if(currentPhase < charges){
					if(type.equalsIgnoreCase("debuff")){
						victim.addBuff(name, effectType, effect, charges, 5000, description);
					}
					else{
						RPGMain.printText(true, description.replace("victimName", victim.getName()));
						//TODO more different effects
						if(effectType.equalsIgnoreCase("damage")){
							victim.addHP(-effect);
						}
						else if(effectType.equalsIgnoreCase("paralysis")){
							victim.dimMovement(effect);
						}
						else if(effectType.endsWith("reduction")){
							String stat = effectType.split(" ",2)[0];
							if(stat.equalsIgnoreCase("strength")){
								victim.increaseStat(0, -effect);
							}
							else if(stat.equalsIgnoreCase("dexterity")){
								victim.increaseStat(1, -effect);
							}
							else if(stat.equalsIgnoreCase("intellect")){
								victim.increaseStat(2, -effect);
							}
							else if(stat.equalsIgnoreCase("charisma")){
								victim.increaseStat(3, -effect);
							}
						}
					}
					currentPhase++;
				}
				else{
					poison = null;
				}
			}
			public String getDescription(){
				return description;
			}
			public int getCurrentPhase(){
				return currentPhase;
			}
			public int getCharges(){
				return charges;
			}
			public void setCharges(int i){
				charges = i;
			}
			public void addCharges(int i){
				charges+=1;
			}
			public void addToCurrentPhase(int i){
				currentPhase+=i;
			}
			public String getEffectType(){
				return effectType;
			}
			public String getName(){
				return name;
			}
		}
}
