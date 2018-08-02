#!/bin/bash

req=( "5" "20" "50")

cs=( "5" "20" "50")

counter=1
for i in "${req[@]}"
do
	for j in "${cs[@]}"
	do
		c="${i}_$j"
		./launcher.sh config_1_$c.txt vxn170230
		echo "launcher done - $counter times"
		./cleanup1.sh config_1_$c.txt vxn170230
		echo "clean done"
		counter=$((counter+1))
    done
done
echo "Everything done"
