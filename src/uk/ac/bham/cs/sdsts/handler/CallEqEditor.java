package uk.ac.bham.cs.sdsts.handler;



import static org.eclipse.papyrus.uml.diagram.wizards.Activator.log;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import uk.ac.bham.cs.sdsts.View;
import uk.ac.bham.cs.sdsts.Model.Equality;
import uk.ac.bham.cs.sdsts.Model.SequenceDiagram;
import uk.ac.bham.cs.sdsts.editor.EqEditor;
import uk.ac.bham.cs.sdsts.editor.EqEditorInput;

public class CallEqEditor extends AbstractHandler {

	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    // Get the view
	    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
	    IWorkbenchPage page = window.getActivePage();
	    View view = (View) page.findView(View.ID);
	    // Get the selection
	    ISelection selection = view.getSite().getSelectionProvider().getSelection();
	    if (selection != null && selection instanceof IStructuredSelection) {
	      Object obj = ((IStructuredSelection) selection).getFirstElement();
	      // If we had a selection lets open the editor
	    if (obj != null) {
	    	if(obj instanceof Equality){
	    		Equality equality = (Equality) obj;
	  	        EqEditorInput input = new EqEditorInput(equality.getFilepath());
	  	        try {
	  	        	if(page.findEditor(input) == null){
	  	        		page.openEditor(input, EqEditor.ID);
	  	        	}
	  	        	else{
	  	        		page.activate(page.findEditor(input));
	  	        	}

	  	        } catch (PartInitException e) {
	  	        	e.printStackTrace();
	  	          throw new RuntimeException(e);
	  	        }
	    	}
	    	if(obj instanceof SequenceDiagram){
	    		SequenceDiagram sequenceDiagram = (SequenceDiagram) obj;
	    		IWorkbenchPage page1 = window.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	    		if(page1 != null) {
	    			try {
	    				IDE.openEditor(page1, sequenceDiagram.getiFile(), true);
	    			} catch (PartInitException e) {
	    				log.error(e);
	    			}
	    		}
	    	}
	      
	      }
	    }
	    return null;
	}

	
}
