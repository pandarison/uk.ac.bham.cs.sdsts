package sitra_rules;

import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.InteractionOperatorKind;
import org.eclipse.uml2.uml.internal.impl.CombinedFragmentImpl;

import uk.ac.bham.sitra.Rule;
import uk.ac.bham.sitra.RuleNotFoundException;
import uk.ac.bham.sitra.Transformer;

@SuppressWarnings({ "rawtypes", "restriction" })
public class CombinedFragmentPar2Alloy implements Rule {

	@Override
	public boolean check(Object source) {
		if (source instanceof CombinedFragmentImpl) {
			CombinedFragment combinedFragment = (CombinedFragment) source;
			if (combinedFragment.getInteractionOperator() == InteractionOperatorKind.PAR_LITERAL) {
				return true;
			} else
				return false;
		} else
			return false;
	}

	@Override
	public Object build(Object source, Transformer t) {
		String alloyString = "";
		CombinedFragment combinedFragment = (CombinedFragment) source;
		alloyString += "one sig Par_";
		alloyString += combinedFragment.getName();
		alloyString += " extends CombineFragment ";
		alloyString += "{";
		boolean flag_com = false;
		int count = 1;
		for (@SuppressWarnings("unused") InteractionOperand interactionOperand : combinedFragment
				.getOperands()) {
			if (flag_com == true)
				alloyString += ",";
			alloyString += String.format("Op%d:InterationOperand", count++);
			flag_com = true;
		}
		alloyString += "}";

		try {
			java.util.List<? extends Object> stringList = t
					.transformAll(combinedFragment.getOperands());
			for (Object object : stringList) {
				if(object != null)
				alloyString += object + "\n";
			}
		} catch (RuleNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//7-10: we dont need the fact
		// fact for the combinedfragment
		// generate the facts
//		String fact1 = "\nfact{all ";
//		String fact2 = "\n\nfact{all ";
//		count = 0;
//		for (int i = 0; i < combinedFragment.getOperands().size(); i++) {
//			if(i != 0)fact1 += ", ";
//				fact1 += String.format("operand%d:%s", i+1, combinedFragment.getOperands().get(i).getName());
//		}
//		fact1 += String.format(", par:par_%s | ", combinedFragment.getName());
//		for (int i = 0; i < combinedFragment.getOperands().size(); i++) {
//			if(i != 0)fact1 += " and ";
//			fact1 += String.format("par.Op%d in operand%d", i+1, i+1);
//		}
//		fact1 += "}";
//		
//		count=0;
//		for (int i = 0; i < combinedFragment.getOperands().size(); i++) {
//			if(count++ != 0) fact2 += ",";
//			fact2 += String.format("operand%d:%s", i+1, combinedFragment.getOperands().get(i).getName());
//		}
//		fact2 += " | ";
//		count=0;
//		for (int i = 0; i < combinedFragment.getOperands().size(); i++) {
//			for (int j = i+1; j < combinedFragment.getOperands().size(); j++) {
//				if(count++ != 0) fact2 += " and ";
//				fact2 += String.format("operand%d in operand%d.isafter", j, i);
//			}
//		}
//		fact2 += "}";
//		if (count > 0) {
//			alloyString += fact1;
//			alloyString += fact2;
//		}
		return alloyString;
	}

	@Override
	public void setProperties(Object target, Object source, Transformer t) {
		// TODO Auto-generated method stub

	}

}
