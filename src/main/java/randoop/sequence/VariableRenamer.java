package randoop.sequence;

import randoop.types.ArrayType;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.NonParameterizedType;
import randoop.types.ReferenceArgument;
import randoop.types.Type;
import randoop.types.TypeArgument;

class VariableRenamer {

  /** The sequence in which every variable will be renamed. */
  public final Sequence sequence;

  /** Maximum depth to concatenate parameterized type names. */
  private static final int VAR_NAME_MAX_DEPTH = 2;

  public VariableRenamer(Sequence sequence) {
    assert sequence != null : "The given sequence to rename can not be null";
    this.sequence = sequence;
  }

  /**
   * Heuristically transforms variables to better names based on its type name. Here are some
   * examples:
   *
   * <pre>
   *   int var0 = 1 becomes  int int0 = 1
   *   ClassName var0 = new ClassName() becomes ClassName className = new ClassName()
   *   Class var0 = null becomes Class cls = null
   *   Queue<Set<List<Comparable<String>>>> var0 = null becomes Queue<Set<List<Comparable<String>>>> listSetQueue = null
   *   ArrayList<String> var0 = null becomes ArrayList<String> strList = null
   * </pre>
   *
   * @param type the type to use as base of variable name
   * @return a variable name based on its type, without a trailing number
   */
  static String getVariableName(Type type) {
    return getVariableName(type, 0);
  }

  /**
   * Heuristically renames each variable to a name that is based on the variable's type.
   *
   * @param type the type to create a variable name for
   * @param depth the number of components (i.e. type arguments) of the type that have been used to
   *     create the name of the variable so far
   * @return a camel-cased variable name based on the type, without a trailing number
   */
  private static String getVariableName(Type type, int depth) {
    if (type.isVoid()) {
      return "void";
    } else if (type.isArray()) {
      // Array types.
      while (type.isArray()) {
        type = ((ArrayType) type).getComponentType();
      }
      return getVariableName(type) + "Array";
    }

    // Primitive types.
    if (type.isPrimitive() || type.isBoxedPrimitive()) {
      if (type.isBoxedPrimitive()) {
        type = ((NonParameterizedType) type).toPrimitive();
      }
      if (type.equals(JavaTypes.CHAR_TYPE)) {
        return "char";
      }
      if (type.equals(JavaTypes.LONG_TYPE)) {
        return "long";
      }
      if (type.equals(JavaTypes.BYTE_TYPE)) {
        return "byte";
      }
      if (type.equals(JavaTypes.INT_TYPE)) {
        return "int";
      }
      if (type.equals(JavaTypes.BOOLEAN_TYPE)) {
        return "bool";
      }
      if (type.equals(JavaTypes.FLOAT_TYPE)) {
        return "float";
      }
      if (type.equals(JavaTypes.DOUBLE_TYPE)) {
        return "double";
      }
      if (type.equals(JavaTypes.SHORT_TYPE)) {
        return "short";
      }

      // Otherwise, use the first character of the type name.
      return type.getName().substring(0, 1);
    }

    // Get the simple name of the type.
    String varName = type.getSimpleName();

    if (type.isParameterized()) {
      if (varName.toLowerCase().equals("class")) {
        return "cls";
      }

      // Special cases for parameterized types.
      if (varName.contains("Iterator")) {
        // Iterator takes precedence, in cases like ListIterator.
        varName = "itor";
      } else if (varName.contains("List")) {
        // List comes before array, in cases like ArrayList.
        varName = "list";
      } else if (varName.contains("Set")) {
        varName = "set";
      } else if (varName.contains("Map")) {
        varName = "map";
      } else if (varName.contains("Queue")) {
        varName = "queue";
      } else if (varName.contains("Collection")) {
        varName = "collection";
      } else if (varName.contains("Array")) {
        varName = "array";
      }

      // Only use the first type argument to construct the name to simplify things.
      ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
      TypeArgument argument = classType.getTypeArguments().get(0);
      if (argument.isWildcard()) {
        varName = "wildcard" + capitalizeString(varName);
      } else {
        if (depth < VAR_NAME_MAX_DEPTH) {
          String argumentName =
              getVariableName(((ReferenceArgument) argument).getReferenceType(), depth + 1);

          varName = argumentName + capitalizeString(varName);
        }
      }
    } else {
      // Special cases: Object, String, Class.
      if (type.isObject()) {
        varName = "obj";
      } else if (type.isString()) {
        varName = "str";
      } else if (type.equals(JavaTypes.CLASS_TYPE)) {
        varName = "cls";
      } else {
        // All other object types.
        if (varName.length() == 0) {
          varName = "anonymous";
        }
      }
    }

    // Preserve camel case.
    if (Character.isUpperCase(varName.charAt(0))) {
      varName = lowercaseFirstCharacter(varName);
    }

    // Make sure that the last character is not a digit.
    if (Character.isDigit(varName.charAt(varName.length() - 1))) {
      varName += "_";
    }

    return varName;
  }

  /**
   * Capitalize the variable name while preserving any capitalized letters after the first letter.
   *
   * @param variableName the name of the variable
   * @return capitalized form of variable name
   */
  private static String capitalizeString(String variableName) {
    return variableName.substring(0, 1).toUpperCase() + variableName.substring(1);
  }

  /**
   * Lowercase the first character in the variable name while preserving any capitalized letters
   * after the first letter.
   *
   * @param variableName the name of the variable
   * @return variableName with the first letter lowercased
   */
  private static String lowercaseFirstCharacter(String variableName) {
    return variableName.substring(0, 1).toLowerCase() + variableName.substring(1);
  }
}
