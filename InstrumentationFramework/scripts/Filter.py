import sys
import glob
import xlwt
import csv

d = {}

def writeToSheet(filename, sheet, row):
    if row == 0:
    	book = xlwt.Workbook()
    	sh = book.add_sheet(sheet)
    	sh.write(0, 0, "Summary of instrumentation")
    	for i in range(0, len(d)):
    		sh.write(1, i, d.keys()[i])
    		sh.write(row+2, i, d.values()[i])

def writeToCSV(filename, row):
	with open(filename, 'a+') as csvfile:
		#for i in range(0, len(d)):
		spamwriter = csv.writer(csvfile, delimiter=',', quoting=csv.QUOTE_MINIMAL)
		if row == 1:
			spamwriter.writerow(d.keys())
		spamwriter.writerow(d.values())

if __name__ == "__main__":
   for i in range(1,int(sys.argv[1])+1):
	print "myResults/log/*_"+str(i)+".log"
	fd = open("Duration.txt", "r+")
  	for filename in glob.glob("myResults/log/*_"+str(i)+".log"):
		f = open(filename, "r+")
		fw = open(filename+"_summary", "w+")
		lines = f.readlines()
		lines = lines[6:]
		#f.seek(0)
		for line in lines:
			if "Method" in line or "Array" in line:
				continue
			if "; 0" not in line and "//" not in line:
				fw.write(line)
			if "//" in line:	
				fw.write(line)
		#f.truncate()
		fw.close()
	duration = fd.readlines()
  	for filename in glob.glob("myResults/log/*_"+str(i)+".log"):
		f = open(filename, "r+")
		lines = f.readlines()
		lines = lines[6:]
		#f.seek(0)
		d["ExecTime"] = duration[i-1].strip('\n').strip(' ')
		d["STRCALL"] = d.setdefault("STRCALL", 0)
		d["IOCALL"] = d.setdefault("IOCALL", 0)
		d["ARRCOUNT"] = d.setdefault("ARRCOUNT", 0)
		d["MISCCALL"] = d.setdefault("MISCCALL", 0)
		for line in lines:
			if "Method" in line or "Array" in line:
				continue
			if "null; 0" not in line and "//" not in line and "INVOKE" not in line:
				lst = line.split(";")
				d[lst[0]] = d.setdefault(lst[0], 0) + int(lst[1].strip('\n').strip(' '))
			if "//" in line:
				lst = line.split(";")
				if "java.lang.String" in lst[0]:
					if "STRCALL" not in d.keys():
						d["STRCALL"] = d.setdefault("STRCALL", 0) + int(lst[-1].strip('\n').strip(' '))
					else:
						d["STRCALL"] += int(lst[-1].strip('\n').strip(' '))
				elif "java.io" in lst[0]:
					if "IOCALL" not in d.keys():
						d["IOCALL"] = d.setdefault("IOCALL", 0) + int(lst[-1].strip('\n').strip(' '))
					else:
						d["IOCALL"] += int(lst[-1].strip('\n').strip(' '))
				elif "count:" in lst[0]:
					cnt = lst[0].split(":")
					if "ARRCOUNT" not in d.keys():
						d["ARRCOUNT"] = d.setdefault("ARRCOUNT", 0) + int(cnt[-1].strip('\n').strip(' '))
					else:
						d["ARRCOUNT"] += int(cnt[-1].strip('\n').strip(' '))
				else:
					if "MISCCALL" not in d.keys():
						d["MISCCALL"] = d.setdefault("MISCCALL", 0) + int(lst[-1].strip('\n').strip(' '))
					else:
						d["MISCCALL"] += int(lst[-1].strip('\n').strip(' '))
					#f.write(line)
		#f.truncate()
		f.close()
	print i
  	writeToCSV("Summary.CSV", int(i))
	d = {}
	fd.close()
