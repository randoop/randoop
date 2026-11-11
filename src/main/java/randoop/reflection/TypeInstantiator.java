package randoop.reflection;

import static org.plumelib.util.CollectionsPlume.iteratorToIterable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.CollectionsPlume;
import org.plumelib.util.CombinationIterator;
import org.plumelib.util.StringsPlume;
import randoop.operation.TypedClassOperation;
import randoop.types.BoundsCheck;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GenericClassType;
import randoop.types.InstantiatedType;
import randoop.types.JavaTypes;
import randoop.types.ParameterBound;
import randoop.types.ParameterizedType;
import randoop.types.ReferenceArgument;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeArgument;
import randoop.types.TypeVariable;
import randoop.util.Log;
import randoop.util.Randomness;

/** Instantiates type parameters from a set of input types. */
public class TypeInstantiator {

  /** If true, log information about instantiation. */
  private static final boolean debug = false;

  /**
   * The maximum number of random choices of input types for an operation's parameterized parameter
   * types that {@link #selectOperationSubstitution(TypedClassOperation)} tries before it gives up.
   */
  private static final int MAX_INSTANTIATION_ATTEMPTS = 10;

  /**
   * The maximum number of partial substitutions that {@link
   * #selectSubstitutionSatisfyingBounds(Map, Substitution)} checks against the bounds of type
   * variables before it gives up.
   */
  private static final int MAX_CHECKED_SUBSTITUTIONS = 1000;

  // This field is side-effected by aliases held outside the class.
  /**
   * The set of input types for this model. The input types need to be closed on supertypes: if a
   * type is in the input types, then so are all of its supertypes.
   */
  private final Set<Type> inputTypes;

  /**
   * Creates a {@link TypeInstantiator} object using the given types to construct instantiating
   * substitutions.
   *
   * @param inputTypes the ground types for instantiations
   */
  public TypeInstantiator(Set<Type> inputTypes) {
    // No defensive copy:  the value changes with time, but this class doesn't change it.
    this.inputTypes = inputTypes;
  }

  /**
   * Instantiate the given operation by choosing type arguments for its type parameters.
   *
   * @param operation the generic operation to instantiate
   * @return an instantiated version of the operation, or null
   */
  public @Nullable TypedClassOperation instantiate(TypedClassOperation operation) {
    assert operation.isGeneric() || operation.hasWildcardTypes()
        : "operation " + operation + " must be generic or have wildcards";

    // if declaring type of operation is generic, select instantiation
    ClassOrInterfaceType declaringType = operation.getDeclaringType();
    if (debug) {
      Log.logPrintf("instantiate(%s), isgeneric=%s%n", operation, declaringType.isGeneric());
    }
    if (declaringType.isGeneric()) {
      Substitution substitution;

      Type outputType = operation.getOutputType();

      // if operation creates objects of its declaring type, may create new instantiation
      if (operation.isConstructorCall()
          || (operation.isStatic()
              && ((InstantiatedType) outputType).getGenericClassType().equals(declaringType))) {
        int orderedIndex =
            operation.isConstructorCall()
                ? orderedTypeParameterIndex(declaringType.getRuntimeClass())
                : -1;
        if (orderedIndex != -1) {
          substitution = instantiateOrderedType(operation, orderedIndex);
        } else {
          substitution = instantiateClass(declaringType);
        }
      } else { // otherwise, select from existing one
        substitution = selectSubstitution(declaringType);
      }
      if (debug) {
        Log.logPrintf("instantiate(%s), substitution=%s%n", operation, substitution);
      }

      if (substitution == null) { // return null if fail to find instantiation
        return null;
      }
      // instantiate type parameters of declaring type
      operation = operation.substitute(substitution);
    }
    // type parameters of declaring type are instantiated

    // if necessary, do capture conversion first
    if (operation != null && operation.hasWildcardTypes()) {
      Log.logPrintf(
          "Applying capture conversion to %s%n", StringsPlume.toStringAndClass(operation));
      operation = operation.applyCaptureConversion();
      Log.logPrintf(
          "Result of capture conversion = %s%n", StringsPlume.toStringAndClass(operation));
    }
    if (operation != null) {
      operation = instantiateOperationTypes(operation);
    }

    // if operation == null failed to build instantiation
    return operation;
  }

  /**
   * Instantiate the given operation so that its output type is assignable to the given type. If
   * {@code goalType} is a parameterized type and the output type of {@code operation} is a
   * parameterized type of the same generic class, then the output type of the result is {@code
   * goalType}. For example, if {@code operation} is {@code static <T extends Marker> Box<T> of(T)}
   * and {@code goalType} is {@code Box<MarkerImpl>}, returns {@code static Box<MarkerImpl>
   * of(MarkerImpl)}. Chooses type arguments for the remaining type parameters, such as those that
   * occur only in the parameter types of {@code operation}, as {@link
   * #instantiate(TypedClassOperation)} does.
   *
   * @param operation a generic operation
   * @param goalType the type to which the output type of the result is assignable
   * @return an instantiated version of {@code operation} whose output type is assignable to {@code
   *     goalType}, or null if none was found
   */
  public @Nullable TypedClassOperation instantiate(TypedClassOperation operation, Type goalType) {
    assert operation.isGeneric() || operation.hasWildcardTypes()
        : "operation " + operation + " must be generic or have wildcards";

    Type outputType = operation.getOutputType();
    // The instantiated operation, or null if none was found.
    TypedClassOperation result;
    if (goalType instanceof InstantiatedType
        && outputType instanceof ParameterizedType
        && ((ParameterizedType) outputType)
            .getGenericClassType()
            .equals(((InstantiatedType) goalType).getGenericClassType())) {
      InstantiatedType goalInstantiatedType = (InstantiatedType) goalType;
      Substitution substitution =
          outputSubstitution((ParameterizedType) outputType, goalInstantiatedType);
      if (substitution != null
          && operation.getDeclaringType().equals(goalInstantiatedType.getGenericClassType())) {
        // A static method does not use the type parameters of its class, such as T in Box<T>.
        // Instantiate them as in the goal type, as for a constructor, so that the declaring type
        // is not generic.  Otherwise, instantiate(TypedClassOperation) would fail to instantiate
        // the declaring type.
        Substitution classSubstitution = goalInstantiatedType.getTypeSubstitution();
        substitution = classSubstitution == null ? null : substitution.extend(classSubstitution);
      }
      if (substitution == null || !satisfiesBounds(substitution)) {
        return null;
      }
      TypedClassOperation substituted = operation.substitute(substitution);
      result =
          substituted.isGeneric() || substituted.hasWildcardTypes()
              ? instantiate(substituted)
              : substituted;
    } else if (goalType.isAssignableFrom(outputType)) {
      result = instantiate(operation);
    } else {
      return null;
    }
    if (result == null || !goalType.isAssignableFrom(result.getOutputType())) {
      return null;
    }
    return result;
  }

