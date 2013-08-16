package uk.ac.bham.cs.sdsts.Alloy;

public abstract class AObject{
	protected AObject _this;
	
	public abstract String toString();
	
	public abstract String getAName();
	
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
}
