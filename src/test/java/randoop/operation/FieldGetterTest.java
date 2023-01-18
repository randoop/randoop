package randoop.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.field.AccessibleField;
import randoop.field.ClassWithFields;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.NonParameterizedType;
import randoop.types.PrimitiveType;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * FieldGetterTest defines unit tests for FieldGetter class. There is a test method for each kind of
 * PublicField, and each checks types returned, code generation, and execution behavior.
 */
public class FieldGetterTest {

  @Test
  public void testStaticField() throws NoSuchFieldException, SecurityException {
    Class<?> c = ClassWithFields.class;
    ClassOrInterfaceType classType = new NonParameterizedType(c);
    Field field = c.getField("fourField");
    Type fieldType = PrimitiveType.forClass(field.getType());
    TypedOperation rhs = createGetter(field, fieldType, classType);

    // types
    assertTrue("Should be no input types : " + rhs.getInputTypes(), rhs.getInputTypes().isEmpty());
    assertEquals(fieldType, rhs.getOutputType());

    // code generation
    String expected = "int int0 = randoop.field.ClassWithFields.fourField;";
    Statement st = new Statement(rhs);
    Sequence seq = new Sequence().extend(rhs, new ArrayList<Variable>());
    Variable var = new Variable(seq, 0);
    StringBuilder b = new StringBuilder();
    st.appendCode(var, new ArrayList<Variable>(), b);
    assertEquals(expected, b.toString());

    // execution - should be 4 (haven't changed value yet)
    NormalExecution expectedExec = new NormalExecution(4, 0);
    NormalExecution actualExec = (NormalExecution) rhs.execute(new Object[0]);
    assertEquals(expectedExec.getRuntimeValue(), actualExec.getRuntimeValue());
    assertEquals(expectedExec.getExecutionTime(), actualExec.getExecutionTime());
  }

  @SuppressWarnings("ClassNewInstance")
  @Test
  public void testInstanceField()
      throws NoSuchFieldException, NoSuchMethodException, SecurityException, InstantiationException,
          IllegalAccessException, InvocationTargetException {
    Class<?> c = ClassWithFields.class;
    ClassOrInterfaceType classType = new NonParameterizedType(c);

    Field field = c.getField("oneField");
    Type fieldType = PrimitiveType.forClass(field.getType());
    TypedOperation rhs = createGetter(field, fieldType, classType);

    // types
    List<Type> inputTypes = Collections.singletonList(classType);
    assertEquals(new TypeTuple(inputTypes), rhs.getInputTypes());
    assertEquals(fieldType, rhs.getOutputType());

    // code generation
    String expected = "int int1 = classWithFields0.oneField;";

    // first need a variable referring to an instance
    // - sequence where one is declared and initialized by constructed object
    Constructor<?> constructor;
    try {
      constructor = c.getConstructor();
    } catch (NoSuchMethodException e) {
      fail("didn't load constructor " + e);
      throw new Error("Unreachable");
    }
    ConstructorCall cons = new ConstructorCall(constructor);
    TypedOperation consOp = new TypedClassOperation(cons, classType, new TypeTuple(), classType);
    Sequence seqInit = new Sequence().extend(consOp, new ArrayList<Variable>());

    List<Variable> vars = Collections.singletonList(new Variable(seqInit, 0));
    // bind getter "call" to initialization
    Statement st_rhs = new Statement(rhs);
    Sequence seq = seqInit.extend(rhs, vars);
    // - first variable is object
    Variable var1 = new Variable(seq, 0);
    // - second variable is for value
    Variable var2 = new Variable(seq, 1);
    vars = Collections.singletonList(var1);
    StringBuilder b = new StringBuilder();
    st_rhs.appendCode(var2, vars, b);
    assertEquals(expected, b.toString());

    // execution
    // null object
    Object[] inputs = {null};
    ExecutionOutcome nullOutcome = rhs.execute(inputs);
    assertTrue(
        nullOutcome instanceof ExceptionalExecution
            && ((ExceptionalExecution) nullOutcome).getException() instanceof NullPointerException);

    // actual object
    NormalExecution expectedExec = new NormalExecution(1, 0);
    inputs = new Object[1];
    inputs[0] = c.getDeclaredConstructor().newInstance();
    NormalExecution actualExec = (NormalExecution) rhs.execute(inputs);
    assertEquals(expectedExec.getRuntimeValue(), actualExec.getRuntimeValue());
    assertEquals(expectedExec.getExecutionTime(), actualExec.getExecutionTime());
  }

  @Test
  public void testStaticFinalField() throws NoSuchFieldException {
    Class<?> c = ClassWithFields.class;
    ClassOrInterfaceType classType = new NonParameterizedType(c);

    Field field = c.getField("FIVEFIELD");
    Type fieldType = PrimitiveType.forClass(field.getType());
    TypedOperation rhs = createGetter(field, fieldType, classType);

    // types
    assertTrue(rhs.getInputTypes().isEmpty());
    assertEquals(fieldType, rhs.getOutputType());

    // code generation
    String expected = "int int0 = randoop.field.ClassWithFields.FIVEFIELD;";
    Statement st_rhs = new Statement(rhs);
    Sequence seq = new Sequence().extend(rhs, new ArrayList<Variable>());
    Variable var = new Variable(seq, 0);
    StringBuilder b = new StringBuilder();
    st_rhs.appendCode(var, new ArrayList<Variable>(), b);
    assertEquals(expected, b.toString());

    // execution --- has value 5
    NormalExecution expectedExec = new NormalExecution(5, 0);
    NormalExecution actualExec = (NormalExecution) rhs.execute(new Object[0]);
    assertEquals(expectedExec.getRuntimeValue(), actualExec.getRuntimeValue());
    assertEquals(expectedExec.getExecutionTime(), actualExec.getExecutionTime());
  }

  @Test
  public void parseable() {
    String getterDescr = "randoop.field.ClassWithFields.<get>(oneField)";
    try {
      TypedOperation getter = FieldGet.parse(getterDescr);
      assertEquals(getterDescr, getter.toParsableString());
    } catch (OperationParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }

  private TypedOperation createGetter(
      Field field, Type fieldType, ClassOrInterfaceType declaringType) {
    AccessibleField f = new AccessibleField(field, declaringType);
    List<Type> getInputTypesList = new ArrayList<>();
    if (!Modifier.isStatic(field.getModifiers() & Modifier.fieldModifiers())) {
      getInputTypesList.add(declaringType);
    }
    FieldGet getOp = new FieldGet(f);
    return new TypedClassOperation(
        getOp, declaringType, new TypeTuple(getInputTypesList), fieldType);
  }
}
