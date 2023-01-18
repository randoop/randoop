package randoop.reflection;

import org.checkerframework.checker.signature.qual.ClassGetName;
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
   * Returns {@link Class} object for a fully-qualified class name or primitive type name.
   *
   * @param typeName a fully-qualified class name or primitive type name
   * @return {@link Class} object for type given in string
   * @throws ClassNotFoundException if string is not a recognized type
   */
  public static Class<?> getTypeForName(@ClassGetName String typeName)
      throws ClassNotFoundException {
    Class<?> c = PrimitiveTypes.classForName(typeName);
    if (c == null) {
      c = Class.forName(typeName);
    }
    return c;
  }
}
