#!/bin/bash

rm -f jars/*
cd instrument/blocks
rm -rf *.zip log *.txt $1 soot*
cd ../time
rm -rf *.zip $1 *.txt soot*
cd ../../PostProcessing
rm -rf Summary.CSV Rplots.pdf *.txt
cd ..
