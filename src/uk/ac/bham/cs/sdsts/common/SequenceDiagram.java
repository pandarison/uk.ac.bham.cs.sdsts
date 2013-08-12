package uk.ac.bham.cs.sdsts.common;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


public class SequenceDiagram extends Model{

	private IFile iFile;
	private String filePath;
	 
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public IFile getiFile() {
		return iFile;
	}
	public void setiFile(IFile iFile) {
		this.iFile = iFile;
	}
	
	@Override
	public String getFilename() {
		return iFile.getName();
	}
	
	@Override
	public String getId() {
		return filePath;
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String saveState() {
		return "S" + filePath;
	}
	@Override
	public void restore(String str) {
		filePath = str;
		iFile = LinkFile(new Path(filePath).removeFileExtension().addFileExtension("di").toOSString());
		LinkFile(new Path(filePath).removeFileExtension().addFileExtension("uml").toOSString());
		LinkFile(new Path(filePath).removeFileExtension().addFileExtension("notation").toOSString());
	}
	public static IFile LinkFile(String path){
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject("tmp");
		if (!project.exists())
			try {
				project.create(null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (!project.isOpen())
			try {
				project.open(null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		IPath location = new Path(path);
		IFile file = project.getFile(location.lastSegment());
		try {
			file.delete(true, null);
		} catch (CoreException e1) {
		}
		
		try {
			file.createLink(location, IResource.NONE, null);
		} catch (CoreException e) {
		}
		return file;
	}
	@Override
	public String getName() {
		return new Path(filePath).removeFileExtension().lastSegment();
	}

}
