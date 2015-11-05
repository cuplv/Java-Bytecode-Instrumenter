package edu.colorado.instrumentation.costgeneration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;

public class InputParser {
	public static HashMap<String, ArrayList<String>> Parser(String baseFilename) throws ClassHierarchyException, IOException, IllegalArgumentException, CallGraphBuilderCancelException
	{
		File exFile = new FileProvider().getFile("src/exclusions.txt");
		
		// Initialize Scope
		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(baseFilename, exFile);
		
		//make class hierarchy from scope
		ClassHierarchy cha = ClassHierarchy.make(scope);
		
		// Create a file to store a list of classes and methods
		File file = new File("instrumenterMethods.txt");
		FileWriter fw = new FileWriter(file.getName(),true);
		BufferedWriter bw = new BufferedWriter(fw);
		
		ArrayList<String> mlist = new ArrayList<String>();
		HashMap<String, ArrayList<String>> instrumenterMap = new HashMap<String, ArrayList<String>>();
		
		// if file doesn't exist, then create it
		if (!file.exists()) {
				file.createNewFile();
			}
		
		
		// Get class information from android jar
		for(IClass c : cha)
		{
			if (!scope.isApplicationLoader(c.getClassLoader())) continue; 
			
			ArrayList<String> methodList = new ArrayList<String>();
			String cname = c.getName().toString();
			
			/*if(cname.startsWith("Ljava") || cname.startsWith("Lcom/ibm/") || cname.startsWith("Lcom/oracle") || cname.startsWith("Lsunw") || cname.startsWith("LWrapper")
					|| cname.startsWith("Lorg") || cname.startsWith("Lcom/") || cname.startsWith("Lnet") || cname.startsWith("Lantlr") || cname.startsWith("Lcck") 
					|| cname.startsWith("Ljintgen") || cname.startsWith("LSunflowGUI"))
			   continue;*/
			
			cname = handleType(cname);
			instrumenterMap.put(cname, methodList);
			bw.write("Class:");
			bw.write(cname);
			bw.write("\n");
			
			// Get all methods for a class
			for (IMethod m : c.getDeclaredMethods())
			{
				//if(m.isPublic())
				//{
				String mname = m.getName().toString();
				String paramType = new String();
				String paramtemp = new String();
				String retType;
				boolean inheritanceFlag = false;
				if(!((mlist.contains(mname)) || mname.toString().startsWith("java.lang") || mname.toString().contains("<init>") || mname.toString().contains("<clinit>")))
				{
					// Handle method argument reconstruction
					for(int i = 0; i < m.getNumberOfParameters(); i++) {
						paramtemp = m.getParameterType(i).getName().toString();
						bw.write("Paramtype: " + m.getName() + ":" + m.getNumberOfParameters() + "Type:" + m.getParameterType(i).getName().toString());
						bw.write("\n");
						if((handleType(paramtemp).equals(cname) && (i == 0)) && !(cname.startsWith("avrora.sim.state.Complex"))) {
							inheritanceFlag = true;
							continue;
						}
						if(paramtemp.startsWith("[[")){
								//bw.write("PARAMTEMP: " + paramtemp);
								paramtemp = paramtemp.replace("[[", "");
								paramtemp += "[][]";
						}
						else if(paramtemp.startsWith("[")){
							paramtemp = paramtemp.replace("[", "");
							paramtemp += "[]";
						}

						if ((i > 0 && inheritanceFlag == false) || (cname.startsWith("Lavrora.sim.state.Complex"))) {
							paramType += ", " + handleType(paramtemp) + " x" + i ;
						}
						else {
							paramType += handleType(paramtemp) + " x" + i ;
							inheritanceFlag = false;
						}
						
						
					}
					
					// Handle return type
					paramtemp = m.getReturnType().getName().toString();
					if(paramtemp.startsWith("[[")){
						paramtemp = paramtemp.replace("[[", "");
						paramtemp += "[][]";
					} else if(paramtemp.startsWith("[")){
							paramtemp = paramtemp.replace("[", "");
							paramtemp += "[]";
					}
					
					if(m.getNumberOfParameters() == 0) {
						paramType = "";
					}
				
				retType = handleType(paramtemp);
				if(!(mname.toString().startsWith("<init>"))) {
				methodList.add(retType + " " + mname + "(" + paramType + ")");
				instrumenterMap.put(cname, methodList);
				}
				bw.write(" Method:");
				bw.write( retType + " " + mname + "(" + paramType + ")");
				bw.write("\n");
				}
				mlist.add(mname);
			}
			mlist.clear();
		}
		bw.close();
		return instrumenterMap;

	}
	
	public static String handleType(String varType){
		
		varType = varType.replace("/", ".");
		if(varType.startsWith("L")) {
			varType = varType.replaceFirst("L", "").trim();
		} else if(varType.startsWith("Z")) {
			varType = varType.replaceFirst("Z", "boolean");
		} else if(varType.startsWith("V")) {
			varType = varType.replaceFirst("V", "void");
		} else if(varType.startsWith("I")) {
			varType = varType.replaceFirst("I", "int");
		} else if(varType.startsWith("F")) {
			varType = varType.replaceFirst("F", "float");
		} else if(varType.startsWith("D")) {
			varType = varType.replaceFirst("D", "double"); 
		} else if(varType.startsWith("J")) {
			varType = varType.replaceFirst("J", "long");
		} else if(varType.startsWith("C")) {
			varType = varType.replaceFirst("C", "char");
		} else if(varType.startsWith("B")) {
			varType = varType.replaceFirst("B", "byte");
		} 
		return varType;
	}
}
