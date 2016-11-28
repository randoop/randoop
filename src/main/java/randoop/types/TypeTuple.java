package randoop.types;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import plume.UtilMDE;

/**
 * {@code TypeTuple} represents an ordered tuple of type objects.
 * Type tuples primarily used to represent the input types of operations.
 */
public class TypeTuple {

  /** The sequence of types in this type tuple. */
  private final ArrayList<Type> list;

  /**
   * Creates a type tuple from the list of types, preserving the order.
   *
   * @param list  the list of types
   */
  public TypeTuple(List<Type> list) {
    this.list = new ArrayList<>(list);
  }

  /**
   * Creates an empty type tuple.
   */
  public TypeTuple() {
    this(new ArrayList<Type>());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TypeTuple)) {
      return false;
    }
    TypeTuple tuple = (TypeTuple) obj;
    return list.equals(tuple.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(list);
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
  public TypeTuple apply(Substitution<ReferenceType> substitution) {
    List<Type> typeList = new ArrayList<>();
    for (Type type : this.list) {
      Type newType = type.apply(substitution);
      if (newType != null) {
        typeList.add(newType);
      } else {
        typeList.add(type);
      }
    }
    return new TypeTuple(typeList);
  }

  /**
   * Applies a capture conversion to each component of this type type tuple.
   *
   * @return a new type tuple after performing a capture conversion
   */
  public TypeTuple applyCaptureConversion() {
    List<Type> typeList = new ArrayList<>();
    for (Type type : this.list) {
      typeList.add(type.applyCaptureConversion());
    }
    return new TypeTuple(typeList);
  }

  /**
   * Return the ith component type of this tuple.
   *
   * @param i  the component index
   * @return the component type at the position
   */
  public Type get(int i) {
    return list.get(i);
  }

  /**
   * Returns the type parameters that occur in any component of this type tuple.
   *
   * @return the list of type parameters for this type tuple
   */
  public List<TypeVariable> getTypeParameters() {
    Set<TypeVariable> paramSet = new LinkedHashSet<>();
    for (Type type : this.list) {
      if (type.isReferenceType()) {
        paramSet.addAll(((ReferenceType) type).getTypeParameters());
      }
    }
    return new ArrayList<>(paramSet);
  }

  /**
   * Indicates whether any of the types in this type tuple contains a wildcard.
   *
   * @return true if there is at least one wildcard occurrence
   */
  public boolean hasWildcard() {
    for (Type type : list) {
      if (type.isParameterized() && ((ParameterizedType) type).hasWildcard()) {
        return true;
      }
    }
    return false;
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
   * Return the number of components of the tuple
   *
   * @return the number of components of this tuple
   */
  public int size() {
    return list.size();
  }

  /**
   * Indicates whether the tuple has any generic components.
   *
   * @return true if any component of tuple is generic, false if none are
   */
  public boolean isGeneric() {
    for (Type type : list) {
      if (type.isGeneric()) {
        return true;
      }
    }
    return false;
  }
}
