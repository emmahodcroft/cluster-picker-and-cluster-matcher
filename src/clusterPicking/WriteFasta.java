package clusterPicking;

import java.io.*;
import java.util.*;

/**
 * class to write fasta format files
 * 
 *  Copyright (C) 2013  Samantha Lycett
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
 * @created 23 Sept 2011
 * @version 23 Sept 2011
 */
public class WriteFasta {

	private String fileName;
	private BufferedWriter outFile;
	private boolean open 			= false;
	private String fastaStart		= ">";
	
	public WriteFasta() {
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	
	public void openFile() {
		try {
			outFile = new BufferedWriter(new FileWriter(fileName));
			open = true;
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	public void openFile(String fileName) {
		this.fileName = fileName;
		openFile();
	}
	
	public void closeFile() {
		if ( open ) {
			try {
				open = false;
				outFile.close();
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
	}
	
	
	public void append(String sequenceName, String sequence) {
		if (open) {
			try {
				
				if (sequenceName.startsWith(fastaStart)) {
					outFile.write(sequenceName);
				} else {
					outFile.write(fastaStart+sequenceName);
				}
				outFile.newLine();
				outFile.write(sequence);
				outFile.newLine();
				
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		} else {
			System.out.println("WriteFasta:append WARNING - file is not open, nothing was written");
		}
	}
	
	public void append(BasicSequence s) {
		if (open) {
			try {
				String sequenceName = fastaStart + s.header();
				outFile.write(sequenceName);
				outFile.newLine();
				
				String sequence     = new String( s.charSeq() );
				outFile.write(sequence);
				outFile.newLine();
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		} else {
			System.out.println("WriteFasta:append WARNING - file is not open, nothing was written");
		}
	}
	
	public void append(List<BasicSequence> seqs) {
		if (open) {
			try {
				for (BasicSequence s : seqs) {
					String sequenceName = fastaStart + s.header();
					outFile.write(sequenceName);
					outFile.newLine();
				
					String sequence     = new String( s.charSeq() );
					outFile.write(sequence);
					outFile.newLine();
				}
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		} else {
			System.out.println("WriteFasta:append WARNING - file is not open, nothing was written");
		}
	}
	
}
