package uk.ac.bham.cs.sdsts.handler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import kodkod.engine.fol2sat.HigherOrderDeclException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import uk.ac.bham.cs.sdsts.SDConsole;
import uk.ac.bham.cs.sdsts.editor.AlloyEditor;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Computer;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4.ErrorType;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.OurDialog;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4.Version;
import edu.mit.csail.sdg.alloy4.XMLNode;
import edu.mit.csail.sdg.alloy4.Util.BooleanPref;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.sim.SimInstance;
import edu.mit.csail.sdg.alloy4compiler.sim.SimTuple;
import edu.mit.csail.sdg.alloy4compiler.sim.SimTupleset;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4SolutionReader;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
import edu.mit.csail.sdg.alloy4compiler.translator.A4TupleSet;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import edu.mit.csail.sdg.alloy4viz.VizGUI;

public class CallOpenAlloyViz extends AbstractHandler {

	public static IFile iFile;
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			String result = AlloyEditor.getText();
			IWorkspace  ws = ResourcesPlugin.getWorkspace();
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
			iFile = project.getFile(System.currentTimeMillis() + ".als");
			
			File file = new File(iFile.getLocation().toString());
			FileWriter writer = new FileWriter(file);
			writer.write(result);
			writer.flush();
			writer.close();
			Runnable newThread = new Runnable() {
				@Override
				public void run() {
					try {
						openAlloyWiz(new String[]{iFile.getLocation().toString()});
					} catch (Err e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			new Thread(newThread).start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static A4Solution ans = null;
	public static VizGUI viz = null;
	public static int count = 0;
	public static int current = 1;
	public static uk.ac.bham.cs.sdsts.handler.SimpleGUI simpleGUI;
	public static void openAlloyWiz(String[] args) throws Err {


        // Alloy4 sends diagnostic messages and progress reports to the A4Reporter.
        // By default, the A4Reporter ignores all these events (but you can extend the A4Reporter to display the event for the user)
        A4Reporter rep = new A4Reporter() {
            // For example, here we choose to display each "warning" by printing it to System.out
            @Override public void warning(ErrorWarning msg) {
                System.out.print("Relevance Warning:\n"+(msg.toString().trim())+"\n\n");
                System.out.flush();
            }
        };

        for(String filename:args) {

            // Parse+typecheck the model
           // System.out.println("=========== Parsing+Typechecking "+filename+" =============");
        	SDConsole.print_has_time("Parsing and Typechecking Alloy Model.");
        	Module world = null;
        	try {
        		  world = CompUtil.parseEverything_fromFile(rep, null, filename);
			} catch (Err e) {
				SDConsole.print_has_time(e.msg + "\n" +  e.pos.toString().subSequence(0, e.pos.toString().indexOf("filename")-2));
				String tmpString = e.pos.toString().subSequence(0, e.pos.toString().indexOf("filename")-2).toString();
				tmpString =  tmpString.substring(tmpString.indexOf(' '));
				String firstNum = tmpString.substring(1, tmpString.indexOf(','));
				tmpString = tmpString.substring(tmpString.indexOf("column "));
				tmpString = tmpString.substring(tmpString.indexOf(" "));
				AlloyEditor.coloraLineColumn(Integer.parseInt(firstNum), e.pos.x, e.pos.x2);
				//AlloyEditor.coloraLineColumn(Integer.parseInt(firstNum), e.pos.x, e.pos.x2 - e.pos.x);
			}
          

            // Choose some default options for how you want to execute the commands
            A4Options options = new A4Options();
            options.solver = A4Options.SatSolver.SAT4J;

            if(world != null)
            for (Command command: world.getAllCommands()) {
                // Execute the command
                //System.out.println("============ Command "+command+": ============");
                ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);
                // Print the outcome
                //System.out.println(ans);
                SDConsole.print_has_time(ans.toString());
                // If satisfiable...
                if (ans.satisfiable()) {
                    // You can query "ans" to find out the values of each set or type.
                    // This can be useful for debugging.
                    //
                    // You can also write the outcome to an XML file
                	count = 0;
                	current = 1;
                	while(ans.satisfiable()){
						count++;
						ans.writeXML(count + "_alloy_example_output.xml");
						ans = ans.next();
					}
                    
                    //
                    // You can then visualize the XML file by calling this:
                    if (true) {
                    	viz = new VizGUI(false, current++ + "_alloy_example_output.xml", null, new Computer() {
							
							@Override
							public String compute(Object input) throws Exception {	
								if(current > count){
										OurDialog.alert("No more satisfying instances.");
										return null;
								}
								viz.loadXML(current + "_alloy_example_output.xml", false);
								current ++;
								return (String) input;
							}
						}, evaluator);
                        //viz = new VizGUI(false, "alloy_example_output.xml", null);
                    } 
                }
            }
        }
    }
    private static Computer evaluator = new Computer() {
	        private String filename = null;
	        public final String compute(final Object input) throws Exception {
	            if (input instanceof File) { filename = ((File)input).getAbsolutePath(); return ""; }
	            if (!(input instanceof String)) return "";
	            final String str = (String)input;
	            if (str.trim().length()==0) return ""; // Empty line
	            Module root = null;
	            A4Solution ans = null;
	            try {
	                Map<String,String> fc = new LinkedHashMap<String,String>();
	                XMLNode x = new XMLNode(new File(filename));
	                if (!x.is("alloy")) throw new Exception();
	                String mainname=null;
	                for(XMLNode sub: x) if (sub.is("instance")) {
	                   mainname=sub.getAttribute("filename");
	                   break;
	                }
	                if (mainname==null) throw new Exception();
	                for(XMLNode sub: x) if (sub.is("source")) {
	                   String name = sub.getAttribute("filename");
	                   String content = sub.getAttribute("content");
	                   fc.put(name, content);
	                }
	                root = CompUtil.parseEverything_fromFile(A4Reporter.NOP, fc, mainname, (Version.experimental && ImplicitThis.get()) ? 2 : 1);
	                ans = A4SolutionReader.read(root.getAllReachableSigs(), x);
	                for(ExprVar a:ans.getAllAtoms())   { root.addGlobal(a.label, a); }
	                for(ExprVar a:ans.getAllSkolems()) { root.addGlobal(a.label, a); }
	            } catch(Throwable ex) {
	                throw new ErrorFatal("Failed to read or parse the XML file.");
	            }
	            try {
	                Expr e = CompUtil.parseOneExpression_fromString(root, str);
	                if ("yes".equals(System.getProperty("debug")) && Verbosity.get()==Verbosity.FULLDEBUG) {
	                    SimInstance simInst = convert(root, ans);
	                    return simInst.visitThis(e).toString() + (simInst.wasOverflow() ? " (OF)" : "");
	                } else
	                   return ans.eval(e).toString();
	            } catch(HigherOrderDeclException ex) {
	                throw new ErrorType("Higher-order quantification is not allowed in the evaluator.");
	            }
	        }
	    };
	    private enum Verbosity {
	        /** Level 0. */  DEFAULT("0", "low"),
	        /** Level 1. */  VERBOSE("1", "medium"),
	        /** Level 2. */  DEBUG("2", "high"),
	        /** Level 3. */  FULLDEBUG("3", "debug only");
	        /** Returns true if it is greater than or equal to "other". */
	        @SuppressWarnings("unused")
			public boolean geq(Verbosity other) { return ordinal() >= other.ordinal(); }
	        /** This is a unique String for this value; it should be kept consistent in future versions. */
	        private final String id;
	        /** This is the label that the toString() method will return. */
	        private final String label;
	        /** Constructs a new Verbosity value with the given id and label. */
	        private Verbosity(String id, String label) { this.id=id; this.label=label; }
	        /** Given an id, return the enum value corresponding to it (if there's no match, then return DEFAULT). */
	        private static Verbosity parse(String id) {
	            for(Verbosity vb: values()) if (vb.id.equals(id)) return vb;
	            return DEFAULT;
	        }
	        /** Returns the human-readable label for this enum value. */
	        @Override public final String toString() { return label; }
	        /** Saves this value into the Java preference object. */
	        @SuppressWarnings("unused")
			private void set() { Preferences.userNodeForPackage(Util.class).put("Verbosity",id); }
	        /** Reads the current value of the Java preference object (if it's not set, then return DEFAULT). */
	        private static Verbosity get() { return parse(Preferences.userNodeForPackage(Util.class).get("Verbosity","")); }
	    };

	    /** True if Alloy Analyzer should enable the new Implicit This name resolution. */
	    private static final BooleanPref ImplicitThis = new BooleanPref("ImplicitThis");
	    
	    /** Converts an A4Solution into a SimInstance object. */
	    private static SimInstance convert(Module root, A4Solution ans) throws Err {
	       SimInstance ct = new SimInstance(root, ans.getBitwidth(), ans.getMaxSeq());
	        for(Sig s: ans.getAllReachableSigs()) {
	            if (!s.builtin) ct.init(s, convert(ans.eval(s)));
	            for(Field f: s.getFields())  if (!f.defined)  ct.init(f, convert(ans.eval(f)));
	        }
	        for(ExprVar a:ans.getAllAtoms())   ct.init(a, convert(ans.eval(a)));
	        for(ExprVar a:ans.getAllSkolems()) ct.init(a, convert(ans.eval(a)));
	        return ct;
	    }
	    /** Converts an A4TupleSet into a SimTupleset object. */
	    private static SimTupleset convert(Object object) throws Err {
	        if (!(object instanceof A4TupleSet)) throw new ErrorFatal("Unexpected type error: expecting an A4TupleSet.");
	        A4TupleSet s = (A4TupleSet)object;
	        if (s.size()==0) return SimTupleset.EMPTY;
	        List<SimTuple> list = new ArrayList<SimTuple>(s.size());
	        int arity = s.arity();
	        for(A4Tuple t: s) {
	            String[] array = new String[arity];
	            for(int i=0; i<t.arity(); i++) array[i] = t.atom(i);
	            list.add(SimTuple.make(array));
	        }
	        return SimTupleset.make(list);
	    }
}
