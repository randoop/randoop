package randoop.sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import randoop.Globals;
import randoop.SubTypeSet;
import randoop.main.GenInputsAbstract;
import randoop.reflection.TypeInstantiator;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;
import randoop.util.ListOfLists;
import randoop.util.Log;
import randoop.util.SimpleArrayList;
import randoop.util.SimpleList;

/**
 * A collection of sequences that makes it efficient to ask for all the sequences that create a
 * value of a given type. This implements Randoop's pool. A SequenceCollection is the main field of
 * {@link randoop.generation.ComponentManager}.
 */
// Randoop often needs to find all the previously-generated sequences that create values of a
// given type. When Randoop kept all previously-generated sequences together, in a single
// collection, profiling showed that finding these sequences was a bottleneck in generation.
// This class makes the above search faster.
//
// To find all the sequences that create values of a given type, Randoop first uses the {@code
// SubTypeSet} to find the set {@code T} of feasible subtypes, and returns the range of {@code T}
// (that is, all the sequences mapped to by any t&isin;T) in the sequence map.
public class SequenceCollection {

  /** For each type, all the sequences that produce one or more values of exactly the given type. */
  private Map<Type, SimpleArrayList<Sequence>> sequenceMap = new LinkedHashMap<>();

  /**
   * A set of all the types that can be created with the sequences in this. This is the same as
   * {@code sequenceMap.keySet()}, but provides additional operations.
   */
  private SubTypeSet typeSet = new SubTypeSet(false);

  /**
   * A set of all the types that can be created with the sequences in this, and all their
   * supertypes. Thus, this may be larger than {@link #typeSet}.
   */
  private Set<Type> typesAndSupertypes = new TreeSet<>();

  /** Number of sequences in the collection: sum of sizes of all values in sequenceMap. */
  private int sequenceCount = 0;

  private void checkRep() {
    if (!GenInputsAbstract.debug_checks) {
      return;
    }
    if (sequenceMap.size() != typeSet.size()) {
      String b =
          "sequenceMap.keySet()="
              + Globals.lineSep
              + sequenceMap.keySet()
              + ", typeSet.types="
              + Globals.lineSep
              + typeSet.types;
      throw new IllegalStateException(b);
    }
  }

  public int size() {
    return sequenceCount;
  }

  /** Removes all sequences from this collection. */
  public void clear() {
    Log.logPrintf("Clearing sequence collection.%n");
    this.sequenceMap = new LinkedHashMap<>();
    this.typeSet = new SubTypeSet(false);
    sequenceCount = 0;
    checkRep();
  }

  /** Create a new, empty collection. */
  public SequenceCollection() {
    this(new ArrayList<Sequence>());
  }

  /**
   * Create a new collection and adds the given initial sequences.
   *
   * @param initialSequences the initial collection of sequences
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
   * @param col the sequences to add
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
   * @param components the sequences to add
   */
  public void addAll(SequenceCollection components) {
    for (SimpleArrayList<Sequence> s : components.sequenceMap.values()) {
      for (Sequence seq : s) {
        add(seq);
      }
    }
  }

