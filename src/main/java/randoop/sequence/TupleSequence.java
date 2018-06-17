package randoop.sequence;

import java.util.ArrayList;
import java.util.List;

/**
 * A Sequence that produces a tuple or collection of values, all of the same type.
 *
 * <p>To make a new sequence requires choosing an operation, choosing a value (really, a sequence
 * and one of its outputs) for each of the operation's inputs, and then concatenating all the inputs
 * along with the new operation. Most operations take a well-defined number and type of inputs. This
 * helper class supports calling operations that take a variable number of inputs -- namely, array
 * and list creation.
 *
 * <p>Ordinarily:
 *
 * <ul>
 *   <li>Each Sequence consists of a single operation (the final statement) and its inputs. Those
 *       inputs are individually chosen and may be of different types, because the operation takes
 *       multiple types of arguments.
 *   <li>Each Sequence creates multiple values: the value produced by its last statement, and the
 *       values that might be side-effected by its last statement.
 *   <li>Each valid Sequence is output as as regression test. All of its values are in the pool, and
 *       when Randoop needs a value, it might choose any of them. Only one of its outputs is used
 *       when it is used as a component in a larger sequence.
 * </ul>
 *
 * This sequence is different.
 *
 * <ul>
 *   <li>It is created as the concatenation of valid sequences.
 *   <li>Its ouptuts are one value from each of the sequences that created it.
 *   <li>It is not output as a regression test, but is used as one of the inputs to an array- or
 *       list-producing operation that will be output. All of its ouptuts values are used.
 * </ul>
 */
public final class TupleSequence {

  /** The underlying sequence. */
  public Sequence sequence;

  /** The list of statement indices that define outputs of this sequence. */
  private List<Integer> outputIndices;

  /**
   * Create a TupleSequence that concatenates the given sequences, choosing the given variable from
   * each.
   *
   * @param sequences that will be concatenated to mkae the new TupleSequence
   * @param variables one index per sequence in {@code sequences}, defining the ouptuts of the
   *     TupleSequence
   * @param a TupleSequence that concatenates the given sequences
   */
  public TupleSequence(List<Sequence> sequences, List<Integer> variables) {
    assert sequences.size() == variables.size() : "must be one variable for each sequence";
    sequence = Sequence.concatenate(sequences);
    List<Integer> outputIndices = new ArrayList<>();
    int size = 0;
    for (int i = 0; i < sequences.size(); i++) {
      outputIndices.add(size + variables.get(i));
      size += sequences.get(i).size();
    }
  }

  /**
   * Returns the list of output indices.
   *
   * @return the list of output indices for this sequence
   */
  public List<Integer> getOutputIndices() {
    return outputIndices;
  }
}
