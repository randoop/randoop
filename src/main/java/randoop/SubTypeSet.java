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

  /** The members of the set. */
  public ISimpleSet<Type> types;

  /**
   * Maps a type to all its proper subtypes that are in the set. If the mapped-to list is empty,
   * then the set contains no subtypes of the given type.
   */
  private IMultiMap<Type, Type> subTypes;

  /** If true, then {@link #mark} and {@link #undoLastStep()} are supported. */
  private boolean supportsCheckpoints;

  public SubTypeSet(boolean supportsCheckpoints) {
    if (supportsCheckpoints) {
      this.supportsCheckpoints = true;
      this.subTypes = new CheckpointingMultiMap<>();
      this.types = new CheckpointingSet<>();
    } else {
      this.supportsCheckpoints = false;
      this.subTypes = new MultiMap<>();
      this.types = new SimpleSet<>();
    }
  }

  /** Checkpoint the state of the data structure, for use by {@link #undoLastStep()}. */
  public void mark() {
    if (!supportsCheckpoints) {
      throw new RuntimeException("Operation not supported.");
    }
    ((CheckpointingMultiMap<Type, Type>) subTypes).mark();
    ((CheckpointingSet<Type>) types).mark();
  }

  /** Undo changes since the last call to {@link #mark()}. */
  public void undoLastStep() {
    if (!supportsCheckpoints) {
      throw new RuntimeException("Operation not supported.");
    }
    ((CheckpointingMultiMap<Type, Type>) subTypes).undoToLastMark();
    ((CheckpointingSet<Type>) types).undoToLastMark();
  }

  /**
   * Adds a type to this set.
   *
   * @param c the type to be added
   */
  public void add(Type c) {
    if (c == null) throw new IllegalArgumentException("c cannot be null.");
    if (types.contains(c)) {
      return;
    }
    types.add(c);

    // Update existing entries.
    for (Type cls : subTypes.keySet()) {
      if (cls.isAssignableFrom(c)) {
        if (!subTypes.getValues(cls).contains(c)) subTypes.add(cls, c);
      }
    }
  }

  private void addQueryType(Type type) {
    if (type == null) throw new IllegalArgumentException("c cannot be null.");
    Set<Type> keySet = subTypes.keySet();
    if (keySet.contains(type)) {
      return;
    }

    Set<Type> compatibleTypes = new LinkedHashSet<>();
    for (Type t : types.getElements()) {
      if (type.isAssignableFrom(t)) {
        compatibleTypes.add(t);
      }
    }
    for (Type cls : compatibleTypes) {
      subTypes.add(type, cls);
    }
  }

  // TODO: I think that the set does not contain {@code c} itself.  Check and document.

  /**
   * Returns all the classes in the set that can-be-used-as the given {@code c}.
   *
   * @param type the query type
   * @return the set of types that can be used in place of the query type
   */
  public Set<Type> getMatches(Type type) {
    if (!subTypes.keySet().contains(type)) {
      addQueryType(type);
    }
    return Collections.unmodifiableSet(subTypes.getValues(type));
  }

  /**
   * Returns the number of elements of this set.
   *
   * @return the size of the set
   */
  public int size() {
    return types.size();
  }

  /**
   * Returns the elements of this set.
   *
   * @return the elements of the set
   */
  public Set<Type> getElements() {
    return types.getElements();
  }
}
