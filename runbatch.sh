#!/bin/bash

req=( "5" "20" "50")

cs=( "5" "20" "50")

./cleanup1.sh config_15_5_5.txt $1
sleep 5
echo "clean done"
counter=1
for i in "${req[@]}"
do
	for j in "${cs[@]}"
	do
		c="${i}_$j"
		./launcher.sh config_15_$c.txt $1
		echo "launcher done - $counter times"
		./cleanup1.sh config_15_$c.txt $1
		sleep 5
		echo "clean done"
		counter=$((counter+1))
    done
done
echo "Everything done"
