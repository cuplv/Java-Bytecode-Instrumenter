import random
import sys

f = open("cookie.txt", "w+")
count = 16
for ct in range(1, 1000):
	count = count + 1
	if count == 32:
		count = 16
	f.write(''.join(random.choice('0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz') for i in range(count)))
	f.write('\n')
