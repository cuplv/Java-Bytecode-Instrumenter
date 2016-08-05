#!/bin/bash

cp -r ../../jars/* .
cp ../../other/* .

#Instrument for execution time
java -jar ../exploitr.jar $1 --printMethodCount -d 2
grep "void main(" list_of_methods.txt > mainMethod.txt
java -jar ../exploitr.jar $1 -i execTimeInstrument -x mainMethod.txt
cd sootOutput-Instrument
cp ../$1 .
jar uvf $1 *
cp $1 ../
cd ..
 
#Generate inputs for execution
#Note: Comment this logic if you would like to provide a custom input file
if [ "$2" == "-i" ]
then
  python generateInput.py $3 $4 $5
elif [ "$2" == "-f" ]
then
  python generateReal.py $3 $4 $5
elif [ "$2" == "-s" ]
then
  python generateStrings.py $3 $4 $5
fi

if [ "$#" -eq 5 ] 
then
  ./getExecutionTime.sh $1 
elif [ "$#" -eq 6 ] 
then
  ./getExecutionTime.sh $1 $6
elif [ "$#" -eq 7 ]
then
  ./getExecutionTime.sh $1 $6 $7
elif [ "$#" -eq 8 ]
then
  ./getExecutionTime.sh $1 $6 $7 $8
elif [ "$#" -eq 9 ]
then
  ./getExecutionTime.sh $1 $6 $7 $8 $9
elif [ "$#" -eq 10 ]
then
  ./getExecutionTime.sh $1 $6 $7 $8 $9 $10
elif [ "$#" -eq 11 ]
then
  ./getExecutionTime.sh $1 $6 $7 $8 $9 $10 $11
fi

./Filter_Duration.py
