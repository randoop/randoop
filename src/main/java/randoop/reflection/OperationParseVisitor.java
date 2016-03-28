package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import randoop.field.AccessibleField;
import randoop.operation.ArrayCreation;
import randoop.operation.ConstructorCall;
import randoop.operation.EnumConstant;
import randoop.operation.FieldGet;
import randoop.operation.FieldSet;
import randoop.operation.MethodCall;
import randoop.operation.NonreceiverTerm;
import randoop.operation.OperationParseException;
import randoop.operation.TypeArguments;
import randoop.types.ConcreteArrayType;
import randoop.types.ConcreteSimpleType;
import randoop.types.ConcreteType;
import randoop.types.GeneralType;
import randoop.types.GenericArrayType;
import randoop.types.GenericClassType;
import randoop.types.GenericType;
import randoop.types.GenericTypeTuple;
import randoop.types.TypeNames;

/**
 * Created by bjkeller on 3/27/16.
 */
public class OperationParseVisitor {

  private GeneralType classType;
  private TypedOperationManager manager;

  public OperationParseVisitor(TypedOperationManager manager) {
    this.manager = manager;
  }

  public void visitConstructor(String classname, String opname, String arguments) throws OperationParseException {
    String constructorString = classname + "." + opname + arguments;
    Class<?> c;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException e) {
      String msg = "Class for constructor " + constructorString + " not found: " + e;
      throw new OperationParseException(msg);
    }

    visit(c);

    Class<?>[] typeArguments = TypeArguments.getTypeArgumentsForString(arguments);
    Constructor<?> con;
    try {
      con = c.getDeclaredConstructor(typeArguments);
    } catch (NoSuchMethodException e) {
      String msg = "Constructor " + constructorString + " not found: " + e;
      throw new OperationParseException(msg);
    }

