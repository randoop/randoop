package randoop.generation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.checkerframework.checker.signature.qual.ClassGetName;
import randoop.main.GenInputsAbstract;
import randoop.reflection.AccessibilityPredicate;
import randoop.types.Type;

/**
 * The set of classes used during demand-driven input creation that are not part of the software
 * under test (SUT), i.e., not explicitly specified by the user via {@code --classlist} or {@code
 * --testjar}. This class maintains both all non-SUT classes and non-SUT classes that are not part
 * of the JDK.
 */
public class NonSutClassSet {

  /** The classes that are part of the SUT. */
  private final Set<@ClassGetName String> sutClassNames =
      Collections.unmodifiableSet(
          GenInputsAbstract.getClassnamesFromArgs(AccessibilityPredicate.IS_ANY));

  /** The classes used during input creation that are not part of the SUT. */
  private final Set<Class<?>> nonSutClasses = new LinkedHashSet<>();

  /**
   * Non-SUT classes that are not part of the JDK and are not primitive types. This is a subset of
   * {@link #nonSutClasses}.
   */
  private final Set<Class<?>> nonJdkNonSutClasses = new LinkedHashSet<>();

  /** Creates a NonSutClassSet. */
  public NonSutClassSet() {}

  /**
   * Returns the set of classes used during demand-driven input creation that are not part of the
   * SUT.
   *
   * <p>This method exists only so that {@code GenTests} can log these classes for the user.
   *
   * @return an unmodifiable set of all classes that are not part of the software under test
   */
  public Set<Class<?>> getNonSutClasses() {
    return Collections.unmodifiableSet(nonSutClasses);
  }

  /**
   * Returns the set of classes used during demand-driven input creation that are not part of the
   * SUT and are not part of the JDK or primitive types.
   *
   * <p>This method exists only so that {@code GenTests} can log these classes for the user.
   *
   * @return an unmodifiable set of all classes that are not part of the software under test and are
   *     not part of the JDK
   */
  public Set<Class<?>> getNonJdkNonSutClasses() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(nonJdkNonSutClasses));
  }

  /**
   * Matches JDK types that start with {@code java.} in either of the encodings produced by {@link
   * Class#getName()}:
   *
   * <ol>
   *   <li>The binary name of a non-array class (e.g. {@code "java.lang.String"}, {@code
   *       "java.util.Map$Entry"}).
   *   <li>The JVM array-descriptor for an **object** array whose component type is in {@code
   *       java.*} (e.g. {@code "[Ljava.lang.String;"}, {@code "[[Ljava.util.Map$Entry;"}).
   * </ol>
   *
   * <p>Primitive-array descriptors such as {@code "[I"} are not matched by the pattern.
   */
  private static final Pattern JDK_CLASS_PATTERN = Pattern.compile("^(\\[+L)?java\\..");

  /**
   * Returns true if the given class belongs to the JDK.
   *
   * @param className fully qualified class name
   * @return true if it's a JDK class, false otherwise
   */
  private static boolean isJdkClass(String className) {
    return JDK_CLASS_PATTERN.matcher(className).find();
  }

  /**
   * Records a non-array receiver type in the {@link NonSutClassSet}. Since Randoop's invariant of
   * not using operations outside the SUT is violated, we will inform the user about this violation
   * through logging.
   *
   * @param types the receiver types to register (no {@code void}, primitive types, or array types)
   * @throws IllegalArgumentException if a primitive or void type is added
   */
  public void addAll(Set<Type> types) {
    for (Type type : types) {
      if (type.isPrimitive() || type.isVoid()) {
        throw new IllegalArgumentException(
            "Tried to insert non-receiver type in NonSutClassSet: " + type);
      }
      Class<?> cls = type.getRuntimeClass();
      String className = cls.getName();

      if (nonSutClasses.add(cls)) {
        if (!isJdkClass(className)) {
          nonJdkNonSutClasses.add(cls);
        }
      }
    }
  }
}
