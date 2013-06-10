package clusterPicking;

import java.util.*;

/**
 * class to represent a tree node
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
 * @version 22 Sept 2011
 * @version 12 Dec 2011
 */
public class Node implements Comparable<Node> {
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	// class variables and methods
	
	// node counter for labelling internal nodes
	private static int ncount	= 1;
	
	// global tree string format setter, for use with toString() on each node
	private static boolean figTreeFormat = false;
	
	public static void initialiseNodeCount() {
		ncount = 1;
	}
	
	public static void setFigTreeFormat(boolean ff) {
		figTreeFormat = ff;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	// instance variable and methods
	
	String delim 		= ":";

	private String name 		= "";
	private double branchLength = 0;
	private double support 		= 0;
	private int	   totalDescends= 0;
	private Object trait;
	
	private Node   		parent;
	private List<Node> 	children;
	
	public Node() {
		
	}

	////////////////////////////////////////////////////////////////////////////
	// getters and setters
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getBranchLength() {
		return branchLength;
	}

	public void setBranchLength(double branchLength) {
		this.branchLength = branchLength;
	}

	public double getSupport() {
		return support;
	}

	public void setSupport(double support) {
		this.support = support;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	public void addChild(Node child) {
		if (children == null) {
			children = new ArrayList<Node>();
		}
		this.children.add(child);
	}
	
	public void setTrait(Object trait) {
		this.trait = trait;
	}
	
	public Object getTrait() {
		return (this.trait);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	// special setters and getters
	
	public void setNameBranchLength(String txt) {
		String[] els 		= txt.split(delim);
		this.name 			= els[0];
		
		try {
			this.branchLength 	= Double.parseDouble(els[1]);
			//this.support		= 0;
		} catch (NumberFormatException e) {
			System.out.println("Node:setNameBranchLength - Sorry cant set "+txt);
			System.out.println(e.toString());
		}
	}
	
	public void setSupportBranchLength(String txt) {
		String[] els 		= txt.split(delim);
		
		
		try {
			
			if (this.name.equals("")) {
				this.name			= "Node"+ncount;
				ncount++;
			}
			
			this.support 		= Double.parseDouble( els[0] );
			this.branchLength 	= Double.parseDouble( els[1] );
		} catch (NumberFormatException e) {
			System.out.println("Node:setSupportBranchLength - Sorry cant set "+txt);
			System.out.println(e.toString());
		}
	}
	
	public List<Node> getAllMyChildren() {

		List<Node> descends = new ArrayList<Node>();
		descends.add(this);
		
		if ( !isTip() ) {
			for (Node c : children) {
				descends.addAll(c.getAllMyChildren());
			}
		}
		
		/*
		List<Node> toProcess= new ArrayList<Node>();
		toProcess.add(this);
		
		while (toProcess.size() > 0) {
			Node child = toProcess.remove(0);
			descends.add(child);
			if ( !child.isTip() ) {
				toProcess.addAll(child.getChildren());
			}
		}
		*/
		
		return descends;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// parent / child info
	
	/**
	 * returns true if input Node n is in this children list
	 */
	public boolean hasChild(Node n) {
		if ( children != null) {
			return ( children.contains(n) );
		} else {
			return false;
		}
	}
	
	/**
	 * returns true if the input Node n has this as its child
	 * should be the same as hasParent (but is not forced to be in this class)
	 * @param n
	 * @return
	 */
	public boolean isChildOf(Node n) {
		return ( hasChild(this) );
	}
	
	/**
	 * returns true if this parent is equal to input Node n
	 * should be the same as isChildOf (but is not forced to be in this class)
	 * @param n
	 * @return
	 */
	public boolean hasParent(Node n) {
		if ( parent != null ) {
			return ( parent.equals(n) );
		} else {
			return false;
		}
	}

	/**
	 * returns true if no children
	 * @return
	 */
	public boolean isTip() {
		if (children == null) {
			return true;
		} else if ( children.size() == 0 ) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * returns true if no parent
	 * @return
	 */
	public boolean isRoot() {
		return ( parent == null );
	}
	
	/**
	 * calculates the number of progeny recursively, stores the result in totalDescends and returns
	 * @return
	 */
	public int calcNumProgeny() {
		if (isTip()) {
			totalDescends = 1;	//0;
		} else {
			totalDescends = 0;
			for (Node c : children) {
				//totalDescends = totalDescends + 1 + c.calcNumProgeny();
				totalDescends = totalDescends + c.calcNumProgeny();
			}
		}
		return totalDescends;
	}
	
	/**
	 * returns already calculated totalDescends
	 * @return
	 */
	public int getNumberProgeny() {
		return totalDescends;
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	// Comparison methods
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Node)) {
			return false;
		}
		Node other = (Node) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int compareTo(Node b) {
		
		if ( hasChild(b) ) {
			return 1;
		} else if ( hasParent(b) ) {
			return -1;
		} else {
			return 0;
		}
		
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// display methods
	
	private String supportTxt() {
		if (figTreeFormat) {
			return ( "[&support="+this.support+"]" );
		} else {
			return ( ""+this.support );
		}
	}
	
	
	public String toString() {
		String txt = "";
		if (isTip()) {
			txt = this.name + delim + this.branchLength;
		} else {
			
			txt = "(";
			for (Node c : children) {
				txt = txt + c.toString() + ",";
			}
			txt = txt.substring(0, txt.length()-1);
			txt = txt + ")";
			
			// //txt = txt + this.support + delim + this.branchLength;
			txt = txt + supportTxt() + delim + this.branchLength;
			
			// experimental - not necessary
			//txt = txt + supportTxt() + delim + this.branchLength + "\n";
		}
		
		return txt;
	}
	
	public String info() {
		StringBuffer txt = new StringBuffer();
		txt.append("Name\t\t="+this.name);
		txt.append("\nBranchLength\t="+this.branchLength);
		txt.append("\nSuppport\t="+this.support);
		
		if (parent != null) {
			txt.append("\nParent\t\t="+this.parent.getName());
		} else {
			txt.append("\nParent\t\t=(none) this is the root");
		}
		
		if ( (children != null) && (children.size() > 0) ) {
			for (Node n : children) {
				txt.append("\nChild\t\t="+n.getName());
			}
		} else {
			txt.append("\nChild\t\t=(none) this is a tip");
		}
		txt.append("\n");
		
		return txt.toString();
	}
	
}
