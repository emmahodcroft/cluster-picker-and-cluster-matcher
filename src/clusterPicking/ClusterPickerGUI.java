package clusterPicking;


import java.awt.*;
import java.awt.event.*;
//import java.awt.image.*;
import java.io.*;

//import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 *  ClusterPickerGUI - 
 *  This is the GUI front end - see ClusterPicker.java for actual cluster picker implementation.
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
 * @author sam
 * @created 29 June 2012
 * @version 29 June 2012
 * @version 1  July 2012
 * @version 4  July 2012
 * @version 5  July 2012
 * @version 10 July 2012
 * @version 23 July 2012 - genetic distance threshold should set correctly now
 * @version 10 June 2013 - added license details and URL etc to about box
 * @version 4  Sept 2013 - rebuilt with new ReadFasta
 */
public class ClusterPickerGUI {
	
	protected String appName   = "ClusterPicker";
	protected String version   = "1.0 (4 Sept 2013)";	//"1.0 (10 June 2013)";	//"1.0 (23 July 2012)";
	protected String author	   = "Dr. S. J. Lycett";
	protected String institute = "University of Edinburgh";
	protected String citation  = "Ragonnet-Cronin et al Automated Analysis of Phylogenetic Clusters";
	protected String url	   = "http://hiv.bio.ed.ac.uk/software.html";
	protected String license   = "GNU GPLv3: http://www.gnu.org/licenses/gpl-3.0.txt";
	//"To be published";		//"Please cite:";
	

	/*
	Automated Analysis of Phylogenetic Clusters
	Manon Ragonnet-Cronin1,*, Emma Hodcroft1, Stéphane Hué2, Esther Fearnhill3, Valerie Delpech4, Andrew J. Leigh Brown1 and Samantha Lycett1on behalf of the UK HIV Drug Resistance Database
	*/

	
	protected JFrame frame;
	
	protected JTextField sequenceTextField;
	protected JTextField treeTextField;
	
	protected JFormattedTextField initThresTextField;
	protected JFormattedTextField supportThresTextField;
	protected JFormattedTextField geneticThresTextField;
	protected JFormattedTextField largeClusterThresTextField;
	
	protected JFormattedTextField numSeqs;
	protected JFormattedTextField numMissSeqs;
	protected JTextArea			  missingSeqsText = new JTextArea(0, 15);
	protected JButton			  missSeqsShow;
	protected JFormattedTextField numTips;
	protected JFormattedTextField numMissTips;
	protected JTextArea			  missingTipsText = new JTextArea(0, 15);
	protected JButton			  missTipsShow;
	protected JFormattedTextField numClusters;
	protected JFormattedTextField numLargeClusters;
	
	protected Dimension 		  labelBoxSize 	= new Dimension(175, 0);
	protected Dimension			  paramBoxSize 	= new Dimension(50, 0);
	
	protected JTextArea			  messages;
	
	protected ClusterPicker cp; 			//= new ClusterPicker();
	protected File 			sequenceFile 	= null;
	protected File 			treeFile 		= null;
	protected double initialSupportThres 	= 0.9;
	protected double supportThres 			= 0.9;
	protected double geneticThres 			= 4.5;			// this is in %, it is divided by 100 on setting to cp.
	protected int    largeClusterThres 		= 10;
	
	// the store pairwise options seems to make the run time much much worse on Andrew Rambaut Mac Book Air
	// so deprecating
	/*
	protected final int	 defaultPairwiseThres= 2000;
	protected int	 pairwiseThres			 = 2000;		// set storePairwise = true if number sequences <= threshold
															// else storePairwise = false
															// use Advanced settings to change
	*/
	
	protected boolean reprocess 			= true;
	
