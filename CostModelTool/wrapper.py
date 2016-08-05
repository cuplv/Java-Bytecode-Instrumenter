#!/usr/bin/python

import sys
import subprocess

if sys.argv[1] == "-h":
  print "\n"
  print "Usage - Cost Model tool commands"
  print "\n"
  print "$./wrapper.py <Test_jar_file> -<input_type> <no_of_input_args> -range <input_range_min> <input_range_max> <type_of_instrumentation>"
  print "\n"
  print " <Test_jar_file>           : Test application jar filename"
  print " <input_type>              : Input argument type (-i : Integer | -f : Float | -s : String)"
  print " <no_of_input_args>        : Number of arguments required by application (E.g arguments 0,1,2,3...)"
  print " <input_range_min>         : Min range of input (Integer for -i type | Real for -f type)"
  print " <input_range_max>         : Max range of input (Integer for -i type, Real for -f type)"
  print " <type_of_instrumentation> : basic block instrumentation or method instrumentation (block | method)"
  print "\n"
  print " Example: $ ./wrapper.py Test.jar -i 2 -range 0 10 blocks"
  print "\n"
  print " This example run command runs the cost model for basic blocks on integer inputs. The number of input arguments are 2 and the range for each of the arguments is within 0-10."
  print "\n"
else:
  APPJAR=sys.argv[1]

  if sys.argv[2] == "-i":
	subprocess.call(["./run.sh " + APPJAR + " " + "-i" + " " + sys.argv[3] + " " + sys.argv[4] + " " + sys.argv[5] + " " + sys.argv[6] + " " + sys.argv[7] + " " + ">" + " " + "log.txt"],shell=True)
  elif sys.argv[2] == "-f":
	subprocess.call(["./run.sh " + APPJAR + " " + "-f" + " " + sys.argv[3] + " " + sys.argv[4] + " " + sys.argv[5] + " " + sys.argv[6] + " " + sys.argv[7] + " " + ">" + " " + "log.txt"],shell=True)
  elif sys.argv[2] == "-s":
	subprocess.call(["./run.sh " + APPJAR + " " + "-s" + " " + sys.argv[3] + " " + sys.argv[4] + " " + sys.argv[5] + " " + sys.argv[6] + " " + sys.argv[7] + " " + ">" + " " + "log.txt"],shell=True)
  else:
	print 0
