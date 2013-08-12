package xmi2obj;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Model;

public class xml2obj {

	/**
	 * @param args
	 */
	
	public static Model load(String filePath){
        //
        //	Init
        //
    	ResourceSet resourceSet = new ResourceSetImpl();
    	org.eclipse.uml2.uml.resources.util.UMLResourcesUtil.init(resourceSet);
    	
        //
        //	Create
        //
    	Model epo2Model = null;// = createExample();
    	    	
       	// load from file
    	URI filrUri = URI.createFileURI(filePath);
    	Resource resource = resourceSet.createResource(filrUri);
        try {
            resource.load(null);
    		org.eclipse.uml2.uml.Package package_ = (org.eclipse.uml2.uml.Package) resource.getContents().get(0);
            epo2Model = package_.getModel();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    	return epo2Model;
	}

}