	public ClusterPickerGUI() {
		// initialise cluster picker with default settings
		cp = new ClusterPicker();
		cp.setInitialSupportThres(initialSupportThres);
		cp.setSupportThres(supportThres);
		cp.setGeneticThres(geneticThres / 100.0);
		cp.setLargeClusterThreshold(largeClusterThres);
		cp.storePairwise = false;
		cp.verbose = false;
		// make GUI frame
		makeFrame();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	
	void makeFrame() {
		
		frame 					= new JFrame( appName );
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setMinimumSize(new Dimension(600,300));
		//frame.setPreferredSize(frame.getMinimumSize());

		makeMenuBar();
	
		Container contentPane	= frame.getContentPane();		
		makeContent(contentPane);
	
		frame.pack();
		frame.setVisible(true);
	
	}
	
	void makeMenuBar() {
		JMenuBar menubar = new JMenuBar();
		
		frame.setJMenuBar(menubar);
		
		JMenu fileMenu    = new JMenu("File");
		menubar.add(fileMenu);
		
		JMenuItem newItem = new JMenuItem("New");
		newItem.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// return to default settings
				initialSupportThres = 0.9;
				supportThres 		= 0.9;
				geneticThres 		= 4.5;
				largeClusterThres 	= 10;
				
				initThresTextField.setValue(initialSupportThres);
				supportThresTextField.setValue(supportThres);
				geneticThresTextField.setValue(geneticThres);
				largeClusterThresTextField.setValue(largeClusterThres);
				
				// set file names to null
				sequenceFile 		= null;
				treeFile 	 		= null;
				
				sequenceTextField.setText("please select fasta sequence file");
				treeTextField.setText("please select newick tree file");
				
				// set results to null
				numSeqs.setText(null);
				numTips.setText(null);
				numMissSeqs.setText(null);
				numMissTips.setText(null);
				missingSeqsText.setText(null);
				missingTipsText.setText(null);
				messages.setText(null);
				
				// initialise cluster picker with default settings
				cp 					= new ClusterPicker();
				cp.setInitialSupportThres(initialSupportThres);
				cp.setSupportThres(supportThres);
				cp.setGeneticThres(geneticThres / 100.0);
				cp.setLargeClusterThreshold(largeClusterThres);
				cp.storePairwise 	= false;
				cp.verbose 			= false;
				reprocess  			= true;
				// 
			}
			
		});
		fileMenu.add(newItem);
		
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
			
		});
		fileMenu.add(exitItem);
		
		// turn off options menus as not using storePairwise
		
		/*
		JMenu optionsMenu = new JMenu("Options");
		menubar.add(optionsMenu);
		JMenuItem settingsItem = new JMenuItem("Advanced Settings");
		settingsItem.addActionListener( new AdvancedSettingsActionListener() );
		optionsMenu.add(settingsItem);
		*/
				
		// shift to the right see http://64.18.163.122/rgagnon/javadetails/java-0486.html
	    menubar.add(Box.createGlue());
		
		// Help Menu
		JMenu helpMenu	= new JMenu("Help");
		menubar.add(helpMenu);
		
		JMenuItem instructionsItem = new JMenuItem("Instructions");
		instructionsItem.addActionListener(new ShowInstructionsActionListener() );
		helpMenu.add(instructionsItem);
		
		JMenuItem aboutItem		  = new JMenuItem("About");
		aboutItem.addActionListener(new ShowAboutActionListener());
		helpMenu.add(aboutItem);
		
	}
	
	void makeContent(Container contentPane) {
		contentPane.setLayout(new BorderLayout());
		//contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		////////////////////////////////////////////////////////////////////////////////
		// title
		JLabel titleLabel = new JLabel("Cluster Picker");
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setVerticalAlignment(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		contentPane.add(titleLabel, BorderLayout.NORTH);
		
		Container  mainContainer = new Container();
		//GridLayout mainLayout 	= new GridLayout(0,1);
		BoxLayout  mainLayout	 = new BoxLayout(mainContainer, BoxLayout.Y_AXIS);
		//mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
		mainContainer.setLayout( mainLayout );
		
		////////////////////////////////////////////////////////////////////////////////
		// the import data pane
		makeImportDataPane( mainContainer );
		
		////////////////////////////////////////////////////////////////////////////////
		// the settings pane
		makeSettingsPane( mainContainer );
		
		////////////////////////////////////////////////////////////////////////////////
		// results panel
		makeResultsPane( mainContainer );
		
		////////////////////////////////////////////////////////////////////////////////
		// messages panel
		makeMessagesPane( mainContainer );
		
		////////////////////////////////////////////////////////////////////////////////
		// add main container to contents
		contentPane.add( mainContainer, BorderLayout.CENTER );
		
		////////////////////////////////////////////////////////////////////////////////
		// GO Button
		
		JButton GOButton 			= new JButton("GO");
		ActionListener GOListener 	= new GOActionListener();
		GOButton.addActionListener(GOListener);
		contentPane.add(GOButton, BorderLayout.SOUTH);
		
		////////////////////////////////////////////////////////////////////////////////
		// reset the frame
		frame.pack();
        frame.setVisible(true);
		
	}
	
	void makeImportDataPane(Container mainContainer) {
		
		/////////////////////////////////////////////////////////////////////////
		// the import data pane
		JPanel importPane = new JPanel();
		importPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		importPane.setBorder(BorderFactory.createTitledBorder("Input"));

		SetFileListener fileListener = new SetFileListener();


		// for sequence data
		JLabel sequenceLabel = new JLabel("Sequence File:");
		sequenceLabel.setMinimumSize(labelBoxSize);
		sequenceLabel.setHorizontalAlignment(JLabel.RIGHT);
		sequenceLabel.setVerticalAlignment(JLabel.CENTER);

		sequenceTextField = new JTextField();
		sequenceTextField.setText("please select fasta sequence file");
		sequenceTextField.addMouseListener(fileListener);

		// for newick tree data
		JLabel treeLabel = new JLabel("Newick File:");
		treeLabel.setMinimumSize(labelBoxSize);
		treeLabel.setHorizontalAlignment(JLabel.RIGHT);
		treeLabel.setVerticalAlignment(JLabel.CENTER);

		treeTextField = new JTextField();
		treeTextField.setText("please select newick tree file");
		treeTextField.addMouseListener(fileListener);

		GroupLayout importLayout = new GroupLayout(importPane);
		importLayout.setAutoCreateGaps(true);
		importLayout.setAutoCreateContainerGaps(true);

		importLayout.setHorizontalGroup( 
				importLayout.createSequentialGroup()
					.addGroup( importLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(sequenceLabel)
							.addComponent(treeLabel) )
					.addGroup( importLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(sequenceTextField)
							.addComponent(treeTextField) )	
				);
		importLayout.setVerticalGroup( 
				importLayout.createSequentialGroup()
					.addGroup(importLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(sequenceLabel)
							.addComponent(sequenceTextField) )
					.addGroup(importLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(treeLabel)
							.addComponent(treeTextField) )
				);

		importPane.setLayout(importLayout);
		mainContainer.add(importPane);
		
	}
	
	void makeSettingsPane(Container mainContainer) {
		////////////////////////////////////////////////////////////////////////////////
		// the settings pane
		
		JPanel settingsPane = new JPanel();
		settingsPane.setBorder(BorderFactory.createTitledBorder("Settings"));
		settingsPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		/*
		GridLayout layout = new GridLayout(0,2);
		layout.setHgap(5);
		settingsPane.setLayout(layout);
		*/
		
		ParameterChangeListener paramListener = new ParameterChangeListener();
		

		// for initial threshold
		JLabel initThresLabel = new JLabel("Initial Threshold:");
		initThresLabel.setMinimumSize(labelBoxSize);
		initThresLabel.setHorizontalAlignment(JLabel.RIGHT);
		initThresLabel.setVerticalAlignment(JLabel.CENTER);
					
		initThresTextField = new JFormattedTextField();
		initThresTextField.setValue(initialSupportThres);
		initThresTextField.setColumns(5);
		//initThresTextField.setMaximumSize(paramBoxSize);
		initThresTextField.addPropertyChangeListener("value", paramListener);
		
		// for main support threshold
		JLabel supportThresLabel = new JLabel("Main Support Threshold:");
		supportThresLabel.setMinimumSize(labelBoxSize);
		supportThresLabel.setHorizontalAlignment(JLabel.RIGHT);
		supportThresLabel.setVerticalAlignment(JLabel.CENTER);
		
		supportThresTextField = new JFormattedTextField();
		supportThresTextField.setValue(supportThres);
		supportThresTextField.setColumns(5);
		//supportThresTextField.setMaximumSize(paramBoxSize);
		supportThresTextField.addPropertyChangeListener("value", paramListener);
		
		
		// for genetic distance threshold
		JLabel geneticThresLabel = new JLabel("Genetic Distance Threshold:");
		geneticThresLabel.setMinimumSize(labelBoxSize);
		geneticThresLabel.setHorizontalAlignment(JLabel.RIGHT);
		geneticThresLabel.setVerticalAlignment(JLabel.CENTER);
		
		geneticThresTextField = new JFormattedTextField();
		geneticThresTextField.setValue(geneticThres);
		geneticThresTextField.setColumns(5);
		//geneticThresTextField.setMaximumSize(paramBoxSize);
		geneticThresTextField.addPropertyChangeListener("value", paramListener);
		
		// for genetic distance threshold
		JLabel clusterThresLabel = new JLabel("Large Cluster Threshold:");
		clusterThresLabel.setMinimumSize(labelBoxSize);
		clusterThresLabel.setHorizontalAlignment(JLabel.RIGHT);
		clusterThresLabel.setVerticalAlignment(JLabel.CENTER);
				
		largeClusterThresTextField = new JFormattedTextField();
		largeClusterThresTextField.setValue(largeClusterThres);
		largeClusterThresTextField.setColumns(5);
		//largeClusterThresTextField.setMaximumSize(paramBoxSize);
		largeClusterThresTextField.addPropertyChangeListener("value", paramListener);
	
		/*
		settingsPane.add(initThresLabel);
		settingsPane.add(initThresTextField);
		settingsPane.add(supportThresLabel);
		settingsPane.add(supportThresTextField);
		settingsPane.add(geneticThresLabel);
		settingsPane.add(geneticThresTextField);
		settingsPane.add(clusterThresLabel);
		settingsPane.add(largeClusterThresTextField);
		*/
		
		
		GroupLayout layout = new GroupLayout(settingsPane);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup( 
				layout.createSequentialGroup()
					.addGroup( layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
							.addComponent(initThresLabel)
							.addComponent(supportThresLabel)
							.addComponent(geneticThresLabel)
							.addComponent(clusterThresLabel) )
					.addGroup( layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(initThresTextField)
							.addComponent(supportThresTextField)
							.addComponent(geneticThresTextField)
							.addComponent(largeClusterThresTextField) )
				);
		layout.setVerticalGroup( 
				layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(initThresLabel)
							.addComponent(initThresTextField) )
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(supportThresLabel)
							.addComponent(supportThresTextField) )
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(geneticThresLabel)
							.addComponent(geneticThresTextField) )
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(clusterThresLabel)
							.addComponent(largeClusterThresTextField) )
				);

		settingsPane.setLayout(layout);
		
		mainContainer.add(settingsPane);
		
	}

	void makeResultsPane(Container mainContainer) {
		
		ActionListener showActionListener = new ShowMissingActionListener();
		
		////////////////////////////////////////////////////////////////////////////////
		// results panel
		
		JPanel resultsPane = new JPanel();
		resultsPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		resultsPane.setBorder(BorderFactory.createTitledBorder("Results"));
		
		/*
		GridLayout resultsGrid = new GridLayout(0,4);
		resultsGrid.setHgap(5);
		resultsPane.setLayout(resultsGrid);
		*/
		//Dimension paramBoxSize = new Dimension(50, 0);
		
		JLabel numSeqsLabel = new JLabel("Number of Sequences =");
		numSeqsLabel.setMinimumSize(labelBoxSize);
		numSeqsLabel.setHorizontalAlignment(JLabel.RIGHT);
		numSeqsLabel.setVerticalAlignment(JLabel.CENTER);	
					
		numSeqs 			= new JFormattedTextField();
		numSeqs.setMinimumSize(paramBoxSize);
		
		JLabel missSeqsLabel = new JLabel("Sequences with no tips =");
		missSeqsLabel.setMinimumSize(labelBoxSize);
		missSeqsLabel.setHorizontalAlignment(JLabel.RIGHT);
		missSeqsLabel.setVerticalAlignment(JLabel.CENTER);
		
		numMissSeqs			= new JFormattedTextField();
		numMissSeqs.setMinimumSize(paramBoxSize);

		/*
		missingSeqsText 	= new JTextArea(3, 15);
		JPanel 	mspane 	 	= new JPanel();
		//missingSeqsText.setLineWrap(true);
		//missingSeqsText.setWrapStyleWord(true);
		JScrollPane	Sscroll	 = new JScrollPane(missingSeqsText, 	ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
																	ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		mspane.add( Sscroll );
		*/
		
		missSeqsShow = new JButton("Show");
		missSeqsShow.addActionListener(showActionListener);

		JLabel numTipsLabel = new JLabel("Number of Tips =");
		numTipsLabel.setMinimumSize(labelBoxSize);
		numTipsLabel.setHorizontalAlignment(JLabel.RIGHT);
		numTipsLabel.setVerticalAlignment(JLabel.CENTER);	
					
		numTips 			= new JFormattedTextField();
		numTips.setMinimumSize(paramBoxSize);
		
		JLabel missTipsLabel = new JLabel("Tips with no sequences =");
		missTipsLabel.setMinimumSize(labelBoxSize);
		missTipsLabel.setHorizontalAlignment(JLabel.RIGHT);
		missTipsLabel.setVerticalAlignment(JLabel.CENTER);
		
		numMissTips			= new JFormattedTextField();
		numMissTips.setMinimumSize(paramBoxSize);
		
		/*
		missingTipsText		= new JTextArea(3, 15);
		JPanel mtpane		= new JPanel();
		//missingTipsText.setLineWrap(true);
		//missingTipsText.setWrapStyleWord(true);
		JScrollPane	Tscroll	 = new JScrollPane(missingTipsText, 	ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
																	ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		mtpane.add( Tscroll );
		*/

		missTipsShow = new JButton("Show");
		missTipsShow.addActionListener(showActionListener);
		
		
		JLabel numClustersLabel			= new JLabel("Number of clusters =");
		numClustersLabel.setMinimumSize(labelBoxSize);
		numClustersLabel.setHorizontalAlignment(JLabel.RIGHT);
		numClustersLabel.setVerticalAlignment(JLabel.CENTER);
		
		numClusters						= new JFormattedTextField();
		numClusters.setMinimumSize(paramBoxSize);
		
		JLabel numLargeClustersLabel	= new JLabel("Number of large clusters =");
		numLargeClustersLabel.setMinimumSize(labelBoxSize);
		numLargeClustersLabel.setHorizontalAlignment(JLabel.RIGHT);
		numLargeClustersLabel.setVerticalAlignment(JLabel.CENTER);
		
		numLargeClusters				= new JFormattedTextField();
		numLargeClusters.setMinimumSize(paramBoxSize);
		
		GroupLayout layout = new GroupLayout(resultsPane);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup( 
				layout.createSequentialGroup()
					.addGroup( layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
							.addComponent(numSeqsLabel)
							.addComponent(missSeqsLabel) 
							.addComponent(numClustersLabel)
							)
					.addGroup( layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(numSeqs)
							.addComponent(numMissSeqs)
							.addComponent(numClusters)
							//.addComponent(missSeqsShow)
							//.addComponent(mspane) 
							)
					.addGroup( layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(missSeqsShow)
							)
					.addGroup( layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
							.addComponent(numTipsLabel)
							.addComponent(missTipsLabel) 
							.addComponent(numLargeClustersLabel)
							)
					.addGroup( layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(numTips)
							.addComponent(numMissTips)
							.addComponent(numLargeClusters)
							//.addComponent(missTipsShow)
							//.addComponent(mtpane) 
							)	
					.addGroup( layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(missTipsShow)
							)
				);
		layout.setVerticalGroup( 
				layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(numSeqsLabel)
							.addComponent(numSeqs)
							.addComponent(numTipsLabel)
							.addComponent(numTips) 
							)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(missSeqsLabel)
							//.addComponent(mspane)
							.addComponent(numMissSeqs)
							.addComponent(missSeqsShow)
							.addComponent(missTipsLabel)
							.addComponent(numMissTips)
							//.addComponent(mtpane) )
							.addComponent(missTipsShow)
							)
					//.addGroup( layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					//		.addComponent(missSeqsShow)
					//		.addComponent(missTipsShow) 
					//		)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(numClustersLabel)
							.addComponent(numClusters)
							.addComponent(numLargeClustersLabel)
							.addComponent(numLargeClusters)
							)
				);

		resultsPane.setLayout(layout);

		mainContainer.add(resultsPane);

	}
	
	void makeMessagesPane(Container mainContainer) {
		JPanel messagesPane	= new JPanel( );
		messagesPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		messagesPane.setBorder(BorderFactory.createTitledBorder("Messages"));
		messagesPane.setLayout(new BoxLayout(messagesPane, BoxLayout.Y_AXIS));
		messages			= new JTextArea( 7, 0 );
		messages.setLineWrap(true);
		messages.setWrapStyleWord(true);
		JScrollPane	scroll	 = new JScrollPane(messages, 	ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
															ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		messagesPane.add( scroll );
		mainContainer.add(messagesPane);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////
	// data loading methods
	
	void loadSequences() {
		
		if (sequenceFile != null) {
			cp.readSequences(sequenceFile.getAbsolutePath());
			numSeqs.setValue(cp.seqNames.size());
			numClusters.setValue(null);
			numLargeClusters.setValue(null);
			
			/*
			if (cp.seqNames.size() <= pairwiseThres) {
				cp.storePairwise = true;
				System.out.println("Store pairwise = true");
			} else {
				cp.storePairwise = false;
				System.out.println("Store pairwise = false");
			}
			*/
		} else {
			messages.append("Please select a fasta sequence file\n");
		}
		
	}
	
	void loadTree() {
		
		if (sequenceFile == null) {
			messages.append("Please select a fasta sequence file first\n");
		} else {
			if (treeFile != null) {
				cp.readTree(treeFile.getAbsolutePath());
				numTips.setValue(cp.theTree.tipNames().size());
				numClusters.setValue(null);
				numLargeClusters.setValue(null);
			} else {
				messages.append("Please select a newick tree file\n");
			}	
		}
	}
	
	void checkMissing() {
		
		if ( ( numSeqs.getValue() != null ) && (numTips.getValue() != null) ) {
			if ( (cp.theTree != null)) {
				numMissSeqs.setValue(cp.numMissingSeqs());
				numMissTips.setValue(cp.numMissingTips());
				missingSeqsText.setText(cp.missingSeqs());
				missingTipsText.setText(cp.missingTips());
				
				if ( (Integer)numMissSeqs.getValue() > 0) {
					numMissSeqs.setForeground(Color.RED);
					//numMissSeqs.setBackground(Color.RED);
				} else {
					numMissSeqs.setForeground(Color.BLACK);
					//numMissSeqs.setBackground(Color.BLACK);
				}
				
				if ( (Integer)numMissTips.getValue() > 0) {
					numMissTips.setForeground(Color.RED);
				} else {
					numMissTips.setForeground(Color.BLACK);
				}
				
				
			}
		}
		
	}
	

	boolean parametersChanged() {
		System.out.print("Parameters changed ? ");
		if (reprocess) {
			System.out.println("TRUE");
		} else {
			System.out.println("FALSE");
		}
		return reprocess;
		
		// set to always reprocess for now
		// this is because the tree names are perm renamed on writing the results
		// will correct when use jebl
		// return true;
	}
	
	void processData() {
		
		double startTime = System.currentTimeMillis();
		
		messages.setText("-- Processing --\n");
		messages.setVisible(true);
		messages.validate();
		
		// dont need to do this now because cp is initialised and reset on parameter change
		/*
		cp.setInitialSupportThres(initialSupportThres);
		cp.setSupportThres(supportThres);
		cp.setGeneticThres( geneticThres / 100.0 );
		cp.setLargeClusterThreshold(largeClusterThres);
		*/

		if ( (cp.seqNames != null) && (cp.theTree != null) ) {
			
			// 4 July 2012
			// unfortunately, the sequences and tree have to be re-read in on the 2nd go
			// otherwise the cluster picker doesnt work properly
			// (currently it messes up the tree as it processes it)
			
			
			// if any of the parameters, apart from large cluster threshold
			// have been changed then do all of data processing
			if (parametersChanged()) {
			
				loadSequences();
				loadTree();
				checkMissing();
				cp.processData();
				
				// set reprocess to false, if parameters changed then will -> true for next time
				// except for large cluster threshold
				reprocess = false;
			} else {
				messages.append("Just recalculate the number of large clusters\n");
			}
			
			numClusters.setValue( cp.numberOfClusters() );
			numLargeClusters.setValue( cp.numberOfLargeClusters() );
			
			String txt = cp.writeResults();
			messages.append(txt);
			
		} else {
			messages.setText("Sorry couldnt process data, try resetting sequences and tree files\n");
		}
		
		double stopTime = System.currentTimeMillis();
		
		messages.append("-- Completed in "+(stopTime-startTime)/1000+" seconds\n");
		messages.validate();
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////
	// action listeners
	
	/**
	 * SetFileListener for sequence and tree file names fields
	 * @author sam
	 *
	 */
	class SetFileListener implements MouseListener {
		
		void setFile(MouseEvent arg0) {
			
			Object source = arg0.getSource();
			
			FileNameExtensionFilter filter;
			JFileChooser fileChooser = new JFileChooser(); 
			
			if (source == sequenceTextField) {
				filter 	 = new FileNameExtensionFilter("Fasta Format", "fas", "fasta", "fst");
				
				if (sequenceFile != null) {
					// if sequences file previously set then go from that directory
					fileChooser = new JFileChooser(sequenceFile);
				} else if (treeFile != null) {
					// if tree file previously set then go from that directory
					fileChooser = new JFileChooser(treeFile);
				}
				
			} else if ( source == treeTextField ) {
				filter = new FileNameExtensionFilter("Newick Format", "nwk", "newick");
				
				if (treeFile != null) {
					// if tree file previously set then go from that directory
					fileChooser = new JFileChooser(treeFile);
				} else if (sequenceFile != null) {
					// if sequences file previously set then go from that directory
					fileChooser = new JFileChooser(sequenceFile);
				}
				
			} else {
				filter = new FileNameExtensionFilter("Text format", "txt");
			}
			
			fileChooser.setFileFilter(filter);
			
			int returnVal = fileChooser.showOpenDialog(frame);

			if (returnVal != JFileChooser.APPROVE_OPTION) {
				return;  // cancelled
				
			} else {

				reprocess = true;
				
				if (source == sequenceTextField ) {

					messages.append("Loaded sequences file\n");
					
					sequenceFile = fileChooser.getSelectedFile();
					sequenceTextField.setText(sequenceFile.getName());
					
					loadSequences();
					checkMissing();
					
				} else if (source == treeTextField ) {
					
					messages.append("Loaded tree file\n");
					
					treeFile = fileChooser.getSelectedFile();
					treeTextField.setText(treeFile.getName());
					
					loadTree();
					checkMissing();
					
				}
				
			 }
		}
		

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			setFile(arg0);
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 * GOActionListener for main processing
	 * @author Samantha Lycett
	 *
	 */
	class GOActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			processData();
		}
		
	}
	
	/**
	 * sets internal parameters in GUI and underlying cluster picker class
	 * note that genetic thres is divided by 100 when setting underlying cluster picker class
	 * @author Samantha Lycett
	 *
	 */
	class ParameterChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			Object source = arg0.getSource();
			
		    if (source == initThresTextField) {
		        initialSupportThres = ((Number)initThresTextField.getValue()).doubleValue();
		        cp.setInitialSupportThres(initialSupportThres);
		        messages.append("Initial Threshold = "+initialSupportThres+"\n");
		        reprocess 	= true;
		        numClusters.setValue(null);
				numLargeClusters.setValue(null);
		        
		    } else if (source == supportThresTextField) {
		        supportThres = ((Number)supportThresTextField.getValue()).doubleValue();
		        cp.setSupportThres(supportThres);
		        messages.append("Main Support Threshold = "+supportThres+"\n");
		        reprocess 	= true;
		        numClusters.setValue(null);
				numLargeClusters.setValue(null);
		        
		    } else if (source == geneticThresTextField) {
		    	geneticThres = ((Number)geneticThresTextField.getValue()).doubleValue();
		    	cp.setGeneticThres(geneticThres / 100.0);
		    	messages.append("Genetic Distance Threshold = "+geneticThres+"\n");
		    	reprocess	= true;
		    	numClusters.setValue(null);
				numLargeClusters.setValue(null);
		    	
		    } else if (source == largeClusterThresTextField) {
		    	largeClusterThres = ((Number)largeClusterThresTextField.getValue()).intValue();
		    	cp.setLargeClusterThreshold(largeClusterThres);
		    	messages.append("Large Cluster Threshold = "+largeClusterThres+"\n");
		    	numLargeClusters.setValue(null);
		    	messages.append("Press GO to write new large clusters to files\n");
		    	
		    	// do not need to reprocess
		    	// only number of large clusters will change, not number of clusters
		    }

		}
		
	}
	
	
	class ShowMissingActionListener implements ActionListener {

		void makePopListFrame(String popName, JTextArea textarea) {
			JFrame popframe = new JFrame( popName );
			popframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			//frame.setMinimumSize(new Dimension(600,300));
			//frame.setPreferredSize(frame.getMinimumSize());

			//makeMenuBar();
		
			Container contentPane	= popframe.getContentPane();
			contentPane.setLayout(new BorderLayout());
			JLabel titleLabel		= new JLabel(popName);
			titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
			titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
			contentPane.add(titleLabel, BorderLayout.NORTH);
			
			JPanel 		pane 	 	= new JPanel();
			pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
			//textarea.setLineWrap(true);
			//textarea.setWrapStyleWord(true);
			
			JScrollPane	scroll	 	= new JScrollPane(textarea, 	ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
																	ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			pane.add( scroll );
			contentPane.add(pane, BorderLayout.CENTER);
		
			popframe.pack();
			popframe.setVisible(true);
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			String popName		= "x";
			JTextArea textarea	= null;
			
			if (arg0.getSource() == missSeqsShow ) {
				//missingSeqsText 	= new JTextArea(0, 15);	
				textarea			= missingSeqsText;
				popName			    = "Sequences with no tips";
			} else if (arg0.getSource() == missTipsShow ) {
				//missingTipsText 	= new JTextArea(0, 15);	
				textarea			= missingTipsText;
				popName			    = "Tips with no sequences";
			}
			
			makePopListFrame(popName, textarea);
		}
		
	}
	
	
	class ShowAboutActionListener implements ActionListener {
		
		/*
		JLabel thisLabel(String txt) {
			JLabel aLabel = new JLabel(txt);
			aLabel.setHorizontalAlignment(SwingConstants.CENTER);
			aLabel.setVerticalAlignment(SwingConstants.CENTER);
			aLabel.setAlignmentY(JLabel.CENTER);
			aLabel.setMinimumSize(labelBoxSize);
			return aLabel;
		}
		*/
		
		public void actionPerformed(ActionEvent arg0) {
			

			//String txt  = appName+"\nVersion "+version+"\n"+author+"\n"+institute+"\n\n"+citation+"\n\n";
			
			Container pane = new Container();
			//pane.setLayout(new GridLayout(4,2));
			
			JLabel v1 = new JLabel("Version:");
			JLabel v2 = new JLabel(version);
			
			JLabel a1 = new JLabel("Author:");
			JLabel a2 = new JLabel(author);
			
			JLabel i1 = new JLabel("Institute:");
			JLabel i2 = new JLabel(institute);
			
			JLabel c1 = new JLabel("Citation:");
			JLabel c2 = new JLabel(citation);
			
			JLabel u1 = new JLabel("URL:");
			JLabel u2 = new JLabel(url);

			JLabel l1 = new JLabel("License:");
			JLabel l2 = new JLabel(license);
			
			GroupLayout layout = new GroupLayout(pane);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);

			layout.setHorizontalGroup( 
					layout.createSequentialGroup()
						.addGroup( layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
								.addComponent(v1)
								.addComponent(a1)
								.addComponent(i1)
								.addComponent(c1)
								.addComponent(u1)
								.addComponent(l1)
								)
						.addGroup( layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(v2)
								.addComponent(a2)
								.addComponent(i2)
								.addComponent(c2)
								.addComponent(u2)
								.addComponent(l2)		
								 )
					);
			layout.setVerticalGroup( 
					layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(v1)
								.addComponent(v2) )
								
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(a1)
								.addComponent(a2) )
								
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(i1)
								.addComponent(i2) )
								
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(c1)
								.addComponent(c2) )
								
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(u1)
								.addComponent(u2) )
								
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(l1)
								.addComponent(l2) )
								
					);
			
			pane.setLayout(layout);
			
			//JLabel Jtxt = new JLabel(txt);
			//Jtxt.setHorizontalAlignment(JLabel.CENTER);
			
			/*
			JFrame popframe = new JFrame( "About" );
			popframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			
			Container mainPane = popframe.getContentPane();
			JPanel pane = new JPanel();
			pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
			
			pane.add(thisLabel(appName));
			pane.add(thisLabel("Version "+version));
			pane.add(thisLabel(author));
			pane.add(thisLabel(institute));
			pane.add(thisLabel(citation));
			
			mainPane.add(pane);

			popframe.validate();
			popframe.setVisible(true);
			popframe.pack();
			*/
			
			
			JOptionPane.showMessageDialog(frame, 
					pane,
					"About "+appName, 
                    JOptionPane.PLAIN_MESSAGE);
            
		}
		
	}
	
	class ShowInstructionsActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			StringBuffer txt = new StringBuffer();
			txt.append(appName+" Instructions\n");
			txt.append("\n1. Load sequences file\n");
			txt.append("- Choose a fasta format sequences file, e.g. with extension *.fas, *.fasta, or *.fst\n");
			txt.append("\n2. Load tree file\n");
			txt.append("- Choose a newick format tree file, e.g. with extension *.nwk or *.newick\n");
			txt.append("- note that this tree must have node support values\n");
			txt.append("- the sequence names and tip names in the tree should be identical for this analysis\n");
			txt.append("\n3. Choose settings\n");
			txt.append("- the Initial threshold and Main Support Threshold default to 0.9\n");
			txt.append("- but if the tree has bootstrap values 0-100, then you must change the thresholds accordingly (e.g. 90)\n");
			txt.append("- the Genetic distance threshold defaults to 4.5%\n");
			txt.append("- the names of sequences in clusters >= large cluster threshold are written to separate files\n");
			txt.append("- but all cluster memberships are recorded in the log file\n");
			txt.append("\n4. Press GO to perform analysis and write results files\n\n");
			
			JOptionPane.showMessageDialog(frame, 
					txt.toString(),
					appName+" Instructions", 
                    JOptionPane.PLAIN_MESSAGE);
		}
		
	}
	
	// not using options menu as not using storePairwise
	/*
	class AdvancedSettingsActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			//JFrame popframe = new JFrame( "Advanced Settings" );
			//popframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			//Container mainPane = popframe.getContentPane();
			JPanel pane = new JPanel();
			//pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
			pane.setLayout(new BorderLayout());
			
			JLabel titleLabel = new JLabel("Advanced Settings");
			titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
			titleLabel.setVerticalAlignment(SwingConstants.CENTER);
			titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
			//pane.add(titleLabel);
			pane.add(titleLabel, BorderLayout.NORTH);
			
			Container buttonContainer = new Container();
			buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.Y_AXIS));
			buttonContainer.add(Box.createRigidArea(new Dimension(0,10)));
			buttonContainer.add(new JLabel("Store pairwise distance results ?"));
			
			System.out.println("Pairwise threshold = "+pairwiseThres);
			
			//Create the radio buttons.
		    JRadioButton sometimesButton = new JRadioButton("If number of sequences <= "+defaultPairwiseThres);
		    sometimesButton.setActionCommand("threshold");
		    
		    JRadioButton neverButton = new JRadioButton("Never");
		    neverButton.setActionCommand("never");
			
		    JRadioButton alwaysButton = new JRadioButton("Always (might run out of memory)");
		    alwaysButton.setActionCommand("always");
		    
		    if (pairwiseThres == defaultPairwiseThres) {
		    	sometimesButton.setSelected(true);
		    } else if (pairwiseThres < 0) {
		    	neverButton.setSelected(true);
		    } else {
		    	alwaysButton.setSelected(true);
		    }
		    
		    //Group the radio buttons.
		    ButtonGroup group = new ButtonGroup();
		    group.add(sometimesButton);
		    group.add(neverButton);
		    group.add(alwaysButton);
		    
		    //Register a listener for the radio buttons.
		    sometimesButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					pairwiseThres = defaultPairwiseThres;
					//System.out.println("PairwiseThres = "+pairwiseThres);
				}
		    });
		    
		    neverButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					pairwiseThres = -1;	
					//System.out.println("PairwiseThres = "+pairwiseThres);
				}
		    });
		    
		    alwaysButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					pairwiseThres = Integer.MAX_VALUE;
					//System.out.println("PairwiseThres = "+pairwiseThres);
				}
		    });
		    
		    
		    buttonContainer.add(sometimesButton);
		    buttonContainer.add(neverButton);
		    buttonContainer.add(alwaysButton);
		    
		    pane.add(buttonContainer, BorderLayout.CENTER);
		    
			//mainPane.add(pane);
			//popframe.pack();
			//popframe.setVisible(true);
			
			JOptionPane.showMessageDialog(frame, 
					pane,
					appName+" Advanced Settings", 
                    JOptionPane.PLAIN_MESSAGE);
			
		}
		
	}
	*/
	
	////////////////////////////////////////////////////////////////////////////////////////
	// Main Method

	private static void createAndShowGUI() {
		new ClusterPickerGUI();
	}

	public static void main(String[] args) {

		System.out.println("** START ClusterPickerGUI **");

		//  http://docs.oracle.com/javase/tutorial/uiswing/painting/step1.html
		//  Note that when programming in Swing, 
		//  your GUI creation code should be placed on the Event Dispatch Thread (EDT). 
		//  This will prevent potential race conditions that could lead to deadlock
		
		System.out.println("ClusterPicker Copyright (C) 2013 Samantha Lycett");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY");
		System.out.println("This is free software, and you are welcome to redistribute it under certain conditions");
		System.out.println("See GNU GPLv3 for details http://www.gnu.org/licenses/gpl-3.0.txt");
		System.out.println("Project home page (and tutorials): http://hiv.bio.ed.ac.uk/software.html");

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});

		System.out.println("** END **");
	}
	
	
}
