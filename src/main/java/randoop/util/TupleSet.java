package randoop.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a non-empty set of tuples. Each tuple's elements are of type {@code E}. Each tuple has
 * the same length.
 */
public class TupleSet<E> {

  /** The list of element lists (tuples) */
  private List<List<E>> tuples;

  /** The length of tuples in the set */
  private int tupleLength;

  /** Creates a tuple set with a single empty tuple. */
  public TupleSet() {
    this.tuples = new ArrayList<>();
    this.tuples.add(new ArrayList<E>());
    this.tupleLength = 0;
  }

  private TupleSet(List<List<E>> tuples, int tupleLength) {
    this.tuples = tuples;
    this.tupleLength = tupleLength;
  }

  public List<List<E>> tuples() {
    return tuples;
  }

  /**
   * Extends all of the elements of the current tuple set with all of the elements of the given
   * list. In other words, if there are <i>k</i> elements given, then each tuple will be replaced by
   * <i>k</i> new tuples extended by one of the input elements.
   *
   * @param elements the list of elements
   * @return a tuple set formed by extending the tuples with the elements of the given list
   */
  public TupleSet<E> extend(List<E> elements) {
    List<List<E>> tupleList = new ArrayList<>();
    for (List<E> tuple : tuples) {
      for (E e : elements) {
        List<E> extTuple = new ArrayList<>(tuple);
        extTuple.add(e);
        assert extTuple.size() == tupleLength + 1
            : "tuple lengths don't match, expected " + tupleLength + " have " + extTuple.size();
        tupleList.add(extTuple);
      }
    }
    return new TupleSet<>(tupleList, tupleLength + 1);
  }

  /**
   * Creates a new tuple set from this set, where each tuple has been augmented by one element.
   *
   * <p>Suppose that each tuple of this has length <i>tlen</i>, and only 1 element is given. Then
   * each tuple will be be replaced by <i>tlen+1</i> tuples, each of length <i>tlen+1</i> and
   * containing the original tuple plus one element, at an arbitrary location in the tuple.
   *
   * <p>If <i>k</i> elements are given, then each tuple will be be replaced by <i>k * (tlen+1)</i>
   * tuples, each of length <i>tlen+1</i>.
   *
   * @param elements the list of elements
   * @return a tuple set formed by inserting elements of the given list into the tuples of this set
   */
  public TupleSet<E> exhaustivelyExtend(List<E> elements) {
    List<List<E>> tupleList = new ArrayList<>();
    for (List<E> tuple : tuples) {
      for (E e : elements) {
        for (int i = 0; i <= tuple.size(); i++) {
          tupleList.add(insert(tuple, e, i));
        }
      }
    }
    return new TupleSet<>(tupleList, tupleLength + 1);
  }

  /**
   * Returns a new list that is formed by inserting the element at the given position. Does not side
   * effect its argument.
   *
   * @param tuple the original list
   * @param e the element to insert
   * @param i the position where element is to be inserted
   * @return a new list with the element inserted at the given position
   */
  private List<E> insert(List<E> tuple, E e, int i) {
    List<E> extTuple = new ArrayList<>(tupleLength + 1);
    extTuple.addAll(tuple); // a bit inefficient to insert then shift, which could be avoided
    extTuple.add(i, e);
    return extTuple;
  }
}
