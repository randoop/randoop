package randoop.experimental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import randoop.Check;
import randoop.ExecutableSequence;
import randoop.MSequence;
import randoop.MStatement;
import randoop.MVariable;
import randoop.ObjectCheck;
import randoop.Sequence;
import randoop.Variable;

public class SequenceSimplifyUtils {

	/**
	 * Finds out all un-used variables' indices.
	 * */
	public static Integer[] getAllUnusedVariableIndex(Sequence sequence) {
		List<Integer> unusedVars = getAllUnusedVariableIndexInList(sequence);
		//return all unused var index
		return unusedVars.toArray(new Integer[0]);
	}
	
	static List<Integer> getAllUnusedVariableIndexInList(Sequence sequence) {
		assert sequence != null : "The sequence can not be null.";
		//find out all used var indices
		int size = sequence.size();
		Set<Integer> usedVarIndices = new HashSet<Integer>();
		for(int i = 0; i < size; i++) {
			List<Variable> usedvars = sequence.getInputs(i);
			for(Variable usedvar : usedvars) {
				usedVarIndices.add(usedvar.index);
			}
		}
		//the remaining index are vars which are not used 
		List<Integer> unusedVars = new LinkedList<Integer>();
		for(int i = 0; i < size; i++) {
			if(!usedVarIndices.contains(i)) {
				unusedVars.add(i);
			}
		}
		return unusedVars;
	}
	
	/**
	 * This method works like the previous one, but also considers the variables
	 * used in assertions.
	 * */
	public static List<Integer> getAllUnusedVariableIndexAsList(ExecutableSequence eseq) {
        List<Integer> usedVarIndexList = getAllUnusedVariableIndexInList(eseq.sequence);
		
		Set<Integer> usedVarIndexInAssertion = new HashSet<Integer>();
		int size = eseq.sequence.size();
		for(int i = 0; i < size; i++) {
			List<Check> checks = eseq.getChecks(i);
			for(Check check : checks) {
				if(check instanceof ObjectCheck) {
					ObjectCheck objContract = (ObjectCheck)check;
					Variable[] vars = objContract.vars;
					for(Variable var : vars) {
						usedVarIndexInAssertion.add(var.index);
					}
				}
			}
		}
		
		//remove those used var index in assertion
		usedVarIndexList.removeAll(usedVarIndexInAssertion);
		return usedVarIndexList;
	}
	
	public static Integer[] getAllUnusedVariableIndex(ExecutableSequence eseq) {
		List<Integer> usedVarIndexList = getAllUnusedVariableIndexAsList(eseq);
		return usedVarIndexList.toArray(new Integer[0]);
	}
	
	/**
	 * Checks if the index-th statement can be removed safely without
	 * breaking the whole program.
	 * */
	public static boolean isStatementRemovable(Sequence original_sequence, int index) {
		int size = original_sequence.size();
		assert index >= 0 && index < size :
			"The index: " + index + " is not valid. given the sequence length: " + size;
		for(int i = index + 1; i < size; i++) {
			List<Variable> inputs = original_sequence.getInputs(i);
			for(Variable input : inputs) {
				if(input.index == index) {
					//used by other statement as inputs
					return false;
				}
			}
		}
		return true;
	}
	
	public static int computeIndexInOriginalSequence(Sequence sequenceToSimplify, Sequence simplifiedSequence,
	    List<Integer> removed_indices, Integer indexInSimplifiedSequence) {
		assert indexInSimplifiedSequence < simplifiedSequence.size() : "The given index: " + indexInSimplifiedSequence
				+ ", is not valid, the total length of simplified: " + simplifiedSequence.size();
		assert simplifiedSequence.size() + removed_indices.size() == sequenceToSimplify.size() :
				"Error in size, simpilified sequence size: " + simplifiedSequence.size() + ", removed index size: " + removed_indices.size()
				+ ", original sequence size: " + sequenceToSimplify.size();
		//sort it
		Collections.sort(removed_indices);
		//traverse the original un-simplified sequence
		int countInSimplified = -1;
		for(int i = 0; i < sequenceToSimplify.size(); i++) {
			if(removed_indices.contains(i)) {
				continue;
			} else {
				countInSimplified++;
				if(countInSimplified == indexInSimplifiedSequence) {
					return i;
				}
			}
		}
		System.out.println("total length of original: " + sequenceToSimplify.size());
		System.out.println("removed_indices: " + removed_indices);
		System.out.println("Count in simplified: " + countInSimplified);
		System.out.println("indexInSimplifiedSequence: " + indexInSimplifiedSequence);
		throw new Error("Should not be here."); 
	}
	
