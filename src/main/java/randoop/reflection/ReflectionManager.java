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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.plumelib.util.ClassDeterministic;
import org.plumelib.util.CollectionsPlume;
import randoop.util.Log;

/**
 * ReflectionManager contains a set of visitors and an accessibility predicate. It applies each
 * visitor to each declaration (class, method, field) that satisfies the predicate.
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
 * <p>Note that visitors may have their own predicates, but need not check accessibility.
 */
public class ReflectionManager {

  /**
   * If true, output diagnostics to stdout rather than to a log. Useful when running unit tests,
   * which don't do logging.
   */
  boolean logToStdout = false;

  /**
   * The accessibility predicate for classes and class members.
   *
   * <p>DO NOT use this field directly (except on classes and fields)! Instead, call the methods
   * {@code isAccessible()} that are defined in this class.
   */
  private AccessibilityPredicate predicate;

  /** The visitors to apply. */
  private ArrayList<ClassVisitor> visitors;

  /**
   * Creates a manager object that uses the given predicate to determine which classes, methods, and
   * constructors should be visited. The list of visitors is initially empty.
   *
   * @param predicate the predicate to indicate whether classes and class members should be visited
   */
  public ReflectionManager(AccessibilityPredicate predicate) {
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
    logPrintf("Applying visitor %s to class %s%n", visitor, c.getName());

    boolean classIsAccessible = predicate.isAccessible(c);
    // Continue even if the class is not accessible; it might contain public static methods.
    if (!classIsAccessible) {
      logPrintf("Continuing even though class is not accessible: %s%n", c);
    }

    visitBefore(visitor, c); // perform any previsit steps

    if (c.isEnum()) { // treat enum classes differently
      applyToEnum(visitor, c);
    } else {

      try {
        logPrintf(
            "ReflectionManager.apply%n"
                + "  %s%n"
                + "  getMethods = %d%n"
                + "  getDeclaredMethods = %d%n"
                + "  visitor = %s%n",
            c,
            ClassDeterministic.getMethods(c).length,
            ClassDeterministic.getDeclaredMethods(c).length,
            visitor);
      } catch (Throwable e) {
        throw new Error(
            String.format("Problem with ReflectionManager.apply(%s, %s)", visitor, c), e);
      }

      // Methods
      // Need to call both getMethods (which returns only public methods) and also
      // getDeclaredMethods (which includes all methods declared by the class itself, but not
      // inherited ones).

      Method[] deterministicMethods = ClassDeterministic.getMethods(c);
      Set<Method> methods =
          new HashSet<>(CollectionsPlume.mapCapacity(deterministicMethods.length));
      for (Method m : deterministicMethods) {
        methods.add(m);
        if (isAccessible(m)) {
          if (classIsAccessible || Modifier.isStatic(m.getModifiers())) {
            applyTo(visitor, m);
          } else {
            logPrintln("ReflectionManager.apply: method " + m + " is in an inaccessible class");
          }
        } else {
          logPrintln("ReflectionManager.apply: method " + m + " is not accessible");
        }
      }
      logPrintf("ReflectionManager.apply done with getMethods for class %s%n", c);

      for (Method m : ClassDeterministic.getDeclaredMethods(c)) {
        // if not duplicate and satisfies predicate
        if (!methods.contains(m)) {
          if (isAccessible(m)) {
            applyTo(visitor, m);
          } else {
            logPrintln("ReflectionManager.apply: declared method " + m + " is not accessible");
          }
        }
      }
      logPrintf("ReflectionManager.apply done with getDeclaredMethods for class %s%n", c);

      // Constructors
      if (classIsAccessible) {
        for (Constructor<?> co : ClassDeterministic.getDeclaredConstructors(c)) {
          if (isAccessible(co)) {
            applyTo(visitor, co);
          }
        }
      }

      // member types
      for (Class<?> ic : ClassDeterministic.getDeclaredClasses(c)) {
        if (isAccessible(ic) && (classIsAccessible || Modifier.isStatic(c.getModifiers()))) {
          applyTo(visitor, ic);
        }
      }

      // Fields
      // The set of fields declared in class c is needed to ensure we don't
      // collect inherited fields that are shadowed by a local declaration.
      Set<String> declaredNames = new TreeSet<>();
      for (Field f : ClassDeterministic.getDeclaredFields(c)) { // for fields declared by c
        declaredNames.add(f.getName());
        if (predicate.isAccessible(f)
            && (classIsAccessible || Modifier.isStatic(f.getModifiers()))) {
          applyTo(visitor, f);
        }
      }
      for (Field f : ClassDeterministic.getFields(c)) { // for all public fields of c
        // keep a field that satisfies filter, and is not inherited and shadowed by
        // local declaration
        if (predicate.isAccessible(f)
            && (classIsAccessible || Modifier.isStatic(f.getModifiers()))
            && !declaredNames.contains(f.getName())) {
          applyTo(visitor, f);
        }
      }
    }

    visitAfter(visitor, c);
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
          Set<Method> methodSet =
              overrideMethods.computeIfAbsent(m.getName(), __ -> new LinkedHashSet<>());
          methodSet.add(m);
        }
      }
    }
    // get methods that are explicitly declared in the enum
    for (Method m : ClassDeterministic.getDeclaredMethods(c)) {
      if (isAccessible(m)) {
        if (!m.getName().equals("values") && !m.getName().equals("valueOf")) {
          applyTo(visitor, m);
        }
      }
    }
    // get any inherited methods also declared in anonymous class of some
    // constant
    for (Method m : ClassDeterministic.getMethods(c)) {
      if (isAccessible(m)) {
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
    logPrintf("Visiting field %s%n", f.toGenericString());
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
    logPrintf("Visiting member class %s%n", c.toString());
    v.visit(c, this);
  }

  /**
   * Apply a visitor to a constructor.
   *
   * @param v the {@link ClassVisitor}
   * @param co the constructor to be visited
   */
  private void applyTo(ClassVisitor v, Constructor<?> co) {
    logPrintf("Visiting constructor %s%n", co.toGenericString());
    v.visit(co);
  }

  /**
   * Apply a visitor to a method.
   *
   * @param v the {@link ClassVisitor}
   * @param m the method to be visited
   */
  private void applyTo(ClassVisitor v, Method m) {
    logPrintf("ReflectionManager visiting method %s, visitor=%s%n", m.toGenericString(), v);
    v.visit(m);
  }

  /**
   * Apply a visitor to a enum value.
   *
   * @param v the {@link ClassVisitor}
   * @param e the enum value to be visited
   */
  private void applyTo(ClassVisitor v, Enum<?> e) {
    logPrintf("Visiting enum %s%n", e);
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
   * Determines whether a method, its parameter types, and its return type are all accessible.
   *
   * @param m the method to check for accessibility
   * @return true if the method, each parameter type, and the return type are all accessible; and
   *     false otherwise
   */
  private boolean isAccessible(Method m) {
    if (!predicate.isAccessible(m)) {
      logPrintf("Will not use non-accessible method: %s%n", m.toGenericString());
      return false;
    }
    if (!isAccessible(m.getGenericReturnType())) {
      logPrintf("Will not use method with non-accessible return type: %s%n", m.toGenericString());
      return false;
    }
    for (Type p : m.getGenericParameterTypes()) {
      if (!isAccessible(p)) {
        logPrintf(
            "Will not use method with non-accessible parameter %s: %s%n", p, m.toGenericString());
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether a constructor and each of its parameter types are accessible.
   *
   * @param c the constructor
   * @return true if the constructor and each parameter type are accessible; false, otherwise
   */
  private boolean isAccessible(Constructor<?> c) {
    if (!predicate.isAccessible(c)) {
      logPrintf("Will not use non-accessible constructor: %s%n", c.toGenericString());
      return false;
    }
    for (Type p : c.getGenericParameterTypes()) {
      if (!isAccessible(p)) {
        logPrintf(
            "Will not use constructor with non-accessible parameter %s: %s%n",
            p, c.toGenericString());
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether a {@code java.lang.reflect.Type} is a type accessible by to the generated
   * tests.
   *
   * @param type the type to check
   * @return true if the type is accessible, false otherwise
   */
  private boolean isAccessible(Type type) {
    if (type instanceof GenericArrayType) {
      return isAccessible(((GenericArrayType) type).getGenericComponentType());
    } else if (type instanceof ParameterizedType) {
      if (!isAccessible(((ParameterizedType) type).getRawType())) {
        return false;
      }
      for (Type argType : ((ParameterizedType) type).getActualTypeArguments()) {
        if (!isAccessible(argType)) {
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
    return predicate.isAccessible(rawType);
  }

  /**
   * Log a diagnostic message with formatting.
   *
   * @param fmt the format string
   * @param args the arguments to the format string
   */
  private void logPrintf(String fmt, Object... args) {
    if (logToStdout) {
      System.out.printf(fmt, args);
    } else {
      Log.logPrintf(fmt, args);
    }
  }

  /**
   * Log a one-line literal diagnostic message.
   *
   * @param s the message, a complete line without line terminator
   */
  private void logPrintln(String s) {
    if (logToStdout) {
      System.out.println(s);
    } else {
      Log.logPrintln(s);
    }
  }
}
