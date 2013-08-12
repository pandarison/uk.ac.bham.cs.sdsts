package uk.ac.bham.cs.sdsts.common;

public abstract class Model {
	
	public abstract String getFilename();
	
	public abstract String getId();
	
	public abstract String getName();
	
	public abstract void save();
		
	public abstract String saveState();
	
	public abstract void restore(String str); 
	
}
