#!/usr/bin/env Rscript

args <- commandArgs(TRUE)
x = read.csv(args[1], header=TRUE)
a = as.matrix(within(x, rm('ExecTime')))
a[is.na(a)] <- 0
b = as.matrix(x['ExecTime'])
b[is.na(b)] <- 0
library(nnls)
y = nnls(a, b)
coefficients(y)
m = as.matrix(coefficients(y))
m[is.na(m)] <- 0
z = read.csv(args[2], header = TRUE)
a1 = as.matrix(within(z, rm('ExecTime')))
#a1 = t(apply(a1, 1, function(a1)(a1-min(a1))/(max(a1)-min(a1))))
PredictedTime = as.matrix(a1 %*% m)
#write.csv(m ,'LinearModel1.CSV')
ExecTime = as.matrix(z['ExecTime'])
#plot(LinearModel, ExecutionTime)
plot(ExecTime, PredictedTime)
#plot(ExecTime, PredictedTime, xlim = c(0.15,0.28))

