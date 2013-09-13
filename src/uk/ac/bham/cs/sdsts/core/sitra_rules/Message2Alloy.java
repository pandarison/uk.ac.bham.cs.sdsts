/***
 *  Author: Yi Chen
 */
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
		// abstract sig EVENT extends FRAGMENT{COVER:one LIFELINE}
		ASig lifelineAbstract = AlloyModel.getInstance().getSig("LIFELINE");
		ASig fragmentAbstract = AlloyModel.getInstance().getSig("FRAGMENT");
		ASig eventAbstract = AlloyModel.getInstance().getSig("EVENT");
		eventAbstract.set_attr(AAttr.ABSTRACT);
		eventAbstract.set_parent(fragmentAbstract);
		eventAbstract.AddField("COVER", lifelineAbstract.oneOf());
		eventAbstract.zone = "Abstract";
		
		// add abstract for Message
		// abstract sig MESSAGE{SEND:one EVENT, RECEIVE:one EVENT}
		ASig messageAbstract = AlloyModel.getInstance().getSig("MESSAGE");
		messageAbstract.set_attr(AAttr.ABSTRACT);
		messageAbstract.AddField("SEND", eventAbstract.oneOf());
		messageAbstract.AddField("RECEIVE", eventAbstract.oneOf());
		messageAbstract.zone = "Abstract";
		
		ASig SD = AlloyModel.getInstance().getSig("_SD_");
		SD.AddField("MESSAGES", new AFact("_SD_ one -> MESSAGE"));
		
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
		
		messageSig.addNote("SEND", eventSendSig);
		messageSig.addNote("RECEIVE", eventRecSig);
		eventSendSig.addNote("COVER", AlloyModel.getInstance().getSig(lifelineFrom));
		eventRecSig.addNote("COVER", AlloyModel.getInstance().getSig(lifelineTo));
		
		/**
		***  Constraint: Lifeline
		**/
		// at most one event can have no next on the same lifeline
		AlloyModel.getInstance().addFact("// at most one event can have no next on the same lifeline\nfact{all _L: LIFELINE | lone _E1: EVENT | _E1.COVER=_L and (_L !in _E1.BEFORE.COVER or #_E1.BEFORE=0) }").zone = "Constraint: Lifeline";
		// one event can have at most one Next one one lifeline
		AlloyModel.getInstance().addFact("// one event can have at most one Next one one lifeline\nfact{all _E1: EVENT | lone _E2: EVENT-_E1 | _E2 in _E1.BEFORE and _E2.COVER=_E1.COVER}\nfact{all _E1: EVENT | lone _E2: EVENT-_E1 | _E1 in _E2.BEFORE and _E2.COVER=_E1.COVER}").zone="Constraint: Lifeline";
		/**
		***  Constraint: Message
		**/
		// send before receive
		AlloyModel.getInstance().addFact("// send before receive\nfact{all _M: MESSAGE | _M.RECEIVE in _M.SEND.BEFORE}").zone = "Constraint: Message";
		// only allow relation between Events either they are in same message or on same lifeline
		AlloyModel.getInstance().addFact("// only allow relation between Events either they are in same message or on same lifeline\nfact{all _E1: EVENT, _M: MESSAGE,  _E2: EVENT | (_E1 in _M.SEND and _E2 in _E1.BEFORE) => (_M.RECEIVE=_E2) or (_E1.COVER=_E2.COVER)}\nfact{all _E1: EVENT, _M: MESSAGE,  _E2: EVENT | (_E1 in _M.RECEIVE and _E2 in _E1.BEFORE) => (_E1.COVER=_E2.COVER)}").zone="Constraint: Message";
		// message cannot be Before/After of others at the same time
		AlloyModel.getInstance().addFact("// message cannot be Before/After of others at the same time\nfact{all _M1: MESSAGE | no _M2: MESSAGE | (_M1.SEND in _M2.SEND.^BEFORE and _M2.RECEIVE in _M1.RECEIVE.^BEFORE) or (_M2.SEND in _M1.SEND.^BEFORE and _M1.RECEIVE in _M2.RECEIVE.^BEFORE)}").zone = "Constraint: Message";
		// one message's send/receive should be covered by the same operand
		AlloyModel.getInstance().addFact("// one message's send/receive should be covered by the same operand\nfact{all _M: MESSAGE | one _OP: OPERAND | _M.SEND in _OP.COVER and _M.RECEIVE in _OP.COVER}").zone = "Constraint: Message";
		return messageSig;
	}

	@Override
	public void setProperties(Object target, Object source, Transformer t) {
		// TODO Auto-generated method stub
		
	}

}
