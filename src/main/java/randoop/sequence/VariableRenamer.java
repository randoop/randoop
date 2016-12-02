package randoop.sequence;

import randoop.types.ArrayType;
import randoop.types.ClassOrInterfaceType;
import randoop.types.InstantiatedType;
import randoop.types.JavaTypes;
import randoop.types.NonParameterizedType;
import randoop.types.ReferenceArgument;
import randoop.types.Type;
import randoop.types.TypeArgument;

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
  static String getVariableName(Type type) {

    if (type.isVoid()) {
      return "void";
    }
    // renaming for array type
    if (type.isArray()) {
      String arraySuffix = "";
      while (type.isArray()) {
        arraySuffix += "_array";
        type = ((ArrayType) type).getComponentType();
      }
      return getVariableName(type) + arraySuffix;
    }
    // for object, string, class types
    if (type.isObject()) {
      return "obj";
    } else if (type.isString()) {
      return "str";
    } else if (type.equals(JavaTypes.CLASS_TYPE)) {
      return "cls";
    } else if (type.isPrimitive() || type.isBoxedPrimitive()) {
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
      // otherwise, use the first character of the type name
      return type.getName().substring(0, 1);
    }
    if (type.isParameterized()) {
      ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
      String varName = classType.getSimpleName().toLowerCase();
      if (varName.equals("class")) {
        return "cls";
      }
      for (TypeArgument argument : classType.getTypeArguments()) {
        if (argument.isWildcard()) {
          varName += "_" + "wildcard";
        } else {
          String argumentName = getVariableName(((ReferenceArgument) argument).getReferenceType());
          varName += "_" + argumentName;
        }
      }
      return varName;
    } else {
      // for other object types
      String classname = type.getSimpleName();
      if (classname.length() > 0) {
        if (Character.isUpperCase(classname.charAt(0))) { // preserve camel case
          return classname.substring(0, 1).toLowerCase() + classname.substring(1);
        } else {
          return classname + "_instance";
        }
      } else {
        return "anonymous";
      }
    }
  }
}
