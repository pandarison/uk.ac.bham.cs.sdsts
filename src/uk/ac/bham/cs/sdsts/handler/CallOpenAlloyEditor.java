package uk.ac.bham.cs.sdsts.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.papyrus.editor.PapyrusMultiDiagramEditor;

import uk.ac.bham.cs.sdsts.SDConsole;
import uk.ac.bham.cs.sdsts.common.ModelManager;
import uk.ac.bham.cs.sdsts.common.SequenceDiagram;
import uk.ac.bham.cs.sdsts.core.synthesis.AlloyModel;
import uk.ac.bham.cs.sdsts.core.synthesis.Xml2obj;
import uk.ac.bham.cs.sdsts.editor.AlloyEditor;
import uk.ac.bham.cs.sdsts.editor.AlloyEditorInput;

public class CallOpenAlloyEditor extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the view
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage page = window.getActivePage();
		
		PapyrusMultiDiagramEditor iEditorPart = (PapyrusMultiDiagramEditor) page.getActiveEditor();
		
		URI uri = CommonPlugin.resolve(iEditorPart.getDiagram().eResource().getResourceSet().getResources().get(2).getURI());
		
		String modelID = uri.toFileString();
		SequenceDiagram sequenceDiagram = (SequenceDiagram) ModelManager.getInstance().getModel(modelID);
		SDConsole.print_stars();
		
		SDConsole.print_has_time("Generating Alloy Model for: " + sequenceDiagram.getiFile().getLocation().toOSString());
		AlloyModel.clear();
		
		org.eclipse.uml2.uml.Model umlModel = Xml2obj.load(sequenceDiagram.getFilePath());
		AlloyModel.getInstance().addModel(umlModel, sequenceDiagram.getName());
		SDConsole.print_has_time("The Alloy Model for " + sequenceDiagram.getName() + " is shown in editor.");
		SDConsole.print_stars();
		
		AlloyEditorInput input = new AlloyEditorInput(AlloyModel.getInstance().getResult());
		input.setEditorTitle(sequenceDiagram.getName());
		try {
			page.openEditor(input, AlloyEditor.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return null;
	}
	public static void execute1() throws ExecutionException {
		// Get the view
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
 
		AlloyEditorInput input = new AlloyEditorInput(AlloyModel.getInstance().getResult());
		input.setEditorTitle("merged");
		try {
			page.openEditor(input, AlloyEditor.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

}
