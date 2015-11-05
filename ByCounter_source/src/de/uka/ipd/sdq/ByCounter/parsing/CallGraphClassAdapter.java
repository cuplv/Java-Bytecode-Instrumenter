package de.uka.ipd.sdq.ByCounter.parsing;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;

/**
 * This class has the method {@link #parseClass(CallGraph, ClassReader)} that can be 
 * used to create a {@link CallGraph} for a given class.
 * @author Martin Krogmann
 *
 */
public final class CallGraphClassAdapter {
	
	private static Logger log = Logger.getLogger(CallGraphClassAdapter.class.getCanonicalName());
	
	private static Map<String, String[]> globalClassImplementationCache = new HashMap<String, String[]>();

	/**
	 * InstrumentatationParameters specified by the user.
	 * Prefixes of packages/classes that will be ignored are specified with 
	 * {@link InstrumentationParameters#setIgnoredPackagePrefixes(String[])}.
	 */
	private InstrumentationParameters instrumentationParameters;
	
	/**
	 * Construct the adapter.
	 * @param instrumentationParameters InstrumentatationParameters specified by the user.
	 * Prefixes of packages/classes that will be ignored are specified with 
	 * {@link InstrumentationParameters#setIgnoredPackagePrefixes(String[])}.
	 */
	public CallGraphClassAdapter(final InstrumentationParameters instrumentationParameters) {
		this.instrumentationParameters = instrumentationParameters;
	}
	
	/**
	 * @param callGraph This is the {@link CallGraph} that will be extended with the method calls
	 * found in the class named className.
	 * @param classReader An initialised {@link ClassReader} for the class holding the methods that shall be parsed.
	 * Needs to be fully qualified as this is used to find the correct class.
	 * @return True, if the class could be found and parsed successfully.
	 */
	public boolean parseClass(final CallGraph callGraph, ClassReader classReader) {
		if(callGraph == null) {
			log.severe("CallGraph was null. Aborting parsing.");
			return false;
		}
		
		Collection<ClassReader> classesToParse = new LinkedList<ClassReader>();
		classesToParse.add(classReader);

		boolean success = true;

		int numThreads = 1;
		if(instrumentationParameters.getNumberCallGraphClassAnalyserThreads() == null) {
			// heuristic for  the selection of the number of threads: max(1, log2(numCores))
			numThreads = Math.max(1, (int)Math.round(Math.floor(Math.log(Runtime.getRuntime().availableProcessors())/Math.log(2))));
		} else {
			// use the specified number
			numThreads = instrumentationParameters.getNumberCallGraphClassAnalyserThreads();
		}
		log.fine("Using " + numThreads + " thread(s) to read classes for call graph.");
		ExecutorService exec = Executors.newFixedThreadPool(numThreads);
		
		while(!classesToParse.isEmpty()) {
			final BlockingQueue<ClassReader> newClassesToParse = new LinkedBlockingQueue<ClassReader>();
			Collection<Callable<List<ClassReader>>> tasks = new LinkedList<Callable<List<ClassReader>>>();
			for(final ClassReader currentCR : classesToParse) {
				tasks.add(new Callable<List<ClassReader>>() {
					@Override
					public List<ClassReader> call() throws Exception {
						try {
							return parseSingleClass(callGraph, currentCR);
						} catch (IOException e) {
							log.severe("Could not parse class with name '" + currentCR.getClassName() + "'. Skipping.");
						}
						return null;
					}
				});
			}
			try {
				List<Future<List<ClassReader>>> answers = exec.invokeAll(tasks);
				for(Future<List<ClassReader>> a : answers) {
					newClassesToParse.addAll(a.get());
				}
			} catch (InterruptedException e) {
				log.severe("Class parsing did not execute properly.");
				throw new RuntimeException(e);
			} catch (ExecutionException e) {
				log.severe("Class parsing did not execute properly.");
				throw new RuntimeException(e);
			}
			
			// classesToParse is complete; go to newClassesToParse
			classesToParse = newClassesToParse;
		}
		
		return success;
	}

