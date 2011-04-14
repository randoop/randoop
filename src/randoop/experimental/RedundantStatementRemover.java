package randoop.experimental;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import randoop.Check;
import randoop.ExecutableSequence;
import randoop.MultiVisitor;
import randoop.Sequence;

public class RedundantStatementRemover extends AbstractSimplifier {
	
	public RedundantStatementRemover(Sequence sequence, MultiVisitor visitor) {
		super(sequence, visitor);
	}
	
	/**
	 * Given a sequence, this method repeatedly tries to remove a redundant statement,
	 * executes the sequence to see if the same failure occurs. If so, keep simplifying
	 * the sequence. Else, add the index to the unremovable index list.
	 * 
	 * <b>Note: </b> we compares the exception throwing index in the simplify sequence
	 * to decide whether the same exception has been thrown. This is unsound. Ideally,
	 * we should also compare is the same exception been thrown. But we found that is
	 * sufficient in practice.
	 * */
	@Override
	public ExecutableSequence simplfy_sequence() {
		//first get the original failure information
		int original_failed_index = this.getFailureIndex();
		List<Check> original_failed_checks = this.getFailureChecks();
		
		ExecutableSequence sequence_for_simplify = this.eSeq;
		ExecutableSequence simplified_sequence = null;
		//keep track of unremovable_indices and already removed indices in the ORIGINAL sequence
		Set<Integer> unremovable_indices = new HashSet<Integer>();
		List<Integer> removed_indices = new LinkedList<Integer>();
		while(true) {
			//the index positions are different!
			//already in order, unused_indices are in the simplified sequence, NOT original statement
			List<Integer> unused_indices = SequenceSimplifyUtils.getAllUnusedVariableIndexAsList(sequence_for_simplify);
			List<Integer> unused_indices_in_orig = this.compute_indices_in_original_sequence(sequence_for_simplify.sequence,
					removed_indices, unused_indices);
			if(unused_indices_in_orig.isEmpty() || unremovable_indices.containsAll(unused_indices_in_orig)) {
				simplified_sequence = sequence_for_simplify;
				break;
			}
			//manipulate the simplified sequence
			for(int i = unused_indices.size() - 1; i >= 0; i--) {
				Integer stmtToRemvoe = unused_indices.get(i);
				//remove this index
				Sequence s = SequenceSimplifyUtils.removeStatement(sequence_for_simplify.sequence, stmtToRemvoe);
				ExecutableSequence es = new ExecutableSequence(s);
				es.execute(this.visitor);
				if(es.hasFailure()) {
					//which index (in original sequence) has been removed
					int removed_index_in_orig = this.compute_index_in_original_sequence(sequence_for_simplify.sequence,
							removed_indices, stmtToRemvoe);
					//get the failure index, note that need to use a tmp_removed list,
					//suppose that the index is removed correctly
					int failure_index = es.getFailureIndex();
					List<Check> failures_in_simplified = es.getFailures(failure_index);
					List<Integer> tmp_removed = new LinkedList<Integer>(removed_indices);
					tmp_removed.add(removed_index_in_orig);
					int failure_index_in_orig = this.compute_index_in_original_sequence(es.sequence, tmp_removed, failure_index);
					tmp_removed.clear();
					//check if the same exception has been thrown
					if(failure_index_in_orig == original_failed_index
							&& super.compareFailureChecks(original_failed_checks, failures_in_simplified)) {
						sequence_for_simplify = es;
						removed_indices.add(removed_index_in_orig);
					} else {
						//add to the unremovable index list
						unremovable_indices.add(removed_index_in_orig);
					}
				} else {
					//the i-th index in the simplified statement is not removable
					int index_in_original = this.compute_index_in_original_sequence(sequence_for_simplify.sequence,
							removed_indices, stmtToRemvoe);
					unremovable_indices.add(index_in_original);
				}
			}
		}
		
		assert simplified_sequence != null : "The simplified sequence can not be null.";
		
		//keep the removed index
		super.removed_indices.addAll(removed_indices);
		
		return simplified_sequence;
	}
}