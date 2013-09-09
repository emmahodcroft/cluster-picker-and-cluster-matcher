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

import java.io.*;
import java.util.*;
import java.lang.*;
import java.text.*;
import java.lang.RuntimeException.*;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVParser;

/**
 * This is the object class that holds the info about one data set - it reads in one tree
 * (and) one annotation file and stores the information about the tree, clusters, and sequences
 * from that file. I often abbreviate it in my comments as RIC objects. One RIC object is created
 * per data set. The two RIC objects can then be compared, etc. 
 *
 * @author Emma
 */
public class ReadInClusters {

    boolean verbose = false;
    private String newickFile;
    private String wholeTree;
    private String[] totalNodes; //stores all tip names with prefix (Clust11_2223)
    private String[] totalSeqNames; //stores all seq names without prefix (2223)
    private int nodesInClusters=0;
    HashMap<String,SeqNode> clustSeqs = new HashMap<String,SeqNode>();
    HashMap<String,ClusPtr> clusters = new HashMap<String,ClusPtr>();
    HashMap<String,String> annotVars = new HashMap<String,String>();
    private boolean isAnnot = false;
    private String[] annotFields; //holds the column names for the annotations that have been added

    //constructor
    public ReadInClusters()
    {

    }

    //constructor
    public ReadInClusters (String nwkFile) throws FileNotFoundException
    {
        newickFile = nwkFile;
        readInFiles();
    }

    //methods
    private void readInFiles() throws FileNotFoundException
    {
        readInNewick();
    }

    //reads in the newick file and starts getting info about the data set
    //like the total number of nodes and the whole tree code
    //then calls readInClusters to find out about the clusters in the data set
    private void readInNewick()  throws FileNotFoundException
    {
        //String[] temp = newickFile.split("\\\\"); //splits the file path by the hash that separates folders
        String[] temp = splitFile(newickFile); //replaces above with function that splits by / or \ depending on OS
        
        String newEnd = temp[temp.length-1]; //takes the file name from the end of the path that has been split

        ReadNewick rn = new ReadNewick(newickFile);
	rn.openFile();
        wholeTree = rn.next();
        rn.closeFile();

        if(wholeTree == null || wholeTree.isEmpty() || wholeTree.length()<4)
            throw new IllegalArgumentException("Newick file is too short! ("+newEnd+")");

        totalNodes = getNodes(wholeTree);

        try
        {
            readInClusters(wholeTree);
        } catch(IllegalArgumentException e)
        {
            throw new IllegalArgumentException(e.getMessage()+" ("+newEnd+")");
        }

    }

    //reads in the clusters from the newick file
    private void readInClusters(String treeC)
    {
        int index = 0, stop = 0;
        String clusts = "";
        int i = treeC.indexOf("Clust", index);

        //looks for a seq name that starts with 'clust' & gets the cluster number
        //stores this number, then looks for the next 'clust' - if the number already stored, doesnt store again
        //otherwise, stores the next cluster number
        while(i != -1)
        {
            stop = treeC.indexOf("_", i);
            String clus = treeC.substring(i, stop+1);
            if(!clusts.contains(clus))
                clusts = clusts+","+clus;

            index = stop;
            i = treeC.indexOf("Clust", index);
        }

        clusts = clusts.substring(1); //because it starts with a comma

        int pq = 0;
        String[] clust = clusts.split(","); //convert comma-delimited string to an array

        //this is complicated. it digs out the cluster code for the cluster only out of the whole tree code.
        //then it finds all the sequences in the cluster and stores them in SeqNodes
        //then it stores the cluster in a ClusPtr that holds info, code, and the SeqNodes
        for(int j=0; j<clust.length; j++)
        {
            String clus = clust[j];
            int first = treeC.indexOf(clus);
            int last = treeC.lastIndexOf(clus);

            int count = 0;
            int start = first;

            int finder = first;

            while(last > finder)
            {
                finder = start;
                start = start-1;
                if(treeC.charAt(start)=='(')
                    count = 1;
                while(count != 0 && finder<treeC.length())
                {
                    if(treeC.charAt(finder)==')')
                        count--;
                    if(treeC.charAt(finder)=='(')
                        count++;
                    finder = finder+1;
                }
            }

            int end = finder+1;
            while(!(treeC.charAt(end)==')' || treeC.charAt(end)==','))
            {
                end++;
            }

            String clusTree = treeC.substring(start, end)+";";
            String[] clusNodes = getNodes(clusTree);
            String clusSt = clusNodes[1].split("_",2)[0].replace("Clust", "");
            ClusPtr st = new ClusPtr();
            ClusPtr cur = st;
            String prefix="";
            for(int a=0;a<clusNodes.length; a++)
            {
                try{
                    String nod = clusNodes[a];
                    String id = nod.split("_",2)[1];
                    prefix = nod.split("_",2)[0];
                    String cluster = prefix.replace("Clust", "");
                    SeqNode sn = new SeqNode(id, prefix, nod, cluster);
                    cur.next = new ClusPtr(sn);
                    cur = cur.next;
                    clustSeqs.put(id, sn);
                }
                catch(ArrayIndexOutOfBoundsException e)
                {
                    System.out.println("***ERROR*** - Non-clusters sequences found in the cluster!"); //Was inspecting cluster "+prefix);
                    throw new IllegalArgumentException("Cluster file has non-cluster sequences in clusters!");
                }
            }
            st.length = clusNodes.length;
            nodesInClusters = nodesInClusters + st.length;
            st.clusterCode = clusTree;
            st.cluster = clusSt;
            clusters.put(clusSt, st);
            pq++;
        }
        if(pq == 0)
            throw new IllegalArgumentException("There are no clusters!");
    }

