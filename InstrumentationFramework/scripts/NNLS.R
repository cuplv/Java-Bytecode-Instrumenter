#!/usr/bin/env Rscript

args <- commandArgs(TRUE)
x = read.csv(args[1], header=TRUE)
a = as.matrix(within(x, rm('ExecTime')))
b = as.matrix(x['ExecTime'])
b[is.na(b)] <- 0
a[is.na(a)] <- 0
library(nnls)
y = nnls(a, b)
m = as.matrix(coefficients(y))
m[is.na(m)] <- 0
z = read.csv(args[2], header = TRUE)
a1 = as.matrix(within(z, rm('ExecTime')))
LinearModel = as.matrix(a1 %*% m)
ExecutionTime = as.matrix(z['ExecTime'])
plot(ExecutionTime, LinearModel)

