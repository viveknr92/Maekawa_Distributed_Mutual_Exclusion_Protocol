#!/bin/bash

req=( "5" "20" "50")

cs=( "5" "20" "50")

for i in "${req[@]}"
do
	for j in "${cs[@]}"
	do
		c="${i}_$j"
		cd $HOME/mae
		./launcher.sh config_1_$c.txt vxn170230
		read
		echo "launcher done"
		cd $HOME/mae
		./cleanup.sh config_1_$c.txt vxn170230
		read
		echo "clean done"
    done
done
echo "Everything done"
read
echo "Everything done"