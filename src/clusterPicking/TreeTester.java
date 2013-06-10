package clusterPicking;

import java.io.*;
import java.util.*;

/**
 * class to test the Tree class
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
 * @version 22 Sept 2011 - test 5
 */
public class TreeTester {

	public static void test1() {
		
		System.out.println("** Test 1 - define a tree from a newick string");
		
		String trLine 	= "(A:1,B:2)0.9:1";
		
		System.out.println("Newick tree line = "+trLine);
		
		System.out.println("Create tree .... ");
		Tree t 			= new Tree();
		t.readTree(trLine);
		
		System.out.println("Node details");
		System.out.println(t.nodesInfo());
		
	}
	
	public static void test2() {
		
		System.out.println("** Test 2 - read in a tree from file and print out the node details");
		
		String filename = "C://Users//Samantha Lycett//Documents//Emma//ARefFT5.9.newick";
		//String filename = "C://Users//Samantha Lycett//Documents//Emma//example.nwk";
		TreeReader inT  = new TreeReader();
		inT.setFileName(filename);
		inT.openFile();
		Tree t = inT.readNextTree();
		inT.closeFile();
		
		System.out.println(t.nodesInfo());
		
	}
	
	public static void test3() {
		
		System.out.println("** Test 3 - read in a tree from file and print out a few node details, including MRCA function");
		
		String filename = "C://Users//Samantha Lycett//Documents//Emma//ARefFT5.9.newick";
		//String filename = "C://Users//Samantha Lycett//Documents//Emma//example.nwk";
		TreeReader inT  = new TreeReader();
		inT.setFileName(filename);
		inT.openFile();
		Tree t = inT.readNextTree();
		inT.closeFile();
		
		Node a = t.getNode("124153");	//t.getNode("122109");
		Node b = t.getNode("Node143");	//t.getNode("Node539");
		Node c = t.MRCA(a, b);
		
		System.out.println("Chosen Node 1");
		System.out.println(a.info());
		
		System.out.println("Chosen Node 2");
		System.out.println(b.info());
		
		System.out.println("MRCA of Node 1 & 2");
		System.out.println(c.info());
		
		System.out.println("All children info for "+c.getName());
		for (Node dd : c.getAllMyChildren() ) {
			System.out.println(dd.info());
		}
		
		System.out.println("Test newick string writing from nodes (to screen)");
		System.out.println(a.toString());
		System.out.println(b.toString());
		System.out.println(c.toString());
		
		System.out.println("Number of progeny (tips) for "+c.getName());
		System.out.println(c.calcNumProgeny());
		
	}
	
