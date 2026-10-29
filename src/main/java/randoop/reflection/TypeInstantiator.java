package randoop.reflection;

import static org.plumelib.util.CollectionsPlume.iteratorToIterable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
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
   * Extends the given substitution so that it instantiates both {@code type} and {@code
   * patternType} to an existing type. The pattern type makes it possible to select matches for
   * partial instantiations of a generic type.
   *
   * <p>The extended substitution instantiates each type variable within its bounds, for each type
   * variable whose bounds the extended substitution instantiates.
   *
   * @param type the generic type for which an instantiation is to be found
   * @param patternType an instantiation of {@code type}, by {@code substitution}; only further
   *     instantiations of it are considered
   * @param substitution the substitution to extend
   * @return {@code substitution} extended by instantiating {@code type}'s type parameters to
   *     existing types; null if no such substitution exists
   */
  private @Nullable Substitution selectSubstitution(
      ClassOrInterfaceType type, ClassOrInterfaceType patternType, Substitution substitution) {
    List<ReferenceType> matches = new ArrayList<>(inputTypes.size());
    for (Type inputType : inputTypes) {
      if (inputType.isParameterized()
          && ((ReferenceType) inputType).isInstantiationOf(patternType)) {
        matches.add((ReferenceType) inputType);
      }
    }
    // Try the matches in random order.
    while (!matches.isEmpty()) {
      ReferenceType selectedType = Randomness.randomSetMember(matches);
      Substitution selected = selectedType.getInstantiatingSubstitution(type);
      if (selected != null) {
        Substitution result = substitution.extend(selected);
        if (satisfiesInstantiatedBounds(result)) {
          return result;
        }
      }
      matches.remove(selectedType);
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
    return selectSubstitution(type, type, new Substitution());
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
          substitution =
              selectSubstitution(
                  (ParameterizedType) origParamType, (ParameterizedType) paramType, substitution);
          if (substitution == null) {
            return null;
          }
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

    List<TypeVariable> remaining = uninstantiatedVariables(typeParameters, substitution);
    if (!remaining.isEmpty()) {
      // The variables that the substitution instantiates, but whose bounds mention a variable
      // in `remaining`.  For example, after the capture variable of `? extends T` has been
      // instantiated, its upper bound mentions T.
      List<TypeVariable> dependents = new ArrayList<>();
      for (TypeVariable variable : substitution.keySet()) {
        if (!boundsAreInstantiated(variable, substitution)) {
          dependents.add(variable);
        }
      }
      substitution = selectSubstitution(remaining, substitution, dependents);
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
   * Returns the type variables that must be instantiated to extend the given substitution to the
   * given type variables. These are the given type variables that the substitution does not
   * instantiate, plus the type variables in their bounds and in the bounds of the variables of the
   * substitution, transitively. For example, in {@code <T> void Collections.copy(List<? super T>,
   * List<? extends T>)}, the parameter types are instantiated by instantiating their capture
   * variables, and only the bounds of the capture variables mention {@code T}.
   *
   * @param variables type variables to be instantiated
   * @param substitution a substitution
   * @return the type variables that must be instantiated to extend {@code substitution} to {@code
   *     variables}
   */
  private static List<TypeVariable> uninstantiatedVariables(
      Collection<TypeVariable> variables, Substitution substitution) {
    Deque<TypeVariable> worklist = new ArrayDeque<>(variables);
    for (TypeVariable variable : substitution.keySet()) {
      worklist.addAll(boundVariables(variable));
    }
    List<TypeVariable> result = new ArrayList<>();
    while (!worklist.isEmpty()) {
      TypeVariable variable = worklist.remove();
      if (!containsVariable(substitution.keySet(), variable)
          && !containsVariable(result, variable)) {
        result.add(variable);
        worklist.addAll(boundVariables(variable));
      }
    }
    return result;
  }

  /**
   * Returns the type variables that occur in the bounds of the given type variable.
   *
   * @param variable a type variable
   * @return the type variables that occur in the bounds of {@code variable}
   */
  private static List<TypeVariable> boundVariables(TypeVariable variable) {
    List<TypeVariable> result = new ArrayList<>(variable.getLowerTypeBound().getTypeParameters());
    result.addAll(variable.getUpperTypeBound().getTypeParameters());
    return result;
  }

  /**
   * Returns true if the given collection contains the given type variable, possibly with different
   * bounds.
   *
   * @param variables a collection of type variables
   * @param variable a type variable
   * @return true if {@code variables} contains {@code variable}, possibly with different bounds
   */
  private static boolean containsVariable(
      Collection<? extends TypeVariable> variables, TypeVariable variable) {
    for (TypeVariable v : variables) {
      if (v.equals(variable)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if the given substitution instantiates every type variable in the bounds of the
   * given type variable.
   *
   * @param variable a type variable
   * @param substitution a substitution
   * @return true if {@code substitution} instantiates every type variable in the bounds of {@code
   *     variable}
   */
  private static boolean boundsAreInstantiated(TypeVariable variable, Substitution substitution) {
    for (TypeVariable boundVariable : boundVariables(variable)) {
      if (!containsVariable(substitution.keySet(), boundVariable)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if the given substitution instantiates each of its type variables within the
   * variable's bounds, for each type variable whose bounds the substitution instantiates.
   *
   * @param substitution a substitution
   * @return true if {@code substitution} instantiates each of its type variables within the
   *     variable's bounds, ignoring variables whose bounds it does not instantiate
   */
  private static boolean satisfiesInstantiatedBounds(Substitution substitution) {
    List<TypeVariable> checked = new ArrayList<>();
    for (TypeVariable variable : substitution.keySet()) {
      if (boundsAreInstantiated(variable, substitution)) {
        checked.add(variable);
      }
    }
    return new BoundsCheck(checked).test(substitution);
  }

  /**
   * Selects an instantiating substitution for the given list of type variables.
   *
   * @param typeParameters the type variables to be instantiated
   * @return a substitution instantiating the type variables; null if some type variable has no
   *     instantiating types
   * @see #selectSubstitution(List, Substitution, List)
   */
  private @Nullable Substitution selectSubstitution(List<TypeVariable> typeParameters) {
    return selectSubstitution(typeParameters, new Substitution(), Collections.emptyList());
  }

  /**
   * Extends the given substitution by instantiations for the given list of type variables.
   *
   * @param typeParameters the type variables to be instantiated
   * @param substitution the substitution to extend
   * @param dependents variables that {@code substitution} instantiates, whose bounds mention
   *     variables in {@code typeParameters}; the extended substitution instantiates them within
   *     their bounds
   * @return the substitution extended by instantiating type variables; null if any of the variables
   *     has no instantiating types
   */
  private @Nullable Substitution selectSubstitution(
      List<TypeVariable> typeParameters, Substitution substitution, List<TypeVariable> dependents) {
    List<Substitution> substitutionList =
        allExtendingSubstitutions(typeParameters, substitution, dependents);
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
   * @param dependents variables that {@code substitution} instantiates, whose bounds mention
   *     variables in {@code typeParameters}; each returned substitution instantiates them within
   *     their bounds
   * @return the list of possible substitutions, empty if none is found
   */
  @SuppressWarnings("MixedMutabilityReturnType")
  private List<Substitution> allExtendingSubstitutions(
      List<TypeVariable> typeParameters, Substitution substitution, List<TypeVariable> dependents) {

    // Partition parameters based on whether they might have independent bounds:

    // parameters with generic bounds, or that occur in the bounds of dependents, may be dependent
    // on other parameters
    List<TypeVariable> genericParameters = new ArrayList<>(typeParameters.size());

    // parameters with nongeneric bounds that occur in the bounds of generic parameters
    List<TypeVariable> nongenericParameters = new ArrayList<>(typeParameters.size());

    // parameters with nongeneric bounds that occur in no bound of another parameter, such as
    // wildcard capture variables without generic bounds; they can be selected independently
    List<TypeVariable> independentParameters = new ArrayList<>(typeParameters.size());

    for (TypeVariable variable : typeParameters) {
      if (variable.hasGenericBound() || occursInBounds(variable, dependents)) {
        genericParameters.add(variable);
      }
    }
    for (TypeVariable variable : typeParameters) {
      if (!genericParameters.contains(variable)) {
        if (occursInBounds(variable, genericParameters)) {
          nongenericParameters.add(variable);
        } else {
          independentParameters.add(variable);
        }
      }
    }

    // If any of the type variables has a generic bound, assumes there are dependencies and
    // enumerates all possible substitutions for the generic parameters and the parameters in their
    // bounds, and tests them.  Independently selects an instantiating type for each other variable.

    // These are the candidates that this routine will return.
    List<Substitution> result = new ArrayList<>();
    if (!genericParameters.isEmpty()) {
      // if there are type parameters with generic bounds
      if (!nongenericParameters.isEmpty()) {
        // if there are type parameters with non-generic bounds that are variables in
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
          result.addAll(allExtendingSubstitutions(parameters, initialSubstitution, dependents));
        }
      } else {
        // if no parameters with non-generic bounds, choose instantiation for parameters
        // with generic bounds
        result = allSubstitutions(genericParameters, substitution, dependents);
      }
      if (result.isEmpty()) {
        return result;
      }
    } else {
      result.add(substitution);
    }

    // Select the independent parameters once, and augment each existing candidate.
    if (!independentParameters.isEmpty()) {
      Substitution independent =
          selectSubstitutionIndependently(independentParameters, new Substitution());
      if (independent == null) {
        return Collections.emptyList();
      }
      result.replaceAll(s -> s.extend(independent));
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
   * Returns true if the given type variable occurs in the bounds of any of the given type
   * variables.
   *
   * @param variable a type variable
   * @param variables type variables
   * @return true if {@code variable} occurs in the bounds of a variable in {@code variables}
   */
  private static boolean occursInBounds(TypeVariable variable, List<TypeVariable> variables) {
    for (TypeVariable v : variables) {
      if (containsVariable(boundVariables(v), variable)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the instantiating substitutions for the given parameters that instantiate both the
   * parameters and the dependents within their bounds. Each constructed substitution extends the
   * given initial substitution.
   *
   * @param parameters the list of parameters to instantiate
   * @param initialSubstitution the substitution to be extended by new substitutions
   * @param dependents variables that {@code initialSubstitution} instantiates, whose bounds mention
   *     variables in {@code parameters}
   * @return the list of instantiating substitutions; may be empty. The list is fresh and clients
   *     may side-effect it.
   */
  private List<Substitution> allSubstitutions(
      List<TypeVariable> parameters,
      Substitution initialSubstitution,
      List<TypeVariable> dependents) {
    List<TypeVariable> checked = new ArrayList<>(parameters);
    checked.addAll(dependents);
    BoundsCheck boundsCheck = new BoundsCheck(checked);
    List<Substitution> substitutionList = new ArrayList<>();
    List<List<ReferenceType>> candidateTypes =
        candidateTypes(parameters, initialSubstitution, dependents);
    for (List<ReferenceType> tuple :
        iteratorToIterable(new CombinationIterator<>(candidateTypes))) {
      Substitution substitution = initialSubstitution.extend(parameters, tuple);
      if (boundsCheck.test(substitution)) {
        substitutionList.add(substitution);
      }
    }
    return substitutionList;
  }

  /**
   * Constructs the list of lists of candidate types for the given type parameters. Each sub list is
   * the list of candidates for the corresponding parameter in {@code parameters}, as determined by
   * {@link #candidateTypes(TypeVariable, Substitution, List)}.
   *
   * @param parameters the list of type parameters
   * @return the list of candidate lists for the parameters; returns the empty list if any parameter
   *     has no candidates
   */
  private List<List<ReferenceType>> candidateTypes(List<TypeVariable> parameters) {
    return candidateTypes(parameters, new Substitution(), Collections.emptyList());
  }

  /**
   * Constructs the list of lists of candidate types for the given type parameters. Each sub list is
   * the list of candidates for the corresponding parameter in {@code parameters}, as determined by
   * {@link #candidateTypes(TypeVariable, Substitution, List)}.
   *
   * @param parameters the list of type parameters
   * @param substitution a substitution that instantiates none of {@code parameters}
   * @param dependents variables that {@code substitution} instantiates
   * @return the list of candidate lists for the parameters; returns the empty list if any parameter
   *     has no candidates
   */
  @SuppressWarnings("MixedMutabilityReturnType")
  private List<List<ReferenceType>> candidateTypes(
      List<TypeVariable> parameters, Substitution substitution, List<TypeVariable> dependents) {
    List<List<ReferenceType>> candidateTypes = new ArrayList<>();
    for (TypeVariable typeArgument : parameters) {
      List<ReferenceType> candidates = candidateTypes(typeArgument, substitution, dependents);
      if (candidates.isEmpty()) {
        Log.logPrintf("No candidate types for %s%n", typeArgument);
        return Collections.emptyList();
      }
      candidateTypes.add(candidates);
    }
    return candidateTypes;
  }

  /**
   * Returns all input types that potentially satisfy the bounds on the argument.
   *
   * @param argument the type argument
   * @return the list of candidate instantiations of the type argument
   * @see #candidateTypes(TypeVariable, Substitution, List)
   */
  private List<ReferenceType> candidateTypes(TypeVariable argument) {
    return candidateTypes(argument, new Substitution(), Collections.emptyList());
  }

  /**
   * Returns all input types that potentially satisfy the bounds on the argument, and the bounds of
   * the dependents whose bounds mention no uninstantiated type variable other than the argument. If
   * a bound of the argument has another type parameter, then the default bound is tested. The
   * caller {@link #allSubstitutions} will ensure that the final substitution is consistent.
   *
   * @param argument the type argument
   * @param substitution a substitution that does not instantiate {@code argument}
   * @param dependents variables that {@code substitution} instantiates
   * @return the list of candidate instantiations of the type argument
   */
  private List<ReferenceType> candidateTypes(
      TypeVariable argument, Substitution substitution, List<TypeVariable> dependents) {
    ParameterBound lowerBound = getLowerBound(argument);
    ParameterBound upperBound = getUpperBound(argument);

    // The dependents whose bounds mention the argument and no other uninstantiated variable.
    List<TypeVariable> checked = new ArrayList<>();
    for (TypeVariable dependent : dependents) {
      List<TypeVariable> boundVariables = boundVariables(dependent);
      if (containsVariable(boundVariables, argument)
          && boundVariables.stream()
              .allMatch(v -> v.equals(argument) || containsVariable(substitution.keySet(), v))) {
        checked.add(dependent);
      }
    }
    BoundsCheck boundsCheck = new BoundsCheck(checked);

    List<ReferenceType> typeList = new ArrayList<>(inputTypes.size());
    for (Type inputType : inputTypes) {
      if (inputType.isReferenceType()) {
        ReferenceType inputRefType = (ReferenceType) inputType;
        Substitution argumentSubstitution = new Substitution(argument, inputRefType);
        if (lowerBound.isLowerBound(inputRefType, argumentSubstitution)
            && upperBound.isUpperBound(inputRefType, argumentSubstitution)
            && (checked.isEmpty() || boundsCheck.test(substitution.extend(argumentSubstitution)))) {
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
