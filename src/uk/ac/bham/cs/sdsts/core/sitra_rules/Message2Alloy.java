package uk.ac.bham.cs.sdsts.core.sitra_rules;



import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.internal.impl.MessageImpl;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Attr;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;

import uk.ac.bham.cs.sdsts.Alloy.AAttr;
import uk.ac.bham.cs.sdsts.Alloy.AFact;
import uk.ac.bham.cs.sdsts.Alloy.ASig;
import uk.ac.bham.cs.sdsts.core.synthesis.AlloyModel;
import uk.ac.bham.sitra.Rule;
import uk.ac.bham.sitra.RuleNotFoundException;
import uk.ac.bham.sitra.Transformer;

@SuppressWarnings({ "rawtypes", "restriction", "unused" })
public class Message2Alloy implements Rule{

	@Override
	public boolean check(Object source) {
		if(source instanceof MessageImpl)
			return true;
		else return false;
	}

	@Override
	public Object build(Object source, Transformer t) {
		Message message = (Message) source;
		String currentSD = AlloyModel.getInstance().getSD();
		String currentSD_ = currentSD + "_";
		
		// add abstract for Message
		// abstract sig MESSAGE {}
		ASig messageAbstract = AlloyModel.getInstance().getSig("MESSAGE");
		messageAbstract.set_attr(AAttr.ABSTRACT);
		messageAbstract.zone = "abstract";
		
		ASig SD = AlloyModel.getInstance().getSig("_SD_");
		SD.AddField("MESSAGE", new AFact("_SD_ one -> MESSAGE"));
		
		// add abstract for event
		// one sig abstract event{isbefore: set event}
		ASig eventAbstract = AlloyModel.getInstance().getSig("EVENT");
		eventAbstract.set_attr(AAttr.ABSTRACT);
		eventAbstract.AddField("ISBEFORE", eventAbstract.setOf());	
		eventAbstract.zone = "abstract";
		
		// add fact to avoid circle
		AlloyModel.getInstance().addFact("\n//avoid the circle\nfact {all e:EVENT  | e !in e.^ISBEFORE}").zone = "other";
		AlloyModel.getInstance().addFact("\n//no event can be direct child and subChild of other event at the same time\nfact {no _E: EVENT, _E1: EVENT | _E in _E1.ISBEFORE.^ISBEFORE and _E in _E1.ISBEFORE}");
		
		// add sender and receiver events
		// one sig senderName extends event{occur : LL}
		MessageOccurrenceSpecification mFrom = (MessageOccurrenceSpecification) message.getSendEvent();
		MessageOccurrenceSpecification mTo = (MessageOccurrenceSpecification) message.getReceiveEvent();
		String lifelineFrom = currentSD_ + mFrom.getCovered(null).getName().split(":")[0];
		String lifelineTo = currentSD_ + mTo.getCovered(null).getName().split(":")[0];
		ASig eventSendSig = AlloyModel.getInstance().getSig(currentSD_ + mFrom.getName());
		eventSendSig.set_attr(AAttr.ONE);
		eventSendSig.set_parent(eventAbstract);
		eventSendSig.AddField("occur", AlloyModel.getInstance().getSig(lifelineFrom));
		eventSendSig.zone = "event";
		ASig eventRecSig = AlloyModel.getInstance().getSig(currentSD_ + mTo.getName());
		eventRecSig.set_attr(AAttr.ONE);
		eventRecSig.set_parent(eventAbstract);
		eventRecSig.AddField("occur", AlloyModel.getInstance().getSig(lifelineTo));
		eventRecSig.zone = "event";
		
		// add the fact send is before receive
		//Expr fact = event_send.in(event_receive.join(event_receive.parent.getFields().get(0))).not();
		AlloyModel.getInstance().addFact("%s in %s.ISBEFORE", eventRecSig, eventSendSig).zone = "order";
		
		// add the name
		// one sig name{}
		ASig messageName = AlloyModel.getInstance().getSig("NAME_" + message.getName());
		messageName.set_attr(AAttr.ONE);
		messageName.zone = "name";
		
		// add the message
		ASig messageSig = AlloyModel.getInstance().getSig(currentSD_ + message.getName());
		messageSig.set_attr(AAttr.ONE);
		messageSig.set_parent(messageAbstract);
		messageSig.AddField("send", eventSendSig);
		messageSig.AddField("receive", eventRecSig);
		messageSig.AddField("name", messageName);
		messageSig.zone = "message";
		return null;
	}

	@Override
	public void setProperties(Object target, Object source, Transformer t) {
		// TODO Auto-generated method stub
		
	}

}
