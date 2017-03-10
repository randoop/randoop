package randoop.reflection;

import java.lang.reflect.Array;
import randoop.types.PrimitiveTypes;

/**
 * TypeNames provides a pair of static methods to:
 *
 * <ul>
 *   <li>get string names for classes and primitive types, and
 *   <li>get the {@link Class} object for a string representing a class or primitive type.
 * </ul>
 */
public class TypeNames {

  /**
   * Returns {@link Class} object for a fully qualified class name or primitive type name.
   *
   * @param typeName a fully-qualified class name or primitive type name
   * @return {@link Class} object for type given in string
   * @throws ClassNotFoundException if string is not a recognized type
   */
  public static Class<?> getTypeForName(String typeName) throws ClassNotFoundException {
    if (isArrayType(typeName)) {
      return getArrayType(typeName);
    }
    Class<?> c = PrimitiveTypes.classForName(typeName);
    if (c == null) {
      c = Class.forName(typeName);
    }
    return c;
  }

  /**
   * For an array type name, returns the corresponding {@code Class<>} object.
   *
   * @param typeName the array type name
   * @return the {@code Class<>} object for the type
   * @throws ClassNotFoundException if {@code typeName} is not the name of a valid type in the
   *     classpath
   */
  private static Class<?> getArrayType(String typeName) throws ClassNotFoundException {
    String elementTypeName = typeName.substring(0, typeName.lastIndexOf("[]"));
    Class<?> elementType = getTypeForName(elementTypeName);
    return Array.newInstance(elementType, 0).getClass();
  }

  /**
   * Indicates whether the type name is for an array type (e.g., ends with brackets)
   *
   * @param typeName the type name
   * @return true if {@code typeName} ends with brackets, false otherwise
   */
  private static boolean isArrayType(String typeName) {
    return typeName.endsWith("[]");
  }
}
