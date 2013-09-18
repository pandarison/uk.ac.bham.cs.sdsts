package uk.ac.bham.cs.sdsts.core.synthesis;

import java.util.ArrayList;

public class Equality {
	private ArrayList<Object> A;
	private ArrayList<Object> B;
	private ArrayList<String> Prefix_A, Prefix_B;
	
	public void initial(){
		A = new ArrayList<Object>();
		B = new ArrayList<Object>();
		Prefix_A = new ArrayList<String>();
		Prefix_B = new ArrayList<String>();
	}
	
	public void insert(String prefix_a, Object a, String prefix_b, Object b){
		Prefix_A.add(prefix_a);
		Prefix_B.add(prefix_b);
		A.add(a);
		B.add(b);
	}
	
	public int size(){
		return A.size();
	}
	
	public boolean isIn_B(Object b){
		return B.contains(b);
	}
	public Object getAbyB(Object b){
		int p = B.indexOf(b);
		return A.get(p);
	}
	public String getPrefixByObject(Object o){
		int p = A.indexOf(o);
		if(p != -1)return Prefix_A.get(p);
		else {
			p = B.indexOf(o);
			return Prefix_B.get(p);
		}
	}
}
