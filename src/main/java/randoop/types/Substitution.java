package randoop.types;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;

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
  private Map<Type, ReferenceType> rawMap;

  /**
   * The identities of the type variables that occur in a type that this substitution maps a
   * different type variable to; null if not yet computed. Use {@link #getReplacementVariables()}
   * instead of reading this field.
   */
  private @Nullable Set<Object> replacementVariables = null;

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
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
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
   * @return true if the substitutions are consistent, false otherwise
   */
  @SuppressWarnings("nullness:dereference.of.nullable") // TODO
  public boolean isConsistentWith(Substitution substitution) {
    for (Map.Entry<TypeVariable, ReferenceType> entry : substitution.map.entrySet()) {
      TypeVariable key = entry.getKey();
      if (this.map.containsKey(key) && !this.get(key).equals(entry.getValue())) {
        return false;
      }
    }
    for (Map.Entry<Type, ReferenceType> entry : substitution.rawMap.entrySet()) {
      Type key = entry.getKey();
      if (this.rawMap.containsKey(key) && !this.get(key).equals(entry.getValue())) {
        return false;
      }
    }
    return true;
  }

  /** Throws an exception if its arguments are different non-null values. */
  private static BiFunction<ReferenceType, ReferenceType, ReferenceType> requireSameEntry =
      (v1, v2) -> {
        if (v1 == null) {
          return v2;
        }
        if (v2 == null) {
          return v1;
        }
        if (v1.equals(v2)) {
          return v1;
        }
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
    for (Map.Entry<TypeVariable, ReferenceType> entry : other.map.entrySet()) {
      result.map.merge(entry.getKey(), entry.getValue(), requireSameEntry);
    }
    for (Map.Entry<Type, ReferenceType> entry : other.rawMap.entrySet()) {
      result.rawMap.merge(entry.getKey(), entry.getValue(), requireSameEntry);
    }
    return result;
  }

  /**
   * Returns a copy of this substitution in which the given type variable maps to the given type,
   * whether or not this substitution maps the type variable.
   *
   * @param parameter the type variable
   * @param argument the type that {@code parameter} maps to
   * @return a copy of this substitution that maps {@code parameter} to {@code argument}
   */
  Substitution replace(TypeVariable parameter, ReferenceType argument) {
    Substitution result = new Substitution(this);
    result.put(parameter, argument);
    return result;
  }

  /**
   * Returns a copy of this substitution that does not map the given type variable or any copy of
   * it.
   *
   * @param parameter the type variable
   * @return a copy of this substitution that does not map {@code parameter}
   */
  Substitution without(TypeVariable parameter) {
    Substitution result = new Substitution(this);
    result.map.keySet().removeIf(key -> TypeVariable.isSameVariable(key, parameter));
    if (parameter instanceof ExplicitTypeVariable) {
      result.rawMap.remove(((ExplicitTypeVariable) parameter).getReflectionTypeVariable());
    }
    return result;
  }

  /**
   * Returns a substitution that extends this one by mapping to itself each of the given type
   * variables that this one does not map. Returns this substitution if it maps all of them.
   *
   * @param variables type variables
   * @return a substitution that maps each of {@code variables}
   */
  Substitution extendWithIdentity(Collection<TypeVariable> variables) {
    // A copy of this substitution, made when the first variable is added.
    Substitution result = null;
    for (TypeVariable variable : variables) {
      if ((result == null ? this : result).get(variable) == null) {
        if (result == null) {
          result = new Substitution(this);
        }
        result.put(variable, variable);
      }
    }
    return result == null ? this : result;
  }

  /**
   * Returns true if the given type is a type variable that this substitution maps to itself and
   * that does not occur in the type that this substitution maps a different type variable to. When
   * types are compared, such a type variable stands for an unknown type, and every other type
   * variable stands for itself.
   *
   * @param type a type
   * @return true if {@code type} stands for an unknown type
   */
  boolean isUnknown(ReferenceType type) {
    if (!(type instanceof TypeVariable)) {
      return false;
    }
    TypeVariable variable = (TypeVariable) type;
    ReferenceType replacement = get(variable);
    return replacement != null
        && TypeVariable.isSameVariable(variable, replacement)
        && !getReplacementVariables().contains(variable.getIdentity());
  }

  /**
   * Returns the identities of the type variables that occur in a type that this substitution maps a
   * different type variable to.
   *
   * @return the identities of the type variables that occur in a replacement
   * @see TypeVariable#getIdentity()
   */
  private Set<Object> getReplacementVariables() {
    Set<Object> result = replacementVariables;
    if (result == null) {
      result = new HashSet<>();
      for (Map.Entry<TypeVariable, ReferenceType> entry : map.entrySet()) {
        if (!TypeVariable.isSameVariable(entry.getKey(), entry.getValue())) {
          for (TypeVariable variable : entry.getValue().getTypeParameters()) {
            result.add(variable.getIdentity());
          }
        }
      }
      replacementVariables = result;
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
  public @Nullable ReferenceType get(TypeVariable parameter) {
    // Look up a type parameter by its reflection type variable, which copies of it share.
    if (parameter instanceof ExplicitTypeVariable) {
      return rawMap.get(((ExplicitTypeVariable) parameter).getReflectionTypeVariable());
    }
    return map.get(parameter);
  }

  /**
   * Returns the concrete type mapped from the type variable by this substitution. Returns the given
   * default value if the variable is not in the substitution.
   *
   * @param parameter the variable
   * @param defaultValue the default value to return if the variable is not in the substitution
   * @return the concrete type mapped from the variable in this substitution, or {@code default} if
   *     there is no type for the variable
   */
  public ReferenceType getOrDefault(TypeVariable parameter, ReferenceType defaultValue) {
    ReferenceType result = get(parameter);
    return result == null ? defaultValue : result;
  }

  /**
   * Returns the value for the given type.
   *
   * @param type the type variable
   * @return the value for the type variable, or null if there is none
   */
  public @Nullable ReferenceType get(Type type) {
    return rawMap.get(type);
  }

  /**
   * Returns the type variables mapped from by this.
   *
   * @return the type variables mapped from by this
   */
  public Set<@KeyFor("this.map") TypeVariable> keySet() {
    return map.keySet();
  }

  /** Print the entries of this substitution to standard out on multiple lines. */
  public void print() {
    for (Map.Entry<TypeVariable, ReferenceType> entry : map.entrySet()) {
      System.out.println(entry.getKey() + "(" + entry.getKey() + ")" + " := " + entry.getValue());
    }
  }

  /**
   * Add a type variable to concrete type mapping to the substitution.
   *
   * @param typeParameter the type variable
   * @param type the concrete type
   */
  @RequiresNonNull({"map", "rawMap"})
  private void put(
      @UnknownInitialization Substitution this, TypeVariable typeParameter, ReferenceType type) {
    // Replace any entry for a copy of the type variable.
    map.keySet().removeIf(key -> TypeVariable.isSameVariable(key, typeParameter));
    map.put(typeParameter, type);
    replacementVariables = null;
    if (typeParameter instanceof ExplicitTypeVariable) {
      rawMap.put(((ExplicitTypeVariable) typeParameter).getReflectionTypeVariable(), type);
    }
  }

  /**
   * Returns true if this substitution is empty.
   *
   * @return true if this has no substitution pairs, false otherwise
   */
  public boolean isEmpty() {
    return map.isEmpty();
  }
}
