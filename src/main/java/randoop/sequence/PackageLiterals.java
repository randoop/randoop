package randoop.sequence;

import java.util.HashMap;
import java.util.Map;

/**
 * For a given package P, PackageLiterals maps P (if present) to a collection of literals
 * (represented as single-element sequences) that can be used as inputs to classes in the given
 * package.
 */
public class PackageLiterals extends MappedSequences<Package> {

  /** Map from package to a map from sequence to occurrence. */
  private Map<Package, Map<Sequence, Integer>> sequenceOccurrence;

  /** Map from package to the number of classes in the package. */
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

  /**
   * Adds the occurrence information for a sequence to the global occurrence map associated with the
   * given key.
   *
   * @param key the key value
   * @param seq the sequence
   * @param occurrence the occurrence of the sequence
   */
  public void addSequenceOccurrence(Package key, Sequence seq, int occurrence) {
    isPrimitive(key, seq);
    Map<Sequence, Integer> occurrenceMap =
        sequenceOccurrence.computeIfAbsent(key, __ -> new HashMap<>());
    occurrenceMap.put(seq, occurrence);
  }

  /**
   * Sets the class count for a given package.
   *
   * @param key the key value
   * @param count the class count
   */
  public void setPackageClassCount(Package key, int count) {
    packageClassCount.put(key, count);
  }

  /**
   * Gets the occurrence information associated with the given package.
   *
   * @param key the key value
   * @return the occurrence map associated with the given package
   */
  public Map<Sequence, Integer> getSequenceOccurrence(Package key) {
    return sequenceOccurrence.get(key);
  }

  /**
   * Gets the class count associated with the given package.
   *
   * @param key the key value
   * @return the class count associated with the given package
   */
  public int getPackageClassCount(Package key) {
    if (!packageClassCount.containsKey(key)) {
      // Only for avoiding exception
      // This should never be reached if the package is present
      return 0;
    }
    return packageClassCount.get(key);
  }

  // TODO: DELETE THIS. ONLY USED FOR TESTING
  public Map<Package, Map<Sequence, Integer>> getSequenceOccurrenceMap() {
    return sequenceOccurrence;
  }
}
