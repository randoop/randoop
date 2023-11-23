package randoop.generation;

import java.util.HashMap;
import java.util.Map;
import randoop.types.ClassOrInterfaceType;

/**
 * Stores information about a sequence, including the frequency and occurrence of the sequence in
 * each class and package. Only used when constant mining is enabled.
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
  public int globalOccurrence;

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
  public Map<Package, Integer> packageOccurrence;

  /** Creates a new sequence info object. */
  public SequenceInfo() {
    globalFrequency = 0;
    globalOccurrence = 0;
    classFrequency = new HashMap<>();
    packageFrequency = new HashMap<>();
    packageOccurrence = new HashMap<>();
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
      globalOccurrence++;
      packageOccurrence.put(pkg, packageOccurrence.getOrDefault(pkg, 0) + 1);
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
  public int getGlobalOccurrence() {
    return globalOccurrence;
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
  public int getPackageLevelOccurrence(Package pkg) {
    return packageOccurrence.getOrDefault(pkg, 0);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("SequenceInfo: ");
    sb.append("globalFrequency: ").append(globalFrequency).append(System.lineSeparator());
    sb.append("globalOccurrence: ").append(globalOccurrence).append(System.lineSeparator());
    sb.append("classFrequency: ").append(classFrequency).append(System.lineSeparator());
    sb.append("packageFrequency: ").append(packageFrequency).append(System.lineSeparator());
    sb.append("packageOccurrence: ").append(packageOccurrence).append(System.lineSeparator());
    return sb.toString();
  }
}
