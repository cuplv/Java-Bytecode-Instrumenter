#!/bin/bash

var = 0
rm -rf Duration.txt myResults/*.log myResults/*_summary Summary.CSV

while read line
do 
   echo -e "$line"
   ((var++))
   START_TIME=$(($(date +%s%N)/1000000))
   java -jar encrypt_header.jar send inputDoc.txt $line
   END_TIME=$(($(date +%s%N)/1000000))
   ELAPSED_TIME=$(($END_TIME - $START_TIME))
   echo $ELAPSED_TIME >> Duration.txt
   #cp inputDoc.txt.enc encoded/inputDoc$((var)).txt.enc
done < cookie.txt


count=0
while read line
do
   ((count++)) 
   java -Xss512M -XX:-UseSplitVerifier -jar encrypt_instrumented.jar send inputDoc.txt $line
   echo $line
   cd myResults/
   mkdir -p log/
   for file in *.log; do mv "$file" "${file/.log/_$((count)).log}"; done
   mv *.log log/
   cd ..
done < cookie.txt

data_count=`cat cookie.txt | wc -l`;
echo $data_count
python Filter.py $data_count
./LinearRegression.R Summary.CSV
