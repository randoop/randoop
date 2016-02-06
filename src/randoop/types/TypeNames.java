package randoop.types;

import plume.UtilMDE;

import randoop.util.PrimitiveTypes;

/**
 * TypeNames provides a pair of static methods to:
 * <ul>
 * <li> get string names for classes and primitive types, and
 * <li> get the {@link Class} object for a string representing a class or primitive type.
 * </ul>
 */
public class TypeNames {

  private static ClassLoader loader;

  /**
   * Sets the class loader for loading {@code Class<?>} objects.
   *
   * @param loader  the class loader
   */
  public static void setClassLoader(ClassLoader loader) {
    TypeNames.loader = loader;
  }

  /**
   * Returns a compilable Java name for the type.
   *
   * @param cls  the type for which name is to be generated
   * @return string that is a compilable version of type name
   */
  public static String getCompilableName(Class<?> cls) {
    String retval = cls.getName();

    // If it's an array, it starts with "[".
    if (retval.charAt(0) == '[') {
      // Class.getName() returns a a string that is almost in JVML
      // format, except that it slashes are periods. So before calling
      // classnameFromJvm, we replace the period with slashes to
      // make the string true JVML.
      retval = UtilMDE.fieldDescriptorToBinaryName(retval.replace('.', '/'));
    }

    // If inner classes are involved, Class.getName() will return
    // a string with "$" characters. To make it compilable, must replace with
    // dots.
    retval = retval.replace('$', '.');

    return retval;
  }

  /**
   * Returns {@link Class} object for a fully qualified class name or primitive
   * type name.
   *
   * @param typeName  a fully-qualified class name or primitive type name
   * @return {@link Class} object for type given in string
   * @throws ClassNotFoundException if string is not a recognized type
   */
  public static Class<?> getTypeForName(String typeName) throws ClassNotFoundException {
    Class<?> c = PrimitiveTypes.getClassForName(typeName);
    if (c == null) {
      c = Class.forName(typeName, true, loader);
    }
    return c;
  }
}
