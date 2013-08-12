package sitra_rules;

import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.InteractionOperatorKind;
import org.eclipse.uml2.uml.internal.impl.CombinedFragmentImpl;

import uk.ac.bham.sitra.Rule;
import uk.ac.bham.sitra.RuleNotFoundException;
import uk.ac.bham.sitra.Transformer;

@SuppressWarnings({ "rawtypes", "restriction" })
public class CombinedFragmentLoop2Alloy implements Rule{

	@Override
	public boolean check(Object source) {
		if (source instanceof CombinedFragmentImpl) {
			CombinedFragment combinedFragment = (CombinedFragment) source;
			if (combinedFragment.getInteractionOperator() == InteractionOperatorKind.LOOP_LITERAL) {
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
		alloyString += "one sig Loop_";
		alloyString += combinedFragment.getName();
		alloyString += " extends CombineFragment ";
		alloyString += "{";
		boolean flag_com = false;
		for (@SuppressWarnings("unused") InteractionOperand interactionOperand : combinedFragment
				.getOperands()) {
			if (flag_com == true)
				alloyString += ",";
			alloyString += String.format("Op:InterationOperand");
			flag_com = true;
		}
		alloyString += "}\n";
		
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
		return alloyString;
	}

	@Override
	public void setProperties(Object target, Object source, Transformer t) {
		// TODO Auto-generated method stub
		
	}

}