	public static void test4() {
		
		System.out.println("** Test 4 - Read in Tree from File, extract subtree from a particular node, and write subtree to file");
		
		String filename = "C://Users//Samantha Lycett//Documents//Emma//ARefFT5.9.newick";
		//String filename = "C://Users//Samantha Lycett//Documents//Emma//example.nwk";
		TreeReader inT  = new TreeReader();
		inT.setFileName(filename);
		inT.openFile();
		Tree t = inT.readNextTree();
		inT.closeFile();
		
		Node a = t.getNode("Node539");	
		Node r = t.getRoot();
		
		System.out.println("Number of progeny for "+r.getName()+" = "+r.calcNumProgeny());
		System.out.println("Number of progeny for "+a.getName()+" = "+a.getNumberProgeny());
		
		Tree ta = t.subTree(a);
		Node ra = ta.getRoot();
		
		System.out.println("List of tips for "+ra.getName());
		System.out.println(ta.tipList());
		
		System.out.println("Root node info for subtree from "+ra.getName());
		System.out.println(ra.info());
		
		
		try {
			BufferedWriter outFile = new BufferedWriter(new FileWriter("C://Users//Samantha Lycett//Documents//Emma//example_rootNode.nwk"));
			outFile.write(r.toString()+";");
			outFile.newLine();
			outFile.close();
			
			BufferedWriter outFile2 = new BufferedWriter(new FileWriter("C://Users//Samantha Lycett//Documents//Emma//example_subTree.nwk"));
			outFile2.write(ta.toString()+";");
			outFile2.newLine();
			outFile2.close();
		
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		
		
	}
	
	public static void test5() {
		System.out.println("** Test 5 - read tree from file, split into subtrees with good support");
		
		String filename = "C://Users//Samantha Lycett//Documents//Emma//ARefFT5.9.newick";
		//String filename = "C://Users//Samantha Lycett//Documents//Emma//example.nwk";
		TreeReader inT  = new TreeReader();
		inT.setFileName(filename);
		inT.openFile();
		Tree t = inT.readNextTree();
		inT.closeFile();
		
		double supportThres		= 0.95;
		List<Tree> subTrs		= t.subTrees_with_support(supportThres);
		
		System.out.println("Number of subtrees with support >= "+supportThres+" = "+subTrs.size());
		for (Tree st : subTrs) {
			System.out.println(st.toString());
		}
		
		
		String outName = filename.replace(".newick", "_subtrees.nwk");
		System.out.println("Writing subtrees to "+outName);
		TreeWriter outFile = new TreeWriter();
		outFile.write(subTrs, outName);
	}
	
	public static void test6() {
		System.out.println("** Cluster Picker Test ");
		
		//String sequencesName 	= "C://Users//Samantha Lycett//Documents//Emma//B_outgroup_refSeq.fas";		//A_outgroup_refSeq.fas";
		//String treeName 		= "C://Users//Samantha Lycett//Documents//Emma//BRefFT5.4.newick";			//ARefFT5.9.newick";
		String sequencesName 	= "C://Users//Samantha Lycett//Documents//Emma//C_outgroup_refSeq.fas";		//A_outgroup_refSeq.fas";
		String treeName 		= "C://Users//Samantha Lycett//Documents//Emma//CRefFT5.11.newick";			//ARefFT5.9.newick";
		
		//BasicSequence.useAbs = false;
		BasicSequence.differenceType 		= "gap";
		
		ClusterPicker cp = new ClusterPicker();
		cp.setInitialSupportThres(0.95);
		cp.setSupportThres(0.9);
		cp.setGeneticThres(4.5/100);
		cp.readSequences(sequencesName);
		cp.readTree(treeName);
		cp.processData();
		cp.writeResults();
		
	}
	
	public static void test7() {
		System.out.println("** Cluster Picker Test on 5 data sets **");
		
		ClusterPicker.test();
		
	}
	
	public static void testBEASTTree() {
		
		System.out.println("** Test Cluster Picking on BEAST Tree -> NWK only **");
		
		String path =  "C://data//human_flu//epidemics3//scottish//clades//";
		String name =  "genomes_492_GTR_GI4_strict_exponentialGrowth_expPriors_4.combined.tre.posterior.1.nwk";
		
		//String treeName		 = "C://data//human_swine_flu_sanger_sequencing//final_sequences//phrap_assemblies_15Nov2010_with_alb_mod//clades//genomes_492_GTR_GI4_strict_exponentialGrowth_expPriors_4.combined.posterior.nwk";
		String treeName		 = path + name;
		String outName		 = treeName.replace(".nwk", "") + "_clusters_withSupport.figTree";
		
		double supportThres	 = 0.5;
		
		// read tree
		Tree theTree 		= (new TreeReader()).readTree(treeName);
		System.out.println("Read tree with "+theTree.tipNames().size()+" tips");
		
		// break to supported subtrees
		List<Tree> subTrees = theTree.subTrees_with_support(supportThres);
		System.out.println("Found "+subTrees.size()+" subTrees");
		
		// initialise output file (figTree)
		FigTreeWriter figTree = new FigTreeWriter();
		figTree.loadRainbowColours();
		
		// rename tips according to cluster
		int clusterNumber = 0;
		Hashtable<String,String> clusteredSequenceNames = new Hashtable<String,String>();
		
		for (Tree st : subTrees) {
			clusterNumber++;
			
			for (String tipName : st.tipNames()) {
				String newName = "Clust"+clusterNumber+"_"+tipName;
				clusteredSequenceNames.put(tipName, newName);
				
				int col = (int)Math.floor((double)(255*clusterNumber)/(double)subTrees.size());
				figTree.setColour(newName, col);
				
				System.out.println(tipName+"\t"+newName+"\t"+col);
			}
			
		}
		
		for (Node n : theTree.getNodes()) {
			if ( clusteredSequenceNames.containsKey(n.getName() ) ) {
				String newName = clusteredSequenceNames.get(n.getName());
				n.setName(newName);
			}
		}
		
		figTree.writeTree(theTree, outName);
		
	}
	
	
	public static void main (String args[]) {
		System.out.println("** Trees Tester **");
		//test1();
		//test2();
		//test3();
		//test4();
		//test5();
		//test6();
		test7();
		
		//testBEASTTree();
		
		System.out.println("** END **");
	}
	
}