    ConstructorCall op = new ConstructorCall(con);
    GenericTypeTuple inputTypes = getInputTypes(typeArguments);
    manager.createTypedOperation(op, classType, inputTypes, classType);
  }

  public void visit(Class<?> c) {
    if (c.getTypeParameters().length > 0) {
      classType = new GenericClassType(c);
    } else {
      classType = new ConcreteSimpleType(c);
    }
  }

  public void visitMethod(String classname, String opname, String arguments) throws OperationParseException {
    String methodString = classname + "." + opname + arguments;
    Class<?> c;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException e) {
      String msg = "Class for method " + methodString + " not found: " + e;
      throw new OperationParseException(msg);
    }
    visit(c);

    Class<?>[] typeArguments = TypeArguments.getTypeArgumentsForString(arguments);
    Method m;
    try {
      m = c.getDeclaredMethod(opname, typeArguments);
    } catch (NoSuchMethodException e) {
      String msg = "Method " + methodString + " not found: " + e;
      throw new OperationParseException(msg);
    }

    MethodCall op = new MethodCall(m);
    GenericTypeTuple inputTypes;
    if (!Modifier.isStatic(m.getModifiers() & Modifier.methodModifiers())) {
      inputTypes = getInputTypes(classType, typeArguments);
    } else {
      inputTypes = getInputTypes(typeArguments);
    }
    GeneralType outputType = GeneralType.forType(m.getGenericReturnType());
    manager.createTypedOperation(op, classType, inputTypes, outputType);

  }

  public void visitField(String classname, String opname, String fieldname) throws OperationParseException {
    Class<?> c;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException e) {
      String msg = " The class name \""
              + classname
              + "\" of the field name \""
              + classname + "." + opname
              + "\" was not recognized as a class.";
      throw new OperationParseException(msg);
    }
    visit(c);

    Field field = fieldForName(c, fieldname);
    if (field == null) {
      String msg =  " The field name \""
              + fieldname
              + "\" is not a field of the class "
              + "\""
              + classname
              + "\".";
      throw new OperationParseException(msg);
    }
    GeneralType fieldType = GeneralType.forType(field.getGenericType());
    AccessibleField accessibleField = new AccessibleField(field);

    if (opname.equals("<get>")) {
      List<GeneralType> getInputTypeList = new ArrayList<>();
      if (! accessibleField.isStatic()) {
        getInputTypeList.add(classType);
      }
      manager.createTypedOperation(new FieldGet(accessibleField), classType, new GenericTypeTuple(getInputTypeList), fieldType);
    } else if (opname.equals("<set>")) {
      if (accessibleField.isFinal()) {
        throw new OperationParseException("Cannot create setter for final field " + classname + "." + opname);
      }
      List<GeneralType> setInputTypeList = new ArrayList<>();
      if (! accessibleField.isStatic()) {
        setInputTypeList.add(classType);
      }
      setInputTypeList.add(fieldType);
      manager.createTypedOperation(new FieldSet(accessibleField), classType, new GenericTypeTuple(setInputTypeList), ConcreteType.VOID_TYPE);
    } else {
      String msg = "The field operation name " + opname + " is not recognized";
      throw new OperationParseException(msg);
    }

  }




  public void visitArrayCreation(String elementTypeName, int length) throws OperationParseException {

    Class<?> elementClass;
    try {
      elementClass = TypeNames.getTypeForName(elementTypeName);
    } catch (ClassNotFoundException e) {
      throw new OperationParseException("Type not found for array element type " + elementTypeName);
    }
    GeneralType elementType = GeneralType.forClass(elementClass); // not quite right b/c this is element class instead of declaring class
    if (elementType.isGeneric()) {
      GenericArrayType arrayType = new GenericArrayType((GenericType)elementType);

      manager.createTypedOperation(new GenericArrayCreation(arrayType, length), arrayType, inputTypes, arrayType);

    } else {
      ConcreteArrayType arrayType = new ConcreteArrayType((ConcreteType)elementType);
      manager.createTypedOperation(new ArrayCreation(arrayType, length), arrayType, inputTypes, arrayType);
    }



  }

  public void visitEnum(String typeName, String valueName) throws OperationParseException {
    Class<?> type;
    try {
      type = TypeNames.getTypeForName(typeName);
    } catch (ClassNotFoundException e) {
      String msg = "The type given \"" + typeName + "\" was not recognized.";
      throw new OperationParseException(msg);
    }
    if (!type.isEnum()) {
      String msg = "The type given \"" + typeName + "\" is not an enum.";
      throw new OperationParseException(msg);
    }
    ConcreteType declaringType = (ConcreteType)ConcreteType.forClass(type);

    Enum<?> value = valueOf(type,valueName);
    if (value == null) {
      String msg = "The value given \""
                      + valueName
                      + "\" is not a constant of the enum "
                      + typeName
                      + ".";
      throw new OperationParseException(msg);
    }

    manager.createTypedOperation(new EnumConstant(value), declaringType, new GenericTypeTuple(), declaringType);
  }

  public void visitNonreceiverTerm(NonreceiverTerm nonreceiverTerm) {
    manager.createTypedOperation(nonreceiverTerm, nonreceiverTerm.getType(), new GenericTypeTuple(), nonreceiverTerm.getType());
  }


  private GenericTypeTuple getInputTypes(Class<?>[] typeArguments) {
    return getInputTypes(null, typeArguments);
  }

  private GenericTypeTuple getInputTypes(GeneralType declaringType, Class<?>[] typeArguments) {
    List<GeneralType> paramTypes = new ArrayList<>();

    if (declaringType != null) {
      paramTypes.add(declaringType);
    }

    for (Class<?> c : typeArguments) {
      paramTypes.add(GeneralType.forClass(c));
    }

    return new GenericTypeTuple(paramTypes);
  }

  /**
   * valueOf searches the enum constant list of a class for a constant with the given name.
   * Note: cannot make this work using valueOf method of Enum due to typing.
   *
   * @param type class that is already known to be an enum.
   * @param valueName name for value that may be a constant of the enum.
   * @return reference to actual constant value, or null if none exists in type.
   */
  private static Enum<?> valueOf(Class<?> type, String valueName) {
    for (Object obj : type.getEnumConstants()) {
      Enum<?> e = (Enum<?>) obj;
      if (e.name().equals(valueName)) {
        return e;
      }
    }
    return null;
  }

  /**
   * Searches the field list of a class for a field that has the given name.
   *
   * @param type
   *          - class object.
   * @param fieldName
   *          - field name for which to search the class.
   * @return field of the class with the given name.
   */
  private static Field fieldForName(Class<?> type, String fieldName) {
    for (Field f : type.getDeclaredFields()) {
      if (fieldName.equals(f.getName())) {
        return f;
      }
    }
    return null;
  }


}
