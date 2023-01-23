package randoop.test;

import java.util.Set;
import java.util.function.Predicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;

/** Forbids certain sequences. Returns true if the sequence is not in the exclusion set. */
public class ExcludeTestPredicate implements Predicate<ExecutableSequence> {

  /** The sequences to forbid. */
  private Set<Sequence> excludeSet;

  public ExcludeTestPredicate(Set<Sequence> excludeSet) {
    this.excludeSet = excludeSet;
  }

  @Override
  public boolean test(ExecutableSequence eseq) {
    return !excludeSet.contains(eseq.sequence);
  }
}
