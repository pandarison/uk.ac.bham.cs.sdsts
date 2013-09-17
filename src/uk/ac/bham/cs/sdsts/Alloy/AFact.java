package uk.ac.bham.cs.sdsts.Alloy;

import java.util.ArrayList;

public class AFact extends AObject{
<<<<<<< HEAD
=======
	// format string of the fact
>>>>>>> parent of 36da53d... fixed: *(COVER.COVER)
	private String format;
	private Object[] args;
	
	public AFact(String format, Object... args){
		this.format = format;
		this.args = args;
	}

	@Override
	public String toString() {
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
}
