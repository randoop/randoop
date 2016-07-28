package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import randoop.CheckRep;
import randoop.util.Log;

/**
 * Returns true for public members, with some exceptions (see
 * doNotUseSpecialCase method).
 * <p>
 * If a method has the @CheckRep annotation, returns false (the method will be
 * used as a contract checker, not as a method under test).
 */
public class DefaultReflectionPredicate implements ReflectionPredicate {

  private Pattern omitMethods = null;
  private Set<String> omitFields;

  /**
   * Create a reflection predicate.
   * If omitMethods is null, then no methods are omitted.
   *
   * @param omitMethods  the pattern for names of methods to omit
   */
  public DefaultReflectionPredicate(Pattern omitMethods) {
    this(omitMethods, new HashSet<String>());
  }

  public DefaultReflectionPredicate() {
    this(null, new HashSet<String>());
  }

  /**
   * DefaultReflectionFilter creates a filter object that uses default criteria
   * for inclusion of reflection objects.
   *
   * @param omitMethods
   *          pattern for methods to omit, if null then no methods omitted.
   * @param omitFields
   *          set of field names to omit
   */
  public DefaultReflectionPredicate(Pattern omitMethods, Set<String> omitFields) {
    super();
    this.omitMethods = omitMethods;
    this.omitFields = omitFields;
  }

  @Override
  public boolean test(Class<?> c) {
    return !c.isAnonymousClass();
  }

