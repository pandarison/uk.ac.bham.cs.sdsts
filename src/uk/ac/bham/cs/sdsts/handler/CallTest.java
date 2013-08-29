package uk.ac.bham.cs.sdsts.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class CallTest extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		test(event);
		
		return null;
	}
	private void test(ExecutionEvent event){
//		IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
//		PapyrusMultiDiagramEditor papyrusEditor =((PapyrusMultiDiagramEditor)editorPart);
//		UmlSequenceDiagramForMultiEditor diagramEditor = (UmlSequenceDiagramForMultiEditor)papyrusEditor.getActiveEditor();
//		DiagramEditPart clazzdiagrameditPart = (DiagramEditPart)diagramEditor.getGraphicalViewer().getEditPartRegistry().get(diagramEditor.getDiagram());				//(DiagramEditPart)diagramEditor.getGraphicalViewer().getEditPartRegistry().get(diagramEditor.getDiagram());
		
	}
	

}
