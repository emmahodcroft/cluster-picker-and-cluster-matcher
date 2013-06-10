package clusterPicking;

import java.io.*;
import java.util.*;

/**
 * class to read a newick tree from a file and return a tree object
 * 
 * Copyright (C) 2013  Samantha Lycett
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Samantha Lycett
 * @created 21 Sept 2011
 * @version 21 Sept 2011
 * @version 3  Oct  2011 - modified readNextTree and readTree to keep reading in lines from file until ;
 */
public class TreeReader {

	String 	fileName;
	Scanner inFile;
	boolean open = false;
	
	public TreeReader() {
		
	}
	
	public TreeReader(String f) {
		this.fileName = f;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	
	public void setFileName(String f) {
		this.fileName = f;
	}
	
	public void openFile() {
		try {
			inFile = new Scanner( new File(fileName) );
			open   = true;
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	public void closeFile() {
		inFile.close();
		open = false;
	}
	
	public boolean hasNext() {
		return inFile.hasNext();
	}
	
	/**
	 * reads a newick tree line from the file, does not assume that the tree is on one line
	 * @return
	 */
	public Tree readNextTree() {
		
		if ( (open) && (inFile.hasNext()) ) {
			String trLine = inFile.nextLine();
			
			while (inFile.hasNext() && !trLine.endsWith(";")) {
				trLine = trLine + inFile.nextLine();
			}
			
			// remove final ;
			trLine = trLine.replace(";", "");
			
			Tree tr		  = new Tree();
			tr.readTree(trLine);
			return tr;
			
		} else {
			
			return null;
		}
		
	}
	
	/**
	 * reads a single newick tree line from a file, and closes the file.
	 * @param s
	 * @return
	 */
	public Tree readTree(String s) {
		this.fileName = s;
		
		Tree tr = null;
		
		try {
			inFile = new Scanner( new File(fileName) );
			String trLine = inFile.nextLine();
			
			while (inFile.hasNext() && !trLine.endsWith(";")) {
				trLine = trLine + inFile.nextLine();
			}
			
			// remove final ;
			trLine 	= trLine.replace(";", "");
			
			//System.out.println(trLine);
			
			tr		= new Tree();
			tr.readTree(trLine);
			
			inFile.close();
			
			return tr;
			
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		
		return ( tr );
		
	}
	
}
