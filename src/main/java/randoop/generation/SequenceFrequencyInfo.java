package randoop.generation;

import randoop.types.ClassOrInterfaceType;

/**
 * Stores information about a sequence, including the frequency and occurrence of the sequence in
 * each class and package. Only used when constant mining is enabled.
 */
public interface SequenceFrequencyInfo {

  void update(ClassOrInterfaceType type, Package pkg, boolean hasOccurredInClass, int frequency);

  int getFrequency();

  int getOccurrence();

  int getClassLevelFrequency(ClassOrInterfaceType type);

  int getPackageLevelFrequency(Package pkg);
}
