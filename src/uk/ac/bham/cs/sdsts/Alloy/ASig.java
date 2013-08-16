package uk.ac.bham.cs.sdsts.Alloy;

import java.util.HashMap;
import java.util.Map.Entry;

public class ASig extends AObject{
	// Name of the signature
	private String _name;
	
	// Attributes of the signature
	private AAttr _attr;
	
	// Parent of the signature
	private ASig _parent;

	// Fields of the signature
	private HashMap<String, AObject> _fields;
	
	public void AddField(String label, AObject object){
		if(!_fields.containsKey(label) && object != null && label != null)
			_fields.put(label, object);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		// one/lone/abstract...
		sb.append(_attr);
		sb.append("sig ");
		sb.append(_name);
		if(_parent != null)
			sb.append(" extends " + _parent.get_name());
		sb.append("{");
		String comma = "";
		for (Entry<String, AObject> entry : _fields.entrySet()) {
			sb.append(comma + entry.getKey() + ":" + entry.getValue().getAName());
			comma = ", ";
		}
		sb.append("}");
		// TODO Auto-generated method stub
		return sb.toString();
	}

	public ASig(String _name, AAttr _attr, ASig _parent) {
		super();
		this._this = this;
		this._fields = new HashMap<String, AObject>();
		this._name = _name;
		this._attr = _attr;
		this._parent = _parent;
	}
	@Override
	public String getAName(){
		if(this == _this){
			if(_attr == AAttr.SET)
				return _attr + _name;
			else return _name;
		}
		else return ((ASig)_this).getAName();
	}
	public void mergeTo(ASig sig){
		this._this = sig;
	}
	public ASig setOf(){
		ASig sig = new ASig(_name, _attr, _parent);
		sig.set_attr(AAttr.SET);
		return sig;
	}

	
	// getters and setters
	public String get_name() {
		if(this == _this)
			return _name;
		else return ((ASig)_this).get_name();
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	public AAttr get_attr() {
		if(this == _this)
			return _attr;
		else return ((ASig)_this).get_attr();
	}

	public void set_attr(AAttr _attr) {
		this._attr = _attr;
	}

	public ASig get_parent() {
		if(this == _this)
			return _parent;
		else return ((ASig)_this).get_parent();
	}

	public void set_parent(ASig _parent) {
		this._parent = _parent;
	}

	public HashMap<String, AObject> get_fields() {
		if(this == _this)
			return _fields;
		else return ((ASig)_this).get_fields();
	}

	public void set_fields(HashMap<String, AObject> _fields) {
		this._fields = _fields;
	}

}
