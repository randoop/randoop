package randoop.util;

import randoop.sequence.Sequence;

public interface WeightedRandomSampler<T> {

  WeightedElement<T> getRandomElement();

  void add(WeightedElement<T> weightedElement);

  void add(T t, double weight);

  // TODO decide if the weightedElement should be update outside or inside update
}
