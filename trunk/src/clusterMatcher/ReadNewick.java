package clusterMatcher;

/** Copyright 2011-2013 Emma Hodcroft
 * This file is part of ClusterMatcher. (Also may be referred to as
 * "ClustMatcher" or "ClustMatch".)
 *
 * ClusterMatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * ClusterMatcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClusterMatcher.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.*;
import java.util.*;

/**
 * The outline for this class is based upon Samantha Lycett's class 'ReadFasFile'
 * @author Emma
 */
public class ReadNewick {

	private String 	filename;
	private boolean open 		= false;
	private Scanner	dataFile;

	private String	line		= "x";

	////////////////////////////////////////////////////////////////
	// constructors

	public ReadNewick() {
	}

	public ReadNewick(String filename) {
		this.filename 	= filename;
	}

	///////////////////////////////////////////////////////////////
	// methods

	public void setFilename(String filename) {
		this.filename 	= filename;
	}

	public void openFile() throws FileNotFoundException
        {
		File inputFile 	= new File(filename);
		try {
			// set up scanner to read one line at a time
			dataFile	= new Scanner(inputFile).useDelimiter("\n");
			open		= true;
		} catch (FileNotFoundException e) {
                        String error ="";
                        if(filename==null || filename.isEmpty())
                            error = "No file name specified!";
                        else
                            error = "Sorry file " + filename + " not found";
			System.out.println(error);
			open		= false;
                        throw new FileNotFoundException(error);
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
	 * next - returns the next newick tree record in the file
	 */
	public String	next() {

		if ( open ) {

			if(dataFile.hasNext())
				line = dataFile.next().trim();

		} else {
			System.out.println("File "+filename+" not open");
		}

		return line;

	}

}
