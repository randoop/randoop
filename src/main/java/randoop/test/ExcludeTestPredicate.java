package randoop.test;

import java.util.Set;

import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.util.predicate.DefaultPredicate;

public class ExcludeTestPredicate extends DefaultPredicate<ExecutableSequence> {

  private Set<Sequence> excludeSet;

  public ExcludeTestPredicate(Set<Sequence> excludeSet) {
    this.excludeSet = excludeSet;
  }

  @Override
  public boolean test(ExecutableSequence s) {
    return !excludeSet.contains(s.sequence);
  }
}
