package randoop.sequence;

import java.util.HashMap;
import java.util.Map;

/**
 * For a given package P, PackageLiterals maps P (if present) to a collection of literals
 * (represented as single-element sequences) that can be used as inputs to classes in the given
 * package.
 */
public class PackageLiterals extends MappedSequences<Package> {

  private Map<Package, Map<Sequence, Integer>> sequenceOccurrence;

  private Map<Package, Integer> packageClassCount;

  public PackageLiterals() {
    super();
    sequenceOccurrence = new HashMap<>();
    packageClassCount = new HashMap<>();
  }

  @Override
  public void addSequence(Package key, Sequence seq) {
    if (seq == null) throw new IllegalArgumentException("seq is null");
    if (!seq.isNonreceiver()) {
      throw new IllegalArgumentException("seq is not a primitive sequence");
    }
    super.addSequence(key, seq);
  }

  // TODO: add comments
  public void addSequenceOccurrence(Package key, Sequence seq, int occurrence) {
    isPrimitive(key, seq);
    Map<Sequence, Integer> occurrenceMap =
        sequenceOccurrence.computeIfAbsent(key, __ -> new HashMap<>());
    occurrenceMap.put(seq, occurrence);
  }

  public void putPackageClassCount(Package key, int count) {
    packageClassCount.put(key, count);
  }

  public Map<Sequence, Integer> getSequenceOccurrence(Package key) {
    return sequenceOccurrence.get(key);
  }

  public int getPackageClassCount(Package key) {
    return packageClassCount.get(key);
  }

  public Map<Package, Map<Sequence, Integer>> getSequenceOccurrenceMap() {
    return sequenceOccurrence;
  }
}
