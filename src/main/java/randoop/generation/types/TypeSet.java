package randoop.generation.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import plume.UtilMDE;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GenericClassType;
import randoop.types.InstantiatedType;
import randoop.types.ParameterizedType;
import randoop.types.ReferenceType;
import randoop.types.Type;

/**
 * A set of non-generic Types that is upward closed by supertypes: for type t in S, all supertypes s
 * of t are in the set S.
 */
public class TypeSet {

  /** The subset of non-parameterized types. */
  private final Set<Type> nonParameterizedTypes;

  /** The subset of parameterized types, indexed by underlying generic type */
  private final Map<GenericClassType, Set<InstantiatedType>> parameterizedTypes;

  /** The count of unique parameterized types. */
  private int parameterizedTypeCount;

  /** Creates an empty {@link TypeSet}. */
  TypeSet() {
    nonParameterizedTypes = new LinkedHashSet<>();
    parameterizedTypes = new HashMap<>();
    parameterizedTypeCount = 0;
  }

  /**
   * Creates a {@link TypeSet} containing the given nonParameterizedTypes and their supertypes.
   *
   * @param types the set of nonParameterizedTypes from which the {@link TypeSet} is created.
   */
  public TypeSet(Set<Type> types) {
    this();
    for (Type type : types) {
      this.add(type);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TypeSet)) {
      return false;
    }
    TypeSet other = (TypeSet) obj;
    return this.nonParameterizedTypes.equals(other.nonParameterizedTypes)
        && this.parameterizedTypes.equals(other.parameterizedTypes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nonParameterizedTypes, parameterizedTypes);
  }

  @Override
  public String toString() {
    return "{" + UtilMDE.join(new ArrayList<>(nonParameterizedTypes), ",") + "}";
  }

  /**
   * Adds the given type and its supertypes to this set.
   *
   * @param type the type to add
   * @return true if the type is added, and false if not
   */
  public boolean add(Type type) {
    assert !type.isGeneric() : "type set should only contain non-generic types";

    if (type.isParameterized()) {
      InstantiatedType parameterizedType = (InstantiatedType) type;
      GenericClassType genericType = parameterizedType.getGenericClassType();
      Set<InstantiatedType> types = parameterizedTypes.get(genericType);
      if (types == null) {
        types = new LinkedHashSet<>();
      }
      parameterizedTypeCount -= types.size();
      types.add(parameterizedType);
      parameterizedTypeCount += types.size();
      parameterizedTypes.put(genericType, types);
    } else {
      nonParameterizedTypes.add(type);
    }

    if (type.isObject()) {
      return true;
    }
    if (type.isClassType()) {
      ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
      for (ClassOrInterfaceType supertype : classType.getSuperTypes()) {
        if (!add(supertype)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Returns the list of nonParameterizedTypes that match the given {@link ParameterizedType}.
   *
   * @param searchType the type to search for matches
   * @return the list of matching nonParameterizedTypes
   */
  public List<Type> match(Type searchType) {
    List<Type> matches = new ArrayList<>();
    if (searchType.isParameterized() || searchType.isGeneric()) {
      ParameterizedType parameterizedSearchType = (ParameterizedType) searchType;
      Set<InstantiatedType> types =
          parameterizedTypes.get(parameterizedSearchType.getGenericClassType());
      if (types == null) {
        return new ArrayList<>();
      }
      if (searchType.isGeneric()) {
        return new ArrayList<Type>(types);
      }
      for (InstantiatedType type : types) {
        if (type.isInstantiationOf((ReferenceType) searchType)) {
          matches.add(type);
        }
      }
    } else if (nonParameterizedTypes.contains(searchType)) {
      matches.add(searchType);
    }

    return matches;
  }

  /**
   * Returns the size of this type set.
   *
   * @return the number of unique types in this set
   */
  public int size() {
    return nonParameterizedTypes.size() + parameterizedTypeCount;
  }

  /**
   * Indicates whether the given type is a member of this type set.
   *
   * @param type the type for which to search
   * @return true if the type is an element of this set.
   */
  public boolean contains(Type type) {
    if (type.isGeneric()) {
      return false;
    }
    if (type.isParameterized()) {
      GenericClassType keyType = ((InstantiatedType) type).getGenericClassType();
      Set<InstantiatedType> types = parameterizedTypes.get(keyType);
      return types != null && types.contains(type);
    }
    return nonParameterizedTypes.contains(type);
  }
}
