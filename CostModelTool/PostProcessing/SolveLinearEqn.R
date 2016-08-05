#!/usr/bin/env Rscript

args <- commandArgs(TRUE)
x = read.csv(args[1], header=TRUE)
a = as.matrix(within(x, rm('ExecTime')))
a[is.na(a)] <- 0
b = as.matrix(x['ExecTime'])
b[is.na(b)] <- 0
mat = matrix(0)

check.independence <- function(j,count) {
    for(k in 1:count-1){
	if(k == 0) k = 1
        if(cor(a[j,],mat[k,]) < 0) {
	    next
	}
        else {
	    return(-1)
	}
    }
    return(1)
}

for (i in 1:nrow(a)) {
    count = 1 
    mat[count,] = a[i,]
    count = count + 1
    for (j in 1:nrow(a)) {
	if(check.independence(j,count) == 1) {
	   mat[count,] = a[j,]
	   count = count + 1
	}
	print(count)
	print(ncol(a))
	if(count-1 == ncol(a)) {
	    print(mat)
	    for(x in 1:nrow(mat)){
            	write(mat[x,],file=args[2],ncolumns=ncol(mat),sep=",",append=TRUE)
	    }
	    break
	}
    }
    if(count-1 == ncol(a)) break
    mat = matrix(NA, nrow=ncol(a),ncol=ncol(a))
}

