### combine CP/CM output
### M. Ragonnet
### 5th Feb 2014

## CM is cluster matcher
## CP is cluster picker
## the files output from each program should be in the same folder

setwd("")
path <- getwd()
print(path)

## read the files
#read the cm file
txtFolder <- dir(path)
inds <- grep("clustInfo", txtFolder)
clustInfoFile <- txtFolder[inds]
cm <- read.csv(clustInfoFile, header=TRUE)

#read the CP log file
cp_output <- readLines(list.files(pattern="*clusterPicks_log.txt"))
cp_output2 <- cp_output[20:(length(cp_output)-1)]
writeLines(cp_output2, "CP_output.txt")
cp <- read.table("CP_output.txt", sep="\t", header=TRUE)
colnames(cp)[1] <- "Clust_ID"

# merge the files into one superfile and write to folder
cluster_table <- merge(cp,cm,by=c("Clust_ID"), all=TRUE)


for (line in 1:length(cluster_table[,1])){
  if(cluster_table$NumberOfTips [line] != cluster_table$Num_Seqs[line]){
    print("There was a problem combining the files!")
  } 
}

write.csv (cluster_table, file ="cpcm.csv", row.names = FALSE)
