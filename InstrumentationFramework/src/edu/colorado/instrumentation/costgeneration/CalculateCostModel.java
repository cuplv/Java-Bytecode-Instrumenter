package edu.colorado.instrumentation.costgeneration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import de.uka.ipd.sdq.ByCounter.execution.*;
import de.uka.ipd.sdq.ByCounter.utils.*;

public class CalculateCostModel {

	public int cost(HashMap<String, ArrayList<String>> clist) throws Exception {
		
		Iterator it = clist.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
			String className = (String) pair.getKey();
			
			ArrayList<String> methodList = new ArrayList<String>();
			methodList = (ArrayList<String>) pair.getValue();
			
			Collections.reverse(methodList);
			BytecodeCounter counter = new BytecodeCounter();
			
			for(int i = 0; i < methodList.size(); i++) {
			String methodName = methodList.get(i);
			
			//Set up a BytecodeCounter instance to use ByCounter, using a parameterless constructor. 
			
			counter.getInstrumentationParams().setUseResultCollector(false);
			counter.getInstrumentationParams().enableResultLogWriter("myResults" + File.separatorChar + "ByCounter_result_");
			counter.getInstrumentationParams().setUseArrayParameterRecording(true);
			counter.getInstrumentationParams().setWriteClassesToDisk(true);
			counter.getInstrumentationParams().setUseBasicBlocks(false);
			
			MethodDescriptor myMethod = new MethodDescriptor(
					className,
					methodName); //$NON-NLS-1$
			
			counter.addEntityToInstrument(myMethod);
			}
			
			counter.instrument();
		}
		return 0;
	}
	
public void invokeMethods(HashMap<String, ArrayList<String>> clist) throws IOException{
		
		ArrayList<String[]> args = new ArrayList<String[]>();
		
		Iterator it = clist.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	      
			String className = (String) pair.getKey();
			
			ArrayList<String> methodList = new ArrayList<String>();
			methodList = (ArrayList<String>) pair.getValue();
			
			for(int i = 0; i < methodList.size(); i++) {
			String methodName = methodList.get(i);
		
			//Set up a BytecodeCounter instance to use ByCounter, using a parameterless constructor. 
			BytecodeCounter counter = new BytecodeCounter();
		
			counter.getInstrumentationParams().setUseResultCollector(false);
			counter.getInstrumentationParams().setUseArrayParameterRecording(true);
			counter.getInstrumentationParams().setWriteClassesToDisk(true);
			counter.getInstrumentationParams().setUseBasicBlocks(false);
		
			MethodDescriptor myMethod = new MethodDescriptor(
				className,
				methodName); //$NON-NLS-1$
		
		
			if(methodName.contains("void main(")){
				counter.execute(myMethod, new Object[] {args.get(i)});
			}
			}
		}
	}
	
	public static void main(String[] args) throws IOException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException, ClassNotFoundException {
		InputParser ps = new InputParser();
		HashMap<String, ArrayList<String>> instrumentationList = ps.Parser(args[0]);
		CalculateCostModel cp = new CalculateCostModel();
		//cp.invokeMethods(instrumentationList);
		try {
			cp.cost(instrumentationList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
