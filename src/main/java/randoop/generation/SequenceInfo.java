package randoop.generation;

import java.util.HashMap;
import java.util.Map;
import randoop.types.ClassOrInterfaceType;

/**
 * Stores information about a sequence, including the frequency and number of classes that contains the sequence  in
 * each class and package. Each SequenceInfo always correspond to the frequency information for a
 * length-1 sequence for a literal value. Only used when constant mining is enabled.
 */
public class SequenceInfo {
  /**
   * The number of times this sequence occurs, in any class. Only used when the literal level is
   * ALL.
   */
  public int globalFrequency;

  /**
   * The number of classes in which this sequence occurs. Only used when the literal level is ALL.
   */
  public int globalClassesWithConstants;

  /**
   * The number of times this sequence occurs in each class. Only used when the literal level is
   * CLASS.
   */
  public Map<ClassOrInterfaceType, Integer> classFrequency;

  /**
   * The number of times this sequence occurs in each package. Only used when the literal level is
   * PACKAGE.
   */
  public Map<Package, Integer> packageFrequency;

  /**
   * The number of classes in which this sequence occurs in each package. Only used when the literal
   * level is PACKAGE.
   */
  public Map<Package, Integer> packageClassesWithConstants;

  /** Creates a new sequence info object. */
  public SequenceInfo() {
    // TODO: Null the unused fields. This should be done after reconstruction of SequenceInfo
    globalFrequency = 0;
    globalClassesWithConstants = 0;
    classFrequency = new HashMap<>();
    packageFrequency = new HashMap<>();
    packageClassesWithConstants = new HashMap<>();
  }

  /**
   * Update data structures to account for the fact that {@code seq} has been observed {@code
   * frequency} times in class {@code type}.
   *
   * @param hasOccurredInClass true if this is the second or subsequent occurrence of {@code seq} in
   *     the current class
   */
  public void update(
      ClassOrInterfaceType type, Package pkg, boolean hasOccurredInClass, int frequency) {
    globalFrequency += frequency;
    classFrequency.put(type, classFrequency.getOrDefault(type, 0) + frequency);
    packageFrequency.put(pkg, packageFrequency.getOrDefault(pkg, 0) + frequency);
    if (!hasOccurredInClass) {
      globalClassesWithConstants++;
      packageClassesWithConstants.put(pkg, packageClassesWithConstants.getOrDefault(pkg, 0) + 1);
    }
  }

  /**
   * Returns the number of times this sequence occurs, in any class. Only used when the literal
   * level is ALL.
   *
   * @return the number of times this sequence occurs, in any class
   */
  public int getGlobalFrequency() {
    return globalFrequency;
  }

  /**
   * Returns the number of classes in which this sequence occurs. Only used when the literal level
   * is ALL.
   *
   * @return the number of classes in which this sequence occurs
   */
  public int getGlobalClassesWithConstants() {
    return globalClassesWithConstants;
  }

  /**
   * Returns the number of times this sequence occurs in each class. Only used when the literal
   * level is CLASS.
   *
   * @param type the class
   * @return the number of times this sequence occurs in the given class
   */
  public int getClassLevelFrequency(ClassOrInterfaceType type) {
    return classFrequency.getOrDefault(type, 0);
  }

  /**
   * Returns the number of times this sequence occurs in each package. Only used when the literal
   * level is PACKAGE.
   *
   * @param pkg the package
   * @return the number of times this sequence occurs in the given package
   */
  public int getPackageLevelFrequency(Package pkg) {
    return packageFrequency.getOrDefault(pkg, 0);
  }

  /**
   * Returns the number of classes in which this sequence occurs in each package. Only used when the
   * literal level is PACKAGE.
   *
   * @param pkg the package
   * @return the number of classes in which this sequence occurs in the given package
   */
  public int getPackageLevelClassesWithConstants(Package pkg) {
    return packageClassesWithConstants.getOrDefault(pkg, 0);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("SequenceInfo: ");
    sb.append("globalFrequency: ").append(globalFrequency).append(System.lineSeparator());
    sb.append("globalClassesWithConstants: ").append(globalClassesWithConstants).append(System.lineSeparator());
    sb.append("classFrequency: ").append(classFrequency).append(System.lineSeparator());
    sb.append("packageFrequency: ").append(packageFrequency).append(System.lineSeparator());
    sb.append("packageClassesWithConstants: ").append(packageClassesWithConstants).append(System.lineSeparator());
    return sb.toString();
  }
}
