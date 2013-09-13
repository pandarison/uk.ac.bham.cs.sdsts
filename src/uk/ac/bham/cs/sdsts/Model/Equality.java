/***
 *  Author: Yi Chen
 */
package uk.ac.bham.cs.sdsts.Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.Path;

public class Equality extends Model {

	private ArrayList<String> equalities;

	private File file;

	public Equality(String path) {
		equalities = new ArrayList<String>();
		file = new File(path);
	}

	public ArrayList<String> getEqualities() {
		return equalities;
	}

	@Override
	public String getFilename() {
			return file.getName();
	}

	public String getFilepath() {
		return file.getPath();
	}

	public void setEqualities(ArrayList<String> equalities) {
			this.equalities = equalities;
	}

	@Override
	public void save() {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			for (String string : equalities) {
				fileOutputStream.write((string + "\n").getBytes());
			}
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public String saveState() {
		return "E" + file.getAbsolutePath();
	}

	@Override
	public void restore(String str) {
		file = new File(str);
		try {
			equalities = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if(!line.equals(""))
				equalities.add(line);
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public String getName() {
		return new Path(file.getAbsolutePath()).removeFileExtension().lastSegment();
	}

}