    //returns the number of nodes that are in clusters in this data set
    public int getNodesInClusters()
    {
        return nodesInClusters;
    }

    // returns the whole tree code, raw from how it was read in from the file.
    public String getRawWholeTree()
    {
        return wholeTree;
    }

    // returns the whole tree code, but modified so that the root length will show up in figtree
    public String getWholeTree()
    {
        //return wholeTree;
        String code = "("+wholeTree.substring(0,wholeTree.length()-1)+");";
        return code;
    }

    // returns the whole tree code, with the specified clusters annotated, modified for figtree
    public String getWholeTree(String[] clusters)
    {
        String code = "("+wholeTree.substring(0,wholeTree.length()-1)+");";
        for(int i=0;i<clusters.length;i++)
        {
            code = getWholeTree(clusters[i], code);
        }
        return code;
    }

    // returns the whole tree code, with the specified cluster annotated, modified for figtree
    public String getWholeTree(String cluster)
    {
        String code = "("+wholeTree.substring(0,wholeTree.length()-1)+");";
        return getWholeTree(cluster, code);
    }

    // returns the whole tree code, with the specified cluster annotated
    private String getWholeTree(String cluster, String tree)
    {
        String code = tree;
        ClusPtr st = clusters.get(cluster);

        while(st != null)
        {
            if(st.seq != null)
            {
                SeqNode sn = st.seq;
                code = code.replace(sn.getLongID(), sn.getAnnotCode());
            }
            st = st.next;
        }
        return code;
    }

    public String[] getTotalNodes()
    {
        return totalNodes;
    }

    public String[] getSeqNames()
    {
        return totalSeqNames;
    }

    //returns all the nodes in a specified tree code
    private String[] getNodes(String tree)
    {
        String [] nods = tree.split(",");
        totalSeqNames = new String [nods.length];

        for(int i=0;i<nods.length; i++)
        {
            nods[i] = nods[i].split(":")[0].replaceAll("\\(", "");

            if(nods[i].contains(new String("_")))
                totalSeqNames[i] = nods[i].split("_")[1];
            else
                totalSeqNames[i] = nods[i];
        }
        return nods;
    }

    /*
     * returns true if the field is present as an annotation in this RIC object
     * and false if not
     * This is important because we do not want to allow users to select clusters by an
     * annotation that is not present in both datasets!!!
     * returns false if the object is not annotated
     */
    public boolean isFieldPresent(String field)
    {
        if(isAnnot())
        {
            if(annotVars.containsKey(field))
                return true;
            else
                return false;
        }
        else
            return false;
    }

    //returns whether a specified sequence in this RIC object has a match in the other data set
    // has no protection against non-existant sequences!!
    public boolean hasMatch(String seq)
    {
        SeqNode sn = clustSeqs.get(seq);
        return sn.hasMatch();
    }

