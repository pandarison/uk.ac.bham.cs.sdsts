package uk.ac.bham.cs.sdsts.core.sitra_rules;


import java.util.ArrayList;

import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.InteractionOperatorKind;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.internal.impl.CombinedFragmentImpl;
import org.eclipse.uml2.uml.internal.impl.IntervalImpl;

import uk.ac.bham.cs.sdsts.Alloy.AAttr;
import uk.ac.bham.cs.sdsts.Alloy.AFact;

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
			
			// add abstract combined fragment type
			// abstract sig CF_TYPE{}
			ASig combinedFragmentType = AlloyModel.getInstance().getSig("CF_TYPE");
			combinedFragmentType.set_attr(AAttr.ABSTRACT);
			combinedFragmentType.zone = "Abstract";
			
			// add abstract for combinedFragment
			// abstract sig COMBINEDFRAGMENT{COVER: set OPERAND, TYPE: one CF_TYPE}
			ASig operandAbstract = AlloyModel.getInstance().getSig("OPERAND");
			ASig combinedFragmentAbstract = AlloyModel.getInstance().getSig("COMBINEDFRAGMENT");
			combinedFragmentAbstract.set_attr(AAttr.ABSTRACT);
			combinedFragmentAbstract.AddField("COVER", operandAbstract.setOf());
			combinedFragmentAbstract.AddField("TYPE", combinedFragmentType.oneOf());
			combinedFragmentAbstract.zone = "Abstract";
			
			// add existence to SD
			ASig SD = AlloyModel.getInstance().getSig("_SD_");
			SD.AddField("COMBINEDFRAGMENTS", new AFact("_SD_ one -> COMBINEDFRAGMENT"));
			
			// add sig for combinedFragment type
			ASig cfType = null;
			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.PAR_LITERAL){
				cfType = AlloyModel.getInstance().getSig("CF_TYPE_PAR");
			}
			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.ALT_LITERAL){
				cfType = AlloyModel.getInstance().getSig("CF_TYPE_ALT");
				// Fact: only one operand can be executed for Alt
				AlloyModel.getInstance().addFact("// Fact: only one operand can be executed for Alt\nfact{all _CF: COMBINEDFRAGMENT | (_CF.TYPE = CF_TYPE_ALT) =>#_CF.COVER = 1}").zone = "relation between Operand and Combined Fragment";
			}
			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.LOOP_LITERAL){
				cfType = AlloyModel.getInstance().getSig("CF_TYPE_LOOP");
			}
			cfType.set_attr(AAttr.ONE);
			cfType.set_parent(combinedFragmentType);
			cfType.zone = "Combined Fragment Type";
			
			// add sig for combinedFragment
			// one sig cf1 extends COMBINEDFRAGMENT{}
			ASig combinedFragmentSig = AlloyModel.getInstance().getSig(currentSD_ + combinedFragment.getName());
			combinedFragmentSig.set_attr(AAttr.ONE);
			combinedFragmentSig.set_parent(combinedFragmentAbstract);
			combinedFragmentSig.zone = "Combined Fragment";
			
			// Fact: CF -> Type
			AlloyModel.getInstance().addFact("%s.TYPE = %s", combinedFragmentSig, cfType).zone = "Combined Fragment Type Binding";
			
			// Relations among operands
			for (InteractionOperand interactionOperand : combinedFragment.getOperands()) {
				ASig interactionOperandSig = (ASig) t.transform(interactionOperand);
				// Fact Combined Fragment covers Operand
				AlloyModel.getInstance().addFact("%s in %s.COVER", interactionOperandSig, combinedFragmentSig).zone = "Covering: Combined Fragment->Operand";
				
				// Special for ALT
				if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.ALT_LITERAL){
					interactionOperandSig.set_attr(AAttr.LONE);
					for (Message message : getMessagesInOperand(interactionOperand)) {
						ASig messageSig = AlloyModel.getInstance().getSig(currentSD_ + message.getName());
						messageSig.set_attr(AAttr.LONE);
						AlloyModel.getInstance().addFact("#%s=#%s", messageSig, interactionOperandSig).zone = "CF_Alt: Message->Operand";
					}
				}
			}
			
			
			// Fact
			// all operands can be covered by at most one operand
			AlloyModel.getInstance().addFact("// all operands can be covered by at most one operand\nfact{all _OP: OPERAND | lone _CF: COMBINEDFRAGMENT | _OP in _CF.COVER}").zone = "relation between Operand and Combined Fragment";
			// in one CF, only one Operand's message can have child outside the operand
			AlloyModel.getInstance().addFact("// in one CF, only one Operand's message can have child outside the operand\nfact{all _CF: COMBINEDFRAGMENT | lone _M:_CF.COVER.COVER | _M.BEFORE !in _CF.COVER.COVER or #_M.BEFORE=0}").zone = "relation between Operand and Combined Fragment";
		} catch (RuleNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static ArrayList<Message> getMessagesInOperand(InteractionOperand interactionOperand){
		ArrayList<Message> messages = new ArrayList<Message>();
		for (InteractionFragment interactionFragment : interactionOperand.getFragments()) {
			if(interactionFragment instanceof MessageOccurrenceSpecification){
				MessageOccurrenceSpecification event = (MessageOccurrenceSpecification) interactionFragment;
				if(!messages.contains(event.getMessage())){
					event.getMessage().getSendEvent().setName(interactionOperand.getName() + "_" +event.getMessage().getSendEvent().getName());
					event.getMessage().getReceiveEvent().setName(interactionOperand.getName() + "_" +event.getMessage().getReceiveEvent().getName());
					messages.add(event.getMessage());
				}				 
			}
		}
		return messages;
	}

	@Override
	public void setProperties(Object target, Object source, Transformer t) {
		
	}

}
