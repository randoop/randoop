package randoop.util;

public interface WeightedRandomSampler {

  WeightedElement getRandomElement();

  void add(WeightedElement weightedElement);

  // TODO decide if the weightedElement should be update outside or inside update
  void update(WeightedElement weightedElement);
}
