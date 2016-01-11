# This script launches CP on a series of nwk trees
# Remember bootstraps are out of 100 for Raxml and out of 1 for FastTree!!!
# M. Ragonnet
# 21st June 2013

import subprocess as sub
import os

# to launch on 100 simulated sequences and trees in the same folder

for i in range(1,101):
	sub.call(["java", "-jar", "ClusterPicker_1.2.jar", 
				"tree"+str(i)+"_simulatedSeqs.fas", "tree"+str(i)+".nwk", "90", "90","0.045","0"])


# to launch	in folders and subfolders with structure bootstrap --> genetic distance

bootstraps = [0.70,0.80,0.90, 0.95]
genetic_distances =[0.015, 0.045]
root = os.getcwd()				
	
for j in range(len(genetic_distances)):
	for i in range(len(bootstraps)):
		print(os.getcwd())
		os.chdir(root+ "/"+str(bootstraps[i])+"/"+str(genetic_distances[j]))
		print (bootstraps[i])
		sub.call(["java", "-jar", "ClusterPicker_1.2.jar", "fastaFile.fas", 
			"tree.nwk", str(bootstraps[i]), str(bootstraps[i]),str(genetic_distances[j]),"0", "ambiguity"])
