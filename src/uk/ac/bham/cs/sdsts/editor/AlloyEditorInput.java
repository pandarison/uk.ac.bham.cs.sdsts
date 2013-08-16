package uk.ac.bham.cs.sdsts.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class AlloyEditorInput implements IEditorInput{

	private String codeString;
	private String editorTitle;
	public String getEditorTitle() {
		return editorTitle;
	}
	public void setEditorTitle(String editorTitle) {
		this.editorTitle = editorTitle;
	}
	public AlloyEditorInput(String str){
		codeString = str;
	}
	public String getCodeString(){
		return codeString;
	}
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return null;
	}

}
