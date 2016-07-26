import random
import sys
from random import randint

#f.write(''.join(random.choice('0123456789') for i in range(0,4)))
f = open("input.txt", "w+")
for ct in range(1, 200):
	f.write(str(randint(1,10)))
	f.write('\n')
