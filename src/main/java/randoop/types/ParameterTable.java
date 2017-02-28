package randoop.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a table of parameters for a given generic declaration.
 * Manages mapping from reflection {@code java.lang.reflect.TypeVariable<>} to {@link randoop.types.TypeVariable},
 * as well as the parameter order.
 */
public class ParameterTable {

  /** The map from reflection types to {@link randoop.types.TypeVariable} */
  private final LinkedHashMap<java.lang.reflect.TypeVariable<?>, TypeVariable> parameterMap;

  /** Singleton object for an empty table */
  private final static ParameterTable emptyTable = new ParameterTable();

  /** Returns the empty table */
  static ParameterTable emptyTable() {
    return emptyTable;
  }

  /**
   * Constructes the {@link ParameterTable} from an array of {@code java.lang.reflect.TypeVariable<>}
   * objects.
   *
   * @param typeParameters  the parameters of a generic declaration given by reflection methods
   * @return the {@link ParameterTable} for the given parameter array
   */
  static ParameterTable createTable(java.lang.reflect.TypeVariable<?>[] typeParameters) {
    // LinkedHashMap necessary to preserve parameter order for getParameters()
    LinkedHashMap<java.lang.reflect.TypeVariable<?>, TypeVariable> parameterMap =
        new LinkedHashMap<>();
    for (java.lang.reflect.TypeVariable<?> v : typeParameters) {
      TypeVariable variable = TypeVariable.forType(v);
      parameterMap.put(v, variable);
    }
    return new ParameterTable(parameterMap);
  }

  private ParameterTable() {
    this.parameterMap = null;
  }

  /**
   * Private constructor used by {@link #createTable(java.lang.reflect.TypeVariable[])} to create
   * the {@link ParameterTable} from a {@code LinkedHashMap<>} constructed from a parameter array.
   *
   * @param parameterMap  the {@code LinkedHashMap<>} mapping reflection parameters to {@link TypeVariable}
   */
  private ParameterTable(
      LinkedHashMap<java.lang.reflect.TypeVariable<?>, TypeVariable> parameterMap) {
    this.parameterMap = parameterMap;
  }

  /**
   * Returns the parameter list for the generic declaration corresponding to this {@link ParameterTable}.
   * The order of type variables is preserved from the order of the parameters given to {@link #createTable(java.lang.reflect.TypeVariable[])}.
   *
   * @return the parameters in the same order as the generic declaration
   */
  public List<TypeVariable> getParameters() {
    if (parameterMap == null) {
      return new ArrayList<>();
    }
    return new ArrayList<>(parameterMap.values());
  }
}
