package randoop.test;

import java.util.function.Predicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Value;

/** Returns true if the sequences returns a too-large value. */
public class ValueSizePredicate implements Predicate<ExecutableSequence> {

  /** Creates a ValueSizePredicate. */
  public ValueSizePredicate() {}

  @Override
  public boolean test(ExecutableSequence eseq) {
    return !Value.lastValueSizeOk(eseq);
  }
}
