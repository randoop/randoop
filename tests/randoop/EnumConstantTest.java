package randoop;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

/**
 * EnumConstantTest defines unit tests for {@link randoop.EnumConstant}.
 * 
 * @author bjkeller
 *
 */
public class EnumConstantTest {

  @Test
  public void parseConstraint() {
    String enumPair = "randoop.SimpleEnumForTests:THREE";
    try {
      EnumConstant ec = EnumConstant.parse(enumPair);
      assertEquals("parse(\"e:v\").toParseableString() should equal \"e:v\"", enumPair, ec.toParseableString());
      assertEquals("toString calls toParseableString", ec.toParseableString(), ec.toString());
    } catch (StatementKindParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }
  
  @Test
  public void parseErrors() {
    String missingColon = "randoop.SimpleEnumForTestsTHREE";
    String missingType = ":THREE";
    String missingValue = "randoop.SimpleEnumForTests:";
    String spaceInType = "Simple EnumForTests:THREE";
    String spaceInValue = "randoop.SimpleEnumForTests:THRE E";
    String badType = "SEFT:THREE";
    String badValue = "randoop.SimpleEnumForTests:FOUR";
    String nonEnum = "randoop.EnumConstantTest:FIVE";
    
    try {
      EnumConstant ec = EnumConstant.parse(missingColon);
    } catch (StatementKindParseException e) {
      String msg = "An enum constant description must be of the form \"" +
          "<type>:<value>" + " but description is \"" + missingColon + "\"";
      assertEquals("Expecting missing colon message",msg,e.getMessage());
    }
    
    String errorPrefix1 = "Error when parsing type-value pair "; 
    String errorPrefix2 = " for an enum description of the form <type>:<value>.";
    
    try {
      EnumConstant ec = EnumConstant.parse(missingType);
    } catch (StatementKindParseException e) {
      String msg = errorPrefix1 + missingType + errorPrefix2 + " No type given.";
      assertEquals("Expecting missing type message",msg,e.getMessage());
    }
    
    try {
      EnumConstant ec = EnumConstant.parse(missingValue);
    } catch (StatementKindParseException e) {
      String msg = errorPrefix1 + missingValue + errorPrefix2 + " No value given.";
      assertEquals("Expecting missing value message",msg,e.getMessage());
    }
    
    try {
      EnumConstant ec = EnumConstant.parse(spaceInType);
    } catch (StatementKindParseException e) {
      String msg = errorPrefix1 + spaceInType + errorPrefix2 + " The type has unexpected whitespace characters.";
      assertEquals("Expecting space in type message",msg,e.getMessage());
    }

    try {
      EnumConstant ec = EnumConstant.parse(spaceInValue);
    } catch (StatementKindParseException e) {
      String msg = errorPrefix1 + spaceInValue + errorPrefix2 + " The value has unexpected whitespace characters.";
      assertEquals("Expecting space in value message",msg,e.getMessage());
    }
    
    try {
      EnumConstant ec = EnumConstant.parse(badType);
    } catch (StatementKindParseException e) {
      String msg = errorPrefix1 + badType + errorPrefix2 + " The type given \"SEFT\" was not recognized.";
      assertEquals("Expecting bad type message",msg,e.getMessage());
    }
    try {
      EnumConstant ec = EnumConstant.parse(badValue);
    } catch (StatementKindParseException e) {
      String msg = errorPrefix1 + badValue + errorPrefix2 + " The value given \"FOUR\" is not a constant of the enum randoop.SimpleEnumForTests.";
      assertEquals("Expecting bad value message",msg,e.getMessage());
    }
    try {
      EnumConstant ec = EnumConstant.parse(nonEnum);
    } catch (StatementKindParseException e) {
      String msg = errorPrefix1 + nonEnum + errorPrefix2 + " The type given \"randoop.EnumConstantTest\" is not an enum.";
      assertEquals("Expecting nonenum message",msg,e.getMessage());
    }
  }
  
  @Test
  public void testInheritedMethods() {
    //skipping reflection
    EnumConstant ec1 = new EnumConstant(SimpleEnumForTests.ONE);
    EnumConstant ec1_2 = new EnumConstant(SimpleEnumForTests.ONE);
    EnumConstant ec2 = new EnumConstant(SimpleEnumForTests.TWO);
    
    //equals and hashcode
    assertEquals("Object built from same constant should be equal",ec1,ec1_2);
    assertFalse("Objects of different constants should not be equal",ec1.equals(ec2));
    assertEquals("Objects built from same constant should have same hashcode",ec1.hashCode(),ec1_2.hashCode());
    
    //types
    assertTrue("Should be no input types", ec1.getInputTypes().isEmpty());
    assertEquals("Output type should match enum type of constant",SimpleEnumForTests.ONE.getDeclaringClass(),ec1.getOutputType());
    
    //Execution
    NormalExecution exec = new NormalExecution(ec1.value(),0);
    NormalExecution actual = (NormalExecution)ec1.execute(new Object[0], null);
    assertTrue("Execution should be simply returning value",exec.getRuntimeValue().equals(actual.getRuntimeValue()) && exec.getExecutionTime() == actual.getExecutionTime());
    
    //code generation
    //need a sequence where variable lives
    String expected = "randoop.SimpleEnumForTests simpleEnumForTests0 = randoop.SimpleEnumForTests.TWO;" + Globals.lineSep;
    Sequence seq = new Sequence().extend(ec2, new ArrayList<Variable>());
    Variable var = new Variable(seq,0);
    StringBuilder b = new StringBuilder();
    ec2.appendCode(var, new ArrayList<Variable>(), b);
    assertEquals("Expect fully qualified initialization of variable by constant.",expected,b.toString());
  }

}
