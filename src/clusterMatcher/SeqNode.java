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

/**
 * This is an object class that holds information about one sequence - its ID, the cluster it's in,
 * any matching sequences in the other data set, and it's annotation values
 *
 * @author Emma
 */
public class SeqNode {
    private String id, prefix, longID;
    // for a sequence 'Clust22_1443' id=1443, prefix=Clust22, longID=Clust22_1443
    private String cluster; //cluster = 22 (as a String!!)
    private String match="", fullMatch=""; //contains the match to this sequence from the other data base, and its full name
    private String[] annotFields; //contains the fields this seq has values for
    private String[] annotValues; // contains the values for the fields this seq has

    public SeqNode()
    {

    }

    public SeqNode(String i, String pref, String longI, String clus)
    {
        id = i;
        prefix = pref;
        longID = longI;
        cluster = clus;
    }

    public String getID()
    {
        return id;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public String getLongID()
    {
        return longID;
    }

    public String getCluster()
    {
        return cluster;
    }

    public String getSeqNodeString()
    {
        return(id+" "+longID+" "+cluster);
    }

    public String getFullMatch()
    {
        return fullMatch;
    }

    public String getMatch()
    {
        return match;
    }

    public boolean hasMatch()
    {
        return !match.isEmpty();
    }

    public void setFullMatch(String fm)
    {
        fullMatch = fm;
    }

    public void setMatch(String m)
    {
        match = m;
    }

    //adds these fields and these values to the seq node info (so can be returned as code!)
    public void addAnnots(String[] fields, String[] values)
    {
        annotFields = fields;
        for(int i=0;i<values.length;i++) //trim so that blank spaces are empty strings
        {
            values[i]=values[i].trim();
            if(values[i].equals("NA")) //replace NAs with blanks so don't get written to figtree at all!
                values[i] = "";
        }
        annotValues = values;
    }

    public String[] getAnnotFields()
    {
        return annotFields;
    }

    public String[] getAnnotValues()
    {
        return annotValues;
    }

    /*
     * returns something like: Clust713_124848[&subt="A",eid="Asia"]
     */
    public String getAnnotCode()
    {
        String coS = longID+"[&";
        String co = "";
        for(int i=0;i<annotFields.length;i++)
        {
            if(!annotValues[i].isEmpty())
            {
                if(!co.isEmpty())
                    co=co+",";
                co=co+annotFields[i]+"="+"\""+annotValues[i]+"\"";
            }
        }
        if(co.isEmpty())
            return "";
        else
        {
            return coS+co+"]";
        }
        //Clust713_124848[&subt="A",eid="Asia"]
    }

    /*
     * returns something like: subt="A",eid="Asia"
     */
    public String getTrimAnnotCode()
    {
        String co = "";
        for(int i=0;i<annotFields.length;i++)
        {
            if(!annotValues[i].isEmpty())
            {
                if(!co.isEmpty())
                    co=co+",";
                co=co+annotFields[i]+"="+"\""+annotValues[i]+"\"";
            }
        }
        return co;
    }

}
