#!/bin/bash

PROG=MutexTester
CONFIG=$1


config_file_name=$(echo $CONFIG | rev | cut -f1 -d"/" | rev | cut -f1 -d".")
rm -f $config_file_name-*.out
sed -e "s/#.*//" $CONFIG | sed -e "/^\s*$/d" > temp

echo  >> temp

node_count=0
noOfReq=0
current_line=1

line=$(head -n 1 temp) 
echo $line

line=$(echo $line | tr -s ' ')

node_count=$(echo $line | cut -f1 -d" ")

let node_count=$node_count+0   		

interReqDel=$(echo $line | cut -f2 -d" ")
csExecTime=$(echo $line | cut -f3 -d" ")
noOfReq=$(echo $line | cut -f4 -d" "|tr -d '[[:space:]]')

javac $PROG.java;
java -cp . $PROG $node_count $noOfReq;

exit