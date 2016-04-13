#!/bin/bash

cd instrument
cp -r ../../jars/* .
cp -r ../../classes/* .
cp ../../other/* .
java -cp .:asm-all-5.0.3.jar:lib/commons-cli-1.3.jar Instrumenter $2 $2
jar uvf $1 $2
cp $1 lib/
#python generateInput.py

if [ "$#" -eq 3 ] 
then
  ./getExecutionTime.sh $3
elif [ "$#" -eq 4 ]
then
  ./getExecutionTime.sh $3 $4
elif [ "$#" -eq 5 ]
then
  ./getExecutionTime.sh $3 $4 $5
elif [ "$#" -eq 6 ]
then
  ./getExecutionTime.sh $3 $4 $5 $6
elif [ "$#" -eq 7 ]
then
  ./getExecutionTime.sh $3 $4 $5 $6 $7
elif [ "$#" -eq 8 ]
then
  ./getExecutionTime.sh $3 $4 $5 $6 $7 $8
fi

python Filter_Duration.py
