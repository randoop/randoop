package randoop.sequence;

import randoop.types.ArrayType;
import randoop.types.ConcreteTypes;
import randoop.types.GeneralType;

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
  static String getVariableName(GeneralType type) {

    if (type.isVoid()) {
      return "void";
    }
    // renaming for array type
    if (type.isArray()) {
      String arraySuffix = "";
      while (type.isArray()) {
        arraySuffix += "_array";
        type = ((ArrayType) type).getElementType();
      }
      return getVariableName(type) + arraySuffix;
    }
    // for object, string, class types
    if (type.isObject()) {
      return "obj";
    } else if (type.isString()) {
      return "str";
    } else if (type.equals(ConcreteTypes.CLASS_TYPE)) {
      return "cls";
    } else if (type.isPrimitive() || type.isBoxedPrimitive()) {
      if (type.isBoxedPrimitive()) {
        type = type.toPrimitive();
      }
      if (type.equals(ConcreteTypes.CHAR_TYPE)) {
        return "char";
      }
      if (type.equals(ConcreteTypes.LONG_TYPE)) {
        return "long";
      }
      if (type.equals(ConcreteTypes.BYTE_TYPE)) {
        return "byte";
      }
      // otherwise, use the first character of the type name
      return type.getName().substring(0,1);
    } if (type.isParameterized()) {
      String qualifiedTypeName = type.getName();
      int argumentsPosition = qualifiedTypeName.indexOf('<');
      String qualifiedClassname = qualifiedTypeName.substring(0, argumentsPosition);
      String varName = qualifiedClassname.substring(qualifiedClassname.lastIndexOf('.') + 1).toLowerCase();
      String argumentString = qualifiedTypeName.substring(argumentsPosition + 1, qualifiedTypeName.indexOf('>'));
      String[] toks = argumentString.split(",");
      for (String qualifiedArgumentName : toks) {
        String argumentName = qualifiedArgumentName.substring(qualifiedArgumentName.lastIndexOf('.') + 1);
        varName += "_" + argumentName;
      }
      return varName;
    } else {
      // for other object types
      String qualifiedTypeName = type.getName();
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
