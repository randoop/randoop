package randoop.operation;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.Globals;
import randoop.NormalExecution;
import randoop.field.AccessibleField;
import randoop.field.ClassWithFields;
import randoop.reflection.ModelCollections;
import randoop.reflection.TypedOperationManager;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.ConcreteType;
import randoop.types.ConcreteTypeTuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * FieldSetterTest defines unit tests for FieldSetter class.
 * There is a test method for each kind of PublicField, and each
 * checks types returned, code generation, and execution behavior.
 *
 */
public class FieldSetterTest {

  @Test
  public void testStaticField() {
    Class<?> c = ClassWithFields.class;
    ConcreteType declaringType = ConcreteType.forClass(c);
    try {
      Field field = c.getField("fourField");
      AccessibleField f = new AccessibleField(field, declaringType);
      ConcreteType fieldType = ConcreteType.forClass(field.getType());
      List<ConcreteType> setInputTypeList = new ArrayList<>();
      setInputTypeList.add(fieldType);
      FieldSet setOp = new FieldSet(f);
      ConcreteOperation op = new ConcreteOperation(setOp, declaringType, new ConcreteTypeTuple(setInputTypeList), ConcreteType.VOID_TYPE); 

      //types
      assertEquals("Should be one input type", 1, op.getInputTypes().size());
      assertEquals("Output type should be void", ConcreteType.VOID_TYPE, op.getOutputType());

      //code generation
      String expected = "randoop.field.ClassWithFields.fourField = 24;" + Globals.lineSep;
      StringBuilder b = new StringBuilder();
      ConcreteOperation initOp = new ConcreteOperation(new NonreceiverTerm(ConcreteType.INT_TYPE, 24), ConcreteType.INT_TYPE, new ConcreteTypeTuple(), ConcreteType.INT_TYPE);
      Sequence seq0 =
          new Sequence().extend(initOp, new ArrayList<Variable>());
      ArrayList<Variable> vars = new ArrayList<>();
      vars.add(new Variable(seq0, 0));
      Statement st_op = new Statement(op);
      st_op.appendCode(null, vars, b);
      assertEquals("Expect assignment to static field", expected, b.toString());

      //execution -- gives back null
      assertFalse("Initial value of static is not 24", (int) f.getValue(null) == 24);
      NormalExecution expectedExec = new NormalExecution(null, 0);
      Object[] inputs = new Object[1];
      inputs[0] = 24;
      ExecutionOutcome actualExec = op.execute(inputs, null);
      assertTrue(
          "outcome of static field set should be normal execution",
          actualExec instanceof NormalExecution);
      NormalExecution actualNExec = (NormalExecution) actualExec;
      assertTrue(
          "Expect void result and zero execution",
          expectedExec.getRuntimeValue() == actualNExec.getRuntimeValue()
              && expectedExec.getExecutionTime() == actualNExec.getExecutionTime());
      assertEquals("Expect value to have changed", 24, (int) f.getValue(null));

    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    }
  }

