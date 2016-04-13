#!/usr/bin/python

f = open("Duration.txt","r+")
f1 = open("Duration_updated.txt","w+")

lines = f.readlines()

for line in lines:
	if "DURATION:" in line:
		temp = line.split(":")
		f1.write(temp[1].strip(" "))

f.close()
f1.close()
