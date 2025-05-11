package randoop.generation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.checkerframework.checker.signature.qual.ClassGetName;
import randoop.main.GenInputsAbstract;
import randoop.reflection.AccessibilityPredicate;

/**
 * Tracks classes used during demand-driven input creation that are not part of the system under
 * test (SUT), i.e., not explicitly specified by the user via {@code --classlist} or {@code
 * --testjar}. This class maintains both all Non-SUT classes and Non-SUT classes that are not part
 * of the JDK.
 */
public class NonSUTClassTracker {

  /** The set of classes that are part of the system under test. */
  private static final Set<@ClassGetName String> SUT_CLASSES =
      GenInputsAbstract.getClassnamesFromArgs(AccessibilityPredicate.IS_ANY);

  /** The set of classes that are not part of the system under test. */
  private static final Set<Class<?>> NON_SUT_CLASSES = new LinkedHashSet<>();

  /** Non-SUT classes that are not part of the JDK (and not primitives). */
  private static final Set<Class<?>> NON_JDK_SUT_CLASSES = new LinkedHashSet<>();

  /** Matches JDK classes (including array types like [Ljava.lang.String;). */
  private static final Pattern JDK_CLASS_PATTERN = Pattern.compile("^\\[+L?java\\..*|^java\\..*");

  /** Private constructor to prevent instantiation. */
  private NonSUTClassTracker() {}

  /**
   * Records a class as a non-SUT class.
   *
   * @param cls the class to record
   */
  public static void addNonSutClass(Class<?> cls) {
    NON_SUT_CLASSES.add(cls);
    String name = cls.getName();
    if (!isJdkClass(name) && !cls.isPrimitive()) {
      NON_JDK_SUT_CLASSES.add(cls);
    }
  }

  /**
   * Returns the set of classes that are part of the system under test.
   *
   * @return an unmodifiable set of all classes that are part of the system under test
   */
  public static Set<@ClassGetName String> getSutClasses() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(SUT_CLASSES));
  }

  /**
   * Returns the set of classes that are not part of the system under test.
   *
   * @return an unmodifiable set of all classes that are not part of the system under test
   */
  public static Set<Class<?>> getNonSutClasses() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(NON_SUT_CLASSES));
  }

  /**
   * Returns the set of classes that are not part of the system under test and are not part of the
   * JDK.
   *
   * @return an unmodifiable set of all classes that are not part of the system under test and are
   *     not part of the JDK
   */
  public static Set<Class<?>> getNonJdkNonSutClasses() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(NON_JDK_SUT_CLASSES));
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
