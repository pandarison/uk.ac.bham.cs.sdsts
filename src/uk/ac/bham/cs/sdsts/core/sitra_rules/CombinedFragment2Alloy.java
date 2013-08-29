package uk.ac.bham.cs.sdsts.core.sitra_rules;


import java.util.ArrayList;

import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.InteractionOperatorKind;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.internal.impl.CombinedFragmentImpl;
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
			ASig fragmentAbstract = AlloyModel.getInstance().getSig("FRAGMENT");
			ASig operandAbstract = AlloyModel.getInstance().getSig("OPERAND");
			ASig combinedFragmentAbstract = AlloyModel.getInstance().getSig("COMBINEDFRAGMENT");
			combinedFragmentAbstract.set_attr(AAttr.ABSTRACT);
			combinedFragmentAbstract.set_parent(fragmentAbstract);
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
			
			// Fact: CF.Type = Type
			AlloyModel.getInstance().addFact("%s.TYPE = %s", combinedFragmentSig, cfType).zone = "Binding: Combined Fragment Type";
			
			// Relations among operands
			for (InteractionOperand interactionOperand : combinedFragment.getOperands()) {
				interactionOperand.setName(combinedFragment.getName() + "_" + interactionOperand.getName());
				ASig interactionOperandSig = (ASig) t.transform(interactionOperand);
				// Fact Combined Fragment covers Operand
				AlloyModel.getInstance().addFact("%s in %s.COVER", interactionOperandSig, combinedFragmentSig).zone = "Covering: Combined Fragment->Operand";
				
				// Special for ALT
				if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.ALT_LITERAL){
					interactionOperandSig.set_attr(AAttr.LONE);
					for (Message message : getMessagesInOperand(interactionOperand)) {
						ASig messageSig = AlloyModel.getInstance().getSig(currentSD_ + message.getName());
						messageSig.set_attr(AAttr.LONE);
						AlloyModel.getInstance().addFact("#%s=#%s", messageSig, interactionOperandSig).zone = "Number: Message = Operand";
					}
				}
			}
			/**
			***  Constraint: Combined Fragment
			**/
			// only one Operand can interact with fragment outside the CF
			AlloyModel.getInstance().addFact("// only one Operand can interact with fragment outside the CF\nfact{all _CF: COMBINEDFRAGMENT, _OP2: OPERAND | lone _OP1: _CF.COVER | some _E: _OP1.COVER | all _E1: _OP1.COVER | ((_E in _OP2.COVER.BEFORE or _OP2.COVER in _E.BEFORE) and _OP2 !in _CF.COVER) or #_E1.BEFORE=0 }").zone = "Constraint: Combined Fragment";
			// order before CF and other Fragment
			AlloyModel.getInstance().addFact("// order before CF and other Fragment\nfact{all _CF: COMBINEDFRAGMENT, _E1: _CF.COVER.COVER, _E2: EVENT | (_CF in _E2.BEFORE and _E1.COVER=_E2.COVER ) =>_E1 in _E2.^BEFORE}").zone = "Constraint: Combined Fragment";
			// if one fragment has relation with CF, then its event has relation with CF's events
			AlloyModel.getInstance().addFact("// if one fragment has relation with CF, then its event has relation with CF's events\nfact{all _E1: EVENT, _CF: COMBINEDFRAGMENT, _E2: EVENT| _CF in _E1.BEFORE => not (_E2 in _E1.BEFORE and _E2 !in _CF.COVER.COVER and _E2.COVER=_E1.COVER)}").zone = "Constraint: Combined Fragment";
			// shouldn't be before its children
			AlloyModel.getInstance().addFact("// shouldn't be before its children\nfact{all _CF: COMBINEDFRAGMENT | no _E: _CF.COVER.COVER |  _CF in _E.BEFORE or _E in _CF.BEFORE}\nfact{all _OP: OPERAND | _OP !in _OP.COVER.COVER}").zone = "Constraint: Combined Fragment";
			// one Operand can interact with at most One other Operand
			AlloyModel.getInstance().addFact("// one Operand can interact with at most One other Operand\nfact{all _CF: COMBINEDFRAGMENT, _OP1: _CF.COVER, _OP2: _CF.COVER, _E1: _OP1.COVER,_E2: _OP2.COVER, _E3: _OP1.COVER | no _E4: _OP2.COVER | _OP1 != _OP2 and _E2 in _E1.BEFORE and _E3 in _E4.BEFORE  }").zone = "Constraint: Combined Fragment";
			// all CF can have at most one Next and can be Next of at most one Fragment
			AlloyModel.getInstance().addFact("// all CF can have at most one Next and can be Next of at most one Fragment\nfact{all _CF: COMBINEDFRAGMENT | all _L: LIFELINE | lone _E: EVENT | _E.COVER=_L and _CF in _E.BEFORE}\nfact{all _CF: COMBINEDFRAGMENT | all _L: LIFELINE | lone _E: EVENT | _E.COVER=_L and _CF in _E.BEFORE}").zone = "Constraint: Combined Fragment";
			// all CF have no relation with Lifeline which it does not cover
			AlloyModel.getInstance().addFact("// all CF have no relation with Lifeline which it does not cover\nfact{all _E: EVENT | all _CF: COMBINEDFRAGMENT | some _E1: EVENT | (_E in _CF.BEFORE or _CF in _E.BEFORE) => (_E1.COVER = _E.COVER and _E1 in _CF.COVER.COVER)}").zone = "Constraint: Combined Fragment";
			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.ALT_LITERAL)
				AlloyModel.getInstance().addFact("// alt: exact one operand will be executed\nfact{all _CF: COMBINEDFRAGMENT | (_CF.TYPE = CF_TYPE_ALT) => #_CF.COVER = 1}").zone = "Constraint: Combined Fragment";
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
