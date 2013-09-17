/***
 *  Author: Yi Chen
 */
package uk.ac.bham.cs.sdsts.core.sitra_rules;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.internal.impl.InteractionOperandImpl;
import uk.ac.bham.cs.sdsts.Alloy.AAttr;
import uk.ac.bham.cs.sdsts.Alloy.ASig;
import uk.ac.bham.cs.sdsts.core.synthesis.AlloyModel;
import uk.ac.bham.sitra.Rule;
import uk.ac.bham.sitra.RuleNotFoundException;
import uk.ac.bham.sitra.Transformer;

@SuppressWarnings({ "rawtypes", "restriction" })
public class InteractionOperand2Alloy implements Rule {

	@Override
	public boolean check(Object source) {
		if (source instanceof InteractionOperandImpl)
			return true;
		else
			return false;
	}

	@Override
	public Object build(Object source, Transformer t) {
		InteractionOperand interactionOperand = (InteractionOperand) source;
		String currentSD = AlloyModel.getInstance().getSD();
		String currentSD_ = currentSD + "_";
						
		// add the operand
		// one sig op1 extends OPERAND{}
		ASig operandAbstract = AlloyModel.getInstance().getSig("OPERAND");
		ASig OperandSig = AlloyModel.getInstance().getSig(currentSD_ + interactionOperand.getName());
		OperandSig.set_attr(AAttr.ONE);
		OperandSig.set_parent(operandAbstract);
		OperandSig.zone = "Operand";
		
		// iterate messages
		ASig combinedFragmentAbstract = AlloyModel.getInstance().getSig("COMBINEDFRAGMENT");
		ArrayList<Message> messages = new ArrayList<Message>();
		HashMap<String, ASig> lastElementOnLifeline = new HashMap<String, ASig>();
		ArrayList<ASig> CFs = new ArrayList<ASig>();
		ArrayList<ASig> nonCFs = new ArrayList<ASig>();
		for (InteractionFragment interactionFragment : interactionOperand.getFragments()) {
			if(interactionFragment instanceof MessageOccurrenceSpecification){
				MessageOccurrenceSpecification event = (MessageOccurrenceSpecification) interactionFragment;
				if(!messages.contains(event.getMessage())){
					event.getMessage().getSendEvent().setName(interactionOperand.getName() + "_" + event.getMessage().getSendEvent().getName());
					event.getMessage().getReceiveEvent().setName(interactionOperand.getName() + "_" + event.getMessage().getReceiveEvent().getName());
					messages.add(event.getMessage());
					try {
						t.transform(event.getMessage());
					} catch (RuleNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				for (Lifeline lifeline : interactionFragment.getCovereds()) {
					ASig fragmentSig = AlloyModel.getInstance().getSig(currentSD_ + interactionFragment.getName());
					if(lastElementOnLifeline.containsKey(lifeline.getName())){
						ASig lastElement = lastElementOnLifeline.get(lifeline.getName());
						if(lastElement.get_parent().equals(combinedFragmentAbstract)){
							AlloyModel.getInstance().addFact("all _L:%s, _E1:%s.COVER.COVER, _E2:%s | _E1.COVER=_L => _E2 in _E1.^BEFORE", AlloyModel.getInstance().getSig(currentSD_ + lifeline.getName()), lastElement, fragmentSig).zone = "Ordering";
						}
						else
							AlloyModel.getInstance().addFact("%s in %s.^BEFORE", fragmentSig, lastElement).zone = "Ordering";
					}
					lastElementOnLifeline.put(lifeline.getName(), fragmentSig);
				}
			}
			if(interactionFragment instanceof CombinedFragment){
				interactionFragment.setName(interactionOperand.getName() + "_" + interactionFragment.getName());
				for (Lifeline lifeline : interactionFragment.getCovereds()) {
					ASig fragmentSig = AlloyModel.getInstance().getSig(currentSD_ + interactionFragment.getName());
					if(lastElementOnLifeline.containsKey(lifeline.getName())){
						ASig lastElement = lastElementOnLifeline.get(lifeline.getName());
						if(lastElement.get_parent().equals(combinedFragmentAbstract)){
							AlloyModel.getInstance().addFact("all _L:%s, _E1:%s.COVER.COVER, _E2:%s.COVER.COVER | _E1.COVER=_L and _E2.COVER=_L => _E2 in _E1.^BEFORE", AlloyModel.getInstance().getSig(currentSD_ + lifeline.getName()), lastElement, fragmentSig).zone = "Ordering";
						}
						else
							AlloyModel.getInstance().addFact("all _L:%s, _E1:%s, _E2:%s.COVER.COVER | _E2.COVER=_L => _E2 in _E1.^BEFORE", AlloyModel.getInstance().getSig(currentSD_ + lifeline.getName()), lastElement, fragmentSig).zone = "Ordering";
					}
					lastElementOnLifeline.put(lifeline.getName(), fragmentSig);
				}
				try {
					t.transform(interactionFragment);
				} catch (RuleNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			ASig fragmentSig = AlloyModel.getInstance().getSig(currentSD_ + interactionFragment.getName());
			AlloyModel.getInstance().addFact("all _F: %s | _F in %s.*(COVER.COVER).COVER", fragmentSig, OperandSig).zone = "Covering: Operand->Fragment";
			if(interactionFragment instanceof CombinedFragment)CFs.add(fragmentSig);
			if(interactionFragment instanceof MessageOccurrenceSpecification)nonCFs.add(fragmentSig);
		}
		for (ASig aSig : CFs) {
			for (ASig aSig1 : CFs) {
				if(aSig1.equals(aSig))continue;
				AlloyModel.getInstance().addFact("all _F: %s | _F !in %s.^(COVER.COVER)", aSig1, aSig).zone = "Covering: Operand->Fragment";
			}
			for (ASig aSig2 : nonCFs) {
				AlloyModel.getInstance().addFact("all _F: %s | _F !in %s.^(COVER.COVER)", aSig2, aSig).zone = "Covering: Operand->Fragment";
			}
		}
		
		return OperandSig;
	}

	@Override
	public void setProperties(Object target, Object source, Transformer t) {
		// TODO Auto-generated method stub

	}

}
