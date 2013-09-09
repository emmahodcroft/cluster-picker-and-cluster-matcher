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
 * Output figtree contains:
 * The whole tree of the data set from which the matching was called (usually data set 1)
 * The cluster being examined
 * The clusters from data set 2 which have sequences that match those in the cluster above
 * The whole tree of data set two with matching sequences/clusters coloured.
 * - seqs in red are sequences with matches between the two data sets within the cluster
 * - seqs in seqs in blue are sequences within the cluster but without a match
 * - seqs in green are sequences within the cluster with a match outside the cluster
 *
 * Read a description of how the program calls for various types of analyses work
 * at the very end of the file. For example, to get clusters with only X sequences
 * vs to get clusters with X matching sequences and 50% sex of male.
 *
 * TO ENABLE/DISABLE the 'cheat' button that automatically loads hard-coded file names
 * (for debug/testing), see the boolean variable 'cheating' in Wind.java. Change it to
 * 'true' to show the button, and 'false' to hide the button.
 * 
 * You can find the hard-coded file names (and change them) in the actionPerformed() method
 * under 'else if(e.getSource() == cheat)'
 *
 * boolean verbose is in Wind.java and allows you to switch debug on (true) or off (false)
 */

import java.io.*;
import java.util.*;
import java.lang.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.beans.*;

/**
 *
 * @author Emma
 */
public class Main extends Wind implements WindowListener, ItemListener, ActionListener, MouseListener, DocumentListener, PropertyChangeListener
{
    //boolean verbose is in Wind.java and allows you to switch debug on (true) or off (false)

    public boolean continueRun = true;
    public boolean twoDataSets = false;
    public ReadInClusters ric1; //holds data set 1
    public ReadInClusters ric2; //holds data set 2
    HashMap<String,String> anotOps = new HashMap<String,String>(); //holds annotation info for putting in the GUI
    String writtenFiles=""; //keeps track of what files have been written so warns before overwrite and can delete
    String lastDir = null; //keeps track of the directory the user is using so that they can load files more quickly
                        //and with less chance of suicidal urges
    String outFile = "**Cluster Matcher**\r\n-------------------\r\nCreated 2012 by Emma Hodcroft"
            + "\r\nLeigh Brown Group, Edinburgh University\r\n=======================================\r\n\r\n";
    String logFile = ""; //keeps track of the name of the logFile so that if the user chooses to delete
                    //previous runs, the logfile is not deleted as well!

    ProgressMonitor pm; //allows the progress bar for file writing
    Task task; //related to the progress bar for file writing
    Task2 task2; //related to the progress bar for file writing
    Boolean useMatches = false;
    String anot1File = "";
    String timeN = "";
    int numWrites = 1;

    String clustInfo=""; //used to carry detailed info on clusters

