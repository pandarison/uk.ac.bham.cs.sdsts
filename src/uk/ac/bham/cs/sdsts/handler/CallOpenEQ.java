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
import uk.ac.bham.cs.sdsts.common.Equality;
import uk.ac.bham.cs.sdsts.common.ModelManager;

public class CallOpenEQ extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
	    IWorkbenchPage page = window.getActivePage();
	    View view = (View) page.findView(View.ID);
	    
		FileDialog filedlg = new FileDialog(window.getShell(), SWT.OPEN);
		
		filedlg.setText("Select Equality File");
		filedlg.setFilterPath("SystemRoot");
		filedlg.setFilterNames(new String[]{"Equality File"});
		filedlg.setFilterExtensions(new String[]{"eq"});
		String selected=filedlg.open();
		
		Equality equality = new Equality("");
		equality.restore(selected);
		ModelManager.getInstance().AddModel(equality);
		view.getViewer().refresh();
		return null;
	}

}
