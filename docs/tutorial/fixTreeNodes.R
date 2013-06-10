# script to fix non-annotated tree nodes and trifurcating nodes from FastTree trees (newick)
# M. Ragonnet, S. Lycett
# 21 Nov 2011


#Set your working directory, where the tree file is
setwd("")

# load package ape
library(ape)

# set your tree file name (change tree.nwk to your tree name)
treeFileName  <- ""

# Read your tree
tr 		  <- read.tree( treeFileName )

# extract the nodes as a separate entity, and edit them so that they match the format expected by the Cluster Picker
nodes 	  <- tr$node.label

zeroLabel 	  <- "0.000"
oneLabel	  <- "1.000"
  
inds 		  <- which(nodes=="")
nodes[inds]   <- zeroLabel

inds00 	  <- which(nodes=="0")
nodes[inds00] <- zeroLabel

inds1 	  <- which(nodes=="1")
nodes[inds1]  <- oneLabel

# Re-attach the edited nodes to the tree
tr$node.label <- nodes

# Output the tree to a new file
newTreeFileName <- paste(gsub("\\.nwk", "", treeFileName), ".treeforCPT.nwk", sep="")
write.tree(tr, newTreeFileName)

