package uk.ac.bham.cs.sdsts;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class SDConsole {
	private static MessageConsoleStream stream = null;
	public static void clear(){
		MessageConsole myConsole = findConsole("console");
		myConsole.clearConsole();
	}
	public static void print_has_time(String str){
		String timeString = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		for (String string : str.split("\n")) {
			getConsoleStram().println(String.format("%s  %s", timeString, string));
			flush();
			timeString = "        ";
		}
	}
	private static void flush() {
		try {
			getConsoleStram().flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void print(String str){
		getConsoleStram().println(str);
		flush();
	}
	public static void print_stars(){
		getConsoleStram().println("****************************************************************************************");
		flush();
	}
	public static MessageConsoleStream getConsoleStram() {
		if(stream != null){
			return stream;
		}
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		MessageConsole myConsole = findConsole("console");
		MessageConsoleStream out = myConsole.newMessageStream();
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		IConsoleView view = null;
		try {
			view = (IConsoleView) page.showView(id);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		view.display(myConsole);
		stream = out;
		return out;
	}

	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (IConsole iConsole : existing) {
			if(iConsole.getName().equals(name))
				return (MessageConsole) iConsole;
		}
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}
}
