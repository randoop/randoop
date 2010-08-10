package randoop;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import randoop.util.IMultiMap;
import randoop.util.ISimpleSet;
import randoop.util.MultiMap;
import randoop.util.Reflection;
import randoop.util.ReversibleMultiMap;
import randoop.util.ReversibleSet;
import randoop.util.SimpleSet;

/**
 * A set of classes. This data structure additionally allows for
 * efficient answers to queries about can-be-used-as
 * ({@link randoop.util.Reflection#canBeUsedAs(Class,Class)})
 *  relationships.
 */
public class SubTypeSet {

  // The set of classes that have sequences. I.e. membership in this
  // set means that the SequenceCollection has one or more sequences that
  // create a value of the member type.
  public ISimpleSet<Class<?>> typesWithsequences;

  // Maps a type to the list of subtypes that have sequences.
  // The list for a given type can be empty, which means that there
  // are no subtypes with sequences for the given type.
  public IMultiMap<Class<?>, Class<?>> subTypesWithsequences;
  
  public boolean reversible;

  public SubTypeSet(boolean reversible) {
    if (reversible) {
      this.reversible = true;
      this.subTypesWithsequences = new ReversibleMultiMap<Class<?>, Class<?>>();
      this.typesWithsequences = new ReversibleSet<Class<?>>();
    } else {
      this.reversible = false;
      this.subTypesWithsequences = new MultiMap<Class<?>, Class<?>>();
      this.typesWithsequences = new SimpleSet<Class<?>>();
    }
  }
  
  public void mark() {
    if (!reversible) {
      throw new RuntimeException("Operation not supported.");
    }
    ((ReversibleMultiMap<Class<?>, Class<?>>)subTypesWithsequences).mark();
    ((ReversibleSet<Class<?>>)typesWithsequences).mark();
  }

  public void undoLastStep() {
    if (!reversible) {
      throw new RuntimeException("Operation not supported.");
    }
    ((ReversibleMultiMap<Class<?>, Class<?>>)subTypesWithsequences).undoToLastMark();
    ((ReversibleSet<Class<?>>)typesWithsequences).undoToLastMark();    
  }
  
  public void add(Class<?> c) {
    if (c == null)
      throw new IllegalArgumentException("c cannot be null.");
    if (typesWithsequences.contains(c))
      return;
    typesWithsequences.add(c);

    // Update existing entries.
    for (Class<?> cls : subTypesWithsequences.keySet()) {
      if (Reflection.canBeUsedAs(c, cls)) {
        if (!subTypesWithsequences.getValues(cls).contains(c))
          subTypesWithsequences.add(cls, c);
      }
    }
  }

  private void addQueryType(Class<?> c) {
    if (c == null)
      throw new IllegalArgumentException("c cannot be null.");
    Set<Class<?>> keySet = subTypesWithsequences.keySet();
    if (keySet.contains(c))
      return;

    Set<Class<?>> classesWithsequencesForC = new LinkedHashSet<Class<?>>();
    for (Class<?> classWithsequence : typesWithsequences.getElements()) {
      if (Reflection.canBeUsedAs(classWithsequence, c)) {
        classesWithsequencesForC.add(classWithsequence);
      }
    }
    for (Class<?> cls : classesWithsequencesForC) {
      subTypesWithsequences.add(c, cls);
    }
  }

  /**
   * Returns all the classes in the set that can-be-used-as the given
   * <code>c</code>.
   */
  public Set<Class<?>> getMatches(Class<?> c) {
    if (!subTypesWithsequences.keySet().contains(c)) {
      addQueryType(c);
    }
    return Collections.unmodifiableSet(subTypesWithsequences.getValues(c));
  }

  // TODO create tests for this method.
  /**
   * If <code>match==COMPATIBLE_TYPE</code>, returns <code>true</code> if this
   * set contains any classes that can-be-used-as the given class <code>c</code>.
   *
   * <p>
   *
   * Otherwise, returns <code>true</code> if this set contains the
   * given class <code>c</code>
   */
  public boolean containsAssignableType(Class<?> c, Reflection.Match match) {
    if (!subTypesWithsequences.keySet().contains(c)) {
      addQueryType(c);
    }

    if (typesWithsequences.contains(c))
      return true;

    if (match == Reflection.Match.COMPATIBLE_TYPE) {
      return ! subTypesWithsequences.getValues(c).isEmpty();
    }
    return false;
  }

  public int size() {
    return typesWithsequences.size();
  }

  public Set<Class<?>> getElements() {
    return typesWithsequences.getElements();
  }

}
