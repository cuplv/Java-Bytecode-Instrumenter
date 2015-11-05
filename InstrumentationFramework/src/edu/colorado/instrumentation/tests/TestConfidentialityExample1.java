package edu.colorado.instrumentation.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;
import edu.colorado.instrumentation.helper.subjects.Availability_example_1;
import edu.colorado.instrumentation.helper.subjects.Confidentiality_example_1;

public class TestConfidentialityExample1 {

	@Test
	public void testMain() throws IOException {
		BytecodeCounter counter = new BytecodeCounter();
		counter.getInstrumentationParams().setUseResultCollector(false);
		counter.getInstrumentationParams().enableResultLogWriter(Confidentiality_example_1.class.getCanonicalName() + File.separatorChar + "ByCounter_result_");
		
		counter.getInstrumentationParams().setUseArrayParameterRecording(true);
		counter.getInstrumentationParams().setWriteClassesToDisk(true);
		counter.getInstrumentationParams().setUseBasicBlocks(false);
		
		//Specify the method to be instrumented (several methods are supported as well)
		MethodDescriptor myMethod = new MethodDescriptor(
				Confidentiality_example_1.class.getCanonicalName(),
				"public static void main(java.lang.String[] args)"); //$NON-NLS-1$
		
		//Tell ByCounter to instrument the specified method
		counter.addEntityToInstrument(myMethod);
		counter.instrument();
		String[] args = {"10"};
		//let ByCounter execute the method (note that here, this class is reloaded internally)
		counter.execute(myMethod, new Object[] {args});
	}

	@Test
	public void testSquareMult() throws IOException {
		BytecodeCounter counter = new BytecodeCounter();
		counter.getInstrumentationParams().setUseResultCollector(false);
		counter.getInstrumentationParams().enableResultLogWriter(Confidentiality_example_1.class.getCanonicalName() + File.separatorChar + "ByCounter_result_");
		
		counter.getInstrumentationParams().setUseArrayParameterRecording(true);
		counter.getInstrumentationParams().setWriteClassesToDisk(true);
		counter.getInstrumentationParams().setUseBasicBlocks(false);
		
		//Specify the method to be instrumented (several methods are supported as well)
		MethodDescriptor myMethod = new MethodDescriptor(
				Confidentiality_example_1.class.getCanonicalName(),
				"public static int squareMult(int x, boolean[] e, int n, int N)"); //$NON-NLS-1$
		
		//Tell ByCounter to instrument the specified method
		counter.addEntityToInstrument(myMethod);
		counter.instrument();
		boolean[] b = {true, true, true, false, true};
		//let ByCounter execute the method (note that here, this class is reloaded internally)
		counter.execute(myMethod, new Object[] {4, b, 5, 13});
	}

}
