package randoop.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.field.AccessibleField;
import randoop.field.ClassWithFields;
import randoop.reflection.StaticCache;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.NonParameterizedType;
import randoop.types.PrimitiveType;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * FieldSetterTest defines unit tests for FieldSetter class. There is a test method for each kind of
 * PublicField, and each checks types returned, code generation, and execution behavior.
 */
public class FieldSetterTest {

  private static StaticCache cacheClassWithFields;

  @BeforeClass
  public static void saveState() {
    cacheClassWithFields = new StaticCache(ClassWithFields.class);
    cacheClassWithFields.saveState();
  }

  @AfterClass
  public static void restoreState() {
    cacheClassWithFields.restoreState();
  }

  @Test
  public void testStaticField() throws NoSuchFieldException {
    Class<?> c = ClassWithFields.class;
    ClassOrInterfaceType declaringType = new NonParameterizedType(c);
    Field field = c.getField("fourField");
    AccessibleField f = new AccessibleField(field, declaringType);
    Type fieldType = PrimitiveType.forClass(field.getType());
    List<Type> setInputTypeList = Collections.singletonList(fieldType);
    FieldSet setOp = new FieldSet(f);
    TypedOperation op =
        new TypedClassOperation(
            setOp, declaringType, new TypeTuple(setInputTypeList), JavaTypes.VOID_TYPE);

    // types
    assertEquals(1, op.getInputTypes().size());
    assertEquals(JavaTypes.VOID_TYPE, op.getOutputType());

    // code generation
    String expected = "randoop.field.ClassWithFields.fourField = 24;";
    StringBuilder b = new StringBuilder();
    TypedOperation initOp =
        new TypedTermOperation(
            new NonreceiverTerm(JavaTypes.INT_TYPE, 24), new TypeTuple(), JavaTypes.INT_TYPE);
    Sequence seq0 = new Sequence().extend(initOp, new ArrayList<Variable>());
    List<Variable> vars = Collections.singletonList(new Variable(seq0, 0));
    Statement st_op = new Statement(op);
    st_op.appendCode(null, vars, b);
    assertEquals(expected, b.toString());

    // execution -- gives back null
    assertFalse((int) f.getValue(null) == 24);
    NormalExecution expectedExec = new NormalExecution(null, 0);
    Object[] inputs = new Object[1];
    inputs[0] = 24;
    ExecutionOutcome actualExec = op.execute(inputs);
    assertTrue(actualExec instanceof NormalExecution);
    NormalExecution actualNExec = (NormalExecution) actualExec;
    assertEquals(expectedExec.getRuntimeValue(), actualNExec.getRuntimeValue());
    assertEquals(expectedExec.getExecutionTime(), actualNExec.getExecutionTime());
    assertEquals(24, (int) f.getValue(null));
  }

  @SuppressWarnings("ClassNewInstance")
  @Test
  public void testInstanceField()
      throws NoSuchFieldException, NoSuchMethodException, InstantiationException,
          InvocationTargetException, IllegalAccessException {
    Class<?> c = ClassWithFields.class;
    ClassOrInterfaceType declaringType = new NonParameterizedType(c);
    Field field = c.getField("oneField");
    AccessibleField f = new AccessibleField(field, declaringType);
    Type fieldType = new NonParameterizedType(field.getDeclaringClass());
    List<Type> setInputTypeList = new ArrayList<>();
    setInputTypeList.add(declaringType);
    setInputTypeList.add(fieldType);
    FieldSet setOp = new FieldSet(f);
    TypedOperation op =
        new TypedClassOperation(
            setOp, declaringType, new TypeTuple(setInputTypeList), JavaTypes.VOID_TYPE);

    // types
    assertEquals(2, op.getInputTypes().size());
    assertEquals(JavaTypes.VOID_TYPE, op.getOutputType());

    // code generation
    String expected = "classWithFields0.oneField = 24;";
    StringBuilder b = new StringBuilder();
    Constructor<?> constructor;
    try {
      constructor = c.getConstructor();
    } catch (NoSuchMethodException e) {
      fail("didn't load constructor " + e);
      throw new Error("Unreachable");
    }
    ConstructorCall cons = new ConstructorCall(constructor);
    TypedOperation consOp =
        new TypedClassOperation(cons, declaringType, new TypeTuple(), declaringType);

    Sequence seq0 = new Sequence().extend(consOp, new ArrayList<Variable>());
    TypedOperation initOp =
        new TypedTermOperation(
            new NonreceiverTerm(JavaTypes.INT_TYPE, 24), new TypeTuple(), JavaTypes.INT_TYPE);
    Sequence seq1 = seq0.extend(initOp, new ArrayList<Variable>());
    ArrayList<Variable> vars = new ArrayList<>();
    vars.add(new Variable(seq1, 0));
    vars.add(new Variable(seq1, 1));
    Statement st_op = new Statement(op);
    st_op.appendCode(null, vars, b);
    assertEquals(expected, b.toString());

    // execution
    Object[] inputs = new Object[2];
    inputs[0] = null;
    inputs[1] = 9;
    // null object
    ExecutionOutcome nullOutcome = op.execute(inputs);
    assertTrue(
        nullOutcome instanceof ExceptionalExecution
            && ((ExceptionalExecution) nullOutcome).getException() instanceof NullPointerException);

    // real live object
    Object[] inputs2 = new Object[2];
    inputs2[0] = c.getDeclaredConstructor().newInstance();
    inputs2[1] = 9;
    assertFalse(9 == (int) f.getValue(inputs2[0]));
    NormalExecution expectedExec = new NormalExecution(null, 0);
    ExecutionOutcome actualExec = op.execute(inputs2);
    assertTrue(actualExec instanceof NormalExecution);
    NormalExecution actualNExec = (NormalExecution) actualExec;
    assertEquals(expectedExec.getRuntimeValue(), actualNExec.getRuntimeValue());
    assertEquals(expectedExec.getExecutionTime(), actualNExec.getExecutionTime());
    assertEquals(9, (int) f.getValue(inputs2[0]));
  }

  @Test
  public void testFinalField() throws NoSuchFieldException {
    Class<?> c = ClassWithFields.class;
    ClassOrInterfaceType declaringType = new NonParameterizedType(c);
    Field field = c.getField("tenField");
    AccessibleField f = new AccessibleField(field, declaringType);

    try {
      @SuppressWarnings("unused")
      FieldSet setOp = new FieldSet(f);
      fail(
          "IllegalArgumentException expected when final instance field given to FieldSet"
              + " constructor");
    } catch (IllegalArgumentException e) {
      assertEquals("Field may not be final for FieldSet", e.getMessage());
    }
  }

  @Test
  public void testFinalStaticField() throws NoSuchFieldException {
    Class<?> c = ClassWithFields.class;
    ClassOrInterfaceType declaringType = new NonParameterizedType(c);
    Field field = c.getField("FIVEFIELD");
    AccessibleField f = new AccessibleField(field, declaringType);

    try {
      @SuppressWarnings("unused")
      FieldSet op = new FieldSet(f);
      fail(
          "IllegalArgumentException expected when static final field given to FieldSet"
              + " constructor");
    } catch (IllegalArgumentException e) {
      assertEquals("Field may not be final for FieldSet", e.getMessage());
    }
  }

  @Test
  public void parseable() {
    String setterDesc = "randoop.field.ClassWithFields.<set>(oneField)";

    try {
      TypedOperation setter = FieldSet.parse(setterDesc);
      assertEquals(setterDesc, setter.toParsableString());
    } catch (OperationParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }
}