	/**
	 * retains the statements.
	 * XXX NOTE,this may return null, if the given index list is not valid to construct a  sequence
	 * */
	public static Sequence retainStatements(Sequence original_sequence, List<Integer> retained) {
		for(int index : retained) {
			assert index > -1 && index < original_sequence.size() :
					"The index is not valid: " + index
					+ " for a sequence of length: " + original_sequence.size();
		}
		//new size, and sort the retained statement indices
		//int new_size = retained.size();
		Collections.sort(retained);
		//the original sequence
		MSequence msequence = original_sequence.toModifiableSequence();
		//create a simplified sequence
		MSequence simplifiedSequence = new MSequence();
		List<MVariable> newvars = new LinkedList<MVariable>();
		for(int i = 0; i < retained.size(); i++) {
			String name = original_sequence.getVariable(retained.get(i) /*index in the original sequence*/).getName();
			newvars.add(new MVariable(simplifiedSequence, name));
		}
		//creates a bunch of statements
		List<MStatement> statements = new ArrayList<MStatement>();
		for(int i = 0; i < retained.size(); i++) {
			MStatement mstatement = msequence.statements.get(retained.get(i));
			List<MVariable> newinputs = new ArrayList<MVariable>();
			for(MVariable v : mstatement.inputs) {
				int indexInOriginalStatement = v.getDeclIndex();
				if(!retained.contains(indexInOriginalStatement)) {
					//you remove some statements that you should not
					return null;
				}
				//get the variable
				int indexOfRetained = retained.indexOf(indexInOriginalStatement);
				MVariable mv = newvars.get(indexOfRetained);
			    newinputs.add(mv);
			}
			statements.add(new MStatement(mstatement.statementKind, newinputs, newvars.get(i)));
		}
		
		simplifiedSequence.statements = statements;
		simplifiedSequence.checkRep();
		
		System.out.println("Construct a valid sequence!");
		return simplifiedSequence.toImmutableSequence();
	}
	
	/**
	 * Removes the given statement
	 * */
	public static Sequence removeStatement(Sequence original_sequence, int index) {
		assert index > -1 && index < original_sequence.size() :
				"The index is not valid: " + index
				+ " for a sequence of length: " + original_sequence.size();
		MSequence msequence = original_sequence.toModifiableSequence();
		
		//no that you need to
		int new_size = original_sequence.size() - 1;
		MSequence new_sequence = new MSequence();
		List<MVariable> newvars = new LinkedList<MVariable>();
		for(int i = 0; i < new_size; i++) {
			String name = i < index ? original_sequence.getVariable(i).getName()
					: original_sequence.getVariable(i + 1).getName();
			newvars.add(new MVariable(new_sequence, name));
		}
		
		List<MStatement> statements = new ArrayList<MStatement>();
		for(int i = 0; i < new_size; i++) {
			MStatement mstatement = i < index ? msequence.statements.get(i)
					: msequence.statements.get(i + 1);
			List<MVariable> newinputs = new ArrayList<MVariable>();
			for(MVariable v : mstatement.inputs) {
				int vindex = v.getDeclIndex();
				if(vindex >= index) {
					vindex = vindex - 1;
				}
				newinputs.add(newvars.get(vindex));
			}
			statements.add(new MStatement(mstatement.statementKind, newinputs, newvars.get(i)));
		}
		
		new_sequence.statements = statements;
		new_sequence.checkRep();
		
		return new_sequence.toImmutableSequence();
	}
	
	public static Sequence makeCopy(Sequence seq) {
		Sequence sequence = new Sequence();
		for(int i = 0; i < seq.size(); i++) {
			List<Variable> inputs = new ArrayList<Variable>();
			for(Variable v : seq.getInputs(i)) {
				int varIndex = v.getDeclIndex();
				inputs.add(sequence.getVariable(varIndex));
			}
			sequence = sequence.extend(seq.getStatementKind(i), inputs);
		}
		return sequence;
	}
}
