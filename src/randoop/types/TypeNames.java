package randoop.types;

import plume.UtilMDE;

public class TypeNames {
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
}
