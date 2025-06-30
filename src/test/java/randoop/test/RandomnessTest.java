package randoop.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.plumelib.util.SIList;
import randoop.util.Randomness;

public class RandomnessTest extends TestCase {

  private static final double epsilon = 0.01;

  /**
   * Checks that randomMemberWeighted returns a random element that is properly selected based on
   * the weights of all the elements in the given list. Creates a list of 10 elements with weights
   * 1..10, Selects an element from the list 100K times using randomMemberWeighted. Finally, checks
   * that each element was selected approximately as many times as expected (the ith element should
   * be selected approximately i/sum(1..10) times.
   */
  public void testRandomMemberWeighted() {

    Map<Object, Double> weightMap = new HashMap<>();

    // Create a list of weighted elements.
    List<Object> backingList = new ArrayList<>();
    int sumOfAllWeights = 0;
    for (int i = 1; i < 10; i++) {
      int weight = i;
      backingList.add(i);
      weightMap.put(i, (double) weight);
      sumOfAllWeights += weight;
    }
    SIList<Object> list = SIList.fromList(backingList);

    Map<Double, Integer> weightToTimesSelected = new LinkedHashMap<>();
    int totalSelections = 0;

    // Select lots of times.
    for (int i = 0; i < 100000; i++) {
      Object selected = Randomness.randomMemberWeighted(list, weightMap);
      double weightSelected = weightMap.get(selected);
      Integer timesSelected = weightToTimesSelected.get(weightSelected);
      if (timesSelected == null) {
        timesSelected = 0;
      }
      weightToTimesSelected.put(weightSelected, timesSelected + 1);
      totalSelections++;
    }

    // Check that elements were selected the right number of times.
    for (Map.Entry<Double, Integer> e : weightToTimesSelected.entrySet()) {
      double actualRatio = e.getValue() / (double) totalSelections;
      double expectedRatio = e.getKey() / (double) sumOfAllWeights;
      assertTrue(Math.abs(actualRatio - expectedRatio) < epsilon);
    }
  }
}
