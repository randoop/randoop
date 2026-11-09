package randoop.reflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericSignatureFormatError;
import java.lang.reflect.MalformedParameterizedTypeException;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;
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
 * call {@code myObject.toString()} might dispatch to {@code my.package.MyClass.toString()}).
 *
 * <p>A predicate built from omit patterns alone does not omit overridden methods, because it cannot
 * determine which methods its patterns match without a set of classes to examine. A client first
 * applies {@link #shouldOmit} to the operations of the classes under test, then passes the
 * operations that it omits to {@link #omittingOverriddenMethods}, which returns a new predicate
 * that also omits every method that those operations override.
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
   * Methods that this predicate omits even though no pattern matches them, because a call to them
   * might dispatch to an omitted method. Maps each such method to the classes whose instances
   * dispatch a call to the method to an omitted method. For example, if a pattern matches {@code
   * MyClass.toString()}, then this maps {@code Object.toString()} to a set that contains {@code
   * MyClass}. Neither the map nor its values are ever side-effected.
   */
  private final Map<Method, Set<Class<?>>> overriddenMethods;

  /**
   * Create a new OmitMethodsPredicate.
   *
   * @param omitPatterns a list of regular expressions for method signatures. May be empty.
   */
  public OmitMethodsPredicate(List<Pattern> omitPatterns) {
    this(omitPatterns, Collections.emptyMap());
  }

  /**
   * Create a new OmitMethodsPredicate.
   *
   * @param omitPatterns a list of regular expressions for method signatures. May be empty.
   * @param overriddenMethods methods to omit even though no pattern matches them, each mapped to
   *     the classes whose instances dispatch a call to it to an omitted method
   */
  private OmitMethodsPredicate(
      List<Pattern> omitPatterns, Map<Method, Set<Class<?>>> overriddenMethods) {
    this.omitPatterns = new ArrayList<>(omitPatterns);
    this.overriddenMethods = overriddenMethods;
  }

  /**
   * Returns a predicate that omits everything that this one does, plus every method that is
   * overridden by a method among {@code omittedOperations}. If an operation's declaring type
   * inherits the operation's method, the returned predicate also omits that method, because the
   * receiver of a call to the method might be an instance of the declaring type.
   *
   * @param omittedOperations a collection of operations that this predicate omits
   * @return a predicate that also omits the methods that {@code omittedOperations} override, or
   *     this predicate if it already omits them
   */
  public OmitMethodsPredicate omittingOverriddenMethods(
      Collection<? extends TypedOperation> omittedOperations) {
    // Group the omitted methods by receiver class, so that each class's hierarchy is examined once.
    Map<Class<?>, Set<Method>> omittedMethodsByReceiver = new LinkedHashMap<>();
    for (TypedOperation omitted : omittedOperations) {
      AccessibleObject reflectionObject = omitted.getOperation().getReflectionObject();
      if (!(reflectionObject instanceof Method)) {
        continue;
      }
      Method m = (Method) reflectionObject;
      // The operation's declaring type can be a subclass that inherits m. Search from that
      // subclass, so that methods it implements via m (such as methods of interfaces that the
      // subclass implements but m's declaring class does not) are found.
      Class<?> receiverClass = m.getDeclaringClass();
      if (omitted instanceof TypedClassOperation) {
        Class<?> declaringTypeClass =
            ((TypedClassOperation) omitted).getDeclaringType().getRuntimeClass();
        if (receiverClass.isAssignableFrom(declaringTypeClass)) {
          receiverClass = declaringTypeClass;
        }
      }
      omittedMethodsByReceiver.computeIfAbsent(receiverClass, k -> new HashSet<>()).add(m);
    }

    Map<Method, Set<Class<?>>> newOverriddenMethods = new HashMap<>();
    for (Map.Entry<Method, Set<Class<?>>> entry : overriddenMethods.entrySet()) {
      newOverriddenMethods.put(entry.getKey(), new HashSet<>(entry.getValue()));
    }
    boolean changed = false;
    for (Map.Entry<Class<?>, Set<Method>> entry : omittedMethodsByReceiver.entrySet()) {
      Class<?> receiverClass = entry.getKey();
      for (Method overridden : overriddenMethods(entry.getValue(), receiverClass)) {
        changed |=
            newOverriddenMethods
                .computeIfAbsent(overridden, k -> new HashSet<>())
                .add(receiverClass);
      }
    }
    if (!changed) {
      return this;
    }
    if (logOmit) {
      Log.logPrintf("omittingOverriddenMethods: %s%n", newOverriddenMethods);
    }
    return new OmitMethodsPredicate(omitPatterns, newOverriddenMethods);
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
    return shouldOmit(operation, null);
  }

  /**
   * Returns true if the operation is a constructor or method call that this predicate omits, when
   * the run-time class of the receiver is {@code receiverClass}. This is like {@link
   * #shouldOmit(TypedClassOperation)}, but it does not omit a call to a method that is overridden
   * by an omitted method if, on a receiver of class {@code receiverClass}, the call does not
   * dispatch to an omitted method.
   *
   * @param operation the operation for the method or constructor
   * @param receiverClass the run-time class of the receiver, or null if it is unknown; ignored if
   *     the operation is not a call to an instance method
   * @return true if this predicate omits the operation on a receiver of class {@code receiverClass}
   */
  public boolean shouldOmit(TypedClassOperation operation, @Nullable Class<?> receiverClass) {
    if (logOmit) {
      Log.logPrintf("shouldOmit: testing %s [%s]%n", operation, operation.getClass());
    }

    if (!operation.isConstructorCall() && !operation.isMethodCall()) {
      return false;
    }

    if (isOverriddenByOmittedMethod(operation, receiverClass)) {
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
   * Returns true if the operation calls a method that is overridden by a method that this predicate
   * omits (see {@link #omittingOverriddenMethods}). Does not check whether an omit pattern matches
   * the operation.
   *
   * @param operation an operation
   * @param receiverClass the run-time class of the receiver, or null if it is unknown
   * @return true if a call to the operation, on a receiver of class {@code receiverClass}, might
   *     dispatch to an omitted method that overrides the operation's method
   */
  boolean isOverriddenByOmittedMethod(
      TypedClassOperation operation, @Nullable Class<?> receiverClass) {
    if (overriddenMethods.isEmpty()) {
      return false;
    }
    AccessibleObject reflectionObject = operation.getOperation().getReflectionObject();
    if (!(reflectionObject instanceof Method)) {
      return false;
    }
    Set<Class<?>> dispatchingClasses = overriddenMethods.get(reflectionObject);
    if (dispatchingClasses == null) {
      return false;
    }
    if (receiverClass == null) {
      return true;
    }
    for (Class<?> dispatchingClass : dispatchingClasses) {
      if (dispatchingClass.isAssignableFrom(receiverClass)) {
        return true;
      }
    }
    return false;
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
   * Returns the methods whose calls, on an instance of {@code receiverClass}, might dispatch to one
   * of the given methods: the methods that they override or implement as members of {@code
   * receiverClass}, plus each of the given methods that {@code receiverClass} inherits rather than
   * declares.
   *
   * @param methods methods of {@code receiverClass}, each declared in it or inherited by it
   * @param receiverClass a class
   * @return the methods whose calls, on an instance of {@code receiverClass}, might dispatch to one
   *     of {@code methods}
   */
  private static Set<Method> overriddenMethods(Collection<Method> methods, Class<?> receiverClass) {
    Set<Method> result = new HashSet<>();
    // The given methods that can override another method, indexed by name.
    Map<String, List<Method>> methodsByName = new HashMap<>();
    for (Method m : methods) {
      int mods = m.getModifiers();
      if (Modifier.isStatic(mods) || Modifier.isPrivate(mods)) {
        continue;
      }
      if (m.getDeclaringClass() != receiverClass) {
        // receiverClass inherits m, so the receiver of a call to m might be an instance of
        // receiverClass.
        result.add(m);
      }
      methodsByName.computeIfAbsent(m.getName(), k -> new ArrayList<>()).add(m);
    }
    if (methodsByName.isEmpty()) {
      return result;
    }

    // The type arguments are computed lazily, because computing them is relatively expensive and
    // most supertypes declare no method with the same name and arity as a given method.
    boolean typeArgumentsComputed = false;
    // Null if some generic signature is malformed or names a type that is not present.
    @Nullable Map<TypeVariable<?>, Type> typeArguments = null;
    for (Class<?> supertype : strictSupertypes(receiverClass)) {
      for (Method candidate : supertype.getDeclaredMethods()) {
        List<Method> sameNameMethods = methodsByName.get(candidate.getName());
        if (sameNameMethods == null) {
          continue;
        }
        int candidateMods = candidate.getModifiers();
        if (Modifier.isStatic(candidateMods) || Modifier.isPrivate(candidateMods)) {
          continue;
        }
        boolean isPackagePrivate =
            !Modifier.isPublic(candidateMods) && !Modifier.isProtected(candidateMods);
        for (Method m : sameNameMethods) {
          Class<?> declaringClass = m.getDeclaringClass();
          if (supertype == declaringClass) {
            // m itself, or a bridge method; neither is overridden by m.
            continue;
          }
          if (candidate.getParameterCount() != m.getParameterCount()) {
            continue;
          }
          if (isPackagePrivate
              && !Objects.equals(declaringClass.getPackage(), supertype.getPackage())) {
            continue;
          }
          if (!typeArgumentsComputed) {
            typeArguments = supertypeTypeArguments(receiverClass);
            typeArgumentsComputed = true;
          }
          if (hasParameterTypes(candidate, m.getParameterTypes(), typeArguments)) {
            result.add(candidate);
          }
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
   * <p>If generic signature information is unavailable (because a generic signature is malformed or
   * names a type that is not present), this method conservatively returns true if {@code candidate}
   * might have the given parameter types: that is, if each of its erased parameter types is a
   * supertype of the corresponding element of {@code paramTypes}.
   *
   * @param candidate a method declared in a supertype of the subclass
   * @param paramTypes parameter types
   * @param typeArguments the type arguments of the subclass's supertypes, as returned by {@link
   *     #supertypeTypeArguments}; null if they could not be determined
   * @return true if the parameter types of {@code candidate}, as seen from the subclass, erase (or
   *     might erase) to {@code paramTypes}
   */
  private static boolean hasParameterTypes(
      Method candidate, Class<?>[] paramTypes, @Nullable Map<TypeVariable<?>, Type> typeArguments) {
    if (typeArguments == null) {
      return mightHaveParameterTypes(candidate, paramTypes);
    }
    Type[] candidateParamTypes;
    try {
      candidateParamTypes = candidate.getGenericParameterTypes();
    } catch (TypeNotPresentException
        | MalformedParameterizedTypeException
        | GenericSignatureFormatError e) {
      return mightHaveParameterTypes(candidate, paramTypes);
    }
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
   * Returns true if {@code candidate} might have the given parameter types, after substitution of
   * type arguments for its class's type parameters: that is, if each erased parameter type of
   * {@code candidate} is a supertype of the corresponding element of {@code paramTypes}. Used when
   * generic signature information is unavailable.
   *
   * @param candidate a method declared in a supertype of the subclass
   * @param paramTypes parameter types
   * @return true if the erased parameter types of {@code candidate} are supertypes of {@code
   *     paramTypes}
   */
  private static boolean mightHaveParameterTypes(Method candidate, Class<?>[] paramTypes) {
    Class<?>[] candidateParamTypes = candidate.getParameterTypes();
    if (candidateParamTypes.length != paramTypes.length) {
      return false;
    }
    for (int i = 0; i < paramTypes.length; i++) {
      if (!candidateParamTypes[i].isAssignableFrom(paramTypes[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the type arguments that the given class supplies, directly or transitively, to the type
   * parameters of its strict supertypes. For example, if class {@code Foo} implements {@code
   * Comparable<Foo>}, the result maps the type parameter {@code T} of {@code Comparable} to {@code
   * Foo}. A type argument can refer to type parameters of other classes; {@link #erasure} resolves
   * them. A supertype that is used as a raw type contributes no type arguments.
   *
   * @param c a class
   * @return a map from type parameters of the strict supertypes of {@code c} to their type
   *     arguments, or null if some generic signature is malformed or names a type that is not
   *     present
   */
  private static @Nullable Map<TypeVariable<?>, Type> supertypeTypeArguments(Class<?> c) {
    Map<TypeVariable<?>, Type> result = new HashMap<>();
    Set<Class<?>> visited = new HashSet<>();
    List<Class<?>> worklist = new ArrayList<>();
    worklist.add(c);
    while (!worklist.isEmpty()) {
      Class<?> current = worklist.remove(worklist.size() - 1);
      List<Type> genericSupertypes = new ArrayList<>();
      Type genericSuperclass;
      Type[] genericInterfaces;
      try {
        genericSuperclass = current.getGenericSuperclass();
        genericInterfaces = current.getGenericInterfaces();
      } catch (TypeNotPresentException
          | MalformedParameterizedTypeException
          | GenericSignatureFormatError e) {
        // The type arguments cannot be determined; the caller compares parameter types
        // conservatively.
        return null;
      }
      if (genericSuperclass != null) {
        genericSupertypes.add(genericSuperclass);
      }
      Collections.addAll(genericSupertypes, genericInterfaces);
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
    if (overriddenMethods.isEmpty()) {
      return "OmitMethodsPredicate: " + omitPatterns;
    }
    return "OmitMethodsPredicate: " + omitPatterns + ", overridden methods: " + overriddenMethods;
  }
}
