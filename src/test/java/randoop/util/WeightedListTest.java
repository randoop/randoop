package randoop.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WeightedListTest {

  @Test
  public void testEmptyList() {
    WeightedList wl = new WeightedList();
    assertTrue(wl.getRandomElement() == null);
  }

  @Test
  public void testOneElement() {
    WeightedList wl = new WeightedList();
    WeightObject expected = new WeightObject(3);
    wl.add(expected);
    WeightedElement result = wl.getRandomElement();
    assertEquals(expected, result);
  }

  private class WeightObject implements WeightedElement {
    public double weight = 0.0;

    public WeightObject(double weight) {
      this.weight = weight;
    }

    @Override
    public double getWeight() {
      return weight;
    }
  }
}
