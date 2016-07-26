#!/usr/bin/env Rscript

args <- commandArgs(TRUE)
x = read.csv(args[1], header=TRUE)
a = as.matrix(within(x, rm('ExecTime')))
a[is.na(a)] <- 0
n = names(within(x, rm('ExecTime')))
count = 0
mat=matrix("xyz")
for(i in 1:ncol(a)) {
    for(j in 1:ncol(a)) {
	val = cor(a[,i],a[,j])
	if(!is.na(val) && (i != j) && (val > 0.3)) {
		flag = 0
		if(count > 0){
		for(k in 1:count) {
			if( (!is.na(mat[k]) && !is.na(n[j])) && ((paste(n[i],n[j]) == mat[k]) || (paste(n[j],n[i]) == mat[k]))) {
			    print(n[j])
			    flag = 1	
			    break
			} 
		}
		}
		if(flag == 1) next
		mat[count] = paste(n[i],n[j])
		count = count + 1
		mat[count] = paste(n[j],n[i])
		count = count + 1
		write(n[i], "Methods.csv", append=TRUE)
		
	}
    }
}
print(mat)
