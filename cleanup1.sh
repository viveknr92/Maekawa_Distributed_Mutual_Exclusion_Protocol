#!/bin/bash

CONFIG=$1
netid=$2

n=1
cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    nodes=$( echo $i | cut -f1 -d" ")
    while read line 
    do
        host=$( echo $line | awk '{ print $2 }' )

        echo $host
        ssh -o StrictHostKeyChecking=no $netid@$host "ps -fu $USER | grep java | tr -s ' ' | cut -f2 -d' ' | xargs kill " &

        n=$(( n + 1 ))
        if [ $n -gt $nodes ];then
        	break
        fi
    done
   
)
rm *.class *.out debug.txt temp
wait
echo "Cleanup complete"