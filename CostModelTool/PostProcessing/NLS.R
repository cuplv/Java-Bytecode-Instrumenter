#!/usr/bin/env Rscript

m <- nls(ExecTime ~  function2_139 * (a+2*a+3*a) + function5_37 * (b+2*b+3*b) + function2_147 * (c+2*c+3*c) + function3_25 * (d+2*d+3*d) + function1_19 * (e+2*e+3*e) + function4_31 * (f+2*f+3*f), data = x,start=list(a=1,b=1,c=1,d=1,e=1,f=1))

m <- nls(ExecTime ~  exp(function2_139 * a + function5_37 * b + function2_147 * c + function3_25 * d + function1_19 * e + function4_31 * f), data = x,start=list(a=0,b=0,c=0,d=0,e=0,f=0)
