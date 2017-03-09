package randoop;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import randoop.types.Type;
import randoop.util.IMultiMap;
import randoop.util.ISimpleSet;
import randoop.util.MultiMap;
import randoop.util.ReversibleMultiMap;
import randoop.util.ReversibleSet;
import randoop.util.SimpleSet;

/**
 * A set of classes. This data structure additionally allows for efficient answers to queries about
 * can-be-used-as relationships.
 */
public class SubTypeSet {

  // The set of classes that have sequences. I.e. membership in this
  // set means that the SequenceCollection has one or more sequences that
  // create a value of the member type.
  public ISimpleSet<Type> typesWithsequences;

  // Maps a type to the list of subtypes that have sequences.
  // The list for a given type can be empty, which means that there
  // are no subtypes with sequences for the given type.
  private IMultiMap<Type, Type> subTypesWithsequences;

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
    ((ReversibleMultiMap<Type, Type>) subTypesWithsequences).mark();
    ((ReversibleSet<Type>) typesWithsequences).mark();
  }

  public void undoLastStep() {
    if (!reversible) {
      throw new RuntimeException("Operation not supported.");
    }
    ((ReversibleMultiMap<Type, Type>) subTypesWithsequences).undoToLastMark();
    ((ReversibleSet<Type>) typesWithsequences).undoToLastMark();
  }

  public void add(Type c) {
    if (c == null) throw new IllegalArgumentException("c cannot be null.");
    if (typesWithsequences.contains(c)) return;
    typesWithsequences.add(c);

    // Update existing entries.
    for (Type cls : subTypesWithsequences.keySet()) {
      if (cls.isAssignableFrom(c)) {
        if (!subTypesWithsequences.getValues(cls).contains(c)) subTypesWithsequences.add(cls, c);
      }
    }
  }

  private void addQueryType(Type type) {
    if (type == null) throw new IllegalArgumentException("c cannot be null.");
    Set<Type> keySet = subTypesWithsequences.keySet();
    if (keySet.contains(type)) {
      return;
    }

    Set<Type> compatibleTypesWithSequences = new LinkedHashSet<>();
    for (Type t : typesWithsequences.getElements()) {
      if (type.isAssignableFrom(t)) {
        compatibleTypesWithSequences.add(t);
      }
    }
    for (Type cls : compatibleTypesWithSequences) {
      subTypesWithsequences.add(type, cls);
    }
  }

  /**
   * Returns all the classes in the set that can-be-used-as the given <code>c</code>.
   *
   * @param type the query type
   * @return the set of types that can be used in place of the query type
   */
  public Set<Type> getMatches(Type type) {
    if (!subTypesWithsequences.keySet().contains(type)) {
      addQueryType(type);
    }
    return Collections.unmodifiableSet(subTypesWithsequences.getValues(type));
  }

  // TODO create tests for this method.

  public int size() {
    return typesWithsequences.size();
  }

  public Set<Type> getElements() {
    return typesWithsequences.getElements();
  }
}
