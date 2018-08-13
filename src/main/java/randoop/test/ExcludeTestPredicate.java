package randoop.test;

import java.util.Set;
import java.util.function.Predicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;

public class ExcludeTestPredicate implements Predicate<ExecutableSequence> {

  private Set<Sequence> excludeSet;

  public ExcludeTestPredicate(Set<Sequence> excludeSet) {
    this.excludeSet = excludeSet;
  }

  @Override
  public boolean test(ExecutableSequence eseq) {
    return !excludeSet.contains(eseq.sequence);
  }
}
