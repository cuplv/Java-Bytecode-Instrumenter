package de.uka.ipd.sdq.ByCounter.parsing;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link IImplementingClassesFinder} that wraps an implementation adding
 * a cache so that already found sets of implementing classes are not searched 
 * for repeatedly.
 * @author Martin Krogmann
 */
public class CachedImplementingClassesFinder implements
		IImplementingClassesFinder {

	/** A cache for already found implementations for classes. Once
	 * an interface has an entry in the cache, no more searching is done. */
	private Map<String, String[]> classCache;
	private IImplementingClassesFinder wrappedClassFinder;
	
	/**
	 * @param wrappedClassFinder {@link IImplementingClassesFinder} used when the
	 * interface is not cached yet.
	 * @param classCache A cache for already found implementations for classes. Once
	 * an interface has an entry in the cache, no more searching is done. When null,
	 * a new {@link HashMap} is created.
	 */
	public CachedImplementingClassesFinder(
			final IImplementingClassesFinder wrappedClassFinder, 
			final Map<String, String[]> classCache) {
		this.wrappedClassFinder = wrappedClassFinder;
		if(classCache != null) {
			this.classCache = classCache;
		} else {
			this.classCache = new HashMap<String, String[]>();
		}
	}

	@Override
	public String[] findImplementingClasses(final Class<?> interfaceToImplement) {
		String[] resultingClasses;
		// try to find a cached entry
		resultingClasses = this.classCache.get(interfaceToImplement.getCanonicalName());
		if(resultingClasses != null) {
			return resultingClasses;
		}

		resultingClasses = this.wrappedClassFinder.findImplementingClasses(interfaceToImplement);
       	// add the results to the cache
       	this.classCache.put(interfaceToImplement.getCanonicalName(), resultingClasses);
       	return resultingClasses;
	}

}
