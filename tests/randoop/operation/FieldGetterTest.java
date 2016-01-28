package randoop.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.Globals;
import randoop.NormalExecution;
import randoop.field.InstanceField;
import randoop.field.StaticField;
import randoop.field.StaticFinalField;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.sequence.Variable;

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
    try {
      
      StaticField sf = new StaticField(c.getField("fourField"));
      FieldGet rhs = new FieldGet(sf);

      //types
      assertTrue("Should be no input types", rhs.getInputTypes().isEmpty());
      assertEquals("Output type should match type of field", sf.getType(), rhs.getOutputType());

      //code generation
      String expected = "int i0 = randoop.operation.ClassWithFields.fourField;" + Globals.lineSep;
      Statement st = new Statement(rhs);
      Sequence seq = new Sequence().extend(rhs, new ArrayList<Variable>());
      Variable var = new Variable(seq, 0);
      StringBuilder b = new StringBuilder();
      st.appendCode(var, new ArrayList<Variable>(), b);
      assertEquals("Expect initialization of variable from static field", expected, b.toString());

      //execution - should be 4 (haven't changed value yet)
      NormalExecution expectedExec = new NormalExecution(4, 0);
      NormalExecution actualExec = (NormalExecution)rhs.execute(new Object[0], null);
      assertTrue("Execution should simply return value", 
          expectedExec.getRuntimeValue().equals(actualExec.getRuntimeValue()) && 
          expectedExec.getExecutionTime() == actualExec.getExecutionTime());
      
    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    }

  }

  @Test
  public void testInstanceField() {
    Class<?> c = ClassWithFields.class;
    try {
      
      InstanceField f = new InstanceField(c.getField("oneField"));
      FieldGet rhs = new FieldGet(f);

      //types
      List<Class<?>> inputTypes = new ArrayList<>();
      inputTypes.add(c);
      assertEquals("Input types should just be declaring class", inputTypes, rhs.getInputTypes());
      assertEquals("Output type should match type of field", f.getType(), rhs.getOutputType());

      //code generation
      String expected = "int i1 = classWithFields0.oneField;" + Globals.lineSep;

      //first need a variable referring to an instance
      // - sequence where one is declared and initialized by constructed object
      ConstructorCall cons = new ConstructorCall(
          ConstructorSignatures.getConstructorForSignatureString("randoop.operation.ClassWithFields.ClassWithFields()"));
      Sequence seqInit = new Sequence().extend(cons, new ArrayList<Variable>());
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
      ExecutionOutcome nullOutcome = rhs.execute(new Object[0], null);
      assertTrue("Expect null pointer exception", nullOutcome instanceof ExceptionalExecution && 
          ((ExceptionalExecution)nullOutcome).getException() instanceof NullPointerException);
      
      //actual object
      NormalExecution expectedExec = new NormalExecution(1, 0); 
      Object[] inputs = new Object[1];
      inputs[0] = c.newInstance();
      NormalExecution actualExec = (NormalExecution)rhs.execute(inputs, null);
      assertTrue("Execution should simply return value", 
          expectedExec.getRuntimeValue().equals(actualExec.getRuntimeValue()) && 
          expectedExec.getExecutionTime() == actualExec.getExecutionTime());
      
    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    } catch (InstantiationException e) {
      fail("test failed because of unexpected exception creating class instance");
    } catch (IllegalAccessException e) {
      fail("test failed because of unexpected access exception when creating instance");
      e.printStackTrace();
    } catch (OperationParseException e) {
      fail("test failed because ClassWithFields constructor not found");
    }

  }

  @Test
  public void testStaticFinalField() {
    Class<?> c = ClassWithFields.class;
    try {
      
      StaticFinalField f = new StaticFinalField(c.getField("FIVEFIELD"));
      FieldGet rhs = new FieldGet(f);

      //types
      assertTrue("Should be no input types", rhs.getInputTypes().isEmpty());
      assertEquals("Output type should match type of field", f.getType(), rhs.getOutputType());

      //code generation
      String expected = "int i0 = randoop.operation.ClassWithFields.FIVEFIELD;" + Globals.lineSep;
      Statement st_rhs = new Statement(rhs);
      Sequence seq = new Sequence().extend(rhs, new ArrayList<Variable>());
      Variable var = new Variable(seq, 0);
      StringBuilder b = new StringBuilder();
      st_rhs.appendCode(var, new ArrayList<Variable>(), b);
      assertEquals("Expect initialization of variable from static final field",
          expected, b.toString());

      //execution --- has value 5
      NormalExecution expectedExec = new NormalExecution(5, 0);
      NormalExecution actualExec = (NormalExecution)rhs.execute(new Object[0], null);
      assertTrue("Execution should simply return value",
          expectedExec.getRuntimeValue().equals(actualExec.getRuntimeValue()) && 
          expectedExec.getExecutionTime() == actualExec.getExecutionTime());
      
    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    }

  }

  @Test
  public void parseable() {
    String getterDescr = "<get>(int:randoop.operation.ClassWithFields.oneField)";
    try {
      FieldGet getter = FieldGet.parse(getterDescr);
      assertEquals("parse should return object that converts to string", getterDescr, getter.toParseableString());
    } catch (OperationParseException e) {
     fail("Parse error: " + e.getMessage());
    }
    
  }

}
