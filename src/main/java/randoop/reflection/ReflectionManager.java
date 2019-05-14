package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.plumelib.util.ClassDeterministic;
import randoop.util.Log;

/**
 * ReflectionManager contains a set of visitors and a visibility predicate. It applies each visitor
 * to each declaration (class, method, field) that satisfies the predicate.
 *
 * <p>For a non-enum class, visits:
 *
 * <ul>
 *   <li>all methods satisfying predicate.
 *   <li>all constructors satisfying predicate.
 *   <li>all fields that satisfy predicate and are not shadowed. (A shadowed field is a member of a
 *       superclass with the same name as a field in the current class. These are accessible via
 *       reflection.)
 *   <li>inner enums satisfying predicate.
 * </ul>
 *
 * <p>For an enum, visits:
 *
 * <ul>
 *   <li>all enum constants.
 *   <li>methods of the enum satisfying predicate other than {@code values} and {@code valueOf}.
 *   <li>methods defined for enum constants that satisfy predicate.
 * </ul>
 *
 * <p>Note that visitors may have their own predicates, but need not check visibility.
 */
public class ReflectionManager {

  /**
   * The visibility predicate for classes and class members.
   *
   * <p>DO NOT use this field directly (except on classes and fields)! Instead, call the methods
   * {@code isVisible()} that are defined in this class.
   */
  private VisibilityPredicate predicate;

  /** The visitors to apply. */
  private ArrayList<ClassVisitor> visitors;

  /**
   * Creates a manager object that uses the given predicate to determine which classes, methods, and
   * constructors should be visited. The list of visitors is initially empty.
   *
   * @param predicate the predicate to indicate whether classes and class members should be visited
   */
  public ReflectionManager(VisibilityPredicate predicate) {
    this.predicate = predicate;
    this.visitors = new ArrayList<>();
  }

  /**
   * Registers a {@link ClassVisitor} for use by the {@link ReflectionManager#apply(Class)} method.
   *
   * @param visitor the {@link ClassVisitor} object to add
   */
  public void add(ClassVisitor visitor) {
    visitors.add(visitor);
  }

  /**
   * Applies the registered {@link ClassVisitor} objects of this object to the given class and its
   * members that satisfy the given predicate. Excludes fields that are shadowed by inheritance that
   * are otherwise still accessible by reflection. Each visitor is applied to each member at most
   * once.
   *
   * @param c the {@link Class} object to be visited
   */
  public void apply(Class<?> c) {
    for (ClassVisitor visitor : visitors) {
      apply(visitor, c);
    }
  }

  /**
   * Applies the given {@link ClassVisitor} visitor to the class object and its members that satisfy
   * the predicate of this reflection manager.
   *
   * <p>Sorts class members to ensure a canonical order for visits.
   *
   * @param visitor the {@link ClassVisitor} to apply to the class
   * @param c the class
   */
  public void apply(ClassVisitor visitor, Class<?> c) {
    if (predicate.isVisible(c)) {
      Log.logPrintf("Applying visitors to class %s%n", c.getName());

      visitBefore(visitor, c); // perform any previsit steps

      if (c.isEnum()) { // treat enum classes differently
        applyToEnum(visitor, c);
      } else {

        Log.logPrintf(
            "ReflectionManager.apply%n  %s%n  getMethods => %d%n  getDeclaredMethods => %d%n",
            c,
            ClassDeterministic.getMethods(c).length,
            ClassDeterministic.getDeclaredMethods(c).length);

        // Methods
        // Need to call both getMethods (which returns only public methods) and also
        // getDeclaredMethods (which includes all methods declared by the class itself, but not
        // inherited ones).

        Set<Method> methods = new HashSet<>();
        for (Method m : ClassDeterministic.getMethods(c)) {
          Log.logPrintf("ReflectionManager.apply considering method %s%n", m);
          methods.add(m);
          if (isVisible(m)) {
            applyTo(visitor, m);
          }
        }
        Log.logPrintf("ReflectionManager.apply done with getMethods for class %s%n", c);

        for (Method m : ClassDeterministic.getDeclaredMethods(c)) {
          Log.logPrintf("ReflectionManager.apply considering declared method %s%n", m);
          // if not duplicate and satisfies predicate
          if (!methods.contains(m) && isVisible(m)) {
            applyTo(visitor, m);
          }
        }
        Log.logPrintf("ReflectionManager.apply done with getDeclaredMethods for class %s%n", c);

        // Constructors
        for (Constructor<?> co : ClassDeterministic.getDeclaredConstructors(c)) {
          if (isVisible(co)) {
            applyTo(visitor, co);
          }
        }

        // member types
        for (Class<?> ic : ClassDeterministic.getDeclaredClasses(c)) {
          if (isVisible(ic)) {
            applyTo(visitor, ic);
          }
        }

        // Fields
        // The set of fields declared in class c is needed to ensure we don't
        // collect inherited fields that are shadowed by a local declaration.
        Set<String> declaredNames = new TreeSet<>();
        for (Field f : ClassDeterministic.getDeclaredFields(c)) { // for fields declared by c
          declaredNames.add(f.getName());
          if (predicate.isVisible(f)) {
            applyTo(visitor, f);
          }
        }
        for (Field f : ClassDeterministic.getFields(c)) { // for all public fields of c
          // keep a field that satisfies filter, and is not inherited and shadowed by
          // local declaration
          if (predicate.isVisible(f) && !declaredNames.contains(f.getName())) {
            applyTo(visitor, f);
          }
        }
      }

      visitAfter(visitor, c);
    }
  }

