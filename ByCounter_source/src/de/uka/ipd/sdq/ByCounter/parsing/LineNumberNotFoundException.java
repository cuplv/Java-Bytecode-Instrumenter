package de.uka.ipd.sdq.ByCounter.parsing;

import java.util.Set;

/**
 * Exception thrown when a specified line number cannot be found in 
 * the bytecode of the method.
 * 
 * @author Martin Krogmann
 *
 */
public class LineNumberNotFoundException extends IllegalArgumentException {

	/** Serialization ID. */
	private static final long serialVersionUID = -4964481312212975624L;
	/** The line numbers found in the bytecode of the method. */
	private Set<Integer> availableLineNumbers;
	/** The line numbers that were specified, but could not be found in the method. */
	private Set<Integer> lineNumbersNotFound;
	
	public LineNumberNotFoundException(final String exceptionMessage,
			final Set<Integer> lineNumbersNotFound,
			final Set<Integer> availableLineNumbers) {
		super(exceptionMessage);
		this.availableLineNumbers = availableLineNumbers;
		this.lineNumbersNotFound = lineNumbersNotFound;
	}

	/**
	 * @return The line numbers found in the bytecode of the method.
	 */
	public Set<Integer> getAvailableLineNumbers() {
		return this.availableLineNumbers;
	}
	
	/**
	 * @return The line numbers that were specified, but could not be found 
	 * in the method. 
	 */
	public Set<Integer> getLineNumbersNotFound() {
		return this.lineNumbersNotFound;
	}
}
