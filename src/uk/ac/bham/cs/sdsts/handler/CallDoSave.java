package uk.ac.bham.cs.sdsts.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;

import uk.ac.bham.cs.sdsts.SDConsole;


public class CallDoSave extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
//		IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
//		NullProgressMonitor monitor = new NullProgressMonitor();
//		if ( editorReferences != null ){ 
//		    for (IEditorReference iEditorReference : editorReferences) {
//		        IEditorPart editor = iEditorReference.getEditor(false);
//		        
//		        if ( editor.isDirty() )
//		            editor.doSave(monitor);
//		    }
//		}
		NullProgressMonitor monitor = new NullProgressMonitor();
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		editor.doSave(monitor);
		SDConsole.print_has_time("saved " + editor.getTitle());
		return null;
	}

	

}
