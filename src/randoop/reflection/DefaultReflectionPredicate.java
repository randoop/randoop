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
import randoop.util.Reflection;

/**
 * Returns true for public members, with some exceptions (see
 * doNotUseSpecialCase method).
 * 
 * If a method has the @CheckRep annotation, returns false
 * (the method will be used as a contract checker, not
 *  as a method under test).
 */
public class DefaultReflectionPredicate implements ReflectionPredicate {

  private Pattern omitMethods = null;
  private Set<String> omitFields;
  private VisibilityPredicate visibility;
  
  public DefaultReflectionPredicate() {
    this(null);
  }
  
  /** If omitMethods is null, then no methods are omitted. */
  public DefaultReflectionPredicate(Pattern omitMethods) {
    this(omitMethods, new HashSet<String>());
  }

  public DefaultReflectionPredicate(Pattern omitMethods, Set<String> omitFields) {
   this(omitMethods, omitFields, new PublicVisibilityPredicate());
  }

  /** 
   * DefaultReflectionFilter creates a filter object that uses default
   * criteria for inclusion of reflection objects. 
   * @param omitMethods pattern for methods to omit, if null then no methods omitted.
   * @param visibility 
   * @see OperationExtractor#getOperations(java.util.Collection, ReflectionPredicate)
   */
  public DefaultReflectionPredicate(Pattern omitMethods, Set<String> omitFields, VisibilityPredicate visibility) {
    super();
    this.omitMethods = omitMethods;
    this.omitFields = omitFields;
    this.visibility = visibility;
  }

  public boolean canUse(Class<?> c) {
    return visibility.isVisible (c);
  }

  public boolean canUse(Method m) {

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
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + m.toString());
        Log.logLine("  reason: it's a bridge method");
      }
      return false;
    }

    if (m.isSynthetic()) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + m.toString());
        Log.logLine("  reason: it's a synthetic method");
      }
      return false;
    }

    if (!visibility.isVisible(m)) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + m.toString());
        Log.logLine("  reason: randoop.util.Reflection.isVisible(int modifiers) returned false ");
      }
      return false;
    }
    if (!visibility.isVisible(m.getReturnType())) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + m.toString());
        Log.logLine("  reason: randoop.util.Reflection.isVisible(Class<?> cls) returned false for method's return type");
      }
      return false;
    }

    // TODO we could enable some methods from Object, like getClass
    if (m.getDeclaringClass().equals(java.lang.Object.class))
      return false;// handled here to avoid printing reasons

    if (m.getDeclaringClass().equals(java.lang.Thread.class))
      return false;// handled here to avoid printing reasons

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

  private String doNotUseSpecialCase(Method m) {

    // Special case 1:
    // We're skipping compareTo method in enums - you can call it only with the same type as receiver
    // but the signature does not tell you that
    if (m.getDeclaringClass().getCanonicalName().equals("java.lang.Enum")
        && m.getName().equals("compareTo")
        && m.getParameterTypes().length == 1
        && m.getParameterTypes()[0].equals(Enum.class))
      return "We're skipping compareTo method in enums";

    // Sepcial case 2:
    if (m.getName().equals("randomUUID"))
      return "We're skipping this to get reproducibility when running java.util tests.";

    // Special case 2:
    // hashCode is bad in general but String.hashCode is fair game
    if (m.getName().equals("hashCode") && ! m.getDeclaringClass().equals(String.class))
      return "hashCode";

    // Special case 3: (just clumps together a bunch of hashCodes, so skip it)
    if (m.getName().equals("deepHashCode") && m.getDeclaringClass().equals(Arrays.class))
      return "deepHashCode";

    // Special case 4: (differs too much between JDK installations)
    if (m.getName().equals("getAvailableLocales"))
      return "getAvailableLocales";

    // During experimentaion, we obseved that exception-related
    // methods can cause lots of nonterminating runs of Randoop. So we
    // don't explore them.
    if (m.getName().equals("fillInStackTrace"))
      return "Randoop avoids exploring Exception class methods.";
    if (m.getName().equals("getCause"))
      return "Randoop avoids exploring Exception class methods.";
    if (m.getName().equals("getLocalizedMessage"))
      return "Randoop avoids exploring Exception class methods.";
    if (m.getName().equals("getMessage"))
      return "Randoop avoids exploring Exception class methods.";
    if (m.getName().equals("getStackTrace"))
      return "Randoop avoids exploring Exception class methods.";
    if (m.getName().equals("initCause"))
      return "Randoop avoids exploring Exception class methods.";
    if (m.getName().equals("printStackTrace"))
      return "Randoop avoids exploring Exception class methods.";
    if (m.getName().equals("setStackTrace"))
      return "Randoop avoids exploring Exception class methods.";

    return null;
  }

  public boolean canUse(Constructor<?> c) {

    if (matchesOmitMethodPattern(c.toString())) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + c.toString());
      }
      return false;
    }

    // synthetic constructors are OK

    if (Modifier.isAbstract(c.getDeclaringClass().getModifiers()))
      return false;

    return visibility.isVisible(c);
  }

  private boolean matchesOmitMethodPattern(String name) {
     if (omitMethods == null) {
       return false;
     }
     boolean result = omitMethods.matcher(name).find();
     if (Log.isLoggingOn()) {
       Log.logLine (String.format("Comparing '%s' against pattern '%s' = %b%n", name,
                    omitMethods, result));
     }
     return result;
  }

  /**
   * canUse tests whether the name of a field is included among the 
   * omitted method names.
   * 
   * @param f field to test 
   * @return true if field name does not occur in omitFields pattern, and false if it does.
   */
  @Override
  public boolean canUse(Field f) {

    if (omitFields == null) {
      return true;
    }
    
    String name = f.getDeclaringClass().getName() + "." + f.getName();
    boolean result = visibility.isVisible(f) && !omitFields.contains(name);
    if (Log.isLoggingOn()) {
      if (result) {
        Log.logLine(String.format("Including field '%s'", name));
      } else {
        Log.logLine(String.format("Not including field '%s'", name));
      }
    }
    return result;

  }

}
