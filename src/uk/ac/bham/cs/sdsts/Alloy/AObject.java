package uk.ac.bham.cs.sdsts.Alloy;

import java.util.HashMap;

public abstract class AObject{
	protected AObject _this;
	protected HashMap<String, Object> _notebook; // used to store some temporary items when generating alloy code
	
	public abstract String toString();
	
<<<<<<< HEAD
=======
	// used to store some temporary items when generating alloy code
	protected HashMap<String, Object> _notebook; 
	
	public abstract String toString();
	
	// return Attribute and Name, e.g. one LifelineA
>>>>>>> parent of 36da53d... fixed: *(COVER.COVER)
	public abstract String getAName();
	
	public abstract String getName();
	
	public String zone = "other"; // used for separating items to different zone to make the result clearer
	
	public void setThis(AObject object){
		this._this = object;
	}
	public boolean isSameWith(AObject object){
		if(object == object._this)
			return _this == object;
		if(this == _this)
			return _this == object._this;
		return false;
	}
	public void addNote(String string, Object object){
		if(_notebook == null)_notebook = new HashMap<String, Object>();
		if(!_notebook.containsKey(string))
			_notebook.put(string, object);
	}
	public Object getNote(String string){
		return _notebook.get(string);
	}
}
