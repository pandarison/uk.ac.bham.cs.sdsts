package uk.ac.bham.cs.sdsts.core.sitra_rules;


import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.InteractionOperatorKind;
import org.eclipse.uml2.uml.internal.impl.CombinedFragmentImpl;

import uk.ac.bham.cs.sdsts.Alloy.AAttr;

import uk.ac.bham.cs.sdsts.Alloy.ASig;
import uk.ac.bham.cs.sdsts.core.synthesis.AlloyModel;
import uk.ac.bham.sitra.Rule;
import uk.ac.bham.sitra.RuleNotFoundException;
import uk.ac.bham.sitra.Transformer;

@SuppressWarnings({ "rawtypes", "restriction" })
public class CombinedFragment2Alloy implements Rule {

	@Override
	public boolean check(Object source) {
		if (source instanceof CombinedFragmentImpl) {
			return true;
		} else
			return false;
	}

	@Override
	public Object build(Object source, Transformer t) {
		try {
			CombinedFragment combinedFragment = (CombinedFragment) source;
			String currentSD = AlloyModel.getInstance().getSD();
			String currentSD_ = currentSD + "_";
			// add abstract for combinedFragment
			// abstract sig COMBINEDFRAGMENT {}
			ASig combinedFragmentAbstract = AlloyModel.getInstance().getSig("COMBINEDFRAGMENT");
			combinedFragmentAbstract.set_attr(AAttr.ABSTRACT);
			combinedFragmentAbstract.zone = "abstract";
			
			// add sig for combinedFragment type
			ASig cfType = null;
			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.PAR_LITERAL){
				cfType = AlloyModel.getInstance().getSig("CombiedFragmentType_Par");
			}
			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.ALT_LITERAL){
				cfType = AlloyModel.getInstance().getSig("CombinedFragmentType_Alt");
			}
			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.LOOP_LITERAL){
				cfType = AlloyModel.getInstance().getSig("CombinedFragmentType_Loop");
			}
			cfType.set_attr(AAttr.ONE);
			cfType.zone = "class";
			
			// add sig for combinedFragment
			// one sig SDid_CFname {op1:operand1, op2:operand2 ...}
			ASig combinedFragmentSig = AlloyModel.getInstance().getSig(currentSD_ + combinedFragment.getName());
			combinedFragmentSig.AddField("type", cfType);
			combinedFragmentSig.set_parent(combinedFragmentAbstract);
			combinedFragmentSig.zone = "combinedFragment";
			int i = 1;
			for (InteractionOperand interactionOperand : combinedFragment.getOperands()) {
				ASig interactionOperandSig = (ASig) t.transform(interactionOperand);
				combinedFragmentSig.AddField("op" + i++, interactionOperandSig);
			}
			
			// add order among operands of the combined fragment for Alt
			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.ALT_LITERAL){
				for (i = 0; i < combinedFragment.getOperands().size(); i++) {
					for (int j = i+1; j < combinedFragment.getOperands().size(); j++) {
						InteractionOperand op1 = combinedFragment.getOperands().get(i);
						InteractionOperand op2 = combinedFragment.getOperands().get(j);
						ASig op1Sig = AlloyModel.getInstance().getSig(currentSD_ + op1.getName());
						ASig op2Sig = AlloyModel.getInstance().getSig(currentSD_ + op2.getName());
//						fact {all _E:sd1_InteractionOperand.cov | no _E1: sd1_InteractionOperand0.cov | _E in _E1.ISBEFORE}
//						fact {all _E:sd1_InteractionOperand0.cov | no _E1: sd1_InteractionOperand.cov | _E in _E1.ISBEFORE}
						AlloyModel.getInstance().addFact("all _E:%s.cov | no _E1: %s.cov | _E in _E1.ISBEFORE", op1Sig, op2Sig).zone = "cf_alt_relation_removal";
						AlloyModel.getInstance().addFact("all _E:%s.cov | no _E1: %s.cov | _E in _E1.ISBEFORE", op2Sig, op1Sig).zone = "cf_alt_relation_removal";
					}
				}
			}			
			
		} catch (RuleNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public void setProperties(Object target, Object source, Transformer t) {
		
	}

}
