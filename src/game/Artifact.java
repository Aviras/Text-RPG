package game;

public class Artifact extends Item {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int worth, difficulty, amount;
	private String effect;
	
	public Artifact(Integer id, String name, String description, String effect, Integer amount, Integer worth, Integer difficulty, String logbookpath, String logbooksummary){
		super(id,name,description,0.0,-1,logbookpath,logbooksummary);
		
		this.effect = effect;
		this.amount = amount;
		this.worth = worth;
		this.difficulty = difficulty;
		
	}
	
	public Artifact(Integer id, String name, String description, Double weight, Integer cost, String effect, Integer amount, Integer worth, Integer difficulty, String logbookpath, String logbooksummary){
		super(id,name,description,weight,cost,logbookpath,logbooksummary);
		
		this.effect = effect;
		this.amount = amount;
		this.worth = worth;
		this.difficulty = difficulty;
	}
	
	public Artifact(Artifact another){
		this.ID = another.ID;
		this.name = another.name;
		this.description = another.description;
		this.effect = another.effect;
		this.amount = another.amount;
		this.worth = another.worth;
		this.difficulty = another.difficulty;
		this.logbookPath = another.logbookPath;
		this.logbookSummary = another.logbookSummary;
		
		this.weight = another.weight;
		this.cost = another.cost;
	}
	
	
	
	

}
