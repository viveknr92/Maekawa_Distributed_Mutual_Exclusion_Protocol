#!/bin/bash
#Author: Moses Ike. http://mosesike.org
#This script needs 2 argument. path to config file, and netid

CONFIG=$1
netid=$2

n=1
cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    #echo $i
    nodes=$( echo $i | cut -f1 -d" ")
    while read line 
    do
        host=$( echo $line | awk '{ print $2 }' )

        echo $host
        #ssh $netid@$host killall -u $netid &
        #ssh -o StrictHostKeyChecking=no $netid@$host "ps -u $USER | grep java | tr -s ' ' | cut -f1 -d' ' | xargs kill " &
        ssh -o StrictHostKeyChecking=no $netid@$host "ps -fu $USER | grep java | tr -s ' ' | cut -f2 -d' ' | xargs kill " &
        #sleep 1

        n=$(( n + 1 ))
        if [ $n -gt $nodes ];then
        	break
        fi
    done
   
)


echo "Cleanup complete"
