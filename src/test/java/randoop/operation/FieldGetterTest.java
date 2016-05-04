package randoop.operation;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.Globals;
import randoop.NormalExecution;
import randoop.field.AccessibleField;
import randoop.field.ClassWithFields;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GeneralType;
import randoop.types.SimpleClassOrInterfaceType;
import randoop.types.TypeTuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * FieldGetterTest defines unit tests for FieldGetter class.
 * There is a test method for each kind of PublicField, and each
 * checks types returned, code generation, and execution behavior.
 *
 */
public class FieldGetterTest {

  @Test
  public void testStaticField() {
    Class<?> c = ClassWithFields.class;
    ClassOrInterfaceType classType = new SimpleClassOrInterfaceType(c);
    try {
      Field field = c.getField("fourField");
      GeneralType fieldType = new SimpleClassOrInterfaceType(field.getType());
      TypedOperation rhs = createGetter(field, fieldType, classType);

      //types
      assertTrue("Should be no input types : " + rhs.getInputTypes(), rhs.getInputTypes().isEmpty());
      assertEquals("Output type should match type of field", fieldType, rhs.getOutputType());

      //code generation
      String expected = "int i0 = randoop.field.ClassWithFields.fourField;" + Globals.lineSep;
      Statement st = new Statement(rhs);
      Sequence seq = new Sequence().extend(rhs, new ArrayList<Variable>());
      Variable var = new Variable(seq, 0);
      StringBuilder b = new StringBuilder();
      st.appendCode(var, new ArrayList<Variable>(), b);
      assertEquals("Expect initialization of variable from static field", expected, b.toString());

      //execution - should be 4 (haven't changed value yet)
      NormalExecution expectedExec = new NormalExecution(4, 0);
      NormalExecution actualExec = (NormalExecution) rhs.execute(new Object[0], null);
      assertTrue(
          "Execution should simply return value",
          expectedExec.getRuntimeValue().equals(actualExec.getRuntimeValue())
              && expectedExec.getExecutionTime() == actualExec.getExecutionTime());

    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    }
  }



  @Test
  public void testInstanceField() {
    Class<?> c = ClassWithFields.class;
    ClassOrInterfaceType classType = new SimpleClassOrInterfaceType(c);
    try {

      Field field = c.getField("oneField");
      GeneralType fieldType = new SimpleClassOrInterfaceType(field.getType());
      TypedOperation rhs = createGetter(field, fieldType, classType);

      //types
      List<GeneralType> inputTypes = new ArrayList<>();
      inputTypes.add(classType);
      assertEquals("Input types should just be declaring class", new TypeTuple(inputTypes), rhs.getInputTypes());
      assertEquals("Output type should match type of field", fieldType, rhs.getOutputType());

      //code generation
      String expected = "int i1 = classWithFields0.oneField;" + Globals.lineSep;

      //first need a variable referring to an instance
      // - sequence where one is declared and initialized by constructed object
      Constructor<?> constructor = null;
      try {
        constructor = c.getConstructor();
      } catch (NoSuchMethodException e) {
        fail("didn't load constructor " + e);
      }
      assert constructor != null;
      ConstructorCall cons = new ConstructorCall(constructor);
      TypedOperation consOp = new TypedClassOperation(cons, classType, new TypeTuple(), classType);
      Sequence seqInit = new Sequence().extend(consOp, new ArrayList<Variable>());

      ArrayList<Variable> vars = new ArrayList<>();
      vars.add(new Variable(seqInit, 0));
      // bind getter "call" to initialization
      Statement st_rhs = new Statement(rhs);
      Sequence seq = seqInit.extend(rhs, vars);
      // - first variable is object
      Variable var1 = new Variable(seq, 0);
      // - second variable is for value
      Variable var2 = new Variable(seq, 1);
      vars = new ArrayList<>();
      vars.add(var1);
      StringBuilder b = new StringBuilder();
      st_rhs.appendCode(var2, vars, b);
      assertEquals("Expect initialization of variable from static field", expected, b.toString());

      //execution
      //null object
      Object[] inputs = {null};
      ExecutionOutcome nullOutcome = rhs.execute(inputs, null);
      assertTrue(
          "Expect null pointer exception",
          nullOutcome instanceof ExceptionalExecution
              && ((ExceptionalExecution) nullOutcome).getException()
                  instanceof NullPointerException);

      //actual object
      NormalExecution expectedExec = new NormalExecution(1, 0);
      inputs = new Object[1];
      inputs[0] = c.newInstance();
      NormalExecution actualExec = (NormalExecution) rhs.execute(inputs, null);
      assertTrue(
          "Execution should simply return value",
          expectedExec.getRuntimeValue().equals(actualExec.getRuntimeValue())
              && expectedExec.getExecutionTime() == actualExec.getExecutionTime());

    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    } catch (InstantiationException e) {
      fail("test failed because of unexpected exception creating class instance");
    } catch (IllegalAccessException e) {
      fail("test failed because of unexpected access exception when creating instance");
      e.printStackTrace();
    }
  }

  @Test
  public void testStaticFinalField() {
    Class<?> c = ClassWithFields.class;
    ClassOrInterfaceType classType = new SimpleClassOrInterfaceType(c);
    try {

      Field field = c.getField("FIVEFIELD");
      GeneralType fieldType = new SimpleClassOrInterfaceType(field.getType());
      TypedOperation rhs = createGetter(field, fieldType, classType);


      //types
      assertTrue("Should be no input types", rhs.getInputTypes().isEmpty());
      assertEquals("Output type should match type of field", fieldType, rhs.getOutputType());

      //code generation
      String expected = "int i0 = randoop.field.ClassWithFields.FIVEFIELD;" + Globals.lineSep;
      Statement st_rhs = new Statement(rhs);
      Sequence seq = new Sequence().extend(rhs, new ArrayList<Variable>());
      Variable var = new Variable(seq, 0);
      StringBuilder b = new StringBuilder();
      st_rhs.appendCode(var, new ArrayList<Variable>(), b);
      assertEquals(
          "Expect initialization of variable from static final field", expected, b.toString());

      //execution --- has value 5
      NormalExecution expectedExec = new NormalExecution(5, 0);
      NormalExecution actualExec = (NormalExecution) rhs.execute(new Object[0], null);
      assertTrue(
          "Execution should simply return value",
          expectedExec.getRuntimeValue().equals(actualExec.getRuntimeValue())
              && expectedExec.getExecutionTime() == actualExec.getExecutionTime());

    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    }
  }

  @Test
  public void parseable() {
    String getterDescr = "randoop.field.ClassWithFields.<get>(oneField)";
    try {
      TypedOperation getter = FieldGet.parse(getterDescr);
      assertEquals(
          "parse should return object that converts to string",
          getterDescr,
          getter.toParsableString());
    } catch (OperationParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }

  private TypedOperation createGetter(Field field, GeneralType fieldType, ClassOrInterfaceType declaringType) {
    AccessibleField f = new AccessibleField(field, declaringType);
    List<GeneralType> getInputTypesList = new ArrayList<>();
    if (! Modifier.isStatic(field.getModifiers() & Modifier.fieldModifiers())) {
      getInputTypesList.add(declaringType);
    }
    FieldGet getOp = new FieldGet(f);
    return new TypedClassOperation(getOp, declaringType, new TypeTuple(getInputTypesList), fieldType);
  }
}
