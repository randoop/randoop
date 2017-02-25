package randoop.generation.exhaustive;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.math.BigIntegerMath;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SequenceGeneratorTest {
  @Rule public ExpectedException expectedForEmptySets = ExpectedException.none();
  private Set<String> emptySet = Sets.newHashSet();
  private Set<String> singletonSet = Sets.newHashSet("A");
  private Set<String> largerSet = Sets.newHashSet("A", "B", "C", "D");

  @Test
  public void hasNextForEmptySet() throws Exception {

    // Checks if an empty set produces just one sequence (an empty sequence).
    expectedForEmptySets.expect(IllegalArgumentException.class);
    SequenceGenerator<String> sgen = new SequenceGenerator<>(emptySet);
  }

  @Test
  public void hasNextForSingletonSet() throws Exception {

    // Checks if a singleton set produces just two sequences (empty and singleton).
    SequenceGenerator<String> sgen = new SequenceGenerator<>(singletonSet);

    Assert.assertTrue(sgen.hasNext());
    sgen.next();
    Assert.assertFalse(sgen.hasNext());
  }

  private static Set<List<String>> getAllExpectedSequencesForLargerSet() {
    Set<List<String>> expected = Sets.newHashSet();
    expected.add(Lists.newArrayList("A", "B", "C", "D"));
    expected.add(Lists.newArrayList("A", "B", "D", "C"));
    expected.add(Lists.newArrayList("A", "C", "B", "D"));
    expected.add(Lists.newArrayList("A", "C", "D", "B"));
    expected.add(Lists.newArrayList("A", "D", "B", "C"));
    expected.add(Lists.newArrayList("A", "D", "C", "B"));
    expected.add(Lists.newArrayList("B", "A", "C", "D"));
    expected.add(Lists.newArrayList("B", "A", "D", "C"));
    expected.add(Lists.newArrayList("B", "C", "A", "D"));
    expected.add(Lists.newArrayList("B", "C", "D", "A"));
    expected.add(Lists.newArrayList("B", "D", "A", "C"));
    expected.add(Lists.newArrayList("B", "D", "C", "A"));
    expected.add(Lists.newArrayList("C", "A", "B", "D"));
    expected.add(Lists.newArrayList("C", "A", "D", "B"));
    expected.add(Lists.newArrayList("C", "B", "A", "D"));
    expected.add(Lists.newArrayList("C", "B", "D", "A"));
    expected.add(Lists.newArrayList("C", "D", "A", "B"));
    expected.add(Lists.newArrayList("C", "D", "B", "A"));
    expected.add(Lists.newArrayList("D", "A", "B", "C"));
    expected.add(Lists.newArrayList("D", "A", "C", "B"));
    expected.add(Lists.newArrayList("D", "B", "A", "C"));
    expected.add(Lists.newArrayList("D", "B", "C", "A"));
    expected.add(Lists.newArrayList("D", "C", "A", "B"));
    expected.add(Lists.newArrayList("D", "C", "B", "A"));
    expected.add(Lists.newArrayList("A", "B", "C"));
    expected.add(Lists.newArrayList("A", "B", "D"));
    expected.add(Lists.newArrayList("A", "C", "B"));
    expected.add(Lists.newArrayList("A", "C", "D"));
    expected.add(Lists.newArrayList("A", "D", "B"));
    expected.add(Lists.newArrayList("A", "D", "C"));
    expected.add(Lists.newArrayList("B", "A", "C"));
    expected.add(Lists.newArrayList("B", "A", "D"));
    expected.add(Lists.newArrayList("B", "C", "A"));
    expected.add(Lists.newArrayList("B", "C", "D"));
    expected.add(Lists.newArrayList("B", "D", "A"));
    expected.add(Lists.newArrayList("B", "D", "C"));
    expected.add(Lists.newArrayList("C", "A", "B"));
    expected.add(Lists.newArrayList("C", "A", "D"));
    expected.add(Lists.newArrayList("C", "B", "A"));
    expected.add(Lists.newArrayList("C", "B", "D"));
    expected.add(Lists.newArrayList("C", "D", "A"));
    expected.add(Lists.newArrayList("C", "D", "B"));
    expected.add(Lists.newArrayList("D", "A", "B"));
    expected.add(Lists.newArrayList("D", "A", "C"));
    expected.add(Lists.newArrayList("D", "B", "A"));
    expected.add(Lists.newArrayList("D", "B", "C"));
    expected.add(Lists.newArrayList("D", "C", "A"));
    expected.add(Lists.newArrayList("D", "C", "B"));
    expected.add(Lists.newArrayList("A", "B"));
    expected.add(Lists.newArrayList("A", "C"));
    expected.add(Lists.newArrayList("A", "D"));
    expected.add(Lists.newArrayList("B", "A"));
    expected.add(Lists.newArrayList("B", "C"));
    expected.add(Lists.newArrayList("B", "D"));
    expected.add(Lists.newArrayList("C", "A"));
    expected.add(Lists.newArrayList("C", "B"));
    expected.add(Lists.newArrayList("C", "D"));
    expected.add(Lists.newArrayList("D", "A"));
    expected.add(Lists.newArrayList("D", "B"));
    expected.add(Lists.newArrayList("D", "C"));
    expected.add(Lists.newArrayList("A"));
    expected.add(Lists.newArrayList("B"));
    expected.add(Lists.newArrayList("C"));
    expected.add(Lists.newArrayList("D"));

    return expected;
  }

  private static int getNumberOfArrangements(int n, int choose) {
    int nFactorial = BigIntegerMath.factorial(n).intValue();
    int nMinusMFactorial = BigIntegerMath.factorial(n - choose).intValue();

    int numberOfArrangements = nFactorial / nMinusMFactorial;
    return numberOfArrangements;
  }

  private static int getNumberOfSequences(int maximumLength, int numberOfElements) {
    int total = 0;

    // The expected number of sequences is equal to the sum of A(n,1) + A(n,2) + ... + A(n,n)
    for (int i = 1; i <= maximumLength; i++) {
      total += getNumberOfArrangements(numberOfElements, i);
    }

    return total;
  }

  @Test
  public void hasNextForLargerSet() throws Exception {

    SequenceGenerator<String> sgen = new SequenceGenerator<>(largerSet);
    int n = largerSet.size();
    int expectedNumberOfSequences = getNumberOfSequences(n, n);

    Set<List<String>> generatedSequences = Sets.newHashSet();

    while (sgen.hasNext()) {
      generatedSequences.add(sgen.next());
    }

    assertEquals(generatedSequences.size(), expectedNumberOfSequences);
  }

  @Test
  public void getTotalSequencesIterated() throws Exception {
    SequenceGenerator<String> sgenLarger = new SequenceGenerator<>(largerSet);

    long beforeIterateLarger = sgenLarger.getTotalSequencesIterated().longValue();
    int numberOfSequencesToGenerate = 13;

    for (int i = 0; i < numberOfSequencesToGenerate; i++) {
      sgenLarger.next();
    }

    assertEquals(0, beforeIterateLarger);
    assertEquals(numberOfSequencesToGenerate, sgenLarger.getTotalSequencesIterated());
  }

  @Test
  public void generationResumptionOfSameSize() throws Exception {
    SequenceGenerator<String> first = new SequenceGenerator<>(largerSet);
    Set<List<String>> firstSequences = Sets.newHashSet();

    int initialSize = largerSet.size() - 1;
    List<int[]> indices = new LinkedList<>();
    for (int i = 0; i < initialSize; i++) {
      firstSequences.add(first.next());
      indices.add(first.getCurrentIndex().getCurrentPermutationIndices());
    }

    SequenceGenerator<String> last = new SequenceGenerator<>(largerSet, first.getCurrentIndex());
    Set<List<String>> remainingSequences = Sets.newHashSet();

    while (last.hasNext()) {
      remainingSequences.add(last.next());
    }

    int expectedNumberOfSequences = getNumberOfSequences(largerSet.size(), largerSet.size());

    Set<List<String>> actualSequences = Sets.union(firstSequences, remainingSequences);
    Set<List<String>> expectedSequences = getAllExpectedSequencesForLargerSet();

    assertEquals(initialSize, firstSequences.size());
    assertEquals(expectedNumberOfSequences - initialSize, remainingSequences.size());
    assertEquals(expectedNumberOfSequences, actualSequences.size());
    assertEquals(expectedSequences, actualSequences);
  }

  @Test
  public void generationResumptionChangingSizeWhenThereAreMoreToGenerate() throws Exception {
    SequenceGenerator<String> first = new SequenceGenerator<>(largerSet, 3);
    Set<List<String>> firstSequences = Sets.newHashSet();

    while (first.hasNext()) {
      firstSequences.add(first.next());
    }

    SequenceGenerator.SequenceIndex currIndex = first.getCurrentIndex();

    SequenceGenerator<String> last = new SequenceGenerator<>(largerSet, 4, currIndex);
    Set<List<String>> remainingSequences = Sets.newHashSet();

    while (last.hasNext()) {
      remainingSequences.add(last.next());
    }

    int expectedNumberOfSequences = getNumberOfSequences(largerSet.size(), largerSet.size());

    Set<List<String>> actualSequences = Sets.union(firstSequences, remainingSequences);
    Set<List<String>> expectedSequences = getAllExpectedSequencesForLargerSet();

    assertEquals(getNumberOfSequences(3, largerSet.size()), firstSequences.size());
    assertEquals(BigIntegerMath.factorial(4).intValue(), remainingSequences.size());
    assertEquals(expectedNumberOfSequences, actualSequences.size());
    assertEquals(expectedSequences, actualSequences);
  }

  @Test
  public void generationResumptionChangingSizeWhenThereAreNotAnymoreLeftToGenerate()
      throws Exception {
    SequenceGenerator<String> first = new SequenceGenerator<>(largerSet, 3);
    Set<List<String>> firstSequences = Sets.newHashSet();

    while (first.hasNext()) {
      firstSequences.add(first.next());
    }

    SequenceGenerator.SequenceIndex currIndex = first.getCurrentIndex();

    SequenceGenerator<String> last = new SequenceGenerator<>(largerSet, 3, currIndex);
    Set<List<String>> remainingSequences = Sets.newHashSet();

    while (last.hasNext()) {
      remainingSequences.add(last.next());
    }

    assertTrue(remainingSequences.isEmpty());
  }
}
