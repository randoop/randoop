package randoop.sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.Globals;
import randoop.SubTypeSet;
import randoop.main.GenInputsAbstract;
import randoop.reflection.TypeInstantiator;
import randoop.types.Type;
import randoop.util.ArrayListSimpleList;
import randoop.util.ListOfLists;
import randoop.util.Log;
import randoop.util.SimpleList;

/**
 * A collection of sequences that makes its efficient to ask for all the
 * sequences that create a value of a given type.
 *
 * <p>
 * RANDOOP IMPLEMENTATION NOTE.
 * <p>
 *
 * When creating new sequences, Randoop often needs to search for all the
 * previously-generated sequences that create one or more values of a given
 * type. Since this set can contain thousands of sequences, finding these
 * sequences can can be time-consuming and a bottleneck in generation (as we
 * discovered during profiling).
 *
 * <p>
 *
 * This class makes the above search faster by maintaining two data structures:
 *
 * <ul>
 * <li>A map from types to the sets of all sequences that create one or more
 * values of exactly the given type.
 *
 * <li>A set of all the types that can be created with the existing set of
 * sequences. The set is maintained as a {@link SubTypeSet} that allows for
 * quick queries about can-be-used-as relationships among the types in the set.
 * </ul>
 *
 * To find all the sequences that create values of a given type, Randoop first
 * uses the <code>SubTypeSet</code> to find the set <code>S</code> of feasible
 * subtypes in set of sequences, and returns the range of <code>S</code> in the
 * sequence map.
 */
public class SequenceCollection {

  // We make it a list to make it easier to pick out an element at random.
  private Map<Type, ArrayListSimpleList<Sequence>> sequenceMap = new LinkedHashMap<>();

  private SubTypeSet typeSet = new SubTypeSet(false);

  private Set<Type> sequenceTypes = new LinkedHashSet<>();

  private int sequenceCount = 0;

  private void checkRep() {
    if (!GenInputsAbstract.debug_checks) return;
    if (sequenceMap.size() != typeSet.size()) {
      String b =
          "activesequences types="
              + Globals.lineSep
              + sequenceMap.keySet()
              + ", typesWithsequencesMap types="
              + Globals.lineSep
              + typeSet.typesWithsequences;
      throw new IllegalStateException(b);
    }
  }

  public int size() {
    return sequenceCount;
  }

  /**
   * Removes all sequences from this collection.
   */
  public void clear() {
    if (Log.isLoggingOn()) Log.logLine("Clearing sequence collection.");
    this.sequenceMap = new LinkedHashMap<>();
    this.typeSet = new SubTypeSet(false);
    sequenceCount = 0;
    checkRep();
  }

  /**
   * Create a new, empty collection.
   */
  public SequenceCollection() {
    this(new ArrayList<Sequence>());
  }

  /**
   * Create a new collection and adds the given initial sequences.
   *
   * @param initialSequences  the initial collection of sequences
   */
  public SequenceCollection(Collection<Sequence> initialSequences) {
    if (initialSequences == null) throw new IllegalArgumentException("initialSequences is null.");
    this.sequenceMap = new LinkedHashMap<>();
    this.typeSet = new SubTypeSet(false);
    sequenceCount = 0;
    addAll(initialSequences);
    checkRep();
  }

  /**
   * All all sequences to this collection.
   *
   * @param col  the sequences to add
   */
  public void addAll(Collection<Sequence> col) {
    if (col == null) {
      throw new IllegalArgumentException("col is null");
    }
    for (Sequence c : col) {
      add(c);
    }
  }

  /**
   * Add all sequences to this collection.
   *
   * @param components  the sequences to add
   */
  public void addAll(SequenceCollection components) {
    for (ArrayListSimpleList<Sequence> s : components.sequenceMap.values()) {
      for (Sequence seq : s.theList) {
        add(seq);
      }
    }
  }

  /**
   * Add a sequence to this collection. This method takes into account the
   * active indices in the sequence. If sequence[i] creates a values of type T,
   * and sequence[i].isActive==true, then the sequence is seen as creating a
   * useful value at index i. More precisely, the method/constructor at that
   * index is said to produce a useful value (and if the user later queries for
   * all sequences that create a T, the sequence will be in the collection
   * returned by the query). How a value is deemed useful or not is left up to
   * the client.
   *
   * @param sequence  the sequence to add to this collection
   */
  public void add(Sequence sequence) {
    List<Type> formalTypes = sequence.getTypesForLastStatement();
    List<Variable> arguments = sequence.getVariablesOfLastStatement();
    assert formalTypes.size() == arguments.size();
    for (int i = 0; i < formalTypes.size(); i++) {
      Variable argument = arguments.get(i);
      assert formalTypes.get(i).isAssignableFrom(argument.getType())
          : formalTypes.get(i).getName()
              + " should be assignable from "
              + argument.getType().getName();
      if (sequence.isActive(argument.getDeclIndex())) {
        Type type = formalTypes.get(i);
        sequenceTypes.add(type);
        typeSet.add(type);
        updateCompatibleMap(sequence, type);
      }
    }
    checkRep();
  }

  /**
   * Add an entry from the given type to the sequence to the map.
   *
   * @param sequence  the sequence
   * @param type  the {@link Type}
   */
  private void updateCompatibleMap(Sequence sequence, Type type) {
    ArrayListSimpleList<Sequence> set = this.sequenceMap.get(type);
    if (set == null) {
      set = new ArrayListSimpleList<>();
      this.sequenceMap.put(type, set);
    }
    if (Log.isLoggingOn()) Log.logLine("Adding sequence of type " + type);
    boolean added = set.add(sequence);
    sequenceCount++;
    assert added;
  }

  /**
   * Searches through the set of active sequences to find all sequences whose
   * types match with the parameter type.
   *
   * @param type  the type desired for the sequences being sought
   * @param exactMatch  the flag to indicate whether an exact type match is required
   * @return list of sequence objects that are of type 'type' and abide by the
   *         constraints defined by nullOk
   */
  public SimpleList<Sequence> getSequencesForType(Type type, boolean exactMatch) {

    if (type == null) {
      throw new IllegalArgumentException("type cannot be null.");
    }

    if (Log.isLoggingOn()) {
      Log.logLine("getSequencesForType: entering method, type=" + type.toString());
    }

    List<SimpleList<Sequence>> resultList = new ArrayList<>();

    if (exactMatch) {
      SimpleList<Sequence> l = this.sequenceMap.get(type);
      if (l != null) {
        resultList.add(l);
      }
    } else {
      for (Type compatibleType : typeSet.getMatches(type)) {
        resultList.add(this.sequenceMap.get(compatibleType));
      }
    }

    if (resultList.isEmpty()) {
      if (Log.isLoggingOn()) {
        Log.logLine("getSequencesForType: found no sequences matching type " + type);
      }
    }
    SimpleList<Sequence> selector = new ListOfLists<>(resultList);
    if (Log.isLoggingOn()) {
      Log.logLine("getSequencesForType: returning " + selector.size() + " sequences.");
    }
    return selector;
  }

  /**
   * Returns the set of all sequences in this collection.
   *
   * @return  the set of all sequences in this collection
   */
  public Set<Sequence> getAllSequences() {
    Set<Sequence> result = new LinkedHashSet<>();
    for (ArrayListSimpleList<Sequence> a : sequenceMap.values()) {
      result.addAll(a.theList);
    }
    return result;
  }

  public TypeInstantiator getTypeInstantiator() {
    return new TypeInstantiator(sequenceTypes);
  }
}
