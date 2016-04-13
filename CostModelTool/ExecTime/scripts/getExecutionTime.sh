#!/bin/bash

var = 0
rm -rf Duration.txt

while read line
do 
   echo -e "$line"
   a=`java -Xint -Xmx512M -Xss256M $1 $2 $line` 
   echo $a >> Duration.txt
done < input.txt