    //returns the cluster number of a match to sequence 'seq' in the other data set
    //or null if no match
    public String getMatchClus(String seq)
    {
        if(hasMatch(seq))
        {
            SeqNode sn = clustSeqs.get(seq);
            String fm = sn.getFullMatch();
            if(fm != null)
                return fm.substring(5).split("_")[0];
            else
                return fm;
        }
        else
            return null;
    }

    //returns the sequence name of the match to seq in the other data set
    // ex: '11022'
    public String getMatch(String seq)
    {
        if(hasMatch(seq))
        {
            SeqNode sn = clustSeqs.get(seq);
            return sn.getMatch();
        }
        else
            return null;
    }

    //returns the FULL sequence name of the match to seq in the other data set
    // ex: 'Clus21_11022'
    public String getFullMatch(String seq)
    {
        if(hasMatch(seq))
        {
            SeqNode sn = clustSeqs.get(seq);
            return sn.getFullMatch();
        }
        else
            return null;
    }

    //returns a list of the sequences in the specified cluster ("22") in this RIC object
    public String[] getSeqsInCluster(String cluster)
    {
        ClusPtr st = clusters.get(cluster);
        String p = "";
        while(st!= null)
        {
            if(st.seq != null)
                p=p+","+st.seq.getID();
            st = st.next;
        }
        return p.substring(1).split(",");
    }

    /*
     * Returns the numbers of the clusters that match the cluster 'clust' in the other data set
     */
    public String[] getClusterMatches(String clust)
    {
        ClusPtr st = clusters.get(clust);
        if(st.matchClusters.trim().length() > 0)
            return st.matchClusters.split(",");
        else
            return null;
    }

    /*
     * Returns the number of sequences that match another sequence in this cluster
     */
    public int getNumMatches(String clust)
    {
        ClusPtr st = clusters.get(clust);
        return st.matches;
    }

    /*
     * returns a string vector containing the cluster numbers that have
     * more than 'num' sequences in them
     */
    public String[] returnClustersMoreThan(int num)
    {
        Iterator it = clusters.keySet().iterator();
        String clusts="";
        for(;it.hasNext();)
        {
            String key = it.next().toString();
            ClusPtr st = clusters.get(key);
            if(st.length > num)
                clusts=clusts+","+key;
        }
        if(!clusts.isEmpty())
        {
            clusts = clusts.substring(1);
            return clusts.split(",");
        }
        else
            return null;
    }

    /*
     * returns a string vector containing the cluster numbers that have
     * more than 'num' matches in them
     */
    public String[] returnClusterMatchMoreThan(int num)
    {
        Iterator it = clusters.keySet().iterator();
        String clusts="";
        for(;it.hasNext();)
        {
            String key = it.next().toString();
            ClusPtr st = clusters.get(key);
            if(st.matches > num)
                clusts=clusts+","+key;
        }
        if(clusts.length()>0)
        {
            clusts = clusts.substring(1);
            return clusts.split(",");
        }
        else
            return null;
    }

    /* returns a string vector containing the cluster numbers that have
     * more than 'perc' percent of sequence with 'field' value of 'value'
     * 'includeNAs' if true, means that only sequences that HAVE a value in this field will be counted
     * if false, means that sequences without this field will be counted as not a match
     *
     * This version looks at ALL clusters (for example, if user has not requested only clusters with x matches
     * or if it is just a single data set)
     */
    public String[] returnClusterAnnotPerc(String field, String value, double perc, boolean includeNAs)
    {
        Iterator it = clusters.keySet().iterator();
        String clusts="";
        for(;it.hasNext();)
        {
            String key = it.next().toString();
            ClusPtr st = clusters.get(key);
            if(st.hasAnnots(field, value, perc, includeNAs))
                clusts=clusts+","+key;
        }
        if(!clusts.isEmpty())
            clusts = clusts.substring(1);
        return clusts.split(",");
    }

    /* returns a string vector containing the cluster numbers that have
     * more than 'perc' percent of sequence with 'field' value of 'value'
     * FROM the list of clusters provided.
     * 'includeNAs' if true, means that only sequences that HAVE a value in this field will be counted
     * if false, means that sequences without this field will be counted as not a match
     *
     * This version only looks at the clusters passed in in 'clus' - so if they've already been
     * narrowed down by number of seqs or number of matches
     */
    public String[] returnClusterAnnotPerc(String[] clus, String field, String value, double perc, boolean includeNAs)
    {
        String clusts="";
        for(int i=0; i<clus.length; i++)
        {
            String key = clus[i];
            ClusPtr st = clusters.get(key);
            if(st.hasAnnots(field, value, perc, includeNAs))
                clusts=clusts+","+key;
        }
        if(!clusts.isEmpty())
            clusts = clusts.substring(1);
        return clusts.split(",");
    }

