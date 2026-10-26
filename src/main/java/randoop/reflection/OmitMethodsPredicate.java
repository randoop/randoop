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
import randoop.types.ClassOrInterfaceType;
import randoop.util.Log;

/**
 * This class tests whether an operation should be omitted from the operation set, because it is
 * matched by an omit pattern or because a call to it might dispatch to an operation that is.
 *
 * <p>A pattern matches an operation representing a constructor if the pattern matches the {@link
 * RawSignature} of the operation. A pattern matches an operation representing a method if the
 * pattern matches the {@link RawSignature} of the operation in the declaring type of the operation,
 * or in any supertype of the declaring type that has a public method with the same name and
 * parameter types.
 *
 * <p>If a method m is omitted, then every method that m overrides should also be omitted, so that
 * Randoop doesn't call any method that might dispatch to m at run time. For example, if an omit
 * pattern matches {@code my.package.MyClass.toString()}, then {@code Object.toString()} should also
 * be omitted (because otherwise a variable {@code myObject} might hold a {@code MyClass}, and a
 * call {@code myObject.toString()} might dispatch to {@code my.package.MyClass.toString()}). A
 * client that has determined the omitted methods calls {@link #omittingOverriddenMethods} to obtain
 * a predicate that also omits the methods that they override.
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
   * omit pattern matches the {@link RawSignature} of the operation (for a method, in the declaring
   * type of the operation or in a supertype), or the operation is a call to a method overridden by
   * an omitted method (see {@link #omittingOverriddenMethods}).
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

    if (operation.isConstructorCall()) {
      return shouldOmitConstructor(operation);
    }

    return shouldOmitMethod(operation);
  }

  /**
   * Returns true if an omit pattern matches the raw signature of the constructor.
   *
   * @param operation the operation for the method
   * @return true if the signature of the constructor is matched by an omit pattern, false otherwise
   */
  // * @throws NoSuchMethodException if Randoop can't find the operation (this is a bug in Randoop)
  private boolean shouldOmitConstructor(TypedClassOperation operation) {
    return shouldOmitExact(operation);
  }

  /**
   * Returns true if an omit pattern matches the raw signature of the method in either the declaring
   * class of the method or a supertype.
   *
   * @param operation the operation for the method
   * @return true if the signature of the method in the current class or a superclass is matched by
   *     an omit pattern, false otherwise
   */
  // * @throws NoSuchMethodException if Randoop can't find the operation (this is a bug in Randoop)
  @SuppressWarnings("ReferenceEquality")
  private boolean shouldOmitMethod(TypedClassOperation operation) {
    if (logOmit) {
      Log.logPrintf("%nshouldOmitMethod(%s)%n", operation);
    }

    RawSignature signature = operation.getRawSignature();

    // Search the type and its supertypes that have the method.

    for (ClassOrInterfaceType type : operation.getDeclaringType().getSupertypesInclusive()) {
      if (logOmit) {
        Log.logPrintf("shouldOmit looking in %s for %s%n", type, signature.getName());
      }

      if (logOmit) {
        Log.logPrintf(
            " operation = %s%n"
                + " signature = %s%n"
                + " signature.getName() = %s%n"
                + " signature.getClassname() = %s%n"
                + " type = %s [%s]%n"
                + " type.getRuntimeClass() = %s%n"
                + " type.getRuntimeClass().getSimpleName()) = %s%n"
                + " type.getRuntimeClass().getname()) = %s%n"
                + " type.getRuntimeClass().getTypeName()) = %s%n",
            operation,
            signature,
            signature.getName(),
            signature.getClassname(),
            type,
            type.getClass(),
            type.getRuntimeClass(),
            type.getRuntimeClass().getSimpleName(),
            type.getRuntimeClass().getName(),
            type.getRuntimeClass().getTypeName());
      }

      // Try to get the method for type
      boolean exists;
      try {
        type.getRuntimeClass().getMethod(signature.getName(), signature.getParameterTypes());
        exists = true;
      } catch (NoSuchMethodException e) {
        // This is not necessarily an error (yet); it might be a constructor.
        if (logOmit) {
          Log.logPrintf(
              "no method %s in %stype %s%n",
              signature,
              (type == operation.getDeclaringType()) ? "" : "super",
              type.getRuntimeClass().getSimpleName());
        }
        exists = false;
      }

      // If type has the method or constructor
      if (exists) {
        // Create the operation and test whether it is matched by an omit pattern
        TypedClassOperation superTypeOperation = operation.getOperationForType(type);
        if (shouldOmitExact(superTypeOperation)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Returns true if the operation is a constructor or method call and some omit pattern matches the
   * {@link RawSignature} of the operation, in the operation's class.
   *
   * <p>This method does not check for matches of overridden definitions of the operation in
   * superclasses.
   *
   * @param operation the operation to be matched against the omit patterns of this predicate
   * @return true if the signature matches an omit pattern, and false otherwise
   */
  // TODO: Choose a better name for this helper method, that reflects its semantics.
  private boolean shouldOmitExact(TypedClassOperation operation) {
    if (logOmit) {
      Log.logPrintf("shouldOmitExact(%s)%n", operation);
    }

    if (!operation.isConstructorCall() && !operation.isMethodCall()) {
      throw new IllegalArgumentException(
          String.format("operation = %s [%s]", operation, operation.getClass()));
    }

    if (omitPatterns.isEmpty()) {
      return false;
    }

    String signature = operation.getRawSignature().toString();

    for (Pattern pattern : omitPatterns) {
      boolean result = pattern.matcher(signature).find();
      if (logOmit) {
        Log.logPrintf(
            "shouldOmitExact(%s): \"%s\".matches(%s) => %s%n",
            operation, pattern, signature, result);
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
