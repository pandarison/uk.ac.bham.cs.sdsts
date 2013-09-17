/***
 *  Author: Yi Chen
 */
package uk.ac.bham.cs.sdsts.core.sitra_rules;

import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.internal.impl.LifelineImpl;

import uk.ac.bham.cs.sdsts.Alloy.AAttr;
import uk.ac.bham.cs.sdsts.Alloy.AFact;
import uk.ac.bham.cs.sdsts.Alloy.ASig;
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
		Lifeline lifeline = (Lifeline) source;
		String currentSD = AlloyModel.getInstance().getSD();
		
		// add abstract for lifeline
		// abstract sig LIFELINE {}
		ASig lifelineAbstract = AlloyModel.getInstance().getSig("LIFELINE");
		lifelineAbstract.set_attr(AAttr.ABSTRACT).zone = "Abstract";
		
		ASig SD = AlloyModel.getInstance().getSig("_SD_");
		SD.AddField("LIFELINES", new AFact("_SD_ one -> LIFELINE"));
		
		// get the name and class
		String[] fields = lifeline.getName().split(":");
		String lName = fields[0], lClass = fields[0];
		if(fields.length > 1)lClass = fields[1];
		
		// add the class, once for each
		// one sig class {}
		ASig lifelineClass = AlloyModel.getInstance().getSig("CLASS_" + lClass);
		lifelineClass.set_attr(AAttr.ONE).zone = "Classes";
		
		// add the name
		// one sig name{}
		ASig lifelineName = AlloyModel.getInstance().getSig("NAME_" + lName);
		lifelineName.set_attr(AAttr.ONE).zone = "Names";
				
		// add the lifeline
		// one sig LL extends Lifeline {type: class , Name: name}
		ASig lifelineSig = AlloyModel.getInstance().getSig(currentSD + "_" + lName);
		lifelineSig.set_attr(AAttr.ONE).set_parent(lifelineAbstract);
		lifelineSig.AddField("CLASS", lifelineClass).AddField("NAME", lifelineName);
		lifelineSig.zone = "Lifeline";
		return null;
	}

	@Override
	public void setProperties(Object target, Object source, Transformer t) {
		// TODO Auto-generated method stub
	}

}