    /* returns a string vector containing the cluster numbers that have
     * more than 'perc' percent of sequence with 'field' value of 'value' !!**in both themselves and their matches**!!
     * FROM the list of clusters provided.
     * 'includeNAs' if true, means that only sequences that HAVE a value in this field will be counted
     * if false, means that sequences without this field will be counted as not a match
     *
     * Looks at field value !!**in both themselves and their matches**!!
     */
    public String[] returnClusterAnnotPerc(String[] clus, String field, String value, double perc, boolean includeNAs, ReadInClusters ric)
    {
        String clusts="";
        for(int i=0; i<clus.length; i++)
        {
            String key = clus[i];
            ClusPtr st = clusters.get(key);
            if(st.hasAnnots(field, value, perc, includeNAs))//test database 1
            {
                String mats[] = st.getMatchClusters();
                if(mats != null && mats.length!=0)
                {
                    boolean add = true;
                    for(int j=0; j<mats.length; j++)
                    {
                        ClusPtr cp = ric.clusters.get(mats[j]);
                        if(!cp.hasAnnots(field, value, perc, includeNAs)) //test database 2
                            add = false;
                    }
                    if(add==true) clusts=clusts+","+key;
                }
            }
        }
        if(!clusts.isEmpty())
            clusts = clusts.substring(1);
        return clusts.split(",");
    }

    /*
     * Called by traverseClustersAll in Main.java, this method is
     * just for debug!
     */
    public void traverseClustersAll(ReadInClusters ric)
    {
        Iterator it = clusters.keySet().iterator();
        for(;it.hasNext();)
        {
            System.out.println("\n-----------------------------------------------------------------------");
            String key = it.next().toString();
            System.out.println("Cluster "+key);
            ClusPtr st = clusters.get(key);
            if(verbose) st.printAnnotArray();
            System.out.println("Matches:");
            String[] matClus = st.getMatchClusters();
            if(matClus !=null)
            {
                for(int i=0; i<matClus.length; i++)
                {
                    ClusPtr ab = ric.clusters.get(matClus[i]);
                    System.out.println("*Cluster "+matClus[i]);
                    if(verbose) ab.printAnnotArray();
                }
            }
            else
                System.out.println("No matches");
        }
    }

    /*
     * also just for debug
     */
    public void traverseClustersAnnots()
    {
        Iterator it = clusters.keySet().iterator();
        for(;it.hasNext();)
        {
            String key = it.next().toString();
            System.out.println("\nCluster "+key);
            ClusPtr st = clusters.get(key);
            if(verbose) st.printAnnotArray();
        }
    }

    /*
     * this checks all clusters to make sure that they have matches in the annotation file.
     * This sends the user a warning! Because if for some reason they've input the wrong annotation file
     * They wouldn't know, but none of their sequences will have any matches, so they could make
     * False assumptions about their data. however, cannot completely prevent user error here... depends on the
     * user's intent! Totally possible they only have a little annotation data and are aware of this!
     */
    public boolean allClusAnnot()
    {
        Iterator it = clusters.keySet().iterator();
        for(;it.hasNext();)
        {
            String key = it.next().toString();
            ClusPtr st = clusters.get(key);
            if(!st.hasAnnotCounts())//if a cluster has no annotations at all, send a warning
                return false;
                //throw new IllegalStateException("One or more clusters have no sequences with a match in the annotation file!");
        }
        return true;
    }

    //returns the number of sequences in the specified cluster
    public int getNumSeqsInClus(String[] clus)
    {
        int lengt = 0;
        for(int i=0; i<clus.length; i++)
        {
            ClusPtr cp = clusters.get(clus[i]);
            lengt = lengt + cp.length;
        }
        return lengt;
    }

    //returns the number of sequences in the specified cluster
    public int getNumSeqsInClus(String clus)
    {
        int lengt = 0;
        ClusPtr cp = clusters.get(clus);
        lengt = cp.length;
        return lengt;
    }

