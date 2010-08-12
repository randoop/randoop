package randoop.util;

import java.util.ArrayList;
import java.util.List;

import randoop.BugInRandoopException;

public class WeightedList {

  private List<WeightedElement> theList;

  public WeightedList() {
    theList = new ArrayList<WeightedElement>();
  }

  public void add(WeightedElement elt) {
    if (elt == null)
      throw new IllegalArgumentException("element to be added cannot be null.");
    theList.add(elt);
  }

  public WeightedElement getRandomElement() {
    return theList.get(getRandomIndex());
  }

  public int getRandomIndex() {

    // Find interval length. TODO cache max value.
    double max = 0;
    for (int i = 0 ; i < theList.size() ; i++) {
      double weight = theList.get(i).getWeight();
      if (weight <= 0) throw new BugInRandoopException("weight was " + weight);
      max += weight;
    }
    assert max > 0;

    // Select a random point in interval and find its corresponding element.
    double randomPoint = Randomness.random.nextDouble() * max;
    double currentPoint = 0;
    for (int i = 0 ; i < theList.size() ; i++) {
      currentPoint += theList.get(i).getWeight();
      if (currentPoint >= randomPoint) {
        return i;
      }
    }
    throw new BugInRandoopException();
  }
}
