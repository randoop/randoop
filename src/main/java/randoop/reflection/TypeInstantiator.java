package randoop.reflection;

import static org.plumelib.util.CollectionsPlume.iteratorToIterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
import randoop.types.JDKTypes;
import randoop.types.JavaTypes;
import randoop.types.ParameterBound;
import randoop.types.ParameterizedType;
import randoop.types.ReferenceArgument;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeArgument;
import randoop.types.TypeTuple;
import randoop.types.TypeVariable;
import randoop.util.Log;
import randoop.util.Randomness;

/** Instantiates type parameters from a set of input types. */
public class TypeInstantiator {

  /** If true, log information about instantiation. */
  private static final boolean debug = false;

  // This field is side-effected by aliases held outside the class.
  /**
   * The input types: the types of the values that Randoop's sequence pool can produce, and their
   * supertypes. For example, if the pool can produce an {@code ArrayList<String>}, then {@code
   * List<String>} and {@code Object} are also input types. The set needs to be closed under
   * supertypes: if a type is in the set, then so are all of its supertypes.
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
        if (declaringType.isSubtypeOfOrEqualTo(JDKTypes.SORTED_SET_TYPE)) {
          substitution = instantiateSortedSetType(operation);
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
   * Returns a substitution that instantiates the {@code SortedSet} type of the given constructor. A
   * {@code SortedSet} may be built so that the element type {@code E} is ordered either explicitly
   * with a {@code Comparator<E>}, or the element type satisfies {@code E implements Comparable<E>}.
   *
   * @param operation a constructor to be instantiated, for a class that implements {@code
   *     SortedSet}
   * @return the substitution to instantiate the element type of the {@code SortedSet} type
   */
  private Substitution instantiateSortedSetType(TypedClassOperation operation) {
    assert operation.isConstructorCall() : "only call with constructors of SortedSet subtype";

    // TODO: we have no guarantee that there is a type parameter.
    // For example, "class MyClass implements SortedSet<String>".
    // TODO: we have no guarantee that the first type parameter is the set element type.
    // For example, "class MyClass<A, B> implements SortedSet<B>".
    TypeVariable typeParameter = operation.getDeclaringType().getTypeParameters().get(0);

    TypeTuple opInputTypes = operation.getInputTypes();

    // There are four constructors in the SortedSet interface.

    if (opInputTypes.isEmpty()) {
      // This is the default constructor, choose a type E that is Comparable<E>.
      return selectSubstitutionForSortedSet(JavaTypes.COMPARABLE_TYPE, typeParameter);
    } else if (opInputTypes.size() == 1) {
      ClassOrInterfaceType inputType = (ClassOrInterfaceType) opInputTypes.get(0);
      if (inputType.isInstantiationOf(JDKTypes.COMPARATOR_TYPE)) {
        // This constructor has Comparator<E> arg, choose type E with Comparator<E>.
        return selectSubstitutionForSortedSet(JDKTypes.COMPARATOR_TYPE, typeParameter);
      } else if (inputType.isInstantiationOf(JDKTypes.COLLECTION_TYPE)) {
        // This constructor has Collection<E> arg, choose type E that is Comparable<E>.
        return selectSubstitutionForSortedSet(JavaTypes.COMPARABLE_TYPE, typeParameter);
      } else if (inputType.isInstantiationOf(JDKTypes.SORTED_SET_TYPE)) {
        // This constructor has SortedSet<E> arg, choose existing matching type.
        return selectSubstitutionForSortedSet(JDKTypes.SORTED_SET_TYPE, typeParameter);
      }
    }

    // This isn't one of the four standard SortedSet constructors.  We don't know what to do.
    return null;
  }

