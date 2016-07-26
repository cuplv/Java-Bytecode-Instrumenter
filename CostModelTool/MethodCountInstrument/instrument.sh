#!/bin/bash

mkdir sootOutput-Instrument/
mkdir log/
java -jar exploitr.jar $1 --printMethodCount -d 2
java -jar exploitr.jar $1 -i expFunInstrument -x list_of_methods.txt
#java -jar exploitr.jar $1 -i blockCodeInstrument -x list_of_blocks.txt
cd sootOutput-Instrument/
cp ../$1 .
jar uvf $1 *
cd ..
cp sootOutput-Instrument/$1 .
cp $1 lib/
cp ../other/* .
./getMethodCount.sh $1
