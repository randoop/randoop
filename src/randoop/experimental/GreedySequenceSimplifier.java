package randoop.experimental;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import randoop.ExecutableSequence;
import randoop.MSequence;
import randoop.MultiVisitor;
import randoop.Sequence;
import randoop.Variable;
import randoop.util.Reflection;

public class GreedySequenceSimplifier extends AbstractSimplifier {
	
	public GreedySequenceSimplifier(Sequence sequence, MultiVisitor visitor) {
		super(sequence, visitor);
	}

	@Override
	/**
	 * A greedy algorithm to simplify the given sequence. This algorithm works from
	 * the last statement, iteratively change the input vars of each staement to a
	 * type-compatible output (of certain earlier statements). It uses a heuristic
	 * to find the earliest type-compatible variable output. After each replacement,
	 * the algorithm executes the simplified statement, to see if the same failure
	 * occurs. If so, it continues to remove redundant statements. If not, it rolls back
	 * to replace another input.
	 * */
	public ExecutableSequence simplfy_sequence() {
		int orig_size = this.eSeq.sequence.size();
		//the index list in original list that has already been removed
		List<Integer> removed_indice = new LinkedList<Integer>();
		//the original sequence value to simplify
		Sequence simplified_sequence = this.eSeq.sequence;
		
		for(int i = orig_size - 1; i > -1; i--) {
			if(removed_indice.contains(i)) {
				//the statement has already been removed
				continue;
			}
			int is = super.compute_index_in_simplified_sequence(simplified_sequence, removed_indice, i);
			assert is >= 0 && is < simplified_sequence.size() : "is is not legal: " + is + ", the simplified sequence length: "
					+ simplified_sequence.size();
			//get all possible inputs from the original sequence
			List<Variable> inputVars = simplified_sequence.getInputs(is);
			for(int input_index = 0;  input_index < inputVars.size(); input_index++) {
				Variable var = inputVars.get(input_index);
				int iInSimplified = super.compute_index_in_simplified_sequence(simplified_sequence, removed_indice, i);
				List<Integer> compatible_indices
				  = this.findCompatibleTypeIndicesInReverseOrder(simplified_sequence, iInSimplified, var.getType());
				if(compatible_indices.isEmpty()) {
					continue;
				} else {
					//1. replace var with variables produced by statement in compatible_indices,
					//and then get the smallest one
					int indexInSimplified = super.compute_index_in_simplified_sequence(simplified_sequence, removed_indice, i);
					Sequence s = this.replaceVarWithSmallestCompatibleIndex(compatible_indices, removed_indice, simplified_sequence,
							indexInSimplified, input_index);
					//2. remove all unused vars, then keep going
					if(s != null) {
					    simplified_sequence = s;
					    //then remove all unused vars
					    RedundantStatementRemover unused_simplifier = new RedundantStatementRemover(simplified_sequence, this.visitor);
					    Sequence tmp_sequence = unused_simplifier.simplfy_sequence().sequence;
					    List<Integer> remove_indices_in_orig = new LinkedList<Integer>();
					    for(int removed_index_in_simplified : unused_simplifier.getRemovedIndices()) {
					    	remove_indices_in_orig.add(super.compute_index_in_original_sequence(simplified_sequence, removed_indice,
					    			removed_index_in_simplified));
					    }
					    removed_indice.addAll(remove_indices_in_orig);
					    simplified_sequence = tmp_sequence;
					}
				}
			}
		}
		//add to the removed index list
		this.removed_indices.addAll(removed_indice);
		//executes the sequence and returns it
		ExecutableSequence ret_eseq = new ExecutableSequence(simplified_sequence);
		ret_eseq.execute(this.visitor);
		return ret_eseq;
	}
	
	/**
	 * Reorganize the simplifying sequence by use the output of the earliest
	 * statements in <code>replacable_var_index</code> to as the <code>input_index</code>-th
	 * input of the <code>stmt_num</code>-th statement.
	 * */
	protected Sequence replaceVarWithSmallestCompatibleIndex(List<Integer> replacable_var_index,
			List<Integer> removed_indices, Sequence sequence, int stmt_num, int input_index) {
		Collections.sort(replacable_var_index); //not necessary?
		for(int i : replacable_var_index) {
			Variable replace_var = sequence.getVariable(i);
			MSequence msequence = sequence.toModifiableSequence();
			msequence.getInputs(stmt_num).remove(input_index);
			msequence.getInputs(stmt_num).add(input_index, msequence.getVariable(replace_var.index));
			Sequence replaced_sequence = msequence.toImmutableSequence();
			ExecutableSequence eSeq = new ExecutableSequence(replaced_sequence);
			eSeq.execute(this.visitor);
			//check if we got the same failure
			if(!eSeq.hasFailure()) {
				continue;
			} else {
				int failure_index = eSeq.getFailureIndex();
				int failure_index_in_orig = super.compute_index_in_original_sequence(sequence, removed_indices, failure_index);
				if(failure_index_in_orig == super.getFailureIndex()
						&& super.compareFailureChecks(super.getFailureChecks(), eSeq.getFailures(failure_index))) {
					return replaced_sequence;
				}
			}
		}
		//nothing found
		return null;
	}
	
	/**Finding a list output-type-compatible statements in the sequence, between range [0, maxIndex)*/
	protected List<Integer> findCompatibleTypeIndicesInReverseOrder(Sequence sequence, int maxIndex, Class<?> type) {
		assert maxIndex >= 0 && maxIndex < sequence.size() : "The max index: "
				+ maxIndex + " is not correct for sequence size: " + sequence.size();
		List<Integer> compatible_indices = new LinkedList<Integer>();
		for(int i = 0 ; i < maxIndex; i++) {
			Class<?> outputType = sequence.getStatementKind(i).getOutputType();
			if(Reflection.canBeUsedAs(outputType, type)) {
				compatible_indices.add(i);
			}
		}
		return compatible_indices;
	}
}
