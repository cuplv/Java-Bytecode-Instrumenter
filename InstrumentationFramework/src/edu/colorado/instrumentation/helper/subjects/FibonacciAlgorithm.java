package edu.colorado.instrumentation.helper.subjects;

public class FibonacciAlgorithm {
	public long fibonacci(long rounds) {
		long i1 = 0;
		long i2 = 1;
		long i3 = 0;
		// normalized loop
		for (
				long i = 0; 
				i < rounds; 
				i++) {
			i3 = i1 + i2;
			i2 = i1;
			i1 = i3;
		}
		return i3;
	}
}
