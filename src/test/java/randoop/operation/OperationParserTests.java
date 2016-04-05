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
import randoop.types.GenericType;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OperationParserTests {

  @Test
  public void testPrimStKind() {

    // String.
    checkParse(new NonreceiverTerm(ConcreteType.STRING_TYPE, null), ConcreteType.STRING_TYPE, new ConcreteTypeTuple(), ConcreteType.STRING_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.STRING_TYPE, ""), ConcreteType.STRING_TYPE, new ConcreteTypeTuple(), ConcreteType.STRING_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.STRING_TYPE, " "), ConcreteType.STRING_TYPE, new ConcreteTypeTuple(), ConcreteType.STRING_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.STRING_TYPE, "\""), ConcreteType.STRING_TYPE, new ConcreteTypeTuple(), ConcreteType.STRING_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.STRING_TYPE, "\n"), ConcreteType.STRING_TYPE, new ConcreteTypeTuple(), ConcreteType.STRING_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.STRING_TYPE, "\u0000"), ConcreteType.STRING_TYPE, new ConcreteTypeTuple(), ConcreteType.STRING_TYPE);

    // Object.
    checkParse(new NonreceiverTerm(ConcreteType.OBJECT_TYPE, null), ConcreteType.OBJECT_TYPE, new ConcreteTypeTuple(), ConcreteType.OBJECT_TYPE);
    try {
      checkParse(new NonreceiverTerm(ConcreteType.OBJECT_TYPE, new Object()), ConcreteType.OBJECT_TYPE, new ConcreteTypeTuple(), ConcreteType.OBJECT_TYPE);
      fail("did not throw exception");
    } catch (IllegalArgumentException e) {
      // Good.
    }

    // Array.
    ConcreteType arrayType = ConcreteType.forClass(new Object[][]{}.getClass());
    checkParse(new NonreceiverTerm(arrayType, null), arrayType, new ConcreteTypeTuple(), arrayType);

    // Primitives.
    checkParse(new NonreceiverTerm(ConcreteType.INT_TYPE, 0), ConcreteType.INT_TYPE, new ConcreteTypeTuple(), ConcreteType.INT_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.INT_TYPE, 1), ConcreteType.INT_TYPE, new ConcreteTypeTuple(), ConcreteType.INT_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.INT_TYPE, -1), ConcreteType.INT_TYPE, new ConcreteTypeTuple(), ConcreteType.INT_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.INT_TYPE, Integer.MAX_VALUE), ConcreteType.INT_TYPE, new ConcreteTypeTuple(), ConcreteType.INT_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.INT_TYPE, Integer.MIN_VALUE), ConcreteType.INT_TYPE, new ConcreteTypeTuple(), ConcreteType.INT_TYPE);

    checkParse(new NonreceiverTerm(ConcreteType.BYTE_TYPE, (byte) 0), ConcreteType.BYTE_TYPE, new ConcreteTypeTuple(), ConcreteType.BYTE_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.SHORT_TYPE, (short) 0), ConcreteType.SHORT_TYPE, new ConcreteTypeTuple(), ConcreteType.SHORT_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.LONG_TYPE, (long) 0), ConcreteType.LONG_TYPE, new ConcreteTypeTuple(), ConcreteType.LONG_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.FLOAT_TYPE, (float) 0), ConcreteType.FLOAT_TYPE, new ConcreteTypeTuple(), ConcreteType.FLOAT_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.DOUBLE_TYPE, (double) 0), ConcreteType.DOUBLE_TYPE, new ConcreteTypeTuple(), ConcreteType.DOUBLE_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.BOOLEAN_TYPE, false), ConcreteType.BOOLEAN_TYPE, new ConcreteTypeTuple(), ConcreteType.BOOLEAN_TYPE);

    checkParse(new NonreceiverTerm(ConcreteType.CHAR_TYPE, ' '), ConcreteType.CHAR_TYPE, new ConcreteTypeTuple(), ConcreteType.CHAR_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.CHAR_TYPE, '\u0000'), ConcreteType.CHAR_TYPE, new ConcreteTypeTuple(), ConcreteType.CHAR_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.CHAR_TYPE, '\''), ConcreteType.CHAR_TYPE, new ConcreteTypeTuple(), ConcreteType.CHAR_TYPE);
    checkParse(new NonreceiverTerm(ConcreteType.CHAR_TYPE, '0'), ConcreteType.CHAR_TYPE, new ConcreteTypeTuple(), ConcreteType.CHAR_TYPE);
  }

  @Test
  public void testRMethod() {

    for (Method m : ArrayList.class.getMethods()) {
      ConcreteType declaringType = ConcreteType.forClass(m.getDeclaringClass());
      List<ConcreteType> paramTypes = new ArrayList<>();
      for (Type t : m.getGenericParameterTypes()) {
        paramTypes.add(ConcreteType.forType(t));
      }
      ConcreteTypeTuple inputTypes = new ConcreteTypeTuple(paramTypes);
      ConcreteType outputType = ConcreteType.forType(m.getGenericReturnType());
      checkParse(new MethodCall(m), declaringType, inputTypes, outputType);
    }
  }

  @Test
  public void testRConstructor() {

    for (Constructor<?> c : ArrayList.class.getConstructors()) {
      ConcreteType declaringType = ConcreteType.forClass(c.getDeclaringClass());
      List<ConcreteType> paramTypes = new ArrayList<>();
      for (Type t : c.getGenericParameterTypes()) {
        paramTypes.add(ConcreteType.forType(t));
      }
      ConcreteTypeTuple inputTypes = new ConcreteTypeTuple(paramTypes);
      checkParse(new ConstructorCall(c), declaringType, inputTypes, declaringType);
    }
  }

  @Test
  public void testArrayDecl() {
    ConcreteType elementType = ConcreteType.INT_TYPE;
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
    final List<ConcreteOperation> collected = new ArrayList<>();
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
      public void addConcreteOperation(GenericType declaringType, ConcreteOperation operation) {
        collected.add(operation);
      }

      @Override
      public void addGenericOperation(ConcreteType declaringType, GenericOperation operation) {
        super.addGenericOperation(declaringType, operation);
      }

      @Override
      public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
        super.addConcreteOperation(declaringType, operation);
      }
    });

    try {
      OperationParser.parse(OperationParser.getId(stOp) + ":" + stStr, operationManager);
    } catch (OperationParseException e) {
      throw new Error(e);
    }
    assertTrue("collected one operation: ", collected.size() == 1);
    ConcreteOperation collectedOperation = collected.get(0);
    assertTrue(st.toString() + "," + collectedOperation.toString(), collectedOperation.equals(stOp));
    assertTrue(
        stStr + "," + collectedOperation.toParseableString(),
        stStr.equals(collectedOperation.toParseableString()));
  }
}