    public Main()
    {
        super();

        this.addWindowListener(this);

        //checkboxes
        file1RB.addItemListener(this);
        file2RB.addItemListener(this);
        anotDs2CB.addItemListener(this);
        anotDs1CB.addItemListener(this);
        sameMatCB.addItemListener(this);
        percCB.addItemListener(this);
        anotFCB.addItemListener(this);

        //all the file text fields
        newDs1T.getDocument().addDocumentListener(this);
        newDs2T.getDocument().addDocumentListener(this);
        matFT.getDocument().addDocumentListener(this);
        anotDs1T.getDocument().addDocumentListener(this);
        anotDs2T.getDocument().addDocumentListener(this);
        percT.getDocument().addDocumentListener(this);
        return1T.getDocument().addDocumentListener(this);
        outFileT.getDocument().addDocumentListener(this);
        newDs1T.addMouseListener(this);
        newDs2T.addMouseListener(this);
        matFT.addMouseListener(this);
        anotDs1T.addMouseListener(this);
        anotDs2T.addMouseListener(this);
        outFileT.addMouseListener(this);

        //comboboxes
        fieldC.addActionListener(this);

        //buttons
        readFileB.addActionListener(this);
        runB.addActionListener(this);
        prevB.addActionListener(this);
        prevHelpB.addActionListener(this);
        percHelpB.addActionListener(this);
        incB.addActionListener(this);
        anotFB.addActionListener(this);
        outHelpB.addActionListener(this);
        matHelpB.addActionListener(this);
        extendInfB.addActionListener(this);
        extendInfBPrint.addActionListener(this);

        cheat.addActionListener(this);
        cheat2.addActionListener(this);

        this.setTitle("Cluster Matcher");
        this.setLocation(50,50);

        file1RB.setSelected(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        // TODO code application logic here
        System.out.println("** Cluster Matcher **");
        run(); //gogogo!
    }

    //creates an instance of the main class (which creates the GUI)
    public static void run()
    {
        Main m = new Main ();

        m.setSize(600,675);
        m.setVisible(true);
        m.setLayout(new FlowLayout());
        m.pack();
        m.validate();

        //default values (for safety, so that no values are ever blank).
        int whi =1, matNum = 0;
        String field = null, value = null;
        Double perc = 0.0;
        boolean includeNAs = true, bothDS = false;

    }

    //called when 'read files' button is pressed. starts to read in the files.
    //every time the user changes a file field, checkbox, or radiobutton, the program
    //checks to see if the input provided is valid for a run (if two data sets are selected,
    // has the user provided two newick files? etc). If not, it disables the read files button,
    //so if it has been pressed, we can assume the data present is valid.
    public void readFiles()
    {
        //assume that because button was pressed, have right number of files.
        //(the gui checks this as the user modifies the entry fields)
        //always read in data set 1 files

        //the variable 'continueRun' is true to start, but can be switched to false if the run does not proceed as expected..
        //this stops the program from continuing to run if data is missing/incorrect/etc
        readClusters(1, newDs1T.getText().trim());
        
        if(continueRun && file2RB.isSelected()) //if 2 data sets, also read in 2nd set, and matches
        {
            readClusters(2, newDs2T.getText().trim());
            if(continueRun) attachMatches(matFT.getText().trim());
            if(continueRun && anotDs2CB.isSelected()) // if want annotate with 2 data sets, read in that, too
            {
                if(useMatches == false) //if seq names are different btwn data sets, can use two different annotation files
                    attachAnnots(1, anotDs1T.getText().trim(), 2, anotDs2T.getText().trim());
                else //however if seq names are the same, then because of limitations of FigTree we must use just one! so use the data set 2 annotations on both
                    attachAnnots(1, anotDs2T.getText().trim(), 2, anotDs2T.getText().trim());
            }
            else if(continueRun)//if have 2 data sets, but not annotating, set up the clustInfo file correctly
                clustInfo = "DataSet,Clust_ID,Matching_Clust_ID,Num_Seqs,Num_Seq_wMatch\n";
        }
        else if(continueRun && anotDs1CB.isSelected()) // if want annotate with 1 data set, read in that, too
            attachAnnots(1, anotDs1T.getText().trim());
        else if(continueRun)//if have 1 data sets, but not annotating, set up the clustInfo file correctly
            clustInfo = "Clust_ID,Num_Seqs\n";

        if(continueRun)
        {
            if(verbose) System.out.println("Run would now continue to second stage.");
            //make output file 1 makes the first part of the output file - the overview.
            //later make output file 2 adds any preview or run information that the user asks for
            makeOutputFile1();
            //switch the GUI (disable file panel, enable option panel)
            switchPanels();
        }

    }

    //reads in a newick file and assigns it to a dataset number (and RIC object)
    public void readClusters(int whi, String newFile)
    {
        //String[] temp = newFile.split("\\\\"); //this splits the file by the \ in the path.. yes it takes four.
        String[] temp = splitFile(newFile); //replaces above method with one that checks whether Mac or Windows before splitting...
        String newEnd = temp[temp.length-1]; //gives the file name (no path)
        try{
            if(whi == 1)
            {
                ric1 = new ReadInClusters(newFile);
                //warn users if they've read in a corrupt, empty, or not-really-a-newick file
                if(ric1.getNumNodes()<5) warningPane("The number of nodes in the newick file is suspiciously low. ("+newEnd+")");
                if(verbose) System.out.println("number clus in 1: "+ric1.getNumClusters());
            }
            else
            {
                ric2 = new ReadInClusters(newFile);
                if(ric2.getNumNodes()<5) warningPane("The number of nodes in the newick file is suspiciously low. ("+newEnd+")");
                if(verbose) System.out.println("number clus in 2: "+ric2.getNumClusters());
            }
            //System.out.println(ric1.getWholeTree());
            continueRun=true;
        }
        catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                "Error!", JOptionPane.ERROR_MESSAGE);
            validate();
            continueRun=false;
        }
        catch(IllegalArgumentException e){
            JOptionPane.showMessageDialog(null, e.getMessage(),
                "Error!", JOptionPane.ERROR_MESSAGE);
            validate();
            continueRun=false;
        }
    }

    //attaches the matching sequence file to the two databases
    //(provides a way to catch and display errors for file-related exceptions)
    public void attachMatches(String matchFile)
    {
        try{
            //decide which matching function to call depending on whether sequence names match between data sets
            if(sameMatCB.isSelected())//if user has said to use same matches, call attachSameMatches()
                attachSameMatches();
            else //otherwise, they must have specified a match file (we hope) so call the old matching function
                attachMatchesP(matchFile);
            continueRun=true;
        }
        catch(FileNotFoundException e){
            String pie = e.getMessage();
            if(pie.equals("No file name specified!"))
                pie = "No matches file name specified!";
            JOptionPane.showMessageDialog(null, pie,
                "Error!", JOptionPane.ERROR_MESSAGE);
            validate();
            continueRun=false;
        }
        catch(IllegalArgumentException e){
            JOptionPane.showMessageDialog(null, e.getMessage(),
                "Error!", JOptionPane.ERROR_MESSAGE);
            validate();
            continueRun=false;
        }
    }

    //attaches the matching sequence file to the two databases
    private void attachMatchesP(String matchFile) throws FileNotFoundException
    {
        ReadNewick rn = new ReadNewick(matchFile);
        rn.openFile();
        rn.next(); //get rid of the header
        int i=0;
        while(rn.hasNext())
        {
            String line = rn.next();
            String[] mat = line.split(",");
            if(mat.length != 2) //it's not in the right format...
                throw new IllegalArgumentException("Matches file is not in the correct format!");
            //go down the list of pairs of sequences and see if
            //the dataset1 seq and its dataset2 match are both present as part of clusters in the RICs
            if(ric1.clustSeqs.containsKey(mat[0]) && ric2.clustSeqs.containsKey(mat[1]))
            {
                //set each one as the other's match
                SeqNode sn = ric1.clustSeqs.get(mat[0]);
                sn.setMatch(mat[1]);
                SeqNode sn2 = ric2.clustSeqs.get(mat[1]);
                sn2.setMatch(mat[0]);

                sn.setFullMatch(sn2.getLongID());
                sn2.setFullMatch(sn.getLongID());

                //update each sequence's cluster information
                String clus = sn.getCluster();
                String clus2 = sn2.getCluster();
                ClusPtr ds1Clus = ric1.clusters.get(clus);
                ClusPtr ds2Clus = ric2.clusters.get(clus2);
                ds1Clus.matches++;
                ds2Clus.matches++;

                if(!ds1Clus.matchClusters.contains(clus2))
                    ds1Clus.matchClusters = ds1Clus.matchClusters+clus2+",";
                if(!ds2Clus.matchClusters.contains(clus))
                    ds2Clus.matchClusters = ds2Clus.matchClusters+clus+",";
            }
            i++;
        }
        if(i==0) //file had nothing in it
        {
            throw new IllegalArgumentException("Matches file is empty!");
        }
        rn.closeFile();
        if(verbose) ric1.traverseClusters(); //debug
        if(verbose) ric2.traverseClusters(); //debug
    }

    //attaches the matching sequence file to the two databases
    private void attachSameMatches()
    {
        int i=0;
        Iterator it = ric1.clustSeqs.keySet().iterator();
        String clusts="";
        for(;it.hasNext();)
        {
            //get each seq name from data set 1
            String mat = it.next().toString();

            //see if they are in data set 2 - if they are, that's a match... so follow the same code as attachMatchesP basically
            if(ric2.clustSeqs.containsKey(mat))
            {
                //set each one as the other's match
                SeqNode sn = ric1.clustSeqs.get(mat);
                sn.setMatch(mat);
                SeqNode sn2 = ric2.clustSeqs.get(mat);
                sn2.setMatch(mat);

                sn.setFullMatch(sn2.getLongID());
                sn2.setFullMatch(sn.getLongID());

                //update each sequence's cluster information
                String clus = sn.getCluster();
                String clus2 = sn2.getCluster();
                ClusPtr ds1Clus = ric1.clusters.get(clus);
                ClusPtr ds2Clus = ric2.clusters.get(clus2);
                ds1Clus.matches++;
                ds2Clus.matches++;

                if(!ds1Clus.matchClusters.contains(clus2))
                    ds1Clus.matchClusters = ds1Clus.matchClusters+clus2+",";
                if(!ds2Clus.matchClusters.contains(clus))
                    ds2Clus.matchClusters = ds2Clus.matchClusters+clus+",";
                i++;
            }
        }

        if(verbose) ric1.traverseClusters(); //debug
        if(verbose) ric2.traverseClusters(); //debug
    }

    //if just one data set is being used, reads the annotation file for that data set and stores
    //the values in anotOps
    public void attachAnnots(int whi, String file)
    {
        try{
            if(whi == 1)
            {
                anotOps = ric1.attachAnnots(file, anotOps);
                if(!ric1.allClusAnnot()) warningPane("One or more clusters have no matches in the annotation file!");
            }
            else
            {
                anotOps = ric2.attachAnnots(file, anotOps);
                if(!ric2.allClusAnnot()) warningPane("One or more clusters have no matches in the annotation file!");
            }

            if(setAnotOps(anotOps)==true)
                continueRun=true;
            else
            {
                throw new IllegalArgumentException("No annotation fields with less than 11 possible values were found!");
            }
            //for extended cluster info run
            String header = "Clust_ID,Num_Seqs";
            for(int i=0; i<fieldC.getItemCount(); i++)
            {
                String item = (String) fieldC.getItemAt(i);
                String val = anotOps.get(item);
                //System.out.println("val: "+val);
                String tr = "\""+val.replaceAll("£&","\",\"")+"\""; //because fields are separated by '£&' change this to quotes and commas...
                //System.out.println("tr: "+tr);
                header = header+","+tr+",\""+item+"_NA\"";
            }
            clustInfo = header+"\n";
            //for extended cluster info run
        }
        catch (FileNotFoundException e) {
            continueRun=false;
            JOptionPane.showMessageDialog(null, e.getMessage(),
                "Error!", JOptionPane.ERROR_MESSAGE);
            validate();
        }
        catch (IllegalArgumentException e){
            continueRun=false;
            JOptionPane.showMessageDialog(null, e.getMessage(),
                "Error!", JOptionPane.ERROR_MESSAGE);
            validate();
        }
        catch (IOException e){
            continueRun=false;
            JOptionPane.showMessageDialog(null, e.getMessage(),
                "Error!", JOptionPane.ERROR_MESSAGE);
            validate();
        }
    }

    /*
     * attaches the annotation information to each data set.
     *
     * If two data sets are being used,
     * we do not want to display annotation options for selection in the GUI
     * that are not present in both data sets!!
     *
     * So this works by having anotOps as a global variable. First the annotation
     * information is read in for data set 1, and the annotations found are put into
     * anotOps. Then, anotOps is passed in when the annotations are read in for data set
     * 2, and any annotations that are not already present in data set 1 are not added
     * as annotation selection options in the GUI!
     */
    public void attachAnnots(int whi, String file, int whi2, String file2)
    {
        ReadInClusters ric, ricO;
        if(whi == 1)
        {
            ric = ric1;
            ricO = ric2;
        }
        else
        {
            ricO = ric1;
            ric = ric2;
        }
        try{
            anotOps = ric.attachAnnots(file, anotOps);
            anotOps = ricO.attachAnnots(file2, anotOps);
            if(setAnotOps(anotOps)==true)
            {
                if(!ric.allClusAnnot()) warningPane("One or more clusters in Data Set "+whi+" have no matches in the annotation file!");
                if(!ricO.allClusAnnot()) warningPane("One or more clusters in Data Set "+(3-whi)+" have no matches in the annotation file!");

                continueRun=true;
            }
            else
            {
                throw new IllegalArgumentException("No annotation fields (with less than 11 possible values) were found in common between the data sets!");
            }
            //for extended cluster info run
            String header = "DataSet,Clust_ID,Matching_Clust_ID,Num_Seqs,Num_Seq_wMatch";
            for(int i=0; i<fieldC.getItemCount(); i++)
            {
                String item = (String) fieldC.getItemAt(i);
                String val = anotOps.get(item);
                //System.out.println("val: "+val);
                String tr = "\""+val.replaceAll("£&","\",\"")+"\""; //because fields are separated by '£&' change this to quotes and commas...
                //System.out.println("tr: "+tr);
                header = header+","+tr+",\""+item+"_NA\"";
            }
            clustInfo = header+"\n";
            //for extended cluster info run
        }
        catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                "Error!", JOptionPane.ERROR_MESSAGE);
            validate();
            continueRun=false;
        }
        catch (IllegalArgumentException e){
            JOptionPane.showMessageDialog(null, e.getMessage(),
                "Error!", JOptionPane.ERROR_MESSAGE);
            validate();
            continueRun=false;
        }
        catch (IOException e){
            JOptionPane.showMessageDialog(null, e.getMessage(),
                "Error!", JOptionPane.ERROR_MESSAGE);
            validate();
            continueRun=false;
        }
    }

    /*
     * Puts the annotation options recovered from the data set (or sets) in the GUI
     * as selection options.
     * Does NOT put in any field that has more than 10 different values - would make the
     * drop-down selection box start getting too long. So for example, usually excludes country.
     *
     * returns true if got annotation options without problem.. returns false if didn't work.
     *
     */
    public boolean setAnotOps(HashMap<String,String> anotOps)
    {
        Iterator it = anotOps.keySet().iterator();
        String toAdd = "";
        for(;it.hasNext();)
        {
            String k = it.next().toString();
            String soFar = anotOps.get(k);
            String[] sf = soFar.split("£&");//(",");
            if(verbose) 
            {
                System.out.println("Annotation " + k + " has " + (sf.length) + " variables.");
                System.out.print("\t");
                for(int i=0;i<sf.length;i++)
                {
                    System.out.print(sf[i]+" ");
                }
                System.out.println();
            }

            if(sf.length<11)//if less than 11 values, then add to the GUI
            {
                if(file2RB.isSelected()) //if two files are selected, check to make sure field is in both data sets, if not, don't display
                {
                    if(ric1.isFieldPresent(k) && ric2.isFieldPresent(k))
                        toAdd = toAdd + k + "£&";//",";
                }
                else
                    toAdd = toAdd+k+"£&";//",";
            }
        }
        if(!toAdd.isEmpty())
        {
            fieldC.setForeground(Color.BLACK);
            String[] add = toAdd.split("£&");//(",");
            for(int i=0;i<add.length; i++)
            {
                fieldC.addItem(add[i]);
            }
            validate();
            return true;
        }
        else//if is empty... that's bad.
            return false;
    }

    //this method is just for debug purposes.
    public void traverseClustersAll(int whi)
    {
        ReadInClusters ric = ric1, ricO = ric2;
        if(whi == 1)
        {
            ric = ric1;
            ricO = ric2;
        }
        else
        {
            ric = ric2;
            ricO = ric1;
        }
        ric.traverseClustersAll(ricO);
    }

    /*
     * Prints clusters from data set (whi) that have more than 'num' sequences
     */
    public int printClustersMoreThan(int whi, int num, boolean annot, JButton b)
    {
        int number = 0;
        ReadInClusters ric = ric1;
        if(whi == 1)
            ric = ric1;
        else
            ric = ric2;
        String[] clus = ric.returnClustersMoreThan(num);

        if(clus!=null && clus.length != 0 && !isArrayEmpty(clus))
        {
            number = clus.length;
            if(b == runB || b == extendInfBPrint)
            {
                task2 = new Task2(clus, whi, annot, b);
                pm = new ProgressMonitor(Main.this, "Writing files...", "", 0, task2.getLengthOfTask());
                pm.setMillisToDecideToPopup(5);
                pm.setMillisToPopup(5);

                pm.setProgress(0);
                task2.addPropertyChangeListener(this);
                task2.execute();

            }
        }
        else if(b == runB || b == extendInfBPrint)
        {
            String mes = "Sorry, no clusters matching those parameters were found.";
            if(verbose) System.out.println(mes);
            JOptionPane.showMessageDialog(null, mes,
                "Not Found!", JOptionPane.INFORMATION_MESSAGE);
            validate();
        }
        makeOutputFile2(number, clus, b);
        return number;
    }

    /*
     * Prints clusters from data set (whi) that have more than 'num' matches,
     * and fit the annotation specified - in one or both datasets
     */
    public int printMatches(int whi, int matNum, String field, String value, double perc, boolean includeNAs, boolean bothDS, JButton b)
    {
        int number = 0;
        ReadInClusters ric = ric1, ricO = ric2;
        if(whi == 1)
        {
            ric = ric1;
            ricO = ric2;
        }
        else
        {
            ric = ric2;
            ricO = ric1;
        }

        String[] clus = ric.returnClusterMatchMoreThan(matNum); //if no min match num is specified, will return all clusters with matches
        if(clus!=null && clus.length != 0 && !isArrayEmpty(clus))
        {
            if(field != null && value != null ) //then we need to sort by an annot value as well
            {
                if(bothDS == false)
                    clus = ric.returnClusterAnnotPerc(clus, field, value, perc, includeNAs); //only sort based on 1 db
                else
                    clus = ric.returnClusterAnnotPerc(clus, field, value, perc, includeNAs, ricO); //sort based on both dbs
            }
                if(verbose) System.out.println("clusters that fit these: "); //debug
                if(verbose) printArray(clus); //debug
            if(clus!=null && clus.length != 0 && !isArrayEmpty(clus))
            {
                number = clus.length;
                if(b == runB || b == extendInfBPrint)
                {
                    task = new Task(clus, whi, true, b);
                    pm = new ProgressMonitor(Main.this, "Writing files...", "", 0, task.getLengthOfTask());
                    pm.setMillisToDecideToPopup(5);
                    pm.setMillisToPopup(5);

                    pm.setProgress(0);
                    task.addPropertyChangeListener(this);
                    task.execute();
                }
            }
            else if(b==runB || b == extendInfBPrint)
            {
                String mes = "Sorry, no clusters matching those parameters were found.";
                System.out.println(mes);
                JOptionPane.showMessageDialog(null, mes,
                    "Not Found!", JOptionPane.INFORMATION_MESSAGE);
                validate();
            }
        }
        else if(b==runB || b == extendInfBPrint)
        {
            String mes = "Sorry, no clusters matching those parameters were found.";
            System.out.println(mes);
            JOptionPane.showMessageDialog(null, mes,
                "Not Found!", JOptionPane.INFORMATION_MESSAGE);
            validate();
        }
        makeOutputFile2(number, clus, b);
        return number;
    }

    /*
     * prints clusters that have the specified percent of 'value' in 'field'
     * just looks at one data set
     */
    public int printClusterAnnots(int whi, int matNum, String field, String value, double perc, boolean includeNAs, JButton b)
    {
        int number = 0;
        ReadInClusters ric = ric1;
        if(whi == 1)
            ric = ric1;
        else
            ric = ric2;

        String[] clus = ric.returnClustersMoreThan(matNum); //if no min match num is specified, will return all clusters
        if(clus!=null && clus.length != 0 && !isArrayEmpty(clus))
        {
            if(field != null && value != null ) //then we need to sort by an annot value as well
            {
                clus = ric.returnClusterAnnotPerc(clus, field, value, perc, includeNAs);
            }

   if(verbose) System.out.println("clusters that match: ");
   if(verbose) printArray(clus,"-");
            if(clus!=null && clus.length != 0 && !isArrayEmpty(clus))
            {
                number = clus.length;
                if(b == runB || b == extendInfBPrint) //only if running print files
                {
                    task2 = new Task2(clus, whi, true, b);
                    pm = new ProgressMonitor(Main.this, "Writing files...", "", 0, task2.getLengthOfTask());
                    pm.setMillisToDecideToPopup(5);
                    pm.setMillisToPopup(5);

                    pm.setProgress(0);
                    task2.addPropertyChangeListener(this);
                    task2.execute();

                }
            }
            else if(b == runB || b == extendInfBPrint)
            {
                String mes = "Sorry, no clusters matching those parameters were found.";
                System.out.println(mes);
                JOptionPane.showMessageDialog(null, mes,
                    "Not Found!", JOptionPane.INFORMATION_MESSAGE);
                validate();
            }
        }
        else if(b==runB || b == extendInfBPrint)
        {
            String mes = "Sorry, no clusters matching those parameters were found.";
            System.out.println(mes);
            JOptionPane.showMessageDialog(null, mes,
                "Not Found!", JOptionPane.INFORMATION_MESSAGE);
            validate();
        }
        makeOutputFile2(number, clus, b);
        return number;

    }

    /*
     * Prints clusters from data set (whi) that have more than 'num' sequences with matches in the other data set
     */
    public int printMatchingClusters(int whi, int num, boolean annot, JButton b)
    {
        long start = System.currentTimeMillis();
        int number = 0;
        ReadInClusters ric = ric1;
        if(whi == 1)
            ric = ric1;
        else
            ric = ric2;
        String[] clus = ric.returnClusterMatchMoreThan(num);

        if(clus!=null && clus.length != 0 && !isArrayEmpty(clus))
        {
            number = clus.length;
            if(b==runB || b == extendInfBPrint)
            {
                task = new Task(clus, whi, annot, b);
                pm = new ProgressMonitor(Main.this, "Writing files...", "", 0, task.getLengthOfTask());
                pm.setMillisToDecideToPopup(5);
                pm.setMillisToPopup(5);
                pm.setProgress(0);

                task.addPropertyChangeListener(this);
                task.execute();
            }
        }
        else if(b==runB || b == extendInfBPrint)
        {
            String mes = "Sorry, no clusters matching those parameters were found.";
            System.out.println(mes);
            JOptionPane.showMessageDialog(null, mes,
                "Not Found!", JOptionPane.INFORMATION_MESSAGE);
            validate();
        }

        long end = System.currentTimeMillis();
        if(verbose) System.out.println("Start: "+start+" End: "+end+"  Total: "+(end-start));

        makeOutputFile2(number, clus, b);
        return number;
    }

    /*
     * An internal function used by the matching functions above
     * It prints a cluster, but also prints the clusters from the other data set that have sequences matching this cluster
     * Including annotations, and colours
     *
     * THIS is function called if the sequence names are DIFFERENT between Data sets!!
     */
    public void printClusterMatch(int whi, String cluster, boolean annot, JButton b)
    {
     long st = System.currentTimeMillis(); //for timing the efficiency of the function

        ReadInClusters ric = ric1, ricO = ric2;
        String clustFile = "Cluster"+cluster+".figTree";
        if(whi == 1)
        {
            ric = ric1; ricO = ric2;
            clustFile = "dataSet1_"+clustFile;
        }
        else
        {
            ric = ric2; ricO = ric1;
            clustFile = "dataSet2_"+clustFile;
        }
        String[] totNodes = ric.getTotalNodes();  //returns string array of all nodes in RIC, with orig names (Clust5_7122)
        String[] matches = ric.getClusterMatches(cluster);
   if(verbose) System.out.println("For cluster "+cluster+" matches are: ");
   if(verbose) printArray(matches);
        String[] ric2TotNodes = ricO.getTotalNodes();

        //prints extended cluster info if requested by the user:
        if(extendInfCB.isSelected() || b == extendInfBPrint) //if tickbox selected OR button was pressed
        {
            String info = whi+","+cluster+",\"";
            String[] mats = ric.getClusterMatches(cluster);
            for(int i=0; i<mats.length; i++)
                info=info+mats[i]+",";
            info=info.substring(0, info.length()-1)+"\","+ric.getNumSeqsInClus(cluster)+","+ric.getNumMatches(cluster);

            //only try and print anots if they put in annotations
            if(anotDs2CB.isSelected())
            {
                String clus[] = {cluster};
                for(int i=0; i<fieldC.getItemCount(); i++)
                {
                    String item = (String) fieldC.getItemAt(i);
                    String val = anotOps.get(item);
                    String[] vals = val.split("£&");//(",");
                    int[] nums = ric.getClusAnnot(clus, item, vals);
                    for(int j=0; j<vals.length; j++)
                    {
                        info = info+","+nums[j];
                    }
                    vals = new String[1];
                    vals[0] = "";
                    nums = ric.getClusAnnot(clus, item, vals);
                    info = info+","+nums[0];
                }
            }
            info = info+"\n";
            clustInfo = clustInfo+info;
            //now do matches....
            for(int i=0; i<mats.length; i++)
            {
                info = (3-whi)+","+mats[i]+",\"";
                String[] mutch = ricO.getClusterMatches(mats[i]);
                for(int v=0; v<mutch.length; v++)
                    info=info+mutch[v]+",";
                info=info.substring(0, info.length()-1)+"\","+ricO.getNumSeqsInClus(mats[i])+","+ricO.getNumMatches(mats[i]);

                //only try and print anots if they put in annotations
                if(anotDs2CB.isSelected())
                {
                    String clus2[] = {mats[i]};
                    for(int x=0; x<fieldC.getItemCount(); x++)
                    {
                        String item = (String) fieldC.getItemAt(x);
                        String val = anotOps.get(item);
                        String[] vals = val.split("£&");//(",");
                        int[] nums = ricO.getClusAnnot(clus2, item, vals);
                        for(int j=0; j<vals.length; j++)
                        {
                            info = info+","+nums[j];
                        }
                        vals = new String[1];
                        vals[0] = "";
                        nums = ricO.getClusAnnot(clus2, item, vals);
                        info = info+","+nums[0];
                    }
                }
                info = info+"\n";
                clustInfo = clustInfo+info;
            }
        }
        //extended cluster info 

        if(b == runB)
        {

            String file = "";
            file = "#NEXUS"
                    +"\nbegin taxa;"
                    +"\ndimensions ntax="+(totNodes.length+ric2TotNodes.length)+";"
                    +"\n\ttaxlabels\n";

     long stAllSeq = System.currentTimeMillis(); //for timing the efficiency of the function
     //there used to be code in here, hence the two timers
     long loopArray = System.currentTimeMillis(); //for timing the efficiency of the function

            //if the sequences
            String totN = Arrays.toString(totNodes);
            totN = totN.substring(1,totN.length()-1);
            totN = totN.replaceAll(", ", "\n\t");
            file = file + "\t"+totN+"\n";
            totN = null;

            totN = Arrays.toString(ric2TotNodes);
            totN = totN.substring(1,totN.length()-1);
            totN = totN.replaceAll(", ", "\n\t");
            file = file + "\t"+totN+"\n";
            totN = null;


    long stTrees = System.currentTimeMillis(); //for timing the efficiency of the function
            //now make a new string to hold the tree code
            //put in the full tree ds1, cluster ds1, cluster(s) ds2, full tree ds2
            String treeFile1="", treeFile2="";
            String tree1="", tree2="";
            if(ric.isAnnot() && annot)
                tree1 = ric.getWholeTree(cluster);
            else
                tree1 = ric.getWholeTree();
            treeFile1 = treeFile1+"\ttree allDataSet"+whi+" = [&R] "+tree1+"\n"; //file = file+"\ttree allDataSet"+whi+" = [&R] "+ric.getWholeTree()+"\n";
            treeFile1 = treeFile1+"\ttree ds"+whi+"_Clust"+cluster+" = [&R] "+ric.getClusterCode(cluster, annot)+"\n";

            for(int i=0;i<matches.length; i++)
            {
                treeFile2 = treeFile2+"\ttree ds"+(3-whi)+"_Clust"+matches[i]+" = [&R] "+ricO.getClusterCode(matches[i], annot)+"\n";
            }
            if(ricO.isAnnot() && annot)
                tree2 = ricO.getWholeTree(matches);
            else
                tree2 = ricO.getWholeTree();
            treeFile2 = treeFile2+"\ttree allDataSet"+(3-whi)+" = [&R] "+tree2+"\n"//file = file+"\ttree allDataSet"+(3-whi)+" = [&R] "+ricO.getWholeTree()+"\n"
            +"end;\n";

    long stRIC1 = System.currentTimeMillis(); //for timing the efficiency of the function
            //now search the existing list of sequences and replace those in clusters/with matches etc
            //first search for those in the cluster in RIC1
            String[] seqs = ric.getSeqsInCluster(cluster);
            for(int i=0; i<seqs.length; i++)
            {
                String p ="Clust"+cluster+"_"+seqs[i];
                String orig = p;
                //get annot code so can display as tip labels the annots
                String seqAnnot = "";
                if(annot == true)
                    seqAnnot = ric.getTrimmedSeqAnnot(seqs[i]);
                if(!seqAnnot.isEmpty()) seqAnnot = ","+seqAnnot;
                if(ric.hasMatch(seqs[i]))
                {
                    String m = ric.getMatch(seqs[i]);//ric.getFullMatch(seqs[i]);
                    p = p+"{"+m+"}[&!color=#FF0000"+seqAnnot+"]";  //if a match, colour red
                    //treeFile1 = treeFile1.replaceAll(orig, orig+"{"+m+"}");
                    treeFile1 = treeFile1.replace(orig, orig+"{"+m+"}");
                }
                else
                    p = p+"[&!color=#0000FF"+seqAnnot+"]"; //if just in the cluster, colour blue

                //file = file.replaceAll(orig, p);
                file = file.replace(orig, p);
            }

    long stRIC2 = System.currentTimeMillis(); //for timing the efficiency of the function
            //now search for those in the matching clusters in RIC2
            for(int i=0; i<matches.length; i++)//for each cluster that matches
            {
                seqs = ricO.getSeqsInCluster(matches[i]);
                for(int j=0; j<seqs.length; j++)
                {
                    String p = "Clust"+matches[i]+"_"+seqs[j];
                    String orig = p;
                    //get annot code so can display as tip labels the annots
                    String seqAnnot = "";
                    if(annot == true)
                        seqAnnot = ricO.getTrimmedSeqAnnot(seqs[j]);
                    if(!seqAnnot.isEmpty()) seqAnnot = ","+seqAnnot;
                    //see if has a match
                    String matClus = ricO.getMatchClus(seqs[j]);
                    if(matClus == null) //if no match
                    {
                        p = p + "[&!color=#0000FF"+seqAnnot+"]"; //color blue as part of cluster
                    }
                    else if(matClus.equals(cluster)) //if a match in the cluster in RIC1
                    {
                        p = p + "[&!color=#FF0000"+seqAnnot+"]"; //color red as match
                    }
                    else //if a match with another sequence, not in RIC1 cluster
                    {
                        p = p + "[&!color=#008000"+seqAnnot+"]"; //color green
                        //and colour the match green too!
                        String mat = ricO.getFullMatch(seqs[j]);
                        String sA = "";
                        if(annot==true)
                            sA = ric.getTrimmedSeqAnnot(ricO.getMatch(seqs[j]));
                        if(!sA.isEmpty()) sA = ","+sA;
                        String rep = mat + "{"+seqs[j]+"}[&!color=#008000"+sA+"]";
                        //file = file.replaceAll(mat, rep);
                        file = file.replace(mat, rep);
                        //replace in tree file too
                        //treeFile1 = treeFile1.replaceAll(mat, mat+"{"+seqs[j]+"}");
                        treeFile1 = treeFile1.replace(mat, mat+"{"+seqs[j]+"}");
                    }
                    //file = file.replaceAll(orig, p);
                    file = file.replace(orig, p);
                }
            }

            file = file+"\t;\n"
            +"end;\n"
            +"begin trees;\n";

            //now add the tree file holders to the file
            file = file+treeFile1+treeFile2;

            file = file+"begin figtree;\n"
            +"\tset tipLabels.colorAttribute=\"User Selection\";\n"
            +"\tset tipLabels.displayAttribute=\"Names\";\n"
            +"\tset tipLabels.fontName=\"Arial\";\n"
            +"\tset tipLabels.fontSize=10;\n"
            +"\tset tipLabels.fontStyle=0;\n"
            +"\tset tipLabels.isShown=true;\n"
            +"\tset tipLabels.significantDigits=4;\n"
            +"\tset trees.order=true;\n"
            +"\tset trees.orderType=\"increasing\";\n"
            +"\tset trees.rooting=false;\n"
            +"\tset trees.rootingType=\"User Selection\";\n"
            +"\tset trees.transform=false;\n"
            +"\tset trees.transformType=\"cladogram\";\n"
            +"\tset branchLabels.fontName=\"Arial\";\n"
            +"\tset branchLabels.fontSize=10;\n"
            +"\tset scaleBar.fontSize=11.0;\n"
            +"end;\n";


            printStringToFile(file, clustFile);
      long en = System.currentTimeMillis(); //for timing the efficiency of the function
            if(verbose) //for timing the efficiency of the function
            {
                System.out.println("Print " + cluster + ": \ntot: " + (en - st) + ""
                    + "  1: "+(stAllSeq-st)+"  2: "+ (stTrees-stAllSeq)
                    + "  3: "+(stRIC1-stTrees)+"  4: "+ (stRIC2-stRIC1)
                    + "  5: "+(en-stRIC2) + "  loopArray: "+(stTrees-loopArray));
            }
        }
    }

    /*
     * An internal function used by the matching functions above
     * It prints a cluster, but also prints the clusters from the other data set that have sequences matching this cluster
     * Including annotations, and colours
     *
     * THIS is the function called if the sequence names are the SAME between Data sets!
     */
    public void printClusterMatchSame(int whi, String cluster, boolean annot, JButton b)
    {
     long st = System.currentTimeMillis(); //for timing the efficiency of the function

        ReadInClusters ric = ric1, ricO = ric2;
        String clustFile = "Cluster"+cluster+".figTree";
        if(whi == 1)
        {
            ric = ric1; ricO = ric2;
            clustFile = "dataSet1_"+clustFile;
        }
        else
        {
            ric = ric2; ricO = ric1;
            clustFile = "dataSet2_"+clustFile;
        }
        String[] totNodes = ric.getTotalNodes();  //returns string array of all nodes in RIC, with orig names (Clust5_7122)
        String[] matches = ric.getClusterMatches(cluster);
   if(verbose) System.out.println("For cluster "+cluster+" matches are: ");
   if(verbose) printArray(matches);
        String[] ric2TotNodes = ricO.getTotalNodes();

        //prints extended cluster info if requested by the user:
        if(extendInfCB.isSelected() || b == extendInfBPrint) //if tickbox selected OR button was pressed
        {
            String info = whi+","+cluster+",\"";
            String[] mats = ric.getClusterMatches(cluster);
            for(int i=0; i<mats.length; i++)
                info=info+mats[i]+",";
            info=info.substring(0, info.length()-1)+"\","+ric.getNumSeqsInClus(cluster)+","+ric.getNumMatches(cluster);

            //only try and print anot info if they put in annotations
            if(anotDs2CB.isSelected())
            {
                String clus[] = {cluster};
                for(int i=0; i<fieldC.getItemCount(); i++)
                {
                    String item = (String) fieldC.getItemAt(i);
                    String val = anotOps.get(item);
                    String[] vals = val.split("£&");//(",");
                    int[] nums = ric.getClusAnnot(clus, item, vals);
                    for(int j=0; j<vals.length; j++)
                    {
                        info = info+","+nums[j];
                    }
                    vals = new String[1];
                    vals[0] = "";
                    nums = ric.getClusAnnot(clus, item, vals);
                    info = info+","+nums[0];
                }
            }
            info = info+"\n";
            clustInfo = clustInfo+info;
            //now do matches....
            for(int i=0; i<mats.length; i++)
            {
                info = (3-whi)+","+mats[i]+",\"";
                String[] mutch = ricO.getClusterMatches(mats[i]);
                for(int v=0; v<mutch.length; v++)
                    info=info+mutch[v]+",";
                info=info.substring(0, info.length()-1)+"\","+ricO.getNumSeqsInClus(mats[i])+","+ricO.getNumMatches(mats[i]);

                //only try and print anots if they put in annotations
                if(anotDs2CB.isSelected())
                {
                    String clus2[] = {mats[i]};
                    for(int x=0; x<fieldC.getItemCount(); x++)
                    {
                        String item = (String) fieldC.getItemAt(x);
                        String val = anotOps.get(item);
                        String[] vals = val.split("£&");//(",");
                        int[] nums = ricO.getClusAnnot(clus2, item, vals);
                        for(int j=0; j<vals.length; j++)
                        {
                            info = info+","+nums[j];
                        }
                        vals = new String[1];
                        vals[0] = "";
                        nums = ricO.getClusAnnot(clus2, item, vals);
                        info = info+","+nums[0];
                    }
                }
                info = info+"\n";
                clustInfo = clustInfo+info;
            }
        }
        //extended cluster info

        if(b == runB)
        {

            String file = "";

     long stAllSeq = System.currentTimeMillis(); //for timing the efficiency of the function
     //there used to be code in here, hence the two timers
     long loopArray = System.currentTimeMillis(); //for timing the efficiency of the function

            //print each sequence only once - do not simply print all sequences from both
            //data sets or there will be duplicates!!
            //HOWEVER, sequences that have the same sequence name MAY NOT have the same FULL seq name
            //because they may be in diff numbered clusters by chance!! (OR they could be!)
            //Seq 1212 could be in Clust11 in data set 1 AND Clust11 in data set 2
            //OR could be in Clust11 in data set 1 and Clust20 in data set 2!
            //In first case, Clust11_1212 needs to go into taxtabels ONCE and be annotated ONCE
            //In second case, Clust11_1212 and Clust20_1212 BOTH need to go into taxlabels and BOTh need to be annotated

            //I got this idea to solve this from: http://www.theeggeadventure.com/wikimedia/index.php/Java_Unique_List
            java.util.List ric1List = Arrays.asList(totNodes); //get list of all seqs in data set 1
            java.util.List ric2List = Arrays.asList(ric2TotNodes); //get list of all seqs in data set 2
            HashSet set = new HashSet(ric1List); //put into hashed set - will disregard duplicates!!
            HashSet set2 = new HashSet(ric2List);
            set.addAll(set2); //add all data set 2 seqs to data set 1 hashset

            String totN = set.toString(); //now we can put to a string knowing duplicates are removed
            totN = totN.substring(1,totN.length()-1); //get rid of [ and ] at begin and end
            totN = totN.replaceAll(", ", "\n\t"); //replace commas with new lines and tabs
            //because all the seqs were added in this one go, don't need to do separately for data set 1 and data set 2

            //but only NOW add the file heading to the file because we know the length of the unique nodes....
            file = "#NEXUS"
            +"\nbegin taxa;"
            +"\ndimensions ntax="+set.size()+";"
            +"\n\ttaxlabels\n";
            file = file + "\t"+totN+"\n";
            totN = null;

    long stTrees = System.currentTimeMillis(); //for timing the efficiency of the function
            //now make a new string to hold the tree code
            //put in the full tree ds1, cluster ds1, cluster(s) ds2, full tree ds2
            String treeFile1="", treeFile2="";
            String tree1="", tree2="";
            if(ric.isAnnot() && annot)
                tree1 = ric.getWholeTree(cluster);
            else
                tree1 = ric.getWholeTree();
            treeFile1 = treeFile1+"\ttree allDataSet"+whi+" = [&R] "+tree1+"\n"; //file = file+"\ttree allDataSet"+whi+" = [&R] "+ric.getWholeTree()+"\n";
            treeFile1 = treeFile1+"\ttree ds"+whi+"_Clust"+cluster+" = [&R] "+ric.getClusterCode(cluster, annot)+"\n";

            for(int i=0;i<matches.length; i++)
            {
                treeFile2 = treeFile2+"\ttree ds"+(3-whi)+"_Clust"+matches[i]+" = [&R] "+ricO.getClusterCode(matches[i], annot)+"\n";
            }
            if(ricO.isAnnot() && annot)
                tree2 = ricO.getWholeTree(matches);
            else
                tree2 = ricO.getWholeTree();
            treeFile2 = treeFile2+"\ttree allDataSet"+(3-whi)+" = [&R] "+tree2+"\n"//file = file+"\ttree allDataSet"+(3-whi)+" = [&R] "+ricO.getWholeTree()+"\n"
            +"end;\n";
    long stRIC1 = System.currentTimeMillis(); //for timing the efficiency of the function

            //DO NOT NEED TO ADD MATCHES AFTER NAME AS SEQUENCES NAMES ARE THE SAME!!

            //now search the existing list of sequences and replace those in clusters/with matches etc
            //first search for those in the cluster in RIC1
            String[] seqs = ric.getSeqsInCluster(cluster);
            for(int i=0; i<seqs.length; i++)
            {
                String p ="Clust"+cluster+"_"+seqs[i];
                String orig = p;
                //get annot code so can display as tip labels the annots
                String seqAnnot = "";
                if(annot == true)
                    seqAnnot = ric.getTrimmedSeqAnnot(seqs[i]);
                if(!seqAnnot.isEmpty()) seqAnnot = ","+seqAnnot;
                if(ric.hasMatch(seqs[i]))
                {
                    p = p+"[&!color=#FF0000"+seqAnnot+"]";  //if a match, colour red
                }
                else
                    p = p+"[&!color=#0000FF"+seqAnnot+"]"; //if just in the cluster, colour blue

                //file = file.replaceAll(orig, p);
                file = file.replace(orig, p);
            }

    long stRIC2 = System.currentTimeMillis(); //for timing the efficiency of the function
            //now search for those in the matching clusters in RIC2
            for(int i=0; i<matches.length; i++)//for each cluster that matches
            {
                seqs = ricO.getSeqsInCluster(matches[i]);
                for(int j=0; j<seqs.length; j++)
                {
                    String p = "Clust"+matches[i]+"_"+seqs[j];
                    String orig = p;
                    //get annot code so can display as tip labels the annots
                    String seqAnnot = "";
                    if(annot == true)
                        seqAnnot = ricO.getTrimmedSeqAnnot(seqs[j]);
                    if(!seqAnnot.isEmpty()) seqAnnot = ","+seqAnnot;
                    //see if has a match
                    String matClus = ricO.getMatchClus(seqs[j]);
                    if(matClus == null) //if no match
                    {
                        p = p + "[&!color=#0000FF"+seqAnnot+"]"; //color blue as part of cluster
                    }
                    else if(matClus.equals(cluster)) //if a match in the cluster in RIC1
                    {
                        //should already be red from being coloured by RIC1 search above!
                        //unless the cluster numbers have changed
                        p = p + "[&!color=#FF0000"+seqAnnot+"]"; //color red as match
                    }
                    else //if a match with another sequence, not in RIC1 cluster
                    {
                        p = p + "[&!color=#008000"+seqAnnot+"]"; //color green
                        //and colour the match green too!
                        String mat = ricO.getFullMatch(seqs[j]);
                        String sA = "";
                        if(annot==true)
                            sA = ric.getTrimmedSeqAnnot(ricO.getMatch(seqs[j]));
                        if(!sA.isEmpty()) sA = ","+sA;
                        String rep = mat + "[&!color=#008000"+sA+"]";
                        //file = file.replaceAll(mat, rep);
                        file = file.replace(mat, rep);
                        //if they have the same cluster number, this line above will append the colour to the one seq name
                        //if they have different cluster numbers, the line below will append to the other seq name
                    }
                    //only replace if it has not already been annotated with something!!!!
                    //file = file.replaceAll(orig+"\n", p+"\n");
                    file = file.replace(orig+"\n", p+"\n");
                }
            }

            file = file+"\t;\n"
            +"end;\n"
            +"begin trees;\n";

            //now add the tree file holders to the file
            file = file+treeFile1+treeFile2;

            file = file+"begin figtree;\n"
            +"\tset tipLabels.colorAttribute=\"User Selection\";\n"
            +"\tset tipLabels.displayAttribute=\"Names\";\n"
            +"\tset tipLabels.fontName=\"Arial\";\n"
            +"\tset tipLabels.fontSize=10;\n"
            +"\tset tipLabels.fontStyle=0;\n"
            +"\tset tipLabels.isShown=true;\n"
            +"\tset tipLabels.significantDigits=4;\n"
            +"\tset trees.order=true;\n"
            +"\tset trees.orderType=\"increasing\";\n"
            +"\tset trees.rooting=false;\n"
            +"\tset trees.rootingType=\"User Selection\";\n"
            +"\tset trees.transform=false;\n"
            +"\tset trees.transformType=\"cladogram\";\n"
            +"\tset branchLabels.fontName=\"Arial\";\n"
            +"\tset branchLabels.fontSize=10;\n"
            +"\tset scaleBar.fontSize=11.0;\n"
            +"end;\n";


            printStringToFile(file, clustFile);
      long en = System.currentTimeMillis(); //for timing the efficiency of the function
            if(verbose) //for timing the efficiency of the function
            {
                System.out.println("Print " + cluster + ": \ntot: " + (en - st) + ""
                    + "  1: "+(stAllSeq-st)+"  2: "+ (stTrees-stAllSeq)
                    + "  3: "+(stRIC1-stTrees)+"  4: "+ (stRIC2-stRIC1)
                    + "  5: "+(en-stRIC2) + "  loopArray: "+(stTrees-loopArray));
            }
        }
    }

    /*
     * A function to just print 1 cluster, no matches
     * Since there is no matching, does not have to be adjusted for matches based on the same seq names in both
     * data sets.
     */
    public void printCluster(int whi, String cluster, boolean annot, JButton b)
    {
  long st = System.currentTimeMillis(); //for timing the efficiency of the function
        ReadInClusters ric = ric1;
        String clustFile = "Cluster"+cluster+".figTree";
        if(whi == 1)
        {
            ric = ric1;
            clustFile = "dataSet1_"+clustFile;
        }
        else
        {
            ric = ric2;
            clustFile = "dataSet2_"+clustFile;
        }
        String file = "";
        String[] totNodes = ric.getTotalNodes();
        file = "#NEXUS"
                +"\nbegin taxa;"
                +"\ndimensions ntax="+totNodes.length+";"
                +"\n\ttaxlabels\n";

        //prints extended cluster info if requested by the user:
        if(extendInfCB.isSelected() || b == extendInfBPrint) //if tickbox selected OR button was pressed)
        {
            String info = cluster+","+ric.getNumSeqsInClus(cluster);
            //only try and print annot info if they put in annotations
            if(anotDs1CB.isSelected())
            {
                String clus[] = {cluster};
                for(int i=0; i<fieldC.getItemCount(); i++)
                {
                    String item = (String) fieldC.getItemAt(i);
                    String val = anotOps.get(item);
                    String[] vals = val.split("£&");//(",");
                    int[] nums = ric.getClusAnnot(clus, item, vals);
                    for(int j=0; j<vals.length; j++)
                    {
                        info = info+","+nums[j];
                    }
                    vals = new String[1];
                    vals[0] = "";
                    nums = ric.getClusAnnot(clus, item, vals);
                    info = info+","+nums[0];
                }
            }
            info = info+"\n";
            clustInfo = clustInfo+info;
        }
        //end of printing extended cluster info

        if(b == runB)
        {

            //add all sequences from the tree to the file
      long stSeqAdd = System.currentTimeMillis(); //for timing the efficiency of the function
            String totN = Arrays.toString(totNodes);
            totN = totN.substring(1,totN.length()-1);
            totN = totN.replaceAll(", ", "\n\t");
            file = file + "\t"+totN+"\n";
            totN = null;

            //now modify those that are in the cluster
            String[] seqs = ric.getSeqsInCluster(cluster);
            for(int i=0; i<seqs.length; i++)
            {
                String p ="Clust"+cluster+"_"+seqs[i];
                String orig = p;
                //get annot code so can display as tip labels the annots
                String seqAnnot = "";
                if(annot == true)
                    seqAnnot = ric.getTrimmedSeqAnnot(seqs[i]);
                if(!seqAnnot.isEmpty()) seqAnnot = ","+seqAnnot;
                p = p+"[&!color=#0000FF"+seqAnnot+"]"; //make blue because just a cluster.. no match
                //file = file.replaceAll(orig, p); //DON'T USE THIS, IF NAME HAS SPECIAL SYMBOLS WILL MESS UP GREP!!
                file = file.replace(orig, p); //replace the sequence name with seqName + color ... but don't use
            }

            //get the tree code for the whole tree and the cluster
        long treeAdd = System.currentTimeMillis(); //for timing the efficiency of the function
            String tree = "";
            if(ric.isAnnot() && annot)
                tree = ric.getWholeTree(cluster);
            else
                tree = ric.getWholeTree();

            file = file+"\t;\n"
            +"end;\n"
            +"begin trees;\n"
            +"\ttree cluster = [&R] " + ric.getClusterCode(cluster, annot)+"\n"
            +"\ttree wholeTree = [&R] " +tree+"\n"
            +"end;\n";

            //finish off the file
      long stEndFile = System.currentTimeMillis(); //for timing the efficiency of the function
            file = file+"begin figtree;\n"
            +"\tset tipLabels.colorAttribute=\"User Selection\";\n"
            +"\tset tipLabels.displayAttribute=\"Names\";\n"
            +"\tset tipLabels.fontName=\"Arial\";\n"
            +"\tset tipLabels.fontSize=10;\n"
            +"\tset tipLabels.fontStyle=0;\n"
            +"\tset tipLabels.isShown=true;\n"
            +"\tset tipLabels.significantDigits=4;\n"
            +"\tset trees.order=true;\n"
            +"\tset trees.orderType=\"increasing\";\n"
            +"\tset trees.rooting=false;\n"
            +"\tset trees.rootingType=\"User Selection\";\n"
            +"\tset trees.transform=false;\n"
            +"\tset trees.transformType=\"cladogram\";\n"
            +"\tset branchLabels.fontName=\"Arial\";\n"
            +"\tset branchLabels.fontSize=10;\n"
            +"\tset scaleBar.fontSize=11.0;\n"
            +"end;\n";

            printStringToFile(file, clustFile);
       long en = System.currentTimeMillis(); //for timing the efficiency of the function
            if(verbose) //for timing the efficiency of the function
            {
                System.out.println("Print " + cluster + ": \ntot: " + (en - st) + ""
                    + "  1: "+(stSeqAdd-st)+"  2: "+ (treeAdd-stSeqAdd)
                    + "  3: "+(stEndFile-treeAdd)+"  4: "+ (en-stEndFile)
                    + "  loopArray: "+(treeAdd-stSeqAdd));
            }
        }
    }

    /*
     * a generic function that prints a string passed in to a filename passed in
     * While used by the main program for output, this also makes printing debug or test
     * files really easy!!
     */
    public void printStringToFile(String st, String file)
    {
        File outFileN = new File(outFileT.getText().trim(), file);
        if(!file.equals(logFile))
            addFileToList(outFileN.getAbsolutePath());
        PrintWriter p = null;
        try
        {
            p = new PrintWriter(outFileN);
            p.print(st);
        }
        catch(FileNotFoundException e)
        { System.out.println("File not found");
            errorPane(e.getMessage()+" File not found!");}
        catch(IOException e)
        { System.out.println("Problem Reading File");
            errorPane(e.getMessage()+" Problem Reading File");}
        catch(NoSuchElementException e)
        { System.out.println("File may not be valid!!");
            errorPane(e.getMessage()+" File may not be valid!!");}
        finally
        {
            if(p != null)
            {
                p.flush();
                p.close();
            }
        }
    }

    //this is the list that keeps track of the files written in any given run
    //this way the program can tell if another run might overwrite or add
    //to these files... want to prevent this because two different runs looks the same from
    //the file name alone!!
    public void addFileToList(String file)
    {
        String com = "";
        if(!writtenFiles.isEmpty())
            com = "££";
        writtenFiles = writtenFiles+com+file;
    }

    //generic use function, tests if array is empty
    public boolean isArrayEmpty(String [] a)
    {
        for(int i=0; i<a.length; i++)
        {
            if(a[i]!=null && a[i].trim().length()>0)
                return false;
        }
        return true;
    }

    //generic use function, prints elements in an int array
    public void printArray(int [] a)
    {
        for(int i=0; i<a.length; i++)
        {
            System.out.print(a[i]+" ");
        }
        System.out.println();
    }

    //generic use function, prints elements in a String array
    public void printArray(String [] a)
    {
        for(int i=0; i<a.length; i++)
        {
            System.out.print(a[i]+" ");
        }
        System.out.println();
    }

    //generic use function, prints elements in an array
    //allows the user to specify what separates elements in an array
    public void printArray(String [] a, String sep)
    {
        for(int i=0; i<a.length; i++)
        {
            System.out.print(a[i]+sep);
        }
        System.out.println();
    }

    //deletes the files specified in the string array 'files'
    //returns true if all files were deleted successfully
    //displays an error if not
    private boolean deleteFiles(String[] files)
    {
        boolean ok = true;
        for(int i=0; i<files.length; i++)
        {
            File fi = new File(files[i]);
            try{
                if(fi.exists())
                    if(fi.delete()==false)
                        ok = false;
            }
            catch(SecurityException e)
            {
                ok = false;
            }
        }
        if(ok == false)
        {
            String[] mess = {"There was a problem while deleting the previous run.", "Please manually delete the files or change the output folder to print the current run results."};
            JOptionPane.showMessageDialog(null, mess,
                "Error!", JOptionPane.ERROR_MESSAGE);
            validate();
        }
        return ok;
    }

    //pops up a warning pane with the message passed in
    private void warningPane(String e)
    {
        JOptionPane.showMessageDialog(null, e,
                "Warning!", JOptionPane.WARNING_MESSAGE);
        validate();
    }

    //pops up an error pane with the message passed in
    private void errorPane(String e)
    {
        JOptionPane.showMessageDialog(null, e,
                "Error!", JOptionPane.ERROR_MESSAGE);
        validate();
    }

    //this fucntion is called whenever the user changes a selection in the option panel
    //or the outfile panel and checks whether there is valid input for a run - if so, enables preview and printfiles buttons
    //
    //for example, if the user puts in letters instead of numbers for the number of matching sequences,
    //the buttons will not be enabled - same as if the box is blank. Same for all kinds of options that could be changed.
    private void checkOpts()
    {
        boolean enable = true;
        if(filePaneDisabled == false)
            enable = false;
        if(outFileT.getText().trim().isEmpty())
            enable = false;
        else
        {
            if(return1T.getText().trim().isEmpty())
            {
                enable = false;
            }
            else
            {
                try{
                    Integer.parseInt(return1T.getText().trim());
                }
                catch(NumberFormatException e)
                {
                    enable = false;
                }
            }
            if(percCB.isSelected())
            {
                if(percT.getText().trim().isEmpty())
                    enable = false;
                else
                {
                    try{
                        Integer.parseInt(percT.getText().trim());
                    }
                    catch(NumberFormatException e)
                    {
                        enable = false;
                    }
                }
            }
        }
        prevB.setEnabled(enable);
        runB.setEnabled(enable);
        extendInfBPrint.setEnabled(enable);
        validate();
    }

    /*
     * This called anytime the user changes soemthing in the File panel - modifies a file
     * path in a text box, deletes a file path, changes from 1 data set to two data sets (or vice versa),
     * checks or unchecks annotations.
     * If all the appropriate boxes do not have valid input, the 'read files' button is not enabled
     */
    private void checkFiles()
    {
        //if 1 data set selected, annotation file is checked, but empty, disable button
        if(anotDs1CB.isSelected()==true && anotDs1T.getText().trim().isEmpty())
            readFileB.setEnabled(false);
        else //if 1 data set selected, need newick1 & cluster1
        {
            if(newDs1T.getText().trim().isEmpty())
            {
                readFileB.setEnabled(false);
            }
            else if(file2RB.isSelected())//if 2 data set selected, 
            {
                //if 2 data set selected, annotation file is checked, but empty, disable button
                if(anotDs2CB.isSelected()==true && anotDs2T.getText().trim().isEmpty())
                    readFileB.setEnabled(false);
                else if(newDs2T.getText().trim().isEmpty() ) // || matFT.getText().trim().isEmpty())
                    readFileB.setEnabled(false);//need newick2 & cluster2 & matches
                else if(matFT.getText().trim().isEmpty() && !sameMatCB.isSelected())
                    readFileB.setEnabled(false);
                else
                    readFileB.setEnabled(true);
            }
            else
                readFileB.setEnabled(true);
        }
    }

    //when the files are read in, disables the FilePanel and enables the Option panel
    //depending on how many data sets were read in and whether annotated or not
    private void switchPanels()
    {
        String panel = "1 Data Set";
        if(file2RB.isSelected())
        {
            panel = "2 Data Sets";
        }
        if(anotDs1CB.isSelected()) //if annotated
            enableOptP(true); //enable all
        else
            enableOptP(true, false); //do not enable annotation ones.
        optPane.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(panel),
                BorderFactory.createEmptyBorder(5,5,5,5)));

        enableFileP(false);
        checkOpts();
        //removeFileListeners();
    }

    //calls a function that pops up a little box to select files
    //then checks to see if file panel valid
    private void getFile(JTextField t)
    {
        File f = getFile();
        if(f != null)
            t.setText(f.getPath());
        checkFiles();
    }

    //pops up a box to select files
    private File getFile()
    {
        File file = null;
        FileDialog d = new FileDialog(this, "Select file", FileDialog.LOAD);
        d.setDirectory(lastDir);//d.setDirectory(".");
        d.setVisible(true);
        if (d.getFile() != null)
        {
            file = new File(d.getDirectory(), d.getFile());
            lastDir = d.getDirectory();
        }
        return file;
    }

    //calls a function to pop up a box to select an output directory
    //then checks to see if options panel is valid
    private void getDir(JTextField t)
    {
        String dir = getDir(t.getText().trim());
        if( dir != null)
            t.setText(dir);
        checkOpts();
    }

    //pops up a window to select an output directory.
    //has to be two different things inside because apparently JFileChooser doesn't work on OSX
    //however have never had a chance to test this so no idea if it works.
    //if worst comes to worst, user can type in a directory!
    private String getDir(String s)
    {
        if(isMacOSX()) //do something different because apaprently JFileChooser doesnt work on OS X
        {
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            File file = null;
            FileDialog d = new FileDialog(this, "Select folder", FileDialog.LOAD);
            d.setDirectory(".");
            d.setVisible(true);
            if (d.getFile() != null)
                return d.getDirectory();
            return null;
        }
        else //for windows, linux
        {
            String dirToOpen = lastDir;
            if(new File(s).exists())
                dirToOpen = s;
            JFileChooser d = new JFileChooser(dirToOpen);
            d.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            d.showOpenDialog(this);
            File fi = d.getSelectedFile();
            if( fi != null)
                return fi.getPath();
            return null;
        }
    }

    //checks if the computer is OS X, I think. Never tested.
    public boolean isMacOSX()
    {
        String osName = System.getProperty("os.name");
        return osName.startsWith("Mac OS X");
    }

    /*
     * The Big Function.
     * Called when preview or print Figtree Files is called.
     *
     * Does the actual analysis by checking the options and calling the appropriate function
     * Prints files if it was runbutton not previewbutton
     * And checks to make sure that files have not already been printed!
     */
    public void runAnalysis(ActionEvent e)
    {
        //choice cells us whether to continue the run.
        int choice = 0;
        //First, if the user wants to print files, have files already been written?
        if(e.getSource() == runB && !writtenFiles.isEmpty()) //if not empty, means this instance has already printed files.. must delete or change output folder
        {
            String curOutDir = outFileT.getText().trim();
            String oldOutDir = writtenFiles.split("££")[0];
            if(isWindows())
                oldOutDir = oldOutDir.substring(0, oldOutDir.lastIndexOf('\\'));
            else //is unix, mac, or macosx
                oldOutDir = oldOutDir.substring(0, oldOutDir.lastIndexOf('/'));
            //are we still in the same directory as we last printed files? If not, then we can continue.
            if(oldOutDir.equals(curOutDir)) //if we are in same directory, could be adding files to previous output, meaning results are confused
            {
                String[] mess = {"To delete the previous results and replace with the new results, press OK",
                        "To change the output folder and prevent deletion of the previous run, press cancel." };
                choice = JOptionPane.showConfirmDialog(null, mess,
                        "Warning!", JOptionPane.OK_CANCEL_OPTION); //'choice' catches the button that the user presses. 0 is ok
                validate();
                if(choice == 0) //need to delete the previous files.
                {
                    if(deleteFiles(writtenFiles.split("££")) == true) //files deleted properly.. continue
                    {
                        writtenFiles = "";
                        outFile = outFile+"\r\n**Previously Written Files Were Deleted!**\r\n";
                    }
                    else //files did not delete properly... do not continue (deleteFiles function throws up its own warning)
                        choice = 2;
                }
                //if the user presses 'cancel' (that would be choice==1), then run does not proceed, files are not deleted,
                //they get a chance to change the output folder and try again. nothing happens, in short.
            }
        }

        //if files deleted successfully and/or no previous results were found in the current output folder
        if(choice == 0) //continue with the run
        {
            JButton b = (JButton) e.getSource(); //find out if it was runButton or PreviewButton
            int number=0;

            //default values.
            int whi = 1, matNum = 0;
            String field = null, value = null;
            double perc = 0;
            boolean includeNAs = true, bothDS = false;

            //for one or two datasets, we need to get the match or seq number
            String matchNum = return1T.getText().trim();
            matNum = Integer.parseInt(matchNum); //as typing, entry is checked to be a valid int - cannot press button if it's not!

            //now go through all the options and figure out exactly what the user wants to run based on what's selected

            if(file1RB.isSelected())
            {
                if(percCB.isSelected()) //if annotated and want to select by annotation
                {
                    String percent = percT.getText().trim();
                    int perce = Integer.parseInt(percent); //as typing, entry is checked to be a valid int - cannot press button if it's not!
                    perc = ((double)perce)/100.0;

                    field = (String)fieldC.getSelectedItem();
                    value = (String)valueC.getSelectedItem();
                    whi = 1;
                    includeNAs = !incCB.isSelected(); //(if isn't selected, this means INCLUDE nas)

                    number = printClusterAnnots(whi, matNum, field, value, perc, includeNAs, b);
                }
                else if(anotFCB.isSelected()) //if annotated and want to annotate figtree files
                    number = printClustersMoreThan(1, matNum, true, b);
                else //if not annotated (or dont want to annotate), just get clusters with more than x sequences
                    number = printClustersMoreThan(1, matNum, false, b);
            }
            else if(file2RB.isSelected())
            {
                if(percCB.isSelected()) //if annotated and want to select by annotation
                {
                    String percent = percT.getText().trim();
                    int perce = Integer.parseInt(percent); //as typing, entry is checked to be a valid int - cannot press button if it's not!
                    perc = ((double)perce)/100.0;

                    field = (String)fieldC.getSelectedItem();
                    value = (String)valueC.getSelectedItem();
                    whi = 1;
                    bothDS = false;
                    int ds = dsC.getSelectedIndex(); //0 is ds1 (send 1), 1 is ds2 (send 2), 2 is both (send 1 and bothDs=true)
                    if(ds == 1) whi = 2;
                    if(ds == 2) bothDS = true;
                    includeNAs = !incCB.isSelected(); //(if isn't selected, this means INCLUDE nas)

                    //System.out.println("whi: "+whi+" matNum: "+matNum+" field: "+field+" value: "+value+" perc: "+perc+" includeNAs: "+includeNAs+" bothDS: "+bothDS);
                    number = printMatches(whi, matNum, field, value, perc, includeNAs, bothDS, b);

                }
                else if(anotFCB.isSelected()) //if annotated and want to annotate figtree files
                    number = printMatchingClusters(1, matNum, true, b);
                else //if not annotated (or dont want to annotate), just get clusters with more than x matches
                    number = printMatchingClusters(1, matNum, false, b);
            }
            prevT.setText(Integer.toString(number));
            if(verbose) System.out.println("run completed.");

        }
        else
            if(verbose) System.out.println("Run not continued.");
    }

    /*
     * Another big daddy function - catches all button presses and some other stuff and processes them!
     */
    public void actionPerformed(ActionEvent e)
    {
        //if the user selects a certain field value, load the corresponding values in the other choice box
        //(ex: if they chose 'sex', load 'male' and 'female' in the other box
        if(e.getSource() == fieldC)
        {
            if(fieldC.getSelectedIndex() != -1) //if *something* is selected
            {
                valueC.removeAllItems();
                String k = (String)fieldC.getSelectedItem();
                String soFar = anotOps.get(k);
                String[] sf = soFar.split("£&");//(",");
                for(int i=0;i<sf.length; i++)
                {
                    valueC.addItem(sf[i]);
                }
                validate();
            }
        }
        else if(e.getSource() == readFileB) //read file button pressed
        {
            readFiles();
        }
        else if(e.getSource() == runB || e.getSource() == prevB || e.getSource() == extendInfBPrint) //run button (print figtree files) or preview button pressed OR print CSV pressed
        {
            runAnalysis(e);
        }
        //all the 'help' buttons just pop up help dialogues.
        else if(e.getSource() == prevHelpB)
        {
            String[] mess = {"The preview button allows you to see how many files will be returned by this set of parameters, without writing any files.",
                "This allows you to adjust parameters accordingly before files are written.",
                " ",
                "However, details about the clusters returned by pressing the preview button are printed in the log file in real time, so by refreshing",
                "the log file while trying different previews, you can see more information about the clusters being returned."};
            JOptionPane.showMessageDialog(null, mess,
                "Preview Button", JOptionPane.INFORMATION_MESSAGE);
            validate();
        }
        else if(e.getSource() == percHelpB)
        {
            String[] mess;
            if(file1RB.isSelected())
                mess = new String[]{"Will return only clusters where at least x% of their sequences have the specified value.",
                    " ",
                    "Only fields with less than 11 possible values are displayed."};
            else
                mess = new String[]{"Will return only clusters where at least x% of their sequences have the specified value.",
                    "This can be applied only to clusters in one data set (only the cluster from this data set must fit the criteria - not its match),",
                    " or to both data sets (a cluster and its match must both fit the criteria to be included).",
                    " ",
                    "Only fields present in both data sets with less than 11 possible values are displayed."};
            JOptionPane.showMessageDialog(null, mess,
                "Selecting by Annotation", JOptionPane.INFORMATION_MESSAGE);
            validate();
        }
        else if(e.getSource() == anotFB)
        {
            String [] mess = {"Writes the annotations for each sequence into the FigTree file, so that line colour, tip names, and tip colour ",
                    "can be changed to reflect annotation.",
                    " ",
                    "This is compulsory if you have chosen to select clusters by an annotation value so that users can be sure of",
                    "the validity of the selection."};
            JOptionPane.showMessageDialog(null, mess,
                "Embedding Annotations", JOptionPane.INFORMATION_MESSAGE);
        }
        else if(e.getSource() == extendInfB)
        {
            String [] mess = {
                    "Writes a .csv file with a line for each cluster detailing the dataset it is from (if applicable), the cluster ID, the ID",
                    "of matching clusters (if applicable), the number of sequences in the cluster, and the number of sequences that match",
                    "between clusters (if applicable).",
                    " ",
                    "If annotations have been provided, the file will also include the number of sequences that fall into each possible field",
                    "value. For example: 2 female, 1 male, 2 sex_NA. Please note ONLY fields displayed by the program will be printed",
                    "(must be present in both data sets with less than 11 possible values).",
                    " ",
                    "Can be useful for gathering information about clusters without having to hand-count."};
            JOptionPane.showMessageDialog(null, mess,
                "Extended Information File", JOptionPane.INFORMATION_MESSAGE);
        }
        else if(e.getSource() == incB)
        {
            String [] mess = {"By default, only sequences with a value for the trait selected are considered when calculating the percentage.",
                    "For example, a cluster with 4 sequences has two listed as 'Male' and two with no value. By default, this cluster",
                    "is considered 100% 'Male'.",
                    " ",
                    "However, if the 'Out of all sequences...' option is selected, sequences with no value will be included in calculating",
                    "the percentage.",
                    "For example, the same cluster as above would now be considered 50% 'Male'."};
            JOptionPane.showMessageDialog(null, mess,
                "Not Including Sequences without a Value", JOptionPane.INFORMATION_MESSAGE);
            validate();
        }
        else if(e.getSource() == outHelpB)
        {
            String [] mess;
            if(file1RB.isSelected())
                mess = new String[] {"A FigTree file is produced for each cluster that fits the criteria, containing two trees - one of the cluster only",
                    "and one of the whole tree.",
                    "The sequences in the cluster will be coloured blue.",
                    " ",
                    "A log file is also written in real time, updated every time 'Preview' or 'Produce FigTree Files' is pressed. This contains",
                    "more detailed information about the clusters returned by the criteria currently applied, and allows you to explore the data in",
                    "real time if you refresh the log file after each button press."};
            else
                mess = new String[] {"A FigTree file is produced for each cluster that fits the criteria, containing at least 4 trees - one of the whole 'Data Set 1'",
                                    "tree, one of just the 'Data Set 1' cluster, one for each matching 'Data Set 2' cluster, and one of the whole 'Data Set 2' tree.",
                                    " ",
                                    "The sequences are colour-coded: a cluster sequence with a match between the two data sets will be red, a cluster sequence",
                                    "in either data set without a match will be blue, and a cluster sequence in the second data set that was in a different",
                                    "cluster in the first data set will be green.",
                                    " ",
                                    "Cluster sequences in the first data set with matches have their matching 'Data Set 2' sequence name attached to the end of",
                                    "their 'Data Set 1' sequence name between curly braces {}.",
                                    " ",
                                    "A log file is also written in real time, updated every time 'Preview' or 'Produce FigTree Files' is pressed. This contains",
                                    "more detailed information about the clusters returned by the criteria currently applied, and allows you to explore the data in",
                                    "real time if you refresh the log file after each button press."
                };

            JOptionPane.showMessageDialog(null, mess,
                "Output Information", JOptionPane.INFORMATION_MESSAGE);
            validate();
        }
        else if(e.getSource() == matHelpB)
        {
            if(file2RB.isSelected())
            {
               // String [] mess = {"A .csv output file from SeqMatcher, with pairs of matching sequences. Please ensure that the sequences in the left-hand",
               //         "column correspond to the files in 'Data Set 1', and the sequences in the right-hand column correspond to the files in 'Data Set 2'."};

                String [] mess = {"If matching sequences have the same name in both data sets, simply select \"Sequence names are the same in both data sets.\"",
                        "When this option is selected, due to restrictions in FigTree, the annotation data in 'Data Set 2' will be associated with BOTH data sets.",
                        "For optimal operation, supply an annotation file with data for ALL sequences in 'Data Set 1' AND 'Data Set 2'.",
                        " ",
                        "If matching sequences have different names between data sets, select the two-column .csv file that shows which sequence names should",
                        "be considered matches. Ensure that the sequences in the left-hand column correspond to 'Data Set 1', and the sequences in the",
                        "right-hand column correspond to 'Data Set 2'.",
                        "**All sequence names must be unique!** (A sequence name in 'Data Set 1' cannot also be present in 'Data Set 2'!)"};

                String [] mess2 = {"If matching sequences have the same name in both data sets, simply select \"Sequence names are the same in both data sets.\"",
                        " ",
                        "If matching sequences have different names between data sets, select the two-column .csv file that shows which sequence names should",
                        "be considered matches. Ensure that the sequences in the left-hand column correspond to 'Data Set 1', and the sequences in the",
                        "right-hand column correspond to 'Data Set 2'.",
                        "**All sequence names must be unique!** (A sequence name in 'Data Set 1' cannot also be present in 'Data Set 2'!)"};
                if(anotDs1CB.isSelected()) //if have selected to use annotations, show first message
                    JOptionPane.showMessageDialog(null, mess,
                        "Matching Sequences File", JOptionPane.INFORMATION_MESSAGE);
                else //otherwise show second message
                    JOptionPane.showMessageDialog(null, mess2,
                        "Matching Sequences File", JOptionPane.INFORMATION_MESSAGE);
                validate();
            }
        }
        //allows you to hard-code in file pathways that automatically fill when the cheat button is pressed.
        else if(e.getSource() == cheat)
        {
            
            //newDs1T.setText("test\\newick2a.nwk");
            newDs1T.setText("test\\2007CTree_clusterPicks.nwk");
            //newDs2T.setText("test\\newick1.nwk");
            newDs2T.setText("test\\2007CTree_clusterPicks_clusDiff.nwk");

  //          anotDs1T.setText("test\\annotData2007.csv");
  //          anotDs1CB.setSelected(true);
  //          anotDs2T.setText("test\\annotData2010_new.csv");

           // matFT.setText("test\\matches.csv");

            //outFileT.setText("C:\\Users\\Emma\\Documents\\My Dropbox\\Personal\\Code\\ClustMatch");
            //outFileT.setText("C:\\Users\\Emma\\Documents\\My Dropbox\\Personal\\Code\\ClustMatch\\out");
            outFileT.setText("C:\\Users\\Emma\\Documents\\My Dropbox\\Personal\\Code\\ClustMatch1.2.3\\out");
            
            /*
//for testing matching sequence names in datasets
            newDs1T.setText("test\\2007CTree_clusterPicks.nwk");
            //newDs2T.setText("test\\2007CTree_clusterPicks.nwk");
            newDs2T.setText("test\\2007CTree_clusterPicks_difClusNum.nwk");
            anotDs1T.setText("test\\annotData2007.csv");
            anotDs1CB.setSelected(true);
            anotDs2T.setText("test\\annotData2007.csv");
            sameMatCB.setSelected(true);
            outFileT.setText("C:\\Users\\Emma\\Documents\\My Dropbox\\Personal\\Code\\ClustMatch1.1");
            */
            
            checkFiles();
        }
        /* //this was used as debug to check that disabling/enabling worked properly
        else if(e.getSource() == cheat2)
        {
            if(percP1a.getBorder() == null)
            {
                percP1.setBorder(BorderFactory.createLineBorder(Color.BLUE));
                percP1a.setBorder(BorderFactory.createLineBorder(Color.GREEN));
                percP1b.setBorder(BorderFactory.createLineBorder(Color.RED));
                percP1c.setBorder(BorderFactory.createLineBorder(Color.YELLOW));
                percP2.setBorder(BorderFactory.createLineBorder(Color.RED));
                percP3.setBorder(BorderFactory.createLineBorder(Color.YELLOW));
                percP4.setBorder(BorderFactory.createLineBorder(Color.GREEN));
            }
            else
            {
                percP1.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
                percP1a.setBorder(null);
                percP1b.setBorder(null);
                percP1c.setBorder(null);
                percP2.setBorder(null);
                percP3.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
                percP4.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
            }
        }*/
        else
            if(verbose) System.out.println(e.getSource()); //if it was something else pressed.. what could it be??
    }

    /*
     * this is triggered when checkboxes and radiobuttons are clicked.
     */
    public void itemStateChanged(ItemEvent e)
    {
        //if chose 1 data set, adjust GUI
        if(e.getSource() == file1RB)
        {
            if(file1RB.isSelected())
            {
                twoDataSets = false;
                //for the file selection part
                filePane2.setEnabled(false);
                newDs2L.setForeground(new Color(238,238,238));
                anotDs2CB.setForeground(new Color(238,238,238));
                
                //anotDs2CB.setIcon(new ImageIcon(getClass().getResource("invisBox.jpg")));
                
                // SJL 11 June 2013 - trying to make work in Eclipse
                //anotDs2CB.setIcon(new ImageIcon(getClass().getResource("invisBox.jpg")));
                anotDs2CB.setIcon(new ImageIcon("src\\invisBox.jpg"));
                
                anotDs2CB.setFocusable(false);
                sameMatCB.setForeground(new Color(238,238,238));
                
                // SJL 11 June 2013 - trying to make work in Eclipse
                //sameMatCB.setIcon(new ImageIcon(getClass().getResource("invisBox.jpg")));
                sameMatCB.setIcon(new ImageIcon("src\\invisBox.jpg"));
                
                sameMatCB.setFocusable(false);

                newDs2T.setEditable(false);
                newDs2T.setForeground(new Color(238,238,238));
                newDs2T.setBorder(BorderFactory.createLineBorder(new Color(238,238,238),2));
                anotDs2T.setEditable(false);
                anotDs2T.setForeground(new Color(238,238,238));
                anotDs2T.setBorder(BorderFactory.createLineBorder(new Color(238,238,238),2));

                if(sameMatCB.isSelected()) //need to re-allow annotation file selection
                {
                    anotDs1CB.setEnabled(true);
                    anotDs1T.setEnabled(true);
                    anotDs1T.setText(anot1File);
                }

                percHelpBP.setBorder(null);

                filePane2.setBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(238,238,238)) , "Data Set 2", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Arial Bold", Font.PLAIN, 12) , new Color(238,238,238)),
                          BorderFactory.createEmptyBorder(5,5,5,5))); 
        /*        filePane2.setBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK) , "Data Set 2", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Arial Bold", Font.PLAIN, 12) , Color.BLACK),
                          BorderFactory.createEmptyBorder(5,5,5,5))); */

                matFL.setForeground(new Color(238,238,238));
                matFT.setEditable(false);
                matFT.setForeground(new Color(238,238,238));
                matFT.setBorder(BorderFactory.createLineBorder(new Color(238,238,238),2));

                matHelpB.setForeground(new Color(238,238,238));
                matHelpB.setBackground(new Color(238,238,238));
                matHelpB.setBorderPainted(false);
                matHelpB.setFocusable(false);
                
                // SJL 11 June 2013 - trying to make work in Eclipse
                //matHelpB.setPressedIcon(new ImageIcon(getClass().getResource("pressIco.jpg")));
                //matHelpB.setSelectedIcon(new ImageIcon(getClass().getResource("pressIco.jpg")));
                matHelpB.setPressedIcon(new ImageIcon("src/pressIco.jpg"));
                matHelpB.setSelectedIcon(new ImageIcon("src/pressIco.jpg"));
                
                optPane.setBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,1),"1 Data Set", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial Bold", Font.PLAIN, 12), Color.GRAY),
                        BorderFactory.createEmptyBorder(6,6,6,6)));

                return2Lb.setForeground(new Color(238,238,238));
                perc4L.setForeground(new Color(238,238,238));
                dsC.setEnabled(false);
                dsC.setVisible(false);

            }
            checkFiles();
        }
        //if chose 2 data sets, adjust GUI
        else if(e.getSource() == file2RB)
        {
            if(file2RB.isSelected())
            {
                twoDataSets = true;
                //for the file selection part
                filePane2.setEnabled(true);
                newDs2L.setEnabled(true);
                newDs2L.setForeground(newDs1T.getForeground());
                anotDs2CB.setForeground(newDs1T.getForeground());
                anotDs2CB.setIcon(null);
                anotDs2CB.setSelected(anotDs1CB.isSelected());
                anotDs2CB.setFocusable(true);
                sameMatCB.setForeground(newDs1T.getForeground());
                sameMatCB.setIcon(null);
                sameMatCB.setSelected(useMatches);
                sameMatCB.setFocusable(true);

                newDs2T.setForeground(Color.BLACK);
                newDs2T.setEditable(true);
                newDs2T.setBorder(newDs1T.getBorder());
                anotDs2T.setEditable(true);
                anotDs2T.setForeground(Color.BLACK);
                anotDs2T.setBorder(newDs1T.getBorder());

                if(sameMatCB.isSelected()) //need to un-allow annotation file selection
                {
                    anotDs1CB.setEnabled(false);
                    anotDs1T.setEnabled(false);
                    anotDs1T.setForeground(Color.BLACK);
                    if(anotDs2CB.isSelected()) //only show it will not be used if they're trying to use it.
                        anotDs1T.setText("Will not be used (see '?' below)");
                }

                percHelpBP.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));

                if(!anotDs2CB.isSelected())
                {
                    anotDs2CB.setForeground(new Color(153,153,153));
                    anotDs2T.setEditable(false);
                }

                filePane2.setBorder(
                            BorderFactory.createCompoundBorder(
                                            BorderFactory.createTitledBorder("Data Set 2"),
                                            BorderFactory.createEmptyBorder(5,5,5,5))); 

                //if the use matching seq names button isn't checked, then make matches file box editable
                if(!sameMatCB.isSelected())
                {
                    matFL.setForeground(newDs1T.getForeground());
                    matFT.setEditable(true);
                    matFT.setForeground(Color.BLACK);
                }
                else // else keep the matches file box disabled-looking
                {
                    matFL.setForeground(new Color(153,153,153));
                    matFT.setEditable(false);
                    matFT.setForeground(Color.BLACK);
                }

                matFT.setBorder(newDs1T.getBorder());

                matHelpB.setForeground(Color.BLUE);
                matHelpB.setBackground(outHelpB.getBackground());
                matHelpB.setBorderPainted(true);
                matHelpB.setFocusable(true);

                optPane.setBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,1),"2 Data Sets", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial Bold", Font.PLAIN, 12), Color.GRAY),
                        BorderFactory.createEmptyBorder(6,6,6,6)));

                //new things that make data set 2 pane look inactive (until load file button pressed)
                return2Lb.setForeground(new Color(153,153,153));
                perc4L.setForeground(new Color(153,153,153));
                dsC.setVisible(true);
                dsC.setEnabled(false);
            }
            checkFiles();
        }
        //if choose to add annotation files by clicking the annotation checkbox by data set 2
        else if(e.getSource() == anotDs2CB)
        {
            //only do something if 2 data sets are selected.. if just one data set this checkbox isn't visible
            //...do nothing (wonder how they clicked it then?)
            if(file2RB.isSelected()) 
            {
                if(anotDs2CB.isSelected()) //if it is 'checked' select the data set 1 annotation checkbox and make selecting files possible
                {
                    anotDs2CB.setForeground(Color.BLACK);
                    anotDs2T.setEditable(true);
                    anotDs1CB.setSelected(true);
                    anotDs1CB.setForeground(Color.BLACK);
                    anotDs1T.setEditable(true);
                    //if using same seq names, show the message in annot file 1 that annot file 1 will not be used
                    if(sameMatCB.isSelected()) 
                        anotDs1T.setText("Will not be used (see '?' below)");
                }
                else //if is 'de-checked' de-select the data set 1 annotation checkbox and make selecting files not possible
                {
                    anotDs2CB.setForeground(new Color(153,153,153));
                    anotDs2T.setEditable(false);
                    anotDs1CB.setSelected(false);
                    anotDs1CB.setForeground(new Color(153,153,153));
                    anotDs1T.setEditable(false);
                    if(sameMatCB.isSelected())
                        anotDs1T.setText(anot1File);
                }
            }
            checkFiles();
        }
        //same thing pretty much if they select the annotation checkbox next to data set 1
        else if(e.getSource() == anotDs1CB)
        {
            if(anotDs1CB.isSelected())
            {
                anotDs1CB.setForeground(Color.BLACK);
                anotDs1T.setEditable(true);
                if(file2RB.isSelected()) //enable the other annotation stuff by data set 2 only if two data sets is selected
                {
                    anotDs2CB.setSelected(true);
                    anotDs2CB.setForeground(Color.BLACK);
                    anotDs2T.setEditable(true);
                }

            }
            else
            {
                anotDs1CB.setForeground(new Color(153,153,153));
                anotDs1T.setEditable(false);
                if(file2RB.isSelected()) //disable the other annotation stuff by data set 2 only if two data sets is selected
                {
                    anotDs2CB.setSelected(false);
                    anotDs2CB.setForeground(new Color(153,153,153));
                    anotDs2T.setEditable(false);
                }
            }
            checkFiles();
        }
        //if user wants to use the same seq names to match instead of a file, disable file selection text box
        //also if annotation files are selected, get rid of the annotation file in DS1
        else if(e.getSource() == sameMatCB)
        {
            if(sameMatCB.isSelected() && file2RB.isSelected())
            {
                matFL.setForeground(new Color(153,153,153));
                matFT.setEditable(false);
                anotDs1CB.setEnabled(false);
                anotDs1T.setEnabled(false);
                anot1File = anotDs1T.getText().trim();
                anotDs1T.setForeground(Color.BLACK);
                if(anotDs2CB.isSelected()) //only show it will not be used if they're trying to use it.
                        anotDs1T.setText("Will not be used (see '?' below)");
                useMatches = true;
            }
            else if(!sameMatCB.isSelected() && file2RB.isSelected())
            {
                matFL.setForeground(Color.BLACK);
                matFT.setEditable(true);
                matFT.setForeground(Color.BLACK);
                anotDs1CB.setEnabled(true);
                anotDs1T.setEnabled(true);
                anotDs1T.setText(anot1File);
                useMatches = false;
            }
            checkFiles();
            //System.out.println("Use Matches is "+useMatches);
        }
        //cannot decide to select by annotations and not have annotations printed in the figtree file! Must have both!
        else if(e.getSource() == anotFCB)
        {
            if(!anotFCB.isSelected() && percCB.isSelected())
                percCB.setSelected(false);
        }
        //if the user wants to select clusters by their annotation, enable the drop-down boxes
        else if (e.getSource() == percCB)
        {
            if(percCB.isSelected())
            {
                enableIncP(true);
                anotFCB.setSelected(true);
            }
            else
            {
                enableIncP(false);
                incCB.setSelected(false);
            }
            checkOpts();   
        }
        validate();
    }

    //just makes the first part of the output file with generic data set info
    public void makeOutputFile1()
    {
        outFile = outFile+ "Input files used:\r\n";
        if(file2RB.isSelected())
             outFile = outFile+"Data Set 1:\r\n";
        outFile = outFile+"\tNewick file: "+newDs1T.getText().trim()+"\r\n";
        if(anotDs1CB.isSelected())
            outFile = outFile+"\tAnnotation file: "+anotDs1T.getText().trim()+"\r\n";

        //add data files for data set 2
        outFile = outFile+"Data Set 2:\r\n";
        outFile = outFile+"\tNewick file: "+newDs2T.getText().trim()+"\r\n";
        if(anotDs2CB.isSelected())
            outFile = outFile+"\tAnnotation file: "+anotDs2T.getText().trim()+"\r\n";

        if(file2RB.isSelected())
            outFile = outFile+"\r\nData set 1 has ";
        else
            outFile = outFile+"\r\nThe data set has ";
        double perc = roundTwoDecimals(((double)ric1.getNodesInClusters()/(double)ric1.getTotalNodes().length)*100);
        outFile = outFile+ric1.getTotalNodes().length+" sequences and "+ric1.getNumClusters()+" clusters (containing "+ric1.getNodesInClusters()+" sequences ("+perc+"%))";
        
        //add data 2 file info and match info
        if(file2RB.isSelected())
        {
            outFile = outFile + "\r\nData set 2 has ";
            perc = roundTwoDecimals(((double)ric2.getNodesInClusters()/(double)ric2.getTotalNodes().length)*100);
            outFile = outFile+ric2.getTotalNodes().length+" sequences and "+ric2.getNumClusters()+" clusters (containing "+ric2.getNodesInClusters()+" sequences ("+perc+"%))";

            /* Uncomment this bit if you want to test that the two datasets are identical (all seq names same in both)
            String[] totNodes = ric1.getSeqNames();  //returns string array of all nodes in RIC, without prefix (7122)
            String[] ric2TotNodes = ric2.getSeqNames();

            java.util.List ric1List = Arrays.asList(totNodes); //get list of all seqs in data set 1
            java.util.List ric2List = Arrays.asList(ric2TotNodes); //get list of all seqs in data set 2
            HashSet set = new HashSet(ric1List); //put into hashed set - will disregard duplicates!!
            HashSet set2 = new HashSet(ric2List);
            if(set.equals(set2)) //if all sequence names within are identical, we can try to do the test...
            {
                outFile = outFile+"\r\n\r\nData set 1 and 2 have identical sequences. An adjusted Rand Index is possible.";
            }
            */
        }
        outFile = outFile+"\r\n\r\n";

        if(verbose) System.out.print(outFile);
    }

    //adds onto the output log file for every preview or run that is selected!
    //this was harder than i thought it was going to be.
    public void makeOutputFile2(int number, String[] clus, JButton b)
    {
        int num = Integer.parseInt(return1T.getText().trim());
        String[] tmp;
        if(file1RB.isSelected())
            tmp = ric1.returnClustersMoreThan(num);
        else
            tmp = ric1.returnClusterMatchMoreThan(num);
        int numClus = 0;
        if(tmp != null)
            numClus = tmp.length;
        double perc = roundTwoDecimals(((double)numClus/(double)ric1.getNumClusters())*100);

        if(b == prevB)
            outFile = outFile+"\r\n*Preview Analysis* (No files written):\r\n";
        else if(b == runB){ //if run button
        
            outFile = outFile+"\r\n*FigTree Files Written*:\r\n";
            outFile = outFile+"Output Location: "+outFileT.getText().trim()+"\r\n";
        }
        else //if print CSV Button
        {
            outFile = outFile+"\r\n*CSV Cluster File Written*:\r\n";
            outFile = outFile+"Output Location: "+outFileT.getText().trim()+"\r\n";
        }

        String hk="", pj="", hj=" of the total clusters";
        if(file2RB.isSelected())
            hk = " that match between data sets"; pj = " in data set 1"; hj="";
        outFile = outFile+numClus+" clusters"+pj+" ("+perc+"%) have more than "+num+" sequences"+hk+".\r\n";
        if(percCB.isSelected())
        {
            int prev = number;
            perc = roundTwoDecimals(((double)prev/(double)ric1.getNumClusters())*100);
            outFile = outFile + "Of these, " + prev + " clusters ("+perc+"%"+hj+") have at least " + percT.getText().trim() + "% "
                    +"sequences with a "+fieldC.getSelectedItem()+" value of "+valueC.getSelectedItem();
            if(file2RB.isSelected() && dsC.getSelectedIndex()!=2)
                outFile = outFile + " in "+dsC.getSelectedItem()+"\r\n";
            else if(file2RB.isSelected() && dsC.getSelectedIndex()==2)
                outFile = outFile + " in both data sets\r\n";
            else
                outFile = outFile + "\r\n";
        }
        if(incCB.isSelected())
            outFile = outFile+"\t(Sequences without a value for "+fieldC.getSelectedItem()+" were not included.)\r\n";

        if(percCB.isSelected())
        {
            if(number != 0)
            {
                if(file1RB.isSelected())
                {
                    int numSeqs = ric1.getNumSeqsInClus(clus);
                    outFile = outFile+"\tOf the "+numSeqs+" sequences in these clusters:\r\n";
                    String values = anotOps.get(fieldC.getSelectedItem());
                    String[] vals = values.split(",");
                    int[] tots = ric1.getClusAnnot(clus, (String)fieldC.getSelectedItem(), vals);
                    int totVal = 0;
                    for(int i=0; i<vals.length; i++)
                    {
                        perc = roundTwoDecimals(((double)tots[i]/(double)numSeqs)*100);
                        outFile = outFile+"\t\t- "+tots[i]+" ("+perc+"%) are "+vals[i]+"\r\n";
                        totVal = totVal + tots[i];
                    }
                    perc = roundTwoDecimals(((double)(numSeqs-totVal)/(double)numSeqs)*100);
                    outFile = outFile+"\t\t- "+(numSeqs-totVal)+" ("+perc+"%) have no "+fieldC.getSelectedItem()+" value\r\n";
                }
                else if(file2RB.isSelected())
                {
                    int numSeqs = ric1.getNumSeqsInClus(clus);
                    outFile = outFile+"\tOf the "+numSeqs+" sequences in the clusters returned from data set 1:\r\n";
                    String values = anotOps.get(fieldC.getSelectedItem());
                    String[] vals = values.split("£&");//(",");
                    int[] tots = ric1.getClusAnnot(clus, (String)fieldC.getSelectedItem(), vals);
                    int totVal = 0;
                    for(int i=0; i<vals.length; i++)
                    {
                        perc = roundTwoDecimals(((double)tots[i]/(double)numSeqs)*100);
                        outFile = outFile+"\t\t- "+tots[i]+" ("+perc+"%) are "+vals[i]+"\r\n";
                        totVal = totVal + tots[i];
                    }
                    perc = roundTwoDecimals(((double)(numSeqs-totVal)/(double)numSeqs)*100);
                    outFile = outFile+"\t\t- "+(numSeqs-totVal)+" ("+perc+"%) have no "+fieldC.getSelectedItem()+" value\r\n";

                    //for data set 2
                    String matches="";
                    for(int i=0; i<clus.length; i++)
                    {
                        String[] n = ric1.getClusterMatches(clus[i]);
                        if(n != null)
                        {
                            for(int j=0; j<n.length; j++)
                                matches=matches+n[j]+",";
                        }
                    }

                    String[] mats = matches.split(",");
                    numSeqs = ric2.getNumSeqsInClus(mats);
                    outFile = outFile+"\tOf the "+numSeqs+" sequences in the matching clusters returned from data set 2:\r\n";
                    tots = ric2.getClusAnnot(mats, (String)fieldC.getSelectedItem(), vals);
                    totVal = 0;
                    for(int i=0; i<vals.length; i++)
                    {
                        perc = roundTwoDecimals(((double)tots[i]/(double)numSeqs)*100);
                        outFile = outFile+"\t\t- "+tots[i]+" ("+perc+"%) are "+vals[i]+"\r\n";
                        totVal = totVal + tots[i];
                    }
                    perc = roundTwoDecimals(((double)(numSeqs-totVal)/(double)numSeqs)*100);
                    outFile = outFile+"\t\t- "+(numSeqs-totVal)+" ("+perc+"%) have no "+fieldC.getSelectedItem()+" value\r\n";
                }
            }
        }

        if(verbose) System.out.print(outFile);
        //names the logFile the first time a run or preview is done, then keeps writing to that same file
        if(logFile.isEmpty())
        {
            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DATE);
            int month = cal.get(Calendar.MONTH)+1; //+1 because it starts numbering months from 0...
            int year = cal.get(Calendar.YEAR);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            String hr = Integer.toString(hour);
            int min = cal.get(Calendar.MINUTE);
            String mn = Integer.toString(min);
            if(hour<10)
                hr = "0"+hr;
            if(min<10)
                mn = "0"+mn;

            timeN = hr+"."+mn;
            logFile = "log_"+year+"-"+month+"-"+day+"_"+hr+"."+mn+".txt";
        }

        printStringToFile(outFile, logFile);
    }

    //I got this code online... from a forum, after searching something like 'round decimal places in java'
    private double roundTwoDecimals(double d)
    {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

    //detects typing in the percent or number of sequences textbox
    public void changedUpdate(DocumentEvent e)
    {
        if(e.getDocument() == percT.getDocument())
        {
            checkOpts();
        }
        else if(e.getDocument() == return1T.getDocument())
        {
            checkOpts();
        }
    }

    //detects typing in the percent or number of sequences textbox
    public void insertUpdate(DocumentEvent e)
    {
        if(e.getDocument() == percT.getDocument())
        {
            checkOpts();
        }
        else if(e.getDocument() == return1T.getDocument())
        {
            checkOpts();
        }
    }

    //detects deleting in the percent or number of sequences textbox, or outfile textbox
    //and also in the file textboxes
    public void removeUpdate(DocumentEvent e)
    {
        //System.out.println("part of doc removed "+e.getDocument().toString());
        if(filePaneDisabled == false)
            checkFiles();
        else
            if(e.getDocument() == percT.getDocument())
            {
                checkOpts();
            }
            else if(e.getDocument() == return1T.getDocument())
            {
                checkOpts();
            }
            else if(e.getDocument() == outFileT.getDocument())
            {
                checkOpts();
            }

    }

    //required to have these but do not need them for my program
    public void mousePressed(MouseEvent e) 
    {
    }

    public void mouseReleased(MouseEvent e) 
    {
    }

    public void mouseEntered(MouseEvent e) 
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }

    //detects mouse clicks in the file dialogues, so that the program knows to
    //pop up a window to select a file
    public void mouseClicked(MouseEvent e)
    {
        if(e.getSource() == outFileT)
        {
            getDir((JTextField)e.getSource());
        }
        else if(filePaneDisabled == false)
        {
            if(e.getSource() == anotDs1T) // only show file selection if have checked annotation box
            {
                if(anotDs1CB.isSelected() && !sameMatCB.isSelected()) //only allow to choose if it will be used!
                    getFile((JTextField) e.getSource());
            }
            else if(e.getSource() == anotDs2T)
            {
                if(anotDs2CB.isSelected() && file2RB.isSelected())
                    getFile((JTextField) e.getSource());
            }
            else if(e.getSource() == newDs2T )// || e.getSource() == matFT)
            {
                if(file2RB.isSelected()) //only show if 2 data sets are selected
                    getFile((JTextField) e.getSource());
            }
            else if(e.getSource() == matFT)
            { //only show if 2 data sets are selected and if they HAVENT selected to use same seq names to match
                if(file2RB.isSelected() && !sameMatCB.isSelected())
                    getFile((JTextField) e.getSource());
            }
            else
                getFile((JTextField) e.getSource());
        }
    }

    //This detects whether the 'cancel' button was pressed when writing out files
    //and whether the progress bar has been updated, and handles both.
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName() ) {
            int progress = (Integer) evt.getNewValue();

            pm.setProgress(progress);
            if (pm.isCanceled())
            {
                if(evt.getSource() == task)
                    task.cancel(true);
                else if(evt.getSource() == task2)
                    task2.cancel(true);
                outFile = outFile + "***RUN WAS CANCELLED BEFORE ALL FILES WERE WRITTEN!!!***\r\n";
                printStringToFile(outFile, logFile);
            } 
        }

    }

    private String[] splitFile(String file)
    {
        if(isMac() || isUnix())
        {
            int p = file.lastIndexOf("/");
            if(verbose) System.out.println("IS MAC/UNIX! last index of / is at : "+p);
            String[] ret = {file.substring(p+1,file.length()), file.substring(0,p)}; //name first, then dir
            return ret;
        }
        else //assume is windows
        {
            int p = file.lastIndexOf("\\");
            if(verbose) System.out.println("IS WINDOWS! last index of \\ is at : "+p);
            String[] ret = {file.substring(p+1,file.length()), file.substring(0,p)}; //name first, then dir
            return ret;
        }
    }

    //code from http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
    private boolean isWindows()
    {
        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.indexOf("win") >= 0);
    }

    //code from http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
    private boolean isMac()
    {
        String os = System.getProperty("os.name").toLowerCase();
        // Mac
        return (os.indexOf("mac") >= 0);
    }

    //code from http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
    private boolean isUnix()
    {
        String os = System.getProperty("os.name").toLowerCase();
        // linux or unix
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
    }

    public void windowClosing(WindowEvent e)
    {
        dispose();
        System.exit(0);
    }
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    //Task is a monitor class that monitors how the file writing is going
    //when TWO data sets are loaded, it calls the function to write the files and
    //and updates the progressbar
    class Task extends SwingWorker<Void, Void>
    {

        String[] clus;
        int whi =0;
        boolean annot;
        boolean wasAnotFCBEnab;
        JButton b;

        public Task()
        {

        }

        public Task(String[] clus1, int whi1, boolean annot1, JButton bb)
        {
            clus = clus1;
            whi = whi1;
            annot = annot1;
            b = bb;
        }

        public Void doInBackground()
        {
            //if there are a lot of files to be written, stops user from changing request on annotations
            //and or extended info halfway through file write...!
            extendInfCB.setEnabled(false);
            wasAnotFCBEnab = anotFCB.isEnabled();
            anotFCB.setEnabled(false);
            setProgress(1);
            double interv = 100.0/(double)clus.length;
        //  System.out.println("interval is: "+interv);

            for(int i=0; i<clus.length; i++)
            {
                if(isCancelled())
                    break;
                String message =
                        String.format("Writing file %d.\n", i+1);
                pm.setNote(message);

                if(useMatches == true)
                    printClusterMatchSame(whi, clus[i], annot, b);
                else
                    printClusterMatch(whi, clus[i], annot, b);

                double upd = ((i+1)*interv) ;
                //System.out.println("current percent is: "+upd);
                int p = (int)Math.floor(upd);
                //System.out.println("setting prgress to: "+p);
                setProgress(p);
                if(i==clus.length && p != 100) //if at end of run and progress hasn't been completed on bar, do it now.
                    setProgress(100);
            } 
            return null;
        }

        public void done()
        {
            extendInfCB.setEnabled(true);
            anotFCB.setEnabled(wasAnotFCBEnab);
            //prints extended cluster info if requested by the user:
            if(extendInfCB.isSelected() || b == extendInfBPrint) //if tickbox selected OR button was pressed)
            {
                printStringToFile(clustInfo, "clustInfo_"+numWrites+"_"+timeN+".csv");
                addFileToList("clustInfo_"+numWrites+"_"+timeN+".csv");
                numWrites++;
            }
        }

        public int getLengthOfTask()
        {
            //return clus.length;
            return 100;
        }

    }

    //Task is a monitor class that monitors how the file writing is going
    //when ONE data set is loaded, it calls the function to write the files and
    //and updates the progressbar
    class Task2 extends SwingWorker<Void, Void>
    {

        String[] clus;
        int whi =0;
        boolean annot;
        boolean wasAnotFCBEnab;
        JButton b;

        public Task2(String[] clus1, int whi1, boolean annot1, JButton bb)
        {
            clus = clus1;
            whi = whi1;
            annot = annot1;
            b = bb;
        }

        public Void doInBackground()
        {
            //if there are a lot of files to be written, stops user from changing request on annotations
            //and or extended info halfway through file write...!
            extendInfCB.setEnabled(false);
            wasAnotFCBEnab = anotFCB.isEnabled();
            anotFCB.setEnabled(false);
            setProgress(1);
            double interv = 100.0/(double)clus.length;
            //System.out.println("interval is: "+interv);

            for(int i=0; i<clus.length; i++)
            {
                if(isCancelled())
                    break;
                String message =
                        String.format("Writing file %d.\n", i+1);
                pm.setNote(message);

                printCluster(whi, clus[i], annot, b);

                double upd = ((i+1)*interv) ;
                //System.out.println("current percent is: "+upd);
                int p = (int)Math.floor(upd);
                //System.out.println("setting prgress to: "+p);
                setProgress(p);
                if(i==clus.length && p != 100) //if at end of run and progress hasn't been completed on bar, do it now.
                    setProgress(100);
            }
            return null;
        }

        public void done()
        {
            extendInfCB.setEnabled(true);
            anotFCB.setEnabled(wasAnotFCBEnab);
            //prints extended cluster info if requested by the user:
            if(extendInfCB.isSelected() || b == extendInfBPrint) //if tickbox selected OR button was pressed)
            {
                printStringToFile(clustInfo, "clustInfo_"+numWrites+"_"+timeN+".csv");
                addFileToList("clustInfo_"+numWrites+"_"+timeN+".csv");
                numWrites++;
            }
        }

        public int getLengthOfTask()
        {
            //return clus.length;
            return 100;
        }
    }

}

