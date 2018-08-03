#!/bin/bash

PROG=Main

javac $PROG.java
CONFIG=$1
netid=$2

echo -e "" > debug.txt

config_file_name=$(echo $CONFIG | rev | cut -f1 -d"/" | rev | cut -f1 -d".") 
rm -f $config_file_name-*.out
sed -e "s/#.*//" $CONFIG | sed -e "/^\s*$/d" > temp
echo  >> temp

node_count=0
nodes_location=""
host_names=()
neighbors_dict=()

current_line=1
while read line; 
do
	line=$(echo $line | tr -s ' ')
	if [ $current_line -eq 1 ]; then
		node_count=$(echo $line | cut -f1 -d" ")
  		let node_count=$node_count+0   		
  		interReqDel=$(echo $line | cut -f2 -d" ")
  		csExecTime=$(echo $line | cut -f3 -d" ")
  		noOfReq=$(echo $line | cut -f4 -d" ")
  	else
  		if [ $current_line -le $(expr $node_count + 1) ]; then
  			nodes_location+=$( echo -e $line"#" )	
  			node_id=$(echo $line | cut -f1 -d" ")
  			hostname=$(echo $line | cut -f2 -d" ")
  			host_names[$node_id]="$hostname"	
  		else			
			let node_id=$current_line-$node_count-2
  			neighbors=$(echo $line)
  			neighbors_dict+=(['"$node_id"']="$neighbors")
  		fi
  	fi
  	let current_line+=1
done < temp

for node_id in $(seq 0 $(expr $node_count - 1))
do
	host=${host_names[$node_id]}
	neighbors=${neighbors_dict["$node_id"]}
	ssh -o StrictHostKeyChecking=no $netid@$host.utdallas.edu "cd $(pwd); java -cp . $PROG $node_id '$nodes_location' '$neighbors' '$csExecTime' \
	'$interReqDel' '$noOfReq' " &
		
done
read
echo "All Done"
