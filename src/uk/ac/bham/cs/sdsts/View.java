package uk.ac.bham.cs.sdsts;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import uk.ac.bham.cs.sdsts.common.Equality;
import uk.ac.bham.cs.sdsts.common.Model;
import uk.ac.bham.cs.sdsts.common.ModelManager;
import uk.ac.bham.cs.sdsts.common.SequenceDiagram;

public class View extends ViewPart {
	public static final String ID = "uk.ac.bham.cs.sdsts.view";

	private ListViewer viewer;
	private IMemento memento;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		restore();
		viewer = new ListViewer(parent);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				Model p = (Model) element;
				return p.getFilename();
			};
		});
		viewer.setInput(ModelManager.getInstance().getModels());
		getSite().setSelectionProvider(viewer);
		hookDoubleClickCommand();
		hookRightClickMenu();
	}

	private void hookRightClickMenu(){
		final Action a = new Action("") {
			@Override
			public void run(){
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				Model model = (Model) selection.getFirstElement();
				ModelManager.getInstance().removeModel(model);
				viewer.refresh();
			}
		};
		a.setText("Delete");
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				System.err.println("R-Clicked");
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (!selection.isEmpty()) {
					mgr.add(a);
				}
			}
		});
		viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
	}
	private void hookDoubleClickCommand() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
				try {
					handlerService.executeCommand("uk.ac.bham.cs.sdsts.openEqEditor", null);
				} catch (Exception ex) {
					ex.printStackTrace();
					throw new RuntimeException("uk.ac.bham.cs.sdsts.openEqEditor not found.\n");
				}
			}
		});
	}
	public void restore(){
		if(this.memento == null)return;
		IMemento selectionsMomento = this.memento.getChild("model_list");
		if (selectionsMomento != null) {
			IMemento models[] = selectionsMomento.getChildren("model_node");
			for (IMemento iMemento : models) {
				String string = iMemento.getID();
				if(string.charAt(0) == 'E')
				{
					Equality equality = new Equality("");
					equality.restore(string.substring(1));
					ModelManager.getInstance().AddModel(equality);
				}
				if(string.charAt(0) == 'S'){
					SequenceDiagram sequenceDiagram = new SequenceDiagram();
					sequenceDiagram.restore(string.substring(1));
					ModelManager.getInstance().AddModel(sequenceDiagram);
				}
			}
		}
		selectionsMomento = this.memento.getChild("model_font");
		if(selectionsMomento != null){
			IMemento fontName = selectionsMomento.getChild("name");
			ModelManager.getInstance().setFont(fontName.getID());
			IMemento fontSize = selectionsMomento.getChild("size");
			ModelManager.getInstance().setFont_size(fontSize.getID());
		}
		
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void saveState(IMemento memento) {
		ArrayList<Model> models = ModelManager.getInstance().getModels();
		if (models.isEmpty()) {
			return;
		}
		IMemento MODELs = memento.createChild("model_list");
		Iterator iter = models.iterator();
		while (iter.hasNext()) {
			Model model = (Model) iter.next();
			MODELs.createChild("model_node", model.saveState());
		}
		memento = memento.createChild("model_font");
		memento.createChild("name", ModelManager.getInstance().getFont());
		memento.createChild("size", ModelManager.getInstance().getFont_size());
	}

	@Override
	public void init(final IViewSite site, final IMemento memento)
			throws PartInitException {
		init(site);
		this.memento = memento;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public ListViewer getViewer() {
		return viewer;
	}
}