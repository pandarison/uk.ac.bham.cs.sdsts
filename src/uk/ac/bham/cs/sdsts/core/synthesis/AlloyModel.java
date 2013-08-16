package uk.ac.bham.cs.sdsts.core.synthesis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.uml2.uml.Model;

import uk.ac.bham.cs.sdsts.SDConsole;
import uk.ac.bham.cs.sdsts.Alloy.AAttr;
import uk.ac.bham.cs.sdsts.Alloy.AFact;
import uk.ac.bham.cs.sdsts.Alloy.AObject;
import uk.ac.bham.cs.sdsts.Alloy.ASig;
import uk.ac.bham.cs.sdsts.core.sitra_rules.CombinedFragment2Alloy;
import uk.ac.bham.cs.sdsts.core.sitra_rules.Interaction2Alloy;
import uk.ac.bham.cs.sdsts.core.sitra_rules.InteractionOperand2Alloy;
import uk.ac.bham.cs.sdsts.core.sitra_rules.Lifeline2Alloy;
import uk.ac.bham.cs.sdsts.core.sitra_rules.Message2Alloy;
import uk.ac.bham.sitra.Rule;
import uk.ac.bham.sitra.RuleNotFoundException;
import uk.ac.bham.sitra.SimpleTransformerImpl;
import uk.ac.bham.sitra.Transformer;

public class AlloyModel {
	private String _current_SD;
	private HashMap<String, ASig> _sigs;
	private HashMap<String, AFact> _facts;

	public AFact addFact(String format, Object... args){
		AFact fact = new AFact(format, args);
		_facts.put(fact.toString(), fact);
		return _facts.get(fact.toString());
	}
	
	public ASig getSig(String name){
		if(!_sigs.containsKey(name)){
			ASig sig = new ASig(name, AAttr.ONE, null);
			_sigs.put(name, sig);
		}
		return _sigs.get(name);
	}
	
	public String getSD(){
		return _current_SD;
	}
	