  /**
   * Add a sequence to this collection. This method takes into account the active indices in the
   * sequence. If sequence[i] creates a values of type T, and sequence[i].isActive==true, then the
   * sequence is seen as creating a useful value at index i. More precisely, the method/constructor
   * at that index is said to produce a useful value (and if the user later queries for all
   * sequences that create a T, the sequence will be in the collection returned by the query). How a
   * value is deemed useful or not is left up to the client.
   *
   * <p>Note that this takes into consideration only the assigned value for each statement. If a
   * statement might side-effect some variable V, then V is considered as an output from the
   * statement that declares/creates V, not the one that side-effects V.
   *
   * <p>(An alternative would be to only use outputs from the last statement, and include its inputs
   * as well. That alternative is not implemented. It would probably be faster, but it would not
   * handle the case of a method side-effecting a variable that that was not explicitly passed to
   * it. That case probably isn't important/common.)
   *
   * @param sequence the sequence to add to this collection
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
        typesAndSupertypes.add(type);
        if (type.isClassOrInterfaceType()) {
          // This adds all the supertypes, not just immediate ones.
          typesAndSupertypes.addAll(((ClassOrInterfaceType) type).getSuperTypes());
        }
        typeSet.add(type);
        updateCompatibleMap(sequence, type);
      }
    }
    checkRep();
  }

  /**
   * Add the entry (type, sequeence) to sequenceMap.
   *
   * @param sequence the sequence
   * @param type the {@link Type}
   */
  private void updateCompatibleMap(Sequence sequence, Type type) {
    SimpleArrayList<Sequence> set = this.sequenceMap.get(type);
    if (set == null) {
      set = new SimpleArrayList<>();
      this.sequenceMap.put(type, set);
    }
    Log.logPrintf("Adding sequence of type %s of length %d%n", type, sequence.size());
    boolean added = set.add(sequence);
    assert added;
    sequenceCount++;
  }

  /**
   * Searches through the set of active sequences to find all sequences whose types match with the
   * parameter type.
   *
   * <p>If exactMatch==true returns only sequences that declare values of the exact class specified;
   * if exactMatch==false returns sequences declaring values of cls or any other class that can be
   * used as a cls (i.e. a subclass of cls).
   *
   * @param type the type desired for the sequences being sought
   * @param exactMatch the flag to indicate whether an exact type match is required
   * @param onlyReceivers if true, only return sequences that are appropriate to use as a method
   *     call receiver
   * @return list of sequence objects that are of type 'type' and abide by the constraints defined
   *     by nullOk
   */
  public SimpleList<Sequence> getSequencesForType(
      Type type, boolean exactMatch, boolean onlyReceivers) {

    if (type == null) {
      throw new IllegalArgumentException("type cannot be null.");
    }

    Log.logPrintf("getSequencesForType(%s, %s, %s)%n", type, exactMatch, onlyReceivers);

    List<SimpleList<Sequence>> resultList = new ArrayList<>();

    if (exactMatch) {
      SimpleList<Sequence> l = this.sequenceMap.get(type);
      if (l != null) {
        resultList.add(l);
      }
    } else {
      for (Type compatibleType : typeSet.getMatches(type)) {
        Log.logPrintf(
            "candidate compatibleType (isNonreceiverType=%s): %s%n",
            compatibleType.isNonreceiverType(), compatibleType);
        if (!(onlyReceivers && compatibleType.isNonreceiverType())) {
          SimpleArrayList<Sequence> newMethods = this.sequenceMap.get(compatibleType);
          Log.logPrintf("  Adding %d methods.%n", newMethods.size());
          resultList.add(newMethods);
        }
      }
    }

    if (resultList.isEmpty()) {
      Log.logPrintf("getSequencesForType: found no sequences matching type %s%n", type);
    }
    SimpleList<Sequence> selector = new ListOfLists<>(resultList);
    Log.logPrintf("getSequencesForType(%s) => %s sequences.%n", type, selector.size());
    return selector;
  }

  /**
   * Returns the set of all sequences in this collection.
   *
   * @return the set of all sequences in this collection
   */
  public Set<Sequence> getAllSequences() {
    Set<Sequence> result = new LinkedHashSet<>();
    for (SimpleArrayList<Sequence> a : sequenceMap.values()) {
      result.addAll(a);
    }
    return result;
  }

  public TypeInstantiator getTypeInstantiator() {
    return new TypeInstantiator(typesAndSupertypes);
  }

  public void log() {
    if (!Log.isLoggingOn()) {
      return;
    }
    for (Type t : sequenceMap.keySet()) {
      SimpleArrayList<Sequence> a = sequenceMap.get(t);
      int asize = a.size();
      Log.logPrintf("Type %s: %d sequences%n", t, asize);
      for (int i = 0; i < asize; i++) {
        Log.logPrintf("  #%d: %s%n", i, a.get(i).toString().trim().replace("\n", "\n       "));
      }
    }
  }
}
