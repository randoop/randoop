package randoop.generation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.checkerframework.checker.signature.qual.ClassGetName;
import randoop.main.GenInputsAbstract;
import randoop.reflection.AccessibilityPredicate;

/**
 * Tracks classes used in demand-driven input creation that were not explicitly specified by the
 * user.
 */
public class UnspecifiedClassTracker {
  /**
   * Classes that were specified by the user through Randoop command-line arguments. This includes
   * classes under test, as provided with the {@code --classlist} or {@code --testjar} command-line
   * argument.
   */
  private static final Set<@ClassGetName String> specifiedClasses =
      GenInputsAbstract.getClassnamesFromArgs(AccessibilityPredicate.IS_ANY);

  /**
   * Classes used in demand-driven input creation that were not explicitly specified by the user.
   */
  private static final Set<Class<?>> unspecifiedClasses = new LinkedHashSet<>();

  /** {@link UnspecifiedClassTracker#unspecifiedClasses} that are not part of the JDK. */
  private static final Set<Class<?>> nonJdkUnspecifiedClasses = new LinkedHashSet<>();

  /** Pattern to match classes in the JDK. */
  private static final Pattern JDK_CLASS_PATTERN = Pattern.compile("^\\[+.java\\..*");

  /** Prevents instantiation. */
  private UnspecifiedClassTracker() {}

  /**
   * Adds a class to the set of unspecified classes.
   *
   * @param cls the class to add
   */
  public static void addClass(Class<?> cls) {
    unspecifiedClasses.add(cls);
    if (!inJdk(cls.getName()) && !cls.isPrimitive()) {
      nonJdkUnspecifiedClasses.add(cls);
    }
  }

  /**
   * Retrieves all specified classes.
   *
   * @return an unmodifiable set of specified classes
   */
  public static Set<@ClassGetName String> getSpecifiedClasses() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(specifiedClasses));
  }

  /**
   * Retrieves all unspecified classes.
   *
   * @return an unmodifiable set of unspecified classes
   */
  public static Set<Class<?>> getUnspecifiedClasses() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(unspecifiedClasses));
  }

  /**
   * Retrieves unspecified classes that are not part of the JDK and are not primitive types.
   *
   * @return an unmodifiable set of non-JDK unspecified classes
   */
  public static Set<Class<?>> getNonJdkUnspecifiedClasses() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(nonJdkUnspecifiedClasses));
  }

  /**
   * Determines whether a class name is part of the JDK.
   *
   * @param className the name of the class
   * @return true if the class is part of the JDK, false otherwise
   */
  public static boolean inJdk(String className) {
    return className.startsWith("java.") || JDK_CLASS_PATTERN.matcher(className).find();
  }
}
