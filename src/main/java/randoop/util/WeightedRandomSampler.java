package randoop.util;

import randoop.sequence.Sequence;

public interface WeightedRandomSampler {

  Sequence getRandomElement();

  void add(Sequence weightedElement);

  // TODO decide if the weightedElement should be update outside or inside update
}
