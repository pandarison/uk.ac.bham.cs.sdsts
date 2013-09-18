package uk.ac.bham.cs.sdsts.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.bham.cs.sdsts.View;
import uk.ac.bham.cs.sdsts.Model.Equality;
import uk.ac.bham.cs.sdsts.Model.ModelManager;

public class CallCreateEqFile extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
	    IWorkbenchPage page = window.getActivePage();
	    View view = (View) page.findView(View.ID);
	    
		FileDialog filedlg = new FileDialog(window.getShell(), SWT.SAVE);
		
		filedlg.setText("Create Equality File");
		filedlg.setFilterPath("SystemRoot");
		filedlg.setFilterExtensions(new String[]{"eq"});
		String selected=filedlg.open();
		
		Equality equality = new Equality(selected);
		ModelManager.getInstance().AddModel(equality);
		equality.save();
		view.getViewer().refresh();
		return null;
	}

}
