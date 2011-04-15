package randoop.experimental;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import randoop.Check;
import randoop.ExecutableSequence;
import randoop.MultiVisitor;
import randoop.Sequence;

/**
 * An abstract class encapsulating common fields and utility functions in
 * simplifying the sequence.
 * */
public abstract class AbstractSimplifier {
	/**
	 * The sequence to simplify
	 * */
    public final Sequence sequenceToSimplify;
	/**
	 * The executable sequence keeps the runtime information of <code>sequenceToSimplify</code>
	 * */
	public final ExecutableSequence eSeq;
	/**
	 * The runtime execution visitor to reproduce the failure
	 * */
	public final MultiVisitor visitor;
	/**
	 * A list of removed indices in the <code>sequenceToSimplify</code> after performing
	 * the simplification task
	 * */
	public final List<Integer> removed_indices;
	
	public AbstractSimplifier(Sequence sequence, MultiVisitor visitor) {
		assert sequence != null : "The input sequence should not be null.";
		assert visitor != null : "The visitor can not be null.";
		this.sequenceToSimplify = sequence;
		this.visitor = visitor;
		this.eSeq = this.execute_sequence(this.sequenceToSimplify, this.visitor);
		//the sequence should not have non-executed sequence
		assert this.eSeq.hasFailure() : "The input sequence should fail!";
		assert !this.eSeq.hasNonExecutedStatements() :
				"The sequence should not have non-executed statement.";
		this.removed_indices = new LinkedList<Integer>();
	}
	
	abstract public ExecutableSequence simplfy_sequence();
	
	/**
	 * Returns a list of removed indices from the original sequence after simplification
	 * */
	protected List<Integer> getRemovedIndices() {
		return this.removed_indices;
	}
	
	/**
	 * Executes a sequence with the given visitor, returns an executable sequence with runtime information
	 * */
	protected ExecutableSequence execute_sequence(Sequence sequenceToSimplify, MultiVisitor visitor) {
		ExecutableSequence eseq = new ExecutableSequence(sequenceToSimplify);
		eseq.execute(visitor);
		return eseq;
	}
	
	/**
	 * Returns a list of indices in the original sequence from a list of indices in the
	 * simplified sequence
	 * */
	protected List<Integer> compute_indices_in_original_sequence(Sequence simplifiedSequence,
			List<Integer> removed_indices, List<Integer> indices_in_simplified) {
		List<Integer> indices_in_original_sequence = new LinkedList<Integer>();
		for(Integer index_in_simplified : indices_in_simplified) {
			Integer original_index = this.compute_index_in_original_sequence(simplifiedSequence,
					removed_indices, index_in_simplified);
			indices_in_original_sequence.add(original_index);
		}
		return indices_in_original_sequence;
	}
	
	/**
	 * Returns the index (of original sequence) in the simplified sequence
	 * For example, the original sequence is: [0, 1, 2, 3, 4, 5], and the removed indices are: [2, 5]
	 * So that, the simplified sequence has a length of 4. The index "3" in the simplified
	 * sequence is actually 4 in the original sequence.
	 * */
	protected int compute_index_in_simplified_sequence(Sequence simplifiedSequence,
			List<Integer> removed_indices, Integer indexInOrigSequence) {
		assert indexInOrigSequence >= 0 && indexInOrigSequence < this.eSeq.sequence.size() :
				"The index in orig sequence: " + indexInOrigSequence + " is not legal!";
		assert removed_indices.size() + simplifiedSequence.size() == this.eSeq.sequence.size() :
				"The sequence size is not correct, removed index num: " + removed_indices.size()
				+ ", simplifiedSequence size: " + simplifiedSequence.size() + ", orig sequence size: "
				+  this.eSeq.sequence.size();
		assert !removed_indices.contains(indexInOrigSequence) : "The removed_indices can not contains queried" +
				" index: " + indexInOrigSequence;
		//the index for return in simplified sequence
		int index_in_simplified = -1;
		for(int i = 0; i < this.eSeq.sequence.size(); i++) {
			if(removed_indices.contains(i)) {
				continue;
			}
			index_in_simplified++;
			if( i == indexInOrigSequence) {
				break;
			}
		}
		//check the correctness of result
		assert index_in_simplified >= 0 && index_in_simplified < simplifiedSequence.size() :
				"The index is illegal: " + index_in_simplified + ", the size of sequence: " + simplifiedSequence.size()
				+ ", indexInOriginalSequence: " + indexInOrigSequence + ", the removed index: " + removed_indices;
		return index_in_simplified;
	}
	
	/**
	 * Returns the index in the original sequence before simplification.
	 * For example, the original sequence is [0, 1, 2, 3, 4, 5]. The removed_indices is [2, 4]
	 * So, that an index 2 in the simplified sequence, is actually 3 in the original sequence.
	 * Invariants:  the length of simplified sequence + the length of removed_indices == the length of original sequence
	 * */
	protected int compute_index_in_original_sequence(Sequence simplifiedSequence,
	    List<Integer> removed_indices, Integer indexInSimplifiedSequence) {
		assert indexInSimplifiedSequence < simplifiedSequence.size() : "The given index: " + indexInSimplifiedSequence
				+ ", is not valid, the total length of simplified: " + simplifiedSequence.size();
		assert simplifiedSequence.size() + removed_indices.size() == this.sequenceToSimplify.size() :
				"Error in size, simpilified sequence size: " + simplifiedSequence.size() + ", removed index size: " + removed_indices.size()
				+ ", original sequence size: " + this.sequenceToSimplify.size();
		//sort it
		Collections.sort(removed_indices);
		//traverse the original un-simplified sequence
		int countInSimplified = -1;
		for(int i = 0; i < this.sequenceToSimplify.size(); i++) {
			if(removed_indices.contains(i)) {
				continue;
			} else {
				countInSimplified++;
				if(countInSimplified == indexInSimplifiedSequence) {
					return i;
				}
			}
		}
		System.out.println("total length of original: " + this.sequenceToSimplify.size());
		System.out.println("removed_indices: " + removed_indices);
		System.out.println("Count in simplified: " + countInSimplified);
		System.out.println("indexInSimplifiedSequence: " + indexInSimplifiedSequence);
		throw new Error("The execution should never be here. A bug in code, please report!"); 
	}
	
	/**
	 * Returns the failure index of the sequence before simplification
	 * */
	protected int getFailureIndex() {
		return eSeq.getFailureIndex();
	}
	
	/**
	 * Returns the failure checks of the sequence before simplification
	 * */
	protected List<Check> getFailureChecks() {
		List<Check> checklist = new LinkedList<Check>();
		int failure_index = this.getFailureIndex();
		if(failure_index != -1) {
			checklist.addAll(this.eSeq.getFailures(failure_index));
		}
		return checklist;
	}
	
	/**
	 * Compares if the same failures occur
	 * */
	protected boolean compareFailureChecks(List<Check> failure_in_original, List<Check> failure_in_simplified) {
		return true;
	}
}
