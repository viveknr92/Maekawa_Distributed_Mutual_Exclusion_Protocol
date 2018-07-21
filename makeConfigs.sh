
# random(min, max) returns a random number between [min,max]
rndm () {
    MIN=1024
    MAX=65535
    while
    rnd=$(cat /dev/urandom | tr -dc 0-9 | fold -w${#MAX} | head -1 | sed 's/^0*//;')
    [ -z $rnd ] && rnd=0
    (( $rnd < $MIN || $rnd > $MAX ))
    do :
    done
}

write_line(){
    STRING=$1
    FILENAME=$2
    echo $STRING >> $FILENAME
}

new_file(){
    FILENAME=$1
    rm -f $FILENAME
    touch $FILENAME
}


new_file "config1.txt"

write_line "5 20 10 1000" "config1.txt"
rndm
write_line "0 dc04 $rnd" "config1.txt"
rndm
write_line "1 dc05 $rnd" "config1.txt"
rndm
write_line "2 dc06 $rnd" "config1.txt"
rndm
write_line "3 dc07 $rnd" "config1.txt"
rndm
write_line "4 dc08 $rnd" "config1.txt"
write_line "0 1 2" "config1.txt"
write_line "1 2 3" "config1.txt"
write_line "2 3 4" "config1.txt"
write_line "3 4 0" "config1.txt"
write_line "4 0 1" "config1.txt"

new_file "config2.txt"

write_line "10 20 10 5000" "config2.txt"
rndm
write_line "0 dc21 $rnd" "config2.txt"
rndm
write_line "1 dc22 $rnd" "config2.txt"
rndm
write_line "2 dc23 $rnd" "config2.txt"
rndm
write_line "3 dc24 $rnd" "config2.txt"
rndm
write_line "4 dc25 $rnd" "config2.txt"
rndm
write_line "5 dc26 $rnd" "config2.txt"
rndm
write_line "6 dc27 $rnd" "config2.txt"
rndm
write_line "7 dc28 $rnd" "config2.txt"
rndm
write_line "8 dc29 $rnd" "config2.txt"
rndm
write_line "9 dc30 $rnd" "config2.txt"
write_line "1 3 5 7 9" "config2.txt"
write_line "0 2 3 4 6 8" "config2.txt"
write_line "1 3 4 5 7 9" "config2.txt"
write_line "0 2 4 6 7 8" "config2.txt"
write_line "1 3 5 7 8 9" "config2.txt"
write_line "0 2 4 6 8 9" "config2.txt"
write_line "1 3 5 7 8 9" "config2.txt"
write_line "0 2 4 5 6 8" "config2.txt"
write_line "1 3 4 5 7 9" "config2.txt"
write_line "0 1 2 4 6 8 " "config2.txt"

new_file "config3.txt"

write_line "10 20 10 100" "config3.txt"
rndm
write_line "0 dc21 $rnd" "config3.txt"
rndm
write_line "1 dc22 $rnd" "config3.txt"
rndm
write_line "2 dc23 $rnd" "config3.txt"
rndm
write_line "3 dc24 $rnd" "config3.txt"
rndm
write_line "4 dc25 $rnd" "config3.txt"
rndm
write_line "5 dc26 $rnd" "config3.txt"
rndm
write_line "6 dc27 $rnd" "config3.txt"
rndm
write_line "7 dc28 $rnd" "config3.txt"
rndm
write_line "8 dc29 $rnd" "config3.txt"
rndm
write_line "9 dc30 $rnd" "config3.txt"
write_line "1 3 5 7 9" "config3.txt"
write_line "0 2 4 6 8" "config3.txt"
write_line "1 3 5 7 9" "config3.txt"
write_line "0 2 4 6 8" "config3.txt"
write_line "1 3 5 7 9" "config3.txt"
write_line "0 2 4 6 8" "config3.txt"
write_line "1 3 5 7 9" "config3.txt"
write_line "0 2 4 6 8" "config3.txt"
write_line "1 3 5 7 9" "config3.txt"
write_line "0 2 4 6 8 " "config3.txt"
