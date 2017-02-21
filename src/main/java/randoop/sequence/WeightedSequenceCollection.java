package randoop.sequence;

import randoop.Globals;
import randoop.SubTypeSet;
import randoop.main.GenInputsAbstract;
import randoop.reflection.TypeInstantiator;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;
import randoop.util.*;

import java.util.*;

/**
 * Created by Justin on 2/20/2017.
 */
public class WeightedSequenceCollection {

  private Map<Type, WeightedList<Sequence>> sequenceMap = new LinkedHashMap<>(); // TODO

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
  public WeightedSequenceCollection() {
    this(new ArrayList<Sequence>());
  }

  /**
   * Create a new collection and adds the given initial sequences.
   *
   * @param initialSequences  the initial collection of sequences
   */
  public WeightedSequenceCollection(Collection<Sequence> initialSequences) {
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
        if (type.isClassType()) {
          sequenceTypes.addAll(((ClassOrInterfaceType) type).getSuperTypes());
        }
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
    WeightedList<Sequence> set = this.sequenceMap.get(type);
    if (set == null) {
      set = new WeightedList<>();
      this.sequenceMap.put(type, set);
    }
    if (Log.isLoggingOn()) Log.logLine("Adding sequence of type " + type);
    set.add(sequence);
    sequenceCount++;
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
  public WeightedList<Sequence> getSequencesForType(Type type, boolean exactMatch) {

    if (type == null) {
      throw new IllegalArgumentException("type cannot be null.");
    }

    if (Log.isLoggingOn()) {
      Log.logLine("getSequencesForType: entering method, type=" + type.toString());
    }

    List<WeightedList<Sequence>> resultList = new ArrayList<>();

    if (exactMatch) {
      WeightedList<Sequence> l = this.sequenceMap.get(type);
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

    // TODO
    return null;
  }

  public TypeInstantiator getTypeInstantiator() {
    return new TypeInstantiator(sequenceTypes);
  }
}
