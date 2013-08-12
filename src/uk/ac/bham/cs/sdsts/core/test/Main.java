package uk.ac.bham.cs.sdsts.core.test;

import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;

import uk.ac.bham.cs.sdsts.core.synthesis.AlloyModel;
import uk.ac.bham.cs.sdsts.core.synthesis.Xml2obj;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			test1();
	}
	public static void test1(){

		Model model = Xml2obj.load("/Users/chenyi/Documents/workspace/SdSts/resource/model2.uml");
		Model model2 = Xml2obj.load("/Users/chenyi/Documents/workspace/SdSts/resource/model3.uml");
		AlloyModel.getInstance().addModel(model, "SD1");
		AlloyModel.getInstance().addModel(model2, "SD2");
		AlloyModel.getInstance().getResult();
		
		
		
		
//		// load file
////		System.out.println("Select the file.");
////		JFileChooser chooser = new JFileChooser();
////		chooser.showOpenDialog(null);
////		System.out.println(chooser.getSelectedFile());
//		
//		// transform to java objects
//		//Model model = xmi2obj.xml2obj.load(chooser.getSelectedFile().toString());
//		Model model = xmi2obj.xml2obj.load("/Users/chenyi/Documents/workspace/SdSts/resource/model3.uml");
//		Model model2 = xmi2obj.xml2obj.load("/Users/chenyi/Documents/workspace/SdSts/resource/model2.uml");
//		
//		
////		Equality equality = new Equality();
////		equality.initial();
////		//equality.insert("SD1", model.getOwnedElements().get(0).getOwnedElements().get(6), "SD2", model2.getOwnedElements().get(0).getOwnedElements().get(2));
////		Synthesis synthesis = new Synthesis();
////		synthesis.initial();
////		try {
////			synthesis.addModel(model, equality, "SD1");
////			synthesis.addModel(model2, equality, "SD2");
////		} catch (Exception e1) {
////			// TODO Auto-generated catch block
////			e1.printStackTrace();
////		}
//
////		Model model = UMLFactory.eINSTANCE.createModel();
////		Interaction interaction = (Interaction) model.createPackagedElement("it1", UMLPackage.eINSTANCE.getInteraction());
////		
////		Lifeline lifeline1 = interaction.createLifeline("lifeline1");
////		Lifeline lifeline2 = interaction.createLifeline("lifeline2");
////		Message message = UMLFactory.eINSTANCE.createMessage();//interaction.createMessage("hellp");
////		MessageOccurrenceSpecification messageOccurrenceSpecification = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
////		messageOccurrenceSpecification.setName("asf");
////		messageOccurrenceSpecification.getCovereds().add(lifeline1);
////		message.setSendEvent(messageOccurrenceSpecification);
////		
////		MessageOccurrenceSpecification messageOccurrenceSpecification1 = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
////		messageOccurrenceSpecification1.setName("asf");
////		messageOccurrenceSpecification1.getCovereds().add(lifeline2);
////		message.setReceiveEvent(messageOccurrenceSpecification1);
//		
//		
//		//List all of all classes that extend the rule interface
//		List<Class<? extends Rule<?, ?>>> rules = new ArrayList<Class<? extends Rule<?, ?>>>();
//		//Add rules to the list of rules
////		rules.add((Class<? extends Rule<?, ?>>) sitra_rules.InteractionOperand2Alloy.class);
////		rules.add((Class<? extends Rule<?, ?>>) sitra_rules.CombinedFragmentLoop2Alloy.class);
////		rules.add((Class<? extends Rule<?, ?>>) sitra_rules.Interaction2Alloy.class);
////		rules.add((Class<? extends Rule<?, ?>>) sitra_rules.Lifeline2Alloy.class);
////		rules.add((Class<? extends Rule<?, ?>>) sitra_rules.Message2Alloy.class);
////		rules.add((Class<? extends Rule<?, ?>>) sitra_rules.CombinedFragmentAlt2Alloy.class);
////		rules.add((Class<? extends Rule<?, ?>>) sitra_rules.CombinedFragmentPar2Alloy.class);
//
//		//Create the transformer 
//		Transformer trans = new SimpleTransformerImpl(rules);
//
//		//Creating user Alloy			
//
//		try {
//			List<? extends Object> lifelineString = trans.transformAll(model.getOwnedElements());
//			for (Object object : lifelineString) {
//				if(object != null)
//					System.out.println(object);
//			}
//		} catch (RuleNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//		
//		for (Element element : model.getOwnedElements().get(0).getOwnedElements()) {
//			showModel(element, "");
//		}
	}
	
	public static void showModel(Element element, String str){
		System.out.println(str + element);
//		if(element instanceof LifelineImpl){
//			for (Element element2 : ((Lifeline)element).getCoveredBys()) {
//				showModel(element2, str+"  ");
//			}
//		}
	
//		if(element.getOwnedElements() != null){
//			for (Element e : element.getOwnedElements()) {
//				showModel(e, str+"  ");
//			}
//		}
	}
}
