package randoop.types;

import randoop.util.PrimitiveTypes;

import plume.UtilMDE;

public class TypeNames {
  
  /**
   * Generates a name for the given type in a format that can be compiled.
   * 
   * @param cls is the type for which name is to be generated.
   * @return string that is a compilable version of type name.
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
   * Recognizes a type from a string and returns the {@link Class} object for the type.
   * Handles primitive types as well.
   * 
   * @param typeName string representation of a type.
   * @return {@link Class} object for type given in string.
   * @throws ClassNotFoundException if string is not a recognized type.
   */
  public static Class<?> recognizeType(String typeName) throws ClassNotFoundException {
    Class<?> c = PrimitiveTypes.getClassForName(typeName);
    if (c == null) {
        c = Class.forName(typeName);
    }
    return c;
  }
}
