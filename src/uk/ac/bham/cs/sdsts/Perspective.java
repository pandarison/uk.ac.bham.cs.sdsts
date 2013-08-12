package uk.ac.bham.cs.sdsts;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.PlatformUI;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);
		layout.setFixed(true);
		
		
		String editorArea = layout.getEditorArea();
		layout.createFolder("uk.ac.bham.cs.sdsts.view", IPageLayout.LEFT, 0.25f,editorArea);
	}

}
