#!/bin/bash

#./clean.sh $1 $2

arg1=`echo $3 | cut -d " " -f1`
arg2=`echo $3 | cut -d " " -f2`
arg3=`echo $3 | cut -d " " -f3`
arg4=`echo $3 | cut -d " " -f4`
arg5=`echo $3 | cut -d " " -f5`
'''
cd ExecTime
./execTime.sh $1 $2 $arg1 
'''
#cd ../MethodCountInstrument
cd MethodCountInstrument
cp ../ExecTime/instrument/input.txt .
cp -r ../other/* .
cp -r ../jars/* .
./instrument.sh $1 $arg1

cd ../PostProcessing
cp ../ExecTime/instrument/Duration_updated.txt .
#cp ../MethodCountInstrument/list_of_loops.txt .
cp ../MethodCountInstrument/list_of_methods.txt .
data_count=`cat input.txt | wc -l`;
echo $data_count
python Filter_instructions.py $data_count list_of_methods.txt
./LinearRegression.R Summary.CSV Summary.CSV 

