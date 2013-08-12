package uk.ac.bham.cs.sdsts.core.synthesis;

import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.impl.InteractionImpl;
import org.eclipse.uml2.uml.internal.impl.LifelineImpl;
import org.eclipse.uml2.uml.internal.impl.MessageImpl;
import org.eclipse.uml2.uml.internal.impl.MessageOccurrenceSpecificationImpl;

@SuppressWarnings("restriction")
public class Synthesis {
			
	private Model _model;
	
	private String _prefix = null;
	
	public void initial(){
		_model = UMLFactory.eINSTANCE.createModel();
		_model.setName("MergedModel");
		_model.createPackagedElement("MergedSD", UMLPackage.eINSTANCE.getInteraction());
	}
	
	public void addModel(Model model, Equality equality, String prefix) throws Exception{
		if(equality == null || equality.size()<1){
			throw new Exception("Cannot merge two SDs with no equalities.");
		}
		if(!(model.getOwnedElements().get(0) instanceof Interaction)){
			throw new Exception("The model may not be Sequence Diagram as it does not have Interaction inside.");
		}
		_prefix = prefix;
		merge_create(model.getOwnedElements().get(0), _model.getOwnedElements().get(0), equality);
		_prefix = "SD_Merged";
	}
	private void merge_create(Element source, Element target, Equality equality){
		if(target instanceof InteractionImpl){
			// create lifeline
			for (Element element : source.getOwnedElements()) {
				if(element instanceof LifelineImpl){
					if(!equality.isIn_B(element))
						((Interaction)target).createLifeline(_prefix + "_" + ((LifelineImpl) element).getName());
				}
			}
			// create MessageOccurranceSpecifications
			for (int i = 0; i < ((Interaction) source).getFragments().size(); i++) {
				Element element = ((Interaction) source).getFragments().get(i);
				if(element instanceof MessageOccurrenceSpecificationImpl){
					MessageOccurrenceSpecification messageEnd = (MessageOccurrenceSpecification) element;
					MessageOccurrenceSpecification _messageEnd = (MessageOccurrenceSpecification) ((Interaction)target).createFragment(_prefix + "_" + messageEnd.getName(), UMLPackage.eINSTANCE.getMessageOccurrenceSpecification());
					Lifeline lifeline = messageEnd.getCovereds().get(0);
					Lifeline _lifeline = null;
					if(equality.isIn_B(lifeline)){
						String lifeline_prefix = equality.getPrefixByObject(equality.getAbyB(lifeline));
						_lifeline = ((InteractionImpl) target).getLifeline(lifeline_prefix + "_" + ((Lifeline) equality.getAbyB(lifeline)).getName());
						//_lifeline = (Lifeline) equality.getAbyB(lifeline);
					}else{
						_lifeline = ((InteractionImpl) target).getLifeline(_prefix + "_" + lifeline.getName());
					}
					_messageEnd.getCovereds().add(_lifeline);
				}
			}
			// create Messages
			for (Element element : source.getOwnedElements()) {
				if(element instanceof MessageImpl){
					// source
					Message message = (Message) element;
					MessageOccurrenceSpecification sender = (MessageOccurrenceSpecification) message.getSendEvent();
					MessageOccurrenceSpecification receiver = (MessageOccurrenceSpecification) message.getReceiveEvent();
					
					// set message with ends
					Message _message = ((InteractionImpl) target).createMessage(_prefix + "_" + message.getName());
					MessageOccurrenceSpecification _sender = (MessageOccurrenceSpecification) ((InteractionImpl) target).getFragment(_prefix + "_" + sender.getName());
					MessageOccurrenceSpecification _receiver = (MessageOccurrenceSpecification) ((InteractionImpl) target).getFragment(_prefix + "_" + receiver.getName());

					_message.setSendEvent(_sender);
					_message.setReceiveEvent(_receiver);
//					// set messageEnds
//					Lifeline lifeline_sender = sender.getCovereds().get(0);
//					Lifeline _lifeline_sender = null;
//					if(equality.isIn_B(lifeline_sender))
//						_lifeline_sender = (Lifeline) equality.getAbyB(lifeline_sender);
//					else _lifeline_sender = ((InteractionImpl) target).getLifeline(_prefix + "_" + lifeline_sender.getName());
//					MessageOccurrenceSpecification _sender = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
//					_sender.setName(_prefix + "_" + sender.getName());
//					_sender.getCovereds().add(_lifeline_sender);
//					
//					Lifeline lifeline_receiver = sender.getCovereds().get(0);
//					Lifeline _lifeline_receiver = null;
//					if(equality.isIn_B(lifeline_receiver))
//						_lifeline_receiver = (Lifeline) equality.getAbyB(lifeline_receiver);
//					else _lifeline_receiver = ((InteractionImpl) target).getLifeline(_prefix + "_" + lifeline_receiver.getName());
//					MessageOccurrenceSpecification _receiver = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
//					_receiver.setName(_prefix + "_" + receiver.getName());
//					_receiver.getCovereds().add(_lifeline_receiver);	
//					
//					// target
//					Message _message = ((InteractionImpl) target).createMessage(_prefix + "_" + message.getName());
//					_message.setSendEvent(_sender);
//					_message.setReceiveEvent(_receiver);
					}
			}
			
			
		}
	}
	public Model getMergedModel() {
		return _model;
	}
	
	public String getSDName(){
		return _prefix;
	}
	
	
}
