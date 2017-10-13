package randoop.sequence;

import randoop.types.ArrayType;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.NonParameterizedType;
import randoop.types.ReferenceArgument;
import randoop.types.Type;
import randoop.types.TypeArgument;

class VariableRenamer {

  /** The sequence in which every variable will be renamed */
  public final Sequence sequence;

  // Maximum depth to concatenate parameterized type names.
  private static final int MAX_DEPTH = 2;

  public VariableRenamer(Sequence sequence) {
    assert sequence != null : "The given sequence to rename can not be null";
    this.sequence = sequence;
  }

  /**
   * Heuristically transforms variables to better names based on its type name. Here are some
   * examples:
   *
   * <p>
   *
   * <pre>
   *   int var0 = 1     becomes  int int0 = 1
   *   ClassName var0 = new ClassName()      becomes ClassName className = new ClassName()
   *   Class var0 = null      becomes Class cls = null
   * </pre>
   *
   * @param type the type to use as base of variable name
   * @return a variable name based on its type, without a trailing number
   */
  static String getVariableName(Type type) {
    return getVariableName(type, 0);
  }

  static String getVariableName(Type type, int depth) {
    if (type.isVoid()) {
      return "void";
    }

    // Arrays.
    if (type.isArray()) {
      String arraySuffix = "";
      while (type.isArray()) {
        arraySuffix += "Array";
        type = ((ArrayType) type).getComponentType();
      }
      return getVariableName(type) + arraySuffix;
    }

    // Primitives types.
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

    // Special cases:  Object, String, Class.
    if (type.isObject()) {
      return "obj";
    } else if (type.isString()) {
      return "str";
    } else if (type.equals(JavaTypes.CLASS_TYPE)) {
      return "cls";
    }

    if (type.isParameterized()) {
      ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
      String varName = classType.getSimpleName().toLowerCase();
      if (varName.equals("class")) {
        return "cls";
      }

      // Special cases for parameterized types.
      if (varName.contains("iterator")) {
        // Iterator takes precedence, in cases like ListIterator.
        varName = "itor";
      } else if (varName.contains("list")) {
        // List comes before array, in cases like ArrayList.
        varName = "list";
      } else if (varName.contains("set")) {
        varName = "set";
      } else if (varName.contains("map")) {
        varName = "map";
      } else if (varName.contains("queue")) {
        varName = "queue";
      } else if (varName.contains("collection")) {
        varName = "collection";
      } else if (varName.contains("array")) {
        varName = "array";
      }

      if (classType.getTypeArguments().size() >= 1) {
        TypeArgument argument = classType.getTypeArguments().get(0);

        if (argument.isWildcard()) {
          // Capitalize the variable name while preserving any capitalized letters after the first letter.
          varName = varName.substring(0, 1).toUpperCase() + varName.substring(1);

          varName = "wildcard" + varName;
        } else {
          if (depth < MAX_DEPTH) {
            String argumentName =
                getVariableName(((ReferenceArgument) argument).getReferenceType(), depth + 1);

            // Capitalize the variable name while preserving any capitalized letters after the first letter.
            varName = varName.substring(0, 1).toUpperCase() + varName.substring(1);

            varName = argumentName + varName;
          }
        }
      }

      // Make sure last character is not a digit.
      if (Character.isDigit(varName.charAt(varName.length() - 1))) {
        varName += "_";
      }

      return varName;
    }

    // All other object types.
    String classname = type.getSimpleName();
    if (classname.length() == 0) {
      return "anonymous";
    }

    // Make sure last character is not a digit.
    if (Character.isDigit(classname.charAt(classname.length() - 1))) {
      classname += "_";
    }

    // Preserve camel case.
    if (Character.isUpperCase(classname.charAt(0))) {
      return classname.substring(0, 1).toLowerCase() + classname.substring(1);
    } else {
      return classname + "_instance";
    }
  }
}
