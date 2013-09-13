/***
 *  Author: Yi Chen
 */
package uk.ac.bham.cs.sdsts.Alloy;

// Attribution of Alloy signatures
public enum AAttr {
	ONE, LONE, ABSTRACT, SET;
	@Override
	public String toString(){
		switch (this) {
		case ONE:return "one ";
		case LONE:return "lone ";
		case ABSTRACT:return "abstract ";
		case SET:return "set ";
		default:return "";
		}
	}
}
