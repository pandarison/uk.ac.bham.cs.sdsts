package uk.ac.bham.cs.sdsts.core.sitra_rules;

import java.util.ArrayList;

import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.internal.impl.InteractionImpl;
import uk.ac.bham.cs.sdsts.Alloy.AAttr;
import uk.ac.bham.cs.sdsts.Alloy.ASig;
import uk.ac.bham.cs.sdsts.core.synthesis.AlloyModel;
import uk.ac.bham.sitra.Rule;
import uk.ac.bham.sitra.RuleNotFoundException;
import uk.ac.bham.sitra.Transformer;

@SuppressWarnings({ "rawtypes", "restriction" })
public class Interaction2Alloy implements Rule{

	@Override
	public boolean check(Object source) {
		if(source instanceof InteractionImpl)
			return true;
		else return false;
	}

	@Override
	public Object build(Object source, Transformer t) {
		// transform the members first
		Interaction interaction = (Interaction) source;
		String currentSD = AlloyModel.getInstance().getSD();
		String currentSD_ = AlloyModel.getInstance().getSD() + "_";
		try {
			t.transformAll(interaction.getOwnedElements());
		} catch (RuleNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			// add abstract for event
			// abstract sig event{isbefore: set event}
			ASig eventAbstract = AlloyModel.getInstance().getSig("EVENT");
			eventAbstract.set_attr(AAttr.ABSTRACT);
			eventAbstract.AddField("ISBEFORE", eventAbstract.setOf());
			eventAbstract.zone = "abstract";
			
			// add abstract for InteractionOperand
			// abstract sig INTERACTIONOPERAND {}
			ASig interactionOperandAbstract = AlloyModel.getInstance().getSig("INTERACTIONOPERAND");
			interactionOperandAbstract.set_attr(AAttr.ABSTRACT);
			interactionOperandAbstract.AddField("cov", eventAbstract.setOf());
			interactionOperandAbstract.zone = "abstract";
			
			
			// add sig for SD
			ASig SD = AlloyModel.getInstance().getSig(currentSD);
			SD.set_attr(AAttr.LONE);
			SD.set_parent(interactionOperandAbstract);
			SD.zone = "sd";
			
			ASig SD_merged = AlloyModel.getInstance().getSig("_SD_");
			SD_merged.set_attr(AAttr.ONE);
			SD_merged.set_parent(interactionOperandAbstract);
			SD_merged.zone = "sd";
			
			AlloyModel.getInstance().addFact(String.format("# %s = 0", SD.get_name())).zone = "hidden";
			SD.mergeTo(SD_merged);
			
			// add facts
			// for every message M(j) after M(i) {
			ArrayList<Message> messages = new ArrayList<Message>();
			for (InteractionFragment interactionFragment : interaction.getFragments()) {
				
				if(interactionFragment instanceof MessageOccurrenceSpecification){
					MessageOccurrenceSpecification event = (MessageOccurrenceSpecification) interactionFragment;
					if(!messages.contains(event.getMessage())){
						t.transform(event.getMessage());
						messages.add(event.getMessage());
					}
					// event that covered by interaction
					ASig eventSig = AlloyModel.getInstance().getSig(currentSD_ + interactionFragment.getName());
					AlloyModel.getInstance().addFact("%s in %s.cov", eventSig, SD).zone = "cover";
				}
			}
			for (int i = 0; i < messages.size() - 1; i++) {
				Message message1 = messages.get(i);
				for (int j = i+1; j < messages.size(); j++) {
					Message message2 = messages.get(j);
					MessageOccurrenceSpecification m1send = (MessageOccurrenceSpecification) message1.getSendEvent();
					MessageOccurrenceSpecification m1rec = (MessageOccurrenceSpecification) message1.getReceiveEvent();
					MessageOccurrenceSpecification m2send = (MessageOccurrenceSpecification) message2.getSendEvent();
					MessageOccurrenceSpecification m2rec = (MessageOccurrenceSpecification) message2.getReceiveEvent();
					
					ASig m1sendASig = AlloyModel.getInstance().getSig(currentSD_ + m1send.getName());
					ASig m1recASig = AlloyModel.getInstance().getSig(currentSD_ + m1rec.getName());
					ASig m2sendASig = AlloyModel.getInstance().getSig(currentSD_ + m2send.getName());
					ASig m2recASig = AlloyModel.getInstance().getSig(currentSD_ + m2rec.getName());
					
					if(j == i + 1){
						AlloyModel.getInstance().addFact("%s in %s.ISBEFORE", m2sendASig, m1sendASig).zone = "order";
						AlloyModel.getInstance().addFact("%s in %s.ISBEFORE", m2recASig, m1recASig).zone = "order";
					}
					AlloyModel.getInstance().addFact("%s !in %s.ISBEFORE", m2sendASig, m1recASig).zone = "order";
					AlloyModel.getInstance().addFact("%s !in %s.ISBEFORE", m1recASig, m2sendASig).zone = "order";
				}						
			}
		} catch (RuleNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void setProperties(Object target, Object source, Transformer t) {
		// TODO Auto-generated method stub
	}

}
