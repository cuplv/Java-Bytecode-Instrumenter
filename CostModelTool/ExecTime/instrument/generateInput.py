import random
import sys

f = open("input.txt", "w+")

for ct in range(1, 200):
	#if ct % 2 == 0:
	f.write(''.join(random.choice('abcdefghijklmnopqrstuvwxyz') for i in range(random.randint(1,10))))
	#else:
	#f.write("pas" + ''.join(random.choice('abcdefghijklmnopqrstuvwxyz') for i in range(random.randint(1,10))))
	f.write('\n')
