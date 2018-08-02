
CONFIG=$PWD/$1
NETID=$2
n=1

cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
read line 
numhosts=$( echo $line | awk '{ print $1 }' ) 

while [[ $n -le numhosts ]] 
do
	read line
	node=$( echo $line | awk '{ print $1 }' )
	hostname=$( echo $line | awk '{ print $2 }' )
	port=$( echo $line | awk '{ print $3 }' )
	if [[ $hostname == dc* ]]		
	then
		n=$(( n + 1 ))
		ssh -o StrictHostKeyChecking=no $NETID@$hostname killall -u $NETID &
	fi
	sleep 1
done
)
rm *.class *.out debug.txt temp
wait
echo "Cleanup complete"
