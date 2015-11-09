package randoop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;

import randoop.util.Reflection;


/**
 * FieldSetterTest defines unit tests for FieldSetter class.
 * There is a test method for each kind of PublicField, and each
 * checks types returned, code generation, and execution behavior.
 * 
 * @author bjkeller
 *
 */
public class FieldSetterTest {

  @Test
  public void testStaticField() {
    Class<?> c = ClassWithFields.class;
    try {
      StaticField f = new StaticField(c.getField("fourField"));
      FieldSetter rhs = new FieldSetter(f);
      
      //types
      assertEquals("Should be one input type", 1, rhs.getInputTypes().size());
      assertEquals("Output type should be void", void.class, rhs.getOutputType());
      
      //code generation
      String expected = "randoop.ClassWithFields.fourField = 24;" + Globals.lineSep;
      StringBuilder b = new StringBuilder();
      Sequence seq0 = new Sequence().extend(new PrimitiveOrStringOrNullDecl(int.class,24), new ArrayList<Variable>());
      ArrayList<Variable> vars = new ArrayList<>();
      vars.add(new Variable(seq0,0));
      rhs.appendCode(null, vars, b);
      assertEquals("Expect assignment to static field",expected,b.toString());
      
      //execution -- gives back null
      assertFalse("Initial value of static is not 24", (int)f.getValue(null) == 24);
      NormalExecution expectedExec = new NormalExecution(null,0);
      Object[] inputs = new Object[1];
      inputs[0] = 24;
      ExecutionOutcome actualExec = rhs.execute(inputs, null);
      assertTrue("outcome of static field set should be normal execution", actualExec instanceof NormalExecution);
      NormalExecution actualNExec = (NormalExecution)actualExec;
      assertTrue("Expect void result and zero execution", expectedExec.getRuntimeValue() == actualNExec.getRuntimeValue() && expectedExec.getExecutionTime() == actualNExec.getExecutionTime());
      assertEquals("Expect value to have changed", 24, (int)f.getValue(null));
      
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
      FieldSetter rhs = new FieldSetter(f);
      
      //types
      assertEquals("Should be two input types", 2, rhs.getInputTypes().size());
      assertEquals("Output type should be void", void.class, rhs.getOutputType());      
      
      //code generation
      String expected = "classWithFields0.oneField = 24;" + Globals.lineSep;
      StringBuilder b = new StringBuilder();
      RConstructor cons = new RConstructor(Reflection.getConstructorForSignature("randoop.ClassWithFields.ClassWithFields()"));
      Sequence seq0 = new Sequence().extend(cons, new ArrayList<Variable>());
      Sequence seq1 = seq0.extend(new PrimitiveOrStringOrNullDecl(int.class,24), new ArrayList<Variable>());
      ArrayList<Variable> vars = new ArrayList<>();
      vars.add(new Variable(seq1,0));
      vars.add(new Variable(seq1,1));
      rhs.appendCode(null, vars, b);
      assertEquals("Expect assignment to instance field",expected,b.toString());
      
      //execution
      Object[] inputs = new Object[1];
      inputs[0] = 9;
      //null object
      ExecutionOutcome nullOutcome = rhs.execute(inputs, null);
      assertTrue("Expect null pointer exception", nullOutcome instanceof ExceptionalExecution && ((ExceptionalExecution)nullOutcome).getException() instanceof NullPointerException);
      
      //real live object
      Object[] inputs2 = new Object[2];
      inputs2[0] = c.newInstance();
      inputs2[1] = 9;
      assertFalse("Initial value of field is not 9", 9 == (int)f.getValue(inputs2[0]));
      NormalExecution expectedExec = new NormalExecution(null,0);
      ExecutionOutcome actualExec = rhs.execute(inputs2, null);
      assertTrue("outcome should be normal execution", actualExec instanceof NormalExecution);
      NormalExecution actualNExec = (NormalExecution)actualExec;
      assertTrue("Expect void result and zero execution", expectedExec.getRuntimeValue() == actualNExec.getRuntimeValue() && expectedExec.getExecutionTime() == actualNExec.getExecutionTime());
      assertEquals("Expect value to have changed", 9, (int)f.getValue(inputs2[0]));
      
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
  public void testStaticFinalField() {
    Class<?> c = ClassWithFields.class;
    try {
      StaticFinalField f = new StaticFinalField(c.getField("FIVEFIELD"));
      try {
        @SuppressWarnings("unused")
        FieldSetter rhs = new FieldSetter(f);
        fail("IllegalArgumentException expected when static final field given to FieldSetter constructor");
      } catch (IllegalArgumentException e) {
        assertEquals("Argument exception","Field may not be static final for FieldSetter",e.getMessage());
      }
    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    }
  }
  
  @Test
  public void parseable() {
    String setterDesc = "<set>(int:randoop.ClassWithFields.oneField)";
    try {
      FieldSetter setter = FieldSetter.parse(setterDesc);
      assertEquals("parse should return object that converts to string", setterDesc, setter.toParseableString());
    } catch (StatementKindParseException e) {
     fail("Parse error: " + e.getMessage());
    }
    
  }

}
