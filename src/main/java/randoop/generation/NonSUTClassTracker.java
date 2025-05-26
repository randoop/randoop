package randoop.generation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.checkerframework.checker.signature.qual.ClassGetName;
import randoop.main.GenInputsAbstract;
import randoop.reflection.AccessibilityPredicate;

/**
 * Keep track of classes used during demand-driven input creation that are not part of the software
 * under test (SUT), i.e., not explicitly specified by the user via {@code --classlist} or {@code
 * --testjar}. This class maintains both all Non-SUT classes and Non-SUT classes that are not part
 * of the JDK.
 */
public class NonSUTClassTracker {

  /** The set of classes that are part of the SUT. */
  private final Set<@ClassGetName String> sutClasses =
      GenInputsAbstract.getClassnamesFromArgs(AccessibilityPredicate.IS_ANY);

  /** The set of classes used during input creation that are not part of the SUT. */
  private final Set<Class<?>> nonSutClasses;

  /** Non-SUT classes that are not part of the JDK (and not primitives). */
  private final Set<Class<?>> nonJdkSutClasses;

  /** Matches JDK classes (including array types like [Ljava.lang.String;). */
  private static final Pattern JDK_CLASS_PATTERN = Pattern.compile("^(\\[+L)?java\\..");

  /** Creates a NonSUTClassTracker. */
  public NonSUTClassTracker() {
    nonSutClasses = new LinkedHashSet<>();
    nonJdkSutClasses = new LinkedHashSet<>();
  }

  /**
   * Records a class as a non-SUT class.
   *
   * @param cls the class to record
   */
  public void addNonSutClass(Class<?> cls) {
    nonSutClasses.add(cls);
    String name = cls.getName();
    if (!isJdkClass(name) && !cls.isPrimitive()) {
      nonJdkSutClasses.add(cls);
    }
  }

  /**
   * Returns the set of classes that are part of the SUT.
   *
   * @return an unmodifiable set of all classes that are part of the SUT
   */
  public Set<@ClassGetName String> getSutClasses() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(sutClasses));
  }

  /**
   * Returns the set of classes used during input creation that are not part of the SUT.
   *
   * @return an unmodifiable set of all classes used during input creation that are not part of the
   *     SUT
   */
  public Set<Class<?>> getNonSutClasses() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(nonSutClasses));
  }

  /**
   * Returns the set of classes used during input creation that are not part of the SUT and are not
   * part of the JDK.
   *
   * @return an unmodifiable set of all classes that are not part of the software under test and are
   *     not part of the JDK
   */
  public Set<Class<?>> getNonJdkNonSutClasses() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(nonJdkSutClasses));
  }

  /**
   * Returns true if the given class belongs to the JDK.
   *
   * @param className fully qualified class name
   * @return true if it's a JDK class, false otherwise
   */
  public static boolean isJdkClass(String className) {
    return className.startsWith("java.") || JDK_CLASS_PATTERN.matcher(className).find();
  }
}
