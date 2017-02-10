package randoop.util;

import randoop.sequence.Sequence;

public interface WeightedRandomSampler {

  WeightedElement getRandomElement();

  void add(WeightedElement weightedElement);

  void add(WeightedElement t, double weight);

  int getSize();

  // TODO decide if the weightedElement should be update outside or inside update
}
