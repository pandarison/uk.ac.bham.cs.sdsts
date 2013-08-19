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
		
		// add abstract for event
		// abstract sig EVENT {BEFORE: set EVENT, COVER:  LIFELINE}
		ASig lifelineAbstract = AlloyModel.getInstance().getSig("LIFELINE");
		ASig eventAbstract = AlloyModel.getInstance().getSig("EVENT");
		eventAbstract.set_attr(AAttr.ABSTRACT);
		eventAbstract.AddField("BEFORE", eventAbstract.setOf());	
		eventAbstract.AddField("COVER", lifelineAbstract.oneOf());
		eventAbstract.zone = "Abstract";
		
		// add abstract for Message
		// abstract sig MESSAGE{BEFORE: lone MESSAGE, SEND: one EVENT, RECEIVE: one EVENT}
		ASig messageAbstract = AlloyModel.getInstance().getSig("MESSAGE");
		messageAbstract.set_attr(AAttr.ABSTRACT);
		messageAbstract.AddField("BEFORE", messageAbstract.loneOf());
		messageAbstract.AddField("SEND", eventAbstract.oneOf());
		messageAbstract.AddField("RECEIVE", eventAbstract.oneOf());
		messageAbstract.zone = "Abstract";
		
		ASig SD = AlloyModel.getInstance().getSig("_SD_");
		SD.AddField("MESSAGES", new AFact("_SD_ one -> MESSAGE"));
		
		// add fact to avoid circle
		AlloyModel.getInstance().addFact("fact{all _E:EVENT  | _E !in _E.^BEFORE}").zone = "Avoid circle in events";
		
		// add sender and receiver events
		// one sig senderName extends event{}
		MessageOccurrenceSpecification mFrom = (MessageOccurrenceSpecification) message.getSendEvent();
		MessageOccurrenceSpecification mTo = (MessageOccurrenceSpecification) message.getReceiveEvent();
		
		String lifelineFrom = currentSD_ + mFrom.getCovered(null).getName().split(":")[0];
		String lifelineTo = currentSD_ + mTo.getCovered(null).getName().split(":")[0];
		
		ASig eventSendSig = AlloyModel.getInstance().getSig(currentSD_ + mFrom.getName());
		eventSendSig.set_attr(AAttr.LONE);
		eventSendSig.set_parent(eventAbstract);
		eventSendSig.zone = "Event";
		
		ASig eventRecSig = AlloyModel.getInstance().getSig(currentSD_ + mTo.getName());
		eventRecSig.set_attr(AAttr.LONE);
		eventRecSig.set_parent(eventAbstract);
		eventRecSig.zone = "Event";
		
		// fact: event.cover=lifeline
		AlloyModel.getInstance().addFact("all _E:%s | _E.COVER=%s", eventSendSig, AlloyModel.getInstance().getSig(lifelineFrom)).zone = "Covering: Event->Lifeline";
		AlloyModel.getInstance().addFact("all _E:%s | _E.COVER=%s", eventRecSig, AlloyModel.getInstance().getSig(lifelineTo)).zone = "Covering: Event->Lifeline";
		
		// just used for generate code. see AlloyModel.AddEqMessage()
		eventSendSig.addNote("COVER", AlloyModel.getInstance().getSig(lifelineFrom));
		eventRecSig.addNote("COVER", AlloyModel.getInstance().getSig(lifelineTo));
		
		// add the name
		// one sig name{}
		ASig messageName = AlloyModel.getInstance().getSig("NAME_" + message.getName());
		messageName.set_attr(AAttr.ONE);
		messageName.zone = "Names";
		
		// add the message
		// lone sig m1 extends MESSAGE{}
		ASig messageSig = AlloyModel.getInstance().getSig(currentSD_ + message.getName());
		messageSig.set_attr(AAttr.ONE);
		messageSig.set_parent(messageAbstract);
		messageSig.AddField("NAME", messageName);
		messageSig.zone = "Message";
		
		//add fact: message.send = event  ... and receive
		AlloyModel.getInstance().addFact("%s.SEND = %s", messageSig, eventSendSig).zone = "Binding: Message->Event";
		AlloyModel.getInstance().addFact("%s.RECEIVE = %s", messageSig, eventRecSig).zone = "Binding: Message->Event";
		// just used for generate code. see AlloyModel.AddEqMessage()
		messageSig.addNote("SEND", eventSendSig);
		messageSig.addNote("RECEIVE", eventRecSig);
		
		
		// Fact
		// all messages can have at most one parent (at most 1 Message -> Message)
		AlloyModel.getInstance().addFact("// all messages can have at most one parent (at most 1 Message -> Message)\nfact{all _M1: MESSAGE | lone _M2: MESSAGE | _M1 in _M2.BEFORE}").zone = "relation among Messages";
		// at most one message can have no child (at most 1 Message -> nothing)
		AlloyModel.getInstance().addFact("// at most one message can have no child (at most 1 Message -> nothing)\nfact{lone _M1: MESSAGE | # _M1.BEFORE < 1}").zone = "relation among Messages";
		// all message must have one send and one receive events
		AlloyModel.getInstance().addFact("// all message must have one send and one receive events\nfact{all _M: MESSAGE | _M.RECEIVE in _M.SEND.BEFORE}").zone = "relation among Messages";
		// if a message A is has direct relation with message B, then message A's send/receive still have direct relation with B's
		AlloyModel.getInstance().addFact("// if a message A is has direct relation with message B, then message A's send/receive still have direct relation with B's\nfact{\nall _M1:MESSAGE | (#_M1.BEFORE > 0 and _M1.BEFORE.SEND.COVER = _M1.SEND.COVER) => (_M1.BEFORE.SEND in _M1.SEND.BEFORE and _M1.BEFORE.RECEIVE in _M1.RECEIVE.BEFORE)\nall _M1:MESSAGE | (#_M1.BEFORE > 0 and _M1.BEFORE.SEND.COVER != _M1.SEND.COVER) =>  (_M1.BEFORE.RECEIVE in _M1.SEND.BEFORE and _M1.BEFORE.SEND in _M1.RECEIVE.BEFORE)\n}").zone = "relation among Messages";
		// no relation between events whose parents have no direct relation
		AlloyModel.getInstance().addFact("// no relation between events whose parents have no direct relation\nfact{all _M1: MESSAGE | all _M2: MESSAGE | (_M2 !in _M1.BEFORE and _M2 != _M1) =>not (_M2.SEND in _M1.SEND.BEFORE or _M2.SEND in _M1.RECEIVE.BEFORE or _M2.RECEIVE in _M1.SEND.BEFORE or _M2.RECEIVE in _M1.RECEIVE.BEFORE)}").zone = "relation among Messages";
		// relation from L1's event to L2's event can only be in one message
		AlloyModel.getInstance().addFact("// relation from L1's event to L2's event can only be in one message\nfact{\nall _M: MESSAGE |no _E3: EVENT |(_E3.COVER != _M.SEND.COVER) and (_E3 !in _M.RECEIVE) and (_E3 in _M.SEND.BEFORE or _M.SEND in _E3.BEFORE)\nall _M: MESSAGE |no _E3: EVENT |(_E3.COVER != _M.RECEIVE.COVER) and (_E3 !in _M.SEND) and (_E3 in _M.RECEIVE.BEFORE or _M.RECEIVE in _E3.BEFORE)\n}").zone = "relation among Messages";
		return messageSig;
	}

	@Override
	public void setProperties(Object target, Object source, Transformer t) {
		// TODO Auto-generated method stub
		
	}

}
