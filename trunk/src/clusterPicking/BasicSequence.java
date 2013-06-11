package clusterPicking;

/**
 *  class to represent a basic sequence
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
 * @author slycett
 * @created 20 July 09
 * @version 20 July 09
 * @version 20 July 2011 - yes really it is exactly two years later !
 * @version 22 July 2011
 * @version 26 Sept 2011
 * @version 3  Oct  2011 - allow differences to be measured as: absolute (abs), ignoring gaps only (gap) or valid only (a,c,t,g)
 * @vesrion 4  July 2012 - added String, char[] constructor
 */
public class BasicSequence {
	
	// class variable
	// protected static boolean useAbs = false;
	protected static String  differenceType = "gap";		// choices are abs, gap, valid
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// instance variables
	protected String header;
	protected char[] charSeq;
	protected int	 internalID;
	
	public BasicSequence() {
		
	}
	
	public BasicSequence(char[] seq) {
		this.charSeq = seq;
	}
	
	public BasicSequence(String header, char[] seq) {
		this.header  = header;
		this.charSeq = seq;
	}
	
	public BasicSequence(String header, String seq) {
		this.header  = header;
		this.charSeq = seq.toLowerCase().toCharArray();
	}
	
	/**
	 * basicSequence constructor, using output of ReadFasta
	 * @param lines
	 */
	public BasicSequence(String[] lines) { 
		if (lines.length == 2) {
			this.header = lines[0].substring(1, lines[0].length());
			this.charSeq= lines[1].toLowerCase().toCharArray();
		} else {
			System.out.println("** WARNING Cannot create BasicSequence from input");
		}
	}
	
	public BasicSequence(String[] lines, int index) {
		this.internalID = index;
		
		if (lines.length == 2) {
			this.header = lines[0].substring(1, lines[0].length());
			this.charSeq= lines[1].toLowerCase().toCharArray();
		} else {
			System.out.println("** WARNING Cannot create BasicSequence from input");
		}
	}
	
	////////////////////////////////////////////////////////////////////////
	// set methods
	
	public void setHeader(String header) {
		this.header = header;
	}
	
	public void setSequence(String seq) {
		this.charSeq = seq.toLowerCase().toCharArray();
	}
	
	public void setSequence(char[] seq) {
		this.charSeq = seq;
	}
	
	public void setIndex(int index) {
		this.internalID = index;
	}
	
	///////////////////////////////////////////////////////////////////////
	// get methods
	
	public String header() {
		return header;
	}
	
	public char[] charSeq() {
		return charSeq;
	}
	
	public int index() {
		return internalID;
	}
	
	
	/**
	 * returns true if site i contains a, c, g, or t.
	 * @param i
	 * @return
	 */
	public boolean isValid(int i) {
		char site = charSeq[i];
		boolean v = ( ( site =='a' ) || ( site == 'c' ) || ( site == 'g') || ( site == 't') );
		return v;
	}
	
	/** 
	 * returns true if site i contains a gap character
	 * @param i
	 * @return
	 */
	public boolean isGap(int i) {
		char site = charSeq[i];
		boolean g = ( ( site == '-') || ( site == '~') );
		return g;
	}
	
	///////////////////////////////////////////////////////////////////////
	// comparison methods
	
	/**
	 * calculates the raw character difference between the sequences, does not check for gaps etc
	 * @param b
	 * @return
	 */
	public int absDifference(BasicSequence b) {
		int count = 0;
		int len   = charSeq.length;
		
		if (b.charSeq.length < len) {
			len	  = b.charSeq.length;
		}
		
		for (int i = 0; i < len; i++) {
			if (charSeq[i] != b.charSeq[i]) {
				count++;
			}
		}
		
		return count;
	}
	
