import sys
import glob
import csv

d = {}

def writeToCSV(filename, row):
	with open(filename, 'a+') as csvfile:
		#for i in range(0, len(d)):
		spamwriter = csv.writer(csvfile, delimiter=',', quoting=csv.QUOTE_MINIMAL)
		if row == 1:
			spamwriter.writerow(d.keys())
		spamwriter.writerow(d.values())

if __name__ == "__main__":
   for i in range(1,int(sys.argv[1])+1):
	fd = open("Duration_updated.txt", "r+")
	duration = fd.readlines()
	mfile = open(sys.argv[2],"r+")
	#mfile = open("list_of_methods.txt","r+")
	methods = mfile.readlines()
	for method in methods:
		d[str(method).strip('\n')] = d.setdefault(str(method).strip('\n'), 0)
	d["ExecTime"] = d.setdefault("ExecTime", 0)
  	for filename in glob.glob("../MethodCountInstrument/log/methodCount"+str(i)+".log"):
		f = open(filename, "r+")
		lines = f.readlines()
		d["ExecTime"] = duration[i-1].strip('\n').strip(' ')
		#mfile = open("list_of_methods.txt","r+")
		#methods = mfile.readlines()
		#for index in range(0, len(methods)-1):
		for line in lines:
			if "INSTRUMENTED" not in line:
				continue
			line = line.replace("INSTRUMENTED:", "")
			loopCount = line.strip('\n').strip(' ').split('\t')
			print loopCount[0]
			#print loopCount[1]
			d[str(loopCount[0]).strip('\n')] += int(loopCount[1])
		f.close()
	print i
  	writeToCSV("Summary.CSV", int(i))
	d = {}
	fd.close()
	mfile.close()
