package randoop.util;

import java.util.ArrayList;
import java.util.List;

import randoop.BugInRandoopException;

public class WeightedList implements WeightedRandomSampler {

  private List<WeightedElement> theList;
  private List<Double> cumulativeWeights;
  private double totalWeight;

  public WeightedList() {
    theList = new ArrayList<>();
    cumulativeWeights = new ArrayList<>();
    totalWeight = 0.0;
  }

  // For now assuming that this is a new element, will decide later if that is a good design decision
  @Override
  public void add(WeightedElement elt) {
    if (elt == null) throw new IllegalArgumentException("element to be added cannot be null.");
    if (elt.getWeight() < 0) throw new BugInRandoopException("weight is less than 0");
    theList.add(elt);
    totalWeight += elt.getWeight();
    cumulativeWeights.add(cumulativeWeights.get(cumulativeWeights.size() - 1) + elt.getWeight());
  }

  // TODO think about how we want to do update here.
  @Override
  public void update(WeightedElement weightedElement) {}

  @Override
  public WeightedElement getRandomElement() {
    return theList.get(getRandomIndex());
  }

  public int getRandomIndex() {

    // Find interval length. TODO cache max value.
    assert totalWeight > 0;

    // Select a random point in interval and find its corresponding element.
    double randomPoint = Randomness.random.nextDouble() * totalWeight;
    return binarySearchForIndex(randomPoint, theList.size() / 2);
  }

  // Assumes that point is between 0 and totalWeight.
  // TODO
  private int binarySearchForIndex(double point, int index) {
    return 0;
  }
}