    /*
     * For the provided string of Values (for example, 'male, female')
     * it will return the number of sequences in the cluster(s) that have these values
     * The numbers returned correspond to the order the values were passed in
     *
     * Passing in 'male, female'
     * will return
     * '3, 4' which means, 3 males, 4 females
     * 
     */
    public int[] getClusAnnot(String[] clus, String field, String[] values)
    {
        int[] tots = new int[values.length];
        for(int i=0; i<tots.length; i++)
            tots[i] = 0;

        for(int i=0; i<clus.length; i++)
        {
            ClusPtr cp = clusters.get(clus[i]);
            for(int j=0; j<values.length; j++)
            {
                tots[j] = tots[j] + cp.hasAnnots(field, values[j]);
            }
        }
        return tots;
    }

    /*
     * for debug
     */
    public void traverseClusters()
    {
        Iterator it = clusters.keySet().iterator();
        for(;it.hasNext();)
        {
            String key = it.next().toString();
            System.out.println("\nCluster "+key);
            ClusPtr st = clusters.get(key);
            while(st != null)
            {
                if(st.seq == null)//is the header node for a cluster set
                    System.out.println("length: "+st.length+"  matches: "+st.matches+"  matchClusters: "+st.matchClusters);
                else
                    System.out.println(st.seq.getID()+"  "+st.seq.getMatch());
                    //System.out.println(st.seq.getSeqNodeString());
                st = st.next;
            }
        }
    }

    /*
     * returns cluster code for a specified cluster
     * if it is annotated and annot = true, will return annotated code
     */
    public String getClusterCode(String clus, boolean annot)
    {
        if(isAnnot && annot == true)
            return getClusterCodeAnnot(clus);
        else
            return getClusterCodePlain(clus);
    }

    /*
     * returns plain cluster code modified for figtree
     */
    private String getClusterCodePlain(String clus)
    {
        ClusPtr st = clusters.get(clus);
        String code = "("+st.clusterCode.substring(0,st.clusterCode.length()-1)+");";
        //return st.clusterSeq;
        return code;
    }

    /* returns annotated cluster code modified for figtree
     *
     */
    private String getClusterCodeAnnot(String clus)
    {
        ClusPtr st = clusters.get(clus);
        String code = "("+st.clusterCode.substring(0,st.clusterCode.length()-1)+");";

        while(st != null)
        {
            if(st.seq != null)
            {
                SeqNode sn = st.seq;
                code = code.replace(sn.getLongID(), sn.getAnnotCode());
            }
            st = st.next;
        }
        return code;
    }

    /* returns the sequence annotation code for the supplied sequence
     * JUST the annot code... not the seq name and  [ ] around it...
     * will return soemthing like:
     * subt="A",eid="Asia"
     */
    public String getTrimmedSeqAnnot(String seq)
    {
        SeqNode sn = clustSeqs.get(seq);
        return sn.getTrimAnnotCode();
    }

    /*
     * Takes in a hashMap of the annotations and the variables already present (for example, from another
     * file that has been read in) or an empty HashMap, and returns a HashMap that contains each
     * annotation field (ex: sex, ethnicity, riskGroup) as the Key and a String list of all the
     * values found separated by commas (ex: "Male,Female" "Black-African,White,Other" )
     *
     * This allows a comprehensive HashMap to be returned, containing all fields and all field values present
     * that can then be put up in the GUI to allow users to select what fields/values they want
     */
    public HashMap<String,String> attachAnnots(String file, HashMap<String,String> annots) throws FileNotFoundException, IllegalStateException, IOException
    {
        //String[] temp = file.split("\\\\");
        String[] temp = splitFile(file); //replaces above line with a function that splits by / or \ depending on OS
        String fileEnd = temp[temp.length-1];
        CSVParser pars = new CSVParser(',','"');

        ReadNewick rn = new ReadNewick(file);
        rn.openFile();
        annotFields = new String[1];
        if(rn.hasNext())
        {
            String line = rn.next();
            line = line.substring(line.indexOf(",")+1); //take off first thing, as it is the sampID
            //annotFields = line.split(",");
           annotFields = pars.parseLine(line);
        }
        else
            throw new IllegalArgumentException("Annotation file is empty!");
        while(rn.hasNext())
        {
            String line = rn.next();
            String seq = line.split(",")[0]; //get the seq name
            line = line.substring(line.indexOf(",")+1); //remove seq name from begin of line
            String[] seqAnot = pars.parseLine(line); //parse the rest of the line
 //System.out.println(line);
 //printArray(seqAnot,".");
            if(seqAnot.length < annotFields.length) //if less than # annotFields, wrong format!
                throw new IllegalArgumentException("Annotation file format is not correct!");
            if(clustSeqs.containsKey(seq)) //if we have this sequence in our tree
            {
                SeqNode sn = clustSeqs.get(seq);
                sn.addAnnots(annotFields, seqAnot);
                updateCluster(sn);
                setAnnotVariety(seqAnot, annots);
            }

            /*
            if(line.split(",",-2).length-1 < annotFields.length) //if less than # annotFields, wrong format!
                throw new IllegalArgumentException("Annotation file format is not correct!");
            if(clustSeqs.containsKey(seq)) //if we have this sequence in our tree
            {
                SeqNode sn = clustSeqs.get(seq);
                line = line.substring(line.indexOf(",")+1);
                String[] anot = line.split(",",-2);
                sn.addAnnots(annotFields, anot);
                updateCluster(sn);
                setAnnotVariety(anot, annots);
            }
            */
        }
        rn.closeFile();
        if(verbose) traverseClustersAnnots();
        isAnnot = true;
        return annots;
    }

