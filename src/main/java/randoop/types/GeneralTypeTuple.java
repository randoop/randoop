package randoop.types;

/**
 * {@code GeneralTypeTuple} represents an ordered tuple of type objects.
 * Type tuples primarily used to represent the input types of operations.
 */
public interface GeneralTypeTuple {

  /**
   * Return the number of components of the tuple
   *
   * @return the number of components of this tuple
   */
  int size();

  /**
   * Return the ith component type of this tuple.
   *
   * @param i  the component index
   * @return the component type at the position
   */
  GeneralType get(int i);

  /**
   * Indicates whether the tuple is empty.
   *
   * @return true if the tuple has no components, false otherwise
   */
  boolean isEmpty();

  /**
   * Indicates whether the tuple has any generic components.
   *
   * @return true if any component of tupe is generic, false if none are
   */
  boolean isGeneric();

  /**
   * Applies a substitution to a type tuple, replacing any occurrences of type variables.
   * Resulting tuple may only be partially instantiated.
   *
   * @param substitution  the substitution
   * @return a new type tuple resulting from applying the given substitution to this tuple
   */
  GeneralTypeTuple apply(Substitution substitution) throws RandoopTypeException;

}
