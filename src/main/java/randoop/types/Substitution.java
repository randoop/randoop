package randoop.types;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import plume.UtilMDE;

import java.util.Objects;

/**
 * Manages the substitution of concrete types for type variables and wildcards in an
 * instantiation of a generic class as a parameterized type.
 * <p>
 * Because a substitution represents the instantiation from a generic class to
 * a parameterized type, an instance is built using
 * {@link Substitution#forArgs(List, GeneralType...)} and then not modified.
 */
public class Substitution {

  /** The substitution map */
  private Map<Type, GeneralType> map;

  /**
   * Create an empty substitution.
   * Objects created publicly using {@link Substitution#forArgs(List, GeneralType...)}
   */
  public Substitution() {
    map = new LinkedHashMap<>();
  }

  /**
   * {@inheritDoc}
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

  /**
   * {@inheritDoc}
   * @return a string representation of the substitution
   */
  @Override
  public String toString() {
    List<String> pairs = new ArrayList<>();
    for (Entry<Type, GeneralType> p : map.entrySet()) {
      pairs.add(p.getKey().toString() + "/" + p.getValue().getName());
    }
    return "[" + UtilMDE.join(pairs, ",") + "]";
  }

  /**
   * Add a type variable to concrete type mapping to the substitution.
   * Only called by {@link Substitution#forArgs(List, GeneralType...)}
   *
   * @param typeParameter  the type variable
   * @param type  the concrete type
   */
  private void put(Type typeParameter, GeneralType type) {
    map.put(typeParameter, type);
  }

  /**
   * Returns the concrete type mapped from the type variable by this substitution.
   * Returns null if the variable is not in the substitution.
   *
   * @param parameter  the variable
   * @return the concrete type mapped from the variable in this substitution, or
   * null if there is no type for the variable
   */
  public GeneralType get(Type parameter) {
    return map.get(parameter);
  }

  /**
   * Create a substitution from the type parameters to the corresponding type
   * arguments.
   * Requires that the number of parameters and arguments agree.
   *
   * @param parameters  the type parameters
   * @param arguments  the type arguments
   * @return a {@code Substitution} mapping each type variable to a type argument
   * @throws IllegalArgumentException if the number of type parameters and arguments
   *         do not agree, or arguments has a primitive value
   */
  public static Substitution forArgs(List<TypeArgument> parameters, GeneralType... arguments) {
    if (parameters.size() != arguments.length) {
      throw new IllegalArgumentException("number of parameters and arguments must agree");
    }
    Substitution s = new Substitution();
    for (int i = 0; i < parameters.size(); i++) {
      if (arguments[i].isPrimitive()) {
        String msg = "type arguments may not be primitive (found: " + arguments[i].getName() + ")";
        throw new IllegalArgumentException(msg);
      }
      s.put(parameters.get(i), arguments[i]);
    }
    return s;
  }

  public static Substitution forArgs(List<TypeArgument> parameters, List<GeneralType> arguments) {
    if (parameters.size() != arguments.size()) {
      throw new IllegalArgumentException("number of parameters and arguments must agree");
    }
    Substitution s = new Substitution();
    for (int i = 0; i < parameters.size(); i++) {
      if (arguments.get(i).isPrimitive()) {
        String msg = "type arguments may not be primitive (found: " + arguments.get(i).getName() + ")";
        throw new IllegalArgumentException(msg);
      }

      s.put(parameters.get(i), arguments.get(i));
    }
    return s;
  }

  public Substitution union(Substitution substitution) {
    Substitution s = new Substitution();
    s.map.putAll(this.map);
    s.map.putAll(substitution.map);
    return s;
  }

  public static Substitution forArgs(List<TypeArgument> parameters, List<TypeArgument> arguments) {
    return null;
  }
}
