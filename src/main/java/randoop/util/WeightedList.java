package randoop.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import randoop.util.WeightedElement;
import randoop.BugInRandoopException;

public class WeightedList<T1 extends WeightedElement> {
  private List<T1> theList;
  private List<Double> cumulativeWeights;
  private double totalWeight;

  public WeightedList() {
    theList = new ArrayList<>();
    cumulativeWeights = new ArrayList<>();
    totalWeight = 0.0;
    cumulativeWeights.add(0.0);
  }

  public WeightedList(Collection<T1> values) {
    this();
    for (T1 w : values) {
      add(w);
    }
  }

  // For now assuming that this is a new element, will decide later if that is a good design decision
  public void add(T1 elt) {
    if (elt == null) throw new IllegalArgumentException("element to be added cannot be null.");
    if (elt.getWeight() < 0) throw new BugInRandoopException("weight is less than 0");
    theList.add(elt);
    totalWeight += elt.getWeight();
    cumulativeWeights.add(cumulativeWeights.get(cumulativeWeights.size() - 1) + elt.getWeight());
  }

  public void add(T1 elt, double weight) {
    add(elt, weight);
  }

  public void update(T1 weightedElement) {
    // this will be O(n), but it is what it is.
    int index = theList.indexOf(weightedElement);
    if (index >= 0) {
      double weight = cumulativeWeights.get(index + 1) - cumulativeWeights.get(index);
      weight -= weightedElement.getWeight();
      for (int i = index + 1; i < cumulativeWeights.size(); i++) {
        cumulativeWeights.set(i, cumulativeWeights.get(i) - weight);
      }
    }
  }

  public T1 getRandomElement() {
    if (theList.size() == 0) {
      return null;
    }
    return theList.get(getRandomIndex());
  }

  private int getRandomIndex() {

    // Find interval length. TODO cache max value.
    assert totalWeight > 0;

    // Select a random point in interval and find its corresponding element.
    double randomPoint = Randomness.random.nextDouble() * totalWeight;
    return binarySearchForIndex(randomPoint);
  }

  // Assumes that point is between 0 and totalWeight.
  // TODO
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
