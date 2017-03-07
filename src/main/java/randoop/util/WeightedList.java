package randoop.util;

import java.util.ArrayList;
import java.util.List;

import randoop.BugInRandoopException;

public class WeightedList {

  private List<WeightedElement> theList;
  private List<Double> cumulativeWeights;
  private double totalWeight;

  public WeightedList() {
    theList = new ArrayList<>();
    cumulativeWeights = new ArrayList<>();
    totalWeight = 0.0;
    cumulativeWeights.add(0.0);
  }

  public void add(WeightedElement elt) {
    if (elt == null) throw new IllegalArgumentException("element to be added cannot be null.");
    theList.add(elt);
    totalWeight += elt.getWeight();
    cumulativeWeights.add(cumulativeWeights.get(cumulativeWeights.size() - 1) + elt.getWeight());
  }

  public WeightedElement getRandomElement() {
    return theList.get(getRandomIndex());
  }

  public int getRandomIndex() {

    // Find interval length. TODO cache max value.
    assert totalWeight > 0;

    // Select a random point in interval and find its corresponding element.
    double randomPoint = Randomness.random.nextDouble() * totalWeight;
    return binarySearchForIndex(randomPoint);
  }

  private int binarySearchForIndex(double point) {
    int low = 0;
    int high = theList.size();
    int mid = (low + high) / 2;
    while (!(cumulativeWeights.get(mid) < point && cumulativeWeights.get(mid + 1) >= point)) {
      if (cumulativeWeights.get(mid) < point) {
        low = mid;
      } else {
        high = mid;
      }
      mid = (low + high) / 2;
    }
    return mid;
  }

  // returns number of elements
  public int getSize() {
    return theList.size();
  }
}
