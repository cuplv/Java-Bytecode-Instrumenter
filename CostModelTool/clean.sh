#!/bin/bash

cd MethodCountInstrument
rm -rf *.zip log *.txt
cd ../ExecTime/instrument
rm -rf *.zip $1 Duration.txt Duration_updated.txt
cd ../../PostProcessing
rm -rf Summary.CSV Rplots.pdf
cd ..
