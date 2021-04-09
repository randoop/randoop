package randoop.util;

import java.util.Set;

public interface ISimpleSet<E> {

  /**
   * Adds the given elt to the set.
   *
   * <p>Precondition: the given elt is not already in the set.
   *
   * @param elt cannot be null
   */
  void add(E elt);

  /**
   * Removes the given elt from the set.
   *
   * <p>Precondition: the given elt is in the set.
   *
   * @param elt cannot be null
   */
  void remove(E elt);

  /**
   * Returns true if elt is in this set.
   *
   * @param elt cannot be null
   * @return true if this set contains the element, false otherwise
   */
  boolean contains(E elt);

  /**
   * Returns the elements in this set, as a java.util.Set
   *
   * @return the elements of this set
   */
  Set<E> getElements();

  /**
   * Returns the size of this set.
   *
   * @return the size of this set
   */
  int size();

  /**
   * Returns a String representation of this set.
   *
   * @return a String representation of this set
   */
  @Override
  String toString();
}
