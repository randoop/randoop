package randoop.generation.exhaustive;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by otmar on 08/02/2017.
 */
public class TestsGenerator {
  private Class<?> testedClass;
  private int maximumLength;
  private boolean finished;
  private SequenceGenerator<String> sequenceGenerator;
  private Set<String> testedMethods;
  private long numberOfAttempts;
  private Set<List<String>> subSequencesKnownToFail;

  public TestsGenerator(Class<?> targetClass, int maximumLength) {
    if (targetClass == null) {
      throw new IllegalArgumentException("targetClass");
    }

    if (maximumLength <= 1) {
      throw new IllegalArgumentException("Tests length must be greater than 1.");
    }

    this.testedClass = targetClass;
    this.maximumLength = maximumLength;
    this.finished = false;
    this.numberOfAttempts = 0;
    this.initializeSequenceGenerator();
  }

  private void initializeSequenceGenerator() {
    // Initialize the methods which will eventually appear in the generated tests:
    this.testedMethods =
        Arrays.stream(testedClass.getMethods()).map(m -> m.getName()).collect(Collectors.toSet());

    this.sequenceGenerator = new SequenceGenerator<>(testedMethods, maximumLength);
  }

  private boolean hasFinished() {
    finished = numberOfAttempts <= 100000 && sequenceGenerator.hasNext();
    return finished;
  }

  public void generate() {
    while (!hasFinished()) {
      this.testCurrentSequence();
      numberOfAttempts++;
    }
  }

  private void testCurrentSequence() {
    List<String> currentSequence = sequenceGenerator.next();

    if (subSequencesKnownToFail.contains(currentSequence)) {
      return;
    } else {

    }
  }
}
