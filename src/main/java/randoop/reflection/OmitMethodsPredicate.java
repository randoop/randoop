package randoop.reflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.util.Log;

/**
 * This class tests whether an operation should be omitted from the operation set, because it is
 * matched by an omit pattern or because a call to it might dispatch to an operation that is.
 *
 * <p>A pattern matches an operation representing a constructor or method if the pattern matches the
 * {@link RawSignature} of the operation. The class in the signature is the class that declares the
 * constructor or method, not a class that inherits it.
 *
 * <p>If a method m is omitted, then every method that m overrides should also be omitted, so that
 * Randoop doesn't call any method that might dispatch to m at run time. For example, if an omit
 * pattern matches {@code my.package.MyClass.toString()}, then {@code Object.toString()} should also
 * be omitted (because otherwise a variable {@code myObject} might hold a {@code MyClass}, and a
 * call {@code myObject.toString()} might dispatch to {@code my.package.MyClass.toString()}). A
 * client that has determined the omitted methods calls {@link #omittingOverriddenMethods} to obtain
 * a predicate that also omits the methods that they override.
 *
 * <p>A method that overrides an omitted method is not omitted. That means that if {@code
 * MyClass.m()} is omitted and {@code MySubclass} overrides {@code m()}, Randoop will not output
 *
 * <pre>
 *   MyClass x = ...
 *   x.m()
 * </pre>
 *
 * but it might output
 *
 * <pre>
 *   MySubclass x = ...
 *   x.m()
 * </pre>
 */
public class OmitMethodsPredicate {

  /** Set to true to produce voluminous debugging regarding omission. */
  private static boolean logOmit = false;

  /** An OmitMethodsPredicate that does no omission. */
  public static final OmitMethodsPredicate NO_OMISSION =
      new OmitMethodsPredicate(new ArrayList<>(0));

  /** {@code Pattern}s to match operations that should be omitted. Never side-effected. */
  private final List<Pattern> omitPatterns;

  /**
   * Methods that are omitted because they are overridden by an omitted method, even though no
   * pattern matches them. Never side-effected.
   */
  private final Set<Method> overriddenMethods;

  /**
   * Create a new OmitMethodsPredicate.
   *
   * @param omitPatterns a list of regular expressions for method signatures. May be empty.
   */
  public OmitMethodsPredicate(List<Pattern> omitPatterns) {
    this(omitPatterns, Collections.emptySet());
  }

  /**
   * Create a new OmitMethodsPredicate.
   *
   * @param omitPatterns a list of regular expressions for method signatures. May be empty.
   * @param overriddenMethods methods to omit even though no pattern matches them
   */
  private OmitMethodsPredicate(List<Pattern> omitPatterns, Set<Method> overriddenMethods) {
    this.omitPatterns = new ArrayList<>(omitPatterns);
    this.overriddenMethods = overriddenMethods;
  }

  /**
   * Returns a predicate that omits everything that this one does, plus every method that is
   * overridden by a method among {@code omittedOperations}.
   *
   * @param omittedOperations operations that this predicate omits
   * @return a predicate that also omits the methods that {@code omittedOperations} override
   */
  public OmitMethodsPredicate omittingOverriddenMethods(
      Collection<? extends TypedOperation> omittedOperations) {
    Set<Method> newOverriddenMethods = new HashSet<>(overriddenMethods);
    for (TypedOperation omitted : omittedOperations) {
      AccessibleObject reflectionObject = omitted.getOperation().getReflectionObject();
      if (reflectionObject instanceof Method) {
        newOverriddenMethods.addAll(overriddenMethods((Method) reflectionObject));
      }
    }
    if (newOverriddenMethods.equals(overriddenMethods)) {
      return this;
    }
    if (logOmit) {
      Log.logPrintf("omittingOverriddenMethods: %s%n", newOverriddenMethods);
    }
    return new OmitMethodsPredicate(
        omitPatterns, Collections.unmodifiableSet(newOverriddenMethods));
  }

  /**
   * Returns true if the operation is a constructor or method call that this predicate omits: some
   * omit pattern matches the {@link RawSignature} of the operation, or the operation is a call to a
   * method overridden by an omitted method (see {@link #omittingOverriddenMethods}).
   *
   * @param operation the operation for the method or constructor
   * @return true if this predicate omits the operation
   */
  public boolean shouldOmit(final TypedClassOperation operation) {
    if (logOmit) {
      Log.logPrintf("shouldOmit: testing %s [%s]%n", operation, operation.getClass());
    }

    if (!operation.isConstructorCall() && !operation.isMethodCall()) {
      return false;
    }

    if (!overriddenMethods.isEmpty()
        && overriddenMethods.contains(operation.getOperation().getReflectionObject())) {
      if (logOmit) {
        Log.logPrintf("shouldOmit(%s): overridden by an omitted method%n", operation);
      }
      return true;
    }

    if (omitPatterns.isEmpty()) {
      return false;
    }

    String signature = operation.getRawSignature().toString();

    for (Pattern pattern : omitPatterns) {
      boolean result = pattern.matcher(signature).find();
      if (logOmit) {
        Log.logPrintf(
            "shouldOmit(%s): \"%s\".matches(%s) => %s%n", operation, pattern, signature, result);
      }
      if (result) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the methods that the given method overrides.
   *
   * @param m a method
   * @return the methods, declared in strict supertypes of the class that declares {@code m}, that
   *     {@code m} overrides
   */
  private static Set<Method> overriddenMethods(Method m) {
    Set<Method> result = new HashSet<>();
    int mods = m.getModifiers();
    if (Modifier.isStatic(mods) || Modifier.isPrivate(mods)) {
      return result;
    }
    Class<?> declaringClass = m.getDeclaringClass();
    for (Class<?> supertype : strictSupertypes(declaringClass)) {
      Method candidate;
      try {
        candidate = supertype.getDeclaredMethod(m.getName(), m.getParameterTypes());
      } catch (NoSuchMethodException e) {
        continue;
      }
      int candidateMods = candidate.getModifiers();
      if (Modifier.isStatic(candidateMods) || Modifier.isPrivate(candidateMods)) {
        continue;
      }
      boolean isPackagePrivate =
          !Modifier.isPublic(candidateMods) && !Modifier.isProtected(candidateMods);
      if (isPackagePrivate
          && !Objects.equals(declaringClass.getPackage(), supertype.getPackage())) {
        continue;
      }
      result.add(candidate);
    }
    return result;
  }

  /**
   * Returns all the superclasses and superinterfaces of the given class, transitively.
   *
   * @param c a class
   * @return the strict supertypes of {@code c}
   */
  private static Set<Class<?>> strictSupertypes(Class<?> c) {
    Set<Class<?>> result = new LinkedHashSet<>();
    List<Class<?>> worklist = new ArrayList<>();
    worklist.add(c);
    while (!worklist.isEmpty()) {
      Class<?> current = worklist.remove(worklist.size() - 1);
      Class<?> superclass = current.getSuperclass();
      if (superclass != null && result.add(superclass)) {
        worklist.add(superclass);
      }
      for (Class<?> iface : current.getInterfaces()) {
        if (result.add(iface)) {
          worklist.add(iface);
        }
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return "OmitMethodsPredicate: " + omitPatterns;
  }
}