  /**
   * Select a substitution for the type parameter of SortedSet.
   *
   * @param searchType one of Comparator, Comparable, or SortedSet
   * @param typeParameter the type parameter to create an instantiation for
   * @return a substitution for the type parameter of SortedSet
   */
  private Substitution selectSubstitutionForSortedSet(
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

    // The substitutions that this method returns.
    List<Substitution> result = new ArrayList<>();
    if (!genericParameters.isEmpty()) {
      // if there are type parameters with generic bounds
      if (!nongenericParameters.isEmpty()) {
        // if there are type parameters with non-generic bounds, these may be variables in
        // generic-bounded parameters

        Map<TypeVariable, List<ReferenceType>> nongenericCandidates =
            candidatesByVariable(nongenericParameters);
        if (nongenericCandidates == null) {
          return Collections.emptyList();
        }
        // The candidate types of a type variable do not depend on the substitution, so compute
        // them once rather than once per tuple.
        Map<TypeVariable, List<ReferenceType>> genericCandidates =
            candidatesByVariable(genericParameters);
        if (genericCandidates == null) {
          return Collections.emptyList();
        }
        List<TypeVariable> nongenericVariables = new ArrayList<>(nongenericCandidates.keySet());
        for (List<ReferenceType> tuple :
            iteratorToIterable(new CombinationIterator<>(nongenericCandidates.values()))) {
          // choose instantiating substitution for non-generic bounded parameters
          Substitution initialSubstitution = substitution.extend(nongenericVariables, tuple);
          // choose instantiation for parameters with generic bounds
          result.addAll(allSubstitutions(genericCandidates, initialSubstitution));
        }
      } else {
        // if no parameters with non-generic bounds, choose instantiation for parameters
        // with generic bounds
        Map<TypeVariable, List<ReferenceType>> genericCandidates =
            candidatesByVariable(genericParameters);
        if (genericCandidates == null) {
          return Collections.emptyList();
        }
        result = allSubstitutions(genericCandidates, substitution);
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

    // Can always select captured wildcards independently.  Extend each substitution in `result`.
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
   * Returns the substitutions that extend the given substitution by instantiating the given type
   * variables with a combination of their candidate types, and that satisfy the bounds of those
   * type variables.
   *
   * @param candidatesByVariable a map from each type variable to instantiate to its candidate types
   * @param initialSubstitution the substitution to be extended by new substitutions
   * @return the list of instantiating substitutions; may be empty. The list is fresh and clients
   *     may side-effect it.
   */
  private static List<Substitution> allSubstitutions(
      Map<TypeVariable, List<ReferenceType>> candidatesByVariable,
      Substitution initialSubstitution) {
    List<TypeVariable> parameters = new ArrayList<>(candidatesByVariable.keySet());
    BoundsCheck boundsCheck = new BoundsCheck(parameters);
    List<Substitution> substitutionList = new ArrayList<>();
    for (List<ReferenceType> tuple :
        iteratorToIterable(new CombinationIterator<>(candidatesByVariable.values()))) {
      Substitution substitution = initialSubstitution.extend(parameters, tuple);
      if (boundsCheck.test(tuple, substitution)) {
        substitutionList.add(substitution);
      }
    }
    return substitutionList;
  }

  /**
   * Returns a map from each of the given type variables to its {@link
   * #candidateTypes(TypeVariable)}. For example, for {@code [K, V]}, the result is {@code {K:
   * candidateTypes(K), V: candidateTypes(V)}}.
   *
   * @param parameters type variables
   * @return a map, whose keys are in the order of {@code parameters}, from each element of {@code
   *     parameters} to its candidate types, which are not empty; or null if some element of {@code
   *     parameters} has no candidate types
   */
  private @Nullable Map<TypeVariable, List<ReferenceType>> candidatesByVariable(
      List<TypeVariable> parameters) {
    Map<TypeVariable, List<ReferenceType>> result = new LinkedHashMap<>();
    for (TypeVariable parameter : parameters) {
      List<ReferenceType> candidates = candidateTypes(parameter);
      if (candidates.isEmpty()) {
        Log.logPrintf("No candidate types for %s%n", parameter);
        return null;
      }
      result.put(parameter, candidates);
    }
    return result;
  }

  /**
   * Returns the candidate types for the given type variable: the input types that are within its
   * bounds, ignoring any bound that mentions another type variable. A caller that instantiates the
   * other type variables must check the ignored bounds.
   *
   * @param parameter a type variable
   * @return the candidate types for {@code parameter}; a fresh list that the caller can modify
   */
  private List<ReferenceType> candidateTypes(TypeVariable parameter) {
    ParameterBound lowerBound = getLowerBound(parameter);
    ParameterBound upperBound = getUpperBound(parameter);

    List<ReferenceType> typeList = new ArrayList<>(inputTypes.size());
    for (Type inputType : inputTypes) {
      if (inputType.isReferenceType()) {
        ReferenceType inputRefType = (ReferenceType) inputType;
        Substitution substitution = new Substitution(parameter, inputRefType);
        if (lowerBound.isLowerBound(inputRefType, substitution)
            && upperBound.isUpperBound(inputRefType, substitution)) {
          typeList.add(inputRefType);
        }
      }
    }
    return typeList;
  }

  /**
   * Returns the upper bound of the given type variable that {@link #candidateTypes(TypeVariable)}
   * tests. If the bound mentions a type variable other than the given one, returns {@code Object}
   * instead.
   *
   * @param parameter a type variable
   * @return the upper bound of {@code parameter} if it mentions no other type variable, or {@code
   *     Object} otherwise
   */
  private ParameterBound getUpperBound(TypeVariable parameter) {
    ParameterBound upperBound = parameter.getUpperTypeBound();
    List<TypeVariable> boundVariables = upperBound.getTypeParameters();
    if (boundVariables.isEmpty()
        || (boundVariables.size() == 1 && boundVariables.get(0).equals(parameter))) {
      return upperBound;
    }
    return ParameterBound.forType(JavaTypes.OBJECT_TYPE);
  }

  /**
   * Returns the lower bound of the given type variable that {@link #candidateTypes(TypeVariable)}
   * tests. If the bound mentions a type variable other than the given one, returns {@link
   * JavaTypes#NULL_TYPE} instead.
   *
   * @param parameter a type variable
   * @return the lower bound of {@code parameter} if it mentions no other type variable, or {@link
   *     JavaTypes#NULL_TYPE} otherwise
   */
  private ParameterBound getLowerBound(TypeVariable parameter) {
    ParameterBound lowerBound = parameter.getLowerTypeBound();
    List<TypeVariable> boundVariables = lowerBound.getTypeParameters();
    if (boundVariables.isEmpty()
        || (boundVariables.size() == 1 && boundVariables.get(0).equals(parameter))) {
      return lowerBound;
    }
    return ParameterBound.forType(JavaTypes.NULL_TYPE);
  }
}
