package randoop.types;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.plumelib.util.UtilPlume;

/**
 * Manages the substitution of concrete types for type variables and wildcards in an instantiation
 * of a generic class as a parameterized type.
 *
 * <p>Because a substitution represents the instantiation from a generic class to a parameterized
 * type, an instance is built using {@link Substitution#forArgs(List, List)} and then not modified.
 */
public class Substitution<T> {

  /** The substitution map. */
  private Map<TypeVariable, T> map;

  /** map on reflection types - used for testing bounds */
  private Map<java.lang.reflect.Type, T> rawMap;

  /** Create an empty substitution. */
  public Substitution() {
    map = new LinkedHashMap<>();
    rawMap = new LinkedHashMap<>();
  }

  public Substitution(Substitution<T> substitution) {
    map = new LinkedHashMap<>(substitution.map);
    rawMap = new LinkedHashMap<>(substitution.rawMap);
  }

  public static <T> Substitution<T> forArg(TypeVariable parameter, T argument) {
    Substitution<T> s = new Substitution<>();
    s.put(parameter, argument);
    return s;
  }

  /**
   * Create a substitution from the type parameters to the corresponding type arguments. Requires
   * that the number of parameters and arguments agree.
   *
   * @param <T> the substituted type
   * @param parameters the type parameters
   * @param arguments the type arguments
   * @return a {@code Substitution} mapping each type variable to a type argument
   */
  @SafeVarargs
  public static <T> Substitution<T> forArgs(List<TypeVariable> parameters, T... arguments) {
    assert parameters.size() == arguments.length;
    Substitution<T> s = new Substitution<>();
    for (int i = 0; i < parameters.size(); i++) {
      s.put(parameters.get(i), arguments[i]);
    }
    return s;
  }

  /**
   * Create a substitution from the type parameters and the list of arguments.
   *
   * @param parameters the type parameters
   * @param arguments the type arguments
   * @param <T> the argument type
   * @return the substitution that maps the type parameters to the corresponding type argument
   */
  public static <T> Substitution<T> forArgs(List<TypeVariable> parameters, List<T> arguments) {
    assert parameters.size() == arguments.size();
    Substitution<T> s = new Substitution<>();
    for (int i = 0; i < parameters.size(); i++) {
      s.put(parameters.get(i), arguments.get(i));
    }
    return s;
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
    @SuppressWarnings("rawtypes")
    Substitution s = (Substitution) obj;
    return map.equals(s.map);
  }

  @Override
  public int hashCode() {
    return Objects.hash(map);
  }

  /**
   * {@inheritDoc}
   *
   * @return a string representation of the substitution
   */
  @Override
  public String toString() {
    List<String> pairs = new ArrayList<>();
    for (Entry<TypeVariable, T> p : map.entrySet()) {
      pairs.add(p.getKey().toString() + " := " + p.getValue().toString());
    }
    return "[" + UtilPlume.join(pairs, ", ") + "]";
  }

  /**
   * Indicates whether this substitution is disjoint from another substitution, or that if they both
   * map the same type variable, they map it to the same type. This is the test for whether this
   * substitution can be extended by the other substitution using {@link #extend(Substitution)}.
   *
   * @param substitution the other substitution to check for consistency with this substitution
   * @return true if the the substitutions are consistent, false otherwise
   */
  public boolean isConsistentWith(Substitution<T> substitution) {
    for (Entry<TypeVariable, T> entry : substitution.map.entrySet()) {
      if (this.map.containsKey(entry.getKey())
          && !this.get(entry.getKey()).equals(entry.getValue())) {
        return false;
      }
    }
    for (Entry<java.lang.reflect.Type, T> entry : substitution.rawMap.entrySet()) {
      if (this.rawMap.containsKey(entry.getKey())
          && !this.get(entry.getKey()).equals(entry.getValue())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Extends this substitution by adding the entries of another substitution. If both substitutions
   * contain the same type variable, they must map to the same type.
   *
   * @param substitution the substitution to add to this substitution
   * @return a new substitution that is this substitution extended by the given substitution
   */
  public Substitution<T> extend(Substitution<T> substitution) {
    Substitution<T> result = new Substitution<>(this);
    for (Entry<TypeVariable, T> entry : substitution.map.entrySet()) {
      if (result.map.containsKey(entry.getKey())
          && !result.get(entry.getKey()).equals(entry.getValue())) {
        throw new IllegalArgumentException(
            "Substitutions not disjoint, and map " + entry.getKey() + " to distinct types");
      }
      result.map.put(entry.getKey(), entry.getValue());
    }
    for (Entry<java.lang.reflect.Type, T> entry : substitution.rawMap.entrySet()) {
      if (result.rawMap.containsKey(entry.getKey())
          && !result.get(entry.getKey()).equals(entry.getValue())) {
        throw new IllegalArgumentException(
            "Substitutions not disjoint, and map " + entry.getKey() + " to distinct types");
      }
      result.rawMap.put(entry.getKey(), entry.getValue());
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
  public T get(TypeVariable parameter) {
    return map.get(parameter);
  }

  /**
   * Returns the value for the given {@link java.lang.reflect.Type}
   *
   * @param parameter the type variable
   * @return the value for the type variable, or null if there is none
   */
  public T get(Type parameter) {
    return rawMap.get(parameter);
  }

  public Collection<TypeVariable> getVariables() {
    return map.keySet();
  }

  /** Print the entries of this substitution to standard out. */
  public void print() {
    for (Entry<TypeVariable, T> entry : map.entrySet()) {
      System.out.println(
          entry.getKey() + "(" + entry.getKey().hashCode() + ")" + " := " + entry.getValue());
    }
  }

  /**
   * Add a type variable to concrete type mapping to the substitution. Only called by {@link
   * #forArgs(List, List)} and {@link #forArgs(List, Object[])}.
   *
   * @param typeParameter the type variable
   * @param type the concrete type
   */
  private void put(TypeVariable typeParameter, T type) {
    map.put(typeParameter, type);
    if (typeParameter instanceof ExplicitTypeVariable) {
      rawMap.put(((ExplicitTypeVariable) typeParameter).getReflectionTypeVariable(), type);
    }
  }

  /**
   * Indicates whether this substitution has any variable-type pairs.
   *
   * @return true if there are no substitution pairs, false otherwise
   */
  public boolean isEmpty() {
    return map.isEmpty();
  }
}
