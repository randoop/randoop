package randoop.reflection;

import static org.plumelib.util.CollectionsPlume.iteratorToIterable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
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
   * Instantiate the given operation so that its output type is assignable to the given type. If
   * {@code goalType} is a parameterized type and the output type of {@code operation} is a
   * parameterized type of the same generic class, then the output type of the result is {@code
   * goalType}. For example, if {@code operation} is {@code static <T extends Marker> Box<T> of(T)}
   * and {@code goalType} is {@code Box<MarkerImpl>}, this method returns {@code static
   * Box<MarkerImpl> of(MarkerImpl)}. This method chooses type arguments for the remaining type
   * parameters, such as those that occur only in the parameter types of {@code operation}, as
   * {@link #instantiate(TypedClassOperation)} does.
   *
   * @param operation a generic operation
   * @param goalType a type
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
      // The substitution that makes the output type of the operation equal to the goal type.  The
      // output type of a constructor is a generic class type, such as Box<T>.
      Substitution substitution =
          matchingSubstitution((ParameterizedType) outputType, goalInstantiatedType);
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
   * Returns the substitution that, applied to the given parameterized type, yields the given
   * instantiated type. For example, for {@code Box<T>} and {@code Box<MarkerImpl>}, returns the
   * substitution that maps {@code T} to {@code MarkerImpl}.
   *
   * @param pattern a parameterized type that can contain type variables, such as {@code Box<T>}
   * @param instance the instantiated type of the same generic class as {@code pattern}, such as
   *     {@code Box<MarkerImpl>}
   * @return the substitution {@code s} such that {@code pattern.substitute(s)} equals {@code
   *     instance}, or null if there is no such substitution
   */
  private static @Nullable Substitution matchingSubstitution(
      ParameterizedType pattern, InstantiatedType instance) {
    Substitution substitution;
    if (pattern instanceof GenericClassType) {
      // A generic class type, such as Box<T>.
      substitution = instance.getTypeSubstitution();
    } else if (instance.isInstantiationOf(pattern)) {
      substitution = instance.getInstantiatingSubstitution(pattern);
    } else {
      return null;
    }
    if (substitution == null || !pattern.substitute(substitution).equals(instance)) {
      return null;
    }
    return substitution;
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
   *         <li>For each remaining type variable, compute its candidate types: the input types, and
   *             the types that the substitution maps type variables to, that are within its
   *             bounds, ignoring any bound that mentions another type variable, and that are
   *             consistent with the substitution; see {@link #consistentCandidatesByVariable(List,
   *             Substitution)}. If some type variable has no candidate types, the attempt fails.
   *         <li>Because the candidate types are computed for each type variable separately, a
   *             combination of them can violate the bounds. Return a randomly selected combination
   *             of candidate types that, when added to the substitution, satisfies the bounds; see
   *             {@link #selectSubstitutionSatisfyingBounds(Map, Substitution)}. If the search for
   *             such a combination finds none, the attempt fails.
   *       </ul>
   * </ol>
   *
   * <p>The remaining type variables in step 2 also include each type variable that occurs in the
   * bounds of an instantiated or remaining type variable, such as {@code T} in {@code <T> void
   * copy(List<? super T>, List<? extends T>)}, where {@code T} occurs only in the bounds of the
   * wildcards; see {@link #addTransitiveBoundTypeVariables(Set, Substitution)}. Checking the bounds
   * of the capture variables of the wildcards against the type chosen for {@code T} ensures that
   * the types chosen for the wildcards are consistent with one another.
   *
   * <p>If an attempt fails, and step 1 chose among several input types for some parameter type,
   * this makes another attempt, up to {@link #MAX_INSTANTIATION_ATTEMPTS} attempts in all.
   * Otherwise, another attempt would start from the same substitution, so this returns null.
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
            // The type need not be a parameterized type:  a non-generic member class of a generic
            // class, such as Outer<T>.Inner, is generic because its enclosing type is.
            List<ReferenceType> matches = matchingInputTypes((ClassOrInterfaceType) paramType);
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
                selectedType.getInstantiatingSubstitution((ReferenceType) origParamType);
            if (subst == null) {
              if (Log.isLoggingOn()) {
                failure = "no substitution instantiates " + origParamType + " as " + selectedType;
              }
              allMatched = false;
              break;
            }
            // The substitution can conflict with an earlier one, for example if two parameter
            // types contain copies of one capture variable.  Then extending would throw.
            if (!substitution.isConsistentWith(subst)) {
              if (Log.isLoggingOn()) {
                failure = subst + " for " + origParamType + " conflicts with " + substitution;
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
        addTransitiveBoundTypeVariables(typeParameters, substitution);

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
   * @param typeParameters type variables that {@code substitution} does not instantiate; this
   *     method adds to it
   * @param substitution a substitution
   */
  private static void addTransitiveBoundTypeVariables(
      Set<TypeVariable> typeParameters, Substitution substitution) {
    Set<? extends TypeVariable> instantiated = substitution.keySet();
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
   * Returns a map from each of the given type variables to its candidate types that are consistent
   * with the given substitution.
   *
   * <p>Let {@code X} be one of the given type variables. A type variable that {@code substitution}
   * instantiates is <i>constrained by</i> {@code X} if its bounds mention {@code X} and no other
   * type variable that {@code substitution} does not instantiate; see {@link
   * #constrainedVariables(Substitution)}. The map's value for {@code X} contains each type that
   * satisfies both of these conditions:
   *
   * <ul>
   *   <li>The type is one of the {@link #candidateTypes(TypeVariable)} of {@code X}; or it is one of
   *       the {@link #inferredTypes(Substitution)} that potentially satisfies the bounds of {@code
   *       X} (such a type need not be an input type: it can be a type argument of one); or it is the
   *       non-generic type that {@code substitution} gives to a type variable constrained by {@code
   *       X}.
   *   <li>When {@code substitution} is extended by mapping {@code X} to the type, each type
   *       variable constrained by {@code X} is within its bounds.
   * </ul>
   *
   * <p>For example, for {@code <K, V> Map<K, V> unmodifiableMap(Map<? extends K, ? extends V>)}, if
   * {@code substitution} maps the capture of {@code ? extends K} to {@code Integer}, then the
   * result contains only supertypes of {@code Integer} for {@code K}.
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
    List<ReferenceType> inferredTypes = inferredTypes(substitution);
    Map<TypeVariable, List<ReferenceType>> result = new LinkedHashMap<>();
    for (TypeVariable parameter : parameters) {
      List<TypeVariable> constrained =
          constrainedVariables.getOrDefault(parameter, Collections.emptyList());
      List<ReferenceType> candidates = candidateTypes(parameter);
      // A type that the substitution gives to another type variable is also a candidate, if it is
      // within the bounds of `parameter`.  For example, for `<E extends Enum<E>, T extends E> void
      // consume(List<? super T>)`, if the substitution maps the capture of `? super T` to MyEnum,
      // then MyEnum is a candidate for E, even if MyEnum is not an input type.
      candidates.addAll(candidateTypes(parameter, inferredTypes));
      if (!constrained.isEmpty()) {
        // The types of the constrained type variables are also candidates, even if they are not
        // input types.  For example, an input type can be List<Foo> though Foo is not an input
        // type.  If a capture of `? extends T` is instantiated to Foo, then Foo is a candidate for
        // T.  The bounds of `parameter` itself are checked when the substitution is completed.
        for (TypeVariable variable : constrained) {
          ReferenceType type = substitution.get(variable);
          if (type != null && !type.isGeneric() && !candidates.contains(type)) {
            candidates.add(type);
          }
        }
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
   * Returns the types that the given substitution maps type variables to and that are not {@link
   * #inputTypes input types}. Such a type, which may be a type argument of an input type, can
   * instantiate another type variable. For example, for {@code <T> void copy(List<? super T>,
   * List<? extends T>)}, if {@code substitution} maps both capture variables to {@code Integer}
   * because the input types contain {@code List<Integer>}, then {@code T} can be {@code Integer}
   * even if {@code Integer} is not an input type.
   *
   * @param substitution a substitution
   * @return the non-generic types in the range of {@code substitution} that are not input types
   */
  private List<ReferenceType> inferredTypes(Substitution substitution) {
    Set<ReferenceType> result = new LinkedHashSet<>();
    for (TypeVariable variable : substitution.keySet()) {
      ReferenceType type = substitution.get(variable);
      if (type != null
          && !type.isGeneric()
          && !type.equals(JavaTypes.NULL_TYPE)
          && !inputTypes.contains(type)) {
        result.add(type);
      }
    }
    return new ArrayList<>(result);
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
   * and trying each one's candidate types in random order. It checks the bounds of each type
   * variable as soon as every type variable in those bounds, and the type variable itself, is
   * instantiated. Thus, it abandons a partial substitution as soon as the partial substitution
   * violates some bound, rather than extending it to every combination of the remaining candidate
   * types. For example, for {@code <K, V extends K>}, once the search chooses {@code Integer} for
   * {@code K}, it rejects each candidate type for {@code V} that is not a subtype of {@code
   * Integer}.
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

    /**
     * The type variables to instantiate, each with its candidate types, in the order that the
     * search instantiates them.
     */
    private final List<Map.Entry<TypeVariable, List<ReferenceType>>> parameters;

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
      this.parameters = new ArrayList<>(candidatesByVariable.entrySet());
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
      TypeVariable parameter = parameters.get(index).getKey();
      List<ReferenceType> untried = new ArrayList<>(parameters.get(index).getValue());
      while (!untried.isEmpty() && remainingChecks > 0) {
        // Remove a random element of `untried`.
        int i = Randomness.nextRandomInt(untried.size());
        ReferenceType candidate = untried.get(i);
        untried.set(i, untried.get(untried.size() - 1));
        untried.remove(untried.size() - 1);

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
    return candidateTypes(argument, inputTypes);
  }

  /**
   * Returns the given types that potentially satisfy the bounds on the argument, as in {@link
   * #candidateTypes(TypeVariable)}.
   *
   * @param argument the type argument
   * @param types the types to choose from
   * @return the elements of {@code types} that are candidate instantiations of the type argument
   */
  private static List<ReferenceType> candidateTypes(
      TypeVariable argument, Collection<? extends Type> types) {
    ParameterBound lowerBound = getLowerBound(argument);
    ParameterBound upperBound = getUpperBound(argument);

    List<ReferenceType> typeList = new ArrayList<>(types.size());
    for (Type inputType : types) {
      if (inputType.isReferenceType()) {
        ReferenceType inputRefType = (ReferenceType) inputType;
        if (isWithinBounds(argument, inputRefType, lowerBound, upperBound)) {
          typeList.add(inputRefType);
        }
      }
    }
    return typeList;
  }

  /**
   * Returns true if the given type is within the given bounds, when it instantiates the given type
   * variable.
   *
   * @param argument the type variable
   * @param type a type
   * @param lowerBound the lower bound to test, from {@link #getLowerBound(TypeVariable)}
   * @param upperBound the upper bound to test, from {@link #getUpperBound(TypeVariable)}
   * @return true if {@code type} is within {@code lowerBound} and {@code upperBound}
   */
  private static boolean isWithinBounds(
      TypeVariable argument,
      ReferenceType type,
      ParameterBound lowerBound,
      ParameterBound upperBound) {
    Substitution substitution = new Substitution(argument, type);
    return lowerBound.isLowerBound(type, substitution)
        && upperBound.isUpperBound(type, substitution);
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
  private static ParameterBound getUpperBound(TypeVariable argument) {
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
  private static ParameterBound getLowerBound(TypeVariable argument) {
    ParameterBound lowerBound = argument.getLowerTypeBound();
    List<TypeVariable> parameters = lowerBound.getTypeParameters();
    if (parameters.isEmpty() || (parameters.size() == 1 && parameters.contains(argument))) {
      return lowerBound;
    }
    return ParameterBound.forType(JavaTypes.NULL_TYPE);
  }
}
