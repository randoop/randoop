package randoop.operation;

import org.checkerframework.checker.signature.qual.FqBinaryName;
import randoop.types.Type;

/**
 * TypeArguments provides static methods for creating and recognizing strings representing the type
 * arguments of a method or constructor.
 *
 * <p>Type arguments are given as comma separated lists of fully-qualified class and primitive type
 * names. For example:
 *
 * <ul>
 *   <li>{@code int}
 *   <li>{@code int,double,java.lang.String}
 *   <li>{@code randoop.operation.Operation}
 * </ul>
 */
class TypeArguments {

  /**
   * Parses comma-no-space-delimited type argument string and returns a list of types.
   *
   * @param argStr the string containing type arguments for a signature, each a @FqBinaryName,
   *     separated by commas
   * @return the array of {@link Class} objects for the type arguments in argStr
   * @throws OperationParseException if a type name in the string is not a valid type
   */
  static Class<?>[] getTypeArgumentsForString(String argStr) throws OperationParseException {
    Class<?>[] argTypes = new Class<?>[0];
    if (argStr.trim().length() > 0) {
      String[] argsStrs = argStr.split(",");
      argTypes = new Class<?>[argsStrs.length];
      for (int i = 0; i < argsStrs.length; i++) {
        @SuppressWarnings("signature") // exception caught below if type is wrong
        @FqBinaryName String typeName = argsStrs[i].trim();

        try {
          argTypes[i] = Type.forFullyQualifiedName(typeName);
        } catch (ClassNotFoundException e) {
          throw new OperationParseException("Class " + typeName + " is not on classpath");
        }
      }
    }
    return argTypes;
  }

  /**
   * Adds the type names for the arguments of a signature to the {@code StringBuilder}.
   *
   * @param sb the {@link StringBuilder} to which type names are added
   * @param params the array of {@link Class} objects representing the types of signature arguments
   */
  static void getTypeArgumentString(StringBuilder sb, Class<?>[] params) {
    for (int j = 0; j < params.length; j++) {
      sb.append(params[j].getName());
      if (j < (params.length - 1)) sb.append(",");
    }
  }
}
