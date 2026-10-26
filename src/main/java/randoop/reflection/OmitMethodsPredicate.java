package randoop.reflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    Class<?>[] paramTypes = m.getParameterTypes();
    Map<TypeVariable<?>, Type> typeArguments = supertypeTypeArguments(declaringClass);
    for (Class<?> supertype : strictSupertypes(declaringClass)) {
      for (Method candidate : supertype.getDeclaredMethods()) {
        if (!candidate.getName().equals(m.getName())
            || candidate.getParameterCount() != paramTypes.length) {
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
        if (hasParameterTypes(candidate, paramTypes, typeArguments)) {
          result.add(candidate);
        }
      }
    }
    return result;
  }

  /**
   * Returns true if the parameter types of {@code candidate}, as seen from a subclass, erase to
   * {@code paramTypes}. For example, if class {@code Foo} implements {@code Comparable<Foo>}, then
   * from {@code Foo}, the parameter types of {@code Comparable.compareTo(T)} erase to {@code
   * [Foo]}.
   *
   * @param candidate a method declared in a supertype of the subclass
   * @param paramTypes parameter types
   * @param typeArguments the type arguments of the subclass's supertypes, as returned by {@link
   *     #supertypeTypeArguments}
   * @return true if the parameter types of {@code candidate}, as seen from the subclass, erase to
   *     {@code paramTypes}
   */
  private static boolean hasParameterTypes(
      Method candidate, Class<?>[] paramTypes, Map<TypeVariable<?>, Type> typeArguments) {
    Type[] candidateParamTypes = candidate.getGenericParameterTypes();
    if (candidateParamTypes.length != paramTypes.length) {
      // The generic parameter types can omit synthetic parameters.
      return Arrays.equals(candidate.getParameterTypes(), paramTypes);
    }
    for (int i = 0; i < paramTypes.length; i++) {
      if (!erasure(candidateParamTypes[i], typeArguments).equals(paramTypes[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the type arguments that the given class supplies, directly or transitively, to the type
   * parameters of its strict supertypes. For example, if class {@code Foo} implements {@code
   * Comparable<Foo>}, the result maps the type parameter {@code T} of {@code Comparable} to {@code
   * Foo}. A type argument may refer to type parameters of other classes; {@link #erasure} resolves
   * them. A supertype that is used as a raw type contributes no type arguments.
   *
   * @param c a class
   * @return a map from type parameters of the strict supertypes of {@code c} to their type
   *     arguments
   */
  private static Map<TypeVariable<?>, Type> supertypeTypeArguments(Class<?> c) {
    Map<TypeVariable<?>, Type> result = new HashMap<>();
    Set<Class<?>> visited = new HashSet<>();
    List<Class<?>> worklist = new ArrayList<>();
    worklist.add(c);
    while (!worklist.isEmpty()) {
      Class<?> current = worklist.remove(worklist.size() - 1);
      List<Type> genericSupertypes = new ArrayList<>();
      Type genericSuperclass = current.getGenericSuperclass();
      if (genericSuperclass != null) {
        genericSupertypes.add(genericSuperclass);
      }
      Collections.addAll(genericSupertypes, current.getGenericInterfaces());
      for (Type genericSupertype : genericSupertypes) {
        Class<?> rawSupertype;
        if (genericSupertype instanceof ParameterizedType) {
          ParameterizedType parameterized = (ParameterizedType) genericSupertype;
          rawSupertype = (Class<?>) parameterized.getRawType();
          TypeVariable<?>[] typeParameters = rawSupertype.getTypeParameters();
          Type[] actualTypeArguments = parameterized.getActualTypeArguments();
          for (int i = 0; i < typeParameters.length; i++) {
            result.put(typeParameters[i], actualTypeArguments[i]);
          }
        } else {
          rawSupertype = (Class<?>) genericSupertype;
        }
        if (visited.add(rawSupertype)) {
          worklist.add(rawSupertype);
        }
      }
    }
    return result;
  }

  /**
   * Returns the erasure of the given type, after replacing type parameters according to {@code
   * typeArguments}.
   *
   * @param t a type
   * @param typeArguments a map from type parameters to their type arguments
   * @return the erasure of {@code t}
   */
  private static Class<?> erasure(Type t, Map<TypeVariable<?>, Type> typeArguments) {
    if (t instanceof Class) {
      return (Class<?>) t;
    } else if (t instanceof ParameterizedType) {
      return (Class<?>) ((ParameterizedType) t).getRawType();
    } else if (t instanceof GenericArrayType) {
      Class<?> componentErasure =
          erasure(((GenericArrayType) t).getGenericComponentType(), typeArguments);
      return Array.newInstance(componentErasure, 0).getClass();
    } else if (t instanceof TypeVariable) {
      Type typeArgument = typeArguments.get(t);
      if (typeArgument != null) {
        return erasure(typeArgument, typeArguments);
      }
      return erasure(((TypeVariable<?>) t).getBounds()[0], typeArguments);
    } else if (t instanceof WildcardType) {
      return erasure(((WildcardType) t).getUpperBounds()[0], typeArguments);
    } else {
      throw new IllegalArgumentException("Unexpected type: " + t + " [" + t.getClass() + "]");
    }
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
