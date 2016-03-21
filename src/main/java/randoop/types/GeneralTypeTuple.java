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
  public abstract int size();

  /**
   * Return the ith component type of this tuple.
   *
   * @param i  the component index
   * @return the component type at the position
   */
  public abstract GeneralType get(int i);

  /**
   * Indicates whether the tuple is empty.
   *
   * @return true if the tuple has no components, false otherwise
   */
  boolean isEmpty();
}
