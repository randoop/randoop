package randoop.generation.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import randoop.operation.TypedClassOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GenericClassType;
import randoop.types.InstantiatedType;
import randoop.types.JDKTypes;
import randoop.types.JavaTypes;
import randoop.types.ParameterizedType;
import randoop.types.ReferenceArgument;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeArgument;
import randoop.types.TypeTuple;
import randoop.types.TypeVariable;
import randoop.util.Randomness;

/**
 * Function object to instantiate type parameters from a set of input types.
 */
public class TypeInstantiator {

  private final TypeSet inputTypes;

  /**
   * Creates a {@link randoop.generation.types.TypeInstantiator} object using the given types to
   * construct instantiating substitutions.
   *
   * @param inputTypes the ground types for instantiations
   */
  public TypeInstantiator(Set<Type> inputTypes) {
    this.inputTypes = new TypeSet(inputTypes);
  }

  public TypedClassOperation instantiate(TypedClassOperation operation) {
    assert operation.isGeneric() || operation.hasWildcardTypes()
        : "operation must be generic or have wildcards";

    Substitution<ReferenceType> substitution = new Substitution<>();
    Type outputType = operation.getOutputType();
    if (operation.isConstructorCall()
        || (operation.isStatic() && outputType.equals(operation.getDeclaringType()))) {
      if (outputType.isGeneric()) {
        if (outputType.isSubtypeOf(JDKTypes.SORTED_SET_TYPE)) {
          substitution = instantiateSortedSetType(operation);
        } else if (Randomness.weightedCoinFlip(0.5)) {
          substitution = selectMatch((ParameterizedType) outputType);
        }
      }
      if (substitution == null) {
        return null;
      }
      operation = operation.apply(substitution);
    }

    // if necessary, do capture conversion first
    if (operation != null && operation.hasWildcardTypes()) {
      operation = operation.applyCaptureConversion();
    }
    if (operation != null) {
      operation = instantiateTypes(operation);
    }

    return operation;
  }

  /**
   * Selects an instantiation of the generic types of an operation, and returns a new operation with
   * the types instantiated.
   *
   * @param operation  the operation
   * @return the operation with generic types instantiated
   */
  private TypedClassOperation instantiateTypes(TypedClassOperation operation) {
    //compile arguments into constraints -- note that argument type have constraint that exist in input types
    return null;
  }

  /**
   * Selects an existing type that instantiates the given generic declaring type
   * and returns the instantiating substitution.
   *
   * @param searchType  the generic type for which instantiation is to be found
   * @return a substitution instantiating given type as an existing type; null if no such type
   */
  private Substitution<ReferenceType> selectMatch(ParameterizedType searchType) {
    List<Type> matches = inputTypes.match(searchType);
    if (matches.isEmpty()) {
      return null;
    }
    InstantiatedType selectedType = (InstantiatedType) Randomness.randomSetMember(matches);
    return selectedType.getInstantiatingSubstitution(searchType);
  }

  /**
   * Returns a substitution that instantiates the {@code SortedSet} type of the given constructor.
   * A {@code SortedSet} may be built so that the element type {@code E} is ordered either
   * explicitly with a {@code Comparator<E>}, or the element type satisfies
   * {@code E implements Comparable<E>}.
   * The {@code SortedSet} documentation indicates that there should be four constructors, each
   * of which indicates the order to be used and so constrains the element type.
   *
   * @param operation  the constructor for the {@code SortedSet} subtype to be instantiated
   * @return the substitution to instantiate the element type of the {@code SortedSet} type
   */
  private Substitution<ReferenceType> instantiateSortedSetType(TypedClassOperation operation) {
    assert operation.isConstructorCall() : "only call with constructors of SortedSet subtype";

    TypeVariable parameter = operation.getDeclaringType().getTypeParameters().get(0);
    List<TypeVariable> parameters = new ArrayList<>();
    parameters.add(parameter);

    TypeTuple opInputTypes = operation.getInputTypes();

    // this is default constructor, choose a type E that is Comparable<E>
    if (opInputTypes.isEmpty()) {
      return getSearchTypeSubstitution(JavaTypes.COMPARABLE_TYPE, parameters);
    }

    if (opInputTypes.size() == 1) {
      ClassOrInterfaceType inputType = (ClassOrInterfaceType) opInputTypes.get(0);

      // this constructor has Comparator<E> arg, choose type E with Comparator<E> in sequence types
      if (inputType.isInstantiationOf(JDKTypes.COMPARATOR_TYPE)) {
        return getSearchTypeSubstitution(JDKTypes.COMPARATOR_TYPE, parameters);
      }

      // this constructor has Collection<E> arg, choose type E that is Comparable<E>
      if (inputType.isInstantiationOf(JDKTypes.COLLECTION_TYPE)) {
        return getSearchTypeSubstitution(JavaTypes.COMPARABLE_TYPE, parameters);
      }

      // this constructor has SortedSet<E> arg, choose existing matching type
      if (inputType.isInstantiationOf(JDKTypes.SORTED_SET_TYPE)) {
        return getSearchTypeSubstitution(JDKTypes.SORTED_SET_TYPE, parameters);
      }
    }

    return null;
  }

  /**
   * Constructs a type substitution for the given search type by first selecting a
   * matching type from the input types.
   *
   * @param searchType the generic search type to instantiate
   * @param parameters  the singleton list with one type parameter for the substitution
   * @return a substitution instantiating the given type parameter
   */
  private Substitution<ReferenceType> getSearchTypeSubstitution(
      GenericClassType searchType, List<TypeVariable> parameters) {
    Substitution<ReferenceType> substitution = selectMatch(searchType);
    if (substitution == null) {
      return null;
    }
    TypeArgument argumentType = searchType.apply(substitution).getTypeArguments().get(0);
    return Substitution.forArgs(parameters, ((ReferenceArgument) argumentType).getReferenceType());
  }
}
