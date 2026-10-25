package randoop.generation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import randoop.types.Type;

/**
 * The set of classes visited during demand-driven input creation that are not part of the software
 * under test (SUT), i.e., not explicitly specified by the user via {@code --classlist} or {@code
 * --testjar}. This class also records which visited non-SUT classes are not part of the JDK.
 *
 * <p>A class is visited if we inspected its constructors or static methods as potential producers
 * during producer discovery, or if it appeared as a parameter type of such a producer. A producer
 * is a constructor or static method that yields a value assignable to the type being resolved.
 */
public class NonSutClassSet {

  /** The classes visited during input creation that are not part of the SUT. */
  private final Set<Class<?>> nonSutClasses = new LinkedHashSet<>();

  /**
   * Non-SUT classes that are not part of the JDK and are not primitive types. This is a subset of
   * {@link #nonSutClasses}.
   */
  private final Set<Class<?>> nonJdkNonSutClasses = new LinkedHashSet<>();

  /**
   * Creates a {@code NonSutClassSet} from a set of types.
   *
   * @param types the set of types to add to the {@code NonSutClassSet}. The types must not be
   *     primitive or void types. The types are not part of the SUT.
   * @throws IllegalArgumentException if a primitive or void type is added
   */
  public NonSutClassSet(Set<Type> types) {
    addAll(types);
  }

  /**
   * Returns the set of non-SUT-classes.
   *
   * <p>This method exists so that {@code GenTests} can log these classes for the user.
   *
   * @return an unmodifiable set of all classes that are not part of the SUT visited during
   *     demand-driven input creation
   */
  public Set<Class<?>> getNonSutClasses() {
    return Collections.unmodifiableSet(nonSutClasses);
  }

  /**
   * Returns the subset of non-SUT classes that are not part of the JDK.
   *
   * <p>This method exists so that {@code GenTests} can log these classes for the user.
   *
   * @return an unmodifiable set of all classes that are not part of the SUT and are not part of the
   *     JDK
   */
  public Set<Class<?>> getNonJdkNonSutClasses() {
    return Collections.unmodifiableSet(nonJdkNonSutClasses);
  }

  /**
   * Matches JDK types that start with {@code java.} in either of the encodings produced by {@link
   * Class#getName()}:
   *
   * <ol>
   *   <li>The binary name of a non-array class (e.g. {@code "java.lang.String"}, {@code
   *       "java.util.Map$Entry"}).
   *   <li>The JVM array-descriptor for an <em>object</em> array whose component type is in {@code
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
   * Adds types to this {@link NonSutClassSet}.
   *
   * @param types the set of types to add to the {@code NonSutClassSet}. The types must not be
   *     primitive or void types. The types are not part of the SUT.
   * @throws IllegalArgumentException if a primitive or void type is added
   */
  private void addAll(Set<Type> types) {
    for (Type type : types) {
      if (type.isPrimitive() || type.isVoid()) {
        throw new IllegalArgumentException(
            "Tried to insert a primitive or void type in NonSutClassSet: " + type);
      }
      Class<?> cls = type.getRuntimeClass();
      if (cls == null) {
        throw new IllegalArgumentException("Type has no runtime class: " + type);
      }
      String className = cls.getName();

      if (nonSutClasses.add(cls)) {
        if (!isJdkClass(className)) {
          nonJdkNonSutClasses.add(cls);
        }
      }
    }
  }
}
