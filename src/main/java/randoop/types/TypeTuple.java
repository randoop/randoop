package randoop.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import plume.UtilMDE;

/**
 * {@code TypeTuple} represents an ordered tuple of type objects.
 * Type tuples primarily used to represent the input types of operations.
 */
public class TypeTuple {

  private final ArrayList<GeneralType> list;

  public TypeTuple(List<GeneralType> list) {
    this.list = new ArrayList<>(list);
  }

  public TypeTuple() {
    this(new ArrayList<GeneralType>());
  }

  @Override
  public boolean equals (Object obj) {
    if (! (obj instanceof TypeTuple)) {
      return false;
    }
    TypeTuple tuple = (TypeTuple)obj;
    return list.equals(tuple.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(list);
  }

  /**
   * Return the number of components of the tuple
   *
   * @return the number of components of this tuple
   */
  public int size() {
    return list.size();
  }

  /**
   * Return the ith component type of this tuple.
   *
   * @param i  the component index
   * @return the component type at the position
   */
  public GeneralType get(int i) {
    assert 0 <= i && i < list.size();
    return list.get(i);
  }

  /**
   * Indicates whether the tuple is empty.
   *
   * @return true if the tuple has no components, false otherwise
   */
  public boolean isEmpty() {
    return list.isEmpty();
  }

  /**
   * Indicates whether the tuple has any generic components.
   *
   * @return true if any component of tupe is generic, false if none are
   */
  public boolean isGeneric() {
    for (GeneralType type : list) {
    if (type.isGeneric()) {
      return true;
    }
  }
  return false;
}

  @Override
  public String toString() {
    return "(" + UtilMDE.join(list, ", ") + ")";
  }

  /**
   * Applies a substitution to a type tuple, replacing any occurrences of type variables.
   * Resulting tuple may only be partially instantiated.
   *
   * @param substitution  the substitution
   * @return a new type tuple resulting from applying the given substitution to this tuple
   */
  public TypeTuple apply(Substitution substitution) throws RandoopTypeException {
    List<GeneralType> typeList = new ArrayList<>();
    for (GeneralType type : this.list) {
      GeneralType newType = type.apply(substitution);
      if (newType != null) {
        typeList.add(newType);
      } else {
        typeList.add(type);
      }
    }
    return new TypeTuple(typeList);
  }

}
