package randoop.operation;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import randoop.types.ArrayType;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * These tests are disabled in build.gradle
 */
public class OperationParserTests {

  @Test
  public void testNonreceiver() {

    //Class
    checkParse(
        new NonreceiverTerm(JavaTypes.CLASS_TYPE, Comparable.class),
        new TypeTuple(),
        JavaTypes.CLASS_TYPE);

    // String.
    checkParse(
        new NonreceiverTerm(JavaTypes.STRING_TYPE, null), new TypeTuple(), JavaTypes.STRING_TYPE);
    checkParse(
        new NonreceiverTerm(JavaTypes.STRING_TYPE, ""), new TypeTuple(), JavaTypes.STRING_TYPE);
    checkParse(
        new NonreceiverTerm(JavaTypes.STRING_TYPE, " "), new TypeTuple(), JavaTypes.STRING_TYPE);
    checkParse(
        new NonreceiverTerm(JavaTypes.STRING_TYPE, "\""), new TypeTuple(), JavaTypes.STRING_TYPE);
    checkParse(
        new NonreceiverTerm(JavaTypes.STRING_TYPE, "\n"), new TypeTuple(), JavaTypes.STRING_TYPE);
    checkParse(
        new NonreceiverTerm(JavaTypes.STRING_TYPE, "\u0000"),
        new TypeTuple(),
        JavaTypes.STRING_TYPE);

    // Object.
    checkParse(
        new NonreceiverTerm(JavaTypes.OBJECT_TYPE, null),
        JavaTypes.OBJECT_TYPE,
        new TypeTuple(),
        JavaTypes.OBJECT_TYPE);
    try {
      checkParse(
          new NonreceiverTerm(JavaTypes.OBJECT_TYPE, new Object()),
          JavaTypes.OBJECT_TYPE,
          new TypeTuple(),
          JavaTypes.OBJECT_TYPE);
      fail("did not throw exception");
    } catch (IllegalArgumentException e) {
      // Good.
    }

    // Array.
    Type arrayType;
    arrayType = Type.forClass(new Object[][] {}.getClass());
    checkParse(new NonreceiverTerm(arrayType, null), new TypeTuple(), arrayType);

    // Primitives.
    checkParse(new NonreceiverTerm(JavaTypes.INT_TYPE, 0), new TypeTuple(), JavaTypes.INT_TYPE);
    checkParse(new NonreceiverTerm(JavaTypes.INT_TYPE, 1), new TypeTuple(), JavaTypes.INT_TYPE);
    checkParse(new NonreceiverTerm(JavaTypes.INT_TYPE, -1), new TypeTuple(), JavaTypes.INT_TYPE);
    checkParse(
        new NonreceiverTerm(JavaTypes.INT_TYPE, Integer.MAX_VALUE),
        new TypeTuple(),
        JavaTypes.INT_TYPE);
    checkParse(
        new NonreceiverTerm(JavaTypes.INT_TYPE, Integer.MIN_VALUE),
        new TypeTuple(),
        JavaTypes.INT_TYPE);

    checkParse(
        new NonreceiverTerm(JavaTypes.BYTE_TYPE, (byte) 0), new TypeTuple(), JavaTypes.BYTE_TYPE);
    checkParse(
        new NonreceiverTerm(JavaTypes.SHORT_TYPE, (short) 0),
        new TypeTuple(),
        JavaTypes.SHORT_TYPE);
    checkParse(
        new NonreceiverTerm(JavaTypes.LONG_TYPE, (long) 0), new TypeTuple(), JavaTypes.LONG_TYPE);
    checkParse(
        new NonreceiverTerm(JavaTypes.FLOAT_TYPE, (float) 0),
        new TypeTuple(),
        JavaTypes.FLOAT_TYPE);
    checkParse(
        new NonreceiverTerm(JavaTypes.DOUBLE_TYPE, (double) 0),
        new TypeTuple(),
        JavaTypes.DOUBLE_TYPE);
    checkParse(
        new NonreceiverTerm(JavaTypes.BOOLEAN_TYPE, false),
        new TypeTuple(),
        JavaTypes.BOOLEAN_TYPE);

    checkParse(new NonreceiverTerm(JavaTypes.CHAR_TYPE, ' '), new TypeTuple(), JavaTypes.CHAR_TYPE);
    checkParse(
        new NonreceiverTerm(JavaTypes.CHAR_TYPE, '\u0000'), new TypeTuple(), JavaTypes.CHAR_TYPE);
    checkParse(
        new NonreceiverTerm(JavaTypes.CHAR_TYPE, '\''), new TypeTuple(), JavaTypes.CHAR_TYPE);
    checkParse(new NonreceiverTerm(JavaTypes.CHAR_TYPE, '0'), new TypeTuple(), JavaTypes.CHAR_TYPE);
  }

  @Test
  public void testRMethod() {

    for (Method m : ArrayList.class.getMethods()) {
      ClassOrInterfaceType declaringType;
      declaringType = ClassOrInterfaceType.forClass(m.getDeclaringClass());
      List<Type> paramTypes = new ArrayList<>();
      for (java.lang.reflect.Type t : m.getGenericParameterTypes()) {
        paramTypes.add(Type.forType(t));
      }
      TypeTuple inputTypes = new TypeTuple(paramTypes);
      Type outputType;
      outputType = Type.forType(m.getGenericReturnType());
      checkParse(new MethodCall(m), declaringType, inputTypes, outputType);
    }
  }

  @Test
  public void testRConstructor() {

    for (Constructor<?> c : ArrayList.class.getConstructors()) {
      ClassOrInterfaceType declaringType;
      declaringType = ClassOrInterfaceType.forClass(c.getDeclaringClass());
      List<Type> paramTypes = new ArrayList<>();
      for (java.lang.reflect.Type t : c.getGenericParameterTypes()) {
        paramTypes.add(Type.forType(t));
      }
      TypeTuple inputTypes = new TypeTuple(paramTypes);
      checkParse(new ConstructorCall(c), declaringType, inputTypes, declaringType);
    }
  }

  @Test
  public void testArrayDecl() {
    Type elementType = JavaTypes.INT_TYPE;
    ArrayType arrayType = ArrayType.ofComponentType(elementType);
    List<Type> paramTypes = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      paramTypes.add(elementType);
    }
    TypeTuple inputTypes = new TypeTuple(paramTypes);
    checkParse(new InitializedArrayCreation(arrayType, 3), inputTypes, arrayType);
  }

  private void checkParse(CallableOperation st, TypeTuple inputTypes, Type outputType) {
    String stStr = st.toParsableString(null, inputTypes, outputType);
    TypedOperation stOp = new TypedTermOperation(st, inputTypes, outputType);
    checkOp(st, stStr, stOp);
  }

  private void checkParse(
      CallableOperation st,
      ClassOrInterfaceType declaringType,
      TypeTuple inputTypes,
      Type outputType) {
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
