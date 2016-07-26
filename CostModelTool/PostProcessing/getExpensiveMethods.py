#!/usr/bin/python

import sys

lookup = "(Intercept)"
methodDict = {}
f = open(sys.argv[1], "r+")
lines = f.readlines()

with open(sys.argv[1], "r+") as myFile:
    for num, line in enumerate(myFile, 1):
        if lookup in line:
            lines = lines[num-1:]
	    break

lines = lines[2:]
for index in range(0,len(lines)-1):
	if index % 2 == 0 or index == 0:
		methodDict[lines[index]] = 0
		key = lines[index]
	elif index % 2 != 0:
		methodDict[key] = lines[index]

fw = open("expensiveMethods.txt", "w+")
for k,v in methodDict.items():
	fw.write(str(k).strip(' +').strip('\n') + ": " + str(v).strip('\t').strip(' +').strip('\n'))
	fw.write('\n')
fw.close()
f.close()
