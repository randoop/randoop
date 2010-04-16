package randoop.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.Abstractable;
import randoop.BugInRandoopException;
import randoop.NotPartOfState;

/**
 * Takes as input an object and returns a linearization of the heap reachable
 * from the object.
 */
public class HeapLinearizer {

  public static enum LinearizationKind { FULL, SHAPE }

  /**
   * Returns a linearization of the heap reachable from o.
   * See Rostra paper for a definition of linearization (and pseudocode).
   * Every element of the returned list is a primitive value (can be null),
   * a String, or a boxed primitive.
   * If linearizationKind is FULL, then primitive object fields are included.
   * If linearizationKind is SHAPE, then only refernce object fields are included.
   * See HeapLinearizerTests for examples.
   */
  public static List<Object> linearize(Object o, LinearizationKind linearizationKind, boolean outputFieldNames) {
    Map<Integer, Integer> ids = new LinkedHashMap<Integer, Integer>();
    List<Object> retval = linearize(null, o, ids, linearizationKind, outputFieldNames);
    checkRep(retval);
    if (Log.isLoggingOn()) Log.log("Linearized state: " + retval);
    return retval;
  }

  private HeapLinearizer() {
    // no instances.
  }

  private static FieldComparator theFieldComparator = new FieldComparator();

  /** Compares fields based on their names. */
  private static class FieldComparator implements Comparator<Field>, Serializable {
    private static final long serialVersionUID = -6861599687120317067L;

    public int compare(Field arg0, Field arg1) {
      if (arg0 == null || arg1 == null) throw new IllegalStateException();
      return arg0.getName().compareTo(arg1.getName());
    }
  }

  /*
   * Skip boxed primitives to avoid infinite points-to graphs. Every boxed
   * primitive class has a field "value". If you ask for the field, a *new*
   * boxed object is returned (of the same class). So you can have an infinite
   * path.
   */
  private static boolean skipClass(Class<? extends Object> objectClass) {
    if (PrimitiveTypes.isBoxedPrimitiveTypeOrString(objectClass))
      return true;
    return false;
  }

  private static List<Field> getFields(Class<?> c) {
    List<Field> fields = new ArrayList<Field>();
    for (Class<?> clazz : classAndSuperClasses(c)) {
      for (Field f : clazz.getDeclaredFields()) {
        if (!skip(f))
          fields.add(f);
      }
    }
    return fields;
  }

  private static boolean skip(Field f) {
    if (Modifier.isStatic(f.getModifiers()))
      return true;
    if (f.getAnnotation(NotPartOfState.class) != null)
      return true;
    return false;
  }

  private static Set<Class<?>> classAndSuperClasses(Class<?> c) {
    Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
    Class<?> currentClass = c;
    while (currentClass != null) {
      classes.add(currentClass);
      for (Class<?> inter : currentClass.getInterfaces()) {
        classes.add(inter);
      }
      currentClass = currentClass.getSuperclass();
    }
    return classes;
  }

  private static void checkRep(List<Object> retval) {
    if (retval == null) throw new IllegalStateException();
    for (Object o : retval) {
      if (o == null || PrimitiveTypes.isBoxedOrPrimitiveOrStringType(o.getClass()))
        continue;
      throw new IllegalArgumentException("Expected primitive but got object of type " + o.getClass().getName());
    }
  }

  /** Follows the pseudo-code from Rostra paper */
  private static List<Object> linearize(Field f, Object o, Map<Integer, Integer> visited, LinearizationKind linKind, boolean outputFieldNames) {

    String fName = "";
    if (outputFieldNames) {
      fName = (f == null ? "field_name_null" : f.getName()) + ":";
    }

    // Base case 1: o is null.
    if (o == null) return Collections.singletonList((Object)(fName + null));

    // Base case 2: o is primitive or boxed primitive or string.
    if (PrimitiveTypes.isBoxedOrPrimitiveOrStringType(o.getClass())) {
      if (linKind == LinearizationKind.FULL) {
        return Collections.singletonList(((Object)(fName + o)));
      } else if (linKind == LinearizationKind.SHAPE) {
        return Collections.emptyList();
      } else {
        throw new BugInRandoopException();
      }
    }

    // Base case 3: o is a reference that has already been visited.
    int identityHashcode = System.identityHashCode(o);
    if (visited.containsKey(identityHashcode)) {
      return Collections.singletonList((Object)(fName + visited.get(identityHashcode)));
    }

    // Recursive case: o is a reference that has not been visited.
    int id = visited.size() + 1;
    visited.put(identityHashcode, id);
    List<Object> seq = new ArrayList<Object>();
    seq.add(fName + id);

    if (o.getClass().isArray()) {
      int length = Array.getLength(o);
      for (int i = 0; i < length; i++) {
        seq.addAll(linearize(null, Array.get(o, i), visited, linKind, outputFieldNames));
      }
    } else {
      Class<? extends Object> objectClass = o.getClass();
      if (skipClass(objectClass))
        return Collections.emptyList();


      // o is a reference. Before proceeding any further, see if it
      // declares an abstraction method, and if so, replace o by
      // its abstraction.
      if (o instanceof Abstractable) {
        Abstractable abstractable = (Abstractable)o;
        if (abstractable.shouldAbstract()) {
          for (Object o2 : abstractable.getAbstraction()) {
            seq.addAll(linearize(null, o2, visited, linKind, outputFieldNames));
          }
          return seq;
        }
      }

      List<Field> flst = getFields(objectClass);
      Collections.sort(flst, theFieldComparator);
      for (Field f2 : flst) {
        f2.setAccessible(true);
        try {
          seq.addAll(linearize(f2, f2.get(o), visited, linKind, outputFieldNames));
        } catch (IllegalArgumentException e) {
          throw new RuntimeException("this is a bug in randoop..." + e);
        } catch (IllegalAccessException e) {
          throw new RuntimeException("this is a bug in randoop..." + e);
        }
      }
    }

    return seq;
  }
}