/*
 * RUNNING METHODS FOR PRINTING CLUSTERS!
 *
Depending on the options the user has provided, different methods are called in different orders.

The RIC (ReadInClusters) functions to return lists of clusters can be passed in lists of clusters as well,
so that they will look only at the clusters on the list to see if they fit the criteria.

One data set:
Find clusters with more than X nodes:
  clusterList = ric.returnClustersMoreThan(X)

Find clusters with more than 50% sex of male:
  clusterList = ric.returnClusterAnnotPerc('sex', 'male', 0.5, false) //looks at all clusters

Find clusters with more than X nodes with more then 50% sex of male:
  clusterList = ric.returnClustersMoreThan(X)
  finalList = ric.returnClusterAnnotPerc(clusterList, 'sex', 'male', 0.5, false) //looks only the clusters in clusterList


The same can be done if there are two data sets:
Find clusters that have more than X matching sequences:
  clusterList = ric.returnClusterMatchMoreThan(X)

Find clusters with more than X matching sequences with more than 50% sex of male in data set 1
  clusterList = ric.returnClusterMatchMoreThan(X)
  finalList = ric.returnClusterAnnotPerc(clusterList, 'sex', 'male', 0.5, false)

Find clusters with more than X matching sequences with more than 50% sex of male in BOTH data sets!!
  clusterList = ric.returnClusterMatchMoreThan(X)
  finalList = ric.returnClusterAnnotPerc(clusterList, 'sex', 'male', 0.5, false, ric2) //must provide the other data set


So there are four functions in Main.java that call a selection of these RIC functions:

printClustersMoreThan
looks at ONE data set and returns clusters that have more than the specified number of sequences

printMatches
looks at TWO data sets and returns clusters with more than X matching sequences that fit the field and value specified
If no X is specified, returns all clusters with matches that fit the field and value specified

printClusterAnnots
Looks at ONE data set and returns clusters that fit the field and value specified

printMatchingClusters
Looks at TWO data sets and returns clusters with more than X matching sequences

Then THREE functions print them to a file:
printClusterMatch - prints if there are two data sets
printClusterMatchSame - prints if there are two data sets and the sequence names are the same between them
printCluster - prints if there is one data set

*/