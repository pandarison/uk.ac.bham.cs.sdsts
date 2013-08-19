package uk.ac.bham.cs.sdsts.core.sitra_rules;

import java.util.ArrayList;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
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
		try {
			InteractionOperand interactionOperand = (InteractionOperand) source;
			String currentSD = AlloyModel.getInstance().getSD();
			String currentSD_ = currentSD + "_";
					
			// add abstract for InteractionOperand
			// abstract sig OPERAND{COVER: set MESSAGE}
			ASig messageAbstract = AlloyModel.getInstance().getSig("MESSAGE");
			ASig interactionOperandAbstract = AlloyModel.getInstance().getSig("OPERAND");
			interactionOperandAbstract.set_attr(AAttr.ABSTRACT);
			interactionOperandAbstract.AddField("COVER", messageAbstract.setOf());
			interactionOperandAbstract.zone = "Abstract";
						
			// add the interactionOperand
			// one sig op1 extends OPERAND{}
			ASig interactionOperandSig = AlloyModel.getInstance().getSig(currentSD_ + interactionOperand.getName());
			interactionOperandSig.set_attr(AAttr.ONE);
			interactionOperandSig.set_parent(interactionOperandAbstract);
			interactionOperandSig.zone = "Operand";
			
			// iterate messages
			ArrayList<Message> messages = new ArrayList<Message>();
			for (InteractionFragment interactionFragment : interactionOperand.getFragments()) {
				if(interactionFragment instanceof MessageOccurrenceSpecification){
					MessageOccurrenceSpecification event = (MessageOccurrenceSpecification) interactionFragment;
					if(!messages.contains(event.getMessage())){
						event.getMessage().getSendEvent().setName(interactionOperand.getName() + "_" +event.getMessage().getSendEvent().getName());
						event.getMessage().getReceiveEvent().setName(interactionOperand.getName() + "_" +event.getMessage().getReceiveEvent().getName());
						messages.add(event.getMessage());

						// Fact: message covered by operand
						ASig messageSig = (ASig) t.transform(event.getMessage());
						AlloyModel.getInstance().addFact("%s in %s.COVER", messageSig, interactionOperandSig).zone = "Covering: Operand->Message";
					}				 
				}
			}
			for (int i = 0; i < messages.size() - 1; i++) {
				Message message1 = messages.get(i);
				Message message2 = messages.get(i + 1);
				ASig message1Sig = AlloyModel.getInstance().getSig(currentSD_ + message1.getName());
				ASig message2Sig = AlloyModel.getInstance().getSig(currentSD_ + message2.getName());		
				AlloyModel.getInstance().addFact("%s in %s.^BEFORE", message2Sig, message1Sig).zone = "Ordering";
			}
			
			// Fact: in ONE operand, only one message can have child message outside the operand
			AlloyModel.getInstance().addFact("// in ONE operand, only one message can have child message outside the operand\nfact{all _OP: OPERAND | lone _M: _OP.COVER | _M.BEFORE !in _OP.COVER or #_M.BEFORE=0}").zone = "relation between Message and Operand";
			// all message can be covered by at most one operand
			AlloyModel.getInstance().addFact("// all message can be covered by at most one operand\nfact{all _M: MESSAGE | lone _OP: OPERAND | _M in _OP.COVER}").zone = "relation between Message and Operand";
			return interactionOperandSig;
			
		} catch (RuleNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void setProperties(Object target, Object source, Transformer t) {
		// TODO Auto-generated method stub

	}

}
