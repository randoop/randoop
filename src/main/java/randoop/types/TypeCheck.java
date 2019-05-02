package randoop.types;

import java.util.List;

/** Predicate to check the type of a substitution for a list of type variables. */
public class TypeCheck {
  /** The type variables to check against. */
  private final List<TypeVariable> parameters;

  /**
   * Creates a {@link TypeCheck} predicate for a given list of type parameters.
   *
   * @param parameters the list of type parameters checked by the predicate
   * @return the {@link TypeCheck} object for the given parameters
   */
  public static TypeCheck forParameters(List<TypeVariable> parameters) {
    return new TypeCheck(parameters);
  }

  /**
   * Builds a {@link TypeCheck} object for the parameters.
   *
   * @param parameters the list of of type parameters to be checked by the predicate
   */
  private TypeCheck(List<TypeVariable> parameters) {
    this.parameters = parameters;
  }

  /**
   * Checks if the list of types satisfies the bounds of the type parameters given the substitution.
   * Requires that the list of types be the same length as the parameters. (Generally, the
   * substitution is constructed from the tuple instantiating the type parameters of this object,
   * but all that is require is that it instantiate any generic bounds of the the type parameters.)
   *
   * @param tuple the list of instantiating types
   * @param substitution substitution for checking bounds
   * @return true if instantiation of the parameters by the tuple is valid, false otherwise
   */
  public boolean test(List<ReferenceType> tuple, Substitution<ReferenceType> substitution) {
    int i = 0;
    while (i < tuple.size()
        && parameters.get(i).getLowerTypeBound().isLowerBound(tuple.get(i), substitution)
        && parameters.get(i).getUpperTypeBound().isUpperBound(tuple.get(i), substitution)) {
      i++;
    }
    return i == tuple.size();
  }
}
