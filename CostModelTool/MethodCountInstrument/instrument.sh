#!/bin/bash

mkdir sootOutput-Instrument/
mkdir log/
#java -jar auditr.jar $1 --printMethodCount -d 2
#java -jar auditr.jar $1 -a loopCodeInstrument -x list_of_loops.txt
java -jar auditr.jar $1 -a expFunInstrument -x list_of_methods.txt
cd sootOutput-Instrument/
cp ../$1 .
jar uvf $1 *
cd ..
cp sootOutput-Instrument/$1 .
cp $1 lib/
cp ../other/* .
./getMethodCount.sh $2
