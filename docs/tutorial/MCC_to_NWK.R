# function to convert a BEAST MCC tree to a newick string
# with node names as posterior probabilities
# S. J. Lycett
# 8  Nov 2011

# 20 Sept 2013 - for use with supplementary data from paper:
# Automated Analysis of Phylogenetic Clusters, Ragonnet et al (2013) BMC Bioinformatics

# 25 Sept 2013 - includes option to ladderize and force to bifuraction
# 25 Sept 2013 - also assigns 0 support to nodes with no label



# needs package ape
library(ape)

# FUNCTION DEFINITION
MCC_to_NWK <- function( fname, doLadderize=FALSE, tol=1e-8, root0=TRUE ) {

	# read the MCC tree file from TreeAnnotator
	lines 	<- readLines(fname)

	# get the taxa information
	ts		<- grep("Translate", lines)[1]+1
	te		<- grep(";", lines)[6]-1
	taxaLines 	<- lines[ts:te]
	taxaTbl	<- unlist(apply(as.matrix(taxaLines), 1, strsplit, " "))
	taxaTbl	<- t(matrix(taxaTbl, 2, length(taxaTbl)/2))
	taxaTbl[,1] <- gsub("\t", "", taxaTbl[,1])
	taxaTbl[,2] <- gsub("'", "", taxaTbl[,2])
	taxaTbl[,2] <- gsub(",", "", taxaTbl[,2])

	# get strip the tree string from the relevant line
	trLine	<- lines[te+2]
	trLine	<- strsplit(trLine, "\\[\\&R\\]")[[1]][2]

	sb		<- gregexpr("\\[", trLine)[[1]]
	eb		<- gregexpr("\\]", trLine)[[1]]
	
	# replace , by | in node names
	for (i in 1:length(sb)) {
		before <- substring(trLine, 1, sb[i])
		between<- substring(trLine, sb[i]+1, eb[i]-1)
		after  <- substring(trLine, eb[i], nchar(trLine))
		between<- gsub(",", "|", between)
		trLine <- paste(before,between,after,sep="")
	}

	tr 		<- read.tree(text=trLine)

	if (doLadderize) {
		tr		<- multi2di(tr)
		tr		<- ladderize(tr, right=FALSE)
		einds		<- which(tr$edge.length < tol)
		if (length(einds) > 0) {
			tr$edge.length[einds] <- tol
		}
	}
	

	# extract proper tip names
	tips 		<- unlist(apply(as.matrix(tr$tip.label), 1, strsplit, "\\["))
	tips  	<- t(matrix(tips, 2, length(tips)/2))
	tips  	<- tips[,1]
	tinds 	<- match(tips, taxaTbl[,1])
	tr$tip.label <- taxaTbl[tinds,2]

	# extract posterior support
	for (i in 1:length(tr$node.label)) {
		jj 	<- gregexpr("posterior=([0-9]+\\.)?[0-9Ee\\-]+", tr$node.label[i])
		js	<- jj[[1]]
		je	<- js + attributes(jj[[1]])$match.length-1
		nn	<- substring(tr$node.label[i], js, je)
		nn	<- gsub("posterior=", "", nn)
		tr$node.label[i] <- nn
	}

	pp		<- as.numeric(tr$node.label)
	inds		<- which(!is.finite(pp))
	if (length(inds) > 0) {
		pp[inds]	<- 0
	}
	tr$node.label <- format(pp, digits=4)

	

	if (root0) {
		# set support at root to 0 for cluster picker
		tr$node.label[1] <- "0.000"
		trString	<- write.tree(tr)

		# this string should end with support:root length
		trString	<- gsub(";","",trString)
		trString	<- paste(trString, ":0.0;", sep="")
	} else {
		# remove last support value - this is not usually a good idea
		# so not doing it now

		trString	<- write.tree(tr)

		#lastEl	<- strsplit(trString, "\\)")[[1]]
		#lastEl	<- lastEl[length(lastEl)]
		#trString	<- gsub(lastEl, ";", trString, fixed=TRUE)
	}

	outName <- paste(fname, ".posterior.nwk", sep="")
	write(trString, file=outName)
	#write.tree(tr, file=outName)	

	print( paste("Newick tree written to",outName) )

	# uncomment the line below if you actually want the tree object returned to the R-workspace
	# return( tr )
}

###############################################################
# EXAMPLE USE OF FUNCTION
###############################################################

mccName	<- "h3n2_1027_beast_mcc.tre"
MCC_to_NWK(mccName)




