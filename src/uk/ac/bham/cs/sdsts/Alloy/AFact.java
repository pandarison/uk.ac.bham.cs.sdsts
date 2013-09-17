/***
 *  Author: Yi Chen
 */
package uk.ac.bham.cs.sdsts.Alloy;

import java.util.ArrayList;

// Alloy fact
public class AFact extends AObject{
	
	private ArrayList<AAction> _actions;
	
	// format string of the fact
	private String format;
	
	// object that involved in the fact
	private Object[] args;
	
	public AFact(String format, Object... args){
		this.format = format;
		this.args = args;
	}
	
	public void addAction(AAction action){
		if(_actions == null)
			_actions = new ArrayList<AAction>();
		_actions.add(action);
	}

	@Override
	public String toString() {
		if(_actions != null)
		for (AAction action : _actions) {
			action.run(this);
		}
		ArrayList<String> args1 = new ArrayList<String>();
		for (Object object : args) {
			args1.add(((AObject)object).getName());
		}
		return String.format(format, args1.toArray());
	}

	@Override
	public String getAName() {
		return toString();
	}

	@Override
	public String getName() {
		return null;
	}
	public void setFormat(String format){
		this.format = format;
	}
}
