package randoop.generation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.checkerframework.checker.signature.qual.ClassGetName;
import randoop.main.GenInputsAbstract;
import randoop.reflection.AccessibilityPredicate;

/**
 * Tracks classes used during demand-driven input creation that are "out of scope", i.e., not
 * explicitly specified by the user via {@code --classlist} or {@code --testjar}. Maintains both all
 * out-of-scope classes and those outside the JDK.
 */
public class OutOfScopeClassTracker {

  /** Classes the user explicitly specified (in-scope). */
  private static final Set<@ClassGetName String> IN_SCOPE_CLASSES =
      GenInputsAbstract.getClassnamesFromArgs(AccessibilityPredicate.IS_ANY);

  /** Out-of-scope classes encountered during demand-driven generation. */
  private static final Set<Class<?>> OUT_OF_SCOPE_CLASSES = new LinkedHashSet<>();

  /** Out-of-scope classes that are not part of the JDK (and not primitives). */
  private static final Set<Class<?>> NON_JDK_OUT_OF_SCOPE_CLASSES = new LinkedHashSet<>();

  /** Matches JDK classes (including array types like [Ljava.lang.String;). */
  private static final Pattern JDK_CLASS_PATTERN = Pattern.compile("^\\[+L?java\\..*|^java\\..*");

  /** Private constructor to prevent instantiation. */
  private OutOfScopeClassTracker() {}

  /**
   * Records a class as out-of-scope.
   *
   * @param cls the class to record
   */
  public static void addOutOfScopeClass(Class<?> cls) {
    OUT_OF_SCOPE_CLASSES.add(cls);
    String name = cls.getName();
    if (!isJdkClass(name) && !cls.isPrimitive()) {
      NON_JDK_OUT_OF_SCOPE_CLASSES.add(cls);
    }
  }

  /**
   * Getter for the set of in-scope classes.
   *
   * @return an unmodifiable set of user-specified (in-scope) class names
   */
  public static Set<@ClassGetName String> getInScopeClasses() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(IN_SCOPE_CLASSES));
  }

  /**
   * Getter for the set of out-of-scope classes.
   *
   * @return an unmodifiable set of all out-of-scope classes
   */
  public static Set<Class<?>> getOutOfScopeClasses() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(OUT_OF_SCOPE_CLASSES));
  }

  /**
   * Getter for the set of out-of-scope classes that are not part of the JDK.
   *
   * @return an unmodifiable set of out-of-scope classes outside the JDK
   */
  public static Set<Class<?>> getNonJdkOutOfScopeClasses() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(NON_JDK_OUT_OF_SCOPE_CLASSES));
  }

  /**
   * Checks whether a class name belongs to the JDK.
   *
   * @param className fully qualified class name
   * @return true if it's a JDK class, false otherwise
   */
  public static boolean isJdkClass(String className) {
    return className.startsWith("java.") || JDK_CLASS_PATTERN.matcher(className).find();
  }
}
