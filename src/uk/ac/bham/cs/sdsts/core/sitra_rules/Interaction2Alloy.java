package uk.ac.bham.cs.sdsts.core.sitra_rules;

import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperatorKind;
import org.eclipse.uml2.uml.internal.impl.CombinedFragmentImpl;
import org.eclipse.uml2.uml.internal.impl.InteractionImpl;
import org.eclipse.uml2.uml.internal.impl.LifelineImpl;
import org.eclipse.uml2.uml.internal.impl.MessageOccurrenceSpecificationImpl;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Attr;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;

import uk.ac.bham.cs.sdsts.SDConsole;
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
		Interaction interaction = (Interaction) source;
		try {
			t.transformAll(interaction.getOwnedElements());
		} catch (RuleNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//  add sig
		//	One sig Sd { 
		//	Messages : Sd one -> Messages,
		//	Lifelines : Sd one -> Lifelines
		//	Cf ( if there is ): Sd one ->combine fragment  }
		try {
		PrimSig SD = new PrimSig(AlloyModel.getInstance().SD_num, Attr.ONE);
		for (Element element : interaction.getOwnedElements()) {
			if(element instanceof LifelineImpl){
				Expr expr = SD.one_arrow_any((Expr) AlloyModel.getInstance().getSigByName("LIFELINE"));
				SD.addField("LIFELINE", expr);
				break;
			}
		}
		for (Element element : interaction.getOwnedElements()) {
			if(element instanceof LifelineImpl){
				Expr expr = SD.one_arrow_any((Expr) AlloyModel.getInstance().getSigByName("MESSAGE"));
				SD.addField("MESSAGE", expr);
				break;
			}
		}
		for (Element element : interaction.getOwnedElements()) {
			if(element instanceof CombinedFragmentImpl){
				Expr expr = SD.one_arrow_any((Expr) AlloyModel.getInstance().getSigByName("COMBINEDFRAGMENT"));
				SD.addField("COMBINEDFRAGMENT", expr);
				break;
			}
		}
		AlloyModel.getInstance().addSig(SD);
		} catch (Err e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		// add facts
//		for (int i = 0; i < interaction.getFragments().size(); i++) {
//			for (int j = i+1; j < interaction.getFragments().size(); j++) {
//				InteractionFragment o1 = interaction.getFragments().get(i);
//				InteractionFragment o2 = interaction.getFragments().get(j);
//				if(o1.getClass().equals(MessageOccurrenceSpecificationImpl.class) && o2.getClass().equals(MessageOccurrenceSpecificationImpl.class)){
//					PrimSig s1 = (PrimSig) AlloyModel.getInstance().getSigByName(AlloyModel.getInstance().getSD_prefix() + o1.getName());
//					PrimSig s2 = (PrimSig) AlloyModel.getInstance().getSigByName(AlloyModel.getInstance().getSD_prefix() + o2.getName());
//					Expr fact = s1.in(s2.join(s2.parent.getFields().get(0)));
//					AlloyModel.getInstance().addFact(fact);
//				}
//				// todo 5th Aug
//				
////				if(o1.getClass().equals(CombinedFragmentImpl.class) && o2.getClass().equals(MessageOccurrenceSpecificationImpl.class)){
////					CombinedFragmentImpl cf1 = (CombinedFragmentImpl) o1;
////					if(cf1.getInteractionOperator() == InteractionOperatorKind.ALT_LITERAL){
////						
////					}
////				}
//				
//				//fact1 += String.format("%s in %s.isBefore\n", interaction.getFragments().get(j).getName(), interaction.getFragments().get(i).getName());
//			}
//		}
		
		return null;
		
//		String alloyString = "";
//		
//		// flag of appearances of three basic type, for the purpose of generate Abstract
//		int[] flag = new int[3];
//		
//		// generate the interaction
//		alloyString += "one sig SD_Merged {";
//		Interaction interaction = (Interaction) source;
//		int count = 1;
//		for (Element element : interaction.getOwnedElements()) {
//			if(element instanceof LifelineImpl){
//				flag[0]=1;
//				if(count != 1)alloyString += ", ";
//				alloyString += String.format("L%d:%s", count++, ((LifelineImpl) element).getName());
//			}
//		}
//		
//		count = 1;
//		for (Element element : interaction.getOwnedElements()) {
//			if(element instanceof MessageImpl){
//				flag[1]=1;
//				alloyString += ", ";
//				alloyString += String.format("M%d:%s", count++, ((MessageImpl) element).getName());
//			}
//		}
//		
//		count = 1;
//		for (Element element : interaction.getOwnedElements()) {
//			if(element instanceof CombinedFragmentImpl){
//				flag[2]=1;
//				alloyString += ", ";
//				alloyString += String.format("alt%d:%s", count++, ((CombinedFragmentImpl) element).getName());
//			}
//		}
//		alloyString += "}";
//		
//		// generate the abstract objects
//		if(flag[0] == 1){
//			alloyString += "\n\nabstract sig Lifeline {}";
//		}
//		if(flag[1] == 1){
//			alloyString += "\nabstract sig Message {}";
//			alloyString += "\nabstract sig EventOccurent {isBefore: set EventOccurent}";
//		}
//		if(flag[2] == 1){
//			alloyString += "\nabstract sig CombinedFragment {}";
//			alloyString += "\nabstract sig InteractionOperand {isafter: InteractionOperand}\n";
//		}
//		alloyString += "\n\n";
//		
//		// recursive to lower level members
//		try {
//			java.util.List<? extends Object> stringList = t.transformAll(interaction.getOwnedElements());
//			for (Object object : stringList) {
//				if(object != null)
//				alloyString += object + "\n\n";
//			}
//		} catch (RuleNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		// generate the facts
//		String fact1 = "fact{\n";
//		String fact2 = "fact {no e:elem  |  e in e.^next}\n";
//		count = 0;
//		for (int i = 0; i < interaction.getAllOperations().size(); i++) {
//			for (int j = i+1; j < interaction.getFragments().size(); j++) {
//				count ++;
//				fact1 += String.format("%s in %s.isBefore\n", interaction.getFragments().get(j).getName(), interaction.getFragments().get(i).getName());
//			}
//		}
//		fact1 += "}\n";
//		if(count > 0){
//			alloyString += fact1;
//			alloyString += fact2;
//		}
//		
//		return alloyString;
	}

	@Override
	public void setProperties(Object target, Object source, Transformer t) {
		// TODO Auto-generated method stub
	}

}
