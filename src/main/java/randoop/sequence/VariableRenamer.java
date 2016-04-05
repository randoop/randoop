package randoop.sequence;

import randoop.types.ConcreteArrayType;
import randoop.types.ConcreteType;

class VariableRenamer {

  /**
   * The sequence in which every variable will be renamed
   */
  public final Sequence sequence;

  public VariableRenamer(Sequence sequence) {
    assert sequence != null : "The given sequence to rename can not be null";
    this.sequence = sequence;
  }

  /**
   * Heuristically transforms variables to better names based on its type name.
   * Here are some examples: int var0 = 1 will be transformed to int i0 = 1
   * ClassName var0 = new ClassName() will be transformed to ClassName className
   * = new ClassName() Class var0 = null will be transformed to Class clazz =
   * null
   * @param type  the type to use as base of variable name
   * @return a variable name based on its type
   */
  static String getVariableName(ConcreteType type) {

    if (type.isVoid()) {
      return "void";
    }
    // renaming for array type
    if (type.isArray()) {
      String arraySuffix = "";
      while (type.isArray()) {
        arraySuffix += "_array";
        type = ((ConcreteArrayType) type).getElementType();
      }
      return getVariableName(type) + arraySuffix;
    }
    // for object, string, class types
    if (type.isObject()) {
      return "obj";
    } else if (type.hasRuntimeClass(String.class)) {
      return "str";
    } else if (type.hasRuntimeClass(Class.class)) {
      return "clazz";
    }
    // for primitive types (including boxing or unboxing types)
    else if (type.hasRuntimeClass(int.class) || type.hasRuntimeClass(Integer.class)) {
      return "i";
    } else if (type.hasRuntimeClass(double.class) || type.hasRuntimeClass(Double.class)) {
      return "d";
    } else if (type.hasRuntimeClass(float.class) || type.hasRuntimeClass(Float.class)) {
      return "f";
    } else if (type.hasRuntimeClass(short.class) || type.hasRuntimeClass(Short.class)) {
      return "s";
    } else if (type.hasRuntimeClass(boolean.class) || type.hasRuntimeClass(Boolean.class)) {
      return "b";
    } else if (type.hasRuntimeClass(char.class) || type.hasRuntimeClass(Character.class)) {
      return "char";
    } else if (type.hasRuntimeClass(long.class) || type.hasRuntimeClass(Long.class)) {
      return "long";
    } else if (type.hasRuntimeClass(byte.class) || type.hasRuntimeClass(Byte.class)) {
      return "byte";
    } else {
      // for other object types
      String qualifiedTypeName = type.getName();
      assert qualifiedTypeName.indexOf('.') > 0 : "expecting qualified type name, got " + qualifiedTypeName;
      String typeName = qualifiedTypeName.substring(qualifiedTypeName.lastIndexOf('.') + 1);
      if (typeName.length() > 0) {
        if (Character.isUpperCase(typeName.charAt(0))) {
          return typeName.substring(0, 1).toLowerCase() + typeName.substring(1);
        } else {
          return typeName + "_instance";
        }
      } else {
        return "anonymous";
      }
    }
  }
}
