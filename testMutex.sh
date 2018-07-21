#!/bin/bash
#Author: Bharat M Bhavsar UTDallas
#This script needs 2 argument. path to config file, and netid

PROG=MutexTester
CONFIG=$1


config_file_name=$(echo $CONFIG | rev | cut -f1 -d"/" | rev | cut -f1 -d".") #without extension
rm -f $config_file_name-*.out
# extract the important lines from the config file. the ones with no '#' or empty lines
sed -e "s/#.*//" $CONFIG | sed -e "/^\s*$/d" > temp
# insert a new line to EOF # necessary for the while loop
echo  >> temp

node_count=0
noOfReq=0
current_line=1
# Reading from the temp file created above
line=$(head -n 1 temp) 
echo $line
	#turn all spaces to single line spaces
line=$(echo $line | tr -s ' ')
########Extract Number of nodes and, min and max per Active
#number of nodes
node_count=$(echo $line | cut -f1 -d" ")
#convert it to an integer
let node_count=$node_count+0   		
#minPerActive, maxPerActive
interReqDel=$(echo $line | cut -f2 -d" ")
csExecTime=$(echo $line | cut -f3 -d" ")
noOfReq=$(echo $line | cut -f4 -d" "|tr -d '[[:space:]]')

javac $PROG.java;
java -cp . $PROG $node_count $noOfReq;

exit