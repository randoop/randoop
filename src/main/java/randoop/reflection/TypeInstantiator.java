package randoop.reflection;

import static org.plumelib.util.CollectionsPlume.iteratorToIterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import randoop.operation.TypedClassOperation;
import randoop.types.BoundsCheck;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GenericClassType;
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
    this.inputTypes = inputTypes;
  }

  /**
   * Instantiate the given operation by choosing type arguments for its type parameters.
   *
   * @param operation the generic operation to instantiate
   * @return an instantiated version of the operation
   */
  public TypedClassOperation instantiate(TypedClassOperation operation) {
    assert operation.isGeneric() || operation.hasWildcardTypes()
        : "operation " + operation + " must be generic or have wildcards";

    // Need to allow for backtracking, because choice of instantiation for declaring type may fail
    // for generic operation --- OR maybe not.

    // if declaring type of operation is generic, select instantiation
    ClassOrInterfaceType declaringType = operation.getDeclaringType();
    if (declaringType.isGeneric()) {
      Substitution substitution;

      // if operation creates objects of its declaring type, may create new instantiation
      if (operation.isConstructorCall()
          || (operation.isStatic() && operation.getOutputType().equals(declaringType))) {
        if (declaringType.isSubtypeOf(JDKTypes.SORTED_SET_TYPE)) {
          substitution = instantiateSortedSetType(operation);
        } else {
          substitution = instantiateClass(declaringType);
        }
      } else { // otherwise, select from existing one
        substitution = selectSubstitution(declaringType);
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
      Log.logPrintf("Applying capture conversion to %s%n", operation);
      operation = operation.applyCaptureConversion();
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

    // TODO: we have no guaranteed that there is a type parameter.
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
  private Substitution instantiateClass(ClassOrInterfaceType type) {
    if (Randomness.weightedCoinFlip(0.5)) {
      Substitution substitution = selectSubstitution(type);
      if (substitution != null) {
        return substitution;
      }
    }
    List<TypeVariable> typeParameters = type.getTypeParameters();
    Substitution substitution = selectSubstitution(typeParameters);
    if (substitution != null) {
      ClassOrInterfaceType instantiatedType = type.substitute(substitution);
      if (!instantiatedType.isGeneric()) {
        return substitution;
      } else {
        Log.logPrintf("Didn't find types to satisfy bounds on generic type: %s%n", type);
        return null;
      }
    }
    return null;
  }

  /**
   * Selects an existing type that instantiates the given pattern type which is an instantiation of
   * a generic declaring type and returns the instantiating substitution for the more general type.
   * The pattern type makes it possible to select matches for partial instantiations of a generic
   * type.
   *
   * @param type the generic type for which an instantiation is to be found
   * @param patternType the generic type from which match is to be determined, must be an
   *     instantiation of {@code type}
   * @return a substitution instantiating {@code type}'s type parameters to existing types; null if
   *     no such type exists
   */
  private Substitution selectSubstitution(
      ClassOrInterfaceType type, ClassOrInterfaceType patternType) {
    List<ReferenceType> matches = new ArrayList<>();
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
  private TypedClassOperation instantiateOperationTypes(TypedClassOperation operation) {
    // answer question: what type instantiation would allow a call to this operation?
    Set<TypeVariable> typeParameters = new LinkedHashSet<>();
    Substitution substitution = new Substitution();
    for (Type parameterType : operation.getInputTypes()) {
      Type workingType = parameterType.substitute(substitution);
      if (workingType.isGeneric()) {
        if (workingType.isClassOrInterfaceType()) {
          Substitution subst =
              selectSubstitution(
                  (ParameterizedType) parameterType, (ParameterizedType) workingType);
          if (subst == null) {
            return null;
          }
          substitution = substitution.extend(subst);
        } else {
          typeParameters.addAll(((ReferenceType) workingType).getTypeParameters());
        }
      }
    }
    // return types don't have to exist, but do need to have their type parameters instantiated
    if (operation.getOutputType().isReferenceType()) {
      Type workingType = operation.getOutputType().substitute(substitution);
      if (workingType.isGeneric()) {
        typeParameters.addAll(((ReferenceType) workingType).getTypeParameters());
      }
    }

    if (!typeParameters.isEmpty()) {
      typeParameters.removeAll(substitution.keySet());
    }

    if (!typeParameters.isEmpty()) {
      substitution = extendSubstitution(new ArrayList<>(typeParameters), substitution);
      if (substitution == null) {
        return null;
      }
    }

    operation = operation.substitute(substitution);
    if (operation.isGeneric()) {
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
   * @see #extendSubstitution(List, Substitution)
   */
  private Substitution selectSubstitution(List<TypeVariable> typeParameters) {
    Substitution substitution = new Substitution();
    return extendSubstitution(typeParameters, substitution);
  }

  /**
   * Extends the given substitution by instantiations for the given list of type variables. If any
   * of the type variables has a generic bound, assumes there are dependencies and enumerates all
   * possible substitutions and tests them. Otherwise, independently selects an instantiating type
   * for each variable.
   *
   * @param typeParameters the type variables to be instantiated
   * @param substitution the substitution to extend
   * @return the substitution extended by instantiating type variables; null if any of the variables
   *     has no instantiating types
   */
  private Substitution extendSubstitution(
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
   * @return the list of possible substitutions, empty if none are found
   */
  @SuppressWarnings("MixedMutabilityReturnType")
  private List<Substitution> allExtendingSubstitutions(
      List<TypeVariable> typeParameters, Substitution substitution) {

    // Partition parameters based on whether they might have independent bounds:

    // parameters with generic bounds may be dependent on other parameters
    List<TypeVariable> genericParameters = new ArrayList<>();

    // parameters with nongeneric bounds can be selected independently, but may be used by
    List<TypeVariable> nongenericParameters = new ArrayList<>();

    // wildcard capture variables without generic bounds can be selected independently, and
    // may not be used in the bounds of another parameter.
    List<TypeVariable> captureParameters = new ArrayList<>();

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

    // These are the candidates that this routine will return.
    List<Substitution> result = new ArrayList<>();
    if (!genericParameters.isEmpty()) {
      // if there are type parameters with generic bounds
      if (!nongenericParameters.isEmpty()) {
        // if there are type parameters with non-generic bounds, these may be variables in
        // generic-bounded parameters

        List<List<ReferenceType>> nongenCandidates = allCandidateTypeLists(nongenericParameters);
        if (nongenCandidates.isEmpty()) {
          return Collections.emptyList();
        }
        for (List<ReferenceType> tuple : iteratorToIterable(new ListIterator<>(nongenCandidates))) {
          // choose instantiating substitution for non-generic bounded parameters
          Substitution initialSubstitution = substitution.extend(nongenericParameters, tuple);
          // apply selected substitution to all generic-bounded parameters
          List<TypeVariable> parameters = new ArrayList<>();
          for (TypeVariable variable : genericParameters) {
            ReferenceType paramType = variable.substitute(initialSubstitution);
            if (paramType.isVariable()) {
              parameters.add(variable);
            }
          }
          // choose instantiation for parameters with generic-bounds
          result.addAll(allExtendingSubstitutions(parameters, initialSubstitution));
        }
      } else {
        // if no parameters with non-generic bounds, choose instantiation for parameters
        // with generic bounds
        BoundsCheck boundsCheck = new BoundsCheck(genericParameters);
        result = allInstantiations(genericParameters, substitution, boundsCheck);
      }
      if (result.isEmpty()) {
        return result;
      }
    } else if (!nongenericParameters.isEmpty()) {
      // if there are no type parameters with generic bounds, can select others independently
      substitution = extendSubstitutionIndependently(nongenericParameters, substitution);
      if (substitution == null) {
        return Collections.emptyList();
      }
      result.add(substitution);
    }

    // Can always select captured wildcards independently.  Augment each existing candidate.
    if (!captureParameters.isEmpty()) {
      if (result.isEmpty()) {
        result.add(extendSubstitutionIndependently(captureParameters, substitution));
      } else {
        List<Substitution> substList = new ArrayList<>();
        for (Substitution s : result) {
          substList.add(extendSubstitutionIndependently(captureParameters, s));
        }
        result = substList;
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
  private Substitution extendSubstitutionIndependently(
      List<TypeVariable> parameters, Substitution substitution) {
    List<ReferenceType> selectedTypes = new ArrayList<>();
    for (TypeVariable typeArgument : parameters) {
      List<ReferenceType> candidates = allCandidates(typeArgument);
      if (candidates.isEmpty()) {
        Log.logPrintf(
            "TypeInstantiator.extendSubstitutionIndependently: No candidate types for %s%n",
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
   * @return the list of instantiating substitutions
   */
  private List<Substitution> allInstantiations(
      List<TypeVariable> parameters, Substitution initialSubstitution, BoundsCheck boundsCheck) {
    List<Substitution> substitutionList = new ArrayList<>();
    List<List<ReferenceType>> candidateTypes = allCandidateTypeLists(parameters);
    if (candidateTypes.isEmpty()) {
      // cannot use `Collections.emptyList()` because clients will add elements to the returned list
      return new ArrayList<>();
    }
    for (List<ReferenceType> tuple : iteratorToIterable(new ListIterator<>(candidateTypes))) {
      Substitution substitution = initialSubstitution.extend(parameters, tuple);
      if (boundsCheck.test(tuple, substitution)) {
        substitutionList.add(substitution);
      }
    }
    return substitutionList;
  }

  /**
   * Constructs the list of lists of candidate types for the given type parameters. Each list is the
   * list of candidates for the parameter in the corresponding position of the given list as
   * determined by {@link #allCandidates(TypeVariable)}.
   *
   * @param parameters the list of type parameters
   * @return the list of candidate lists for the parameters; returns the empty list if any parameter
   *     has no candidates
   */
  @SuppressWarnings("MixedMutabilityReturnType")
  private List<List<ReferenceType>> allCandidateTypeLists(List<TypeVariable> parameters) {
    List<List<ReferenceType>> candidateTypes = new ArrayList<>();
    for (TypeVariable typeArgument : parameters) {
      List<ReferenceType> candidates = allCandidates(typeArgument);
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
   * another type parameter, then the default bound is tested.
   *
   * @param argument the type arguments
   * @return the list of candidate types to include in tested tuples
   */
  private List<ReferenceType> allCandidates(TypeVariable argument) {
    ParameterBound lowerBound = selectLowerBound(argument);
    ParameterBound upperBound = selectUpperBound(argument);

    List<ReferenceType> typeList = new ArrayList<>();
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
   * Chooses the upper bound of the given argument to test in {@link #allCandidates(TypeVariable)}.
   * If the bound contains a type parameter other than the given argument, then the bound for the
   * {@code Object} type is returned.
   *
   * @param argument the type argument
   * @return the upperbound of the argument if no other type parameter is needed, the {@code Object}
   *     bound otherwise
   */
  private ParameterBound selectUpperBound(TypeVariable argument) {
    ParameterBound upperBound = argument.getUpperTypeBound();
    List<TypeVariable> parameters = upperBound.getTypeParameters();
    if (parameters.isEmpty() || (parameters.size() == 1 && parameters.contains(argument))) {
      return upperBound;
    }
    return ParameterBound.forType(JavaTypes.OBJECT_TYPE);
  }

  /**
   * Chooses the lower bound of the given argument to be tested in {@link
   * #allCandidates(TypeVariable)}. If the bound has a type parameter other than the given argument,
   * then the {@link JavaTypes#NULL_TYPE} is return as the bound.
   *
   * @param argument the type argument
   * @return the lower bound of the argument if no other type parameter is needed, the {@link
   *     JavaTypes#NULL_TYPE} otherwise
   */
  private ParameterBound selectLowerBound(TypeVariable argument) {
    ParameterBound lowerBound = argument.getLowerTypeBound();
    List<TypeVariable> parameters = lowerBound.getTypeParameters();
    if (parameters.isEmpty() || (parameters.size() == 1 && parameters.contains(argument))) {
      return lowerBound;
    }
    return ParameterBound.forType(JavaTypes.NULL_TYPE);
  }
}
