package uk.ac.bham.cs.sdsts.core.synthesis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.uml2.uml.Model;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4compiler.ast.Attr;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprQt;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import uk.ac.bham.cs.sdsts.SDConsole;
import uk.ac.bham.cs.sdsts.core.sitra_rules.*;
import uk.ac.bham.sitra.Rule;
import uk.ac.bham.sitra.RuleNotFoundException;
import uk.ac.bham.sitra.SimpleTransformerImpl;
import uk.ac.bham.sitra.Transformer;

public class AlloyModel {
	
	private HashMap<String, Object> _mapping_sigs;
	private HashMap<String, String> _mapping_name;
	private HashMap<String, Object> _mapping_facts;
	
	public String SD_num;
	private int LL_num;
	private int MSG_num;
	
	private static AlloyModel _instance;
	
	public static void clear(){
		_instance = null;
	}
	
	public static AlloyModel getInstance(){
		if(_instance == null){
			_instance = new AlloyModel();
			_instance.init();
		}
		return _instance;
	}
	
	private void init(){
		_mapping_sigs = new HashMap<String, Object>();
		_mapping_name = new HashMap<String, String>();
		_mapping_facts = new HashMap<String, Object>();
	}
	
	public String getSD_prefix(){
		return this.SD_num + "_";
	}
	public String getLifeLineID(String oringinalName) {
		if(_mapping_name.containsKey(this.getSD_prefix() + oringinalName))
			return _mapping_name.get(this.getSD_prefix() + oringinalName);
		else{
			String string = String.format("%s_Lifeline_%d", SD_num, LL_num++);
			_mapping_name.put(this.getSD_prefix() + oringinalName, string);
			return string;
		}
	}
	public String getMessageID(String oringinalName) {
		if(_mapping_name.containsKey(this.getSD_prefix() + oringinalName))
			return _mapping_name.get(this.getSD_prefix() + oringinalName);
		else{
			String string = String.format("%s_Message_%d", SD_num, MSG_num++);
			_mapping_name.put(this.getSD_prefix() + oringinalName, string);
			return string;
		}
	}
	@SuppressWarnings("unchecked")
	public void addModel(Model model, String SD_num){
		this.SD_num = SD_num;
		LL_num = 1;
		MSG_num = 1;
		// List all of all classes that extend the rule interface
		List<Class<? extends Rule<?, ?>>> rules = new ArrayList<Class<? extends Rule<?, ?>>>();
		// Add rules to the list of rules
		rules.add((Class<? extends Rule<?, ?>>) InteractionOperand2Alloy.class);
		rules.add((Class<? extends Rule<?, ?>>) CombinedFragment2Alloy.class);
		rules.add((Class<? extends Rule<?, ?>>) Interaction2Alloy.class);
		rules.add((Class<? extends Rule<?, ?>>) Lifeline2Alloy.class);
		rules.add((Class<? extends Rule<?, ?>>) Message2Alloy.class);
		//rules.add((Class<? extends Rule<?, ?>>) sitra_rules.CombinedFragmentAlt2Alloy.class);
		//rules.add((Class<? extends Rule<?, ?>>) sitra_rules.CombinedFragmentPar2Alloy.class);

		// Create the transformer
		Transformer trans = new SimpleTransformerImpl(rules);

		// Transform to alloy model
		try {
			trans.transform(model.getOwnedElements().get(0));
		} catch (RuleNotFoundException e) {
			e.printStackTrace();
		}
	}
	public Object addAbstract(Object object){
		return addSig(object);
	}
	public Object addSig(Object object){
		if(!_mapping_sigs.containsKey(object.toString())){
			_mapping_sigs.put(object.toString(), object);
		}
		return _mapping_sigs.get(object.toString());
	}
	private String fields2String(SafeList<Field> fields){
		String result = "";
		for (Field field : fields) {
			if(result.equals(""))
				result += String.format("%s:%s", field.label, field.decl().expr);
			else result += String.format(", %s:%s", field.label, field.decl().expr);
		}
		return result;
	}
	public String getResult() {
		// module
		String resultString = "module SD";
		
		// abstract
		resultString += "\n\n// abstract";
		for (Object object : _mapping_sigs.values()) {
			PrimSig p = (PrimSig) object;
			if(p.isAbstract != null)resultString += String.format("\nabstract sig %s {%s}", p.toString(), fields2String(p.getFields()));
		}
		
		// classes and names for lifelines, messages
		resultString += "\n\n// classes and names";
		for (Object object : _mapping_sigs.values()) {
			PrimSig p = (PrimSig) object;
			if(p.isTopLevel() && p.getFields().size() == 0){
				if(p.isOne != null)resultString += String.format("\none sig %s{%s}", p.toString(), fields2String(p.getFields()));
				if(p.isLone != null)resultString += String.format("\nlone sig %s{%s}", p.toString(), fields2String(p.getFields()));
				if(p.isSome != null)resultString += String.format("\nsome sig %s{%s}", p.toString(), fields2String(p.getFields()));
			}
		}
		
		// SDs existence of lifelines, messages, combined fragment
		resultString += "\n\n// SDs";
		for (Object object : _mapping_sigs.values()) {
			PrimSig p = (PrimSig) object;
			if (p.isTopLevel() && p.getFields().size() != 0) {
				if (p.isOne != null)
					resultString += String.format("\none sig %s{%s}",
							p.toString(), fields2String(p.getFields()));
				if (p.isLone != null)
					resultString += String.format("\nlone sig %s{%s}",
							p.toString(), fields2String(p.getFields()));
				if (p.isSome != null)
					resultString += String.format("\nsome sig %s{%s}",
							p.toString(), fields2String(p.getFields()));
			}
		}
		
		// sigs Lifeline, Message, Event, CombinedFragment
		resultString += "\n\n// sigs";
		for (Object object : _mapping_sigs.values()) {
			PrimSig p = (PrimSig) object;
			if(!p.isTopLevel()){
				if(p.isOne != null)resultString += String.format("\none sig %s extends %s{%s}", p.toString(), p.parent, fields2String(p.getFields()));
				if(p.isLone != null)resultString += String.format("\nlone sig %s extends %s{%s}", p.toString(), p.parent, fields2String(p.getFields()));
				if(p.isSome != null)resultString += String.format("\nsome sig %s extends %s{%s}", p.toString(), p.parent, fields2String(p.getFields()));
			}
		}
		resultString += "\n\n// facts";
		resultString += "\nfact{";
		for (Object object : _mapping_facts.values()) {
			if(object.getClass() != edu.mit.csail.sdg.alloy4compiler.ast.ExprQt.class)continue;
			ExprQt expr = (ExprQt) object;
			String check = expr.decls.get(0).expr.toString();
			String string = "";
			if(check.indexOf("_Lifeline_") != -1){
				string = String.format("(all L1:%s, L2:%s | (L1.name = L2.name && L1.type = L2.type) => # L2 = 0)", expr.decls.get(0).expr,expr.decls.get(1).expr);
			}else{
				string = String.format("(all M1:%s, M2:%s | (M1.name = M2.name) => # M2 = 0)", expr.decls.get(0).expr, expr.decls.get(1).expr);
			}
			resultString += "\n" + string;
		}
		resultString += "\n}";
		
		resultString += "\n\nfact{"; //ExprUnary
		for (Object object : _mapping_facts.values()) {
			if(object.getClass() != edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary.class)continue;
			Expr expr = (Expr) object;
			resultString += String.format("\n%s", expr);
		}
		resultString += "\n}";
		
		resultString += "\n\nfact{"; //ExprUnary
		for (Object object : _mapping_facts.values()) {
			if(object.getClass() != edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary.class)continue;
			ExprUnary expr = (ExprUnary) object;
			resultString += String.format("\n%s", expr);
		}
		resultString += "\n}";
		
		for (Object object : _mapping_facts.values()) {
			if(object.getClass() != String.class)continue;
			resultString += "\n\n" + object.toString();
		}
		
		resultString += "\n\n//avoid the circle\nfact {all e:EVENT  | e !in e.^ISBEFORE}";
		resultString += "\n\n//no event can be direct child and subChild of other event at the same time\nfact {no _E: EVENT, _E1: EVENT | _E in _E1.ISBEFORE.^ISBEFORE and _E in _E1.ISBEFORE}";
//		resultString += "\n// predicator\npred merge{\n";
//		for (Expr expr : _predicator) {
//			resultString += expr.toString() + "\n";
//		}
//		resultString += "}\n";
//		
		resultString += "\nrun {}\n";
		return resultString;
	}
	public Object getSigByName(String name) {
		return _mapping_sigs.get(name);
	}
	public String getName(String oringinalName){
		return _mapping_name.get(oringinalName);
	}
	public void addFact(Object object){
		if(!_mapping_facts.containsKey(object.toString())){
			_mapping_facts.put(object.toString(), object);
		}
	}
	private boolean addEqLifeline(String lifeline1, String lifeline2){
		try {
			// change life line 2 to lone
			String sdID1 = lifeline1.split("_")[0];
			String sdID2 = lifeline2.split("_")[0];
			if(sdID1.equals(sdID2)){
				SDConsole.print_has_time("Can not add equality between lifelines in same diagram.\nThis equality has been ignored.");
				return false;
			}
			String llID1 = this.getName(lifeline1);
			String llID2 = this.getName(lifeline2);

			PrimSig sig1 = (PrimSig) this.getSigByName(llID1);
			PrimSig sig2 = (PrimSig) this.getSigByName(llID2);
			SafeList<Field> fields = sig2.getFields();
			
			PrimSig newSig2 = new PrimSig(sig2.label, sig2.parent, Attr.LONE);
			for (Field field : fields) {
				newSig2.addField(field.label, field.decl().expr);
			}
			this._mapping_sigs.values().remove(sig2);
			this.addSig(newSig2);
			
			// add fact
			// all SD1L:SD1_Lifeline_1 , SD2L:SD2_Lifeline_2 | SD1L.name = SD2L.name && SD1L.type = SD2L.type
			Expr expr1 = sig1.join(sig1.getFields().get(0)).equal(sig2.join(sig2.getFields().get(0)));
			Expr expr2 = sig1.join(sig1.getFields().get(1)).equal(sig2.join(sig2.getFields().get(1)));
			
			
			PrimSig tmpPrimSig = new PrimSig("asd", Attr.ONE);
			tmpPrimSig.addField("L1:" + sig1, sig1);
			tmpPrimSig.addField("L2:" + sig2, sig2);
			SafeList<Field> fields2 = tmpPrimSig.getFields();
			Expr expr3 = expr1.and(expr2).forAll(fields2.get(0).decl(), fields2.get(1).decl());
			AlloyModel.getInstance().addFact(expr3);
			
//			// add function pred
//			Expr newFact = sig2.cardinality().lt(ExprConstant.makeNUMBER(1));
//			_predicator.add(newFact);
			
			
			
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	private boolean addEqMessage(String message1, String message2){
		try {
			// check the life lines
			String sdID1 = message1.split("_")[0];
			String sdID2 = message2.split("_")[0];
			if(sdID1.equals(sdID2))return false;
			PrimSig sig1 = (PrimSig) this.getSigByName(message1);
			PrimSig sig2 = (PrimSig) this.getSigByName(message2);
			String sig1_send_event_name = sig1.getFields().get(0).decl().expr.type().toExpr().toString();
			String sig2_send_event_name = sig2.getFields().get(0).decl().expr.type().toExpr().toString();
			String sig1_rec_event_name = sig1.getFields().get(1).decl().expr.type().toExpr().toString(); 
			String sig2_rec_event_name = sig2.getFields().get(1).decl().expr.type().toExpr().toString(); 
			
			PrimSig sig1_send_event = (PrimSig) this.getSigByName(sig1_send_event_name);
			PrimSig sig2_send_event = (PrimSig) this.getSigByName(sig2_send_event_name);
			PrimSig sig1_rec_event = (PrimSig) this.getSigByName(sig1_rec_event_name);
			PrimSig sig2_rec_event = (PrimSig) this.getSigByName(sig2_rec_event_name);
			
			String sig1_send_lifeline_name1 = sig1_send_event.getFields().get(0).decl().expr.type().toExpr().toString();
			String sig1_rec_lifeline_name1 = sig1_rec_event.getFields().get(0).decl().expr.type().toExpr().toString();
			String sig2_send_lifeline_name1 = sig2_send_event.getFields().get(0).decl().expr.type().toExpr().toString();
			String sig2_rec_lifeline_name1 = sig2_rec_event.getFields().get(0).decl().expr.type().toExpr().toString();
			
			String sig1_send_lifeline_name = ((PrimSig)getSigByName(sig1_send_lifeline_name1)).getFields().get(1).decl().expr.type().toExpr().toString();
			String sig1_rec_lifeline_name = ((PrimSig)getSigByName(sig1_rec_lifeline_name1)).getFields().get(1).decl().expr.type().toExpr().toString();
			String sig2_send_lifeline_name = ((PrimSig)getSigByName(sig2_send_lifeline_name1)).getFields().get(1).decl().expr.type().toExpr().toString();
			String sig2_rec_lifeline_name = ((PrimSig)getSigByName(sig2_rec_lifeline_name1)).getFields().get(1).decl().expr.type().toExpr().toString();
			
			// the from/to life lines do not match
			if(!sig1_send_lifeline_name.equals(sig2_send_lifeline_name)){
				SDConsole.print_has_time("Can not add equality between messages whose begin and end lifelines do not match.\nThis equality has been ignored");
				return false;
			}
			if(!sig1_rec_lifeline_name.equals(sig2_rec_lifeline_name)){
				SDConsole.print_has_time("Can not add equality between messages whose begin and end lifelines do not match.\nThis equality has been ignored");
				return false;
			}
			
			String sig1_send_lifeline_class = ((PrimSig)getSigByName(sig1_send_lifeline_name1)).getFields().get(0).decl().expr.type().toExpr().toString();
			String sig1_rec_lifeline_class = ((PrimSig)getSigByName(sig1_rec_lifeline_name1)).getFields().get(0).decl().expr.type().toExpr().toString();
			String sig2_send_lifeline_class = ((PrimSig)getSigByName(sig2_send_lifeline_name1)).getFields().get(0).decl().expr.type().toExpr().toString();
			String sig2_rec_lifeline_class = ((PrimSig)getSigByName(sig2_rec_lifeline_name1)).getFields().get(0).decl().expr.type().toExpr().toString();
			
			// the from/to life lines do not match
			if(!sig1_send_lifeline_class.equals(sig2_send_lifeline_class)){
				SDConsole.print_has_time("Can not add equality between messages whose begin and end lifelines do not match.\nThis equality has been ignored");
				return false;
			}
			if(!sig1_rec_lifeline_class.equals(sig2_rec_lifeline_class)){
				SDConsole.print_has_time("Can not add equality between messages whose begin and end lifelines do not match.\nThis equality has been ignored");
				return false;
			}
			
			// make sig to lone and replace the fields
			SafeList<Field> fields = sig1.getFields();
			
			PrimSig newSig2 = new PrimSig(sig2.label, sig2.parent, Attr.LONE);
			for (Field field : fields) {
				newSig2.addField(field.label, field.decl().expr);
			}
			this._mapping_sigs.values().remove(sig2);
			this.addSig(newSig2);
			
			// add the fact
			// add fact
			// all SD1L:SD1_Lifeline_1 , SD2L:SD2_Lifeline_2 | SD1L.name = SD2L.name && SD1L.type = SD2L.type
			Expr expr1 = sig1.join(sig1.getFields().get(0)).equal(sig2.join(sig2.getFields().get(0)));

			PrimSig tmpPrimSig = new PrimSig("asd", Attr.ONE);
			tmpPrimSig.addField("L1:" + sig1, sig1);
			tmpPrimSig.addField("L2:" + sig2, sig2);
			SafeList<Field> fields2 = tmpPrimSig.getFields();
			Expr expr3 = expr1.forAll(fields2.get(0).decl(), fields2.get(1).decl());
			AlloyModel.getInstance().addFact(expr3);
			
			
			
			} catch (Err e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	public boolean addEquality(String left, String right){
		String lName = getName(left), rName = getName(right);
		Object lObject = getSigByName(lName), rObject = getSigByName(rName);
		if(lObject != null && rObject != null){
			PrimSig sig1 = (PrimSig) lObject;
			if(sig1.getFields().size() == 3)
				this.addEqMessage(lName, rName);
			if(sig1.getFields().size() == 2)
				this.addEqLifeline(left, right);
			return true;
		}
		return false;
	}
}
