#!/usr/bin/env Rscript

args <- commandArgs(TRUE)
x = read.csv(args[1], header=TRUE)
full = lm(ExecTime ~ . - 1, data = x)
#stepResults = step(full, data=x, direction="backward")
#results = lm(ExecTime ~ 1, data=x)
#stepResults = step(results, direction = "forward", scope=list(lower=results, upper=full))
#coefficients(stepResults)
summary(full)
m = as.matrix(coefficients(full))
m[is.na(m)] <- 0
y = read.csv(args[2], header = TRUE)
a = as.matrix(within(y, rm('ExecTime')))
LinearModel = as.matrix(a %*% m)
ExecutionTime = as.matrix(y['ExecTime'])
plot(ExecutionTime, LinearModel)

