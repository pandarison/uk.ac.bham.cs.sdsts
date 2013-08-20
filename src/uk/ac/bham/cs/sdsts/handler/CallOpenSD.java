package uk.ac.bham.cs.sdsts.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.bham.cs.sdsts.View;
import uk.ac.bham.cs.sdsts.common.ModelManager;
import uk.ac.bham.cs.sdsts.common.SequenceDiagram;

public class CallOpenSD extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
	    IWorkbenchPage page = window.getActivePage();
	    View view = (View) page.findView(View.ID);
	    
		FileDialog filedlg = new FileDialog(window.getShell(), SWT.OPEN);
		
		filedlg.setText("Select Sequence Diagram File");
		filedlg.setFilterPath("SystemRoot");
		filedlg.setFilterNames(new String[]{"Sequence Diagram Files"});
		filedlg.setFilterExtensions(new String[]{"di;uml;notation"});
		String selected=filedlg.open();
		
		SequenceDiagram SD = new SequenceDiagram();
		SD.restore(new Path(selected).removeFileExtension().addFileExtension("uml").toOSString());
		ModelManager.getInstance().AddModel(SD);
		view.getViewer().refresh();
		return null;
	}

}
