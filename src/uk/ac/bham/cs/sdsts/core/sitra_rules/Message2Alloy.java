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
		try {
			Message message = (Message) source;
			
			// add abstract for Message
			// abstract sig MESSAGE {}
			PrimSig message_abstract = new PrimSig("MESSAGE", Attr.ABSTRACT);
			message_abstract = (PrimSig) AlloyModel.getInstance().addAbstract(message_abstract);
			
			// add abstract for event
			// one sig abstract event{isbefore: set event}
			PrimSig event_abstract = new PrimSig("EVENT", Attr.ABSTRACT);
			event_abstract.addField("ISBEFORE", event_abstract.setOf());
			event_abstract = (PrimSig) AlloyModel.getInstance().addAbstract(event_abstract);
			
			
			// add sender and receiver events
			// one sig senderName extends event{occur : LL}
			MessageOccurrenceSpecification mFrom = (MessageOccurrenceSpecification) message.getSendEvent();
			MessageOccurrenceSpecification mTo = (MessageOccurrenceSpecification) message.getReceiveEvent();
			// if the message is inside an operand, use the get
			Element teElement = mFrom.getOwner();
			t.transform(mFrom.getCovered(null));
			t.transform(mTo.getCovered(null));
			String lifelineFrom = AlloyModel.getInstance().getName(AlloyModel.getInstance().getSD_prefix() + mFrom.getCovered(null).getName());
			String lifelineTo = AlloyModel.getInstance().getName(AlloyModel.getInstance().getSD_prefix() + mTo.getCovered(null).getName());
			PrimSig event_send = new PrimSig(AlloyModel.getInstance().getSD_prefix() + mFrom.getName(), event_abstract, Attr.ONE);
			PrimSig event_receive = new PrimSig(AlloyModel.getInstance().getSD_prefix() + mTo.getName(), event_abstract, Attr.ONE);
			
			event_send.addField("occur", (Expr) AlloyModel.getInstance().getSigByName(lifelineFrom));
			event_receive.addField("occur", (Expr) AlloyModel.getInstance().getSigByName(lifelineTo));
			AlloyModel.getInstance().addSig(event_receive);
			AlloyModel.getInstance().addSig(event_send);
			
			// add the fact send is before receive
			Expr fact = event_send.in(event_receive.join(event_receive.parent.getFields().get(0))).not();
			Expr fact1 = event_receive.in(event_send.join(event_send.parent.getFields().get(0)));
			AlloyModel.getInstance().addFact(fact);
			AlloyModel.getInstance().addFact(fact1);
			
			// add the name
			// one sig name{}
			PrimSig message_name = new PrimSig("NAME_" + message.getName(), Attr.ONE);
			AlloyModel.getInstance().addSig(message_name);
			
			// add the message
			PrimSig message_sig = new PrimSig(AlloyModel.getInstance().getMessageID(message.getName()), message_abstract, Attr.ONE);
			message_sig.addField("send", event_send);
			message_sig.addField("receive", event_receive);
			message_sig.addField("name", message_name);
			AlloyModel.getInstance().addSig(message_sig);
						
		} catch (Err e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuleNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
//		String alloyString = "";
//		Message message = (Message) source;
//		alloyString += "one sig ";
//		alloyString += message.getName();
//		alloyString += " extends Message ";
//		alloyString += String.format("{send:%s receive:%s}", message.getSendEvent().getName(), message.getReceiveEvent().getName());
//		
//		MessageOccurrenceSpecification mFrom = (MessageOccurrenceSpecification) message.getSendEvent();
//		MessageOccurrenceSpecification mTo = (MessageOccurrenceSpecification) message.getReceiveEvent();
//		//one sig Event1 extends EventOccurent{from: LifeLine_A}
//		alloyString += String.format("\none sig %s extends EventOccurent {from: %s}", mFrom.getName(), mFrom.getCovered(null).getName());
//		alloyString += String.format("\none sig %s extends EventOccurent {to: %s}", mTo.getName(), mTo.getCovered(null).getName());
//
//		return alloyString;
	}

	@Override
	public void setProperties(Object target, Object source, Transformer t) {
		// TODO Auto-generated method stub
		
	}

}
