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
		String currentSD_ = currentSD + "_";
		try {
			t.transformAll(interaction.getOwnedElements());
		} catch (RuleNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			// create _SD_
			ASig SD = AlloyModel.getInstance().getSig("_SD_");
			SD.set_attr(AAttr.ONE).zone = "SD";
			
			// iterate messages
			ArrayList<Message> messages = new ArrayList<Message>();
			for (InteractionFragment interactionFragment : interaction.getFragments()) {
				if(interactionFragment instanceof MessageOccurrenceSpecification){
					MessageOccurrenceSpecification event = (MessageOccurrenceSpecification) interactionFragment;
					if(!messages.contains(event.getMessage())){
						t.transform(event.getMessage());
						messages.add(event.getMessage());
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
