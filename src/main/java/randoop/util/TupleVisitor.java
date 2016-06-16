package randoop.util;

import java.util.List;

/**
 * Interface for visitors on elements of {@link TupleSet}.
 */
public interface TupleVisitor<E,T> {

  /**
   * Transforms a list representing a tuple of a {@link TupleSet}.
   * {@code <E>} is the type of elements of the tuples, and
   * {@code <T>} is the type returned by the transformation.
   *
   * @param tuple  the list to be transformed
   * @return the transformed list
   */
  T apply(List<E> tuple);
}
