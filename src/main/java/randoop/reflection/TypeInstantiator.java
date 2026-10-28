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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
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

  /** Memoizes {@link #orderedTypeParameterIndex}. */
  private static final Map<Class<?>, Integer> orderedTypeParameterIndexCache = new HashMap<>();

  /**
   * Returns the index of the type parameter whose values instances of the given class keep in
   * order, or -1 if there is none. Such a type parameter is recognized by a constructor whose only
   * formal parameter is a comparator for it (see {@link #isComparatorInterface}). Examples are the
   * {@code E} in {@code TreeSet<E>} and the {@code K} in {@code TreeMap<K,V>}.
   *
   * @param c a class
   * @return the index of the ordered type parameter of {@code c}, or -1
   */
  private static int orderedTypeParameterIndex(Class<?> c) {
    return orderedTypeParameterIndexCache.computeIfAbsent(
        c, TypeInstantiator::computeOrderedTypeParameterIndex);
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
    for (Constructor<?> constructor : c.getDeclaredConstructors()) {
      java.lang.reflect.Type[] formals = constructor.getGenericParameterTypes();
      if (formals.length != 1 || !(formals[0] instanceof java.lang.reflect.ParameterizedType)) {
        continue;
      }
      java.lang.reflect.ParameterizedType formal = (java.lang.reflect.ParameterizedType) formals[0];
      if (!isComparatorInterface((Class<?>) formal.getRawType())) {
        continue;
      }
      // The type argument is T or `? super T`.
      java.lang.reflect.Type typeArg = formal.getActualTypeArguments()[0];
      if (typeArg instanceof WildcardType) {
        java.lang.reflect.Type[] lowerBounds = ((WildcardType) typeArg).getLowerBounds();
        if (lowerBounds.length == 1) {
          typeArg = lowerBounds[0];
        }
      }
      int index = classTypeParameters.indexOf(typeArg);
      if (index != -1) {
        return index;
      }
    }
    return -1;
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
   * Returns true if values of the given type carry their own ordering: the type is generic and has
   * a method {@code comparator()} that returns a comparator interface. Examples are {@code
   * SortedSet<E>} and {@code SortedMap<K,V>}.
   *
   * @param type a type
   * @return true if {@code type} is a generic type with a {@code comparator()} method
   */
  private static boolean isSortedType(Type type) {
    if (!type.isParameterized()) {
      return false;
    }
    try {
      Method comparator = type.getRuntimeClass().getMethod("comparator");
      return isComparatorInterface(comparator.getReturnType());
    } catch (NoSuchMethodException e) {
      return false;
    }
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

    List<TypeVariable> typeParameters = operation.getDeclaringType().getTypeParameters();
    TypeVariable orderedParameter = typeParameters.get(orderedIndex);

    // By default, choose a type T that is Comparable<T>.
    GenericClassType searchType = JavaTypes.COMPARABLE_TYPE;
    for (Type inputType : operation.getInputTypes()) {
      if (!inputType.isParameterized()) {
        continue;
      }
      if (isComparatorInterface(inputType.getRuntimeClass()) || isSortedType(inputType)) {
        // This constructor has an argument that orders T, such as a Comparator<T> or a
        // SortedSet<T>.  Half the time, choose a type T that has such a value.  Otherwise, choose a
        // type T that is Comparable<T>, because the argument might be null or might use the natural
        // ordering of T (as Collections.reverseOrder() does).
        if (Randomness.weightedCoinFlip(0.5)) {
          searchType = ((ParameterizedType) inputType).getGenericClassType();
        }
        break;
      }
    }

    Substitution substitution = selectSubstitutionForOrderedType(searchType, orderedParameter);
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
   * Select a substitution for an ordered type parameter.
   *
   * @param searchType a generic type whose first type argument is the ordered type, such as
   *     Comparator, Comparable, or SortedSet
   * @param typeParameter the type parameter to create an instantiation for
   * @return a substitution for {@code typeParameter}, or null if none is found
   */
  private @Nullable Substitution selectSubstitutionForOrderedType(
      GenericClassType searchType, TypeVariable typeParameter) {
    // Select a substitution for searchType's formal parameter.
    Substitution substitution = selectSubstitution(searchType);
    if (substitution == null) {
      return null;
    }
    // Convert it into a substitution for the given type parameter.
    TypeArgument argumentType = searchType.substitute(substitution).getTypeArguments().get(0);
    return new Substitution(typeParameter, ((ReferenceArgument) argumentType).getReferenceType());
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
   * Returns a substitution that instantiates both {@code type} and {@code patternType} to an
   * existing type. The pattern type makes it possible to select matches for partial instantiations
   * of a generic type.
   *
   * @param type the generic type for which an instantiation is to be found
   * @param patternType an instantiation of {@code type}; only further instantiations of it are
   *     considered
   * @return a substitution instantiating {@code type}'s type parameters to existing types; null if
   *     no such substitution exists
   */
  private Substitution selectSubstitution(
      ClassOrInterfaceType type, ClassOrInterfaceType patternType) {
    List<ReferenceType> matches = new ArrayList<>(inputTypes.size());
    for (Type inputType : inputTypes) {
      if (inputType.isParameterized()
          && ((ReferenceType) inputType).isInstantiationOf(patternType)) {
        matches.add((ReferenceType) inputType);
      }
    }
    if (matches.isEmpty()) {
      return null;
    }
    ReferenceType selectedType = Randomness.randomSetMember(matches);
    Substitution result = selectedType.getInstantiatingSubstitution(type);
    return result;
  }

  /**
   * Selects an existing type that instantiates the given generic declaring type and returns the
   * instantiating substitution.
   *
   * @param type the generic type for which an instantiation is to be found
   * @return a substitution instantiating the given type as an existing type; null if no such type
   */
  private Substitution selectSubstitution(ClassOrInterfaceType type) {
    return selectSubstitution(type, type);
  }

  /**
   * Selects an instantiation of the generic types of an operation, and returns a new operation with
   * the types instantiated.
   *
   * @param operation the operation
   * @return the operation with generic types instantiated
   */
  // TODO: This seems redundant with isInstantiationOf.  Should they be combined?
  private TypedClassOperation instantiateOperationTypes(TypedClassOperation operation) {
    // answer question: what type instantiation would allow a call to this operation?
    Set<TypeVariable> typeParameters = new LinkedHashSet<>();
    Substitution substitution = new Substitution();
    for (Type origParamType : operation.getInputTypes()) {
      Type paramType = origParamType.substitute(substitution);
      if (paramType.isGeneric()) {
        if (paramType.isClassOrInterfaceType()) {
          Substitution subst =
              selectSubstitution((ParameterizedType) origParamType, (ParameterizedType) paramType);
          if (subst == null) {
            return null;
          }
          substitution = substitution.extend(subst);
        } else {
          typeParameters.addAll(((ReferenceType) paramType).getTypeParameters());
        }
      }
    }
    // return types don't have to exist, but do need to have their type parameters instantiated
    Type outputType = operation.getOutputType();
    if (outputType.isReferenceType()) {
      Type paramType = outputType.substitute(substitution);
      if (paramType.isGeneric()) {
        typeParameters.addAll(((ReferenceType) paramType).getTypeParameters());
      }
    }

    if (!typeParameters.isEmpty()) {
      typeParameters.removeAll(substitution.keySet());
    }

    if (!typeParameters.isEmpty()) {
      substitution = selectSubstitution(new ArrayList<>(typeParameters), substitution);
      if (substitution == null) {
        return null;
      }
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
  private Substitution selectSubstitutionIndependently(
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
      selectedTypes.add(Randomness.randomMember(candidates));
    }
    return substitution.extend(parameters, selectedTypes);
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
