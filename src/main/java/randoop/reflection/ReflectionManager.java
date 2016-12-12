package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import randoop.util.Log;

/**
 * ReflectionManager reflectively visits a {@link Class} instance to apply a set
 * of {@link ClassVisitor} objects to the class members. Uses a
 * {@link VisibilityPredicate} and heuristics to determine which classes and
 * class members to visit.
 * <p>
 * For a non-enum class, visits:
 * <ul>
 * <li>all methods satisfying predicate.
 * <li>all constructors satisfying predicate.
 * <li>all fields that satisfy predicate and are not hidden. (A hidden field is
 * a member of superclass with field of same name in current class. These are
 * accessible via reflection.).
 * <li>inner enums satisfying predicate.
 * </ul>
 * <p>
 * For an enum, visits:
 * <ul>
 * <li>all enum constants.
 * <li>methods of the enum satisfying predicate other than <code>values</code>
 * and <code>valueOf</code>.
 * <li>methods defined for enum constants that satisfy predicate.
 * </ul>
 * <p>
 * Note that visitors may have their own predicates, but need not check visibility.
 */
public class ReflectionManager {

  private VisibilityPredicate predicate;
  private ArrayList<ClassVisitor> visitors;

  /**
   * Creates a manager object that uses the given predicate to determine which
   * classes, methods and constructors should be visited. The list of visitors
   * is initially empty.
   *
   * @param predicate
   *          the predicate to indicate whether classes and class members should
   *          be visited
   */
  public ReflectionManager(VisibilityPredicate predicate) {
    this.predicate = predicate;
    this.visitors = new ArrayList<>();
  }

  /**
   * Registers a {@link ClassVisitor} for use by the
   * {@link ReflectionManager#apply(Class)} method.
   *
   * @param visitor  the {@link ClassVisitor} object to add
   */
  public void add(ClassVisitor visitor) {
    visitors.add(visitor);
  }

  /**
   * Applies the registered {@link ClassVisitor} objects of this object to the
   * given class and its members that satisfy the given predicate.
   * Excludes fields that are hidden by inheritance that are otherwise still accessible by
   * reflection.
   * Each visitor is applied to each member at most once.
   *
   * @param c  the {@link Class} object to be visited.
   */
  public void apply(Class<?> c) {
    for (ClassVisitor visitor : visitors) {
      apply(visitor, c);
    }
  }

  /**
   * Applies the given {@link ClassVisitor} visitor to the class object and its members that
   * satisfy the predicate of this reflection manager.
   * <p>
   * Sorts class members to ensure a canonical order for visits.
   *
   * @param visitor  the {@link ClassVisitor} to apply to the class
   * @param c  the class
   */
  public void apply(ClassVisitor visitor, Class<?> c) {
    if (predicate.isVisible(c)) {
      if (Log.isLoggingOn()) {
        Log.logLine("Applying visitors to class " + c.getName());
      }

      visitBefore(visitor, c); // perform any previsit steps

      if (c.isEnum()) { // treat enum classes differently
        applyToEnum(visitor, c);
      } else {

        // Methods
        Set<Method> methods = new HashSet<>();
        List<Method> methodList = toSortedList(c.getMethods(), new MethodComparator());
        List<Method> declaredMethodList =
            toSortedList(c.getDeclaredMethods(), new MethodComparator());
        for (Method m : methodList) { // for all class methods
          methods.add(m); // remember to avoid duplicates
          if (isVisible(m)) { // if satisfies predicate then visit
            applyTo(visitor, m);
          }
        }
        for (Method m : declaredMethodList) { // for all methods declared by c
          // if not duplicate and satisfies predicate
          if ((!methods.contains(m)) && predicate.isVisible(m)) {
            applyTo(visitor, m);
          }
        }

        // Constructors
        List<Constructor<?>> constructorList =
            toSortedList(c.getDeclaredConstructors(), new ConstructorComparator());
        for (Constructor<?> co : constructorList) {
          if (isVisible(co)) {
            applyTo(visitor, co);
          }
        }

        // member types
        List<Class<?>> classList = toSortedList(c.getDeclaredClasses(), new ClassComparator());
        for (Class<?> ic : classList) {
          if (predicate.isVisible(ic)) {
            visitBefore(visitor, ic);
            if (ic.isEnum()) { // inner enums require special attention
              applyToEnum(visitor, ic);
            }
            visitAfter(visitor, ic);
          }
        }

        // Fields
        // The set of fields declared in class c is needed to ensure we don't
        // collect inherited fields that are hidden by local declaration
        Set<String> declaredNames = new TreeSet<>();
        List<Field> declaredFieldList = toSortedList(c.getDeclaredFields(), new FieldComparator());
        List<Field> fieldList = toSortedList(c.getFields(), new FieldComparator());
        for (Field f : declaredFieldList) { // for fields declared by c
          declaredNames.add(f.getName());
          if (predicate.isVisible(f)) {
            applyTo(visitor, f);
          }
        }
        for (Field f : fieldList) { // for all public fields of c
          // keep a field that satisfies filter, and is not inherited and hidden by
          // local declaration
          if (predicate.isVisible(f) && (!declaredNames.contains(f.getName()))) {
            applyTo(visitor, f);
          }
        }
      }

      visitAfter(visitor, c);
    }
  }

