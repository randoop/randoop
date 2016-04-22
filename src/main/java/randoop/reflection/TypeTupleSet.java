package randoop.reflection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import randoop.types.GeneralType;
import randoop.types.Substitution;
import randoop.types.TypeArgument;

/**
 * Represents a set of lists of candidate type arguments for a {@link randoop.types.ParameterizedType},
 * and uses this set to construct substitutions for a list of {@link TypeArgument} objects.
 * <p>
 * Starts as a singleton list with the empty list, which is expanded by repeated use of
 * the {@link #extend(List)} method that adds new types to the lists in the
 * set, then the set of {@link Substitution} objects is created by a call to {@link #filter(List)}.
 *
 * @see OperationModel#getSubstitutions(Set, GeneralType)
 */
public class TypeTupleSet {

  /** The list of type lists (tuples) */
  private List<List<GeneralType>> typeTuples;

  /** The length of tuples in the set */
  private int tupleLength;

  /**
   * Creates a tuple set with a single empty tuple.
   */
  public TypeTupleSet() {
    this.typeTuples = new ArrayList<>();
    this.typeTuples.add(new ArrayList<GeneralType>());
    this.tupleLength = 0;
  }

  /**
   * Extends all of the elements of the current tuple set with the types in
   * the given list.
   *
   * @param types  the list of types
   */
  public void extend(List<GeneralType> types) {
    tupleLength += types.size();
    List<List<GeneralType>> tupleList = new ArrayList<>();
    for (List<GeneralType> tuple : typeTuples) {
      for (GeneralType type : types) {
        List<GeneralType> extTuple = new ArrayList<>(tuple);
        extTuple.add(type);
        assert extTuple.size() == tupleLength : "tuple lengths don't match";
        tupleList.add(extTuple);
      }
    }
    typeTuples = tupleList;

  }

  /**
   * Returns a list of substitutions for type tuples that can instantiate the
   * given list of {@link TypeArgument} objects, and filters the set of tuples.
   *
   * @param typeParameters  the type arguments
   * @return the list of substitutions that instantiate the type arguments
   */
  List<Substitution> filter(List<TypeArgument> typeParameters) {
    assert typeParameters.size() == tupleLength: "tuple size must equal number of parameters";
    List<Substitution> substitutionSet = new ArrayList<>();
    List<List<GeneralType>> tupleList = new ArrayList<>();
    for (List<GeneralType> tuple : typeTuples) {
      Substitution substitution = Substitution.forArgs(typeParameters, tuple);

      int i = 0;
      while (i < tuple.size() && typeParameters.get(i).canBeInstantiatedAs(tuple.get(i))) {
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
