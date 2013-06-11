package clusterPicking;

import java.io.*;
import java.util.List;

/**
 *  class to write newick tree strings to file
 *  Copyright (C) 2013  Samantha Lycett
 *  
 *  This file is part of ClusterPicker.
 *
 *  ClusterPicker is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  ClusterPicker is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ClusterPicker.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * 
 * @author Samantha Lycett
 * @created 22 Sept 2011
 * @version 22 Sept 2011
 */
public class TreeWriter {

	private String filename;
	private BufferedWriter outFile;
	private boolean open = false;
	
	public TreeWriter() {
		Node.setFigTreeFormat(false);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	
	public void setFilename(String fn) {
		this.filename = fn;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	
	public void openFile(String fn) {
		this.filename = fn;
		openFile();
	}
	
	public void openFile() {
		try {
			outFile = new BufferedWriter(new FileWriter(filename));
			open    = true;
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	/**
	 * appends the newick string of Tree t to file - note the file must be already open
	 * allows multiple tree lines to be written to file
	 * @param t
	 */
	public void append(Tree t) {
		if (open) {
			try {
				outFile.write(t.toString()+";");
				outFile.newLine();
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		} else {
			System.out.println("TreeWriter:append WARNING: nothing has been written because the file is not open for appending");
		}
	}
	
	public void closeFile() {
		if (open) {
			try {
				outFile.close();
				open = false;
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * writes the newick string from input Tree t to filename and closes the file.
	 * use this method to write a single tree to a single file
	 * @param t
	 * @param filename
	 */
	public void write(Tree t, String filename) {
		try {
			BufferedWriter outFile = new BufferedWriter(new FileWriter(filename));
			outFile.write(t.toString()+";");
			outFile.newLine();
			outFile.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	/**
	 * writes the set of newick strings from the input Tree list to filename, and closes the file
	 * @param ts
	 * @param filename
	 */
	public void write(List<Tree> ts, String filename) {
		this.filename=filename;
		openFile();
		for (Tree t : ts) {
			append(t);
		}
		closeFile();
	}
	
}
