#!/usr/bin/env Rscript

args <- commandArgs(TRUE)
x = read.csv(args, header=TRUE)
results = lm(ExecTime ~ . - 1, data = x)
m = as.matrix(coefficients(results)/1000000000000)
m[is.na(m)] <- 0
y = read.csv('Combined.CSV', header = TRUE)
a = as.matrix(within(y, rm('ExecTime')))
LinearModel = as.matrix(a %*% m)
ExecutionTime = as.matrix(y['ExecTime'])
plot(ExecutionTime, LinearModel)

