package randoop.generation.exhaustive;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.math.BigIntegerMath;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class CombinationIteratorTest {
  @Rule public ExpectedException expectedForInvalidLists = ExpectedException.none();

  @Test
  public void constructorFailsEmptyList() throws Exception {
    List<String> emptyList = Lists.newArrayList();
    expectedForInvalidLists.expect(IllegalArgumentException.class);
    CombinationIterator<String> it = new CombinationIterator<>(emptyList, 1);
  }

  @Test
  public void constructorFailsChoosingNonPositive() throws Exception {
    expectedForInvalidLists.expect(IllegalArgumentException.class);
    CombinationIterator<String> it =
        new CombinationIterator<>(Lists.newArrayList("A", "B", "C"), 0);
  }

  @Test
  public void getCurrentIndicesWithChooseOne() throws Exception {
    CombinationIterator<String> it =
        new CombinationIterator<>(Lists.newArrayList("A", "B", "C"), 1);

    int[] initialIndices = it.getCurrentIndices();

    it.next();
    it.next();

    int[] finalIndices = it.getCurrentIndices();

    Assert.assertArrayEquals(new int[] {0}, initialIndices);
    Assert.assertArrayEquals(new int[] {2}, finalIndices);
  }

  @Test
  public void getCurrentIndicesWithChooseThree() throws Exception {
    CombinationIterator<String> it =
        new CombinationIterator<>(Lists.newArrayList("A", "B", "C", "D"), 3);

    int[] initialIndices = it.getCurrentIndices();

    it.next();
    it.next();
    it.next();

    int[] finalIndices = it.getCurrentIndices();

    Assert.assertArrayEquals(new int[] {0, 1, 2}, initialIndices);
    Assert.assertArrayEquals(new int[] {1, 2, 3}, finalIndices);
  }

  @Test
  public void hasNextForSingletonList() throws Exception {
    CombinationIterator<String> it = new CombinationIterator<>(Lists.newArrayList("A"), 1);

    boolean beforeFirstNext = it.hasNext();
    it.next();
    boolean afterFirstNext = it.hasNext();

    assertTrue(beforeFirstNext);
    assertFalse(afterFirstNext);
  }

  private int getNumberOfCombinations(int n, int choose) {
    int nFactorial = BigIntegerMath.factorial(n).intValue();

    // Considering m = choose
    int m = choose;
    int nMinusMFactorial = BigIntegerMath.factorial(n - m).intValue();
    int mFactorial = BigIntegerMath.factorial(m).intValue();

    int numberOfArrangements = nFactorial / (nMinusMFactorial * mFactorial);
    return numberOfArrangements;
  }

  @Test
  public void hasNextForLargerList() throws Exception {
    CombinationIterator<String> it =
        new CombinationIterator<>(Lists.newArrayList("A", "B", "C", "D", "E"), 2);

    int numberOfCombinations = getNumberOfCombinations(5, 2);

    for (int i = 0; i < numberOfCombinations; i++) {
      assertTrue(it.hasNext());
      it.next();
    }

    assertFalse(it.hasNext());
  }

  @Test
  public void nextForSingletonList() throws Exception {
    Set<String> singleElement = Sets.newHashSet("A");
    Set<Set<String>> expectedCombinations = Sets.newHashSet();
    expectedCombinations.add(singleElement);

    Set<Set<String>> actualCombinations = Sets.newHashSet();

    CombinationIterator<String> it = new CombinationIterator<>(Lists.newArrayList("A"), 1);

    while (it.hasNext()) {
      actualCombinations.add(it.next());
    }

    Assert.assertEquals(expectedCombinations, actualCombinations);
  }

  @Test
  public void nextForLargerListChoosingTwo() throws Exception {
    Set<String> largerList = Sets.newHashSet("A", "B", "C", "D", "E");
    Set<Set<String>> expectedCombinations = Sets.newHashSet();
    expectedCombinations.add(Sets.newHashSet("B", "A"));
    expectedCombinations.add(Sets.newHashSet("C", "A"));
    expectedCombinations.add(Sets.newHashSet("D", "A"));
    expectedCombinations.add(Sets.newHashSet("E", "A"));
    expectedCombinations.add(Sets.newHashSet("C", "B"));
    expectedCombinations.add(Sets.newHashSet("D", "B"));
    expectedCombinations.add(Sets.newHashSet("E", "B"));
    expectedCombinations.add(Sets.newHashSet("D", "C"));
    expectedCombinations.add(Sets.newHashSet("E", "C"));
    expectedCombinations.add(Sets.newHashSet("D", "E"));

    Set<Set<String>> actualCombinations = Sets.newHashSet();

    CombinationIterator<String> it = new CombinationIterator<>(largerList, 2);

    while (it.hasNext()) {
      actualCombinations.add(it.next());
    }

    assertEquals(expectedCombinations, actualCombinations);
  }

  @Test
  public void nextForLargerListChoosingThree() throws Exception {
    Set<String> largerList = Sets.newHashSet("A", "B", "C", "D", "E");
    Set<Set<String>> expectedCombinations = Sets.newHashSet();
    expectedCombinations.add(Sets.newHashSet("B", "C", "A")); // AB
    expectedCombinations.add(Sets.newHashSet("B", "A", "D")); // AB
    expectedCombinations.add(Sets.newHashSet("A", "B", "E")); // AB
    expectedCombinations.add(Sets.newHashSet("C", "A", "E")); // AC
    expectedCombinations.add(Sets.newHashSet("A", "C", "D")); //AC
    expectedCombinations.add(Sets.newHashSet("A", "D", "E")); //AD
    expectedCombinations.add(Sets.newHashSet("A", "D", "B")); //AD
    expectedCombinations.add(Sets.newHashSet("B", "C", "D")); // BC
    expectedCombinations.add(Sets.newHashSet("B", "E", "C")); // BC
    expectedCombinations.add(Sets.newHashSet("C", "E", "D")); // CD
    expectedCombinations.add(Sets.newHashSet("B", "E", "D")); // DE

    Set<Set<String>> actualCombinations = Sets.newHashSet();

    CombinationIterator<String> it = new CombinationIterator<>(largerList, 3);

    while (it.hasNext()) {
      actualCombinations.add(it.next());
    }

    assertEquals(expectedCombinations, actualCombinations);
  }

  @Test
  public void testResumeCombinationGenerationViaConstructorWithCurrentIndices() throws Exception {
    Set<String> largerList = Sets.newHashSet("A", "B", "C", "D", "E");

    Set<Set<String>> expectedCombinations = Sets.newHashSet();
    expectedCombinations.add(Sets.newHashSet("B", "C", "A")); // AB
    expectedCombinations.add(Sets.newHashSet("B", "A", "D")); // AB
    expectedCombinations.add(Sets.newHashSet("A", "B", "E")); // AB
    expectedCombinations.add(Sets.newHashSet("C", "A", "E")); // AC
    expectedCombinations.add(Sets.newHashSet("A", "C", "D")); //AC
    expectedCombinations.add(Sets.newHashSet("A", "D", "E")); //AD
    expectedCombinations.add(Sets.newHashSet("A", "D", "B")); //AD
    expectedCombinations.add(Sets.newHashSet("B", "C", "D")); // BC
    expectedCombinations.add(Sets.newHashSet("B", "E", "C")); // BC
    expectedCombinations.add(Sets.newHashSet("B", "E", "D")); // BD
    expectedCombinations.add(Sets.newHashSet("C", "E", "D")); // CD

    Set<Set<String>> firstCombinations = Sets.newHashSet();

    CombinationIterator<String> firstIterator = new CombinationIterator<>(largerList, 3);

    int totalNumberOfCombinations = getNumberOfCombinations(largerList.size(), 3);

    for (int i = 0; i < totalNumberOfCombinations - 1; i++) {
      firstCombinations.add(firstIterator.next());
    }

    int[] indices = firstIterator.getCurrentIndices();

    CombinationIterator<String> secondIterator = new CombinationIterator<>(largerList, 3, indices);
    Set<Set<String>> remainingCombinations = Sets.newHashSet();

    while (secondIterator.hasNext()) {
      remainingCombinations.add(secondIterator.next());
    }

    Set<Set<String>> allCombinations = Sets.union(firstCombinations, remainingCombinations);
    assertEquals(totalNumberOfCombinations - 1, firstCombinations.size());
    assertEquals(1, remainingCombinations.size());
    assertEquals(totalNumberOfCombinations, allCombinations.size());
    assertEquals(expectedCombinations, allCombinations);
  }
}
