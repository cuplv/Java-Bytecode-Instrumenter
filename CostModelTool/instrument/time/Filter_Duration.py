#!/usr/bin/python

f = open("Duration.txt","r+")
f1 = open("Duration_updated.txt","w+")

lines = f.readlines()

for line in lines:
	if "void main" in line:
		line = line.split(">")
		f1.write(line[1].strip(" "))

f.close()
f1.close()