	/**
	 * Parse the class given as a class reader and adds method calls to the 
	 * call graph.
	 * @param callGraph Call graph that will be written to.
	 * @param currentCR ClassReader for a class.
	 * @return A {@link List} of classes that are used by this class. These
	 * need to be parsed as well for a complete call graph.
	 * @throws IOException Thrown when a class cannot be read.
	 */
	@SuppressWarnings("unchecked")
	private List<ClassReader> parseSingleClass(final CallGraph callGraph, ClassReader currentCR) throws IOException {
		List<ClassReader> newClassesToParse = new LinkedList<ClassReader>();
		// check if the class was already parsed to avoid endless recursion
		if(callGraph.getParsedClasses().contains(currentCR.getClassName())) {
			log.finer("Class already parsed");
			return newClassesToParse;
		}
		
		// check if the class is to be ignored
		for(String prefix : instrumentationParameters.getIgnoredPackagePrefixes()) {
			if (currentCR.getClassName().startsWith(prefix)) {
				log.finer("Class is in ignored package: " + currentCR.getClassName());				
				return newClassesToParse;
			}
		}
		
		log.info("Parsing class: " + currentCR.getClassName());
		// We can now parse the class 
		callGraph.addParsedClass(currentCR.getClassName());
		
		// transformation chain:
		ClassNode cn = new ClassNode();		
		ClassReader cr = currentCR;
		
		cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		
		// go through all methods
		Iterator<MethodNode> it = cn.methods.iterator();
		while(it.hasNext()) {
			MethodNode m = it.next();
			CallGraphMethod m1 = new CallGraphMethod(cr.getClassName(), m.name, m.desc);
			if(cr.getClassName().startsWith("[")) {
				continue;
			}
			log.fine("Parsing method: " + m.name + m.desc);
			
			// go through all instructions in the method
			Iterator<AbstractInsnNode> iterator = m.instructions.iterator();
			while(iterator.hasNext()) {
				AbstractInsnNode insn = iterator.next();
				if(insn instanceof MethodInsnNode) {
					// A method invocation
					MethodInsnNode methodCall = ((MethodInsnNode)insn);

		        	if(methodCall.owner.charAt(0) == '[') {					        	                                                                 
		        		// this case happens for example for [I.clone()
		        		continue;
		        	}
					
					if(insn.getOpcode() == Opcodes.INVOKEINTERFACE) {
						// use a class finder tool to find all implementing classes
						IImplementingClassesFinder finder = new CachedImplementingClassesFinder(new ClapperImplementingClassesFinder(), globalClassImplementationCache);
						try {
							Class<?> interfaceClass = Class.forName(methodCall.owner.replace('/', '.'));
							String[] foundClasses = finder.findImplementingClasses(interfaceClass);
					        for(String foundClassName : foundClasses) {
					        	if(methodCall.owner.charAt(0) == '[' || foundClassName.charAt(0) == '[') {					        	                                                                 
					        		// this case happens for example for [I.clone()
					        		continue;
					        	}
					     		// Add an edge from the node representing this method to the the node
					     		// for methodCall
					     		CallGraphMethod m2 = 
					     			new CallGraphMethod(foundClassName, methodCall.name, methodCall.desc);

					     		// this method (m1) can call the method m2 since a MethodInsnNode for m2 was found
					     		// add the call to the graph
					     		callGraph.addMethodCall(m1, m2);
					     		
					     		// now parse the class that contains m2
					     		newClassesToParse.add(new ClassReader(methodCall.owner));
					        }
						} catch (ClassNotFoundException e) {
							log.severe("Could not find definition for called method.");
							throw new RuntimeException(e);
						}
					} else { // not invoked by interface

						// Add an edge from the node representing this method to the the node
						// for methodCall
						CallGraphMethod m2 = 
							new CallGraphMethod(methodCall.owner, methodCall.name, methodCall.desc);
	
						// this method (m1) can call the method m2 since a MethodInsnNode for m2 was found
						// add the call to the graph
						callGraph.addMethodCall(m1, m2);
						
						// now parse the class that contains m2
						newClassesToParse.add(new ClassReader(methodCall.owner));
					}
				}
			}
		}
		return newClassesToParse;
	}
}
