package randoop.util;

import java.lang.reflect.Constructor;
// For Java 8: import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import plume.Pair;

/** Utility methods that operate on reflection objects (classes, methods, etc.). */
public final class Reflection {

  /**
   * Used by methods that that a java.lang.Class&lt;?&gt; object as
   * argument and use it to compute something based on it.
   */
  public static enum Match { EXACT_TYPE, COMPATIBLE_TYPE }

  static Map<String, Member> cached_deserializeMethodOrCtor =
      new LinkedHashMap<String, Member>();

  private Reflection() {
    // no instance
  }

  /**
   * Returns the set of classes that appear, recursively, in the interface of
   * the given class, to a given depth. For example, if class C1 declares
   * only method foo(C2)/C3,  and class C2 declares method bar(C4)/C5, then:
   *
   * We say that C1, C2 and C3 are related to C1 at depth &ge; 0.
   * We say that C4 and C5 are related to C1 at depth &ge; 1.
   *
   * We say that a class C2 appears in the interface of C iff:
   * (1) C2 is C
   * (2) C2 is a return value of some method in C.getMethods()
   * (2) C2 is a parameter of some method in C.getMethods() or some
   *     constructor in C.getConstructors().
   */
  public static Set<Class<?>> relatedClasses(Class<?> clazz, int depth) {
    if (clazz == null)
      throw new IllegalArgumentException("clazz cannot be null.");
    if (depth < 0)
      throw new IllegalArgumentException("depth must be non-negative.");
    return relatedClassesInternal(Collections.<Class<?>>singleton(clazz), depth);
  }

  public static Set<Class<?>> relatedClasses(Collection<Class<?>> classes, int i) {
    Set<Class<?>> result = new LinkedHashSet<Class<?>>();
    for (Class<?> c : classes) {
      result.addAll(relatedClasses(c, i));
    }
    return result;
  }

  private static Set<Class<?>> relatedClassesInternal(Set<Class<?>> classes, int depth) {
    if (depth < 0)
      return classes;
    Set<Class<?>> acc = new LinkedHashSet<Class<?>>();
    for (Class<?> c : classes) {
      acc.addAll(classesAppearingInInterface(c));
    }
    return relatedClassesInternal(acc, depth - 1);
  }

  private static Set<Class<?>> classesAppearingInInterface(Class<?> c) {
    Set<Class<?>> retval = new LinkedHashSet<Class<?>>();
    retval.add(c);
    for (Method m : c.getMethods()) {
      retval.add(m.getReturnType());
      retval.addAll(Arrays.asList(m.getParameterTypes()));
    }
    for (Constructor<?> cons : c.getConstructors()) {
      retval.addAll(Arrays.asList(cons.getParameterTypes()));
    }
    return Collections.unmodifiableSet(retval);
  }

  private static Set<Class<?>> getInterfacesTransitive(Class<?> c1) {

    Set<Class<?>> ret = new LinkedHashSet<Class<?>>();

    Class<?>[] c1Interfaces = c1.getInterfaces();
    for (int i = 0; i < c1Interfaces.length; i++) {
      ret.add(c1Interfaces[i]);
      ret.addAll(getInterfacesTransitive(c1Interfaces[i]));
    }

    Class<?> superClass = c1.getSuperclass();
    if (superClass != null)
      ret.addAll(getInterfacesTransitive(superClass));

    return ret;
  }

  public static Set<Class<?>> getDirectSuperTypes(Class<?> c) {
    Set<Class<?>> result= new LinkedHashSet<Class<?>>();
    Class<?> superclass = c.getSuperclass();
    if (superclass != null)
      result.add(superclass);
    result.addAll(Arrays.<Class<?>>asList(c.getInterfaces()));
    return result;
  }

  /**
   * Preconditions (established because this method is only called from
   * canBeUsedAs): params are non-null, are not Void.TYPE, and are not
   * isInterface().
   *
   * @param c1
   * @param c2
   */
  private static boolean isSubclass(Class<?> c1, Class<?> c2) {
    assert(c1 != null);
    assert(c2 != null);
    assert(!c1.equals(Void.TYPE));
    assert(!c2.equals(Void.TYPE));
    assert(!c1.isInterface());
    assert(!c2.isInterface());
    return c2.isAssignableFrom(c1);
  }


  private static Map<Pair<Class<?>, Class<?>>, Boolean> canBeUsedCache =
      new LinkedHashMap<Pair<Class<?>, Class<?>>, Boolean>();

  public static long num_times_canBeUsedAs_called = 0;

