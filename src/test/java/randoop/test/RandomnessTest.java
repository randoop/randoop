package randoop.test;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;
import randoop.util.ArrayListSimpleList;
import randoop.util.Randomness;
import randoop.util.WeightedElement;

public class RandomnessTest extends TestCase {

  private static final double epsilon = 0.01;

  /**
   * Checks that randomMemberWeighted returns a random element that
   * is properly selected based on the weights of all the elements in
   * the given list. Creates a list of 10 elements with weights 1..10,
   * Selects an element from the list 100K times using randomMemberWeighted.
   * Finally, checks that each element was selected approximately as
   * many times as expected (the ith element should be selected
   * approximately i/sum(1..10) times.
   */
  public void testRandomMemberWeighted() {

    class WeightedElt implements WeightedElement {
      public final int weight;

      public WeightedElt(int weight) {
        this.weight = weight;
      }

      public double getWeight() {
        return this.weight;
      }
    }

    // Create a list of weighted elements.
    ArrayListSimpleList<WeightedElt> list = new ArrayListSimpleList<WeightedElt>();
    int sumOfAllWeights = 0;
    for (int i = 1; i < 10; i++) {
      int weight = i;
      list.add(new WeightedElt(weight));
      sumOfAllWeights += weight;
    }

    Map<Integer, Integer> weightToTimesSelected = new LinkedHashMap<Integer, Integer>();
    int totalSelections = 0;

    // Select lots of times.
    for (int i = 0; i < 100000; i++) {
      int weightSelected = Randomness.randomMemberWeighted(list).weight;
      Integer timesSelected = weightToTimesSelected.get(weightSelected);
      if (timesSelected == null) timesSelected = 0;
      weightToTimesSelected.put(weightSelected, timesSelected + 1);
      totalSelections++;
    }

    // Check that elements were selected the right number of times.
    for (Map.Entry<Integer, Integer> e : weightToTimesSelected.entrySet()) {
      double actualRatio = e.getValue() / (double) totalSelections;
      double expectedRatio = e.getKey() / (double) sumOfAllWeights;
      assertTrue(Math.abs(actualRatio - expectedRatio) < epsilon);
    }
  }
}
