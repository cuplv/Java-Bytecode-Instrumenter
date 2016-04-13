#!/bin/bash

count=1

while read line
do 
   echo -e "$line"
   if [ "$#" -eq 1 ]
   then
     $1 $line > log/methodCount.log
   elif [ "$#" -eq 2 ]
   then
     $1 $2 $line > log/methodCount.log
   elif [ "$#" -eq 3 ]
   then
     $1 $2 $3 $line > log/methodCount.log
   elif [ "$#" -eq 4 ]
   then	
     $1 $2 $3 $4 $line > log/methodCount.log
   elif [ "$#" -eq 5 ]
   then
     java -Xmx512M -Xss256M -jar $1 $2 $3 $4 $5 $line > log/methodCount.log
   elif [ "$#" -eq 6 ]
   then
     java -Xmx512M -Xss256M -jar $1 $2 $3 $4 $5 $6 $line > log/methodCount.log
   elif [ "$#" -eq 7 ]
   then
     java -Xmx512M -Xss256M -jar $1 $2 $3 $4 $5 $6 $7 $line > log/methodCount.log
   fi 
   mv log/methodCount.log log/methodCount$count.log
   count=`expr $count + 1`
done < input.txt