    //updates the data on the annotations present in a cluster...
    //after the annotations have been added to the seqnode sn
    //the total anntoation counts for the cluster that 'sn' in is are updated
    private void updateCluster(SeqNode sn)  //ac.length = number of rows ..... ac[0].length = num of columns
    {
        ClusPtr cp = clusters.get(sn.getCluster());
        cp.updateClusterAnnots(sn);
    }

    ///bleh.
    private void setAnnotVariety(String[] anot, HashMap<String,String> annots)
    {
        for(int i=0;i<annotFields.length;i++)
        {
            if(annots.containsKey(annotFields[i]))
            {
                if(!anot[i].isEmpty())
                {
                    String soFar = annots.get(annotFields[i]);
                    if(!soFar.contains(anot[i]))
                    {
                        //System.out.println("******** "+soFar+" does not yet contain "+anot[i]);
                        if(!soFar.isEmpty())
                            soFar = soFar+"£&";
                            //soFar = soFar+",";
                        soFar = soFar + anot[i];
                        annots.put(annotFields[i], soFar);
                    }
                }
            }
            else
                annots.put(annotFields[i], anot[i]);
            if(annotVars.containsKey(annotFields[i]))
            {
                if(!anot[i].isEmpty())
                {
                    String soFar = annotVars.get(annotFields[i]);
                    if(!soFar.contains(anot[i]))
                    {
                        if(!soFar.isEmpty())
                            soFar = soFar+",";
                        soFar = soFar + anot[i];
                        annotVars.put(annotFields[i], soFar);
                    }
                }
            }
            else
                annotVars.put(annotFields[i], anot[i]);
        }
    }

    //debug
    private void copyAnnots(HashMap<String,String> annots)
    {
        Iterator it = annots.keySet().iterator();
        for(;it.hasNext();)
        {
            String f = (String)it.next();
            annotVars.put(f, annots.get(f));
        }
    }

    //debug
    private void printAnnotVariety()
    {
        for(int i=0;i<annotFields.length;i++)
        {
            String soFar = annotVars.get(annotFields[i]);
            String[] possibs = soFar.split(",");
            System.out.print(annotFields[i]+": "+possibs.length+" variables");
            for(int j=0;j<possibs.length;j++)
                System.out.print(" "+possibs[j]);
            System.out.println();
        }
    }

    public int getNumClusters()
    {
        return clusters.size();
    }

    public int getNumNodes()
    {
        return totalNodes.length;
    }

    public boolean isAnnot()
    {
        return isAnnot;
    }

    //useful for debug!
    public void printArray(String [] a)
    {
        for(int i=0; i<a.length; i++)
        {
            System.out.print(a[i]+" ");
        }
        System.out.println();
    }

    //useful for debug - lets you specify how you want to separate elements of the array
    public void printArray(String [] a, String sep)
    {
        for(int i=0; i<a.length; i++)
        {
            System.out.print(a[i]+sep);
        }
        System.out.println();
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

}
