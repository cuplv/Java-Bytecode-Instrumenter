#!/usr/bin/python

import random
import sys
from random import randint

FILE_NAME="input.txt"
NUMBER_OF_ROWS=200
INPUT_RANGE_MIN=int(sys.argv[2])
INPUT_RANGE_MAX=int(sys.argv[3])
NUMBER_OF_ARGS=int(sys.argv[1])

f = open(FILE_NAME, "w+")
for ct in range(0, NUMBER_OF_ROWS):
	for args in range(0, NUMBER_OF_ARGS):
		f.write(str(randint(INPUT_RANGE_MIN,INPUT_RANGE_MAX)))
		f.write(' ')
	f.write('\n')