  /**
   * Checks if an object of class c1 can be used as an object of class c2.
   * This is more than subtyping: for example, int can be used as Integer,
   * but the latter is not a subtype of the former.
   */
  public static boolean canBeUsedAs(Class<?> c1, Class<?> c2) {
    if (c1 == null || c2 == null)
      throw new IllegalArgumentException("Parameters cannot be null.");
    if (c1.equals(c2))
      return true;
    if (c1.equals(void.class) && c2.equals(void.class))
      return true;
    if (c1.equals(void.class) || c2.equals(void.class))
      return false;
    Pair<Class<?>, Class<?>> classPair = new Pair<Class<?>, Class<?>>(c1, c2);
    Boolean cachedRetVal = canBeUsedCache.get(classPair);
    boolean retval;
    if (cachedRetVal == null) {
      retval = canBeUsedAs0(c1, c2);
      canBeUsedCache.put(classPair, retval);
    } else {
      retval = cachedRetVal;
    }
    return retval;
  }

  // TODO testclasses array code (third if clause)
  private static boolean canBeUsedAs0(Class<?> c1, Class<?> c2) {
    if (c1.isArray()) {
      if (c2.equals(Object.class))
        return true;
      if (!c2.isArray())
        return false;
      Class<?> c1SequenceType = c1.getComponentType();
      Class<?> c2componentType = c2.getComponentType();

      if (c1SequenceType.isPrimitive()) {
        if (c2componentType.isPrimitive()) {
          return (c1SequenceType.equals(c2componentType));
        } else {
          return false;
        }
      } else {
        if (c2componentType.isPrimitive()) {
          return false;
        } else {
          c1 = c1SequenceType;
          c2 = c2componentType;
        }
      }
    }

    if (c1.isPrimitive())
      c1 = PrimitiveTypes.boxedType(c1);
    if (c2.isPrimitive())
      c2 = PrimitiveTypes.boxedType(c2);

    boolean ret = false;

    if (c1.equals(c2)) { // XXX redundant (see canBeUsedAs(..)).
      ret = true;
    } else if (c2.isInterface()) {
      Set<Class<?>> c1Interfaces = getInterfacesTransitive(c1);
      if (c1Interfaces.contains(c2))
        ret = true;
      else
        ret = false;
    } else if (c1.isInterface()) {
      // c1 represents an interface and c2 a class.
      // The only safe possibility is when c2 is Object.
      if (c2.equals(Object.class))
        ret = true;
      else
        ret = false;
    } else {
      ret = isSubclass(c1, c2);
    }
    return ret;
  }

  /**
   * Checks whether the inputs can be used as arguments for the specified parameter types.
   * This method considers "null" as always being a valid argument.
   * errMsgContext is uninterpreted - just printed in error messages
   * Returns null if inputs are OK wrt paramTypes. Returns error message otherwise.
   */
  public static String checkArgumentTypes(Object[] inputs, Class<?>[] paramTypes, Object errMsgContext) {
    if (inputs.length != paramTypes.length)
      return "Bad number of parameters for " + errMsgContext + " was:" + inputs.length;

    for (int i = 0; i < paramTypes.length; i++) {
      Object input= inputs[i];
      Class<?> pType = paramTypes[i];
      if (! canBePassedAsArgument(input, pType))
        return "Invalid type of argument at pos " + i + " for:" + errMsgContext + " expected:" + pType + " was:"
        + (input == null ? "n/a(input was null)" : input.getClass());
    }
    return null;
  }

  /**
   * Returns whether the input can be used as argument for the specified parameter type.
   */
  public static boolean canBePassedAsArgument(Object inputObject, Class<?> parameterType) {
    if (parameterType == null || parameterType.equals(Void.TYPE))
      throw new IllegalStateException("Illegal type of parameter " + parameterType);
    if (inputObject == null) {
      return true;
    } else if (! Reflection.canBeUsedAs(inputObject.getClass(), parameterType)) {
      return false;
    } else
      return true;
  }
 
  /**
   * Looks for the specified method name in the specified class and all of its
   * superclasses
   */
  public static Method super_get_declared_method (Class<?> c,
      String methodname, Class<?>... parameter_types) throws Exception {

    // Try and find the method in the base class
    Exception exception = null;
    Method method = null;
    try {
      method = c.getDeclaredMethod (methodname, parameter_types);
    } catch (Exception e) {
      exception = e;
    }
    if (method != null)
      return method;


    // Otherwise, look for the method in all superclasses for the method
    while (c.getSuperclass() != null) {
      c = c.getSuperclass();
      try {
        method = c.getDeclaredMethod (methodname, parameter_types);
      } catch (Exception e) {
      }
      if (method != null)
        return method;
    }

    // couldn't find the method anywhere
    throw exception;
  }
  
}
