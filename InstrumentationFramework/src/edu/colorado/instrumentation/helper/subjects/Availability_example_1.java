package edu.colorado.instrumentation.helper.subjects;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

class Shape {
    int position;

    Shape(int n) {
		position = n;
    }
}

class CandleStick extends Shape {
    CandleStick(int n) {
		super(n);
    }
}

class Circle extends Shape{
    Circle(int n) {
		super(n);
    }
}

public class Availability_example_1 {
	
	private static List<Shape> objectPosition = new ArrayList<Shape>();
	
	public static void main(String[] args){
		final int N = Integer.parseInt(args[0]);
		//final int N = 72165;
		Availability_example(N);

	}
	
	public static void Availability_example(int N) {
	    
		for(int i = 0; i < N; i++)
		    objectPosition.add(new Circle(i));
	    }
	    
	    // We want to add new objects and calculate their positions based on old objects.
	    public double addNewObject(Shape shape) {
		// There are several categories of new objects. One of them requires long and complicated calculation, while the others don't.
		int result = 0;
		if(shape instanceof CandleStick) {
		    // CANDLE_STICK represents the category of CandleStick
		    int count = 0;
		    for(ListIterator<Shape> i = objectPosition.listIterator(); i.hasNext();) {
			result += i.next().position;
			count++;
		    }
		    return (double)result/count;
		} else {
		    // Otherwise, simply return 0
		    return result;
		}
	    }
}


