#!/bin/bash

./clean.sh $1

arg1=`echo $8 | cut -d " " -f1`
arg2=`echo $8 | cut -d " " -f2`
arg3=`echo $8 | cut -d " " -f3`
arg4=`echo $8 | cut -d " " -f4`
arg5=`echo $8 | cut -d " " -f5`

cp $1 jars/

#Instrument for execution time and run the application
cd instrument/time
./execTime.sh $1 $2 $3 $5 $6

#Instrument for method/block count and run the application
cd ../blocks
cp ../time/input.txt .
cp -r ../../other/* .
cp -r ../../jars/* .
./instrument.sh $1 $7

#Regression analysis
cd ../../PostProcessing
cp ../instrument/time/input.txt .
cp ../instrument/time/Duration_updated.txt .
cp ../instrument/blocks/list_of_*.txt .
data_count=`cat input.txt | wc -l`;
echo $data_count
if [ "$7" == "methods" ]
then
  python Filter_instructions.py $data_count list_of_methods.txt
elif [ "$7" == "blocks" ]
then
  python Filter_instructions.py $data_count list_of_blocks.txt
fi
#Result is stored in ~PostProcessing/coefficients.txt
./LinearRegression.R Summary.CSV Summary.CSV > coefficients.txt
./LinearRegression.R Summary.CSV Summary.CSV
