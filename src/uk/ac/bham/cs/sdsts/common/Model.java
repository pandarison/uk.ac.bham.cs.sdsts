package uk.ac.bham.cs.sdsts.common;

public abstract class Model {
	
	public abstract String getFilename(); // good.ext
	
	public abstract String getFilepath();  // full path includes the name /sys/asd/file.ext
	
	public abstract String getName(); // good
	
	public abstract void save();
		
	public abstract String saveState();
	
	public abstract void restore(String str); 
	
}
