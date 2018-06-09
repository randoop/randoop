package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import randoop.CheckRep;
import randoop.util.Log;

/**
 * Default implementations of methods that indicate what is "under test": whether a class, method,
 * constructor, or field should be used in Randoop's exploration. Returns true for public members,
 * with some exceptions (see {@link #doNotUseSpecialCase} method).
 *
 * <p>If a method has the {@code @CheckRep} annotation, returns false (the method will be used as a
 * contract checker, not as a method under test).
 */
public class DefaultReflectionPredicate implements ReflectionPredicate {

  /**
   * The set of fully-qualified field names to omit from generated tests. See {@link
   * randoop.main.GenInputsAbstract#omit_field}.
   */
  private Set<String> omitFields;

  /** Create a reflection predicate. */
  public DefaultReflectionPredicate() {
    this(new HashSet<String>());
  }

  /**
   * DefaultReflectionFilter creates a filter object that uses default criteria for inclusion of
   * reflection objects.
   *
   * @param omitFields set of fully-qualified field names to omit
   */
  public DefaultReflectionPredicate(Set<String> omitFields) {
    super();
    this.omitFields = omitFields;
  }

  @Override
  public boolean test(Class<?> c) {
    return !c.isAnonymousClass();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Does checks for the several cases, including main methods, bridge methods (see {@link
   * #discardBridge(Method)}, non-bridge synthetic methods, non-visible methods, or methods with
   * non-visible return types.
   *
   * <p>See the code for the full list.
   */
  @Override
  public boolean test(Method m) {

    if (isRandoopInstrumentation(m)) {
      return false;
    }

    // If it's a main entry method, don't use it (we're doing unit
    // testing, not running programs).
    Class<?>[] paramTypes = m.getParameterTypes();
    if (m.getName().equals("main")
        && paramTypes.length == 1
        && paramTypes[0].isArray()
        && paramTypes[0].getComponentType().equals(String.class)) {
      // Main method is not applicable to unit testing.
      Log.logPrintf("Will not use main method: %s%n", m);
      return false;
    }

    if (m.isBridge()) {
      if (discardBridge(m)) {
        return false;
      } else {
        Log.logPrintf("Using visibility bridge method: %s%n", m);
      }
    }

    if (!m.isBridge() && m.isSynthetic()) {
      Log.logPrintf("Will not use synthetic method: %s%n", m);
      return false;
    }

    // Within Object, consider only getClass to be under test (even if not specified by user).
    // Exclude all other methods.  They involve threads, waiting, or are somehow problematic
    // (e.g. toString).
    if (m.getDeclaringClass().equals(java.lang.Object.class)) {
      return m.getName().equals("getClass");
    }

    // This is a special case handled here to avoid printing the reason for exclusion.
    if (m.getDeclaringClass().equals(java.lang.Thread.class)) {
      return false;
    }

    if (m.getAnnotation(CheckRep.class) != null) {
      return false;
    }

    String reason = doNotUseSpecialCase(m);
    if (reason != null) {
      Log.logPrintf("Will not use: %s%n  reason: %s%n", m, reason);
      return false;
    }

    return true;
  }

  private boolean isRandoopInstrumentation(Method m) {
    return m.getName().contains("randoop_");
  }

  /**
   * Determines whether a bridge method should be discarded.
   *
   * <p>Bridge methods are synthetic overriding methods that are generated by the compiler to make
   * certain calls type-correct.
   *
   * <p>Two of the three known cases involve forcing unchecked casts to allow type narrowing of
   * return types (covariant return types) and instantiation of generic type parameters in methods.
   * Both of these are situations that a programmer could view as overriding, but really aren't.
   * These bridge methods do unchecked type conversions from the general type to the more specific
   * type expected by the local method. As a result, if included for testing, Randoop would generate
   * many tests that would confirm that there is an unchecked type conversion. So, we do not want to
   * include these methods.
   *
   * <p>The third known case involves a public class inheriting a public method defined in a private
   * class of the same package. The bridge method in the public class exposes the method outside of
   * the package, and we *do* want to be able to call this method. (This sort of trick is useful in
   * providing a facade to an API where implementation details are only accessible within the
   * package.)
   *
   * <p>The only case in which a bridge method should be kept is when it is a visibility bridge.
   *
   * @param m the bridge method to test
   * @return true if the bridge method should be discarded, false otherwise
   */
  private boolean discardBridge(Method m) {
    if (!isVisibilityBridge(m)) {
      Log.logPrintf("Will not use bridge method: %s%n", m);
      return true;
    } else if (m.getDeclaringClass().isAnonymousClass()
        && m.getDeclaringClass().getEnclosingClass() != null
        && m.getDeclaringClass().getEnclosingClass().isEnum()) {
      return true; // bridge method in enum constant anonymous class
    }
    return false;
  }

  /**
   * Determines whether a bridge method is a <i>visibility</i> bridge, which allows access to a
   * definition of the method in a non-visible superclass.
   *
   * <p>The method is a visibility bridge if this class is public and some superclass defines the
   * method as non-public.
   *
   * @param m the bridge method to test
   * @return true iff {@code m} is a visibility bridge
   * @throws Error if a {@link SecurityException} is thrown when accessing superclass methods
   */
  private boolean isVisibilityBridge(Method m) throws Error {
    Class<?> c = m.getDeclaringClass();
    if (!isPublic(c)) {
      return false;
    }
    c = c.getSuperclass();
    while (c != null) {
      if (!isPublic(c) && definesNonBridgeMethod(c, m)) {
        // System.out.printf("class %s defines non-bridge method %s%n", c, m);
        return true;
      }
      c = c.getSuperclass();
    }
    // System.out.printf("Never found superclass with definition of %s%n", m);
    return false;
  }

  /**
   * Returns true if the class defines the given method, not as a bridge method. Returns false if
   * the class does not define the given method, or if the class defines the method as a bridge
   * method. Ifnores inheritance of methods.
   */
  private boolean definesNonBridgeMethod(Class<?> c, Method goalMethod) {
    try {
      Method defined = c.getDeclaredMethod(goalMethod.getName(), goalMethod.getParameterTypes());
      return !defined.isBridge();
    } catch (NoSuchMethodException e) {
      return false;
    } catch (SecurityException e) {
      String msg =
          "Cannot access method " + goalMethod.getName() + " in class " + c.getCanonicalName();
      throw new Error(msg);
    }
  }

  /**
   * Indicates whether the {@code Class} is public.
   *
   * @param c the class
   * @return true if {@code c} is a public class
   */
  private boolean isPublic(Class<?> c) {
    return Modifier.isPublic(c.getModifiers() & Modifier.classModifiers());
  }

  /**
   * Indicates methods for which this predicate should return false. See inline comments for
   * details. This is a main place that Randoop controls which methods are methods under test.
   *
   * @param m the method to accept or reject for inclusion in methods under test
   * @return a non-null string giving a reason the method should be skipped, or null to not skip it
   */
  @SuppressWarnings("ReferenceEquality")
  private String doNotUseSpecialCase(Method m) {

    String mName = m.getName().intern();
    Class<?> mClass = m.getDeclaringClass();

    // Special case 1:
    // Skip compareTo method in enums -- you can call it only with the
    // same type as receiver, but the signature does not tell you that.
    if (!mClass.isAnonymousClass()
        && mClass.getCanonicalName().equals("java.lang.Enum")
        && mName == "compareTo" // interned
        && m.getParameterTypes().length == 1
        && m.getParameterTypes()[0].equals(Enum.class))
      return "Enum compareTo method has restrictions on argument types";

    // Special case 2: Nondeterminism
    if (mName == "randomUUID") { // interned
      return "randomUUID() is nondeterministic";
    }
    // hashCode is nondeterministic in general, but String.hashCode is deterministic.
    if (mName == "hashCode" // interned
        && !mClass.equals(String.class)) {
      return "hashCode may be nondeterministic";
    }
    if (mName == "deepHashCode" // interned
        && mClass.equals(Arrays.class)) {
      return "deepHashCode is nondeterministic because hashCode() is";
    }
    if (mName == "getAvailableLocales") { // interned
      return "getAvailableLocales differs too much between JDK installations";
    }

    // Special case 3:
    // During experimentation, we observed that exception-related methods can
    // cause lots of nonterminating runs of Randoop. So we don't explore them.
    if ((mName == "fillInStackTrace") // interned
        || (mName == "getCause") // interned
        || (mName == "getLocalizedMessage") // interned
        || (mName == "getMessage") // interned
        || (mName == "getStackTrace") // interned
        || (mName == "initCause") // interned
        || (mName == "printStackTrace") // interned
        || (mName == "setStackTrace")) { // interned
      return "Randoop avoids exploring Exception class methods, to avoid nontermination.";
    }

    return null;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Use the constructor unless it is specifically omitted, is synthetic with anonymous
   * parameters, or the class is abstract.
   */
  @Override
  public boolean test(Constructor<?> c) {

    // Synthetic constructors are OK unless they have anonymous parameters.
    if (c.isSynthetic()) {
      for (Class<?> p : c.getParameterTypes()) {
        if (p.isAnonymousClass()) {
          return false;
        }
      }
    }

    return !Modifier.isAbstract(c.getDeclaringClass().getModifiers());
  }

  /**
   * Determines whether the name of a field is included among the omitted field names.
   *
   * @param f field to test
   * @return true if field name does not occur in omitFields pattern, and false if it does
   */
  @Override
  public boolean test(Field f) {

    if (isRandoopInstrumentation(f)) {
      return false;
    }

    String name = f.getDeclaringClass().getName() + "." + f.getName();

    if (omitFields == null) { // No omitFields were given
      Log.logPrintf("Field '%s' included, no omit-field arguments%n", name);
      return true;
    }

    boolean result = !omitFields.contains(name);
    if (result) {
      Log.logPrintf("Field '%s' does not match omit-field, including field%n", name);
    } else {
      Log.logPrintf("Field '%s' matches omit-field, not including field%n", name);
    }
    return result;
  }

  /**
   * Indicates that a field is generated by the covered-class instrumentation agent.
   *
   * <p>Tests whether the field begins with {@code "randoop_"}.
   *
   * @param f the field
   * @return true if the field name
   */
  private boolean isRandoopInstrumentation(Field f) {
    return f.getName().contains("randoop_");
  }
}
