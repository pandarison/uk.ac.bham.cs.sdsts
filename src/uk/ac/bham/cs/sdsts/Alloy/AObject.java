/***
 *  Author: Yi Chen
 */
package uk.ac.bham.cs.sdsts.Alloy;

import java.util.HashMap;

public abstract class AObject{
	
	protected AObject _this;
	
	// used to store some temporary items when generating alloy code
	protected HashMap<String, Object> _notebook; 
		
	// return Attribute and Name, e.g. one LifelineA
	public abstract String getAName();
	
	// return only Name, e.g. LifelineA
	public abstract String getName();
	
	// used for separating items to different zone to make the result clearer
	public String zone = "other"; 
	
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