	/**
	 * calculates the difference between this sequence and the input sequence, but only counts valid sites, i.e. a, c, t, g
	 * @param b
	 * @return
	 */
	public int validDifference(BasicSequence b) {
		int count = 0;
		int len   = charSeq.length;
		
		if (b.charSeq.length < len) {
			len	  = b.charSeq.length;
		}
		
		for (int i = 0; i < len; i++) {
			if (charSeq[i] != b.charSeq[i]) {
				if ( isValid(i) && b.isValid(i) ) {
				//if (!isGap(i) && !b.isGap(i)) {
					count++;
				}
			}
		}
		
		return count;
	}
	
	/**
	 * calculates the difference between this sequence and the input sequence, but only counts if neither is a gap
	 * @param b
	 * @return
	 */
	public int noGapDifference(BasicSequence b) {
		int count = 0;
		int len   = charSeq.length;
		
		if (b.charSeq.length < len) {
			len	  = b.charSeq.length;
		}
		
		for (int i = 0; i < len; i++) {
			if (charSeq[i] != b.charSeq[i]) {
				//if ( isValid(i) && b.isValid(i) ) {
				if (!isGap(i) && !b.isGap(i)) {
					count++;
				}
			}
		}
		
		return count;
	}
	
	public int difference(BasicSequence b) {
		if (differenceType.equals("abs")) {
			return absDifference(b);
		} else if (differenceType.equals("gap")) {
			return noGapDifference(b);
		} else if (differenceType.equals("valid")) {
			return validDifference(b);
		} else {
			System.out.println("BasicSequence:difference\tcant understand "+differenceType);
			return -1;
		}
	}
	
	/**
	 * calculates the fraction difference between this sequence and the input sequence, but only counts valid sites, i.e. a, c, t, g and divides by the length of the shortest
	 * @param b
	 * @return
	 */
	public double validFractDifference(BasicSequence b) {
		int count = 0;
		int len   = charSeq.length;
		
		if (b.charSeq.length < len) {
			len	  = b.charSeq.length;
		}
		
		for (int i = 0; i < len; i++) {
			if (charSeq[i] != b.charSeq[i]) {
				if ( isValid(i) && b.isValid(i) ) {
					count++;
				}
			}
		}
		
		return ( (double)count / (double)len ) ;
	}
	
	/**
	 * calculates the fraction difference between this sequence and the input sequence, but only counts where the sites are not gaps
	 * @param b
	 * @return
	 */
	public double noGapFractDifference(BasicSequence b) {
		int count = 0;
		int len   = charSeq.length;
		
		if (b.charSeq.length < len) {
			len	  = b.charSeq.length;
		}
		
		for (int i = 0; i < len; i++) {
			if (charSeq[i] != b.charSeq[i]) {
				if ( !isGap(i) && !b.isGap(i) ) {
					count++;
				}
			}
		}
		
		return ( (double)count / (double)len ) ;
	}
	
	/**
	 * calculates the fraction difference between this sequence and the input sequence, and divides by the length of the shortest
	 * @param b
	 * @return
	 */
	public double absFractDifference(BasicSequence b) {
		int count = 0;
		int len   = charSeq.length;
		
		if (b.charSeq.length < len) {
			len	  = b.charSeq.length;
		}
		
		for (int i = 0; i < len; i++) {
			if (charSeq[i] != b.charSeq[i]) {
					count++;
			}
		}
		
		return ( (double)count / (double)len ) ;
	}
	
	public double fractDifference(BasicSequence b) {
		if (differenceType.equals("abs")) {
			return absFractDifference(b);
		} else if (differenceType.equals("gap")){
			return noGapFractDifference(b);
		} else if (differenceType.equals("valid")) {
			return validFractDifference(b);
		} else {
			System.out.println("BasicSequence:fractDifference\tcant understand "+differenceType);
			return -1;
		}
	}
	
	/**
	 * returns true if the sequence names match - not the sequence contents !
	 */
	public boolean equals(Object o) {
		
		if (o instanceof BasicSequence) {
			BasicSequence b = (BasicSequence)o;
			return (difference(b) == 0);
		} else if (o instanceof String) {
			String bHeader  = (String)o;
			return this.header.equals(bHeader);
		} else {
			return false;
		}
		
	}
	
}
