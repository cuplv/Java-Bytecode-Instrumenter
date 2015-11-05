package de.uka.ipd.sdq.ByCounter.parsing;

import java.util.Iterator;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import de.uka.ipd.sdq.ByCounter.instrumentation.IInstructionAnalyser;

/**
 * This {@link IInstructionAnalyser} adds new labels directly after method 
 * invocations so that i.e. pop instructions are not in the same label block.
 * For analysers to pick up on the changes made by this class, it needs to be
 * in a seperate pass before those analysers.
 * @author Martin Krogmann
 *
 */
public class LabelAfterInvokeAnalyser implements IInstructionAnalyser {

	@Override
	public void analyseInstruction(AbstractInsnNode insn) {
		// do nothing
	}

	@Override
	public void analyseTryCatchBlock(TryCatchBlockNode tryCatchNode) {
		// do nothing
	}

	@Override
	public void postAnalysisEvent(InsnList instructions) {
		int lastLineBeforeInvoke = -1;
		// go through all instructions
		for (	
				@SuppressWarnings("unchecked")
				Iterator<AbstractInsnNode> iterator = instructions.iterator(); 
				iterator.hasNext();
			) {
			AbstractInsnNode insn = iterator.next();
			if(insn instanceof LineNumberNode) {
				lastLineBeforeInvoke = ((LineNumberNode) insn).line;
			}
			
			// look for labels that are marked to start an instruction block
			if(insn instanceof MethodInsnNode) {
				// only add a label if there isn't one already
				if(!(insn.getNext() instanceof LabelNode)) {
					LabelNode newLabel = new LabelNode();
					instructions.insert(insn, newLabel);
					if(lastLineBeforeInvoke >= 0) {
						// try to specify a line number node as well, so that the following analysers have more information
						LineNumberNode newLineNumberNode = new LineNumberNode(lastLineBeforeInvoke, newLabel);
						instructions.insert(newLabel, newLineNumberNode);
					}
				}
			}
		}
	}

}
