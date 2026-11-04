package randoop.reflection;

import static org.plumelib.util.CollectionsPlume.iteratorToIterable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
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

  /**
   * The maximum number of random choices of input types for an operation's parameterized parameter
   * types that {@link #selectOperationSubstitution(TypedClassOperation)} tries before it gives up.
   */
  private static final int MAX_INSTANTIATION_ATTEMPTS = 10;

  /**
   * The maximum number of candidate substitutions for an operation's other type variables that
   * {@link #selectOperationSubstitution(TypedClassOperation)} checks against their bounds.
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
   * Selects an existing type that instantiates the given generic declaring type and returns the
   * instantiating substitution.
   *
   * @param type the generic type for which an instantiation is to be found
   * @return a substitution instantiating the given type as an existing type; null if no such type
   */
  private Substitution selectSubstitution(ClassOrInterfaceType type) {
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
   * operation and satisfies the bounds of its type variables; see {@link
   * #satisfiesBounds(Substitution)}.
   *
   * <p>For each parameterized parameter type of the operation, this chooses an input type randomly.
   * That choice instantiates the type variables in the parameter type. This then extends the
   * substitution to the operation's other type variables. It chooses the candidate types for each
   * of them separately, so a choice for one type variable can treat another one as an unknown type,
   * and the choices can be inconsistent. This returns only a substitution that satisfies the bounds
   * of all the type variables that it instantiates. If there are more than {@link
   * #MAX_CHECKED_SUBSTITUTIONS} combinations of candidate types, this checks that many random
   * combinations rather than all of them.
   *
   * <p>The other type variables include each type variable that occurs in the bounds of an
   * instantiated type variable, such as {@code T} in {@code <T> void copy(List<? super T>, List<?
   * extends T>)}, where {@code T} occurs only in the bounds of the wildcards; see {@link
   * #addBoundTypeVariables(Set, Substitution)}. Checking the bounds of the capture variables of the
   * wildcards against the type chosen for {@code T} ensures that the types chosen for the wildcards
   * are consistent with one another.
   *
   * <p>If no substitution is found, and some parameter type matched several input types, this
   * chooses the input types again, up to {@link #MAX_INSTANTIATION_ATTEMPTS} times.
   *
   * @param operation an operation
   * @return a substitution that instantiates the generic types of {@code operation} and satisfies
   *     the bounds of its type variables, or null if none was found
   */
  private @Nullable Substitution selectOperationSubstitution(TypedClassOperation operation) {
    // Why the most recent attempt failed; used for diagnostics.
    String failure = "";
    for (int attempt = 0; attempt < MAX_INSTANTIATION_ATTEMPTS; attempt++) {
      // answer question: what type instantiation would allow a call to this operation?
      Set<TypeVariable> typeParameters = new LinkedHashSet<>();
      Substitution substitution = new Substitution();
      // True if an input type was chosen among several for some parameter type.
      boolean choseAmongSeveral = false;
      // True if each parameterized parameter type matched an input type.
      boolean allMatched = true;
      for (Type origParamType : operation.getInputTypes()) {
        Type paramType = origParamType.substitute(substitution);
        if (paramType.isGeneric()) {
          if (paramType.isClassOrInterfaceType()) {
            // TODO: This seems redundant with isInstantiationOf.  Should they be combined?
            List<ReferenceType> matches = matchingInputTypes((ParameterizedType) paramType);
            if (matches.isEmpty()) {
              failure = "no input type instantiates " + paramType;
              allMatched = false;
              break;
            }
            choseAmongSeveral |= matches.size() > 1;
            ReferenceType selectedType = Randomness.randomSetMember(matches);
            Substitution subst =
                selectedType.getInstantiatingSubstitution((ParameterizedType) origParamType);
            if (subst == null) {
              failure = "no substitution instantiates " + origParamType + " as " + selectedType;
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
        addBoundTypeVariables(typeParameters, substitution);

        if (typeParameters.isEmpty()) {
          if (satisfiesBounds(substitution)) {
            return substitution;
          }
          failure = substitution + " does not satisfy the bounds";
        } else {
          List<TypeVariable> parameters = new ArrayList<>(typeParameters);
          List<List<ReferenceType>> candidates = consistentCandidateTypes(parameters, substitution);
          if (candidates.isEmpty()) {
            failure = "some type variable in " + parameters + " has no candidate types";
          } else {
            Substitution result =
                selectSubstitutionSatisfyingBounds(parameters, candidates, substitution);
            if (result != null) {
              return result;
            }
            failure = "no extension of " + substitution + " satisfies the bounds";
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
   * Adds to the given set each type variable that the given substitution does not instantiate and
   * that occurs in the bounds of a type variable that is in the set or that the substitution
   * instantiates. Repeats until no more type variables are added, so the set also contains each
   * such type variable that occurs in the bounds of an added type variable.
   *
   * <p>For example, in {@code <T> void copy(List<? super T>, List<? extends T>)}, {@code T} occurs
   * only in the bounds of the capture variables of the wildcards. If {@code substitution}
   * instantiates those capture variables, then this adds {@code T} to {@code typeParameters}.
   * Instantiating {@code T} lets {@link #satisfiesBounds(Substitution)} check that the types chosen
   * for the wildcards are consistent with one another.
   *
   * @param typeParameters type variables that {@code substitution} does not instantiate; is
   *     side-effected
   * @param substitution a substitution
   */
  private static void addBoundTypeVariables(
      Set<TypeVariable> typeParameters, Substitution substitution) {
    Set<TypeVariable> instantiated = substitution.keySet();
    Deque<TypeVariable> worklist = new ArrayDeque<>(instantiated);
    worklist.addAll(typeParameters);
    while (!worklist.isEmpty()) {
      TypeVariable variable = worklist.remove();
      List<TypeVariable> boundVariables =
          new ArrayList<>(variable.getLowerTypeBound().getTypeParameters());
      boundVariables.addAll(variable.getUpperTypeBound().getTypeParameters());
      for (TypeVariable boundVariable : boundVariables) {
        if (!instantiated.contains(boundVariable) && typeParameters.add(boundVariable)) {
          worklist.add(boundVariable);
        }
      }
    }
  }

  /**
   * Returns the candidate types for the given type variables that are consistent with the given
   * substitution.
   *
   * <p>A candidate type for one of the type variables, {@code X}, is in the result if it is one of
   * the {@link #candidateTypes(TypeVariable)} of {@code X}, and if, when {@code substitution} is
   * extended by mapping {@code X} to it, each type variable that {@code substitution} instantiates
   * and whose bounds contain {@code X} and no other uninstantiated type variable is within its
   * bounds. For example, for {@code <K, V> Map<K, V> unmodifiableMap(Map<? extends K, ? extends
   * V>)}, if {@code substitution} maps the capture of {@code ? extends K} to {@code Integer}, then
   * the result contains only supertypes of {@code Integer} for {@code K}.
   *
   * @param parameters type variables that {@code substitution} does not instantiate
   * @param substitution a substitution
   * @return the list of candidate lists for the parameters; empty if any parameter has no
   *     candidates
   */
  @SuppressWarnings("MixedMutabilityReturnType")
  private List<List<ReferenceType>> consistentCandidateTypes(
      List<TypeVariable> parameters, Substitution substitution) {
    Set<TypeVariable> instantiated = new LinkedHashSet<>(substitution.keySet());
    List<List<ReferenceType>> result = new ArrayList<>(parameters.size());
    for (TypeVariable parameter : parameters) {
      // The instantiated type variables whose bounds constrain the type of `parameter`.
      List<TypeVariable> constrained = new ArrayList<>();
      for (TypeVariable variable : instantiated) {
        Set<TypeVariable> boundVariables =
            new LinkedHashSet<>(variable.getLowerTypeBound().getTypeParameters());
        boundVariables.addAll(variable.getUpperTypeBound().getTypeParameters());
        boundVariables.removeAll(instantiated);
        if (boundVariables.equals(Collections.singleton(parameter))) {
          constrained.add(variable);
        }
      }
      List<ReferenceType> candidates = candidateTypes(parameter);
      if (!constrained.isEmpty()) {
        candidates.removeIf(
            candidate ->
                !satisfiesBounds(
                    constrained, substitution.extend(new Substitution(parameter, candidate))));
      }
      if (candidates.isEmpty()) {
        Log.logPrintf("No candidate types for %s consistent with %s%n", parameter, substitution);
        return Collections.emptyList();
      }
      result.add(candidates);
    }
    return result;
  }

  /**
   * Returns a randomly selected substitution that extends the given substitution by instantiating
   * the given type variables with the given candidate types, and that satisfies the bounds of all
   * the type variables that it instantiates.
   *
   * <p>If there are more than {@link #MAX_CHECKED_SUBSTITUTIONS} combinations of the candidates,
   * this checks that many random combinations rather than all of them.
   *
   * @param parameters type variables that {@code substitution} does not instantiate
   * @param candidates the candidate types for each of {@code parameters}; not empty
   * @param substitution a substitution to extend
   * @return an extending substitution that satisfies the bounds, or null if none was found
   * @see #satisfiesBounds(Substitution)
   */
  private static @Nullable Substitution selectSubstitutionSatisfyingBounds(
      List<TypeVariable> parameters,
      List<List<ReferenceType>> candidates,
      Substitution substitution) {
    if (combinationCount(candidates) <= MAX_CHECKED_SUBSTITUTIONS) {
      List<Substitution> substitutions =
          allSubstitutions(
              parameters, candidates, substitution, (tuple, extended) -> satisfiesBounds(extended));
      return substitutions.isEmpty() ? null : Randomness.randomMember(substitutions);
    }
    for (int i = 0; i < MAX_CHECKED_SUBSTITUTIONS; i++) {
      List<ReferenceType> tuple = new ArrayList<>(parameters.size());
      for (List<ReferenceType> parameterCandidates : candidates) {
        tuple.add(Randomness.randomMember(parameterCandidates));
      }
      Substitution extended = substitution.extend(parameters, tuple);
      if (satisfiesBounds(extended)) {
        return extended;
      }
    }
    return null;
  }

  /**
   * Returns the number of combinations of the given candidates, or {@link Long#MAX_VALUE} if it is
   * more than {@link #MAX_CHECKED_SUBSTITUTIONS}.
   *
   * @param candidates lists of candidate types, one list per type variable
   * @return the number of combinations of {@code candidates}, capped
   */
  private static long combinationCount(List<List<ReferenceType>> candidates) {
    long count = 1;
    for (List<ReferenceType> parameterCandidates : candidates) {
      count *= parameterCandidates.size();
      if (count > MAX_CHECKED_SUBSTITUTIONS) {
        return Long.MAX_VALUE;
      }
    }
    return count;
  }

  /**
   * Returns true if each type to which the given substitution maps a type variable is within the
   * bounds of that type variable, with the substitution applied to the bounds.
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
   * Returns true if the type to which the given substitution maps each of the given type variables
   * is within the bounds of that type variable, with the substitution applied to the bounds.
   *
   * @param parameters type variables that {@code substitution} instantiates
   * @param substitution a substitution
   * @return true if the type of each of {@code parameters} is within its bounds
   */
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
        result =
            allSubstitutions(
                genericParameters,
                candidateTypes(genericParameters),
                substitution,
                boundsCheck::test);
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
   * Returns the substitutions that extend the given substitution by instantiating the given type
   * variables with a combination of the given candidate types, and that satisfy the given
   * predicate.
   *
   * @param parameters the list of parameters to instantiate
   * @param candidates the candidate types for each of {@code parameters}
   * @param initialSubstitution the substitution to be extended by new substitutions
   * @param test a predicate on a combination of candidate types and the substitution that extends
   *     {@code initialSubstitution} by it
   * @return the list of instantiating substitutions; may be empty. The list is fresh and clients
   *     may side-effect it.
   */
  private static List<Substitution> allSubstitutions(
      List<TypeVariable> parameters,
      List<List<ReferenceType>> candidates,
      Substitution initialSubstitution,
      BiPredicate<List<ReferenceType>, Substitution> test) {
    List<Substitution> substitutionList = new ArrayList<>();
    for (List<ReferenceType> tuple : iteratorToIterable(new CombinationIterator<>(candidates))) {
      Substitution substitution = initialSubstitution.extend(parameters, tuple);
      if (test.test(tuple, substitution)) {
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
