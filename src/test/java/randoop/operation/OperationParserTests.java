package randoop.operation;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import randoop.reflection.ModelCollections;
import randoop.reflection.TypedOperationManager;
import randoop.types.ConcreteArrayType;
import randoop.types.ConcreteType;
import randoop.types.ConcreteTypeTuple;
import randoop.types.ConcreteTypes;
import randoop.types.GenericType;
import randoop.types.RandoopTypeException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OperationParserTests {

  @Test
  public void testPrimStKind() {

    // String.
    checkParse(new NonreceiverTerm(ConcreteTypes.STRING_TYPE, null), ConcreteTypes.STRING_TYPE, new ConcreteTypeTuple(), ConcreteTypes.STRING_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.STRING_TYPE, ""), ConcreteTypes.STRING_TYPE, new ConcreteTypeTuple(), ConcreteTypes.STRING_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.STRING_TYPE, " "), ConcreteTypes.STRING_TYPE, new ConcreteTypeTuple(), ConcreteTypes.STRING_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.STRING_TYPE, "\""), ConcreteTypes.STRING_TYPE, new ConcreteTypeTuple(), ConcreteTypes.STRING_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.STRING_TYPE, "\n"), ConcreteTypes.STRING_TYPE, new ConcreteTypeTuple(), ConcreteTypes.STRING_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.STRING_TYPE, "\u0000"), ConcreteTypes.STRING_TYPE, new ConcreteTypeTuple(), ConcreteTypes.STRING_TYPE);

    // Object.
    checkParse(new NonreceiverTerm(ConcreteTypes.OBJECT_TYPE, null), ConcreteTypes.OBJECT_TYPE, new ConcreteTypeTuple(), ConcreteTypes.OBJECT_TYPE);
    try {
      checkParse(new NonreceiverTerm(ConcreteTypes.OBJECT_TYPE, new Object()), ConcreteTypes.OBJECT_TYPE, new ConcreteTypeTuple(), ConcreteTypes.OBJECT_TYPE);
      fail("did not throw exception");
    } catch (IllegalArgumentException e) {
      // Good.
    }

    // Array.
    ConcreteType arrayType = null;
    try {
      arrayType = ConcreteType.forClass(new Object[][]{}.getClass());
    } catch (RandoopTypeException e) {
      fail("Array type type error: " + e);
    }
    checkParse(new NonreceiverTerm(arrayType, null), arrayType, new ConcreteTypeTuple(), arrayType);

    // Primitives.
    checkParse(new NonreceiverTerm(ConcreteTypes.INT_TYPE, 0), ConcreteTypes.INT_TYPE, new ConcreteTypeTuple(), ConcreteTypes.INT_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.INT_TYPE, 1), ConcreteTypes.INT_TYPE, new ConcreteTypeTuple(), ConcreteTypes.INT_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.INT_TYPE, -1), ConcreteTypes.INT_TYPE, new ConcreteTypeTuple(), ConcreteTypes.INT_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.INT_TYPE, Integer.MAX_VALUE), ConcreteTypes.INT_TYPE, new ConcreteTypeTuple(), ConcreteTypes.INT_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.INT_TYPE, Integer.MIN_VALUE), ConcreteTypes.INT_TYPE, new ConcreteTypeTuple(), ConcreteTypes.INT_TYPE);

    checkParse(new NonreceiverTerm(ConcreteTypes.BYTE_TYPE, (byte) 0), ConcreteTypes.BYTE_TYPE, new ConcreteTypeTuple(), ConcreteTypes.BYTE_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.SHORT_TYPE, (short) 0), ConcreteTypes.SHORT_TYPE, new ConcreteTypeTuple(), ConcreteTypes.SHORT_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.LONG_TYPE, (long) 0), ConcreteTypes.LONG_TYPE, new ConcreteTypeTuple(), ConcreteTypes.LONG_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.FLOAT_TYPE, (float) 0), ConcreteTypes.FLOAT_TYPE, new ConcreteTypeTuple(), ConcreteTypes.FLOAT_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.DOUBLE_TYPE, (double) 0), ConcreteTypes.DOUBLE_TYPE, new ConcreteTypeTuple(), ConcreteTypes.DOUBLE_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.BOOLEAN_TYPE, false), ConcreteTypes.BOOLEAN_TYPE, new ConcreteTypeTuple(), ConcreteTypes.BOOLEAN_TYPE);

    checkParse(new NonreceiverTerm(ConcreteTypes.CHAR_TYPE, ' '), ConcreteTypes.CHAR_TYPE, new ConcreteTypeTuple(), ConcreteTypes.CHAR_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.CHAR_TYPE, '\u0000'), ConcreteTypes.CHAR_TYPE, new ConcreteTypeTuple(), ConcreteTypes.CHAR_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.CHAR_TYPE, '\''), ConcreteTypes.CHAR_TYPE, new ConcreteTypeTuple(), ConcreteTypes.CHAR_TYPE);
    checkParse(new NonreceiverTerm(ConcreteTypes.CHAR_TYPE, '0'), ConcreteTypes.CHAR_TYPE, new ConcreteTypeTuple(), ConcreteTypes.CHAR_TYPE);
  }

  @Test
  public void testRMethod() {

    for (Method m : ArrayList.class.getMethods()) {
      ConcreteType declaringType = null;
      try {
        declaringType = ConcreteType.forClass(m.getDeclaringClass());
      } catch (RandoopTypeException e) {
        fail("Type error declaring class: " + e);
      }
      List<ConcreteType> paramTypes = new ArrayList<>();
      for (Type t : m.getGenericParameterTypes()) {
        try {
          paramTypes.add(ConcreteType.forType(t));
        } catch (RandoopTypeException e) {
          fail("Type error parameter: " + e);
        }
      }
      ConcreteTypeTuple inputTypes = new ConcreteTypeTuple(paramTypes);
      ConcreteType outputType = null;
      try {
        outputType = ConcreteType.forType(m.getGenericReturnType());
      } catch (RandoopTypeException e) {
        fail("Type error return type: " + e);
      }
      checkParse(new MethodCall(m), declaringType, inputTypes, outputType);
    }
  }

  @Test
  public void testRConstructor() {

    for (Constructor<?> c : ArrayList.class.getConstructors()) {
      ConcreteType declaringType = null;
      try {
        declaringType = ConcreteType.forClass(c.getDeclaringClass());
      } catch (RandoopTypeException e) {
        fail("Type error: " + e);
      }
      List<ConcreteType> paramTypes = new ArrayList<>();
      for (Type t : c.getGenericParameterTypes()) {
        try {
          paramTypes.add(ConcreteType.forType(t));
        } catch (RandoopTypeException e) {
          fail("Type error: " + e);
        }
      }
      ConcreteTypeTuple inputTypes = new ConcreteTypeTuple(paramTypes);
      checkParse(new ConstructorCall(c), declaringType, inputTypes, declaringType);
    }
  }

  @Test
  public void testArrayDecl() {
    ConcreteType elementType = ConcreteTypes.INT_TYPE;
    ConcreteArrayType arrayType = ConcreteType.forArrayOf(elementType);
    List<ConcreteType> paramTypes = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      paramTypes.add(elementType);
    }
    ConcreteTypeTuple inputTypes = new ConcreteTypeTuple(paramTypes);
    checkParse(new ArrayCreation(arrayType, 3), arrayType, inputTypes, arrayType);
  }

  private void checkParse(CallableOperation st, ConcreteType declaringType, ConcreteTypeTuple inputTypes, ConcreteType outputType) {
    String stStr = st.toParseableString(declaringType, inputTypes, outputType);
    ConcreteOperation stOp = new ConcreteOperation(st, declaringType, inputTypes, outputType);
    System.out.println(stStr);
    final List<ConcreteOperation> concreteOperations = new ArrayList<>();
    final List<GenericOperation> genericOperations = new ArrayList<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {

      @Override
      public void addGenericClassType(GenericType type) {
        fail("not expecting generic class type: " + type.getName());
      }

      @Override
      public void addGenericOperation(GenericType declaringType, GenericOperation operation) {
        fail("not expecting generic operation: " + operation.toString());
      }

      @Override
      public void addGenericOperation(ConcreteType declaringType, GenericOperation operation) {
        genericOperations.add(operation);
      }

      @Override
      public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
        concreteOperations.add(operation);
      }
    });

    try {
      OperationParser.parse(OperationParser.getId(stOp) + ":" + stStr, operationManager);
    } catch (OperationParseException e) {
      throw new Error(e);
    }
    assertTrue("collected one operation: ", concreteOperations.size() == 1);
    ConcreteOperation collectedOperation = concreteOperations.get(0);
    assertTrue(st.toString() + "," + collectedOperation.toString(), collectedOperation.equals(stOp));
    assertTrue(
        stStr + "," + collectedOperation.toParseableString(),
        stStr.equals(collectedOperation.toParseableString()));
    assertTrue("no generic operations: ", genericOperations.size() == 0);
  }
}