  /**
   * {@inheritDoc} Does checks for the following cases:
   * <ul>
   * <li>Main methods
   * <li>Methods matching omission pattern
   * <li>Bridge methods related to type
   * <li>Non-bridge, synthetic methods
   * <li>Methods that are not visible, or do not have visible return type
   * <li>[Special cases that need to be listed TODO]
   * </ul>
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
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + m.toString());
        Log.logLine("  reason: main method not applicable to unit testing.");
      }
      return false;
    }

    if (matchesOmitMethodPattern(m.toString())) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + m.toString());
        Log.logLine("  reason: matches regexp specified in --omitmethods option.");
      }
      return false;
    }

    if (m.isBridge()) {
      if (discardBridge(m)) {
        return false;
      } else {
        if (Log.isLoggingOn()) {
          Log.logLine("Using visibility bridge method: " + m.toString());
        }
      }
    }

    if (!m.isBridge() && m.isSynthetic()) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + m.toString());
        Log.logLine("  reason: it's a synthetic method");
      }
      return false;
    }

    // TODO we could enable some methods from Object, like getClass
    if (m.getDeclaringClass().equals(java.lang.Object.class))
      return false; // handled here to avoid printing reasons

    if (m.getDeclaringClass().equals(java.lang.Thread.class))
      return false; // handled here to avoid printing reasons

    if (m.getAnnotation(CheckRep.class) != null) {
      return false;
    }

    String reason = doNotUseSpecialCase(m);
    if (reason != null) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + m.toString());
        Log.logLine("  reason: " + reason);
      }
      return false;
    }

    return true;
  }

  private boolean isRandoopInstrumentation(Method m) {
    return m.getName().contains("randoop_");
  }

  /**
   * Determines whether a bridge method should be discarded.
   * <p>
   * Bridge methods are synthetic overriding methods that are used by the
   * compiler to make certain things possible that seem reasonable but need
   * tweaks to make them work. Two of the three known cases involve forcing
   * unchecked casts to allow type narrowing of return types (covariant return
   * types) and instantiation of generic type parameters in methods. Both of
   * these are situations that we think of as overriding, but really aren't.
   * These bridge methods do unchecked type conversions from the general type to
   * the more specific type expected by the local method. As a result, if
   * included for testing, Randoop would generate many tests that would confirm
   * that there is an unchecked type conversion. So, we do not want to include
   * these methods.
   * <p>
   * The third known case involves a public class inheriting a public method
   * defined in a private class of the same package. The bridge method in the public
   * class exposes the method outside of the package, and we *do* want to be
   * able to call this method. (This sort of trick is useful in providing a
   * facade to an API where implementation details are only accessible within the
   * package.)
   * <p>
   * The only case in which a bridge method should be kept is when it is a visibility bridge.
   *
   * @param m the bridge method to test
   * @return true if the bridge method should be discarded, false otherwise
   */
  private boolean discardBridge(Method m) {
    if (isNotVisibilityBridge(m)) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + m.toString());
        Log.logLine("  reason: it's a bridge method");
      }
      return true;
    } else if (m.getDeclaringClass().isAnonymousClass()
        && m.getDeclaringClass().getEnclosingClass() != null
        && m.getDeclaringClass().getEnclosingClass().isEnum()) {
      return true; // bridge method in enum constant anonymous class
    }
    return false;
  }

  /**
   * Determines whether a bridge method is not a <i>visibility</i> bridge, which
   * allows access to a definition of the method in a non-visible superclass.
   * <p>
   * To recognize a visibility bridge, it is sufficient to run up the superclass
   * chain and confirm that the visibility of the class changes to non-public.
   * If it does not, then the bridge method is not a visibility bridge.
   *
   * @param m
   *          the bridge method to test
   * @return true if {@code m} is not a visibility bridge, and false otherwise
   * @throws Error
   *           if a {@link SecurityException} is thrown when accessing
   *           superclass methods
   */
  private boolean isNotVisibilityBridge(Method m) throws Error {
    Method method = m;
    Class<?> c = m.getDeclaringClass();
    if (!isPublic(c)) {
      return true;
    }
    while (c != null && isPublic(c) && method != null && method.isBridge()) {
      c = c.getSuperclass();
      try {
        method = c.getDeclaredMethod(m.getName(), m.getParameterTypes());
      } catch (NoSuchMethodException e) {
        method = null;
      } catch (SecurityException e) {
        String msg = "Cannot access method " + m.getName() + " in class " + c.getCanonicalName();
        throw new Error(msg);
      }
    }
    return isPublic(c);
  }

  private boolean isPublic(Class<?> c) {
    return Modifier.isPublic(c.getModifiers() & Modifier.classModifiers());
  }

  private String doNotUseSpecialCase(Method m) {

    // Special case 1:
    // We're skipping compareTo method in enums - you can call it only with the
    // same type as receiver
    // but the signature does not tell you that
    if (!m.getDeclaringClass().isAnonymousClass()
        && m.getDeclaringClass().getCanonicalName().equals("java.lang.Enum")
        && m.getName().equals("compareTo")
        && m.getParameterTypes().length == 1
        && m.getParameterTypes()[0].equals(Enum.class))
      return "We're skipping compareTo method in enums";

    // Sepcial case 2:
    if (m.getName().equals("randomUUID"))
      return "We're skipping this to get reproducibility when running java.util tests.";

    // Special case 2:
    // hashCode is bad in general but String.hashCode is fair game
    if (m.getName().equals("hashCode") && !m.getDeclaringClass().equals(String.class))
      return "hashCode";

    // Special case 3: (just clumps together a bunch of hashCodes, so skip it)
    if (m.getName().equals("deepHashCode") && m.getDeclaringClass().equals(Arrays.class))
      return "deepHashCode";

    // Special case 4: (differs too much between JDK installations)
    if (m.getName().equals("getAvailableLocales")) return "getAvailableLocales";

    // During experimentaion, we obseved that exception-related
    // methods can cause lots of nonterminating runs of Randoop. So we
    // don't explore them.
    if (m.getName().equals("fillInStackTrace"))
      return "Randoop avoids exploring Exception class methods.";
    if (m.getName().equals("getCause")) return "Randoop avoids exploring Exception class methods.";
    if (m.getName().equals("getLocalizedMessage"))
      return "Randoop avoids exploring Exception class methods.";
    if (m.getName().equals("getMessage"))
      return "Randoop avoids exploring Exception class methods.";
    if (m.getName().equals("getStackTrace"))
      return "Randoop avoids exploring Exception class methods.";
    if (m.getName().equals("initCause")) return "Randoop avoids exploring Exception class methods.";
    if (m.getName().equals("printStackTrace"))
      return "Randoop avoids exploring Exception class methods.";
    if (m.getName().equals("setStackTrace"))
      return "Randoop avoids exploring Exception class methods.";

    return null;
  }

  @Override
  public boolean test(Constructor<?> c) {

    if (matchesOmitMethodPattern(c.toString())) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + c.toString());
      }
      return false;
    }

    // synthetic constructors are OK
    // unless they have anonymous parameters
    if (c.isSynthetic()) {
      for (Class<?> p : c.getParameterTypes()) {
        if (p.isAnonymousClass()) {
          return false;
        }
      }
    }

    return !Modifier.isAbstract(c.getDeclaringClass().getModifiers());
  }

  private boolean matchesOmitMethodPattern(String name) {
    if (omitMethods == null) {
      return false;
    }
    boolean result = omitMethods.matcher(name).find();
    if (Log.isLoggingOn()) {
      Log.logLine(
          String.format("Comparing '%s' against pattern '%s' = %b%n", name, omitMethods, result));
    }
    return result;
  }

  /**
   * Determines whether the name of a field is included among the omitted field
   * names.
   *
   * @param f
   *          field to test
   * @return true if field name does not occur in omitFields pattern, and false
   *         if it does
   */
  @Override
  public boolean test(Field f) {

    if (isRandoopInstrumentation(f)) {
      return false;
    }

    if (omitFields == null) {
      return true;
    }

    String name = f.getDeclaringClass().getName() + "." + f.getName();
    boolean result = !omitFields.contains(name);
    if (Log.isLoggingOn()) {
      if (result) {
        Log.logLine(String.format("Including field '%s'", name));
      } else {
        Log.logLine(String.format("Not including field '%s'", name));
      }
    }
    return result;
  }

  private boolean isRandoopInstrumentation(Field f) {
    return f.getName().contains("randoop_");
  }
}
