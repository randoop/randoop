package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
   *          be visited.
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
    if (predicate.isVisible(c)) {
      if (Log.isLoggingOn()) Log.logLine("Applying visitors to class " + c.getName());

      visitBefore(c); // perform any previsit steps

      if (c.isEnum()) { // treat enum classes differently
        applyToEnum(c);
      } else {

        // Methods
        Set<Method> methods = new HashSet<>();
        for (Method m : c.getMethods()) { // for all public methods
          methods.add(m); // remember to avoid duplicates
          if (isVisible(m)) { // if satisfies predicate then visit
            applyTo(m);
          }
        }
        for (Method m : c.getDeclaredMethods()) { // for all methods declared by c
          // if not duplicate and satisfies predicate
          if ((!methods.contains(m)) && predicate.isVisible(m)) {
            applyTo(m);
          }
        }

        // Constructors
        for (Constructor<?> co : c.getDeclaredConstructors()) {
          if (isVisible(co)) {
            applyTo(co);
          }
        }

        // Inner enums
        for (Class<?> ic : c.getDeclaredClasses()) { // look for inner enums
          if (predicate.isVisible(ic)){
            if (ic.isEnum()) {
              visitBefore(ic);
              applyToEnum(ic);
              visitAfter(ic);
            }
          }
        }

        // Fields
        // The set of fields declared in class c is needed to ensure we don't
        // collect inherited fields that are hidden by local declaration
        Set<String> declaredNames = new TreeSet<>();
        for (Field f : c.getDeclaredFields()) { // for fields declared by c
          declaredNames.add(f.getName());
          if (predicate.isVisible(f)) {
            applyTo(f);
          }
        }
        for (Field f : c.getFields()) { // for all public fields of c
          // keep a field that satisfies filter, and is not inherited and hidden by
          // local declaration
          if (predicate.isVisible(f) && (!declaredNames.contains(f.getName()))) {
            applyTo(f);
          }
        }
      }

      visitAfter(c);
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
   * @param c the enum class object from which constants and methods are extracted
   */
  private void applyToEnum(Class<?> c) {
    Map<String, Set<Method>> overrideMethods = new HashMap<>();
    for (Object obj : c.getEnumConstants()) {
      Enum<?> e = (Enum<?>) obj;
      applyTo(e);
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
    for (Method m : c.getDeclaredMethods()) {
      if (predicate.isVisible(m)) {
        if (!m.getName().equals("values") && !m.getName().equals("valueOf")) {
          applyTo(m);
        }
      }
    }
    // get any inherited methods also declared in anonymous class of some
    // constant
    for (Method m : c.getMethods()) {
      if (predicate.isVisible(m)) {
        Set<Method> methodSet = overrideMethods.get(m.getName());
        if (methodSet != null) {
          for (Method method : methodSet) {
            applyTo(method);
          }
        }
      }
    }
  }

  /**
   * Apply all registered visitors to a field.
   *
   * @param f
   *          the field to be visited.
   */
  private void applyTo(Field f) {
    if (Log.isLoggingOn()) {
      Log.logLine(String.format("Considering field %s", f.toGenericString()));
    }
    for (ClassVisitor v : visitors) {
      v.visit(f);
    }
  }

  /**
   * Apply all registered visitors to the constructor.
   *
   * @param co
   *          the constructor to be visited.
   */
  private void applyTo(Constructor<?> co) {
    if (Log.isLoggingOn()) {
      Log.logLine(String.format("Considering constructor %s", co.toGenericString()));
    }
    for (ClassVisitor v : visitors) {
      v.visit(co);
    }
  }

  /**
   * Apply all registered visitors to the method.
   *
   * @param m
   *          the method to be visited.
   */
  private void applyTo(Method m) {
    if (Log.isLoggingOn()) {
      Log.logLine(String.format("Considering method %s", m.toGenericString()));
    }
    for (ClassVisitor v : visitors) {
      v.visit(m);
    }
  }

  /**
   * Apply all registered visitors to the enum value.
   *
   * @param e
   *          the enum value to be visited.
   */
  private void applyTo(Enum<?> e) {
    if (Log.isLoggingOn()) {
      Log.logLine(String.format("Considering enum %s", e));
    }
    for (ClassVisitor v : visitors) {
      v.visit(e);
    }
  }

  /**
   * Apply all registered visitors to the class. Called at the end of
   * {@link #apply(Class)}.
   *
   * @param c
   *          the class to be visited.
   */
  private void visitAfter(Class<?> c) {
    for (ClassVisitor v : visitors) {
      v.visitAfter(c);
    }
  }

  /**
   * Apply all registered visitors to the class. Called at the beginning of
   * {@link #apply(Class)}.
   *
   * @param c
   *          the class to be visited.
   */
  private void visitBefore(Class<?> c) {
    for (ClassVisitor v : visitors) {
      v.visitBefore(c);
    }
  }

  /**
   * Determines whether a method, its parameter types, and its return type are all visible.
   *
   * @param m  the method to check for visibility
   * @return true if the method, each parameter type, and the return type are all visible; and false otherwise
   */
  private boolean isVisible(Method m) {
    if (! predicate.isVisible(m)) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + m.toGenericString());
        Log.logLine("  reason: the method is not visible from test classes");
      }
      return false;
    }
    if (! predicate.isVisible(m.getReturnType())) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + m.toGenericString());
        Log.logLine("  reason: the method's return type is not visible from test classes");
      }
      return false;
    }
    for (Class<?> p : m.getParameterTypes()) {
      if (! predicate.isVisible(p)) {
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
   * Determines whether a concsturctor and each of its parameter types are visible.
   *
   * @param c  the constructor
   * @return true if the constructor and each parameter type are visible; false, otherwise
   */
  private boolean isVisible(Constructor<?> c) {
    if (! predicate.isVisible(c)) {
      if (Log.isLoggingOn()) {
        Log.logLine("Will not use: " + c.toGenericString());
        Log.logLine("  reason: the constructor is not visible from test classes");
      }
      return false;
    }
    for (Class<?> p : c.getParameterTypes()) {
      if (! predicate.isVisible(p)) {
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
}
