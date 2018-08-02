#!/bin/bash

req=( "5" "20" "50")

cs=( "5" "20" "50")

for i in "${req[@]}"
do
	for j in "${cs[@]}"
	do
		read continue
		cd $HOME/mae
		./launcher.sh config_1_$i_$j.txt vxn170230
		echo "done1"
		read continue
		cd $HOME/mae
		./cleanup.sh config_1_$i_$j.txt vxn170230
		echo "done"
    done
done