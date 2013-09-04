package clusterPicking;

import java.io.*;
import java.util.*;
/**
 *  ReadFasta - class to read fasta format records from a file
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
 * @author 	Sam Lycett
 * @created	19 Sept 07
 * @version	19 Sept 07
 * @version 26 Sept 07
 * @version 4  Sept 2013 - using nextLine rather than specifying delimiter on Scanner
 *
 */
public class ReadFasta {

	private String 		filename;
	private boolean 	open 		= false;
	private Scanner		dataFile;
	
	private String		recordStart = ">";
	private String		line		= "x";
	
	////////////////////////////////////////////////////////////////
	// constructors
	
	public ReadFasta() {
	}
	
	public ReadFasta(String filename) {
		this.filename 	= filename;
	}
	
	///////////////////////////////////////////////////////////////
	// methods
	
	public String recordStart() {
		return recordStart;
	}
	
	public void setFilename(String filename) {
		this.filename 	= filename;
	}
	
	public void openFile() {
		File inputFile 	= new File(filename);
		try {
			// set up scanner to read one line at a time
			//dataFile	= new Scanner(inputFile).useDelimiter("\n");
			dataFile	= new Scanner(inputFile);						// 4 Sept 2013
			open		= true;
		} catch (FileNotFoundException e) {
			System.out.println("Sorry file "+filename+" not found");
			open		= false;
		}
	}
	
	public void closeFile() {
		dataFile.close();
		open = false;
	}
	
	public boolean hasNext() {
		if ( open ) {
			return dataFile.hasNext();
		} else {
			return false;
		}
	}
	
	/**
	 * next - returns the next fasta record in the file
	 */
	public String[]	next() {
		
		String[] lines			= new String[2];
		
		if ( open ) {
		
			// while not at start line
			while ( ( !line.startsWith(recordStart) ) && (dataFile.hasNext()) ) {
				//line = dataFile.next().trim();
				line = dataFile.nextLine().trim();			// 4 Sept 2013
			}
			
			// add the header line
			lines[0] = line;
			
			line			= "x";
			String sequence = "";
			// while not at a start line
			while ( ( !line.startsWith(recordStart) ) && (dataFile.hasNext()) ) {
				//line 		= dataFile.next().trim();
				line		= dataFile.nextLine().trim();	// 4 Sept 2013
				
				if ( !line.startsWith(recordStart) ) {
					sequence 	= sequence + line;
				}
			}
			
			// add the sequence data
			lines[1] = sequence;
			
		} else {
			System.out.println("File "+filename+" not open");
		}
		
		return lines;
		
	}
	
}
