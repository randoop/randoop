package randoop;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import randoop.types.ConcreteType;
import randoop.types.Match;
import randoop.util.IMultiMap;
import randoop.util.ISimpleSet;
import randoop.util.MultiMap;
import randoop.util.ReversibleMultiMap;
import randoop.util.ReversibleSet;
import randoop.util.SimpleSet;

/**
 * A set of classes. This data structure additionally allows for efficient
 * answers to queries about can-be-used-as relationships.
 */
public class SubTypeSet {

  // The set of classes that have sequences. I.e. membership in this
  // set means that the SequenceCollection has one or more sequences that
  // create a value of the member type.
  public ISimpleSet<ConcreteType> typesWithsequences;

  // Maps a type to the list of subtypes that have sequences.
  // The list for a given type can be empty, which means that there
  // are no subtypes with sequences for the given type.
  private IMultiMap<ConcreteType, ConcreteType> subTypesWithsequences;

  private boolean reversible;

  public SubTypeSet(boolean reversible) {
    if (reversible) {
      this.reversible = true;
      this.subTypesWithsequences = new ReversibleMultiMap<>();
      this.typesWithsequences = new ReversibleSet<>();
    } else {
      this.reversible = false;
      this.subTypesWithsequences = new MultiMap<>();
      this.typesWithsequences = new SimpleSet<>();
    }
  }

  public void mark() {
    if (!reversible) {
      throw new RuntimeException("Operation not supported.");
    }
    ((ReversibleMultiMap<ConcreteType, ConcreteType>) subTypesWithsequences).mark();
    ((ReversibleSet<ConcreteType>) typesWithsequences).mark();
  }

  public void undoLastStep() {
    if (!reversible) {
      throw new RuntimeException("Operation not supported.");
    }
    ((ReversibleMultiMap<ConcreteType, ConcreteType>) subTypesWithsequences).undoToLastMark();
    ((ReversibleSet<ConcreteType>) typesWithsequences).undoToLastMark();
  }

  public void add(ConcreteType c) {
    if (c == null) throw new IllegalArgumentException("c cannot be null.");
    if (typesWithsequences.contains(c)) return;
    typesWithsequences.add(c);

    // Update existing entries.
    for (ConcreteType cls : subTypesWithsequences.keySet()) {
      if (cls.isAssignableFrom(c)) {
        if (!subTypesWithsequences.getValues(cls).contains(c)) subTypesWithsequences.add(cls, c);
      }
    }
  }

  private void addQueryType(ConcreteType type) {
    if (type == null) throw new IllegalArgumentException("c cannot be null.");
    Set<ConcreteType> keySet = subTypesWithsequences.keySet();
    if (keySet.contains(type)) return;

    Set<ConcreteType> compatibleTypesWithSequences = new LinkedHashSet<>();
    for (ConcreteType t : typesWithsequences.getElements()) {
      if (type.isAssignableFrom(t)) {
        compatibleTypesWithSequences.add(t);
      }
    }
    for (ConcreteType cls : compatibleTypesWithSequences) {
      subTypesWithsequences.add(type, cls);
    }
  }

  /**
   * Returns all the classes in the set that can-be-used-as the given
   * <code>c</code>.
   *
   * @param type  the query type
   * @return the set of types that can be used in place of the query type
   */
  public Set<ConcreteType> getMatches(ConcreteType type) {
    if (!subTypesWithsequences.keySet().contains(type)) {
      addQueryType(type);
    }
    return Collections.unmodifiableSet(subTypesWithsequences.getValues(type));
  }

  // TODO create tests for this method.
  /**
   * If <code>match==COMPATIBLE_TYPE</code>, returns <code>true</code> if this
   * set contains any classes that can-be-used-as the given class <code>c</code>
   * .
   *
   * <p>
   *
   * Otherwise, returns <code>true</code> if this set contains the given class
   * <code>c</code>
   *
   * @param type  the query type
   * @param match  the type matching criterion
   * @return true if either there is a sequence with the query type as its output
   *   type, or {@code match=COMPATIBLE_TYPE} and there is a sequence with a
   *   subtype of the query type as its output type.
   */
  public boolean containsAssignableType(ConcreteType type, Match match) {
    if (!subTypesWithsequences.keySet().contains(type)) {
      addQueryType(type);
    }

    return typesWithsequences.contains(type)
            || ((match == Match.COMPATIBLE_TYPE)
            && !subTypesWithsequences.getValues(type).isEmpty());

  }

  public int size() {
    return typesWithsequences.size();
  }

  public Set<ConcreteType> getElements() {
    return typesWithsequences.getElements();
  }
}
