#!/bin/sh

echo "don2"
cd $HOME/mae
./launcher.sh config.txt vxn170230
echo "done1"
read continue
cd $HOME/mae
./cleanup.sh config.txt vxn170230
echo "done"