  @Test
  public void testInstanceField() {
    Class<?> c = ClassWithFields.class;
    ConcreteType declaringType = ConcreteType.forClass(c);
    try {
      Field field = c.getField("oneField");
      AccessibleField f = new AccessibleField(field, declaringType);
      ConcreteType fieldType = ConcreteType.forClass(field.getDeclaringClass());
      List<ConcreteType> setInputTypeList = new ArrayList<>();
      setInputTypeList.add(declaringType);
      setInputTypeList.add(fieldType);
      FieldSet setOp = new FieldSet(f);
      ConcreteOperation op = new ConcreteOperation(setOp, declaringType, new ConcreteTypeTuple(setInputTypeList), ConcreteType.VOID_TYPE);

      //types
      assertEquals("Should be two input types", 2, op.getInputTypes().size());
      assertEquals("Output type should be void", ConcreteType.VOID_TYPE, op.getOutputType());

      //code generation
      String expected = "classWithFields0.oneField = 24;" + Globals.lineSep;
      StringBuilder b = new StringBuilder();
      Constructor<?> constructor = null;
      try {
        constructor = c.getConstructor();
      } catch (NoSuchMethodException e) {
        fail("didn't load constructor " + e);
      }
      assert constructor != null;
      ConstructorCall cons = new ConstructorCall(constructor);
      ConcreteOperation consOp = new ConcreteOperation(cons,declaringType, new ConcreteTypeTuple(), declaringType);

      Sequence seq0 = new Sequence().extend(consOp, new ArrayList<Variable>());
      ConcreteOperation initOp = new ConcreteOperation(new NonreceiverTerm(ConcreteType.INT_TYPE, 24), ConcreteType.INT_TYPE, new ConcreteTypeTuple(), ConcreteType.INT_TYPE);
      Sequence seq1 = seq0.extend(initOp, new ArrayList<Variable>());
      ArrayList<Variable> vars = new ArrayList<>();
      vars.add(new Variable(seq1, 0));
      vars.add(new Variable(seq1, 1));
      Statement st_op = new Statement(op);
      st_op.appendCode(null, vars, b);
      assertEquals("Expect assignment to instance field", expected, b.toString());

      //execution
      Object[] inputs = new Object[2];
      inputs[0] = null;
      inputs[1] = 9;
      //null object
      ExecutionOutcome nullOutcome = op.execute(inputs, null);
      assertTrue(
          "Expect null pointer exception",
          nullOutcome instanceof ExceptionalExecution
              && ((ExceptionalExecution) nullOutcome).getException()
                  instanceof NullPointerException);

      //real live object
      Object[] inputs2 = new Object[2];
      inputs2[0] = c.newInstance();
      inputs2[1] = 9;
      assertFalse("Initial value of field is not 9", 9 == (int) f.getValue(inputs2[0]));
      NormalExecution expectedExec = new NormalExecution(null, 0);
      ExecutionOutcome actualExec = op.execute(inputs2, null);
      assertTrue("outcome should be normal execution", actualExec instanceof NormalExecution);
      NormalExecution actualNExec = (NormalExecution) actualExec;
      assertTrue(
          "Expect void result and zero execution",
          expectedExec.getRuntimeValue() == actualNExec.getRuntimeValue()
              && expectedExec.getExecutionTime() == actualNExec.getExecutionTime());
      assertEquals("Expect value to have changed", 9, (int) f.getValue(inputs2[0]));

    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    } catch (InstantiationException e) {
      fail("test failed because object instantiation failed.");
    } catch (IllegalAccessException e) {
      fail("test failed because of unexpected access exception.");
    }
  }

  @Test
  public void testFinalField() {
    Class<?> c = ClassWithFields.class;
    ConcreteType declaringType = ConcreteType.forClass(c);
    try {
      Field field = c.getField("tenField");
      AccessibleField f = new AccessibleField(field, declaringType);

      try {
        @SuppressWarnings("unused")
        FieldSet setOp = new FieldSet(f);
        fail(
            "IllegalArgumentException expected when final instance field given to FieldSet constructor");
      } catch (IllegalArgumentException e) {
        assertEquals(
            "Argument Exception", "Field may not be final for FieldSet", e.getMessage());
      }
    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    }
  }

  @Test
  public void testFinalStaticField() {
    Class<?> c = ClassWithFields.class;
    ConcreteType declaringType = ConcreteType.forClass(c);
    try {
      Field field = c.getField("FIVEFIELD");
      AccessibleField f = new AccessibleField(field, declaringType);

      try {
        @SuppressWarnings("unused")
        FieldSet op = new FieldSet(f);
        fail(
            "IllegalArgumentException expected when static final field given to FieldSet constructor");
      } catch (IllegalArgumentException e) {
        assertEquals(
            "Argument exception", "Field may not be final for FieldSet", e.getMessage());
      }
    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    }
  }

  @Test
  public void parseable() {
    String setterDesc = "randoop.field.ClassWithFields.<set>(oneField)";
    final List<ConcreteOperation> ops = new ArrayList<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
        ops.add(operation);
      }
    });
    try {
      FieldSet.parse(setterDesc, operationManager);
      assert ops.size() > 0 : "operations should have element";
      ConcreteOperation setter = ops.get(0);
      assertEquals(
          "parse should return object that converts to string",
          setterDesc,
          setter.toParseableString());
    } catch (OperationParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }
}
