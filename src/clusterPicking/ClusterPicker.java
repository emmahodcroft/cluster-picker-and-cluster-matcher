package clusterPicking;

import java.util.*;
import java.io.*;

/**
 *  ClusterPicker -
 *  This is the ClusterPicker and also contains a command line interface.
 *  Picks clusters from a phylogenetic tree and set of sequences according to bootstrap values and genetic distance.
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
 * @created 22 Sept 2011
 * @version 22 Sept 2011
 * @version 23 Sept 2011
 * @version 25 Sept 2011
 * @version 2  Oct  2011 - added main method
 * @version 3  Oct  2011 - changed checkMatchingNames; log file filename now derives from tree file name, not sequences file name
 * @version 3  Oct  2011 - note differences between sequences are measured on sites which are not gaps
 * @version 9  Nov  2011 - option to write lists of large cluster membership; default is to write lists of cluster names with 10 or more members
 * @version 17 Nov  2011 - re-run tests with large cluster threshold
 * @version 23 Nov  2011 - set storePairwise to false - this will make the code run slower, but should be OK for large data sets
 * @version 28 March 2012 - modified for release 
 * @version 2  April 2012 - set storePairwise to true - for testing. tidying o/p for release
 * @version 2  April 2012 - set storePairwise to false again - for testing with jar files.
 * @version 1  July  2012 - added missingSeqs and missingTips specifically for use with ClusterPickerGUI
 * @version 4  July  2012 - added numMissingSeqs and numMissingTips for use with ClusterPickerGUI
 * @version 4  July  2012 - added numberOfClusters and numberOfLargeClusters for use with ClusterPickerGUI
 * @version 20 July  2013 - corrections for command line operation from a single line
 * @version 12 Sept  2013 - added options for processing with ambiguities
 */
public class ClusterPicker {
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ClusterPicker instance variables

	boolean verbose 		= true;
	boolean storePairwise 	= false;					// set this to true if you have a small dataset
														// it will store any calculated pairwise distance and re-use the value if needed rather than calculate from scratch each time
	
	String treeFileName;
	String sequencesFileName;
	String outName;
	String logName;
	String outTreeName;
	
	Tree 						theTree;
	List<BasicSequence> 		seqs;
	List<String>				seqNames;
	List<List<BasicSequence>> 	clusterList;
	List<Tree> 					goodTrees;
	List<Integer>				keepResult;
	
	double  initialSupportThres = 0.95;
	double	supportThres 		= 0.75;
	double  geneticThres 		= 4.5/100;
	
	Hashtable<String,Double> pairwiseDistances = new Hashtable<String,Double>();
	
	int		largeClusterThreshold = 10;
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ClusterPicker instance constructor
	