  /**
   * Returns a substitution that, applied to the given output type, yields the given goal type. For
   * example, for {@code Box<T>} and {@code Box<MarkerImpl>}, returns the substitution that maps
   * {@code T} to {@code MarkerImpl}.
   *
   * @param outputType the output type of an operation
   * @param goalType a parameterized type of the same generic class as {@code outputType}
   * @return a substitution that maps {@code outputType} to {@code goalType}, or null if there is no
   *     such substitution
   */
  private static @Nullable Substitution outputSubstitution(
      ParameterizedType outputType, InstantiatedType goalType) {
    Substitution substitution;
    if (outputType instanceof GenericClassType) {
      // The output type of a constructor of a generic class, such as Box<T>.
      substitution = goalType.getTypeSubstitution();
    } else if (goalType.isInstantiationOf(outputType)) {
      substitution = goalType.getInstantiatingSubstitution(outputType);
    } else {
      return null;
    }
    if (substitution == null || !outputType.substitute(substitution).equals(goalType)) {
      return null;
    }
    return substitution;
  }

  /** Memoizes {@link #orderedTypeParameterIndex}. */
  private static final Map<Class<?>, Integer> orderedTypeParameterIndexCache = new HashMap<>();

  /**
   * Returns the index of the type parameter whose values instances of the given class keep in
   * order, or -1 if there is none. Such a type parameter is recognized by a constructor whose only
   * formal parameter is a comparator for it (see {@link #isComparatorInterface}), in the class or
   * in a generic superclass. Examples are the {@code E} in {@code TreeSet<E>} and the {@code K} in
   * {@code TreeMap<K,V>}.
   *
   * @param c a class
   * @return the index of the ordered type parameter of {@code c}, or -1
   */
  private static int orderedTypeParameterIndex(Class<?> c) {
    // Not computeIfAbsent, because the computation is recursive and would modify the map.
    Integer result = orderedTypeParameterIndexCache.get(c);
    if (result == null) {
      result = computeOrderedTypeParameterIndex(c);
      orderedTypeParameterIndexCache.put(c, result);
    }
    return result;
  }

  /**
   * Computes the result of {@link #orderedTypeParameterIndex}.
   *
   * @param c a class
   * @return the index of the ordered type parameter of {@code c}, or -1
   */
  private static int computeOrderedTypeParameterIndex(Class<?> c) {
    List<java.lang.reflect.TypeVariable<?>> classTypeParameters =
        Arrays.asList(c.getTypeParameters());
    if (classTypeParameters.isEmpty()) {
      return -1;
    }
    for (Constructor<?> constructor : c.getDeclaredConstructors()) {
      java.lang.reflect.Type[] formals = constructor.getGenericParameterTypes();
      if (formals.length != 1) {
        continue;
      }
      int index = comparedTypeParameterIndex(formals[0], classTypeParameters);
      if (index != -1) {
        return index;
      }
    }
    // A subclass of an ordered class, such as `class MySet<E> extends TreeSet<E>`, is also ordered,
    // even if it has no comparator constructor.
    java.lang.reflect.Type superclass = c.getGenericSuperclass();
    if (superclass instanceof java.lang.reflect.ParameterizedType) {
      java.lang.reflect.ParameterizedType superType =
          (java.lang.reflect.ParameterizedType) superclass;
      int superIndex = orderedTypeParameterIndex((Class<?>) superType.getRawType());
      if (superIndex != -1) {
        return classTypeParameters.indexOf(superType.getActualTypeArguments()[superIndex]);
      }
    }
    return -1;
  }

  /**
   * If the given type is a comparator interface (see {@link #isComparatorInterface}) whose type
   * argument is {@code T} or {@code ? super T}, where {@code T} is one of the given type
   * parameters, returns the index of {@code T}. Otherwise, returns -1.
   *
   * @param type a type, such as {@code Comparator<? super T>}
   * @param typeParameters the type parameters of a class
   * @return the index in {@code typeParameters} of the type that {@code type} compares, or -1
   */
  private static int comparedTypeParameterIndex(
      java.lang.reflect.Type type, List<java.lang.reflect.TypeVariable<?>> typeParameters) {
    if (!(type instanceof java.lang.reflect.ParameterizedType)) {
      return -1;
    }
    java.lang.reflect.ParameterizedType ptype = (java.lang.reflect.ParameterizedType) type;
    if (!isComparatorInterface((Class<?>) ptype.getRawType())) {
      return -1;
    }
    java.lang.reflect.Type typeArg = ptype.getActualTypeArguments()[0];
    if (typeArg instanceof WildcardType) {
      java.lang.reflect.Type[] lowerBounds = ((WildcardType) typeArg).getLowerBounds();
      if (lowerBounds.length == 1) {
        typeArg = lowerBounds[0];
      }
    }
    return typeParameters.indexOf(typeArg);
  }

