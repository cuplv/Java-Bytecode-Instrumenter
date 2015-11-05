package de.uka.ipd.sdq.ByCounter.parsing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * {@link ClassAdapter} that reads a class and executes 
 * {@link FindLineNumbersMethodAdapter} for each method found.
 * The resulting line numbers are available through 
 * {@link #getLineNumbersPerMethod()}.
 * @author Martin Krogmann
 */
public class FindLineNumbersClassAdapter extends ClassAdapter {

	/**
	 * Line numbers per method.
	 */
	protected Map<String, List<Integer>> lineNumbersPerMethod;
	/** Class name in byte code notation. */
	protected String classNameBC;
	/** Construct with the class visitor of the transformation chain. */
	public FindLineNumbersClassAdapter(ClassVisitor cv) {
		super(cv);
		lineNumbersPerMethod = new HashMap<String, List<Integer>>();
	}
	
	@Override
	public void visit(int version, int access, String name,
			String signature, String superName, String[] interfaces) {
		// save class name
		this.classNameBC = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name,
			String desc, String signature, String[] exceptions) {
		// visit the class and find the line numbers
		MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
		FindLineNumbersMethodAdapter flna = new FindLineNumbersMethodAdapter(mv);
		List<Integer> methodLineNumbers = flna.getLineNumbers();
		MethodDescriptor md = MethodDescriptor._constructMethodDescriptorFromASM(this.classNameBC, name, desc);
		this.lineNumbersPerMethod.put(md.getCanonicalMethodName(), methodLineNumbers);
		return flna;
	}
	
	/**
	 * @return Line numbers per method 
	 * ({@link MethodDescriptor#getCanonicalMethodName()}).
	 */
	public Map<String, List<Integer>> getLineNumbersPerMethod() {
		return this.lineNumbersPerMethod;
	}
}