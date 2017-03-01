package randoop.types;

import java.lang.reflect.GenericDeclaration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Represents a table of parameters for a generic declaration, which may be
 * a class ({@link GenericClassType}, a method, or a constructor (both represented as
 * {@link randoop.operation.TypedClassOperation}).
 * Manages the mapping from reflection {@code java.lang.reflect.TypeVariable<>} to
 * {@link randoop.types.TypeVariable},
 * as well as the parameter order.
 */
public class ParameterTable {

  /** The map from reflection types to {@link randoop.types.TypeVariable}. null if table is empty. */
  private final LinkedHashMap<java.lang.reflect.TypeVariable<?>, TypeVariable> parameterMap;

  /** Singleton object for an empty table. */
  private final static ParameterTable emptyTable = new ParameterTable();

  /** The {@link ParameterTable} for the enclosing type. May be null */
  private final ParameterTable enclosingTable;

  /** Returns the empty table */
  public static ParameterTable emptyTable() {
    return emptyTable;
  }

  //rewrite
  static ParameterTable createTable(java.lang.reflect.GenericDeclaration declaration) {
    return createTable(emptyTable, declaration);
  }

  public static ParameterTable createTable(
      ParameterTable enclosingTable, GenericDeclaration declaration) {
    java.lang.reflect.TypeVariable<?>[] typeParameters = declaration.getTypeParameters();
    if (typeParameters.length == 0) {
      return enclosingTable;
    }

    LinkedHashMap<java.lang.reflect.TypeVariable<?>, TypeVariable> parameterMap;
    parameterMap = new LinkedHashMap<>();

    Set<java.lang.reflect.TypeVariable<?>> variables = new HashSet<>();
    Collections.addAll(variables, typeParameters);

    for (java.lang.reflect.TypeVariable<?> v : typeParameters) {
      ParameterBound bound = ParameterBound.forTypes(enclosingTable, variables, v.getBounds());
      ExplicitTypeVariable variable = new ExplicitTypeVariable(v, bound);

      parameterMap.put(v, variable);
    }

    return new ParameterTable(enclosingTable, parameterMap);
  }

  /**
   * Creates an empty {@link ParameterTable}.
   * @see #emptyTable
   */
  private ParameterTable() {
    this.enclosingTable = null;
    this.parameterMap = null;
  }

  /**
   * Private constructor used by {@link #createTable(ParameterTable, GenericDeclaration)} to create
   * the {@link ParameterTable} from a {@code LinkedHashMap<>} constructed from a parameter array.
   *
   * @param parameterMap  the {@code LinkedHashMap<>} mapping reflection parameters to {@link TypeVariable}
   */
  private ParameterTable(
      ParameterTable enclosingTable,
      LinkedHashMap<java.lang.reflect.TypeVariable<?>, TypeVariable> parameterMap) {
    this.enclosingTable = enclosingTable;
    this.parameterMap = parameterMap;
  }

  /**
   * Returns the parameter list for the generic declaration corresponding to this {@link ParameterTable}.
   * The order of type variables is preserved from the order of the parameters given in the
   * {@code GenericDeclaration} argument to {@link #createTable(ParameterTable, GenericDeclaration)}.
   *
   * @return the parameters in the same order as the generic declaration
   */
  public List<TypeVariable> getParameters() {
    if (parameterMap == null) {
      return new ArrayList<>();
    }
    return new ArrayList<>(parameterMap.values());
  }

  /**
   * Returns the {@link TypeVariable} corresponding to the given {@code java.lang.reflect.TypeVariable<>}.
   *
   * @param type  the reflection type variable object
   * @return the {@link TypeVariable} for {@code type}, null otherwise
   */
  public TypeVariable get(java.lang.reflect.TypeVariable<?> type) {
    TypeVariable variable = null;
    if (parameterMap != null) {
      variable = parameterMap.get(type);
    }
    if (variable == null) {
      if (enclosingTable != null) {
        variable = enclosingTable.get(type);
      }
    }
    return variable;
  }
}
