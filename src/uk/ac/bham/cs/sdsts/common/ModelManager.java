package uk.ac.bham.cs.sdsts.common;


import java.util.ArrayList;

public class ModelManager {
	private static ModelManager modelManager;
	
	private ArrayList<Model> models = new ArrayList<Model>();
	
	public static ModelManager getInstance(){
		if(modelManager == null){
			modelManager = new ModelManager();
		}
		return modelManager;
	}
	
	public void AddModel(Model model){
		models.add(model);
	}
	public Model getModel(String id){
		for (Model model : models) {
			if(model.getId().equals(id))
				return model;
		}
		return null;
	}
	public ArrayList<Model> getModels(){
		return models;
	}
	public void removeModel(Model model){
		this.models.remove(model);
	}
	
}
