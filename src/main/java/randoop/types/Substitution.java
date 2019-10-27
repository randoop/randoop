package randoop.types;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * A substitution maps type parameters/variables (including wildcards) to concrete types. It
 * represents the instantiation of a generic class to a parameterized type.
 */
public class Substitution {

  /** The map from type variables to concrete types. */
  private Map<TypeVariable, ReferenceType> map;

  /**
   * Map on reflection types - used for testing bounds. Its keys are a subset of the keys of {@link
   * #map}: those that are type parameters as opposed to other type variables such as wildcards.
   */
  private Map<java.lang.reflect.Type, ReferenceType> rawMap;

  /** Create an empty substitution. */
  public Substitution() {
    map = new LinkedHashMap<>();
    rawMap = new LinkedHashMap<>();
  }

  /**
   * Make a copy of the given substitution.
   *
   * @param substitution the substitution to copy
   */
  public Substitution(Substitution substitution) {
    map = new LinkedHashMap<>(substitution.map);
    rawMap = new LinkedHashMap<>(substitution.rawMap);
  }

  /**
   * Create a substitution that maps the given type parameter to the given type argument.
   *
   * @param parameter the type parameter
   * @param argument the type argument
   */
  public Substitution(TypeVariable parameter, ReferenceType argument) {
    this();
    put(parameter, argument);
  }

  /**
   * Create a substitution from the type parameters to the corresponding type arguments.
   *
   * @param parameters the type parameters
   * @param arguments the type arguments
   */
  public Substitution(List<TypeVariable> parameters, ReferenceType... arguments) {
    this();
    assert parameters.size() == arguments.length
        : "parameters=" + parameters + "  arguments=" + Arrays.toString(arguments);
    for (int i = 0; i < parameters.size(); i++) {
      put(parameters.get(i), arguments[i]);
    }
  }

  /**
   * Create a substitution from the type parameters to the corresponding type arguments.
   *
   * @param parameters the type parameters
   * @param arguments the type arguments
   */
  public Substitution(List<TypeVariable> parameters, List<ReferenceType> arguments) {
    this();
    assert parameters.size() == arguments.size();
    for (int i = 0; i < parameters.size(); i++) {
      put(parameters.get(i), arguments.get(i));
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return true if the substitution maps are identical and false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Substitution)) {
      return false;
    }
    Substitution s = (Substitution) obj;
    return map.equals(s.map);
  }

  @Override
  public int hashCode() {
    return Objects.hash(map);
  }

  @Override
  public String toString() {
    return map.toString();
  }

  /**
   * Two substitutions are consistent if their type variables are disjoint or, if they both map the
   * same type variable, they map it to the same type. This is the test for whether this
   * substitution can be extended by the other substitution using {@link #extend(Substitution)}.
   *
   * @param substitution the other substitution to check for consistency with this substitution
   * @return true if the the substitutions are consistent, false otherwise
   */
  public boolean isConsistentWith(Substitution substitution) {
    for (Entry<TypeVariable, ReferenceType> entry : substitution.map.entrySet()) {
      if (this.map.containsKey(entry.getKey())
          && !this.get(entry.getKey()).equals(entry.getValue())) {
        return false;
      }
    }
    for (Entry<java.lang.reflect.Type, ReferenceType> entry : substitution.rawMap.entrySet()) {
      if (this.rawMap.containsKey(entry.getKey())
          && !this.get(entry.getKey()).equals(entry.getValue())) {
        return false;
      }
    }
    return true;
  }

  /** Throws an exception if its arguments are different non-null values. */
  private static BiFunction<ReferenceType, ReferenceType, ReferenceType> requireSameEntry =
      (v1, v2) -> {
        if (v1 == null) return v2;
        if (v2 == null) return v1;
        if (v1.equals(v2)) return v1;
        throw new IllegalArgumentException(
            String.format("Substitutions map a key to distinct types %s and %s", v1, v2));
      };

  /**
   * Creates a new substitution that contains the mappings of this substitution, extended by the
   * given mappings. If this and the additional mappings contain the same type variable, both must
   * map it to the same type.
   *
   * @param parameters the type parameters
   * @param arguments the type arguments
   * @return a new substitution that is this substitution extended by the given mappings
   */
  public Substitution extend(List<TypeVariable> parameters, List<ReferenceType> arguments) {
    return extend(new Substitution(parameters, arguments));
  }

  /**
   * Creates a new substitution that contains the entries of two substitutions. If both
   * substitutions contain the same type variable, they must map to the same type.
   *
   * @param other the substitution to add to this substitution
   * @return a new substitution that is this substitution extended by the given substitution
   */
  public Substitution extend(Substitution other) {
    Substitution result = new Substitution(this);
    for (Entry<TypeVariable, ReferenceType> entry : other.map.entrySet()) {
      result.map.merge(entry.getKey(), entry.getValue(), requireSameEntry);
    }
    for (Entry<java.lang.reflect.Type, ReferenceType> entry : other.rawMap.entrySet()) {
      result.rawMap.merge(entry.getKey(), entry.getValue(), requireSameEntry);
    }
    return result;
  }

  /**
   * Returns the concrete type mapped from the type variable by this substitution. Returns null if
   * the variable is not in the substitution.
   *
   * @param parameter the variable
   * @return the concrete type mapped from the variable in this substitution, or null if there is no
   *     type for the variable
   */
  public ReferenceType get(TypeVariable parameter) {
    return map.get(parameter);
  }

  /**
   * Returns the value for the given {@link java.lang.reflect.Type}
   *
   * @param parameter the type variable
   * @return the value for the type variable, or null if there is none
   */
  public ReferenceType get(Type parameter) {
    return rawMap.get(parameter);
  }

  /**
   * Returns the type variables mapped from by this.
   *
   * @return the type variables mapped from by this
   */
  public Set<TypeVariable> keySet() {
    return map.keySet();
  }

  /** Print the entries of this substitution to standard out on multiple lines. */
  public void print() {
    for (Entry<TypeVariable, ReferenceType> entry : map.entrySet()) {
      System.out.println(entry.getKey() + "(" + entry.getKey() + ")" + " := " + entry.getValue());
    }
  }

  /**
   * Add a type variable to concrete type mapping to the substitution.
   *
   * @param typeParameter the type variable
   * @param type the concrete type
   */
  private void put(TypeVariable typeParameter, ReferenceType type) {
    map.put(typeParameter, type);
    if (typeParameter instanceof ExplicitTypeVariable) {
      rawMap.put(((ExplicitTypeVariable) typeParameter).getReflectionTypeVariable(), type);
    }
  }

  /**
   * Indicates whether this substitution is empty.
   *
   * @return true if this has no substitution pairs, false otherwise
   */
  public boolean isEmpty() {
    return map.isEmpty();
  }
}
