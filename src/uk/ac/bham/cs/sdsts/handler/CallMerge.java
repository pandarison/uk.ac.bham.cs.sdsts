package uk.ac.bham.cs.sdsts.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.bham.cs.sdsts.SDConsole;
import uk.ac.bham.cs.sdsts.common.Equality;
import uk.ac.bham.cs.sdsts.common.Model;
import uk.ac.bham.cs.sdsts.common.ModelManager;
import uk.ac.bham.cs.sdsts.common.SequenceDiagram;
import uk.ac.bham.cs.sdsts.core.synthesis.AlloyModel;
import uk.ac.bham.cs.sdsts.core.synthesis.Xml2obj;


public class CallMerge extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		// select the equality file
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(window.getShell(), new LabelProvider());
				
		AlloyModel.clear();
		dialog.setTitle("Select the equality file:");
		ArrayList<String> eqFileList = new ArrayList<String>();
		for (Model model : ModelManager.getInstance().getModels()) {
			if(model instanceof Equality)
				eqFileList.add(model.getFilepath());
		}
		dialog.setElements(eqFileList.toArray());
		if(dialog.open() != Window.OK)
			return null;
		String result = dialog.getResult()[0].toString(); 
		SDConsole.print_stars();
		SDConsole.print_has_time("Selected equality file: " + result);
		
		SDConsole.print_has_time("Start merging.");
		
		List<Model> models = ModelManager.getInstance().getModels();
		for (Model model : models) {
			if(model instanceof SequenceDiagram){
				String filepath = ((SequenceDiagram) model).getFilePath();
				org.eclipse.uml2.uml.Model umlModel = Xml2obj.load(filepath);
				SDConsole.print_has_time("Added sequence diagram model: " + filepath);
				AlloyModel.getInstance().addModel(umlModel, model.getName());
			}
		}
		Equality equality = (Equality) ModelManager.getInstance().getModel(result);
		//String[] lines = equality.getEqualities().split("\n");
		for (String string : equality.getEqualities()) {
			if(string.equals(""))continue;
			String[] lr = string.split(" <=> ");
			if(lr.length < 2 || lr.length > 2){
				SDConsole.print_has_time("There're something wrong in this expressions.\n" + "in line: " + string);
				return null;
			}
			SDConsole.print_has_time("Adding equality: " + string);
			boolean isOk = AlloyModel.getInstance().addEquality(lr[0], lr[1]);	
			if(isOk == false){
				SDConsole.print_has_time(String.format("There're something wrong in this expressions.\nPossible cause:%s or %s are not in the model.\nin line:%s", lr[0], lr[1], string));
				return null;
			}
			
		}
		CallOpenAlloyEditor.execute1();
		SDConsole.print_has_time("== Ended ==");
		SDConsole.print_stars();
		return null;
	}

}