  /**
   * Applies the visitors to the constants and methods of the given enum. A
   * method is included if it satisfies the predicate, and either is declared in
   * the enum, or in the anonymous class of some constant. Note that methods
   * will either belong to the enum itself, or to an anonymous class attached to
   * a constant. Ordinarily, the type of the constant is the enum, but when
   * there is an anonymous class for constant e, e.getClass() returns the
   * anonymous class. This is used to check for method overrides (that could
   * include Object methods) within the constant.
   * <p>
   * Heuristically exclude methods <code>values</code> and <code>valueOf</code>
   * since their definition is implicit, and we aren't testing Java enum
   * implementation.
   *
   * @param visitor  the {@link ClassVisitor}
   * @param c  the enum class object from which constants and methods are extracted
   */
  private void applyToEnum(ClassVisitor visitor, Class<?> c) {
    Map<String, Set<Method>> overrideMethods = new HashMap<>();
    for (Object obj : c.getEnumConstants()) {
      Enum<?> e = (Enum<?>) obj;
      applyTo(visitor, e);
      if (!e.getClass().equals(c)) { // does constant have an anonymous class?
        for (Method m : e.getClass().getDeclaredMethods()) {
          Set<Method> methodSet = overrideMethods.get(m.getName());
          if (methodSet == null) {
            methodSet = new HashSet<>();
          }
          methodSet.add(m);
          overrideMethods.put(m.getName(), methodSet); // collect any potential overrides
        }
      }
    }
    // get methods that are explicitly declared in the enum
    List<Method> declaredMethodList = toSortedList(c.getDeclaredMethods(), new MethodComparator());
    for (Method m : declaredMethodList) {
      if (predicate.isVisible(m)) {
        if (!m.getName().equals("values") && !m.getName().equals("valueOf")) {
          applyTo(visitor, m);
        }
      }
    }
    // get any inherited methods also declared in anonymous class of some
    // constant
    List<Method> methodList = toSortedList(c.getMethods(), new MethodComparator());
    for (Method m : methodList) {
      if (predicate.isVisible(m)) {
        Set<Method> methodSet = overrideMethods.get(m.getName());
        if (methodSet != null) {
          for (Method method : methodSet) {
            applyTo(visitor, method);
          }
        }
      }
    }
  }

  /**
   * Apply a visitor to a field.
   *
   * @param v  the {@link ClassVisitor}
   * @param f
   *          the field to be visited
   */
  private void applyTo(ClassVisitor v, Field f) {
    if (Log.isLoggingOn()) {
      Log.logLine(String.format("Visiting field %s", f.toGenericString()));
    }
    v.visit(f);
  }

  /**
   * Apply a visitor to a constructor.
   *
   * @param v  the {@link ClassVisitor}
   * @param co
   *          the constructor to be visited
   */
  private void applyTo(ClassVisitor v, Constructor<?> co) {
    if (Log.isLoggingOn()) {
      Log.logLine(String.format("Visiting constructor %s", co.toGenericString()));
    }
    v.visit(co);
  }

  /**
   * Apply a visitor to a method.
   *
   * @param v  the {@link ClassVisitor}
   * @param m
   *          the method to be visited
   */
  private void applyTo(ClassVisitor v, Method m) {
    if (Log.isLoggingOn()) {
      Log.logLine(String.format("Visiting method %s", m.toGenericString()));
    }
    v.visit(m);
  }

  /**
   * Apply a visitor to a enum value.
   *
   * @param v  the {@link ClassVisitor}
   * @param e
   *          the enum value to be visited
   */
  private void applyTo(ClassVisitor v, Enum<?> e) {
    if (Log.isLoggingOn()) {
      Log.logLine(String.format("Visiting enum %s", e));
    }
    v.visit(e);
  }

  /**
   * Apply a visitor to a class. Called at the end of
   * {@link #apply(Class)}.
   *
   * @param v  the {@link ClassVisitor}
   * @param c
   *          the class to be visited
   */
  private void visitAfter(ClassVisitor v, Class<?> c) {
    v.visitAfter(c);
  }

  /**
   * Apply a visitor to a class. Called at the beginning of
   * {@link #apply(Class)}.
   *
   * @param v  the {@link ClassVisitor}
   * @param c  the class to be visited
   */
  private void visitBefore(ClassVisitor v, Class<?> c) {
    v.visitBefore(c);
  }

