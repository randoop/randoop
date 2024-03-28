package randoop.generation;

import java.util.HashMap;
import java.util.Map;
import randoop.sequence.Sequence;

/**
 * Stores information about a sequence, including the frequency and number of classes that contains
 * the current sequence in each class and package. Only used when constant mining is enabled.
 */
public class SequenceFrequencyInfo<T> {

  private Map<T, Map<Sequence, Integer>> frequencyMap;

  private Map<T, Map<Sequence, Integer>> classesWithConstantMap;

  private Map<T, Integer> classCount;

  public SequenceFrequencyInfo() {
    frequencyMap = new HashMap<>();
    classesWithConstantMap = new HashMap<>();
    classCount = new HashMap<>();
  }

  public void updateFrequency(T key, Sequence seq, int frequency) {
    //    if (GenInputsAbstract.literals_level == GenInputsAbstract.literals_level.CLASS) {
    //      // TODO
    //    }

    Map<Sequence, Integer> frequencyMap =
        this.frequencyMap.computeIfAbsent(key, __ -> new HashMap<>());
    frequencyMap.put(seq, frequency);
  }

  public void updateClassesWithConstant(T key, Sequence seq, int classesWithConstant) {
    Map<Sequence, Integer> classesWithConstantMap =
        this.classesWithConstantMap.computeIfAbsent(key, __ -> new HashMap<>());
    classesWithConstantMap.put(seq, classesWithConstant);
  }

  public void updateClassCount(T key, int count) {
    classCount.put(key, count);
  }

  public void getFrequency(T key) {
    frequencyMap.get(key);
  }

  public void getClassesWithConstant(T key) {
    classesWithConstantMap.get(key);
  }
}
