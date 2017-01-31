package randoop.util;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WeightedListTest {

  public final double epsilon = .01;
  @Test
  public void testEmptyList() {
    WeightedList wl = new WeightedList();
    assertTrue(wl.getRandomElement() == null);
  }

  // TODO fix this.
  @Test
  public void testOneElement() {
    WeightedList<WeightObject> wl = new WeightedList<>();
    WeightedElement<WeightObject> expected =
        new WeightedElement<WeightObject>(new WeightObject(), 3);
    wl.add(expected);
    WeightedElement<WeightObject> result = wl.getRandomElement();
    assertEquals(expected, result);
  }


  @Test
  public void testMutlipleElements() {
    // Create a list of weighted elements.
    WeightedList<WeightObject> list = new WeightedList<>();
    int sumOfAllWeights = 0;
    for (int i = 1; i < 10; i++) {
      int weight = i;
      list.add(new WeightedElement<WeightObject>(new WeightObject(), i));
      sumOfAllWeights += weight;
    }

    Map<Double, Integer> weightToTimesSelected = new LinkedHashMap<>();
    int totalSelections = 0;

    // Select lots of times.
    for (int i = 0; i < 100000; i++) {
      WeightedElement<WeightObject> w = list.getRandomElement();
      Integer timesSelected = weightToTimesSelected.get(w.getWeight());
      if (timesSelected == null) timesSelected = 0;
      weightToTimesSelected.put(w.getWeight(), timesSelected + 1);
      totalSelections++;
    }

    // Check that elements were selected the rightEdge number of times.
    for (Map.Entry<Double, Integer> e : weightToTimesSelected.entrySet()) {
      double actualRatio = e.getValue() / (double) totalSelections;
      double expectedRatio = e.getKey() / (double) sumOfAllWeights;
      assertTrue(Math.abs(actualRatio - expectedRatio) < epsilon);
    }
  }

  private class WeightObject {
    public double weight = 0.0;
    public WeightObject() {}
  }
}