  /**
   * Applies the visitors to the constants and methods of the given enum. A method is included if it
   * satisfies the predicate, and either is declared in the enum, or in the anonymous class of some
   * constant. Note that methods will either belong to the enum itself, or to an anonymous class
   * attached to a constant. Ordinarily, the type of the constant is the enum, but when there is an
   * anonymous class for constant e, e.getClass() returns the anonymous class. This is used to check
   * for method overrides (that could include Object methods) within the constant.
   *
   * <p>Heuristically exclude methods {@code values} and {@code valueOf} since their definition is
   * implicit, and we aren't testing Java enum implementation.
   *
   * @param visitor the {@link ClassVisitor}
   * @param c the enum class object from which constants and methods are extracted
   */
  @SuppressWarnings("GetClassOnEnum")
  private void applyToEnum(ClassVisitor visitor, Class<?> c) {
    // Maps from a name to a set of methods.
    Map<String, Set<Method>> overrideMethods = new HashMap<>();
    for (Object obj : c.getEnumConstants()) {
      Enum<?> e = (Enum<?>) obj;
      applyTo(visitor, e);
      if (!e.getClass().equals(c)) { // does constant have an anonymous class?
        for (Method m : e.getClass().getDeclaredMethods()) {
          Set<Method> methodSet = overrideMethods.get(m.getName());
          if (methodSet == null) {
            methodSet = new LinkedHashSet<>();
          }
          methodSet.add(m);
          overrideMethods.put(m.getName(), methodSet); // collect any potential overrides
        }
      }
    }
    // get methods that are explicitly declared in the enum
    for (Method m : ClassDeterministic.getDeclaredMethods(c)) {
      if (isVisible(m)) {
        if (!m.getName().equals("values") && !m.getName().equals("valueOf")) {
          applyTo(visitor, m);
        }
      }
    }
    // get any inherited methods also declared in anonymous class of some
    // constant
    for (Method m : ClassDeterministic.getMethods(c)) {
      if (isVisible(m)) {
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
   * @param v the {@link ClassVisitor}
   * @param f the field to be visited
   */
  private void applyTo(ClassVisitor v, Field f) {
    Log.logPrintf("Visiting field %s%n", f.toGenericString());
    v.visit(f);
  }

  /**
   * Apply a visitor to the member class.
   *
   * <p>The {@link ReflectionManager} does not apply itself to the member class, since it could
   * violate assumptions in the visitor. And, so instead allows the visitor to implement that call
   * if it is necessary.
   *
   * @param v the {@link ClassVisitor}
   * @param c the member class to be visited
   */
  private void applyTo(ClassVisitor v, Class<?> c) {
    Log.logPrintf("Visiting member class %s%n", c.toString());
    v.visit(c, this);
  }

  /**
   * Apply a visitor to a constructor.
   *
   * @param v the {@link ClassVisitor}
   * @param co the constructor to be visited
   */
  private void applyTo(ClassVisitor v, Constructor<?> co) {
    Log.logPrintf("Visiting constructor %s%n", co.toGenericString());
    v.visit(co);
  }

  /**
   * Apply a visitor to a method.
   *
   * @param v the {@link ClassVisitor}
   * @param m the method to be visited
   */
  private void applyTo(ClassVisitor v, Method m) {
    Log.logPrintf("ReflectionManager visiting method %s%n", m.toGenericString());
    v.visit(m);
  }

  /**
   * Apply a visitor to a enum value.
   *
   * @param v the {@link ClassVisitor}
   * @param e the enum value to be visited
   */
  private void applyTo(ClassVisitor v, Enum<?> e) {
    Log.logPrintf("Visiting enum %s%n", e);
    v.visit(e);
  }

  /**
   * Apply a visitor to a class. Called at the beginning of {@link #apply(Class)}.
   *
   * @param v the {@link ClassVisitor}
   * @param c the class to be visited
   */
  private void visitBefore(ClassVisitor v, Class<?> c) {
    v.visitBefore(c);
  }

  /**
   * Apply a visitor to a class. Called at the end of {@link #apply(Class)}.
   *
   * @param v the {@link ClassVisitor}
   * @param c the class to be visited
   */
  private void visitAfter(ClassVisitor v, Class<?> c) {
    v.visitAfter(c);
  }

  /**
   * Determines whether a method, its parameter types, and its return type are all visible.
   *
   * @param m the method to check for visibility
   * @return true if the method, each parameter type, and the return type are all visible; and false
   *     otherwise
   */
  private boolean isVisible(Method m) {
    if (!predicate.isVisible(m)) {
      Log.logPrintf("Will not use non-visible method: %s%n", m.toGenericString());
      return false;
    }
    if (!isVisible(m.getGenericReturnType())) {
      Log.logPrintf("Will not use method with non-visible return type: %s%n", m.toGenericString());
      return false;
    }
    for (Type p : m.getGenericParameterTypes()) {
      if (!isVisible(p)) {
        Log.logPrintf(
            "Will not use method with non-visible parameter %s: %s%n", p, m.toGenericString());
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether a constructor and each of its parameter types are visible.
   *
   * @param c the constructor
   * @return true if the constructor and each parameter type are visible; false, otherwise
   */
  private boolean isVisible(Constructor<?> c) {
    if (!predicate.isVisible(c)) {
      Log.logPrintf("Will not use non-visible constructor: %s%n", c.toGenericString());
      return false;
    }
    for (Type p : c.getGenericParameterTypes()) {
      if (!isVisible(p)) {
        Log.logPrintf(
            "Will not use constructor with non-visible parameter %s: %s%n", p, c.toGenericString());
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether a {@code java.lang.reflect.Type} is a type visible to the generated tests.
   *
   * @param type the type to check
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
}
