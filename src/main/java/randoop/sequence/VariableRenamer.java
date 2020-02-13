package randoop.sequence;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
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
    assert sequence != null : "The given sequence to rename cannot be null";
    this.sequence = sequence;
  }

  /**
   * Heuristically transforms variables to better names based on its type name. Here are some
   * examples:
   *
   * <pre>{@code
   * int var0 = 1 becomes  int int0 = 1
   * ClassName var0 = new ClassName() becomes ClassName className = new ClassName()
   * Class var0 = null becomes Class cls = null
   * Queue<Set<List<Comparable<String>>>> var0 = null becomes Queue<Set<List<Comparable<String>>>> listSetQueue = null
   * ArrayList<String> var0 = null becomes ArrayList<String> strList = null
   * }</pre>
   *
   * @param type the type to use as base of variable name
   * @return a variable name based on its type, with the first character lowercase and the final
   *     character not a digit
   */
  static String getVariableName(Type type) {
    String varName = getVariableName(type, VAR_NAME_MAX_DEPTH);

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
   * Heuristically renames each variable to a name that is based on the variable's type.
   *
   * @param type the type to use as the base of the variable name
   * @param depth the number of components (i.e. type arguments) of the type that will be used to
   *     create a name for the variable
   * @return a variable name based on its type and is camel cased. The first character may be
   *     uppercase.
   */
  private static String getVariableName(Type type, int depth) {
    // Special cases.
    if (type.isVoid()) {
      return "void";
    } else if (type.equals(JavaTypes.CLASS_TYPE)) {
      return "cls";
    } else if (type.isObject()) {
      return "obj";
    } else if (type.isString()) {
      return "str";
    }

    // Primitive types.
    if (type.isBoxedPrimitive()) {
      type = ((NonParameterizedType) type).toPrimitive();
    }
    if (type.isPrimitive()) {
      return type.getFqName();
    }

    // Array types.
    if (type.isArray()) {
      while (type.isArray()) {
        type = ((ArrayType) type).getComponentType();
      }
      return getVariableName(type, depth - 1) + "Array";
    }

    // Get the simple name of the type.
    String varName = type.getSimpleName();
    if (varName.length() == 0) {
      return "anonymous";
    }

    if (type.isParameterized()) {
      Class<?> typeClass = type.getRuntimeClass();

      // Special cases for parameterized types.
      if (Iterator.class.isAssignableFrom(typeClass)) {
        varName = "itor";
      } else if (List.class.isAssignableFrom(typeClass)) {
        varName = "list";
      } else if (Set.class.isAssignableFrom(typeClass)) {
        varName = "set";
      } else if (Map.class.isAssignableFrom(typeClass)) {
        varName = "map";
      } else if (Queue.class.isAssignableFrom(typeClass)) {
        varName = "queue";
      } else if (Collection.class.isAssignableFrom(typeClass)) {
        varName = "collection";
      }

      List<TypeArgument> arglist = ((ClassOrInterfaceType) type).getTypeArguments();
      // TODO: This test seems like a hack.  Shouldn't the arglist always be empty
      /// for a parameterized type?
      if (!arglist.isEmpty()) {
        // Only use the first type argument to construct the name to simplify things.
        TypeArgument argument = arglist.get(0);
        if (argument.isWildcard()) {
          varName = "wildcard" + capitalizeString(varName);
        } else {
          if (depth >= 0) {
            String argumentName =
                getVariableName(((ReferenceArgument) argument).getReferenceType(), depth - 1);

            varName = argumentName + capitalizeString(varName);
          }
        }
      }
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