	@SuppressWarnings("unchecked")
	public void addModel(Model model, String SD_num){
		this._current_SD= SD_num;

		// List all of all classes that extend the rule interface
		List<Class<? extends Rule<?, ?>>> rules = new ArrayList<Class<? extends Rule<?, ?>>>();
		
		// Add rules to the list of rules
		rules.add((Class<? extends Rule<?, ?>>) InteractionOperand2Alloy.class);
		rules.add((Class<? extends Rule<?, ?>>) CombinedFragment2Alloy.class);
		rules.add((Class<? extends Rule<?, ?>>) Interaction2Alloy.class);
		rules.add((Class<? extends Rule<?, ?>>) Lifeline2Alloy.class);
		rules.add((Class<? extends Rule<?, ?>>) Message2Alloy.class);

		// Create the transformer
		Transformer trans = new SimpleTransformerImpl(rules);

		// Transform to alloy model
		try {
			trans.transform(model.getOwnedElements().get(0));
		} catch (RuleNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public String toResult(String filter, HashMap data){
		String string = "";
		for (Object object : data.values()) {
			if(((AObject)object).zone.equals(filter))
				string += object.toString() + "\n";
		}
		return string;
	}
	
	public String getResult(){
		String string = "// Abstract sigs\n";
		string += toResult("abstract", _sigs);
		
		string += "\n// SDs\n";
		string += toResult("sd", _sigs);
		
		string += "\n// Names\n";
		string += toResult("name", _sigs);
		
		string += "\n// Classes\n";
		string += toResult("class", _sigs);
		
		string += "\n// Lifelines\n";
		string += toResult("lifeline", _sigs);
		
		string += "\n// Messages\n";
		string += toResult("message", _sigs);
		
		string += "\n// Operands\n";
		string += toResult("operand", _sigs);
		
		string += "\n// Combined Fragments\n";
		string += toResult("combinedFragment", _sigs);
		
		string += "\n// Events\n";
		string += toResult("event", _sigs);
		
		string += "\n// Covering\nfact{\n";
		string += toResult("cover", _facts);
		string += "}\n";
		
		string += "\n// Orders\nfact{\n";
		string += toResult("order", _facts);
		string += "}\n";
		
		string += "\n// Hidden the merged objects\nfact{\n";
		string += toResult("hidden", _facts);
		string += "}\n";
		
		string += "\n// Remove the relation among operands in the same combined fragment Alt\nfact{\n";
		string += toResult("cf_alt_relation_removal", _facts);
		string += "}\n";
		
		string += "\n// Other\n";
		string += toResult("other", _facts);
		
		string += "run{}\n";
		
		return string;
	}
	
	private void addEqLifeline(String lifeline1, String lifeline2){
		try {
			// two life lines to be equal cannot be in the same SD
			String sdID1 = lifeline1.split("_")[0];
			String sdID2 = lifeline2.split("_")[0];
			if(sdID1.equals(sdID2)){
				SDConsole.print_has_time("Can not add equality between lifelines in the same diagram.\nThis equality has been ignored.");
				return ;
			}
			
			// get the two life lines sig
			ASig lifeline1Sig = this.getSig(lifeline1.split(":")[0]);
			ASig lifeline2Sig = this.getSig(lifeline2.split(":")[0]);
			
			// make lifeline2 to lone
			lifeline2Sig.set_attr(AAttr.LONE);

			// add fact
			// all SD1L:SD1_Lifeline_1 , SD2L:SD2_Lifeline_2 | SD1L.name = SD2L.name && SD1L.type = SD2L.type
			AlloyModel.getInstance().addFact(String.format("# %s = 0", lifeline2Sig.get_name())).zone = "hidden";
			
			lifeline2Sig.mergeTo(lifeline1Sig);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void addEqMessage(String message1, String message2){
		// two messages to be equal cannot be in the same SD
		String sdID1 = message1.split("_")[0];
		String sdID2 = message2.split("_")[0];
		if(sdID1.equals(sdID2)){
			SDConsole.print_has_time("Can not add equality between messages in the same diagram.\nThis equality has been ignored.");
			return ;
		}
		ASig message1Sig = this.getSig(message1);
		ASig message2Sig = this.getSig(message2);
		
		ASig message1SendSig = (ASig) message1Sig.get_fields().get("send");
		ASig message1RecSig = (ASig) message1Sig.get_fields().get("receive");
		ASig message2SendSig = (ASig) message2Sig.get_fields().get("send");
		ASig message2RecSig = (ASig) message2Sig.get_fields().get("receive");
		
		ASig message1SendLifelineSig = (ASig) message1SendSig.get_fields().get("occur");
		ASig message1RecLifelineSig = (ASig) message1RecSig.get_fields().get("occur");
		ASig message2SendLifelineSig = (ASig) message2SendSig.get_fields().get("occur");
		ASig message2RecLifelineSig = (ASig) message2RecSig.get_fields().get("occur");
		

		// the from/to life lines do not match
		if(!message1SendLifelineSig.isSameWith(message2SendLifelineSig)){
			SDConsole.print_has_time("Can not add equality between messages whose begin and end lifelines do not match.\nThis equality has been ignored");
			return;
		}
		if(!message1RecLifelineSig.isSameWith(message2RecLifelineSig)){
			SDConsole.print_has_time("Can not add equality between messages whose begin and end lifelines do not match.\nThis equality has been ignored");
			return;
		}
		
		// make sig to lone and replace the fields
		message2Sig.set_attr(AAttr.LONE);
		message2SendSig.set_attr(AAttr.LONE);
		message2RecSig.set_attr(AAttr.LONE);
		
		this.addFact(String.format("# %s = 0", message2Sig.get_name())).zone = "hidden";
		this.addFact(String.format("# %s = 0", message2SendSig.get_name())).zone = "hidden";
		this.addFact(String.format("# %s = 0", message2RecSig.get_name())).zone = "hidden";
		
		message2Sig.mergeTo(message1Sig);
		message2SendSig.mergeTo(message1SendSig);
		message2RecSig.mergeTo(message1RecSig);
	}
	
	public boolean addEquality(String lName, String rName){
		Object lObject = _sigs.get(lName), rObject = _sigs.get(rName);
		if(lObject != null && rObject != null){
			ASig sig1 = (ASig) lObject;
			if(sig1.get_fields().size() == 3)
				this.addEqMessage(lName, rName);
			if(sig1.get_fields().size() == 2)
				this.addEqLifeline(lName, rName);
			return true;
		}
		return false;
	}
	
	public static void clear(){
		_instance = null;
	}
	
	private AlloyModel(){
		this._sigs = new HashMap<String, ASig>();
		this._facts = new HashMap<String, AFact>();
	}
	
	private static AlloyModel _instance;
	
	public static AlloyModel getInstance(){
		if(_instance == null)_instance = new AlloyModel();
		return _instance;
	}
}
