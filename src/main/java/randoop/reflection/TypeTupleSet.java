package randoop.reflection;

import java.util.ArrayList;
import java.util.List;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.TypeArgument;
import randoop.types.TypeVariable;

/**
 * Represents a set of lists of candidate type arguments for a {@link
 * randoop.types.ParameterizedType}, and uses this set to construct substitutions for a list of
 * {@link TypeArgument} objects.
 *
 * <p>Starts as a singleton list with the empty list, which is expanded by repeated use of the
 * {@link #extend(List)} method that adds new types to the lists in the set, then the set of {@link
 * Substitution} objects is created by a call to {@link #filter(List)}.
 */
class TypeTupleSet {

  /** The list of type lists (tuples) */
  private List<List<ReferenceType>> typeTuples;

  /** The length of tuples in the set. */
  private int tupleLength;

  /** Creates a tuple set with a single empty tuple. */
  TypeTupleSet() {
    this.typeTuples = new ArrayList<>();
    this.typeTuples.add(new ArrayList<ReferenceType>());
    this.tupleLength = 0;
  }

  /**
   * Extends all of the elements of the current tuple set with all of the types in the given list.
   * In other words, if there are <i>k</i> types given then each tuple will be replaced by <i>k</i>
   * new tuples extended by one of the input types.
   *
   * @param types the list of types
   */
  public void extend(List<ReferenceType> types) {
    tupleLength += 1;
    List<List<ReferenceType>> tupleList = new ArrayList<>();
    for (List<ReferenceType> tuple : typeTuples) {
      for (ReferenceType type : types) {
        List<ReferenceType> extTuple = new ArrayList<>(tuple);
        extTuple.add(type);
        assert extTuple.size() == tupleLength
            : "tuple lengths don't match, expected: " + tupleLength + " have " + extTuple.size();
        tupleList.add(extTuple);
      }
    }
    typeTuples = tupleList;
  }

  /**
   * Returns a list of substitutions for type tuples that can instantiate the given list of {@link
   * TypeArgument} objects, and filters the set of tuples.
   *
   * @param typeParameters the type arguments
   * @return the list of substitutions that instantiate the type arguments
   */
  List<Substitution<ReferenceType>> filter(List<TypeVariable> typeParameters) {
    assert typeParameters.size() == tupleLength
        : "tuple size " + tupleLength + " must equal number of parameters " + typeParameters.size();
    List<Substitution<ReferenceType>> substitutionSet = new ArrayList<>();
    List<List<ReferenceType>> tupleList = new ArrayList<>();
    for (List<ReferenceType> tuple : typeTuples) {
      Substitution<ReferenceType> substitution = Substitution.forArgs(typeParameters, tuple);

      int i = 0;
      while (i < tuple.size()
          && typeParameters.get(i).getUpperTypeBound().isUpperBound(tuple.get(i), substitution)) {
        i++;
      }
      if (i == tuple.size()) {
        substitutionSet.add(substitution);
        tupleList.add(tuple);
      }
    }
    typeTuples = tupleList;
    return substitutionSet;
  }
}