  /**
   * Returns true if the given class is a comparator interface: a generic interface with one type
   * parameter and an abstract method {@code int compare(T, T)}. Examples are {@code
   * java.util.Comparator} and copies of it.
   *
   * @param c a class
   * @return true if {@code c} is a comparator interface
   */
  private static boolean isComparatorInterface(Class<?> c) {
    if (!c.isInterface() || c.getTypeParameters().length != 1) {
      return false;
    }
    try {
      Method compare = c.getMethod("compare", Object.class, Object.class);
      return compare.getReturnType() == int.class && Modifier.isAbstract(compare.getModifiers());
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  /**
   * If values of the given type carry their own ordering, returns the index of the type parameter
   * that they order; otherwise returns -1. A type carries its own ordering if it is generic and has
   * a method {@code comparator()} that returns a comparator interface. Examples are {@code
   * SortedSet<E>}, which orders {@code E} (index 0), and {@code SortedMap<K,V>}, which orders
   * {@code K} (index 0).
   *
   * @param type a type
   * @return the index of the type parameter that {@code type}'s {@code comparator()} method
   *     compares, or -1
   */
  private static int sortedTypeParameterIndex(Type type) {
    if (!type.isParameterized()) {
      return -1;
    }
    Class<?> c = type.getRuntimeClass();
    Method comparator;
    try {
      comparator = c.getMethod("comparator");
    } catch (NoSuchMethodException e) {
      return -1;
    }
    if (!isComparatorInterface(comparator.getReturnType())) {
      return -1;
    }
    int index =
        comparedTypeParameterIndex(
            comparator.getGenericReturnType(), Arrays.asList(c.getTypeParameters()));
    if (index != -1) {
      return index;
    }
    // The comparator() method may be inherited, so its return type refers to a type parameter of a
    // superclass.  Use the constructors to determine the ordered type parameter.
    return orderedTypeParameterIndex(c);
  }

  /**
   * Returns a substitution that instantiates the declaring type of the given constructor, for a
   * class that keeps the values of one of its type parameters, {@code T}, in order. Such a class
   * orders {@code T} either explicitly with a comparator, or by requiring that {@code T implements
   * Comparable<T>}.
   *
   * @param operation a constructor to be instantiated
   * @param orderedIndex the index of the ordered type parameter {@code T}; see {@link
   *     #orderedTypeParameterIndex}
   * @return the substitution to instantiate the declaring type of the constructor, or null if none
   *     is found
   */
  private @Nullable Substitution instantiateOrderedType(
      TypedClassOperation operation, int orderedIndex) {
    assert operation.isConstructorCall() : "only call with constructors: " + operation;

    ClassOrInterfaceType declaringType = operation.getDeclaringType();
    // For a member class of a generic class, typeParameters starts with the type parameters of the
    // enclosing classes, and ends with those of the member class, which orderedIndex indexes.
    List<TypeVariable> typeParameters = declaringType.getTypeParameters();
    int ownParameterCount = declaringType.getRuntimeClass().getTypeParameters().length;
    TypeVariable orderedParameter =
        typeParameters.get(typeParameters.size() - ownParameterCount + orderedIndex);

    // If this constructor has an argument that orders T, such as a Comparator<T> or a
    // SortedSet<T>, then orderingType is the generic class of that argument, and orderingIndex is
    // the index of its type parameter that corresponds to T.
    GenericClassType orderingType = null;
    int orderingIndex = -1;
    for (Type inputType : operation.getInputTypes()) {
      if (!inputType.isParameterized()) {
        continue;
      }
      int index =
          isComparatorInterface(inputType.getRuntimeClass())
              ? 0
              : sortedTypeParameterIndex(inputType);
      if (index != -1) {
        orderingType = ((ParameterizedType) inputType).getGenericClassType();
        orderingIndex = index;
        break;
      }
    }

    // By default, choose a type T that is Comparable<T>.  If there is an ordering argument, then
    // half the time, choose a type T that has such a value.  (Not always, because the argument
    // might be null or might use the natural ordering of T, as Collections.reverseOrder() does.)
    // If the first choice fails, try the other.
    boolean useOrderingType = orderingType != null && Randomness.weightedCoinFlip(0.5);
    Substitution substitution =
        useOrderingType
            ? selectSubstitutionForOrderedType(orderingType, orderingIndex, orderedParameter)
            : selectComparableSubstitution(orderedParameter);
    if (substitution == null) {
      if (useOrderingType) {
        substitution = selectComparableSubstitution(orderedParameter);
      } else if (orderingType != null) {
        substitution =
            selectSubstitutionForOrderedType(orderingType, orderingIndex, orderedParameter);
      }
    }
    if (substitution == null) {
      return null;
    }
    List<TypeVariable> otherParameters = new ArrayList<>(typeParameters);
    otherParameters.remove(orderedParameter);
    if (otherParameters.isEmpty()) {
      return substitution;
    }
    return selectSubstitution(otherParameters, substitution);
  }

  /**
   * Select a substitution for an ordered type parameter, from an existing instantiation of a type
   * that orders it.
   *
   * @param searchType a generic type that orders one of its type arguments, such as Comparator,
   *     SortedSet, or SortedMap
   * @param searchIndex the index of the type argument of {@code searchType} that it orders
   * @param typeParameter the type parameter to create an instantiation for
   * @return a substitution for {@code typeParameter}, or null if none is found
   */
  private @Nullable Substitution selectSubstitutionForOrderedType(
      GenericClassType searchType, int searchIndex, TypeVariable typeParameter) {
    // Select a substitution for searchType's formal parameters.
    Substitution substitution = selectSubstitution(searchType);
    if (substitution == null) {
      return null;
    }
    // Convert it into a substitution for the given type parameter.
    TypeArgument argumentType =
        searchType.substitute(substitution).getTypeArguments().get(searchIndex);
    if (!(argumentType instanceof ReferenceArgument)) {
      return null;
    }
    ReferenceType selected = ((ReferenceArgument) argumentType).getReferenceType();
    Substitution result = new Substitution(typeParameter, selected);
    // Check the bounds of typeParameter as candidateTypes does, ignoring bounds that mention other
    // type variables.
    if (!getLowerBound(typeParameter).isLowerBound(selected, result)
        || !getUpperBound(typeParameter).isUpperBound(selected, result)) {
      return null;
    }
    return result;
  }

  /**
   * Select a substitution for an ordered type parameter that uses the natural ordering: that is,
   * one that maps the type parameter to an input type {@code T} that is a subtype of {@code
   * Comparable<? super T>} (see {@link #isComparable}). For example, if the input types contain
   * {@code Comparable<Number>} but not a {@code Number} subtype that implements it, then this does
   * not choose {@code Number}, because not every {@code Number} is comparable.
   *
   * @param typeParameter the type parameter to create an instantiation for
   * @return a substitution for {@code typeParameter}, or null if none is found
   */
  private @Nullable Substitution selectComparableSubstitution(TypeVariable typeParameter) {
    List<ReferenceType> candidates =
        CollectionsPlume.filter(candidateTypes(typeParameter), TypeInstantiator::isComparable);
    if (candidates.isEmpty()) {
      return null;
    }
    return new Substitution(typeParameter, Randomness.randomMember(candidates));
  }

  /**
   * Chooses an instantiating substitution for the given class. Performs a coin flip to determine
   * whether to use a previous instantiation, or to create a new one. Verifies that all of the type
   * parameters of the type are instantiated, and logs failure if not.
   *
   * @param type the type to be instantiated
   * @return a substitution instantiating the given type; null if none is found
   */
  private @Nullable Substitution instantiateClass(ClassOrInterfaceType type) {
    boolean tryPrevious = Randomness.weightedCoinFlip(0.5);

    if (tryPrevious) {
      Substitution substitution = selectSubstitution(type);
      if (substitution != null) {
        return substitution;
      }
    }

    {
      List<TypeVariable> typeParameters = type.getTypeParameters();
      Substitution substitution = selectSubstitution(typeParameters);
      if (substitution != null) {
        ClassOrInterfaceType instantiatedType = type.substitute(substitution);
        if (!instantiatedType.isGeneric()) {
          return substitution;
        } else {
          Log.logPrintf("Didn't find types to satisfy bounds on generic type: %s%n", type);
        }
      }
    }

    if (!tryPrevious) {
      // We didn't already try an existing instantiation, and creating a new one failed.
      // So try an existing one now.
      Substitution substitution = selectSubstitution(type);
      if (substitution != null) {
        return substitution;
      }
    }

    return null;
  }

  /**
   * Selects an existing type that instantiates the given generic declaring type and returns the
   * instantiating substitution.
   *
   * @param type the generic type for which an instantiation is to be found
   * @return a substitution instantiating the given type as an existing type; null if no such type
   */
  private @Nullable Substitution selectSubstitution(ClassOrInterfaceType type) {
    List<ReferenceType> matches = matchingInputTypes(type);
    if (matches.isEmpty()) {
      return null;
    }
    ReferenceType selectedType = Randomness.randomSetMember(matches);
    return selectedType.getInstantiatingSubstitution(type);
  }

  /**
   * Returns the parameterized input types that instantiate the given type.
   *
   * @param patternType a type
   * @return the parameterized input types that are instantiations of {@code patternType}
   */
  private List<ReferenceType> matchingInputTypes(ClassOrInterfaceType patternType) {
    List<ReferenceType> matches = new ArrayList<>(inputTypes.size());
    for (Type inputType : inputTypes) {
      if (inputType.isParameterized()
          && ((ReferenceType) inputType).isInstantiationOf(patternType)) {
        matches.add((ReferenceType) inputType);
      }
    }
    return matches;
  }

  /**
   * Selects an instantiation of the generic types of an operation, and returns a new operation with
   * the types instantiated.
   *
   * @param operation an operation
   * @return the operation with generic types instantiated, or null if no instantiation was found
   */
  private @Nullable TypedClassOperation instantiateOperationTypes(TypedClassOperation operation) {
    Substitution substitution = selectOperationSubstitution(operation);
    if (substitution == null) {
      return null;
    }
    operation = operation.substitute(substitution);
    // An operation is generic if it has type variables.  This seems to assume that substitution
    // failed, because it left some type variables unreplaced.  Should that be an error?
    if (operation.isGeneric(/* ignoreWildcards= */ true)) {
      return null;
    }
    return operation;
  }

  /**
   * Returns a randomly selected substitution that instantiates the generic types of the given
   * operation in a way that satisfies the bounds of its type variables; see {@link
   * #satisfiesBounds(Substitution)}.
   *
   * <p>This method makes one or more attempts to build such a substitution. Each attempt makes new
   * random choices and either returns a substitution or fails. An attempt starts with an empty
   * substitution and does the following:
   *
   * <ol>
   *   <li>Instantiate the type variables in the parameterized parameter types. For each parameter
   *       type of the operation, in order, with the substitution so far applied to it:
   *       <ul>
   *         <li>If the parameter type is a generic class or interface type, such as {@code
   *             List<T>}, randomly choose an {@link #inputTypes input type} that instantiates it,
   *             such as {@code List<String>}, and extend the substitution accordingly, as by
   *             mapping {@code T} to {@code String}. If no input type instantiates the parameter
   *             type, the attempt fails.
   *         <li>If the parameter type is some other generic type, such as {@code T} or {@code T[]},
   *             defer its type variables to step 2.
   *       </ul>
   *   <li>Instantiate the remaining type variables: those in the return type and those deferred in
   *       step 1. The return type need not be an input type.
   *       <ul>
   *         <li>If no type variables remain, return the substitution if it satisfies the bounds;
   *             otherwise, the attempt fails.
   *         <li>For each remaining type variable, compute its candidate types: the input types that
   *             are within its bounds, ignoring any bound that mentions another type variable, and
   *             that are consistent with the substitution; see {@link
   *             #consistentCandidatesByVariable(List, Substitution)}. If some type variable has no
   *             candidate types, the attempt fails.
   *         <li>Because the candidate types are computed for each type variable separately, a
   *             combination of them can violate the bounds. Return a randomly selected combination
   *             of candidate types that, when added to the substitution, satisfies the bounds; see
   *             {@link #selectSubstitutionSatisfyingBounds(Map, Substitution)}. If the search for
   *             such a combination finds none, the attempt fails.
   *       </ul>
   * </ol>
   *
   * <p>If an attempt fails, and step 1 chose among several input types for some parameter type,
   * this makes another attempt, up to {@link #MAX_INSTANTIATION_ATTEMPTS} attempts in all.
   * Otherwise, another attempt would start from the same substitution, so this returns null.
   *
   * <p>A type variable that occurs only in the bounds of wildcards, such as {@code T} in {@code <T>
   * void copy(List<? super T>, List<? extends T>)}, is not instantiated. Therefore, this does not
   * check that the types chosen for those wildcards are consistent with one another.
   *
   * @param operation an operation
   * @return a substitution that instantiates the generic types of {@code operation} and satisfies
   *     the bounds of its type variables, or null if none was found
   */
  private @Nullable Substitution selectOperationSubstitution(TypedClassOperation operation) {
    // Why the most recent attempt failed; used for diagnostics.  It is computed only if logging is
    // on, because failure is common and computing it calls toString() on types and substitutions.
    String failure = "";
    for (int attempt = 0; attempt < MAX_INSTANTIATION_ATTEMPTS; attempt++) {
      // answer question: what type instantiation would allow a call to this operation?
      Set<TypeVariable> typeParameters = new LinkedHashSet<>();
      Substitution substitution = new Substitution();
      // True if an input type was chosen among several for some parameter type.
      boolean choseAmongSeveral = false;
      // True if each parameterized parameter type matched an input type.  If false, this attempt
      // fails without instantiating the remaining type variables.
      boolean allMatched = true;
      for (Type origParamType : operation.getInputTypes()) {
        Type paramType = origParamType.substitute(substitution);
        if (paramType.isGeneric()) {
          if (paramType.isClassOrInterfaceType()) {
            // TODO: This seems redundant with isInstantiationOf.  Should they be combined?
            List<ReferenceType> matches = matchingInputTypes((ParameterizedType) paramType);
            if (matches.isEmpty()) {
              if (Log.isLoggingOn()) {
                failure = "no input type instantiates " + paramType;
              }
              allMatched = false;
              break;
            }
            choseAmongSeveral |= matches.size() > 1;
            ReferenceType selectedType = Randomness.randomSetMember(matches);
            Substitution subst =
                selectedType.getInstantiatingSubstitution((ParameterizedType) origParamType);
            if (subst == null) {
              if (Log.isLoggingOn()) {
                failure = "no substitution instantiates " + origParamType + " as " + selectedType;
              }
              allMatched = false;
              break;
            }
            substitution = substitution.extend(subst);
          } else {
            typeParameters.addAll(((ReferenceType) paramType).getTypeParameters());
          }
        }
      }

      if (allMatched) {
        // return types don't have to exist, but do need to have their type parameters instantiated
        Type outputType = operation.getOutputType();
        if (outputType.isReferenceType()) {
          Type paramType = outputType.substitute(substitution);
          if (paramType.isGeneric()) {
            typeParameters.addAll(((ReferenceType) paramType).getTypeParameters());
          }
        }
        typeParameters.removeAll(substitution.keySet());

        if (typeParameters.isEmpty()) {
          // The substitution instantiates every type variable in the parameter and return types.
          if (satisfiesBounds(substitution)) {
            return substitution;
          }
          if (Log.isLoggingOn()) {
            failure = substitution + " does not satisfy the bounds";
          }
        } else {
          List<TypeVariable> parameters = new ArrayList<>(typeParameters);
          Map<TypeVariable, List<ReferenceType>> candidatesByVariable =
              consistentCandidatesByVariable(parameters, substitution);
          if (candidatesByVariable == null) {
            if (Log.isLoggingOn()) {
              failure = "some type variable in " + parameters + " has no candidate types";
            }
          } else {
            Substitution result =
                selectSubstitutionSatisfyingBounds(candidatesByVariable, substitution);
            if (result != null) {
              return result;
            }
            if (Log.isLoggingOn()) {
              failure = "no extension of " + substitution + " satisfies the bounds";
            }
          }
        }
      }

      if (!choseAmongSeveral) {
        break;
      }
    }
    Log.logPrintf("TypeInstantiator: no instantiation of %s: %s%n", operation, failure);
    return null;
  }

  /**
   * Returns a map from each of the given type variables to its candidate types that are consistent
   * with the given substitution.
   *
   * <p>A candidate type for one of the type variables, {@code X}, is in the map's value for {@code
   * X} if it is one of the {@link #candidateTypes(TypeVariable)} of {@code X}, and if, when {@code
   * substitution} is extended by mapping {@code X} to it, each type variable that {@code
   * substitution} instantiates and whose bounds contain {@code X} and no other uninstantiated type
   * variable is within its bounds. For example, for {@code <K, V> Map<K, V> unmodifiableMap(Map<?
   * extends K, ? extends V>)}, if {@code substitution} maps the capture of {@code ? extends K} to
   * {@code Integer}, then the result contains only supertypes of {@code Integer} for {@code K}.
   *
   * @param parameters type variables that {@code substitution} does not instantiate
   * @param substitution a substitution
   * @return a map, whose keys are in the order of {@code parameters}, from each element of {@code
   *     parameters} to its consistent candidate types, which are not empty; or null if some element
   *     of {@code parameters} has no consistent candidate types
   */
  private @Nullable Map<TypeVariable, List<ReferenceType>> consistentCandidatesByVariable(
      List<TypeVariable> parameters, Substitution substitution) {
    Map<TypeVariable, List<TypeVariable>> constrainedVariables = constrainedVariables(substitution);
    Map<TypeVariable, List<ReferenceType>> result = new LinkedHashMap<>();
    for (TypeVariable parameter : parameters) {
      List<TypeVariable> constrained =
          constrainedVariables.getOrDefault(parameter, Collections.emptyList());
      List<ReferenceType> candidates = candidateTypes(parameter);
      if (!constrained.isEmpty()) {
        candidates.removeIf(
            candidate ->
                !satisfiesBounds(
                    constrained, substitution.extend(new Substitution(parameter, candidate))));
      }
      if (candidates.isEmpty()) {
        Log.logPrintf("No candidate types for %s consistent with %s%n", parameter, substitution);
        return null;
      }
      result.put(parameter, candidates);
    }
    return result;
  }

  /**
   * Returns a map from each uninstantiated type variable {@code X} to the type variables that the
   * given substitution instantiates and whose bounds mention {@code X} and no other uninstantiated
   * type variable. Once {@code X} is instantiated, the bounds of those type variables can be
   * checked. For example, if {@code substitution} instantiates the capture of {@code ? extends K}
   * but not {@code K}, then the value for {@code K} contains the capture variable.
   *
   * @param substitution a substitution
   * @return a map from each uninstantiated type variable to the instantiated type variables whose
   *     bounds mention it and no other uninstantiated type variable
   */
  private static Map<TypeVariable, List<TypeVariable>> constrainedVariables(
      Substitution substitution) {
    Set<TypeVariable> instantiated = new LinkedHashSet<>(substitution.keySet());
    Map<TypeVariable, List<TypeVariable>> result = new HashMap<>();
    for (TypeVariable variable : instantiated) {
      // The uninstantiated type variables in the bounds of `variable`.
      Set<TypeVariable> boundVariables =
          new LinkedHashSet<>(variable.getLowerTypeBound().getTypeParameters());
      boundVariables.addAll(variable.getUpperTypeBound().getTypeParameters());
      boundVariables.removeAll(instantiated);
      if (boundVariables.size() == 1) {
        TypeVariable boundVariable = boundVariables.iterator().next();
        result.computeIfAbsent(boundVariable, k -> new ArrayList<>()).add(variable);
      }
    }
    return result;
  }

  /**
   * Returns a randomly selected substitution that extends the given substitution by instantiating
   * the given type variables with the given candidate types, and that satisfies the bounds of all
   * the type variables that it instantiates.
   *
   * <p>This searches depth-first, instantiating the type variables in the order of the map's keys
   * and trying each one's candidate types in a random order that favors comparable types (see
   * {@link CandidateChooser}). It checks the bounds of each type variable as soon as every type
   * variable in those bounds, and the type variable itself, is instantiated. Thus, it abandons a
   * partial substitution as soon as the partial substitution violates some bound, rather than
   * extending it to every combination of the remaining candidate types. For example, for {@code <K,
   * V extends K>}, once the search chooses {@code Integer} for {@code K}, it rejects each candidate
   * type for {@code V} that is not a subtype of {@code Integer}.
   *
   * <p>If there are at most {@link #MAX_CHECKED_SUBSTITUTIONS} combinations of candidate types, the
   * search is exhaustive: it returns null only if no combination satisfies the bounds. Otherwise,
   * the search checks at most {@link #MAX_CHECKED_SUBSTITUTIONS} partial substitutions.
   *
   * @param candidatesByVariable a map from each type variable that {@code substitution} does not
   *     instantiate, but that the result instantiates, to its candidate types, which are not empty
   * @param substitution a substitution to extend
   * @return an extending substitution that satisfies the bounds, or null if none was found
   * @see #satisfiesBounds(Substitution)
   */
  private static @Nullable Substitution selectSubstitutionSatisfyingBounds(
      Map<TypeVariable, List<ReferenceType>> candidatesByVariable, Substitution substitution) {
    // The type variables to instantiate, in the order that the search instantiates them.
    List<TypeVariable> parameters = new ArrayList<>(candidatesByVariable.keySet());
    // The type variables whose bounds can be checked before the search instantiates any of
    // `parameters`.
    List<TypeVariable> checkFirst = new ArrayList<>();
    // Element i holds the type variables whose bounds can be checked once the search instantiates
    // `parameters.get(i)`, but not earlier.
    List<List<TypeVariable>> checkAfter = new ArrayList<>(parameters.size());
    for (int i = 0; i < parameters.size(); i++) {
      checkAfter.add(new ArrayList<>());
    }
    Set<TypeVariable> variables = new LinkedHashSet<>(substitution.keySet());
    variables.addAll(parameters);
    for (TypeVariable variable : variables) {
      Set<TypeVariable> needed =
          new LinkedHashSet<>(variable.getLowerTypeBound().getTypeParameters());
      needed.addAll(variable.getUpperTypeBound().getTypeParameters());
      needed.add(variable);
      int last = -1;
      for (TypeVariable neededVariable : needed) {
        last = Math.max(last, parameters.indexOf(neededVariable));
      }
      if (last == -1) {
        checkFirst.add(variable);
      } else {
        checkAfter.get(last).add(variable);
      }
    }
    if (!satisfiesBounds(checkFirst, substitution)) {
      return null;
    }
    int maxChecks =
        combinationCount(candidatesByVariable) <= MAX_CHECKED_SUBSTITUTIONS
            ? Integer.MAX_VALUE
            : MAX_CHECKED_SUBSTITUTIONS;
    return new BoundsSearch(candidatesByVariable, checkAfter, maxChecks).search(0, substitution);
  }

  /**
   * Returns the number of combinations of the given candidate types, or {@link Long#MAX_VALUE} if
   * it is more than {@link #MAX_CHECKED_SUBSTITUTIONS}.
   *
   * @param candidatesByVariable a map from type variables to their candidate types
   * @return the number of combinations of candidate types, one per key of {@code
   *     candidatesByVariable}, or {@link Long#MAX_VALUE} if it is more than {@link
   *     #MAX_CHECKED_SUBSTITUTIONS}
   */
  private static long combinationCount(
      Map<TypeVariable, List<ReferenceType>> candidatesByVariable) {
    long count = 1;
    for (List<ReferenceType> candidates : candidatesByVariable.values()) {
      count *= candidates.size();
      if (count > MAX_CHECKED_SUBSTITUTIONS) {
        return Long.MAX_VALUE;
      }
    }
    return count;
  }

  /**
   * A depth-first search for a substitution that satisfies the bounds of the type variables that it
   * instantiates; see {@link #selectSubstitutionSatisfyingBounds(Map, Substitution)}.
   */
  private static final class BoundsSearch {

    /** The type variables to instantiate, in the order that the search instantiates them. */
    private final List<TypeVariable> parameters;

    /** Element i holds the candidate types of {@code parameters.get(i)}. */
    private final List<CandidateChooser> candidates;

    /**
     * Element i holds the type variables whose bounds can be checked once the search instantiates
     * {@code parameters.get(i)}, but not earlier.
     */
    private final List<List<TypeVariable>> checkAfter;

    /** The number of partial substitutions that the search can still check. */
    private int remainingChecks;

    /**
     * Creates a search.
     *
     * @param candidatesByVariable a map from each type variable to instantiate to its candidate
     *     types; the search instantiates the type variables in the order of the map's keys
     * @param checkAfter the type variables to check after each key of {@code candidatesByVariable}
     *     is instantiated
     * @param maxChecks the maximum number of partial substitutions that the search checks
     */
    BoundsSearch(
        Map<TypeVariable, List<ReferenceType>> candidatesByVariable,
        List<List<TypeVariable>> checkAfter,
        int maxChecks) {
      this.parameters = new ArrayList<>(candidatesByVariable.keySet());
      this.candidates = new ArrayList<>(parameters.size());
      for (List<ReferenceType> variableCandidates : candidatesByVariable.values()) {
        candidates.add(new CandidateChooser(variableCandidates));
      }
      this.checkAfter = checkAfter;
      this.remainingChecks = maxChecks;
    }

    /**
     * Returns a substitution that extends the given substitution by instantiating {@code
     * parameters.get(index)} and the parameters after it, and that satisfies the bounds checked
     * after each of them.
     *
     * @param index the index in {@link #parameters} of the next type variable to instantiate
     * @param substitution a substitution that instantiates the parameters before {@code index}
     * @return an extending substitution that satisfies the bounds, or null if none was found
     */
    @Nullable Substitution search(int index, Substitution substitution) {
      if (index == parameters.size()) {
        return substitution;
      }
      TypeVariable parameter = parameters.get(index);
      CandidateChooser untried = new CandidateChooser(candidates.get(index));
      while (!untried.isEmpty() && remainingChecks > 0) {
        ReferenceType candidate = untried.removeNext();
        remainingChecks--;
        Substitution extended = substitution.extend(new Substitution(parameter, candidate));
        if (satisfiesBounds(checkAfter.get(index), extended)) {
          Substitution result = search(index + 1, extended);
          if (result != null) {
            return result;
          }
        }
      }
      return null;
    }
  }

  /**
   * Returns true if, for each type variable {@code X} that the given substitution instantiates, the
   * substitution's type for {@code X} is within the bounds of {@code X}. The check applies the
   * substitution to the bounds, which can mention other type variables.
   *
   * <p>For example, for {@code <K, V> Map<K, V> unmodifiableMap(Map<? extends K, ? extends V>)},
   * this returns false for a substitution that maps the capture of {@code ? extends K} to {@code
   * Integer} and maps {@code K} to {@code String}.
   *
   * @param substitution a substitution
   * @return true if each type in the substitution is within the bounds of its type variable
   */
  private static boolean satisfiesBounds(Substitution substitution) {
    return satisfiesBounds(new ArrayList<>(substitution.keySet()), substitution);
  }

  /**
   * Returns true if, for each of the given type variables {@code X}, the given substitution's type
   * for {@code X} is within the bounds of {@code X}. The check applies the substitution to the
   * bounds, which can mention other type variables.
   *
   * @param parameters type variables that {@code substitution} instantiates
   * @param substitution a substitution
   * @return true if the type of each of {@code parameters} is within its bounds
   */
  @SuppressWarnings("nullness:argument") // substitution instantiates each of parameters
  private static boolean satisfiesBounds(List<TypeVariable> parameters, Substitution substitution) {
    List<ReferenceType> arguments = new ArrayList<>(parameters.size());
    for (TypeVariable parameter : parameters) {
      arguments.add(substitution.get(parameter));
    }
    return new BoundsCheck(parameters).test(arguments, substitution);
  }

  /**
   * Selects an instantiating substitution for the given list of type variables.
   *
   * @param typeParameters the type variables to be instantiated
   * @return a substitution instantiating the type variables; null if some type variable has no
   *     instantiating types
   * @see #selectSubstitution(List, Substitution)
   */
  private Substitution selectSubstitution(List<TypeVariable> typeParameters) {
    Substitution substitution = new Substitution();
    return selectSubstitution(typeParameters, substitution);
  }

  /**
   * Extends the given substitution by instantiations for the given list of type variables.
   *
   * @param typeParameters the type variables to be instantiated
   * @param substitution the substitution to extend
   * @return the substitution extended by instantiating type variables; null if any of the variables
   *     has no instantiating types
   */
  private Substitution selectSubstitution(
      List<TypeVariable> typeParameters, Substitution substitution) {
    List<Substitution> substitutionList = allExtendingSubstitutions(typeParameters, substitution);
    if (substitutionList.isEmpty()) {
      return null;
    }
    return Randomness.randomMember(substitutionList);
  }

  /**
   * Returns all substitutions that extend a substitution for the given type parameters.
   *
   * @param typeParameters the type parameters to be instantiated
   * @param substitution the substitution to be extended
   * @return the list of possible substitutions, empty if none is found
   */
  @SuppressWarnings("MixedMutabilityReturnType")
  private List<Substitution> allExtendingSubstitutions(
      List<TypeVariable> typeParameters, Substitution substitution) {

    // Partition parameters based on whether they might have independent bounds:

    // parameters with generic bounds may be dependent on other parameters
    List<TypeVariable> genericParameters = new ArrayList<>(typeParameters.size());

    // parameters with nongeneric bounds can be selected independently, but may be used by
    List<TypeVariable> nongenericParameters = new ArrayList<>(typeParameters.size());

    // wildcard capture variables without generic bounds can be selected independently, and
    // may not be used in the bounds of another parameter.
    List<TypeVariable> captureParameters = new ArrayList<>(typeParameters.size());

    for (TypeVariable variable : typeParameters) {
      if (variable.hasGenericBound()) {
        genericParameters.add(variable);
      } else {
        if (variable.isCaptureVariable()) {
          captureParameters.add(variable);
        } else {
          nongenericParameters.add(variable);
        }
      }
    }

    // If any of the type variables has a generic bound, assumes there are dependencies and
    // enumerates all possible substitutions and tests them. Otherwise, independently selects an
    // instantiating type for each variable.

    // These are the candidates that this routine will return.
    List<Substitution> result = new ArrayList<>();
    if (!genericParameters.isEmpty()) {
      // if there are type parameters with generic bounds
      if (!nongenericParameters.isEmpty()) {
        // if there are type parameters with non-generic bounds, these may be variables in
        // generic-bounded parameters

        List<List<ReferenceType>> nongenCandidates = candidateTypes(nongenericParameters);
        if (nongenCandidates.isEmpty()) {
          return Collections.emptyList();
        }
        for (List<ReferenceType> tuple :
            iteratorToIterable(new CombinationIterator<>(nongenCandidates))) {
          // choose instantiating substitution for non-generic bounded parameters
          Substitution initialSubstitution = substitution.extend(nongenericParameters, tuple);
          // apply selected substitution to all generic-bounded parameters
          List<TypeVariable> parameters = new ArrayList<>(genericParameters.size());
          for (TypeVariable variable : genericParameters) {
            ReferenceType paramType = variable.substitute(initialSubstitution);
            if (paramType.isVariable()) {
              parameters.add(variable);
            }
          }
          // choose instantiation for parameters with generic bounds
          result.addAll(allExtendingSubstitutions(parameters, initialSubstitution));
        }
      } else {
        // if no parameters with non-generic bounds, choose instantiation for parameters
        // with generic bounds
        BoundsCheck boundsCheck = new BoundsCheck(genericParameters);
        result = allSubstitutions(genericParameters, substitution, boundsCheck);
      }
      if (result.isEmpty()) {
        return result;
      }
    } else if (!nongenericParameters.isEmpty()) {
      // if there are no type parameters with generic bounds, can select others independently
      substitution = selectSubstitutionIndependently(nongenericParameters, substitution);
      if (substitution == null) {
        return Collections.emptyList();
      }
      result.add(substitution);
    }

    // Can always select captured wildcards independently.  Augment each existing candidate.
    if (!captureParameters.isEmpty()) {
      if (result.isEmpty()) {
        result.add(selectSubstitutionIndependently(captureParameters, substitution));
      } else {
        result.replaceAll(s -> selectSubstitutionIndependently(captureParameters, s));
      }
    }

    return result;
  }

  /**
   * Selects types independently for a list of type parameters, and extends the given substitution
   * by the substitution of the selected types for the parameters.
   *
   * <p>IMPORTANT: Should only be used for parameters that have non-generic bounds.
   *
   * @param parameters a list of independent type parameters
   * @param substitution the substitution to extend
   * @return the substitution extended by mapping given parameters to selected types; null, if there
   *     are no candidate types for some parameter
   */
  private @Nullable Substitution selectSubstitutionIndependently(
      List<TypeVariable> parameters, Substitution substitution) {
    List<ReferenceType> selectedTypes = new ArrayList<>(parameters.size());
    for (TypeVariable typeArgument : parameters) {
      List<ReferenceType> candidates = candidateTypes(typeArgument);
      if (candidates.isEmpty()) {
        Log.logPrintf(
            "TypeInstantiator.selectSubstitutionIndependently: No candidate types for %s%n",
            typeArgument);
        return null;
      }
      selectedTypes.add(new CandidateChooser(candidates).removeNext());
    }
    return substitution.extend(parameters, selectedTypes);
  }

  /**
   * The probability that {@link CandidateChooser#removeNext()} chooses among the comparable
   * candidates, rather than among all the candidates.
   */
  private static final double COMPARABLE_PROBABILITY = 0.5;

  /**
   * Candidate types to instantiate a type variable, from which {@link #removeNext()} chooses and
   * removes one at a time. The choice favors comparable types (see {@link #isComparable}), because
   * many generic classes and methods, such as {@code TreeMap} and {@code Collections.sort}, can do
   * useful work only on values of such types.
   */
  private static final class CandidateChooser {

    /** The remaining candidates that are comparable. */
    private final List<ReferenceType> comparable;

    /** The remaining candidates that are not comparable. */
    private final List<ReferenceType> notComparable;

    /**
     * Creates a chooser whose remaining candidates are the given candidates.
     *
     * @param candidates the candidate types
     */
    CandidateChooser(List<ReferenceType> candidates) {
      this.comparable = new ArrayList<>();
      this.notComparable = new ArrayList<>();
      for (ReferenceType candidate : candidates) {
        (isComparable(candidate) ? comparable : notComparable).add(candidate);
      }
    }

    /**
     * Creates a chooser whose remaining candidates are those of the given chooser.
     *
     * @param other a chooser
     */
    CandidateChooser(CandidateChooser other) {
      this.comparable = new ArrayList<>(other.comparable);
      this.notComparable = new ArrayList<>(other.notComparable);
    }

    /**
     * Returns true if no candidates remain.
     *
     * @return true if no candidates remain
     */
    boolean isEmpty() {
      return comparable.isEmpty() && notComparable.isEmpty();
    }

    /**
     * Removes a randomly chosen candidate from the remaining candidates and returns it. With
     * probability {@link #COMPARABLE_PROBABILITY}, the choice is uniform among the remaining
     * comparable candidates, if there are any. Otherwise, it is uniform among all the remaining
     * candidates.
     *
     * @return the chosen candidate
     */
    ReferenceType removeNext() {
      int index;
      if (Randomness.weightedCoinFlip(COMPARABLE_PROBABILITY) && !comparable.isEmpty()) {
        index = Randomness.nextRandomInt(comparable.size());
      } else {
        index = Randomness.nextRandomInt(comparable.size() + notComparable.size());
      }
      if (index < comparable.size()) {
        return swapRemove(comparable, index);
      } else {
        return swapRemove(notComparable, index - comparable.size());
      }
    }

    /**
     * Removes the element at the given index from the given list, by replacing it by the list's
     * last element, and returns it.
     *
     * @param list a list
     * @param index an index into {@code list}
     * @return the element that was at {@code index}
     */
    private static ReferenceType swapRemove(List<ReferenceType> list, int index) {
      ReferenceType result = list.get(index);
      list.set(index, list.get(list.size() - 1));
      list.remove(list.size() - 1);
      return result;
    }
  }

  /**
   * Returns true if values of the given type can be compared to one another: that is, if the type
   * is a subtype of {@code Comparable<? super T>}, where {@code T} is the type. That is the bound
   * that methods such as {@code Collections.sort} and {@code Collections.max} require. For example,
   * {@code String} is comparable, but {@code Comparable<String>} is not, because a {@code
   * Comparable<String>} can be compared only to a {@code String}.
   *
   * @param type a type
   * @return true if {@code type} is a subtype of {@code Comparable<? super type>}
   */
  /*package-private*/ static boolean isComparable(ReferenceType type) {
    // Not computeIfAbsent, for consistency with orderedTypeParameterIndex.
    Boolean result = isComparableCache.get(type);
    if (result == null) {
      result = computeIsComparable(type);
      isComparableCache.put(type, result);
    }
    return result;
  }

  /** Memoizes {@link #isComparable}. */
  private static final Map<ReferenceType, Boolean> isComparableCache = new HashMap<>();

  /**
   * Computes the result of {@link #isComparable}.
   *
   * @param type a type
   * @return true if {@code type} is a subtype of {@code Comparable<? super type>}
   */
  private static boolean computeIsComparable(ReferenceType type) {
    if (!(type instanceof ClassOrInterfaceType)) {
      return false;
    }
    InstantiatedType comparableSupertype =
        ((ClassOrInterfaceType) type).getMatchingSupertype(JavaTypes.COMPARABLE_TYPE);
    if (comparableSupertype == null) {
      return false;
    }
    TypeArgument comparedType = comparableSupertype.getTypeArguments().get(0);
    return comparedType instanceof ReferenceArgument
        && type.isSubtypeOfOrEqualTo(((ReferenceArgument) comparedType).getReferenceType());
  }

  /**
   * Adds instantiating substitutions for the given parameters to the list if satisfies the given
   * type check predicate. Each constructed substitution extends the given initial substitution.
   * Assumes that the parameters are or are refinements of the set of parameters check by the type
   * check predicate.
   *
   * @param parameters the list of parameters to instantiate
   * @param initialSubstitution the substitution to be extended by new substitutions
   * @param boundsCheck the predicate to type check a substitution
   * @return the list of instantiating substitutions; may be empty. The list is fresh and clients
   *     may side-effect it.
   */
  private List<Substitution> allSubstitutions(
      List<TypeVariable> parameters, Substitution initialSubstitution, BoundsCheck boundsCheck) {
    List<Substitution> substitutionList = new ArrayList<>();
    List<List<ReferenceType>> candidateTypes = candidateTypes(parameters);
    for (List<ReferenceType> tuple :
        iteratorToIterable(new CombinationIterator<>(candidateTypes))) {
      Substitution substitution = initialSubstitution.extend(parameters, tuple);
      if (boundsCheck.test(tuple, substitution)) {
        substitutionList.add(substitution);
      }
    }
    return substitutionList;
  }

  /**
   * Constructs the list of lists of candidate types for the given type parameters. Each sub list is
   * the list of candidates for the corresponding parameter in {@code parameters}, as determined by
   * {@link #candidateTypes(TypeVariable)}.
   *
   * @param parameters the list of type parameters
   * @return the list of candidate lists for the parameters; returns the empty list if any parameter
   *     has no candidates
   */
  @SuppressWarnings("MixedMutabilityReturnType")
  private List<List<ReferenceType>> candidateTypes(List<TypeVariable> parameters) {
    List<List<ReferenceType>> candidateTypes = new ArrayList<>();
    for (TypeVariable typeArgument : parameters) {
      List<ReferenceType> candidates = candidateTypes(typeArgument);
      if (candidates.isEmpty()) {
        Log.logPrintf("No candidate types for %s%n", typeArgument);
        return Collections.emptyList();
      }
      candidateTypes.add(candidates);
    }
    return candidateTypes;
  }

  /**
   * Returns all input types that potentially satisfy the bounds on the argument. If a bound has
   * another type parameter, then the default bound is tested. The caller {@link #allSubstitutions}
   * will ensure that the final substitution is consistent.
   *
   * @param argument the type argument
   * @return the list of candidate instantiations of the type argument
   */
  private List<ReferenceType> candidateTypes(TypeVariable argument) {
    ParameterBound lowerBound = getLowerBound(argument);
    ParameterBound upperBound = getUpperBound(argument);

    List<ReferenceType> typeList = new ArrayList<>(inputTypes.size());
    for (Type inputType : inputTypes) {
      if (inputType.isReferenceType()) {
        ReferenceType inputRefType = (ReferenceType) inputType;
        Substitution substitution = new Substitution(argument, inputRefType);
        if (lowerBound.isLowerBound(inputRefType, substitution)
            && upperBound.isUpperBound(inputRefType, substitution)) {
          typeList.add(inputRefType);
        }
      }
    }
    return typeList;
  }

  /**
   * Gets the upper bound of the given argument to test in {@link #candidateTypes(TypeVariable)}. If
   * the bound contains a type parameter other than the given argument, then returns {@code Object}
   * as the bound.
   *
   * @param argument the type argument
   * @return the upperbound of the argument if no other type parameter is needed, the {@code Object}
   *     bound otherwise
   */
  private ParameterBound getUpperBound(TypeVariable argument) {
    ParameterBound upperBound = argument.getUpperTypeBound();
    List<TypeVariable> parameters = upperBound.getTypeParameters();
    if (parameters.isEmpty() || (parameters.size() == 1 && parameters.contains(argument))) {
      return upperBound;
    }
    return ParameterBound.forType(JavaTypes.OBJECT_TYPE);
  }

  /**
   * Gets the lower bound of the given argument to be tested in {@link
   * #candidateTypes(TypeVariable)}. If the bound has a type parameter other than the given
   * argument, then returns {@link JavaTypes#NULL_TYPE} as the bound.
   *
   * @param argument the type argument
   * @return the lower bound of the argument if no other type parameter is needed, the {@link
   *     JavaTypes#NULL_TYPE} otherwise
   */
  private ParameterBound getLowerBound(TypeVariable argument) {
    ParameterBound lowerBound = argument.getLowerTypeBound();
    List<TypeVariable> parameters = lowerBound.getTypeParameters();
    if (parameters.isEmpty() || (parameters.size() == 1 && parameters.contains(argument))) {
      return lowerBound;
    }
    return ParameterBound.forType(JavaTypes.NULL_TYPE);
  }
}
