package clusterPicking;

import java.io.*;
import java.util.*;

/**
 *  class to write tree in fig tree format, optional to include colours for taxa
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
 *  
 * @author Samantha Lycett
 * @created 26 Sept 2011
 * @version 3  Oct  2011  - changed order of adding the ' around taxa with . in their names in writeHeader so that colours work with BEAST names
 * @version 27 March 2012 - modified to use rainbow_256_cols class rather than separate file
 */
public class FigTreeWriter {

	private String 			filename;
	private BufferedWriter 	outFile;
	private boolean 		open 		= false;
	private boolean			headerDone 	= false;
	private boolean			footerDone  = false;
	private Tree 			theTree;

	private int				treeNumber  = 0;
	private Hashtable<String,String> taxaColours = new Hashtable<String,String>();
	
	private List<String>	colourTable = new ArrayList<String>();
	
	public FigTreeWriter() {
		loadRainbowColours();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	
	public void setFilename(String fn) {
		this.filename = fn;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	
	public void loadColourTable(String filename) {
		try {
			colourTable 	= new ArrayList<String>();
			Scanner inFile 	= new Scanner(new File(filename));
			while (inFile.hasNext()) {
				String line = inFile.nextLine();
				line = line.substring(0, line.length()-2);
				colourTable.add(line);
			}
			inFile.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	public void loadRainbowColours() {
		//loadColourTable("rainbow_256_cols.txt");
		colourTable = rainbow_256_cols.getRainbowColours();
	}
	
	public void setColour(String taxaName, int col) {
		int colIndex 		= col % colourTable.size();
		String colourTxt 	= "[&!color="+colourTable.get(colIndex)+"]";
		if (taxaColours.containsKey(taxaName)) {
			taxaColours.remove(taxaName);
		}
		taxaColours.put(taxaName, colourTxt);
	}
	
	public void setColour(String taxaName, int red, int green, int blue) {
		
		String RR = Integer.toHexString(red);
		while (RR.length() < 2) {
			RR = "0"+RR;
		}
		
		String GG = Integer.toHexString(green);
		while (GG.length() < 2) {
			GG = "0"+GG;
		}
		
		String BB = Integer.toHexString(blue);
		while (BB.length() < 2) {
			BB = "0"+BB;
		}
		
		//int colNum		 = (256*256*(255-red))+(256*(255-green))+(255-blue);
		String colNum 	 = RR + GG + BB;
		String colourTxt = "[&!color=#"+colNum+"]";
		
		if (taxaColours.containsKey(taxaName)) {
			taxaColours.remove(taxaName);
		}
		taxaColours.put(taxaName, colourTxt);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * opens the file with this input filename
	 */
	public void openFile(String fn) {
		this.filename = fn;
		openFile();
	}
	
	/**
	 * opens the file
	 */
	public void openFile() {
		try {
			outFile 	= new BufferedWriter(new FileWriter(filename));
			open    	= true;
			headerDone 	= false;
			footerDone  = false;
			treeNumber  = 0;
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	
	/**
	 * writes the footer if not already done and closes the file
	 */
	public void closeFile() {
		if (open) {
			try {
				
				if (!footerDone) {
					writeFooter();
				}
				
				open 		= false;
				headerDone 	= false;
				outFile.close();
				
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
	}
	
	private void writeHeader() {
		if (open && !headerDone) {
			try {
				List<String> tipList = theTree.tipNames();
				int ntax 			 = tipList.size();
				
				outFile.write("#NEXUS\r");
				outFile.write("begin taxa;\r");
				outFile.write("dimensions ntax="+ntax+";\r");
				outFile.write("\ttaxlabels\r");
				
				for (String taxa : tipList) {
					String colTxt = "";
					if (taxaColours.containsKey(taxa)) {
						colTxt = taxaColours.get(taxa);
					}
					
					if (taxa.contains(".")) {
						taxa = "'"+taxa+"'";
					}
					//String line = "\t"+taxa;
					//if (taxaColours.containsKey(taxa)) {
					//	line = line + taxaColours.get(taxa);
					//}
					//line = line + "\r";
					
					String line = "\t"+taxa+colTxt+"\r";
					
					outFile.write(line);
				}
				
				outFile.write("\t;\r");
				outFile.write("end;\r");
				outFile.write("begin trees;\r");
				
				headerDone = true;
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
	}
	
	private void writeFooter() {
		
		if ( (open) && !footerDone) {
			try {
				
				outFile.write("end;\r");
				outFile.write("begin figtree;\r");
				outFile.write("\tset appearance.backgroundColorAttribute=\"User Selection\";\r");
				outFile.write("\tset appearance.backgroundColour=#-1;\r");
				outFile.write("\tset appearance.branchColorAttribute=\"User Selection\";\r");
				outFile.write("\tset appearance.branchLineWidth=1.0;\r");
				outFile.write("\tset appearance.foregroundColour=#-16777216;\r");
				outFile.write("\tset appearance.selectionColour=#-2144520576;\r");
				outFile.write("\tset branchLabels.colorAttribute=\"User Selection\";\r");
				outFile.write("\tset branchLabels.displayAttribute=\"Branch times\";\r");
				outFile.write("\tset branchLabels.fontName=\"sansserif\";\r");
				outFile.write("\tset branchLabels.fontSize=8;\r");
				outFile.write("\tset branchLabels.fontStyle=0;\r");
				outFile.write("\tset branchLabels.isShown=false;\r");
				outFile.write("\tset branchLabels.significantDigits=4;\r");
				outFile.write("\tset layout.expansion=0;\r");
				outFile.write("\tset layout.layoutType=\"RECTILINEAR\";\r");
				outFile.write("\tset layout.zoom=0;\r");
				outFile.write("\tset nodeBars.barWidth=4.0;\r");
				outFile.write("\tset nodeLabels.colorAttribute=\"User Selection\";\r");
				outFile.write("\tset nodeLabels.displayAttribute=\"support\";\r");
				outFile.write("\tset nodeLabels.fontName=\"Arial\";\r");
				outFile.write("\tset nodeLabels.fontSize=10;\r");
				outFile.write("\tset nodeLabels.fontStyle=0;\r");
				outFile.write("\tset nodeLabels.isShown=true;\r");
				outFile.write("\tset nodeLabels.significantDigits=3;\r");
				outFile.write("\tset polarLayout.alignTipLabels=false;\r");
				outFile.write("\tset polarLayout.angularRange=0;\r");
				outFile.write("\tset polarLayout.rootAngle=0;\r");
				outFile.write("\tset polarLayout.rootLength=100;\r");
				outFile.write("\tset polarLayout.showRoot=true;\r");
				outFile.write("\tset radialLayout.spread=0.0;\r");
				outFile.write("\tset rectilinearLayout.alignTipLabels=false;\r");
				outFile.write("\tset rectilinearLayout.curvature=0;\r");
				outFile.write("\tset rectilinearLayout.rootLength=100;\r");
				outFile.write("\tset scale.offsetAge=0.0;\r");
				outFile.write("\tset scale.rootAge=1.0;\r");
				outFile.write("\tset scale.scaleFactor=1.0;\r");
				outFile.write("\tset scale.scaleRoot=false;\r");
				outFile.write("\tset scaleAxis.automaticScale=true;\r");
				outFile.write("\tset scaleAxis.fontSize=8.0;\r");
				outFile.write("\tset scaleAxis.isShown=false;\r");
				outFile.write("\tset scaleAxis.lineWidth=1.0;\r");
				outFile.write("\tset scaleAxis.majorTicks=1.0;\r");
				outFile.write("\tset scaleAxis.origin=0.0;\r");
				outFile.write("\tset scaleAxis.reverseAxis=false;\r");
				outFile.write("\tset scaleAxis.showGrid=true;\r");
				outFile.write("\tset scaleAxis.significantDigits=4;\r");
				outFile.write("\tset scaleBar.automaticScale=true;\r");
				outFile.write("\tset scaleBar.fontSize=10.0;\r");
				outFile.write("\tset scaleBar.isShown=true;\r");
				outFile.write("\tset scaleBar.lineWidth=1.0;\r");
				outFile.write("\tset scaleBar.scaleRange=0.0;\r");
				outFile.write("\tset scaleBar.significantDigits=4;\r");
				outFile.write("\tset tipLabels.colorAttribute=\"User Selection\";\r");
				outFile.write("\tset tipLabels.displayAttribute=\"Names\";\r");
				outFile.write("\tset tipLabels.fontName=\"Arial\";\r");
				outFile.write("\tset tipLabels.fontSize=10;\r");
				outFile.write("\tset tipLabels.fontStyle=0;\r");
				outFile.write("\tset tipLabels.isShown=true;\r");
				outFile.write("\tset tipLabels.significantDigits=4;\r");
				outFile.write("\tset trees.order=true;\r");
				outFile.write("\tset trees.orderType=\"increasing\";\r");
				outFile.write("\tset trees.rooting=false;\r");
				outFile.write("\tset trees.rootingType=\"User Selection\";\r");
				outFile.write("\tset trees.transform=false;\r");
				outFile.write("\tset trees.transformType=\"cladogram\";\r");
				outFile.write("end;\r");
				
				footerDone = true;

			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
	}
	
	/**
	 * appends the stored tree to an already open file
	 */
	public void writeTree() {
		// set fig tree format for all nodes (global)
		Node.setFigTreeFormat(true);
		writeHeader();

		try {
			treeNumber++;
			outFile.write("\ttree tree_"+treeNumber+" = [&R] "+theTree.toString()+";\r");
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	/**
	 * appends the input tree to an alreay open file - note that the number of tips and taxa is not checked
	 * @param t
	 */
	public void writeTree(Tree t) {
		this.theTree = t;
		writeTree();
	}
	
	/**
	 * opens a file, writes the tree, closes the file
	 * @param t
	 * @param filename
	 */
	public void writeTree(Tree t, String filename) {
		this.filename = filename;
		this.theTree  = t;
		openFile();
		writeTree();
		closeFile();
	}

	
}
