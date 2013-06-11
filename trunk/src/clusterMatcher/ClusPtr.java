package clusterMatcher;


/* Copyright 2011-2013 Emma Hodcroft
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

/**
 *
 * This is an object class that holds information about one cluster from a data set.
 * Each cluster is a linked-list of ClusPtr objects. The 'head' node stores information
 * about the cluster - the number of sequences in the cluster, the number of matches,
 * the cluster numbers of clusters in the other data set that match this cluster, the
 * newick code for the cluster, and the counts of the annotation data in the sequences in
 * this cluster (ex: how many females, how many homosexuals, etc)
 *
 * Each ClusPtr in the linked-list has a link to the next ClusPtr object, with the last ClusPtr object having a link to 'null'
 *
 * Each non-header node has a link to a SeqNode object that holds the info about one seq in that cluster.
 *
 * to try and represent visually:
 *
 * [cluster=66, clusterCode=((seq1:0.2,seq2..., length=2, matches=2, matchClusters=192, ...] seq -> null
 *   next
 *    |
 *    v
 * [cluster=null, clusterCode=null, length=null, matches=null, matchClusters=null] seq -> [SeqNode 12734...]
 *   next
 *    |
 *    v
 * [cluster=null, clusterCode=null, length=null, matches=null, matchClusters=null] seq -> [SeqNode 72361...]
 *   next
 *    |
 *    v
 *   null
 *
 * @author Emma
 */
public class ClusPtr {
    boolean verbose = false;
    ClusPtr next = null; //links to the next ClusPtr object in the linked-list (end points to null)
    String cluster = ""; //only in header - stores the number of this cluster
    SeqNode seq = null; // pointer to the seqNode in this cluster - only stored in each non-header node
    String clusterCode = ""; //only stored in header node - the tree code for this cluster
    int length = 0; //the number of sequences in this cluster -only stored in the 'header' node for each cluster
    int matches = 0; //only stored in header ndoe - number of sequences that have matches in the other dataset
    String matchClusters=""; //the cluster numbers of the sequences from the other dataset that match sequences in this cluster - only header
    String [][] annotCounts = null; //only header - stores the field variables and the the value for each sequence in the cluster

    public ClusPtr()
    {

    }

    public ClusPtr(SeqNode sn)
    {
        seq = sn;
        next = null;
    }

    //have we counted up the annotations present in the sequences in this cluster?
    public boolean hasAnnotCounts()
    {
        if(annotCounts==null)
            return false;
        return true;
    }

    //returns the cluster numbers for the clusters in the other data set that match this one
    public String[] getMatchClusters()
    {
        if(matchClusters.isEmpty())
            return null;
        return matchClusters.split(",");
    }

    //updates the data on the annotations present in a cluster...
    //after the annotations have been added to the seqnode sn
    //the total anntoation counts for the cluster that 'sn' in is are updated
    public void updateClusterAnnots(SeqNode sn)
    {
        if(annotCounts == null)
        {
            String[] annotF = sn.getAnnotFields();
            annotCounts = new String[length+1][annotF.length];
            System.arraycopy(annotF, 0, annotCounts[0], 0, annotCounts[0].length);
            int j=0;
            while(j<annotCounts.length && annotCounts[j][0]!=null)
                j++;

            if(annotCounts[j][0] == null)
            {
                System.arraycopy(sn.getAnnotValues(), 0, annotCounts[j], 0, annotCounts[j].length);
            }
            else
                System.out.println("****ERROR**** more sequences in cluster than thought to be!!");
        }
        else
        {
            int j=0;
            while(j<annotCounts.length && annotCounts[j][0]!=null)
                j++;

            if(annotCounts[j][0] == null)
            {
                System.arraycopy(sn.getAnnotValues(), 0, annotCounts[j], 0, annotCounts[j].length);
            }
            else
                System.out.println("****ERROR**** more sequences in cluster than thought to be!!");
        }
    }

    /*
     * debug
     */
    public void printAnnotArray()
    {
        printArray(annotCounts, true);
    }

