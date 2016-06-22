package randoop.operation;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import randoop.types.ArrayType;
import randoop.types.ClassOrInterfaceType;
import randoop.types.ConcreteTypes;
import randoop.types.GeneralType;
import randoop.types.TypeTuple;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OperationParserTests {

  @Test
  public void testPrimStKind() {

    // String.
    checkParse(
        new NonreceiverTerm(ConcreteTypes.STRING_TYPE, null),
        new TypeTuple(),
        ConcreteTypes.STRING_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.STRING_TYPE, ""),
        new TypeTuple(),
        ConcreteTypes.STRING_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.STRING_TYPE, " "),
        new TypeTuple(),
        ConcreteTypes.STRING_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.STRING_TYPE, "\""),
        new TypeTuple(),
        ConcreteTypes.STRING_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.STRING_TYPE, "\n"),
        new TypeTuple(),
        ConcreteTypes.STRING_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.STRING_TYPE, "\u0000"),
        new TypeTuple(),
        ConcreteTypes.STRING_TYPE);

    // Object.
    checkParse(
        new NonreceiverTerm(ConcreteTypes.OBJECT_TYPE, null),
        ConcreteTypes.OBJECT_TYPE,
        new TypeTuple(),
        ConcreteTypes.OBJECT_TYPE);
    try {
      checkParse(
          new NonreceiverTerm(ConcreteTypes.OBJECT_TYPE, new Object()),
          ConcreteTypes.OBJECT_TYPE,
          new TypeTuple(),
          ConcreteTypes.OBJECT_TYPE);
      fail("did not throw exception");
    } catch (IllegalArgumentException e) {
      // Good.
    }

    // Array.
    GeneralType arrayType;
    arrayType = GeneralType.forClass(new Object[][] {}.getClass());
    checkParse(new NonreceiverTerm(arrayType, null), new TypeTuple(), arrayType);

    // Primitives.
    checkParse(
        new NonreceiverTerm(ConcreteTypes.INT_TYPE, 0), new TypeTuple(), ConcreteTypes.INT_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.INT_TYPE, 1), new TypeTuple(), ConcreteTypes.INT_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.INT_TYPE, -1), new TypeTuple(), ConcreteTypes.INT_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.INT_TYPE, Integer.MAX_VALUE),
        new TypeTuple(),
        ConcreteTypes.INT_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.INT_TYPE, Integer.MIN_VALUE),
        new TypeTuple(),
        ConcreteTypes.INT_TYPE);

    checkParse(
        new NonreceiverTerm(ConcreteTypes.BYTE_TYPE, (byte) 0),
        new TypeTuple(),
        ConcreteTypes.BYTE_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.SHORT_TYPE, (short) 0),
        new TypeTuple(),
        ConcreteTypes.SHORT_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.LONG_TYPE, (long) 0),
        new TypeTuple(),
        ConcreteTypes.LONG_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.FLOAT_TYPE, (float) 0),
        new TypeTuple(),
        ConcreteTypes.FLOAT_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.DOUBLE_TYPE, (double) 0),
        new TypeTuple(),
        ConcreteTypes.DOUBLE_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.BOOLEAN_TYPE, false),
        new TypeTuple(),
        ConcreteTypes.BOOLEAN_TYPE);

    checkParse(
        new NonreceiverTerm(ConcreteTypes.CHAR_TYPE, ' '),
        new TypeTuple(),
        ConcreteTypes.CHAR_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.CHAR_TYPE, '\u0000'),
        new TypeTuple(),
        ConcreteTypes.CHAR_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.CHAR_TYPE, '\''),
        new TypeTuple(),
        ConcreteTypes.CHAR_TYPE);
    checkParse(
        new NonreceiverTerm(ConcreteTypes.CHAR_TYPE, '0'),
        new TypeTuple(),
        ConcreteTypes.CHAR_TYPE);
  }

  @Test
  public void testRMethod() {

    for (Method m : ArrayList.class.getMethods()) {
      ClassOrInterfaceType declaringType;
      declaringType = ClassOrInterfaceType.forClass(m.getDeclaringClass());
      List<GeneralType> paramTypes = new ArrayList<>();
      for (Type t : m.getGenericParameterTypes()) {
        paramTypes.add(GeneralType.forType(t));
      }
      TypeTuple inputTypes = new TypeTuple(paramTypes);
      GeneralType outputType;
      outputType = GeneralType.forType(m.getGenericReturnType());
      checkParse(new MethodCall(m), declaringType, inputTypes, outputType);
    }
  }

  @Test
  public void testRConstructor() {

    for (Constructor<?> c : ArrayList.class.getConstructors()) {
      ClassOrInterfaceType declaringType;
      declaringType = ClassOrInterfaceType.forClass(c.getDeclaringClass());
      List<GeneralType> paramTypes = new ArrayList<>();
      for (Type t : c.getGenericParameterTypes()) {
        paramTypes.add(GeneralType.forType(t));
      }
      TypeTuple inputTypes = new TypeTuple(paramTypes);
      checkParse(new ConstructorCall(c), declaringType, inputTypes, declaringType);
    }
  }

  @Test
  public void testArrayDecl() {
    GeneralType elementType = ConcreteTypes.INT_TYPE;
    ArrayType arrayType = ArrayType.ofElementType(elementType);
    List<GeneralType> paramTypes = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      paramTypes.add(elementType);
    }
    TypeTuple inputTypes = new TypeTuple(paramTypes);
    checkParse(new ArrayCreation(arrayType, 3), inputTypes, arrayType);
  }

  private void checkParse(CallableOperation st, TypeTuple inputTypes, GeneralType outputType) {
    String stStr = st.toParsableString(null, inputTypes, outputType);
    TypedOperation stOp = new TypedTermOperation(st, inputTypes, outputType);
    checkOp(st, stStr, stOp);
  }

  private void checkParse(
      CallableOperation st,
      ClassOrInterfaceType declaringType,
      TypeTuple inputTypes,
      GeneralType outputType) {
    String stStr = st.toParsableString(declaringType, inputTypes, outputType);
    TypedOperation stOp = new TypedClassOperation(st, declaringType, inputTypes, outputType);
    System.out.println(stStr);

    checkOp(st, stStr, stOp);
  }

  private void checkOp(CallableOperation st, String stStr, TypedOperation stOp) {
    TypedOperation collectedOperation;
    try {
      collectedOperation = OperationParser.parse(OperationParser.getId(stOp) + ":" + stStr);
    } catch (OperationParseException e) {
      throw new Error(e);
    }

    assertTrue(
        st.toString() + "," + collectedOperation.toString(), collectedOperation.equals(stOp));
    assertTrue(
        stStr + "," + collectedOperation.toParsableString(),
        stStr.equals(collectedOperation.toParsableString()));
  }
}
