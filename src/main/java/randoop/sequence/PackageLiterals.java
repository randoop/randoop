package randoop.sequence;

import java.util.HashMap;
import java.util.Map;

/**
 * For a given package P, PackageLiterals maps P (if present) to a collection of literals
 * (represented as single-element sequences) that can be used as inputs to classes in the given
 * package.
 */
public class PackageLiterals extends MappedSequences<Package> {

  /** Map that stores the classesWithConstants information for each sequence in each package. */
  private Map<Package, Map<Sequence, Integer>> classesWithConstantsUnderPackage;

  /** Map that stores the class count for each package. */
  private Map<Package, Integer> packageClassCount;

  public PackageLiterals() {
    super();
    classesWithConstantsUnderPackage = new HashMap<>();
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
   * Adds the classesWithConstants information for a sequence to the global classesWithConstants map associated with the
   * given key.
   *
   * @param key the key value
   * @param seq the sequence
   * @param classesWithConstants the number of classes that contain the sequence in the package
   */
  public void addClassesWithConstant(Package key, Sequence seq, int classesWithConstants) {
    isPrimitive(key, seq);
    Map<Sequence, Integer> classesWithConstantMap =
        classesWithConstantsUnderPackage.computeIfAbsent(key, __ -> new HashMap<>());
    classesWithConstantMap.put(seq, classesWithConstants);
  }

  /**
   * Puts the class count for a given package.
   *
   * @param key the key value
   * @param count the class count
   */
  public void putPackageClassCount(Package key, int count) {
    packageClassCount.put(key, count);
  }

  /**
   * Gets the classesWithConstants information associated with the given package.
   *
   * @param key the key value
   * @return the classesWithConstants map associated with the given package
   */
  public Map<Sequence, Integer> getClassesWithConstants(Package key) {
    return classesWithConstantsUnderPackage.get(key);
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
  public Map<Package, Map<Sequence, Integer>> getClassesWithConstantsUnderPackage() {
    return classesWithConstantsUnderPackage;
  }
}
