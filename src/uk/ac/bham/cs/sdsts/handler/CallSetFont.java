/***
 *  Author: Yi Chen
 */
package uk.ac.bham.cs.sdsts.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.bham.cs.sdsts.Model.ModelManager;
import uk.ac.bham.cs.sdsts.editor.AlloyEditor;

public class CallSetFont extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
	    FontDialog fd = new FontDialog(window.getShell(), SWT.NONE);
        fd.setText("Select Font");
        fd.setRGB(new RGB(0, 0, 255));
        //FontData defaultFont = new FontData("Courier", 10, SWT.BOLD);
        FontData newFont = fd.open();
        if (newFont == null)return null;
        ModelManager.getInstance().setFont(newFont.getName());
        ModelManager.getInstance().setFont_size("" + newFont.getHeight());
        AlloyEditor.setFont(newFont.getName(), "" + newFont.getHeight());
		return null;
	}


}
