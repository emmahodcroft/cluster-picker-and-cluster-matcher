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

/*
 *
 * Class Overview: Wind is a class the creates and adds the GUI widgets
 * to be used in the program to allow the user to load a file containing a tree
 * (or trees) and other files, and also control the other
 * functions of the program (such as adding and deleting new nodes).
 *
 */

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class Wind extends JFrame
{
    // Allows consule debug messages to be turned on (true) or off(false)
    final static boolean verbose = false;
    // Shows the cheat button (fills the GUI in with hard-coded file names) if TRUE, hides it if false
    final static boolean cheating = false;

    //shows whether the option pane or the file pane are disabled (greyed out) - the option panel is disabled
    //before files are loaded, the file panel is disabled after the files are loaded
    boolean optPaneDisabled = false;
    boolean filePaneDisabled = false;

    //JPanel mainP = new JPanel(new BorderLayout());
    JPanel mainP = new JPanel();
    
    //top panel
    JPanel topP = new JPanel(new BorderLayout());
        JPanel titleP = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JLabel title = new JLabel("Cluster Matcher");
            Font f = new Font("f", Font.PLAIN, 18);
            
        JPanel fileP = new JPanel(new BorderLayout());
            JPanel numFileP = new JPanel(new GridLayout(1,2));
                ButtonGroup numFileBG = new ButtonGroup();
                JPanel f1RBP = new JPanel();
                    JRadioButton file1RB = new JRadioButton("1 data set");
                JPanel f2RBP = new JPanel();
                    JRadioButton file2RB = new JRadioButton("2 data sets");
            JPanel treeFileP = new JPanel(new BorderLayout());
                JPanel treeFileP1 = new JPanel(new GridLayout(3,1));
                    JPanel ds1LP = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        JLabel ds1L = new JLabel("Data set 1:");
                    JPanel ds1newP = new JPanel(new FlowLayout());
                        JLabel newDs1L = new JLabel("Newick file: ");
                        JTextField newDs1T = new JTextField(18);
                    JPanel ds1clusP = new JPanel(new FlowLayout());
                        JCheckBox anotDs1CB = new JCheckBox("Annotation file:");
                        JTextField anotDs1T = new JTextField(18);

                JPanel fakoP = new JPanel();
                    JLabel fakoL = new JLabel("   ");
                JPanel treeFileP2 = new JPanel(new GridLayout(3,1));
                    JPanel ds2LP = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        JLabel ds2L = new JLabel("Data set 2:");
                    JPanel ds2newP = new JPanel(new FlowLayout());
                        JLabel newDs2L = new JLabel("Newick file: ");
                        JTextField newDs2T = new JTextField(18);
                    JPanel ds2clusP = new JPanel(new FlowLayout());

                    // SJL 11 June 2013 - trying to make work in Eclipse
                    //JCheckBox anotDs2CB = new JCheckBox("Annotation file:", new ImageIcon(getClass().getResource("invisBox.jpg")));
                    JCheckBox anotDs2CB = new JCheckBox("Annotation file:", new ImageIcon("src\\invisBox.jpg") );
                    
                    JTextField anotDs2T = new JTextField(18);

            JPanel matchAP = new JPanel();
            JPanel matchAnnotP = new JPanel();
            JPanel matExP = new JPanel(new FlowLayout());
                JPanel matchP = new JPanel(new GridLayout(2,1));
                    JLabel matFL = new JLabel("Matches File: ");
                    JTextField matFT = new JTextField(18);
                    JButton matHelpB = new JButton("?");
                    
                    //JCheckBox sameMatCB = new JCheckBox("Sequence names are the same in both data sets.", new ImageIcon("src\\invisBox.jpg"));
                    
                    // SJL 11 June 2013 - trying to make work in Eclipse
                    //JCheckBox sameMatCB = new JCheckBox("Sequence names are the same in both data sets.", new ImageIcon(getClass().getResource("invisBox.jpg")));
                    
                    JCheckBox sameMatCB = new JCheckBox("Sequence names are the same in both data sets.", new ImageIcon("src\\invisBox.jpg"));
                    
                    
                    JButton sameMatHelpB = new JButton("?");
                //JPanel annotP = new JPanel(new FlowLayout());
                    JCheckBox annotCB = new JCheckBox("Annotation File:");
 JButton cheat = new JButton ("cheat!");
 JButton cheat2 = new JButton ("cheat2!");
 JLabel reading = new JLabel("Reading...");
                    JTextField annotT = new JTextField(18);
        JPanel readFP = new JPanel(new FlowLayout());
            JButton readFileB = new JButton("Read Files");

                JPanel treeFileP2F = new JPanel(new GridLayout(3,1));
                    JPanel ds2LPF = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        JLabel ds2LF = new JLabel("Data set 2:");
                    JPanel ds2newPF = new JPanel(new FlowLayout());
                        JLabel newDs2LF = new JLabel("Newick file: ");
                        JTextField newDs2TF = new JTextField(18);
                    JPanel ds2clusPF = new JPanel(new FlowLayout());
                        JLabel clusDs2LF = new JLabel("Cluster file: ");
                        JTextField clusDs2TF = new JTextField(18);

        JPanel filePane = new JPanel();
        JPanel optPane = new JPanel();
        JPanel outPane = new JPanel();
        JPanel filePane1 = new JPanel();
        JPanel filePane2 = new JPanel();

        JPanel outFileP = new JPanel();
        JLabel outFileL = new JLabel("Output: ");
        JTextField outFileT = new JTextField(18);
        JButton outHelpB = new JButton("About the Output");
        JPanel outHelpP = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JPanel returnP = new JPanel();
        JLabel return1L = new JLabel("Return only clusters with more than ");
        JTextField return1T = new JTextField(3);
        JLabel return2La = new JLabel(" sequences");
        JLabel return2Lb = new JLabel("that match between datasets");

        JPanel percP = new JPanel(new BorderLayout());
        JPanel percP1 = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
            JPanel percP1a = new JPanel(new GridLayout(1,2));
            JPanel percP1b = new JPanel(new FlowLayout(FlowLayout.LEFT,3,2));
            JPanel percP1c = new JPanel(new FlowLayout(FlowLayout.LEFT,3,0));
        JPanel percP2 = new JPanel(new GridLayout(1,2));
            JPanel percP3 = new JPanel(new FlowLayout(FlowLayout.RIGHT,3,0));
            JPanel percP4 = new JPanel(new FlowLayout(FlowLayout.LEFT,3,0));

        JCheckBox percCB = new JCheckBox("and which at least ");
        JTextField percT = new JTextField(3);
        JLabel perc2L = new JLabel("% of the sequences have a ");
        JLabel perc2La = new JLabel("have a");
        JLabel fakeo = new JLabel("blahblahblahblahblah");
        JLabel fakeob = new JLabel("Fakeoooo");

        JComboBox fieldC = new JComboBox();
        JLabel perc3L = new JLabel(" value of ");

        JComboBox valueC = new JComboBox();
        JLabel perc4L = new JLabel(" in ");
String[] dataSets = {"Data Set 1", "Data Set 2", "Both"};
        JPanel percHelpBP = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        JButton percHelpB = new JButton("?");
        JComboBox dsC = new JComboBox(dataSets);

        JPanel incP = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
            //JCheckBox incCB = new JCheckBox("Out of only sequences with a value for this field");
            JCheckBox incCB = new JCheckBox("Out of all sequences in the cluster, including those with no value for this field");
            JButton incB = new JButton("?");

        JPanel anotFP = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
            JCheckBox anotFCB = new JCheckBox("Embed annotations in the FigTree files");
            JButton anotFB = new JButton("?");

        JPanel extendInfP = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
            JCheckBox extendInfCB = new JCheckBox("Print a .csv file with extended information about the clusters");
            JButton extendInfB = new JButton("?");
            JButton extendInfBPrint = new JButton("Print just .csv file");

        JPanel previewP = new JPanel();
            JButton prevB = new JButton ("Preview");
            JTextField prevT = new JTextField(3);
            JLabel prevL = new JLabel(" files will be returned");
            JButton prevHelpB = new JButton("?");
        JPanel runP = new JPanel();
            JButton runB = new JButton("Produce FigTree Files");
        JPanel runProgP = new JPanel();
            JLabel runProgL = new JLabel("Writing file 1 of 11...");


    JPanel bottomP = new JPanel(new BorderLayout());
        JPanel bottomP1 = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        JPanel bottomP2 = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
            JLabel bottomL = new JLabel("Copyright 2013 - Emma Hodcroft, Leigh Brown Group, University of Edinburgh");
            JLabel bottomL2 = new JLabel("Licensed under GNU GPLv3"); 
            Font f2 = new Font("f", Font.PLAIN, 10);

    //Class Constructor
    public Wind()
    {
        if(verbose)
            System.out.println("Window instance created");
        setLayout(new BorderLayout());


                    f1RBP.add(file1RB);
                    f2RBP.add(file2RB);
                    numFileBG.add(file1RB);
                    numFileBG.add(file2RB);

                    newDs1L.setLabelFor(newDs1T);
                    newDs2L.setLabelFor(newDs2T);

                    
                    GridBagLayout gridbag0 = new GridBagLayout();
                    GridBagConstraints c0 = new GridBagConstraints();
                    filePane.setLayout(gridbag0);
                    optPane.setLayout(gridbag0);
                    outPane.setLayout(gridbag0);

                    GridBagLayout gridbag = new GridBagLayout();
                    GridBagConstraints c = new GridBagConstraints();
                    GridBagLayout gridbag2 = new GridBagLayout();
                    GridBagConstraints c2 = new GridBagConstraints();

                    filePane1.setLayout(gridbag);
                    filePane2.setLayout(gridbag2);

                    JLabel[] labels = {newDs1L};
                    JTextField[] textFields = {newDs1T};
                    JLabel[] labels2 = {newDs2L};
                    JTextField[] textFields2 = {newDs2T};

                    addLabelTextRows(labels, textFields, gridbag, filePane1);
                    addLabelTextRows(labels2, textFields2, gridbag2, filePane2);

                    GridBagConstraints temp = new GridBagConstraints();
                    temp.anchor = GridBagConstraints.EAST;
                    temp.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
                    temp.fill = GridBagConstraints.NONE;      //reset to default
                    temp.weightx = 0.0;                       //reset to default
                    filePane1.add(anotDs1CB, temp);
                        anotDs1CB.setForeground(new Color(153,153,153));
                    filePane2.add(anotDs2CB, temp);

                    temp.gridwidth = GridBagConstraints.REMAINDER;     //end row
                    temp.fill = GridBagConstraints.HORIZONTAL;
                    temp.weightx = 1.0;
                    filePane1.add(anotDs1T, temp);
                        anotDs1T.setEditable(false);
                    filePane2.add(anotDs2T, temp);


                    c2.gridwidth = GridBagConstraints.REMAINDER; //last
                    c2.anchor = GridBagConstraints.WEST;
                    c2.weightx = 1.0;

                    c.gridwidth = GridBagConstraints.REMAINDER; //last
                    c.anchor = GridBagConstraints.WEST;
                    c.weightx = 1.0;

                    filePane1.setBorder(
                            BorderFactory.createCompoundBorder(
                                            BorderFactory.createTitledBorder("Data Set 1"),
                                            BorderFactory.createEmptyBorder(5,5,5,5)));
                    filePane2.setBorder(
                            BorderFactory.createCompoundBorder(
                                            BorderFactory.createTitledBorder("Data Set 2"),
                                            BorderFactory.createEmptyBorder(5,5,5,5)));

                    c0.fill = GridBagConstraints.HORIZONTAL;
                    c0.gridx = 0;
                    c0.gridy = 0;
                    filePane.add(f1RBP, c0);
                    c0.gridx = 1;
                    c0.gridy = 0;
                    filePane.add(f2RBP, c0);
                    c0.fill = GridBagConstraints.HORIZONTAL;
                    c0.gridx = 0;
                    c0.gridy = 1;
                    filePane.add(filePane1, c0);
                    c0.fill = GridBagConstraints.HORIZONTAL;
                    c0.gridx = 1;
                    c0.gridy = 1;
                    filePane.add(filePane2, c0);

        matchAnnotP.setLayout(gridbag);
        JLabel[] lbl = {matFL};
        JTextField[] txtF = {matFT};
        addLabelTextRows(lbl, txtF, gridbag, matchAnnotP);
        temp = new GridBagConstraints();
        //temp.anchor = GridBagConstraints.EAST;
        temp.gridwidth = GridBagConstraints.REMAINDER;
	temp.fill = GridBagConstraints.HORIZONTAL;
	temp.weightx = 1.0;
        matchAnnotP.add(sameMatCB, temp);
            sameMatCB.setForeground(new Color(153,153,153));

 /*                   matchAnnotP.setLayout(gridbag);
                    matFL.setLabelFor(matFT);
                        annotT.setEditable(false);
                        annotCB.setForeground(Color.GRAY);
                    JTextField[] tf= {matFT};//, annotT};

                    for (int i = 0; i < 1; i++)
                   // for (int i = 0; i< 2; i++)
                    {
                        c.anchor = GridBagConstraints.WEST;
                        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
                        c.fill = GridBagConstraints.NONE;      //reset to default
                        c.weightx = 0.0;                       //reset to default
               // c.gridx = 0;
                        if(i == 0)
                        {
               // c.gridy = 0;
                            c.anchor = GridBagConstraints.EAST;
                            matchAnnotP.add(matFL, c);
                        }
//                else
//                {
//                c.gridy = 1;
//                c.anchor = GridBagConstraints.EAST;
//                matchAnnotP.add(sameMatCB, c);
//                }

                        c.weightx = 1.0;
    //            if(i == 0)
    //            {
                //c.anchor = GridBagConstraints.LINE_END;
    //            c.anchor = GridBagConstraints.EAST;
                        matchAnnotP.add(tf[i], c);
    //                    }
                        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
                        c.fill = GridBagConstraints.HORIZONTAL;

                    }
   
  */
            //in matchP, with gridLayout - matches File label seperates from textbox:
            //matchP.add(matchAnnotP);
            //matchP.add(sameMatCB);

            //part in matExP, flowlayout - matches files close to textbox, but vertically far away from CB
            //matExP.add(matchAnnotP);
            //matchP.add(matExP);
            //matchP.add(sameMatCB);

            //in matExP, flowlayout
            //matExP.add(matchAnnotP);
            //matExP.add(sameMatCB);
            //matExP.setBorder(BorderFactory.createEmptyBorder(0,100,0,0));
            //matchAP.setLayout(new BorderLayout());
            //matchAP.add(matExP, BorderLayout.CENTER);

               //matchAP.setLayout(new BorderLayout());
               //matchAP.add(matchP, BorderLayout.CENTER);

               //matchAP.add(matHelpB, BorderLayout.EAST);
                    matchAP.add(matchAnnotP);
                    matchAP.add(matHelpB);
                        matHelpB.setMargin(new Insets(0,0,0,0));
                        matHelpB.setForeground(Color.BLUE);
                    matchAP.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

                    c0.fill = GridBagConstraints.NONE;
                    c0.gridx = 0;
                    c0.gridy = 2;
                    c0.gridwidth = 2;
                    filePane.add(matchAP,c0);
                    
                    c0.gridy = 3;
                    filePane.add(readFileB, c0);

                    filePane.setBorder(
                            BorderFactory.createCompoundBorder(
                                            BorderFactory.createTitledBorder("Import Data"),
                                            BorderFactory.createEmptyBorder(5,5,5,5)));

                    outFileP.setLayout(gridbag);
                        outFileL.setLabelFor(outFileT);
                    JLabel[] jl = {outFileL};
                    JTextField[] jt = {outFileT};
                    addLabelTextRows(jl, jt, gridbag, outFileP);
                    outFileP.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));

                    c0.fill = GridBagConstraints.NONE;
                    c0.gridx = 0; 
                    c0.gridy = 0;
                    c0.gridwidth = 2;
                    outPane.add(outFileP, c0);

                    outPane.setBorder(
                            BorderFactory.createCompoundBorder(
                                            BorderFactory.createTitledBorder("Output Folder"),
                                            BorderFactory.createEmptyBorder(5,5,5,5)));

                    returnP.add(return1L);
                    returnP.add(return1T);
                        return1T.setText("0");
                        return1T.setHorizontalAlignment(JTextField.CENTER);
                    returnP.add(return2La);
                    returnP.add(return2Lb);
                    returnP.setBorder(BorderFactory.createEmptyBorder(0,0,3,0));

                    c0.fill = GridBagConstraints.NONE;
                    c0.anchor = GridBagConstraints.WEST;
                    c0.gridx = 0;
                    c0.gridy = 0;
                    c0.gridwidth = 2;
                    optPane.add(returnP, c0);

                    optPane.setBorder(
                            BorderFactory.createCompoundBorder(
                                            BorderFactory.createTitledBorder("2 Data Sets"),
                                            BorderFactory.createEmptyBorder(5,5,5,5)));

                    percP1.add(percCB);
                    percP1.add(percP1a);
                    percP1.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
                        percP1a.add(percP1b);
                            percP1b.add(percT);
                                percT.setHorizontalAlignment(JTextField.CENTER);
                            percP1b.add(perc2L);
                        percP1a.add(percP1c);
                            percP1c.add(fieldC);

                    percP2.add(percP3);
                        percP3.add(perc3L);
                        percP3.add(valueC);
                        percP3.add(perc4L);
                        percP3.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
                    percP2.add(percP4);
                        percP4.add(dsC);
                        percP4.add(percHelpBP);
                        percP4.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
                            percHelpBP.add(percHelpB);
                                percHelpB.setMargin(new Insets(0,0,0,0));
                                percHelpB.setForeground(Color.BLUE);

                    percP.add(percP1, BorderLayout.NORTH);
                    percP.add(percP2, BorderLayout.CENTER);

                    c0.fill = GridBagConstraints.NONE;
                    c0.gridx = 0;
                    c0.gridy = 2;
                    c0.gridwidth = 2;
                    optPane.add(percP, c0);

                    JLabel fakeo2 = new JLabel("blah");
                    incP.add(fakeo2);
                        fakeo2.setForeground(new Color(238,238,238));
                    incP.add(incCB);
                    incP.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
                    incP.add(incB);
                        incB.setMargin(new Insets(0,0,0,0));
                        incB.setForeground(Color.BLUE);

                    c0.gridy = 3;
                    c0.gridwidth = 2;
                    optPane.add(incP, c0);

                    previewP.add(prevB);
                    previewP.add(prevT);
                        prevT.setEditable(false);
                        prevT.setBackground(new Color(245,245,245));
                        prevT.setHorizontalAlignment(JTextField.CENTER);
                    previewP.add(prevL);
                    previewP.add(prevHelpB);
                        prevHelpB.setMargin(new Insets(0,0,0,0));
                        prevHelpB.setForeground(Color.BLUE);
                    c0.anchor = GridBagConstraints.CENTER;
                    c0.gridy = 4;
                    c0.gridwidth = 2;
                    optPane.add(previewP, c0);

                    runProgP.add(runProgL);
                        runProgL.setForeground(new Color(238,238,238));
                    c0.anchor = GridBagConstraints.CENTER;
                    c0.gridy = 5;
                    c0.gridwidth = 2;
                    optPane.add(runProgP, c0);

                    anotFP.add(anotFCB);
                    anotFP.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));//7,0,5,0));
                    anotFP.add(anotFB);
                        anotFB.setMargin(new Insets(0,0,0,0));
                        anotFB.setForeground(Color.BLUE);
                    c0.anchor = GridBagConstraints.WEST;
                    c0.gridy = 6;
                    c0.gridwidth = 2;
                    optPane.add(anotFP, c0);

                    extendInfP.add(extendInfCB);
                    extendInfP.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));//7,0,5,0));
                    extendInfP.add(extendInfB);
                        extendInfB.setMargin(new Insets(0,0,0,0));
                        extendInfB.setForeground(Color.BLUE);
                    extendInfP.add(new Label(" "));
                    extendInfP.add(extendInfBPrint);
                        extendInfBPrint.setMargin(new Insets(0,0,0,0));
                    c0.anchor = GridBagConstraints.WEST;
                    c0.gridy = 7;
                    c0.gridwidth = 2;
                    optPane.add(extendInfP, c0);

                    runP.add(runB);
                    c0.anchor = GridBagConstraints.CENTER;
                    c0.gridy = 8;
                    c0.gridwidth = 2;
                    optPane.add(runP, c0);

                    outHelpP.add(outHelpB);
                    c0.anchor = GridBagConstraints.CENTER;
                    c0.gridy = 9;
                    c0.gridwidth = 2;
                    optPane.add(outHelpP, c0);


        mainP.setLayout(new BorderLayout());
        c0.fill = GridBagConstraints.REMAINDER;

        titleP.add(title);
            title.setFont(f);
        JPanel top = new JPanel(new BorderLayout());
        
        top.add(titleP, BorderLayout.CENTER);
        top.add(filePane, BorderLayout.PAGE_END);
        mainP.add(top, BorderLayout.PAGE_START);

        c0.gridx = 0;
        c0.gridy = 1;
        mainP.add(outPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottomP.add(bottomP1, BorderLayout.CENTER);
            bottomP1.add(bottomL);
                bottomL.setFont(f2);
        bottomP.add(bottomP2, BorderLayout.PAGE_END);
            bottomP2.add(bottomL2);
                bottomL2.setFont(f2);
        bottom.add(optPane, BorderLayout.PAGE_START);
        bottom.add(bottomP, BorderLayout.CENTER);
        if(cheating == true) bottom.add(cheat, BorderLayout.PAGE_END);

        c0.gridx = 0;
        c0.gridy = 2;
        mainP.add(bottom, BorderLayout.PAGE_END);

        enableOptP(false);
        add(mainP);

    }

    //a function that adds Jlabels to JTextFields in a pretty way
    //stolen from the SUN Java Tutorial website
    private void addLabelTextRows(JLabel[] labels, JTextField[] textFields, GridBagLayout gridbag, Container container)
    {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        int numLabels = labels.length;

        for (int i = 0; i < numLabels; i++)
        {
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;                       //reset to default
            container.add(labels[i], c);

            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            container.add(textFields[i], c);
        }
    }

    //enable the option panel (after files are read in)
    //can also disable if set to false.
    //this function can be called if you want both all the other panels and the annotation panels enabled/disabled
    //(for example, if an annotation file is present)
    //otherwise you can call the next function, which allows you to enable all panels except the ones related to annotation
    public void enableOptP(boolean ena)
    {
        enableOptP(ena, ena);
    }

    //enables/disables each panel in the option panel in turn
    // allows you to separately enable all the other panels (ena=true) and the panels relating to annotation
    // (anot=true) separately
    public void enableOptP(boolean ena, boolean anot)
    {
        enableReturnP(ena);
        enablePercP(anot);
        if(anot == false) //if trying to enable anotation selection options, do not, because can only be enabled by selecting the checkbox for percP
            enableIncP(anot);
        enableAnotP(anot);
        enableExtInfoP(ena);
        enablePreviewP(ena);
        enableRunP(ena);
        extendInfBPrint.setEnabled(ena);

        optPaneDisabled = ena;
    }

    //functions to enable/disable each panel
    //'Return only clusters with more than [] sequences
    public void enableReturnP(boolean ena)
    {
        Color c;
        if(ena==false) //if disabling
            c = new Color(153, 153, 153);
        else //if enabling
            c = newDs1T.getForeground();

        return1L.setForeground(c);
        return1T.setEnabled(ena);
        return2La.setForeground(c);

        if(ena == true && file2RB.isSelected()) //if enabling and on 2 data sets
            return2Lb.setForeground(c);
    }

    //functions to enable/disable each panel
    //'and which at least []% of the sequences...'
    public void enablePercP(boolean ena)
    {
        Color c;
        if(ena==false) //if disabling
            c = new Color(153, 153, 153);
        else //if enabling
            c = newDs1T.getForeground();

        percCB.setEnabled(ena);
        percT.setEnabled(ena);
        perc2L.setForeground(c);
        fieldC.setEnabled(ena);
        perc3L.setForeground(c);
        valueC.setEnabled(ena);
        percHelpB.setEnabled(ena);

        if(ena == true && file2RB.isSelected()) //if enabling and on 2 data sets
        {
            perc4L.setForeground(c);
            dsC.setEnabled(true);
        }
    }

    //functions to enable/disable each panel
    //'Embed annotatios in the FigTree file...'
    public void enableAnotP(boolean ena)
    {
        anotFCB.setEnabled(ena);
        anotFB.setEnabled(ena);
    }

    //functions to enable/disable each panel
    //'Print a .csv file with extended information...'
    public void enableExtInfoP(boolean ena)
    {
        extendInfCB.setEnabled(ena);
        extendInfB.setEnabled(ena);
    //    extendInfBPrint.setEnabled(ena);
    }

    //functions to enable/disable each panel
    //'Do not include sequences with no value...'
    public void enableIncP(boolean ena)
    {
        incCB.setEnabled(ena);
        incB.setEnabled(ena);
    }

    //functions to enable/disable each panel
    //'Preview [] '
    public void enablePreviewP(boolean ena)
    {
        Color c;
        if(ena==false) //if disabling
            c = new Color(153, 153, 153);
        else //if enabling
            c = newDs1T.getForeground();
        
        prevB.setEnabled(ena);
        prevL.setForeground(c);
        prevHelpB.setEnabled(ena);
    }

    //functions to enable/disable each panel
    //'Produce FigTree Files'
    public void enableRunP(boolean ena)
    {
        runB.setEnabled(ena);
    }

    //enables/disables the top panel that allows file input
    public void enableFileP(boolean ena)
    {
        Color c;
        if(ena==false) //if disabling
            c = new Color(153, 153, 153);
        else //if enabling
            c = newDs1T.getForeground();

        //total panel stuff
        file1RB.setEnabled(ena);
        file2RB.setEnabled(ena);
        readFileB.setEnabled(ena);
        if(ena == false) // if disabling
        {
            filePane.setBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,1),"Import Data", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial Bold", Font.PLAIN, 12), Color.GRAY),
                        BorderFactory.createEmptyBorder(6,5,5,5)));
        }
        else //if enabling
            filePane.setBorder(
                            BorderFactory.createCompoundBorder(
                                            BorderFactory.createTitledBorder("Import Data"),
                                            BorderFactory.createEmptyBorder(5,5,5,5)));

        //data set 1 stuff
        newDs1L.setForeground(c);
        newDs1T.setEnabled(ena);
        newDs1T.setDisabledTextColor(Color.DARK_GRAY);

        if(anotDs1CB.isSelected())
        {
            anotDs1T.setEnabled(ena);
            anotDs1T.setDisabledTextColor(Color.DARK_GRAY);
        }
        anotDs1CB.setEnabled(ena);
        if(ena == false) // if disabling
        {
            filePane1.setBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,1),"Data Set 1", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial Bold", Font.PLAIN, 12), Color.GRAY),
                        BorderFactory.createEmptyBorder(6,5,5,5)));
        }
        else //if enabling
            filePane1.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Data Set 1"),
                    BorderFactory.createEmptyBorder(5,5,5,5)));

        //data set 2 stuff
        if(file2RB.isSelected())
        {
            newDs2L.setForeground(c);
            newDs2T.setEnabled(ena);
            newDs2T.setDisabledTextColor(Color.DARK_GRAY);
            if(anotDs2CB.isSelected())
            {
                anotDs2T.setEnabled(ena);
                anotDs2T.setDisabledTextColor(Color.DARK_GRAY);
            }
            anotDs2CB.setEnabled(ena);
            sameMatCB.setEnabled(ena);
            if(ena == false) // if disabling
            {
                filePane2.setBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,1),"Data Set 2", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial Bold", Font.PLAIN, 12), Color.GRAY),
                        BorderFactory.createEmptyBorder(6,5,5,5)));
            }
            else //if enabling
                filePane1.setBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Data Set 2"),
                        BorderFactory.createEmptyBorder(5,5,5,5)));
            //match file stuff
            matFL.setForeground(c);
            matFT.setEnabled(ena);
            matFT.setDisabledTextColor(Color.DARK_GRAY);
            matHelpB.setEnabled(ena);

        }
        filePaneDisabled = true;
    }

}

