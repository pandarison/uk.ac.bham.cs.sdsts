package uk.ac.bham.cs.sdsts.core.sitra_rules;

import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.internal.impl.LifelineImpl;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Attr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;

import uk.ac.bham.cs.sdsts.core.synthesis.AlloyModel;
import uk.ac.bham.sitra.Rule;
import uk.ac.bham.sitra.Transformer;

@SuppressWarnings({ "rawtypes", "restriction" })
public class Lifeline2Alloy implements Rule{

	@Override
	public boolean check(Object source) {
		if(source instanceof LifelineImpl){
			return true;
		}
		else return false;
	}

	@Override
	public Object build(Object source, Transformer t) {
		try {
			Lifeline lifeline = (Lifeline) source;
			
			// add abstract for lifeline
			// abstract sig LIFELINE {}
			PrimSig lifeline_abstract = new PrimSig("LIFELINE", Attr.ABSTRACT);
			lifeline_abstract = (PrimSig) AlloyModel.getInstance().addAbstract(lifeline_abstract);
			
			// get the name and class
			String[] fields = lifeline.getName().split(":");
			String lName = fields[0], lClass = fields[0];
			if(fields.length > 1)lClass = fields[1];
			
			// add the class, once for each
			// one sig class {}
			PrimSig lifeline_class = new PrimSig("CLASS_" + lClass, Attr.ONE);
			AlloyModel.getInstance().addSig(lifeline_class);
			
			// add the name
			// one sig name{}
			PrimSig lifeline_name = new PrimSig("NAME_" + lName, Attr.ONE);
			AlloyModel.getInstance().addSig(lifeline_name);
			
			// add the lifeline
			// one sig LL extends Lifeline {type: class , Name: name}
			PrimSig newLifeline = new PrimSig(AlloyModel.getInstance().getLifeLineID(lifeline.getName()), lifeline_abstract, Attr.ONE);
			newLifeline.addField("type", lifeline_class);
			newLifeline.addField("name", lifeline_name);
			AlloyModel.getInstance().addSig(newLifeline);
			
		} catch (Err e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
//		String alloyString = "one sig ";
//		Lifeline lifeline = (Lifeline) source;
//		alloyString += lifeline.getLabel();
//		alloyString += " extends Lifeline { }";
//		return alloyString;
	}

	@Override
	public void setProperties(Object target, Object source, Transformer t) {
		// TODO Auto-generated method stub
	}

	

}