	public ClusterPicker() {
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// get and set methods
	
	public void setInitialSupportThres(double s) {
		this.initialSupportThres = s;
	}
	
	public void setSupportThres(double s) {
		this.supportThres = s;
	}
	
	public void setGeneticThres(double g) {
		this.geneticThres = g;
	}
	
	public void setLargeClusterThreshold(int thres) {
		if (thres <= 0) {
			this.largeClusterThreshold = Integer.MAX_VALUE;
		} else {
			this.largeClusterThreshold 	= thres;
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// processing methods
	
	/**
	 * reads the fasta format sequences from input sequences filename (s).
	 */
	public void readSequences(String s) {
		this.sequencesFileName 	= s;
		//this.outName 			= sequencesFileName.replace(".fas", "")+"_clusterPicks.fas";
		//this.logName			= sequencesFileName.replace(".fas", "")+"_clusterPicks_log.txt";
		readSequences();
	}
	
	private void readSequences() {
		
		if (verbose) {
			System.out.print("Reading sequences from "+sequencesFileName+" ... ");
		}
		
		ReadFasta rf = new ReadFasta(this.sequencesFileName);
		rf.openFile();
		
		seqs 		= new ArrayList<BasicSequence>();
		seqNames	= new ArrayList<String>();
		int count 	= 0;
		
		while (rf.hasNext()) {
			String[] lines 		= rf.next();
			BasicSequence bs	= new BasicSequence(lines, count);
			seqs.add(bs);
			seqNames.add(bs.header());
			count++;
		}
		
		rf.closeFile();
		
		if (verbose) {
			System.out.println("Read "+seqs.size()+" sequences");
			System.out.println("First sequence is:"+seqs.get(0).header());
			System.out.println("Last  sequence is:"+seqs.get(seqs.size()-1).header());
		}
		
	}
	
	
	/**
	 * reads a newick format tree, with branch lengths and node support from input tree filename (s).
	 * Recommend using after readSequences.
	 * Also checks that tree tip names match sequences names (optional with verbose) and sets the output file names, based on the input tree file name.
	 * @param s
	 */
	public void readTree(String s) {
		this.treeFileName 	 = s;
		
		// set output file names
		String[] els		 = s.split("\\.");
		String ext			 = els[els.length-1];
		
		this.outTreeName 	 = s.replace("."+ext, "")+"_clusterPicks.nwk";
		this.logName		 = s.replace("."+ext, "")+"_clusterPicks_log.txt";
		
		String tn			 = (new File(treeFileName)).getName().replace("."+ext, ""); 
		
		this.outName 		 = sequencesFileName.replace(".fas", "")+"_"+tn+"_clusterPicks.fas";
		
		// read the tree from the tree file name
		readTree();
	}
	
	private void readTree() {
		
		if (verbose) System.out.print("Reading tree from "+treeFileName+" ... ");
		
		TreeReader inTree 	= new TreeReader();
		theTree 			= inTree.readTree(this.treeFileName);
		
		if (verbose) {
			System.out.println("Read tree with "+theTree.tipNames().size()+" tips");
			checkMatchingNames();
		}
	}
	
	/**
	 * returns the tipnames for which there are no sequence names for GUI
	 * @return
	 */
	protected String missingTips() {
		StringBuffer missingTips = new StringBuffer();
		
		for (String tipName : theTree.tipNames()) {
			if (!seqNames.contains(tipName)) {
				missingTips.append(tipName+"\n");
			}
		}
		
		if (missingTips.length() == 0) {
			missingTips.append("all tips have sequences");
		}
		
		return missingTips.toString();
		
	}
	
	/**
	 * returns the number of tip names for which there are no sequence names for GUI
	 * @return
	 */
	protected int numMissingTips() {
		int numMissing = 0;
		
		for (String tipName : theTree.tipNames()) {
			if (!seqNames.contains(tipName)) {
				numMissing++;
			}
		}
		
		return numMissing;
	}
	
	/**
	 * returns the sequence names for which there are no tip names for GUI
	 * @return
	 */
	protected String missingSeqs() {
		StringBuffer missingSeqs = new StringBuffer();
		
		for (String seqName : seqNames) {
			if (!theTree.tipNames().contains(seqName)) {
				missingSeqs.append(seqName+"\n");
			}
		}
		
		if (missingSeqs.length() == 0) {
			missingSeqs.append("all sequences have tips");
		}
		
		return missingSeqs.toString();
		
	}
	

	/**
	 * returns the number of sequence names for which there are no tip names for GUI
	 * @return
	 */
	protected int numMissingSeqs() {
		int numMissing = 0;
		
		for (String seqName : seqNames) {
			if (!theTree.tipNames().contains(seqName)) {
				numMissing++;
			}
		}
		
		return numMissing;
	}
	
	
	private void checkMatchingNames() {
		
		// checking that all tree tips have corresponding sequence names
		List<String> tipLabels = theTree.tipNames();
		
		int match =  0;
		int miss  = 0;
		
		List<String> missingTips = new ArrayList<String>();
		for (String s : tipLabels) {
			if (seqNames.contains(s)) {
				match++;
			} else {
				miss++;
				missingTips.add(s);
			}
		}
		
		System.out.println(match+"\ttip names have matching sequence names");
		System.out.println(miss+"\ttip names do not have matching sequence names");
		for (String sn : missingTips) {
			System.out.println("Couldnt find sequence for "+sn);
		}
		
		// checking that all sequence names have corresponding tip names
		// actually doesnt matter if there are extra sequences (e.g. outgroups)
		
		match = 0;
		miss  = 0;
		List<String> missingSequences = new ArrayList<String>();
		for (String sn : seqNames) {
			if (tipLabels.contains(sn)) {
				match++;
			} else {
				miss++;
				missingSequences.add(sn);
			}
		}
		
		System.out.println(match+"\tsequence names have matching tip names");
		System.out.println(miss+"\tsequence names do not have matching tip names");
		for (String sn : missingSequences) {
			System.out.println("Couldnt find tip for "+sn);
		}
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private List<BasicSequence> getTipSequences(Tree t) {
		// get tip labels
		List<String> tipLabels = t.tipNames();
		
		
		// get the corresponding sequences
		List<BasicSequence> tipSeqs = new ArrayList<BasicSequence>();
		for (String sname : tipLabels) {
		
			if (seqNames.contains(sname)) {
				int j = seqNames.indexOf(sname);
				tipSeqs.add( seqs.get(j) );
				//System.out.println("Found sequence "+sname);
			} else {
				System.out.println("Sorry couldnt find "+sname+" in sequences names");
			}
		}
		
		return ( tipSeqs );
	}
	
	private double getMaxGeneticDistance(List<BasicSequence> tipSeqs) {
		// get pairwise distances of these sequences
		// notice that store as calculate in hash table for later use - if enabled
		double max_d_ij = 0;
		
		for (int i = 0; i < (tipSeqs.size()-1); i++) {
			BasicSequence seq_i = tipSeqs.get(i);
			
			for (int j = (i+1); j < tipSeqs.size(); j++) {
				BasicSequence seq_j = tipSeqs.get(j);
				
				double d_ij	= 0;
				
				if (storePairwise) {
				
					String n_ij	= seq_i.header() + "-" + seq_j.header();
					String n_ji = seq_j.header() + "-" + seq_i.header();
				
				
					if ( pairwiseDistances.containsKey(n_ij) || ( pairwiseDistances.containsKey(n_ji) ) ) {
						d_ij = pairwiseDistances.get(n_ij);
					} else {
						d_ij = seq_i.fractDifference(seq_j);
						pairwiseDistances.put(n_ij, d_ij);
					}
				
				} else {
					d_ij = seq_i.fractDifference(seq_j);
				}
				
				if (d_ij > max_d_ij) {
					max_d_ij = d_ij;
				}
				
			}
		}
		
		return max_d_ij;
	}
	
	private void calcMaxGeneticDistance_forSubTree(Tree t) {
		
		double max_d_ij = getMaxGeneticDistance(getTipSequences(t));
		Object trait	= new Double(max_d_ij);
		
		Node rootNode   = t.getRoot();
		rootNode.setTrait(trait);
		
	}
		
	private boolean good_support_and_genetic_distance(Node n) {
		
		double support = n.getSupport();
		Object trait   = n.getTrait();
		if (trait != null) {
			double  dd = (Double)trait;
			boolean ok = ( (support >= supportThres) && ( dd <= geneticThres ) );
			return ok;
		} else {
			return false;
		}
		
	}
	
	/**
	 * method to actually process the data - assumes that readSequences and readTree have already been done.
	 * use writeResults after this has been called.
	 */
	public void processData() {
		
		// do first cut on subtrees separately so that are not calculating all pairwise distances		
		List<Tree> subTrees = theTree.subTrees_with_support(initialSupportThres);
		if (verbose) System.out.println("Found "+subTrees.size()+" initial subtrees within support = "+initialSupportThres);
		
		if (verbose) System.out.println("Investigating initial subtrees for support = "+supportThres+" and diversity <= "+geneticThres+" genetic distance");
		
		goodTrees 								= new ArrayList<Tree>();
		clusterList							 	= new ArrayList<List<BasicSequence>>();
		
		for (Tree t : subTrees) {	
			calcMaxGeneticDistance_forSubTree(t);
			Node rootNode = t.getRoot();
			if (verbose) System.out.println("Original Subtree:"+rootNode.getName()+" has support = "+rootNode.getSupport()+", genetic distance = "+(Double)rootNode.getTrait()+", and number of tips = "+t.tipNames().size());
			
			
			List<Node> toProcess = new ArrayList<Node>();
			toProcess.add( t.getRoot() );
			
			while ( toProcess.size() > 0) {
				Node n = toProcess.remove(0);
				Tree st = t.subTree(n);
				calcMaxGeneticDistance_forSubTree(st);
				
				if (good_support_and_genetic_distance(n)) {	
					goodTrees.add( st );
					clusterList.add( getTipSequences(st) );
					
					if (verbose) System.out.println("Subtree:"+n.getName()+" has support = "+n.getSupport()+", genetic distance = "+(Double)n.getTrait()+", and number of tips = "+st.tipNames().size());
					
				} else {
					if (!n.isTip()) {
						//System.out.println(n.getName()+" is too diverse will add all children to be processed");
						toProcess.addAll(n.getChildren());
					}
				}
			}
			
		}
		
		
		if (verbose) System.out.println("Found "+goodTrees.size()+" subtrees within genetic distance = "+geneticThres);
				
	}
	
	/*
	private int decideToKeepCluster(List<BasicSequence> cl) {
		
		//boolean result = (cl.size() >= largeClusterThreshold);
		
		int result 		= 0;
		int lanlCount 	= 0;
		int ukCount 	= 0;
		
			for ( BasicSequence s : cl ) {
				String seqName = s.header();
				//depending on what clusters are of interest modify this part
					if (seqName.startsWith("A1.")) {
						lanlCount++;
					} else {
						ukCount++;
					}
			}
			
					if (ukCount <= 2){
						result = 0;
					}
					else if (lanlCount==0){
						result= 1;
					}
					else {
						result=2;
					}
					
		return result;
	}
	*/
	
	/**
	 * returns the number of clusters found - for use with ClusterPickerGUI
	 */
	public int numberOfClusters() {
		if (clusterList != null) {
			return clusterList.size();
		} else {
			return 0;
		}
	}
	
	/**
	 * returns the number of clusters found >= large cluster threshold - for use with ClusterPickerGUI
	 * @return
	 */
	public int numberOfLargeClusters() {
		int numLarge = 0;
		
		if ( clusterList != null ) {
			for (List<BasicSequence> cl : clusterList ) {
				if (cl.size() >= largeClusterThreshold) {
					numLarge++;
				}
			}
		}
		
		return numLarge;
	}
	
	/**
	 * writes sequences, renamed newick tree, renamed figTree tree and log files.
	 * use after processData()
	 * returns String of where files are written to (for ClusterPickerGUI)
	 */
	public String writeResults() {
		
		///////////////////////////////////////////////////////////////////////////////////
		// write clustered sequences to file
		
		WriteFasta outFile 	= new WriteFasta();
		outFile.openFile(outName);
		
		int clusterNumber = 0;
		//int clusterTreeNumber =0;
		
		// hash table to contain old name, new name
		Hashtable<String,String> clusteredSequenceNames = new Hashtable<String,String>();
		
		// start setting up fig tree colour tables
		FigTreeWriter figTree = new FigTreeWriter();
		figTree.loadRainbowColours();
		
		//keepResult		= new ArrayList<Integer>();
		
		for (List<BasicSequence> cl : clusterList ) {
			
			clusterNumber++;
			//int result = decideToKeepCluster(cl);
			//keepResult.add(result);
			
			//if (cl.size() >= largeClusterThreshold && result==1) {
			if (cl.size() >= largeClusterThreshold) {
				try {
					String outName2	= (new String(outName)).replace(".fas", "")+"_cluster"+clusterNumber+"_sequenceList.txt";
					BufferedWriter largeClusterOutFile = new BufferedWriter(new FileWriter(outName2));
					
					for (BasicSequence s : cl) {
						largeClusterOutFile.write(s.header());
						largeClusterOutFile.newLine();
					}
					
					largeClusterOutFile.close();
					
				} catch (IOException e) {
					
				}
			}
			
			/*
			if (cl.size() >= largeClusterThreshold && result==2) {
				try {
					String outName2	= (new String(outName)).replace(".fas", "")+"_clusterLANL"+clusterNumber+"_sequenceList.txt";
					BufferedWriter largeClusterOutFile = new BufferedWriter(new FileWriter(outName2));
					
					for (BasicSequence s : cl) {
						largeClusterOutFile.write(s.header());
						largeClusterOutFile.newLine();
					}
					
					largeClusterOutFile.close();
					
				} catch (IOException e) {
					
				}
			}
			*/
			
			for ( BasicSequence s : cl ) {
				
				// rename sequence with cluster name
				String newName = "Clust"+clusterNumber+"_"+s.header();
				clusteredSequenceNames.put(s.header(), newName);
				
				// do not reset header of sequences
				// need to keep these unchanged for GUI operation
				
				//s.setHeader(newName);
				
				int clustNumberCol = (int)Math.floor( ((double)clusterNumber*256.0)/(double)clusterList.size() );
				figTree.setColour(newName, clustNumberCol);
				
				BasicSequence tempS = new BasicSequence(newName, s.charSeq());
				outFile.append(tempS);
			}
			
			// do not write whole cluster of sequence to file
			// have done above with tempS
			
			// write this cluster of sequences to file
			//outFile.append( cl );
		}
		
		outFile.closeFile();
		
		////////////////////////////////////////////////////////////////////////////////
		// rename nodes of tree to correspond to new cluster names - for ease of plotting
		// note this permanently changes the names of the tree object
			
		Hashtable<String,String> tempTbl = new Hashtable<String,String>();
		
		for ( Node n : theTree.getNodes() ) {
			if ( clusteredSequenceNames.containsKey(n.getName() ) ) {
				String newName = clusteredSequenceNames.get(n.getName());
				//System.out.println(n.getName()+" renamed "+newName);
				
				tempTbl.put(newName, new String(n.getName()) );
				
				n.setName(newName);
				//figTree.setColour(newName, 255, 0, 255);

			}
		}
		
		TreeWriter outTree = new TreeWriter();
		outTree.write(theTree, outTreeName);
		
		figTree.writeTree(theTree, outTreeName+".figTree");
		
		// put the original names back in the tree, incase want to reuse
		// e.g. in GUI
		// this is not very elegant, will correct when use jebl
		
		for (Node n : theTree.getNodes() ) {
			if (tempTbl.containsKey(n.getName())) {
				String origName = tempTbl.get(n.getName());
				//System.out.println(n.getName()+" renamed "+origName);
				n.setName(origName);
			}
		}
		
		/////////////////////////////////////////////////////////////////////////////////
		// write log file
		
		try {
			BufferedWriter logFile = new BufferedWriter(new FileWriter(logName));
		
			logFile.write("** Cluster Picker Results **\n");
			logFile.write("Input sequences =\t"+sequencesFileName+"\n");
			logFile.write("Input tree =\t"+treeFileName+"\n");
			logFile.write("Initial support threshold=\t"+initialSupportThres+"\n");
			logFile.write("Support threshold=\t"+supportThres+"\n");
			logFile.write("Genetic distance threshold=\t"+geneticThres+"\n");
			logFile.write("Large cluster threshold=\t"+largeClusterThreshold+"\n");
			logFile.write("-------------------------\n");
			logFile.write("** Sequences with cluster assignment output with new names\n");
			logFile.write("** Tree modified to contain new names\n");
			logFile.write("** new names have form: Clust(C)_(SequenceName) where C = cluster number, e.g. Clust25_139320\n");
			logFile.write("-------------------------\n");
			logFile.write("Output sequences =\t"+outName+"\n");
			logFile.write("Output tree=\t"+outTreeName+"\n");
			logFile.write("Output figtree=\t"+outTreeName+".figTree\n");
			logFile.write("-------------------------\n");
			logFile.write("There are\t"+seqs.size()+"\tsequences\n");
			logFile.write("Tree has\t"+theTree.tipNames().size()+"\ttips\n");
			logFile.write("Found\t"+clusterList.size()+"\tclusters\n");
			//logFile.write("ClusterNumber\tNumberOfTips\tNumberOfTipsCheck\tTipNames\tBootstrap\tGD\ttoKeepResult\n");
			logFile.write("ClusterNumber\tNumberOfTips\tNumberOfTipsCheck\tTipNames\tBootstrap\tGD\n");
			clusterNumber = 0;
			for (List<BasicSequence> cl : clusterList) {
				clusterNumber++;
				Tree ct = goodTrees.get(clusterNumber-1);
				//int result2 = keepResult.get(clusterNumber-1); //
				//logFile.write(clusterNumber+"\t"+cl.size()+"\t"+ct.getRoot().getSupport()+"\t"+ct.getRoot().getTrait()+"\n");
				
				// for release
				logFile.write(clusterNumber+"\t"+cl.size()+"\t"+ct.tipNames().size()+"\t"+ct.tipNames()+"\t"+ct.getRoot().getSupport()+"\t"+ct.getRoot().getTrait()+"\n");
				
				//logFile.write(clusterNumber+"\t"+cl.size()+"\t"+ct.tipNames().size()+"\t"+ct.tipNames()+"\t"+ct.getRoot().getSupport()+"\t"+ct.getRoot().getTrait()+"\t"+result2+"\n");
			}
			//for (Tree ct : goodTrees){
			//	clusterTreeNumber++;
			//	logFile.write(clusterTreeNumber+"\t"+ct.tipNames().size()+"\t"+ct.getRoot().getSupport()+"\t"+ct.getRoot().getTrait()+"\n");
			//}
			logFile.write("-------------------------\n");
			
			logFile.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		/////////////////////////////////////////////////////////////////////////////////
		
		StringBuffer txt = new StringBuffer();
		txt.append("Written sequences to "+outName+"\n");
		txt.append("Written renamed tree to "+outTreeName+"\n");
		txt.append("Written coloured figTree to "+outTreeName+".figTree\n");
		txt.append("Written log to "+logName+"\n");
		
		if (verbose) {
			System.out.println(txt.toString());
			/*
			System.out.println("Written sequences to "+outName);
			System.out.println("Written renamed tree to "+outTreeName);
			System.out.println("Written coloured figTree tree to "+outTreeName+".figTree");
			System.out.println("Written log to "+logName);
			*/
		}
		
		
		return (txt.toString());
		
	}
	
	// END OF ClusterPicker instance class definition
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ClusterPicker class methods - including MAIN
	
	/**
	 * test cluster picker on 5 data sets - note the paths etc are hardwired for SJL testing
	 */
	public static void test() {
		
		double initialThres 				= 0.9;
		double supportThres					= 0.9;
		double geneticThres					= 4.5/100;
		int 	largeClusterThres			= 10;
		BasicSequence.differenceType 		= "gap";
		
		// default settings
		String path				= "C://Users//Samantha Lycett//Documents//Emma//";
		String[] snames			= {"A_outgroup_refSeq.fas", "B_outgroup_refSeq.fas", "C_outgroup_refSeq.fas", "C_outgroup_refSeq.fas", "C_outgroup_refSeq.fas"};
		String[] tnames			= {"ARefFT5.9.newick", "BRefFT5.4.newick", "CRefFT5.1.newick", "CRefFT5.11.newick", "CRefFT5.19.newick"};
		
		for (int i = 0; i < snames.length; i++) {
			String sequencesName 	= path + snames[i];
			String treeName 		= path + tnames[i];
			
			System.out.println("---------------------------------------");
			
			ClusterPicker cp = new ClusterPicker();
			cp.setInitialSupportThres(initialThres);
			cp.setSupportThres(supportThres);
			cp.setGeneticThres(geneticThres);
			cp.setLargeClusterThreshold(largeClusterThres);
			cp.readSequences(sequencesName);
			cp.readTree(treeName);
			cp.processData();
			cp.writeResults();
			
			System.out.println("---------------------------------------");
		}
		
	}
	
	/**
	 * timing test run for Cluster Picker - note the paths etc are hardwired for SJL testing.
	 * Also this timing test is not the same as Manon's timing test reported in the paper.
	 */
	public static void timingTest() {
		
		System.out.println("** Timing Test **");
		
		double initialThres 				= 0.9;
		double supportThres					= 0.9;
		double geneticThres					= 4.5/100;
		int 	largeClusterThres			= 10;
		BasicSequence.differenceType 		= "gap";			// "ambiguity";	//"valid"; // "abs";
		
		// default settings
		String path				= "C://Users//Samantha Lycett//Documents//Manon//CPT_Write-up//tests//";
		String sname			= "HIVpol20000uniqueRenamed.fas";
		String tname			= "tr";
		
		
		String sequencesName 	= path + sname;
		
		//////////////////////////////////////////////////////////
		System.out.print("Initialise Cluster Picker ");
		long t1 = System.currentTimeMillis();
		
		ClusterPicker cp = new ClusterPicker();
		cp.setInitialSupportThres(initialThres);
		cp.setSupportThres(supportThres);
		cp.setGeneticThres(geneticThres);
		cp.setLargeClusterThreshold(largeClusterThres);
		cp.verbose = false;
		
		long t2 = System.currentTimeMillis();
		System.out.println("in "+(float)(t2-t1)/1000.0+"s");
		
		//////////////////////////////////////////////////////////
		
		System.out.print("Read sequences ");
		long t3 = System.currentTimeMillis();
		
		cp.readSequences(sequencesName);
		
		long t4 = System.currentTimeMillis();
		System.out.println("in "+(float)(t4-t3)/1000.0+"s");

		//////////////////////////////////////////////////////////
		
		for (int i = 18; i >= 1; i--) {
		
			String treeName 		= path + tname + i + ".nwk";
			
			System.out.println("---------------------------------------");
		
			long t5 = System.currentTimeMillis();
			cp.readTree(treeName);
			long t6 = System.currentTimeMillis();
			System.out.print("Read tree in "+(float)(t6-t5)/1000.0+"s");
			

			long t7 = System.currentTimeMillis();
			cp.processData();
			long t8 = System.currentTimeMillis();
			System.out.print("\tProcess data in "+(float)(t8-t7)/1000.0+"s with "+cp.theTree.tipNames().size()+" tips");
			

			long t9 = System.currentTimeMillis();
			cp.writeResults();
			long t10 = System.currentTimeMillis();
			System.out.println("\tWrite results in "+(float)(t10-t9)/1000.0+"s");
			
			
			System.out.println("---------------------------------------");
		}
		
		
	}
	
	
	/**
	 * run cluster picker using input arguments, or enter via prompts
	 * @param args
	 */
	public static void run(String args[]) {
		
		
		// default settings
		String sequencesName 	= "C://Users//Samantha Lycett//Documents//C_outgroup_refSeq.fas";		//A_outgroup_refSeq.fas";
		String treeName 		= "C://Users//Samantha Lycett//Documents//CRefFT5.11.newick";			//ARefFT5.9.newick";
		
		double initialThres 	= 0.9;
		double supportThres		= 0.9;
		double geneticThres		= 4.5/100;
		int	   largeClusterThres = 10;
		
		//BasicSequence.useAbs 	= false;
		//BasicSequence.differenceType 		= "gap";														// this means calculate the genetic distances between sites which are not gaps
		BasicSequence.setDifferenceType("gap");
		
		//////////////////////////////////////////////////////////////////////////////////
		// enter parameters from keyboard input if not in args
		
		Scanner keyboard = new Scanner(System.in);
		
		if (args.length >= 1) {
			sequencesName = args[0];	
		} else {
			System.out.println("Please enter fasta format sequences file name to process (include // in path and .fas extension):");
			sequencesName = keyboard.nextLine().trim();
		}
		
		if (args.length >= 2) {
			treeName = args[1];
		} else {
			System.out.println("Please enter newick format tree, with branch lengths and node support (include // in path and .nwk extension):");
			treeName = keyboard.nextLine().trim();
		}
		
		if (args.length >= 3) {
			
			if (args[2].startsWith("-d")) {
				System.out.println("Using defaults:");
				System.out.println("Initial Support Thres =\t"+initialThres);
				System.out.println("Main Support Thres =\t"+supportThres);
				System.out.println("Genetic Thres =\t"+geneticThres);
				if (largeClusterThres > 0) {
					System.out.println("Output cluster membership lists for clusters with >= "+largeClusterThres+" members");
				}
				System.out.println("Sequence difference type =\t"+BasicSequence.differenceType);
				
			} else {
				try {
					initialThres = Double.parseDouble(args[2]);
				} catch (NumberFormatException e) {
					System.out.println("Sorry cant process "+args[2]+" will use default = "+initialThres);
				}
			}
			
			if (args.length >= 4) {
				try {
					supportThres = Double.parseDouble(args[3]);
				} catch (NumberFormatException e) {
					System.out.println("Sorry cant process "+args[3]+" will use default = "+supportThres);
				}
			} else {
				System.out.println("Using default support thres = "+supportThres);
			}
			
			if (args.length >= 5) {
				try {
					geneticThres = Double.parseDouble(args[4]);
				} catch (NumberFormatException e) {
					System.out.println("Sorry cant process "+args[4]+" will use default = "+geneticThres);
				}
			} else {
				System.out.println("Using default genetic thres = "+geneticThres);
			}
			
			if (args.length >= 6) {
				try {
					largeClusterThres = Integer.parseInt(args[5]);
				} catch (NumberFormatException e) {
					System.out.println("Sorry cant process "+args[5]+" will use default = "+largeClusterThres);
				}
			}
			
			if (args.length >= 7) {
				String diffType = args[6];
				BasicSequence.setDifferenceType(diffType);
			}
			
		} else {
	
			System.out.println("The tree will be broken into initial subtrees before further processing");
			System.out.println("Enter initial support threshold for these initial subtrees");
			System.out.println("To use the default setting of "+initialThres+" enter y or enter a new value:");
			String ans = keyboard.nextLine().trim();
			if (!ans.startsWith("y")) {
				try {
					initialThres = Double.parseDouble(ans);
				} catch (NumberFormatException e) {
					System.out.println("Sorry cant process "+ans+" will use default = "+initialThres);
				}
			}
			
			System.out.println("Please enter main support threshold for clusters");
			System.out.println("To use the default setting of "+supportThres+" enter y or enter a new value:");
			ans = keyboard.nextLine().trim();
			if (!ans.startsWith("y")) {
				try {
					supportThres = Double.parseDouble(ans);
				} catch (NumberFormatException e) {
					System.out.println("Sorry cant process "+ans+" will use default = "+supportThres);
				}
			}
		
			System.out.println("Please enter genetic distance threshold for clusters in %");
			System.out.println("To use the default setting of "+100*geneticThres+"% enter y or enter a new value (e.g. 5.0):");
			ans = keyboard.nextLine().trim();
			if (!ans.startsWith("y")) {
				try {
					geneticThres = Double.parseDouble(ans)/100;
				} catch (NumberFormatException e) {
					System.out.println("Sorry cant process "+ans+" will use default = "+geneticThres);
				}
			}
			
			System.out.println("To output all cluster names for clusters of size >= X enter X");
			System.out.println("To use default = "+largeClusterThres+" enter y or enter a new value or enter 0 to output nothing:");
			ans = keyboard.nextLine().trim();
			if (!ans.startsWith("y")) {
				try {
					largeClusterThres = Integer.parseInt(ans);
				} catch (NumberFormatException e) {
					System.out.println("Sorry cant process "+ans+" will use default = "+largeClusterThres);
				}
			}
			
			System.out.println("Please enter scoring type for genetic distance");
			System.out.println("abs      \t= count absolute character differences");
			System.out.println("gap      \t= disregard sites with -, ~, or n");
			System.out.println("valid    \t= only count differences for sites with nucleotides: a, c, t, g in both sequences");
			System.out.println("ambiguity\t= disregard sites with -, ~, or n and do not count ambiguities as differences (e.g. a vs r is not a difference)");
			System.out.println("To use default = "+BasicSequence.differenceType+" enter y or enter new value from list above");
			ans = keyboard.nextLine().trim().toLowerCase();
			if (!ans.startsWith("y")) {
				BasicSequence.setDifferenceType(ans);
			}
		}
		
		////////////////////////////////////////////////////////////////////////////////////////
		// MAIN LOOP
		
		boolean again = true;
		
		while (again) {
			
			//////////////////////////////////////////////////////////////////////////////////
			// run the cluster picker with the parameters
			
			System.out.println("---------------------------------------");
			
			ClusterPicker cp = new ClusterPicker();
			cp.setInitialSupportThres(initialThres);
			cp.setSupportThres(supportThres);
			cp.setGeneticThres(geneticThres);
			cp.setLargeClusterThreshold(largeClusterThres);
			cp.readSequences(sequencesName);
			cp.readTree(treeName);
			cp.processData();
			cp.writeResults();
			
			System.out.println("---------------------------------------");
			//////////////////////////////////////////////////////////////////////////////////
			
			again = false;			// need to set this to false if not in interactive mode
			
			// if in interactive mode then ask about doing it again
			if (args.length == 0) {
				System.out.println("Again with different data but same settings ? (y/n)");
				String ans 	= keyboard.nextLine().trim();
				again 		= ans.startsWith("y");
				
				if (again) {
				
					System.out.println("Please enter fasta format sequences file name to process (include // in path and .fas extension):");
					sequencesName 	= keyboard.nextLine().trim();
					System.out.println("Please enter newick format tree, with branch lengths and node support (include // in path and .nwk extension):");
					treeName 		= keyboard.nextLine().trim();
				
				} else {
					
					System.out.println("Again with same data but different settings ? (y/n)");
					ans 			= keyboard.nextLine().trim();
					again 			= ans.startsWith("y");
					
					if (again) {
						System.out.println("Please enter initial support threshold for clusters:");
						ans = keyboard.nextLine().trim();
						try {
							initialThres = Double.parseDouble(ans);
						} catch (NumberFormatException e) {
							System.out.println("Sorry cant process "+ans+" will use default = "+supportThres);
						}
						
						System.out.println("Please enter main support threshold for clusters:");
						ans = keyboard.nextLine().trim();
						try {
							supportThres = Double.parseDouble(ans);
						} catch (NumberFormatException e) {
							System.out.println("Sorry cant process "+ans+" will use default = "+supportThres);
						}
						
						System.out.println("Please enter genetic threshold for clusters in %:");
						ans = keyboard.nextLine().trim();
						try {
							geneticThres = Double.parseDouble(ans)/100;
						} catch (NumberFormatException e) {
							System.out.println("Sorry cant process "+ans+" will use default = "+supportThres);
						}
						
						System.out.println("To output all cluster names for clusters of size >= X enter X, or enter 0 to output nothing:");
						ans = keyboard.nextLine().trim();
						try {
							largeClusterThres = Integer.parseInt(ans);
						} catch (NumberFormatException e) {
							System.out.println("Sorry cant process "+ans+" will use default = "+largeClusterThres);
						}
						
						System.out.println("Please enter scoring type for genetic distance, or d to use the current default ("+BasicSequence.differenceType+"):");
						ans = keyboard.nextLine().trim().toLowerCase();
						if (!ans.startsWith("d") && !ans.startsWith("y")) {
							BasicSequence.setDifferenceType(ans);
						}
						
					}
				}
			}
			
		}
	}
	
	/**
	 * MAIN METHOD - CLUSTER PICKER
	 * @param args
	 */
	public static void main(String args[]) {
		System.out.println("** ClusterPicker **");
		System.out.println("");
		System.out.println("ClusterPicker Copyright (C) 2013 Samantha Lycett");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY");
		System.out.println("This is free software, and you are welcome to redistribute it under certain conditions");
		System.out.println("See GNU GPLv3 for details http://www.gnu.org/licenses/gpl-3.0.txt");
		System.out.println("Project home page (and tutorials): http://hiv.bio.ed.ac.uk/software.html");
		System.out.println("");
		
		run(args);
		//test();
		//timingTest();
		
		System.out.println("** END **");
	}
	
}
