package uk.ac.bham.cs.sdsts.editor;


import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.internal.impl.LifelineImpl;
import org.eclipse.uml2.uml.internal.impl.MessageImpl;

import uk.ac.bham.cs.sdsts.View;
import uk.ac.bham.cs.sdsts.common.Equality;
import uk.ac.bham.cs.sdsts.common.Model;
import uk.ac.bham.cs.sdsts.common.ModelManager;
import uk.ac.bham.cs.sdsts.common.SequenceDiagram;
import uk.ac.bham.cs.sdsts.core.synthesis.Xml2obj;

@SuppressWarnings("restriction")
public class EqEditor extends EditorPart {
	public static final String ID = "uk.ac.bham.cs.sdsts.editor.EqEditor";
	private EqEditorInput input;
	private Text text1;
	private Text text2;
	private org.eclipse.swt.widgets.List equalityList;
	private int item1Type;
	private int item2Type;
	private boolean dirty = false;
	

	public EqEditor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		Equality equality = (Equality) ModelManager.getInstance().getModel(this.input.getId());
		equality.setEqualities(new ArrayList<String>(Arrays.asList(equalityList.getItems())));
		equality.save();
		if(dirty)this.setPartName(this.getPartName().substring(1));
		dirty = false;
		
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		View view = (View) page.findView(View.ID);
		view.getViewer().refresh();
		
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if(!(input instanceof EqEditorInput)){
			throw new RuntimeException("Wrong input for Equality Editor");
		}
		this.input = (EqEditorInput) input;
		setSite(site);
		setInput(input);
		Equality equality = (Equality) ModelManager.getInstance().getModel(this.input.getId());
		setPartName(equality.getFilename());
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}
	@Override
	public boolean isSaveOnCloseNeeded(){
		return true;
		
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void createPartControl(Composite parent) {
		Equality equality = (Equality) ModelManager.getInstance().getModel(this.input.getId());
		
		GridLayout layout = new GridLayout();
	    layout.numColumns = 1;
	    parent.setLayout(layout);
	    
	    Label label1 = new Label(parent, SWT.NONE);
	    label1.setText("Object 1");
	    text1  = new Text(parent, SWT.BORDER);
	    text1.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
	    text1.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				ArrayList<String> items = new ArrayList<String>();
				for (Model model : ModelManager.getInstance().getModels()) {
					if(model instanceof SequenceDiagram){
						org.eclipse.uml2.uml.Model umlModel = Xml2obj.load(((SequenceDiagram) model).getFilePath());
						for (Element element : umlModel.getOwnedElements().get(0).getOwnedElements()) {
							if(element instanceof MessageImpl){
								if(!text2.getText().equals(String.format("%s_%s", model.getName(), ((MessageImpl) element).getName())))
								items.add(String.format("Message:  %s_%s", model.getName(), ((MessageImpl) element).getName()));
							}
							if(element instanceof LifelineImpl){
								if(!text2.getText().equals(String.format("%s_%s", model.getName(), ((LifelineImpl) element).getName())))
								items.add(String.format("Lifeline:  %s_%s", model.getName(), ((LifelineImpl) element).getName()));
							}
						}
					}
				}
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), new LabelProvider());
				dialog.setTitle("Select the first object:");
				dialog.setElements(items.toArray());
				if(dialog.open() != Window.OK)
					return;
				String result = dialog.getResult()[0].toString().split("  ")[1];
				if(dialog.getResult()[0].toString().split("  ")[0].equals("Lifeline:")){
					if(item2Type == 2)text2.setText("");
					item1Type = 1;
				}
				else{ 
					if(item2Type == 1)text2.setText("");
					item1Type = 2;
				}
				text1.setText(result);
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    Label label2 = new Label(parent, SWT.NONE);
	    label2.setText("Object 2");
	    text2 = new Text(parent, SWT.BORDER);
	    text2.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
	    text2.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				ArrayList<String> items = new ArrayList<String>();
				for (Model model : ModelManager.getInstance().getModels()) {
					if(model instanceof SequenceDiagram){
						org.eclipse.uml2.uml.Model umlModel = Xml2obj.load(((SequenceDiagram) model).getFilePath());
						for (Element element : umlModel.getOwnedElements().get(0).getOwnedElements()) {
							if(element instanceof MessageImpl){
								if(!text1.getText().equals(String.format("%s_%s", model.getName(), ((MessageImpl) element).getName())))
								items.add(String.format("Message:  %s_%s", model.getName(), ((MessageImpl) element).getName()));
							}
							if(element instanceof LifelineImpl){
								if(!text1.getText().equals(String.format("%s_%s", model.getName(), ((LifelineImpl) element).getName())))
								items.add(String.format("Lifeline:  %s_%s", model.getName(), ((LifelineImpl) element).getName()));
							}
						}
					}
				}
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), new LabelProvider());
				dialog.setTitle("Select the second object:");
				dialog.setElements(items.toArray());
				if(dialog.open() != Window.OK)
					return;
				String result = dialog.getResult()[0].toString().split("  ")[1]; 
				if(dialog.getResult()[0].toString().split("  ")[0].equals("Lifeline:")){
					if(item1Type == 2)text1.setText("");
					item2Type = 1;
				}
				else{ 
					if(item1Type == 1)text1.setText("");
					item2Type = 2;
				}
				text2.setText(result);
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	   
	    
	    
	    org.eclipse.swt.widgets.Button button = new org.eclipse.swt.widgets.Button(parent, SWT.PUSH);
	    button.setText("Add");
	    button.addSelectionListener(new SelectionListener() {
	    	
			@Override
			public void widgetSelected(SelectionEvent e) {

				if(text1.getText().equals("") || text2.getText().equals("")){
					MessageBox mBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
					mBox.setText("Error");
					mBox.setMessage("Both Object 1 and Object 2 must not be empty.");
					mBox.open();
					return;
				}
				String str1 = text1.getText(), str2 = text2.getText();
				if(text1.getText().compareTo(text2.getText()) > 0){
					str1 = text2.getText();
					str2 = text1.getText();
				}
				for (String string : equalityList.getItems()) {
					if(string.equals(str1 + " <=> " + str2)){
						MessageBox mBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
						mBox.setText("Error");
						mBox.setMessage("This expression already exists.");
						mBox.open();
						return;
					}
				}
				dirty = true;
				EqEditor thisEditor = (EqEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				if(thisEditor.getPartName().charAt(0) != '*')
					thisEditor.setPartName("*" + thisEditor.getPartName());
				equalityList.add(str1 + " <=> " + str2);
				text1.setText("");
				text2.setText("");
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    Composite textComposite = new Composite(parent, SWT.FILL);
	    GridLayout textLayout = new GridLayout();
	    textLayout.numColumns = 1;
	    textComposite.setLayout(textLayout);
	 
	    Label label3 = new Label(textComposite, SWT.NONE);
	    label3.setText("Equalities:");
	    
	    equalityList = new List(textComposite, SWT.SINGLE);
	    for (String string : equality.getEqualities()) {
			equalityList.add(string);
		}
	    hookRightClickMenu();
	    
	    GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
	    gridData.heightHint = 200;
	    gridData.widthHint = 800;
	    equalityList.setLayoutData(gridData);
	    text2.setEditable(false);
	    text1.setEditable(false);
	   
//	    class MyModifyListener implements ModifyListener{
//	    	public MyModifyListener(Equality equality){
//	    		this.equality = equality;
//	    	}
//	    	private Equality equality;
//			@Override
//			public void modifyText(ModifyEvent e) {
//				equality.setEqualities(((Text)e.widget).getText());
//				IWorkbench wb = PlatformUI.getWorkbench();
//				IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
//				IWorkbenchPage page = win.getActivePage();
//				View view = (View) page.findView(View.ID);
//				view.getViewer().refresh();
//			}
//	    };
//	    
//	    text3.addModifyListener(new MyModifyListener(equality));
	    
	}
	private void hookRightClickMenu(){
		final Action a = new Action("") {
			@Override
			public void run(){
				String[] selection = equalityList.getSelection();
				for (String string : selection) {
					dirty = true;
					EqEditor thisEditor = (EqEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
					if(thisEditor.getPartName().charAt(0) != '*')
						thisEditor.setPartName("*" + thisEditor.getPartName());
					equalityList.remove(string);
				}
			}
		};
		a.setText("Delete");
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				String[] selection = equalityList.getSelection();
				if (selection.length > 0) {
					mgr.add(a);
				}
			}
		});
		equalityList.setMenu(mgr.createContextMenu(equalityList.getParent()));
		
	}
	@Override
	public void setFocus() {
//		Equality equality = (Equality) ModelManager.getInstance().getModel(this.input.getId());
//		text3.setText(equality.getEqualities());
	}
	
	

}