    /*
     * debug
     */
    private void printArray(String[][] ac)
    {
        System.out.println();
        for(int i=0; i<ac.length; i++)
        {
            for(int j=0; j<ac[0].length; j++)
            {
                System.out.print(ac[i][j]+"-");
            }
            System.out.println();
        }
    }

    //a special hard-coded vesion of printarray for my own debugging so that it aligns properly
    //debug
    private void printArray(String[][] ac, boolean t)
    {
        if(ac != null)
        {
            printMatrix(ac);
            int[][] copy = new int[ac.length][ac[0].length];
            for(int i=0; i<ac.length;i++)
            {
                for(int j=0; j<ac[0].length; j++)
                {
                    if(ac[i][j] != null)
                        copy[i][j] = ac[i][j].length();
                    else
                        copy[i][j] = 0;
                }
            }

            for(int i=0; i<ac[0].length; i++)
            {
                int max=0;
                for(int j=0; j<ac.length; j++)
                {
                    if(copy[j][i]>max)
                        max=copy[j][i];
                }
                copy[0][i] = max;
            }

            for(int i=0; i<ac.length;i++)
            {
                for(int j=0; j<ac[0].length; j++)
                {
                    String tr = "";
                    if(ac[i][j] != null)
                        tr = ac[i][j];
                    while(tr.length() < copy[0][j]+1)
                        tr = tr+" ";
                    System.out.print(tr);
                }
                System.out.println();
            }
            System.out.println();
        }
        else
            System.out.println("ERROR: cluster "+cluster+" doesn't have any annotations!");
    }

    //how many sequences in this cluster have 'value' for 'field'?
    public int hasAnnots(String field, String value)
    {
        if(annotCounts != null)
        {
            int i=0;
            while(i<annotCounts[0].length && !field.equals(annotCounts[0][i]))
            {
                i++;
            }
            if(field.equals(annotCounts[0][i]))
            {
                int mats=0, total=0, nas=0;
                for(int j=1; j<annotCounts.length; j++)
                {
                    total++;
                    if(annotCounts[j][i]!=null && annotCounts[j][i].equals(value))
                        mats++;
                    if(annotCounts[j][i]==null || annotCounts[j][i].isEmpty())
                        nas++;
                }
                return mats;
            }
            else
                return 0;
        }
        else
            return 0;
    }

    /*
     * does this cluster have annotations in 'field' with value 'value' with 'perc' percent of the sequences in the cluster
     * having them?
     */
    public boolean hasAnnots(String field, String value, double perc, boolean includeNAs)
    {
        if(annotCounts != null)
        {
            int i=0;
            while(i<annotCounts[0].length && !field.equals(annotCounts[0][i]))
            {
                i++;
            }
            if(field.equals(annotCounts[0][i]))
            {
                int mats=0, total=0, nas=0;
                for(int j=1; j<annotCounts.length; j++)
                {
                    total++;
                    if(annotCounts[j][i]!=null && annotCounts[j][i].equals(value))
                        mats++;
                    if(annotCounts[j][i]==null || annotCounts[j][i].isEmpty())
                        nas++;
                }
          
                if(includeNAs == true) //only consider sequences that HAVE this value
                    total = total-nas;
                // otherwise, consider all sequences in the cluster, even those that do not have this value
                double pe =0;
                if(total != 0)
                    pe = (double)mats/(double)total;//roundTwoDecimals(mats/total);
if(verbose) System.out.println("for cluster "+cluster+".. total: "+total+" mats: "+mats+" nas: "+nas+ " cutoff is: "+perc + " this percent is: "+pe);
                if(pe>=perc)
                    return true;
                else
                    return false;
            }
            else
                System.out.println("***ERROR*** A field was chosen for cluster selection that doesn't exist!");
            return false;
        }
        else
            return false;
    }

    //I got this code online... from a forum, after searching something like 'round decimal places in java'
    private double roundTwoDecimals(double d)
    {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

    /*debug
     *
     */
    private void printMatrix(String[][] ac)
    {
        for(int i=0; i<ac.length; i++)
        {
            for(int j=0; j<ac[0].length; j++)
            {
                System.out.print(ac[i][j]+"-");
            }
            System.out.println();
        }
    }
}
