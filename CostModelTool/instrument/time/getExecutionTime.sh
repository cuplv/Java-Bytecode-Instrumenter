#!/bin/bash

rm -rf Duration.txt

while read line
do 
   echo -e "$line"
   if [ "$#" -eq 1 ]
   then
      a=`java -Xint -Xmx512M -Xss256M -jar $1 $line` 
   elif [ "$#" -eq 2 ]
   then
      a=`java -Xint -Xmx512M -Xss256M -jar $1 $2 $line` 
   elif [ "$#" -eq 3 ]
   then
      a=`java -Xint -Xmx512M -Xss256M -jar $1 $2 $3 $line` 
   elif [ "$#" -eq 4 ]
   then	
      a=`java -Xint -Xmx512M -Xss256M -jar $1 $2 $3 $4 $line` 
   elif [ "$#" -eq 5 ]
   then
      a=`java -Xint -Xmx512M -Xss256M -jar $1 $2 $3 $4 $5 $line` 
   elif [ "$#" -eq 6 ]
   then
      a=`java -Xint -Xmx512M -Xss256M -jar $1 $2 $3 $4 $5 $6 $line`
   elif [ "$#" -eq 7 ]
   then
      a=`java -Xint -Xmx512M -Xss256M -jar $1 $2 $3 $4 $5 $6 $7`
   fi 
   echo $a >> Duration.txt
done < input.txt
