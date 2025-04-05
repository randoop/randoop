package collections;

import java.util.*;

/**
 * This class holds a list of seed integers contained in {@link randoop.generation.SeedSequences}
 * for the purpose of verifying the functionality of GRT Impurity.
 *
 * <p>When selecting primitives and String inputs for a method under test, Randoop uses a set of
 * seed values to generate sequences. Normally, Randoop would use these seed values to test this
 * class (including running containsSeed), where method handleSeedNotFound would not be called.
 * However, if GRT Impurity is enabled, Randoop will fuzz the inputs to the method under test. This
 * will cause the value to be different from the seed values. In this case, Randoop will call {@link
 * handleSeedNotFound}. By checking whether {@link handleSeedNotFound} is covered, we can verify
 * that GRT Impurity is working correctly.
 *
 * <p>Note: It is possible (very unlikely) that all fuzzed inputs are contained in the seed
 * collection if determinism is disabled. This happens when all inputs that Randoop selected are
 * either unfuzzable (e.g. (int) char) or on an extremely rare occasion, the fuzzed inputs are the
 * same as the seed values. In this case, the method handleSeedNotFound will not be covered and the
 * test will fail, resulting in a false negative. This is a limitation of the test.
 *
 * <p>By default, tests for this class will be run deterministically with seed=0, so this should not
 * be an issue.
 */
public class SeedIntegerCollection {
  private final List<Integer> seedList =
      Arrays.asList(-1, 0, 1, 10, 100, (int) '#', (int) ' ', (int) 'a', (int) '4');

  public SeedIntegerCollection() {}

  // Returns the seed value or Integer.MIN_VALUE if not found
  public int containsSeed(int e) {
    if (seedList.contains(e)) {
      return e;
    }
    handleSeedNotFound();
    return Integer.MIN_VALUE;
  }

  // Does nothing, only used to test coverage
  private void handleSeedNotFound() {}
}
