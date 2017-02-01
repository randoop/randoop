package randoop.util;

import java.util.ArrayList;
import java.util.List;

import randoop.BugInRandoopException;

public class WeightedList<T extends Comparable<T>> implements WeightedRandomSampler<T> {

  private List<WeightedElement<T>> theList;
  private List<Double> cumulativeWeights;
  private double totalWeight;

  public WeightedList() {
    theList = new ArrayList<>();
    cumulativeWeights = new ArrayList<>();
    totalWeight = 0.0;
    cumulativeWeights.add(0.0);
  }

  // For now assuming that this is a new element, will decide later if that is a good design decision
  @Override
  public void add(WeightedElement<T> elt) {
    if (elt == null) throw new IllegalArgumentException("element to be added cannot be null.");
    if (elt.getWeight() < 0) throw new BugInRandoopException("weight is less than 0");
    theList.add(elt);
    totalWeight += elt.getWeight();
    cumulativeWeights.add(cumulativeWeights.get(cumulativeWeights.size() - 1) + elt.getWeight());
  }

  public void add(T elt, double weight) {
    add(new WeightedElement<T>(elt, weight));
  }

  public void update(WeightedElement<T> weightedElement) {
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

  @Override
  public WeightedElement<T> getRandomElement() {
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

  @Override
  // returns number of elements
  public int getSize() {
    return theList.size();
  }
}
