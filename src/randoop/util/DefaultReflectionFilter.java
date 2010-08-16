package randoop.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.regex.Pattern;

import randoop.CheckRep;

/**
 * Returns true for public members, with some exceptions (see
 * doNotUseSpecialCase method).
 * 
 * If a method has the @CheckRep annotation, returns false
 * (the method will be used as a contract checker, not
 *  as a method under test).
 */
public class DefaultReflectionFilter implements ReflectionFilter {

  private Pattern omitmethods = null;

  /** omitmethods can be null (which means "omit no methods") */
  public DefaultReflectionFilter(Pattern omitmethods) {
    super();
    this.omitmethods = omitmethods;
  }

  public boolean canUse(Class<?> c) {
    return Reflection.isVisible (c);
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

    if (!Reflection.isVisible(m.getModifiers())) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + m.toString());
        Log.logLine("  reason: randoop.util.Reflection.isVisible(int modifiers) returned false ");
      }
      return false;
    }
    if (!Reflection.isVisible(m.getReturnType())) {
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

    return Reflection.isVisible (c.getModifiers());
  }

  private boolean matchesOmitMethodPattern(String name) {
     if (omitmethods == null) {
       return false;
     }
     boolean result = omitmethods.matcher(name).find();
     if (Log.isLoggingOn()) {
       Log.logLine (String.format("Comparing '%s' against pattern '%s' = %b%n", name,
                    omitmethods, result));
     }
     return result;
  }

}
