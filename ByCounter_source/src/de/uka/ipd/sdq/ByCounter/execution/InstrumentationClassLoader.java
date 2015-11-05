package de.uka.ipd.sdq.ByCounter.execution;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.Loader;
import javassist.NotFoundException;

/**
 * {@link ClassLoader} for ByCounter that handles loading of instrumented 
 * classes.
 * <p>Interfaces will be loaded using the default class loader instead, so
 * that objects of instrumented classes can be used by their interfaces in 
 * external code.
 * </p>
 * @author Martin Krogmann
 *
 */
public class InstrumentationClassLoader extends java.lang.ClassLoader {
	
	/**
	 * A logger instance (java.util Logging)
	 */
	private static final Logger log = Logger.getLogger(InstrumentationClassLoader.class.getCanonicalName());
	
	/**
	 * When a method is instrumented by ByCounter, the class that holds the 
	 * method is changed. So if the unchanged class has been loaded by a 
	 * {@link InstrumentationClassLoader}, we need to make sure that the instrumented version
	 * of the class is used. 
	 * This Javassist {@link ClassPool} holds the instrumented classes that 
	 * are then used instead of their unmodified versions.
	 */
	private final ClassPool classPool;

	/**
	 * The class loader to which the loading itself is delegated. This class only stores the class pool and reuses the loader.
	 */
	private Loader delegationLoader;

	/**
	 * A list of canonical class names for all classes that have been
	 * updated in the classpool using 
	 * {@link #updateClassInClassPool(String, byte[])}.
	 */
	private final List<String> classesInClassPool;

	private final Map<String, Class<?>> ctClassCache;

	private Set<String> externalClassesDefinition;
	
	/**
	 * Construct the class loader with a default class pool.
	 * This constructor is defined so the class loader can be used as the 
	 * system class loader.
	 * @see ClassLoader#getSystemClassLoader()
	 * @param parentClassLoader System class loader parent.
	 */
	public InstrumentationClassLoader(ClassLoader parentClassLoader) {
		super(parentClassLoader);
		this.setParentClassLoader(parentClassLoader);
		this.classPool = new ClassPool();
		this.classPool.appendSystemPath();
		this.classesInClassPool = new LinkedList<String>();
		this.ctClassCache = new HashMap<String, Class<?>>();
		this.externalClassesDefinition = null;
		delegationLoader = new Loader(this.classPool);
	}
	
	/**
	 * Updates the class definition for the class given by className
	 * and bytes by adding it to the Javassist {@link ClassPool}.
	 * @param className Fully qualified name of the given class.
	 * @param bytes Byte array representing the class.
	 */
	public void updateClassInClassPool(String className, byte[] bytes) {
		log.fine("Updating class pool");
		if(className == null || className.length() == 0) {
			throw new RuntimeException(
					new ClassNotFoundException("Cannot update class pool because the given classname "
					+ "was null or invalid."));
		} else if(bytes == null) {
			throw new RuntimeException(
				new ClassNotFoundException("Cannot update class pool as the given byte[] for the class"
					+ "was null."));
		}
		if(this.classesInClassPool.contains(className)) {
			try {
				// remove the previous definition from the class pool
				log.info("Removing previous definition of '" + className 
						+ "' from the class pool to allow for a new definition.");
				this.classPool.get(className).detach();
			} catch (NotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		if(this.ctClassCache.containsKey(className)) {
			throw new IllegalStateException("The class '" + className + "' was previously defined and cannot be redefined correctly!");
		}
		// make the class known to the class pool
		this.classPool.insertClassPath(new ByteArrayClassPath(className, bytes));
		this.classesInClassPool.add(className);
	}
	

	/**
	 * @param canonicalClassName Canonical class name.
	 */
	public Class<?> loadClass(String canonicalClassName) throws ClassNotFoundException {
			
		CtClass ctClassToExecute = null;
		// get the CtClass from the pool
		try {
			ctClassToExecute = this.classPool.get(canonicalClassName);
		} catch (NotFoundException e) {
			throw new ClassNotFoundException("Class pool cannot find the class " + canonicalClassName + ".", e);
		}
		
		// make sure that the CountingResultCollector (important!) and all other 
		// ByCounter classes do not get reloaded.
		delegationLoader.delegateLoadingOf("de.uka.ipd.sdq.ByCounter.execution.");
		if(this.externalClassesDefinition != null) {
			for(String external : this.externalClassesDefinition) {
				// Javassist uses '.' as a wild card.
				if(external.charAt(external.length()-1) == ExecutionSettings.CLASSES_DEFINITION_WILDCARD_CHAR) {
					String externalName = external.substring(0, external.length()-1)+'.';
					delegationLoader.delegateLoadingOf(externalName);
				} else {
					delegationLoader.delegateLoadingOf(external);
				}
			}
		}
		
		// use the ClassLoader loader to get the Class<?> object
		// use a standard protection domain
		try {
			Class<?> rClass = ctClassCache.get(canonicalClassName);
			if(rClass == null) {
				rClass = ctClassToExecute.toClass(
						delegationLoader
						);
				ctClassCache.put(canonicalClassName, rClass);
			}
			return rClass;
		} catch (CannotCompileException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Tries to load the class from a .class file.
	 * {@inheritDoc}
	 */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		
		// TODO: try to find a test if a parent class loader has a definition for an instrumented class in order to provide a useful error message
		String file = name.replace('.', File.separatorChar) + ".class";
		byte[] b = null;
		try {
			b = loadClassData(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Class<?> c = defineClass(name, b, 0, b.length);
		resolveClass(c);
		return c;
	}
	
	/**
	 * Uses the system class loader to find the given file data.
	 * @param file Filename of a .class.
	 * @return Class file as byte[].
	 * @throws IOException When accessing the file fails, this is thrown.
	 */
	private byte[] loadClassData(String file) throws IOException {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(file);
		int size = stream.available();
		byte buff[] = new byte[size];
		DataInputStream in = new DataInputStream(stream);
		in.readFully(buff);
		in.close();
		return buff;
	}
	
	/**
	 * @return The class loader used internally by this class loader.
	 */
	public ClassLoader getInnerClassLoader() {
		return this.delegationLoader;
	}

	/**Replaces the parent class loader.
	 * @param parentClassLoader The parent class loader to use.
	 */
	public void setParentClassLoader(ClassLoader parentClassLoader) {
		if (delegationLoader != null && delegationLoader.getParent() != null) {
			log.warning("Replacing parent class loader. Accessing classes, which were loaded by the old parent, may not be accessible anymore.");
		}
		if(parentClassLoader == null) {
			delegationLoader = new Loader(this.classPool);
		} else {
			delegationLoader = new Loader(parentClassLoader, this.classPool);
		}
	}

	/**
	 * @param externalToClassLoaderClassesDefinition Definition of classes 
	 * that are not to be delegated by this class loader.
	 */
	public void setExternalClassesDefinition(
			Set<String> externalToClassLoaderClassesDefinition) {
		this.externalClassesDefinition = externalToClassLoaderClassesDefinition;
	}
}
