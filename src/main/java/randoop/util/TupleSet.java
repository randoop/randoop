package randoop.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an extensible tuple of objects of the parameter type.
 */
public class TupleSet<E> {

  /** The list of element lists (tuples) */
  private List<List<E>> tuples;

  /** The length of tuples in the set */
  private int tupleLength;

  /**
   * Creates a tuple set with a single empty tuple.
   */
  public TupleSet() {
    this.tuples = new ArrayList<>();
    this.tuples.add(new ArrayList<E>());
    this.tupleLength = 0;
  }

  private TupleSet(List<List<E>> tuples, int tupleLength) {
    this.tuples = tuples;
    this.tupleLength = tupleLength;
  }

  /**
   * Extends all of the elements of the current tuple set with all of the elements of
   * the given list.
   * In other words, if there are <i>k</i> elements given then each tuple will be replaced by <i>k</i>
   * new tuples extended by one of the input elements.
   *
   * @param elements  the list of elements
   * @return a tuple set formed by extending the tuples with the elements of the given list
   */
  public TupleSet<E> extend(List<E> elements) {
    List<List<E>> tupleList = new ArrayList<>();
    for (List<E> tuple : tuples) {
      for (E e : elements) {
        List<E> extTuple = new ArrayList<>(tuple);
        extTuple.add(e);
        assert extTuple.size() == tupleLength + 1 : "tuple lengths don't match, expected " + tupleLength + " have " + extTuple.size();
        tupleList.add(extTuple);
      }
    }
    return new TupleSet<>(tupleList, tupleLength + 1);
  }

  /**
   * Creates a new tuple set from this set by inserting elements of the given list
   * at all positions in the tuple.
   *
   * @param elements  the list of elements
   * @return a tuple set formed by inserting elements of the given list into the tuples of this set
   */
  public TupleSet<E> exhaustivelyExtend(List<E> elements) {
    List<List<E>> tupleList = new ArrayList<>();
    for (List<E> tuple : tuples) {
      for (E e : elements) {
        for (int i = 0; i <= tuple.size(); i++) {
          tupleList.add(insert(e, tuple, i));
        }
      }
    }
    return new TupleSet<>(tupleList, tupleLength + 1);
  }

  /**
   * Returns a new list that is formed by inserting the element at the given position
   * in the tuple.
   *
   * @param e  the element to insert
   * @param tuple  the original list
   * @param i  the position where element is to be inserted
   * @return a new list with the element inserted at the given position
   */
  private List<E> insert(E e, List<E> tuple, int i) {
    List<E> extTuple = new ArrayList<>(tuple);
    if (i < tuple.size()) {
      extTuple.add(i, e);
    } else {
      extTuple.add(e);
    }
    return extTuple;
  }

  /**
   * Finds the first tuple that the visitor is able to transform,
   * and results the result of the transformation.
   *
   * @param visitor  the visitor that transforms a tuple
   * @param <T>  the return type of the visitor
   * @return a transformed tuple, or null
   */
  public <T> T findAndTransform(TupleVisitor<E,T> visitor) {
    for (List<E> tuple : tuples) {
      T transformedTuple = visitor.apply(tuple);
      if (transformedTuple != null) {
        return transformedTuple;
      }
    }
    return null;
  }
}
