package randoop;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import randoop.types.Type;
import randoop.util.CheckpointingMultiMap;
import randoop.util.CheckpointingSet;
import randoop.util.IMultiMap;
import randoop.util.ISimpleSet;
import randoop.util.MultiMap;
import randoop.util.SimpleSet;

/**
 * A set of classes. This data structure additionally allows for efficient answers to queries about
 * can-be-used-as relationships.
 */
public class SubTypeSet {

  // The members of the set.
  public ISimpleSet<Type> typesWithsequences;

  // Maps a type to the list of subtypes in the set.
  // The list for a given type can be empty, which means that the set contains
  // no subtypes for the given type.
  private IMultiMap<Type, Type> subTypesWithsequences;

  private boolean supportsCheckpoints;

  public SubTypeSet(boolean supportsCheckpoints) {
    if (supportsCheckpoints) {
      this.supportsCheckpoints = true;
      this.subTypesWithsequences = new CheckpointingMultiMap<>();
      this.typesWithsequences = new CheckpointingSet<>();
    } else {
      this.supportsCheckpoints = false;
      this.subTypesWithsequences = new MultiMap<>();
      this.typesWithsequences = new SimpleSet<>();
    }
  }

  /** Checkpoint the state of the data structure, for use by {@link #undoLastStep()}. */
  public void mark() {
    if (!supportsCheckpoints) {
      throw new RuntimeException("Operation not supported.");
    }
    ((CheckpointingMultiMap<Type, Type>) subTypesWithsequences).mark();
    ((CheckpointingSet<Type>) typesWithsequences).mark();
  }

  /** Undo changes since the last call to {@link #mark()}. */
  public void undoLastStep() {
    if (!supportsCheckpoints) {
      throw new RuntimeException("Operation not supported.");
    }
    ((CheckpointingMultiMap<Type, Type>) subTypesWithsequences).undoToLastMark();
    ((CheckpointingSet<Type>) typesWithsequences).undoToLastMark();
  }

  public void add(Type c) {
    if (c == null) throw new IllegalArgumentException("c cannot be null.");
    if (typesWithsequences.contains(c)) {
      return;
    }
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
   * Returns all the classes in the set that can-be-used-as the given {@code c}.
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