  /**
   * Determines whether a method, its parameter types, and its return type are all visible.
   *
   * @param m  the method to check for visibility
   * @return true if the method, each parameter type, and the return type are all visible; and false otherwise
   */
  private boolean isVisible(Method m) {
    if (!predicate.isVisible(m)) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + m.toGenericString());
        Log.logLine("  reason: the method is not visible from test classes");
      }
      return false;
    }
    if (!isVisible(m.getGenericReturnType())) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + m.toGenericString());
        Log.logLine("  reason: the method's return type is not visible from test classes");
      }
      return false;
    }
    for (Type p : m.getGenericParameterTypes()) {
      if (!isVisible(p)) {
        if (Log.isLoggingOn()) {
          Log.logLine("Will not use: " + m.toGenericString());
          Log.logLine("  reason: the method has a parameter that is not visible from test classes");
        }
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether a constructor and each of its parameter types are visible.
   *
   * @param c  the constructor
   * @return true if the constructor and each parameter type are visible; false, otherwise
   */
  private boolean isVisible(Constructor<?> c) {
    if (!predicate.isVisible(c)) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + c.toGenericString());
        Log.logLine("  reason: the constructor is not visible from test classes");
      }
      return false;
    }
    for (Type p : c.getGenericParameterTypes()) {
      if (!isVisible(p)) {
        if (Log.isLoggingOn()) {
          Log.logLine("Will not use: " + c.toGenericString());
          Log.logLine(
              "  reason: the constructor has a parameter that is not visible from test classes");
        }
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether a {@code java.lang.reflect.Type} is a type visible to the generated tests.
   *
   * @param type  the type to check
   * @return true if the type is visible, false otherwise
   */
  private boolean isVisible(Type type) {
    if (type instanceof GenericArrayType) {
      return isVisible(((GenericArrayType) type).getGenericComponentType());
    } else if (type instanceof ParameterizedType) {
      if (!isVisible(((ParameterizedType) type).getRawType())) {
        return false;
      }
      for (Type argType : ((ParameterizedType) type).getActualTypeArguments()) {
        if (!isVisible(argType)) {
          return false;
        }
      }
      return true;
    } else if (type instanceof TypeVariable) {
      return true;
    } else if (type instanceof WildcardType) {
      return true;
    }
    // if type is none of the types above then must be Class<?>, which predicate can handle
    Class<?> rawType = (Class<?>) type;
    return predicate.isVisible(rawType);
  }

  /**
   * Creates a sorted list from an array of elements using the given classComparator.
   *
   * @param array  the array of elements to be sorted
   * @param comparator  the classComparator over the element type
   * @param <T>  the element type
   * @return the sorted list of elements of the given array
   */
  private <T> List<T> toSortedList(T[] array, Comparator<T> comparator) {
    List<T> list = new ArrayList<>();
    Collections.addAll(list, array);
    Collections.sort(list, comparator);
    return list;
  }

  /**
   * The classComparator for class objects.
   * Compares by name.
   */
  private class ClassComparator implements Comparator<Class<?>> {

    @Override
    public int compare(Class<?> c1, Class<?> c2) {
      return c1.getName().compareTo(c2.getName());
    }
  }

  /**
   * The classComparator for methods of a class.
   * Orders by signature: compares names, number of parameters, and parameter type names.
   */
  private class MethodComparator implements Comparator<Method> {

    private ClassComparator classComparator;

    MethodComparator() {
      this.classComparator = new ClassComparator();
    }

    @Override
    public int compare(Method m1, Method m2) {
      int result = classComparator.compare(m1.getDeclaringClass(), m2.getDeclaringClass());
      if (result != 0) {
        return result;
      }
      result = m1.getName().compareTo(m2.getName());
      if (result != 0) {
        return result;
      }
      result = m1.getParameterTypes().length - m2.getParameterTypes().length;
      for (int i = 0; i < m1.getParameterTypes().length && result == 0; i++) {
        result = classComparator.compare(m1.getParameterTypes()[i], m2.getParameterTypes()[i]);
      }
      return result;
    }
  }

  /**
   * The classComparator for constructors of a class.
   * Orders by signature: number of parameters, and then parameter type names.
   */
  private class ConstructorComparator implements Comparator<Constructor<?>> {

    private ClassComparator classComparator;

    ConstructorComparator() {
      this.classComparator = new ClassComparator();
    }

    @Override
    public int compare(Constructor<?> c1, Constructor<?> c2) {
      int result = classComparator.compare(c1.getDeclaringClass(), c2.getDeclaringClass());
      if (result != 0) {
        return result;
      }
      result = c1.getParameterTypes().length - c2.getParameterTypes().length;
      for (int i = 0; i < c1.getParameterTypes().length && result == 0; i++) {
        result = classComparator.compare(c1.getParameterTypes()[i], c2.getParameterTypes()[i]);
      }
      return result;
    }
  }

  /**
   * The classComparator for field members of a class.
   * Compares by name.
   */
  private class FieldComparator implements Comparator<Field> {

    private ClassComparator classComparator;

    FieldComparator() {
      this.classComparator = new ClassComparator();
    }

    @Override
    public int compare(Field f1, Field f2) {
      int result = classComparator.compare(f1.getDeclaringClass(), f2.getDeclaringClass());
      if (result != 0) {
        return result;
      }
      return f1.getName().compareTo(f2.getName());
    }
  }
}
