package clusterPicking;

import java.util.*;
import java.util.regex.*;

/**
 *  class to represent a newick tree with support values and branch lengths
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
 * @created 21 Sept 2011
 * @version 21 Sept 2011
 * @version 22 Sept 2011 - extracting subtrees with support
 * @version 27 Sept 2011 - accepts | and / in tip names also
 * @version 3  Oct  2011 - accepts - in tip names also
 * @version 8  Nov  2011 - changed support regex
 * @version 15 Sept	2015 - Now errors if polytomy detected. Can handle missing branch length and bootstrap info. Can handle different types of rooting. Message update works now when processing, and spinning bar is displayed. 'GO' button disabled while processing and if error.
 */
public class Tree {

	List<Node> 	nodes;
	String 		trLine;
	
	String  branchLengthRegex 	= "(\\-)?+[0-9]+(\\.[0-9eE\\-]+)?";   //(-)0-9+ (.0-9eE-0-9)
String bootstrapRegex = "((\\-)?+[0-9]+(\\.[0-9eE\\-]+)?)?"; //ebh 
	String 	branchDelim			= ":";
	//String  nodeRegex   	  	= "[a-zA-Z_0-9\\.]+" +   branchDelim + branchLengthRegex;
	String  nodeRegex   	  	= "[a-zA-Z_0-9\\.\\|/\\-]+" +   branchDelim + branchLengthRegex;
	//String  supportRegex		= "[0-9]+\\.[0-9]+" + branchDelim + branchLengthRegex;
//	String  supportRegex		= branchLengthRegex + branchDelim + branchLengthRegex; //ebh
String supportRegex = bootstrapRegex + branchDelim + branchLengthRegex; //ebh
	Pattern nodePattern 	  	= Pattern.compile( nodeRegex  );
	//Pattern supportPattern	= Pattern.compile( supportRegex );
	//Pattern sibPattern  	  	= Pattern.compile( "\\("+nodeRegex+","+nodeRegex+"\\)");
	Pattern sibSupportPattern	= Pattern.compile( "\\(" + nodeRegex + "," + nodeRegex + "\\)" + supportRegex );
	Pattern triPattern			= Pattern.compile( "\\(" + nodeRegex + "," + nodeRegex + "," + nodeRegex + "\\)");
	Pattern polyPattern 		= Pattern.compile( "\\(" + "("+nodeRegex+",){2,}" + nodeRegex + "\\)");  //{n,} -match at least n times
Pattern biPattern = Pattern.compile( "\\(" + nodeRegex + "," + nodeRegex + "\\)"); //ebh
	
	
	public Tree() {
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private String processTree(String tempTr) {
		
		Matcher elMatcher = sibSupportPattern.matcher(tempTr);
		
		while (elMatcher.find()) {
//System.out.println("\t "+tempTr); //ebh
			String txt	  	= elMatcher.group(); //matches sibsupport pattern, returns 1st match
		
			String txt2 	= txt.replace("(", "");
			txt2 			= txt2.replace(")", ",");
			String[] els 	= txt2.split(",");
		
			// the last element is support:branchLength
			// this is the parent
			Node parent = new Node();
			parent.setSupportBranchLength( els[els.length-1] );
			
			Node[] childs = new Node[2];//ebh - to hold br lengths of children to check if identical seqs
			
			// the first two elements are nodeName:branchLength
			// these are children
			for (int i = 0; i < (els.length-1); i++) {
				Node n = new Node();
				n.setNameBranchLength( els[i] );
				
				if (nodes.contains(n)) {
					// nodes list already has this element, it will have name, support and branchlength
					int j 	= nodes.indexOf(n);
					Node n2 = nodes.get(j);
					
					// add parent to this node
					n2.setParent(parent);
					
					// add child to parent
					parent.addChild(n2);
				} else {
					n.setParent(parent);
					parent.addChild(n);
					
					nodes.add(n);
				}
				childs[i] = n;
			}
			
			String[] parSupp = els[els.length-1].split(":");
			if(parSupp[0].length() == 0){   //if no bootstrap value, check lengths of children!
				if(childs[0].getBranchLength() == 0 && childs[1].getBranchLength() == 0){
					//if both child branch length is zero - set parent support to 100 - identical seqs
					parent.setSupport(100.0);
					System.out.println("**WARNING**: No support value and children have branch length of 0 - identical sequences likely. "
							+ "\n\t**Setting support to 100% to allow cluster picking of this pair.**");
				}
			}
			
			if ( !nodes.contains(parent) ) {
				nodes.add(parent);
			} else {
				System.out.println("Tree:readTree WARNING already have "+parent.getName()+" in nodes list");
			}
		
			// replace txt=(A:1,B:2)0.9:3 with parentName:branchLength
			String newTxt = parent.getName()+parent.delim+parent.getBranchLength();
			//txt2		  = txt.replace("(", "");
			//txt2		  = txt2.replace(")", "");
			//tempTr 		  = tempTr.replace(txt, txt2);
			//tempTr		  = tempTr.replace(txt2, newTxt);
			tempTr		  = tempTr.replace(txt, newTxt);
			
			//System.out.println(txt);
			//System.out.println(newTxt);
			//System.out.println("Temp tree = "+tempTr);
			
		}
				
		return ( tempTr );
	}
	
	/**
	 * use this to do the last tri fucation - and make up a root node
	 * @param tempTr
	 * @return
	 */
	private String processTrifucation(String tempTr) {
		Matcher elMatcher = triPattern.matcher(tempTr);
		
		if (elMatcher.find()) {
			
			Node rootNode		= new Node();
			rootNode.setName("Root");
			rootNode.setSupport(0);
			rootNode.setBranchLength(0);
			
			String txt 			= elMatcher.group();
			Matcher nodeMatcher = nodePattern.matcher(txt);
			
			while (nodeMatcher.find()) {
				String el = nodeMatcher.group();
				Node n = new Node();
				n.setNameBranchLength( el );
				
				if (nodes.contains(n)) {
					// nodes list already has this element, it will have name, support and branchlength
					int j 	= nodes.indexOf(n);
					Node n2 = nodes.get(j);
					
					// add parent to this node
					n2.setParent(rootNode);
					
					// add child to parent
					rootNode.addChild(n2);
				} else {
					n.setParent(rootNode);
					rootNode.addChild(n);
					
					nodes.add(n);
				}
				
			}
			
			nodes.add(rootNode);
			String newTxt = rootNode.getName()+rootNode.delim+rootNode.getBranchLength();
			tempTr		  = tempTr.replace(txt, newTxt);
		}
		
		return (tempTr);
		
	}
	
	//ebh all below function
	/**
	 * use this to do the last bi furcation - and make up a root node -- in cases where tree is re-rooted for example, and has one
	 * node at root without bootstrap/length for connection to rest of tree!
	 * @param tempTr
	 * @return
	 */
	private String processBifucation(String tempTr) {
		Matcher elMatcher = biPattern.matcher(tempTr);
		
		if (elMatcher.find()) {
			
			Node rootNode		= new Node();
			rootNode.setName("Root");
			rootNode.setSupport(0);
			rootNode.setBranchLength(0);
			
			String txt 			= elMatcher.group();
			Matcher nodeMatcher = nodePattern.matcher(txt);
			
			while (nodeMatcher.find()) {
				String el = nodeMatcher.group();
				Node n = new Node();
				n.setNameBranchLength( el );
				
				if (nodes.contains(n)) {
					// nodes list already has this element, it will have name, support and branchlength
					int j 	= nodes.indexOf(n);
					Node n2 = nodes.get(j);
					
					// add parent to this node
					n2.setParent(rootNode);
					
					// add child to parent
					rootNode.addChild(n2);
				} else {
					n.setParent(rootNode);
					rootNode.addChild(n);
					
					nodes.add(n);
				}
				
			}
			
			nodes.add(rootNode);
			String newTxt = rootNode.getName()+rootNode.delim+rootNode.getBranchLength();
			tempTr		  = tempTr.replace(txt, newTxt);
		}
		
		return (tempTr);
		
	}
	
	public void readTree(String trLine) {
		
		this.trLine 	  = trLine;
		
		nodes			  = new ArrayList<Node>();
		
		String tempTr	  = new String(trLine);
		int	   diffLength = tempTr.length();
		
		//System.out.println("Temp tree = "+tempTr);
		
		//check to see if it contains any polytomies:
		Matcher elMatcher = polyPattern.matcher(tempTr);
		
		if (elMatcher.find()) {
			System.out.println("Tip polytomy found: "+elMatcher.group());
			System.out.println("***ERROR*** TIP POLYTOMY FOUND!!!");
			throw new UnsupportedOperationException("ERROR: A tip polytomy was found (possibly due to identical sequences). The ClusterMatcher cannot handle tip or internal polytomies. Please remove the polytomy from your tree and re-run the ClusterPicker.\n...Exiting...");
		}
		
//System.out.println();  //ebh
		while (diffLength > 0) {
//System.out.println(tempTr); //ebh
		
			int		origLength	= tempTr.length();			
			tempTr 			  	= processTree(tempTr);
			diffLength 			= origLength - tempTr.length();

			//System.out.println("Diff = "+diffLength);
			//System.out.println("Temp tree = "+tempTr);
		}
		
		//check again it hasn't stopped early because of a polytomy
		//polytomies found here are internal - where one or more nodes in the polytomy
		//are internal nodes (not tips)
		elMatcher = polyPattern.matcher(tempTr);  //ensure has new tempTr
		if (elMatcher.find()) {  //if find polytomy
//			System.out.println("Internal polytomy found: "+elMatcher.group()); //this will show even if is root polytomy which is ok
			//polytomy only bad at this stage if not at root 
			//should only have 1 open parenthesis
			Matcher m = Pattern.compile("\\(").matcher(tempTr);

			int matches = 0;
			while(m.find())
			    matches++;
			
			//System.out.println("number of (: "+matches);
			if(matches > 1){
				System.out.println("***ERROR*** INTERNAL POLYTOMY FOUND!!!");
				throw new UnsupportedOperationException("ERROR: An internal polytomy was found. The ClusterMatcher cannot handle tip or internal polytomies. Please remove the polytomy from your tree and re-run the ClusterPicker.\n...Exiting...");
			}
		}
			
		tempTr = processBifucation(tempTr);
		// now do final part if necessary - the tree should be really short by now
		tempTr = processTrifucation(tempTr);
		//System.out.println("Temp tree = "+tempTr);
		
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// getters and setters
	
	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	
	public List<Node> getNodes() {
		return nodes;
	}
	
	public Node getNode(String nodeName) {
		Node n = new Node();
		n.setName(nodeName);
		int j = nodes.indexOf(n);
		return (nodes.get(j));
	}
	
	public Node getRoot() {
		Node rootNode = nodes.get(nodes.size()-1);
		for (Node n : nodes) {
			if (n.isRoot()) {
				rootNode = n;
			}
		}
		return rootNode;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * returns subtree subtended by Node a
	 * Note - I think this is where the problem with re-running for the GUI stems from
	 * should be fixable when go to jebl
	 */
	public Tree subTree(Node a) {
		int j 			= nodes.indexOf(a);
		Node newRoot 	= nodes.get(j);
		List<Node> subNodes = newRoot.getAllMyChildren();
		
		// the first one in the list of subNodes is the newRoot
		// but we need to remove its parent
		newRoot = subNodes.get(0);
		newRoot.setParent(null);
		
		Tree subTr = new Tree();
		subTr.setNodes(subNodes);
		
		return ( subTr );
	}
	
	public List<Tree> subTrees() {
		List<Node> toProcess 	= new ArrayList<Node>();
		List<Tree> subTrees 	= new ArrayList<Tree>();
		
		toProcess.add( getRoot() );
		
		while (toProcess.size() > 0) {
			
			Node n = toProcess.remove(0);
			
			if (!n.isTip()) {
				
				List<Node> subNodes = n.getAllMyChildren();
				
				// the first one in the list of subNodes is the newRoot
				// but we need to remove its parent
				n = subNodes.get(0);
				n.setParent(null);
				
				Tree subTr = new Tree();
				subTr.setNodes(subNodes);
				subTrees.add(subTr);
				
				toProcess.addAll(subNodes);
				
			}
			
		}

		return ( subTrees );
	}
	
	public List<Tree> subTrees_with_support(double supportThres) {
		
		List<Node> toProcess 	= new ArrayList<Node>();
		List<Node> subTreeHeads = new ArrayList<Node>();
		toProcess.add( getRoot() );
		
		while (toProcess.size() > 0) {
			
			Node n = toProcess.remove(0);
			
			if (n.getSupport() >= supportThres) {
				subTreeHeads.add(n);
				
			} else {
				if (!n.isTip()) {
					toProcess.addAll(n.getChildren());
				}
			}
			
		}
		
		/*
		System.out.println("Number of subtrees with support >= "+supportThres+" = "+subTreeHeads.size());
		for (Node n : subTreeHeads) {
			System.out.println(n.toString());
		}
		*/
		
		List<Tree> subTr = new ArrayList<Tree>();
		for (Node n : subTreeHeads) {
			Tree t = subTree(n);
			subTr.add(t);
		}
		
		return ( subTr );
		//return ( subTreeHeads );
	}
	
	public Node MRCA(Node a, Node b) {
		
		List<Node> ancsA = new ArrayList<Node>();
		Node tempA = a;
		do {
			ancsA.add(tempA);
			tempA = tempA.getParent();
		} while (!tempA.isRoot());
		

		List<Node> ancsB = new ArrayList<Node>();
		Node tempB = b;
		do {
			ancsB.add(tempB);
			tempB = tempB.getParent();
		} while (!tempB.isRoot());
		
		Node mrca = ancsA.get(ancsA.size()-1);
		
		boolean again = true;
		while (again) {
			tempA = ancsA.remove(0);
			if (ancsB.contains(tempA)) {
				mrca 	= tempA;
				again 	= false;
			} else {
				again = (ancsA.size() > 0);
			}
		}
		
		return mrca;
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	void calculateTreeString() {
		Node rootNode = getRoot();
		trLine = rootNode.toString();
	}
	
	/**
	 * returns the newick tree string - but note this may not be current if the nodes have changed
	 * @return
	 */
	public String trLine() {
		return trLine;
	}
	
	/**
	 * recalculates newick tree string from nodes list
	 */
	public String toString() {
		calculateTreeString();
		return trLine;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String nodesInfo() {
		StringBuffer txt = new StringBuffer();
		for (Node n : nodes) {
			txt.append(n.info());
			txt.append("------------\n");
		}
		return txt.toString();
	}
	
	public String tipList() {
		StringBuffer txt = new StringBuffer();
		for (Node n : nodes) {
			if (n.isTip()) {
				txt.append(n.getName()+"\n");
			}
		}
		return txt.toString();
	}
	
	public List<String> tipNames() {
		List<String> tn = new ArrayList<String>();
		for (Node n : nodes) {
			if (n.isTip()) {
				tn.add(n.getName());
			}
		}
		return tn;
	}
	
}
